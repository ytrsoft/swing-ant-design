package com.antdesign.swing.chart;

import java.awt.Color;

/**
 * Ant Design 图表配色方案。
 *
 * <p>提供亮色和暗色两套预定义配色序列，覆盖图表中常见的系列色、网格色、
 * 背景色等。颜色值取自 Ant Design Charts 官方色板。
 *
 * @see AbstractAntChart
 */
public final class ChartColorPalette {

  private ChartColorPalette() {}

  // =========================================================================
  // 亮色主题系列色（10 色）
  // =========================================================================

  /** 亮色模式下的数据系列配色。 */
  public static final Color[] LIGHT_SERIES = {
      new Color(0x16, 0x77, 0xFF),  // 蓝 — 品牌主色
      new Color(0x52, 0xC4, 0x1A),  // 绿
      new Color(0xFA, 0xAD, 0x14),  // 金
      new Color(0xFF, 0x4D, 0x4F),  // 红
      new Color(0x72, 0x2E, 0xD1),  // 紫
      new Color(0x13, 0xC2, 0xC2),  // 青
      new Color(0xFA, 0x54, 0x1C),  // 橙
      new Color(0xEB, 0x2F, 0x96),  // 洋红
      new Color(0xA0, 0xD9, 0x11),  // 青柠
      new Color(0xF5, 0x22, 0x2D),  // 火山红
  };

  // =========================================================================
  // 暗色主题系列色（10 色）
  // =========================================================================

  /** 暗色模式下的数据系列配色（降低饱和度以适配深色背景）。 */
  public static final Color[] DARK_SERIES = {
      new Color(0x40, 0x96, 0xFF),  // 蓝
      new Color(0x73, 0xD1, 0x3D),  // 绿
      new Color(0xFF, 0xC5, 0x3D),  // 金
      new Color(0xFF, 0x70, 0x75),  // 红
      new Color(0x95, 0x54, 0xDB),  // 紫
      new Color(0x33, 0xCC, 0xCC),  // 青
      new Color(0xFF, 0x76, 0x45),  // 橙
      new Color(0xF0, 0x5B, 0xAB),  // 洋红
      new Color(0xBA, 0xE6, 0x37),  // 青柠
      new Color(0xF7, 0x51, 0x5B),  // 火山红
  };

  // =========================================================================
  // 网格 / 轴线 / 背景
  // =========================================================================

  /** 亮色模式 — 网格线色。 */
  public static final Color LIGHT_GRID = new Color(0xF0, 0xF0, 0xF0);

  /** 暗色模式 — 网格线色。 */
  public static final Color DARK_GRID = new Color(0x30, 0x30, 0x30);

  /** 亮色模式 — 轴线色。 */
  public static final Color LIGHT_AXIS = new Color(0xD9, 0xD9, 0xD9);

  /** 暗色模式 — 轴线色。 */
  public static final Color DARK_AXIS = new Color(0x43, 0x43, 0x43);

  /** 亮色模式 — 轴标签色。 */
  public static final Color LIGHT_LABEL = new Color(0x59, 0x59, 0x59);

  /** 暗色模式 — 轴标签色。 */
  public static final Color DARK_LABEL = new Color(0xA6, 0xA6, 0xA6);

  /** 亮色模式 — 图表背景。 */
  public static final Color LIGHT_BACKGROUND = Color.WHITE;

  /** 暗色模式 — 图表背景。 */
  public static final Color DARK_BACKGROUND = new Color(0x14, 0x14, 0x14);

  // =========================================================================
  // 便捷方法
  // =========================================================================

  /**
   * 根据主题模式返回系列色数组。
   *
   * @param dark 是否暗色主题
   * @return 系列色数组
   */
  public static Color[] seriesColors(boolean dark) {
    return dark ? DARK_SERIES : LIGHT_SERIES;
  }

  /**
   * 根据序号获取系列色（自动循环取余）。
   *
   * @param index 系列索引
   * @param dark  是否暗色主题
   * @return 系列颜色
   */
  public static Color seriesColor(int index, boolean dark) {
    Color[] palette = seriesColors(dark);
    return palette[Math.abs(index) % palette.length];
  }

  /**
   * 获取网格线颜色。
   *
   * @param dark 是否暗色主题
   * @return 网格线颜色
   */
  public static Color gridColor(boolean dark) {
    return dark ? DARK_GRID : LIGHT_GRID;
  }

  /**
   * 获取轴线颜色。
   *
   * @param dark 是否暗色主题
   * @return 轴线颜色
   */
  public static Color axisColor(boolean dark) {
    return dark ? DARK_AXIS : LIGHT_AXIS;
  }

  /**
   * 获取轴标签颜色。
   *
   * @param dark 是否暗色主题
   * @return 标签颜色
   */
  public static Color labelColor(boolean dark) {
    return dark ? DARK_LABEL : LIGHT_LABEL;
  }

  /**
   * 获取图表背景色。
   *
   * @param dark 是否暗色主题
   * @return 背景色
   */
  public static Color backgroundColor(boolean dark) {
    return dark ? DARK_BACKGROUND : LIGHT_BACKGROUND;
  }
}
