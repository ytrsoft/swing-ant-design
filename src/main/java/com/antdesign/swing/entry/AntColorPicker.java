package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.ColorUtils;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 颜色选择器组件。
 *
 * <p>对应 Ant Design {@code <ColorPicker>}，点击显示系统颜色选择器。
 *
 * <pre>{@code
 * AntColorPicker picker = new AntColorPicker();
 * picker.setValue(Color.RED);
 * picker.addChangeListener(e -> System.out.println(ColorUtils.toHex(e.getNewValue())));
 * }</pre>
 */
public class AntColorPicker extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private Color value;
  @Getter private boolean showText;
  private ComponentSize size;

  private final List<AntChangeListener<Color>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建颜色选择器。 */
  public AntColorPicker() {
    this.value = new Color(0x16, 0x77, 0xFF);
    this.showText = true;
    this.size = ComponentSize.MIDDLE;

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (isEnabled()) {
          openChooser();
        }
      }
    });
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(Color value) {
    Color old = this.value;
    this.value = (value != null) ? value : Color.BLACK;
    repaint();
    if (!this.value.equals(old)) {
      AntChangeEvent<Color> evt = new AntChangeEvent<>(this, old, this.value);
      for (AntChangeListener<Color> l : changeListeners) {
        l.valueChanged(evt);
      }
    }
  }

  public void setShowText(boolean showText) {
    this.showText = showText;
    revalidate();
    repaint();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    revalidate();
    repaint();
  }

  public ComponentSize getComponentSize() { return size; }

  public void addChangeListener(AntChangeListener<Color> listener) {
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
    SizeToken st = sizeToken();
    int h = st.controlHeightOf(size);
    int w = h;
    if (showText) {
      w += 70;
    }
    return new Dimension(w, h);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();
    int w = width;
    int h = height;
    int arc = st.getBorderRadius();

    // 边框
    GraphicsUtils.fillRoundRect(g2, 0, 0, w, h, arc, ct.getBgContainer());
    GraphicsUtils.drawRoundRect(g2, 0, 0, w - 1, h - 1, arc, ct.getBorderColor(), 1f);

    // 色块
    int swatchSize = h - 8;
    g2.setColor(value);
    g2.fillRoundRect(4, 4, swatchSize, swatchSize, 3, 3);

    // 文本
    if (showText) {
      g2.setColor(ct.getTextColor());
      g2.setFont(fontToken()
          .createFont(fontToken().getFontSizeSm(),
              java.awt.Font.PLAIN));
      g2.drawString(ColorUtils.toHex(value), swatchSize + 8,
          (h + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);
    }
  }

  private void openChooser() {
    Color chosen = JColorChooser.showDialog(this, "Choose Color", value);
    if (chosen != null) {
      setValue(chosen);
    }
  }
}
