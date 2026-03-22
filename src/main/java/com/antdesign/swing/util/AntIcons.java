package com.antdesign.swing.util;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 图标便捷入口。
 *
 * <p>对 {@link SvgIconLoader} 的高层封装，提供简洁的 API 按名称获取图标。
 * 所有方法内部均委托给 {@link SvgIconLoader}，共享其缓存。
 *
 * <pre>{@code
 * // outlined 风格，16px，黑色
 * ImageIcon icon = AntIcons.outlined("search", 16);
 *
 * // filled 风格，24px，主色
 * ImageIcon icon = AntIcons.filled("heart", 24, AntColor.PRIMARY);
 * }</pre>
 */
public final class AntIcons {

  /** 默认图标尺寸 (px)。 */
  public static final int DEFAULT_SIZE = 16;

  private AntIcons() {}

  // ---------------------------------------------------------------------------
  // Outlined
  // ---------------------------------------------------------------------------

  /**
   * 获取 outlined 风格图标（默认 16px，黑色）。
   *
   * @param name 图标名称
   * @return ImageIcon
   */
  public static ImageIcon outlined(String name) {
    return SvgIconLoader.load("outlined", name, DEFAULT_SIZE, null);
  }

  /**
   * 获取指定尺寸的 outlined 风格图标（黑色）。
   *
   * @param name 图标名称
   * @param size 尺寸 (px)
   * @return ImageIcon
   */
  public static ImageIcon outlined(String name, int size) {
    return SvgIconLoader.load("outlined", name, size, null);
  }

  /**
   * 获取指定尺寸和颜色的 outlined 风格图标。
   *
   * @param name  图标名称
   * @param size  尺寸 (px)
   * @param color 着色颜色
   * @return ImageIcon
   */
  public static ImageIcon outlined(String name, int size, Color color) {
    return SvgIconLoader.load("outlined", name, size, color);
  }

  // ---------------------------------------------------------------------------
  // Filled
  // ---------------------------------------------------------------------------

  /**
   * 获取 filled 风格图标（默认 16px，黑色）。
   *
   * @param name 图标名称
   * @return ImageIcon
   */
  public static ImageIcon filled(String name) {
    return SvgIconLoader.load("filled", name, DEFAULT_SIZE, null);
  }

  /**
   * 获取指定尺寸的 filled 风格图标（黑色）。
   *
   * @param name 图标名称
   * @param size 尺寸 (px)
   * @return ImageIcon
   */
  public static ImageIcon filled(String name, int size) {
    return SvgIconLoader.load("filled", name, size, null);
  }

  /**
   * 获取指定尺寸和颜色的 filled 风格图标。
   *
   * @param name  图标名称
   * @param size  尺寸 (px)
   * @param color 着色颜色
   * @return ImageIcon
   */
  public static ImageIcon filled(String name, int size, Color color) {
    return SvgIconLoader.load("filled", name, size, color);
  }

  // ---------------------------------------------------------------------------
  // 通用
  // ---------------------------------------------------------------------------

  /**
   * 按风格名获取图标。
   *
   * @param style 风格：{@code "outlined"} 或 {@code "filled"}
   * @param name  图标名称
   * @param size  尺寸 (px)
   * @param color 着色颜色
   * @return ImageIcon
   */
  public static ImageIcon get(String style, String name, int size, Color color) {
    return SvgIconLoader.load(style, name, size, color);
  }

  /**
   * 清空图标缓存。主题切换时应调用。
   */
  public static void clearCache() {
    SvgIconLoader.clearCache();
  }
}
