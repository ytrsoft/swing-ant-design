package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ant Design 表单组件。
 *
 * <p>对应 Ant Design {@code <Form>}，具有数据收集、校验和提交功能的表单。
 * 支持水平/垂直/内联布局、标签宽度和字段校验。
 *
 * <pre>{@code
 * AntForm form = new AntForm();
 * form.addField("username", "Username", new AntInput());
 * form.addField("password", "Password", new AntInput(true));
 * }</pre>
 */
public class AntForm extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 表单布局方向。 */
  public enum Layout { HORIZONTAL, VERTICAL, INLINE }

  private Layout formLayout;
  @Getter private int labelWidth;
  @Getter private boolean colon;
  private ComponentSize size;

  private final Map<String, FormField> fields = new LinkedHashMap<>();
  private java.util.function.Consumer<Map<String, Object>> onFinish;
  private java.util.function.Consumer<Map<String, String>> onFinishFailed;

  // =========================================================================
  // Rule 校验规则
  // =========================================================================

  /** 表单校验规则。 */
  public static class Rule {
    private boolean required;
    private String message;
    private Integer min;
    private Integer max;
    private String pattern;
    private java.util.function.Function<Object, String> validator;

    /** 必填规则。 */
    public static Rule required(String message) {
      Rule r = new Rule();
      r.required = true;
      r.message = message;
      return r;
    }

    /** 最小长度规则。 */
    public static Rule min(int min, String message) {
      Rule r = new Rule();
      r.min = min;
      r.message = message;
      return r;
    }

    /** 最大长度规则。 */
    public static Rule max(int max, String message) {
      Rule r = new Rule();
      r.max = max;
      r.message = message;
      return r;
    }

    /** 正则规则。 */
    public static Rule pattern(String pattern, String message) {
      Rule r = new Rule();
      r.pattern = pattern;
      r.message = message;
      return r;
    }

    /** 自定义校验器。返回 null 表示通过，否则返回错误信息。 */
    public static Rule custom(java.util.function.Function<Object, String> validator) {
      Rule r = new Rule();
      r.validator = validator;
      return r;
    }
  }

  /** 表单字段。 */
  @Getter
  public static class FormField {
    private final String name;
    private final String label;
    private final Component component;
    private boolean required;
    private String errorMessage;
    private Object initialValue;
    private final List<Rule> rules = new ArrayList<>();

    public FormField(String name, String label, Component component) {
      this.name = name;
      this.label = label;
      this.component = component;
    }
  }

  /** 创建表单。 */
  public AntForm() {
    this.formLayout = Layout.HORIZONTAL;
    this.labelWidth = 100;
    this.colon = true;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // 字段操作
  // =========================================================================

  /**
   * 添加表单字段。
   *
   * @param name      字段名称
   * @param label     标签文本
   * @param component 输入组件
   */
  public void addField(String name, String label, Component component) {
    FormField field = new FormField(name, label, component);
    fields.put(name, field);
    rebuildUi();
  }

  /**
   * 添加必填表单字段。
   *
   * @param name      字段名称
   * @param label     标签文本
   * @param component 输入组件
   */
  public void addRequiredField(String name, String label, Component component) {
    FormField field = new FormField(name, label, component);
    field.required = true;
    fields.put(name, field);
    rebuildUi();
  }

  /**
   * 设置字段错误信息。
   *
   * @param name    字段名称
   * @param message 错误信息，为 null 则清除错误
   */
  public void setFieldError(String name, String message) {
    FormField field = fields.get(name);
    if (field != null) {
      field.errorMessage = message;
      rebuildUi();
    }
  }

  /** 获取所有字段。 */
  public List<FormField> getFields() {
    return new ArrayList<>(fields.values());
  }

  /** 获取指定字段组件。 */
  public Component getFieldComponent(String name) {
    FormField field = fields.get(name);
    return (field != null) ? field.component : null;
  }

  /**
   * 添加带校验规则的字段。
   *
   * @param name      字段名称
   * @param label     标签文本
   * @param component 输入组件
   * @param rules     校验规则列表
   */
  public void addField(String name, String label, Component component, Rule... rules) {
    FormField field = new FormField(name, label, component);
    if (rules != null) {
      for (Rule r : rules) {
        field.rules.add(r);
        if (r.required) {
          field.required = true;
        }
      }
    }
    field.initialValue = extractValue(component);
    fields.put(name, field);
    rebuildUi();
  }

  // =========================================================================
  // 数据收集
  // =========================================================================

  /**
   * 获取所有字段的值。
   *
   * @return 字段名 → 值 的映射
   */
  public Map<String, Object> getFieldsValue() {
    Map<String, Object> values = new LinkedHashMap<>();
    for (Map.Entry<String, FormField> entry : fields.entrySet()) {
      values.put(entry.getKey(), extractValue(entry.getValue().component));
    }
    return values;
  }

  /**
   * 获取指定字段的值。
   *
   * @param name 字段名
   * @return 字段值
   */
  public Object getFieldValue(String name) {
    FormField field = fields.get(name);
    return (field != null) ? extractValue(field.component) : null;
  }

  /**
   * 批量设置字段值。
   *
   * @param values 字段名 → 值 的映射
   */
  public void setFieldsValue(Map<String, Object> values) {
    if (values == null) {
      return;
    }
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      FormField field = fields.get(entry.getKey());
      if (field != null) {
        injectValue(field.component, entry.getValue());
      }
    }
  }

  /**
   * 重置所有字段为初始值，并清除错误信息。
   */
  public void resetFields() {
    for (FormField field : fields.values()) {
      field.errorMessage = null;
      injectValue(field.component, field.initialValue);
    }
    rebuildUi();
  }

  // =========================================================================
  // 校验
  // =========================================================================

  /**
   * 校验所有字段。
   *
   * @return 校验错误映射（字段名 → 错误信息），为空表示全部通过
   */
  public Map<String, String> validateFields() {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FormField field : fields.values()) {
      String error = validateField(field);
      if (error != null) {
        errors.put(field.name, error);
        field.errorMessage = error;
      } else {
        field.errorMessage = null;
      }
    }

    rebuildUi();
    return errors;
  }

  /**
   * 提交表单：先校验，通过则调用 onFinish，失败则调用 onFinishFailed。
   *
   * @return 是否校验通过
   */
  public boolean submit() {
    Map<String, String> errors = validateFields();
    if (errors.isEmpty()) {
      if (onFinish != null) {
        onFinish.accept(getFieldsValue());
      }
      return true;
    } else {
      if (onFinishFailed != null) {
        onFinishFailed.accept(errors);
      }
      return false;
    }
  }

  /** 设置提交成功回调。 */
  public void setOnFinish(java.util.function.Consumer<Map<String, Object>> onFinish) {
    this.onFinish = onFinish;
  }

  /** 设置校验失败回调。 */
  public void setOnFinishFailed(
      java.util.function.Consumer<Map<String, String>> onFinishFailed) {
    this.onFinishFailed = onFinishFailed;
  }

  // =========================================================================
  // 值提取 / 注入 辅助
  // =========================================================================

  /** 从组件中提取值。支持常见的 Ant 组件和标准 Swing 组件。 */
  @SuppressWarnings("rawtypes")
  private static Object extractValue(Component comp) {
    if (comp instanceof AntInput) {
      return ((AntInput) comp).getValue();
    } else if (comp instanceof AntSelect) {
      return ((AntSelect) comp).getValue();
    } else if (comp instanceof AntCheckbox) {
      return ((AntCheckbox) comp).isChecked();
    } else if (comp instanceof AntSwitch) {
      return ((AntSwitch) comp).isChecked();
    } else if (comp instanceof AntDatePicker) {
      return ((AntDatePicker) comp).getValue();
    } else if (comp instanceof AntInputNumber) {
      return ((AntInputNumber) comp).getValue();
    } else if (comp instanceof AntRate) {
      return ((AntRate) comp).getValue();
    } else if (comp instanceof javax.swing.JTextField) {
      return ((javax.swing.JTextField) comp).getText();
    } else if (comp instanceof javax.swing.JCheckBox) {
      return ((javax.swing.JCheckBox) comp).isSelected();
    }
    return null;
  }

  /** 向组件注入值。 */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void injectValue(Component comp, Object value) {
    if (comp instanceof AntInput) {
      ((AntInput) comp).setValue(value != null ? value.toString() : "");
    } else if (comp instanceof AntSelect && value != null) {
      ((AntSelect) comp).setValue(value);
    } else if (comp instanceof AntCheckbox) {
      ((AntCheckbox) comp).setChecked(Boolean.TRUE.equals(value));
    } else if (comp instanceof AntSwitch) {
      ((AntSwitch) comp).setChecked(Boolean.TRUE.equals(value));
    } else if (comp instanceof AntDatePicker && value instanceof java.time.LocalDate) {
      ((AntDatePicker) comp).setValue((java.time.LocalDate) value);
    } else if (comp instanceof javax.swing.JTextField) {
      ((javax.swing.JTextField) comp).setText(value != null ? value.toString() : "");
    }
  }

  /** 校验单个字段。返回第一个错误信息，或 null 表示通过。 */
  private String validateField(FormField field) {
    Object value = extractValue(field.component);
    String strVal = (value != null) ? value.toString() : "";

    // 隐式 required 校验
    if (field.required && field.rules.isEmpty()) {
      if (value == null || strVal.trim().isEmpty()) {
        return field.label + " is required";
      }
    }

    // 显式规则校验
    for (Rule rule : field.rules) {
      if (rule.required) {
        if (value == null || strVal.trim().isEmpty()) {
          return (rule.message != null) ? rule.message : field.label + " is required";
        }
      }
      if (rule.min != null && strVal.length() < rule.min) {
        return (rule.message != null) ? rule.message
            : field.label + " must be at least " + rule.min + " characters";
      }
      if (rule.max != null && strVal.length() > rule.max) {
        return (rule.message != null) ? rule.message
            : field.label + " must be at most " + rule.max + " characters";
      }
      if (rule.pattern != null && !strVal.matches(rule.pattern)) {
        return (rule.message != null) ? rule.message
            : field.label + " format is invalid";
      }
      if (rule.validator != null) {
        String err = rule.validator.apply(value);
        if (err != null) {
          return err;
        }
      }
    }
    return null;
  }

  // =========================================================================
  // Setter
  // =========================================================================

  /** 获取表单布局方向。 */
  public Layout getFormLayout() {
    return formLayout;
  }

  public void setFormLayout(Layout layout) {
    this.formLayout = (layout != null) ? layout : Layout.HORIZONTAL;
    rebuildUi();
  }

  public void setLabelWidth(int labelWidth) {
    this.labelWidth = Math.max(40, labelWidth);
    rebuildUi();
  }

  public void setColon(boolean colon) {
    this.colon = colon;
    rebuildUi();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    rebuildUi();
  }

  public ComponentSize getComponentSize() { return size; }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    int gap = st.getMarginSm();

    for (FormField field : fields.values()) {
      JPanel row = new JPanel();
      row.setOpaque(false);
      row.setAlignmentX(LEFT_ALIGNMENT);

      if (formLayout == Layout.VERTICAL) {
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
      } else {
        row.setLayout(new BorderLayout(gap, 0));
      }

      // 标签
      String labelText = field.label + (colon ? ":" : "");
      if (field.required) {
        labelText = "* " + labelText;
      }
      JLabel label = new JLabel(labelText);
      label.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      label.setForeground(field.required && field.errorMessage != null
          ? ct.getErrorColor() : ct.getTextColor());

      if (formLayout == Layout.HORIZONTAL) {
        label.setPreferredSize(new Dimension(labelWidth, st.controlHeightOf(size)));
        label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        row.add(label, BorderLayout.WEST);
        row.add(field.component, BorderLayout.CENTER);
      } else {
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        row.add(label);
        JPanel compWrapper = new JPanel(new BorderLayout());
        compWrapper.setOpaque(false);
        compWrapper.setAlignmentX(LEFT_ALIGNMENT);
        compWrapper.add(field.component, BorderLayout.CENTER);
        row.add(compWrapper);
      }

      // 错误信息
      if (field.errorMessage != null && !field.errorMessage.isEmpty()) {
        JLabel errorLabel = new JLabel(field.errorMessage);
        errorLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
        errorLabel.setForeground(ct.getErrorColor());
        if (formLayout == Layout.HORIZONTAL) {
          JPanel errorRow = new JPanel(new BorderLayout());
          errorRow.setOpaque(false);
          errorRow.add(javax.swing.Box.createHorizontalStrut(labelWidth + gap),
              BorderLayout.WEST);
          errorRow.add(errorLabel, BorderLayout.CENTER);
          errorRow.setAlignmentX(LEFT_ALIGNMENT);
          errorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,
              errorRow.getPreferredSize().height));
          // 在行后追加
          JPanel wrapper = new JPanel();
          wrapper.setOpaque(false);
          wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
          wrapper.setAlignmentX(LEFT_ALIGNMENT);
          row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
              row.getPreferredSize().height));
          wrapper.add(row);
          wrapper.add(errorRow);
          wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, gap, 0));
          add(wrapper);
          continue;
        } else {
          errorLabel.setAlignmentX(LEFT_ALIGNMENT);
          row.add(errorLabel);
        }
      }

      row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
          row.getPreferredSize().height));
      row.setBorder(BorderFactory.createEmptyBorder(0, 0, gap, 0));
      add(row);
    }

    revalidate();
    repaint();
  }
}
