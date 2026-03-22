package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Placement;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 气泡卡片组件。
 *
 * <p>对应 Ant Design {@code <Popover>}，点击/移入元素时弹出气泡式的卡片浮层。
 * 支持标题和自定义内容区域。
 *
 * <pre>{@code
 * AntPopover popover = new AntPopover(myButton, "Title", new JLabel("Content"));
 * }</pre>
 */
public class AntPopover extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int OFFSET = 8;

  /** 触发方式。 */
  public enum Trigger { HOVER, CLICK }

  @Getter private String title;
  @Getter private Component content;
  @Getter private Placement placement;
  @Getter private Trigger trigger;
  private final Component owner;

  private Popup popup;

  /**
   * 创建气泡卡片。
   *
   * @param owner     宿主组件
   * @param title     标题
   * @param content   内容组件
   * @param placement 弹出方向
   * @param trigger   触发方式
   */
  public AntPopover(Component owner, String title, Component content,
      Placement placement, Trigger trigger) {
    this.owner = owner;
    this.title = title;
    this.content = content;
    this.placement = (placement != null) ? placement : Placement.TOP;
    this.trigger = (trigger != null) ? trigger : Trigger.HOVER;

    installListeners();
  }

  public AntPopover(Component owner, String title, Component content) {
    this(owner, title, content, Placement.TOP, Trigger.HOVER);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContent(Component content) {
    this.content = content;
  }

  public void setPlacement(Placement placement) {
    this.placement = (placement != null) ? placement : Placement.TOP;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = (trigger != null) ? trigger : Trigger.HOVER;
  }

  // =========================================================================
  // 显示/隐藏
  // =========================================================================

  /** 显示气泡。 */
  public void showPopover() {
    if (popup != null) {
      return;
    }
    buildPopoverPanel();
    Dimension pref = getPreferredSize();
    setSize(pref);

    Point screen = owner.getLocationOnScreen();
    int ox = screen.x;
    int oy = screen.y;
    int ow = owner.getWidth();
    int oh = owner.getHeight();

    int px;
    int py;
    switch (placement) {
      case BOTTOM:
        px = ox + (ow - pref.width) / 2;
        py = oy + oh + OFFSET;
        break;
      case LEFT:
        px = ox - pref.width - OFFSET;
        py = oy + (oh - pref.height) / 2;
        break;
      case RIGHT:
        px = ox + ow + OFFSET;
        py = oy + (oh - pref.height) / 2;
        break;
      default:
        px = ox + (ow - pref.width) / 2;
        py = oy - pref.height - OFFSET;
        break;
    }

    popup = PopupFactory.getSharedInstance().getPopup(owner, this, px, py);
    popup.show();
  }

  /** 隐藏气泡。 */
  public void hidePopover() {
    if (popup != null) {
      popup.hide();
      popup = null;
    }
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();
    int arc = st.getBorderRadiusLg();

    GraphicsUtils.drawShadow(g2, 0, 0, width, height, arc,
        new Color(0, 0, 0, 40), 4, 12);
    GraphicsUtils.fillRoundRect(g2, 0, 0, width, height, arc,
        ct.getBgElevated());
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void buildPopoverPanel() {
    removeAll();
    setLayout(new BorderLayout());
    SizeToken st = sizeToken();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int pad = st.getPaddingSm();

    JPanel inner = new JPanel(new BorderLayout());
    inner.setOpaque(false);
    inner.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

    if (title != null && !title.isEmpty()) {
      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(ft.createFont(ft.getFontSize(), Font.BOLD));
      titleLabel.setForeground(ct.getTextColor());
      titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, st.getPaddingXs(), 0));
      inner.add(titleLabel, BorderLayout.NORTH);
    }

    if (content != null) {
      inner.add(content, BorderLayout.CENTER);
    }
    add(inner, BorderLayout.CENTER);
    revalidate();
  }

  private void installListeners() {
    if (trigger == Trigger.CLICK) {
      owner.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (popup == null) {
            SwingUtilities.invokeLater(() -> showPopover());
          } else {
            hidePopover();
          }
        }
      });
    } else {
      owner.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          SwingUtilities.invokeLater(() -> showPopover());
        }

        @Override
        public void mouseExited(MouseEvent e) {
          hidePopover();
        }
      });
    }
  }
}
