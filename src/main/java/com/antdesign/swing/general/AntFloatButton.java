package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntActionEvent;
import com.antdesign.swing.event.AntActionListener;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.util.ColorUtils;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 悬浮按钮组件。
 *
 * <p>对应 Ant Design {@code <FloatButton>} 组件。圆形悬浮按钮，带阴影、
 * 悬停效果和 Tooltip，通常固定在页面右下角。
 *
 * <p>支持两种类型：
 * <ul>
 *   <li>{@link ButtonType#PRIMARY} — 主色背景，白色图标</li>
 *   <li>{@link ButtonType#DEFAULT} — 白色背景，文本色图标，带边框</li>
 * </ul>
 *
 * <pre>{@code
 * // 主色悬浮按钮
 * AntFloatButton fab = new AntFloatButton(
 *     AntIcons.outlined("plus", 18, Color.WHITE));
 *
 * // 带文字描述
 * AntFloatButton fab2 = new AntFloatButton(
 *     AntIcons.outlined("question-circle", 18, Color.WHITE));
 * fab2.setDescription("Help");
 *
 * // Default 类型
 * AntFloatButton fab3 = new AntFloatButton(
 *     AntIcons.outlined("setting", 18), ButtonType.DEFAULT);
 * }</pre>
 *
 * @see AntButton
 */
public class AntFloatButton extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 默认按钮直径 (px)。 */
  private static final int DEFAULT_SIZE = 40;

  /** 大号按钮直径 (px)。 */
  private static final int LARGE_SIZE = 56;

  /** 阴影模糊半径 (px)。 */
  private static final int SHADOW_BLUR = 8;

  /** 阴影 Y 偏移 (px)。 */
  private static final int SHADOW_OFFSET_Y = 2;

  /** 总尺寸包含阴影扩展 (px)。 */
  private static final int SHADOW_SPREAD = SHADOW_BLUR + 2;

  // --- 属性 ---

  @Getter private Icon icon;
  @Getter private ButtonType buttonType;
  @Getter private String description;
  @Getter private String tooltip;
  @Getter private int buttonSize;

  // --- 内部交互状态 ---

  private boolean hovered;
  private boolean pressed;

  private final List<AntActionListener> actionListeners = new CopyOnWriteArrayList<>();

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建主色悬浮按钮。
   *
   * @param icon 图标
   */
  public AntFloatButton(Icon icon) {
    this(icon, ButtonType.PRIMARY);
  }

  /**
   * 创建指定类型的悬浮按钮。
   *
   * @param icon       图标
   * @param buttonType 按钮类型（PRIMARY 或 DEFAULT）
   */
  public AntFloatButton(Icon icon, ButtonType buttonType) {
    this.icon = icon;
    this.buttonType = (buttonType != null) ? buttonType : ButtonType.PRIMARY;
    this.buttonSize = DEFAULT_SIZE;

    setOpaque(false);
    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMouseListener();
  }

  // =========================================================================
  // Setter
  // =========================================================================

  /** 设置图标。 */
  public void setIcon(Icon icon) {
    updateAndRepaint(() -> this.icon = icon);
  }

  /** 设置按钮类型。 */
  public void setButtonType(ButtonType buttonType) {
    updateAndRepaint(() ->
        this.buttonType = (buttonType != null) ? buttonType : ButtonType.PRIMARY);
  }

  /**
   * 设置按钮下方的文字描述。
   *
   * @param description 描述文本，{@code null} 则不显示
   */
  public void setDescription(String description) {
    updateAndRelayout(() -> this.description = description);
  }

  /**
   * 设置 Tooltip 文本。
   *
   * @param tooltip 提示文本
   */
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
    setToolTipText(tooltip);
  }

  /** 设置按钮直径。 */
  public void setButtonSize(int size) {
    updateAndRelayout(() -> this.buttonSize = Math.max(24, size));
  }

  /** 设为大号按钮。 */
  public void setLarge(boolean large) {
    setButtonSize(large ? LARGE_SIZE : DEFAULT_SIZE);
  }

  // =========================================================================
  // 事件
  // =========================================================================

  /** 添加点击监听器。 */
  public void addActionListener(AntActionListener listener) {
    if (listener != null) {
      actionListeners.add(listener);
    }
  }

  /** 移除点击监听器。 */
  public void removeActionListener(AntActionListener listener) {
    actionListeners.remove(listener);
  }

  private void fireActionEvent() {
    if (!isEnabled()) {
      return;
    }
    AntActionEvent event = new AntActionEvent(this, description);
    for (AntActionListener listener : actionListeners) {
      listener.actionPerformed(event);
    }
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    int totalSize = buttonSize + SHADOW_SPREAD * 2;

    if (hasDescription()) {
      FontMetrics fm = getFontMetrics(
          fontToken().createFont(fontToken().getFontSizeSm(), Font.PLAIN));
      int descW = fm.stringWidth(description);
      int width = Math.max(totalSize, descW + 8);
      int height = totalSize + fm.getHeight() + 4;
      return new Dimension(width, height);
    }

    return new Dimension(totalSize, totalSize);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制
  // =========================================================================

  @Override
  protected void paintAnt(Graphics2D g2, int w, int h) {
    ColorToken ct = colorToken();
    boolean isPrimary = (buttonType == ButtonType.PRIMARY);

    // 按钮圆心坐标（在含阴影的空间内居中）
    int cx = w / 2;
    int cy = SHADOW_SPREAD + buttonSize / 2;
    int radius = buttonSize / 2;

    // --- 阴影 ---
    paintShadow(g2, cx, cy, radius);

    // --- 背景圆 ---
    Color bgColor = computeBackgroundColor(ct, isPrimary);
    g2.setColor(bgColor);
    g2.fill(new Ellipse2D.Double(
        cx - radius, cy - radius, buttonSize, buttonSize));

    // --- 边框（仅 Default 类型） ---
    if (!isPrimary) {
      Color borderColor = hovered
          ? ct.getPrimaryHoverColor() : ct.getBorderColor();
      g2.setColor(borderColor);
      g2.setStroke(new BasicStroke(1.0f));
      g2.draw(new Ellipse2D.Double(
          cx - radius + 0.5, cy - radius + 0.5,
          buttonSize - 1, buttonSize - 1));
    }

    // --- 图标 ---
    if (icon != null) {
      int iconW = icon.getIconWidth();
      int iconH = icon.getIconHeight();
      int iconX = cx - iconW / 2;
      int iconY = cy - iconH / 2;
      Color iconColor = computeIconColor(ct, isPrimary);
      GraphicsUtils.paintTintedIcon(g2, this, icon, iconX, iconY, iconColor);
    }

    // --- 描述文字 ---
    if (hasDescription()) {
      Font descFont = fontToken().createFont(fontToken().getFontSizeSm(), Font.PLAIN);
      g2.setFont(descFont);
      g2.setColor(ct.getTextSecondaryColor());
      FontMetrics fm = g2.getFontMetrics();
      int textX = (w - fm.stringWidth(description)) / 2;
      int textY = cy + radius + SHADOW_SPREAD / 2 + fm.getAscent();
      g2.drawString(description, textX, textY);
    }
  }

  // =========================================================================
  // 内部辅助
  // =========================================================================

  private Color computeBackgroundColor(ColorToken ct, boolean isPrimary) {
    if (!isEnabled()) {
      return ct.getDisabledBgColor();
    }
    if (isPrimary) {
      if (pressed) {
        return ct.getPrimaryActiveColor();
      }
      if (hovered) {
        return ct.getPrimaryHoverColor();
      }
      return ct.getPrimaryColor();
    } else {
      if (pressed) {
        return ct.getFillSecondaryColor();
      }
      if (hovered) {
        return ct.getBgContainer();
      }
      return ct.getBgContainer();
    }
  }

  private Color computeIconColor(ColorToken ct, boolean isPrimary) {
    if (!isEnabled()) {
      return ct.getDisabledColor();
    }
    if (isPrimary) {
      return Color.WHITE;
    }
    if (hovered) {
      return ct.getPrimaryHoverColor();
    }
    return ct.getTextSecondaryColor();
  }

  private void paintShadow(Graphics2D g2, int cx, int cy, int radius) {
    Color shadowColor = new Color(0, 0, 0, 25);
    for (int i = SHADOW_BLUR; i > 0; i--) {
      float ratio = (float) i / SHADOW_BLUR;
      int alpha = Math.round(25 * (1f - ratio) * 0.6f);
      if (alpha <= 0) {
        continue;
      }
      g2.setColor(new Color(0, 0, 0, alpha));
      int spread = radius + i;
      g2.fill(new Ellipse2D.Double(
          cx - spread, cy - spread + SHADOW_OFFSET_Y,
          spread * 2, spread * 2));
    }
  }

  private boolean hasDescription() {
    return description != null && !description.isEmpty();
  }

  private void installMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        hovered = true;
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        hovered = false;
        pressed = false;
        repaint();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (isEnabled()) {
          pressed = true;
          repaint();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (pressed) {
          pressed = false;
          repaint();
          if (contains(e.getPoint())) {
            fireActionEvent();
          }
        }
      }
    });
  }

  @Override
  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }
}
