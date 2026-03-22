package com.antdesign.swing.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用图标资源加载器。
 *
 * <p>提供多尺寸 PNG 图标的统一加载，用于窗口图标列表、系统托盘图标等场景。
 * 图标资源存放在 {@code icons/app/} 目录下。
 *
 * <pre>{@code
 * // 设置窗口多尺寸图标（系统自动选择最佳尺寸）
 * frame.setIconImages(AppIconProvider.getWindowIcons());
 *
 * // 获取系统托盘图标
 * Image trayIcon = AppIconProvider.getTrayIcon();
 * }</pre>
 */
public final class AppIconProvider {

  private static final String ICON_BASE = "/com/antdesign/swing/icons/app/";

  /** 窗口图标可用尺寸列表。 */
  private static final int[] WINDOW_SIZES = {16, 20, 24, 32, 48, 64, 128, 256};

  private AppIconProvider() {}

  /**
   * 获取多尺寸窗口图标列表。
   *
   * <p>供 {@link java.awt.Window#setIconImages(List)} 使用，
   * 系统会根据平台和上下文自动选择最合适的尺寸。
   *
   * @return 图标 Image 列表（从小到大排列），加载失败时返回空列表
   */
  public static List<Image> getWindowIcons() {
    List<Image> icons = new ArrayList<>();
    for (int size : WINDOW_SIZES) {
      Image img = loadIcon("app-icon-" + size + ".png");
      if (img != null) {
        icons.add(img);
      }
    }
    return icons;
  }

  /**
   * 获取系统托盘图标。
   *
   * <p>返回 64px 的高分辨率图标，系统托盘会自动缩放到合适尺寸。
   *
   * @return 托盘图标 Image，加载失败时返回 {@code null}
   */
  public static Image getTrayIcon() {
    return loadIcon("tray-icon.png");
  }

  /**
   * 获取指定尺寸的应用图标。
   *
   * @param size 图标尺寸（16/20/24/32/48/64/128/256/512）
   * @return Image 实例，加载失败时返回 {@code null}
   */
  public static Image getIcon(int size) {
    return loadIcon("app-icon-" + size + ".png");
  }

  /**
   * 获取最大尺寸的应用图标（512px），可用于关于对话框等场景。
   *
   * @return 512px Image，加载失败时返回 {@code null}
   */
  public static Image getLargeIcon() {
    return loadIcon("app-icon-512.png");
  }

  private static Image loadIcon(String name) {
    try (InputStream is = AppIconProvider.class.getResourceAsStream(ICON_BASE + name)) {
      if (is == null) {
        return null;
      }
      return ImageIO.read(is);
    } catch (Exception e) {
      return null;
    }
  }
}
