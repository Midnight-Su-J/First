# -*- coding: utf-8 -*-
"""
本地Web视频播放器后端服务
- 托管“当前媒体目录”下的静态文件（LocalWebPlayer.html / artplayer.min.js / 视频等）
- 支持 HTTP Range 分段请求（视频进度条拖动必需）
- /api/browse            浏览本机盘符/目录（供网页端选择文件夹）
- /api/set-root          切换当前媒体目录
- /api/videos            当前目录视频列表（含封面地址、当前目录路径）
- /.thumbnails/<d>/<f>.jpg 视频封面（ffmpeg 截帧，仅内存缓存，不在硬盘留文件）
- /api/remux             上传无法播放的视频，ffmpeg 无损转封装为 MP4(+faststart)

启动: python server.py
访问: http://localhost:8000
"""
import os
import re
import json
import shutil
import string
import hashlib
import subprocess
import tempfile
import threading
from collections import OrderedDict
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import unquote, urlparse

HOST = "0.0.0.0"  # HOST = "127.0.0.1",仅本机访问：本服务可切换本机目录，不应对局域网开放
PORT = 8000

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# 当前媒体目录（可通过 /api/set-root 切换），初始为脚本所在目录
MEDIA_ROOT = SCRIPT_DIR

MIME_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".js": "application/javascript; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".mp4": "video/mp4",
    ".webm": "video/webm",
    ".mkv": "video/x-matroska",
    ".m3u8": "application/vnd.apple.mpegurl",
    ".ts": "video/mp2t",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".gif": "image/gif",
    ".webp": "image/webp",
    ".ico": "image/x-icon",
    ".vtt": "text/vtt; charset=utf-8",
    ".json": "application/json; charset=utf-8",
}

# 浏览器可直接播放的视频扩展名（用于视频列表接口）
VIDEO_EXTS = {".mp4", ".webm", ".ogv", ".ogg", ".m3u8", ".mkv"}

# 允许转封装上传的视频扩展名
REMUX_EXTS = {".mp4", ".ts", ".m2ts", ".mts", ".mkv", ".flv", ".avi",
              ".mov", ".wmv", ".m4v", ".webm", ".ogv"}

# 浏览器 <video> 可直接解码的音视频编码（仅这些允许 -c copy 无损转封装）
COPY_VIDEO_CODECS = {"h264"}
COPY_AUDIO_CODECS = {"aac", "mp3"}

# 上传文件的临时存放区（程序目录下，文件名唯一）
TMP_DIR = os.path.join(SCRIPT_DIR, ".remux_tmp")

# 调用 ffmpeg 时的统一参数：
# - stdin 指向空设备，禁止子进程继承服务端 socket/管道
# - close_fds 关闭其余句柄继承（Windows 上若有第三方程序/并行业务的句柄被
#   子进程继承，communicate() 会等不到 EOF 而永久挂起）
# - CREATE_NO_WINDOW 避免后台服务运行时弹出控制台窗口
FF_SUBPROCESS_KWARGS = {"stdin": subprocess.DEVNULL, "close_fds": True}
if os.name == "nt":
    FF_SUBPROCESS_KWARGS["creationflags"] = getattr(
        subprocess, "CREATE_NO_WINDOW", 0)


def find_ffmpeg():
    """优先用 PATH 中的 ffmpeg，找不到则回退到 imageio-ffmpeg 自带的二进制。"""
    exe = shutil.which("ffmpeg")
    if exe:
        return exe
    try:
        import imageio_ffmpeg
        return imageio_ffmpeg.get_ffmpeg_exe()
    except Exception:
        return None


def root_hash(root):
    return hashlib.md5(os.path.normcase(root).encode("utf-8")).hexdigest()[:16]


def file_key(name):
    """视频文件名 → URL 中使用的稳定标识（不暴露真实文件名）。"""
    return hashlib.md5(name.encode("utf-8")).hexdigest()


def thumb_url(root, name):
    return "/.thumbnails/%s/%s.jpg" % (root_hash(root), file_key(name))


# ===== 封面只存内存 =====
# 不在视频文件夹、项目文件夹或系统临时目录落任何文件；
# 服务重启后缓存自然清空，首次浏览时后台线程重新截帧。
# OrderedDict 做 LRU：超过条目上限淘汰最久未访问的封面。
THUMB_MEM_MAX = 2000  # 单张 480px JPG 约 20~50KB，2000 张约占 40~100MB
_thumb_mem = OrderedDict()   # (目录哈希, 文件哈希) -> jpeg 字节
_thumb_lock = threading.Lock()


def thumb_memory_get(rkey, fkey):
    with _thumb_lock:
        data = _thumb_mem.pop((rkey, fkey), None)
        if data is not None:
            _thumb_mem[(rkey, fkey)] = data  # 提到末尾（最近使用）
        return data


def thumb_memory_put(rkey, fkey, data):
    with _thumb_lock:
        _thumb_mem[(rkey, fkey)] = data
        _thumb_mem.move_to_end((rkey, fkey))
        while len(_thumb_mem) > THUMB_MEM_MAX:
            _thumb_mem.popitem(last=False)


def thumb_memory_forget(rkey, fkey):
    with _thumb_lock:
        _thumb_mem.pop((rkey, fkey), None)


def generate_thumbnail(ffmpeg, src):
    """用 ffmpeg 截取视频中的真实画面帧生成 JPG 字节。
    依次尝试 3s、1s、首帧，避开开头黑屏；成功返回 bytes，失败返回 None。"""
    fd, tmp = tempfile.mkstemp(prefix="thumb_", suffix=".jpg")
    os.close(fd)
    try:
        for seek in ("3", "1", None):
            cmd = [ffmpeg, "-y", "-hide_banner", "-loglevel", "error"]
            if seek:
                cmd += ["-ss", seek]
            cmd += ["-i", src, "-frames:v", "1",
                    "-vf", "scale=480:-2", "-q:v", "4", tmp]
            try:
                r = subprocess.run(cmd, capture_output=True, timeout=120,
                                   **FF_SUBPROCESS_KWARGS)
            except subprocess.TimeoutExpired:
                continue
            if r.returncode == 0 and os.path.isfile(tmp) and os.path.getsize(tmp) > 0:
                with open(tmp, "rb") as f:
                    return f.read()
        return None
    finally:
        try:
            os.remove(tmp)
        except OSError:
            pass


def ensure_thumbnails_async(root, names):
    """后台线程逐个生成缺失的缩略图（写入内存），不阻塞列表接口。"""
    def worker():
        ffmpeg = find_ffmpeg()
        if not ffmpeg:
            return
        rkey = root_hash(root)
        for name in names:
            fkey = file_key(name)
            if thumb_memory_get(rkey, fkey) is not None:
                continue
            try:
                data = generate_thumbnail(ffmpeg, os.path.join(root, name))
            except Exception:
                data = None
            if data:
                thumb_memory_put(rkey, fkey, data)
    threading.Thread(target=worker, daemon=True).start()


def list_videos(root):
    return sorted(
        name for name in os.listdir(root)
        if os.path.isfile(os.path.join(root, name))
        and os.path.splitext(name)[1].lower() in VIDEO_EXTS
    )


def list_drives():
    """Windows 下列出所有存在的盘符；其他平台返回根目录。"""
    if os.name != "nt":
        return ["/"]
    drives = []
    for letter in string.ascii_uppercase:
        d = "%s:\\" % letter
        if os.path.exists(d):
            drives.append(d)
    return drives


class PlayerHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):
        ua = self.headers.get("User-Agent", "-")
        super().log_message("%s | UA=%s", fmt % args, ua[:90])

    def do_GET(self):
        self.serve(send_body=True)

    def do_HEAD(self):
        self.serve(send_body=False)

    def do_POST(self):
        path = self.path.split("?", 1)[0].split("#", 1)[0]
        if path == "/api/set-root":
            self.handle_set_root()
        elif path == "/api/remux":
            self.handle_remux()
        else:
            self.send_json(404, {"ok": False, "error": "未知接口"})

    def send_json(self, code, obj):
        data = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()
        try:
            self.wfile.write(data)
        except (ConnectionAbortedError, BrokenPipeError):
            pass

    def read_json_body(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
        except ValueError:
            length = 0
        if length <= 0 or length > 4096:
            return None
        try:
            return json.loads(self.rfile.read(length).decode("utf-8"))
        except Exception:
            return None

    # ---------------- 目录浏览 / 切换 ----------------

    def handle_browse(self, query):
        """GET /api/browse?path=xxx → {path, parent, canUp, drives, dirs:[...]}}"""
        global MEDIA_ROOT
        raw = unquote(query.get("path", [""])[0]).strip()
        path = raw if raw else MEDIA_ROOT
        path = os.path.abspath(path)
        if not os.path.isdir(path):
            self.send_json(404, {"ok": False, "error": "目录不存在: %s" % path})
            return

        dirs = []
        files = []
        try:
            for name in os.listdir(path):
                full = os.path.join(path, name)
                if os.path.isdir(full) and not name.startswith("."):
                    dirs.append(name)
                elif (os.path.isfile(full)
                      and os.path.splitext(name)[1].lower() in VIDEO_EXTS):
                    files.append(name)
        except OSError:
            pass
        dirs.sort(key=lambda s: s.lower())
        files.sort(key=lambda s: s.lower())

        if os.name == "nt":
            # 盘根目录（如 C:\）的上级仍是自身
            can_up = not re.match(r"^[A-Za-z]:\\?$", path)
        else:
            can_up = path != "/"
        parent = os.path.dirname(path) if can_up else None

        self.send_json(200, {
            "ok": True,
            "path": path,
            "parent": parent,
            "canUp": can_up,
            "drives": list_drives(),
            "dirs": dirs,
            "files": files,
        })

    def handle_set_root(self):
        """POST /api/set-root {"path": "..."} → 切换当前媒体目录。"""
        global MEDIA_ROOT
        body = self.read_json_body()
        if not body or "path" not in body:
            self.send_json(400, {"ok": False, "error": "参数错误"})
            return
        path = os.path.abspath(os.path.expanduser(str(body["path"]).strip()))
        if not os.path.isabs(path) or not os.path.isdir(path):
            self.send_json(400, {"ok": False, "error": "目录不存在: %s" % path})
            return
        MEDIA_ROOT = path
        names = list_videos(path)
        self.send_json(200, {
            "ok": True,
            "path": path,
            "videos": names,
        })

    # ---------------- 视频封面 ----------------

    def send_thumbnail_jpg(self, root, dir_key, fkey):
        """根据 (目录哈希, 文件名哈希) 返回缩略图 JPG（从内存缓存读取）。"""
        if root_hash(root) != dir_key:
            self.send_error(404, "缩略图不存在")
            return
        target_name = None
        for name in list_videos(root):
            if file_key(name) == fkey:
                target_name = name
                break
        if target_name is None:
            self.send_error(404, "缩略图不存在")
            return

        data = thumb_memory_get(dir_key, fkey)
        if data is None:
            ffmpeg = find_ffmpeg()
            if not ffmpeg:
                self.send_error(503, "未找到 ffmpeg，无法生成缩略图")
                return
            data = generate_thumbnail(ffmpeg, os.path.join(root, target_name))
            if not data:
                self.send_error(422, "无法从该视频截取画面")
                return
            thumb_memory_put(dir_key, fkey, data)
        self.send_response(200)
        self.send_header("Content-Type", "image/jpeg")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "max-age=86400")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        if self.command == "GET":
            try:
                self.wfile.write(data)
            except (ConnectionAbortedError, BrokenPipeError):
                pass

    # ---------------- ffmpeg 转封装 ----------------

    def handle_remux(self):
        """接收上传的视频 → ffmpeg 探测 → 无损转封装为 MP4(+faststart) → 输出到当前媒体目录。"""
        root = MEDIA_ROOT  # 成品放进当前视频列表所在文件夹
        ffmpeg = find_ffmpeg()
        if not ffmpeg:
            self.send_json(500, {
                "ok": False,
                "error": "未找到 ffmpeg，请先执行: pip install imageio-ffmpeg",
            })
            return

        raw_name = unquote(self.headers.get("X-Filename", ""))
        name = os.path.basename(raw_name)  # 只取文件名，杜绝目录穿越
        stem, ext = os.path.splitext(name)
        ext = ext.lower()
        if not name or ext not in REMUX_EXTS:
            self.send_json(400, {"ok": False, "error": "不支持的文件类型: %s" % name})
            return

        try:
            length = int(self.headers.get("Content-Length", 0))
        except ValueError:
            length = 0
        if length <= 0:
            self.send_json(400, {"ok": False, "error": "上传内容为空"})
            return

        os.makedirs(TMP_DIR, exist_ok=True)
        # 上传的源文件放程序目录临时区（文件名唯一，避免并发互相覆盖）
        src_fd, src_path = tempfile.mkstemp(prefix="upload_", suffix=ext,
                                            dir=TMP_DIR)
        os.close(src_fd)
        # 输出临时文件直接放“当前媒体目录”：
        # 之后 os.replace 是同盘改名，跨盘（如 C: 临时区 → D: 媒体目录）
        # 直接 os.replace 在 Windows 上会报 WinError 5 拒绝访问。
        out_path = None
        try:
            with open(src_path, "wb") as f:  # 流式落盘，避免大文件占内存
                remaining = length
                while remaining > 0:
                    chunk = self.rfile.read(min(1024 * 1024, remaining))
                    if not chunk:
                        break
                    f.write(chunk)
                    remaining -= len(chunk)
            if remaining > 0:
                self.send_json(400, {"ok": False, "error": "文件上传不完整"})
                return

            # 1) 探测编码
            probe = subprocess.run(
                [ffmpeg, "-hide_banner", "-i", src_path],
                capture_output=True, timeout=120,
                **FF_SUBPROCESS_KWARGS)
            info = probe.stderr.decode("utf-8", "ignore")
            m_v = re.search(r"Video:\s*(\w+)", info)
            m_a = re.search(r"Audio:\s*(\w+)", info)
            vcodec = m_v.group(1) if m_v else None
            acodec = m_a.group(1) if m_a else None
            if not vcodec:
                self.send_json(400, {"ok": False, "error": "无法识别视频流，文件可能已损坏"})
                return
            if vcodec not in COPY_VIDEO_CODECS:
                self.send_json(415, {
                    "ok": False,
                    "error": "视频编码为 %s，无损转封装后浏览器仍无法播放（需要 H.264）。"
                             % vcodec,
                })
                return

            # 2) 视频流无损 copy；音频兼容则 copy，否则转 AAC；+faststart
            try:
                out_fd, out_path = tempfile.mkstemp(prefix=".remux_",
                                                    suffix=".mp4.tmp",
                                                    dir=root)
                os.close(out_fd)
            except OSError:
                self.send_json(500, {
                    "ok": False,
                    "error": "无法写入当前视频文件夹（拒绝访问）: %s" % root,
                })
                return
            cmd = [ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
                   "-i", src_path, "-c:v", "copy"]
            if acodec is None:
                audio_action = "无音频流"
            elif acodec in COPY_AUDIO_CODECS:
                cmd += ["-c:a", "copy"]
                audio_action = "音频流无损保留(%s)" % acodec
            else:
                cmd += ["-c:a", "aac", "-b:a", "192k"]
                audio_action = "音频 %s 转为 AAC" % acodec
            # 显式 -f mp4：输出临时文件名后缀是 .mp4.tmp（防误关联/防直接点开），
            # ffmpeg 无法从扩展名推断封装格式，必须手动指定，否则报
            # "Unable to choose an output format"
            cmd += ["-f", "mp4", "-movflags", "+faststart", out_path]

            result = subprocess.run(cmd, capture_output=True, timeout=1800,
                                    **FF_SUBPROCESS_KWARGS)
            if result.returncode != 0 or not os.path.isfile(out_path):
                err = result.stderr.decode("utf-8", "ignore").strip()[-500:]
                self.send_json(500, {"ok": False, "error": "ffmpeg 封装失败: " + err})
                return

            # 3) 原名 .mp4 则同名替换；否则另存为 前缀名.mp4（原始上传文件仍在用户本机）
            target_name = name if ext == ".mp4" else stem + ".mp4"
            target_path = os.path.join(root, target_name)
            os.replace(out_path, target_path)
            out_path = None  # 已改名，finally 无需再清理

            # 同名文件内容已变，清除内存中的旧封面
            thumb_memory_forget(root_hash(root), file_key(target_name))

            self.send_json(200, {
                "ok": True,
                "name": target_name,
                "videoCodec": vcodec,
                "audio": audio_action,
                "renamed": target_name != name,
                "size": os.path.getsize(target_path),
            })
        except subprocess.TimeoutExpired:
            self.send_json(500, {"ok": False, "error": "ffmpeg 处理超时"})
        except Exception as e:
            self.send_json(500, {"ok": False, "error": "服务器错误: %s" % e})
        finally:
            # 清理上传源文件与输出临时文件（成品已改名的 out_path 为 None）
            for p in (src_path, out_path):
                try:
                    if p and os.path.isfile(p):
                        os.remove(p)
                except OSError:
                    pass

    # ---------------- 静态文件 / 接口 ----------------

    def serve(self, send_body):
        parsed = urlparse(self.path)
        path = unquote(parsed.path.split("#", 1)[0])
        root = MEDIA_ROOT  # 本次请求固定使用当前媒体目录（避免处理中途被切换）

        # 目录浏览接口
        if path == "/api/browse":
            from urllib.parse import parse_qs
            self.handle_browse(parse_qs(parsed.query))
            return

        # 缩略图：/.thumbnails/<目录哈希>/<文件名哈希>.jpg
        if path.startswith("/.thumbnails/") and path.lower().endswith(".jpg"):
            parts = path[len("/.thumbnails/"):-4].split("/")
            if len(parts) == 2:
                self.send_thumbnail_jpg(root, parts[0], parts[1])
            else:
                self.send_error(404, "缩略图不存在")
            return

        # 视频列表接口：{path, items:[{name, thumb}]}，后台预生成缩略图
        if path == "/api/videos":
            names = list_videos(root)
            ensure_thumbnails_async(root, names)
            data = json.dumps({
                "path": root,
                "items": [{"name": n, "thumb": thumb_url(root, n)} for n in names],
            }, ensure_ascii=False).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(data)))
            self.send_header("Cache-Control", "no-cache")
            self.end_headers()
            if send_body:
                try:
                    self.wfile.write(data)
                except (ConnectionAbortedError, BrokenPipeError):
                    pass
            return

        if path == "/":
            path = "/LocalWebPlayer.html"

        rel = path.lstrip("/")
        page_candidate = os.path.normpath(os.path.join(SCRIPT_DIR, rel))
        media_candidate = os.path.normpath(os.path.join(root, rel))

        # 程序自带资源（页面、artplayer、背景图等）始终从程序目录提供；
        # 其余文件（视频/图片等用户媒体）从当前媒体目录提供。
        # 这样切换媒体目录后，页面背景等内置资源依然正常。
        in_script = (page_candidate == SCRIPT_DIR
                     or page_candidate.startswith(SCRIPT_DIR + os.sep))
        in_media = (media_candidate == root
                    or media_candidate.startswith(root + os.sep))
        if in_script and os.path.isfile(page_candidate):
            file_path, base_dir = page_candidate, SCRIPT_DIR
        elif in_media and os.path.isfile(media_candidate):
            file_path, base_dir = media_candidate, root
        elif in_script:  # 路径合法但程序目录无此文件 → 404
            file_path, base_dir = page_candidate, SCRIPT_DIR
        else:
            self.send_error(403)
            return

        # 防止目录穿越攻击
        if file_path != base_dir and not file_path.startswith(base_dir + os.sep):
            self.send_error(403)
            return
        if not os.path.isfile(file_path):
            self.send_error(404, "File not found: %s" % path)
            return

        file_size = os.path.getsize(file_path)
        content_type = MIME_TYPES.get(
            os.path.splitext(file_path)[1].lower(), "application/octet-stream"
        )

        # 解析 Range 请求头
        start, end = 0, file_size - 1
        status = 200
        range_header = self.headers.get("Range")
        if range_header and file_size > 0:
            m = re.match(r"bytes=(\d*)-(\d*)", range_header)
            if m:
                start_s, end_s = m.group(1), m.group(2)
                if start_s:
                    start = int(start_s)
                    if end_s:
                        end = min(int(end_s), file_size - 1)
                elif end_s:
                    start = max(0, file_size - int(end_s))
                if start >= file_size or start > end:
                    self.send_response(416)
                    self.send_header("Content-Range", "bytes */%d" % file_size)
                    self.end_headers()
                    return
                status = 206

        length = end - start + 1
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(length))
        self.send_header("Accept-Ranges", "bytes")
        if status == 206:
            self.send_header("Content-Range", "bytes %d-%d/%d" % (start, end, file_size))
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()

        if not send_body:
            return

        with open(file_path, "rb") as f:
            f.seek(start)
            remaining = length
            while remaining > 0:
                chunk = f.read(min(64 * 1024, remaining))
                if not chunk:
                    break
                try:
                    self.wfile.write(chunk)
                except (ConnectionAbortedError, BrokenPipeError):
                    return
                remaining -= len(chunk)


def main():
    server = ThreadingHTTPServer((HOST, PORT), PlayerHandler)
    print("播放器服务已启动: http://localhost:%d" % PORT)
    print("媒体目录: %s（可在网页中通过“选择文件夹”切换）" % MEDIA_ROOT)
    print("按 Ctrl+C 停止服务")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n服务已停止")
        server.server_close()


if __name__ == "__main__":
    main()
