package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ant Design 自动完成组件。
 *
 * <p>对应 Ant Design {@code <AutoComplete>}，输入框自动完成功能。
 *
 * <pre>{@code
 * AntAutoComplete ac = new AntAutoComplete();
 * ac.setOptions(Arrays.asList("Burns Bay Road", "Downing Street", "Wall Street"));
 * }</pre>
 */
public class AntAutoComplete extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private List<String> options;
  @Getter private String placeholder;
  private ComponentSize size;
  private Function<String, List<String>> filterFunction;

  private JTextField textField;
  private Popup dropdownPopup;
  private final List<AntChangeListener<String>> changeListeners = new ArrayList<>();

  /** 创建自动完成组件。 */
  public AntAutoComplete() {
    this.options = new ArrayList<>();
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setOptions(List<String> options) {
    this.options = (options != null) ? new ArrayList<>(options) : new ArrayList<>();
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
    textField.putClientProperty("JTextField.placeholderText", placeholder);
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    applyTheme();
  }

  public ComponentSize getComponentSize() { return size; }

  public void setFilterFunction(Function<String, List<String>> filterFunction) {
    this.filterFunction = filterFunction;
  }

  public String getValue() { return textField.getText(); }
  public void setValue(String value) { textField.setText(value); }

  public void addChangeListener(AntChangeListener<String> listener) {
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
    textField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) { onInput(); }
      @Override
      public void removeUpdate(DocumentEvent e) { onInput(); }
      @Override
      public void changedUpdate(DocumentEvent e) { onInput(); }
    });
    add(textField, BorderLayout.CENTER);
    applyTheme();
  }

  private void onInput() {
    String text = textField.getText();
    AntChangeEvent<String> evt = new AntChangeEvent<>(this, null, text);
    for (AntChangeListener<String> l : changeListeners) {
      l.valueChanged(evt);
    }

    if (text.isEmpty()) {
      hideDropdown();
      return;
    }

    List<String> filtered;
    if (filterFunction != null) {
      filtered = filterFunction.apply(text);
    } else {
      String lower = text.toLowerCase();
      filtered = options.stream()
          .filter(o -> o.toLowerCase().contains(lower))
          .collect(Collectors.toList());
    }

    if (filtered.isEmpty()) {
      hideDropdown();
    } else {
      showDropdown(filtered);
    }
  }

  private void showDropdown(List<String> items) {
    hideDropdown();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    DefaultListModel<String> model = new DefaultListModel<>();
    items.forEach(model::addElement);
    JList<String> list = new JList<>(model);
    list.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    list.setBackground(ct.getBgElevated());
    list.setForeground(ct.getTextColor());
    list.setSelectionBackground(ct.getPrimaryBgColor());
    list.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        String selected = list.getSelectedValue();
        if (selected != null) {
          textField.setText(selected);
          hideDropdown();
        }
      }
    });

    JScrollPane scroll = new JScrollPane(list);
    scroll.setPreferredSize(new Dimension(getWidth(),
        Math.min(items.size() * 32, 200)));
    scroll.setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));

    try {
      java.awt.Point p = textField.getLocationOnScreen();
      dropdownPopup = PopupFactory.getSharedInstance().getPopup(
          this, scroll, p.x, p.y + getHeight());
      dropdownPopup.show();
    } catch (Exception ignored) {
      // 组件未显示时忽略
    }
  }

  private void hideDropdown() {
    if (dropdownPopup != null) {
      dropdownPopup.hide();
      dropdownPopup = null;
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
    setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    repaint();
  }
}
