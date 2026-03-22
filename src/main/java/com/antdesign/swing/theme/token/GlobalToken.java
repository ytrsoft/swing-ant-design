package com.antdesign.swing.theme.token;

import lombok.Builder;
import lombok.Getter;

/**
 * Ant Design 全局令牌。
 *
 * <p>聚合 {@link ColorToken}、{@link FontToken} 和 {@link SizeToken}，
 * 构成完整的设计令牌集合。每个 {@link com.antdesign.swing.theme.AntTheme} 实例
 * 持有一份 {@code GlobalToken}，组件通过主题管理器获取当前令牌以完成渲染。
 *
 * @see ColorToken
 * @see FontToken
 * @see SizeToken
 */
@Getter
@Builder(toBuilder = true)
public class GlobalToken {

  /** 颜色令牌。 */
  @Builder.Default
  private final ColorToken colorToken = ColorToken.builder().build();

  /** 字体令牌。 */
  @Builder.Default
  private final FontToken fontToken = FontToken.builder().build();

  /** 尺寸令牌。 */
  @Builder.Default
  private final SizeToken sizeToken = SizeToken.builder().build();

  /**
   * 创建使用全部默认值的全局令牌。
   *
   * @return 默认全局令牌实例
   */
  public static GlobalToken defaultToken() {
    return GlobalToken.builder().build();
  }

  /**
   * 基于当前令牌覆盖颜色令牌，返回新实例。
   *
   * @param colorToken 新颜色令牌
   * @return 包含新颜色令牌的全局令牌
   */
  public GlobalToken withColorToken(ColorToken colorToken) {
    return this.toBuilder().colorToken(colorToken).build();
  }

  /**
   * 基于当前令牌覆盖字体令牌，返回新实例。
   *
   * @param fontToken 新字体令牌
   * @return 包含新字体令牌的全局令牌
   */
  public GlobalToken withFontToken(FontToken fontToken) {
    return this.toBuilder().fontToken(fontToken).build();
  }

  /**
   * 基于当前令牌覆盖尺寸令牌，返回新实例。
   *
   * @param sizeToken 新尺寸令牌
   * @return 包含新尺寸令牌的全局令牌
   */
  public GlobalToken withSizeToken(SizeToken sizeToken) {
    return this.toBuilder().sizeToken(sizeToken).build();
  }
}
