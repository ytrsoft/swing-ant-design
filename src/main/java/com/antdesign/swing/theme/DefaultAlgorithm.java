package com.antdesign.swing.theme;

import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.GlobalToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Ant Design 默认（亮色）主题算法。
 *
 * <p>从 {@code default-theme.json} 资源文件加载亮色主题的颜色令牌，
 * 字体和尺寸令牌使用 Ant Design 标准默认值。
 *
 * @see ThemeAlgorithm
 */
@Slf4j
public class DefaultAlgorithm implements ThemeAlgorithm {

  private static final String THEME_RESOURCE = "default-theme.json";

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public boolean isDark() {
    return false;
  }

  @Override
  public GlobalToken derive(GlobalToken seedToken) {
    ColorToken colorToken = loadColorsFromResource(THEME_RESOURCE);
    FontToken fontToken = (seedToken != null)
        ? seedToken.getFontToken()
        : FontToken.builder().build();
    SizeToken sizeToken = (seedToken != null)
        ? seedToken.getSizeToken()
        : SizeToken.builder().build();

    return GlobalToken.builder()
        .colorToken(colorToken)
        .fontToken(fontToken)
        .sizeToken(sizeToken)
        .build();
  }

  /**
   * 从资源文件加载颜色令牌。
   *
   * @param resourceName 资源文件名
   * @return 颜色令牌，加载失败时返回默认值
   */
  @SuppressWarnings("unchecked")
  static ColorToken loadColorsFromResource(String resourceName) {
    try (InputStream is = DefaultAlgorithm.class.getResourceAsStream(
        "/com/antdesign/swing/theme/" + resourceName)) {
      if (is == null) {
        log.warn("Theme resource not found: {}, using defaults", resourceName);
        return ColorToken.builder().build();
      }
      Gson gson = new Gson();
      Map<String, Object> root = gson.fromJson(
          new InputStreamReader(is, StandardCharsets.UTF_8),
          new TypeToken<Map<String, Object>>() {}.getType());
      Map<String, String> colors = (Map<String, String>) root.get("colors");
      if (colors == null) {
        return ColorToken.builder().build();
      }
      return buildColorToken(colors);
    } catch (Exception e) {
      log.error("Failed to load theme resource: {}", resourceName, e);
      return ColorToken.builder().build();
    }
  }

  private static ColorToken buildColorToken(Map<String, String> colors) {
    ColorToken.ColorTokenBuilder builder = ColorToken.builder();

    applyIfPresent(colors, "primaryColor",
        v -> builder.primaryColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "primaryHoverColor",
        v -> builder.primaryHoverColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "primaryActiveColor",
        v -> builder.primaryActiveColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "primaryBgColor",
        v -> builder.primaryBgColor(ColorToken.parseHexColor(v)));

    applyIfPresent(colors, "successColor",
        v -> builder.successColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "successBgColor",
        v -> builder.successBgColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "warningColor",
        v -> builder.warningColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "warningBgColor",
        v -> builder.warningBgColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "errorColor",
        v -> builder.errorColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "errorBgColor",
        v -> builder.errorBgColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "infoColor",
        v -> builder.infoColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "infoBgColor",
        v -> builder.infoBgColor(ColorToken.parseHexColor(v)));

    applyIfPresent(colors, "textColor",
        v -> builder.textColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "textSecondaryColor",
        v -> builder.textSecondaryColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "textTertiaryColor",
        v -> builder.textTertiaryColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "textQuaternaryColor",
        v -> builder.textQuaternaryColor(ColorToken.parseHexColor(v)));

    applyIfPresent(colors, "borderColor",
        v -> builder.borderColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "borderSecondaryColor",
        v -> builder.borderSecondaryColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "fillColor",
        v -> builder.fillColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "fillSecondaryColor",
        v -> builder.fillSecondaryColor(ColorToken.parseHexColor(v)));

    applyIfPresent(colors, "bgContainer",
        v -> builder.bgContainer(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "bgElevated",
        v -> builder.bgElevated(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "bgLayout",
        v -> builder.bgLayout(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "bgSpotlight",
        v -> builder.bgSpotlight(ColorToken.parseHexColor(v)));

    applyIfPresent(colors, "disabledColor",
        v -> builder.disabledColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "disabledBgColor",
        v -> builder.disabledBgColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "linkColor",
        v -> builder.linkColor(ColorToken.parseHexColor(v)));
    applyIfPresent(colors, "linkHoverColor",
        v -> builder.linkHoverColor(ColorToken.parseHexColor(v)));

    return builder.build();
  }

  private static void applyIfPresent(Map<String, String> map, String key,
      java.util.function.Consumer<String> consumer) {
    String value = map.get(key);
    if (value != null) {
      consumer.accept(value);
    }
  }
}
