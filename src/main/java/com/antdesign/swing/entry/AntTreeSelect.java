package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.display.AntTree;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.model.Variant;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Ant Design 树选择组件。
 *
 * <p>对应 Ant Design {@code <TreeSelect>}，类似 Select 的选择控件，
 * 可选择的数据结构是一个树形结构。
 *
 * <pre>{@code
 * DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
 * root.add(new DefaultMutableTreeNode("Child 1"));
 * AntTreeSelect ts = new AntTreeSelect(root);
 * }</pre>
 */
public class AntTreeSelect extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String placeholder;
  @Getter private Variant variant;
  private ComponentSize size;

  private final DefaultMutableTreeNode root;
  private JTextField textField;
  private Popup treePopup;
  private AntChangeListener<String> changeListener;

  /**
   * 创建树选择器。
   *
   * @param root 树根节点
   */
  public AntTreeSelect(DefaultMutableTreeNode root) {
    this.root = (root != null) ? root : new DefaultMutableTreeNode("Root");
    this.placeholder = "Please select";
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  public AntTreeSelect() {
    this(new DefaultMutableTreeNode("Root"));
  }

  // =========================================================================
  // Setter
  // =========================================================================

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

  public String getValue() { return textField.getText(); }

  public void addChangeListener(AntChangeListener<String> listener) {
    this.changeListener = listener;
  }

  /** 获取树根节点。 */
  public DefaultMutableTreeNode getRoot() { return root; }

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
        toggleTree();
      }
    });
    add(textField, BorderLayout.CENTER);

    javax.swing.JLabel arrow = new javax.swing.JLabel("▼");
    arrow.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
    add(arrow, BorderLayout.EAST);
    applyTheme();
  }

  private void toggleTree() {
    if (treePopup != null) {
      treePopup.hide();
      treePopup = null;
      return;
    }

    AntTree tree = new AntTree(root);
    tree.setRootVisible(false);
    tree.setDefaultExpandAll(true);
    tree.setPreferredSize(new Dimension(getWidth(), 200));
    tree.addSelectionListener(e -> {
      TreePath[] paths = e.getNewValue();
      if (paths != null && paths.length > 0) {
        Object node = paths[0].getLastPathComponent();
        String val = node.toString();
        textField.setText(val);
        if (changeListener != null) {
          changeListener.valueChanged(new AntChangeEvent<>(this, null, val));
        }
        if (treePopup != null) {
          treePopup.hide();
          treePopup = null;
        }
      }
    });

    JScrollPane scroll = new JScrollPane(tree);
    scroll.setPreferredSize(new Dimension(getWidth(), 200));
    ColorToken ct = colorToken();
    scroll.setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));

    try {
      java.awt.Point p = getLocationOnScreen();
      treePopup = PopupFactory.getSharedInstance().getPopup(
          this, scroll, p.x, p.y + getHeight());
      treePopup.show();
    } catch (Exception ignored) {
      // 未显示时忽略
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

    if (variant == Variant.BORDERLESS) {
      setBorder(null);
    } else {
      setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    }
    repaint();
  }
}
