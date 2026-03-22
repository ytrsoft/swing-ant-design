package com.antdesign.swing.model;

/**
 * Ant Design 按钮类型。
 *
 * <p>对应 Ant Design {@code <Button type="...">} 属性，决定按钮的视觉风格。
 */
public enum ButtonType {

  /** 默认按钮：白底 + 边框。 */
  DEFAULT,

  /** 主按钮：实心主色背景。 */
  PRIMARY,

  /** 虚线按钮：虚线边框。 */
  DASHED,

  /** 文本按钮：无边框无背景，仅文字。 */
  TEXT,

  /** 链接按钮：无边框，主色文字，带下划线交互。 */
  LINK
}
