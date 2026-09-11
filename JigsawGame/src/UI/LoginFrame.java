package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class LoginFrame extends JFrame implements ActionListener {
    //创建集合存储用户名和密码
    static ArrayList<user> list = new ArrayList <>();
    static {
        list.add(new user("Midnight_Su", "123456"));
    }

    //添加用户名输入框
    JTextField usernameInput = new JTextField(10);

    //添加密码输入框
    JPasswordField passwordInput = new JPasswordField(10);

    //添加注册按钮
    JButton register = new JButton("注册");

    //添加登录按钮
    JButton login = new JButton("登录");

    public LoginFrame() {
        //初始化窗口
        initJFrame();
        //初始化登录面板
        initLogin();
    }

    //初始化窗口
    private void initJFrame() {
        //设置窗口标题
        this.setTitle("登录");
        //设置窗口图标
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("JigsawGame/image/sport/Icon.png"));
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

    private void initLogin() {
        //添加用户名标签
        JLabel username = new JLabel("用户名");
        //设置用户名标签颜色、字体和大小：白色，黑体加粗，20号
        username.setForeground(Color.WHITE);
        username.setFont(new Font("黑体", Font.BOLD, 20));
        //设置用户名标签位置
        username.setBounds(100, 100, 100, 30);
        //将用户名标签添加到登录面板中
        this.add(username);

        //设置用户名输入框位置
        usernameInput.setBounds(180, 100, 200, 30);
        this.add(usernameInput);
        //设置输入框颜色、字体和大小：黑色，宋体字体，16号
        usernameInput.setForeground(Color.BLACK);
        usernameInput.setFont(new Font("宋体", Font.PLAIN, 16));

        //添加密码标签
        JLabel password = new JLabel("密码");
        //设置密码标签颜色、字体和大小：白色，黑体加粗，20号
        password.setForeground(Color.WHITE);
        password.setFont(new Font("黑体", Font.BOLD, 20));
        //设置密码标签位置
        password.setBounds(100, 150, 100, 30);
        //将密码标签添加到登录面板中
        this.add(password);

        //设置密码输入框位置
        passwordInput.setBounds(180, 150, 200, 30);
        this.add(passwordInput);
        //设置输入框颜色、字体和大小：黑色，宋体字体，16号
        passwordInput.setForeground(Color.BLACK);
        passwordInput.setFont(new Font("宋体", Font.PLAIN, 16));

        //设置注册按钮位置
        register.setBounds(120, 250, 100, 30);
        this.add(register);
        //设置注册按钮背景颜色和样式：橙色，有边框
        register.setBackground(Color.ORANGE);
        register.setBorderPainted(true);
        //设置注册按钮颜色、字体和大小：白色，黑体加粗，20号
        register.setForeground(Color.WHITE);
        register.setFont(new Font("黑体", Font.BOLD, 20));
        //添加注册按钮点击事件监听器
        register.addActionListener(this);

        //设置登录按钮位置
        login.setBounds(270, 250, 100, 30);
        this.add(login);
        //设置登录按钮背景颜色和样式：绿色，有边框
        login.setBackground(Color.GREEN);
        login.setBorderPainted(true);
        //设置登录按钮颜色、字体和大小：白色，黑体加粗，20号
        login.setForeground(Color.WHITE);
        login.setFont(new Font("黑体", Font.BOLD, 20));
        //添加登录按钮点击事件监听器
        login.addActionListener(this);

        //添加窗口背景图片
        JLabel background = new JLabel(new ImageIcon("JigsawGame/image/sport/background.jpg"));
        background.setBounds(0, 0, 488, 430);
        this.getContentPane().add(background);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //判断点击的是哪个按钮
        if (e.getSource() == register) {
            //点击的是注册按钮
            System.out.println("点击的是注册按钮");
            //隐藏登录面板
            this.setVisible(false);
            //显示注册面板
            new RegisterFrame();
        } else if (e.getSource() == login) {
            //点击的是登录按钮
            System.out.println("点击的是登录按钮");
            //登录检测
            loginCheck();
        }
    }
    private void loginCheck() {
        //读取输入
        String inputName = usernameInput.getText().trim();
        String inputPwd  = new String(passwordInput.getPassword()).trim();

        //判空
        if (inputName.isEmpty() || inputPwd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "用户名或密码不能为空！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        //遍历比对
        for (user u : list) {
            if (u.getUsername().equals(inputName)) {
                if (u.getPassword().equals(inputPwd)) {
                    //登录成功
                    this.dispose();
                    new GameFrame();
                } else {
                    //密码错误
                    JOptionPane.showMessageDialog(this,
                            "密码错误！",
                            "登录失败",
                            JOptionPane.ERROR_MESSAGE);
                    passwordInput.setText("");
                }
                return;
            }
        }

        //用户名不存在
        JOptionPane.showMessageDialog(this,
                "用户不存在，请注册！",
                "登录失败",
                JOptionPane.ERROR_MESSAGE);
    }
}
