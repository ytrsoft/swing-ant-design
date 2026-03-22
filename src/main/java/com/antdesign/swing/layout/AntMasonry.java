package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;

import java.awt.*;

/**
 * Ant Design 风格瀑布流布局。
 *
 * <p>将子组件分配到指定列数的瀑布流中，每个组件放入当前最矮的列，
 * 适用于不等高卡片、图片墙等场景。
 *
 * <pre>{@code
 * AntMasonry masonry = new AntMasonry(3);  // 3 列
 * masonry.setGap(16);
 * masonry.add(card1);
 * masonry.add(card2);
 * masonry.add(card3);
 * masonry.add(card4);
 * masonry.add(card5);
 * }</pre>
 */
public class AntMasonry extends AntPanel {

  private static final long serialVersionUID = 1L;

  private int columns;
  private int gap;

  /**
   * 创建 2 列、16px 间距的瀑布流容器。
   */
  public AntMasonry() {
    this(2, 16);
  }

  /**
   * 创建指定列数的瀑布流容器（默认 16px 间距）。
   *
   * @param columns 列数
   */
  public AntMasonry(int columns) {
    this(columns, 16);
  }

  /**
   * 创建指定列数和间距的瀑布流容器。
   *
   * @param columns 列数
   * @param gap     水平和垂直间距（像素）
   */
  public AntMasonry(int columns, int gap) {
    this.columns = Math.max(1, columns);
    this.gap = gap;
    setOpaque(false);
    setLayout(new MasonryLayout());
  }

  /** 设置列数。 */
  public void setColumns(int columns) {
    this.columns = Math.max(1, columns);
    revalidate();
  }

  /** 获取列数。 */
  public int getColumns() {
    return columns;
  }

  /** 设置间距（像素）。 */
  public void setGap(int gap) {
    this.gap = gap;
    revalidate();
  }

  /** 获取间距。 */
  public int getGap() {
    return gap;
  }

  // ---------------------------------------------------------------------------
  // MasonryLayout
  // ---------------------------------------------------------------------------

  private class MasonryLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return computeSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return computeSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
      Insets insets = parent.getInsets();
      int containerWidth = parent.getWidth() - insets.left - insets.right;

      int colWidth = (containerWidth - (columns - 1) * gap) / columns;
      int[] colHeights = new int[columns];

      for (int i = 0; i < parent.getComponentCount(); i++) {
        Component c = parent.getComponent(i);
        if (!c.isVisible()) {
          continue;
        }

        // 找最矮列
        int shortest = 0;
        for (int col = 1; col < columns; col++) {
          if (colHeights[col] < colHeights[shortest]) {
            shortest = col;
          }
        }

        // 使用 colWidth 作为宽度，按比例缩放高度
        Dimension pref = c.getPreferredSize();
        int h = pref.width > 0
            ? (int) ((long) pref.height * colWidth / pref.width)
            : pref.height;

        int x = insets.left + shortest * (colWidth + gap);
        int y = insets.top + colHeights[shortest];

        c.setBounds(x, y, colWidth, h);
        colHeights[shortest] += h + gap;
      }
    }

    private Dimension computeSize(Container parent) {
      Insets insets = parent.getInsets();
      int containerWidth = parent.getWidth();
      if (containerWidth <= 0) {
        containerWidth = 400; // 合理默认值
      }
      int available = containerWidth - insets.left - insets.right;
      int colWidth = (available - (columns - 1) * gap) / columns;
      if (colWidth <= 0) {
        colWidth = 1;
      }

      int[] colHeights = new int[columns];

      for (int i = 0; i < parent.getComponentCount(); i++) {
        Component c = parent.getComponent(i);
        if (!c.isVisible()) {
          continue;
        }

        int shortest = 0;
        for (int col = 1; col < columns; col++) {
          if (colHeights[col] < colHeights[shortest]) {
            shortest = col;
          }
        }

        Dimension pref = c.getPreferredSize();
        int h = pref.width > 0
            ? (int) ((long) pref.height * colWidth / pref.width)
            : pref.height;
        colHeights[shortest] += h + gap;
      }

      // 总高度 = 最高列
      int maxHeight = 0;
      for (int colH : colHeights) {
        maxHeight = Math.max(maxHeight, colH);
      }
      // 去掉最后一个 gap
      if (maxHeight > 0) {
        maxHeight -= gap;
      }

      return new Dimension(
          insets.left + insets.right,
          maxHeight + insets.top + insets.bottom);
    }
  }
}
