package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 卡片组件。
 *
 * <p>对应 Ant Design {@code <Card>}，通用卡片容器。支持标题、额外操作区、
 * 封面图、多个操作按钮和加载态等特性。
 *
 * <pre>{@code
 * AntCard card = new AntCard("Card Title");
 * card.setExtra(new JLabel("More"));
 * card.setBody(new JLabel("Card content"));
 * }</pre>
 */
public class AntCard extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String title;
  @Getter private Component extra;
  @Getter private Component body;
  @Getter private Component cover;
  @Getter private boolean bordered;
  @Getter private boolean hoverable;
  @Getter private boolean loading;
  private ComponentSize size;
  private final List<Component> actions = new ArrayList<>();

  private boolean hovered;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /** 创建无标题卡片。 */
  public AntCard() {
    this(null);
  }

  /**
   * 创建带标题的卡片。
   *
   * @param title 卡片标题
   */
  public AntCard(String title) {
    this.title = title;
    this.bordered = true;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    rebuildUi();

    addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        hovered = true;
        if (hoverable) {
          repaint();
        }
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        hovered = false;
        if (hoverable) {
          repaint();
        }
      }
    });
    setThemeListener(theme -> { rebuildUi(); repaint(); });
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = title;
    rebuildUi();
  }

  public void setExtra(Component extra) {
    this.extra = extra;
    rebuildUi();
  }

  public void setBody(Component body) {
    this.body = body;
    rebuildUi();
  }

  public void setCover(Component cover) {
    this.cover = cover;
    rebuildUi();
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    repaint();
  }

  public void setHoverable(boolean hoverable) {
    this.hoverable = hoverable;
    repaint();
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
    rebuildUi();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    rebuildUi();
  }

  /**
   * 获取卡片尺寸。
   *
   * <p>方法名为 {@code getComponentSize()} 而非 {@code getSize()}，
   * 以避免与 {@link Component#getSize()} 返回类型冲突。
   *
   * @return 卡片尺寸
   */
  public ComponentSize getComponentSize() {
    return size;
  }

  /**
   * 添加底部操作项。
   *
   * @param action 操作组件
   */
  public void addAction(Component action) {
    actions.add(action);
    rebuildUi();
  }

  /** 清空底部操作项。 */
  public void clearActions() {
    actions.clear();
    rebuildUi();
  }

  // =========================================================================
  // 绘制
  // =========================================================================

  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();
    int arc = st.getBorderRadiusLg();

    if (hoverable && hovered) {
      GraphicsUtils.drawShadow(g2, 0, 0, width, height, arc,
          new Color(0, 0, 0, 30), 4, 12);
    }

    GraphicsUtils.fillRoundRect(g2, 0, 0, width, height, arc, ct.getBgContainer());

    if (bordered) {
      GraphicsUtils.drawRoundRect(g2, 0, 0, width - 1, height - 1, arc,
          ct.getBorderSecondaryColor(), 1f);
    }
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    SizeToken st = sizeToken();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    int pad = (size == ComponentSize.SMALL) ? st.getPaddingSm() : st.getPaddingLg();

    JPanel mainPanel = new JPanel();
    mainPanel.setOpaque(false);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    // 封面
    if (cover != null) {
      JPanel coverPanel = new JPanel(new BorderLayout());
      coverPanel.setOpaque(false);
      coverPanel.add(cover, BorderLayout.CENTER);
      mainPanel.add(coverPanel);
    }

    // 头部
    if (title != null || extra != null) {
      JPanel header = new JPanel(new BorderLayout());
      header.setOpaque(false);
      header.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 1, 0, ct.getBorderSecondaryColor()),
          BorderFactory.createEmptyBorder(pad, pad, pad, pad)));

      if (title != null) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
        titleLabel.setForeground(ct.getTextColor());
        header.add(titleLabel, BorderLayout.CENTER);
      }
      if (extra != null) {
        header.add(extra, BorderLayout.EAST);
      }
      mainPanel.add(header);
    }

    // 内容
    JPanel bodyPanel = new JPanel(new BorderLayout());
    bodyPanel.setOpaque(false);
    bodyPanel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

    if (loading) {
      JLabel loadingLabel = new JLabel("Loading...");
      loadingLabel.setForeground(ct.getTextTertiaryColor());
      bodyPanel.add(loadingLabel, BorderLayout.CENTER);
    } else if (body != null) {
      bodyPanel.add(body, BorderLayout.CENTER);
    }
    mainPanel.add(bodyPanel);

    // 操作栏
    if (!actions.isEmpty()) {
      JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
      actionsPanel.setOpaque(false);
      actionsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
          ct.getBorderSecondaryColor()));

      for (int i = 0; i < actions.size(); i++) {
        if (i > 0) {
          JPanel sep = new JPanel();
          sep.setPreferredSize(new Dimension(1, 20));
          sep.setBackground(ct.getBorderSecondaryColor());
          actionsPanel.add(sep);
        }
        JPanel actionWrapper = new JPanel(new BorderLayout());
        actionWrapper.setOpaque(false);
        actionWrapper.setBorder(BorderFactory.createEmptyBorder(
            st.getPaddingSm(), st.getPaddingLg(), st.getPaddingSm(), st.getPaddingLg()));
        actionWrapper.add(actions.get(i), BorderLayout.CENTER);
        actionsPanel.add(actionWrapper);
      }
      mainPanel.add(actionsPanel);
    }

    add(mainPanel, BorderLayout.CENTER);
    revalidate();
    repaint();
  }
}
