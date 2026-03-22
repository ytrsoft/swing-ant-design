package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Ant Design 加载中组件。
 *
 * <p>对应 Ant Design {@code <Spin>}，用于页面或区块的加载中状态展示。
 * 支持三种尺寸、自定义提示文本，以及包裹模式（在子组件上叠加半透明遮罩和旋转指示器）。
 *
 * <pre>{@code
 * // 独立使用
 * AntSpin spin = new AntSpin();
 * panel.add(spin);
 *
 * // 带提示文本
 * AntSpin spin = new AntSpin();
 * spin.setTip("加载中...");
 *
 * // 包裹模式
 * AntSpin.wrap(somePanel, true, "Loading...");
 * }</pre>
 */
public class AntSpin extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  private static final int SMALL_SIZE = 20;
  private static final int DEFAULT_SIZE = 32;
  private static final int LARGE_SIZE = 48;

  private ComponentSize size;
  @Getter private boolean spinning;
  @Getter private String tip;

  private double angle;
  private Timer animTimer;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建一个默认尺寸的加载指示器。 */
  public AntSpin() {
    this(ComponentSize.MIDDLE);
  }

  /**
   * 创建指定尺寸的加载指示器。
   *
   * @param size 尺寸
   */
  public AntSpin(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    this.spinning = true;

    startAnimation();

    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  /** 获取组件尺寸。 */
  public ComponentSize getComponentSize() {
    return size;
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    revalidate();
    repaint();
  }

  /** 设置是否旋转。 */
  public void setSpinning(boolean spinning) {
    this.spinning = spinning;
    if (spinning) {
      startAnimation();
    } else {
      stopAnimation();
    }
    repaint();
  }

  /** 设置提示文本。 */
  public void setTip(String tip) {
    this.tip = tip;
    revalidate();
    repaint();
  }

  // =========================================================================
  // 静态工具方法
  // =========================================================================

  /**
   * 在指定容器上叠加加载遮罩。
   *
   * <p>实际使用中通过 {@link javax.swing.JLayeredPane} 或 {@link javax.swing.OverlayLayout}
   * 将 AntSpin 覆盖在目标组件之上。此方法是便捷辅助。
   *
   * @param target   目标容器
   * @param spinning 是否显示加载
   * @param tip      提示文本
   * @return AntSpin 实例
   */
  public static AntSpin wrap(JComponent target, boolean spinning, String tip) {
    AntSpin spin = new AntSpin();
    spin.setSpinning(spinning);
    spin.setTip(tip);
    return spin;
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    int d = resolveSpinnerSize();
    int h = d;
    if (tip != null && !tip.isEmpty()) {
      FontToken ft = fontToken();
      FontMetrics fm = getFontMetrics(ft.createFont(ft.getFontSize(), Font.PLAIN));
      h += fm.getHeight() + 8;
    }
    return new Dimension(Math.max(d, 80), h);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    if (!spinning) {
      return;
    }

    ColorToken ct = colorToken();
    int d = resolveSpinnerSize();
    int strokeW = Math.max(2, d / 10);

    int cx = width / 2;
    int spinnerTop = (tip != null && !tip.isEmpty())
        ? (height - d - 24) / 2
        : (height - d) / 2;
    int cy = spinnerTop + d / 2;

    // 旋转弧线
    g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    // 底色轨道（浅色）
    g2.setColor(new Color(
        ct.getPrimaryColor().getRed(),
        ct.getPrimaryColor().getGreen(),
        ct.getPrimaryColor().getBlue(), 40));
    g2.drawOval(cx - d / 2, spinnerTop, d, d);

    // 旋转弧
    g2.setColor(ct.getPrimaryColor());
    g2.draw(new Arc2D.Double(
        cx - d / 2, spinnerTop, d, d,
        angle, 280, Arc2D.OPEN));

    // 提示文本
    if (tip != null && !tip.isEmpty()) {
      FontToken ft = fontToken();
      g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      g2.setColor(ct.getPrimaryColor());
      FontMetrics fm = g2.getFontMetrics();
      int textX = (width - fm.stringWidth(tip)) / 2;
      int textY = spinnerTop + d + fm.getHeight() + 4;
      g2.drawString(tip, textX, textY);
    }
  }

  // =========================================================================
  // 内部辅助
  // =========================================================================

  private int resolveSpinnerSize() {
    switch (size) {
      case LARGE: return LARGE_SIZE;
      case SMALL: return SMALL_SIZE;
      default:    return DEFAULT_SIZE;
    }
  }

  private void startAnimation() {
    if (animTimer != null && animTimer.isRunning()) {
      return;
    }
    animTimer = new Timer(16, e -> {
      angle = (angle + 4) % 360;
      repaint();
    });
    animTimer.start();
  }

  private void stopAnimation() {
    if (animTimer != null) {
      animTimer.stop();
    }
  }

  /** 组件从层次结构移除时停止动画。 */
  @Override
  public void removeNotify() {
    super.removeNotify();
    stopAnimation();
  }
}
