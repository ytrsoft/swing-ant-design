package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.ColorUtils;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ant Design 风格滚动面板。
 *
 * <p>对应 Ant Design 整体设计语言中的滚动容器，提供主题化的滚动条样式
 * （迷你轨道、圆角滑块、悬停高亮）、可选边框和圆角、内边距控制等功能。
 *
 * <p>可用来包裹任何子组件，替代原生 {@link JScrollPane} 以获得
 * Ant Design 一致的视觉效果。
 *
 * <pre>{@code
 * // 基本用法 — 包裹一个面板
 * AntScrollPanel scroll = new AntScrollPanel(contentPanel);
 *
 * // 带边框 + 圆角
 * AntScrollPanel bordered = new AntScrollPanel(listPanel);
 * bordered.setBordered(true);
 *
 * // 设置滚动策略
 * AntScrollPanel horz = new AntScrollPanel(widePanel);
 * horz.setHorizontalPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
 *
 * // 迷你滚动条模式（更细的滑块）
 * AntScrollPanel mini = new AntScrollPanel(contentPanel);
 * mini.setMiniThumb(true);
 *
 * // 控制内边距
 * AntScrollPanel padded = new AntScrollPanel(contentPanel);
 * padded.setContentPadding(new Insets(16, 16, 16, 16));
 * }</pre>
 *
 * @see javax.swing.JScrollPane
 */
public class AntScrollPanel extends AbstractAntPanel {

    private static final long serialVersionUID = 1L;

    /** 默认滚动条宽度 (px)。 */
    private static final int SCROLLBAR_WIDTH = 10;

    /** 迷你模式滚动条宽度 (px)。 */
    private static final int SCROLLBAR_WIDTH_MINI = 6;

    /** 滚动条圆角 (px)。 */
    private static final int THUMB_ARC = 10;

    // =========================================================================
    // 字段
    // =========================================================================

    @Getter private boolean bordered;
    @Getter private boolean miniThumb;
    @Getter private Insets contentPadding;
    @Getter private int scrollUnitIncrement;

    private final JScrollPane scrollPane;
    private Component viewComponent;

    // =========================================================================
    // 构造方法
    // =========================================================================

    /** 创建空滚动面板。 */
    public AntScrollPanel() {
        this(null);
    }

    /**
     * 创建包裹子组件的滚动面板。
     *
     * @param view 要滚动的子组件
     */
    public AntScrollPanel(Component view) {
        super(new BorderLayout());
        this.viewComponent = view;
        this.scrollUnitIncrement = 16;

        scrollPane = new JScrollPane(view);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(scrollUnitIncrement);

        add(scrollPane, BorderLayout.CENTER);

        // 安装自定义滚动条 UI
        installScrollBarUI(scrollPane.getVerticalScrollBar());
        installScrollBarUI(scrollPane.getHorizontalScrollBar());

        setThemeListener(theme -> applyTheme());
    }

    // =========================================================================
    // 视图操作
    // =========================================================================

    /**
     * 设置被滚动的子组件。
     *
     * @param view 新的子组件
     */
    public void setViewComponent(Component view) {
        this.viewComponent = view;
        scrollPane.setViewportView(view);
        revalidate();
        repaint();
    }

    /**
     * 获取被滚动的子组件。
     */
    public Component getViewComponent() {
        return viewComponent;
    }

    /**
     * 获取内部 JScrollPane。
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * 获取视口组件。
     */
    public JViewport getViewport() {
        return scrollPane.getViewport();
    }

    // =========================================================================
    // Setter
    // =========================================================================

    /**
     * 设置是否显示边框。
     *
     * @param bordered true 显示 Ant Design 风格边框
     */
    public void setBordered(boolean bordered) {
        updateAndRepaint(() -> this.bordered = bordered);
        applyTheme();
    }

    /**
     * 设置迷你滚动条模式。
     *
     * <p>迷你模式下滚动条更细（6px），适用于紧凑布局。
     *
     * @param miniThumb true 启用迷你模式
     */
    public void setMiniThumb(boolean miniThumb) {
        this.miniThumb = miniThumb;
        installScrollBarUI(scrollPane.getVerticalScrollBar());
        installScrollBarUI(scrollPane.getHorizontalScrollBar());
        revalidate();
        repaint();
    }

    /**
     * 设置内容区域内边距。
     *
     * @param padding 内边距
     */
    public void setContentPadding(Insets padding) {
        this.contentPadding = padding;
        if (padding != null) {
            scrollPane.setBorder(BorderFactory.createEmptyBorder(
                    padding.top, padding.left, padding.bottom, padding.right));
        } else {
            scrollPane.setBorder(null);
        }
        revalidate();
    }

    /**
     * 设置滚动单位增量（鼠标滚轮每次滚动像素数）。
     *
     * @param increment 像素数
     */
    public void setScrollUnitIncrement(int increment) {
        this.scrollUnitIncrement = Math.max(1, increment);
        scrollPane.getVerticalScrollBar().setUnitIncrement(this.scrollUnitIncrement);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(this.scrollUnitIncrement);
    }

    /**
     * 设置垂直滚动条策略。
     *
     * @param policy 滚动策略常量，如 {@link ScrollPaneConstants#VERTICAL_SCROLLBAR_AS_NEEDED}
     */
    public void setVerticalPolicy(int policy) {
        scrollPane.setVerticalScrollBarPolicy(policy);
    }

    /**
     * 设置水平滚动条策略。
     *
     * @param policy 滚动策略常量
     */
    public void setHorizontalPolicy(int policy) {
        scrollPane.setHorizontalScrollBarPolicy(policy);
    }

    /**
     * 滚动到顶部。
     */
    public void scrollToTop() {
        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(0));
    }

    /**
     * 滚动到底部。
     */
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vBar = scrollPane.getVerticalScrollBar();
            vBar.setValue(vBar.getMaximum());
        });
    }

    /**
     * 确保指定组件在视口内可见。
     *
     * @param comp 目标组件
     */
    public void scrollToVisible(Component comp) {
        if (comp != null) {
            SwingUtilities.invokeLater(() ->
                    scrollPane.getViewport().scrollRectToVisible(comp.getBounds()));
        }
    }

    // =========================================================================
    // 绘制
    // =========================================================================

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
        if (!bordered) return;

        ColorToken ct = colorToken();
        SizeToken st = sizeToken();
        int arc = st.getBorderRadius();

        // 绘制圆角边框
        GraphicsUtils.setupAntialiasing(g2);
        g2.setColor(ct.getBorderColor());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(
                0.5f, 0.5f, width - 1f, height - 1f, arc, arc));
    }

    // =========================================================================
    // 主题
    // =========================================================================

    private void applyTheme() {
        ColorToken ct = colorToken();
        SizeToken st = sizeToken();

        setBackground(ct.getBgContainer());
        scrollPane.getViewport().setBackground(ct.getBgContainer());

        if (bordered) {
            int arc = st.getBorderRadius();
            // 用空 border 留出绘制圆角边框的空间
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        } else {
            setBorder(null);
        }

        // 重新安装滚动条 UI 以应用新的主题色
        installScrollBarUI(scrollPane.getVerticalScrollBar());
        installScrollBarUI(scrollPane.getHorizontalScrollBar());

        repaint();
    }

    // =========================================================================
    // 自定义滚动条 UI
    // =========================================================================

    private void installScrollBarUI(JScrollBar scrollBar) {
        if (scrollBar == null) return;
        scrollBar.setUI(new AntScrollBarUI());
        int barWidth = miniThumb ? SCROLLBAR_WIDTH_MINI : SCROLLBAR_WIDTH;
        scrollBar.setPreferredSize(
                scrollBar.getOrientation() == Adjustable.VERTICAL
                        ? new Dimension(barWidth, 0)
                        : new Dimension(0, barWidth));
        scrollBar.setOpaque(false);
    }

    /**
     * Ant Design 风格滚动条 UI。
     *
     * <p>特性：
     * <ul>
     *   <li>透明轨道</li>
     *   <li>圆角滑块</li>
     *   <li>悬停高亮</li>
     *   <li>无箭头按钮</li>
     * </ul>
     */
    private class AntScrollBarUI extends BasicScrollBarUI {

        private boolean thumbHovered;
        private boolean thumbDragging;

        @Override
        protected void installListeners() {
            super.installListeners();
            scrollbar.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    checkHover(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    thumbHovered = false;
                    scrollbar.repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (thumbRect.contains(e.getPoint())) {
                        thumbDragging = true;
                        scrollbar.repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    thumbDragging = false;
                    checkHover(e);
                    scrollbar.repaint();
                }
            });
            scrollbar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    checkHover(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // 拖拽期间不做额外处理
                }
            });
        }

        private void checkHover(MouseEvent e) {
            boolean wasHovered = thumbHovered;
            thumbHovered = thumbRect.contains(e.getPoint());
            if (wasHovered != thumbHovered) {
                scrollbar.repaint();
            }
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // 透明轨道 — 不绘制
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                GraphicsUtils.setupAntialiasing(g2);

                ColorToken ct = colorToken();
                Color thumbColor;

                if (thumbDragging) {
                    thumbColor = ct.getTextTertiaryColor();
                } else if (thumbHovered) {
                    thumbColor = ct.getTextQuaternaryColor();
                } else {
                    thumbColor = ColorUtils.withAlpha(ct.getFillColor(), 0.4f);
                }

                g2.setColor(thumbColor);

                int inset = miniThumb ? 1 : 2;
                int x = thumbBounds.x + inset;
                int y = thumbBounds.y + inset;
                int w = thumbBounds.width - inset * 2;
                int h = thumbBounds.height - inset * 2;

                g2.fillRoundRect(x, y, w, h, THUMB_ARC, THUMB_ARC);
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        /** 创建不可见的占位按钮（隐藏箭头）。 */
        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            btn.setFocusable(false);
            btn.setBorder(null);
            return btn;
        }
    }
}
