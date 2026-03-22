package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntPanel;
import com.antdesign.swing.model.Placement;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ant Design 抽屉组件。
 *
 * <p>对应 Ant Design {@code <Drawer>}，从屏幕边缘滑出的浮层面板。
 * 支持四个方向（上/下/左/右）、自定义宽/高和标题。
 *
 * <pre>{@code
 * AntDrawer drawer = new AntDrawer(frame, "详情");
 * drawer.setPlacement(Placement.RIGHT);
 * drawer.setWidth(400);
 * drawer.setContent(detailPanel);
 * drawer.open();
 * }</pre>
 *
 * @see Placement
 */
public class AntDrawer {

  private static final int DEFAULT_WIDTH = 378;
  private static final int DEFAULT_HEIGHT = 378;

  @Getter private String title;
  @Getter private Placement placement;
  @Getter @Setter private JComponent content;
  @Getter @Setter private int width;
  @Getter @Setter private int height;
  @Getter @Setter private boolean closable;
  @Getter @Setter private boolean mask;
  @Getter @Setter private Runnable onClose;

  private final Window owner;
  private JDialog dialog;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建一个抽屉。
   *
   * @param owner 父窗口
   * @param title 标题
   */
  public AntDrawer(Window owner, String title) {
    this.owner = owner;
    this.title = (title != null) ? title : "";
    this.placement = Placement.RIGHT;
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.closable = true;
    this.mask = true;
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = (title != null) ? title : "";
  }

  public void setPlacement(Placement placement) {
    this.placement = (placement != null) ? placement : Placement.RIGHT;
  }

  // =========================================================================
  // 公共 API
  // =========================================================================

  /** 打开抽屉。 */
  public void open() {
    SwingUtilities.invokeLater(this::buildAndShow);
  }

  /** 关闭抽屉。 */
  public void close() {
    if (dialog != null) {
      dialog.setVisible(false);
      dialog.dispose();
      dialog = null;
    }
    if (onClose != null) {
      onClose.run();
    }
  }

  /** 抽屉是否可见。 */
  public boolean isVisible() {
    return dialog != null && dialog.isVisible();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void buildAndShow() {
    dialog = new JDialog(owner instanceof JFrame ? (JFrame) owner : null, true);
    dialog.setUndecorated(true);
    dialog.setBackground(new Color(0, 0, 0, 0));

    JPanel root = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        if (mask) {
          g.setColor(new Color(0, 0, 0, 100));
          g.fillRect(0, 0, getWidth(), getHeight());
        }
      }
    };
    root.setOpaque(false);

    // 点击遮罩关闭
    if (mask) {
      root.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          close();
        }
      });
    }

    // 抽屉面板
    DrawerPanel drawerPanel = new DrawerPanel();
    drawerPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        e.consume(); // 阻止穿透到遮罩
      }
    });

    // 根据方向设置尺寸和位置
    switch (placement) {
      case LEFT:
        drawerPanel.setPreferredSize(new Dimension(width, 0));
        root.add(drawerPanel, BorderLayout.WEST);
        break;
      case TOP:
        drawerPanel.setPreferredSize(new Dimension(0, height));
        root.add(drawerPanel, BorderLayout.NORTH);
        break;
      case BOTTOM:
        drawerPanel.setPreferredSize(new Dimension(0, height));
        root.add(drawerPanel, BorderLayout.SOUTH);
        break;
      default: // RIGHT
        drawerPanel.setPreferredSize(new Dimension(width, 0));
        root.add(drawerPanel, BorderLayout.EAST);
        break;
    }

    dialog.setContentPane(root);
    dialog.setSize(owner.getWidth(), owner.getHeight());
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  // =========================================================================
  // 抽屉面板
  // =========================================================================

  private class DrawerPanel extends AbstractAntPanel {

    DrawerPanel() {
      setOpaque(false);
      setLayout(new BorderLayout());

      ColorToken ct = colorToken();
      FontToken ft = fontToken();
      SizeToken st = sizeToken();

      // 头部
      JPanel header = new JPanel(new BorderLayout());
      header.setOpaque(false);
      header.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 1, 0, ct.getBorderSecondaryColor()),
          BorderFactory.createEmptyBorder(st.getPadding(), st.getPaddingLg(),
              st.getPadding(), st.getPaddingLg())));

      JLabel titleLabel = new JLabel(title);
      titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
      titleLabel.setForeground(ct.getTextColor());
      header.add(titleLabel, BorderLayout.CENTER);

      if (closable) {
        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        closeBtn.setForeground(ct.getTextTertiaryColor());
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            close();
          }
        });
        header.add(closeBtn, BorderLayout.EAST);
      }

      add(header, BorderLayout.NORTH);

      // 内容
      JPanel body = new JPanel(new BorderLayout());
      body.setOpaque(false);
      body.setBorder(BorderFactory.createEmptyBorder(
          st.getPadding(), st.getPaddingLg(), st.getPadding(), st.getPaddingLg()));
      if (content != null) {
        body.add(content, BorderLayout.CENTER);
      }
      add(body, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        GraphicsUtils.setupAntialiasing(g2);
        ColorToken ct = colorToken();
        g2.setColor(ct.getBgElevated());
        g2.fillRect(0, 0, getWidth(), getHeight());
      } finally {
        g2.dispose();
      }
    }
  }
}
