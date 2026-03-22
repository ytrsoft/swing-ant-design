package com.antdesign.swing;

import com.antdesign.swing.base.AbstractAntFrame;
import com.antdesign.swing.chart.AntAreaChart;
import com.antdesign.swing.chart.AntBarChart;
import com.antdesign.swing.chart.AntLineChart;
import com.antdesign.swing.chart.AntPieChart;
import com.antdesign.swing.general.AntButton;
import com.antdesign.swing.general.AntFloatButton;
import com.antdesign.swing.model.ButtonShape;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.DarkAlgorithm;
import com.antdesign.swing.theme.DefaultAlgorithm;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.util.AntIcons;


import net.miginfocom.swing.MigLayout;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Swing Ant Design 组件展示示例。
 *
 * <p>演示图表（折线图、柱状图、饼图、面积图）、图标和按钮的使用方式，
 * 以及亮色/暗色主题切换。所有图标颜色跟随主题响应。
 */
public class AntDesign extends AbstractAntFrame {

  /** 图标名称列表，用于图标网格展示。 */
  private static final String[] ICON_NAMES = {
      "search", "home", "setting", "edit", "delete",
      "plus", "close", "check", "info-circle", "warning",
      "exclamation-circle", "user", "mail", "bell", "calendar",
      "clock", "upload", "download", "folder", "file",
      "lock", "unlock", "eye", "link", "share",
      "cloud", "save", "copy", "login", "logout"
  };

  // --- 需要在主题切换时更新图标的组件引用 ---
  private AntButton themeBtn;
  private AntButton downloadBtn;
  private AntButton settingBtn;

  // --- 需要在主题切换时重建内容的容器 ---
  private JPanel iconGrid16;
  private JPanel iconGrid24;

  public static void main(String[] args) {
    launch();
  }

  @Override
  protected void initContent() {
    JPanel root = new JPanel(new BorderLayout());
    root.add(createToolbar(), BorderLayout.NORTH);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Charts", createChartsPanel());
    tabs.addTab("Buttons", createButtonsPanel());
    tabs.addTab("Icons", createIconsPanel());
    root.add(tabs, BorderLayout.CENTER);

    setContentPane(root);

    // 注册全局主题监听：更新所有图标颜色
    AntThemeManager.getInstance().addThemeChangeListener(this::onThemeChanged);
  }

  // =========================================================================
  // 主题响应 — 图标颜色更新
  // =========================================================================

  /**
   * 主题切换回调：将所有使用 textColor 的图标重新着色。
   *
   * <p>Primary 按钮上的白色图标不需要更新，因为白色不随主题变化。
   * Default 按钮上跟随文本色的图标需要用新的 textColor 重新渲染。
   * 图标网格需要整体重建。
   */
  private void onThemeChanged(AntTheme theme) {
    Color textColor = theme.getColorToken().getTextColor();

    // 工具栏 — setting 图标跟随文本色
    if (themeBtn != null) {
      themeBtn.setIcon(AntIcons.outlined("setting", 14, textColor));
    }

    // 按钮 Tab — Default 类型按钮的图标跟随文本色
    if (downloadBtn != null) {
      downloadBtn.setIcon(AntIcons.outlined("download", 14, textColor));
    }
    if (settingBtn != null) {
      settingBtn.setIcon(AntIcons.outlined("setting", 14, textColor));
    }

    // 图标 Tab — 整体重建网格
    rebuildIconGrid(iconGrid16, 16, theme);
    rebuildIconGrid(iconGrid24, 24, theme);
  }

  /**
   * 重建图标网格内容。
   *
   * @param grid  网格容器
   * @param size  图标尺寸
   * @param theme 当前主题
   */
  private void rebuildIconGrid(JPanel grid, int size, AntTheme theme) {
    if (grid == null) {
      return;
    }
    grid.removeAll();
    fillIconGrid(grid, size, theme.getColorToken());
    grid.revalidate();
    grid.repaint();
  }

  // =========================================================================
  // 工具栏
  // =========================================================================

  private JPanel createToolbar() {
    JPanel toolbar = new JPanel(new MigLayout("insets 8 16 8 16", "[]push[]"));

    JLabel titleLabel = new JLabel("Ant Design for Swing");
    titleLabel.setFont(new Font("PingFang SC", Font.BOLD, 18));
    toolbar.add(titleLabel);

    Color textColor = AntThemeManager.getInstance().getColorToken().getTextColor();
    themeBtn = new AntButton("Switch Theme", ButtonType.DEFAULT);
    themeBtn.setIcon(AntIcons.outlined("setting", 14, textColor));
    themeBtn.addActionListener(e -> {
      AntThemeManager mgr = AntThemeManager.getInstance();
      mgr.switchTheme(mgr.isDark() ? new DefaultAlgorithm() : new DarkAlgorithm());
    });
    toolbar.add(themeBtn);

    return toolbar;
  }

  // =========================================================================
  // 图表 Tab
  // =========================================================================

  private JPanel createChartsPanel() {
    JPanel panel = new JPanel(new MigLayout(
        "insets 16, gap 16", "[grow,fill][grow,fill]", "[grow,fill][grow,fill]"));

    panel.add(createLineChart());
    panel.add(createBarChart(), "wrap");
    panel.add(createPieChart());
    panel.add(createAreaChart());

    return panel;
  }

  private AntLineChart createLineChart() {
    XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries sales = new XYSeries("Sales");
    sales.add(1, 820);
    sales.add(2, 932);
    sales.add(3, 901);
    sales.add(4, 934);
    sales.add(5, 1290);
    sales.add(6, 1330);
    sales.add(7, 1320);
    dataset.addSeries(sales);

    XYSeries cost = new XYSeries("Cost");
    cost.add(1, 620);
    cost.add(2, 732);
    cost.add(3, 701);
    cost.add(4, 734);
    cost.add(5, 890);
    cost.add(6, 1030);
    cost.add(7, 1120);
    dataset.addSeries(cost);

    AntLineChart chart = new AntLineChart("Line Chart", dataset);
    chart.setShowShapes(true);
    return chart;
  }

  private AntBarChart createBarChart() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    dataset.addValue(43, "Direct", "Mon");
    dataset.addValue(58, "Direct", "Tue");
    dataset.addValue(35, "Direct", "Wed");
    dataset.addValue(68, "Direct", "Thu");
    dataset.addValue(45, "Direct", "Fri");

    dataset.addValue(23, "Email", "Mon");
    dataset.addValue(38, "Email", "Tue");
    dataset.addValue(55, "Email", "Wed");
    dataset.addValue(28, "Email", "Thu");
    dataset.addValue(65, "Email", "Fri");

    return new AntBarChart("Bar Chart", dataset);
  }

  private AntPieChart createPieChart() {
    DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue("Direct", 335);
    dataset.setValue("Email", 310);
    dataset.setValue("Search", 234);
    dataset.setValue("Referral", 135);
    dataset.setValue("Social", 148);

    return new AntPieChart("Pie Chart (Donut)", dataset, true);
  }

  private AntAreaChart createAreaChart() {
    XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries series1 = new XYSeries("Revenue");
    series1.add(2019, 3500);
    series1.add(2020, 4200);
    series1.add(2021, 6800);
    series1.add(2022, 8200);
    series1.add(2023, 9500);
    series1.add(2024, 11200);
    dataset.addSeries(series1);

    XYSeries series2 = new XYSeries("Profit");
    series2.add(2019, 1200);
    series2.add(2020, 1800);
    series2.add(2021, 3100);
    series2.add(2022, 3800);
    series2.add(2023, 4500);
    series2.add(2024, 5600);
    dataset.addSeries(series2);

    return new AntAreaChart("Area Chart", dataset);
  }

  // =========================================================================
  // 按钮 Tab
  // =========================================================================

  private JScrollPane createButtonsPanel() {
    JPanel panel = new JPanel(new MigLayout(
        "insets 24, gapy 16", "[grow,fill]"));

    Color textColor = AntThemeManager.getInstance().getColorToken().getTextColor();

    // ---- 按钮类型 ----
    panel.add(createSectionLabel("Button Types"), "wrap");
    JPanel typeRow = new JPanel(new MigLayout("insets 0, gap 12"));
    typeRow.add(new AntButton("Primary", ButtonType.PRIMARY));
    typeRow.add(new AntButton("Default", ButtonType.DEFAULT));
    typeRow.add(new AntButton("Dashed", ButtonType.DASHED));
    typeRow.add(new AntButton("Text", ButtonType.TEXT));
    typeRow.add(new AntButton("Link", ButtonType.LINK));
    panel.add(typeRow, "wrap");

    // ---- 图标按钮 ----
    panel.add(createSectionLabel("Icon Buttons"), "wrap");
    JPanel iconRow = new JPanel(new MigLayout("insets 0, gap 12"));

    // Primary 按钮 — 白色图标不需要跟随主题
    AntButton searchBtn = new AntButton("Search", ButtonType.PRIMARY);
    searchBtn.setIcon(AntIcons.outlined("search", 14, Color.WHITE));
    iconRow.add(searchBtn);

    // Default 按钮 — 图标跟随 textColor，需要存引用
    downloadBtn = new AntButton("Download", ButtonType.DEFAULT);
    downloadBtn.setIcon(AntIcons.outlined("download", 14, textColor));
    iconRow.add(downloadBtn);

    AntButton plusBtn = new AntButton(
        AntIcons.outlined("plus", 14, Color.WHITE), ButtonType.PRIMARY);
    plusBtn.setShape(ButtonShape.CIRCLE);
    iconRow.add(plusBtn);

    settingBtn = new AntButton(
        AntIcons.outlined("setting", 14, textColor), ButtonType.DEFAULT);
    settingBtn.setShape(ButtonShape.CIRCLE);
    iconRow.add(settingBtn);

    panel.add(iconRow, "wrap");

    // ---- 尺寸 ----
    panel.add(createSectionLabel("Sizes"), "wrap");
    JPanel sizeRow = new JPanel(new MigLayout("insets 0, gap 12, ay center"));
    sizeRow.add(new AntButton("Large", ButtonType.PRIMARY, ComponentSize.LARGE));
    sizeRow.add(new AntButton("Middle", ButtonType.PRIMARY, ComponentSize.MIDDLE));
    sizeRow.add(new AntButton("Small", ButtonType.PRIMARY, ComponentSize.SMALL));
    panel.add(sizeRow, "wrap");

    // ---- 危险 / 幽灵 / 禁用 ----
    panel.add(createSectionLabel("Danger / Ghost / Disabled"), "wrap");
    JPanel stateRow = new JPanel(new MigLayout("insets 0, gap 12"));

    AntButton dangerPrimary = new AntButton("Danger", ButtonType.PRIMARY);
    dangerPrimary.setDanger(true);
    stateRow.add(dangerPrimary);

    AntButton dangerDefault = new AntButton("Danger", ButtonType.DEFAULT);
    dangerDefault.setDanger(true);
    stateRow.add(dangerDefault);

    AntButton ghostBtn = new AntButton("Ghost", ButtonType.PRIMARY);
    ghostBtn.setGhost(true);
    stateRow.add(ghostBtn);

    AntButton disabledBtn = new AntButton("Disabled", ButtonType.PRIMARY);
    disabledBtn.setEnabled(false);
    stateRow.add(disabledBtn);

    panel.add(stateRow, "wrap");

    // ---- 形状 ----
    panel.add(createSectionLabel("Shapes"), "wrap");
    JPanel shapeRow = new JPanel(new MigLayout("insets 0, gap 12"));
    shapeRow.add(new AntButton("Default", ButtonType.PRIMARY));

    AntButton roundBtn = new AntButton("Round", ButtonType.PRIMARY);
    roundBtn.setShape(ButtonShape.ROUND);
    shapeRow.add(roundBtn);

    AntButton circleBtn = new AntButton(
        AntIcons.outlined("search", 14, Color.WHITE), ButtonType.PRIMARY);
    circleBtn.setShape(ButtonShape.CIRCLE);
    shapeRow.add(circleBtn);

    panel.add(shapeRow, "wrap");

    // ---- 悬浮按钮 ----
    panel.add(createSectionLabel("Float Buttons"), "wrap");
    JPanel floatRow = new JPanel(new MigLayout("insets 0, gap 16, ay center"));

    AntFloatButton fab1 = new AntFloatButton(
        AntIcons.outlined("plus", 18, Color.WHITE));
    fab1.setTooltip("Add");
    floatRow.add(fab1);

    AntFloatButton fab2 = new AntFloatButton(
        AntIcons.outlined("question-circle", 18, Color.WHITE));
    fab2.setTooltip("Help");
    floatRow.add(fab2);

    AntFloatButton fab3 = new AntFloatButton(
        AntIcons.outlined("setting", 18), ButtonType.DEFAULT);
    fab3.setTooltip("Settings");
    floatRow.add(fab3);

    AntFloatButton fab4 = new AntFloatButton(
        AntIcons.outlined("upload", 18), ButtonType.DEFAULT);
    fab4.setDescription("Upload");
    floatRow.add(fab4);

    AntFloatButton fab5 = new AntFloatButton(
        AntIcons.outlined("edit", 18, Color.WHITE));
    fab5.setLarge(true);
    fab5.setTooltip("Edit (Large)");
    floatRow.add(fab5);

    panel.add(floatRow, "wrap");

    return wrapInScrollPane(panel);
  }

  // =========================================================================
  // 图标 Tab
  // =========================================================================

  private JScrollPane createIconsPanel() {
    JPanel panel = new JPanel(new MigLayout(
        "insets 24, gapy 16", "[grow,fill]"));

    ColorToken ct = AntThemeManager.getInstance().getColorToken();

    panel.add(createSectionLabel("Outlined Icons (16px)"), "wrap");
    iconGrid16 = new JPanel(new MigLayout("insets 8, gap 4"));
    fillIconGrid(iconGrid16, 16, ct);
    panel.add(iconGrid16, "wrap");

    panel.add(createSectionLabel("Outlined Icons (24px)"), "wrap");
    iconGrid24 = new JPanel(new MigLayout("insets 8, gap 4"));
    fillIconGrid(iconGrid24, 24, ct);
    panel.add(iconGrid24, "wrap");

    return wrapInScrollPane(panel);
  }

  /**
   * 向图标网格中填充图标和名称标签。
   *
   * @param grid       目标网格容器
   * @param iconSize   图标尺寸 (px)
   * @param colorToken 当前颜色令牌
   */
  private static void fillIconGrid(JPanel grid, int iconSize, ColorToken colorToken) {
    Color iconColor = colorToken.getTextColor();
    Color labelColor = colorToken.getTextSecondaryColor();

    for (String name : ICON_NAMES) {
      JPanel cell = new JPanel(new MigLayout("insets 8, flowy, align center"));
      cell.setPreferredSize(new Dimension(80, 70));

      ImageIcon icon = AntIcons.outlined(name, iconSize, iconColor);
      if (icon != null) {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(iconLabel, "align center");
      }

      JLabel nameLabel = new JLabel(name);
      nameLabel.setFont(new Font("PingFang SC", Font.PLAIN, 10));
      nameLabel.setForeground(labelColor);
      nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
      cell.add(nameLabel, "align center");

      grid.add(cell);
    }
  }

  // =========================================================================
  // 辅助方法
  // =========================================================================

  private static JLabel createSectionLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("PingFang SC", Font.BOLD, 16));
    label.setBorder(new EmptyBorder(8, 0, 4, 0));
    return label;
  }

  private static JScrollPane wrapInScrollPane(JPanel panel) {
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }
}
