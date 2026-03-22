package com.antdesign.swing.base;

import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 所有基于 {@link JPanel} 的 Ant Design 组件的公共基类。
 *
 * <p>与 {@link AbstractAntComponent} 功能一致，但继承自 {@link JPanel}，
 * 适用于包含子组件布局的容器型组件。需要自定义绘制的子类可覆写
 * {@link #paintAnt(Graphics2D, int, int)}。
 *
 * @see AbstractAntComponent
 */
public abstract class AbstractAntPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private final ThemeListenerSupport themeSupport = new ThemeListenerSupport();

  /** 创建面板并注册默认主题监听器。 */
  protected AbstractAntPanel() {
    setThemeListener(this::changeTheme);
  }

  /**
   * 创建面板并指定布局管理器。
   *
   * @param layout 布局管理器
   */
  protected AbstractAntPanel(LayoutManager layout) {
    super(layout);
    setThemeListener(this::changeTheme);
  }

  /** 默认主题变更处理：更新背景色。 */
  protected void changeTheme(AntTheme theme) {
    setBackground(theme.getColorToken().getBgContainer());
  }

  // =========================================================================
  // 主题监听管理（委托给 ThemeListenerSupport）
  // =========================================================================

  /**
   * 设置主题变更监听器。已存在旧监听器则先移除。
   *
   * @param listener 新的监听器
   */
  protected final void setThemeListener(Consumer<AntTheme> listener) {
    themeSupport.setThemeListener(listener);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    themeSupport.unregister();
  }

  // =========================================================================
  // 主题令牌便捷访问（委托给 ThemeListenerSupport）
  // =========================================================================

  protected static AntThemeManager themeManager() {
    return ThemeListenerSupport.themeManager();
  }

  protected static ColorToken colorToken() {
    return ThemeListenerSupport.colorToken();
  }

  protected static FontToken fontToken() {
    return ThemeListenerSupport.fontToken();
  }

  protected static SizeToken sizeToken() {
    return ThemeListenerSupport.sizeToken();
  }

  // =========================================================================
  // Graphics2D 生命周期
  // =========================================================================

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      GraphicsUtils.setupAntialiasing(g2);
      paintAnt(g2, getWidth(), getHeight());
    } finally {
      g2.dispose();
    }
  }

  /**
   * 子类覆写此方法实现自定义绘制。默认为空。
   *
   * @param g2     已设置反锯齿的 Graphics2D
   * @param width  组件宽度
   * @param height 组件高度
   */
  protected void paintAnt(Graphics2D g2, int width, int height) {
    // 默认空实现
  }

  // =========================================================================
  // 属性变更辅助
  // =========================================================================

  protected void updateAndRepaint(Runnable action) {
    action.run();
    repaint();
  }

  protected void updateAndRelayout(Runnable action) {
    action.run();
    revalidate();
    repaint();
  }
}
