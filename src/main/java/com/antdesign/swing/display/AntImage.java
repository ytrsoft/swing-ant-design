package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 图片组件。
 *
 * <p>对应 Ant Design {@code <Image>}，可预览的图片展示。
 * 支持加载失败回退、预览弹窗和自定义宽高。
 *
 * <pre>{@code
 * AntImage img = new AntImage(myImage, 200, 150);
 * img.setPreview(true);
 * }</pre>
 */
public class AntImage extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private Image image;
  @Getter private int imageWidth;
  @Getter private int imageHeight;
  @Getter private String alt;
  @Getter private String fallback;
  @Getter private boolean preview;
  @Getter private boolean rounded;

  private boolean hovered;

  /**
   * 创建图片组件。
   *
   * @param image  图片
   * @param width  显示宽度
   * @param height 显示高度
   */
  public AntImage(Image image, int width, int height) {
    this.image = image;
    this.imageWidth = width;
    this.imageHeight = height;
    this.preview = true;
    this.alt = "";

    installMouseListener();
    // Theme listener handled by base class
  }

  /**
   * 创建自适应大小的图片组件。
   *
   * @param image 图片
   */
  public AntImage(Image image) {
    this(image, -1, -1);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setImage(Image image) {
    this.image = image;
    revalidate();
    repaint();
  }

  public void setImageWidth(int imageWidth) {
    this.imageWidth = imageWidth;
    revalidate();
    repaint();
  }

  public void setImageHeight(int imageHeight) {
    this.imageHeight = imageHeight;
    revalidate();
    repaint();
  }

  public void setAlt(String alt) {
    this.alt = (alt != null) ? alt : "";
    repaint();
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
    setCursor(preview
        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        : Cursor.getDefaultCursor());
  }

  public void setRounded(boolean rounded) {
    this.rounded = rounded;
    repaint();
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    int w = imageWidth > 0 ? imageWidth : (image != null ? image.getWidth(null) : 200);
    int h = imageHeight > 0 ? imageHeight : (image != null ? image.getHeight(null) : 200);
    return new Dimension(Math.max(w, 1), Math.max(h, 1));
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    int w = width;
    int h = height;
    SizeToken st = sizeToken();
    ColorToken ct = colorToken();
    int arc = rounded ? st.getBorderRadiusLg() : 0;

    if (rounded && arc > 0) {
      g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
    }

    if (image != null) {
      g2.drawImage(image, 0, 0, w, h, null);
    } else {
      // 回退显示
      g2.setColor(ct.getFillSecondaryColor());
      g2.fillRect(0, 0, w, h);
      if (!alt.isEmpty()) {
        g2.setColor(ct.getTextTertiaryColor());
        GraphicsUtils.drawCenteredText(g2, alt, 0, 0, w, h);
      }
    }

    // 悬浮遮罩
    if (preview && hovered && image != null) {
      g2.setColor(new Color(0, 0, 0, 80));
      g2.fillRect(0, 0, w, h);
      g2.setColor(Color.WHITE);
      GraphicsUtils.drawCenteredText(g2, "Preview", 0, 0, w, h);
    }
  }

  // =========================================================================
  // 内部
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
        repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (preview && image != null) {
          showPreviewDialog();
        }
      }
    });
  }

  private void showPreviewDialog() {
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame == null) {
      return;
    }
    JDialog dialog = new JDialog(frame, "Image Preview", true);
    int iw = image.getWidth(null);
    int ih = image.getHeight(null);
    int maxW = Math.min(iw, 800);
    int maxH = Math.min(ih, 600);
    double scale = Math.min((double) maxW / iw, (double) maxH / ih);
    int dw = (int) (iw * scale);
    int dh = (int) (ih * scale);

    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, dw, dh, null);
      }
    };
    panel.setPreferredSize(new Dimension(dw, dh));
    panel.setBackground(Color.BLACK);
    dialog.setContentPane(panel);
    dialog.pack();
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }
}
