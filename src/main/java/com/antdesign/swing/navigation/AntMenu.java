package com.antdesign.swing.navigation;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Ant Design 导航菜单组件。
 *
 * <p>对应 Ant Design {@code <Menu>}，支持 horizontal / vertical / inline 三种模式，
 * light / dark 两种主题。
 */
public class AntMenu extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  public enum Mode { HORIZONTAL, VERTICAL, INLINE }
  public enum Theme { LIGHT, DARK }
  public enum ItemType { ITEM, SUB_MENU, GROUP, DIVIDER }

  // =========================================================================
  // MenuItem
  // =========================================================================

  @Getter
  public static class MenuItem {
    private final ItemType type;
    private final String key;
    private String label;
    private Icon icon;
    private boolean disabled;
    private boolean danger;
    private final List<MenuItem> children;

    private MenuItem(ItemType type, String key, String label, Icon icon) {
      this.type = type;
      this.key = key;
      this.label = label != null ? label : "";
      this.icon = icon;
      this.children = new ArrayList<>();
    }

    public MenuItem setDanger(boolean d) { danger = d; return this; }
    public MenuItem setDisabled(boolean d) { disabled = d; return this; }
    public MenuItem setIcon(Icon i) { icon = i; return this; }
  }

  // 工厂
  public static MenuItem item(String key, String label) { return new MenuItem(ItemType.ITEM, key, label, null); }
  public static MenuItem item(String key, String label, Icon icon) { return new MenuItem(ItemType.ITEM, key, label, icon); }
  public static MenuItem sub(String key, String label, Icon icon, MenuItem... ch) {
    MenuItem s = new MenuItem(ItemType.SUB_MENU, key, label, icon);
    if (ch != null) for (MenuItem c : ch) s.children.add(c);
    return s;
  }
  public static MenuItem sub(String key, String label, MenuItem... ch) { return sub(key, label, null, ch); }
  public static MenuItem group(String label, MenuItem... ch) {
    MenuItem g = new MenuItem(ItemType.GROUP, null, label, null);
    if (ch != null) for (MenuItem c : ch) g.children.add(c);
    return g;
  }
  public static MenuItem divider() { return new MenuItem(ItemType.DIVIDER, null, null, null); }

  // =========================================================================
  // 常量
  // =========================================================================

  private static final int ITEM_H = 40;
  private static final int ICON_GAP = 10;
  private static final int H_PAD = 16;

  // =========================================================================
  // 状态
  // =========================================================================

  @Getter private Mode mode;
  @Getter private Theme theme;
  @Getter private boolean inlineCollapsed;
  @Getter private int inlineIndent = 24;

  private List<MenuItem> items = new ArrayList<>();
  private final Set<String> selectedKeys = new LinkedHashSet<>();
  private final Set<String> openKeys = new LinkedHashSet<>();
  private String hoveredKey; // 仅影响绘制，不触发rebuild

  private Consumer<String> onClick;
  private Consumer<Set<String>> onOpenChange;

  // =========================================================================
  // 构造
  // =========================================================================

  public AntMenu(List<MenuItem> items, Mode mode) {
    this.items = items != null ? items : new ArrayList<>();
    this.mode = mode != null ? mode : Mode.INLINE;
    this.theme = Theme.LIGHT;
    setThemeListener(t -> rebuild());
    rebuild();
  }

  public AntMenu(List<MenuItem> items) { this(items, Mode.INLINE); }
  public AntMenu() { this(new ArrayList<>(), Mode.INLINE); }

  // =========================================================================
  // Setter（结构变更 → rebuild；视觉变更 → repaint）
  // =========================================================================

  public void setItems(List<MenuItem> v) { items = v != null ? v : new ArrayList<>(); rebuild(); }
  public void setMode(Mode v) { mode = v != null ? v : Mode.INLINE; rebuild(); }
  public void setTheme(Theme v) { theme = v != null ? v : Theme.LIGHT; rebuild(); }
  public void setInlineCollapsed(boolean v) { inlineCollapsed = v; rebuild(); }
  public void setInlineIndent(int v) { inlineIndent = Math.max(0, v); rebuild(); }
  public void setOnClick(Consumer<String> v) { onClick = v; }
  public void setOnOpenChange(Consumer<Set<String>> v) { onOpenChange = v; }

  public void setSelectedKeys(String... keys) {
    selectedKeys.clear();
    if (keys != null) for (String k : keys) if (k != null) selectedKeys.add(k);
    repaintAll();
  }

  public void setOpenKeys(String... keys) {
    openKeys.clear();
    if (keys != null) for (String k : keys) if (k != null) openKeys.add(k);
    rebuild(); // 结构变更
  }

  public Set<String> getSelectedKeys() { return new HashSet<>(selectedKeys); }
  public Set<String> getOpenKeys() { return new HashSet<>(openKeys); }

  // =========================================================================
  // 颜色
  // =========================================================================

  private boolean dk() { return theme == Theme.DARK; }
  private Color bgC()  { return dk() ? hex("#001529") : colorToken().getBgContainer(); }
  private Color iFg()  { return dk() ? new Color(255,255,255,166) : colorToken().getTextColor(); }
  private Color hFg()  { return dk() ? Color.WHITE : colorToken().getTextColor(); }
  private Color hBg()  { return dk() ? new Color(255,255,255,20) : colorToken().getFillSecondaryColor(); }
  private Color sFg()  { return dk() ? Color.WHITE : colorToken().getPrimaryColor(); }
  private Color sBg()  { return dk() ? colorToken().getPrimaryColor() : colorToken().getPrimaryBgColor(); }
  private Color dFg()  { return dk() ? new Color(255,255,255,64) : colorToken().getDisabledColor(); }
  private Color divC() { return dk() ? new Color(255,255,255,30) : colorToken().getBorderSecondaryColor(); }
  private Color grpC() { return dk() ? new Color(255,255,255,100) : colorToken().getTextTertiaryColor(); }
  private static Color hex(String h) { return ColorToken.parseHexColor(h); }

  // =========================================================================
  // 构建（仅在结构变化时调用）
  // =========================================================================

  private void rebuild() {
    removeAll();
    setOpaque(true);
    setBackground(bgC());

    if (mode == Mode.HORIZONTAL) {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      for (MenuItem it : items) {
        if (it.type == ItemType.DIVIDER) add(mkHDiv());
        else add(mkHItem(it));
      }
      add(Box.createHorizontalGlue());
    } else {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(Box.createVerticalStrut(4));
      for (MenuItem it : items) addInline(it, 0);
      add(Box.createVerticalGlue());
    }
    revalidate();
    repaint();
  }

  private void repaintAll() { repaint(); }

  // =========================================================================
  // 水平条目
  // =========================================================================

  private JComponent mkHItem(MenuItem it) {
    HItemPanel p = new HItemPanel(it);
    Dimension d = p.calcSize();
    p.setPreferredSize(d);
    p.setMinimumSize(d);
    p.setMaximumSize(d);
    return p;
  }

  private JComponent mkHDiv() {
    JSeparator s = new JSeparator(JSeparator.VERTICAL);
    s.setForeground(divC());
    Dimension d = new Dimension(1, ITEM_H - 16);
    s.setPreferredSize(d);
    s.setMaximumSize(d);
    return s;
  }

  /** 水平菜单项——自己绘制背景、文本、选中线。 */
  private class HItemPanel extends JPanel {
    final MenuItem item;

    HItemPanel(MenuItem item) {
      this.item = item;
      setOpaque(false);
      setLayout(new BorderLayout());
      if (!item.disabled) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
          @Override public void mouseEntered(MouseEvent e) { setHover(item.key); }
          @Override public void mouseExited(MouseEvent e)  { clearHover(item.key); }
          @Override public void mouseClicked(MouseEvent e)  { doSelect(item.key); }
        });
      }
    }

    Dimension calcSize() {
      FontMetrics fm = getFontMetrics(fontToken().createFont(fontToken().getFontSize(), Font.PLAIN));
      int w = fm.stringWidth(item.label) + H_PAD * 2;
      if (item.icon != null) w += item.icon.getIconWidth() + ICON_GAP;
      return new Dimension(w, ITEM_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      GraphicsUtils.setupAntialiasing(g2);
      int w = getWidth(), h = getHeight();
      boolean sel = selectedKeys.contains(item.key);
      boolean hov = eq(item.key, hoveredKey);

      // 背景
      if (hov && !item.disabled) {
        g2.setColor(hBg());
        g2.fillRect(0, 0, w, h);
      }

      // 选中底线
      if (sel) {
        g2.setColor(sFg());
        g2.fillRect(0, h - 2, w, 2);
      }

      // 图标+文字
      Font font = fontToken().createFont(fontToken().getFontSize(), Font.PLAIN);
      g2.setFont(font);
      Color fg = item.disabled ? dFg() : item.danger ? colorToken().getErrorColor() : sel ? sFg() : hov ? hFg() : iFg();
      g2.setColor(fg);

      FontMetrics fm = g2.getFontMetrics();
      int totalW = 0;
      if (item.icon != null) totalW += item.icon.getIconWidth() + ICON_GAP;
      totalW += fm.stringWidth(item.label);

      int x = (w - totalW) / 2;
      int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

      if (item.icon != null) {
        int iy = (h - item.icon.getIconHeight()) / 2;
        item.icon.paintIcon(this, g2, x, iy);
        x += item.icon.getIconWidth() + ICON_GAP;
      }
      g2.drawString(item.label, x, textY);
      g2.dispose();
    }
  }

  // =========================================================================
  // 内联条目
  // =========================================================================

  private void addInline(MenuItem it, int depth) {
    switch (it.type) {
      case DIVIDER:  add(mkIDiv()); break;
      case GROUP:    add(mkIGroup(it, depth)); for (MenuItem c : it.children) addInline(c, depth); break;
      case SUB_MENU: add(mkISub(it, depth)); if (openKeys.contains(it.key)) for (MenuItem c : it.children) addInline(c, depth+1); break;
      default:       add(mkIItem(it, depth)); break;
    }
  }

  private JComponent mkIItem(MenuItem it, int depth) {
    IItemPanel p = new IItemPanel(it, depth);
    fixH(p, ITEM_H);
    return p;
  }

  private JComponent mkISub(MenuItem it, int depth) {
    ISubPanel p = new ISubPanel(it, depth);
    fixH(p, ITEM_H);
    return p;
  }

  private JComponent mkIGroup(MenuItem it, int depth) {
    int indent = depth * inlineIndent;
    JPanel p = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        // 仅绘制文本标题
        Graphics2D g2 = (Graphics2D) g.create();
        GraphicsUtils.setupAntialiasing(g2);
        g2.setFont(fontToken().createFont(fontToken().getFontSizeSm(), Font.PLAIN));
        g2.setColor(grpC());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(it.label, H_PAD + indent, 12 + fm.getAscent());
        g2.dispose();
      }
    };
    p.setOpaque(false);
    fixH(p, 36);
    return p;
  }

  private JComponent mkIDiv() {
    JPanel w = new JPanel(new BorderLayout());
    w.setOpaque(false);
    w.setBorder(BorderFactory.createEmptyBorder(4, H_PAD, 4, H_PAD));
    JSeparator s = new JSeparator();
    s.setForeground(divC());
    w.add(s, BorderLayout.CENTER);
    fixH(w, 9);
    return w;
  }

  /** 普通菜单项面板——自绘背景+文字。 */
  private class IItemPanel extends JPanel {
    final MenuItem item;
    final int depth;

    IItemPanel(MenuItem item, int depth) {
      this.item = item;
      this.depth = depth;
      setOpaque(false);
      if (!item.disabled) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
          @Override public void mouseEntered(MouseEvent e) { setHover(item.key); }
          @Override public void mouseExited(MouseEvent e)  { clearHover(item.key); }
          @Override public void mouseClicked(MouseEvent e)  { doSelect(item.key); }
        });
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      GraphicsUtils.setupAntialiasing(g2);
      int w = getWidth(), h = getHeight();
      int margin = 4;
      int radius = sizeToken().getBorderRadius();
      boolean sel = selectedKeys.contains(item.key);
      boolean hov = eq(item.key, hoveredKey);

      // 圆角背景
      if (sel) {
        g2.setColor(sBg());
        g2.fillRoundRect(margin, 0, w - margin * 2, h, radius, radius);
      } else if (hov && !item.disabled) {
        g2.setColor(hBg());
        g2.fillRoundRect(margin, 0, w - margin * 2, h, radius, radius);
      }

      // 左侧选中指示条
      if (sel) {
        g2.setColor(sFg());
        g2.fillRoundRect(0, 6, 3, h - 12, 2, 2);
      }

      // icon + text
      int indent = inlineCollapsed ? 0 : depth * inlineIndent;
      Font font = fontToken().createFont(fontToken().getFontSize(), Font.PLAIN);
      g2.setFont(font);
      Color fg = item.disabled ? dFg() : item.danger ? colorToken().getErrorColor() : sel ? sFg() : hov ? hFg() : iFg();
      g2.setColor(fg);

      int x = H_PAD + indent;
      FontMetrics fm = g2.getFontMetrics();
      int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

      if (item.icon != null) {
        int iy = (h - item.icon.getIconHeight()) / 2;
        item.icon.paintIcon(this, g2, x, iy);
        x += item.icon.getIconWidth() + ICON_GAP;
      }

      if (!inlineCollapsed) {
        g2.drawString(item.label, x, textY);
      }
      g2.dispose();
    }
  }

  /** 子菜单标题面板——自绘背景+文字+箭头。 */
  private class ISubPanel extends JPanel {
    final MenuItem item;
    final int depth;

    ISubPanel(MenuItem item, int depth) {
      this.item = item;
      this.depth = depth;
      setOpaque(false);
      if (!item.disabled) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
          @Override public void mouseEntered(MouseEvent e) { setHover(item.key); }
          @Override public void mouseExited(MouseEvent e)  { clearHover(item.key); }
          @Override public void mouseClicked(MouseEvent e)  { toggleSub(item.key); }
        });
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      GraphicsUtils.setupAntialiasing(g2);
      int w = getWidth(), h = getHeight();
      int margin = 4;
      int radius = sizeToken().getBorderRadius();
      boolean hov = eq(item.key, hoveredKey);
      boolean hasSel = hasSelChild(item);

      if (hov && !item.disabled) {
        g2.setColor(hBg());
        g2.fillRoundRect(margin, 0, w - margin * 2, h, radius, radius);
      }

      int indent = inlineCollapsed ? 0 : depth * inlineIndent;
      Font font = fontToken().createFont(fontToken().getFontSize(), Font.PLAIN);
      g2.setFont(font);
      Color fg = item.disabled ? dFg() : hasSel ? sFg() : hov ? hFg() : iFg();
      g2.setColor(fg);

      int x = H_PAD + indent;
      FontMetrics fm = g2.getFontMetrics();
      int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

      if (item.icon != null) {
        int iy = (h - item.icon.getIconHeight()) / 2;
        item.icon.paintIcon(this, g2, x, iy);
        x += item.icon.getIconWidth() + ICON_GAP;
      }

      if (!inlineCollapsed) {
        g2.drawString(item.label, x, textY);

        // 箭头
        boolean open = openKeys.contains(item.key);
        String arrow = open ? "▾" : "›";
        g2.setFont(fontToken().createFont(12, Font.PLAIN));
        FontMetrics afm = g2.getFontMetrics();
        g2.setColor(item.disabled ? dFg() : iFg());
        g2.drawString(arrow, w - H_PAD - afm.stringWidth(arrow), textY);
      }
      g2.dispose();
    }
  }

  // =========================================================================
  // 状态操作
  // =========================================================================

  private void setHover(String key) {
    if (!eq(key, hoveredKey)) {
      hoveredKey = key;
      repaint(); // 只重绘，不rebuild
    }
  }

  private void clearHover(String key) {
    if (eq(key, hoveredKey)) {
      hoveredKey = null;
      repaint();
    }
  }

  private void doSelect(String key) {
    if (key == null) return;
    selectedKeys.clear();
    selectedKeys.add(key);
    if (onClick != null) onClick.accept(key);
    repaint(); // 选中只重绘
  }

  private void toggleSub(String key) {
    if (key == null) return;
    if (openKeys.contains(key)) openKeys.remove(key); else openKeys.add(key);
    if (onOpenChange != null) onOpenChange.accept(new HashSet<>(openKeys));
    rebuild(); // 子菜单展开/收起是结构变更，需rebuild
  }

  // =========================================================================
  // 工具
  // =========================================================================

  private boolean hasSelChild(MenuItem it) {
    for (MenuItem c : it.children) {
      if (c.type == ItemType.ITEM && selectedKeys.contains(c.key)) return true;
      if ((c.type == ItemType.SUB_MENU || c.type == ItemType.GROUP) && hasSelChild(c)) return true;
    }
    return false;
  }

  private static void fixH(JComponent c, int h) {
    c.setAlignmentX(LEFT_ALIGNMENT);
    c.setPreferredSize(new Dimension(Short.MAX_VALUE, h));
    c.setMinimumSize(new Dimension(0, h));
    c.setMaximumSize(new Dimension(Short.MAX_VALUE, h));
  }

  private static boolean eq(String a, String b) { return a != null && a.equals(b); }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) return super.getPreferredSize();
    if (mode == Mode.HORIZONTAL) return new Dimension(super.getPreferredSize().width, ITEM_H);
    if (inlineCollapsed) return new Dimension(64, super.getPreferredSize().height);
    return super.getPreferredSize();
  }
}
