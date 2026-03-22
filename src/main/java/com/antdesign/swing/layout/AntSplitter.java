package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.util.AntColor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

/**
 * Ant Design 风格可拖拽分割面板。
 *
 * <p>将区域一分为二，支持水平和垂直两种方向，用户可拖拽分割条调整尺寸。
 * 内部基于 {@link JSplitPane} 封装，自动应用 Ant Design 分割线样式。
 *
 * <pre>{@code
 * AntSplitter splitter = new AntSplitter();
 * splitter.setFirstComponent(leftPanel);
 * splitter.setSecondComponent(rightPanel);
 * splitter.setDividerLocation(0.3);
 *
 * // 垂直分割
 * AntSplitter vSplit = new AntSplitter(AntSplitter.Orientation.VERTICAL);
 * vSplit.setFirstComponent(topPanel);
 * vSplit.setSecondComponent(bottomPanel);
 * }</pre>
 */
public class AntSplitter extends AntPanel {

    private static final long serialVersionUID = 1L;

    /**
     * 分割方向。
     */
    public enum Orientation {
        /**
         * 左右分割。
         */
        HORIZONTAL,
        /**
         * 上下分割。
         */
        VERTICAL
    }

    private static final int DEFAULT_DIVIDER_SIZE = 8;

    private final JSplitPane splitPane;

    /**
     * 创建默认水平分割面板。
     */
    public AntSplitter() {
        this(Orientation.HORIZONTAL);
    }

    /**
     * 创建指定方向的分割面板。
     *
     * @param orientation 分割方向
     */
    public AntSplitter(Orientation orientation) {
        super(new BorderLayout());

        int swingOrientation = orientation == Orientation.HORIZONTAL
                ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;
        splitPane = new JSplitPane(swingOrientation);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(DEFAULT_DIVIDER_SIZE);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        applyTheme();
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * 设置第一个组件（水平时为左侧，垂直时为上方）。
     *
     * @param comp 组件
     */
    public void setFirstComponent(Component comp) {
        splitPane.setLeftComponent(wrapIfNeeded(comp));
    }

    /**
     * 设置第二个组件（水平时为右侧，垂直时为下方）。
     *
     * @param comp 组件
     */
    public void setSecondComponent(Component comp) {
        splitPane.setRightComponent(wrapIfNeeded(comp));
    }

    /**
     * 按比例设置分割位置。
     *
     * @param proportion 比例值，0.0 ~ 1.0
     */
    public void setDividerLocation(double proportion) {
        splitPane.setResizeWeight(proportion);
        splitPane.setDividerLocation(proportion);
    }

    /**
     * 按像素设置分割位置。
     *
     * @param location 像素值
     */
    public void setDividerLocation(int location) {
        splitPane.setDividerLocation(location);
    }

    /**
     * 设置分割条宽度（像素）。
     */
    public void setDividerSize(int size) {
        splitPane.setDividerSize(size);
    }

    /**
     * 设置是否可一键收起/展开。
     */
    public void setOneTouchExpandable(boolean expandable) {
        splitPane.setOneTouchExpandable(expandable);
    }

    /**
     * 设置拖拽时的大小分配权重（0.0 ~ 1.0）。
     */
    public void setResizeWeight(double weight) {
        splitPane.setResizeWeight(weight);
    }

    /**
     * 设置分割方向。
     */
    public void setOrientation(Orientation orientation) {
        splitPane.setOrientation(orientation == Orientation.HORIZONTAL
                ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
    }

    /**
     * 获取内部 {@link JSplitPane} 以进行高级定制。
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    private static Component wrapIfNeeded(Component comp) {
        if (comp instanceof JPanel) {
            return comp;
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(comp, BorderLayout.CENTER);
        return wrapper;
    }

    private void applyTheme() {
        splitPane.setBackground(Color.WHITE);
        splitPane.setUI(new AntSplitPaneUI());
    }

    // ---------------------------------------------------------------------------
    // 自定义 UI
    // ---------------------------------------------------------------------------

    private static class AntSplitPaneUI extends BasicSplitPaneUI {

        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new AntSplitDivider(this);
        }
    }

    private static class AntSplitDivider extends BasicSplitPaneDivider {

        private static final long serialVersionUID = 1L;

        AntSplitDivider(BasicSplitPaneUI ui) {
            super(ui);
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(Cursor.getPredefinedCursor(
                    ui.getOrientation() == JSplitPane.HORIZONTAL_SPLIT
                            ? Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR));
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(AntColor.BG_CONTAINER);
            g.fillRect(0, 0, getWidth(), getHeight());

            // 中心绘制拖拽指示点
            g.setColor(AntColor.BORDER_SECONDARY);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int dotSize = 2;

            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                for (int i = -2; i <= 2; i++) {
                    g.fillRect(cx - dotSize / 2, cy + i * 4 - dotSize / 2,
                            dotSize, dotSize);
                }
            } else {
                for (int i = -2; i <= 2; i++) {
                    g.fillRect(cx + i * 4 - dotSize / 2, cy - dotSize / 2,
                            dotSize, dotSize);
                }
            }
        }

        @Override
        public Border getBorder() {
            return BorderFactory.createEmptyBorder();
        }

        @Override
        public Dimension getPreferredSize() {
            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                return new Dimension(getDividerSize(), 1);
            }
            return new Dimension(1, getDividerSize());
        }
    }
}
