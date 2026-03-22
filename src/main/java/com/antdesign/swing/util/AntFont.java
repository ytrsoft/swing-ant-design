package com.antdesign.swing.util;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ant Design 字体管理工具。
 *
 * <p>管理 Ant Design 推荐的字体栈，提供按尺寸和样式获取 {@link Font} 实例的方法，
 * 内部使用缓存避免重复创建。
 */
public final class AntFont {

  private AntFont() {}

  /** Ant Design 推荐字体族的优先级列表。 */
  private static final List<String> PREFERRED_FAMILIES = Arrays.asList(
      "Microsoft YaHei",     // Windows 中文
      "PingFang SC",         // macOS 中文
      "Noto Sans SC",        // Linux 中文
      "Segoe UI",            // Windows 西文
      "Roboto",              // Android / Material
      "Helvetica Neue",      // macOS 西文
      "Arial",               // 通用回退
      "SansSerif"            // JDK 逻辑字体
  );

  /** Ant Design 默认字号 (px)。 */
  public static final int FONT_SIZE = 14;
  public static final int FONT_SIZE_SM = 12;
  public static final int FONT_SIZE_LG = 16;
  public static final int FONT_SIZE_XL = 20;

  /** Ant Design 标题字号。 */
  public static final int FONT_SIZE_HEADING_1 = 38;
  public static final int FONT_SIZE_HEADING_2 = 30;
  public static final int FONT_SIZE_HEADING_3 = 24;
  public static final int FONT_SIZE_HEADING_4 = 20;
  public static final int FONT_SIZE_HEADING_5 = 16;

  /** 字体缓存。key = "family-style-size"。 */
  private static final Map<String, Font> CACHE = new LinkedHashMap<String, Font>(32, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Font> eldest) {
      return size() > 64;
    }
  };

  /** 首次可用的字体族名称（惰性初始化）。 */
  private static volatile String resolvedFamily;

  /**
   * 获取已解析的首选字体族名称。
   *
   * @return 当前系统中首个可用的推荐字体族
   */
  public static String family() {
    if (resolvedFamily == null) {
      synchronized (AntFont.class) {
        if (resolvedFamily == null) {
          resolvedFamily = resolveFamily();
        }
      }
    }
    return resolvedFamily;
  }

  /**
   * 获取默认字号（14）的常规字体。
   *
   * @return Font 实例
   */
  public static Font regular() {
    return of(Font.PLAIN, FONT_SIZE);
  }

  /**
   * 获取指定字号的常规字体。
   *
   * @param size 字号 (px)
   * @return Font 实例
   */
  public static Font regular(int size) {
    return of(Font.PLAIN, size);
  }

  /**
   * 获取指定字号的粗体字体。
   *
   * @param size 字号 (px)
   * @return Font 实例
   */
  public static Font bold(int size) {
    return of(Font.BOLD, size);
  }

  /**
   * 获取默认字号的粗体字体。
   *
   * @return Font 实例
   */
  public static Font bold() {
    return of(Font.BOLD, FONT_SIZE);
  }

  /**
   * 获取指定样式和字号的字体。
   *
   * @param style {@link Font#PLAIN}、{@link Font#BOLD} 或 {@link Font#ITALIC}
   * @param size 字号 (px)
   * @return Font 实例
   */
  public static Font of(int style, int size) {
    String key = family() + "-" + style + "-" + size;
    Font font = CACHE.get(key);
    if (font == null) {
      font = new Font(family(), style, size);
      CACHE.put(key, font);
    }
    return font;
  }

  /**
   * 获取标题字体。
   *
   * @param level 标题级别 1 ~ 5
   * @return 对应的粗体字体
   * @throws IllegalArgumentException level 不在 1 ~ 5 范围
   */
  public static Font heading(int level) {
    switch (level) {
      case 1: return bold(FONT_SIZE_HEADING_1);
      case 2: return bold(FONT_SIZE_HEADING_2);
      case 3: return bold(FONT_SIZE_HEADING_3);
      case 4: return bold(FONT_SIZE_HEADING_4);
      case 5: return bold(FONT_SIZE_HEADING_5);
      default:
        throw new IllegalArgumentException("Heading level must be 1-5, got: " + level);
    }
  }

  /**
   * 计算指定字体下文本的像素宽度。
   *
   * @param g Graphics 上下文
   * @param font 字体
   * @param text 文本
   * @return 像素宽度
   */
  public static int textWidth(Graphics g, Font font, String text) {
    FontMetrics fm = g.getFontMetrics(font);
    return fm.stringWidth(text);
  }

  private static String resolveFamily() {
    List<String> available = Arrays.asList(
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    for (String family : PREFERRED_FAMILIES) {
      if (available.contains(family)) {
        return family;
      }
    }
    return "SansSerif";
  }
}
