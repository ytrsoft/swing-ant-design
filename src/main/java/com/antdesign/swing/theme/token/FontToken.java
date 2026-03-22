package com.antdesign.swing.theme.token;

import lombok.Builder;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 字体令牌。
 *
 * <p>定义 Ant Design 设计系统中的字体族、各级字号以及行高比例。组件在渲染文本时
 * 应引用此令牌，而非硬编码字体参数，以保证整体排版一致性。
 *
 * @see GlobalToken
 */
@Getter
@Builder(toBuilder = true)
public class FontToken {

  /** 默认字体族名称。 */
  public static final String DEFAULT_FONT_FAMILY = "PingFang SC";

  /** 西文后备字体族。 */
  public static final String FALLBACK_FONT_FAMILY = "Helvetica Neue";

  // ---------------------------------------------------------------------------
  // 字体族
  // ---------------------------------------------------------------------------

  /** 正文字体族。 */
  @Builder.Default
  private final String fontFamily = DEFAULT_FONT_FAMILY;

  /** 代码字体族。 */
  @Builder.Default
  private final String fontFamilyCode = "SFMono-Regular";

  // ---------------------------------------------------------------------------
  // 字号
  // ---------------------------------------------------------------------------

  /** 极小字号(px)。 */
  @Builder.Default
  private final int fontSizeSm = 12;

  /** 基础字号(px)。 */
  @Builder.Default
  private final int fontSize = 14;

  /** 大字号(px)。 */
  @Builder.Default
  private final int fontSizeLg = 16;

  /** 一级标题字号(px)。 */
  @Builder.Default
  private final int fontSizeHeading1 = 38;

  /** 二级标题字号(px)。 */
  @Builder.Default
  private final int fontSizeHeading2 = 30;

  /** 三级标题字号(px)。 */
  @Builder.Default
  private final int fontSizeHeading3 = 24;

  /** 四级标题字号(px)。 */
  @Builder.Default
  private final int fontSizeHeading4 = 20;

  /** 五级标题字号(px)。 */
  @Builder.Default
  private final int fontSizeHeading5 = 16;

  // ---------------------------------------------------------------------------
  // 行高
  // ---------------------------------------------------------------------------

  /** 基础行高倍数。 */
  @Builder.Default
  private final double lineHeight = 1.5714;

  /** 大文本行高倍数。 */
  @Builder.Default
  private final double lineHeightLg = 1.5;

  /** 小文本行高倍数。 */
  @Builder.Default
  private final double lineHeightSm = 1.6667;

  /** 标题行高倍数。 */
  @Builder.Default
  private final double lineHeightHeading = 1.2105;

  // ---------------------------------------------------------------------------
  // 字重
  // ---------------------------------------------------------------------------

  /** 正常字重。 */
  @Builder.Default
  private final int fontWeightNormal = Font.PLAIN;

  /** 加粗字重。 */
  @Builder.Default
  private final int fontWeightBold = Font.BOLD;

  // ---------------------------------------------------------------------------
  // 便捷方法
  // ---------------------------------------------------------------------------

  /**
   * 创建基础字号的普通字体。
   *
   * @return 基础正文字体
   */
  public Font createBaseFont() {
    return new Font(fontFamily, fontWeightNormal, fontSize);
  }

  /**
   * 创建指定字号和字重的字体。
   *
   * @param size 字号
   * @param style {@link Font#PLAIN}、{@link Font#BOLD} 等
   * @return 新字体实例
   */
  public Font createFont(int size, int style) {
    return new Font(fontFamily, style, size);
  }

  /**
   * 根据字号计算行高像素值。
   *
   * @param fontSizePx 字号（px）
   * @return 行高（px），向上取整
   */
  public int computeLineHeightPx(int fontSizePx) {
    return (int) Math.ceil(fontSizePx * lineHeight);
  }
}
