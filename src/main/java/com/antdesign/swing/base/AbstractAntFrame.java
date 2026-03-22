package com.antdesign.swing.base;

import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.DarkAlgorithm;
import com.antdesign.swing.theme.DefaultAlgorithm;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.util.AppIconProvider;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Swing Ant Design 应用程序基类。
 *
 * <p>封装窗口初始化、主题安装、系统托盘等通用逻辑。
 * 子类只需覆写 {@link #initContent()} 来添加界面内容。
 *
 * <pre>{@code
 * public class MyApp extends AbstractAntFrame {
 *   public static void main(String[] args) {
 *     launch();
 *   }
 *
 *   @Override
 *   protected void initContent() {
 *     add(new JLabel("Hello"));
 *   }
 * }
 * }</pre>
 */
public abstract class AbstractAntFrame extends JFrame {

  private static final double DEFAULT_SCREEN_RATIO = 0.8;

  /**
   * 启动应用程序。在 main 方法中调用。
   */
  protected static void launch() {
    initDockIcon();
    System.setProperty("apple.awt.application.name", "Swing Ant Design");
    AntThemeManager.getInstance().install(AntTheme.defaultTheme());

    String callerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
    SwingUtilities.invokeLater(() -> {
      try {
        Class<?> clazz = Class.forName(callerClassName);
        AbstractAntFrame frame =
            (AbstractAntFrame) clazz.getDeclaredConstructor().newInstance();
        frame.initFrame();
        frame.setVisible(true);
        installSystemTray(frame);
      } catch (Exception e) {
        throw new RuntimeException("Failed to launch application", e);
      }
    });
  }

  private static void initDockIcon() {
    try {
      Image dockIcon = AppIconProvider.getLargeIcon();
      if (dockIcon != null && Taskbar.isTaskbarSupported()) {
        Taskbar taskbar = Taskbar.getTaskbar();
        if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
          taskbar.setIconImage(dockIcon);
        }
      }
    } catch (Exception ignored) {
      // Dock 图标设置失败不影响启动
    }
  }

  private void initFrame() {
    setTitle("Ant Design");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setIconImages(AppIconProvider.getWindowIcons());

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = (int) (screenSize.width * DEFAULT_SCREEN_RATIO);
    int height = (int) (screenSize.height * DEFAULT_SCREEN_RATIO);
    setSize(width, height);
    setLocationRelativeTo(null);

    initContent();
  }

  /**
   * 子类覆写此方法以添加界面内容。
   */
  protected void initContent() {
    // 默认空实现
  }

  // =========================================================================
  // 系统托盘
  // =========================================================================

  private static void installSystemTray(AbstractAntFrame frame) {
    if (!SystemTray.isSupported()) {
      return;
    }

    Image trayImage = AppIconProvider.getTrayIcon();
    if (trayImage == null) {
      return;
    }

    TrayIcon trayIcon = new TrayIcon(trayImage, "Ant Design");
    trayIcon.setImageAutoSize(true);

    JPopupMenu popup = createTrayPopup(frame, trayIcon);
    JDialog popupAnchor = createPopupAnchor();

    AntThemeManager.getInstance().addThemeChangeListener(theme -> {
      SwingUtilities.updateComponentTreeUI(popup);
      applyThemeToPopup(popup, theme);
    });

    // 初次应用当前主题
    applyThemeToPopup(popup, AntThemeManager.getInstance().getCurrentTheme());

    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // no-op
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        popupAnchor.setVisible(false);
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
        popupAnchor.setVisible(false);
      }
    });

    trayIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          restoreFrame(frame);
        }
        if (e.isPopupTrigger()) {
          showTrayPopup(popupAnchor, popup);
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showTrayPopup(popupAnchor, popup);
        }
      }
    });

    try {
      SystemTray.getSystemTray().add(trayIcon);
      frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    } catch (AWTException e) {
      // 托盘安装失败，保持 EXIT_ON_CLOSE
    }
  }

  /** 创建托盘右键弹出菜单。 */
  private static JPopupMenu createTrayPopup(
      AbstractAntFrame frame, TrayIcon trayIcon) {

    JPopupMenu popup = new JPopupMenu();

    JMenuItem showItem = new JMenuItem("显示");
    showItem.addActionListener(e -> restoreFrame(frame));
    popup.add(showItem);

    JMenuItem themeItem = new JMenuItem("切换主题");
    themeItem.addActionListener(e -> {
      boolean isDark = AntThemeManager.getInstance().isDark();
      AntThemeManager.getInstance().switchTheme(
          isDark ? new DefaultAlgorithm() : new DarkAlgorithm());
    });
    popup.add(themeItem);

    JMenuItem exitItem = new JMenuItem("退出");
    exitItem.addActionListener(e -> {
      SystemTray.getSystemTray().remove(trayIcon);
      frame.dispose();
      System.exit(0);
    });
    popup.add(exitItem);

    return popup;
  }

  /**
   * 将主题颜色强制应用到托盘弹出菜单。
   *
   * <p>因为托盘弹出菜单不在正常的 Window 组件树中，即使 UIDefaults 已更新，
   * 某些属性可能被 FlatLaf UI delegate 缓存。这里对每个子组件逐一设置颜色，
   * 确保视觉上 100% 跟随主题。
   */
  private static void applyThemeToPopup(JPopupMenu popup, AntTheme theme) {
    ColorToken ct = theme.getColorToken();
    Color bg = ct.getBgContainer();
    Color fg = ct.getTextColor();

    popup.setBackground(bg);
    popup.setForeground(fg);
    popup.setBorder(null);
    popup.setOpaque(true);

    for (Component comp : popup.getComponents()) {
      if (comp instanceof JMenuItem) {
        JMenuItem item = (JMenuItem) comp;
        item.setBackground(bg);
        item.setForeground(fg);
        item.setOpaque(true);
      } else if (comp instanceof JSeparator) {
        comp.setForeground(ct.getBorderSecondaryColor());
        comp.setBackground(bg);
      }
    }

    popup.revalidate();
    popup.repaint();
  }

  private static JDialog createPopupAnchor() {
    JDialog anchor = new JDialog();
    anchor.setUndecorated(true);
    anchor.setSize(1, 1);
    anchor.setAlwaysOnTop(true);
    anchor.setType(Window.Type.UTILITY);
    return anchor;
  }

  private static void restoreFrame(JFrame frame) {
    frame.setVisible(true);
    frame.setExtendedState(JFrame.NORMAL);
    frame.toFront();
    frame.requestFocus();
  }

  private static void showTrayPopup(JDialog anchor, JPopupMenu popup) {
    SwingUtilities.invokeLater(() -> {
      Point mouse = MouseInfo.getPointerInfo().getLocation();
      Dimension size = popup.getPreferredSize();
      anchor.setLocation(mouse.x, mouse.y - size.height);
      anchor.setVisible(true);
      popup.show(anchor, 0, 0);
    });
  }
}
