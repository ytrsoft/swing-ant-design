package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.AntIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Ant Design 通知提醒框。
 *
 * <p>对应 Ant Design {@code notification}，在页面右上角弹出通知，
 * 带标题和描述，支持图标和手动关闭。通过静态方法调用：
 *
 * <pre>{@code
 * AntNotification.success("成功", "操作已完成");
 * AntNotification.error("失败", "服务器异常，请稍后重试");
 * AntNotification.open("提示", "这是一条普通通知");
 * }</pre>
 */
public final class AntNotification {

  private static final int DEFAULT_DURATION = 4500;
  private static final int WIDTH = 384;
  private static final int GAP = 12;

  private static final ConcurrentLinkedQueue<JWindow> ACTIVE = new ConcurrentLinkedQueue<>();

  private AntNotification() {}

  // =========================================================================
  // 静态 API
  // =========================================================================

  /** 成功通知。 */
  public static void success(String title, String description) {
    show(title, description, Status.SUCCESS, DEFAULT_DURATION);
  }

  /** 错误通知。 */
  public static void error(String title, String description) {
    show(title, description, Status.ERROR, DEFAULT_DURATION);
  }

  /** 警告通知。 */
  public static void warning(String title, String description) {
    show(title, description, Status.WARNING, DEFAULT_DURATION);
  }

  /** 信息通知。 */
  public static void info(String title, String description) {
    show(title, description, Status.INFO, DEFAULT_DURATION);
  }

  /** 无图标通知。 */
  public static void open(String title, String description) {
    show(title, description, null, DEFAULT_DURATION);
  }

  /**
   * 显示通知。
   *
   * @param title       标题
   * @param description 描述
   * @param status      状态（{@code null} 表示无图标）
   * @param durationMs  显示时长
   */
  public static void show(String title, String description, Status status, int durationMs) {
    SwingUtilities.invokeLater(() -> {
      Window owner = findActiveWindow();
      if (owner == null) {
        return;
      }

      JWindow popup = new JWindow(owner);
      popup.setAlwaysOnTop(true);

      NotificationPanel panel = new NotificationPanel(title, description, status, () -> close(popup));
      popup.setContentPane(panel);
      popup.setSize(WIDTH, panel.getPreferredSize().height);

      ACTIVE.add(popup);
      repositionAll(owner);
      popup.setVisible(true);

      if (durationMs > 0) {
        Timer timer = new Timer(durationMs, e -> close(popup));
        timer.setRepeats(false);
        timer.start();
      }
    });
  }

  private static void close(JWindow popup) {
    SwingUtilities.invokeLater(() -> {
      ACTIVE.remove(popup);
      popup.dispose();
      Window owner = findActiveWindow();
      if (owner != null) {
        repositionAll(owner);
      }
    });
  }

  private static void repositionAll(Window owner) {
    int x = owner.getX() + owner.getWidth() - WIDTH - 24;
    int y = owner.getY() + 48;

    for (JWindow w : ACTIVE) {
      w.setLocation(x, y);
      y += w.getHeight() + GAP;
    }
  }

  private static Window findActiveWindow() {
    for (Window w : Window.getWindows()) {
      if (w.isVisible() && !(w instanceof JWindow)) {
        return w;
      }
    }
    return null;
  }

  // =========================================================================
  // 通知面板
  // =========================================================================

  private static class NotificationPanel extends AbstractAntPanel {

    NotificationPanel(String title, String description, Status status, Runnable onClose) {
      setOpaque(false);
      setLayout(new BorderLayout(12, 0));

      SizeToken st = sizeToken();
      setBorder(BorderFactory.createEmptyBorder(
          st.getPadding(), st.getPaddingLg(), st.getPadding(), st.getPaddingLg()));

      ColorToken ct = colorToken();
      FontToken ft = fontToken();

      // 图标
      if (status != null) {
        ImageIcon icon = AntIcons.filled(status.getDefaultIconName(), 24, status.getDefaultColor());
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        add(iconLabel, BorderLayout.WEST);
      }

      // 文本区
      JPanel textPanel = new JPanel();
      textPanel.setOpaque(false);
      textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

      JLabel titleLabel = new JLabel(title != null ? title : "");
      titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
      titleLabel.setForeground(ct.getTextColor());
      titleLabel.setAlignmentX(LEFT_ALIGNMENT);
      textPanel.add(titleLabel);

      if (description != null && !description.isEmpty()) {
        textPanel.add(Box.createVerticalStrut(4));
        JLabel descLabel = new JLabel(
            "<html><body style='width:260px'>" + description + "</body></html>");
        descLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
        descLabel.setForeground(ct.getTextSecondaryColor());
        descLabel.setAlignmentX(LEFT_ALIGNMENT);
        textPanel.add(descLabel);
      }

      add(textPanel, BorderLayout.CENTER);

      // 关闭按钮
      JLabel closeLabel = new JLabel("×");
      closeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
      closeLabel.setForeground(ct.getTextTertiaryColor());
      closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      closeLabel.setVerticalAlignment(JLabel.TOP);
      closeLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          onClose.run();
        }
      });
      add(closeLabel, BorderLayout.EAST);
    }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
      // 阴影
      for (int i = 6; i > 0; i--) {
        g2.setColor(new Color(0, 0, 0, 5 * (7 - i)));
        g2.fillRoundRect(i, i + 2, width - i * 2, height - i * 2, 8, 8);
      }

      ColorToken ct = colorToken();
      g2.setColor(ct.getBgElevated());
      g2.fillRoundRect(0, 0, width - 1, height - 1, 8, 8);
    }
  }
}
