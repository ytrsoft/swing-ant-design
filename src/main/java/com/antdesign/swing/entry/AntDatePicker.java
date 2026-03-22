package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.display.AntCalendar;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 日期选择器组件。
 *
 * <p>对应 Ant Design {@code <DatePicker>}，点击输入框弹出日历选择日期。
 *
 * <pre>{@code
 * AntDatePicker picker = new AntDatePicker();
 * picker.addChangeListener(e -> System.out.println("Date: " + e.getNewValue()));
 * }</pre>
 */
public class AntDatePicker extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Getter private LocalDate value;
  @Getter private String placeholder;
  @Getter private Variant variant;
  private ComponentSize size;

  private JTextField textField;
  private Popup calendarPopup;
  private final List<AntChangeListener<LocalDate>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建日期选择器。 */
  public AntDatePicker() {
    this.placeholder = "Select date";
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(LocalDate value) {
    LocalDate old = this.value;
    this.value = value;
    textField.setText(value != null ? value.format(FORMATTER) : "");
    if (old != value) {
      AntChangeEvent<LocalDate> evt = new AntChangeEvent<>(this, old, value);
      for (AntChangeListener<LocalDate> l : changeListeners) {
        l.valueChanged(evt);
      }
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

  public void addChangeListener(AntChangeListener<LocalDate> listener) {
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
    return new Dimension(200, st.controlHeightOf(size));
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildUi() {
    textField = new JTextField();
    textField.setEditable(false);
    textField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    textField.putClientProperty("JTextField.placeholderText", placeholder);
    textField.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        toggleCalendar();
      }
    });
    add(textField, BorderLayout.CENTER);

    javax.swing.JLabel icon = new javax.swing.JLabel("📅");
    icon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    add(icon, BorderLayout.EAST);

    applyTheme();
  }

  private void toggleCalendar() {
    if (calendarPopup != null) {
      calendarPopup.hide();
      calendarPopup = null;
      return;
    }
    AntCalendar calendar = new AntCalendar();
    calendar.setFullscreen(false);
    if (value != null) {
      calendar.setSelectedDate(value);
    }
    calendar.setPreferredSize(new Dimension(280, 300));
    calendar.addChangeListener(e -> {
      setValue(e.getNewValue());
      if (calendarPopup != null) {
        calendarPopup.hide();
        calendarPopup = null;
      }
    });

    java.awt.Point p = textField.getLocationOnScreen();
    calendarPopup = PopupFactory.getSharedInstance().getPopup(
        this, calendar, p.x, p.y + getHeight());
    calendarPopup.show();
  }

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int fontSize = (size == ComponentSize.LARGE) ? ft.getFontSizeLg()
        : (size == ComponentSize.SMALL) ? ft.getFontSizeSm() : ft.getFontSize();
    textField.setFont(ft.createFont(fontSize, Font.PLAIN));
    textField.setForeground(ct.getTextColor());
    textField.setBackground(ct.getBgContainer());

    if (variant == Variant.BORDERLESS) {
      setBorder(null);
    } else {
      setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    }
    repaint();
  }
}
