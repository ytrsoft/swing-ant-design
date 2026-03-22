package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.model.Variant;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ant Design 级联选择组件。
 *
 * <p>对应 Ant Design {@code <Cascader>}，需要从一组相关联的数据集合中进行选择。
 * 使用扁平化的 JComboBox 实现，每一级联动触发下一级刷新。
 *
 * <pre>{@code
 * AntCascader cascader = new AntCascader();
 * cascader.setOptions(rootOptions);
 * }</pre>
 */
public class AntCascader extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  /** 级联选项节点。 */
  @Getter
  public static class Option {
    private final String value;
    private final String label;
    private final List<Option> children;

    public Option(String value, String label) {
      this(value, label, null);
    }

    public Option(String value, String label, List<Option> children) {
      this.value = value;
      this.label = label;
      this.children = (children != null) ? children : new ArrayList<>();
    }
  }

  @Getter private List<Option> options;
  @Getter private String placeholder;
  @Getter private Variant variant;
  private ComponentSize size;

  private final List<String> selectedPath = new ArrayList<>();
  private final List<JComboBox<String>> combos = new ArrayList<>();
  private final List<AntChangeListener<List<String>>> changeListeners = new CopyOnWriteArrayList<>();

  /** 创建级联选择器。 */
  public AntCascader() {
    this.options = new ArrayList<>();
    this.placeholder = "Please select";
    this.variant = Variant.OUTLINED;
    this.size = ComponentSize.MIDDLE;

    setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
    setThemeListener(theme -> rebuildCombos());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setOptions(List<Option> options) {
    this.options = (options != null) ? options : new ArrayList<>();
    selectedPath.clear();
    rebuildCombos();
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public void setVariant(Variant variant) {
    this.variant = (variant != null) ? variant : Variant.OUTLINED;
    rebuildCombos();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    rebuildCombos();
  }

  public ComponentSize getComponentSize() { return size; }

  /** 获取当前选中路径。 */
  public List<String> getSelectedPath() {
    return new ArrayList<>(selectedPath);
  }

  public void addChangeListener(AntChangeListener<List<String>> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void rebuildCombos() {
    removeAll();
    combos.clear();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();
    int fontSize = (size == ComponentSize.LARGE) ? ft.getFontSizeLg()
        : (size == ComponentSize.SMALL) ? ft.getFontSizeSm() : ft.getFontSize();

    buildLevel(options, 0, fontSize, ct, ft);
    revalidate();
    repaint();
  }

  private void buildLevel(List<Option> levelOptions, int level, int fontSize,
      ColorToken ct, FontToken ft) {
    if (levelOptions == null || levelOptions.isEmpty()) {
      return;
    }

    JComboBox<String> combo = new JComboBox<>();
    combo.setFont(ft.createFont(fontSize, Font.PLAIN));
    combo.setForeground(ct.getTextColor());
    combo.setBackground(ct.getBgContainer());
    combo.setPreferredSize(new Dimension(140,
        sizeToken().controlHeightOf(size)));

    combo.addItem("-- " + placeholder + " --");
    for (Option opt : levelOptions) {
      combo.addItem(opt.label);
    }

    int lvl = level;
    combo.addActionListener(e -> {
      int idx = combo.getSelectedIndex() - 1; // skip placeholder
      // 清除后续级别
      while (selectedPath.size() > lvl) {
        selectedPath.remove(selectedPath.size() - 1);
      }
      while (combos.size() > lvl + 1) {
        remove(combos.remove(combos.size() - 1));
      }

      if (idx >= 0 && idx < levelOptions.size()) {
        Option selected = levelOptions.get(idx);
        selectedPath.add(selected.value);
        if (!selected.children.isEmpty()) {
          buildLevel(selected.children, lvl + 1, fontSize, ct, ft);
        }
        AntChangeEvent<List<String>> evt = new AntChangeEvent<>(this, null, getSelectedPath());
        for (AntChangeListener<List<String>> l : changeListeners) {
          l.valueChanged(evt);
        }
      }
      revalidate();
      repaint();
    });

    combos.add(combo);
    add(combo);
  }
}
