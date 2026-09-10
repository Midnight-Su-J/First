package UI;

import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        //1.创建一个游戏主界面
        JFrame gameFrame = new JFrame();
        gameFrame.setTitle("拼图游戏");
        gameFrame.setSize(603,680);
        gameFrame.setVisible(true);

        //2.创建一个登录界面
        JFrame loginFrame = new JFrame();
        loginFrame.setTitle("登录");
        loginFrame.setSize(488,430);
        loginFrame.setVisible(true);

        //3.创建一个注册界面
        JFrame registerFrame = new JFrame();
        registerFrame.setTitle("注册");
        registerFrame.setSize(488,500);
        registerFrame.setVisible(true);
    }
}
