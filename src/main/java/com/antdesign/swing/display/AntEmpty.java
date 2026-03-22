package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 空状态组件。
 *
 * <p>对应 Ant Design {@code <Empty>}，空状态时的展示占位。
 * 支持自定义图标、描述文本和底部操作区。
 *
 * <pre>{@code
 * AntEmpty empty = new AntEmpty();
 * AntEmpty custom = new AntEmpty("No data available");
 * custom.setFooter(createButton);
 * }</pre>
 */
public class AntEmpty extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String description;
  @Getter private Icon image;
  @Getter private Component footer;

  /**
   * 创建默认空状态。
   */
  public AntEmpty() {
    this("No Data");
  }

  /**
   * 创建自定义描述的空状态。
   *
   * @param description 描述文本
   */
  public AntEmpty(String description) {
    this.description = (description != null) ? description : "";
    setLayout(new BorderLayout());
    rebuildUi();
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setDescription(String description) {
    this.description = (description != null) ? description : "";
    rebuildUi();
  }

  public void setImage(Icon image) {
    this.image = image;
    rebuildUi();
  }

  public void setFooter(Component footer) {
    this.footer = footer;
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

    JPanel center = new JPanel();
    center.setOpaque(false);
    center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
    center.setBorder(BorderFactory.createEmptyBorder(
        st.getPaddingXl(), st.getPadding(), st.getPaddingXl(), st.getPadding()));

    // 图标/图片
    if (image != null) {
      JLabel imageLabel = new JLabel(image);
      imageLabel.setAlignmentX(CENTER_ALIGNMENT);
      center.add(imageLabel);
      center.add(Box.createVerticalStrut(st.getPaddingXs()));
    } else {
      // 默认空状态图标：简单的空盒子符号
      JLabel defaultIcon = new JLabel("📭");
      defaultIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
      defaultIcon.setAlignmentX(CENTER_ALIGNMENT);
      center.add(defaultIcon);
      center.add(Box.createVerticalStrut(st.getPaddingXs()));
    }

    // 描述
    JLabel descLabel = new JLabel(description);
    descLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    descLabel.setForeground(ct.getTextTertiaryColor());
    descLabel.setAlignmentX(CENTER_ALIGNMENT);
    center.add(descLabel);

    // 底部操作
    if (footer != null) {
      center.add(Box.createVerticalStrut(st.getPadding()));
      JPanel footerPanel = new JPanel();
      footerPanel.setOpaque(false);
      footerPanel.add(footer);
      footerPanel.setAlignmentX(CENTER_ALIGNMENT);
      center.add(footerPanel);
    }

    add(center, BorderLayout.CENTER);
    revalidate();
    repaint();
  }
}
