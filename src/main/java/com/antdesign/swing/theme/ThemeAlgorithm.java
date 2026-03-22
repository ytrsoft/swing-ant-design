package com.antdesign.swing.theme;

import com.antdesign.swing.theme.token.GlobalToken;

/**
 * Ant Design 主题算法接口。
 *
 * <p>主题算法负责根据一组种子令牌（或默认值）派生出完整的
 * {@link GlobalToken}。不同算法实现对应不同的视觉风格，例如亮色、暗色和紧凑模式。
 *
 * <p>算法之间可组合使用：先用 {@link DefaultAlgorithm} 或 {@link DarkAlgorithm}
 * 生成基础令牌，再用 {@link CompactAlgorithm} 缩减尺寸。
 *
 * @see DefaultAlgorithm
 * @see DarkAlgorithm
 */
public interface ThemeAlgorithm {

  /**
   * 返回算法的可读名称。
   *
   * @return 算法名称
   */
  String getName();

  /**
   * 当前算法是否为暗色模式。
   *
   * @return 暗色模式返回 {@code true}
   */
  boolean isDark();

  /**
   * 基于给定的种子令牌派生完整的全局令牌。
   *
   * <p>实现类可以忽略 {@code seedToken} 中的部分字段，也可以对其进行
   * 颜色变换或尺寸缩放等处理。
   *
   * @param seedToken 种子令牌，为 {@code null} 时使用算法内置默认值
   * @return 派生后的完整全局令牌
   */
  GlobalToken derive(GlobalToken seedToken);

  /**
   * 使用算法内置默认值生成全局令牌，等价于 {@code derive(null)}。
   *
   * @return 默认全局令牌
   */
  default GlobalToken derive() {
    return derive(null);
  }
}
