package com.antdesign.swing.base;

import com.antdesign.swing.theme.AntTheme;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;

import java.util.function.Consumer;

/**
 * 主题监听管理的委托助手。
 *
 * <p>封装主题变更监听器的注册、反注册和令牌便捷访问逻辑，供
 * {@link AbstractAntComponent}（继承 JComponent）和
 * {@link AbstractAntPanel}（继承 JPanel）共享，避免代码重复。
 *
 * <p>使用方式：在基类中持有本类实例，将主题监听的生命周期方法委托给它。
 *
 * @see AbstractAntComponent
 * @see AbstractAntPanel
 */
final class ThemeListenerSupport {

  /** 当前注册的主题监听器引用。 */
  private Consumer<AntTheme> themeListener;

  /**
   * 设置主题变更监听器。已存在旧监听器则先移除。
   *
   * @param listener 新的监听器，{@code null} 表示不监听
   */
  void setThemeListener(Consumer<AntTheme> listener) {
    if (this.themeListener != null) {
      themeManager().removeThemeChangeListener(this.themeListener);
    }
    this.themeListener = listener;
    if (listener != null) {
      themeManager().addThemeChangeListener(listener);
    }
  }

  /** 反注册当前监听器。应在组件 {@code removeNotify()} 中调用。 */
  void unregister() {
    if (themeListener != null) {
      themeManager().removeThemeChangeListener(themeListener);
    }
  }

  // =========================================================================
  // 主题令牌便捷访问
  // =========================================================================

  static AntThemeManager themeManager() {
    return AntThemeManager.getInstance();
  }

  static ColorToken colorToken() {
    return AntThemeManager.getInstance().getColorToken();
  }

  static FontToken fontToken() {
    return AntThemeManager.getInstance().getFontToken();
  }

  static SizeToken sizeToken() {
    return AntThemeManager.getInstance().getSizeToken();
  }
}
