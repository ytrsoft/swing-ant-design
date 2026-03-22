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
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 选择器组件。
 *
 * <p>对应 Ant Design {@code <Select>}，下拉选择器。
 *
 * <pre>{@code
 * AntSelect<String> select = new AntSelect<>();
 * select.addOption("Option A");
 * select.addOption("Option B");
 * select.addChangeListener(e -> System.out.println(e.getNewValue()));
 * }</pre>
 *
 * @param <T> 选项值类型
 */
public class AntSelect<T> extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String placeholder;
  @Getter private boolean allowClear;
  @Getter private Variant variant;
  private ComponentSize size;

  private final JComboBox<T> comboBox;
  private final List<AntChangeListener<T>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建选择器。 */
  public AntSelect() {
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;
    this.comboBox = new JComboBox<>();

    setLayout(new BorderLayout());
    add(comboBox, BorderLayout.CENTER);

    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        @SuppressWarnings("unchecked")
        T val = (T) comboBox.getSelectedItem();
        AntChangeEvent<T> evt = new AntChangeEvent<>(this, null, val);
        for (AntChangeListener<T> l : changeListeners) {
          l.valueChanged(evt);
        }
      }
    });

    applyTheme();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // 选项操作
  // =========================================================================

  /** 添加选项。 */
  public void addOption(T item) {
    comboBox.addItem(item);
  }

  /** 移除选项。 */
  public void removeOption(T item) {
    comboBox.removeItem(item);
  }

  /** 清空所有选项。 */
  public void clearOptions() {
    comboBox.removeAllItems();
  }

  /** 获取选中值。 */
  @SuppressWarnings("unchecked")
  public T getValue() {
    return (T) comboBox.getSelectedItem();
  }

  /** 设置选中值。 */
  public void setValue(T value) {
    comboBox.setSelectedItem(value);
  }

  /** 获取内部 JComboBox。 */
  public JComboBox<T> getComboBox() {
    return comboBox;
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
    comboBox.putClientProperty("JComboBox.placeholderText", placeholder);
  }

  public void setAllowClear(boolean allowClear) {
    this.allowClear = allowClear;
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

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    comboBox.setEnabled(enabled);
  }

  public void addChangeListener(AntChangeListener<T> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public void removeChangeListener(AntChangeListener<T> listener) {
    changeListeners.remove(listener);
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

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int fontSize = (size == ComponentSize.LARGE) ? ft.getFontSizeLg()
        : (size == ComponentSize.SMALL) ? ft.getFontSizeSm() : ft.getFontSize();
    comboBox.setFont(ft.createFont(fontSize, Font.PLAIN));
    comboBox.setForeground(ct.getTextColor());
    comboBox.setBackground(ct.getBgContainer());

    if (variant == Variant.BORDERLESS) {
      setBorder(null);
      comboBox.setBorder(null);
    } else {
      setBorder(null);
    }
    repaint();
  }
}
