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

    public Column(String key, String title) {
      this(key, title, -1);
    }

    public Column(String key, String title, int width) {
      this.key = key;
      this.title = title;
      this.width = width;
    }
  }

  @Getter private Column[] columns;
  @Getter private boolean bordered;
  @Getter private boolean showHeader;
  private ComponentSize size;

  private final List<Object[]> dataRows = new ArrayList<>();
  private JTable jTable;
  private AntTableModel tableModel;
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
    tableModel.fireTableRowsInserted(dataRows.size() - 1, dataRows.size() - 1);
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
    tableModel.fireTableDataChanged();
  }

  /**
   * 移除指定行。
   *
   * @param rowIndex 行索引
   */
  public void removeRow(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < dataRows.size()) {
      dataRows.remove(rowIndex);
      tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
    }
  }

  /** 清空所有数据。 */
  public void clearData() {
    dataRows.clear();
    tableModel.fireTableDataChanged();
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

    JScrollPane scrollPane = new JScrollPane(jTable);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);

    applyTheme();
    revalidate();
    repaint();
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

    // 表头
    JTableHeader header = jTable.getTableHeader();
    header.setFont(ft.createFont(fontSize, Font.BOLD));
    header.setBackground(themeManager().isDark()
        ? ColorToken.parseHexColor("#1D1D1D") : ColorToken.parseHexColor("#FAFAFA"));
    header.setForeground(ct.getTextColor());

    // 单元格渲染
    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        int pad = (size == ComponentSize.SMALL) ? st.getPaddingXs() : st.getPaddingSm();
        label.setBorder(BorderFactory.createEmptyBorder(0, pad, 0, pad));
        return label;
      }
    };
    for (int i = 0; i < jTable.getColumnCount(); i++) {
      jTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
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
      return dataRows.size();
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
      if (rowIndex < dataRows.size()) {
        Object[] row = dataRows.get(rowIndex);
        if (columnIndex < row.length) {
          return row[columnIndex];
        }
      }
      return "";
    }
  }
}
