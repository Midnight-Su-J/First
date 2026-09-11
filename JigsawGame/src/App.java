import UI.GameFrame;
import UI.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        //把界面创建交给Swing EDT事件调度线程执行
        //避免渲染异常
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
