package com.antdesign.swing.model;

import lombok.Getter;

/**
 * Ant Design 组件尺寸。
 *
 * <p>三档尺寸对应 Ant Design 的 {@code controlHeight} / {@code controlHeightLG} /
 * {@code controlHeightSM} 设计令牌，每档预置默认的高度、字号、圆角和内边距。
 * 实际渲染值可被主题令牌覆盖。
 */
@Getter
public enum ComponentSize {

  LARGE(40, 16, 8, 16),
  MIDDLE(32, 14, 6, 12),
  SMALL(24, 12, 4, 8);

  /** 默认控件高度(px)。 */
  private final int defaultHeight;

  /** 默认字号(px)。 */
  private final int defaultFontSize;

  /** 默认圆角半径(px)。 */
  private final int defaultBorderRadius;

  /** 默认水平内边距(px)。 */
  private final int defaultPadding;

  ComponentSize(int defaultHeight, int defaultFontSize,
      int defaultBorderRadius, int defaultPadding) {
    this.defaultHeight = defaultHeight;
    this.defaultFontSize = defaultFontSize;
    this.defaultBorderRadius = defaultBorderRadius;
    this.defaultPadding = defaultPadding;
  }
}
