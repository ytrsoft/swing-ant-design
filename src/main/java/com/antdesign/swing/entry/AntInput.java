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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
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
  private JLabel countLabel;
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
    installMaxLengthFilter();
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
    rebuildLayout();
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

    // 使用 DocumentFilter 安全地限制最大长度，避免 setText 递归
    installMaxLengthFilter();

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
        updateCountLabel();
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

  /** 通过 DocumentFilter 限制最大输入长度。 */
  private void installMaxLengthFilter() {
    if (textField.getDocument() instanceof AbstractDocument) {
      ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
        @Override
        public void insertString(FilterBypass fb, int offset, String string,
            AttributeSet attr) throws BadLocationException {
          if (maxLength > 0) {
            int currentLen = fb.getDocument().getLength();
            int available = maxLength - currentLen;
            if (available <= 0) {
              return;
            }
            if (string.length() > available) {
              string = string.substring(0, available);
            }
          }
          super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
          if (maxLength > 0 && text != null) {
            int currentLen = fb.getDocument().getLength();
            int available = maxLength - (currentLen - length);
            if (available <= 0) {
              return;
            }
            if (text.length() > available) {
              text = text.substring(0, available);
            }
          }
          super.replace(fb, offset, length, text, attrs);
        }
      });
    }
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

    // --- 右侧区域：addonAfter > (suffix + allowClear + showCount) ---
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
    } else {
      // 组合右侧面板：suffix / allowClear / showCount
      JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
      rightPanel.setOpaque(false);
      boolean hasRight = false;

      if (suffix != null) {
        JLabel sufLabel = new JLabel(suffix);
        sufLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        rightPanel.add(sufLabel);
        hasRight = true;
      }

      if (allowClear) {
        JLabel clearLabel = new JLabel("✕");
        clearLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
        clearLabel.setForeground(ct.getTextTertiaryColor());
        clearLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        clearLabel.setToolTipText("Clear");
        clearLabel.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (isEnabled()) {
              textField.setText("");
              textField.requestFocusInWindow();
            }
          }

          @Override
          public void mouseEntered(java.awt.event.MouseEvent e) {
            clearLabel.setForeground(ct.getTextSecondaryColor());
          }

          @Override
          public void mouseExited(java.awt.event.MouseEvent e) {
            clearLabel.setForeground(ct.getTextTertiaryColor());
          }
        });
        rightPanel.add(clearLabel);
        hasRight = true;
      }

      if (showCount) {
        countLabel = new JLabel(buildCountText());
        countLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
        countLabel.setForeground(ct.getTextTertiaryColor());
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        rightPanel.add(countLabel);
        hasRight = true;
      } else {
        countLabel = null;
      }

      if (hasRight) {
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, st.getPaddingXs()));
        add(rightPanel, BorderLayout.EAST);
      }
    }

    revalidate();
    repaint();
  }

  /** 构建字数统计文本。 */
  private String buildCountText() {
    int len = textField != null ? textField.getText().length() : 0;
    if (maxLength > 0) {
      return len + " / " + maxLength;
    }
    return String.valueOf(len);
  }

  /** 更新字数统计显示。 */
  private void updateCountLabel() {
    if (countLabel != null && showCount) {
      countLabel.setText(buildCountText());
    }
  }

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    textField.setFont(ft.createFont(size, Font.PLAIN));
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
