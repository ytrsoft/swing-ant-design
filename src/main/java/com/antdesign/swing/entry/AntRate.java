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
import java.awt.geom.Path2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 评分组件。
 *
 * <p>对应 Ant Design {@code <Rate>}，支持半星和自定义星数。
 *
 * <pre>{@code
 * AntRate rate = new AntRate();
 * rate.setValue(3.5);
 * rate.setAllowHalf(true);
 * }</pre>
 */
public class AntRate extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int STAR_SIZE = 22;
  private static final int GAP = 6;
  private static final Color STAR_COLOR = new Color(0xFA, 0xDB, 0x14);

  @Getter private double value;
  @Getter private int count;
  @Getter private boolean allowHalf;
  @Getter private boolean allowClear;

  private double hoverValue = -1;
  private final List<AntChangeListener<Double>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建评分组件。 */
  public AntRate() {
    this.count = 5;
    this.allowClear = true;
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMouse();
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(double value) {
    double old = this.value;
    this.value = Math.max(0, Math.min(count, value));
    repaint();
    if (old != this.value) {
      AntChangeEvent<Double> evt = new AntChangeEvent<>(this, old, this.value);
      for (AntChangeListener<Double> l : changeListeners) {
        l.valueChanged(evt);
      }
    }
  }

  public void setCount(int count) {
    this.count = Math.max(1, count);
    revalidate();
    repaint();
  }

  public void setAllowHalf(boolean allowHalf) { this.allowHalf = allowHalf; }
  public void setAllowClear(boolean allowClear) { this.allowClear = allowClear; }

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
    return new Dimension(count * (STAR_SIZE + GAP) - GAP, STAR_SIZE + 4);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    double display = (hoverValue >= 0) ? hoverValue : value;

    for (int i = 0; i < count; i++) {
      int x = i * (STAR_SIZE + GAP);
      int y = 2;
      double starVal = i + 1;

      if (display >= starVal) {
        // 满星
        g2.setColor(STAR_COLOR);
        fillStar(g2, x, y, STAR_SIZE);
      } else if (display > starVal - 1) {
        // 半星
        g2.setColor(ct.getFillSecondaryColor());
        fillStar(g2, x, y, STAR_SIZE);
        java.awt.Shape oldClip = g2.getClip();
        g2.clipRect(x, y, STAR_SIZE / 2, STAR_SIZE);
        g2.setColor(STAR_COLOR);
        fillStar(g2, x, y, STAR_SIZE);
        g2.setClip(oldClip);
      } else {
        // 空星
        g2.setColor(ct.getFillSecondaryColor());
        fillStar(g2, x, y, STAR_SIZE);
      }
    }
  }

  private void fillStar(Graphics2D g2, int x, int y, int size) {
    Path2D star = new Path2D.Double();
    double cx = x + size / 2.0;
    double cy = y + size / 2.0;
    double outerR = size / 2.0;
    double innerR = outerR * 0.38;

    for (int i = 0; i < 10; i++) {
      double angle = Math.PI / 2.0 + i * Math.PI / 5.0;
      double r = (i % 2 == 0) ? outerR : innerR;
      double px = cx + r * Math.cos(angle);
      double py = cy - r * Math.sin(angle);
      if (i == 0) {
        star.moveTo(px, py);
      } else {
        star.lineTo(px, py);
      }
    }
    star.closePath();
    g2.fill(star);
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void installMouse() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (!isEnabled()) {
          return;
        }
        double clicked = calcValue(e.getX());
        if (allowClear && clicked == value) {
          setValue(0);
        } else {
          setValue(clicked);
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        hoverValue = -1;
        repaint();
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (isEnabled()) {
          hoverValue = calcValue(e.getX());
          repaint();
        }
      }
    });
  }

  private double calcValue(int mouseX) {
    for (int i = 0; i < count; i++) {
      int sx = i * (STAR_SIZE + GAP);
      if (mouseX >= sx && mouseX < sx + STAR_SIZE) {
        if (allowHalf && mouseX < sx + STAR_SIZE / 2) {
          return i + 0.5;
        }
        return i + 1;
      }
    }
    return value;
  }
}
