package com.antdesign.swing.general;

import com.antdesign.swing.base.AbstractAntPanel;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Ant Design 风格固钉组件。
 *
 * <p>将子组件"钉"在最近的 {@link JScrollPane} 可视区域内。
 * 当滚动导致组件即将移出视口时，自动将其固定在视口的顶部或底部。
 * 滚动回原位后恢复正常文档流定位。
 *
 * <pre>{@code
 * // 固定在滚动区域顶部
 * AntAffix affix = new AntAffix(toolbar);
 *
 * // 固定在底部，偏移 10px
 * AntAffix bottomAffix = new AntAffix(statusBar, AntAffix.Position.BOTTOM, 10);
 *
 * // 添加到可滚动内容中
 * contentPanel.add(affix);
 * JScrollPane scrollPane = new JScrollPane(contentPanel);
 * }</pre>
 */
public class AntAffix extends AbstractAntPanel {

  private static final long serialVersionUID = 1L;

  /** 固定位置。 */
  public enum Position {
    /** 固定在视口顶部。 */
    TOP,
    /** 固定在视口底部。 */
    BOTTOM
  }

  private final JComponent target;
  @Getter private Position position;
  @Getter private int offsetPixels;

  @Getter private boolean affixed;
  private JScrollPane scrollPane;
  private JPanel placeholder;
  private ChangeListener viewportListener;

  /**
   * 创建顶部固钉（无偏移）。
   *
   * @param target 要固定的组件
   */
  public AntAffix(JComponent target) {
    this(target, Position.TOP, 0);
  }

  /**
   * 创建固钉。
   *
   * @param target   要固定的组件
   * @param position 固定位置
   */
  public AntAffix(JComponent target, Position position) {
    this(target, position, 0);
  }

  /**
   * 创建固钉。
   *
   * @param target       要固定的组件
   * @param position     固定位置
   * @param offsetPixels 距视口边缘的偏移（像素）
   */
  public AntAffix(JComponent target, Position position, int offsetPixels) {
    super(new BorderLayout());
    this.target = target;
    this.position = position;
    this.offsetPixels = offsetPixels;
    setOpaque(false);
    add(target, BorderLayout.CENTER);

    addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(AncestorEvent event) {
        attachToScrollPane();
      }

      @Override
      public void ancestorRemoved(AncestorEvent event) {
        detachFromScrollPane();
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {}
    });
  }

  /** 设置固定位置。 */
  public void setPosition(Position position) {
    this.position = position;
    updateAffix();
  }

  /** 设置偏移量（像素）。 */
  public void setOffsetPixels(int offsetPixels) {
    this.offsetPixels = offsetPixels;
    updateAffix();
  }

  // ---------------------------------------------------------------------------
  // 内部逻辑
  // ---------------------------------------------------------------------------

  /** 查找最近的 JScrollPane 祖先并监听视口变化。 */
  private void attachToScrollPane() {
    scrollPane = findScrollPane();
    if (scrollPane == null) {
      return;
    }
    viewportListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateAffix();
      }
    };
    scrollPane.getViewport().addChangeListener(viewportListener);
  }

  private void detachFromScrollPane() {
    if (scrollPane != null && viewportListener != null) {
      scrollPane.getViewport().removeChangeListener(viewportListener);
    }
    if (affixed) {
      restoreNormal();
    }
    scrollPane = null;
    viewportListener = null;
  }

  private JScrollPane findScrollPane() {
    Container parent = getParent();
    while (parent != null) {
      if (parent instanceof JViewport
          && parent.getParent() instanceof JScrollPane) {
        return (JScrollPane) parent.getParent();
      }
      parent = parent.getParent();
    }
    return null;
  }

  private void updateAffix() {
    if (scrollPane == null || !isShowing()) {
      return;
    }

    JViewport viewport = scrollPane.getViewport();
    Rectangle viewRect = viewport.getViewRect();

    // 本组件在视口坐标系中的位置
    Point locInViewport = SwingUtilities.convertPoint(
        getParent(), getLocation(), viewport.getView());

    boolean shouldAffix;
    if (position == Position.TOP) {
      shouldAffix = locInViewport.y < viewRect.y + offsetPixels;
    } else {
      int bottomEdge = locInViewport.y + getPreferredSize().height;
      int viewBottom = viewRect.y + viewRect.height - offsetPixels;
      shouldAffix = bottomEdge > viewBottom;
    }

    if (shouldAffix && !affixed) {
      pinToViewport();
    } else if (!shouldAffix && affixed) {
      restoreNormal();
    } else if (affixed) {
      repositionInLayeredPane();
    }
  }

  /** 将 target 从文档流中移出，放到 JLayeredPane 的固定位置。 */
  private void pinToViewport() {
    JLayeredPane layeredPane = findLayeredPane();
    if (layeredPane == null) {
      return;
    }

    Dimension size = target.getPreferredSize();

    // 用占位符保留文档流中的空间
    placeholder = new JPanel();
    placeholder.setOpaque(false);
    placeholder.setPreferredSize(size);
    remove(target);
    add(placeholder, BorderLayout.CENTER);

    // 将 target 放到 layeredPane 上
    layeredPane.add(target, JLayeredPane.PALETTE_LAYER);
    affixed = true;
    repositionInLayeredPane();

    revalidate();
    repaint();
  }

  /** 将 target 从 JLayeredPane 移回文档流。 */
  private void restoreNormal() {
    JLayeredPane layeredPane = findLayeredPane();
    if (layeredPane != null) {
      layeredPane.remove(target);
      layeredPane.repaint();
    }

    if (placeholder != null) {
      remove(placeholder);
      placeholder = null;
    }
    add(target, BorderLayout.CENTER);
    affixed = false;

    revalidate();
    repaint();
  }

  /** 重新计算 target 在 JLayeredPane 中的绝对位置。 */
  private void repositionInLayeredPane() {
    JLayeredPane layeredPane = findLayeredPane();
    if (layeredPane == null || scrollPane == null) {
      return;
    }

    JViewport viewport = scrollPane.getViewport();
    Dimension size = target.getPreferredSize();
    int width = viewport.getWidth();

    // 视口左上角在 layeredPane 坐标系中的位置
    Point vpOrigin = SwingUtilities.convertPoint(
        viewport, 0, 0, layeredPane);

    int x = vpOrigin.x;
    int y;
    if (position == Position.TOP) {
      y = vpOrigin.y + offsetPixels;
    } else {
      y = vpOrigin.y + viewport.getHeight() - size.height - offsetPixels;
    }

    target.setBounds(x, y, width, size.height);
    target.revalidate();
    target.repaint();
  }

  private JLayeredPane findLayeredPane() {
    Container parent = scrollPane;
    while (parent != null) {
      if (parent instanceof JLayeredPane) {
        return (JLayeredPane) parent;
      }
      // JFrame/JDialog 的 rootPane 包含 layeredPane
      if (parent instanceof javax.swing.JRootPane) {
        return ((javax.swing.JRootPane) parent).getLayeredPane();
      }
      parent = parent.getParent();
    }
    return null;
  }
}
