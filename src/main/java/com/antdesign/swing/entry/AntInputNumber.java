package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.model.Variant;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 数字输入框组件。
 *
 * <p>对应 Ant Design {@code <InputNumber>}，带增减步进器的数字输入。
 *
 * <pre>{@code
 * AntInputNumber num = new AntInputNumber();
 * num.setMin(0);
 * num.setMax(100);
 * num.setStep(1);
 * num.setValue(50);
 * }</pre>
 */
public class AntInputNumber extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private double value;
  @Getter private double min;
  @Getter private double max;
  @Getter private double step;
  @Getter private int precision;
  @Getter private Variant variant;
  private ComponentSize size;
  @Getter private boolean controls;

  private JTextField textField;
  private boolean updatingText;
  private final List<AntChangeListener<Double>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建数字输入框。 */
  public AntInputNumber() {
    this.min = -Double.MAX_VALUE;
    this.max = Double.MAX_VALUE;
    this.step = 1;
    this.precision = -1;
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;
    this.controls = true;

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(double value) {
    double old = this.value;
    this.value = clamp(value);
    updateText();
    if (old != this.value) {
      fireChange(old, this.value);
    }
  }

  public void setMin(double min) { this.min = min; }
  public void setMax(double max) { this.max = max; }
  public void setStep(double step) { this.step = Math.max(0.001, step); }
  public void setPrecision(int precision) { this.precision = precision; }

  public void setVariant(Variant variant) {
    this.variant = (variant != null) ? variant : Variant.OUTLINED;
    applyTheme();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    applyTheme();
  }

  public ComponentSize getComponentSize() { return size; }

  public void setControls(boolean controls) {
    this.controls = controls;
    buildUi();
  }

  public void addChangeListener(AntChangeListener<Double> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  /** 递增。 */
  public void increment() { setValue(value + step); }

  /** 递减。 */
  public void decrement() { setValue(value - step); }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    SizeToken st = sizeToken();
    return new Dimension(120, st.controlHeightOf(size));
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildUi() {
    removeAll();
    ColorToken ct = colorToken();

    textField = new JTextField();
    textField.setHorizontalAlignment(SwingConstants.LEFT);
    textField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) { parseText(); }
      @Override
      public void removeUpdate(DocumentEvent e) { parseText(); }
      @Override
      public void changedUpdate(DocumentEvent e) { parseText(); }
    });
    add(textField, BorderLayout.CENTER);

    if (controls) {
      JPanel btns = new JPanel(new java.awt.GridLayout(2, 1, 0, 0));
      btns.setOpaque(false);
      btns.setPreferredSize(new Dimension(22, 0));

      JLabel up = new JLabel("▲", SwingConstants.CENTER);
      up.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
      up.setForeground(ct.getTextTertiaryColor());
      up.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      up.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, ct.getBorderSecondaryColor()));
      up.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { increment(); }
      });

      JLabel down = new JLabel("▼", SwingConstants.CENTER);
      down.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
      down.setForeground(ct.getTextTertiaryColor());
      down.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      down.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ct.getBorderSecondaryColor()));
      down.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { decrement(); }
      });

      btns.add(up);
      btns.add(down);
      add(btns, BorderLayout.EAST);
    }

    updateText();
    applyTheme();
  }

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int fontSize = (size == ComponentSize.LARGE) ? ft.getFontSizeLg()
        : (size == ComponentSize.SMALL) ? ft.getFontSizeSm() : ft.getFontSize();
    textField.setFont(ft.createFont(fontSize, Font.PLAIN));
    textField.setForeground(ct.getTextColor());
    textField.setBackground(ct.getBgContainer());
    textField.setCaretColor(ct.getTextColor());

    if (variant == Variant.BORDERLESS) {
      setBorder(null);
    } else if (variant == Variant.FILLED) {
      textField.setBackground(ct.getFillSecondaryColor());
      setBorder(BorderFactory.createLineBorder(ct.getFillSecondaryColor()));
    } else {
      setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    }
    repaint();
  }

  private void updateText() {
    updatingText = true;
    String fmt;
    if (precision >= 0) {
      fmt = String.format("%." + precision + "f", value);
    } else if (value == Math.floor(value) && !Double.isInfinite(value)) {
      fmt = String.valueOf((long) value);
    } else {
      fmt = String.valueOf(value);
    }
    textField.setText(fmt);
    updatingText = false;
  }

  private void parseText() {
    if (updatingText) {
      return;
    }
    try {
      double parsed = Double.parseDouble(textField.getText().trim());
      double old = value;
      value = clamp(parsed);
      if (old != value) {
        fireChange(old, value);
      }
    } catch (NumberFormatException ignored) {
      // 忽略非法输入
    }
  }

  private double clamp(double v) {
    return Math.max(min, Math.min(max, v));
  }

  private void fireChange(double oldVal, double newVal) {
    AntChangeEvent<Double> evt = new AntChangeEvent<>(this, oldVal, newVal);
    for (AntChangeListener<Double> l : changeListeners) {
      l.valueChanged(evt);
    }
  }
}
