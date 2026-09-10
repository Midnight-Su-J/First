package UI;

import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        //1.创建一个游戏主界面
        JFrame gameFrame = new JFrame();
        gameFrame.setTitle("拼图游戏");
        gameFrame.setSize(603,680);
        //设置界面居中显示
        gameFrame.setLocationRelativeTo(null);
        //设置界面关闭模式
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //取消默认容器的默认居中布局
        gameFrame.getContentPane().setLayout(null);

        //创建一个按钮对象
        JButton Button = new JButton("监听");
        //设置按钮的位置和大小
        Button.setBounds(100,100,100,30);
        //给按键添加事件监听器
        //实现ActionListener接口，重写actionPerformed方法
        Button.addActionListener(e -> {
            System.out.println("点击了按钮");
        });
        //添加按钮到窗口
        gameFrame.getContentPane().add(Button);

        //设置界面可见
        gameFrame.setVisible(true);

        //2.创建一个登录界面
        //JFrame loginFrame = new JFrame();
        //loginFrame.setTitle("登录");
        //loginFrame.setSize(488,430);
        //loginFrame.setVisible(true);

        //3.创建一个注册界面
        //JFrame registerFrame = new JFrame();
        //registerFrame.setTitle("注册");
        //registerFrame.setSize(488,500);
        //registerFrame.setVisible(true);
    }
}
