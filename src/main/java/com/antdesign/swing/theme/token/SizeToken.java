package com.antdesign.swing.theme.token;

import com.antdesign.swing.model.ComponentSize;
import lombok.Builder;
import lombok.Getter;

/**
 * Ant Design 尺寸令牌。
 *
 * <p>涵盖圆角、间距、控件高度、边框宽度和外轮廓宽度等布局度量值。
 * 组件在计算尺寸与间距时应引用此令牌，而非硬编码像素值。
 *
 * @see GlobalToken
 */
@Getter
@Builder(toBuilder = true)
public class SizeToken {

  // ---------------------------------------------------------------------------
  // 圆角
  // ---------------------------------------------------------------------------

  /** 极小圆角(px)。 */
  @Builder.Default
  private final int borderRadiusXs = 2;

  /** 小圆角(px)。 */
  @Builder.Default
  private final int borderRadiusSm = 4;

  /** 基础圆角(px)。 */
  @Builder.Default
  private final int borderRadius = 6;

  /** 大圆角(px)。 */
  @Builder.Default
  private final int borderRadiusLg = 8;

  // ---------------------------------------------------------------------------
  // 间距
  // ---------------------------------------------------------------------------

  /** 极小间距(px)。 */
  @Builder.Default
  private final int paddingXxs = 4;

  /** 小间距(px)。 */
  @Builder.Default
  private final int paddingXs = 8;

  /** 次小间距(px)。 */
  @Builder.Default
  private final int paddingSm = 12;

  /** 基础间距(px)。 */
  @Builder.Default
  private final int padding = 16;

  /** 大间距(px)。 */
  @Builder.Default
  private final int paddingLg = 24;

  /** 极大间距(px)。 */
  @Builder.Default
  private final int paddingXl = 32;

  // ---------------------------------------------------------------------------
  // 外边距
  // ---------------------------------------------------------------------------

  /** 极小外边距(px)。 */
  @Builder.Default
  private final int marginXxs = 4;

  /** 小外边距(px)。 */
  @Builder.Default
  private final int marginXs = 8;

  /** 次小外边距(px)。 */
  @Builder.Default
  private final int marginSm = 12;

  /** 基础外边距(px)。 */
  @Builder.Default
  private final int margin = 16;

  /** 大外边距(px)。 */
  @Builder.Default
  private final int marginLg = 24;

  /** 极大外边距(px)。 */
  @Builder.Default
  private final int marginXl = 32;

  // ---------------------------------------------------------------------------
  // 控件高度
  // ---------------------------------------------------------------------------

  /** 小控件高度(px)。 */
  @Builder.Default
  private final int controlHeightSm = 24;

  /** 基础控件高度(px)。 */
  @Builder.Default
  private final int controlHeight = 32;

  /** 大控件高度(px)。 */
  @Builder.Default
  private final int controlHeightLg = 40;

  // ---------------------------------------------------------------------------
  // 边框与轮廓
  // ---------------------------------------------------------------------------

  /** 边框宽度(px)。 */
  @Builder.Default
  private final int lineWidth = 1;

  /** 粗边框宽度(px)。 */
  @Builder.Default
  private final int lineWidthBold = 2;

  /** 聚焦外轮廓宽度(px)。 */
  @Builder.Default
  private final int controlOutlineWidth = 2;

  // ---------------------------------------------------------------------------
  // 便捷方法
  // ---------------------------------------------------------------------------

  /**
   * 根据 {@link com.antdesign.swing.model.ComponentSize} 返回对应的控件高度。
   *
   * @param size 组件尺寸
   * @return 控件高度(px)
   */
  public int controlHeightOf(ComponentSize size) {
    switch (size) {
      case LARGE:
        return controlHeightLg;
      case SMALL:
        return controlHeightSm;
      default:
        return controlHeight;
    }
  }
}
