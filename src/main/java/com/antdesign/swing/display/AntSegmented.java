package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 分段控制器组件。
 *
 * <p>对应 Ant Design {@code <Segmented>}，分段控制器，用作非此即彼的选择操作。
 *
 * <pre>{@code
 * AntSegmented seg = new AntSegmented(new String[]{"Daily", "Weekly", "Monthly"});
 * seg.addChangeListener(e -> System.out.println("Selected: " + e.getNewValue()));
 * }</pre>
 */
public class AntSegmented extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String[] options;
  @Getter private int selectedIndex;
  @Getter private boolean block;
  private ComponentSize size;

  private int hoveredIndex = -1;
  private final List<AntChangeListener<String>> changeListeners = new CopyOnWriteArrayList<>();

  /**
   * 创建分段控制器。
   *
   * @param options 选项数组
   */
  public AntSegmented(String[] options) {
    this.options = (options != null) ? options : new String[0];
    this.size = ComponentSize.MIDDLE;
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    installMouseListener();
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setOptions(String[] options) {
    this.options = (options != null) ? options : new String[0];
    this.selectedIndex = 0;
    revalidate();
    repaint();
  }

  public void setSelectedIndex(int index) {
    if (index >= 0 && index < options.length) {
      String oldVal = options[selectedIndex];
      this.selectedIndex = index;
      repaint();
      fireChange(oldVal, options[index]);
    }
  }

  public void setBlock(boolean block) {
    this.block = block;
    revalidate();
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

  /** 获取当前选中值。 */
  public String getSelectedValue() {
    return (selectedIndex >= 0 && selectedIndex < options.length)
        ? options[selectedIndex] : null;
  }

  public void addChangeListener(AntChangeListener<String> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public void removeChangeListener(AntChangeListener<String> listener) {
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
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    int h = st.controlHeightOf(size);
    int fontSize = resolveFontSize(ft);
    Font font = ft.createFont(fontSize, Font.PLAIN);
    FontMetrics fm = getFontMetrics(font);

    int totalW = 4; // 外边距
    for (String opt : options) {
      totalW += fm.stringWidth(opt) + st.getPadding() * 2;
    }
    return new Dimension(totalW, h + 4);
  }

  @Override
  public Dimension getMaximumSize() {
    if (block) {
      return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    int w = width;
    int h = height;
    int arc = st.getBorderRadius();

    // 背景
    g2.setColor(ct.getFillSecondaryColor());
    g2.fillRoundRect(0, 0, w, h, arc + 2, arc + 2);

    int fontSize = resolveFontSize(ft);
    Font font = ft.createFont(fontSize, Font.PLAIN);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics();

    // 计算每个选项的 x 和宽度
    int[] optX = new int[options.length];
    int[] optW = new int[options.length];
    int x = 2;
    int segW = block ? ((w - 4) / Math.max(1, options.length)) : 0;

    for (int i = 0; i < options.length; i++) {
      optX[i] = x;
      optW[i] = block ? segW : fm.stringWidth(options[i]) + st.getPadding() * 2;
      x += optW[i];
    }

    // 选中背景
    if (selectedIndex >= 0 && selectedIndex < options.length) {
      g2.setColor(ct.getBgContainer());
      g2.fillRoundRect(optX[selectedIndex] + 1, 2,
          optW[selectedIndex] - 2, h - 4, arc, arc);
    }

    // 绘制文字
    for (int i = 0; i < options.length; i++) {
      if (i == selectedIndex) {
        g2.setColor(ct.getTextColor());
      } else if (i == hoveredIndex) {
        g2.setColor(ct.getTextSecondaryColor());
      } else {
        g2.setColor(ct.getTextTertiaryColor());
      }
      int textX = optX[i] + (optW[i] - fm.stringWidth(options[i])) / 2;
      int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(options[i], textX, textY);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private int resolveFontSize(FontToken ft) {
    switch (size) {
      case LARGE:  return ft.getFontSizeLg();
      case SMALL:  return ft.getFontSizeSm();
      default:     return ft.getFontSize();
    }
  }

  private int getIndexAt(int mouseX) {
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    Font font = ft.createFont(resolveFontSize(ft), Font.PLAIN);
    FontMetrics fm = getFontMetrics(font);

    int x = 2;
    for (int i = 0; i < options.length; i++) {
      int segW = block
          ? ((getWidth() - 4) / Math.max(1, options.length))
          : fm.stringWidth(options[i]) + st.getPadding() * 2;
      if (mouseX >= x && mouseX < x + segW) {
        return i;
      }
      x += segW;
    }
    return -1;
  }

  private void fireChange(String oldVal, String newVal) {
    AntChangeEvent<String> evt = new AntChangeEvent<>(this, oldVal, newVal);
    for (AntChangeListener<String> l : changeListeners) {
      l.valueChanged(evt);
    }
  }

  private void installMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int idx = getIndexAt(e.getX());
        if (idx >= 0 && idx != selectedIndex) {
          setSelectedIndex(idx);
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        hoveredIndex = -1;
        repaint();
      }
    });
    addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int idx = getIndexAt(e.getX());
        if (idx != hoveredIndex) {
          hoveredIndex = idx;
          repaint();
        }
      }
    });
  }
}
