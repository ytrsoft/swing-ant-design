package com.antdesign.swing;

import com.antdesign.swing.base.AbstractAntFrame;
import com.antdesign.swing.feedback.AntMessage;
import com.antdesign.swing.general.AntButton;
import com.antdesign.swing.model.ButtonType;
import com.antdesign.swing.navigation.AntMenu;
import com.antdesign.swing.theme.AntThemeManager;
import com.antdesign.swing.theme.DarkAlgorithm;
import com.antdesign.swing.theme.DefaultAlgorithm;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.util.AntIcons;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AntMenu 组件演示 — 模拟 Ant Design Pro 侧边栏布局。
 */
public class AntDesign extends AbstractAntFrame {

  private static final int SIDE_W = 220;
  private static final int SIDE_COLLAPSED_W = 64;

  private AntMenu sideMenu;
  private AntMenu topMenu;
  private JPanel sidePanel;
  private JLabel titleLabel;
  private JPanel bodyContent;
  private boolean collapsed;

  @Override
  protected void initContent() {
    super.initContent();
    getContentPane().setLayout(new BorderLayout());

    // 顶部
    getContentPane().add(makeTopBar(), BorderLayout.NORTH);

    // 中部 = 侧边栏 + 内容
    JPanel mid = new JPanel(new BorderLayout());
    mid.setOpaque(false);

    sidePanel = makeSidebar();
    mid.add(sidePanel, BorderLayout.WEST);
    mid.add(makeContentArea(), BorderLayout.CENTER);

    getContentPane().add(mid, BorderLayout.CENTER);

    AntThemeManager.getInstance().addThemeChangeListener(t ->
        SwingUtilities.invokeLater(this::refreshAll));
  }

  // =====================================================================
  // 顶部栏
  // =====================================================================

  private JPanel makeTopBar() {
    JPanel bar = new JPanel(new BorderLayout());
    bar.setBackground(ColorToken.parseHexColor("#001529"));
    bar.setPreferredSize(new Dimension(0, 48));

    // logo
    JLabel logo = new JLabel("  ✦  Ant Design Pro");
    logo.setFont(ft().createFont(16, Font.BOLD));
    logo.setForeground(Color.WHITE);
    logo.setPreferredSize(new Dimension(SIDE_W, 48));
    bar.add(logo, BorderLayout.WEST);

    // 水平菜单
    Color ic = new Color(255, 255, 255, 166);
    List<AntMenu.MenuItem> top = new ArrayList<>();
    top.add(AntMenu.item("t-home", "首页", AntIcons.outlined("home", 14, ic)));
    top.add(AntMenu.item("t-docs", "文档", AntIcons.outlined("file-text", 14, ic)));
    top.add(AntMenu.item("t-comp", "组件", AntIcons.outlined("menu", 14, ic)));
    topMenu = new AntMenu(top, AntMenu.Mode.HORIZONTAL);
    topMenu.setTheme(AntMenu.Theme.DARK);
    topMenu.setSelectedKeys("t-home");
    topMenu.setOnClick(k -> AntMessage.info("顶部导航: " + k));
    bar.add(topMenu, BorderLayout.CENTER);

    // 右侧
    JLabel right = new JLabel("🌓 Admin  ", JLabel.RIGHT);
    right.setForeground(new Color(255, 255, 255, 200));
    right.setFont(ft().createFont(ft().getFontSize(), Font.PLAIN));
    right.setPreferredSize(new Dimension(120, 48));
    right.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    right.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        boolean dark = AntThemeManager.getInstance().isDark();
        AntThemeManager.getInstance().switchTheme(
            dark ? new DefaultAlgorithm() : new DarkAlgorithm());
      }
    });
    bar.add(right, BorderLayout.EAST);

    return bar;
  }

  // =====================================================================
  // 侧边栏
  // =====================================================================

  private JPanel makeSidebar() {
    JPanel sb = new JPanel(new BorderLayout());
    sb.setPreferredSize(new Dimension(SIDE_W, 0));

    sideMenu = new AntMenu(sideItems(), AntMenu.Mode.INLINE);
    sideMenu.setSelectedKeys("dashboard");
    sideMenu.setOpenKeys("data", "settings");
    sideMenu.setOnClick(this::onSideClick);

    JScrollPane sp = new JScrollPane(sideMenu);
    sp.setBorder(BorderFactory.createEmptyBorder());
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sb.add(sp, BorderLayout.CENTER);

    // 折叠按钮
    JLabel tog = new JLabel("☰", JLabel.CENTER);
    tog.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    tog.setForeground(ct().getTextSecondaryColor());
    tog.setOpaque(true);
    tog.setBackground(ct().getBgContainer());
    tog.setPreferredSize(new Dimension(0, 32));
    tog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    tog.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ct().getBorderSecondaryColor()));
    tog.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) { toggleCollapse(); }
    });
    sb.add(tog, BorderLayout.SOUTH);

    sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ct().getBorderSecondaryColor()));
    return sb;
  }

  private List<AntMenu.MenuItem> sideItems() {
    Color ic = ct().getTextSecondaryColor();
    List<AntMenu.MenuItem> list = new ArrayList<>();

    list.add(AntMenu.item("dashboard", "Dashboard", AntIcons.outlined("home", 14, ic)));

    list.add(AntMenu.sub("data", "数据管理", AntIcons.outlined("folder", 14, ic),
        AntMenu.item("data-list", "数据列表"),
        AntMenu.item("data-search", "数据查询"),
        AntMenu.sub("data-analysis", "数据分析",
            AntMenu.item("analysis-report", "分析报告"),
            AntMenu.item("analysis-chart", "图表统计"))
    ));

    list.add(AntMenu.sub("user-mgmt", "用户管理", AntIcons.outlined("user", 14, ic),
        AntMenu.item("user-list", "用户列表"),
        AntMenu.item("user-roles", "角色权限")
    ));

    list.add(AntMenu.divider());

    list.add(AntMenu.sub("settings", "系统设置", AntIcons.outlined("setting", 14, ic),
        AntMenu.group("基础设置",
            AntMenu.item("set-basic", "基本信息"),
            AntMenu.item("set-security", "安全设置")),
        AntMenu.group("高级设置",
            AntMenu.item("set-notify", "通知设置"),
            AntMenu.item("set-log", "操作日志"))
    ));

    list.add(AntMenu.divider());
    list.add(AntMenu.item("logout", "退出登录",
        AntIcons.outlined("logout", 14, ct().getErrorColor())).setDanger(true));

    return list;
  }

  // =====================================================================
  // 内容区
  // =====================================================================

  private JPanel makeContentArea() {
    JPanel wrap = new JPanel(new BorderLayout());
    wrap.setOpaque(false);

    // 页头
    titleLabel = new JLabel("Dashboard");
    titleLabel.setFont(ft().createFont(ft().getFontSizeHeading4(), Font.BOLD));
    titleLabel.setForeground(ct().getTextColor());
    titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));
    wrap.add(titleLabel, BorderLayout.NORTH);

    // 内容
    bodyContent = new JPanel();
    bodyContent.setOpaque(false);
    bodyContent.setLayout(new BoxLayout(bodyContent, BoxLayout.Y_AXIS));
    bodyContent.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
    fillDashboard();

    JScrollPane sp = new JScrollPane(bodyContent);
    sp.setBorder(BorderFactory.createEmptyBorder());
    sp.setOpaque(false);
    sp.getViewport().setOpaque(false);
    wrap.add(sp, BorderLayout.CENTER);

    return wrap;
  }

  private void fillDashboard() {
    bodyContent.removeAll();

    // 说明
    JPanel info = mkCard();
    addLabel(info, "AntMenu 组件演示", ft().getFontSizeLg(), Font.BOLD, ct().getTextColor());
    info.add(Box.createVerticalStrut(8));
    addLabel(info,
        "<html><body style='width:520px'>"
            + "• <b>侧边栏</b> — inline 模式，多级子菜单、分组标题、分割线、danger 样式<br>"
            + "• <b>顶部导航</b> — horizontal 模式（深色），底部选中指示条<br>"
            + "• <b>折叠</b> — 点击左下角 ☰ 可折叠/展开侧边栏<br>"
            + "• <b>主题切换</b> — 点击右上角 🌓 切换 light/dark</body></html>",
        ft().getFontSize(), Font.PLAIN, ct().getTextSecondaryColor());
    bodyContent.add(info);
    bodyContent.add(Box.createVerticalStrut(16));

    // 统计
    JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
    row.setOpaque(false);
    row.setAlignmentX(LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
    row.add(mkStat("菜单模式", "3 种", "horizontal / vertical / inline"));
    row.add(mkStat("菜单主题", "2 种", "light / dark"));
    row.add(mkStat("Item 类型", "4 种", "item / sub / group / divider"));
    bodyContent.add(row);
    bodyContent.add(Box.createVerticalStrut(20));

    // 按钮
    JPanel btns = new JPanel();
    btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
    btns.setOpaque(false);
    btns.setAlignmentX(LEFT_ALIGNMENT);

    AntButton b1 = new AntButton("切换侧边栏折叠", ButtonType.PRIMARY);
    b1.addActionListener(e -> toggleCollapse());
    btns.add(b1);
    btns.add(Box.createHorizontalStrut(8));

    AntButton b2 = new AntButton("侧边栏深色主题", ButtonType.DEFAULT);
    b2.addActionListener(e -> {
      boolean dk = sideMenu.getTheme() == AntMenu.Theme.DARK;
      sideMenu.setTheme(dk ? AntMenu.Theme.LIGHT : AntMenu.Theme.DARK);
    });
    btns.add(b2);
    btns.add(Box.createHorizontalStrut(8));

    AntButton b3 = new AntButton("选中「用户列表」", ButtonType.DASHED);
    b3.addActionListener(e -> {
      sideMenu.setOpenKeys("user-mgmt");
      sideMenu.setSelectedKeys("user-list");
      titleLabel.setText("用户列表");
    });
    btns.add(b3);

    bodyContent.add(btns);
    bodyContent.add(Box.createVerticalGlue());
    bodyContent.revalidate();
    bodyContent.repaint();
  }

  // =====================================================================
  // 辅助
  // =====================================================================

  private JPanel mkCard() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBackground(ct().getBgContainer());
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ct().getBorderSecondaryColor()),
        BorderFactory.createEmptyBorder(16, 20, 16, 20)));
    p.setAlignmentX(LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
    return p;
  }

  private JPanel mkStat(String title, String value, String desc) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBackground(ct().getBgContainer());
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ct().getBorderSecondaryColor()),
        BorderFactory.createEmptyBorder(12, 16, 12, 16)));
    addLabel(p, title, ft().getFontSizeSm(), Font.PLAIN, ct().getTextSecondaryColor());
    p.add(Box.createVerticalStrut(4));
    addLabel(p, value, ft().getFontSizeHeading3(), Font.BOLD, ct().getTextColor());
    p.add(Box.createVerticalStrut(4));
    addLabel(p, desc, ft().getFontSizeSm(), Font.PLAIN, ct().getTextTertiaryColor());
    return p;
  }

  private static void addLabel(JPanel p, String text, int size, int style, Color fg) {
    JLabel l = new JLabel(text);
    l.setFont(AntThemeManager.getInstance().getFontToken().createFont(size, style));
    l.setForeground(fg);
    l.setAlignmentX(LEFT_ALIGNMENT);
    p.add(l);
  }

  private void onSideClick(String key) {
    if ("logout".equals(key)) { AntMessage.warning("退出登录"); return; }
    titleLabel.setText(key);
    titleLabel.setForeground(ct().getTextColor());
  }

  private void toggleCollapse() {
    collapsed = !collapsed;
    sideMenu.setInlineCollapsed(collapsed);
    sidePanel.setPreferredSize(new Dimension(collapsed ? SIDE_COLLAPSED_W : SIDE_W, 0));
    sidePanel.revalidate();
  }

  private void refreshAll() {
    Set<String> sel = sideMenu.getSelectedKeys();
    Set<String> open = sideMenu.getOpenKeys();
    sideMenu.setItems(sideItems());
    sideMenu.setSelectedKeys(sel.toArray(new String[0]));
    sideMenu.setOpenKeys(open.toArray(new String[0]));
    titleLabel.setForeground(ct().getTextColor());
    fillDashboard();
  }

  private static FontToken ft() { return AntThemeManager.getInstance().getFontToken(); }
  private static ColorToken ct() { return AntThemeManager.getInstance().getColorToken(); }

  public static void main(String[] args) { launch(); }
}
