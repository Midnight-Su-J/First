package UI;

import javax.swing.*;

public class RegisterFrame extends JFrame {
    public RegisterFrame() {
        //设置窗口标题
        this.setTitle("注册");
        //设置窗口大小
        this.setSize(488,500);
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
