package com.antdesign.swing.painter;

import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ant Design 按钮绘制器。
 *
 * <p>根据 {@link ButtonType} 绘制不同风格的按钮背景和边框，支持普通、悬停、
 * 按下、禁用四种交互状态。所有颜色和尺寸从当前主题令牌获取。
 *
 * <pre>{@code
 * ButtonPainter painter = new ButtonPainter(ButtonType.PRIMARY);
 * painter.paint(g2, button, width, height);
 * }</pre>
 *
 * @see AntPainter
 * @see ButtonType
 */
@Getter
public class ButtonPainter implements AntPainter {

    /**
     * -- GETTER --
     *  返回按钮类型。
     *
     */
    private final ButtonType buttonType;

  /**
   * 创建指定类型的按钮绘制器。
   *
   * @param buttonType 按钮类型
   */
  public ButtonPainter(ButtonType buttonType) {
    this.buttonType = (buttonType != null) ? buttonType : ButtonType.DEFAULT;
  }

  /** 创建默认类型的按钮绘制器。 */
  public ButtonPainter() {
    this(ButtonType.DEFAULT);
  }

  @Override
  public void paint(Graphics2D g2, JComponent component, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      ColorToken ct = AntThemeManager.getInstance().getColorToken();
      SizeToken st = AntThemeManager.getInstance().getSizeToken();
      int arc = st.getBorderRadius();

      ButtonModel model = (component instanceof AbstractButton)
          ? ((AbstractButton) component).getModel()
          : null;

      boolean isEnabled = component.isEnabled();
      boolean isPressed = model != null && model.isPressed();
      boolean isHover = model != null && model.isRollover();

      switch (buttonType) {
        case PRIMARY:
          paintPrimary(g, ct, arc, width, height, isEnabled, isPressed, isHover);
          break;
        case DASHED:
          paintDashed(g, ct, arc, width, height, isEnabled, isPressed, isHover);
          break;
        case TEXT:
          paintText(g, ct, width, height, isEnabled, isHover);
          break;
        case LINK:
          // 链接按钮无背景和边框
          break;
        default:
          paintDefault(g, ct, arc, width, height, isEnabled, isPressed, isHover);
          break;
      }
    } finally {
      g.dispose();
    }
  }

  // ---------------------------------------------------------------------------
  // 绘制方法
  // ---------------------------------------------------------------------------

  private void paintPrimary(Graphics2D g, ColorToken ct, int arc,
      int width, int height, boolean enabled, boolean pressed, boolean hover) {
    Color bg;
    if (!enabled) {
      bg = ct.getDisabledBgColor();
    } else if (pressed) {
      bg = ct.getPrimaryActiveColor();
    } else if (hover) {
      bg = ct.getPrimaryHoverColor();
    } else {
      bg = ct.getPrimaryColor();
    }

    RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc);
    g.setColor(bg);
    g.fill(rect);
  }

  private void paintDefault(Graphics2D g, ColorToken ct, int arc,
      int width, int height, boolean enabled, boolean pressed, boolean hover) {
    RoundRectangle2D rect = paintButton(g, ct, arc, width, height, enabled, pressed, hover);
    g.setStroke(new BasicStroke(1f));
    g.draw(rect);
  }

  private void paintDashed(Graphics2D g, ColorToken ct, int arc,
      int width, int height, boolean enabled, boolean pressed, boolean hover) {
    RoundRectangle2D rect = paintButton(g, ct, arc, width, height, enabled, pressed, hover);
    g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
        1f, new float[]{4f, 4f}, 0f));
    g.draw(rect);
  }

  private RoundRectangle2D paintButton(Graphics2D g, ColorToken ct, int arc,
                           int width, int height, boolean enabled, boolean pressed, boolean hover) {
    // 背景
    g.setColor(enabled ? ct.getBgContainer() : ct.getDisabledBgColor());
    RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc);
    g.fill(rect);

    // 边框
    Color borderColor;
    if (!enabled) {
      borderColor = ct.getBorderColor();
    } else if (pressed) {
      borderColor = ct.getPrimaryActiveColor();
    } else if (hover) {
      borderColor = ct.getPrimaryHoverColor();
    } else {
      borderColor = ct.getBorderColor();
    }
    g.setColor(borderColor);

    return rect;
  }

  private void paintText(Graphics2D g, ColorToken ct,
      int width, int height, boolean enabled, boolean hover) {
    if (hover && enabled) {
      g.setColor(ct.getFillSecondaryColor());
      g.fillRect(0, 0, width, height);
    }
  }
}
