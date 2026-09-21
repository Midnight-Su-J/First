//导入Swing图形界面组件类
import javax.swing.*;
//导入AWT基础类（颜色、字体等）
import java.awt.*;
//导入动作事件类
import java.awt.event.ActionEvent;
//导入动作监听器接口
import java.awt.event.ActionListener;
//导入文件读取类
import java.io.BufferedReader;
//导入文件读取异常类
import java.io.IOException;
//导入标准字符集类，用于处理文件读取时的编码问题
import java.nio.charset.StandardCharsets;
//导入文件读取工具类，用于读取文件内容
import java.nio.file.Files;
//导入文件路径类，用于处理文件路径
import java.nio.file.Paths;
//导入哈希集合类，用于存储名字
import java.util.HashSet;
//导入有序不重复集合类
import java.util.LinkedHashSet;
//导入Set集合接口
import java.util.Set;

//作弊界面类：继承JFrame窗口并实现点击事件监听
public class Cheat extends JFrame implements ActionListener {
    //第一排输入框：填写不会被抽到的名字
    JTextField excludedField = new JTextField();
    //第二排名字输入框：填写第几次会被抽到的名字
    JTextField forcedNameField = new JTextField();
    //第二排次数输入框：填写第几次
    JTextField forcedCountField = new JTextField();

    //创建“确定”按钮
    JButton confirm = new JButton("确定");

    //空参构造方法
    public Cheat() {
        //调用方法初始化界面
        initUI();
    }

    //初始化作弊界面
    private void initUI() {
        //设置窗口标题
        setTitle("作弊");
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

        //第一排：添加排除名字输入框
        this.add(excludedField);
        //设置输入框位置和大小
        excludedField.setBounds(30, 170, 210, 36);
        //设置输入框字体
        excludedField.setFont(new Font("宋体", Font.PLAIN, 18));

        //第一排说明标签：不会被抽到
        JLabel excludedTip = new JLabel("不会被抽到");
        //把标签添加到窗口
        this.add(excludedTip);
        //设置标签位置和大小
        excludedTip.setBounds(248, 170, 130, 36);
        //设置标签字体
        excludedTip.setFont(new Font("宋体", Font.PLAIN, 18));

        //分隔符提示标签
        JLabel multiTip = new JLabel("多个名字用逗号分隔");
        //把提示添加到窗口
        this.add(multiTip);
        //设置提示位置和大小
        multiTip.setBounds(30, 210, 340, 22);
        //设置提示字体
        multiTip.setFont(new Font("宋体", Font.PLAIN, 14));
        //设置提示文字为灰色
        multiTip.setForeground(Color.GRAY);

        //第二排：添加必中名字输入框
        this.add(forcedNameField);
        //设置输入框位置和大小
        forcedNameField.setBounds(30, 260, 120, 36);
        //设置输入框字体
        forcedNameField.setFont(new Font("宋体", Font.PLAIN, 18));

        //“第”字标签
        JLabel labelDi = new JLabel("第");
        //把标签添加到窗口
        this.add(labelDi);
        //设置标签位置和大小
        labelDi.setBounds(157, 260, 30, 36);
        //设置标签字体
        labelDi.setFont(new Font("宋体", Font.PLAIN, 18));

        //第二排：添加次数输入框
        this.add(forcedCountField);
        //设置输入框位置和大小
        forcedCountField.setBounds(185, 260, 55, 36);
        //设置输入框字体
        forcedCountField.setFont(new Font("宋体", Font.PLAIN, 18));

        //“次会被抽到”标签
        JLabel labelCi = new JLabel("次会被抽到");
        //把标签添加到窗口
        this.add(labelCi);
        //设置标签位置和大小
        labelCi.setBounds(247, 260, 130, 36);
        //设置标签字体
        labelCi.setFont(new Font("宋体", Font.PLAIN, 18));

        //把确定按钮添加到窗口
        this.add(confirm);
        //设置按钮位置和大小
        confirm.setBounds(150, 400, 100, 30);
        //设置按钮字体
        confirm.setFont(new Font("宋体", Font.PLAIN, 20));
        //给确定按钮注册点击监听器
        confirm.addActionListener(this);

        //创建背景图标签
        JLabel background = new JLabel(new ImageIcon("src/main/resources/Image/background.jpg"));
        //设置背景铺满整个窗口
        background.setBounds(0, 0, 400, 600);
        //背景最后添加，显示在最底层
        this.add(background);

        //设置窗口可见
        setVisible(true);
    }

    //处理确定按钮点击事件
    @Override //重写接口方法
    public void actionPerformed(ActionEvent e) {
        //判断点击的是不是确定按钮
        if (e.getSource() == confirm) {
            //创建有序不重复集合，保存不会被抽到的名字
            Set<String> excludedNames = new LinkedHashSet<>();
            //按中英文逗号、顿号、空格切分第一排输入的名字
            String[] names = excludedField.getText().trim().split("[,，、\\s]+");
            //增强For循环逐个处理切出的名字
            for (String name : names) {
                //非空名字才加入集合
                if (!name.isEmpty()) {
                    //把名字加入排除集合（自动去重）
                    excludedNames.add(name);
                }
            }

            //读取第二排名字输入框内容并去掉空白
            String forcedName = forcedNameField.getText().trim();
            //读取第二排次数输入框内容并去掉空白
            String forcedCountText = forcedCountField.getText().trim();

            //名字和次数必须同时填写或同时为空
            if (forcedName.isEmpty() != forcedCountText.isEmpty()) {
                //弹出提示
                JOptionPane.showMessageDialog(this, "需要同时填写名字和次数");
                //不继续跳转
                return;
            }

            if (!forcedCountText.isEmpty()) {
                //判断名字是否在list.csv中或在排除集合中
                if (!Cheat.isNameValid(forcedName, excludedNames)) {
                    //弹出提示
                    JOptionPane.showMessageDialog(this, "名字不在名单中或不会被抽中");
                    //不继续跳转
                    return;
                }
            }

            //声明必中次数变量
            int forcedCount = -1;
            //次数文字非空时进行解析
            if (!forcedCountText.isEmpty()) { //次数非空判断
                //把文字解析成整数
                try {
                    forcedCount = Integer.parseInt(forcedCountText);
                } catch (NumberFormatException ex) {
                    //弹出提示
                    JOptionPane.showMessageDialog(this, "次数必须是整数");
                    //不继续跳转
                    return;
                }
                //次数必须大于0
                if (forcedCount < 1) {
                    //弹出提示
                    JOptionPane.showMessageDialog(this, "次数必须大于0");
                    //不继续跳转
                    return;
                }
            } else {
                //两个框都为空
                //把必中名字置为null，表示不设置必中
                forcedName = null;
            }

            //隐藏作弊界面
            setVisible(false);
            //显示抽签界面，并把作弊设置传过去
            new Choice(excludedNames, forcedName, forcedCount);
        }
    }

    // 缓存csv里的名单，只加载一次
    private static Set<String> csvNameSet = null;

    /**
     * 校验名字是否合法
     * @param name 要强制抽取的名字（已经trim）
     * @param excludedNames 界面传过来的排除名字集合 LinkedHashSet
     * @return true：名字存在csv 且 不在排除名单；false：无效
     */


    public static boolean isNameValid(String name, Set<String> excludedNames) {
        // 空名字直接无效
        if (name == null || name.isBlank()) {
            return false;
        }
        // 第一次调用加载csv名单
        if (csvNameSet == null) {
            loadCsvNames();
        }
        // 名字不在csv里 → 无效
        if (!csvNameSet.contains(name)) {
            return false;
        }
        // 名字在排除集合里 → 无效
        if (excludedNames.contains(name)) {
            return false;
        }
        // 全部校验通过
        return true;
    }

    // 加载list.csv文件里面全部名字，存入静态集合csvNameSet
    private static void loadCsvNames() {
        // 初始化HashSet，用来存放从csv读取到的所有有效名字，自动去重
        csvNameSet = new HashSet<>();
        // try-with-resources写法，BufferedReader用完会自动关闭流，不需要手动close
        // Paths.get("list.csv") 读取项目根目录下的list.csv文件
        // StandardCharsets.UTF_8 指定读取文件使用UTF-8编码，防止中文乱码
        try (BufferedReader br = Files.newBufferedReader(Paths.get("list.csv"), StandardCharsets.UTF_8)) {
            // 定义字符串变量line，用来接收每次读取的一行文本
            String line;
            // 循环读取文件每一行，readLine读到文件末尾返回null，循环终止
            while ((line = br.readLine()) != null) {
                // 去除这一行文本首尾的空格
                line = line.trim();
                // 如果去除空格后是空行，跳过本次循环，不处理空行
                if (line.isEmpty()) continue;
                // 按英文逗号分割当前这一行文字，得到名字数组（一行可写多个名字，逗号隔开）
                String[] arr = line.split(",");
                // 增强for循环，遍历分割后得到的每一个名字
                for (String n : arr) {
                    // 去除单个名字首尾多余空格
                    n = n.trim();
                    // 判断名字非空，才加入集合，避免空白字符串存入
                    if (!n.isEmpty()) {
                        // 将清洗完成的名字存入集合
                        csvNameSet.add(n);
                    }
                }
            }
        } catch (IOException e) {
            // 捕获文件读取异常：文件找不到、路径错误、权限不足等IO问题
            e.printStackTrace();
            // 打印异常堆栈信息，控制台查看错误原因
            // 读取失败时，重新初始化空集合，后续名字校验全部判定无效，保证程序不崩溃
            csvNameSet = new HashSet<>();
        }
    }

}
