package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Placement;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.PopupHelper;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 文字提示组件。
 *
 * <p>对应 Ant Design {@code <Tooltip>}，鼠标移入则显示提示，移出消失。
 * 支持 12 个方向位置。
 *
 * <pre>{@code
 * AntTooltip.bindTo(myButton, "Prompt Text");
 * AntTooltip.bindTo(myLabel, "Top Tip", Placement.TOP);
 * }</pre>
 */
public class AntTooltip extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int PADDING_H = 8;
  private static final int PADDING_V = 6;
  private static final int OFFSET = 8;

  @Getter private String tipText;
  @Getter private Placement placement;
  @Getter private Color backgroundColor;

  private Popup popup;
  private final Component owner;

  /**
   * 创建工具提示并绑定到目标组件。
   *
   * @param owner     宿主组件
   * @param tipText   提示文本
   * @param placement 弹出方向
   */
  public AntTooltip(Component owner, String tipText, Placement placement) {
    this.owner = owner;
    this.tipText = (tipText != null) ? tipText : "";
    this.placement = (placement != null) ? placement : Placement.TOP;
    installListeners();
  }

  /**
   * 创建默认方向（TOP）的工具提示。
   *
   * @param owner   宿主组件
   * @param tipText 提示文本
   */
  public AntTooltip(Component owner, String tipText) {
    this(owner, tipText, Placement.TOP);
  }

  /**
   * 便捷方法：为组件绑定 Tooltip。
   *
   * @param target    目标组件
   * @param text      提示文本
   * @param placement 弹出方向
   * @return 创建的 AntTooltip 实例
   */
  public static AntTooltip bindTo(Component target, String text, Placement placement) {
    return new AntTooltip(target, text, placement);
  }

  /**
   * 便捷方法：为组件绑定默认方向的 Tooltip。
   *
   * @param target 目标组件
   * @param text   提示文本
   * @return 创建的 AntTooltip 实例
   */
  public static AntTooltip bindTo(Component target, String text) {
    return new AntTooltip(target, text);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTipText(String tipText) {
    this.tipText = (tipText != null) ? tipText : "";
  }

  public void setPlacement(Placement placement) {
    this.placement = (placement != null) ? placement : Placement.TOP;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  // =========================================================================
  // 尺寸 & 绘制
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    FontToken ft = fontToken();
    FontMetrics fm = getFontMetrics(ft.createFont(ft.getFontSize(), Font.PLAIN));
    int w = fm.stringWidth(tipText) + PADDING_H * 2;
    int h = fm.getHeight() + PADDING_V * 2;
    return new Dimension(w, h);
  }  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int w = width;
    int h = height;
    int arc = st.getBorderRadius();

    Color bg = (backgroundColor != null) ? backgroundColor : ct.getBgSpotlight();
    g2.setColor(bg);
    g2.fillRoundRect(0, 0, w, h, arc, arc);

    g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    g2.setColor(Color.WHITE);
    FontMetrics fm = g2.getFontMetrics();
    g2.drawString(tipText, PADDING_H,
        (h + fm.getAscent() - fm.getDescent()) / 2);
  }

  // =========================================================================
  // 显示/隐藏
  // =========================================================================

  /** 显示提示。 */
  public void showTooltip() {
    if (popup != null || tipText.isEmpty()) {
      return;
    }
    popup = PopupHelper.showPopup(owner, this, placement, OFFSET);
  }

  /** 隐藏提示。 */
  public void hideTooltip() {
    PopupHelper.hidePopup(popup);
    popup = null;
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void installListeners() {
    owner.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        SwingUtilities.invokeLater(() -> showTooltip());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        hideTooltip();
      }
    });
  }
}
