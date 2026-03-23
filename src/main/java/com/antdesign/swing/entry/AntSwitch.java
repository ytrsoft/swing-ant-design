package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 开关组件。
 *
 * <p>对应 Ant Design {@code <Switch>}。
 *
 * <pre>{@code
 * AntSwitch sw = new AntSwitch();
 * sw.addChangeListener(e -> System.out.println("On: " + e.getNewValue()));
 * }</pre>
 */
public class AntSwitch extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private boolean checked;
  @Getter private boolean loading;
  private ComponentSize size;
  @Getter private String checkedText;
  @Getter private String uncheckedText;

  private final List<AntChangeListener<Boolean>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建开关。 */
  public AntSwitch() {
    this(false);
  }

  /**
   * 创建开关。
   *
   * @param checked 初始状态
   */
  public AntSwitch(boolean checked) {
    this.checked = checked;
    this.size = ComponentSize.MIDDLE;
    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (isEnabled() && !loading) {
          toggle();
        }
      }
    });
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setChecked(boolean checked) {
    boolean old = this.checked;
    this.checked = checked;
    repaint();
    if (old != checked) {
      AntChangeEvent<Boolean> evt = new AntChangeEvent<>(this, old, checked);
      for (AntChangeListener<Boolean> l : changeListeners) {
        l.valueChanged(evt);
      }
    }
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
    repaint();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    revalidate();
    repaint();
  }

  public ComponentSize getComponentSize() { return size; }

  public void setCheckedText(String checkedText) {
    this.checkedText = checkedText;
    repaint();
  }

  public void setUncheckedText(String uncheckedText) {
    this.uncheckedText = uncheckedText;
    repaint();
  }

  /** 切换状态。 */
  public void toggle() {
    setChecked(!checked);
  }

  public void addChangeListener(AntChangeListener<Boolean> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public void removeChangeListener(AntChangeListener<Boolean> listener) {
    changeListeners.remove(listener);
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    boolean small = (size == ComponentSize.SMALL);
    int baseW = small ? 28 : 44;
    int baseH = small ? 16 : 22;

    // 如果有 checkedText / uncheckedText，需要加宽轨道以容纳文本
    String longerText = longerOf(checkedText, uncheckedText);
    if (longerText != null && !longerText.isEmpty()) {
      FontMetrics fm = getFontMetrics(
          fontToken().createFont(small ? 10 : 12, java.awt.Font.PLAIN));
      int textW = fm.stringWidth(longerText);
      baseW = Math.max(baseW, textW + (baseH - 4) + 10); // 手柄宽 + padding
    }
    return new Dimension(baseW, baseH);
  }

  /** 返回两个字符串中较长的一个。 */
  private static String longerOf(String a, String b) {
    int la = (a != null) ? a.length() : 0;
    int lb = (b != null) ? b.length() : 0;
    return la >= lb ? a : b;
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    int w = width;
    int h = height;
    int arc = h;

    // 轨道
    Color trackColor;
    if (!isEnabled()) {
      trackColor = ct.getFillColor();
    } else if (checked) {
      trackColor = ct.getPrimaryColor();
    } else {
      trackColor = ct.getFillColor();
    }
    g2.setColor(trackColor);
    g2.fillRoundRect(0, 0, w, h, arc, arc);

    // 边框（暗色模式下增强可见性）
    g2.setColor(ct.getBorderColor());
    g2.setStroke(new java.awt.BasicStroke(0.5f));
    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

    // 手柄
    int handleSize = h - 4;
    int handleX = checked ? (w - handleSize - 2) : 2;
    g2.setColor(Color.WHITE);
    g2.fillOval(handleX, 2, handleSize, handleSize);

    // checkedText / uncheckedText — 显示在手柄对面
    String displayText = checked ? checkedText : uncheckedText;
    if (displayText != null && !displayText.isEmpty()) {
      boolean small = (size == ComponentSize.SMALL);
      g2.setFont(fontToken().createFont(small ? 10 : 12, java.awt.Font.PLAIN));
      g2.setColor(Color.WHITE);
      FontMetrics fm = g2.getFontMetrics();
      int textW = fm.stringWidth(displayText);
      int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
      int textX;
      if (checked) {
        // 文本在左侧（手柄在右）
        textX = (handleX - textW) / 2;
      } else {
        // 文本在右侧（手柄在左）
        textX = handleX + handleSize + (w - handleX - handleSize - textW) / 2;
      }
      g2.drawString(displayText, textX, textY);
    }

    // 加载指示
    if (loading) {
      g2.setColor(checked ? Color.WHITE : ct.getPrimaryColor());
      g2.setStroke(new java.awt.BasicStroke(1.5f));
      int cx = handleX + handleSize / 2;
      int cy = 2 + handleSize / 2;
      int r = handleSize / 2 - 3;
      g2.drawArc(cx - r, cy - r, r * 2, r * 2, 0, 270);
    }
  }
}
