//导入Swing工具类，用于把界面创建交给事件调度线程执行
import javax.swing.*;

//程序入口类
public class app {
    //主方法：程序启动入口
    public static void main(String[] args) {
        //把界面相关操作交给Swing事件调度线程执行，避免界面渲染异常
        SwingUtilities.invokeLater(() -> {
            //程序启动先扫描图片文件夹，用图片前缀名作为姓名自动生成list.csv
            Choice.generateListCsv();
            //创建并显示开始界面
            new Starter();
        });
    }
}
