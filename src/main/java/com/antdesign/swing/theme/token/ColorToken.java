package com.antdesign.swing.theme.token;

import lombok.Builder;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 颜色令牌。
 *
 * <p>封装 Ant Design 设计系统中的全部语义化颜色：品牌主色、功能色（成功/警告/错误/信息）、
 * 中性文本色、边框色、填充色、背景色、禁用色以及链接色。
 *
 * <p>颜色值通常由 {@link com.antdesign.swing.theme.ThemeAlgorithm} 根据主题 JSON
 * 配置文件计算生成，也可通过 {@link Builder} 手工构建。
 *
 * @see GlobalToken
 * @see com.antdesign.swing.theme.ThemeAlgorithm
 */
@Getter
@Builder(toBuilder = true)
public class ColorToken {

  // ---------------------------------------------------------------------------
  // 品牌主色
  // ---------------------------------------------------------------------------

  /** 品牌主色。 */
  @Builder.Default
  private final Color primaryColor = new Color(0x16, 0x77, 0xFF);

  /** 主色悬停态。 */
  @Builder.Default
  private final Color primaryHoverColor = new Color(0x40, 0x96, 0xFF);

  /** 主色激活态。 */
  @Builder.Default
  private final Color primaryActiveColor = new Color(0x09, 0x58, 0xD9);

  /** 主色浅背景。 */
  @Builder.Default
  private final Color primaryBgColor = new Color(0xE6, 0xF4, 0xFF);

  // ---------------------------------------------------------------------------
  // 功能色
  // ---------------------------------------------------------------------------

  /** 成功色。 */
  @Builder.Default
  private final Color successColor = new Color(0x52, 0xC4, 0x1A);

  /** 成功色浅背景。 */
  @Builder.Default
  private final Color successBgColor = new Color(0xF6, 0xFF, 0xED);

  /** 警告色。 */
  @Builder.Default
  private final Color warningColor = new Color(0xFA, 0xAD, 0x14);

  /** 警告色浅背景。 */
  @Builder.Default
  private final Color warningBgColor = new Color(0xFF, 0xFB, 0xE6);

  /** 错误色。 */
  @Builder.Default
  private final Color errorColor = new Color(0xFF, 0x4D, 0x4F);

  /** 错误色浅背景。 */
  @Builder.Default
  private final Color errorBgColor = new Color(0xFF, 0xF2, 0xF0);

  /** 信息色。 */
  @Builder.Default
  private final Color infoColor = new Color(0x16, 0x77, 0xFF);

  /** 信息色浅背景。 */
  @Builder.Default
  private final Color infoBgColor = new Color(0xE6, 0xF4, 0xFF);

  // ---------------------------------------------------------------------------
  // 中性色 — 文本
  // ---------------------------------------------------------------------------

  /** 主文本色。 */
  @Builder.Default
  private final Color textColor = new Color(0x0A, 0x0A, 0x0A);

  /** 次要文本色。 */
  @Builder.Default
  private final Color textSecondaryColor = new Color(0x00, 0x00, 0x00, 0xA6);

  /** 三级文本色。 */
  @Builder.Default
  private final Color textTertiaryColor = new Color(0x00, 0x00, 0x00, 0x73);

  /** 四级文本色（占位符等）。 */
  @Builder.Default
  private final Color textQuaternaryColor = new Color(0x00, 0x00, 0x00, 0x40);

  // ---------------------------------------------------------------------------
  // 中性色 — 边框 / 填充 / 背景
  // ---------------------------------------------------------------------------

  /** 边框色。 */
  @Builder.Default
  private final Color borderColor = new Color(0xD9, 0xD9, 0xD9);

  /** 次要边框色（分割线等）。 */
  @Builder.Default
  private final Color borderSecondaryColor = new Color(0xF0, 0xF0, 0xF0);

  /** 填充色。 */
  @Builder.Default
  private final Color fillColor = new Color(0x00, 0x00, 0x00, 0x26);

  /** 次要填充色。 */
  @Builder.Default
  private final Color fillSecondaryColor = new Color(0x00, 0x00, 0x00, 0x0F);

  /** 容器背景色。 */
  @Builder.Default
  private final Color bgContainer = Color.WHITE;

  /** 浮层背景色。 */
  @Builder.Default
  private final Color bgElevated = Color.WHITE;

  /** 布局背景色。 */
  @Builder.Default
  private final Color bgLayout = new Color(0xF5, 0xF5, 0xF5);

  /** 聚光背景色（Tooltip 等）。 */
  @Builder.Default
  private final Color bgSpotlight = new Color(0x00, 0x00, 0x00, 0xD9);

  // ---------------------------------------------------------------------------
  // 禁用 / 链接
  // ---------------------------------------------------------------------------

  /** 禁用前景色。 */
  @Builder.Default
  private final Color disabledColor = new Color(0x00, 0x00, 0x00, 0x40);

  /** 禁用背景色。 */
  @Builder.Default
  private final Color disabledBgColor = new Color(0x00, 0x00, 0x00, 0x0A);

  /** 链接色。 */
  @Builder.Default
  private final Color linkColor = new Color(0x16, 0x77, 0xFF);

  /** 链接悬停色。 */
  @Builder.Default
  private final Color linkHoverColor = new Color(0x69, 0xB1, 0xFF);

  // ---------------------------------------------------------------------------
  // 工具方法
  // ---------------------------------------------------------------------------

  /**
   * 将十六进制颜色字符串解析为 {@link Color}。
   *
   * <p>支持 {@code #RRGGBB} 和 {@code #RRGGBBAA} 两种格式。
   *
   * @param hex 十六进制颜色字符串，如 {@code "#1677FF"} 或 {@code "#000000E0"}
   * @return 解析后的颜色
   * @throws IllegalArgumentException 如果格式不合法
   */
  public static Color parseHexColor(String hex) {
    if (hex == null || hex.isEmpty()) {
      throw new IllegalArgumentException("Color hex string must not be null or empty");
    }
    String stripped = hex.startsWith("#") ? hex.substring(1) : hex;
    if (stripped.length() == 6) {
      return new Color(
          Integer.parseInt(stripped.substring(0, 2), 16),
          Integer.parseInt(stripped.substring(2, 4), 16),
          Integer.parseInt(stripped.substring(4, 6), 16));
    } else if (stripped.length() == 8) {
      return new Color(
          Integer.parseInt(stripped.substring(0, 2), 16),
          Integer.parseInt(stripped.substring(2, 4), 16),
          Integer.parseInt(stripped.substring(4, 6), 16),
          Integer.parseInt(stripped.substring(6, 8), 16));
    }
    throw new IllegalArgumentException("Invalid color hex: " + hex);
  }
}
