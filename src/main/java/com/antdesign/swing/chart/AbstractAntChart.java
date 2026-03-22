package com.antdesign.swing.chart;

import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.util.ColorUtils;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.*;

/**
 * 所有 Ant Design 风格图表的抽象基类。
 *
 * <p>继承 {@link AntPanel}，内部嵌入 {@link ChartPanel} 来渲染 JFreeChart 图表。
 * 自动监听主题变更，在亮色/暗色切换时重新应用 Ant Design 配色。
 *
 * <p>内置 ECharts 风格的交互能力：
 * <ul>
 *   <li>鼠标悬停 Tooltip</li>
 *   <li>十字准线 Crosshair（折线图、面积图）</li>
 *   <li>虚线网格</li>
 *   <li>无刻度线的简洁坐标轴</li>
 * </ul>
 *
 * <p>子类须实现 {@link #createChart()} 来构建具体的 JFreeChart 实例。
 *
 * @see ChartColorPalette
 * @see AntPanel
 */
public abstract class AbstractAntChart extends AntPanel {

  private static final long serialVersionUID = 1L;

  /** ECharts 风格虚线网格线。 */
  protected static final BasicStroke GRID_STROKE = new BasicStroke(
      1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
      10.0f, new float[]{4.0f, 4.0f}, 0.0f);

  /** 十字准线线条。 */
  protected static final BasicStroke CROSSHAIR_STROKE = new BasicStroke(
      1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
      10.0f, new float[]{3.0f, 3.0f}, 0.0f);

  /** 图表标题文本，可为 {@code null}。 */
  private String title;

  /** 内部 JFreeChart 实例。 */
  private JFreeChart chart;

  /** 嵌入的 ChartPanel，负责实际绘制和交互。 */
  private ChartPanel chartPanel;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建无标题的图表。 */
  protected AbstractAntChart() {
    this(null);
  }

  /**
   * 创建带标题的图表。
   *
   * @param title 图表标题，可为 {@code null}
   */
  protected AbstractAntChart(String title) {
    super(new BorderLayout());
    this.title = title;
    initChart();
  }

  // =========================================================================
  // 抽象方法 — 子类必须实现
  // =========================================================================

  /**
   * 构建 JFreeChart 实例。子类在此方法中创建数据集和图表对象。
   *
   * @return 新的 JFreeChart 实例
   */
  protected abstract JFreeChart createChart();

  // =========================================================================
  // 主题响应
  // =========================================================================

  @Override
  protected void changeTheme(AntTheme theme) {
    super.changeTheme(theme);
    if (chart != null) {
      applyThemeToChart(theme.isDark());
      chart.fireChartChanged();
    }
  }

  /**
   * 将 Ant Design 主题配色应用到 JFreeChart。
   *
   * <p>基类实现覆盖了背景色、标题、图例、Plot 背景、网格线和坐标轴。
   * 子类覆写时建议先调用 {@code super.applyThemeToChart(dark)}。
   *
   * @param dark 当前是否暗色主题
   */
  protected void applyThemeToChart(boolean dark) {
    Color bg = ChartColorPalette.backgroundColor(dark);
    Color labelColor = ChartColorPalette.labelColor(dark);
    Color gridColor = ChartColorPalette.gridColor(dark);
    Color axisColor = ChartColorPalette.axisColor(dark);

    chart.setBackgroundPaint(bg);
    chart.setBorderVisible(false);
    chart.setAntiAlias(true);
    chart.setTextAntiAlias(true);

    applyTitleStyle(dark);
    applyLegendStyle(dark, bg, labelColor);

    Plot plot = chart.getPlot();
    plot.setBackgroundPaint(bg);
    plot.setOutlineVisible(false);

    if (plot instanceof CategoryPlot) {
      applyCategoryPlotStyle((CategoryPlot) plot, dark, gridColor, axisColor, labelColor);
    } else if (plot instanceof XYPlot) {
      applyXyPlotStyle((XYPlot) plot, dark, gridColor, axisColor, labelColor);
    } else if (plot instanceof PiePlot) {
      applyPiePlotStyle((PiePlot) plot, bg, labelColor);
    }
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /** 获取图表标题。 */
  public String getTitle() {
    return title;
  }

  /** 设置图表标题。 */
  public void setTitle(String title) {
    this.title = title;
    if (chart != null) {
      if (title != null && !title.isEmpty()) {
        chart.setTitle(new TextTitle(title));
      } else {
        chart.setTitle((TextTitle) null);
      }
      applyThemeToChart(themeManager().isDark());
      chart.fireChartChanged();
    }
  }

  /** 获取底层 JFreeChart 实例。 */
  public JFreeChart getChart() {
    return chart;
  }

  /** 获取内部 ChartPanel。 */
  public ChartPanel getChartPanel() {
    return chartPanel;
  }

  /** 刷新图表。重新调用 {@link #createChart()} 并应用主题。 */
  public void refreshChart() {
    if (chartPanel != null) {
      remove(chartPanel);
    }
    initChart();
    revalidate();
    repaint();
  }

  // =========================================================================
  // 便捷令牌访问（供子类使用）
  // =========================================================================

  /** 当前是否暗色主题。 */
  protected boolean isDark() {
    return AntThemeManager.getInstance().isDark();
  }

  /** 获取系列色。 */
  protected Color getSeriesColor(int index) {
    return ChartColorPalette.seriesColor(index, isDark());
  }

  /** 创建 Ant Design 风格字体。 */
  protected Font createAntFont(int size, int style) {
    return fontToken().createFont(size, style);
  }

  // =========================================================================
  // 内部实现
  // =========================================================================

  private void initChart() {
    chart = createChart();
    if (chart == null) {
      throw new IllegalStateException("createChart() must not return null");
    }

    chart.setPadding(new RectangleInsets(4, 2, 4, 2));
    applyThemeToChart(themeManager().isDark());

    chartPanel = new ChartPanel(chart, true);
    chartPanel.setOpaque(false);
    chartPanel.setBackground(new Color(0, 0, 0, 0));

    // --- 交互配置 ---
    chartPanel.setMouseWheelEnabled(false);
    chartPanel.setDomainZoomable(false);
    chartPanel.setRangeZoomable(false);
    chartPanel.setPopupMenu(null);

    // 开启 Tooltip — 立即响应，不自动消失
    javax.swing.ToolTipManager ttm = javax.swing.ToolTipManager.sharedInstance();
    ttm.setInitialDelay(100);
    ttm.setDismissDelay(10000);
    ttm.setReshowDelay(0);

    // 确保 chartPanel 填满整个区域
    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);
    chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
    chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

    add(chartPanel, BorderLayout.CENTER);
  }

  private void applyTitleStyle(boolean dark) {
    TextTitle textTitle = chart.getTitle();
    if (textTitle != null) {
      textTitle.setPaint(
          dark ? colorToken().getTextColor() : new Color(0x26, 0x26, 0x26));
      textTitle.setFont(createAntFont(fontToken().getFontSizeHeading5(), Font.BOLD));
      textTitle.setPadding(new RectangleInsets(4, 0, 8, 0));
    }
  }

  private void applyLegendStyle(boolean dark, Color bg, Color labelColor) {
    LegendTitle legend = chart.getLegend();
    if (legend != null) {
      legend.setBackgroundPaint(bg);
      legend.setItemPaint(labelColor);
      legend.setItemFont(createAntFont(fontToken().getFontSizeSm(), Font.PLAIN));
      legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
      legend.setPosition(RectangleEdge.BOTTOM);
      legend.setPadding(new RectangleInsets(8, 0, 4, 0));
    }
  }

  /**
   * 统一配置坐标轴 — ECharts 风格（无刻度线、简洁标签）。
   */
  private void styleAxis(org.jfree.chart.axis.Axis axis,
      Color axisColor, Color labelColor) {
    if (axis == null) {
      return;
    }
    Font tickFont = createAntFont(fontToken().getFontSizeSm(), Font.PLAIN);
    Font labelFont = createAntFont(fontToken().getFontSizeSm(), Font.PLAIN);

    axis.setAxisLinePaint(axisColor);
    axis.setTickLabelPaint(labelColor);
    axis.setLabelPaint(labelColor);
    axis.setLabelFont(labelFont);
    axis.setTickLabelFont(tickFont);
    axis.setTickMarkPaint(new Color(0, 0, 0, 0)); // 隐藏刻度线
    axis.setTickMarksVisible(false);
  }

  private void applyCategoryPlotStyle(CategoryPlot plot, boolean dark,
      Color gridColor, Color axisColor, Color labelColor) {
    // 网格线 — ECharts 风格虚线
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(gridColor);
    plot.setRangeGridlineStroke(GRID_STROKE);
    plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
    plot.setInsets(new RectangleInsets(8, 8, 0, 8));

    // 十字准线
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairPaint(
        ColorUtils.withAlpha(ChartColorPalette.seriesColor(0, dark), 0.5f));
    plot.setRangeCrosshairStroke(CROSSHAIR_STROKE);

    styleAxis(plot.getDomainAxis(), axisColor, labelColor);
    styleAxis(plot.getRangeAxis(), axisColor, labelColor);

    // 分类轴额外设置
    CategoryAxis domainAxis = plot.getDomainAxis();
    if (domainAxis != null) {
      domainAxis.setCategoryMargin(0.2);
      domainAxis.setLowerMargin(0.02);
      domainAxis.setUpperMargin(0.02);
    }
  }

  private void applyXyPlotStyle(XYPlot plot, boolean dark,
      Color gridColor, Color axisColor, Color labelColor) {
    // 网格线 — ECharts 风格虚线
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(gridColor);
    plot.setRangeGridlineStroke(GRID_STROKE);
    plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
    plot.setInsets(new RectangleInsets(8, 8, 0, 8));

    // 十字准线 — ECharts 核心交互
    Color crosshairColor = ColorUtils.withAlpha(
        ChartColorPalette.seriesColor(0, dark), 0.5f);
    plot.setDomainCrosshairVisible(true);
    plot.setDomainCrosshairPaint(crosshairColor);
    plot.setDomainCrosshairStroke(CROSSHAIR_STROKE);
    plot.setRangeCrosshairVisible(true);
    plot.setRangeCrosshairPaint(crosshairColor);
    plot.setRangeCrosshairStroke(CROSSHAIR_STROKE);

    styleAxis(plot.getDomainAxis(), axisColor, labelColor);
    styleAxis(plot.getRangeAxis(), axisColor, labelColor);

    // 值轴额外设置 — 让最高值上方有空间
    ValueAxis rangeAxis = plot.getRangeAxis();
    if (rangeAxis instanceof NumberAxis) {
      ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(false);
    }
  }

  private void applyPiePlotStyle(PiePlot plot, Color bg, Color labelColor) {
    plot.setLabelPaint(labelColor);
    plot.setLabelFont(createAntFont(fontToken().getFontSizeSm(), Font.PLAIN));
    plot.setLabelBackgroundPaint(null);
    plot.setLabelOutlinePaint(null);
    plot.setLabelShadowPaint(null);
    plot.setShadowPaint(null);
    plot.setBackgroundPaint(bg);
  }
}
