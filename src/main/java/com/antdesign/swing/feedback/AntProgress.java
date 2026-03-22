package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Ant Design 进度条组件。
 *
 * <p>对应 Ant Design {@code <Progress>}，支持线形（line）和环形（circle）两种类型。
 *
 * <pre>{@code
 * // 线形进度条
 * AntProgress line = new AntProgress(75);
 *
 * // 环形进度条
 * AntProgress circle = new AntProgress(80, AntProgress.ProgressType.CIRCLE);
 *
 * // 带状态
 * AntProgress success = new AntProgress(100);
 * success.setStatus(Status.SUCCESS);
 * }</pre>
 */
@Getter
public class AntProgress extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 进度条类型。 */
  public enum ProgressType {
    LINE, CIRCLE
  }

  /** 线形进度条默认高度 (px)。 */
  private static final int LINE_HEIGHT = 8;

  /** 环形默认直径 (px)。 */
  private static final int CIRCLE_SIZE = 120;

  private double percent;
  private ProgressType progressType;
  private Status status;
  private boolean showInfo;
  private int strokeWidth;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建线形进度条。
   *
   * @param percent 百分比（0-100）
   */
  public AntProgress(double percent) {
    this(percent, ProgressType.LINE);
  }

  /**
   * 创建指定类型的进度条。
   *
   * @param percent      百分比（0-100）
   * @param progressType 进度条类型
   */
  public AntProgress(double percent, ProgressType progressType) {
    this.percent = Math.max(0, Math.min(100, percent));
    this.progressType = (progressType != null) ? progressType : ProgressType.LINE;
    this.showInfo = true;
    this.strokeWidth = (this.progressType == ProgressType.CIRCLE) ? 6 : LINE_HEIGHT;

    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setPercent(double percent) {
    this.percent = Math.max(0, Math.min(100, percent));
    repaint();
  }

  public void setProgressType(ProgressType progressType) {
    this.progressType = (progressType != null) ? progressType : ProgressType.LINE;
    revalidate();
    repaint();
  }

  public void setStatus(Status status) {
    this.status = status;
    repaint();
  }

  public void setShowInfo(boolean showInfo) {
    this.showInfo = showInfo;
    revalidate();
    repaint();
  }

  public void setStrokeWidth(int strokeWidth) {
    this.strokeWidth = Math.max(2, strokeWidth);
    revalidate();
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
    if (progressType == ProgressType.CIRCLE) {
      return new Dimension(CIRCLE_SIZE, CIRCLE_SIZE);
    }
    int textWidth = showInfo ? 48 : 0;
    return new Dimension(300 + textWidth, Math.max(strokeWidth + 4, 22));
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    if (progressType == ProgressType.CIRCLE) {
      paintCircle(g2);
    } else {
      paintLine(g2);
    }
  }

  private void paintLine(Graphics2D g2) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    int textWidth = showInfo ? 48 : 0;
    int barWidth = getWidth() - textWidth;
    int barY = (getHeight() - strokeWidth) / 2;
    int arc = strokeWidth;

    // 轨道
    g2.setColor(ct.getBorderSecondaryColor());
    g2.fillRoundRect(0, barY, barWidth, strokeWidth, arc, arc);

    // 进度
    int progressWidth = (int) (barWidth * percent / 100.0);
    if (progressWidth > 0) {
      g2.setColor(resolveColor());
      g2.fillRoundRect(0, barY, progressWidth, strokeWidth, arc, arc);
    }

    // 百分比文本
    if (showInfo) {
      g2.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
      g2.setColor(ct.getTextSecondaryColor());
      String text = (int) percent + "%";
      FontMetrics fm = g2.getFontMetrics();
      int textX = barWidth + 8;
      int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(text, textX, textY);
    }
  }

  private void paintCircle(Graphics2D g2) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    int size = Math.min(getWidth(), getHeight());
    int pad = strokeWidth / 2 + 2;

    // 轨道
    g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setColor(ct.getBorderSecondaryColor());
    g2.drawOval(pad, pad, size - pad * 2, size - pad * 2);

    // 进度弧
    double angle = percent / 100.0 * 360.0;
    g2.setColor(resolveColor());
    g2.draw(new Arc2D.Double(pad, pad, size - pad * 2, size - pad * 2,
        90, -angle, Arc2D.OPEN));

    // 中心百分比
    if (showInfo) {
      g2.setFont(ft.createFont(ft.getFontSizeHeading4(), Font.BOLD));
      g2.setColor(ct.getTextColor());
      String text = (int) percent + "%";
      FontMetrics fm = g2.getFontMetrics();
      int textX = (size - fm.stringWidth(text)) / 2;
      int textY = (size + fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(text, textX, textY);
    }
  }

  private Color resolveColor() {
    ColorToken ct = colorToken();
    if (status != null) {
      return status.getDefaultColor();
    }
    if (percent >= 100) {
      return ct.getSuccessColor();
    }
    return ct.getPrimaryColor();
  }
}
