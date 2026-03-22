package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Ant Design 统计数值组件。
 *
 * <p>对应 Ant Design {@code <Statistic>}，展示统计数值。
 * 支持标题、前缀、后缀、精度和自定义颜色。
 *
 * <pre>{@code
 * AntStatistic stat = new AntStatistic("Active Users", 112893);
 * stat.setPrecision(0);
 * stat.setPrefix("👤 ");
 * }</pre>
 */
public class AntStatistic extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private String title;
  @Getter private double value;
  @Getter private int precision;
  @Getter private String prefix;
  @Getter private String suffix;
  @Getter private Icon prefixIcon;
  @Getter private Icon suffixIcon;
  @Getter private Color valueColor;
  @Getter private boolean groupSeparator;

  /**
   * 创建统计数值。
   *
   * @param title 标题
   * @param value 数值
   */
  public AntStatistic(String title, double value) {
    this.title = (title != null) ? title : "";
    this.value = value;
    this.precision = -1;
    this.groupSeparator = true;
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setTitle(String title) {
    this.title = (title != null) ? title : "";
    revalidate();
    repaint();
  }

  public void setValue(double value) {
    this.value = value;
    repaint();
  }

  public void setPrecision(int precision) {
    this.precision = precision;
    repaint();
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
    revalidate();
    repaint();
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
    revalidate();
    repaint();
  }

  public void setPrefixIcon(Icon prefixIcon) {
    this.prefixIcon = prefixIcon;
    revalidate();
    repaint();
  }

  public void setSuffixIcon(Icon suffixIcon) {
    this.suffixIcon = suffixIcon;
    revalidate();
    repaint();
  }

  public void setValueColor(Color valueColor) {
    this.valueColor = valueColor;
    repaint();
  }

  public void setGroupSeparator(boolean groupSeparator) {
    this.groupSeparator = groupSeparator;
    repaint();
  }

  // =========================================================================
  // 尺寸
  // =========================================================================

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    FontToken ft = fontToken();

    Font titleFont = ft.createFont(ft.getFontSize(), Font.PLAIN);
    Font valueFont = ft.createFont(ft.getFontSizeHeading3(), Font.BOLD);
    FontMetrics titleFm = getFontMetrics(titleFont);
    FontMetrics valueFm = getFontMetrics(valueFont);

    String valStr = formatValue();
    String full = buildDisplayString(valStr);
    int w = Math.max(titleFm.stringWidth(title), valueFm.stringWidth(full));
    int iconExtra = 0;
    if (prefixIcon != null) {
      iconExtra += prefixIcon.getIconWidth() + 4;
    }
    if (suffixIcon != null) {
      iconExtra += suffixIcon.getIconWidth() + 4;
    }

    int h = titleFm.getHeight() + 4 + valueFm.getHeight();
    return new Dimension(w + iconExtra + 16, h + 8);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();

    // 标题
    Font titleFont = ft.createFont(ft.getFontSize(), Font.PLAIN);
    g2.setFont(titleFont);
    g2.setColor(ct.getTextSecondaryColor());
    FontMetrics titleFm = g2.getFontMetrics();
    g2.drawString(title, 0, titleFm.getAscent());

    // 数值
    int valueY = titleFm.getHeight() + 4;
    Font valueFont = ft.createFont(ft.getFontSizeHeading3(), Font.BOLD);
    g2.setFont(valueFont);
    g2.setColor((valueColor != null) ? valueColor : ct.getTextColor());
    FontMetrics valueFm = g2.getFontMetrics();

    int x = 0;

    if (prefixIcon != null) {
      prefixIcon.paintIcon(this, g2, x,
          valueY + (valueFm.getHeight() - prefixIcon.getIconHeight()) / 2);
      x += prefixIcon.getIconWidth() + 4;
    }

    String valStr = formatValue();
    String full = buildDisplayString(valStr);
    g2.drawString(full, x, valueY + valueFm.getAscent());
    x += valueFm.stringWidth(full) + 4;

    if (suffixIcon != null) {
      suffixIcon.paintIcon(this, g2, x,
          valueY + (valueFm.getHeight() - suffixIcon.getIconHeight()) / 2);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private String formatValue() {
    if (precision >= 0) {
      StringBuilder pattern = new StringBuilder(groupSeparator ? "#,##0" : "0");
      if (precision > 0) {
        pattern.append('.');
        for (int i = 0; i < precision; i++) {
          pattern.append('0');
        }
      }
      return new DecimalFormat(pattern.toString()).format(value);
    }
    if (value == Math.floor(value) && !Double.isInfinite(value)) {
      if (groupSeparator) {
        return new DecimalFormat("#,##0").format((long) value);
      }
      return String.valueOf((long) value);
    }
    if (groupSeparator) {
      return new DecimalFormat("#,##0.##").format(value);
    }
    return String.valueOf(value);
  }

  private String buildDisplayString(String valStr) {
    StringBuilder sb = new StringBuilder();
    if (prefix != null) {
      sb.append(prefix);
    }
    sb.append(valStr);
    if (suffix != null) {
      sb.append(suffix);
    }
    return sb.toString();
  }
}
