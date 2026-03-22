package com.antdesign.swing.painter;

import com.antdesign.swing.model.Status;
import com.antdesign.swing.model.Variant;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ant Design 输入框绘制器。
 *
 * <p>支持三种变体（{@link Variant}）和四种状态（{@link Status}）的组合绘制，
 * 涵盖正常、聚焦、悬停和禁用等交互状态下的背景与边框渲染。
 *
 * <p>组件可通过 {@code putClientProperty} 传递额外状态：
 * <ul>
 *   <li>{@code "antd.focused"} — {@code Boolean}，是否处于聚焦态</li>
 *   <li>{@code "antd.hovered"} — {@code Boolean}，是否处于悬停态</li>
 * </ul>
 *
 * @see AntPainter
 * @see Variant
 * @see Status
 */
public class InputPainter implements AntPainter {

  /** 客户端属性键：聚焦状态。 */
  public static final String PROP_FOCUSED = "antd.focused";

  /** 客户端属性键：悬停状态。 */
  public static final String PROP_HOVERED = "antd.hovered";

  private final Variant variant;
  private final Status status;

  /**
   * 创建指定变体和状态的输入框绘制器。
   *
   * @param variant 输入框变体
   * @param status 校验状态，{@code null} 表示无状态
   */
  public InputPainter(Variant variant, Status status) {
    this.variant = (variant != null) ? variant : Variant.OUTLINED;
    this.status = status;
  }

  /**
   * 创建默认变体且无状态的输入框绘制器。
   */
  public InputPainter() {
    this(Variant.OUTLINED, null);
  }

  @Override
  public void paint(Graphics2D g2, JComponent component, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      ColorToken ct = AntThemeManager.getInstance().getColorToken();
      SizeToken st = AntThemeManager.getInstance().getSizeToken();
      int arc = st.getBorderRadius();

      boolean enabled = component.isEnabled();
      boolean focused = Boolean.TRUE.equals(component.getClientProperty(PROP_FOCUSED));
      boolean hovered = Boolean.TRUE.equals(component.getClientProperty(PROP_HOVERED));

      switch (variant) {
        case FILLED:
          paintFilled(g, ct, arc, width, height, enabled, focused, hovered);
          break;
        case BORDERLESS:
          paintBorderless(g, ct, width, height, enabled, focused);
          break;
        default:
          paintOutlined(g, ct, arc, width, height, enabled, focused, hovered);
          break;
      }

      // 聚焦外发光
      if (focused && enabled && variant != Variant.BORDERLESS) {
        paintFocusGlow(g, st, arc, width, height);
      }
    } finally {
      g.dispose();
    }
  }

  /**
   * 返回当前变体。
   *
   * @return 变体
   */
  public Variant getVariant() {
    return variant;
  }

  /**
   * 返回当前状态。
   *
   * @return 状态，可能为 {@code null}
   */
  public Status getStatus() {
    return status;
  }

  // ---------------------------------------------------------------------------
  // 绘制方法
  // ---------------------------------------------------------------------------

  private void paintOutlined(Graphics2D g, ColorToken ct, int arc,
      int width, int height, boolean enabled, boolean focused, boolean hovered) {
    RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc);

    // 背景
    g.setColor(enabled ? ct.getBgContainer() : ct.getDisabledBgColor());
    g.fill(rect);

    // 边框
    g.setColor(resolveBorderColor(ct, enabled, focused, hovered));
    g.setStroke(new BasicStroke(1f));
    g.draw(rect);
  }

  private void paintFilled(Graphics2D g, ColorToken ct, int arc,
      int width, int height, boolean enabled, boolean focused, boolean hovered) {
    RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc);

    // 填充背景
    if (!enabled) {
      g.setColor(ct.getDisabledBgColor());
    } else if (focused) {
      g.setColor(ct.getBgContainer());
    } else {
      g.setColor(ct.getFillSecondaryColor());
    }
    g.fill(rect);

    // 聚焦时显示边框
    if (focused && enabled) {
      g.setColor(resolveBorderColor(ct, true, true, false));
      g.setStroke(new BasicStroke(1f));
      g.draw(rect);
    }
  }

  private void paintBorderless(Graphics2D g, ColorToken ct,
      int width, int height, boolean enabled, boolean focused) {
    // 无边框模式仅在聚焦时有微弱底线
    if (focused && enabled) {
      g.setColor(resolveStatusColor(ct));
      g.setStroke(new BasicStroke(2f));
      g.drawLine(0, height - 1, width, height - 1);
    }
  }

  private void paintFocusGlow(Graphics2D g, SizeToken st, int arc, int width, int height) {
    ColorToken ct = AntThemeManager.getInstance().getColorToken();
    Color statusColor = resolveStatusColor(ct);
    Color glow = new Color(
        statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 0x33);

    int outlineWidth = st.getControlOutlineWidth();
    RoundRectangle2D outer = new RoundRectangle2D.Double(
        -outlineWidth, -outlineWidth,
        width - 1 + 2 * outlineWidth, height - 1 + 2 * outlineWidth,
        arc + outlineWidth, arc + outlineWidth);
    g.setColor(glow);
    g.setStroke(new BasicStroke(outlineWidth));
    g.draw(outer);
  }

  private Color resolveBorderColor(ColorToken ct,
      boolean enabled, boolean focused, boolean hovered) {
    if (!enabled) {
      return ct.getBorderColor();
    }
    if (status != null) {
      return status.getDefaultColor();
    }
    if (focused) {
      return ct.getPrimaryColor();
    }
    if (hovered) {
      return ct.getPrimaryHoverColor();
    }
    return ct.getBorderColor();
  }

  private Color resolveStatusColor(ColorToken ct) {
    return (status != null) ? status.getDefaultColor() : ct.getPrimaryColor();
  }
}
