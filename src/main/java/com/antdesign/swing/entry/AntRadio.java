package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 单选框组件。
 *
 * <p>对应 Ant Design {@code <Radio>}。通常在 {@link Group} 中使用。
 *
 * <pre>{@code
 * AntRadio.Group group = new AntRadio.Group();
 * group.addRadio(new AntRadio("Option A", "a"));
 * group.addRadio(new AntRadio("Option B", "b"));
 * group.addChangeListener(e -> System.out.println(e.getNewValue()));
 * }</pre>
 */
public class AntRadio extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int CIRCLE_SIZE = 16;
  private static final int GAP = 8;

  @Getter private String text;
  @Getter private String value;
  @Getter private boolean selected;

  private boolean hovered;
  private Group group;

  /**
   * 创建单选框。
   *
   * @param text  显示文本
   * @param value 关联值
   */
  public AntRadio(String text, String value) {
    this.text = (text != null) ? text : "";
    this.value = value;
    setFocusable(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
      @Override
      public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
      @Override
      public void mouseClicked(MouseEvent e) {
        if (isEnabled() && !selected) {
          if (group != null) {
            group.select(AntRadio.this);
          } else {
            selected = true;
            repaint();
          }
        }
      }
    });
    // Theme listener handled by base class
  }

  public AntRadio(String text) {
    this(text, text);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setText(String text) {
    this.text = (text != null) ? text : "";
    revalidate();
    repaint();
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
    repaint();
  }

  void setGroup(Group group) {
    this.group = group;
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
    return new Dimension(CIRCLE_SIZE + textW, Math.max(CIRCLE_SIZE, fm.getHeight()) + 4);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int h = height;
    int cy = (h - CIRCLE_SIZE) / 2;

    // 外圆
    Color border = !isEnabled() ? ct.getBorderColor()
        : selected ? ct.getPrimaryColor()
        : hovered ? ct.getPrimaryColor() : ct.getBorderColor();
    g2.setColor(isEnabled() ? ct.getFillSecondaryColor() : ct.getDisabledBgColor());
    g2.fillOval(0, cy, CIRCLE_SIZE, CIRCLE_SIZE);
    g2.setColor(border);
    g2.setStroke(new java.awt.BasicStroke(1.5f));
    g2.drawOval(0, cy, CIRCLE_SIZE - 1, CIRCLE_SIZE - 1);

    // 内圆
    if (selected) {
      g2.setColor(isEnabled() ? ct.getPrimaryColor() : ct.getDisabledColor());
      int inner = 6;
      int offset = (CIRCLE_SIZE - inner) / 2;
      g2.fillOval(offset, cy + offset, inner, inner);
    }

    // 文本
    if (!text.isEmpty()) {
      g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      g2.setColor(isEnabled() ? ct.getTextColor() : ct.getDisabledColor());
      FontMetrics fm = g2.getFontMetrics();
      g2.drawString(text, CIRCLE_SIZE + GAP,
          (h + fm.getAscent() - fm.getDescent()) / 2);
    }
  }

  // =========================================================================
  // Radio Group
  // =========================================================================

  /**
   * 单选框组。
   */
  public static class Group extends AbstractAntComponent {
    private static final long serialVersionUID = 1L;
    private final List<AntRadio> radios = new ArrayList<>();
    private final List<AntChangeListener<String>> changeListeners = new CopyOnWriteArrayList<>();

    public Group() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    public void addRadio(AntRadio radio) {
      radios.add(radio);
      radio.setGroup(this);
      add(radio);
      add(javax.swing.Box.createHorizontalStrut(16));
    }

    void select(AntRadio target) {
      String oldVal = getSelectedValue();
      for (AntRadio r : radios) {
        r.setSelected(r == target);
      }
      String newVal = target.getValue();
      AntChangeEvent<String> evt = new AntChangeEvent<>(this, oldVal, newVal);
      for (AntChangeListener<String> l : changeListeners) {
        l.valueChanged(evt);
      }
    }

    public String getSelectedValue() {
      for (AntRadio r : radios) {
        if (r.isSelected()) {
          return r.getValue();
        }
      }
      return null;
    }

    public void setSelectedValue(String value) {
      for (AntRadio r : radios) {
        r.setSelected(value != null && value.equals(r.getValue()));
      }
    }

    public void addChangeListener(AntChangeListener<String> listener) {
      if (listener != null) {
        changeListeners.add(listener);
      }
    }
  }
}
