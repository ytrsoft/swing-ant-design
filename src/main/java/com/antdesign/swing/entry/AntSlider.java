package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 滑动输入条组件。
 *
 * <p>对应 Ant Design {@code <Slider>}。
 *
 * <pre>{@code
 * AntSlider slider = new AntSlider(0, 100, 50);
 * slider.addChangeListener(e -> System.out.println(e.getNewValue()));
 * }</pre>
 */
public class AntSlider extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int TRACK_HEIGHT = 4;
  private static final int HANDLE_SIZE = 14;

  @Getter private double min;
  @Getter private double max;
  @Getter private double value;
  @Getter private double step;
  @Getter private boolean vertical;
  @Getter private boolean showTooltip;

  private boolean dragging;
  private final List<AntChangeListener<Double>> changeListeners = new CopyOnWriteArrayList<>();

  /**
   * 创建滑动条。
   *
   * @param min   最小值
   * @param max   最大值
   * @param value 初始值
   */
  public AntSlider(double min, double max, double value) {
    this.min = min;
    this.max = Math.max(min + 1, max);
    this.value = Math.max(min, Math.min(max, value));
    this.step = 1;
    this.showTooltip = true;

    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMouse();
    // Theme listener handled by base class
  }

  public AntSlider() {
    this(0, 100, 0);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(double value) {
    double old = this.value;
    this.value = snap(Math.max(min, Math.min(max, value)));
    repaint();
    if (old != this.value) {
      AntChangeEvent<Double> evt = new AntChangeEvent<>(this, old, this.value);
      for (AntChangeListener<Double> l : changeListeners) {
        l.valueChanged(evt);
      }
    }
  }

  public void setMin(double min) { this.min = min; repaint(); }
  public void setMax(double max) { this.max = max; repaint(); }
  public void setStep(double step) { this.step = Math.max(0.001, step); }

  public void addChangeListener(AntChangeListener<Double> listener) {
    if (listener != null) {
      changeListeners.add(listener);
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
    return new Dimension(200, HANDLE_SIZE + 10);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    int w = width;
    int h = height;
    int trackY = (h - TRACK_HEIGHT) / 2;
    int usable = w - HANDLE_SIZE;

    // 轨道
    g2.setColor(ct.getFillSecondaryColor());
    g2.fillRoundRect(HANDLE_SIZE / 2, trackY, usable, TRACK_HEIGHT,
        TRACK_HEIGHT, TRACK_HEIGHT);

    // 已填充部分
    double ratio = (value - min) / (max - min);
    int filled = (int) (usable * ratio);
    g2.setColor(isEnabled() ? ct.getPrimaryColor() : ct.getDisabledColor());
    g2.fillRoundRect(HANDLE_SIZE / 2, trackY, filled, TRACK_HEIGHT,
        TRACK_HEIGHT, TRACK_HEIGHT);

    // 手柄
    int handleX = (int) (ratio * usable);
    int handleY = (h - HANDLE_SIZE) / 2;
    g2.setColor(Color.WHITE);
    g2.fillOval(handleX, handleY, HANDLE_SIZE, HANDLE_SIZE);
    g2.setColor(isEnabled() ? ct.getPrimaryColor() : ct.getBorderColor());
    g2.setStroke(new java.awt.BasicStroke(2f));
    g2.drawOval(handleX, handleY, HANDLE_SIZE - 1, HANDLE_SIZE - 1);

    // Tooltip
    if (dragging && showTooltip) {
      String txt = formatValue();
      java.awt.FontMetrics fm = g2.getFontMetrics();
      int tw = fm.stringWidth(txt) + 8;
      int th = fm.getHeight() + 4;
      int tx = handleX + HANDLE_SIZE / 2 - tw / 2;
      int ty = handleY - th - 4;

      g2.setColor(ct.getBgSpotlight());
      g2.fillRoundRect(tx, ty, tw, th, 4, 4);
      g2.setColor(Color.WHITE);
      g2.drawString(txt, tx + 4, ty + fm.getAscent() + 2);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void installMouse() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (isEnabled()) {
          dragging = true;
          updateFromMouse(e.getX());
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        dragging = false;
        repaint();
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if (dragging) {
          updateFromMouse(e.getX());
        }
      }
    });
  }

  private void updateFromMouse(int mouseX) {
    int usable = getWidth() - HANDLE_SIZE;
    double ratio = (double) (mouseX - HANDLE_SIZE / 2) / usable;
    ratio = Math.max(0, Math.min(1, ratio));
    setValue(min + ratio * (max - min));
  }

  private double snap(double v) {
    if (step > 0) {
      return Math.round((v - min) / step) * step + min;
    }
    return v;
  }

  private String formatValue() {
    if (value == Math.floor(value)) {
      return String.valueOf((int) value);
    }
    return String.format("%.1f", value);
  }
}
