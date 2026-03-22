package com.antdesign.swing.theme;

import com.antdesign.swing.theme.token.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Ant Design 暗色主题算法。
 *
 * <p>从 {@code dark-theme.json} 资源文件加载暗色主题的颜色令牌。暗色模式下
 * 背景色以深灰/黑色为主，前景色以浅白色为主，品牌主色做降饱和处理。
 *
 * @see ThemeAlgorithm
 * @see DefaultAlgorithm
 */
@Slf4j
public class DarkAlgorithm implements ThemeAlgorithm {

  private static final String THEME_RESOURCE = "dark-theme.json";

  @Override
  public String getName() {
    return "dark";
  }

  @Override
  public boolean isDark() {
    return true;
  }

  @Override
  public GlobalToken derive(GlobalToken seedToken) {
    FontToken fontToken = (seedToken != null)
        ? seedToken.getFontToken()
        : FontToken.builder().build();
    SizeToken sizeToken = (seedToken != null)
        ? seedToken.getSizeToken()
        : SizeToken.builder().build();

    return GlobalToken.builder()
        .colorToken(DefaultAlgorithm.loadColorsFromResource(THEME_RESOURCE))
        .fontToken(fontToken)
        .sizeToken(sizeToken)
        .build();
  }
}
