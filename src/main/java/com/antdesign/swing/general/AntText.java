package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 文本展示组件。
 *
 * <p>轻量级的文本显示组件，类似 {@link JLabel} 但提供 Ant Design 风格的
 * 语义颜色、多行自动换行、省略号截断、可复制、前后缀图标等特性。
 * 与 {@link AntTypography} 相比更轻量、API 更简洁，适合在列表、
 * 卡片、表单等场景中快速展示文本。
 *
 * <pre>{@code
 * // 基本用法
 * AntText text = new AntText("Hello World");
 *
 * // 语义颜色
 * AntText success = new AntText("Done!", AntText.Type.SUCCESS);
 * AntText error = new AntText("Failed", AntText.Type.DANGER);
 *
 * // 带图标
 * AntText withIcon = new AntText("Settings");
 * withIcon.setPrefixIcon(AntIcons.outlined("setting", 14));
 *
 * // 多行换行
 * AntText wrap = new AntText("This is a very long text that should wrap automatically.");
 * wrap.setLineWrap(true);
 * wrap.setPreferredSize(new Dimension(200, 60));
 *
 * // 省略号截断
 * AntText ellipsis = new AntText("This is a very long text...");
 * ellipsis.setMaxLines(1);
 * ellipsis.setPreferredSize(new Dimension(120, 20));
 *
 * // 可复制
 * AntText copy = new AntText("Click to copy me");
 * copy.setCopyable(true);
 *
 * // 可选中
 * AntText selectable = new AntText("You can select this text");
 * selectable.setSelectable(true);
 * }</pre>
 *
 * @see AntTypography
 */
public class AntText extends AbstractAntComponent {

    private static final long serialVersionUID = 1L;

    /** 图标与文本间距 (px)。 */
    private static final int ICON_GAP = 6;

    /** 省略号。 */
    private static final String ELLIPSIS = "...";

    // =========================================================================
    // 枚举
    // =========================================================================

    /**
     * 文本语义类型，影响文字颜色。
     */
    public enum Type {
        DEFAULT, SECONDARY, SUCCESS, WARNING, DANGER
    }

    /**
     * 文本水平对齐。
     */
    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    // =========================================================================
    // 字段
    // =========================================================================

    @Getter private String text;
    @Getter private Type type;
    @Getter private Color color;
    @Getter private boolean bold;
    @Getter private boolean italic;
    @Getter private boolean underline;
    @Getter private boolean strikethrough;
    @Getter private boolean lineWrap;
    @Getter private int maxLines;
    @Getter private boolean copyable;
    @Getter private boolean selectable;
    @Getter private Icon prefixIcon;
    @Getter private Icon suffixIcon;
    @Getter private Alignment alignment;
    private ComponentSize size;

    private boolean hovered;

    // 文本选择相关
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private boolean selecting;
    /** 缓存换行后的行列表（仅 lineWrap=true 时使用）。 */
    private List<String> wrappedLines;

    // =========================================================================
    // 构造方法
    // =========================================================================

    /** 创建默认文本。 */
    public AntText(String text) {
        this(text, Type.DEFAULT);
    }

    /**
     * 创建指定语义类型的文本。
     *
     * @param text 显示文本
     * @param type 语义类型
     */
    public AntText(String text, Type type) {
        this.text = (text != null) ? text : "";
        this.type = (type != null) ? type : Type.DEFAULT;
        this.alignment = Alignment.LEFT;
        this.size = ComponentSize.MIDDLE;
        this.maxLines = 0; // 0 = 不限制

        installMouseListeners();
    }

    // =========================================================================
    // 静态工厂
    // =========================================================================

    /** 创建默认文本。 */
    public static AntText of(String text) {
        return new AntText(text);
    }

    /** 创建次要文本。 */
    public static AntText secondary(String text) {
        return new AntText(text, Type.SECONDARY);
    }

    /** 创建成功文本。 */
    public static AntText success(String text) {
        return new AntText(text, Type.SUCCESS);
    }

    /** 创建警告文本。 */
    public static AntText warning(String text) {
        return new AntText(text, Type.WARNING);
    }

    /** 创建危险文本。 */
    public static AntText danger(String text) {
        return new AntText(text, Type.DANGER);
    }

    // =========================================================================
    // Setter
    // =========================================================================

    public void setText(String text) {
        updateAndRelayout(() -> {
            this.text = (text != null) ? text : "";
            wrappedLines = null;
        });
    }

    public void setType(Type type) {
        updateAndRepaint(() -> this.type = (type != null) ? type : Type.DEFAULT);
    }

    /** 设置自定义颜色（优先于 type）。 */
    public void setColor(Color color) {
        updateAndRepaint(() -> this.color = color);
    }

    public void setBold(boolean bold) {
        updateAndRelayout(() -> {
            this.bold = bold;
            wrappedLines = null;
        });
    }

    public void setItalic(boolean italic) {
        updateAndRepaint(() -> this.italic = italic);
    }

    public void setUnderline(boolean underline) {
        updateAndRepaint(() -> this.underline = underline);
    }

    public void setStrikethrough(boolean strikethrough) {
        updateAndRepaint(() -> this.strikethrough = strikethrough);
    }

    /** 启用自动换行（需配合固定宽度使用）。 */
    public void setLineWrap(boolean lineWrap) {
        updateAndRelayout(() -> {
            this.lineWrap = lineWrap;
            wrappedLines = null;
        });
    }

    /**
     * 设置最大行数，超出用省略号截断。
     *
     * @param maxLines 最大行数，0 = 不限制
     */
    public void setMaxLines(int maxLines) {
        updateAndRelayout(() -> {
            this.maxLines = Math.max(0, maxLines);
            wrappedLines = null;
        });
    }

    /** 启用点击复制。 */
    public void setCopyable(boolean copyable) {
        this.copyable = copyable;
        setCursor(copyable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
    }

    /** 启用文本可选中。 */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        if (selectable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else if (!copyable) {
            setCursor(Cursor.getDefaultCursor());
        }
        clearSelection();
    }

    public void setPrefixIcon(Icon icon) {
        updateAndRelayout(() -> this.prefixIcon = icon);
    }

    public void setSuffixIcon(Icon icon) {
        updateAndRelayout(() -> this.suffixIcon = icon);
    }

    public void setAlignment(Alignment alignment) {
        updateAndRepaint(() -> this.alignment = (alignment != null) ? alignment : Alignment.LEFT);
    }

    public void setSize(ComponentSize size) {
        updateAndRelayout(() -> {
            this.size = (size != null) ? size : ComponentSize.MIDDLE;
            wrappedLines = null;
        });
    }

    public ComponentSize getComponentSize() {
        return size;
    }

    // =========================================================================
    // 尺寸
    // =========================================================================

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Font font = resolveFont();
        FontMetrics fm = getFontMetrics(font);

        int iconW = calcIconWidth();
        int textW = fm.stringWidth(text);
        int lineH = fm.getHeight();

        if (lineWrap && getParent() != null && getParent().getWidth() > 0) {
            int availW = getParent().getWidth() - iconW;
            List<String> lines = wrapText(fm, availW);
            int displayLines = (maxLines > 0) ? Math.min(lines.size(), maxLines) : lines.size();
            return new Dimension(getParent().getWidth(), lineH * displayLines);
        }

        return new Dimension(iconW + textW, lineH);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // =========================================================================
    // 绘制
    // =========================================================================

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
        GraphicsUtils.setupAntialiasing(g2);

        Font font = resolveFont();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();

        int x = 0;

        // 绘制前缀图标
        if (prefixIcon != null) {
            int iconY = (lineH - prefixIcon.getIconHeight()) / 2;
            prefixIcon.paintIcon(this, g2, x, iconY);
            x += prefixIcon.getIconWidth() + ICON_GAP;
        }

        int textAreaW = width - x;
        if (suffixIcon != null) {
            textAreaW -= (suffixIcon.getIconWidth() + ICON_GAP);
        }

        g2.setColor(resolveColor());

        if (lineWrap) {
            paintWrappedText(g2, fm, x, textAreaW, lineH, height);
        } else {
            paintSingleLine(g2, fm, x, textAreaW, lineH, height);
        }

        // 绘制后缀图标
        if (suffixIcon != null) {
            int sx = width - suffixIcon.getIconWidth();
            int sy = (Math.min(lineH, height) - suffixIcon.getIconHeight()) / 2;
            suffixIcon.paintIcon(this, g2, sx, sy);
        }

        // copyable 悬停提示
        if (copyable && hovered) {
            paintCopyHint(g2, fm, width, height);
        }
    }

    private void paintSingleLine(Graphics2D g2, FontMetrics fm,
                                  int x, int availW, int lineH, int height) {
        String display = text;

        // 省略号截断
        if (fm.stringWidth(display) > availW && availW > 0) {
            display = truncateWithEllipsis(fm, display, availW);
        }

        int textW = fm.stringWidth(display);
        int drawX = calcAlignedX(x, textW, availW);
        int drawY = (height - lineH) / 2 + fm.getAscent();

        // 选中高亮
        if (selectable && selectionStart >= 0 && selectionEnd > selectionStart) {
            paintSelection(g2, fm, display, drawX, drawY, lineH);
        }

        g2.setColor(resolveColor());
        g2.drawString(display, drawX, drawY);
        paintDecorations(g2, fm, display, drawX, drawY);
    }

    private void paintWrappedText(Graphics2D g2, FontMetrics fm,
                                   int x, int availW, int lineH, int height) {
        List<String> lines = wrapText(fm, availW);
        int displayCount = (maxLines > 0) ? Math.min(lines.size(), maxLines) : lines.size();
        boolean truncated = maxLines > 0 && lines.size() > maxLines;

        int drawY = fm.getAscent();

        for (int i = 0; i < displayCount; i++) {
            String line = lines.get(i);

            // 最后一行需要省略号
            if (truncated && i == displayCount - 1) {
                line = truncateWithEllipsis(fm, line, availW);
            }

            int textW = fm.stringWidth(line);
            int drawX = calcAlignedX(x, textW, availW);

            g2.drawString(line, drawX, drawY);
            paintDecorations(g2, fm, line, drawX, drawY);
            drawY += lineH;
        }
    }

    private void paintDecorations(Graphics2D g2, FontMetrics fm,
                                   String display, int drawX, int drawY) {
        int textW = fm.stringWidth(display);

        if (underline) {
            int uy = drawY + fm.getDescent() / 2;
            g2.drawLine(drawX, uy, drawX + textW, uy);
        }
        if (strikethrough) {
            int sy = drawY - fm.getAscent() / 3;
            g2.drawLine(drawX, sy, drawX + textW, sy);
        }
    }

    private void paintSelection(Graphics2D g2, FontMetrics fm,
                                 String display, int drawX, int drawY, int lineH) {
        int s = Math.max(0, Math.min(selectionStart, display.length()));
        int e = Math.max(0, Math.min(selectionEnd, display.length()));
        if (s >= e) return;

        String before = display.substring(0, s);
        String selected = display.substring(s, e);

        int selX = drawX + fm.stringWidth(before);
        int selW = fm.stringWidth(selected);

        ColorToken ct = colorToken();
        g2.setColor(ct.getPrimaryBgColor());
        g2.fillRect(selX, drawY - fm.getAscent(), selW, lineH);
    }

    private void paintCopyHint(Graphics2D g2, FontMetrics fm, int width, int height) {
        ColorToken ct = colorToken();
        g2.setColor(ct.getPrimaryColor());
        // 在右侧绘制一个小复制图标指示
        int size = 10;
        int cx = width - size - 2;
        int cy = (height - size) / 2;
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRect(cx + 2, cy, size - 3, size - 3);
        g2.drawRect(cx, cy + 2, size - 3, size - 3);
    }

    // =========================================================================
    // 内部辅助
    // =========================================================================

    private Font resolveFont() {
        FontToken ft = fontToken();
        int fontSize;
        switch (size) {
            case LARGE:  fontSize = ft.getFontSizeLg(); break;
            case SMALL:  fontSize = ft.getFontSizeSm(); break;
            default:     fontSize = ft.getFontSize(); break;
        }
        int style = Font.PLAIN;
        if (bold) style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;
        return ft.createFont(fontSize, style);
    }

    private Color resolveColor() {
        if (!isEnabled()) return colorToken().getDisabledColor();
        if (color != null) return color;

        ColorToken ct = colorToken();
        switch (type) {
            case SECONDARY: return ct.getTextSecondaryColor();
            case SUCCESS:   return ct.getSuccessColor();
            case WARNING:   return ct.getWarningColor();
            case DANGER:    return ct.getErrorColor();
            default:        return ct.getTextColor();
        }
    }

    private int calcIconWidth() {
        int w = 0;
        if (prefixIcon != null) w += prefixIcon.getIconWidth() + ICON_GAP;
        if (suffixIcon != null) w += suffixIcon.getIconWidth() + ICON_GAP;
        return w;
    }

    private int calcAlignedX(int baseX, int textW, int availW) {
        switch (alignment) {
            case CENTER: return baseX + Math.max(0, (availW - textW) / 2);
            case RIGHT:  return baseX + Math.max(0, availW - textW);
            default:     return baseX;
        }
    }

    private String truncateWithEllipsis(FontMetrics fm, String str, int maxW) {
        int ellipsisW = fm.stringWidth(ELLIPSIS);
        if (maxW <= ellipsisW) return ELLIPSIS;

        int target = maxW - ellipsisW;
        for (int i = str.length() - 1; i > 0; i--) {
            if (fm.stringWidth(str.substring(0, i)) <= target) {
                return str.substring(0, i) + ELLIPSIS;
            }
        }
        return ELLIPSIS;
    }

    private List<String> wrapText(FontMetrics fm, int maxW) {
        if (wrappedLines != null) return wrappedLines;
        if (maxW <= 0) maxW = Integer.MAX_VALUE;

        List<String> result = new ArrayList<>();
        String[] paragraphs = text.split("\n", -1);

        for (String para : paragraphs) {
            if (para.isEmpty()) {
                result.add("");
                continue;
            }

            StringBuilder line = new StringBuilder();
            for (int i = 0; i < para.length(); i++) {
                line.append(para.charAt(i));
                if (fm.stringWidth(line.toString()) > maxW) {
                    if (line.length() > 1) {
                        line.deleteCharAt(line.length() - 1);
                        i--;
                    }
                    result.add(line.toString());
                    line.setLength(0);
                }
            }
            if (line.length() > 0) {
                result.add(line.toString());
            }
        }

        if (result.isEmpty()) result.add("");
        wrappedLines = result;
        return result;
    }

    private void clearSelection() {
        selectionStart = -1;
        selectionEnd = -1;
        selecting = false;
        repaint();
    }

    private int charIndexAtX(int mouseX) {
        Font font = resolveFont();
        FontMetrics fm = getFontMetrics(font);
        int iconW = (prefixIcon != null) ? prefixIcon.getIconWidth() + ICON_GAP : 0;
        int x = mouseX - iconW;
        if (x <= 0) return 0;
        for (int i = 1; i <= text.length(); i++) {
            if (fm.stringWidth(text.substring(0, i)) > x) {
                return i - 1;
            }
        }
        return text.length();
    }

    // =========================================================================
    // 鼠标交互
    // =========================================================================

    private void installMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (copyable && text != null && !text.isEmpty()) {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(text), null);
                    // 闪烁提示
                    ToolTipManager.sharedInstance().setInitialDelay(0);
                    setToolTipText("Copied!");
                    Timer timer = new Timer(1500, evt -> setToolTipText(null));
                    timer.setRepeats(false);
                    timer.start();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (selectable && !lineWrap) {
                    selectionStart = charIndexAtX(e.getX());
                    selectionEnd = selectionStart;
                    selecting = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selecting = false;
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selecting && selectable && !lineWrap) {
                    selectionEnd = charIndexAtX(e.getX());
                    repaint();
                }
            }
        });
    }
}
