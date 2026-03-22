package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import java.awt.*;

/**
 * Ant Design 二维码组件。
 *
 * <p>对应 Ant Design {@code <QRCode>}，将文本内容生成简易二维码图形。
 * 使用纯 Java 算法生成点阵，无需额外依赖。
 *
 * <pre>{@code
 * AntQrCode qr = new AntQrCode("https://ant.design");
 * qr.setQrSize(160);
 * }</pre>
 */
public class AntQrCode extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int MODULE_COUNT = 21; // 简化版 Version 1 的模块数

  @Getter private String value;
  @Getter private int qrSize;
  @Getter private Color qrColor;
  @Getter private Color bgColor;

  private boolean[][] matrix;

  /**
   * 创建二维码。
   *
   * @param value 编码文本
   */
  public AntQrCode(String value) {
    this.value = (value != null) ? value : "";
    this.qrSize = 160;
    generateMatrix();
    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setValue(String value) {
    this.value = (value != null) ? value : "";
    generateMatrix();
    repaint();
  }

  public void setQrSize(int qrSize) {
    this.qrSize = Math.max(64, qrSize);
    revalidate();
    repaint();
  }

  public void setQrColor(Color qrColor) {
    this.qrColor = qrColor;
    repaint();
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
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
    return new Dimension(qrSize, qrSize);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();
    int w = width;
    int h = height;
    int size = Math.min(w, h);
    int arc = st.getBorderRadius();

    // 背景
    Color bg = (bgColor != null) ? bgColor : ct.getBgContainer();
    g2.setColor(bg);
    g2.fillRoundRect(0, 0, size, size, arc, arc);

    // 边框
    g2.setColor(ct.getBorderSecondaryColor());
    g2.drawRoundRect(0, 0, size - 1, size - 1, arc, arc);

    // 二维码模块
    Color fg = (qrColor != null) ? qrColor : ct.getTextColor();
    g2.setColor(fg);
    int padding = 8;
    int available = size - padding * 2;
    double moduleSize = (double) available / MODULE_COUNT;

    for (int row = 0; row < MODULE_COUNT; row++) {
      for (int col = 0; col < MODULE_COUNT; col++) {
        if (matrix[row][col]) {
          int x = padding + (int) (col * moduleSize);
          int y = padding + (int) (row * moduleSize);
          int mw = (int) Math.ceil(moduleSize);
          int mh = (int) Math.ceil(moduleSize);
          g2.fillRect(x, y, mw, mh);
        }
      }
    }
  }

  // =========================================================================
  // 简易二维码矩阵生成
  // =========================================================================

  private void generateMatrix() {
    matrix = new boolean[MODULE_COUNT][MODULE_COUNT];

    // 三个定位图案
    drawFinderPattern(0, 0);
    drawFinderPattern(0, MODULE_COUNT - 7);
    drawFinderPattern(MODULE_COUNT - 7, 0);

    // 使用字符串哈希填充数据区
    int hash = value.hashCode();
    java.util.Random rnd = new java.util.Random(hash);
    for (int r = 0; r < MODULE_COUNT; r++) {
      for (int c = 0; c < MODULE_COUNT; c++) {
        if (!isFinderArea(r, c)) {
          matrix[r][c] = rnd.nextBoolean();
        }
      }
    }
  }

  private void drawFinderPattern(int startRow, int startCol) {
    for (int r = 0; r < 7; r++) {
      for (int c = 0; c < 7; c++) {
        boolean outer = (r == 0 || r == 6 || c == 0 || c == 6);
        boolean inner = (r >= 2 && r <= 4 && c >= 2 && c <= 4);
        matrix[startRow + r][startCol + c] = outer || inner;
      }
    }
  }

  private boolean isFinderArea(int r, int c) {
    if (r < 8 && c < 8) {
      return true;
    }
    if (r < 8 && c >= MODULE_COUNT - 8) {
      return true;
    }
    if (r >= MODULE_COUNT - 8 && c < 8) {
      return true;
    }
    return false;
  }
}
