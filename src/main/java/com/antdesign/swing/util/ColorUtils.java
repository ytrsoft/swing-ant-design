package com.antdesign.swing.util;

import java.awt.*;

/**
 * 颜色工具类。
 *
 * <p>提供 Hex 解析、颜色混合、明暗调节、透明度设置等静态方法，
 * 作为整个组件库中最底层的颜色操作工具。
 */
public final class ColorUtils {

  private ColorUtils() {}

  /**
   * 解析 Hex 颜色字符串为 {@link Color}。
   *
   * <p>支持格式：{@code #RGB}、{@code #RRGGBB}、{@code #AARRGGBB}，前缀 {@code #} 可省略。
   *
   * @param hex 颜色字符串
   * @return 解析后的 Color 对象
   * @throws IllegalArgumentException 格式不合法时抛出
   */
  public static Color parseHex(String hex) {
    if (hex == null) {
      throw new IllegalArgumentException("hex must not be null");
    }
    String s = hex.startsWith("#") ? hex.substring(1) : hex;
    switch (s.length()) {
      case 3:
        // #RGB → #RRGGBB
        int r3 = Integer.parseInt(s.substring(0, 1), 16);
        int g3 = Integer.parseInt(s.substring(1, 2), 16);
        int b3 = Integer.parseInt(s.substring(2, 3), 16);
        return new Color(r3 << 4 | r3, g3 << 4 | g3, b3 << 4 | b3);
      case 6:
        return new Color(Integer.parseInt(s, 16));
      case 8:
        long argb = Long.parseLong(s, 16);
        return new Color(
            (int) ((argb >> 16) & 0xFF),
            (int) ((argb >> 8) & 0xFF),
            (int) (argb & 0xFF),
            (int) ((argb >> 24) & 0xFF));
      default:
        throw new IllegalArgumentException("Invalid hex color: " + hex);
    }
  }

  /**
   * 将 {@link Color} 转为 {@code #RRGGBB} 格式字符串。不含透明度。
   *
   * @param color 颜色
   * @return Hex 字符串
   */
  public static String toHex(Color color) {
    return String.format("#%02X%02X%02X",
        color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * 将 {@link Color} 转为 {@code #AARRGGBB} 格式字符串（含透明度）。
   *
   * @param color 颜色
   * @return Hex 字符串
   */
  public static String toHexWithAlpha(Color color) {
    return String.format("#%02X%02X%02X%02X",
        color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * 设置颜色的透明度。
   *
   * @param color 原始颜色
   * @param alpha 透明度，0.0（全透明）~ 1.0（不透明）
   * @return 新颜色
   */
  public static Color withAlpha(Color color, float alpha) {
    int a = Math.round(clamp01(alpha) * 255);
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
  }

  /**
   * 将两种颜色按比例混合。
   *
   * @param c1 颜色 1
   * @param c2 颜色 2
   * @param ratio 混合比例，0.0 = 全 c1，1.0 = 全 c2
   * @return 混合后的颜色
   */
  public static Color mix(Color c1, Color c2, float ratio) {
    float r = clamp01(ratio);
    float inv = 1f - r;
    return new Color(
        clamp255(Math.round(c1.getRed() * inv + c2.getRed() * r)),
        clamp255(Math.round(c1.getGreen() * inv + c2.getGreen() * r)),
        clamp255(Math.round(c1.getBlue() * inv + c2.getBlue() * r)),
        clamp255(Math.round(c1.getAlpha() * inv + c2.getAlpha() * r)));
  }

  /**
   * 使颜色变亮（与白色混合）。
   *
   * @param color 原始颜色
   * @param amount 变亮程度，0.0 = 不变，1.0 = 纯白
   * @return 变亮后的颜色
   */
  public static Color lighten(Color color, float amount) {
    return mix(color, Color.WHITE, amount);
  }

  /**
   * 使颜色变暗（与黑色混合）。
   *
   * @param color 原始颜色
   * @param amount 变暗程度，0.0 = 不变，1.0 = 纯黑
   * @return 变暗后的颜色
   */
  public static Color darken(Color color, float amount) {
    return mix(color, Color.BLACK, amount);
  }

  /**
   * 判断颜色是否为浅色。基于相对亮度公式 (ITU-R BT.709)。
   *
   * @param color 颜色
   * @return 浅色返回 {@code true}
   */
  public static boolean isLight(Color color) {
    return luminance(color) > 0.5;
  }

  /**
   * 判断颜色是否为深色。
   *
   * @param color 颜色
   * @return 深色返回 {@code true}
   */
  public static boolean isDark(Color color) {
    return !isLight(color);
  }

  /**
   * 计算颜色的相对亮度 (0.0 ~ 1.0)。
   *
   * @param color 颜色
   * @return 亮度值
   */
  public static double luminance(Color color) {
    double r = color.getRed() / 255.0;
    double g = color.getGreen() / 255.0;
    double b = color.getBlue() / 255.0;
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  /**
   * 根据背景色自动选择前景色（黑或白）。
   *
   * @param background 背景色
   * @return 深色背景返回白色，浅色背景返回黑色
   */
  public static Color contrastForeground(Color background) {
    return isLight(background)
        ? new Color(0, 0, 0, 228)   // rgba(0,0,0,0.88)
        : new Color(255, 255, 255, 228);
  }

  /**
   * 从 {@code rgba(r, g, b, a)} 格式字符串解析颜色。
   *
   * @param rgba 如 {@code rgba(0, 0, 0, 0.88)}
   * @return 解析后的颜色
   * @throws IllegalArgumentException 格式不合法时抛出
   */
  public static Color parseRgba(String rgba) {
    if (rgba == null || !rgba.startsWith("rgba(") || !rgba.endsWith(")")) {
      throw new IllegalArgumentException("Invalid rgba format: " + rgba);
    }
    String inner = rgba.substring(5, rgba.length() - 1);
    String[] parts = inner.split(",");
    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid rgba format: " + rgba);
    }
    int r = Integer.parseInt(parts[0].trim());
    int g = Integer.parseInt(parts[1].trim());
    int b = Integer.parseInt(parts[2].trim());
    float a = Float.parseFloat(parts[3].trim());
    return new Color(r, g, b, Math.round(a * 255));
  }

  private static float clamp01(float value) {
    return Math.max(0f, Math.min(1f, value));
  }

  private static int clamp255(int value) {
    return Math.max(0, Math.min(255, value));
  }
}
