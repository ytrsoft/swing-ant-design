package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.util.AntIcons;
import com.antdesign.swing.util.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Ant Design 全局消息提示。
 *
 * <p>对应 Ant Design {@code message}，在页面顶部居中展示轻量级反馈信息，
 * 自动消失，不打断用户操作。通过静态方法调用：
 *
 * <pre>{@code
 * AntMessage.success("操作成功");
 * AntMessage.error("操作失败");
 * AntMessage.warning("警告信息");
 * AntMessage.info("提示信息");
 * AntMessage.loading("加载中...");
 * AntMessage.success("自定义时长", 5000);
 * }</pre>
 *
 * @see Status
 */
public final class AntMessage {

  /** 默认显示时长 (ms)。 */
  private static final int DEFAULT_DURATION = 3000;

  /** 消息之间的垂直间距 (px)。 */
  private static final int GAP = 8;

  /** 当前显示中的消息窗口队列。 */
  private static final ConcurrentLinkedQueue<JWindow> ACTIVE_MESSAGES = new ConcurrentLinkedQueue<>();

  private AntMessage() {}

  // =========================================================================
  // 静态 API
  // =========================================================================

  /** 成功消息。 */
  public static void success(String content) {
    show(content, Status.SUCCESS, DEFAULT_DURATION);
  }

  /** 成功消息（自定义时长）。 */
  public static void success(String content, int durationMs) {
    show(content, Status.SUCCESS, durationMs);
  }

  /** 错误消息。 */
  public static void error(String content) {
    show(content, Status.ERROR, DEFAULT_DURATION);
  }

  /** 错误消息（自定义时长）。 */
  public static void error(String content, int durationMs) {
    show(content, Status.ERROR, durationMs);
  }

  /** 警告消息。 */
  public static void warning(String content) {
    show(content, Status.WARNING, DEFAULT_DURATION);
  }

  /** 警告消息（自定义时长）。 */
  public static void warning(String content, int durationMs) {
    show(content, Status.WARNING, durationMs);
  }

  /** 信息消息。 */
  public static void info(String content) {
    show(content, Status.INFO, DEFAULT_DURATION);
  }

  /** 信息消息（自定义时长）。 */
  public static void info(String content, int durationMs) {
    show(content, Status.INFO, durationMs);
  }

  /** 加载消息，不自动关闭，返回关闭句柄。 */
  public static Runnable loading(String content) {
    final JWindow[] ref = new JWindow[1];
    SwingUtilities.invokeLater(() -> {
      Window owner = findActiveWindow();
      if (owner == null) {
        return;
      }

      JWindow popup = new JWindow(owner);
      popup.setAlwaysOnTop(true);
      ref[0] = popup;

      LoadingPanel panel = new LoadingPanel(content);
      popup.setContentPane(panel);
      popup.pack();

      repositionAll(popup, owner);
      popup.setVisible(true);
      ACTIVE_MESSAGES.add(popup);
    });

    // 返回关闭句柄
    return () -> {
      SwingUtilities.invokeLater(() -> {
        if (ref[0] != null) {
          close(ref[0]);
        }
      });
    };
  }

  // =========================================================================
  // 核心实现
  // =========================================================================

  /**
   * 显示一条消息。
   *
   * @param content    消息内容
   * @param status     状态类型
   * @param durationMs 显示时长 (ms)
   */
  public static void show(String content, Status status, int durationMs) {
    SwingUtilities.invokeLater(() -> {
      Window owner = findActiveWindow();
      if (owner == null) {
        return;
      }

      JWindow popup = new JWindow(owner);
      popup.setAlwaysOnTop(true);

      MessagePanel panel = new MessagePanel(content, status);
      popup.setContentPane(panel);
      popup.pack();

      // 定位到屏幕顶部居中
      repositionAll(popup, owner);

      popup.setVisible(true);
      ACTIVE_MESSAGES.add(popup);

      // 自动关闭定时器
      Timer timer = new Timer(durationMs, e -> close(popup));
      timer.setRepeats(false);
      timer.start();
    });
  }

  /** 关闭指定消息窗口。 */
  private static void close(JWindow popup) {
    SwingUtilities.invokeLater(() -> {
      ACTIVE_MESSAGES.remove(popup);
      popup.dispose();
      // 重新排列剩余消息
      Window owner = findActiveWindow();
      if (owner != null) {
        repositionRemaining(owner);
      }
    });
  }

  /** 将新消息加入队列并排列所有消息位置。 */
  private static void repositionAll(JWindow newPopup, Window owner) {
    int ownerX = owner.getX();
    int ownerY = owner.getY();
    int ownerW = owner.getWidth();
    int startY = ownerY + 24;

    for (JWindow w : ACTIVE_MESSAGES) {
      if (w.isVisible()) {
        int wx = ownerX + (ownerW - w.getWidth()) / 2;
        w.setLocation(wx, startY);
        startY += w.getHeight() + GAP;
      }
    }

    int nx = ownerX + (ownerW - newPopup.getWidth()) / 2;
    newPopup.setLocation(nx, startY);
  }

  /** 重新排列已有消息。 */
  private static void repositionRemaining(Window owner) {
    int ownerX = owner.getX();
    int ownerY = owner.getY();
    int ownerW = owner.getWidth();
    int startY = ownerY + 24;

    for (JWindow w : ACTIVE_MESSAGES) {
      if (w.isVisible()) {
        int wx = ownerX + (ownerW - w.getWidth()) / 2;
        w.setLocation(wx, startY);
        startY += w.getHeight() + GAP;
      }
    }
  }

  /** 找到当前活跃的窗口。 */
  private static Window findActiveWindow() {
    for (Window w : Window.getWindows()) {
      if (w.isActive() && w.isVisible()) {
        return w;
      }
    }
    Window[] windows = Window.getWindows();
    for (Window w : windows) {
      if (w.isVisible() && !(w instanceof JWindow)) {
        return w;
      }
    }
    return null;
  }

  // =========================================================================
  // 消息面板
  // =========================================================================

  /**
   * 消息内容面板，带阴影和圆角。
   */
  private static class MessagePanel extends AbstractAntPanel {

    MessagePanel(String content, Status status) {
      setOpaque(false);
      setLayout(new BorderLayout(8, 0));
      setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

      ColorToken ct = colorToken();
      FontToken ft = fontToken();

      // 图标
      Color iconColor = status.getDefaultColor();
      ImageIcon icon = AntIcons.filled(status.getDefaultIconName(), 16, iconColor);
      JLabel iconLabel = new JLabel(icon);
      add(iconLabel, BorderLayout.WEST);

      // 文本
      JLabel textLabel = new JLabel(content);
      textLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      textLabel.setForeground(ct.getTextColor());
      add(textLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
      GraphicsUtils.drawShadow(g2, 0, 0, width, height,
          8, new Color(0, 0, 0, 32), 1, 4);

      ColorToken ct = colorToken();
      g2.setColor(ct.getBgElevated());
      g2.fillRoundRect(0, 0, width - 1, height - 1, 8, 8);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      return new Dimension(d.width + 8, d.height + 8);
    }
  }

  /**
   * 加载消息面板，带旋转指示器。
   */
  private static class LoadingPanel extends AbstractAntPanel {

    private int angle;
    private final Timer animTimer;

    LoadingPanel(String content) {
      setOpaque(false);
      setLayout(new BorderLayout(8, 0));
      setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

      ColorToken ct = colorToken();
      FontToken ft = fontToken();

      // 旋转图标占位
      JComponent spinner = new JComponent() {
        @Override
        protected void paintComponent(Graphics g) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
          int s = Math.min(getWidth(), getHeight());
          g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          g2.setColor(ct.getPrimaryColor());
          g2.drawArc(1, 1, s - 3, s - 3, angle, 270);
          g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
          return new Dimension(16, 16);
        }
      };
      add(spinner, BorderLayout.WEST);

      // 文本
      JLabel textLabel = new JLabel(content);
      textLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      textLabel.setForeground(ct.getTextColor());
      add(textLabel, BorderLayout.CENTER);

      // 动画定时器
      animTimer = new Timer(50, e -> {
        angle = (angle + 15) % 360;
        spinner.repaint();
      });
      animTimer.start();
    }

    /** 停止动画（面板移除时调用）。 */
    @Override
    public void removeNotify() {
      super.removeNotify();
      animTimer.stop();
    }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
      GraphicsUtils.drawShadow(g2, 0, 0, width, height,
          8, new Color(0, 0, 0, 32), 1, 4);
      ColorToken ct = colorToken();
      g2.setColor(ct.getBgElevated());
      g2.fillRoundRect(0, 0, width - 1, height - 1, 8, 8);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      return new Dimension(d.width + 8, d.height + 8);
    }
  }
}
