package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 排版组件。
 *
 * <p>对应 Ant Design 的 Typography.Title/Text/Paragraph/Link。
 *
 * <pre>{@code
 * AntTypography h1 = AntTypography.title("Hello", 1);
 * AntTypography text = AntTypography.text("Some content");
 * AntTypography link = AntTypography.link("Click me");
 * }</pre>
 *
 * @see FontToken
 */
public class AntTypography extends AbstractAntComponent {

    private static final long serialVersionUID = 1L;

    /**
     * 排版元素的种类。
     */
    public enum Kind {TITLE, TEXT, PARAGRAPH, LINK}

    /**
     * 文本语义类型，影响文字颜色。
     */
    public enum Type {DEFAULT, SECONDARY, SUCCESS, WARNING, DANGER}

    // =========================================================================
    // 字段
    // =========================================================================

    @Getter
    private String content;
    @Getter
    private Kind kind;
    @Getter
    private int level;
    @Getter
    private Type type;
    @Getter
    private Status status;
    @Getter
    private boolean bold;
    @Getter
    private boolean italic;
    @Getter
    private boolean underline;
    @Getter
    private boolean strikethrough;
    @Getter
    private boolean disabled;
    @Getter
    private boolean code;
    @Getter
    private boolean keyboard;
    @Getter
    private boolean mark;

    private boolean hovered;

    // =========================================================================
    // 构造方法
    // =========================================================================

    public AntTypography(String content, Kind kind, int level) {
        this.content = (content != null) ? content : "";
        this.kind = (kind != null) ? kind : Kind.TEXT;
        this.level = Math.max(1, Math.min(5, level));
        this.type = Type.DEFAULT;

        if (kind == Kind.LINK) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            installLinkMouseListener();
        }
    }

    // =========================================================================
    // 静态工厂方法
    // =========================================================================

    public static AntTypography title(String content, int level) {
        return new AntTypography(content, Kind.TITLE, level);
    }

    public static AntTypography text(String content) {
        return new AntTypography(content, Kind.TEXT, 1);
    }

    public static AntTypography paragraph(String content) {
        return new AntTypography(content, Kind.PARAGRAPH, 1);
    }

    public static AntTypography link(String content) {
        return new AntTypography(content, Kind.LINK, 1);
    }

    // =========================================================================
    // Setter — 使用基类辅助方法
    // =========================================================================

    public void setContent(String content) {
        updateAndRelayout(() ->
                this.content = (content != null) ? content : "");
    }

    public void setLevel(int level) {
        updateAndRelayout(() ->
                this.level = Math.max(1, Math.min(5, level)));
    }

    public void setType(Type type) {
        updateAndRepaint(() ->
                this.type = (type != null) ? type : Type.DEFAULT);
    }

    public void setStatus(Status status) {
        updateAndRepaint(() -> this.status = status);
    }

    public void setBold(boolean bold) {
        updateAndRelayout(() -> this.bold = bold);
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

    public void setDisabled(boolean disabled) {
        updateAndRepaint(() -> this.disabled = disabled);
    }

    public void setCode(boolean code) {
        updateAndRelayout(() -> this.code = code);
    }

    public void setKeyboard(boolean keyboard) {
        updateAndRelayout(() -> this.keyboard = keyboard);
    }

    public void setMark(boolean mark) {
        updateAndRepaint(() -> this.mark = mark);
    }

    // =========================================================================
    // 尺寸
    // =========================================================================

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        FontToken ft = fontToken();
        Font font = resolveFont(ft);
        FontMetrics fm = getFontMetrics(font);

        Insets padding = resolvePadding();
        int textWidth = fm.stringWidth(content);
        int textHeight = fm.getHeight();

        if (kind == Kind.PARAGRAPH && getParent() != null) {
            int maxWidth = getParent().getWidth();
            if (maxWidth > 0) {
                int lines = (int) Math.ceil(
                        (double) textWidth / (maxWidth - padding.left - padding.right));
                lines = Math.max(lines, 1);
                return new Dimension(maxWidth,
                        lines * textHeight + padding.top + padding.bottom);
            }
        }

        return new Dimension(
                textWidth + padding.left + padding.right,
                textHeight + padding.top + padding.bottom);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // =========================================================================
    // 绘制 — 覆写基类 paintAnt
    // =========================================================================

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
        ColorToken ct = colorToken();
        FontToken ft = fontToken();

        Font font = resolveFont(ft);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        Insets padding = resolvePadding();
        int x = padding.left;
        int y = padding.top + fm.getAscent();
        int textWidth = fm.stringWidth(content);

        if (mark) {
            g2.setColor(new Color(0xFF, 0xE5, 0x8F));
            g2.fillRect(x, padding.top, textWidth, fm.getHeight());
        }

        if (code) {
            paintCodeBackground(g2, x, padding.top, textWidth, fm.getHeight());
        }

        if (keyboard) {
            paintKeyboardBackground(g2, x, padding.top, textWidth, fm.getHeight());
        }

        g2.setColor(resolveTextColor(ct));

        if (kind == Kind.PARAGRAPH) {
            paintParagraph(g2, fm, x, padding.top);
        } else {
            g2.drawString(content, x, y);
        }

        if (underline || (kind == Kind.LINK && hovered)) {
            g2.drawLine(x, y + fm.getDescent() / 2,
                    x + textWidth, y + fm.getDescent() / 2);
        }

        if (strikethrough) {
            int strikeY = y - fm.getAscent() / 3;
            g2.drawLine(x, strikeY, x + textWidth, strikeY);
        }
    }

    // =========================================================================
    // 内部辅助
    // =========================================================================

    private void paintCodeBackground(Graphics2D g2, int x, int top,
                                     int tw, int th) {
        int arc = 4;
        int pad = 4;
        g2.setColor(new Color(0xF5, 0xF5, 0xF5));
        g2.fillRoundRect(x - pad, top, tw + pad * 2, th, arc, arc);
        g2.setColor(new Color(0xD9, 0xD9, 0xD9));
        g2.drawRoundRect(x - pad, top, tw + pad * 2, th, arc, arc);
    }

    private void paintKeyboardBackground(Graphics2D g2, int x, int top,
                                         int tw, int th) {
        int arc = 4;
        int pad = 6;
        g2.setColor(new Color(0xF5, 0xF5, 0xF5));
        g2.fillRoundRect(x - pad, top - 2, tw + pad * 2, th + 4, arc, arc);
        g2.setColor(new Color(0xD9, 0xD9, 0xD9));
        g2.drawRoundRect(x - pad, top - 2, tw + pad * 2, th + 4, arc, arc);
    }

    private void paintParagraph(Graphics2D g2, FontMetrics fm,
                                int x, int top) {
        int maxWidth = getWidth() - x * 2;
        if (maxWidth <= 0) {
            g2.drawString(content, x, top + fm.getAscent());
            return;
        }

        int lineY = top + fm.getAscent();
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            line.append(content.charAt(i));
            if (fm.stringWidth(line.toString()) > maxWidth) {
                if (line.length() > 1) {
                    line.deleteCharAt(line.length() - 1);
                    i--;
                }
                g2.drawString(line.toString(), x, lineY);
                lineY += fm.getHeight();
                line.setLength(0);
            }
        }

        if (line.length() > 0) {
            g2.drawString(line.toString(), x, lineY);
        }
    }

    private Font resolveFont(FontToken ft) {
        int fontSize;
        int fontStyle = Font.PLAIN;

        if (kind == Kind.TITLE) {
            fontSize = resolveTitleFontSize(ft);
            fontStyle = Font.BOLD;
        } else {
            fontSize = ft.getFontSize();
        }

        if (bold) {
            fontStyle |= Font.BOLD;
        }
        if (italic) {
            fontStyle |= Font.ITALIC;
        }

        String family = code ? ft.getFontFamilyCode() : ft.getFontFamily();
        return new Font(family, fontStyle, fontSize);
    }

    private int resolveTitleFontSize(FontToken ft) {
        switch (level) {
            case 1:
                return ft.getFontSizeHeading1();
            case 2:
                return ft.getFontSizeHeading2();
            case 3:
                return ft.getFontSizeHeading3();
            case 4:
                return ft.getFontSizeHeading4();
            case 5:
                return ft.getFontSizeHeading5();
            default:
                return ft.getFontSizeHeading3();
        }
    }

    private Color resolveTextColor(ColorToken ct) {
        if (disabled) {
            return ct.getDisabledColor();
        }
        if (status != null) {
            return status.getDefaultColor();
        }
        if (kind == Kind.LINK) {
            return hovered ? ct.getLinkHoverColor() : ct.getLinkColor();
        }

        switch (type) {
            case SECONDARY:
                return ct.getTextSecondaryColor();
            case SUCCESS:
                return ct.getSuccessColor();
            case WARNING:
                return ct.getWarningColor();
            case DANGER:
                return ct.getErrorColor();
            default:
                return ct.getTextColor();
        }
    }

    private Insets resolvePadding() {
        if (code) return new Insets(1, 6, 1, 6);
        if (keyboard) return new Insets(3, 8, 3, 8);
        if (kind == Kind.TITLE) return new Insets(0, 0, 8, 0);
        if (kind == Kind.PARAGRAPH) return new Insets(0, 0, 14, 0);
        return new Insets(0, 0, 0, 0);
    }

    private void installLinkMouseListener() {
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
        });
    }
}
