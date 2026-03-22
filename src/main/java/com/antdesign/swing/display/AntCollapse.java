package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ant Design 折叠面板组件。
 *
 * <p>对应 Ant Design {@code <Collapse>}，可以折叠/展开的内容区域。
 * 支持手风琴模式（同时只展开一个面板）和边框模式。
 *
 * <pre>{@code
 * AntCollapse collapse = new AntCollapse();
 * collapse.addPanel("Panel 1", new JLabel("Content 1"));
 * collapse.addPanel("Panel 2", new JLabel("Content 2"));
 * }</pre>
 */
public class AntCollapse extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 折叠面板项。 */
  public static class Panel {
    @Getter private final String key;
    @Getter private final String header;
    @Getter private final Component content;

    public Panel(String key, String header, Component content) {
      this.key = key;
      this.header = header;
      this.content = content;
    }
  }

  @Getter private boolean accordion;
  @Getter private boolean bordered;
  @Getter private boolean ghost;
  private final List<Panel> panels = new ArrayList<>();
  private final Set<String> activeKeys = new HashSet<>();

  /** 创建折叠面板。 */
  public AntCollapse() {
    this.bordered = true;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // 面板操作
  // =========================================================================

  /**
   * 添加折叠面板。
   *
   * @param header  面板标题
   * @param content 面板内容
   */
  public void addPanel(String header, Component content) {
    String key = "panel-" + panels.size();
    panels.add(new Panel(key, header, content));
    rebuildUi();
  }

  /**
   * 添加折叠面板（自定义 key）。
   *
   * @param key     面板唯一标识
   * @param header  面板标题
   * @param content 面板内容
   */
  public void addPanel(String key, String header, Component content) {
    panels.add(new Panel(key, header, content));
    rebuildUi();
  }

  /** 展开指定面板。 */
  public void expand(String key) {
    if (accordion) {
      activeKeys.clear();
    }
    activeKeys.add(key);
    rebuildUi();
  }

  /** 折叠指定面板。 */
  public void collapse(String key) {
    activeKeys.remove(key);
    rebuildUi();
  }

  /** 获取当前展开的面板 key 列表。 */
  public Set<String> getActiveKeys() {
    return new HashSet<>(activeKeys);
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setAccordion(boolean accordion) {
    this.accordion = accordion;
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    rebuildUi();
  }

  public void setGhost(boolean ghost) {
    this.ghost = ghost;
    rebuildUi();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    if (bordered && !ghost) {
      setBorder(BorderFactory.createLineBorder(ct.getBorderColor()));
    } else {
      setBorder(null);
    }

    for (Panel panel : panels) {
      boolean expanded = activeKeys.contains(panel.key);

      // 头部
      JPanel headerPanel = new JPanel(new BorderLayout());
      headerPanel.setOpaque(true);
      headerPanel.setBackground(ghost
          ? ct.getBgContainer()
          : ct.getBgLayout());
      headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      int pad = st.getPaddingSm();
      headerPanel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 1, 0, ct.getBorderSecondaryColor()),
          BorderFactory.createEmptyBorder(pad, st.getPadding(), pad, st.getPadding())));

      JLabel arrow = new JLabel(expanded ? "▼" : "▶");
      arrow.setFont(ft.createFont(10, Font.PLAIN));
      arrow.setForeground(ct.getTextTertiaryColor());
      arrow.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, st.getPaddingXs()));
      headerPanel.add(arrow, BorderLayout.WEST);

      JLabel titleLabel = new JLabel(panel.header);
      titleLabel.setFont(ft.createFont(ft.getFontSize(), Font.BOLD));
      titleLabel.setForeground(ct.getTextColor());
      headerPanel.add(titleLabel, BorderLayout.CENTER);

      headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
          headerPanel.getPreferredSize().height));
      headerPanel.setAlignmentX(LEFT_ALIGNMENT);

      String key = panel.key;
      headerPanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (activeKeys.contains(key)) {
            collapse(key);
          } else {
            expand(key);
          }
        }
      });
      add(headerPanel);

      // 内容
      if (expanded && panel.content != null) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
            st.getPadding(), st.getPadding(), st.getPadding(), st.getPadding()));
        contentPanel.add(panel.content, BorderLayout.CENTER);
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            contentPanel.getPreferredSize().height));
        contentPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(contentPanel);
      }
    }

    revalidate();
    repaint();
  }
}
