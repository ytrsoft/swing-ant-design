package com.antdesign.swing.theme;

import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.GlobalToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Ant Design 主题管理器（单例）。
 *
 * <p>负责全局主题的持有、切换和变更通知。
 *
 * @see AntTheme
 */
@Slf4j
public final class AntThemeManager {

    private static final AntThemeManager INSTANCE = new AntThemeManager();

    @Getter
    private volatile AntTheme currentTheme;

    private final List<Consumer<AntTheme>> listeners = new CopyOnWriteArrayList<>();

    private AntThemeManager() {
        this.currentTheme = AntTheme.defaultTheme();
    }

    public static AntThemeManager getInstance() {
        return INSTANCE;
    }

    public GlobalToken getToken() {
        return currentTheme.getToken();
    }

    public ColorToken getColorToken() {
        return currentTheme.getColorToken();
    }

    public FontToken getFontToken() {
        return currentTheme.getFontToken();
    }

    public SizeToken getSizeToken() {
        return currentTheme.getSizeToken();
    }

    /**
     * 安装主题并应用到 Swing 全局 Look and Feel。
     *
     * <p>执行顺序非常重要：
     * <ol>
     *   <li>安装 FlatLaf（暗色/亮色）— 创建新的 LAF defaults 表</li>
     *   <li>将令牌以 {@link ColorUIResource} 形式写入 UIDefaults — 确保 FlatLaf 能识别</li>
     *   <li>调用 {@code FlatLaf.updateUI()} — 刷新所有 Swing 原生组件</li>
     *   <li>通知自定义组件监听器 — 更新 Ant Design 组件内部状态</li>
     *   <li>强制重绘所有窗口 — 兜底确保视觉更新</li>
     * </ol>
     */
    public void install(AntTheme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("Theme must not be null");
        }
        this.currentTheme = theme;

        // 1) 安装 FlatLaf
        boolean lafInstalled = installFlatLaf(theme);
        if (!lafInstalled) {
            log.warn("FlatLaf install failed, theme may not display correctly");
        }

        // 2) 写入 UIDefaults（使用 ColorUIResource）
        applyTokensToUiDefaults(theme);

        // 3) FlatLaf 全局刷新 — 必须用 FlatLaf.updateUI() 而非手动遍历，
        //    否则 FlatLaf 内部状态（如 Windows 暗色标题栏）不会更新
        FlatLaf.updateUI();

        // 4) 通知自定义 Ant Design 组件
        notifyListeners(theme);

        // 5) 兜底：强制所有窗口内容面板重绘
        repaintAllWindows();

        log.info("Theme installed: {} (dark={})",
                theme.getAlgorithm().getName(), theme.isDark());
    }

    public void switchTheme(ThemeAlgorithm algorithm) {
        install(AntTheme.of(algorithm));
    }

    public void switchTheme(ThemeAlgorithm algorithm, GlobalToken seedToken) {
        install(AntTheme.of(algorithm, seedToken));
    }

    public void addThemeChangeListener(Consumer<AntTheme> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeThemeChangeListener(Consumer<AntTheme> listener) {
        listeners.remove(listener);
    }

    public boolean isDark() {
        return currentTheme.isDark();
    }

    // =========================================================================
    // 内部方法
    // =========================================================================

    /**
     * 安装 FlatLaf。
     *
     * @return 安装是否成功
     */
    private boolean installFlatLaf(AntTheme theme) {
        try {
            if (theme.isDark()) {
                return FlatDarkLaf.setup();
            } else {
                return FlatLightLaf.setup();
            }
        } catch (Exception e) {
            log.error("Failed to install FlatLaf", e);
            return false;
        }
    }

    /**
     * 将 Ant Design 令牌写入 UIManager。
     *
     * <p><b>关键：所有 Color 值必须包装为 {@link ColorUIResource}</b>。
     * FlatLaf 的 UI delegate 在 {@code installDefaults()} 中使用
     * {@code bg instanceof UIResource} 判断是否可以覆盖组件当前颜色。
     * 如果上次主题切换设置了普通 Color，下次切换就无法覆盖。
     */
    private void applyTokensToUiDefaults(AntTheme theme) {
        ColorToken ct = theme.getColorToken();
        SizeToken st = theme.getSizeToken();

        // 将所有令牌颜色转为 UIResource
        Color bgContainer = uiColor(ct.getBgContainer());
        Color bgElevated = uiColor(ct.getBgElevated());
        Color bgLayout = uiColor(ct.getBgLayout());
        Color text = uiColor(ct.getTextColor());
        Color textSecondary = uiColor(ct.getTextSecondaryColor());
        Color textQuaternary = uiColor(ct.getTextQuaternaryColor());
        Color primary = uiColor(ct.getPrimaryColor());
        Color primaryHover = uiColor(ct.getPrimaryHoverColor());
        Color primaryActive = uiColor(ct.getPrimaryActiveColor());
        Color primaryBg = uiColor(ct.getPrimaryBgColor());
        Color border = uiColor(ct.getBorderColor());
        Color borderSecondary = uiColor(ct.getBorderSecondaryColor());
        Color white = uiColor(Color.WHITE);
        Color transparent = uiColor(new Color(0, 0, 0, 0));

        // =================================================================
        // 全局 Swing 基础色
        // =================================================================
        put("control", bgContainer);
        put("text", text);
        put("textText", text);
        put("textHighlight", primaryBg);
        put("textHighlightText", text);
        put("info", bgContainer);
        put("infoText", text);
        put("window", bgContainer);
        put("windowText", text);
        put("menu", bgElevated);
        put("menuText", text);

        // =================================================================
        // 面板 / 根容器
        // =================================================================
        put("Panel.background", bgContainer);
        put("Panel.foreground", text);
        put("RootPane.background", bgLayout);
        put("RootPane.foreground", text);

        // =================================================================
        // 标签
        // =================================================================
        put("Label.foreground", text);
        put("Label.background", bgContainer);

        // =================================================================
        // 按钮
        // =================================================================
        put("Button.background", bgContainer);
        put("Button.foreground", text);
        put("Button.arc", st.getBorderRadius());
        put("Button.borderColor", border);
        put("Button.hoverBorderColor", primaryHover);
        put("Button.default.background", primary);
        put("Button.default.foreground", white);
        put("Button.default.hoverBackground", primaryHover);
        put("Button.default.pressedBackground", primaryActive);
        put("Button.default.borderColor", primary);
        put("Button.innerFocusWidth", 0);
        put("ToggleButton.background", bgContainer);
        put("ToggleButton.foreground", text);

        // =================================================================
        // 文本输入
        // =================================================================
        put("TextField.background", bgContainer);
        put("TextField.foreground", text);
        put("TextField.caretForeground", text);
        put("TextField.arc", st.getBorderRadius());
        put("TextField.borderColor", border);
        put("TextField.hoverBorderColor", primaryHover);
        put("TextField.focusedBorderColor", primary);
        put("TextField.placeholderForeground", textQuaternary);

        put("FormattedTextField.background", bgContainer);
        put("FormattedTextField.foreground", text);

        put("TextArea.background", bgContainer);
        put("TextArea.foreground", text);
        put("TextArea.caretForeground", text);

        put("PasswordField.background", bgContainer);
        put("PasswordField.foreground", text);
        put("PasswordField.caretForeground", text);

        put("EditorPane.background", bgContainer);
        put("EditorPane.foreground", text);
        put("TextPane.background", bgContainer);
        put("TextPane.foreground", text);

        // =================================================================
        // 下拉 / 旋转框
        // =================================================================
        put("ComboBox.background", bgContainer);
        put("ComboBox.foreground", text);
        put("ComboBox.buttonBackground", bgContainer);
        put("ComboBox.buttonEditableBackground", bgContainer);
        put("ComboBox.arc", st.getBorderRadius());
        put("ComboBox.borderColor", border);
        put("ComboBox.hoverBorderColor", primaryHover);
        put("ComboBox.focusedBorderColor", primary);

        put("Spinner.background", bgContainer);
        put("Spinner.foreground", text);

        // =================================================================
        // 复选框 / 单选框
        // =================================================================
        put("CheckBox.icon.background", bgContainer);
        put("CheckBox.icon.borderColor", border);
        put("CheckBox.icon.selectedBackground", primary);
        put("CheckBox.icon.selectedBorderColor", primary);
        put("CheckBox.icon.hoverBorderColor", primary);
        put("CheckBox.icon.checkmarkColor", white);
        put("CheckBox.foreground", text);

        put("RadioButton.icon.background", bgContainer);
        put("RadioButton.icon.borderColor", border);
        put("RadioButton.icon.selectedBorderColor", primary);
        put("RadioButton.icon.selectedCenterColor", primary);
        put("RadioButton.icon.hoverBorderColor", primary);
        put("RadioButton.foreground", text);

        // =================================================================
        // 列表 / 树 / 表格
        // =================================================================
        put("List.background", bgContainer);
        put("List.foreground", text);
        put("List.selectionBackground", primaryBg);
        put("List.selectionForeground", text);

        put("Tree.background", bgContainer);
        put("Tree.foreground", text);
        put("Tree.selectionBackground", primaryBg);
        put("Tree.selectionForeground", text);
        put("Tree.hash", borderSecondary);

        put("Table.background", bgContainer);
        put("Table.foreground", text);
        put("Table.selectionBackground", primaryBg);
        put("Table.selectionForeground", text);
        put("Table.gridColor", borderSecondary);
        put("TableHeader.background",
                theme.isDark() ? bgElevated : uiColor(ColorToken.parseHexColor("#FAFAFA")));
        put("TableHeader.foreground", textSecondary);

        // =================================================================
        // 选项卡
        // =================================================================
        put("TabbedPane.background", bgContainer);
        put("TabbedPane.foreground", text);
        put("TabbedPane.underlineColor", primary);
        put("TabbedPane.contentAreaColor", borderSecondary);

        // =================================================================
        // 滚动面板 / 分割面板
        // =================================================================
        put("ScrollPane.background", bgContainer);
        put("ScrollPane.foreground", text);
        put("SplitPane.background", bgLayout);

        // =================================================================
        // 菜单
        // =================================================================
        put("MenuBar.background", bgContainer);
        put("MenuBar.foreground", text);
        put("MenuBar.borderColor", borderSecondary);

        put("Menu.background", bgElevated);
        put("Menu.foreground", text);
        put("Menu.selectionBackground", primaryBg);
        put("Menu.selectionForeground", text);

        put("MenuItem.background", bgElevated);
        put("MenuItem.foreground", text);
        put("MenuItem.selectionBackground", primaryBg);
        put("MenuItem.selectionForeground", text);

        put("PopupMenu.background", bgElevated);
        put("PopupMenu.foreground", text);
        put("PopupMenu.borderColor", border);
        put("PopupMenu.selectionBackground", transparent);
        put("MenuItem.selectionBackground", transparent);

        // =================================================================
        // 工具栏
        // =================================================================
        put("ToolBar.background", bgContainer);
        put("ToolBar.foreground", text);

        // =================================================================
        // 分割线
        // =================================================================
        put("Separator.foreground", borderSecondary);
        put("Separator.background", borderSecondary);

        // =================================================================
        // 全局组件属性
        // =================================================================
        put("Component.focusColor", primary);
        put("Component.focusWidth", st.getControlOutlineWidth());
        put("Component.arc", st.getBorderRadius());
        put("Component.borderColor", border);

        // =================================================================
        // 进度条
        // =================================================================
        put("ProgressBar.foreground", primary);
        put("ProgressBar.background", uiColor(ct.getDisabledBgColor()));

        // =================================================================
        // 工具提示
        // =================================================================
        put("ToolTip.background", uiColor(ct.getBgSpotlight()));
        put("ToolTip.foreground", white);
        put("ToolTip.borderColor", uiColor(ct.getBgSpotlight()));

        // =================================================================
        // 滚动条
        // =================================================================
        put("ScrollBar.track", transparent);
        put("ScrollBar.thumb", uiColor(ct.getFillColor()));
        put("ScrollBar.hoverThumbColor", textQuaternary);

        // =================================================================
        // Slider
        // =================================================================
        put("Slider.trackColor", border);
        put("Slider.thumbColor", bgContainer);
        put("Slider.thumbBorderColor", primary);
        put("Slider.trackValueColor", primary);

        // =================================================================
        // 其他
        // =================================================================
        put("TitledBorder.titleColor", text);
        put("TitledBorder.borderColor", border);
        put("OptionPane.background", bgContainer);
        put("OptionPane.foreground", text);
        put("OptionPane.messageForeground", text);
        put("FileChooser.background", bgContainer);
        put("FileChooser.foreground", text);
    }

    /**
     * 将 Color 转为 UIResource 兼容色，保留 alpha 通道。
     *
     * <p>FlatLaf 的 {@code LookAndFeel.installColorsAndFont()} 使用
     * {@code bg instanceof UIResource} 判断能否覆盖组件颜色。
     * 只有 UIResource 才能被下次主题切换时正确替换。
     */
    private static Color uiColor(Color c) {
        if (c instanceof UIResource) {
            return c;
        }
        if (c.getAlpha() == 255) {
            return new ColorUIResource(c.getRed(), c.getGreen(), c.getBlue());
        }
        // ColorUIResource(r,g,b) 会丢失 alpha；使用内部类保留透明度
        return new AlphaColorUIResource(c);
    }

    /** 带 alpha 通道的 UIResource 颜色（兼容 Java 8+）。 */
    private static final class AlphaColorUIResource extends Color
            implements javax.swing.plaf.UIResource {
        AlphaColorUIResource(Color c) {
            super(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }
    }

    /**
     * 同时写入 UIManager 用户默认值和 LAF 默认值。
     */
    private static void put(String key, Object value) {
        UIManager.put(key, value);
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        if (lafDefaults != null) {
            lafDefaults.put(key, value);
        }
    }

    /**
     * 兜底：遍历所有 Window，强制设置根容器背景并重绘。
     *
     * <p>某些情况下（如 JFrame 的 contentPane 背景被其他代码设置为非 UIResource），
     * FlatLaf.updateUI() 无法覆盖。这里用令牌颜色强制刷新。
     */
    private void repaintAllWindows() {
        Color bg = uiColor(currentTheme.getColorToken().getBgContainer());
        Color fg = uiColor(currentTheme.getColorToken().getTextColor());

        for (Window window : Window.getWindows()) {
            // JFrame / JDialog 的 contentPane
            if (window instanceof RootPaneContainer) {
                Container contentPane = ((RootPaneContainer) window).getContentPane();
                contentPane.setBackground(bg);
                contentPane.setForeground(fg);
            }
            window.repaint();
        }
    }

    private void notifyListeners(AntTheme theme) {
        for (Consumer<AntTheme> listener : listeners) {
            try {
                listener.accept(theme);
            } catch (Exception e) {
                log.error("Theme change listener threw exception", e);
            }
        }
    }
}
