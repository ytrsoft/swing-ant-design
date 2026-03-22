package com.antdesign.swing.event;

import lombok.Getter;

import java.util.EventObject;

/**
 * Ant Design 组件动作事件。
 *
 * <p>当用户执行明确的交互动作（如按钮点击、菜单项选中）时触发。
 * 继承自 {@link EventObject}，额外携带动作命令标识和事件时间戳。
 *
 * <pre>{@code
 * button.addActionListener(e -> {
 *   System.out.println("command: " + e.getActionCommand());
 * });
 * }</pre>
 */
@Getter
public class AntActionEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  /** 动作命令标识，用于区分同一来源的不同动作。 */
  private final String actionCommand;

  /** 事件发生时的毫秒时间戳。 */
  private final long timestamp;

  /**
   * 创建一个动作事件。
   *
   * @param source 事件来源组件
   * @param actionCommand 动作命令标识
   */
  public AntActionEvent(Object source, String actionCommand) {
    super(source);
    this.actionCommand = actionCommand;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * 创建一个无命令标识的动作事件。
   *
   * @param source 事件来源组件
   */
  public AntActionEvent(Object source) {
    this(source, "");
  }
}
