package com.antdesign.swing.layout;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.util.AntColor;
import com.antdesign.swing.util.AntFont;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 风格分割线。
 *
 * <p>支持水平和垂直方向，水平模式下可附加文字标题并控制对齐位置，
 * 同时支持虚线样式。
 *
 * <pre>{@code
 * // 简单水平分割线
 * AntDivider divider = new AntDivider();
 *
 * // 带标题的分割线
 * AntDivider titled = new AntDivider("设置");
 * titled.setTextPosition(AntDivider.TextPosition.LEFT);
 *
 * // 虚线分割线
 * AntDivider dashed = new AntDivider();
 * dashed.setDashed(true);
 *
 * // 垂直分割线（用于行内元素间）
 * AntDivider vertical = new AntDivider(AntDivider.Orientation.VERTICAL);
 * }</pre>
 */
public class AntDivider extends AbstractAntComponent {

    private static final long serialVersionUID = 1L;

    /**
     * 分割线方向。
     */
    public enum Orientation {
        /**
         * 水平分割线。
         */
        HORIZONTAL,
        /**
         * 垂直分割线。
         */
        VERTICAL
    }

    /**
     * 文字在分割线上的位置（仅水平方向有效）。
     */
    public enum TextPosition {
        LEFT, CENTER, RIGHT
    }

    private static final int DEFAULT_MARGIN = 24;
    private static final int TEXT_SIDE_PADDING = 16;

    private Orientation orientation;
    /**
     * -- GETTER --
     * 获取分割线文字。
     */
    @Getter
    private String text;
    private TextPosition textPosition;
    private boolean dashed;
    private Color lineColor;

    /**
     * 创建默认水平分割线。
     */
    public AntDivider() {
        this(Orientation.HORIZONTAL);
    }

    /**
     * 创建带文字的水平分割线。
     *
     * @param text 分割线上的文字
     */
    public AntDivider(String text) {
        this(Orientation.HORIZONTAL);
        this.text = text;
    }

    /**
     * 创建指定方向的分割线。
     *
     * @param orientation 分割线方向
     */
    public AntDivider(Orientation orientation) {
        this.orientation = orientation;
        this.textPosition = TextPosition.CENTER;
        this.lineColor = AntColor.BORDER_SECONDARY;
    }

    /**
     * 设置分割线文字（仅水平方向有效）。
     */
    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    /**
     * 设置文字位置。
     */
    public void setTextPosition(TextPosition position) {
        this.textPosition = position;
        repaint();
    }

    /**
     * 设置是否使用虚线样式。
     */
    public void setDashed(boolean dashed) {
        this.dashed = dashed;
        repaint();
    }

    /**
     * 设置线条颜色。
     */
    public void setLineColor(Color color) {
        this.lineColor = color;
        repaint();
    }

    /**
     * 设置分割线方向。
     */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        revalidate();
        repaint();
    }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
        GraphicsUtils.setupAntialiasing(g2);
        if (orientation == Orientation.HORIZONTAL) {
            paintHorizontal(g2);
        } else {
            paintVertical(g2);
        }
    }

    private void paintHorizontal(Graphics2D g2) {
        int w = getWidth();
        int y = getHeight() / 2;
        Stroke stroke = createStroke();
        g2.setStroke(stroke);
        g2.setColor(lineColor);

        if (text == null || text.isEmpty()) {
            g2.drawLine(0, y, w, y);
            return;
        }

        Font font = AntFont.bold(AntFont.FONT_SIZE);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        int textX;
        switch (textPosition) {
            case LEFT:
                textX = DEFAULT_MARGIN;
                break;
            case RIGHT:
                textX = w - DEFAULT_MARGIN - textWidth;
                break;
            default:
                textX = (w - textWidth) / 2;
                break;
        }

        // 左侧线段
        int leftEnd = textX - TEXT_SIDE_PADDING;
        if (leftEnd > 0) {
            g2.setColor(lineColor);
            g2.setStroke(stroke);
            g2.drawLine(0, y, leftEnd, y);
        }

        // 标题文字
        g2.setColor(AntColor.TEXT);
        int textY = y + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, textX, textY);

        // 右侧线段
        int rightStart = textX + textWidth + TEXT_SIDE_PADDING;
        if (rightStart < w) {
            g2.setColor(lineColor);
            g2.setStroke(stroke);
            g2.drawLine(rightStart, y, w, y);
        }
    }

    private void paintVertical(Graphics2D g2) {
        int x = getWidth() / 2;
        int h = getHeight();
        g2.setStroke(createStroke());
        g2.setColor(lineColor);
        g2.drawLine(x, 0, x, h);
    }

    private Stroke createStroke() {
        if (dashed) {
            return new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f);
        }
        return new BasicStroke(1f);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        if (orientation == Orientation.HORIZONTAL) {
            int height = DEFAULT_MARGIN;
            if (text != null && !text.isEmpty()) {
                FontMetrics fm = getFontMetrics(AntFont.bold(AntFont.FONT_SIZE));
                height = fm.getHeight() + 16;
            }
            return new Dimension(0, height);
        }
        return new Dimension(DEFAULT_MARGIN, 0);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        if (orientation == Orientation.HORIZONTAL) {
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
        return new Dimension(pref.width, Integer.MAX_VALUE);
    }
}
