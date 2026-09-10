package UI;

import javax.swing.*;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        //设置窗口标题
        this.setTitle("登录");
        //设置窗口大小
        this.setSize(488,430);
        //设置窗口置顶
        //this.setAlwaysOnTop(true);
        //设置窗口居中
        this.setLocationRelativeTo(null);
        //设置窗口关闭时退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口显示
        this.setVisible(true);
    }
}
