package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Ant Design 头像组件。
 *
 * <p>对应 Ant Design {@code <Avatar>}，支持图片、图标或文字展示。
 * 可设置圆形或方形，三种尺寸。
 *
 * <pre>{@code
 * AntAvatar avatar = new AntAvatar("U");
 * AntAvatar imgAvatar = new AntAvatar(myImage);
 * }</pre>
 */
public class AntAvatar extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 头像形状。 */
  public enum Shape { CIRCLE, SQUARE }

  @Getter private String text;
  @Getter private Icon icon;
  @Getter private Image image;
  @Getter private Shape shape;
  @Getter private Color backgroundColor;
  private ComponentSize size;

  /**
   * 创建文字头像。
   *
   * @param text 显示文字
   */
  public AntAvatar(String text) {
    this.text = (text != null) ? text : "";
    this.shape = Shape.CIRCLE;
    this.size = ComponentSize.MIDDLE;
    // Theme listener handled by base class
  }

  /**
   * 创建图片头像。
   *
   * @param image 图片
   */
  public AntAvatar(Image image) {
    this("");
    this.image = image;
  }

  /**
   * 创建图标头像。
   *
   * @param icon 图标
   */
  public AntAvatar(Icon icon) {
    this("");
    this.icon = icon;
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setText(String text) {
    this.text = (text != null) ? text : "";
    repaint();
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
    repaint();
  }

  public void setImage(Image image) {
    this.image = image;
    repaint();
  }

  public void setShape(Shape shape) {
    this.shape = (shape != null) ? shape : Shape.CIRCLE;
    repaint();
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
    repaint();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    revalidate();
    repaint();
  }

  public ComponentSize getComponentSize() {
    return size;
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    int s = resolvePixelSize();
    return new Dimension(s, s);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    int w = width;
    int h = height;
    int s = Math.min(w, h);
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    Color bg = (backgroundColor != null) ? backgroundColor : ct.getTextTertiaryColor();

    // 背景
    if (shape == Shape.CIRCLE) {
      g2.setColor(bg);
      g2.fill(new Ellipse2D.Float(0, 0, s, s));
    } else {
      int arc = sizeToken().getBorderRadius();
      g2.setColor(bg);
      g2.fillRoundRect(0, 0, s, s, arc, arc);
    }

    // 图片
    if (image != null) {
      Graphics2D gc = (Graphics2D) g2.create();
      if (shape == Shape.CIRCLE) {
        gc.setClip(new Ellipse2D.Float(0, 0, s, s));
      }
      gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      gc.drawImage(image, 0, 0, s, s, null);
      gc.dispose();
      return;
    }

    // 图标
    if (icon != null) {
      int ix = (s - icon.getIconWidth()) / 2;
      int iy = (s - icon.getIconHeight()) / 2;
      icon.paintIcon(this, g2, ix, iy);
      return;
    }

    // 文字
    if (!text.isEmpty()) {
      int fontSize = resolvePixelSize() / 2;
      g2.setFont(ft.createFont(Math.max(12, fontSize), Font.PLAIN));
      g2.setColor(Color.WHITE);
      FontMetrics fm = g2.getFontMetrics();
      String display = text.length() > 2 ? text.substring(0, 2) : text;
      int tx = (s - fm.stringWidth(display)) / 2;
      int ty = (s + fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(display, tx, ty);
    }
  }

  private int resolvePixelSize() {
    switch (size) {
      case LARGE:  return 40;
      case SMALL:  return 24;
      default:     return 32;
    }
  }
}
