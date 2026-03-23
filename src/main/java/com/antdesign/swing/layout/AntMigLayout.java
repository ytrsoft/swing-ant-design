package com.antdesign.swing.layout;

import com.antdesign.swing.general.AntPanel;
import com.antdesign.swing.theme.token.SizeToken;
import net.miginfocom.swing.MigLayout;

import java.awt.*;

/**
 * 基于 MigLayout 的 Ant Design 布局容器。
 *
 * <p>对 {@link MigLayout} 进行封装，提供 Java 链式 API 和 Ant Design
 * 主题令牌集成。直接使用 MigLayout 字符串约束时仍然完全兼容。
 *
 * <h3>基本用法</h3>
 * <pre>{@code
 * // 1. 直接传入 MigLayout 字符串约束
 * AntMigLayout panel = new AntMigLayout("wrap 2", "[grow,fill][grow,fill]", "[]8[]");
 * panel.add(nameLabel, "right");
 * panel.add(nameInput, "growx");
 *
 * // 2. Builder API
 * AntMigLayout panel = AntMigLayout.builder()
 *     .wrap(3)
 *     .gap(16, 12)
 *     .fillX()
 *     .insets(16)
 *     .build();
 * panel.add(card1, "grow");
 * panel.add(card2, "grow");
 * panel.add(card3, "grow");
 *
 * // 3. 表单快捷方法
 * AntMigLayout form = AntMigLayout.form(100);
 * form.addRow("用户名：", usernameInput);
 * form.addRow("密码：", passwordInput);
 * form.addFullRow(submitButton);
 *
 * // 4. 卡片网格快捷方法
 * AntMigLayout grid = AntMigLayout.cardGrid(3, 16);
 * grid.add(card1);
 * grid.add(card2);
 * grid.add(card3);
 * }</pre>
 *
 * <h3>与原生 MigLayout 的关系</h3>
 * <p>所有 MigLayout 字符串约束均可直接使用。本类的 Builder API
 * 只是生成对应的约束字符串，不引入任何自定义布局逻辑。
 * 可通过 {@link #getMigLayout()} 获取底层 MigLayout 实例。
 *
 * @see MigLayout
 * @see <a href="http://www.miglayout.com/whitepaper.html">MigLayout White Paper</a>
 */
public class AntMigLayout extends AntPanel {

  private static final long serialVersionUID = 1L;

  private final MigLayout migLayout;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 使用完整 MigLayout 字符串约束创建布局。
   *
   * @param layoutConstraints 布局约束（如 {@code "wrap 2, fill, insets 16"}）
   * @param colConstraints    列约束（如 {@code "[right]16[grow,fill]"}）
   * @param rowConstraints    行约束（如 {@code "[]8[]"}）
   */
  public AntMigLayout(String layoutConstraints, String colConstraints,
      String rowConstraints) {
    migLayout = new MigLayout(layoutConstraints, colConstraints, rowConstraints);
    setLayout(migLayout);
    setOpaque(false);
  }

  /**
   * 使用布局约束和列约束创建。
   *
   * @param layoutConstraints 布局约束
   * @param colConstraints    列约束
   */
  public AntMigLayout(String layoutConstraints, String colConstraints) {
    this(layoutConstraints, colConstraints, "");
  }

  /**
   * 仅使用布局约束创建。
   *
   * @param layoutConstraints 布局约束
   */
  public AntMigLayout(String layoutConstraints) {
    this(layoutConstraints, "", "");
  }

  /** 创建默认空布局。 */
  public AntMigLayout() {
    this("", "", "");
  }

  /** 由 Builder 内部调用。 */
  private AntMigLayout(MigLayout ml) {
    this.migLayout = ml;
    setLayout(migLayout);
    setOpaque(false);
  }

  // =========================================================================
  // 添加组件
  // =========================================================================

  /**
   * 添加组件并指定 MigLayout 组件约束。
   *
   * @param comp       组件
   * @param constraint MigLayout 约束字符串（如 {@code "span 2, growx, wrap"}）
   * @return this（链式调用）
   */
  public AntMigLayout addItem(Component comp, String constraint) {
    add(comp, constraint);
    return this;
  }

  /**
   * 添加组件（无约束）。
   *
   * @param comp 组件
   * @return this
   */
  public AntMigLayout addItem(Component comp) {
    add(comp);
    return this;
  }

  // =========================================================================
  // 表单辅助方法
  // =========================================================================

  /**
   * 添加一行表单字段：标签在左，输入组件在右并自动换行。
   *
   * @param label     标签文本
   * @param component 输入组件
   * @return this
   */
  public AntMigLayout addRow(String label, Component component) {
    javax.swing.JLabel lbl = new javax.swing.JLabel(label);
    lbl.setFont(fontToken().createFont(fontToken().getFontSize(), Font.PLAIN));
    lbl.setForeground(colorToken().getTextColor());
    add(lbl);
    add(component, "growx, wrap");
    return this;
  }

  /**
   * 添加一行占满两列的组件。
   *
   * @param component 组件
   * @return this
   */
  public AntMigLayout addFullRow(Component component) {
    add(component, "span, growx, wrap");
    return this;
  }

  /**
   * 添加一行占满两列的组件，右对齐（用于按钮行）。
   *
   * @param component 组件
   * @return this
   */
  public AntMigLayout addFullRowRight(Component component) {
    add(component, "span, right, wrap");
    return this;
  }

  /**
   * 添加垂直间距行。
   *
   * @param height 间距像素
   * @return this
   */
  public AntMigLayout addGap(int height) {
    add(new javax.swing.Box.Filler(
            new Dimension(0, height),
            new Dimension(0, height),
            new Dimension(Short.MAX_VALUE, height)),
        "span, wrap");
    return this;
  }

  /**
   * 添加分割线行。
   *
   * @return this
   */
  public AntMigLayout addSeparator() {
    javax.swing.JSeparator sep = new javax.swing.JSeparator();
    sep.setForeground(colorToken().getBorderSecondaryColor());
    add(sep, "span, growx, wrap, gaptop 8, gapbottom 8");
    return this;
  }

  // =========================================================================
  // 获取底层对象
  // =========================================================================

  /**
   * 获取底层 MigLayout 实例，用于高级操作。
   *
   * @return MigLayout 实例
   */
  public MigLayout getMigLayout() {
    return migLayout;
  }

  // =========================================================================
  // 静态工厂方法
  // =========================================================================

  /**
   * 创建表单布局。
   *
   * <pre>{@code
   * AntMigLayout form = AntMigLayout.form(100);
   * form.addRow("用户名：", nameInput);
   * form.addRow("密码：",   pwdInput);
   * form.addFullRow(submitBtn);
   * }</pre>
   *
   * @param labelWidth 标签列宽度（像素）
   * @return 表单布局容器
   */
  public static AntMigLayout form(int labelWidth) {
    SizeToken st = themeManager().getSizeToken();
    int gap = st.getMarginSm();
    return new AntMigLayout(
        "wrap 2, insets " + st.getPadding(),
        "[" + labelWidth + ", right]" + gap + "[grow, fill]",
        "[]" + gap + "[]"
    );
  }

  /**
   * 创建带主题间距的表单布局（标签宽度 100）。
   *
   * @return 表单布局容器
   */
  public static AntMigLayout form() {
    return form(100);
  }

  /**
   * 创建卡片网格布局。
   *
   * <pre>{@code
   * AntMigLayout grid = AntMigLayout.cardGrid(3, 16);
   * grid.add(card1);
   * grid.add(card2);
   * grid.add(card3);
   * grid.add(card4); // 自动换行到第二行
   * }</pre>
   *
   * @param columns 列数
   * @param gap     间距（像素）
   * @return 网格布局容器
   */
  public static AntMigLayout cardGrid(int columns, int gap) {
    StringBuilder colSpec = new StringBuilder();
    for (int i = 0; i < columns; i++) {
      if (i > 0) colSpec.append(gap);
      colSpec.append("[grow, fill]");
    }
    return new AntMigLayout(
        "wrap " + columns + ", fillx, insets 0",
        colSpec.toString(),
        "[]" + gap + "[]"
    );
  }

  /**
   * 创建卡片网格（使用主题间距）。
   *
   * @param columns 列数
   * @return 网格布局容器
   */
  public static AntMigLayout cardGrid(int columns) {
    return cardGrid(columns, themeManager().getSizeToken().getMarginSm());
  }

  /**
   * 创建水平工具栏布局。
   *
   * <pre>{@code
   * AntMigLayout toolbar = AntMigLayout.horizontal(8);
   * toolbar.add(btn1);
   * toolbar.add(btn2);
   * toolbar.add(btn3);
   * }</pre>
   *
   * @param gap 按钮间距
   * @return 水平布局容器
   */
  public static AntMigLayout horizontal(int gap) {
    return new AntMigLayout("insets 0, gapx " + gap, "", "[]");
  }

  /**
   * 创建水平工具栏（主题间距）。
   *
   * @return 水平布局容器
   */
  public static AntMigLayout horizontal() {
    return horizontal(themeManager().getSizeToken().getMarginXs());
  }

  /**
   * 创建垂直列表布局。
   *
   * @param gap 行间距
   * @return 垂直布局容器
   */
  public static AntMigLayout vertical(int gap) {
    return new AntMigLayout(
        "wrap 1, fillx, insets 0",
        "[grow, fill]",
        "[]" + gap + "[]"
    );
  }

  /**
   * 创建垂直列表（主题间距）。
   *
   * @return 垂直布局容器
   */
  public static AntMigLayout vertical() {
    return vertical(themeManager().getSizeToken().getMarginSm());
  }

  /**
   * 创建居中布局（单个子组件水平+垂直居中）。
   *
   * @return 居中布局容器
   */
  public static AntMigLayout center() {
    return new AntMigLayout("fill", "[center]", "[center]");
  }

  // =========================================================================
  // Builder
  // =========================================================================

  /**
   * 创建 Builder 实例。
   *
   * <pre>{@code
   * AntMigLayout panel = AntMigLayout.builder()
   *     .wrap(3)
   *     .gap(16, 12)
   *     .fillX()
   *     .insets(16)
   *     .columns("[grow,fill]16[grow,fill]16[grow,fill]")
   *     .build();
   * }</pre>
   *
   * @return Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * 布局构建器。
   *
   * <p>将链式方法调用转换为 MigLayout 字符串约束。
   */
  public static class Builder {

    private final StringBuilder layout = new StringBuilder();
    private String cols = "";
    private String rows = "";

    // ---------------------------------------------------------------
    // 布局约束
    // ---------------------------------------------------------------

    /** 设置每行自动换行的列数。 */
    public Builder wrap(int columns) { append("wrap " + columns); return this; }

    /** 每个组件占一行（等同于 wrap 1）。 */
    public Builder wrap() { append("wrap"); return this; }

    /** 子组件水平方向填充。 */
    public Builder fillX() { append("fillx"); return this; }

    /** 子组件垂直方向填充。 */
    public Builder fillY() { append("filly"); return this; }

    /** 子组件双向填充。 */
    public Builder fill() { append("fill"); return this; }

    /** 设置四边内边距（像素）。 */
    public Builder insets(int all) {
      append("insets " + all);
      return this;
    }

    /** 设置四边内边距（像素）。 */
    public Builder insets(int top, int left, int bottom, int right) {
      append("insets " + top + " " + left + " " + bottom + " " + right);
      return this;
    }

    /** 使用主题 padding 作为内边距。 */
    public Builder themeInsets() {
      int p = themeManager().getSizeToken().getPadding();
      return insets(p);
    }

    /** 设置水平和垂直间距（像素）。 */
    public Builder gap(int horizontal, int vertical) {
      append("gapx " + horizontal);
      append("gapy " + vertical);
      return this;
    }

    /** 设置统一间距。 */
    public Builder gap(int all) {
      return gap(all, all);
    }

    /** 使用主题间距。 */
    public Builder themeGap() {
      SizeToken st = themeManager().getSizeToken();
      return gap(st.getMarginSm(), st.getMarginSm());
    }

    /** 布局方向从右到左。 */
    public Builder rtl() { append("rtl"); return this; }

    /** 隐藏组件时不保留空间。 */
    public Builder hideMode(int mode) { append("hidemode " + mode); return this; }

    /** 调试模式（显示组件边框）。 */
    public Builder debug() { append("debug"); return this; }

    /** 追加任意布局约束字符串。 */
    public Builder layoutConstraint(String constraint) {
      append(constraint);
      return this;
    }

    // ---------------------------------------------------------------
    // 列约束
    // ---------------------------------------------------------------

    /**
     * 设置列约束字符串。
     *
     * <p>示例：{@code "[right, 100]16[grow, fill]"}
     *
     * @param colConstraints MigLayout 列约束
     */
    public Builder columns(String colConstraints) {
      this.cols = colConstraints;
      return this;
    }

    /**
     * 创建等宽列。
     *
     * @param count 列数
     * @param gap   列间距
     */
    public Builder equalColumns(int count, int gap) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < count; i++) {
        if (i > 0) sb.append(gap);
        sb.append("[grow, fill]");
      }
      this.cols = sb.toString();
      return this;
    }

    /** 创建等宽列（主题间距）。 */
    public Builder equalColumns(int count) {
      return equalColumns(count, themeManager().getSizeToken().getMarginSm());
    }

    // ---------------------------------------------------------------
    // 行约束
    // ---------------------------------------------------------------

    /**
     * 设置行约束字符串。
     *
     * @param rowConstraints MigLayout 行约束
     */
    public Builder rows(String rowConstraints) {
      this.rows = rowConstraints;
      return this;
    }

    // ---------------------------------------------------------------
    // 构建
    // ---------------------------------------------------------------

    /** 构建 AntMigLayout 实例。 */
    public AntMigLayout build() {
      MigLayout ml = new MigLayout(layout.toString().trim(), cols, rows);
      return new AntMigLayout(ml);
    }

    private void append(String part) {
      if (layout.length() > 0) layout.append(", ");
      layout.append(part);
    }
  }

  // =========================================================================
  // 便捷约束字符串常量
  // =========================================================================

  /** 水平增长并填充。 */
  public static final String GROW_X = "growx";

  /** 垂直增长并填充。 */
  public static final String GROW_Y = "growy";

  /** 双向增长并填充。 */
  public static final String GROW = "grow";

  /** 跨越所有列。 */
  public static final String SPAN = "span";

  /** 跨越所有列并换行。 */
  public static final String SPAN_WRAP = "span, growx, wrap";

  /** 右对齐。 */
  public static final String RIGHT = "right";

  /** 居中对齐。 */
  public static final String CENTER = "center";

  /** 换行。 */
  public static final String WRAP = "wrap";

  /** 跨越 N 列的约束字符串。 */
  public static String span(int cols) {
    return "span " + cols;
  }

  /** 跨越 N 列并换行。 */
  public static String spanWrap(int cols) {
    return "span " + cols + ", growx, wrap";
  }

  /** 指定宽度（像素）。 */
  public static String width(int px) {
    return "width " + px + "!";
  }

  /** 指定最小宽度。 */
  public static String minWidth(int px) {
    return "wmin " + px;
  }

  /** 指定高度（像素）。 */
  public static String height(int px) {
    return "height " + px + "!";
  }
}
