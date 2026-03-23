package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 表格组件。
 *
 * <p>对应 Ant Design {@code <Table>}，展示行列数据。支持列定义、
 * 行选中、自定义单元格渲染、尺寸切换和边框模式。
 *
 * <pre>{@code
 * AntTable.Column[] columns = {
 *   new AntTable.Column("name", "Name"),
 *   new AntTable.Column("age", "Age", 80)
 * };
 * AntTable table = new AntTable(columns);
 * table.addRow(new Object[]{"John", 28});
 * }</pre>
 */
public class AntTable extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 列定义。 */
  @Getter
  public static class Column {
    private final String key;
    private final String title;
    private final int width;
    private java.util.function.BiFunction<Object, Object[], Component> render;
    private java.util.Comparator<Object> sorter;
    private java.util.function.Function<Object, String[]> filters;

    public Column(String key, String title) {
      this(key, title, -1);
    }

    public Column(String key, String title, int width) {
      this.key = key;
      this.title = title;
      this.width = width;
    }

    /**
     * 设置自定义渲染函数。
     *
     * @param render 渲染函数 (cellValue, rowData) → Component
     * @return this（链式调用）
     */
    public Column setRender(
        java.util.function.BiFunction<Object, Object[], Component> render) {
      this.render = render;
      return this;
    }

    /**
     * 设置排序比较器。设置后表头可点击排序。
     *
     * @param sorter 列值比较器
     * @return this
     */
    public Column setSorter(java.util.Comparator<Object> sorter) {
      this.sorter = sorter;
      return this;
    }
  }

  @Getter private Column[] columns;
  @Getter private boolean bordered;
  @Getter private boolean showHeader;
  @Getter private boolean loading;
  private ComponentSize size;

  // 分页
  @Getter private int pageSize;
  @Getter private int currentPage;

  // 排序
  private int sortColumnIndex = -1;
  private boolean sortAscending = true;

  private final List<Object[]> dataRows = new ArrayList<>();
  private List<Object[]> displayRows; // 排序后视图
  private JTable jTable;
  private AntTableModel tableModel;
  private JPanel paginationPanel;
  private JLabel pageInfoLabel;
  private final List<AntChangeListener<int[]>> selectionListeners = new ArrayList<>();

  /**
   * 创建表格。
   *
   * @param columns 列定义数组
   */
  public AntTable(Column[] columns) {
    this.columns = (columns != null) ? columns : new Column[0];
    this.bordered = true;
    this.showHeader = true;
    this.size = ComponentSize.MIDDLE;
    this.pageSize = -1; // -1 表示不分页
    this.currentPage = 1;
    this.displayRows = dataRows;

    setLayout(new BorderLayout());
    buildTable();
    setThemeListener(theme -> applyTheme());
  }

  // =========================================================================
  // 数据操作
  // =========================================================================

  /**
   * 添加一行数据。
   *
   * @param row 行数据数组，长度应与列数一致
   */
  public void addRow(Object[] row) {
    dataRows.add(row);
    refreshDisplay();
  }

  /**
   * 批量设置数据。
   *
   * @param rows 所有行数据
   */
  public void setData(List<Object[]> rows) {
    dataRows.clear();
    if (rows != null) {
      dataRows.addAll(rows);
    }
    currentPage = 1;
    refreshDisplay();
  }

  /**
   * 移除指定行。
   *
   * @param rowIndex 行索引
   */
  public void removeRow(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < dataRows.size()) {
      dataRows.remove(rowIndex);
      refreshDisplay();
    }
  }

  /** 清空所有数据。 */
  public void clearData() {
    dataRows.clear();
    currentPage = 1;
    refreshDisplay();
  }

  /** 获取选中行索引。 */
  public int[] getSelectedRows() {
    return jTable.getSelectedRows();
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setColumns(Column[] columns) {
    this.columns = (columns != null) ? columns : new Column[0];
    buildTable();
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    applyTheme();
  }

  public void setShowHeader(boolean showHeader) {
    this.showHeader = showHeader;
    jTable.getTableHeader().setVisible(showHeader);
    jTable.getTableHeader().setPreferredSize(
        showHeader ? null : new Dimension(0, 0));
    revalidate();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    applyTheme();
  }

  public ComponentSize getComponentSize() {
    return size;
  }

  /**
   * 设置每页行数。{@code -1} 表示不分页。
   *
   * @param pageSize 每页行数
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
    this.currentPage = 1;
    refreshDisplay();
    buildPagination();
  }

  /**
   * 跳转到指定页。
   *
   * @param page 页码（从 1 开始）
   */
  public void setCurrentPage(int page) {
    this.currentPage = Math.max(1, Math.min(page, getTotalPages()));
    refreshDisplay();
    updatePaginationLabel();
  }

  /** 设置加载状态。 */
  public void setLoading(boolean loading) {
    this.loading = loading;
    repaint();
  }

  /** 获取总页数。 */
  public int getTotalPages() {
    if (pageSize <= 0) {
      return 1;
    }
    return Math.max(1, (int) Math.ceil((double) displayRows.size() / pageSize));
  }

  /** 添加行选中变更监听器。 */
  public void addSelectionListener(AntChangeListener<int[]> listener) {
    if (listener != null) {
      selectionListeners.add(listener);
    }
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void buildTable() {
    removeAll();
    tableModel = new AntTableModel();
    jTable = new JTable(tableModel);
    jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    jTable.setFillsViewportHeight(true);
    jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    jTable.setRowMargin(0);
    jTable.setShowGrid(false);
    jTable.setIntercellSpacing(new Dimension(0, 0));

    jTable.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int[] selected = jTable.getSelectedRows();
        AntChangeEvent<int[]> evt = new AntChangeEvent<>(this, null, selected);
        for (AntChangeListener<int[]> l : selectionListeners) {
          l.valueChanged(evt);
        }
      }
    });

    // 列宽
    for (int i = 0; i < columns.length; i++) {
      if (columns[i].width > 0 && i < jTable.getColumnModel().getColumnCount()) {
        TableColumn tc = jTable.getColumnModel().getColumn(i);
        tc.setPreferredWidth(columns[i].width);
      }
    }

    // 表头点击排序
    jTable.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        int col = jTable.columnAtPoint(e.getPoint());
        if (col >= 0 && col < columns.length && columns[col].getSorter() != null) {
          if (sortColumnIndex == col) {
            sortAscending = !sortAscending;
          } else {
            sortColumnIndex = col;
            sortAscending = true;
          }
          refreshDisplay();
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(jTable);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);

    // 分页栏
    buildPagination();

    applyTheme();
    revalidate();
    repaint();
  }

  /** 刷新显示数据（排序 + 分页）。 */
  private void refreshDisplay() {
    // 排序
    if (sortColumnIndex >= 0 && sortColumnIndex < columns.length
        && columns[sortColumnIndex].getSorter() != null) {
      displayRows = new ArrayList<>(dataRows);
      java.util.Comparator<Object> cmp = columns[sortColumnIndex].getSorter();
      int colIdx = sortColumnIndex;
      displayRows.sort((a, b) -> {
        Object va = (colIdx < a.length) ? a[colIdx] : null;
        Object vb = (colIdx < b.length) ? b[colIdx] : null;
        int result = cmp.compare(va, vb);
        return sortAscending ? result : -result;
      });
    } else {
      displayRows = dataRows;
    }

    if (tableModel != null) {
      tableModel.fireTableDataChanged();
    }
    updatePaginationLabel();
  }

  /** 获取当前页的数据视图。 */
  private List<Object[]> getPageView() {
    if (pageSize <= 0) {
      return displayRows;
    }
    int from = (currentPage - 1) * pageSize;
    int to = Math.min(from + pageSize, displayRows.size());
    if (from >= displayRows.size()) {
      return java.util.Collections.emptyList();
    }
    return displayRows.subList(from, to);
  }

  /** 构建分页栏。 */
  private void buildPagination() {
    if (paginationPanel != null) {
      remove(paginationPanel);
    }
    if (pageSize <= 0) {
      paginationPanel = null;
      return;
    }

    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    paginationPanel.setOpaque(false);
    paginationPanel.setBorder(BorderFactory.createMatteBorder(
        1, 0, 0, 0, ct.getBorderSecondaryColor()));

    JLabel prevBtn = new JLabel("◀");
    prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    prevBtn.setForeground(ct.getTextSecondaryColor());
    prevBtn.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (currentPage > 1) {
          setCurrentPage(currentPage - 1);
        }
      }
    });
    paginationPanel.add(prevBtn);

    pageInfoLabel = new JLabel();
    pageInfoLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
    pageInfoLabel.setForeground(ct.getTextColor());
    paginationPanel.add(pageInfoLabel);

    JLabel nextBtn = new JLabel("▶");
    nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    nextBtn.setForeground(ct.getTextSecondaryColor());
    nextBtn.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (currentPage < getTotalPages()) {
          setCurrentPage(currentPage + 1);
        }
      }
    });
    paginationPanel.add(nextBtn);

    add(paginationPanel, BorderLayout.SOUTH);
    updatePaginationLabel();
  }

  /** 更新分页标签文本。 */
  private void updatePaginationLabel() {
    if (pageInfoLabel != null) {
      pageInfoLabel.setText(currentPage + " / " + getTotalPages()
          + "  (共 " + displayRows.size() + " 条)");
    }
  }

  private void applyTheme() {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int rowHeight;
    int fontSize;
    switch (size) {
      case LARGE:
        rowHeight = 56;
        fontSize = ft.getFontSizeLg();
        break;
      case SMALL:
        rowHeight = 36;
        fontSize = ft.getFontSizeSm();
        break;
      default:
        rowHeight = 46;
        fontSize = ft.getFontSize();
        break;
    }

    jTable.setRowHeight(rowHeight);
    jTable.setFont(ft.createFont(fontSize, Font.PLAIN));
    jTable.setForeground(ct.getTextColor());
    jTable.setBackground(ct.getBgContainer());
    jTable.setSelectionBackground(ct.getPrimaryBgColor());
    jTable.setSelectionForeground(ct.getTextColor());
    jTable.setGridColor(ct.getBorderSecondaryColor());
    jTable.setShowHorizontalLines(true);

    // 表头（带排序指示器）
    JTableHeader header = jTable.getTableHeader();
    header.setFont(ft.createFont(fontSize, Font.BOLD));
    header.setBackground(themeManager().isDark()
        ? ColorToken.parseHexColor("#1D1D1D") : ColorToken.parseHexColor("#FAFAFA"));
    header.setForeground(ct.getTextColor());
    header.setDefaultRenderer(new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, col);
        label.setFont(ft.createFont(fontSize, Font.BOLD));
        label.setBackground(themeManager().isDark()
            ? ColorToken.parseHexColor("#1D1D1D") : ColorToken.parseHexColor("#FAFAFA"));
        label.setForeground(ct.getTextColor());
        label.setHorizontalAlignment(JLabel.LEFT);
        int pad = st.getPaddingSm();
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ct.getBorderSecondaryColor()),
            BorderFactory.createEmptyBorder(0, pad, 0, pad)));
        // 排序指示器
        if (col < columns.length && columns[col].getSorter() != null
            && sortColumnIndex == col) {
          String arrow = sortAscending ? " ↑" : " ↓";
          label.setText(label.getText() + arrow);
        }
        return label;
      }
    });

    // 单元格渲染（支持 Column.render 自定义）
    for (int i = 0; i < jTable.getColumnCount(); i++) {
      final int colIdx = i;
      jTable.getColumnModel().getColumn(i).setCellRenderer(
          new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
              // 尝试使用自定义 render
              if (colIdx < columns.length && columns[colIdx].getRender() != null) {
                List<Object[]> pageData = getPageView();
                Object[] rowData = (row < pageData.size()) ? pageData.get(row) : null;
                Component custom = columns[colIdx].getRender().apply(value, rowData);
                if (custom != null) {
                  if (isSelected) {
                    custom.setBackground(ct.getPrimaryBgColor());
                  }
                  return custom;
                }
              }
              // 默认渲染
              DefaultTableCellRenderer def = new DefaultTableCellRenderer();
              JLabel label = (JLabel) def.getTableCellRendererComponent(
                  table, value, isSelected, hasFocus, row, column);
              int pad = (size == ComponentSize.SMALL) ? st.getPaddingXs() : st.getPaddingSm();
              label.setBorder(BorderFactory.createEmptyBorder(0, pad, 0, pad));
              label.setFont(ft.createFont(fontSize, Font.PLAIN));
              if (isSelected) {
                label.setBackground(ct.getPrimaryBgColor());
                label.setForeground(ct.getTextColor());
              } else {
                label.setBackground(ct.getBgContainer());
                label.setForeground(ct.getTextColor());
              }
              return label;
            }
          });
    }

    if (bordered) {
      setBorder(BorderFactory.createLineBorder(ct.getBorderSecondaryColor()));
    } else {
      setBorder(null);
    }

    repaint();
  }

  // =========================================================================
  // TableModel
  // =========================================================================

  private class AntTableModel extends AbstractTableModel {

    @Override
    public int getRowCount() {
      return getPageView().size();
    }

    @Override
    public int getColumnCount() {
      return columns.length;
    }

    @Override
    public String getColumnName(int column) {
      return (column < columns.length) ? columns[column].title : "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      List<Object[]> page = getPageView();
      if (rowIndex < page.size()) {
        Object[] row = page.get(rowIndex);
        if (columnIndex < row.length) {
          return row[columnIndex];
        }
      }
      return "";
    }
  }
}
