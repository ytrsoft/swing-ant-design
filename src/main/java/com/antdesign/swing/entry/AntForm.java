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

  /** 表单字段。 */
  @Getter
  public static class FormField {
    private final String name;
    private final String label;
    private final Component component;
    private boolean required;
    private String errorMessage;

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
