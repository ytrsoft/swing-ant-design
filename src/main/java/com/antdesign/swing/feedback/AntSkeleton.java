package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 骨架屏组件。
 *
 * <p>对应 Ant Design {@code <Skeleton>}，在数据加载完成前展示占位图形，
 * 减少用户等待焦虑。支持头像、标题、段落三种元素组合，以及动画闪烁效果。
 *
 * <pre>{@code
 * // 默认骨架屏（标题 + 3 行段落）
 * AntSkeleton skeleton = new AntSkeleton();
 *
 * // 带头像
 * AntSkeleton skeleton = new AntSkeleton();
 * skeleton.setAvatar(true);
 *
 * // 加载完毕后隐藏
 * skeleton.setActive(false);
 * skeleton.setVisible(false);
 * }</pre>
 */
public class AntSkeleton extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  private static final int AVATAR_SIZE = 40;
  private static final int LINE_HEIGHT = 16;
  private static final int LINE_GAP = 12;
  private static final int TITLE_HEIGHT = 20;

  @Getter private boolean active;
  @Getter private boolean avatar;
  @Getter private boolean titleVisible;
  @Getter private int rows;

  private float shimmerOffset;
  private Timer animTimer;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建默认骨架屏。 */
  public AntSkeleton() {
    this.active = true;
    this.avatar = false;
    this.titleVisible = true;
    this.rows = 3;

    startAnimation();

    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  /** 设置是否启用闪烁动画。 */
  public void setActive(boolean active) {
    this.active = active;
    if (active) {
      startAnimation();
    } else {
      stopAnimation();
    }
    repaint();
  }

  /** 设置是否显示头像占位。 */
  public void setAvatar(boolean avatar) {
    this.avatar = avatar;
    revalidate();
    repaint();
  }

  /** 设置是否显示标题占位。 */
  public void setTitleVisible(boolean titleVisible) {
    this.titleVisible = titleVisible;
    revalidate();
    repaint();
  }

  /** 设置段落行数。 */
  public void setRows(int rows) {
    this.rows = Math.max(0, rows);
    revalidate();
    repaint();
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }

    int contentHeight = 0;
    if (titleVisible) {
      contentHeight += TITLE_HEIGHT + LINE_GAP;
    }
    contentHeight += rows * (LINE_HEIGHT + LINE_GAP);

    int h = Math.max(contentHeight, avatar ? AVATAR_SIZE : 0) + 16;
    return new Dimension(400, h);
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {

    int x = 8;
    int y = 8;
    int availWidth = width - 16;

    // 头像
    if (avatar) {
      drawBlock(g2, x, y, AVATAR_SIZE, AVATAR_SIZE, AVATAR_SIZE / 2);
      x += AVATAR_SIZE + 16;
      availWidth -= AVATAR_SIZE + 16;
    }

    // 标题
    if (titleVisible) {
      drawBlock(g2, x, y, (int) (availWidth * 0.4), TITLE_HEIGHT, 4);
      y += TITLE_HEIGHT + LINE_GAP;
    }

    // 段落行
    for (int i = 0; i < rows; i++) {
      double widthRatio = (i == rows - 1) ? 0.6 : 0.95;
      int lineWidth = (int) (availWidth * widthRatio);
      drawBlock(g2, x, y, lineWidth, LINE_HEIGHT, 4);
      y += LINE_HEIGHT + LINE_GAP;
    }
  }

  private void drawBlock(Graphics2D g2, int x, int y, int w, int h, int arc) {
    ColorToken ct = colorToken();
    Color baseColor = ct.getBorderSecondaryColor();

    if (active) {
      // 渐变闪烁
      Color lighter = new Color(
          Math.min(255, baseColor.getRed() + 20),
          Math.min(255, baseColor.getGreen() + 20),
          Math.min(255, baseColor.getBlue() + 20));

      float startX = x + w * (shimmerOffset - 0.3f);
      float endX = x + w * (shimmerOffset + 0.3f);

      LinearGradientPaint gradient = new LinearGradientPaint(
          startX, y, endX, y,
          new float[]{0f, 0.5f, 1f},
          new Color[]{baseColor, lighter, baseColor});
      g2.setPaint(gradient);
    } else {
      g2.setColor(baseColor);
    }

    g2.fillRoundRect(x, y, w, h, arc, arc);
  }

  // =========================================================================
  // 动画
  // =========================================================================

  private void startAnimation() {
    if (animTimer != null && animTimer.isRunning()) {
      return;
    }
    animTimer = new Timer(30, e -> {
      shimmerOffset += 0.02f;
      if (shimmerOffset > 1.3f) {
        shimmerOffset = -0.3f;
      }
      repaint();
    });
    animTimer.start();
  }

  private void stopAnimation() {
    if (animTimer != null) {
      animTimer.stop();
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    stopAnimation();
  }
}
