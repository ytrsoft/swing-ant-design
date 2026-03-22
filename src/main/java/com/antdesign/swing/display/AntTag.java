package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntActionEvent;
import com.antdesign.swing.event.AntActionListener;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.ColorUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 标签组件。
 *
 * <p>对应 Ant Design {@code <Tag>}，用于进行标记和分类的小标签。
 * 支持预设状态色、自定义颜色、可关闭、可选中等特性。
 *
 * <pre>{@code
 * AntTag tag = new AntTag("Tag 1");
 * AntTag colorTag = new AntTag("Success", Status.SUCCESS);
 * AntTag closable = new AntTag("Closable");
 * closable.setClosable(true);
 * }</pre>
 *
 * @see Status
 */
public class AntTag extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int CLOSE_SIZE = 12;
  private static final int GAP = 4;

  @Getter private String text;
  @Getter private Status status;
  @Getter private Color color;
  @Getter private Icon icon;
  @Getter private boolean closable;
  @Getter private boolean bordered;
  @Getter private boolean checkable;
  @Getter private boolean checked;

  private boolean hovered;
  private boolean closeHovered;
  private boolean closed;

  private final List<AntActionListener> closeListeners = new CopyOnWriteArrayList<>();
  private final List<AntActionListener> checkListeners = new CopyOnWriteArrayList<>();

  /**
   * 创建默认标签。
   *
   * @param text 标签文本
   */
  public AntTag(String text) {
    this.text = (text != null) ? text : "";
    this.bordered = true;
    installMouseListener();
    // Theme listener handled by base class
  }

  /**
   * 创建带状态色的标签。
   *
   * @param text   标签文本
   * @param status 状态类型
   */
  public AntTag(String text, Status status) {
    this(text);
    this.status = status;
  }

  /**
   * 创建自定义颜色的标签。
   *
   * @param text  标签文本
   * @param color 自定义颜色
   */
  public AntTag(String text, Color color) {
    this(text);
    this.color = color;
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setText(String text) {
    this.text = (text != null) ? text : "";
    revalidate();
    repaint();
  }

  public void setStatus(Status status) {
    this.status = status;
    repaint();
  }

  public void setColor(Color color) {
    this.color = color;
    repaint();
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
    revalidate();
    repaint();
  }

  public void setClosable(boolean closable) {
    this.closable = closable;
    revalidate();
    repaint();
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    repaint();
  }

  public void setCheckable(boolean checkable) {
    this.checkable = checkable;
    repaint();
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
    repaint();
  }

  // =========================================================================
  // 事件
  // =========================================================================

  public void addCloseListener(AntActionListener listener) {
    if (listener != null) {
      closeListeners.add(listener);
    }
  }

  public void removeCloseListener(AntActionListener listener) {
    closeListeners.remove(listener);
  }

  public void addCheckListener(AntActionListener listener) {
    if (listener != null) {
      checkListeners.add(listener);
    }
  }

  public void removeCheckListener(AntActionListener listener) {
    checkListeners.remove(listener);
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (closed) {
      return new Dimension(0, 0);
    }
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    Font font = ft.createFont(ft.getFontSizeSm(), Font.PLAIN);
    FontMetrics fm = getFontMetrics(font);

    int textW = fm.stringWidth(text);
    int iconW = (icon != null) ? icon.getIconWidth() + GAP : 0;
    int closeW = closable ? CLOSE_SIZE + GAP : 0;
    int hPad = st.getPaddingXs() - 1;

    return new Dimension(hPad * 2 + iconW + textW + closeW, 22);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    if (closed) {
      return;
    }
    int w = width;
    int h = height;
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    int arc = st.getBorderRadiusSm();

    g2.setColor(resolveBg(ct));
    g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

    if (bordered && color == null) {
      g2.setColor(resolveBorder(ct));
      g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
    }

    Color fg = resolveFg(ct);
    g2.setColor(fg);
    g2.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
    FontMetrics fm = g2.getFontMetrics();

    int x = st.getPaddingXs() - 1;
    int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

    if (icon != null) {
      icon.paintIcon(this, g2, x, (h - icon.getIconHeight()) / 2);
      x += icon.getIconWidth() + GAP;
    }

    g2.drawString(text, x, textY);

    if (closable) {
      int cx = w - st.getPaddingXs() - CLOSE_SIZE + 2;
      g2.setColor(closeHovered ? fg : ColorUtils.withAlpha(fg, 0.5f));
      g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, CLOSE_SIZE));
      g2.drawString("\u00D7", cx, (h + CLOSE_SIZE) / 2 - 2);
    }
  }

  @Override
  public boolean isVisible() {
    return !closed && super.isVisible();
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private Color resolveBg(ColorToken ct) {
    if (checkable) {
      return checked ? ct.getPrimaryColor() : ct.getBgContainer();
    }
    if (color != null) {
      return color;
    }
    if (status != null) {
      switch (status) {
        case SUCCESS: return ct.getSuccessBgColor();
        case WARNING: return ct.getWarningBgColor();
        case ERROR:   return ct.getErrorBgColor();
        default:      return ct.getInfoBgColor();
      }
    }
    return ct.getFillSecondaryColor();
  }

  private Color resolveBorder(ColorToken ct) {
    if (status != null) {
      return ColorUtils.withAlpha(status.getDefaultColor(), 0.4f);
    }
    return ct.getBorderColor();
  }

  private Color resolveFg(ColorToken ct) {
    if (checkable && checked) {
      return Color.WHITE;
    }
    if (color != null) {
      return ColorUtils.isLight(color) ? ct.getTextColor() : Color.WHITE;
    }
    if (status != null) {
      return status.getDefaultColor();
    }
    return ct.getTextColor();
  }

  private void installMouseListener() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        hovered = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        hovered = false;
        closeHovered = false;
        repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (closable && isCloseArea(e.getX())) {
          closed = true;
          setVisible(false);
          AntActionEvent evt = new AntActionEvent(AntTag.this, "close");
          for (AntActionListener l : closeListeners) {
            l.actionPerformed(evt);
          }
          return;
        }
        if (checkable) {
          checked = !checked;
          repaint();
          AntActionEvent evt = new AntActionEvent(AntTag.this, String.valueOf(checked));
          for (AntActionListener l : checkListeners) {
            l.actionPerformed(evt);
          }
        }
      }
    });
    addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        boolean was = closeHovered;
        closeHovered = closable && isCloseArea(e.getX());
        if (was != closeHovered) {
          repaint();
        }
      }
    });
  }

  private boolean isCloseArea(int x) {
    SizeToken st = sizeToken();
    return x >= getWidth() - st.getPaddingXs() - CLOSE_SIZE - 2;
  }
}
