package UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class RegisterFrame extends JFrame implements ActionListener {

    // 三个成员变量：用户名、密码、确认密码输入框
    private JTextField     usernameInput;
    private JPasswordField passwordInput;
    private JPasswordField confirmInput;

    // 两个按钮：返回、注册
    private JButton back;
    private JButton register;

    public RegisterFrame() {
        initJFrame();
        initRegister();
    }

    private void initJFrame() {
        this.setTitle("注册");
        BufferedImage frameIconBuf = null;
        try {
            frameIconBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/Icon.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setIconImage(frameIconBuf);
        this.setSize(488, 500);
        //取消默认布局管理器
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initRegister() {
        //用户名标签 + 输入框
        JLabel username = new JLabel("用户名");
        username.setForeground(Color.WHITE);
        username.setFont(new Font("黑体", Font.BOLD, 20));
        username.setBounds(100, 80, 100, 30);
        this.add(username);

        usernameInput = new JTextField(10);
        usernameInput.setBounds(180, 80, 200, 30);
        usernameInput.setForeground(Color.BLACK);
        usernameInput.setFont(new Font("宋体", Font.PLAIN, 16));
        this.add(usernameInput);

        //密码标签 + 输入框
        JLabel password = new JLabel("密码");
        password.setForeground(Color.WHITE);
        password.setFont(new Font("黑体", Font.BOLD, 20));
        password.setBounds(100, 140, 100, 30);
        this.add(password);

        passwordInput = new JPasswordField(10);
        passwordInput.setBounds(180, 140, 200, 30);
        passwordInput.setForeground(Color.BLACK);
        passwordInput.setFont(new Font("宋体", Font.PLAIN, 16));
        this.add(passwordInput);

        //确认密码标签 + 输入框
        JLabel confirm = new JLabel("确认密码");
        confirm.setForeground(Color.WHITE);
        confirm.setFont(new Font("黑体", Font.BOLD, 20));
        confirm.setBounds(100, 200, 100, 30);
        this.add(confirm);

        confirmInput = new JPasswordField(10);
        confirmInput.setBounds(180, 200, 200, 30);
        confirmInput.setForeground(Color.BLACK);
        confirmInput.setFont(new Font("宋体", Font.PLAIN, 16));
        this.add(confirmInput);

        //返回按钮
        back = new JButton("返回");
        back.setBounds(120, 300, 100, 30);
        back.setBackground(Color.GRAY);
        back.setBorderPainted(true);
        back.setForeground(Color.WHITE);
        back.setFont(new Font("黑体", Font.BOLD, 20));
        back.addActionListener(this);
        this.add(back);

        //注册按钮
        register = new JButton("注册");
        register.setBounds(270, 300, 100, 30);
        register.setBackground(Color.GREEN);
        register.setBorderPainted(true);
        register.setForeground(Color.WHITE);
        register.setFont(new Font("黑体", Font.BOLD, 20));
        register.addActionListener(this);
        this.add(register);

        //背景图片
        BufferedImage bgBuf = null;
        try {
            bgBuf = ImageIO.read(GameFrame.class.getResourceAsStream("/JigsawGame/image/sport/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel background = new JLabel(new ImageIcon(bgBuf));
        background.setBounds(0, 0, 488, 500);
        this.getContentPane().add(background);

        // 最后显示窗口
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == back) {
            // 点击返回按钮 → 关闭注册窗，回到登录窗
            this.dispose();
            new LoginFrame();
        } else if (e.getSource() == register) {
            // 点击注册按钮 → 注册校验
            registerCheck();
        }
    }

    /**
     * 注册校验逻辑
     * 用户名已存在          → 弹 "用户已存在"
     * 用户名不存在
     *     密码和确认密码不同 → 弹 "两次密码不一致"
     *     密码相同           → 存入 list，弹注册成功，关闭注册窗，回到登录窗
     */
    private void registerCheck() {
        //读取输入
        String inputName    = usernameInput.getText().trim();
        String inputPwd     = new String(passwordInput.getPassword()).trim();
        String inputConfirm = new String(confirmInput.getPassword()).trim();

        //判空
        if (inputName.isEmpty() || inputPwd.isEmpty() || inputConfirm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "用户名或密码不能为空！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        //检查用户名是否已存在
        for (user u : LoginFrame.list) {
            if (u.getUsername().equals(inputName)) {
                //用户名已存在
                JOptionPane.showMessageDialog(this,
                        "用户已存在！",
                        "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        //用户名不存在 → 检查两次密码是否一致
        if (!inputPwd.equals(inputConfirm)) {
            //两次密码不一致
            JOptionPane.showMessageDialog(this,
                    "两次密码不一致！",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE);
            passwordInput.setText("");
            confirmInput.setText("");
            return;
        }

        //用户名不存在 + 两次密码一致 → 注册成功
        LoginFrame.list.add(new user(inputName, inputPwd));

        JOptionPane.showMessageDialog(this,
                "注册成功！",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);

        // 关闭注册窗，回到登录窗
        this.dispose();
        new LoginFrame();
    }
}