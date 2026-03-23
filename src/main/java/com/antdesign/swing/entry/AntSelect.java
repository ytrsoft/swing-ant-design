package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.model.Variant;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

/**
 * Ant Design 选择器组件。
 *
 * <p>对应 Ant Design {@code <Select>}，下拉选择器。支持 {@link Option} 结构
 * （label/value/disabled）、搜索过滤、加载状态和校验状态。
 *
 * <pre>{@code
 * AntSelect<String> select = new AntSelect<>();
 * select.addOption(new AntSelect.Option<>("opt1", "Option 1"));
 * select.addOption(new AntSelect.Option<>("opt2", "Option 2"));
 * select.setShowSearch(true);
 * select.addChangeListener(e -> System.out.println(e.getNewValue()));
 *
 * // 简易 API 仍可使用
 * select.addOption("Option A");
 * }</pre>
 *
 * @param <T> 选项值类型
 */
public class AntSelect<T> extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  // =========================================================================
  // Option 结构
  // =========================================================================

  /**
   * 选择器选项，封装 label/value/disabled。
   *
   * @param <V> 值类型
   */
  @Getter
  public static class Option<V> {
    private final V value;
    private final String label;
    private final boolean disabled;

    /**
     * 创建选项。
     *
     * @param value    选项值
     * @param label    显示文本
     * @param disabled 是否禁用
     */
    public Option(V value, String label, boolean disabled) {
      this.value = value;
      this.label = (label != null) ? label : String.valueOf(value);
      this.disabled = disabled;
    }

    /** 创建可用选项。 */
    public Option(V value, String label) {
      this(value, label, false);
    }

    /** 以 value 的 toString 作为 label 创建。 */
    public Option(V value) {
      this(value, String.valueOf(value), false);
    }

    @Override
    public String toString() {
      return label;
    }
  }

  // =========================================================================
  // 属性
  // =========================================================================

  @Getter private String placeholder;
  @Getter private boolean allowClear;
  @Getter private boolean showSearch;
  @Getter private boolean loading;
  @Getter private Status status;
  @Getter private Variant variant;
  private ComponentSize size;

  private final List<Option<T>> options = new ArrayList<>();
  private final JComboBox<Option<T>> comboBox;
  private T previousValue;
  private BiPredicate<String, Option<T>> filterOption;
  private final List<AntChangeListener<T>> changeListeners = new CopyOnWriteArrayList<>();

  // =========================================================================
  // 构造方法
  // =========================================================================

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
        Option<T> opt = (Option<T>) comboBox.getSelectedItem();
        T newVal = (opt != null) ? opt.getValue() : null;
        AntChangeEvent<T> evt = new AntChangeEvent<>(this, previousValue, newVal);
        previousValue = newVal;
        for (AntChangeListener<T> l : changeListeners) {
          l.valueChanged(evt);
        }
      }
    });

    // 自定义渲染器支持 disabled 项灰显
    comboBox.setRenderer(new OptionRenderer());

    applyTheme();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // 选项操作
  // =========================================================================

  /**
   * 添加结构化选项。
   *
   * @param option 选项对象
   */
  public void addOption(Option<T> option) {
    if (option != null) {
      options.add(option);
      comboBox.addItem(option);
    }
  }

  /**
   * 添加简单选项（以 value 的 toString 为 label）。
   *
   * @param item 选项值
   */
  public void addOption(T item) {
    addOption(new Option<>(item));
  }

  /**
   * 批量设置选项（替换已有选项）。
   *
   * @param newOptions 选项列表
   */
  public void setOptions(List<Option<T>> newOptions) {
    clearOptions();
    if (newOptions != null) {
      for (Option<T> opt : newOptions) {
        addOption(opt);
      }
    }
  }

  /** 移除选项。 */
  public void removeOption(T value) {
    options.removeIf(o -> {
      if (o.getValue() == value
          || (o.getValue() != null && o.getValue().equals(value))) {
        comboBox.removeItem(o);
        return true;
      }
      return false;
    });
  }

  /** 清空所有选项。 */
  public void clearOptions() {
    options.clear();
    comboBox.removeAllItems();
  }

  /** 获取选中值。 */
  @SuppressWarnings("unchecked")
  public T getValue() {
    Option<T> opt = (Option<T>) comboBox.getSelectedItem();
    return (opt != null) ? opt.getValue() : null;
  }

  /** 设置选中值。 */
  public void setValue(T value) {
    for (int i = 0; i < comboBox.getItemCount(); i++) {
      Option<T> opt = comboBox.getItemAt(i);
      if (opt.getValue() == value
          || (opt.getValue() != null && opt.getValue().equals(value))) {
        comboBox.setSelectedIndex(i);
        return;
      }
    }
    comboBox.setSelectedIndex(-1);
  }

  /** 获取内部 JComboBox。 */
  public JComboBox<Option<T>> getComboBox() {
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

  /**
   * 设置是否显示搜索框。启用后 JComboBox 变为可编辑模式。
   *
   * @param showSearch 是否启用搜索
   */
  public void setShowSearch(boolean showSearch) {
    this.showSearch = showSearch;
    comboBox.setEditable(showSearch);
  }

  /**
   * 设置自定义搜索过滤函数。
   *
   * @param filterOption 过滤谓词（输入文本，选项） → 是否保留
   */
  public void setFilterOption(BiPredicate<String, Option<T>> filterOption) {
    this.filterOption = filterOption;
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
    repaint();
  }

  public void setStatus(Status status) {
    this.status = status;
    applyTheme();
  }

  public void setVariant(Variant variant) {
    this.variant = (variant != null) ? variant : Variant.OUTLINED;
    applyTheme();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    applyTheme();
  }

  public ComponentSize getComponentSize() {
    return size;
  }

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

    int fontSize = (size == ComponentSize.LARGE) ? ft.getFontSizeLg()
        : (size == ComponentSize.SMALL) ? ft.getFontSizeSm() : ft.getFontSize();
    comboBox.setFont(ft.createFont(fontSize, Font.PLAIN));
    comboBox.setForeground(ct.getTextColor());
    comboBox.setBackground(ct.getBgContainer());

    Color borderColor = ct.getBorderColor();
    if (status != null) {
      borderColor = status.getDefaultColor();
    }

    if (variant == Variant.BORDERLESS) {
      setBorder(null);
      comboBox.setBorder(null);
    } else if (variant == Variant.FILLED) {
      setBorder(null);
      comboBox.setBackground(ct.getFillSecondaryColor());
    } else {
      setBorder(BorderFactory.createLineBorder(borderColor));
    }
    repaint();
  }

  // =========================================================================
  // 选项渲染器
  // =========================================================================

  /** 自定义渲染器，禁用项灰显且不可选。 */
  private class OptionRenderer extends DefaultListCellRenderer {
    @Override
    @SuppressWarnings("unchecked")
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);

      if (value instanceof Option) {
        Option<T> opt = (Option<T>) value;
        label.setText(opt.getLabel());
        if (opt.isDisabled()) {
          label.setForeground(colorToken().getDisabledColor());
          label.setBackground(list.getBackground());
        }
      }
      return label;
    }
  }
}
