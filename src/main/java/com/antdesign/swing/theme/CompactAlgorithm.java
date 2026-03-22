package com.antdesign.swing.theme;

import com.antdesign.swing.theme.DefaultAlgorithm;
import com.antdesign.swing.theme.token.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Ant Design 紧凑主题算法。
 *
 * <p>在保持颜色方案不变的基础上，缩小圆角、间距、控件高度和字号，使界面更加紧凑，
 * 适用于数据密集型场景。颜色令牌从 {@code compact-theme.json} 加载，
 * 尺寸和字体令牌做对应缩减。
 *
 * <p>若需要暗色 + 紧凑的组合效果，可先使用 {@link DarkAlgorithm} 生成基础令牌，
 * 再将其作为种子传入本算法。
 *
 * @see ThemeAlgorithm
 */
@Slf4j
public class CompactAlgorithm implements ThemeAlgorithm {

  private static final String THEME_RESOURCE = "compact-theme.json";

  @Override
  public String getName() {
    return "compact";
  }

  @Override
  public boolean isDark() {
    return false;
  }

  @Override
  public GlobalToken derive(GlobalToken seedToken) {
    ColorToken colorToken;
    if (seedToken != null && seedToken.getColorToken() != null) {
      // 保留种子令牌中的颜色（支持暗色 + 紧凑组合）
      colorToken = seedToken.getColorToken();
    } else {
      colorToken = DefaultAlgorithm.loadColorsFromResource(THEME_RESOURCE);
    }

    FontToken fontToken = buildCompactFontToken(seedToken);
    SizeToken sizeToken = buildCompactSizeToken();

    return GlobalToken.builder()
        .colorToken(colorToken)
        .fontToken(fontToken)
        .sizeToken(sizeToken)
        .build();
  }

  /**
   * 构建紧凑模式的字体令牌：字号整体缩小。
   */
  private FontToken buildCompactFontToken(GlobalToken seedToken) {
    FontToken base = (seedToken != null && seedToken.getFontToken() != null)
        ? seedToken.getFontToken()
        : FontToken.builder().build();

    return base.toBuilder()
        .fontSizeSm(12)
        .fontSize(14)
        .fontSizeLg(16)
        .fontSizeHeading1(32)
        .fontSizeHeading2(26)
        .fontSizeHeading3(22)
        .fontSizeHeading4(18)
        .fontSizeHeading5(14)
        .build();
  }

  /**
   * 构建紧凑模式的尺寸令牌：圆角、间距、控件高度整体缩减。
   */
  private SizeToken buildCompactSizeToken() {
    return SizeToken.builder()
        .borderRadiusXs(1)
        .borderRadiusSm(2)
        .borderRadius(4)
        .borderRadiusLg(6)
        .paddingXxs(2)
        .paddingXs(4)
        .paddingSm(8)
        .padding(12)
        .paddingLg(16)
        .paddingXl(24)
        .marginXxs(2)
        .marginXs(4)
        .marginSm(8)
        .margin(12)
        .marginLg(16)
        .marginXl(24)
        .controlHeightSm(20)
        .controlHeight(28)
        .controlHeightLg(36)
        .build();
  }
}
