package com.antdesign.swing.model;

import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 状态类型。
 *
 * <p>贯穿 Alert、Message、Notification、Result、Form 校验等场景，
 * 每种状态关联默认颜色与默认图标名称（对应 {@code icons/filled/} 目录下的 SVG 文件）。
 */
@Getter
public enum Status {

  SUCCESS(new Color(0x52, 0xC4, 0x1A), "check-circle"),
  WARNING(new Color(0xFA, 0xAD, 0x14), "warning"),
  ERROR(new Color(0xFF, 0x4D, 0x4F), "close-circle"),
  INFO(new Color(0x16, 0x77, 0xFF), "info-circle");

  /** 状态默认颜色。 */
  private final Color defaultColor;

  /** 状态默认图标名称（不含路径和扩展名）。 */
  private final String defaultIconName;

  Status(Color defaultColor, String defaultIconName) {
    this.defaultColor = defaultColor;
    this.defaultIconName = defaultIconName;
  }
}
