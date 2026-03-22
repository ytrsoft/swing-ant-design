package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 时间轴组件。
 *
 * <p>对应 Ant Design {@code <Timeline>}，垂直展示的时间流信息。
 *
 * <pre>{@code
 * AntTimeline timeline = new AntTimeline();
 * timeline.addItem("Create project 2015-09-01");
 * timeline.addItem("Solve initial bugs 2015-09-01", Status.SUCCESS);
 * timeline.addItem("Technical testing 2015-09-01", Status.ERROR);
 * }</pre>
 */
public class AntTimeline extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int DOT_SIZE = 10;
  private static final int LINE_WIDTH = 2;

  /** 时间轴项。 */
  @Getter
  public static class Item {
    private final String content;
    private final String label;
    private final Color color;

    public Item(String content, String label, Color color) {
      this.content = content;
      this.label = label;
      this.color = color;
    }
  }

  @Getter private boolean reverse;
  @Getter private boolean pending;
  private final List<Item> items = new ArrayList<>();

  /** 创建时间轴。 */
  public AntTimeline() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // 数据操作
  // =========================================================================

  /**
   * 添加时间轴项。
   *
   * @param content 内容文本
   */
  public void addItem(String content) {
    items.add(new Item(content, null, null));
    rebuildUi();
  }

  /**
   * 添加带状态色的时间轴项。
   *
   * @param content 内容文本
   * @param status  状态
   */
  public void addItem(String content, Status status) {
    Color color = (status != null) ? status.getDefaultColor() : null;
    items.add(new Item(content, null, color));
    rebuildUi();
  }

  /**
   * 添加带标签和颜色的时间轴项。
   *
   * @param content 内容文本
   * @param label   时间标签
   * @param color   圆点颜色
   */
  public void addItem(String content, String label, Color color) {
    items.add(new Item(content, label, color));
    rebuildUi();
  }

  /** 清空所有项。 */
  public void clearItems() {
    items.clear();
    rebuildUi();
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setReverse(boolean reverse) {
    this.reverse = reverse;
    rebuildUi();
  }

  public void setPending(boolean pending) {
    this.pending = pending;
    rebuildUi();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    List<Item> displayItems = new ArrayList<>(items);
    if (reverse) {
      java.util.Collections.reverse(displayItems);
    }

    for (int i = 0; i < displayItems.size(); i++) {
      Item item = displayItems.get(i);
      boolean isLast = (i == displayItems.size() - 1);

      JPanel row = new JPanel(new BorderLayout(st.getPaddingSm(), 0));
      row.setOpaque(false);
      row.setAlignmentX(LEFT_ALIGNMENT);

      // 左侧（圆点 + 连线）
      TimelineDot leftPanel = new TimelineDot(
          item.color != null ? item.color : ct.getPrimaryColor(),
          ct.getBorderSecondaryColor(), isLast);
      leftPanel.setPreferredSize(new Dimension(DOT_SIZE + 8, 0));
      row.add(leftPanel, BorderLayout.WEST);

      // 右侧内容
      JPanel contentPanel = new JPanel();
      contentPanel.setOpaque(false);
      contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

      if (item.label != null && !item.label.isEmpty()) {
        JLabel labelL = new JLabel(item.label);
        labelL.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
        labelL.setForeground(ct.getTextTertiaryColor());
        labelL.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(labelL);
        contentPanel.add(Box.createVerticalStrut(2));
      }

      JLabel contentLabel = new JLabel(
          "<html><body style='width:300px'>" + item.content + "</body></html>");
      contentLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      contentLabel.setForeground(ct.getTextColor());
      contentLabel.setAlignmentX(LEFT_ALIGNMENT);
      contentPanel.add(contentLabel);

      int bottomPad = isLast ? 0 : st.getPaddingLg();
      contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, bottomPad, 0));
      row.add(contentPanel, BorderLayout.CENTER);

      row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
          row.getPreferredSize().height));
      add(row);
    }

    if (pending) {
      JLabel pendingLabel = new JLabel("...");
      pendingLabel.setForeground(ct.getTextTertiaryColor());
      pendingLabel.setAlignmentX(LEFT_ALIGNMENT);
      pendingLabel.setBorder(BorderFactory.createEmptyBorder(
          0, DOT_SIZE / 2 + 4, 0, 0));
      add(pendingLabel);
    }

    revalidate();
    repaint();
  }

  /** 时间轴圆点（含连接线）面板。 */
  private static class TimelineDot extends AbstractAntComponent {
    private final Color dotColor;
    private final Color lineColor;
    private final boolean last;

    TimelineDot(Color dotColor, Color lineColor, boolean last) {
      this.dotColor = dotColor;
      this.lineColor = lineColor;
      this.last = last;
      }

    @Override
    protected void paintAnt(Graphics2D g2, int width, int height) {
      int cx = DOT_SIZE / 2 + 2;

      // 圆点
      g2.setColor(dotColor);
      g2.fillOval(cx - DOT_SIZE / 2, 2, DOT_SIZE, DOT_SIZE);

      // 连线
      if (!last) {
        g2.setColor(lineColor);
        g2.fillRect(cx - LINE_WIDTH / 2, DOT_SIZE + 4,
            LINE_WIDTH, height - DOT_SIZE - 4);
      }
    }
  }
}
