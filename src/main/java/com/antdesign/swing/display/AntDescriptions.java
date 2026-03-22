package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 描述列表组件。
 *
 * <p>对应 Ant Design {@code <Descriptions>}，成组展示多个只读字段。
 * 常见于详情页的信息展示。
 *
 * <pre>{@code
 * AntDescriptions desc = new AntDescriptions("User Info");
 * desc.addItem("Name", "John");
 * desc.addItem("Age", "28");
 * desc.addItem("Address", "New York");
 * }</pre>
 */
public class AntDescriptions extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 描述项。 */
  @Getter
  public static class Item {
    private final String label;
    private final String content;
    private final int span;

    public Item(String label, String content, int span) {
      this.label = label;
      this.content = content;
      this.span = Math.max(1, span);
    }

    public Item(String label, String content) {
      this(label, content, 1);
    }
  }

  @Getter private String title;
  @Getter private boolean bordered;
  @Getter private int column;
  private ComponentSize size;
  private final List<Item> items = new ArrayList<>();

  /**
   * 创建描述列表。
   *
   * @param title 标题
   */
  public AntDescriptions(String title) {
    this.title = title;
    this.column = 3;
    this.size = ComponentSize.MIDDLE;
    this.bordered = false;

    setLayout(new BorderLayout());
    rebuildUi();
    setThemeListener(theme -> rebuildUi());
  }

  public AntDescriptions() {
    this(null);
  }

  // =========================================================================
  // 数据操作
  // =========================================================================

  /**
   * 添加描述项。
   *
   * @param label   标签
   * @param content 内容
   */
  public void addItem(String label, String content) {
    items.add(new Item(label, content));
    rebuildUi();
  }

  /**
   * 添加跨列的描述项。
   *
   * @param label   标签
   * @param content 内容
   * @param span    跨列数
   */
  public void addItem(String label, String content, int span) {
    items.add(new Item(label, content, span));
    rebuildUi();
  }

  /** 清空所有项。 */
  public void clearItems() {
    items.clear();
    rebuildUi();
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = title;
    rebuildUi();
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    rebuildUi();
  }

  public void setColumn(int column) {
    this.column = Math.max(1, column);
    rebuildUi();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    rebuildUi();
  }

  public ComponentSize getComponentSize() {
    return size;
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int pad;
    int fontSize;
    switch (size) {
      case LARGE:
        pad = st.getPadding();
        fontSize = ft.getFontSizeLg();
        break;
      case SMALL:
        pad = st.getPaddingXs();
        fontSize = ft.getFontSizeSm();
        break;
      default:
        pad = st.getPaddingSm();
        fontSize = ft.getFontSize();
        break;
    }

    JPanel main = new JPanel(new BorderLayout());
    main.setOpaque(false);

    // 标题
    if (title != null && !title.isEmpty()) {
      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
      titleLabel.setForeground(ct.getTextColor());
      titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, st.getPadding(), 0));
      main.add(titleLabel, BorderLayout.NORTH);
    }

    // 内容网格
    JPanel grid = new JPanel(new GridBagLayout());
    grid.setOpaque(false);
    if (bordered) {
      grid.setBorder(BorderFactory.createLineBorder(ct.getBorderSecondaryColor()));
    }

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(pad, pad, pad, pad);

    int col = 0;
    int row = 0;
    for (Item item : items) {
      int span = Math.min(item.span, column - col);

      // 标签
      gbc.gridx = col * 2;
      gbc.gridy = row;
      gbc.gridwidth = 1;
      gbc.weightx = 0;
      JLabel label = new JLabel(item.label);
      label.setFont(ft.createFont(fontSize, Font.PLAIN));
      label.setForeground(ct.getTextTertiaryColor());
      grid.add(label, gbc);

      // 内容
      gbc.gridx = col * 2 + 1;
      gbc.gridwidth = span * 2 - 1;
      gbc.weightx = 1.0;
      JLabel content = new JLabel(item.content);
      content.setFont(ft.createFont(fontSize, Font.PLAIN));
      content.setForeground(ct.getTextColor());
      grid.add(content, gbc);

      col += span;
      if (col >= column) {
        col = 0;
        row++;
      }
    }

    main.add(grid, BorderLayout.CENTER);
    add(main, BorderLayout.CENTER);
    revalidate();
    repaint();
  }
}
