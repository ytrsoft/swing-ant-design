package com.antdesign.swing.event;

import java.util.EventListener;

/**
 * Ant Design 值变更事件监听器。
 *
 * <p>函数式接口，用于监听 {@link AntChangeEvent}。适用于 Input 文本变更、Select 选中变更、
 * Switch 开关切换、Slider 滑动等持续性值变化的场景。
 *
 * <pre>{@code
 * AntChangeListener<String> listener = e -> {
 *   System.out.println(e.getOldValue() + " -> " + e.getNewValue());
 * };
 * }</pre>
 *
 * @param <T> 值的类型
 * @see AntChangeEvent
 */
@FunctionalInterface
public interface AntChangeListener<T> extends EventListener {

  /**
   * 当值发生变化时被调用。
   *
   * @param event 值变更事件
   */
  void valueChanged(AntChangeEvent<T> event);
}
