package com.antdesign.swing.model;

import lombok.Getter;

/**
 * Ant Design 弹出层方位。
 *
 * <p>适用于 Tooltip、Popover、Popconfirm、Dropdown、Drawer 等需要指定弹出方向的组件。
 * 12 个方位值与 Ant Design 的 {@code placement} 属性一一对应。
 */
@Getter
public enum Placement {

  TOP("top", true),
  TOP_LEFT("topLeft", true),
  TOP_RIGHT("topRight", true),

  BOTTOM("bottom", true),
  BOTTOM_LEFT("bottomLeft", true),
  BOTTOM_RIGHT("bottomRight", true),

  LEFT("left", false),
  LEFT_TOP("leftTop", false),
  LEFT_BOTTOM("leftBottom", false),

  RIGHT("right", false),
  RIGHT_TOP("rightTop", false),
  RIGHT_BOTTOM("rightBottom", false);

  /** 对应 Ant Design 中的字符串值。 */
  private final String value;

  /** 主方向是否为垂直方向（上/下）。 */
  private final boolean vertical;

  Placement(String value, boolean vertical) {
    this.value = value;
    this.vertical = vertical;
  }

  /** 主方向是否为水平方向（左/右）。 */
  public boolean isHorizontal() {
    return !vertical;
  }

  /**
   * 返回主方向的对面方位。
   *
   * <p>例如 {@code TOP → BOTTOM}、{@code LEFT_TOP → RIGHT_TOP}。
   * 常用于弹出层因空间不足而自动翻转的场景。
   */
  public Placement opposite() {
    switch (this) {
      case TOP:          return BOTTOM;
      case TOP_LEFT:     return BOTTOM_LEFT;
      case TOP_RIGHT:    return BOTTOM_RIGHT;
      case BOTTOM:       return TOP;
      case BOTTOM_LEFT:  return TOP_LEFT;
      case BOTTOM_RIGHT: return TOP_RIGHT;
      case LEFT:         return RIGHT;
      case LEFT_TOP:     return RIGHT_TOP;
      case LEFT_BOTTOM:  return RIGHT_BOTTOM;
      case RIGHT:        return LEFT;
      case RIGHT_TOP:    return LEFT_TOP;
      case RIGHT_BOTTOM: return LEFT_BOTTOM;
      default:           return this;
    }
  }
}
