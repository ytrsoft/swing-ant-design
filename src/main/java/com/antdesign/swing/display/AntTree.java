package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 树形控件。
 *
 * <p>对应 Ant Design {@code <Tree>}，支持节点展开收起、勾选、搜索、
 * 拖拽等场景。内部封装 {@link JTree}，并应用 Ant Design 样式。
 *
 * <pre>{@code
 * DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
 * root.add(new DefaultMutableTreeNode("Child 1"));
 * root.add(new DefaultMutableTreeNode("Child 2"));
 * AntTree tree = new AntTree(root);
 * }</pre>
 */
public class AntTree extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private boolean showLine;
  @Getter private boolean checkable;
  @Getter private boolean defaultExpandAll;
  @Getter private boolean showIcon;

  private final JTree jTree;
  private final DefaultTreeModel treeModel;
  private final List<AntChangeListener<TreePath[]>> selectionListeners = new ArrayList<>();

  /**
   * 用根节点创建树。
   *
   * @param root 根节点
   */
  public AntTree(DefaultMutableTreeNode root) {
    this.treeModel = new DefaultTreeModel(root);
    this.jTree = new JTree(treeModel);
    this.showIcon = true;

    setLayout(new BorderLayout());

    jTree.setRootVisible(true);
    jTree.setShowsRootHandles(true);
    jTree.getSelectionModel().setSelectionMode(
        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    jTree.addTreeSelectionListener(e -> {
      TreePath[] paths = jTree.getSelectionPaths();
      AntChangeEvent<TreePath[]> evt = new AntChangeEvent<>(this, null, paths);
      for (AntChangeListener<TreePath[]> l : selectionListeners) {
        l.valueChanged(evt);
      }
    });

    JScrollPane scroll = new JScrollPane(jTree);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    add(scroll, BorderLayout.CENTER);

    applyTheme();
    setThemeListener(theme -> applyTheme());
  }

  /** 创建空根的树。 */
  public AntTree() {
    this(new DefaultMutableTreeNode("Root"));
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setShowLine(boolean showLine) {
    this.showLine = showLine;
    jTree.putClientProperty("JTree.lineStyle", showLine ? "Angled" : "None");
    repaint();
  }

  public void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  public void setDefaultExpandAll(boolean defaultExpandAll) {
    this.defaultExpandAll = defaultExpandAll;
    if (defaultExpandAll) {
      expandAll();
    }
  }

  public void setShowIcon(boolean showIcon) {
    this.showIcon = showIcon;
    applyTheme();
  }

  public void setRootVisible(boolean visible) {
    jTree.setRootVisible(visible);
  }

  /** 添加选中变更监听器。 */
  public void addSelectionListener(AntChangeListener<TreePath[]> listener) {
    if (listener != null) {
      selectionListeners.add(listener);
    }
  }

  /** 获取内部 JTree。 */
  public JTree getJTree() {
    return jTree;
  }

  /** 获取树模型。 */
  public DefaultTreeModel getTreeModel() {
    return treeModel;
  }

  /** 展开全部节点。 */
  public void expandAll() {
    for (int i = 0; i < jTree.getRowCount(); i++) {
      jTree.expandRow(i);
    }
  }

  /** 折叠全部节点。 */
  public void collapseAll() {
    for (int i = jTree.getRowCount() - 1; i >= 0; i--) {
      jTree.collapseRow(i);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    jTree.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    jTree.setBackground(ct.getBgContainer());
    jTree.setForeground(ct.getTextColor());
    jTree.setRowHeight(28);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setBackgroundSelectionColor(ct.getPrimaryBgColor());
    renderer.setTextSelectionColor(ct.getPrimaryColor());
    renderer.setTextNonSelectionColor(ct.getTextColor());
    renderer.setBackgroundNonSelectionColor(ct.getBgContainer());
    renderer.setBorderSelectionColor(null);
    renderer.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));

    if (!showIcon) {
      renderer.setLeafIcon(null);
      renderer.setClosedIcon(null);
      renderer.setOpenIcon(null);
    }

    jTree.setCellRenderer(renderer);
    repaint();
  }
}
