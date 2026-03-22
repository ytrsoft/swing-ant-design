package com.antdesign.swing.util;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Graphics2D 绘制工具类。
 *
 * <p>封装 Ant Design 组件中高频使用的绘制操作：抗锯齿设置、圆角矩形、焦点外发光、
 * 虚线边框、阴影等。所有方法均为无副作用的静态方法，不会改变传入 {@link Graphics2D} 的
 * 持久状态（内部使用 {@code create()} 拷贝）。
 */
public final class GraphicsUtils {

  /** Ant Design 默认圆角半径 (px)。 */
  public static final int DEFAULT_ARC = 6;

  /** Ant Design 默认边框宽度 (px)。 */
  public static final float DEFAULT_BORDER_WIDTH = 1f;

  private GraphicsUtils() {}

  /**
   * 为 Graphics2D 开启高质量抗锯齿渲染。
   *
   * @param g2 目标 Graphics2D（会被原地修改）
   */
  public static void setupAntialiasing(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
        RenderingHints.VALUE_STROKE_PURE);
  }

  /**
   * 填充圆角矩形。
   *
   * @param g2    Graphics2D
   * @param x     左上角 x
   * @param y     左上角 y
   * @param w     宽度
   * @param h     高度
   * @param arc   圆角半径
   * @param color 填充颜色
   */
  public static void fillRoundRect(Graphics2D g2, int x, int y, int w, int h,
      int arc, Color color) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
      g.setColor(color);
      g.fill(new RoundRectangle2D.Float(x, y, w, h, arc, arc));
    } finally {
      g.dispose();
    }
  }

  /**
   * 绘制圆角矩形边框。
   *
   * @param g2          Graphics2D
   * @param x           左上角 x
   * @param y           左上角 y
   * @param w           宽度
   * @param h           高度
   * @param arc         圆角半径
   * @param borderColor 边框颜色
   * @param borderWidth 边框宽度
   */
  public static void drawRoundRect(Graphics2D g2, int x, int y, int w, int h,
      int arc, Color borderColor, float borderWidth) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
      g.setColor(borderColor);
      g.setStroke(new BasicStroke(borderWidth));
      float half = borderWidth / 2f;
      g.draw(new RoundRectangle2D.Float(
          x + half, y + half, w - borderWidth, h - borderWidth, arc, arc));
    } finally {
      g.dispose();
    }
  }

  /**
   * 绘制 Ant Design 焦点外发光环。
   *
   * <p>在组件外围绘制一圈半透明色环，模拟 CSS {@code box-shadow: 0 0 0 2px rgba(...)}.
   *
   * @param g2    Graphics2D
   * @param x     组件左上角 x
   * @param y     组件左上角 y
   * @param w     组件宽度
   * @param h     组件高度
   * @param arc   组件圆角半径
   * @param color 焦点色（会自动应用 25% 透明度）
   * @param spread 外发光扩散宽度 (px)，Ant Design 默认为 2
   */
  public static void drawFocusRing(Graphics2D g2, int x, int y, int w, int h,
      int arc, Color color, int spread) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
      Color ringColor = ColorUtils.withAlpha(color, 0.25f);
      g.setColor(ringColor);
      int outerArc = arc + spread;
      Shape outer = new RoundRectangle2D.Float(
          x - spread, y - spread, w + spread * 2, h + spread * 2, outerArc, outerArc);
      Shape inner = new RoundRectangle2D.Float(x, y, w, h, arc, arc);
      Area ring = new Area(outer);
      ring.subtract(new Area(inner));
      g.fill(ring);
    } finally {
      g.dispose();
    }
  }

  /**
   * 绘制虚线圆角边框（用于 dashed 按钮）。
   *
   * @param g2          Graphics2D
   * @param x           左上角 x
   * @param y           左上角 y
   * @param w           宽度
   * @param h           高度
   * @param arc         圆角半径
   * @param borderColor 边框颜色
   */
  public static void drawDashedRoundRect(Graphics2D g2, int x, int y, int w, int h,
      int arc, Color borderColor) {
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
      g.setColor(borderColor);
      float[] dash = {4f, 4f};
      g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
          10f, dash, 0f));
      g.draw(new RoundRectangle2D.Float(
          x + 0.5f, y + 0.5f, w - 1f, h - 1f, arc, arc));
    } finally {
      g.dispose();
    }
  }

  /**
   * 绘制矩形投影（用于弹出层、卡片等）。
   *
   * @param g2      Graphics2D
   * @param x       组件左上角 x
   * @param y       组件左上角 y
   * @param w       组件宽度
   * @param h       组件高度
   * @param arc     圆角半径
   * @param shadow  阴影颜色（含透明度）
   * @param offsetY 阴影 Y 偏移
   * @param blur    模糊半径（通过多层半透明模拟）
   */
  public static void drawShadow(Graphics2D g2, int x, int y, int w, int h,
      int arc, Color shadow, int offsetY, int blur) {
    if (blur <= 0) {
      return;
    }
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
        int baseAlpha = shadow.getAlpha();
      for (int i = blur; i > 0; i--) {
        float ratio = (float) i / blur;
        int alpha = Math.round(baseAlpha * (1f - ratio) * 0.5f);
        if (alpha <= 0) {
          continue;
        }
        g.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), alpha));
          g.fill(new RoundRectangle2D.Float(
            x - i, y - i + offsetY,
            w + i * 2, h + i * 2,
            arc + i, arc + i));
      }
    } finally {
      g.dispose();
    }
  }

  /**
   * 在指定区域内绘制居中文本。
   *
   * @param g2   Graphics2D
   * @param text 文本
   * @param x    区域左上角 x
   * @param y    区域左上角 y
   * @param w    区域宽度
   * @param h    区域高度
   */
  public static void drawCenteredText(Graphics2D g2, String text,
      int x, int y, int w, int h) {
    if (text == null || text.isEmpty()) {
      return;
    }
    Graphics2D g = (Graphics2D) g2.create();
    try {
      setupAntialiasing(g);
      java.awt.FontMetrics fm = g.getFontMetrics();
      int textWidth = fm.stringWidth(text);
      int textX = x + (w - textWidth) / 2;
      int textY = y + (h - fm.getHeight()) / 2 + fm.getAscent();
      g.drawString(text, textX, textY);
    } finally {
      g.dispose();
    }
  }

  /**
   * 以指定透明度绘制内容。
   *
   * @param g2    Graphics2D
   * @param alpha 透明度 0.0 ~ 1.0
   * @return 设置了透明度的 Graphics2D 副本，调用方需自行 {@code dispose()}
   */
  public static Graphics2D withAlpha(Graphics2D g2, float alpha) {
    Graphics2D g = (Graphics2D) g2.create();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    return g;
  }

  /**
   * 绘制着色图标（HiDPI 安全）。
   *
   * <p>在真实像素分辨率下对图标进行 {@link AlphaComposite#SrcAtop} 着色，
   * 然后以逻辑尺寸绘制回目标 Graphics2D。保证在 Retina / HiDPI 屏幕上不模糊。
   *
   * @param g2    目标 Graphics2D
   * @param comp  宿主组件（用于 ImageObserver）
   * @param icon  图标
   * @param x     绘制 x（逻辑坐标）
   * @param y     绘制 y（逻辑坐标）
   * @param tint  着色颜色
   */
  public static void paintTintedIcon(Graphics2D g2, Component comp,
      Icon icon, int x, int y, Color tint) {
    int logW = icon.getIconWidth();
    int logH = icon.getIconHeight();
    if (logW <= 0 || logH <= 0) {
      return;
    }

    // 获取真实像素尺寸 — HiDpiImageIcon 内部存的是高分辨率图像
    int realW = logW;
    int realH = logH;
    Image srcImage = null;
    if (icon instanceof ImageIcon) {
      srcImage = ((ImageIcon) icon).getImage();
      int imgW = srcImage.getWidth(null);
      int imgH = srcImage.getHeight(null);
      if (imgW > 0 && imgH > 0) {
        realW = imgW;
        realH = imgH;
      }
    }

    // 在真实像素分辨率下创建 buffer 并着色
    BufferedImage buffer = new BufferedImage(realW, realH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D ig = buffer.createGraphics();
    try {
      setupAntialiasing(ig);
      if (srcImage != null) {
        // 直接绘制原始图像到 buffer（1:1 像素，无缩放）
        ig.drawImage(srcImage, 0, 0, realW, realH, null);
      } else {
        // 非 ImageIcon — 缩放绘制
        double sx = (double) realW / logW;
        double sy = (double) realH / logH;
        ig.scale(sx, sy);
        icon.paintIcon(comp, ig, 0, 0);
      }
      // SrcAtop：只着色已有像素区域，保留原始 alpha
      ig.setComposite(AlphaComposite.SrcAtop);
      ig.setColor(tint);
      ig.fillRect(0, 0, realW, realH);
    } finally {
      ig.dispose();
    }

    // 以逻辑尺寸绘制回目标 — Graphics2D 的显示变换会映射到物理像素
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(buffer, x, y, logW, logH, comp);
  }
}
