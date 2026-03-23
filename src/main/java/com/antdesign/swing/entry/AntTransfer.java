package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 穿梭框组件。
 *
 * <p>对应 Ant Design {@code <Transfer>}，双栏穿梭选择框。
 *
 * <pre>{@code
 * AntTransfer transfer = new AntTransfer();
 * transfer.setSourceData(Arrays.asList("Item 1", "Item 2", "Item 3"));
 * }</pre>
 */
public class AntTransfer extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String sourceTitle;
  @Getter private String targetTitle;

  private final DefaultListModel<String> sourceModel = new DefaultListModel<>();
  private final DefaultListModel<String> targetModel = new DefaultListModel<>();
  private JList<String> sourceList;
  private JList<String> targetList;
  private final List<AntChangeListener<List<String>>> changeListeners = new ArrayList<>();

  /** 创建穿梭框。 */
  public AntTransfer() {
    this.sourceTitle = "Source";
    this.targetTitle = "Target";

    setLayout(new BorderLayout());
    buildUi();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // 数据操作
  // =========================================================================

  /** 设置源列表数据。 */
  public void setSourceData(List<String> items) {
    sourceModel.clear();
    if (items != null) {
      items.forEach(sourceModel::addElement);
    }
  }

  /** 设置目标列表数据。 */
  public void setTargetData(List<String> items) {
    targetModel.clear();
    if (items != null) {
      items.forEach(targetModel::addElement);
    }
  }

  /** 获取目标列表数据。 */
  public List<String> getTargetData() {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < targetModel.size(); i++) {
      result.add(targetModel.get(i));
    }
    return result;
  }

  /** 将选中项移至目标。 */
  public void moveToTarget() {
    List<String> selected = sourceList.getSelectedValuesList();
    for (String item : selected) {
      sourceModel.removeElement(item);
      targetModel.addElement(item);
    }
    fireChange();
  }

  /** 将选中项移回源。 */
  public void moveToSource() {
    List<String> selected = targetList.getSelectedValuesList();
    for (String item : selected) {
      targetModel.removeElement(item);
      sourceModel.addElement(item);
    }
    fireChange();
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setSourceTitle(String title) {
    this.sourceTitle = title;
    buildUi();
  }

  public void setTargetTitle(String title) {
    this.targetTitle = title;
    buildUi();
  }

  public void addChangeListener(AntChangeListener<List<String>> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    return new Dimension(500, 300);
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    // 创建 JList 实例
    sourceList = new JList<>(sourceModel);
    targetList = new JList<>(targetModel);

    // 源列表面板
    JPanel sourcePanel = buildListPanel(sourceTitle, sourceList, ct, ft, st);

    // 按钮
    JPanel btnPanel = new JPanel();
    btnPanel.setOpaque(false);
    btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
    btnPanel.setBorder(BorderFactory.createEmptyBorder(0, st.getPaddingXs(), 0, st.getPaddingXs()));

    JLabel toRight = new JLabel(" ▶ ");
    toRight.setForeground(ct.getPrimaryColor());
    toRight.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    toRight.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ct.getBorderColor()),
        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    toRight.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) { moveToTarget(); }
    });

    JLabel toLeft = new JLabel(" ◀ ");
    toLeft.setForeground(ct.getPrimaryColor());
    toLeft.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    toLeft.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ct.getBorderColor()),
        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    toLeft.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) { moveToSource(); }
    });

    btnPanel.add(javax.swing.Box.createVerticalGlue());
    btnPanel.add(toRight);
    btnPanel.add(javax.swing.Box.createVerticalStrut(8));
    btnPanel.add(toLeft);
    btnPanel.add(javax.swing.Box.createVerticalGlue());

    // 目标列表面板
    JPanel targetPanel = buildListPanel(targetTitle, targetList, ct, ft, st);

    JPanel main = new JPanel(new BorderLayout());
    main.setOpaque(false);
    main.add(sourcePanel, BorderLayout.WEST);
    main.add(btnPanel, BorderLayout.CENTER);
    main.add(targetPanel, BorderLayout.EAST);
    add(main, BorderLayout.CENTER);

    revalidate();
    repaint();
  }

  private JPanel buildListPanel(String title, JList<String> list,
      ColorToken ct, FontToken ft, SizeToken st) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setPreferredSize(new Dimension(200, 0));
    panel.setBorder(BorderFactory.createLineBorder(ct.getBorderSecondaryColor()));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(ft.createFont(ft.getFontSize(), Font.BOLD));
    titleLabel.setForeground(ct.getTextColor());
    titleLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, ct.getBorderSecondaryColor()),
        BorderFactory.createEmptyBorder(st.getPaddingXs(), st.getPaddingSm(),
            st.getPaddingXs(), st.getPaddingSm())));
    panel.add(titleLabel, BorderLayout.NORTH);

    list.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    list.setForeground(ct.getTextColor());
    list.setBackground(ct.getBgContainer());
    list.setSelectionBackground(ct.getPrimaryBgColor());

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    panel.add(scroll, BorderLayout.CENTER);

    return panel;
  }

  private void applyTheme() {
    buildUi();
  }

  private void fireChange() {
    List<String> target = getTargetData();
    AntChangeEvent<List<String>> evt = new AntChangeEvent<>(this, null, target);
    for (AntChangeListener<List<String>> l : changeListeners) {
      l.valueChanged(evt);
    }
  }
}
