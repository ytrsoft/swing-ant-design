package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 徽标数组件。
 *
 * <p>对应 Ant Design {@code <Badge>}，一般出现在通知图标或头像的右上角，
 * 用于显示需要处理的消息条数。支持数字、小红点、状态点和自定义颜色。
 *
 * <pre>{@code
 * AntBadge badge = new AntBadge(myIcon);
 * badge.setCount(5);
 *
 * AntBadge dot = new AntBadge(myAvatar);
 * dot.setDot(true);
 * }</pre>
 */
public class AntBadge extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int DOT_SIZE = 8;
  private static final int BADGE_HEIGHT = 20;
  private static final int MIN_WIDTH = 20;

  @Getter private int count;
  @Getter private int overflowCount;
  @Getter private boolean dot;
  @Getter private boolean showZero;
  @Getter private Status status;
  @Getter private String statusText;
  @Getter private Color color;
  @Getter private Component child;

  /**
   * 创建包裹子组件的徽标。
   *
   * @param child 被包裹的子组件
   */
  public AntBadge(Component child) {
    this.child = child;
    this.overflowCount = 99;
    setLayout(null);
    if (child != null) {
      add(child);
    }
    // Theme listener handled by base class
  }

  /** 创建独立的状态徽标。 */
  public AntBadge() {
    this(null);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setCount(int count) {
    this.count = Math.max(0, count);
    repaint();
  }

  public void setOverflowCount(int overflowCount) {
    this.overflowCount = Math.max(1, overflowCount);
    repaint();
  }

  public void setDot(boolean dot) {
    this.dot = dot;
    repaint();
  }

  public void setShowZero(boolean showZero) {
    this.showZero = showZero;
    repaint();
  }

  public void setStatus(Status status) {
    this.status = status;
    revalidate();
    repaint();
  }

  public void setStatusText(String statusText) {
    this.statusText = statusText;
    revalidate();
    repaint();
  }

  public void setColor(Color color) {
    this.color = color;
    repaint();
  }

  // =========================================================================
  // 布局
  // =========================================================================

  @Override
  public void doLayout() {
    if (child != null) {
      Dimension childPref = child.getPreferredSize();
      child.setBounds(0, 0, childPref.width, childPref.height);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    if (status != null) {
      FontToken ft = fontToken();
      FontMetrics fm = getFontMetrics(ft.createFont(ft.getFontSize(), Font.PLAIN));
      int textW = (statusText != null) ? fm.stringWidth(statusText) + 14 : 0;
      return new Dimension(DOT_SIZE + textW, Math.max(DOT_SIZE, fm.getHeight()));
    }
    if (child != null) {
      Dimension cp = child.getPreferredSize();
      int extra = (count > 0 || dot || showZero) ? BADGE_HEIGHT / 2 : 0;
      return new Dimension(cp.width + extra, cp.height + extra);
    }
    return new Dimension(MIN_WIDTH, BADGE_HEIGHT);
  }

  // =========================================================================
  // 绘制
  // =========================================================================

  @Override
  protected void paintChildren(Graphics g) {
    super.paintChildren(g);
  }  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    // 状态点模式
    if (status != null) {
      Color dotColor = status.getDefaultColor();
      g2.setColor(dotColor);
      int dotY = (height - DOT_SIZE) / 2;
      g2.fillOval(0, dotY, DOT_SIZE, DOT_SIZE);

      if (statusText != null && !statusText.isEmpty()) {
        g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
        g2.setColor(ct.getTextColor());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(statusText, DOT_SIZE + 6,
            (height + fm.getAscent() - fm.getDescent()) / 2);
      }
      return;
    }

    boolean shouldShow = dot || count > 0 || (count == 0 && showZero);
    if (!shouldShow) {
      return;
    }

    Color badgeColor = (color != null) ? color : ct.getErrorColor();

    if (child != null) {
      Dimension cp = child.getPreferredSize();
      if (dot) {
        g2.setColor(badgeColor);
        g2.fillOval(cp.width - DOT_SIZE / 2, -DOT_SIZE / 2 + 2,
            DOT_SIZE, DOT_SIZE);
      } else {
        String text = (count > overflowCount) ? overflowCount + "+" : String.valueOf(count);
        g2.setFont(ft.createFont(11, Font.PLAIN));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int bw = Math.max(MIN_WIDTH, textW + 10);
        int bx = cp.width - bw / 2;
        int by = -BADGE_HEIGHT / 2 + 4;

        g2.setColor(badgeColor);
        g2.fillRoundRect(bx, by, bw, BADGE_HEIGHT, BADGE_HEIGHT, BADGE_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.drawString(text, bx + (bw - textW) / 2,
            by + (BADGE_HEIGHT + fm.getAscent() - fm.getDescent()) / 2);
      }
    } else {
      // 独立徽标
      String text = (count > overflowCount) ? overflowCount + "+" : String.valueOf(count);
      g2.setFont(ft.createFont(11, Font.PLAIN));
      FontMetrics fm = g2.getFontMetrics();
      int textW = fm.stringWidth(text);
      int bw = Math.max(MIN_WIDTH, textW + 10);

      g2.setColor(badgeColor);
      g2.fillRoundRect(0, 0, bw, BADGE_HEIGHT, BADGE_HEIGHT, BADGE_HEIGHT);
      g2.setColor(Color.WHITE);
      g2.drawString(text, (bw - textW) / 2,
          (BADGE_HEIGHT + fm.getAscent() - fm.getDescent()) / 2);
    }
  }

  @Override
  public Insets getInsets() {
    return new Insets(0, 0, 0, 0);
  }
}
