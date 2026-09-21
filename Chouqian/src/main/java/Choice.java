//导入Swing图形界面所需的组件类（窗口、按钮、标签等）
import javax.swing.*;
//导入AWT基础类（颜色、字体、图形绘制等）
import java.awt.*;
//导入动作事件类，用于处理按钮点击
import java.awt.event.ActionEvent;
//导入动作监听器接口
import java.awt.event.ActionListener;
//导入内存图像类，用于缩放图片和生成占位图
import java.awt.image.BufferedImage;
//导入文件类，用于表示文件和文件夹
import java.io.File;
//导入文件写入类，用于生成CSV名单文件
import java.io.FileWriter;
//导入URL类，兼容网络图片地址
import java.net.URL;
//导入字符集常量类
import java.nio.charset.StandardCharsets;
//导入文件读写工具类
import java.nio.file.Files;
//导入路径接口
import java.nio.file.Path;
//导入路径构造工具类
import java.nio.file.Paths;
//导入动态数组类
import java.util.ArrayList;
//导入数组工具类
import java.util.Arrays;
//导入List集合接口
import java.util.List;
//导入随机数类
import java.util.Random;
//导入Set集合接口
import java.util.Set;
//导入图片读写工具类
import javax.imageio.ImageIO;

//抽签窗口类：继承JFrame窗口并实现点击事件监听接口
public class Choice extends JFrame implements ActionListener {
    //名单CSV文件的路径
    private static final String CSV_PATH = "list.csv";
    //图片文件夹的路径，程序启动时自动扫描这个文件夹
    private static final String IMAGE_DIR = "src/main/resources/Image/list";
    //照片显示的边长（像素），照片会被缩放到这个正方形尺寸
    private static final int PHOTO_SIZE = 170;
    //滚动时画面刷新的间隔（毫秒），数值越小滚动越快
    private static final int ROLL_INTERVAL = 60;

    //作弊设置：不会被抽到的姓名集合
    private final Set<String> excludedNames;
    //作弊设置：第forcedCount次必被抽中的姓名（null表示不设置）
    private final String forcedName;
    //作弊设置：必中发生的第几次抽签
    private final int forcedCount;

    //从CSV读取到的姓名列表
    private final List<String> names = new ArrayList<>();
    //与姓名一一对应的头像图片列表
    private final List<ImageIcon> icons = new ArrayList<>();
    //每个人的权重数组：初始都是1，被抽中一次后减半
    private double[] weights;

    //已经完成的抽签次数
    private int drawCount = 0;
    //随机数生成器对象
    private final Random random = new Random();

    //是否正在滚动的状态标记
    private boolean rolling = false;
    //控制滚动刷新的Swing定时器
    private Timer rollTimer;
    //本次滚动中允许出现的人的下标数组
    private int[] flashIndices;
    //最终抽中人的下标
    private int finalIndex;

    //显示抽签次数和状态的标签（文字居中），初始为空白，只留标题
    private final JLabel countLabel = new JLabel("", SwingConstants.CENTER);
    //显示姓名的标签（文字居中），初始显示“？”
    private final JLabel nameLabel = new JLabel("？", SwingConstants.CENTER);
    //显示照片的标签
    private final JLabel photoLabel = new JLabel();
    //抽签/停止二合一按钮
    private final JButton drawButton = new JButton("抽签");
    //重置按钮
    private final JButton resetButton = new JButton("重置");

    // 全局占位图标，使用icon.jpg
    private ImageIcon placeholderIcon;



    //带作弊参数的构造方法：由Cheat作弊窗口调用
    public Choice(Set<String> excludedNames, String forcedName, int forcedCount) {
        // 加载原始占位图片
        Image rawPlaceholderImage = new ImageIcon("src/main/resources/Image/icon.jpg").getImage();
        // 调用公共缩放方法生成170*170占位图标
        placeholderIcon = scaleImageToPhotoSize(rawPlaceholderImage);

        //保存“不会被抽到”的姓名集合
        this.excludedNames = excludedNames;
        //保存“第N次必中”的姓名
        this.forcedName = forcedName;
        //保存“第N次必中”的次数
        this.forcedCount = forcedCount;
        //读取list.csv名单到内存
        loadNameList();
        //初始化并显示抽签窗口
        initUI();
    }

    //程序启动时调用：扫描图片文件夹，用图片前缀名作为姓名生成list.csv
    public static void generateListCsv() {
        //把图片文件夹路径包装成File对象
        File dir = new File(IMAGE_DIR);
        //列出文件夹里所有支持格式（jpg/jpeg/png/gif）的图片文件
        File[] files = dir.listFiles((folder, fileName) -> fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".gif"));
        //文件夹不存在或读取失败时files为null
        if (files == null) {
            //弹出提示框告知找不到图片文件夹
            JOptionPane.showMessageDialog(null, "找不到图片文件夹：" + IMAGE_DIR);
            //结束方法，不再生成CSV
            return;
        }
        //按文件名排序，保证每次生成的名单顺序一致
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        //创建UTF-8编码的文件写入器，try小括号会在结束后自动关闭它
        try (FileWriter writer = new FileWriter(CSV_PATH, StandardCharsets.UTF_8)) {
            //写入CSV表头行
            writer.write("姓名,图片地址\n");
            //遍历文件夹里的每一张图片
            for (File file : files) {
                //获取图片的文件名（含扩展名）
                String fileName = file.getName();
                //找到最后一个点的位置，用于去掉扩展名
                int dot = fileName.lastIndexOf('.');
                //截取点之前的部分作为姓名，没有点就用整个文件名
                String name = dot > 0 ? fileName.substring(0, dot) : fileName;
                //获取图片相对路径，并把Windows反斜杠统一替换成斜杠
                String path = file.getPath().replace('\\', '/');
                //写入一行CSV：姓名,图片地址
                writer.write(name + "," + path + "\n");
            }
        } catch (Exception e) {
            //生成CSV失败时弹出错误提示
            JOptionPane.showMessageDialog(null, "生成名单文件失败：" + e.getMessage()); //写入失败提示
        }
    }

    //读取list.csv名单文件，解析出姓名并加载对应照片
    private void loadNameList() {
        //把CSV路径包装成Path对象
        Path path = Paths.get(CSV_PATH);
        //文件不存在时给出明确提示
        if (!Files.exists(path)) {
            //弹出提示框
            JOptionPane.showMessageDialog(null, "找不到名单文件：" + CSV_PATH);
            //结束方法
            return;
        }
        //用UTF-8编码读取CSV全部行，try小括号自动关闭读取资源
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            //表头只可能出现在第一行，用标记判断
            boolean firstLine = true;
            //逐行解析CSV
            for (String raw : lines) {
                //去掉UTF-8 BOM头并去掉首尾空白
                String line = raw.replace("\uFEFF", "").trim();
                //空行直接跳过
                if (line.isEmpty()) {
                    //继续处理下一行
                    continue;
                }
                //第一行如果是表头（含“姓名”或英文name）则跳过
                if (firstLine && (line.contains("姓名") || line.toLowerCase().contains("name"))) {
                    //首行标记置为false
                    firstLine = false;
                    //跳过表头行
                    continue;
                }
                //无论是否表头，本行处理完后首行标记都失效
                firstLine = false;
                //找到第一个逗号的位置：姓名,图片地址
                int comma = line.indexOf(',');
                //没有逗号说明不是合法数据行
                if (comma < 0) {
                    //跳过这一行
                    continue;
                }
                //截取逗号之前的部分作为姓名并去掉空白
                String name = line.substring(0, comma).trim();
                //截取逗号之后的部分作为图片地址并去掉空白
                String imagePath = line.substring(comma + 1).trim();
                //姓名为空的行不处理
                if (name.isEmpty()) {
                    //跳过这一行
                    continue;
                }
                //把姓名加入姓名列表
                names.add(name);
                //加载对应照片并加入头像列表（与姓名一一对应）
                icons.add(loadIcon(imagePath, name));
            }
        } catch (Exception e) {
            //读取失败时弹出错误提示
            JOptionPane.showMessageDialog(null, "读取名单文件失败：" + e.getMessage());
        }
        //根据人数创建权重数组
        weights = new double[names.size()];
        //把每个人的权重都初始化为1
        Arrays.fill(weights, 1.0);
    }

    /**
     * 公共方法：把任意Image缩放裁切到 PHOTO_SIZE × PHOTO_SIZE，cover铺满模式
     * @param image 原始图片对象
     * @return 缩放裁切完成的ImageIcon
     */
    public static ImageIcon scaleImageToPhotoSize(Image image) {
        // 创建正方形内存画布，支持透明通道
        BufferedImage scaled = new BufferedImage(PHOTO_SIZE, PHOTO_SIZE, BufferedImage.TYPE_INT_ARGB);
        // 获取画布画笔
        Graphics2D g = scaled.createGraphics();
        // 设置平滑缩放抗锯齿
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 获取原图宽高
        int srcW = image.getWidth(null);
        int srcH = image.getHeight(null);

        // cover模式：放大/缩小铺满方框，超出区域裁剪
        double coverScale = Math.max((double) PHOTO_SIZE / srcW, (double) PHOTO_SIZE / srcH);
        int newW = (int) Math.round(srcW * coverScale);
        int newH = (int) Math.round(srcH * coverScale);

        // 居中绘制，超出画布的部分自动裁掉
        int drawX = (PHOTO_SIZE - newW) / 2;
        int drawY = (PHOTO_SIZE - newH) / 2;
        g.drawImage(image, drawX, drawY, newW, newH, null);

        // 释放画笔资源
        g.dispose();
        // 返回封装好的图标
        return new ImageIcon(scaled);
    }


    //加载头像图片，加载失败时返回icon.jpg占位图
    private ImageIcon loadIcon(String imagePath, String name) {
        //先声明图片对象
        Image image;
        //把图片路径包装成File对象
        File file = new File(imagePath);
        //判断本地文件是否存在
        if (file.isFile()) {
            //从本地文件读取图片
            image = readImageQuietly(file);
        } else {
            //尝试当作网络图片地址读取
            image = readImageFromUrl(imagePath);
        }
        //图片读取失败（返回null）时使用占位图icon.jpg
        if (image == null) {
            return placeholderIcon;
        }
        // 直接调用公共缩放方法，不需要重复写缩放代码
        return scaleImageToPhotoSize(image);
    }


    //从本地文件读取图片，出错时返回null
    private Image readImageQuietly(File file) {
        //用try-catch包住读取过程
        try {
            //调用ImageIO读取本地图片文件
            return ImageIO.read(file);
        } catch (Exception e) {
            //出错时返回null，调用方会改用占位图
            return null;
        }
    }

    //从网络地址读取图片，出错时返回null
    private Image readImageFromUrl(String imagePath) {
        //用try-catch包住读取过程
        try {
            //调用ImageIO按网络URL读取图片
            return ImageIO.read(new URL(imagePath));
        } catch (Exception e) {
            //出错时返回null，调用方会改用占位图
            return null;
        }
    }

    //初始化抽签窗口界面
    private void initUI() {
        //设置窗口标题
        setTitle("抽签");
        //设置窗口标题栏图标
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/main/resources/Image/icon.jpg"));
        //设置窗口大小为宽400高600
        setSize(400, 600);
        //设置窗口相对屏幕居中显示
        setLocationRelativeTo(null);
        //取消默认布局管理器，改用绝对坐标摆放组件
        setLayout(null);
        //设置点击窗口关闭按钮时退出整个程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //标题文字
        JLabel titleLabel = new JLabel("随机抽签");
        //把标题加入窗口
        this.add(titleLabel);
        //设置标题位置和大小
        titleLabel.setBounds(100, 60, 400, 50);
        //设置标题字体：宋体、加粗、50号
        titleLabel.setFont(new Font("宋体", Font.BOLD, 50));

        //把抽签次数标签加入窗口
        this.add(countLabel);
        //设置次数状态标签位置和大小（在标题下方）
        countLabel.setBounds(0, 120, 400, 34);
        //设置次数状态标签字体：宋体、普通、15号
        countLabel.setFont(new Font("宋体", Font.PLAIN, 15));

        //把姓名标签加入窗口
        this.add(nameLabel);
        //设置姓名标签位置和大小
        nameLabel.setBounds(0, 150, 400, 60);
        //设置姓名字体：宋体、加粗、30号
        nameLabel.setFont(new Font("宋体", Font.BOLD, 30));

        //把照片标签加入窗口
        this.add(photoLabel); //添加照片标签
        //设置照片位置（姓名正下方居中）和大小
        photoLabel.setBounds(115, 208, PHOTO_SIZE, PHOTO_SIZE);
        // 初始占位图使用icon.jpg
        photoLabel.setIcon(placeholderIcon);

        //把抽签按钮加入窗口
        this.add(drawButton);
        //设置抽签按钮位置和大小
        drawButton.setBounds(90, 400, 100, 40);
        //设置抽签按钮字体
        drawButton.setFont(new Font("宋体", Font.PLAIN, 20));
        //给抽签按钮注册点击监听器（由本类处理）
        drawButton.addActionListener(this);

        //把重置按钮加入窗口
        this.add(resetButton);
        //设置重置按钮位置和大小
        resetButton.setBounds(210, 400, 100, 40);
        //设置重置按钮字体
        resetButton.setFont(new Font("宋体", Font.PLAIN, 20));
        //给重置按钮注册点击监听器
        resetButton.addActionListener(this);

        //名单为空时禁用抽签按钮，避免空数组报错
            if (names.isEmpty()) {
            //禁用抽签按钮
            drawButton.setEnabled(false);
        }

        //创建背景图标签
        JLabel background = new JLabel(new ImageIcon("src/main/resources/Image/background.jpg"));
        //设置背景铺满整个窗口
        background.setBounds(0, 0, 400, 600);
        //背景最后添加，显示在最底层（先添加的组件在上层）
        this.add(background);

        //设置窗口可见
        setVisible(true);
    }

    //抽签按钮的两种状态：第一次点击开始滚动，第二次点击停止并确认结果
    private void toggleDraw() {
        //名单为空时不处理任何点击
        if (names.isEmpty()) {
            //直接结束方法
            return;
        }
        //判断当前是否处于滚动中
        if (!rolling) {
            //点击抽签即开始一次新的抽签，次数先加1
            drawCount++;
            //标题下方出现“第N次抽签中...”
            countLabel.setText("第 " + drawCount + " 次抽签中...");
            //把滚动状态标记为真
            rolling = true;
            //按钮文字改为“停止”，提示再点一次就停下
            drawButton.setText("停止");
            //滚动期间禁用重置按钮，避免状态错乱
            resetButton.setEnabled(false);
            //准备滚动时出现的人（所有人都参与滚动，包括被设置为不会抽中的人）
            flashIndices = buildFlashIndices();
            //创建定时器，每隔ROLL_INTERVAL毫秒刷新一次画面
            rollTimer = new Timer(ROLL_INTERVAL, e -> onRollTick());
            //启动定时器，姓名和照片开始滚动
            rollTimer.start(); //启动滚动
        } else {
            //停止定时器，画面不再自动切换
            rollTimer.stop();
            //把滚动状态标记为假
            rolling = false;
            //按钮文字恢复为“抽签”
            drawButton.setText("抽签");
            //按权重和作弊规则确定最终抽中人的下标（次数在开始时已经计数）
            finalIndex = decidePickedIndex();
            //被抽中者下次被抽中的权重减半
            weights[finalIndex] *= 0.5;
            //画面定格在最终抽中的人的姓名和照片
            showPerson(finalIndex);
            //显示“第N次抽签，抽中：”，下方是抽中人的姓名和照片
            countLabel.setText("第 " + drawCount + " 次抽签，抽中：");
            //重新启用重置按钮
            resetButton.setEnabled(true);
        }
    }

    //定时器每触发一次：随机切换一个人的姓名和照片
    private void onRollTick() {
        //从候选下标里随机取一个
        int idx = flashIndices[random.nextInt(flashIndices.length)];
        //把姓名和照片一起切换到这个人（两者同步变动）
        showPerson(idx);
    }

    //按作弊规则和权重计算最终抽中人的下标
    private int decidePickedIndex() {
        //查出必中姓名在名单中的下标（不存在时为-1）
        int forcedIndex = indexOfName(forcedName);
        //到了作弊设置的次数且指定姓名存在：直接抽中它
        if (forcedName != null && drawCount == forcedCount && forcedIndex >= 0) {
            //返回指定姓名的下标
            return forcedIndex;
        }
        //收集本次有权参与随机的人的下标
        List<Integer> eligible = new ArrayList<>();
        //统计合格人员的权重总和
        double totalWeight = 0;
        //遍历名单中的每一个人
        for (int i = 0; i < names.size(); i++) {
            if (excludedNames.contains(names.get(i))) {
                //跳过这个人
                continue;
            }
            //指定名字在第N次之前不能被随机抽走，否则无法保证第N次必中
            if (forcedName != null && i == forcedIndex && drawCount < forcedCount) {
                //跳过这个人
                continue;
            }
            //把合格下标加入列表
            eligible.add(i);
            //把这个人的权重累加到总和
            totalWeight += weights[i];
        }
        //兜底：所有人都被排除时随机取一人，避免程序无法继续
        if (eligible.isEmpty()) {
            //从全部名单中随机返回一个下标
            return random.nextInt(names.size());
        }
        //在[0,总权重)范围内取一个随机数
        double r = random.nextDouble() * totalWeight;
        //权重累加器
        double acc = 0;
        //按顺序累加每个人的权重
        for (int idx : eligible) {
            //累加当前人的权重
            acc += weights[idx];
            //随机落点落在当前人区间内则选中当前人
            if (r < acc) {
                //返回当前人的下标
                return idx;
            }
        }
        //浮点误差兜底：返回最后一个合格者
        return eligible.get(eligible.size() - 1);
    }

    //显示指定下标的人的姓名和照片
    private void showPerson(int index) {
        //把姓名标签文字换成这个人的名字
        nameLabel.setText(names.get(index));
        //把照片标签图标换成这个人的头像
        photoLabel.setIcon(icons.get(index));
    }

    //构建滚动时出现的人的下标数组：所有人都参与滚动
    //被设置为“不会被抽中”的人也会出现在滚动画面中，只是不会成为最终结果
    private int[] buildFlashIndices() {
        //创建长度等于人数的下标数组
        int[] all = new int[names.size()];
        //依次填入0到n-1的下标，让每个人都会在滚动中出现
        for (int i = 0; i < all.length; i++) {
            //填入当前下标
            all[i] = i;
        }
        //返回全量下标数组
        return all;
    }

    //按姓名在名单中查找下标，找不到返回-1
    private int indexOfName(String name) {
        //传入null直接返回-1
        if (name == null) {
            //返回-1表示不存在
            return -1;
        }
        //遍历名单逐一比对姓名
        for (int i = 0; i < names.size(); i++) {
            //姓名相等即找到
            if (names.get(i).equals(name)) {
                //返回当前下标
                return i;
            }
        }
        //遍历完没找到，返回-1
        return -1;
    }

    //处理窗口上所有按钮的点击事件
    @Override
    public void actionPerformed(ActionEvent e) {
        //判断点击来源是不是抽签按钮
        if (e.getSource() == drawButton) {
            //执行开始滚动/停止确认的切换逻辑
            toggleDraw();
        } else if (e.getSource() == resetButton) {
            //滚动过程中不允许重置
            if (rolling) {
                //直接结束方法
                return;
            }
            //次数清零
            drawCount = 0;
            //所有人物权重恢复为1
            Arrays.fill(weights, 1.0);
            //姓名恢复为“？”
            nameLabel.setText("？");
            // 重置占位图
            photoLabel.setIcon(placeholderIcon);
            //清空次数状态行
            countLabel.setText("");
        }
    }
}

