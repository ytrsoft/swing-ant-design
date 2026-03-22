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
 * 所有基于 {@link JComponent} 的 Ant Design 组件的公共基类。
 *
 * <p>提供主题变更监听、{@link Graphics2D} 生命周期管理、主题令牌便捷访问
 * 和属性变更辅助方法。子类应覆写 {@link #paintAnt(Graphics2D, int, int)}
 * 而非直接覆写 {@link #paintComponent(Graphics)}。
 *
 * @see AbstractAntPanel
 * @see AntThemeManager
 */
public abstract class AbstractAntComponent extends JComponent {

  private static final long serialVersionUID = 1L;

  private final ThemeListenerSupport themeSupport = new ThemeListenerSupport();

  /** 创建组件并注册默认主题监听器。 */
  protected AbstractAntComponent() {
    setThemeListener(this::changeTheme);
  }

  /** 默认主题变更处理：更新背景色。 */
  protected void changeTheme(AntTheme theme) {
    setOpaque(false);
    setBackground(theme.getColorToken().getBgContainer());
  }

  // =========================================================================
  // 主题监听管理（委托给 ThemeListenerSupport）
  // =========================================================================

  /**
   * 设置主题变更监听器。已存在旧监听器则先移除。
   *
   * @param listener 新的监听器，{@code null} 表示不监听
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
  // Graphics2D 生命周期
  // =========================================================================

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      GraphicsUtils.setupAntialiasing(g2);
      paintAnt(g2, getWidth(), getHeight());
    } finally {
      g2.dispose();
    }
  }

  /**
   * 子类覆写此方法实现绘制逻辑。调用时已完成反锯齿设置。
   *
   * @param g2     已设置反锯齿的 Graphics2D
   * @param width  组件宽度
   * @param height 组件高度
   */
  protected void paintAnt(Graphics2D g2, int width, int height) {
    // 默认空实现，子类覆写
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
  // 属性变更辅助
  // =========================================================================

  /** 设置属性后仅触发重绘。 */
  protected void updateAndRepaint(Runnable action) {
    action.run();
    repaint();
  }

  /** 设置属性后同时触发重新布局和重绘。 */
  protected void updateAndRelayout(Runnable action) {
    action.run();
    revalidate();
    repaint();
  }
}
