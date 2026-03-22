package com.antdesign.swing.painter;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 组件绘制器接口。
 *
 * <p>定义组件自定义绘制的统一契约。每个实现类负责绘制特定类型组件的背景、边框、
 * 前景等视觉元素，并在绘制时引用当前主题令牌以保证风格一致。
 *
 * <p>绘制器遵循以下约定：
 * <ul>
 *   <li>绘制操作不应修改传入的 {@link Graphics2D} 状态，需在绘制前后自行保存/恢复。</li>
 *   <li>绘制器应当是无状态的，可安全在多组件间共享。</li>
 *   <li>绘制范围由组件自身的 {@code width} 和 {@code height} 决定。</li>
 * </ul>
 *
 * @see ButtonPainter
 * @see InputPainter
 * @see RoundedBorderPainter
 */
public interface AntPainter {

  /**
   * 绘制组件的自定义视觉效果。
   *
   * <p>实现类应在方法内部创建 {@link Graphics2D} 的副本进行绘制，
   * 避免影响后续绘制流程。
   *
   * @param g2 当前图形上下文
   * @param component 被绘制的目标组件
   * @param width 绘制区域宽度
   * @param height 绘制区域高度
   */
  void paint(Graphics2D g2, JComponent component, int width, int height);
}
