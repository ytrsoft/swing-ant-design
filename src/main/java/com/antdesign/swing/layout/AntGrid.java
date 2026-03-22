package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 风格 24 列栅格布局。
 *
 * <p>模拟 Ant Design 的 Row/Col 栅格系统，将容器水平等分为 24 列，
 * 子组件通过 {@code span}（占用列数）和 {@code offset}（左偏移列数）定位。
 * 当一行总列数超过 24 时自动换行。
 *
 * <pre>{@code
 * AntGrid grid = new AntGrid(16);  // gutter = 16px
 * grid.addColumn(panel1, 8);       // 占 8 列
 * grid.addColumn(panel2, 16);      // 占 16 列
 *
 * // 带偏移
 * grid.addColumn(panel3, 6, 6);    // offset=6, span=6
 *
 * // 垂直间距
 * grid.setVerticalGutter(16);
 * }</pre>
 */
public class AntGrid extends AntPanel {

    private static final long serialVersionUID = 1L;

    /**
     * 栅格总列数。
     */
    public static final int TOTAL_COLUMNS = 24;

    /**
     * 交叉轴对齐方式。
     */
    public enum Align {
        TOP, MIDDLE, BOTTOM
    }

    private int gutter;
    private int verticalGutter;
    private Align align = Align.TOP;

    /**
     * 创建无间距的栅格容器。
     */
    public AntGrid() {
        this(0);
    }

    /**
     * 创建指定水平间距的栅格容器。
     *
     * @param gutter 列间距（像素）
     */
    public AntGrid(int gutter) {
        this.gutter = gutter;
        setOpaque(false);
        setLayout(new GridLayout());
    }

    /**
     * 添加一列，占满 24 栅格。
     *
     * @param comp 组件
     */
    public void addColumn(Component comp) {
        addColumn(comp, TOTAL_COLUMNS, 0);
    }

    /**
     * 添加一列。
     *
     * @param comp 组件
     * @param span 占用列数（1 ~ 24）
     */
    public void addColumn(Component comp, int span) {
        addColumn(comp, span, 0);
    }

    /**
     * 添加一列（带偏移）。
     *
     * @param comp   组件
     * @param span   占用列数（1 ~ 24）
     * @param offset 左偏移列数
     */
    public void addColumn(Component comp, int span, int offset) {
        add(comp, new ColConstraint(span, offset));
    }

    /**
     * 设置水平列间距（像素）。
     */
    public void setGutter(int gutter) {
        this.gutter = gutter;
        revalidate();
    }

    /**
     * 设置垂直行间距（像素）。
     */
    public void setVerticalGutter(int verticalGutter) {
        this.verticalGutter = verticalGutter;
        revalidate();
    }

    /**
     * 设置行内交叉轴对齐方式。
     */
    public void setAlign(Align align) {
        this.align = align;
        revalidate();
    }

    // ===========================================================================
    // ColConstraint
    // ===========================================================================

    /**
     * 列约束。
     */
    public static class ColConstraint {

        final int span;
        final int offset;

        /**
         * 创建列约束。
         *
         * @param span   占用列数
         * @param offset 左偏移列数
         */
        public ColConstraint(int span, int offset) {
            this.span = Math.max(1, Math.min(span, TOTAL_COLUMNS));
            this.offset = Math.max(0, offset);
        }
    }

    // ===========================================================================
    // GridLayout (LayoutManager2)
    // ===========================================================================

    private class GridLayout implements LayoutManager2 {

        private final java.util.Map<Component, ColConstraint> constraints
                = new java.util.HashMap<>();

        @Override
        public void addLayoutComponent(Component comp, Object constraint) {
            if (constraint instanceof ColConstraint) {
                constraints.put(comp, (ColConstraint) constraint);
            } else {
                constraints.put(comp, new ColConstraint(TOTAL_COLUMNS, 0));
            }
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            constraints.put(comp, new ColConstraint(TOTAL_COLUMNS, 0));
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            constraints.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return computeSize(parent);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return computeSize(parent);
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0.5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0.5f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int containerWidth = parent.getWidth() - insets.left - insets.right;

            // 栅格单位计算
            // 24 columns with 23 gutters:
            //   colWidth = (containerWidth - 23 * gutter) / 24
            //   step     = colWidth + gutter = (containerWidth + gutter) / 24
            double step = (containerWidth + gutter) / (double) TOTAL_COLUMNS;
            double colWidth = step - gutter;

            // 按行分组
            List<List<Integer>> rows = buildRows(parent);

            int y = insets.top;
            for (int r = 0; r < rows.size(); r++) {
                List<Integer> row = rows.get(r);
                if (r > 0) {
                    y += verticalGutter;
                }

                // 计算行高
                int rowHeight = 0;
                for (int idx : row) {
                    Component c = parent.getComponent(idx);
                    rowHeight = Math.max(rowHeight, c.getPreferredSize().height);
                }

                // 布局每个组件
                int gridCol = 0;
                for (int idx : row) {
                    Component c = parent.getComponent(idx);
                    ColConstraint cc = getConstraint(c);
                    gridCol += cc.offset;

                    int x = insets.left + (int) (gridCol * step);
                    int w = (int) (cc.span * colWidth + (cc.span - 1) * gutter);
                    int h = c.getPreferredSize().height;

                    int cy;
                    switch (align) {
                        case MIDDLE:
                            cy = y + (rowHeight - h) / 2;
                            break;
                        case BOTTOM:
                            cy = y + rowHeight - h;
                            break;
                        default:
                            cy = y;
                            break;
                    }

                    c.setBounds(x, cy, w, h);
                    gridCol += cc.span;
                }
                y += rowHeight;
            }
        }

        /**
         * 将可见组件按 24 列规则分组为多行。
         */
        private List<List<Integer>> buildRows(Container parent) {
            List<List<Integer>> rows = new ArrayList<>();
            List<Integer> currentRow = new ArrayList<>();
            int currentSpan = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                ColConstraint cc = getConstraint(c);
                int needed = cc.offset + cc.span;

                if (!currentRow.isEmpty() && currentSpan + needed > TOTAL_COLUMNS) {
                    rows.add(currentRow);
                    currentRow = new ArrayList<>();
                    currentSpan = 0;
                }
                currentRow.add(i);
                currentSpan += needed;
            }

            if (!currentRow.isEmpty()) {
                rows.add(currentRow);
            }
            return rows;
        }

        private ColConstraint getConstraint(Component c) {
            ColConstraint cc = constraints.get(c);
            return cc != null ? cc : new ColConstraint(TOTAL_COLUMNS, 0);
        }

        private Dimension computeSize(Container parent) {
            Insets insets = parent.getInsets();
            List<List<Integer>> rows = buildRows(parent);
            int totalHeight = 0;

            for (int r = 0; r < rows.size(); r++) {
                if (r > 0) {
                    totalHeight += verticalGutter;
                }
                int rowHeight = 0;
                for (int idx : rows.get(r)) {
                    Component c = parent.getComponent(idx);
                    rowHeight = Math.max(rowHeight, c.getPreferredSize().height);
                }
                totalHeight += rowHeight;
            }

            return new Dimension(
                    insets.left + insets.right,
                    totalHeight + insets.top + insets.bottom);
        }
    }
}
