package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 水印组件。
 *
 * <p>对应 Ant Design {@code <Watermark>}，在页面上覆盖半透明的水印文字，
 * 用于敏感信息防截图溯源。作为透明覆盖层叠加在内容之上。
 *
 * <pre>{@code
 * AntWatermark watermark = new AntWatermark("Confidential");
 * watermark.setBounds(0, 0, panel.getWidth(), panel.getHeight());
 * layeredPane.add(watermark, JLayeredPane.POPUP_LAYER);
 * }</pre>
 */
public class AntWatermark extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String content;
  @Getter private int fontSize;
  @Getter private float alpha;
  @Getter private double rotate;
  @Getter private int gapX;
  @Getter private int gapY;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建水印。
   *
   * @param content 水印文本
   */
  public AntWatermark(String content) {
    this.content = (content != null) ? content : "";
    this.fontSize = 14;
    this.alpha = 0.12f;
    this.rotate = -22;
    this.gapX = 180;
    this.gapY = 120;

  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setContent(String content) {
    this.content = (content != null) ? content : "";
    repaint();
  }

  public void setFontSize(int fontSize) {
    this.fontSize = Math.max(8, fontSize);
    repaint();
  }

  /** 设置透明度（0.0 ~ 1.0）。 */
  public void setAlpha(float alpha) {
    this.alpha = Math.max(0f, Math.min(1f, alpha));
    repaint();
  }

  /** 设置旋转角度（度）。 */
  public void setRotate(double rotate) {
    this.rotate = rotate;
    repaint();
  }

  /** 设置水平间距 (px)。 */
  public void setGapX(int gapX) {
    this.gapX = Math.max(20, gapX);
    repaint();
  }

  /** 设置垂直间距 (px)。 */
  public void setGapY(int gapY) {
    this.gapY = Math.max(20, gapY);
    repaint();
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    if (content.isEmpty()) {
      return;
    }

    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    FontToken ft = fontToken();
    Font font = ft.createFont(fontSize, Font.PLAIN);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics();

    int textWidth = fm.stringWidth(content);
    int textHeight = fm.getHeight();

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(new Color(0, 0, 0));

    // 扩大遍历范围以覆盖旋转后的边角
    int expandX = height;
    int expandY = width;

    for (int y = -expandY; y < height + expandY; y += gapY + textHeight) {
      for (int x = -expandX; x < width + expandX; x += gapX + textWidth) {
        Graphics2D rotated = (Graphics2D) g2.create();
        rotated.translate(x + textWidth / 2.0, y + textHeight / 2.0);
        rotated.rotate(Math.toRadians(rotate));
        rotated.drawString(content, -textWidth / 2, textHeight / 4);
        rotated.dispose();
      }
    }
  }

  /** 水印层不消费鼠标事件，让下层组件可正常交互。 */
  @Override
  public boolean contains(int x, int y) {
    return false;
  }
}
