package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 多选框组件。
 *
 * <p>对应 Ant Design {@code <Checkbox>}。
 *
 * <pre>{@code
 * AntCheckbox cb = new AntCheckbox("Remember me");
 * cb.addChangeListener(e -> System.out.println("Checked: " + e.getNewValue()));
 * }</pre>
 */
public class AntCheckbox extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int BOX_SIZE = 16;
  private static final int GAP = 8;

  @Getter private String text;
  @Getter private boolean checked;
  @Getter private boolean indeterminate;

  private boolean hovered;
  private final List<AntChangeListener<Boolean>> changeListeners = new CopyOnWriteArrayList<>();

  /**
   * 创建多选框。
   *
   * @param text 标签文本
   */
  public AntCheckbox(String text) {
    this.text = (text != null) ? text : "";
    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
      @Override
      public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
      @Override
      public void mouseClicked(MouseEvent e) {
        if (isEnabled()) {
          toggle();
        }
      }
    });
    // Theme listener handled by base class
  }

  public AntCheckbox() {
    this("");
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setText(String text) {
    this.text = (text != null) ? text : "";
    revalidate();
    repaint();
  }

  public void setChecked(boolean checked) {
    boolean old = this.checked;
    this.checked = checked;
    this.indeterminate = false;
    repaint();
    if (old != checked) {
      fireChange(old, checked);
    }
  }

  public void setIndeterminate(boolean indeterminate) {
    this.indeterminate = indeterminate;
    repaint();
  }

  /** 切换选中状态。 */
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
    FontToken ft = fontToken();
    FontMetrics fm = getFontMetrics(ft.createFont(ft.getFontSize(), Font.PLAIN));
    int textW = text.isEmpty() ? 0 : fm.stringWidth(text) + GAP;
    return new Dimension(BOX_SIZE + textW, Math.max(BOX_SIZE, fm.getHeight()) + 4);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int h = height;
    int boxY = (h - BOX_SIZE) / 2;

    // 方框
    Color boxBg;
    Color boxBorder;
    if (!isEnabled()) {
      boxBg = ct.getDisabledBgColor();
      boxBorder = ct.getBorderColor();
    } else if (checked || indeterminate) {
      boxBg = ct.getPrimaryColor();
      boxBorder = ct.getPrimaryColor();
    } else {
      boxBg = ct.getFillSecondaryColor();
      boxBorder = hovered ? ct.getPrimaryColor() : ct.getBorderColor();
    }

    g2.setColor(boxBg);
    g2.fillRoundRect(0, boxY, BOX_SIZE, BOX_SIZE, 3, 3);
    g2.setColor(boxBorder);
    g2.setStroke(new java.awt.BasicStroke(1.5f));
    g2.drawRoundRect(0, boxY, BOX_SIZE - 1, BOX_SIZE - 1, 3, 3);

    // 勾选标记
    if (checked) {
      g2.setColor(Color.WHITE);
      g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND,
          java.awt.BasicStroke.JOIN_ROUND));
      g2.drawLine(4, boxY + 8, 7, boxY + 11);
      g2.drawLine(7, boxY + 11, 12, boxY + 5);
    } else if (indeterminate) {
      g2.setColor(Color.WHITE);
      g2.fillRect(4, boxY + 7, BOX_SIZE - 8, 2);
    }

    // 文本
    if (!text.isEmpty()) {
      g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      g2.setColor(isEnabled() ? ct.getTextColor() : ct.getDisabledColor());
      FontMetrics fm = g2.getFontMetrics();
      g2.drawString(text, BOX_SIZE + GAP,
          (h + fm.getAscent() - fm.getDescent()) / 2);
    }
  }

  private void fireChange(boolean oldVal, boolean newVal) {
    AntChangeEvent<Boolean> evt = new AntChangeEvent<>(this, oldVal, newVal);
    for (AntChangeListener<Boolean> l : changeListeners) {
      l.valueChanged(evt);
    }
  }
}
