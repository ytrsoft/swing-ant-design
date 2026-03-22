package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.util.AntColor;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 风格页面布局容器。
 *
 * <p>提供经典的上—中—下、左—右页面布局结构，内含
 * {@link Header}、{@link Sider}、{@link Content} 和 {@link Footer}
 * 四种区域组件，支持嵌套以构建复杂布局。
 *
 * <pre>{@code
 * AntLayout layout = new AntLayout();
 *
 * AntLayout.Header header = new AntLayout.Header();
 * header.add(new JLabel("Logo"));
 *
 * AntLayout.Sider sider = new AntLayout.Sider(200);
 * sider.add(menuPanel);
 *
 * AntLayout.Content content = new AntLayout.Content();
 * content.add(mainPanel);
 *
 * AntLayout.Footer footer = new AntLayout.Footer();
 * footer.add(new JLabel("© 2024"));
 *
 * layout.setHeader(header);
 * layout.setSider(sider);
 * layout.setContent(content);
 * layout.setFooter(footer);
 * }</pre>
 */
public class AntLayout extends AntPanel {

    private static final long serialVersionUID = 1L;

    private Header header;
    private Footer footer;
    private Content content;
    private Sider sider;

    /**
     * 创建空白布局容器。
     */
    public AntLayout() {
        super(new BorderLayout());
        setBackground(AntColor.BG_LAYOUT);
    }

    /**
     * 设置头部区域。
     */
    public void setHeader(Header header) {
        if (this.header != null) {
            remove(this.header);
        }
        this.header = header;
        if (header != null) {
            add(header, BorderLayout.NORTH);
        }
        revalidate();
        repaint();
    }

    /**
     * 获取头部区域。
     */
    public Header getHeader() {
        return header;
    }

    /**
     * 设置底部区域。
     */
    public void setFooter(Footer footer) {
        if (this.footer != null) {
            remove(this.footer);
        }
        this.footer = footer;
        if (footer != null) {
            add(footer, BorderLayout.SOUTH);
        }
        revalidate();
        repaint();
    }

    /**
     * 获取底部区域。
     */
    public Footer getFooter() {
        return footer;
    }

    /**
     * 设置主内容区域。
     */
    public void setContent(Content content) {
        if (this.content != null) {
            remove(this.content);
        }
        this.content = content;
        if (content != null) {
            add(content, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    /**
     * 获取主内容区域。
     */
    public Content getContent() {
        return content;
    }

    /**
     * 设置侧边栏。
     */
    public void setSider(Sider sider) {
        if (this.sider != null) {
            remove(this.sider);
        }
        this.sider = sider;
        if (sider != null) {
            String pos = sider.isPlacedRight() ? BorderLayout.EAST : BorderLayout.WEST;
            add(sider, pos);
        }
        revalidate();
        repaint();
    }

    /**
     * 获取侧边栏。
     */
    public Sider getSider() {
        return sider;
    }

    // ===========================================================================
    // 内部区域组件
    // ===========================================================================

    /**
     * 头部区域，默认高度 64px，深色背景。
     *
     * <p>通常放置 Logo 和顶部导航。
     */
    public static class Header extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int DEFAULT_HEIGHT = 64;

        public Header() {
            super(new BorderLayout());
            setBackground(new Color(0x001529));
            setPreferredSize(new Dimension(0, DEFAULT_HEIGHT));
        }

        /**
         * 设置头部高度。
         */
        public void setHeight(int height) {
            setPreferredSize(new Dimension(0, height));
            revalidate();
        }
    }

    /**
     * 底部区域，默认高度 64px。
     *
     * <p>通常放置版权信息和链接。
     */
    public static class Footer extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int DEFAULT_HEIGHT = 64;

        public Footer() {
            super(new BorderLayout());
            setBackground(AntColor.BG_LAYOUT);
            setPreferredSize(new Dimension(0, DEFAULT_HEIGHT));
        }

        /**
         * 设置底部高度。
         */
        public void setHeight(int height) {
            setPreferredSize(new Dimension(0, height));
            revalidate();
        }
    }

    /**
     * 主内容区域，白色背景。
     *
     * <p>放置页面主体内容，会自动填满剩余空间。
     */
    public static class Content extends JPanel {

        private static final long serialVersionUID = 1L;

        public Content() {
            super(new BorderLayout());
            setBackground(Color.WHITE);
        }
    }

    /**
     * 侧边栏区域，默认宽度 200px。
     *
     * <p>通常放置侧边导航菜单。支持折叠/展开，可放置在左侧或右侧。
     */
    public static class Sider extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int DEFAULT_WIDTH = 200;
        private static final int DEFAULT_COLLAPSED_WIDTH = 48;

        private int expandedWidth;
        private int collapsedWidth;
        private boolean collapsed;
        private boolean collapsible;
        private boolean placedRight;

        public Sider() {
            this(DEFAULT_WIDTH);
        }

        /**
         * 创建指定宽度的侧边栏。
         *
         * @param width 展开宽度（像素）
         */
        public Sider(int width) {
            super(new BorderLayout());
            this.expandedWidth = width;
            this.collapsedWidth = DEFAULT_COLLAPSED_WIDTH;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(width, 0));
        }

        /**
         * 设置侧边栏是否放置在右侧。
         */
        public void setPlacedRight(boolean right) {
            this.placedRight = right;
        }

        /**
         * 侧边栏是否放置在右侧。
         */
        public boolean isPlacedRight() {
            return placedRight;
        }

        /**
         * 设置是否可折叠。
         */
        public void setCollapsible(boolean collapsible) {
            this.collapsible = collapsible;
        }

        /**
         * 是否可折叠。
         */
        public boolean isCollapsible() {
            return collapsible;
        }

        /**
         * 设置折叠状态。不可折叠时调用无效。
         */
        public void setCollapsed(boolean collapsed) {
            if (!collapsible) {
                return;
            }
            this.collapsed = collapsed;
            int width = collapsed ? collapsedWidth : expandedWidth;
            setPreferredSize(new Dimension(width, 0));
            revalidate();
            repaint();
        }

        /**
         * 是否处于折叠状态。
         */
        public boolean isCollapsed() {
            return collapsed;
        }

        /**
         * 切换折叠/展开状态。
         */
        public void toggleCollapsed() {
            setCollapsed(!collapsed);
        }

        /**
         * 设置展开宽度。
         */
        public void setExpandedWidth(int width) {
            this.expandedWidth = width;
            if (!collapsed) {
                setPreferredSize(new Dimension(width, 0));
                revalidate();
            }
        }

        /**
         * 设置折叠后的宽度。
         */
        public void setCollapsedWidth(int width) {
            this.collapsedWidth = width;
            if (collapsed) {
                setPreferredSize(new Dimension(width, 0));
                revalidate();
            }
        }
    }
}
