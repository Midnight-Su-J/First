package UI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class GameFrame extends JFrame implements KeyListener {
    //创建一个二维数组，用于存储图片序号
    int[][] date = new int[4][4];
    //定义两个变量，用于记录空白块的位置
    int x = 0;
    int y = 0;

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

        //添加菜单栏选项到菜单栏
        jMenuBar.add(functionMenu);
        jMenuBar.add(aboutMenu);

        //创建菜单栏选项的条目（功能：重新游戏、重新登录、退出游戏;关于：联系）
        JMenuItem replayItem = new JMenuItem("重新游戏");
        JMenuItem reloginItem = new JMenuItem("重新登录");
        JMenuItem exitItem = new JMenuItem("退出游戏");

        JMenuItem contactItem = new JMenuItem("联系");

        //添加菜单栏选项的条目到菜单栏选项
        functionMenu.add(replayItem);
        functionMenu.add(reloginItem);
        functionMenu.add(exitItem);

        aboutMenu.add(contactItem);
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
                date[i][j] = temArr[i*4+j];
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

        //加载图片
        for(int i = 0; i<4; i++){
            for (int j = 0; j<4; j++){
                //获取当前图片序号
                int number = date[i][j];

                //根据图片序号加载图片
                if(number < 10){
                //加载01.jpg-09.jpg
                JLabel imageLabel = new JLabel(new ImageIcon("JigsawGame/image/ACG/ACG01/0"+number+".jpg"));
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
                JLabel imageLabel = new JLabel(new ImageIcon("JigsawGame/image/ACG/ACG01/"+number+".jpg"));
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
        JLabel background = new JLabel(new ImageIcon("JigsawGame/image/sport/background.jpg"));
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

    }

    @Override
    public void keyReleased(KeyEvent e) {
        //判断上下左右方向键是否被按下
        //左：VK_LEFT = 37，上：VK_UP = 38，右：VK_RIGHT = 39，下：VK_DOWN = 40
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_LEFT){
            System.out.println("点击了左键");
            //判断是否可以向左移动
            if(y == 3){
                return;
            }
            //空白块儿右方图片的数字赋值给空白块儿
            date[x][y] = date[x][y+1];
            date[x][y+1] = 16;
            //更新空白块儿的角标
            y++;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_UP){
            System.out.println("点击了上键");
            //判断是否可以向上移动
            if(x == 3){
                return;
            }
            //空白块儿下方图片的数字赋值给空白块儿
            date[x][y] = date[x+1][y];
            date[x+1][y] = 16;
            //更新空白块儿的角标
            x++;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_RIGHT){
            System.out.println("点击了右键");
            //判断是否可以向右移动
            if(y == 0){
                return;
            }
            //空白块儿左方图片的数字赋值给空白块儿
            date[x][y] = date[x][y-1];
            date[x][y-1] = 16;
            //更新空白块儿的角标
            y--;
            //刷新图片
            initImage();
        }else if(keyCode == KeyEvent.VK_DOWN){
            System.out.println("点击了下键");
            //判断是否可以向下移动
            if(x == 0){
                return;
            }
            //空白块儿上方图片的数字赋值给空白块儿
            date[x][y] = date[x-1][y];
            date[x-1][y] = 16;
            //更新空白块儿的角标
            x--;
            //刷新图片
            initImage();
        }
    }
}
