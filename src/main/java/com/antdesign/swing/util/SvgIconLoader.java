package com.antdesign.swing.util;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SVG 图标加载器。
 *
 * <p>从 classpath 资源目录加载 SVG 文件，支持按尺寸渲染、颜色着色和内存缓存。
 * SVG 文件中的 {@code fill="currentColor"} 属性会被替换为指定的着色颜色。
 *
 * <p>资源路径约定：{@code /com/antdesign/swing/icons/{style}/{name}.svg}，
 * 其中 style 为 {@code outlined} 或 {@code filled}。
 *
 * <p>自动适配 HiDPI 显示，在高分辨率屏幕上以实际物理像素渲染，
 * 避免图标缩放模糊。
 */
public final class SvgIconLoader {

  private static final Logger LOG = LoggerFactory.getLogger(SvgIconLoader.class);

  /** 图标资源基路径。 */
  private static final String ICONS_BASE = "/com/antdesign/swing/icons/";

  /** SVG 宇宙实例（线程安全）。 */
  private static final SVGUniverse UNIVERSE = new SVGUniverse();

  /** 渲染结果缓存。key = "style/name-size-colorHex-scale"。 */
  private static final ConcurrentHashMap<String, ImageIcon> CACHE =
      new ConcurrentHashMap<>();

  /** 显示缩放因子（启动时检测一次）。 */
  private static final int SCALE_FACTOR = detectScaleFactor();

  private SvgIconLoader() {}

  /**
   * 加载 SVG 图标并渲染为 {@link ImageIcon}。
   *
   * @param style 图标风格，{@code "outlined"} 或 {@code "filled"}
   * @param name  图标名称（不含扩展名），如 {@code "search"}
   * @param size  渲染尺寸 (px)，宽高相等
   * @param color 着色颜色，替换 SVG 中的 {@code currentColor}；为 {@code null} 时使用黑色
   * @return 渲染后的 ImageIcon；加载失败时返回一个空白透明图标
   */
  public static ImageIcon load(String style, String name, int size, Color color) {
    Color tint = color != null ? color : Color.BLACK;
    String colorHex = ColorUtils.toHex(tint);
    String cacheKey = style + "/" + name + "-" + size + "-" + colorHex
        + "-" + SCALE_FACTOR;

    ImageIcon cached = CACHE.get(cacheKey);
    if (cached != null) {
      return cached;
    }

    ImageIcon icon = render(style, name, size, tint);
    CACHE.put(cacheKey, icon);
    return icon;
  }

  /**
   * 以默认黑色加载 outlined 风格图标。
   *
   * @param name 图标名称
   * @param size 渲染尺寸
   * @return ImageIcon
   */
  public static ImageIcon load(String name, int size) {
    return load("outlined", name, size, null);
  }

  /**
   * 以指定颜色加载 outlined 风格图标。
   *
   * @param name  图标名称
   * @param size  渲染尺寸
   * @param color 着色颜色
   * @return ImageIcon
   */
  public static ImageIcon load(String name, int size, Color color) {
    return load("outlined", name, size, color);
  }

  /**
   * 清空全部缓存。主题切换后应调用此方法。
   */
  public static void clearCache() {
    CACHE.clear();
  }

  // ---------------------------------------------------------------------------
  // 内部实现
  // ---------------------------------------------------------------------------

  private static ImageIcon render(String style, String name, int size, Color tint) {
    String resourcePath = ICONS_BASE + style + "/" + name + ".svg";
    try (InputStream is = SvgIconLoader.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        LOG.warn("SVG icon not found: {}", resourcePath);
        return createEmptyIcon(size);
      }

      // 读取 SVG 内容并替换 currentColor
      String svgContent = readStream(is);
      String tintHex = ColorUtils.toHex(tint);
      svgContent = svgContent.replace("currentColor", tintHex);

      // 修复 SVG arc 命令中省略的分隔符（svgSalamander 不支持）
      svgContent = fixArcFlagSyntax(svgContent);

      // 加载到 SVG Universe
      String uniqueName = style + "-" + name + "-" + tintHex + "-"
          + System.nanoTime();
      InputStream tintedStream = new java.io.ByteArrayInputStream(
          svgContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      URI uri = UNIVERSE.loadSVG(tintedStream, uniqueName);
      SVGDiagram diagram = UNIVERSE.getDiagram(uri);
      if (diagram == null) {
        LOG.warn("Failed to parse SVG: {}", resourcePath);
        return createEmptyIcon(size);
      }
      diagram.setIgnoringClipHeuristic(true);

      // HiDPI: 以实际物理像素渲染
      int renderSize = size * SCALE_FACTOR;

      BufferedImage image = new BufferedImage(
          renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = image.createGraphics();
      try {
        GraphicsUtils.setupAntialiasing(g2);

        // 缩放 SVG 到 renderSize（物理像素）
        float svgWidth = diagram.getWidth();
        float svgHeight = diagram.getHeight();
        if (svgWidth > 0 && svgHeight > 0) {
          float scale = Math.min(renderSize / svgWidth, renderSize / svgHeight);
          float offsetX = (renderSize - svgWidth * scale) / 2f;
          float offsetY = (renderSize - svgHeight * scale) / 2f;
          g2.translate(offsetX, offsetY);
          g2.scale(scale, scale);
        }
        diagram.render(g2);
      } catch (SVGException e) {
        LOG.warn("Failed to render SVG: {}", resourcePath, e);
      } finally {
        g2.dispose();
      }

      // 清理 SVG Universe 中的临时条目
      UNIVERSE.removeDocument(uri);

      return new HiDpiImageIcon(image, size);
    } catch (IOException e) {
      LOG.warn("Failed to load SVG: {}", resourcePath, e);
      return createEmptyIcon(size);
    }
  }

  private static ImageIcon createEmptyIcon(int size) {
    return new HiDpiImageIcon(
        new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB), size);
  }

  private static String readStream(InputStream is) throws IOException {
    byte[] buffer = new byte[4096];
    StringBuilder sb = new StringBuilder();
    int bytesRead;
    while ((bytesRead = is.read(buffer)) != -1) {
      sb.append(new String(buffer, 0, bytesRead,
          java.nio.charset.StandardCharsets.UTF_8));
    }
    return sb.toString();
  }

  /**
   * 检测显示器缩放因子。
   *
   * @return 缩放倍数，最小为 1
   */
  private static int detectScaleFactor() {
    try {
      GraphicsEnvironment ge =
          GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      AffineTransform tx = gc.getDefaultTransform();
      double sx = tx.getScaleX();
      double sy = tx.getScaleY();
      return Math.max(1, (int) Math.ceil(Math.max(sx, sy)));
    } catch (Exception e) {
      return 1;
    }
  }

  /**
   * 修复 SVG path 中 arc 命令的 flag 参数省略分隔符问题。
   *
   * <p>SVG 规范允许 arc 的 large-arc-flag 和 sweep-flag（均为 {@code 0} 或 {@code 1}）
   * 与后续参数之间省略空白或逗号，例如 {@code a48 48 0 010-96}
   * （等价于 {@code a48 48 0 0 1 0 -96}）。
   * svgSalamander 无法解析此简写，需要在传入前补齐分隔符。
   *
   * @param svgContent 原始 SVG 文本
   * @return 修复后的 SVG 文本
   */
  private static String fixArcFlagSyntax(String svgContent) {
    Pattern arcFlagPattern = Pattern.compile(
        "([-+]?[\\d.]+[\\s,]+[-+]?[\\d.]+[\\s,]+[-+]?[\\d.]+[\\s,]+)"
        + "([01])([01])([\\d.+-])");

    String result = svgContent;
    Matcher m = arcFlagPattern.matcher(result);
    while (m.find()) {
      result = m.replaceAll("$1$2 $3 $4");
      m = arcFlagPattern.matcher(result);
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // HiDPI 图标
  // ---------------------------------------------------------------------------

  /**
   * 支持 HiDPI 的 ImageIcon。
   *
   * <p>内部持有高分辨率图像，但对外报告逻辑尺寸。
   * 绘制时以逻辑尺寸绘制高分辨率图像，由 Graphics2D 的显示变换
   * 映射到物理像素，从而在 HiDPI 屏幕上保持清晰。
   */
  static class HiDpiImageIcon extends ImageIcon {

    private static final long serialVersionUID = 1L;

    private final int logicalWidth;
    private final int logicalHeight;

    /**
     * 创建 HiDPI 图标。
     *
     * @param image       高分辨率图像
     * @param logicalSize 逻辑尺寸（1x 像素）
     */
    HiDpiImageIcon(Image image, int logicalSize) {
      super(image);
      this.logicalWidth = logicalSize;
      this.logicalHeight = logicalSize;
    }

    @Override
    public int getIconWidth() {
      return logicalWidth;
    }

    @Override
    public int getIconHeight() {
      return logicalHeight;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g;
        GraphicsUtils.setupAntialiasing(g2);
        g2.drawImage(getImage(), x, y, logicalWidth, logicalHeight, c);
    }
  }
}
