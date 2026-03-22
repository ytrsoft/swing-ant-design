package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntButton;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.AntIcons;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ant Design 气泡确认框。
 *
 * <p>对应 Ant Design {@code <Popconfirm>}，点击元素弹出气泡式的确认框。
 * 比 {@link AntModal} 更轻量，适用于不会造成严重后果的二次确认场景。
 *
 * <pre>{@code
 * AntPopconfirm pop = new AntPopconfirm("确定删除?");
 * pop.setOnConfirm(() -> doDelete());
 * pop.showBelow(deleteButton);
 * }</pre>
 */
public class AntPopconfirm {

  @Getter private String title;
  @Getter @Setter private String okText;
  @Getter @Setter private String cancelText;
  @Getter @Setter private Runnable onConfirm;
  @Getter @Setter private Runnable onCancel;

  private JWindow popup;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建气泡确认框。
   *
   * @param title 确认标题
   */
  public AntPopconfirm(String title) {
    this.title = (title != null) ? title : "";
    this.okText = "确定";
    this.cancelText = "取消";
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = (title != null) ? title : "";
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /**
   * 在指定组件下方显示气泡确认框。
   *
   * @param anchor 锚定组件
   */
  public void showBelow(JComponent anchor) {
    close();

    Window owner = SwingUtilities.getWindowAncestor(anchor);
    if (owner == null) {
      return;
    }

    popup = new JWindow(owner);
    popup.setAlwaysOnTop(true);

    PopPanel panel = new PopPanel();
    popup.setContentPane(panel);
    popup.pack();

    // 定位
    Point loc = anchor.getLocationOnScreen();
    int x = loc.x + (anchor.getWidth() - popup.getWidth()) / 2;
    int y = loc.y + anchor.getHeight() + 8;
    popup.setLocation(x, y);
    popup.setVisible(true);

    // 窗口失焦时关闭
    owner.addWindowListener(new WindowAdapter() {
      @Override
      public void windowDeactivated(WindowEvent e) {
        close();
        owner.removeWindowListener(this);
      }
    });
  }

  /** 关闭气泡确认框。 */
  public void close() {
    if (popup != null) {
      popup.dispose();
      popup = null;
    }
  }

  // =========================================================================
  // 气泡面板
  // =========================================================================

  private class PopPanel extends AbstractAntPanel {

    PopPanel() {
      setOpaque(false);
      setLayout(new BorderLayout(0, 8));

      SizeToken st = sizeToken();
      ColorToken ct = colorToken();
      FontToken ft = fontToken();

      setBorder(BorderFactory.createEmptyBorder(
          st.getPaddingSm(), st.getPadding(), st.getPaddingSm(), st.getPadding()));

      // 标题行（图标 + 文本）
      JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
      titleRow.setOpaque(false);

      ImageIcon warnIcon = AntIcons.filled("warning", 16, ct.getWarningColor());
      titleRow.add(new JLabel(warnIcon));

      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      titleLabel.setForeground(ct.getTextColor());
      titleRow.add(titleLabel);

      add(titleRow, BorderLayout.CENTER);

      // 按钮行
      JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
      btnRow.setOpaque(false);

      AntButton cancelBtn = new AntButton(cancelText, ButtonType.DEFAULT, ComponentSize.SMALL);
      cancelBtn.addActionListener(e -> {
        close();
        if (onCancel != null) {
          onCancel.run();
        }
      });
      btnRow.add(cancelBtn);

      AntButton okBtn = new AntButton(okText, ButtonType.PRIMARY, ComponentSize.SMALL);
      okBtn.addActionListener(e -> {
        close();
        if (onConfirm != null) {
          onConfirm.run();
        }
      });
      btnRow.add(okBtn);

      add(btnRow, BorderLayout.SOUTH);
    }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
      // 阴影
      for (int i = 4; i > 0; i--) {
        g2.setColor(new Color(0, 0, 0, 6 * (5 - i)));
        g2.fillRoundRect(i, i + 1, width - i * 2, height - i * 2, 8, 8);
      }

      ColorToken ct = colorToken();
      g2.setColor(ct.getBgElevated());
      g2.fillRoundRect(0, 0, width - 1, height - 1, 8, 8);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      return new Dimension(Math.max(d.width, 200) + 8, d.height + 8);
    }
  }
}
