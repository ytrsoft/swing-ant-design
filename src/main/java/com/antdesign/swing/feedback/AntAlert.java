package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.AntIcons;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 警告提示组件。
 *
 * <p>对应 Ant Design {@code <Alert>}，用于页面中展示重要的提示信息。
 * 支持四种状态类型、可选图标、描述文本和关闭按钮。
 *
 * <pre>{@code
 * AntAlert info = new AntAlert("This is an info alert.", Status.INFO);
 *
 * AntAlert withDesc = new AntAlert("Success Tips", Status.SUCCESS);
 * withDesc.setDescription("Detailed description of the success.");
 * withDesc.setClosable(true);
 * }</pre>
 *
 * @see Status
 */
public class AntAlert extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String message;
  @Getter private String description;
  @Getter private Status status;
  @Getter private boolean showIcon;
  @Getter private boolean closable;
  @Getter private boolean banner;

  private boolean closed;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建一个警告提示。
   *
   * @param message 提示消息
   * @param status  状态类型
   */
  public AntAlert(String message, Status status) {
    // 基类已设置 opaque=false 并注册默认监听器
    // 覆写为 rebuildUi 监听
    this.message = (message != null) ? message : "";
    this.status = (status != null) ? status : Status.INFO;
    this.showIcon = true;
    this.closable = false;

    setLayout(new BorderLayout());
    setThemeListener(theme -> {
      rebuildUi();
      repaint();
    });
    rebuildUi();
  }

  // =========================================================================
  // Setter — 全部走 rebuildUi
  // =========================================================================

  public void setMessage(String message) {
    this.message = (message != null) ? message : "";
    rebuildUi();
  }

  public void setDescription(String description) {
    this.description = description;
    rebuildUi();
  }

  public void setStatus(Status status) {
    this.status = (status != null) ? status : Status.INFO;
    rebuildUi();
  }

  public void setShowIcon(boolean showIcon) {
    this.showIcon = showIcon;
    rebuildUi();
  }

  public void setClosable(boolean closable) {
    this.closable = closable;
    rebuildUi();
  }

  public void setBanner(boolean banner) {
    this.banner = banner;
    rebuildUi();
  }

  // =========================================================================
  // 绘制 — 覆写基类 paintAnt
  // =========================================================================

  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    if (closed) {
      return;
    }

    SizeToken st = sizeToken();
    int arc = banner ? 0 : st.getBorderRadius();

    g2.setColor(resolveBackgroundColor());
    g2.fillRoundRect(0, 0, width, height, arc, arc);

    if (!banner) {
      g2.setColor(resolveStatusColor());
      g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (closed) {
      return new Dimension(0, 0);
    }
    return super.getPreferredSize();
  }

  @Override
  public boolean isVisible() {
    return !closed && super.isVisible();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    if (closed) {
      return;
    }

    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    ColorToken ct = colorToken();
    boolean hasDesc = description != null && !description.isEmpty();

    int pad = st.getPadding();
    setBorder(BorderFactory.createEmptyBorder(
        hasDesc ? pad : st.getPaddingXs(),
        pad,
        hasDesc ? pad : st.getPaddingXs(),
        pad));

    // 图标
    if (showIcon) {
      int iconSize = hasDesc ? 24 : 16;
      Color iconColor = resolveStatusColor();
      ImageIcon iconImg = AntIcons.filled(
          status.getDefaultIconName(), iconSize, iconColor);
      JLabel iconLabel = new JLabel(iconImg);
      iconLabel.setBorder(BorderFactory.createEmptyBorder(
          0, 0, 0, st.getPaddingXs()));
      add(iconLabel, BorderLayout.WEST);
    }

    // 文本区域
    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

    JLabel msgLabel = new JLabel(message);
    msgLabel.setFont(ft.createFont(ft.getFontSize(),
        hasDesc ? Font.BOLD : Font.PLAIN));
    msgLabel.setForeground(ct.getTextColor());
    msgLabel.setAlignmentX(LEFT_ALIGNMENT);
    textPanel.add(msgLabel);

    if (hasDesc) {
      textPanel.add(Box.createVerticalStrut(4));
      JLabel descLabel = new JLabel(
          "<html><body style='width:400px'>"
              + description + "</body></html>");
      descLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      descLabel.setForeground(ct.getTextSecondaryColor());
      descLabel.setAlignmentX(LEFT_ALIGNMENT);
      textPanel.add(descLabel);
    }

    add(textPanel, BorderLayout.CENTER);

    // 关闭按钮
    if (closable) {
      JLabel closeLabel = new JLabel("\u00D7");
      closeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
      closeLabel.setForeground(ct.getTextTertiaryColor());
      closeLabel.setCursor(
          Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      closeLabel.setBorder(BorderFactory.createEmptyBorder(
          0, st.getPaddingXs(), 0, 0));
      closeLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          closed = true;
          setVisible(false);
          revalidate();
        }
      });
      add(closeLabel, BorderLayout.EAST);
    }

    revalidate();
    repaint();
  }

  private Color resolveStatusColor() {
    ColorToken ct = colorToken();
    switch (status) {
      case SUCCESS: return ct.getSuccessColor();
      case WARNING: return ct.getWarningColor();
      case ERROR:   return ct.getErrorColor();
      default:      return ct.getInfoColor();
    }
  }

  private Color resolveBackgroundColor() {
    ColorToken ct = colorToken();
    switch (status) {
      case SUCCESS: return ct.getSuccessBgColor();
      case WARNING: return ct.getWarningBgColor();
      case ERROR:   return ct.getErrorBgColor();
      default:      return ct.getInfoBgColor();
    }
  }
}
