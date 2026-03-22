package com.antdesign.swing.util;

import com.antdesign.swing.painter.RoundedBorderPainter;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Ant Design 边框工具类。
 *
 * <p>提供主题感知的静态工厂方法，用于快速创建符合 Ant Design 规范的 Swing {@link Border}。
 * 所有无显式颜色参数的方法均从当前主题令牌取值，确保边框样式随主题切换自动适配。
 *
 * <h3>常用示例</h3>
 * <pre>{@code
 * // 主题色线框
 * panel.setBorder(AntBorder.line());
 *
 * // 圆角边框
 * panel.setBorder(AntBorder.rounded());
 *
 * // 底部分割线 + 内边距（Header 场景）
 * header.setBorder(AntBorder.bottomWithPadding(pad, pad, pad, pad));
 *
 * // 顶部分割线 + 内边距（Footer 场景）
 * footer.setBorder(AntBorder.topWithPadding(padSm, padLg, padSm, padLg));
 *
 * // 主题令牌内边距
 * body.setBorder(AntBorder.padding());
 * }</pre>
 *
 * @see com.antdesign.swing.painter.RoundedBorderPainter
 * @see ColorToken
 * @see SizeToken
 */
public final class AntBorder {

  private AntBorder() {}

  // ---------------------------------------------------------------------------
  // 主题令牌访问
  // ---------------------------------------------------------------------------

  private static ColorToken colorToken() {
    return AntThemeManager.getInstance().getColorToken();
  }

  private static SizeToken sizeToken() {
    return AntThemeManager.getInstance().getSizeToken();
  }

  // ---------------------------------------------------------------------------
  // 直线边框
  // ---------------------------------------------------------------------------

  /**
   * 使用主题 {@code borderColor} 创建 1px 线框。
   *
   * @return 线框 Border
   */
  public static Border line() {
    return BorderFactory.createLineBorder(colorToken().getBorderColor());
  }

  /**
   * 使用指定颜色创建 1px 线框。
   *
   * @param color 边框颜色
   * @return 线框 Border
   */
  public static Border line(Color color) {
    return BorderFactory.createLineBorder(color);
  }

  /**
   * 使用指定颜色和宽度创建线框。
   *
   * @param color     边框颜色
   * @param thickness 边框宽度 (px)
   * @return 线框 Border
   */
  public static Border line(Color color, int thickness) {
    return BorderFactory.createLineBorder(color, thickness);
  }

  // ---------------------------------------------------------------------------
  // 圆角边框
  // ---------------------------------------------------------------------------

  /**
   * 使用主题默认参数创建圆角边框。
   *
   * @return 圆角 Border
   */
  public static Border rounded() {
    return new RoundedBorderPainter();
  }

  /**
   * 使用指定颜色创建圆角边框（圆角半径取自主题）。
   *
   * @param color 边框颜色
   * @return 圆角 Border
   */
  public static Border rounded(Color color) {
    return new RoundedBorderPainter(color);
  }

  /**
   * 使用完全自定义参数创建圆角边框。
   *
   * @param color     边框颜色，{@code null} 表示使用主题色
   * @param arcRadius 圆角半径
   * @param lineWidth 线宽
   * @return 圆角 Border
   */
  public static Border rounded(Color color, int arcRadius, float lineWidth) {
    return new RoundedBorderPainter(color, arcRadius, lineWidth);
  }

  // ---------------------------------------------------------------------------
  // 空白（内边距）边框
  // ---------------------------------------------------------------------------

  /**
   * 创建四边等距的空白边框。
   *
   * @param padding 内边距 (px)
   * @return 空白 Border
   */
  public static Border empty(int padding) {
    return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
  }

  /**
   * 创建上下/左右对称的空白边框。
   *
   * @param vertical   上下内边距 (px)
   * @param horizontal 左右内边距 (px)
   * @return 空白 Border
   */
  public static Border empty(int vertical, int horizontal) {
    return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
  }

  /**
   * 创建四边分别指定的空白边框。
   *
   * @param top    上 (px)
   * @param left   左 (px)
   * @param bottom 下 (px)
   * @param right  右 (px)
   * @return 空白 Border
   */
  public static Border empty(int top, int left, int bottom, int right) {
    return BorderFactory.createEmptyBorder(top, left, bottom, right);
  }

  /**
   * 使用主题 {@code padding} 令牌创建等距空白边框（16px）。
   *
   * @return 空白 Border
   */
  public static Border padding() {
    int pad = sizeToken().getPadding();
    return empty(pad);
  }

  /**
   * 使用主题 {@code paddingXs} 令牌创建等距空白边框（8px）。
   *
   * @return 空白 Border
   */
  public static Border paddingXs() {
    int pad = sizeToken().getPaddingXs();
    return empty(pad);
  }

  /**
   * 使用主题 {@code paddingSm} 令牌创建等距空白边框（12px）。
   *
   * @return 空白 Border
   */
  public static Border paddingSm() {
    int pad = sizeToken().getPaddingSm();
    return empty(pad);
  }

  /**
   * 使用主题 {@code paddingLg} 令牌创建等距空白边框（24px）。
   *
   * @return 空白 Border
   */
  public static Border paddingLg() {
    int pad = sizeToken().getPaddingLg();
    return empty(pad);
  }

  /**
   * 使用主题 {@code paddingXl} 令牌创建等距空白边框（32px）。
   *
   * @return 空白 Border
   */
  public static Border paddingXl() {
    int pad = sizeToken().getPaddingXl();
    return empty(pad);
  }

  // ---------------------------------------------------------------------------
  // 单侧分割线（Matte 边框）
  // ---------------------------------------------------------------------------

  /**
   * 使用主题 {@code borderSecondaryColor} 创建顶部 1px 分割线。
   *
   * @return 顶部分割线 Border
   */
  public static Border top() {
    return BorderFactory.createMatteBorder(1, 0, 0, 0,
        colorToken().getBorderSecondaryColor());
  }

  /**
   * 使用指定颜色创建顶部 1px 分割线。
   *
   * @param color 分割线颜色
   * @return 顶部分割线 Border
   */
  public static Border top(Color color) {
    return BorderFactory.createMatteBorder(1, 0, 0, 0, color);
  }

  /**
   * 使用指定宽度和颜色创建顶部分割线。
   *
   * @param thickness 线宽 (px)
   * @param color     分割线颜色
   * @return 顶部分割线 Border
   */
  public static Border top(int thickness, Color color) {
    return BorderFactory.createMatteBorder(thickness, 0, 0, 0, color);
  }

  /**
   * 使用主题 {@code borderSecondaryColor} 创建底部 1px 分割线。
   *
   * @return 底部分割线 Border
   */
  public static Border bottom() {
    return BorderFactory.createMatteBorder(0, 0, 1, 0,
        colorToken().getBorderSecondaryColor());
  }

  /**
   * 使用指定颜色创建底部 1px 分割线。
   *
   * @param color 分割线颜色
   * @return 底部分割线 Border
   */
  public static Border bottom(Color color) {
    return BorderFactory.createMatteBorder(0, 0, 1, 0, color);
  }

  /**
   * 使用指定宽度和颜色创建底部分割线。
   *
   * @param thickness 线宽 (px)
   * @param color     分割线颜色
   * @return 底部分割线 Border
   */
  public static Border bottom(int thickness, Color color) {
    return BorderFactory.createMatteBorder(0, 0, thickness, 0, color);
  }

  /**
   * 使用主题 {@code borderSecondaryColor} 创建左侧 1px 分割线。
   *
   * @return 左侧分割线 Border
   */
  public static Border left() {
    return BorderFactory.createMatteBorder(0, 1, 0, 0,
        colorToken().getBorderSecondaryColor());
  }

  /**
   * 使用指定颜色创建左侧 1px 分割线。
   *
   * @param color 分割线颜色
   * @return 左侧分割线 Border
   */
  public static Border left(Color color) {
    return BorderFactory.createMatteBorder(0, 1, 0, 0, color);
  }

  /**
   * 使用指定宽度和颜色创建左侧分割线。
   *
   * @param thickness 线宽 (px)
   * @param color     分割线颜色
   * @return 左侧分割线 Border
   */
  public static Border left(int thickness, Color color) {
    return BorderFactory.createMatteBorder(0, thickness, 0, 0, color);
  }

  /**
   * 使用主题 {@code borderSecondaryColor} 创建右侧 1px 分割线。
   *
   * @return 右侧分割线 Border
   */
  public static Border right() {
    return BorderFactory.createMatteBorder(0, 0, 0, 1,
        colorToken().getBorderSecondaryColor());
  }

  /**
   * 使用指定颜色创建右侧 1px 分割线。
   *
   * @param color 分割线颜色
   * @return 右侧分割线 Border
   */
  public static Border right(Color color) {
    return BorderFactory.createMatteBorder(0, 0, 0, 1, color);
  }

  /**
   * 使用指定宽度和颜色创建右侧分割线。
   *
   * @param thickness 线宽 (px)
   * @param color     分割线颜色
   * @return 右侧分割线 Border
   */
  public static Border right(int thickness, Color color) {
    return BorderFactory.createMatteBorder(0, 0, 0, thickness, color);
  }

  /**
   * 使用主题 {@code borderSecondaryColor} 创建自定义各边 Matte 边框。
   *
   * @param top    上边宽度 (px)
   * @param left   左边宽度 (px)
   * @param bottom 下边宽度 (px)
   * @param right  右边宽度 (px)
   * @return Matte Border
   */
  public static Border matte(int top, int left, int bottom, int right) {
    return BorderFactory.createMatteBorder(top, left, bottom, right,
        colorToken().getBorderSecondaryColor());
  }

  /**
   * 使用指定颜色创建自定义各边 Matte 边框。
   *
   * @param top    上边宽度 (px)
   * @param left   左边宽度 (px)
   * @param bottom 下边宽度 (px)
   * @param right  右边宽度 (px)
   * @param color  边框颜色
   * @return Matte Border
   */
  public static Border matte(int top, int left, int bottom, int right, Color color) {
    return BorderFactory.createMatteBorder(top, left, bottom, right, color);
  }

  // ---------------------------------------------------------------------------
  // 复合边框
  // ---------------------------------------------------------------------------

  /**
   * 组合两个 Border（外层 + 内层）。
   *
   * @param outer 外层 Border
   * @param inner 内层 Border
   * @return 复合 Border
   */
  public static Border compound(Border outer, Border inner) {
    return BorderFactory.createCompoundBorder(outer, inner);
  }

  /**
   * 底部分割线 + 等距内边距（常用于 Header 面板）。
   *
   * <p>等价于:
   * <pre>{@code
   * BorderFactory.createCompoundBorder(
   *     BorderFactory.createMatteBorder(0, 0, 1, 0, borderSecondaryColor),
   *     BorderFactory.createEmptyBorder(pad, pad, pad, pad));
   * }</pre>
   *
   * @param padding 四边等距内边距 (px)
   * @return 复合 Border
   */
  public static Border bottomWithPadding(int padding) {
    return compound(bottom(), empty(padding));
  }

  /**
   * 底部分割线 + 四边分别指定内边距（常用于 Header 面板）。
   *
   * @param top    上内边距 (px)
   * @param left   左内边距 (px)
   * @param bottom 下内边距 (px)
   * @param right  右内边距 (px)
   * @return 复合 Border
   */
  public static Border bottomWithPadding(int top, int left, int bottom, int right) {
    return compound(bottom(), empty(top, left, bottom, right));
  }

  /**
   * 顶部分割线 + 等距内边距（常用于 Footer 面板）。
   *
   * <p>等价于:
   * <pre>{@code
   * BorderFactory.createCompoundBorder(
   *     BorderFactory.createMatteBorder(1, 0, 0, 0, borderSecondaryColor),
   *     BorderFactory.createEmptyBorder(pad, pad, pad, pad));
   * }</pre>
   *
   * @param padding 四边等距内边距 (px)
   * @return 复合 Border
   */
  public static Border topWithPadding(int padding) {
    return compound(top(), empty(padding));
  }

  /**
   * 顶部分割线 + 四边分别指定内边距（常用于 Footer 面板）。
   *
   * @param top    上内边距 (px)
   * @param left   左内边距 (px)
   * @param bottom 下内边距 (px)
   * @param right  右内边距 (px)
   * @return 复合 Border
   */
  public static Border topWithPadding(int top, int left, int bottom, int right) {
    return compound(top(), empty(top, left, bottom, right));
  }

  /**
   * 线框 + 等距内边距。
   *
   * @param padding 内边距 (px)
   * @return 复合 Border
   */
  public static Border lineWithPadding(int padding) {
    return compound(line(), empty(padding));
  }

  /**
   * 线框 + 四边分别指定内边距。
   *
   * @param top    上内边距 (px)
   * @param left   左内边距 (px)
   * @param bottom 下内边距 (px)
   * @param right  右内边距 (px)
   * @return 复合 Border
   */
  public static Border lineWithPadding(int top, int left, int bottom, int right) {
    return compound(line(), empty(top, left, bottom, right));
  }

  /**
   * 圆角边框 + 等距内边距。
   *
   * @param padding 内边距 (px)
   * @return 复合 Border
   */
  public static Border roundedWithPadding(int padding) {
    return compound(rounded(), empty(padding));
  }

  /**
   * 圆角边框 + 四边分别指定内边距。
   *
   * @param top    上内边距 (px)
   * @param left   左内边距 (px)
   * @param bottom 下内边距 (px)
   * @param right  右内边距 (px)
   * @return 复合 Border
   */
  public static Border roundedWithPadding(int top, int left, int bottom, int right) {
    return compound(rounded(), empty(top, left, bottom, right));
  }
}
