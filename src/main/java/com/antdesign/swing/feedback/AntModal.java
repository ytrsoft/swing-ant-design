package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntButton;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.AntIcons;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Ant Design 对话框组件。
 *
 * <p>对应 Ant Design {@code <Modal>}，模态对话框，居中弹出，带遮罩层。
 * 支持自定义标题、内容、页脚按钮，以及确认对话框快捷方法。
 *
 * <pre>{@code
 * // 基本用法
 * AntModal modal = new AntModal(frame, "标题");
 * modal.setContent(new JLabel("内容"));
 * modal.setOnOk(m -> System.out.println("OK"));
 * modal.show();
 *
 * // 确认对话框
 * AntModal.confirm("确认删除？", "此操作不可恢复",
 *     m -> doDelete(), null);
 * }</pre>
 */
public class AntModal {

  /** 默认对话框宽度 (px)。 */
  private static final int DEFAULT_WIDTH = 520;

  @Getter private String title;
  @Getter private JComponent content;
  @Getter @Setter private String okText;
  @Getter @Setter private String cancelText;
  @Getter @Setter private boolean closable;
  @Getter @Setter private Consumer<AntModal> onOk;
  @Getter @Setter private Consumer<AntModal> onCancel;

  private final JDialog dialog;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建一个模态对话框。
   *
   * @param owner 父窗口
   * @param title 标题
   */
  public AntModal(Window owner, String title) {
    this.title = (title != null) ? title : "";
    this.okText = "确定";
    this.cancelText = "取消";
    this.closable = true;

    this.dialog = new JDialog(owner instanceof JFrame ? (JFrame) owner : null, true);
    this.dialog.setUndecorated(true);
    this.dialog.setBackground(new Color(0, 0, 0, 0));
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  public void setTitle(String title) {
    this.title = (title != null) ? title : "";
  }

  public void setContent(JComponent content) {
    this.content = content;
  }

  /** 显示对话框。 */
  public void show() {
    SwingUtilities.invokeLater(() -> {
      buildDialog();
      dialog.setVisible(true);
    });
  }

  /** 关闭对话框。 */
  public void close() {
    dialog.setVisible(false);
    dialog.dispose();
  }

  // =========================================================================
  // 静态快捷方法
  // =========================================================================

  /**
   * 确认对话框。
   *
   * @param title    标题
   * @param content  内容文本
   * @param onOk     确认回调
   * @param onCancel 取消回调
   */
  public static void confirm(String title, String content,
      Consumer<AntModal> onOk, Consumer<AntModal> onCancel) {
    showStatusModal(title, content, Status.WARNING, onOk, onCancel);
  }

  /** 成功对话框。 */
  public static void success(String title, String content) {
    showStatusModal(title, content, Status.SUCCESS, null, null);
  }

  /** 错误对话框。 */
  public static void error(String title, String content) {
    showStatusModal(title, content, Status.ERROR, null, null);
  }

  /** 信息对话框。 */
  public static void info(String title, String content) {
    showStatusModal(title, content, Status.INFO, null, null);
  }

  /** 警告对话框。 */
  public static void warning(String title, String content) {
    showStatusModal(title, content, Status.WARNING, null, null);
  }

  private static void showStatusModal(String title, String content, Status status,
      Consumer<AntModal> onOk, Consumer<AntModal> onCancel) {
    Window owner = findActiveWindow();
    AntModal modal = new AntModal(owner, title);

    FontToken ft = AntThemeManager.getInstance().getFontToken();
    ColorToken ct = AntThemeManager.getInstance().getColorToken();

    JPanel panel = new JPanel(new BorderLayout(12, 0));
    panel.setOpaque(false);

    panel.add(new JLabel(AntIcons.filled(
        status.getDefaultIconName(), 22, status.getDefaultColor())), BorderLayout.WEST);

    JPanel text = new JPanel();
    text.setOpaque(false);
    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
    titleLbl.setForeground(ct.getTextColor());
    titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    text.add(titleLbl);

    if (content != null) {
      text.add(Box.createVerticalStrut(4));
      JLabel contentLbl = new JLabel(
          "<html><body style='width:320px'>" + content + "</body></html>");
      contentLbl.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      contentLbl.setForeground(ct.getTextSecondaryColor());
      contentLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
      text.add(contentLbl);
    }
    panel.add(text, BorderLayout.CENTER);

    modal.setContent(panel);
    modal.setClosable(false);
    modal.setOnOk(onOk);
    modal.setOnCancel(onCancel);
    modal.show();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void buildDialog() {
    JPanel root = new ModalRootPanel();
    root.setLayout(new BorderLayout());

    JPanel card = new ModalCardPanel();
    card.setLayout(new BorderLayout());
    SizeToken st = AntThemeManager.getInstance().getSizeToken();
    ColorToken ct = AntThemeManager.getInstance().getColorToken();
    FontToken ft = AntThemeManager.getInstance().getFontToken();

    // 头部
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    header.setBorder(BorderFactory.createEmptyBorder(
        st.getPadding(), st.getPaddingLg(), st.getPaddingSm(), st.getPaddingLg()));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
    titleLabel.setForeground(ct.getTextColor());
    header.add(titleLabel, BorderLayout.CENTER);

    if (closable) {
      JLabel closeBtn = new JLabel("×");
      closeBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
      closeBtn.setForeground(ct.getTextTertiaryColor());
      closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      closeBtn.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (onCancel != null) {
            onCancel.accept(AntModal.this);
          }
          close();
        }
      });
      header.add(closeBtn, BorderLayout.EAST);
    }
    card.add(header, BorderLayout.NORTH);

    // 内容
    JPanel body = new JPanel(new BorderLayout());
    body.setOpaque(false);
    body.setBorder(BorderFactory.createEmptyBorder(
        0, st.getPaddingLg(), st.getPadding(), st.getPaddingLg()));
    if (content != null) {
      body.add(content, BorderLayout.CENTER);
    }
    card.add(body, BorderLayout.CENTER);

    // 页脚
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, ct.getBorderSecondaryColor()),
        BorderFactory.createEmptyBorder(st.getPaddingSm(), st.getPaddingLg(),
            st.getPaddingSm(), st.getPaddingLg())));

    AntButton cancelBtn = new AntButton(cancelText, ButtonType.DEFAULT);
    cancelBtn.addActionListener(e -> {
      if (onCancel != null) {
        onCancel.accept(this);
      }
      close();
    });
    footer.add(cancelBtn);

    AntButton okBtn = new AntButton(okText, ButtonType.PRIMARY);
    okBtn.addActionListener(e -> {
      if (onOk != null) {
        onOk.accept(this);
      }
      close();
    });
    footer.add(okBtn);

    card.add(footer, BorderLayout.SOUTH);

    // 将卡片居中放在遮罩上
    card.setPreferredSize(new Dimension(DEFAULT_WIDTH, card.getPreferredSize().height));
    root.add(card, BorderLayout.CENTER);

    // 点击遮罩关闭
    root.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (!card.getBounds().contains(e.getPoint())) {
          if (onCancel != null) {
            onCancel.accept(AntModal.this);
          }
          close();
        }
      }
    });

    dialog.setContentPane(root);
    dialog.setSize(owner().getWidth(), owner().getHeight());
    dialog.setLocationRelativeTo(owner());
  }

  private Window owner() {
    return dialog.getOwner() != null ? dialog.getOwner() : findActiveWindow();
  }

  private static Window findActiveWindow() {
    for (Window w : Window.getWindows()) {
      if (w.isVisible() && !(w instanceof JDialog) && !(w instanceof javax.swing.JWindow)) {
        return w;
      }
    }
    return null;
  }

  // =========================================================================
  // 遮罩和卡片面板
  // =========================================================================

  /** 全屏遮罩面板。 */
  private static class ModalRootPanel extends AbstractAntPanel {
    ModalRootPanel() {
      setOpaque(false);
      setLayout(new java.awt.GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
      g.setColor(new Color(0, 0, 0, 100));
      g.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  /** 白色卡片面板。 */
  private static class ModalCardPanel extends JPanel {
    ModalCardPanel() {
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        GraphicsUtils.setupAntialiasing(g2);
        ColorToken ct = AntThemeManager.getInstance().getColorToken();

        // 阴影
        for (int i = 8; i > 0; i--) {
          g2.setColor(new Color(0, 0, 0, 3 * (9 - i)));
          g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, 12, 12);
        }

        g2.setColor(ct.getBgElevated());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
      } finally {
        g2.dispose();
      }
    }
  }
}
