package com.antdesign.swing.theme;

import com.antdesign.swing.theme.token.*;
import lombok.Getter;

/**
 * Ant Design 主题配置。
 *
 * <p>持有一个 {@link ThemeAlgorithm} 和由其派生的 {@link GlobalToken}，提供
 * 对颜色、字体、尺寸令牌的便捷访问。主题实例由 {@link AntThemeManager} 管理。
 *
 * <pre>{@code
 * AntTheme theme = AntTheme.of(new DarkAlgorithm());
 * Color primary = theme.getColorToken().getPrimaryColor();
 * }</pre>
 *
 * @see AntThemeManager
 * @see ThemeAlgorithm
 */
@Getter
public class AntTheme {

  /** 当前使用的主题算法。 */
  private final ThemeAlgorithm algorithm;

  /** 算法派生的全局令牌。 */
  private final GlobalToken token;

  /**
   * 使用指定的算法和种子令牌创建主题。
   *
   * @param algorithm 主题算法
   * @param seedToken 种子令牌，可为 {@code null}
   */
  public AntTheme(ThemeAlgorithm algorithm, GlobalToken seedToken) {
    if (algorithm == null) {
      throw new IllegalArgumentException("ThemeAlgorithm must not be null");
    }
    this.algorithm = algorithm;
    this.token = algorithm.derive(seedToken);
  }

  /**
   * 使用指定的算法和默认种子创建主题。
   *
   * @param algorithm 主题算法
   */
  public AntTheme(ThemeAlgorithm algorithm) {
    this(algorithm, null);
  }

  /**
   * 创建使用默认亮色算法的主题。
   *
   * @return 默认主题
   */
  public static AntTheme defaultTheme() {
    return new AntTheme(new DefaultAlgorithm());
  }

  /**
   * 工厂方法：使用指定算法创建主题。
   *
   * @param algorithm 主题算法
   * @return 主题实例
   */
  public static AntTheme of(ThemeAlgorithm algorithm) {
    return new AntTheme(algorithm);
  }

  /**
   * 工厂方法：使用指定算法和种子令牌创建主题。
   *
   * @param algorithm 主题算法
   * @param seedToken 种子令牌
   * @return 主题实例
   */
  public static AntTheme of(ThemeAlgorithm algorithm, GlobalToken seedToken) {
    return new AntTheme(algorithm, seedToken);
  }

  /**
   * 是否为暗色主题。
   *
   * @return 暗色模式返回 {@code true}
   */
  public boolean isDark() {
    return algorithm.isDark();
  }

  /** 便捷方法：获取颜色令牌。 */
  public ColorToken getColorToken() {
    return token.getColorToken();
  }

  /** 便捷方法：获取字体令牌。 */
  public FontToken getFontToken() {
    return token.getFontToken();
  }

  /** 便捷方法：获取尺寸令牌。 */
  public SizeToken getSizeToken() {
    return token.getSizeToken();
  }
}
