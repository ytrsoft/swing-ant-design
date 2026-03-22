package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.model.ComponentSize;

import java.awt.*;

/**
 * Ant Design 风格间距容器。
 *
 * <p>自动为子组件之间添加统一间距，支持水平和垂直方向，
 * 水平模式下可启用自动换行。
 *
 * <pre>{@code
 * // 水平间距（默认）
 * AntSpace hSpace = new AntSpace();
 * hSpace.add(button1);
 * hSpace.add(button2);
 * hSpace.add(button3);
 *
 * // 垂直间距，大号间距
 * AntSpace vSpace = new AntSpace(AntSpace.Direction.VERTICAL, ComponentSize.LARGE);
 * vSpace.add(panel1);
 * vSpace.add(panel2);
 *
 * // 自定义间距并启用换行
 * AntSpace wrap = new AntSpace(AntSpace.Direction.HORIZONTAL, 12);
 * wrap.setWrap(true);
 * }</pre>
 */
public class AntSpace extends AntPanel {

    private static final long serialVersionUID = 1L;

    /**
     * 排列方向。
     */
    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    /**
     * 交叉轴对齐方式。
     */
    public enum Align {
        START, CENTER, END
    }

    private static final int GAP_SMALL = 8;
    private static final int GAP_MIDDLE = 16;
    private static final int GAP_LARGE = 24;

    private Direction direction;
    private Align align;
    private int gap;
    private boolean wrap;

    /**
     * 创建水平方向、中号间距的 Space。
     */
    public AntSpace() {
        this(Direction.HORIZONTAL, GAP_MIDDLE);
    }

    /**
     * 创建指定方向的 Space（中号间距）。
     *
     * @param direction 排列方向
     */
    public AntSpace(Direction direction) {
        this(direction, GAP_MIDDLE);
    }

    /**
     * 创建指定方向和预设尺寸间距的 Space。
     *
     * @param direction 排列方向
     * @param size      预设尺寸
     */
    public AntSpace(Direction direction, ComponentSize size) {
        this(direction, sizeToGap(size));
    }

    /**
     * 创建指定方向和自定义间距的 Space。
     *
     * @param direction 排列方向
     * @param gap       间距（像素）
     */
    public AntSpace(Direction direction, int gap) {
        this.direction = direction;
        this.gap = gap;
        this.align = Align.CENTER;
        this.wrap = false;
        setOpaque(false);
        setLayout(new SpaceLayout());
    }

    /**
     * 设置排列方向。
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
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
     * 以预设尺寸设置间距。
     */
    public void setGap(ComponentSize size) {
        this.gap = sizeToGap(size);
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
     * 设置水平方向下是否允许换行。
     */
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
        revalidate();
    }

    private static int sizeToGap(ComponentSize size) {
        switch (size) {
            case SMALL:
                return GAP_SMALL;
            case LARGE:
                return GAP_LARGE;
            default:
                return GAP_MIDDLE;
        }
    }

    // ---------------------------------------------------------------------------
    // SpaceLayout
    // ---------------------------------------------------------------------------

    private class SpaceLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
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
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int maxWidth = parent.getWidth() - insets.left - insets.right;

            if (direction == Direction.HORIZONTAL) {
                layoutHorizontal(parent, insets, maxWidth);
            } else {
                layoutVertical(parent, insets, maxWidth);
            }
        }

        private void layoutHorizontal(Container parent, Insets insets,
                                      int maxWidth) {
            int x = insets.left;
            int y = insets.top;
            int rowHeight = 0;
            boolean firstInRow = true;
            int lineStart = 0;

            // 第一遍：布局位置并记录行高
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                Dimension pref = c.getPreferredSize();

                if (wrap && !firstInRow
                        && x + pref.width > insets.left + maxWidth) {
                    alignRow(parent, lineStart, i, y, rowHeight);
                    x = insets.left;
                    y += rowHeight + gap;
                    rowHeight = 0;
                    firstInRow = true;
                    lineStart = i;
                }

                c.setBounds(x, y, pref.width, pref.height);
                x += pref.width + gap;
                rowHeight = Math.max(rowHeight, pref.height);
                firstInRow = false;
            }
            alignRow(parent, lineStart, parent.getComponentCount(), y, rowHeight);
        }

        private void alignRow(Container parent, int from, int to,
                              int rowY, int rowHeight) {
            for (int i = from; i < to; i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                int cy;
                switch (align) {
                    case START:
                        cy = rowY;
                        break;
                    case END:
                        cy = rowY + rowHeight - c.getHeight();
                        break;
                    default:
                        cy = rowY + (rowHeight - c.getHeight()) / 2;
                        break;
                }
                c.setLocation(c.getX(), cy);
            }
        }

        private void layoutVertical(Container parent, Insets insets,
                                    int maxWidth) {
            int y = insets.top;
            boolean first = true;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                Dimension pref = c.getPreferredSize();
                if (!first) {
                    y += gap;
                }

                int cx;
                switch (align) {
                    case START:
                        cx = insets.left;
                        break;
                    case END:
                        cx = insets.left + maxWidth - pref.width;
                        break;
                    default:
                        cx = insets.left + (maxWidth - pref.width) / 2;
                        break;
                }
                c.setBounds(cx, y, pref.width, pref.height);
                y += pref.height;
                first = false;
            }
        }

        private Dimension computeSize(Container parent, boolean preferred) {
            Insets insets = parent.getInsets();
            int w = 0;
            int h = 0;
            int count = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                if (direction == Direction.HORIZONTAL) {
                    w += d.width;
                    h = Math.max(h, d.height);
                } else {
                    w = Math.max(w, d.width);
                    h += d.height;
                }
                count++;
            }

            if (count > 1) {
                int totalGap = (count - 1) * gap;
                if (direction == Direction.HORIZONTAL) {
                    w += totalGap;
                } else {
                    h += totalGap;
                }
            }

            return new Dimension(
                    w + insets.left + insets.right,
                    h + insets.top + insets.bottom);
        }
    }
}
