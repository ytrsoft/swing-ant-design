package com.antdesign.swing.util;

import java.awt.*;

/**
 * Ant Design 调色板。
 *
 * <p>集中定义 Ant Design 设计规范中的所有语义色和中性色常量，
 * 并提供基于种子色生成 10 级色阶的能力。
 *
 * <p>色阶编号 1 ~ 10，其中 6 为基准色。例如 {@code BLUE_6} 即 Ant Design 主色 {@code #1677FF}。
 */
public final class AntColor {

  private AntColor() {}

  // ---------------------------------------------------------------------------
  // 品牌主色 (Daybreak Blue)
  // ---------------------------------------------------------------------------
  public static final Color BLUE_1 = ColorUtils.parseHex("#E6F4FF");
  public static final Color BLUE_2 = ColorUtils.parseHex("#BAE0FF");
  public static final Color BLUE_3 = ColorUtils.parseHex("#91CAFF");
  public static final Color BLUE_4 = ColorUtils.parseHex("#69B1FF");
  public static final Color BLUE_5 = ColorUtils.parseHex("#4096FF");
  public static final Color BLUE_6 = ColorUtils.parseHex("#1677FF");
  public static final Color BLUE_7 = ColorUtils.parseHex("#0958D9");
  public static final Color BLUE_8 = ColorUtils.parseHex("#003EB3");
  public static final Color BLUE_9 = ColorUtils.parseHex("#002C8C");
  public static final Color BLUE_10 = ColorUtils.parseHex("#001D66");

  // ---------------------------------------------------------------------------
  // 成功色 (Polar Green)
  // ---------------------------------------------------------------------------
  public static final Color GREEN_1 = ColorUtils.parseHex("#F6FFED");
  public static final Color GREEN_2 = ColorUtils.parseHex("#D9F7BE");
  public static final Color GREEN_3 = ColorUtils.parseHex("#B7EB8F");
  public static final Color GREEN_4 = ColorUtils.parseHex("#95DE64");
  public static final Color GREEN_5 = ColorUtils.parseHex("#73D13D");
  public static final Color GREEN_6 = ColorUtils.parseHex("#52C41A");
  public static final Color GREEN_7 = ColorUtils.parseHex("#389E0D");
  public static final Color GREEN_8 = ColorUtils.parseHex("#237804");
  public static final Color GREEN_9 = ColorUtils.parseHex("#135200");
  public static final Color GREEN_10 = ColorUtils.parseHex("#092B00");

  // ---------------------------------------------------------------------------
  // 警告色 (Sunset Orange / Gold)
  // ---------------------------------------------------------------------------
  public static final Color GOLD_1 = ColorUtils.parseHex("#FFFBE6");
  public static final Color GOLD_2 = ColorUtils.parseHex("#FFF1B8");
  public static final Color GOLD_3 = ColorUtils.parseHex("#FFE58F");
  public static final Color GOLD_4 = ColorUtils.parseHex("#FFD666");
  public static final Color GOLD_5 = ColorUtils.parseHex("#FFC53D");
  public static final Color GOLD_6 = ColorUtils.parseHex("#FAAD14");
  public static final Color GOLD_7 = ColorUtils.parseHex("#D48806");
  public static final Color GOLD_8 = ColorUtils.parseHex("#AD6800");
  public static final Color GOLD_9 = ColorUtils.parseHex("#874D00");
  public static final Color GOLD_10 = ColorUtils.parseHex("#613400");

  // ---------------------------------------------------------------------------
  // 错误色 (Dust Red)
  // ---------------------------------------------------------------------------
  public static final Color RED_1 = ColorUtils.parseHex("#FFF1F0");
  public static final Color RED_2 = ColorUtils.parseHex("#FFCCC7");
  public static final Color RED_3 = ColorUtils.parseHex("#FFA39E");
  public static final Color RED_4 = ColorUtils.parseHex("#FF7875");
  public static final Color RED_5 = ColorUtils.parseHex("#FF4D4F");
  public static final Color RED_6 = ColorUtils.parseHex("#F5222D");
  public static final Color RED_7 = ColorUtils.parseHex("#CF1322");
  public static final Color RED_8 = ColorUtils.parseHex("#A8071A");
  public static final Color RED_9 = ColorUtils.parseHex("#820014");
  public static final Color RED_10 = ColorUtils.parseHex("#5C0011");

  // ---------------------------------------------------------------------------
  // 中性色 (Gray)
  // ---------------------------------------------------------------------------
  public static final Color GRAY_1 = ColorUtils.parseHex("#FFFFFF");
  public static final Color GRAY_2 = ColorUtils.parseHex("#FAFAFA");
  public static final Color GRAY_3 = ColorUtils.parseHex("#F5F5F5");
  public static final Color GRAY_4 = ColorUtils.parseHex("#F0F0F0");
  public static final Color GRAY_5 = ColorUtils.parseHex("#D9D9D9");
  public static final Color GRAY_6 = ColorUtils.parseHex("#BFBFBF");
  public static final Color GRAY_7 = ColorUtils.parseHex("#8C8C8C");
  public static final Color GRAY_8 = ColorUtils.parseHex("#595959");
  public static final Color GRAY_9 = ColorUtils.parseHex("#434343");
  public static final Color GRAY_10 = ColorUtils.parseHex("#262626");
  public static final Color GRAY_11 = ColorUtils.parseHex("#1F1F1F");
  public static final Color GRAY_12 = ColorUtils.parseHex("#141414");
  public static final Color GRAY_13 = ColorUtils.parseHex("#000000");

  // ---------------------------------------------------------------------------
  // 语义别名
  // ---------------------------------------------------------------------------

  /** 主色。 */
  public static final Color PRIMARY = BLUE_6;
  /** 主色 hover。 */
  public static final Color PRIMARY_HOVER = BLUE_5;
  /** 主色 active。 */
  public static final Color PRIMARY_ACTIVE = BLUE_7;
  /** 主色背景。 */
  public static final Color PRIMARY_BG = BLUE_1;

  /** 成功色。 */
  public static final Color SUCCESS = GREEN_6;
  public static final Color SUCCESS_HOVER = GREEN_5;
  public static final Color SUCCESS_ACTIVE = GREEN_7;
  public static final Color SUCCESS_BG = GREEN_1;

  /** 警告色。 */
  public static final Color WARNING = GOLD_6;
  public static final Color WARNING_HOVER = GOLD_5;
  public static final Color WARNING_ACTIVE = GOLD_7;
  public static final Color WARNING_BG = GOLD_1;

  /** 错误色。 */
  public static final Color ERROR = RED_5;
  public static final Color ERROR_HOVER = RED_4;
  public static final Color ERROR_ACTIVE = RED_7;
  public static final Color ERROR_BG = RED_1;

  // ---------------------------------------------------------------------------
  // 文字色
  // ---------------------------------------------------------------------------
  /** 主文字色 #0A0A0A。 */
  public static final Color TEXT = new Color(0x0A, 0x0A, 0x0A);
  /** 次文字色 rgba(0,0,0,0.65)。 */
  public static final Color TEXT_SECONDARY = new Color(0, 0, 0, 166);
  /** 辅助文字色 rgba(0,0,0,0.45)。 */
  public static final Color TEXT_TERTIARY = new Color(0, 0, 0, 115);
  /** 占位文字色 rgba(0,0,0,0.25)。 */
  public static final Color TEXT_QUATERNARY = new Color(0, 0, 0, 64);

  // ---------------------------------------------------------------------------
  // 边框和填充
  // ---------------------------------------------------------------------------
  /** 默认边框色。 */
  public static final Color BORDER = GRAY_5;
  /** 次边框色。 */
  public static final Color BORDER_SECONDARY = GRAY_4;
  /** 填充色 rgba(0,0,0,0.15)。 */
  public static final Color FILL = new Color(0, 0, 0, 38);
  /** 次填充色 rgba(0,0,0,0.06)。 */
  public static final Color FILL_SECONDARY = new Color(0, 0, 0, 15);
  /** 三级填充色 rgba(0,0,0,0.04)。 */
  public static final Color FILL_TERTIARY = new Color(0, 0, 0, 10);
  /** 四级填充色 rgba(0,0,0,0.02)。 */
  public static final Color FILL_QUATERNARY = new Color(0, 0, 0, 5);
  /** 容器背景色。 */
  public static final Color BG_CONTAINER = GRAY_1;
  /** 布局背景色。 */
  public static final Color BG_LAYOUT = GRAY_3;

  // ---------------------------------------------------------------------------
  // 图表系列配色
  // ---------------------------------------------------------------------------

  /**
   * Ant Design 图表默认系列配色。
   *
   * <p>用于多系列图表（折线图、柱状图、面积图等）中按序列为数据系列着色。
   * 配色取自 Ant Design 官方数据可视化色板。
   */
  public static final Color[] CHART_SERIES_COLORS = {
      BLUE_6, GREEN_6, GOLD_6,
      RED_5, ColorUtils.parseHex("#722ED1"), ColorUtils.parseHex("#13C2C2"),
      BLUE_4, GREEN_4, GOLD_4
  };

  // ---------------------------------------------------------------------------
  // 色阶生成
  // ---------------------------------------------------------------------------

  /**
   * 基于种子色生成 10 级色阶数组。
   *
   * <p>索引 0 ~ 9 分别对应 level 1 ~ 10，其中索引 5（level 6）最接近种子色。
   * 索引 0 ~ 4 依次变亮，索引 6 ~ 9 依次变暗。
   *
   * @param seed 种子色
   * @return 长度为 10 的颜色数组
   */
  public static Color[] generatePalette(Color seed) {
    Color[] palette = new Color[10];
    // 浅色端（level 1 ~ 5）：与白色混合
    float[] lightRatios = {0.92f, 0.78f, 0.62f, 0.45f, 0.25f};
    for (int i = 0; i < 5; i++) {
      palette[i] = ColorUtils.lighten(seed, lightRatios[i]);
    }
    // 基准色（level 6）
    palette[5] = seed;
    // 深色端（level 7 ~ 10）：与黑色混合
    float[] darkRatios = {0.15f, 0.30f, 0.45f, 0.60f};
    for (int i = 0; i < 4; i++) {
      palette[6 + i] = ColorUtils.darken(seed, darkRatios[i]);
    }
    return palette;
  }
}
