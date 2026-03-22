package com.antdesign.swing.chart;

import com.antdesign.swing.util.ColorUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * Ant Design 风格面积图。
 *
 * <p>特性：
 * <ul>
 *   <li>渐变半透明填充（ECharts 风格）</li>
 *   <li>系列色描边</li>
 *   <li>悬停十字准线 + Tooltip</li>
 *   <li>Ant Design 系列配色和主题响应</li>
 * </ul>
 *
 * @see AbstractAntChart
 */
public class AntAreaChart extends AbstractAntChart {

  private static final long serialVersionUID = 1L;

  private static final float AREA_ALPHA = 0.45f;

  private XYDataset dataset;
  private final String xAxisLabel;
  private final String yAxisLabel;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建面积图。 */
  public AntAreaChart(String title, XYDataset dataset) {
    this(title, dataset, null, null);
  }

  /** 创建面积图，指定轴标签。 */
  public AntAreaChart(String title, XYDataset dataset,
      String xAxisLabel, String yAxisLabel) {
    this.dataset = dataset;
    this.xAxisLabel = xAxisLabel;
    this.yAxisLabel = yAxisLabel;
    setTitle(title);
    refreshChart();
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /** 更新数据集并刷新图表。 */
  public void setDataset(XYDataset dataset) {
    this.dataset = dataset;
    refreshChart();
  }

  // =========================================================================
  // 图表构建
  // =========================================================================

  @Override
  protected JFreeChart createChart() {
    return ChartFactory.createXYAreaChart(
        getTitle(), xAxisLabel, yAxisLabel, dataset,
        PlotOrientation.VERTICAL, true, true, false);
  }

  @Override
  protected void applyThemeToChart(boolean dark) {
    super.applyThemeToChart(dark);
    JFreeChart chart = getChart();
    if (chart == null) {
      return;
    }

    XYPlot plot = chart.getXYPlot();
    XYAreaRenderer renderer = (XYAreaRenderer) plot.getRenderer();

    // Tooltip
    renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator(
        "{0}: ({1}, {2})",
        new DecimalFormat("#,##0.##"),
        new DecimalFormat("#,##0.##")));

    int seriesCount = (dataset != null) ? dataset.getSeriesCount() : 0;
    for (int i = 0; i < seriesCount; i++) {
      Color baseColor = getSeriesColor(i);
      Color fillColor = ColorUtils.withAlpha(baseColor, AREA_ALPHA);
      renderer.setSeriesPaint(i, fillColor);
      renderer.setSeriesOutlinePaint(i, baseColor);
      renderer.setSeriesOutlineStroke(i, new BasicStroke(
          2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    plot.setForegroundAlpha(0.9f);
  }
}
