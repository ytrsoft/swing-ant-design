package com.antdesign.swing.event;

import java.util.EventListener;

/**
 * Ant Design 动作事件监听器。
 *
 * <p>函数式接口，用于监听 {@link AntActionEvent}。适用于按钮点击、菜单项选中等
 * 一次性触发的用户动作。
 *
 * <pre>{@code
 * AntActionListener listener = e -> handleClick(e);
 * }</pre>
 *
 * @see AntActionEvent
 */
@FunctionalInterface
public interface AntActionListener extends EventListener {

  /**
   * 当动作发生时被调用。
   *
   * @param event 动作事件
   */
  void actionPerformed(AntActionEvent event);
}
