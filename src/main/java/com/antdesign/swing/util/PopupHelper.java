package com.antdesign.swing.util;

import com.antdesign.swing.model.Placement;

import javax.swing.*;
import java.awt.*;

/**
 * 浮层（Popup）定位与生命周期管理工具。
 *
 * <p>统一 {@code AntTooltip}、{@code AntPopover}、{@code AntDatePicker} 等组件
 * 中重复的 Popup 定位逻辑，并提供安全的显示/隐藏操作。
 */
public final class PopupHelper {

  /** 弹出层与锚定组件之间的默认间距 (px)。 */
  private static final int DEFAULT_OFFSET = 8;

  private PopupHelper() {}

  /**
   * 根据方向和锚定组件计算弹出层屏幕坐标。
   *
   * @param owner     锚定组件
   * @param popupSize 弹出层首选尺寸
   * @param placement 弹出方向
   * @param offset    距离锚定组件的偏移量 (px)
   * @return 屏幕坐标 Point(x, y)
   * @throws IllegalComponentStateException 如果 owner 未显示
   */
  public static Point computePosition(Component owner, Dimension popupSize,
      Placement placement, int offset) {
    Point screen = owner.getLocationOnScreen();
    int ox = screen.x;
    int oy = screen.y;
    int ow = owner.getWidth();
    int oh = owner.getHeight();
    int pw = popupSize.width;
    int ph = popupSize.height;

    int px;
    int py;
    switch (placement) {
      case BOTTOM:
        px = ox + (ow - pw) / 2;
        py = oy + oh + offset;
        break;
      case LEFT:
        px = ox - pw - offset;
        py = oy + (oh - ph) / 2;
        break;
      case RIGHT:
        px = ox + ow + offset;
        py = oy + (oh - ph) / 2;
        break;
      default: // TOP
        px = ox + (ow - pw) / 2;
        py = oy - ph - offset;
        break;
    }

    // 屏幕边界修正
    Rectangle screenBounds = getScreenBounds(owner);
    px = Math.max(screenBounds.x, Math.min(px, screenBounds.x + screenBounds.width - pw));
    py = Math.max(screenBounds.y, Math.min(py, screenBounds.y + screenBounds.height - ph));

    return new Point(px, py);
  }

  /**
   * 使用默认偏移量计算弹出位置。
   */
  public static Point computePosition(Component owner, Dimension popupSize,
      Placement placement) {
    return computePosition(owner, popupSize, placement, DEFAULT_OFFSET);
  }

  /**
   * 安全显示 Popup：先检查 owner 是否可见。
   *
   * @param owner     锚定组件
   * @param content   弹出内容组件
   * @param placement 弹出方向
   * @return 已显示的 Popup，owner 不可见时返回 null
   */
  public static Popup showPopup(Component owner, JComponent content, Placement placement) {
    return showPopup(owner, content, placement, DEFAULT_OFFSET);
  }

  /**
   * 安全显示 Popup。
   */
  public static Popup showPopup(Component owner, JComponent content,
      Placement placement, int offset) {
    if (!owner.isShowing()) {
      return null;
    }
    Dimension pref = content.getPreferredSize();
    content.setSize(pref);
    Point pos = computePosition(owner, pref, placement, offset);
    Popup popup = PopupFactory.getSharedInstance().getPopup(owner, content, pos.x, pos.y);
    popup.show();
    return popup;
  }

  /**
   * 安全隐藏 Popup。
   *
   * @param popup 要隐藏的 Popup，为 null 时忽略
   */
  public static void hidePopup(Popup popup) {
    if (popup != null) {
      popup.hide();
    }
  }

  /**
   * 获取组件所在的屏幕可用区域。
   */
  private static Rectangle getScreenBounds(Component comp) {
    GraphicsConfiguration gc = comp.getGraphicsConfiguration();
    if (gc != null) {
      return gc.getBounds();
    }
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension screenSize = tk.getScreenSize();
    return new Rectangle(0, 0, screenSize.width, screenSize.height);
  }
}
