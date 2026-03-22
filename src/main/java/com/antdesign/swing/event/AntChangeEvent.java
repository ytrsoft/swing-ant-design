package com.antdesign.swing.event;

import lombok.Getter;

import java.util.EventObject;

/**
 * Ant Design 组件值变更事件。
 *
 * <p>当组件的值发生变化（如 Input 输入、Select 选中、Switch 切换）时触发。
 * 使用泛型 {@code <T>} 表示值类型，同时携带变更前后的值和可选的属性名称。
 *
 * <pre>{@code
 * input.addChangeListener((AntChangeEvent<String> e) -> {
 *   System.out.println(e.getOldValue() + " -> " + e.getNewValue());
 * });
 * }</pre>
 *
 * @param <T> 值的类型
 */
@Getter
public class AntChangeEvent<T> extends EventObject {

  private static final long serialVersionUID = 1L;

  /** 变更关联的属性名称，可为 {@code null}。 */
  private final String propertyName;

  /** 变更前的值，可为 {@code null}。 */
  private final T oldValue;

  /** 变更后的值，可为 {@code null}。 */
  private final T newValue;

  /** 事件发生时的毫秒时间戳。 */
  private final long timestamp;

  /**
   * 创建一个值变更事件。
   *
   * @param source 事件来源组件
   * @param propertyName 变更的属性名称
   * @param oldValue 变更前的值
   * @param newValue 变更后的值
   */
  public AntChangeEvent(Object source, String propertyName, T oldValue, T newValue) {
    super(source);
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * 创建一个不关联属性名称的值变更事件。
   *
   * @param source 事件来源组件
   * @param oldValue 变更前的值
   * @param newValue 变更后的值
   */
  public AntChangeEvent(Object source, T oldValue, T newValue) {
    this(source, null, oldValue, newValue);
  }
}
