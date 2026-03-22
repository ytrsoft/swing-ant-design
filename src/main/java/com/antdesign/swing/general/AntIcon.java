package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntActionEvent;
import com.antdesign.swing.event.AntActionListener;
import com.antdesign.swing.util.AntIcons;
import com.antdesign.swing.util.ColorUtils;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 图标组件。
 *
 * <pre>{@code
 * AntIcon searchIcon = AntIcon.outlined("search");
 * AntIcon heartIcon = AntIcon.filled("heart", 24, Color.RED);
 * }</pre>
 */
public class AntIcon extends AbstractAntComponent {

    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_SIZE = 16;

    @Getter
    private String iconName;
    @Getter
    private String style;
    @Getter
    private int iconSize;
    @Getter
    private Color iconColor;
    @Getter
    private double rotate;
    @Getter
    private boolean spin;
    @Getter
    private boolean clickable;

    private boolean hovered;
    private ImageIcon cachedIcon;
    private boolean mouseListenerInstalled;
    private final List<AntActionListener> actionListeners = new CopyOnWriteArrayList<>();

    public AntIcon(String style, String iconName, int iconSize, Color color) {
        this.style = (style != null) ? style : "outlined";
        this.iconName = iconName;
        this.iconSize = (iconSize > 0) ? iconSize : DEFAULT_SIZE;
        this.iconColor = color;
        refreshCachedIcon();
        setThemeListener(theme -> {
            refreshCachedIcon();
            repaint();
        });
    }

    // === Static factories ===
    public static AntIcon outlined(String name) {
        return new AntIcon("outlined", name, DEFAULT_SIZE, null);
    }

    public static AntIcon outlined(String name, int size) {
        return new AntIcon("outlined", name, size, null);
    }

    public static AntIcon outlined(String name, int size, Color color) {
        return new AntIcon("outlined", name, size, color);
    }

    public static AntIcon filled(String name) {
        return new AntIcon("filled", name, DEFAULT_SIZE, null);
    }

    public static AntIcon filled(String name, int size) {
        return new AntIcon("filled", name, size, null);
    }

    public static AntIcon filled(String name, int size, Color color) {
        return new AntIcon("filled", name, size, color);
    }

    // === Setters ===
    public void setIconName(String iconName) {
        updateAndRepaint(() -> {
            this.iconName = iconName;
            refreshCachedIcon();
        });
    }

    public void setStyle(String style) {
        updateAndRepaint(() -> {
            this.style = (style != null) ? style : "outlined";
            refreshCachedIcon();
        });
    }

    public void setIconSize(int iconSize) {
        updateAndRelayout(() -> {
            this.iconSize = (iconSize > 0) ? iconSize : DEFAULT_SIZE;
            refreshCachedIcon();
        });
    }

    public void setIconColor(Color color) {
        updateAndRepaint(() -> {
            this.iconColor = color;
            refreshCachedIcon();
        });
    }

    public void setRotate(double rotate) {
        updateAndRepaint(() -> this.rotate = rotate);
    }

    public void setSpin(boolean spin) {
        updateAndRepaint(() -> this.spin = spin);
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        setCursor(clickable ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (clickable) {
            installMouseListener();
        }
    }

    public ImageIcon getImageIcon() {
        return cachedIcon;
    }

    // === Events ===
    public void addActionListener(AntActionListener l) {
        if (l != null) actionListeners.add(l);
    }

    public void removeActionListener(AntActionListener l) {
        actionListeners.remove(l);
    }

    protected void fireActionEvent() {
        AntActionEvent event = new AntActionEvent(this, iconName);
        for (AntActionListener l : actionListeners) {
            l.actionPerformed(event);
        }
    }

    // === Size ===
    @Override
    public Dimension getPreferredSize() {
        return isPreferredSizeSet() ? super.getPreferredSize() : new Dimension(iconSize, iconSize);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // === Paint ===
    @Override
    protected void paintAnt(Graphics2D g2, int w, int h) {
        if (cachedIcon == null) {
            return;
        }

        GraphicsUtils.setupAntialiasing(g2);

        if (clickable && hovered && isEnabled()) {
            g2.setColor(ColorUtils.withAlpha(colorToken().getPrimaryColor(), 0.1f));
            g2.fillOval(0, 0, w, h);
        }

        int x = (w - cachedIcon.getIconWidth()) / 2;
        int y = (h - cachedIcon.getIconHeight()) / 2;
        if (rotate != 0) {
            g2.rotate(Math.toRadians(rotate), w / 2.0, h / 2.0);
        }
        cachedIcon.paintIcon(this, g2, x, y);
    }

    // === Internal ===
    private void refreshCachedIcon() {
        if (iconName == null || iconName.isEmpty()) {
            cachedIcon = null;
            return;
        }
        Color color = (iconColor != null) ? iconColor : colorToken().getTextColor();
        cachedIcon = AntIcons.get(style, iconName, iconSize, color);
    }

    private void installMouseListener() {
        if (mouseListenerInstalled) {
            return;
        }
        mouseListenerInstalled = true;
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
                if (clickable && isEnabled()) fireActionEvent();
            }
        });
    }
}
