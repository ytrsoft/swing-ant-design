package com.antdesign.swing.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * Ant Design 风格柱状图。
 *
 * <p>特性：
 * <ul>
 *   <li>扁平化柱体（无渐变、无阴影）</li>
 *   <li>悬停 Tooltip 显示数值</li>
 *   <li>适当柱宽和间距</li>
 *   <li>Ant Design 系列配色和主题响应</li>
 * </ul>
 *
 * @see AbstractAntChart
 */
public class AntBarChart extends AbstractAntChart {

  private static final long serialVersionUID = 1L;

  private CategoryDataset dataset;
  private final String categoryAxisLabel;
  private final String valueAxisLabel;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建柱状图。 */
  public AntBarChart(String title, CategoryDataset dataset) {
    this(title, dataset, null, null);
  }

  /** 创建柱状图，指定轴标签。 */
  public AntBarChart(String title, CategoryDataset dataset,
      String categoryAxisLabel, String valueAxisLabel) {
    this.dataset = dataset;
    this.categoryAxisLabel = categoryAxisLabel;
    this.valueAxisLabel = valueAxisLabel;
    setTitle(title);
    refreshChart();
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /** 更新数据集并刷新图表。 */
  public void setDataset(CategoryDataset dataset) {
    this.dataset = dataset;
    refreshChart();
  }

  // =========================================================================
  // 图表构建
  // =========================================================================

  @Override
  protected JFreeChart createChart() {
    return ChartFactory.createBarChart(
        getTitle(), categoryAxisLabel, valueAxisLabel, dataset,
        PlotOrientation.VERTICAL, true, true, false);
  }

  @Override
  protected void applyThemeToChart(boolean dark) {
    super.applyThemeToChart(dark);
    JFreeChart chart = getChart();
    if (chart == null) {
      return;
    }

    CategoryPlot plot = chart.getCategoryPlot();
    BarRenderer renderer = (BarRenderer) plot.getRenderer();

    // 扁平化
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setShadowVisible(false);
    renderer.setDrawBarOutline(false);

    // 柱宽 — 让柱子更饱满
    renderer.setItemMargin(0.02);
    renderer.setMaximumBarWidth(0.12);

    // Tooltip
    renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
        "{0} / {1}: {2}",
        new DecimalFormat("#,##0.##")));

    // 系列色
    int seriesCount = (dataset != null) ? dataset.getRowCount() : 0;
    for (int i = 0; i < seriesCount; i++) {
      renderer.setSeriesPaint(i, getSeriesColor(i));
    }
  }
}
