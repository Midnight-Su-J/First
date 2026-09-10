import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageCutUtil {

    /**
     * 全部基于【项目根目录 JigsawGame】查找
     * @param sourceFileName 原图文件名，放在 image/ACG/folderName/ 下
     * @param folderName ACG01 / ACG02
     * @throws IOException 读取/写入图片发生IO异常
     */
    public static void processImage(String sourceFileName, String folderName) throws IOException {
        // 从JigsawGame根目录出发 image/ACG/ACG01
        String baseDirPath = "image/ACG/" + folderName;
        File outDir = new File(baseDirPath);

        // 打印调试：看程序实际找的路径
        System.out.println("输出文件夹完整路径：" + outDir.getAbsolutePath());

        if (!outDir.exists()) {
            outDir.mkdirs();
            System.out.println("文件夹不存在，自动创建");
        }

        File sourceFile = new File(outDir, sourceFileName);
        System.out.println("待读取原图完整路径：" + sourceFile.getAbsolutePath());
        System.out.println("文件是否存在：" + sourceFile.exists());

        BufferedImage originImg = ImageIO.read(sourceFile);
        if (originImg == null) {
            throw new IOException("读取图片失败！检查文件名、后缀，确认上面打印的路径文件真实存在");
        }

        int originW = originImg.getWidth();
        int originH = originImg.getHeight();

        // 1.居中裁剪1:1正方形
        int squareSize = Math.min(originW, originH);
        int xOffset = (originW - squareSize) / 2;
        int yOffset = (originH - squareSize) / 2;
        BufferedImage squareImage = originImg.getSubimage(xOffset, yOffset, squareSize, squareSize);

        // 2.缩放到420*420
        BufferedImage img420 = scaleImage(squareImage, 420, 420);
        File allFile = new File(outDir, "all.jpg");
        ImageIO.write(img420, "jpg", allFile);

        // 3.切分4*4，105*105，01~16.jpg
        int pieceSize = 105;
        int index = 1;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int subX = col * pieceSize;
                int subY = row * pieceSize;
                BufferedImage piece = img420.getSubimage(subX, subY, pieceSize, pieceSize);

                String fileName = String.format("%02d.jpg", index);
                File pieceFile = new File(outDir, fileName);
                ImageIO.write(piece, "jpg", pieceFile);
                index++;
            }
        }
        System.out.println("✅图片处理完成！");
    }


    private static BufferedImage scaleImage(BufferedImage source, int targetW, int targetH) {
        BufferedImage target = new BufferedImage(targetW, targetH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = target.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(source, 0, 0, targetW, targetH, null);
        g2d.dispose();
        return target;
    }


    public static void main(String[] args) {
        try {
            // =================只改这两行=================
            String inputPicName = "source.jpg";
            String puzzleFolder = "ACG01";
            // ============================================
            processImage(inputPicName, puzzleFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取拼图碎片工具，GameFrame可以直接调用
     * @param folderName ACG01
     * @return 16张图片数组
     * @throws IOException 读取/写入图片发生IO异常
     */
    public static BufferedImage[] loadPuzzlePieces(String folderName) throws IOException {
        BufferedImage[] pieces = new BufferedImage[16];
        String dir = "image/ACG/" + folderName + "/";
        for (int i = 0; i < 16; i++) {
            String name = String.format("%02d.jpg", i + 1);
            pieces[i] = ImageIO.read(new File(dir + name));
        }
        return pieces;
    }
}
