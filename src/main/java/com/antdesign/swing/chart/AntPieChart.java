package com.antdesign.swing.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Ant Design 风格饼图 / 环形图（Donut）。
 *
 * <p>特性：
 * <ul>
 *   <li>扁平化样式（无阴影、无3D效果）</li>
 *   <li>环形图使用 JFreeChart {@link RingPlot}</li>
 *   <li>扇区标签显示百分比</li>
 *   <li>悬停 Tooltip 显示名称和数值</li>
 *   <li>扇区间白色分隔线</li>
 *   <li>Ant Design 系列配色和主题响应</li>
 * </ul>
 *
 * @see AbstractAntChart
 */
public class AntPieChart extends AbstractAntChart {

  private static final long serialVersionUID = 1L;

  private PieDataset dataset;
  private boolean donut;
  private boolean showLabels = true;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建饼图。 */
  public AntPieChart(String title, PieDataset dataset) {
    this(title, dataset, false);
  }

  /**
   * 创建饼图或环形图。
   *
   * @param title   图表标题
   * @param dataset 数据集
   * @param donut   {@code true} 为环形图
   */
  public AntPieChart(String title, PieDataset dataset, boolean donut) {
    this.dataset = dataset;
    this.donut = donut;
    setTitle(title);
    refreshChart();
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /** 更新数据集并刷新图表。 */
  public void setDataset(PieDataset dataset) {
    this.dataset = dataset;
    refreshChart();
  }

  /** 设置是否为环形图。 */
  public void setDonut(boolean donut) {
    this.donut = donut;
    refreshChart();
  }

  /** 设置是否显示扇区标签。 */
  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
    refreshChart();
  }

  // =========================================================================
  // 图表构建
  // =========================================================================

  @Override
  protected JFreeChart createChart() {
    if (donut) {
      // 使用 RingPlot 实现真正的环形图
      RingPlot ringPlot = new RingPlot(dataset);
      ringPlot.setSectionDepth(0.35); // 环的宽度占半径的 35%
      JFreeChart chart = new JFreeChart(getTitle(),
          JFreeChart.DEFAULT_TITLE_FONT, ringPlot, true);
      return chart;
    } else {
      return ChartFactory.createPieChart(
          getTitle(), dataset, true, true, false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void applyThemeToChart(boolean dark) {
    super.applyThemeToChart(dark);
    JFreeChart chart = getChart();
    if (chart == null) {
      return;
    }

    PiePlot plot = (PiePlot) chart.getPlot();
    Color bg = ChartColorPalette.backgroundColor(dark);
    Color labelColor = ChartColorPalette.labelColor(dark);

    // 扁平化
    plot.setShadowPaint(null);
    plot.setCircular(true);
    plot.setBackgroundPaint(bg);
    plot.setOutlineVisible(false);

    // 尺寸 — 尽量让饼图撑满
    plot.setInteriorGap(0.02);

    // 扇区标签 — 显示百分比
    if (showLabels) {
      plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
          "{0}: {2}", new DecimalFormat("#,##0"), new DecimalFormat("0.0%")));
      plot.setLabelPaint(labelColor);
      plot.setLabelFont(createAntFont(fontToken().getFontSizeSm(), Font.PLAIN));
      plot.setLabelBackgroundPaint(null);
      plot.setLabelOutlinePaint(null);
      plot.setLabelShadowPaint(null);
      // 标签连接线
      plot.setLabelLinkPaint(ChartColorPalette.axisColor(dark));
      plot.setLabelLinkStroke(new BasicStroke(1.0f));
    } else {
      plot.setLabelGenerator(null);
    }

    // Tooltip — "Name: Value (xx.x%)"
    plot.setToolTipGenerator(new StandardPieToolTipGenerator(
        "{0}: {1} ({2})",
        new DecimalFormat("#,##0"),
        new DecimalFormat("0.0%")));

    // 环形图特殊配置
    if (plot instanceof RingPlot) {
      RingPlot ring = (RingPlot) plot;
      ring.setSeparatorsVisible(true);
      ring.setSeparatorPaint(bg);
      ring.setSeparatorStroke(new BasicStroke(3.0f));
      // 环形图内部颜色
    }

    // 配色
    if (dataset != null) {
      List<Comparable> keys = dataset.getKeys();
      for (int i = 0; i < keys.size(); i++) {
        Comparable key = keys.get(i);
        plot.setSectionPaint(key, getSeriesColor(i));
        // 白色间隔线（非 RingPlot 时使用 outline 模拟）
        plot.setSectionOutlinePaint(key, bg);
        plot.setSectionOutlineStroke(key, new BasicStroke(3.0f));
      }
    }
  }
}
