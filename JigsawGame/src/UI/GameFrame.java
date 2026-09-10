package UI;

import javax.swing.*;
import java.util.Random;

public class GameFrame extends JFrame {
    //创建一个二维数组，用于存储图片序号
    int[][] date = new int[4][4];

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
    }

    private void initJMenuBar() {
        //初始化菜单
        //创建菜单栏
        JMenuBar jMenuBar = new JMenuBar();

        //添加菜单栏到窗口
        this.setJMenuBar(jMenuBar);

        //创建菜单栏选项（功能、关于我们）
        JMenu functionMenu = new JMenu("功能");
        JMenu aboutMenu = new JMenu("关于我们");

        //添加菜单栏选项到菜单栏
        jMenuBar.add(functionMenu);
        jMenuBar.add(aboutMenu);

        //创建菜单栏选项的条目（功能：重新游戏、重新登录、退出游戏;关于我们：联系方式）
        JMenuItem replayItem = new JMenuItem("重新游戏");
        JMenuItem reloginItem = new JMenuItem("重新登录");
        JMenuItem exitItem = new JMenuItem("退出游戏");

        JMenuItem contactItem = new JMenuItem("联系方式");

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

        for(int i = 0; i<4; i++){
            for (int j = 0; j<4; j++){
                //获取当前图片序号
                int number = date[i][j];

                //根据图片序号加载图片
                if(number < 10){
                JLabel imageLabel = new JLabel(new ImageIcon("JigsawGame/image/ACG/ACG01/0"+number+".jpg"));
                imageLabel.setBounds(105*j,105*i,105,105);
                this.getContentPane().add(imageLabel);
                }else if(number <= 15){
                JLabel imageLabel = new JLabel(new ImageIcon("JigsawGame/image/ACG/ACG01/"+number+".jpg"));
                imageLabel.setBounds(105*j,105*i,105,105);
                this.getContentPane().add(imageLabel);
                }
            }
        }
    }
}
