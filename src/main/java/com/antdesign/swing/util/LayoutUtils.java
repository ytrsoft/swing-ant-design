package com.antdesign.swing.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 布局辅助工具类。
 *
 * <p>提供 {@link Insets} 运算、快捷 Border 创建、居中定位、
 * 水平/垂直盒子布局等高频布局操作。
 */
public final class LayoutUtils {

  private LayoutUtils() {}

  // ---------------------------------------------------------------------------
  // Insets 运算
  // ---------------------------------------------------------------------------

  /**
   * 将两组 {@link Insets} 相加。
   *
   * @param a 第一组
   * @param b 第二组
   * @return 各方向值相加后的新 Insets
   */
  public static Insets addInsets(Insets a, Insets b) {
    return new Insets(
        a.top + b.top, a.left + b.left,
        a.bottom + b.bottom, a.right + b.right);
  }

  /**
   * 将 {@link Insets} 各方向按比例缩放。
   *
   * @param insets 原始 Insets
   * @param scale  缩放比例
   * @return 缩放后的新 Insets
   */
  public static Insets scaleInsets(Insets insets, float scale) {
    return new Insets(
        Math.round(insets.top * scale), Math.round(insets.left * scale),
        Math.round(insets.bottom * scale), Math.round(insets.right * scale));
  }

  /**
   * 创建四边等距的 {@link Insets}。
   *
   * @param size 各方向值
   * @return 新 Insets
   */
  public static Insets uniformInsets(int size) {
    return new Insets(size, size, size, size);
  }

  // ---------------------------------------------------------------------------
  // Border 快捷创建
  // ---------------------------------------------------------------------------

  /**
   * 创建四边等距的空白 {@link EmptyBorder}。
   *
   * @param padding 各方向内边距 (px)
   * @return EmptyBorder 实例
   */
  public static Border emptyBorder(int padding) {
    return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
  }

  /**
   * 创建指定上下、左右内边距的空白 Border。
   *
   * @param vertical   上下内边距 (px)
   * @param horizontal 左右内边距 (px)
   * @return EmptyBorder 实例
   */
  public static Border emptyBorder(int vertical, int horizontal) {
    return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
  }

  /**
   * 创建四边分别指定的空白 Border。
   *
   * @param top    上
   * @param left   左
   * @param bottom 下
   * @param right  右
   * @return EmptyBorder 实例
   */
  public static Border emptyBorder(int top, int left, int bottom, int right) {
    return BorderFactory.createEmptyBorder(top, left, bottom, right);
  }

  // ---------------------------------------------------------------------------
  // 定位
  // ---------------------------------------------------------------------------

  /**
   * 将窗口居中到屏幕。
   *
   * @param window 目标窗口
   */
  public static void centerOnScreen(Window window) {
    window.setLocationRelativeTo(null);
  }

  /**
   * 将组件居中到父容器。
   *
   * @param child  子组件
   * @param parent 父容器
   */
  public static void centerInParent(Component child, Container parent) {
    Dimension parentSize = parent.getSize();
    Dimension childSize = child.getPreferredSize();
    int x = Math.max(0, (parentSize.width - childSize.width) / 2);
    int y = Math.max(0, (parentSize.height - childSize.height) / 2);
    child.setBounds(x, y, childSize.width, childSize.height);
  }

  // ---------------------------------------------------------------------------
  // 盒子布局快捷方法
  // ---------------------------------------------------------------------------

  /**
   * 创建水平排列的面板，子组件之间插入指定间距。
   *
   * @param gap        子组件间距 (px)
   * @param components 子组件列表
   * @return 水平 BoxLayout 面板
   */
  public static JPanel horizontalBox(int gap, JComponent... components) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setOpaque(false);
    for (int i = 0; i < components.length; i++) {
      if (i > 0 && gap > 0) {
        panel.add(Box.createRigidArea(new Dimension(gap, 0)));
      }
      panel.add(components[i]);
    }
    return panel;
  }

  /**
   * 创建垂直排列的面板，子组件之间插入指定间距。
   *
   * @param gap        子组件间距 (px)
   * @param components 子组件列表
   * @return 垂直 BoxLayout 面板
   */
  public static JPanel verticalBox(int gap, JComponent... components) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    for (int i = 0; i < components.length; i++) {
      if (i > 0 && gap > 0) {
        panel.add(Box.createRigidArea(new Dimension(0, gap)));
      }
      panel.add(components[i]);
    }
    return panel;
  }

  /**
   * 设置组件的固定尺寸（同时设置 preferred / minimum / maximum）。
   *
   * @param component 组件
   * @param width     宽度
   * @param height    高度
   */
  public static void setFixedSize(JComponent component, int width, int height) {
    Dimension d = new Dimension(width, height);
    component.setPreferredSize(d);
    component.setMinimumSize(d);
    component.setMaximumSize(d);
  }
}
