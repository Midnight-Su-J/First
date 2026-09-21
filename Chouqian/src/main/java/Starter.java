//导入Swing图形界面组件类
import javax.swing.*;
//导入AWT基础类（字体、工具箱等）
import java.awt.*;
//导入动作事件类
import java.awt.event.ActionEvent;
//导入动作监听器接口
import java.awt.event.ActionListener;

//开始界面类：继承JFrame窗口并实现点击事件监听
public class Starter extends JFrame implements ActionListener {
    //创建“开始”按钮
    JButton start = new JButton("开始");

    //空参构造方法
    public Starter() {
        //调用方法初始化界面
        initUI();
    }

    //初始化开始界面
    private void initUI() {
        //设置窗口标题
        setTitle("随机抽签");
        //设置窗口标题栏图标
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/main/resources/Image/icon.jpg"));
        //设置窗口大小为宽400高600
        setSize(400, 600);
        //设置窗口相对屏幕居中显示
        setLocationRelativeTo(null);
        //取消默认布局管理器，改用绝对坐标摆放
        setLayout(null);
        //设置点击关闭按钮时退出程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //标题文字
        JLabel titleLabel = new JLabel("随机抽签");
        //把标题加入窗口
        this.add(titleLabel);
        //设置标题位置和大小
        titleLabel.setBounds(100, 100, 400, 50);
        //设置标题字体：宋体、加粗、50号
        titleLabel.setFont(new Font("宋体", Font.BOLD, 50));


        //添加图片
        Image rawImg = new ImageIcon("src/main/resources/Image/icon.jpg").getImage();
        //缩放图片到窗口大小
        JLabel imageLabel = new JLabel(Choice.scaleImageToPhotoSize(rawImg));
        //把图片加入窗口
        this.add(imageLabel);
        //设置图片位置和大小
        imageLabel.setBounds((400-170)/2, 200, 170, 170);


        //把开始按钮添加到窗口
        this.add(start);
        //设置按钮位置和大小
        start.setBounds(150, 400, 100, 40);
        //设置按钮字体：宋体、普通、20号
        start.setFont(new Font("宋体", Font.PLAIN, 30));
        //给开始按钮注册点击监听器
        start.addActionListener(this);

        //创建背景图标签
        JLabel background = new JLabel(new ImageIcon("src/main/resources/Image/background.jpg"));
        //设置背景铺满整个窗口
        background.setBounds(0, 0, 400, 600);
        //背景最后添加，显示在最底层
        this.add(background);

        //设置窗口可见
        setVisible(true);
    }

    //处理按钮点击事件
    @Override
    public void actionPerformed(ActionEvent e) {
        //判断点击的是不是开始按钮
        if (e.getSource() == start) {
            //隐藏开始界面
            setVisible(false);
            //显示作弊界面
            new Cheat();
        }
    }
}
