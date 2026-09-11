package UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class GameFrame extends JFrame implements KeyListener, ActionListener {
    //创建一个二维数组，用于存储图片序号
    int[][] data = new int[4][4];
    //定义两个变量，用于记录空白块的位置
    int x = 0;
    int y = 0;
    //定义一个变量，用于记录游戏图片路径
    String imagePath = "JigsawGame/image/ACG/ACG01/";
    //定义一个二维数组，用于记录完整图片的胜利状态
    int[][] winData = new int[][]{
            {1,2,3,4},
            {5,6,7,8},
            {9,10,11,12},
            {13,14,15,16}
    };
    //定义一个变量，用于记录步数
    int step = 0;
    //创建菜单栏选项的条目（功能：重新开始、重新登录、退出游戏;关于：联系）
    JMenuItem replayItem = new JMenuItem("重新开始");
    JMenuItem reloginItem = new JMenuItem("重新登录");
    JMenuItem exitItem = new JMenuItem("退出游戏");

    JMenuItem contactItem = new JMenuItem("联系");

    //创建更换图片选项的条目
    JMenuItem ACG01Item = new JMenuItem("ACG01");
    JMenuItem ACG02Item = new JMenuItem("ACG02");

    public GameFrame() {
        //初始化游戏窗口
        initJFrame();

        //初始化游戏菜单
        initJMenuBar();

        //初始化数据（打乱图片）
        initData();

        //初始化图片
        initImage();

        //设置窗口显示
        this.setVisible(true);
    }


    private void initJFrame() {
        //设置窗口标题
        this.setTitle("拼图游戏");
        //设置窗口图标
        BufferedImage iconImage = null;
        try {
            InputStream is = GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/Icon.png");
            if(is != null){
                iconImage = ImageIO.read(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setIconImage(iconImage);
        //设置窗口大小
        this.setSize(603,680);
        //设置窗口置顶
        //this.setAlwaysOnTop(true);
        //设置窗口居中
        this.setLocationRelativeTo(null);
        //设置窗口关闭时退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口布局为null，取消默认居中放置，手动设置组件位置
        this.setLayout(null);
        //添加键盘事件监听器
        this.addKeyListener(this);
    }

    private void initJMenuBar() {
        //初始化菜单
        //创建菜单栏
        JMenuBar jMenuBar = new JMenuBar();

        //添加菜单栏到窗口
        this.setJMenuBar(jMenuBar);

        //创建菜单栏选项（功能、关于）
        JMenu functionMenu = new JMenu("功能");
        JMenu aboutMenu = new JMenu("关于");

        //在功能下新增更换图片选项
        JMenu changeImage = new JMenu("更换图片");

        //添加菜单栏选项到菜单栏
        jMenuBar.add(functionMenu);
        jMenuBar.add(aboutMenu);

        //给条目添加事件监听器
        replayItem.addActionListener(this);
        reloginItem.addActionListener(this);
        exitItem.addActionListener(this);

        contactItem.addActionListener(this);

        //给更换图片选项的条目添加事件监听器
        ACG01Item.addActionListener(this);
        ACG02Item.addActionListener(this);

        //添加更换图片选项到功能菜单
        functionMenu.add(changeImage);

        //添加功能选项的条目到功能选项
        functionMenu.add(replayItem);
        functionMenu.add(reloginItem);
        functionMenu.add(exitItem);

        aboutMenu.add(contactItem);

        //把ACG01、ACG02添加到更换图片选项中
        changeImage.add(ACG01Item);
        changeImage.add(ACG02Item);
    }

    private void initData() {
        //定义一个一维数组
        int[] temArr = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        //随机打乱数组
        Random r = new Random();
        for(int i = 0; i<temArr.length; i++){
            //生成随机索引
            int index = r.nextInt(temArr.length);
            int temp = temArr[i];
            temArr[i] = temArr[index];
            temArr[index] = temp;
        }
        //给二维数组赋值
        for(int i = 0; i<4; i++){
            for (int j = 0; j<4; j++){
                //记录数组元素16即空白块的角标
                if(temArr[i*4+j] == 16){
                    x = i;
                    y = j;
                }
                data[i][j] = temArr[i*4+j];
            }
        }
    }

    private void initImage() {
        //创建一个图片ImageIcon的对象，用于加载图片
        //ImageIcon imageIcon01 = new ImageIcon("JigsawGame/image/ACG/ACG01/01.jpg");
        //创建一个图片JLabel的对象，用于显示图片ImageIcon
        //JLabel imageLabel01 = new JLabel(imageIcon01);
        //设置图片imageLabel的位置
        //imageLabel01.setBounds(0,0,105,105);
        //添加图片JLabel到窗口
        //this.add(imageLabel);
        //this.getContentPane().add(imageLabel01);

        //清空原本的图片
        this.getContentPane().removeAll();

        //判断是否胜利
        if(victory()){

            //显示胜利图片
            BufferedImage winBuf = null;
            try {
                winBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/win.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JLabel winImage = new JLabel(new ImageIcon(winBuf));
            winImage.setBounds(45,65,510,550);
            this.getContentPane().add(winImage);
        }

        //创建JLabel的对象，用于显示步数
        JLabel stepcount = new JLabel("步数："+step);
        //设置步数标签颜色、字体和大小：白色，黑体加粗，20号
        stepcount.setForeground(Color.WHITE);
        stepcount.setFont(new Font("黑体", Font.BOLD, 20));
        //设置步数标签位置
        stepcount.setBounds(50,60,100,20);
        this.getContentPane().add(stepcount);

        //加载图片
        for(int i = 0; i<4; i++){
            for (int j = 0; j<4; j++){
                //获取当前图片序号
                int number = data[i][j];

                //根据图片序号加载图片
                if(number < 10){
                //加载01.jpg-09.jpg
                    BufferedImage buf = null;
                    try {
                        buf = ImageIO.read(GameFrame.class.getResourceAsStream("/" + imagePath + "0" + number + ".jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    JLabel imageLabel = new JLabel(new ImageIcon(buf));
                    //设置图片imageLabel的位置
                imageLabel.setBounds(105*j+91,105*i+130,105,105);
                //给图片添加边框
                //RAISED = 0：凸起边框
                //LOWERED = 1：下陷边框
                imageLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
                //添加图片imageLabel到窗口
                this.getContentPane().add(imageLabel);
                }else if(number <= 15){
                //加载10.jpg-15.jpg
                    BufferedImage buf = null;
                    try {
                        buf = ImageIO.read(GameFrame.class.getResourceAsStream("/" + imagePath + number + ".jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    JLabel imageLabel = new JLabel(new ImageIcon(buf));

                    //设置图片imageLabel的位置
                imageLabel.setBounds(105*j+91,105*i+130,105,105);
                //给图片添加边框
                //RAISED = 0：凸起边框
                //LOWERED = 1：下陷边框
                imageLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
                //添加图片imageLabel到窗口
                this.getContentPane().add(imageLabel);
                }
            }
        }

        //添加背景图片
        BufferedImage bgBuf = null;
        try {
            bgBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel background = new JLabel(new ImageIcon(bgBuf));
        //设置背景图片的位置
        background.setBounds(0,0,603,680);
        this.getContentPane().add(background);

        //刷新窗口
        this.getContentPane().repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        //按住大写A键，显示完整图片
        //A:VK_A = 65
        if(e.getKeyCode() == KeyEvent.VK_A){
            //判断是否按住大写A键
            System.out.println("按住了大写A键,显示完整图片");
            //删除所有图片
            this.getContentPane().removeAll();
            //加载完整图片
            BufferedImage allBuf = null;
            try {
                allBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/" + imagePath + "all.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JLabel all = new JLabel(new ImageIcon(allBuf));
            //设置完整图片的位置
            all.setBounds(91,130,420,420);
            //添加完整图片到窗口
            this.getContentPane().add(all);
            //添加背景图片
            BufferedImage bgBufA = null;
            try {
                bgBufA = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/background.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JLabel background = new JLabel(new ImageIcon(bgBufA));
            //设置背景图片的位置
            background.setBounds(0,0,603,680);
            //添加背景图片到窗口
            this.getContentPane().add(background);
            //刷新窗口
            this.getContentPane().repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //胜利判定
        if(victory()){
            return;
        }

        //判断上下左右方向键是否被按下
        //左：VK_LEFT = 37，上：VK_UP = 38，右：VK_RIGHT = 39，下：VK_DOWN = 40
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_LEFT){
            System.out.println("点击了左键,向左移动");
            //判断是否可以向左移动
            if(y == 3){
                return;
            }
            //空白块儿右方图片的数字赋值给空白块儿
            data[x][y] = data[x][y+1];
            data[x][y+1] = 16;
            //更新空白块儿的角标
            y++;
            //步数增加1
            step++;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_UP){
            System.out.println("点击了上键,向上移动");
            //判断是否可以向上移动
            if(x == 3){
                return;
            }
            //空白块儿下方图片的数字赋值给空白块儿
            data[x][y] = data[x+1][y];
            data[x+1][y] = 16;
            //更新空白块儿的角标
            x++;
            //步数增加1
            step++;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_RIGHT){
            System.out.println("点击了右键,向右移动");
            //判断是否可以向右移动
            if(y == 0){
                return;
            }
            //空白块儿左方图片的数字赋值给空白块儿
            data[x][y] = data[x][y-1];
            data[x][y-1] = 16;
            //更新空白块儿的角标
            y--;
            //步数增加1
            step++;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_DOWN){
            System.out.println("点击了下键,向下移动");
            //判断是否可以向下移动
            if(x == 0){
                return;
            }
            //空白块儿上方图片的数字赋值给空白块儿
            data[x][y] = data[x-1][y];
            data[x-1][y] = 16;
            //更新空白块儿的角标
            x--;
            //步数增加1
            step++;
            //刷新图片
            initImage();
        }else if (keyCode == KeyEvent.VK_A){
            //恢复图片
            System.out.println("松开了A键,恢复图片");
            initImage();
        }else if (keyCode == KeyEvent.VK_W){
            //松开W键，直接胜利，W:VK_W = 87
            System.out.println("松开了W键,直接胜利");
            //将data数组重置为完整图片的胜利状态
            data = new int[][]{
                    {1,2,3,4},
                    {5,6,7,8},
                    {9,10,11,12},
                    {13,14,15,16}
            };
            //刷新图片
            initImage();
        }
    }

    public boolean victory(){
        //判断是否胜利
        for(int i = 0;i<4;i++){
            for(int j = 0;j<4;j++){
                if(data[i][j] != winData[i][j]){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //获取当前点击的菜单选项条目
        JMenuItem menuItem = (JMenuItem) e.getSource();
        //判断点击的是哪个条目
        if(menuItem == replayItem){
            //重新游戏
            System.out.println("点击了重新开始");
            //将步数重置为0
            step = 0;
            //再次打乱二维数组
            initData();
            //刷新图片
            initImage();
        }else if(menuItem == reloginItem){
            //重新登录
            System.out.println("点击了重新登录");
            //隐藏当前窗口，显示登录窗口
            this.setVisible(false);
            new LoginFrame();
        }else if(menuItem == exitItem){
            //退出游戏
            System.out.println("点击了退出游戏");
            System.exit(0);
        }else if(menuItem == contactItem){
            //联系
            System.out.println("点击了联系");
            //创建弹窗
            JDialog dialog = new JDialog();
            //设置弹窗标题
            dialog.setTitle("Gitee");
            //设置弹窗图标
            BufferedImage dialogIconBuf = null;
            try {
                dialogIconBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/Icon.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dialog.setIconImage(dialogIconBuf);
            //创建一个图片容器
            dialog.setModal(true);
            BufferedImage aboutBuf = null;
            try {
                aboutBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/about.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JLabel label = new JLabel(new ImageIcon(aboutBuf));
            //设置图片容器的位置和大小
            label.setBounds(0,0,380,560);
            //将图片容器添加到弹窗中
            dialog.add(label);
            //设置弹窗大小
            dialog.setSize(380,560);
            //设置弹窗位置
            dialog.setLocationRelativeTo(null);
            //设置弹框不关闭则不能操作其他窗口
            dialog.setModal(true);
            //设置弹窗可见
            dialog.setVisible(true);
        }else if(menuItem == ACG01Item){
            //更换图片为ACG01
            System.out.println("点击了更换图片为ACG01");
            //将步数重置为0
            step = 0;
            //再次打乱二维数组
            initData();
            //将图片路径设置为ACG01的图片
            imagePath = "JigsawGame/image/ACG/ACG01/";
            //刷新图片
            initImage();
        }else if(menuItem == ACG02Item){
            //更换图片为ACG02
            System.out.println("点击了更换图片为ACG02");
            imagePath = "JigsawGame/image/ACG/ACG02/";
            //将步数重置为0
            step = 0;
            //再次打乱二维数组
            initData();
            //刷新图片
            initImage();
        }
    }
}