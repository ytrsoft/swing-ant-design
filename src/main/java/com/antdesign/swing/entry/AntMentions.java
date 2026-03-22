package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ant Design 提及组件。
 *
 * <p>对应 Ant Design {@code <Mentions>}，输入 @ 后弹出候选列表。
 *
 * <pre>{@code
 * AntMentions mentions = new AntMentions();
 * mentions.setOptions(Arrays.asList("afc163", "zombieJ", "yesmeck"));
 * }</pre>
 */
public class AntMentions extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private List<String> options;
  @Getter private String prefix;
  @Getter private String placeholder;

  private JTextArea textArea;
  private Popup dropdownPopup;
  private final List<AntChangeListener<String>> changeListeners = new ArrayList<>();

  /** 创建提及组件。 */
  public AntMentions() {
    this.options = new ArrayList<>();
    this.prefix = "@";

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

  public void setPrefix(String prefix) {
    this.prefix = (prefix != null && !prefix.isEmpty()) ? prefix : "@";
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public String getValue() { return textArea.getText(); }
  public void setValue(String value) { textArea.setText(value); }

  public void addChangeListener(AntChangeListener<String> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  public JTextArea getTextArea() { return textArea; }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    return new Dimension(300, 80);
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildUi() {
    textArea = new JTextArea(3, 30);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) { checkMention(); }
      @Override
      public void removeUpdate(DocumentEvent e) { hideDropdown(); }
      @Override
      public void changedUpdate(DocumentEvent e) { checkMention(); }
    });

    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    add(scroll, BorderLayout.CENTER);
    applyTheme();
  }

  private void checkMention() {
    String text = textArea.getText();
    int caret = textArea.getCaretPosition();
    if (caret <= 0) {
      hideDropdown();
      return;
    }
    // 查找最后一个 prefix
    int prefIdx = text.lastIndexOf(prefix, caret - 1);
    if (prefIdx < 0) {
      hideDropdown();
      return;
    }
    String query = text.substring(prefIdx + prefix.length(), caret).toLowerCase();
    List<String> filtered = options.stream()
        .filter(o -> o.toLowerCase().startsWith(query))
        .collect(Collectors.toList());

    if (filtered.isEmpty()) {
      hideDropdown();
    } else {
      showDropdown(filtered, prefIdx);
    }
  }

  private void showDropdown(List<String> items, int insertPos) {
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
          int caret = textArea.getCaretPosition();
          String text = textArea.getText();
          String before = text.substring(0, insertPos);
          String after = (caret < text.length()) ? text.substring(caret) : "";
          textArea.setText(before + prefix + selected + " " + after);
          hideDropdown();
        }
      }
    });

    JScrollPane scroll = new JScrollPane(list);
    scroll.setPreferredSize(new Dimension(180, Math.min(items.size() * 28, 160)));
    scroll.setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));

    try {
      java.awt.Point p = textArea.getLocationOnScreen();
      java.awt.Rectangle caretRect = textArea.modelToView(textArea.getCaretPosition());
      dropdownPopup = PopupFactory.getSharedInstance().getPopup(
          this, scroll, p.x + (caretRect != null ? caretRect.x : 0),
          p.y + (caretRect != null ? caretRect.y + caretRect.height : textArea.getHeight()));
      dropdownPopup.show();
    } catch (Exception ignored) {
      // 忽略
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
    textArea.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    textArea.setForeground(ct.getTextColor());
    textArea.setBackground(ct.getBgContainer());
    textArea.setCaretColor(ct.getTextColor());
    setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    repaint();
  }
}
