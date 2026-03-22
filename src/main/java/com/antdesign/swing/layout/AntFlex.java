package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Ant Design 风格弹性布局容器。
 *
 * <p>模拟 CSS Flexbox 语义，支持主轴排列方向、内容分布、交叉轴对齐、
 * 换行以及间距控制。子组件可通过 {@code flex} 约束来按比例分配剩余空间。
 *
 * <pre>{@code
 * // 水平居中分布，间距 16px
 * AntFlex flex = new AntFlex();
 * flex.setJustify(AntFlex.Justify.CENTER);
 * flex.setGap(16);
 * flex.add(button1);
 * flex.add(button2);
 *
 * // 垂直方向，两端对齐
 * AntFlex vFlex = new AntFlex();
 * vFlex.setDirection(AntFlex.Direction.COLUMN);
 * vFlex.setJustify(AntFlex.Justify.SPACE_BETWEEN);
 * vFlex.add(top);
 * vFlex.add(bottom);
 *
 * // 子组件按比例伸缩
 * AntFlex row = new AntFlex();
 * row.add(sidebar, AntFlex.flex(1));
 * row.add(main, AntFlex.flex(3));
 * }</pre>
 */
public class AntFlex extends AntPanel {

    private static final long serialVersionUID = 1L;

    /**
     * 主轴方向。
     */
    public enum Direction {
        ROW, ROW_REVERSE, COLUMN, COLUMN_REVERSE
    }

    /**
     * 主轴内容分布方式。
     */
    public enum Justify {
        FLEX_START, CENTER, FLEX_END, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
    }

    /**
     * 交叉轴对齐方式。
     */
    public enum Align {
        FLEX_START, CENTER, FLEX_END, STRETCH
    }

    /**
     * 换行模式。
     */
    public enum Wrap {
        NOWRAP, WRAP, WRAP_REVERSE
    }

    private Direction direction = Direction.ROW;
    private Justify justify = Justify.FLEX_START;
    private Align align = Align.STRETCH;
    private Wrap wrap = Wrap.NOWRAP;
    private int gap;

    /**
     * 创建默认弹性布局（水平，无间距）。
     */
    public AntFlex() {
        this(0);
    }

    /**
     * 创建指定间距的弹性布局。
     *
     * @param gap 主轴和交叉轴间距（像素）
     */
    public AntFlex(int gap) {
        this.gap = gap;
        setOpaque(false);
        setLayout(new FlexLayout());
    }

    /**
     * 创建 flex 约束对象，用于 {@link #add(Component, Object)}。
     *
     * @param value flex 伸缩值（正整数）
     * @return 约束对象
     */
    public static Integer flex(int value) {
        return value;
    }

    // -- 属性设置 ----------------------------------------------------------------

    /**
     * 设置主轴方向。
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
        revalidate();
    }

    /**
     * 设置主轴内容分布方式。
     */
    public void setJustify(Justify justify) {
        this.justify = justify;
        revalidate();
    }

    /**
     * 设置交叉轴对齐方式。
     */
    public void setAlign(Align align) {
        this.align = align;
        revalidate();
    }

    /**
     * 设置换行模式。
     */
    public void setWrap(Wrap wrap) {
        this.wrap = wrap;
        revalidate();
    }

    /**
     * 设置间距（像素）。
     */
    public void setGap(int gap) {
        this.gap = gap;
        revalidate();
    }

    /**
     * 是否水平方向。
     */
    private boolean isRow() {
        return direction == Direction.ROW || direction == Direction.ROW_REVERSE;
    }

    /**
     * 是否反向。
     */
    private boolean isReverse() {
        return direction == Direction.ROW_REVERSE
                || direction == Direction.COLUMN_REVERSE;
    }

    // ---------------------------------------------------------------------------
    // FlexLayout
    // ---------------------------------------------------------------------------

    private class FlexLayout implements LayoutManager2 {

        private final Map<Component, Integer> flexMap = new HashMap<>();

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints instanceof Integer) {
                flexMap.put(comp, (Integer) constraints);
            }
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            flexMap.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return computeSize(parent, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return computeSize(parent, false);
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
            int availMain = isRow()
                    ? parent.getWidth() - insets.left - insets.right
                    : parent.getHeight() - insets.top - insets.bottom;
            int availCross = isRow()
                    ? parent.getHeight() - insets.top - insets.bottom
                    : parent.getWidth() - insets.left - insets.right;

            // 收集可见组件
            int n = parent.getComponentCount();
            int[] indices = new int[n];
            int count = 0;
            for (int i = 0; i < n; i++) {
                if (parent.getComponent(i).isVisible()) {
                    indices[count++] = i;
                }
            }
            if (count == 0) {
                return;
            }

            // 单行布局（NOWRAP）
            if (wrap == Wrap.NOWRAP) {
                layoutLine(parent, indices, 0, count, insets, availMain, availCross,
                        isRow() ? insets.top : insets.left);
                return;
            }

            // 多行布局：分行
            int crossOffset = isRow() ? insets.top : insets.left;
            int lineStart = 0;
            int lineMainSize = 0;
            int lineCrossSize = 0;

            for (int i = 0; i < count; i++) {
                Component c = parent.getComponent(indices[i]);
                Dimension pref = c.getPreferredSize();
                int mainSize = isRow() ? pref.width : pref.height;
                int crossSize = isRow() ? pref.height : pref.width;
                int needed = lineMainSize + mainSize + (i > lineStart ? gap : 0);

                if (i > lineStart && needed > availMain) {
                    layoutLine(parent, indices, lineStart, i, insets,
                            availMain, lineCrossSize, crossOffset);
                    crossOffset += lineCrossSize + gap;
                    lineStart = i;
                    lineMainSize = mainSize;
                    lineCrossSize = crossSize;
                } else {
                    lineMainSize = lineMainSize + mainSize + (i > lineStart ? gap : 0);
                    lineCrossSize = Math.max(lineCrossSize, crossSize);
                }
            }
            layoutLine(parent, indices, lineStart, count, insets,
                    availMain, lineCrossSize, crossOffset);
        }

        /**
         * 布局单行/单列中的组件。
         *
         * @param indices     可见组件索引数组
         * @param from        起始位置（含）
         * @param to          结束位置（不含）
         * @param availMain   主轴可用空间
         * @param lineCross   该行交叉轴尺寸
         * @param crossOffset 交叉轴起始偏移
         */
        private void layoutLine(Container parent, int[] indices,
                                int from, int to, Insets insets,
                                int availMain, int lineCross, int crossOffset) {
            int itemCount = to - from;
            if (itemCount == 0) {
                return;
            }

            // 计算自然主轴尺寸和 flex 总量
            int totalFixed = 0;
            int totalFlex = 0;
            for (int i = from; i < to; i++) {
                Component c = parent.getComponent(indices[i]);
                Dimension pref = c.getPreferredSize();
                int flexVal = getFlexValue(c);
                if (flexVal > 0) {
                    totalFlex += flexVal;
                } else {
                    totalFixed += isRow() ? pref.width : pref.height;
                }
            }

            int gapTotal = (itemCount - 1) * gap;
            int remaining = availMain - totalFixed - gapTotal;

            // 计算每个组件的主轴尺寸
            int[] mainSizes = new int[itemCount];
            for (int i = 0; i < itemCount; i++) {
                Component c = parent.getComponent(indices[from + i]);
                int flexVal = getFlexValue(c);
                if (flexVal > 0 && totalFlex > 0 && remaining > 0) {
                    mainSizes[i] = remaining * flexVal / totalFlex;
                } else {
                    Dimension pref = c.getPreferredSize();
                    mainSizes[i] = isRow() ? pref.width : pref.height;
                }
            }

            // 计算主轴内容总长度
            int contentMain = 0;
            for (int s : mainSizes) {
                contentMain += s;
            }
            contentMain += gapTotal;
            int extraSpace = availMain - contentMain;

            // 根据 justify 计算起始位置和间隔
            int mainOffset;
            int extraGap = 0;
            switch (justify) {
                case CENTER:
                    mainOffset = extraSpace / 2;
                    break;
                case FLEX_END:
                    mainOffset = extraSpace;
                    break;
                case SPACE_BETWEEN:
                    mainOffset = 0;
                    extraGap = itemCount > 1 ? extraSpace / (itemCount - 1) : 0;
                    break;
                case SPACE_AROUND:
                    int padding = itemCount > 0 ? extraSpace / (itemCount * 2) : 0;
                    mainOffset = padding;
                    extraGap = padding * 2;
                    break;
                case SPACE_EVENLY:
                    int slot = itemCount > 0 ? extraSpace / (itemCount + 1) : 0;
                    mainOffset = slot;
                    extraGap = slot;
                    break;
                default: // FLEX_START
                    mainOffset = 0;
                    break;
            }
            mainOffset += isRow() ? insets.left : insets.top;

            // 反向排列
            int[] order = new int[itemCount];
            for (int i = 0; i < itemCount; i++) {
                order[i] = isReverse() ? (itemCount - 1 - i) : i;
            }

            // 定位每个组件
            int pos = mainOffset;
            for (int i = 0; i < itemCount; i++) {
                int idx = order[i];
                Component c = parent.getComponent(indices[from + idx]);
                int mainSize = mainSizes[idx];
                Dimension pref = c.getPreferredSize();
                int crossSize = isRow() ? pref.height : pref.width;

                // 交叉轴位置
                int crossPos;
                switch (align) {
                    case CENTER:
                        crossPos = crossOffset + (lineCross - crossSize) / 2;
                        break;
                    case FLEX_END:
                        crossPos = crossOffset + lineCross - crossSize;
                        break;
                    case STRETCH:
                        crossPos = crossOffset;
                        crossSize = lineCross;
                        break;
                    default: // FLEX_START
                        crossPos = crossOffset;
                        break;
                }

                if (isRow()) {
                    c.setBounds(pos, crossPos, mainSize, crossSize);
                } else {
                    c.setBounds(crossPos, pos, crossSize, mainSize);
                }
                pos += mainSize + gap + extraGap;
            }
        }

        private int getFlexValue(Component c) {
            Integer flex = flexMap.get(c);
            return flex != null ? flex : 0;
        }

        private Dimension computeSize(Container parent, boolean preferred) {
            Insets insets = parent.getInsets();
            int mainTotal = 0;
            int crossMax = 0;
            int count = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                int main = isRow() ? d.width : d.height;
                int cross = isRow() ? d.height : d.width;
                mainTotal += main;
                crossMax = Math.max(crossMax, cross);
                count++;
            }

            if (count > 1) {
                mainTotal += (count - 1) * gap;
            }

            int w, h;
            if (isRow()) {
                w = mainTotal;
                h = crossMax;
            } else {
                w = crossMax;
                h = mainTotal;
            }

            return new Dimension(
                    w + insets.left + insets.right,
                    h + insets.top + insets.bottom);
        }
    }
}
