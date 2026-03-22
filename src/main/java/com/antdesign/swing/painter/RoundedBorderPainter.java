package com.antdesign.swing.painter;

import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Ant Design 圆角边框绘制器。
 *
 * <p>既是 {@link AntPainter} 又是 {@link AbstractBorder}，可同时用于
 * 自定义绘制和 Swing 边框机制。绘制带圆角的矩形边框，支持自定义颜色、
 * 圆角半径和线宽，默认值取自当前主题令牌。
 *
 * <pre>{@code
 * // 作为 Border 使用
 * panel.setBorder(new RoundedBorderPainter());
 *
 * // 自定义参数
 * panel.setBorder(new RoundedBorderPainter(Color.RED, 8, 2f));
 * }</pre>
 *
 * @see AntPainter
 */
public class RoundedBorderPainter extends AbstractBorder implements AntPainter {

  private static final long serialVersionUID = 1L;

  private final Color color;
  private final int arcRadius;
  private final float lineWidth;
  private final Insets insets;

  /**
   * 创建自定义圆角边框。
   *
   * @param color 边框颜色，{@code null} 表示使用主题边框色
   * @param arcRadius 圆角半径，负数表示使用主题默认值
   * @param lineWidth 线宽
   */
  public RoundedBorderPainter(Color color, int arcRadius, float lineWidth) {
    this.color = color;
    this.arcRadius = arcRadius;
    this.lineWidth = Math.max(lineWidth, 0.5f);
    int pad = Math.max((int) Math.ceil(this.lineWidth), 1);
    this.insets = new Insets(pad, pad, pad, pad);
  }

  /**
   * 使用默认主题参数创建圆角边框。
   */
  public RoundedBorderPainter() {
    this(null, -1, 1f);
  }

  /**
   * 使用指定颜色和默认圆角创建边框。
   *
   * @param color 边框颜色
   */
  public RoundedBorderPainter(Color color) {
    this(color, -1, 1f);
  }

  // ---------------------------------------------------------------------------
  // AntPainter 接口
  // ---------------------------------------------------------------------------

  @Override
  public void paint(Graphics2D g2, JComponent component, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      paintBorderShape(g, width, height);
    } finally {
      g.dispose();
    }
  }

  // ---------------------------------------------------------------------------
  // AbstractBorder 覆写
  // ---------------------------------------------------------------------------

  @Override
  public void paintBorder(java.awt.Component c, java.awt.Graphics g,
      int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create(x, y, width, height);
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      paintBorderShape(g2, width, height);
    } finally {
      g2.dispose();
    }
  }

  @Override
  public Insets getBorderInsets(java.awt.Component c) {
    return new Insets(insets.top, insets.left, insets.bottom, insets.right);
  }

  @Override
  public Insets getBorderInsets(java.awt.Component c, Insets insets) {
    insets.set(this.insets.top, this.insets.left, this.insets.bottom, this.insets.right);
    return insets;
  }

  // ---------------------------------------------------------------------------
  // 内部绘制
  // ---------------------------------------------------------------------------

  private void paintBorderShape(Graphics2D g, int width, int height) {
    int arc = resolveArc();
    float halfLine = lineWidth / 2f;

    RoundRectangle2D rect = new RoundRectangle2D.Double(
        halfLine, halfLine,
        width - lineWidth, height - lineWidth,
        arc, arc);

    g.setColor(resolveColor());
    g.setStroke(new BasicStroke(lineWidth));
    g.draw(rect);
  }

  private Color resolveColor() {
    if (color != null) {
      return color;
    }
    ColorToken ct = AntThemeManager.getInstance().getColorToken();
    return ct.getBorderColor();
  }

  private int resolveArc() {
    if (arcRadius >= 0) {
      return arcRadius;
    }
    SizeToken st = AntThemeManager.getInstance().getSizeToken();
    return st.getBorderRadius();
  }
}
