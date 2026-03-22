package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.ComponentSize;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Ant Design 列表组件。
 *
 * <p>对应 Ant Design {@code <List>}，最基础的列表展示，可承载文本、列表、图片、段落。
 * 支持自定义渲染函数、头部/底部、边框和尺寸。
 *
 * <pre>{@code
 * AntList<String> list = new AntList<>();
 * list.setRenderItem(item -> new JLabel(item));
 * list.setDataSource(Arrays.asList("Item 1", "Item 2", "Item 3"));
 * }</pre>
 *
 * @param <T> 列表数据项类型
 */
public class AntList<T> extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private List<T> dataSource;
  @Getter private String header;
  @Getter private String footer;
  @Getter private boolean bordered;
  @Getter private boolean split;
  private ComponentSize size;
  private Function<T, Component> renderItem;

  /**
   * 创建默认列表。
   */
  public AntList() {
    this.dataSource = new ArrayList<>();
    this.bordered = false;
    this.split = true;
    this.size = ComponentSize.MIDDLE;

    setLayout(new BorderLayout());
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setDataSource(List<T> dataSource) {
    this.dataSource = (dataSource != null) ? dataSource : new ArrayList<>();
    rebuildUi();
  }

  public void setRenderItem(Function<T, Component> renderItem) {
    this.renderItem = renderItem;
    rebuildUi();
  }

  public void setHeader(String header) {
    this.header = header;
    rebuildUi();
  }

  public void setFooter(String footer) {
    this.footer = footer;
    rebuildUi();
  }

  public void setBordered(boolean bordered) {
    this.bordered = bordered;
    rebuildUi();
  }

  public void setSplit(boolean split) {
    this.split = split;
    rebuildUi();
  }

  public void setSize(ComponentSize size) {
    this.size = (size != null) ? size : ComponentSize.MIDDLE;
    rebuildUi();
  }

  public ComponentSize getComponentSize() {
    return size;
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    int pad;
    int fontSize;
    switch (size) {
      case LARGE:
        pad = st.getPaddingLg();
        fontSize = ft.getFontSizeLg();
        break;
      case SMALL:
        pad = st.getPaddingXs();
        fontSize = ft.getFontSizeSm();
        break;
      default:
        pad = st.getPaddingSm();
        fontSize = ft.getFontSize();
        break;
    }

    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.setOpaque(false);

    // 头部
    if (header != null && !header.isEmpty()) {
      JPanel headerPanel = new JPanel(new BorderLayout());
      headerPanel.setOpaque(false);
      headerPanel.setBorder(BorderFactory.createCompoundBorder(
          split ? BorderFactory.createMatteBorder(0, 0, 1, 0,
              ct.getBorderSecondaryColor()) : BorderFactory.createEmptyBorder(),
          BorderFactory.createEmptyBorder(pad, pad, pad, pad)));
      JLabel headerLabel = new JLabel(header);
      headerLabel.setFont(ft.createFont(fontSize, Font.BOLD));
      headerLabel.setForeground(ct.getTextColor());
      headerPanel.add(headerLabel, BorderLayout.CENTER);
      container.add(headerPanel);
    }

    // 列表项
    if (dataSource.isEmpty()) {
      JPanel emptyPanel = new JPanel(new BorderLayout());
      emptyPanel.setOpaque(false);
      emptyPanel.setBorder(BorderFactory.createEmptyBorder(pad * 2, pad, pad * 2, pad));
      JLabel emptyLabel = new JLabel("No Data", JLabel.CENTER);
      emptyLabel.setForeground(ct.getTextTertiaryColor());
      emptyPanel.add(emptyLabel, BorderLayout.CENTER);
      container.add(emptyPanel);
    } else {
      for (int i = 0; i < dataSource.size(); i++) {
        T item = dataSource.get(i);
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);
        boolean isLast = (i == dataSource.size() - 1);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            (split && !isLast) ? BorderFactory.createMatteBorder(0, 0, 1, 0,
                ct.getBorderSecondaryColor()) : BorderFactory.createEmptyBorder(),
            BorderFactory.createEmptyBorder(pad, pad, pad, pad)));

        if (renderItem != null) {
          itemPanel.add(renderItem.apply(item), BorderLayout.CENTER);
        } else {
          JLabel label = new JLabel(String.valueOf(item));
          label.setFont(ft.createFont(fontSize, Font.PLAIN));
          label.setForeground(ct.getTextColor());
          itemPanel.add(label, BorderLayout.CENTER);
        }
        container.add(itemPanel);
      }
    }

    // 底部
    if (footer != null && !footer.isEmpty()) {
      JPanel footerPanel = new JPanel(new BorderLayout());
      footerPanel.setOpaque(false);
      footerPanel.setBorder(BorderFactory.createCompoundBorder(
          split ? BorderFactory.createMatteBorder(1, 0, 0, 0,
              ct.getBorderSecondaryColor()) : BorderFactory.createEmptyBorder(),
          BorderFactory.createEmptyBorder(pad, pad, pad, pad)));
      JLabel footerLabel = new JLabel(footer);
      footerLabel.setFont(ft.createFont(fontSize, Font.PLAIN));
      footerLabel.setForeground(ct.getTextSecondaryColor());
      footerPanel.add(footerLabel, BorderLayout.CENTER);
      container.add(footerPanel);
    }

    JScrollPane scroll = new JScrollPane(container);
    scroll.setBorder(bordered
        ? BorderFactory.createLineBorder(ct.getBorderSecondaryColor())
        : BorderFactory.createEmptyBorder());
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    add(scroll, BorderLayout.CENTER);

    revalidate();
    repaint();
  }
}
