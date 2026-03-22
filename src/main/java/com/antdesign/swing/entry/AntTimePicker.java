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
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 时间选择器组件。
 *
 * <p>对应 Ant Design {@code <TimePicker>}。输入或选择时间。
 *
 * <pre>{@code
 * AntTimePicker picker = new AntTimePicker();
 * picker.setValue(LocalTime.of(14, 30));
 * }</pre>
 */
public class AntTimePicker extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private LocalTime value;
  @Getter private String format;
  @Getter private String placeholder;
  @Getter private Variant variant;
  private ComponentSize size;

  private JTextField textField;
  private final List<AntChangeListener<LocalTime>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建时间选择器。 */
  public AntTimePicker() {
    this.format = "HH:mm:ss";
    this.placeholder = "Select time";
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(LocalTime value) {
    LocalTime old = this.value;
    this.value = value;
    textField.setText(value != null ? value.format(DateTimeFormatter.ofPattern(format)) : "");
    if (old != value) {
      AntChangeEvent<LocalTime> evt = new AntChangeEvent<>(this, old, value);
      for (AntChangeListener<LocalTime> l : changeListeners) {
        l.valueChanged(evt);
      }
    }
  }

  public void setFormat(String format) {
    this.format = (format != null) ? format : "HH:mm:ss";
    if (value != null) {
      textField.setText(value.format(DateTimeFormatter.ofPattern(this.format)));
    }
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
    textField.putClientProperty("JTextField.placeholderText", placeholder);
  }

  public void setVariant(Variant variant) {
    this.variant = (variant != null) ? variant : Variant.OUTLINED;
    applyTheme();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    applyTheme();
  }

  public ComponentSize getComponentSize() { return size; }

  public void addChangeListener(AntChangeListener<LocalTime> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    SizeToken st = sizeToken();
    return new Dimension(160, st.controlHeightOf(size));
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildUi() {
    textField = new JTextField();
    textField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    textField.putClientProperty("JTextField.placeholderText", placeholder);
    textField.addActionListener(e -> parseInput());
    add(textField, BorderLayout.CENTER);

    JLabel icon = new JLabel("🕐");
    icon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    add(icon, BorderLayout.EAST);
    applyTheme();
  }

  private void parseInput() {
    try {
      LocalTime parsed = LocalTime.parse(textField.getText().trim(),
          DateTimeFormatter.ofPattern(format));
      setValue(parsed);
    } catch (Exception ignored) {
      // 忽略解析失败
    }
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
    } else {
      setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    }
    repaint();
  }
}
