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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 输入框组件。
 *
 * <p>对应 Ant Design {@code <Input>}，支持前缀、后缀、密码模式、
 * 字数统计、变体样式和状态校验。
 *
 * <pre>{@code
 * AntInput input = new AntInput();
 * input.setPlaceholder("Please enter text");
 * input.setPrefix(AntIcons.outlined("search", 14));
 * }</pre>
 */
public class AntInput extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String placeholder;
  @Getter private Icon prefix;
  @Getter private Icon suffix;
  @Getter private String addonBefore;
  @Getter private String addonAfter;
  @Getter private boolean password;
  @Getter private boolean showCount;
  @Getter private int maxLength;
  @Getter private Status status;
  @Getter private Variant variant;
  private ComponentSize size;
  @Getter private boolean allowClear;

  private JTextField textField;
  private final List<AntChangeListener<String>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建默认输入框。 */
  public AntInput() {
    this(false);
  }

  /**
   * 创建输入框。
   *
   * @param password 是否为密码输入
   */
  public AntInput(boolean password) {
    this.password = password;
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;
    this.maxLength = -1;

    setLayout(new BorderLayout());
    buildField();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // 值操作
  // =========================================================================

  /** 获取文本值。 */
  public String getValue() {
    return textField.getText();
  }

  /** 设置文本值。 */
  public void setValue(String value) {
    textField.setText(value);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
    if (textField instanceof JTextField) {
      textField.putClientProperty("JTextField.placeholderText", placeholder);
    }
    repaint();
  }

  public void setPrefix(Icon prefix) {
    this.prefix = prefix;
    rebuildLayout();
  }

  public void setSuffix(Icon suffix) {
    this.suffix = suffix;
    rebuildLayout();
  }

  public void setAddonBefore(String addonBefore) {
    this.addonBefore = addonBefore;
    rebuildLayout();
  }

  public void setAddonAfter(String addonAfter) {
    this.addonAfter = addonAfter;
    rebuildLayout();
  }

  public void setPassword(boolean password) {
    this.password = password;
    String val = getValue();
    buildField();
    setValue(val);
  }

  public void setShowCount(boolean showCount) {
    this.showCount = showCount;
    rebuildLayout();
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
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

  public void setAllowClear(boolean allowClear) {
    this.allowClear = allowClear;
  }

  public void addChangeListener(AntChangeListener<String> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public void removeChangeListener(AntChangeListener<String> listener) {
    changeListeners.remove(listener);
  }

  /** 获取内部 JTextField。 */
  public JTextField getTextField() {
    return textField;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    textField.setEnabled(enabled);
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
    Dimension pref = super.getPreferredSize();
    return new Dimension(Math.max(pref.width, 200), h);
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildField() {
    String oldVal = textField != null ? textField.getText() : "";
    removeAll();
    textField = password ? new JPasswordField() : new JTextField();
    textField.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

    textField.getDocument().addDocumentListener(new DocumentListener() {
      private String prev = "";

      @Override
      public void insertUpdate(DocumentEvent e) { fireChange(); }

      @Override
      public void removeUpdate(DocumentEvent e) { fireChange(); }

      @Override
      public void changedUpdate(DocumentEvent e) { fireChange(); }

      private void fireChange() {
        String newVal = textField.getText();
        if (maxLength > 0 && newVal.length() > maxLength) {
          textField.setText(newVal.substring(0, maxLength));
          return;
        }
        AntChangeEvent<String> evt = new AntChangeEvent<>(AntInput.this, prev, newVal);
        prev = newVal;
        for (AntChangeListener<String> l : changeListeners) {
          l.valueChanged(evt);
        }
      }
    });

    rebuildLayout();
    applyTheme();
  }

  private void rebuildLayout() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    if (addonBefore != null) {
      JLabel addon = new JLabel(addonBefore);
      addon.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      addon.setForeground(ct.getTextSecondaryColor());
      addon.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 0, 1, ct.getBorderColor()),
          BorderFactory.createEmptyBorder(0, st.getPaddingXs(), 0, st.getPaddingXs())));
      addon.setOpaque(true);
      addon.setBackground(ct.getBgLayout());
      add(addon, BorderLayout.WEST);
    } else if (prefix != null) {
      JLabel prefLabel = new JLabel(prefix);
      prefLabel.setBorder(BorderFactory.createEmptyBorder(0, st.getPaddingXs(), 0, 4));
      add(prefLabel, BorderLayout.WEST);
    }

    add(textField, BorderLayout.CENTER);

    if (addonAfter != null) {
      JLabel addon = new JLabel(addonAfter);
      addon.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      addon.setForeground(ct.getTextSecondaryColor());
      addon.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 1, 0, 0, ct.getBorderColor()),
          BorderFactory.createEmptyBorder(0, st.getPaddingXs(), 0, st.getPaddingXs())));
      addon.setOpaque(true);
      addon.setBackground(ct.getBgLayout());
      add(addon, BorderLayout.EAST);
    } else if (suffix != null) {
      JLabel sufLabel = new JLabel(suffix);
      sufLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, st.getPaddingXs()));
      add(sufLabel, BorderLayout.EAST);
    }

    revalidate();
    repaint();
  }

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int fontSize;
    switch (size) {
      case LARGE:  fontSize = ft.getFontSizeLg(); break;
      case SMALL:  fontSize = ft.getFontSizeSm(); break;
      default:     fontSize = ft.getFontSize(); break;
    }
    textField.setFont(ft.createFont(fontSize, Font.PLAIN));
    textField.setForeground(ct.getTextColor());
    textField.setCaretColor(ct.getTextColor());

    Color borderColor = ct.getBorderColor();
    if (status != null) {
      borderColor = status.getDefaultColor();
    }

    switch (variant) {
      case FILLED:
        textField.setBackground(ct.getFillSecondaryColor());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
            BorderFactory.createEmptyBorder(2, 0, 2, 0)));
        break;
      case BORDERLESS:
        textField.setBackground(ct.getBgContainer());
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        break;
      default:
        textField.setBackground(ct.getBgContainer());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(2, 0, 2, 0)));
        break;
    }
    repaint();
  }
}
