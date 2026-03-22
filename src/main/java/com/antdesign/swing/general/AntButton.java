package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntActionEvent;
import com.antdesign.swing.event.AntActionListener;
import com.antdesign.swing.model.ButtonShape;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.ColorUtils;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 按钮组件。
 *
 * <p>对应 Ant Design {@code <Button>} 组件，支持五种按钮类型（{@link ButtonType}）、
 * 三种尺寸（{@link ComponentSize}）、三种形状（{@link ButtonShape}），以及危险模式、
 * 加载状态、禁用状态、图标和幽灵模式（Ghost）等特性。
 *
 * <pre>{@code
 * AntButton primary = new AntButton("Primary", ButtonType.PRIMARY);
 *
 * AntButton iconBtn = new AntButton("Search", ButtonType.PRIMARY);
 * iconBtn.setIcon(AntIcons.outlined("search", 14, Color.WHITE));
 *
 * AntButton danger = new AntButton("Delete", ButtonType.PRIMARY);
 * danger.setDanger(true);
 * }</pre>
 *
 * @see ButtonType
 * @see ButtonShape
 * @see ComponentSize
 */
public class AntButton extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 图标与文本之间的间距 (px)。 */
  private static final int ICON_TEXT_GAP = 8;

  // --- 公共属性（Lombok 生成 getter） ---

  @Getter private String text;
  @Getter private ButtonType buttonType;
  @Getter private ButtonShape shape;
  private ComponentSize size;
  @Getter private Icon icon;
  @Getter private boolean danger;
  @Getter private boolean ghost;
  @Getter private boolean loading;
  @Getter private boolean block;

  // --- 内部交互状态 ---

  private boolean hovered;
  private boolean pressed;

  private final List<AntActionListener> actionListeners = new CopyOnWriteArrayList<>();

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建一个默认类型的按钮。 */
  public AntButton(String text) {
    this(text, ButtonType.DEFAULT);
  }

  /** 创建指定类型的按钮。 */
  public AntButton(String text, ButtonType buttonType) {
    this(text, buttonType, ComponentSize.MIDDLE);
  }

  /**
   * 创建指定类型和尺寸的按钮。
   *
   * @param text       按钮文本
   * @param buttonType 按钮类型
   * @param size       按钮尺寸
   */
  public AntButton(String text, ButtonType buttonType, ComponentSize size) {
    // 基类构造器已设置 opaque=false 并注册默认主题监听
    this.text = text;
    this.buttonType = (buttonType != null) ? buttonType : ButtonType.DEFAULT;
    this.shape = ButtonShape.DEFAULT;
    this.size = (size != null) ? size : ComponentSize.MIDDLE;

    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMouseListener();
  }

  /** 创建一个仅包含图标的按钮。 */
  public AntButton(Icon icon, ButtonType buttonType) {
    this("", buttonType);
    this.icon = icon;
  }

  // =========================================================================
  // Setter — 使用基类辅助方法
  // =========================================================================

  /**
   * 获取按钮尺寸。方法名为 {@code getComponentSize()} 以避免与
   * {@link java.awt.Component#getSize()} 冲突。
   */
  public ComponentSize getComponentSize() {
    return size;
  }

  public void setText(String text) {
    updateAndRelayout(() -> this.text = text);
  }

  public void setButtonType(ButtonType buttonType) {
    updateAndRepaint(() ->
        this.buttonType = (buttonType != null) ? buttonType : ButtonType.DEFAULT);
  }

  public void setShape(ButtonShape shape) {
    updateAndRelayout(() ->
        this.shape = (shape != null) ? shape : ButtonShape.DEFAULT);
  }

  public void setSize(ComponentSize size) {
    updateAndRelayout(() ->
        this.size = (size != null) ? size : ComponentSize.MIDDLE);
  }

  public void setIcon(Icon icon) {
    updateAndRelayout(() -> this.icon = icon);
  }

  public void setDanger(boolean danger) {
    updateAndRepaint(() -> this.danger = danger);
  }

  public void setGhost(boolean ghost) {
    updateAndRepaint(() -> this.ghost = ghost);
  }

  public void setLoading(boolean loading) {
    updateAndRepaint(() -> {
      this.loading = loading;
      setCursor(loading
          ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
          : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    });
  }

  public void setBlock(boolean block) {
    updateAndRelayout(() -> this.block = block);
  }

  // =========================================================================
  // 事件
  // =========================================================================

  public void addActionListener(AntActionListener listener) {
    if (listener != null) {
      actionListeners.add(listener);
    }
  }

  public void removeActionListener(AntActionListener listener) {
    actionListeners.remove(listener);
  }

  protected void fireActionEvent() {
    if (!isEnabled() || loading) {
      return;
    }
    AntActionEvent event = new AntActionEvent(this, text);
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

    SizeToken st = sizeToken();
    FontToken ft = fontToken();
    int height = st.controlHeightOf(size);

    if (shape == ButtonShape.CIRCLE) {
      //noinspection SuspiciousNameCombination
      return new Dimension(height, height);
    }

    int fontSize = computeFontSize(ft);
    Font font = ft.createFont(fontSize, Font.PLAIN);
    FontMetrics fm = getFontMetrics(font);

    int textWidth = hasText() ? fm.stringWidth(text) : 0;
    int iconWidth = (icon != null) ? icon.getIconWidth() : 0;
    int gap = (iconWidth > 0 && textWidth > 0) ? ICON_TEXT_GAP : 0;
    int horizontalPadding = computeHorizontalPadding(st);

    int width = Math.max(
        iconWidth + gap + textWidth + horizontalPadding * 2, height);

    return new Dimension(width, height);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    if (block) {
      Dimension pref = getPreferredSize();
      return new Dimension(Integer.MAX_VALUE, pref.height);
    }
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制 — 覆写基类 paintAnt
  // =========================================================================

  @Override
  protected void paintAnt(Graphics2D g2, int w, int h) {
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();
    FontToken ft = fontToken();

    paintBackground(g2, ct, st, w, h);
    paintContent(g2, ct, ft, st, w, h);
  }

  private void paintBackground(Graphics2D g2, ColorToken ct, SizeToken st,
      int w, int h) {
    int arc = computeArc(st);

    if (buttonType == ButtonType.LINK) {
      return;
    }
    if (buttonType == ButtonType.TEXT) {
      if (hovered && isEnabled() && !loading) {
        GraphicsUtils.fillRoundRect(g2, 0, 0, w, h, arc,
            ct.getFillSecondaryColor());
      }
      return;
    }

    if (danger) {
      paintDangerBackground(g2, ct, arc, w, h);
    } else if (ghost) {
      paintGhostBackground(g2, ct, arc, w, h);
    } else {
      paintNormalBackground(g2, ct, arc, w, h);
    }

    if (isFocusOwner() && isEnabled() && !loading) {
      Color focusColor = danger ? ct.getErrorColor() : ct.getPrimaryColor();
      GraphicsUtils.drawFocusRing(g2, 0, 0, w, h, arc, focusColor,
          st.getControlOutlineWidth());
    }
  }

  private void paintNormalBackground(Graphics2D g2, ColorToken ct, int arc,
      int w, int h) {
    boolean enabled = isEnabled() && !loading;

    if (buttonType == ButtonType.PRIMARY) {
      Color bg;
      if (!enabled) {
        bg = ct.getDisabledBgColor();
      } else if (pressed) {
        bg = ct.getPrimaryActiveColor();
      } else if (hovered) {
        bg = ct.getPrimaryHoverColor();
      } else {
        bg = ct.getPrimaryColor();
      }
      GraphicsUtils.fillRoundRect(g2, 0, 0, w - 1, h - 1, arc, bg);
    } else {
      GraphicsUtils.fillRoundRect(g2, 0, 0, w - 1, h - 1, arc,
          enabled ? ct.getBgContainer() : ct.getDisabledBgColor());

      Color borderColor;
      if (!enabled) {
        borderColor = ct.getBorderColor();
      } else if (pressed) {
        borderColor = ct.getPrimaryActiveColor();
      } else if (hovered) {
        borderColor = ct.getPrimaryHoverColor();
      } else {
        borderColor = ct.getBorderColor();
      }

      if (buttonType == ButtonType.DASHED) {
        GraphicsUtils.drawDashedRoundRect(g2, 0, 0, w - 1, h - 1,
            arc, borderColor);
      } else {
        GraphicsUtils.drawRoundRect(g2, 0, 0, w - 1, h - 1,
            arc, borderColor, 1f);
      }
    }
  }

  private void paintDangerBackground(Graphics2D g2, ColorToken ct, int arc,
      int w, int h) {
    Color bg;
    Color border;

    if (!isEnabled() || loading) {
      bg = ct.getDisabledBgColor();
      border = ct.getBorderColor();
    } else if (buttonType == ButtonType.PRIMARY) {
      bg = pressed ? darkenColor(ct.getErrorColor())
          : (hovered ? lightenColor(ct.getErrorColor())
              : ct.getErrorColor());
      border = bg;
    } else {
      bg = ct.getBgContainer();
      border = pressed ? darkenColor(ct.getErrorColor())
          : (hovered ? lightenColor(ct.getErrorColor())
              : ct.getErrorColor());
    }

    GraphicsUtils.fillRoundRect(g2, 0, 0, w - 1, h - 1, arc, bg);
    GraphicsUtils.drawRoundRect(g2, 0, 0, w - 1, h - 1, arc, border, 1f);
  }

  private void paintGhostBackground(Graphics2D g2, ColorToken ct, int arc,
      int w, int h) {
    Color border;
    if (!isEnabled() || loading) {
      border = ct.getDisabledColor();
    } else if (buttonType == ButtonType.PRIMARY) {
      border = pressed ? ct.getPrimaryActiveColor()
          : (hovered ? ct.getPrimaryHoverColor() : ct.getPrimaryColor());
    } else {
      border = pressed ? ct.getPrimaryActiveColor()
          : (hovered ? ct.getPrimaryHoverColor() : ct.getBorderColor());
    }
    GraphicsUtils.drawRoundRect(g2, 0, 0, w - 1, h - 1, arc, border, 1f);
  }

  private void paintContent(Graphics2D g2, ColorToken ct, FontToken ft,
      SizeToken st, int w, int h) {
    int fontSize = computeFontSize(ft);
    Font font = ft.createFont(fontSize, Font.PLAIN);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics();

    Color fg = computeForegroundColor(ct);
    g2.setColor(fg);

    int textWidth = hasText() ? fm.stringWidth(text) : 0;
    int iconWidth = (icon != null) ? icon.getIconWidth() : 0;
    int iconHeight = (icon != null) ? icon.getIconHeight() : 0;
    int gap = (iconWidth > 0 && textWidth > 0) ? ICON_TEXT_GAP : 0;
    int contentWidth = iconWidth + gap + textWidth;

    int x = (w - contentWidth) / 2;
    int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

    if (icon != null) {
      int iconY = (h - iconHeight) / 2;
      GraphicsUtils.paintTintedIcon(g2, this, icon, x, iconY, fg);
      x += iconWidth + gap;
    }

    if (hasText()) {
      g2.drawString(text, x, textY);
      if (buttonType == ButtonType.LINK && hovered && isEnabled()) {
        g2.drawLine(x, textY + 2, x + textWidth, textY + 2);
      }
    }
  }

  // =========================================================================
  // 内部辅助
  // =========================================================================

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
        if (isEnabled() && !loading) {
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

  private Color computeForegroundColor(ColorToken ct) {
    if (!isEnabled() || loading) {
      return ct.getDisabledColor();
    }
    if (danger) {
      return (buttonType == ButtonType.PRIMARY) ? Color.WHITE
          : ct.getErrorColor();
    }
    if (ghost && buttonType == ButtonType.PRIMARY) {
      return pressed ? ct.getPrimaryActiveColor()
          : (hovered ? ct.getPrimaryHoverColor() : ct.getPrimaryColor());
    }

    switch (buttonType) {
      case PRIMARY:
        return Color.WHITE;
      case LINK:
        return pressed ? ct.getPrimaryActiveColor()
            : (hovered ? ct.getPrimaryHoverColor() : ct.getLinkColor());
      case TEXT:
        return ct.getTextColor();
      default:
        return pressed ? ct.getPrimaryActiveColor()
            : (hovered ? ct.getPrimaryHoverColor() : ct.getTextColor());
    }
  }

  private int computeFontSize(FontToken ft) {
    switch (size) {
      case LARGE:  return ft.getFontSizeLg();
      case SMALL:  return ft.getFontSizeSm();
      default:     return ft.getFontSize();
    }
  }

  private int computeArc(SizeToken st) {
    if (shape == ButtonShape.CIRCLE || shape == ButtonShape.ROUND) {
      return st.controlHeightOf(size);
    }
    return st.getBorderRadius();
  }

  private int computeHorizontalPadding(SizeToken st) {
    switch (size) {
      case LARGE:  return st.getPaddingLg() - 1;
      case SMALL:  return st.getPaddingXs() - 1;
      default:     return st.getPadding() - 1;
    }
  }

  private boolean hasText() {
    return text != null && !text.isEmpty();
  }

  private static Color lightenColor(Color c) {
    return ColorUtils.lighten(c, 0.15f);
  }

  private static Color darkenColor(Color c) {
    return ColorUtils.darken(c, 0.15f);
  }

  @Override
  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }
}
