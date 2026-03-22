package com.antdesign.swing.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;

/**
 * Ant Design 风格折线图。
 *
 * <p>特性：
 * <ul>
 *   <li>平滑圆角折线，2.5px 线宽</li>
 *   <li>数据点标记 — 实心圆 + 白色描边（ECharts 风格）</li>
 *   <li>悬停十字准线 + Tooltip</li>
 *   <li>Ant Design 系列配色和主题响应</li>
 * </ul>
 *
 * <pre>{@code
 * XYSeriesCollection dataset = new XYSeriesCollection();
 * XYSeries series = new XYSeries("Sales");
 * series.add(1, 100);
 * series.add(2, 200);
 * dataset.addSeries(series);
 *
 * AntLineChart chart = new AntLineChart("Monthly Sales", dataset);
 * }</pre>
 *
 * @see AbstractAntChart
 */
public class AntLineChart extends AbstractAntChart {

  private static final long serialVersionUID = 1L;

  private static final float LINE_WIDTH = 2.5f;
  private static final double POINT_RADIUS = 4.0;

  private XYDataset dataset;
  private final String xAxisLabel;
  private final String yAxisLabel;
  private boolean showShapes = true;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建折线图。
   *
   * @param title   图表标题
   * @param dataset 数据集
   */
  public AntLineChart(String title, XYDataset dataset) {
    this(title, dataset, null, null);
  }

  /**
   * 创建折线图，指定轴标签。
   *
   * @param title      图表标题
   * @param dataset    数据集
   * @param xAxisLabel X 轴标签
   * @param yAxisLabel Y 轴标签
   */
  public AntLineChart(String title, XYDataset dataset,
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

  /** 设置是否显示数据点标记。 */
  public void setShowShapes(boolean show) {
    this.showShapes = show;
    refreshChart();
  }

  // =========================================================================
  // 图表构建
  // =========================================================================

  @Override
  protected JFreeChart createChart() {
    return ChartFactory.createXYLineChart(
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
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    // Tooltip — 格式: "Series: (X, Y)"
    renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator(
        "{0}: ({1}, {2})",
        new DecimalFormat("#,##0.##"),
        new DecimalFormat("#,##0.##")));

    // 圆形数据点（实心 + 白色描边 = ECharts 风格）
    Shape pointShape = new Ellipse2D.Double(
        -POINT_RADIUS, -POINT_RADIUS,
        POINT_RADIUS * 2, POINT_RADIUS * 2);

    int seriesCount = (dataset != null) ? dataset.getSeriesCount() : 0;
    for (int i = 0; i < seriesCount; i++) {
      Color color = getSeriesColor(i);
      renderer.setSeriesPaint(i, color);
      renderer.setSeriesStroke(i, new BasicStroke(
          LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      renderer.setSeriesLinesVisible(i, true);

      // 数据点
      renderer.setSeriesShapesVisible(i, showShapes);
      renderer.setSeriesShape(i, pointShape);
      renderer.setSeriesShapesFilled(i, true);
      renderer.setSeriesFillPaint(i, color);
      renderer.setSeriesOutlinePaint(i, Color.WHITE);
      renderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
      renderer.setUseFillPaint(true);
      renderer.setUseOutlinePaint(true);
    }

    plot.setRenderer(renderer);
  }
}
