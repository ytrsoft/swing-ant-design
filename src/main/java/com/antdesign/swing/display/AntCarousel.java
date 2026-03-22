package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 走马灯组件。
 *
 * <p>对应 Ant Design {@code <Carousel>}，旋转木马/幻灯片效果。
 * 支持自动播放、指示器和手动切换。
 *
 * <pre>{@code
 * AntCarousel carousel = new AntCarousel();
 * carousel.addSlide(panel1);
 * carousel.addSlide(panel2);
 * carousel.setAutoplay(true);
 * }</pre>
 */
public class AntCarousel extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int DOT_SIZE = 8;

  @Getter private boolean autoplay;
  @Getter private int autoplaySpeed;
  @Getter private boolean dots;
  @Getter private int currentIndex;

  private final List<Component> slides = new ArrayList<>();
  private final CardLayout cardLayout;
  private final JPanel contentPanel;
  private Timer autoplayTimer;

  /** 创建走马灯。 */
  public AntCarousel() {
    this.autoplaySpeed = 3000;
    this.dots = true;
    this.cardLayout = new CardLayout();
    this.contentPanel = new JPanel(cardLayout);
    contentPanel.setOpaque(false);

    setLayout(new BorderLayout());
    add(contentPanel, BorderLayout.CENTER);

    setThemeListener(theme -> rebuildDots());
  }

  // =========================================================================
  // 幻灯片操作
  // =========================================================================

  /**
   * 添加幻灯片。
   *
   * @param slide 幻灯片组件
   */
  public void addSlide(Component slide) {
    String name = "slide-" + slides.size();
    slides.add(slide);
    contentPanel.add(slide, name);
    rebuildDots();
  }

  /** 跳转到下一张。 */
  public void next() {
    if (slides.isEmpty()) {
      return;
    }
    currentIndex = (currentIndex + 1) % slides.size();
    cardLayout.show(contentPanel, "slide-" + currentIndex);
    rebuildDots();
  }

  /** 跳转到上一张。 */
  public void previous() {
    if (slides.isEmpty()) {
      return;
    }
    currentIndex = (currentIndex - 1 + slides.size()) % slides.size();
    cardLayout.show(contentPanel, "slide-" + currentIndex);
    rebuildDots();
  }

  /**
   * 跳转到指定位置。
   *
   * @param index 幻灯片索引
   */
  public void goTo(int index) {
    if (index >= 0 && index < slides.size()) {
      currentIndex = index;
      cardLayout.show(contentPanel, "slide-" + currentIndex);
      rebuildDots();
    }
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setAutoplay(boolean autoplay) {
    this.autoplay = autoplay;
    if (autoplay) {
      startAutoplay();
    } else {
      stopAutoplay();
    }
  }

  public void setAutoplaySpeed(int autoplaySpeed) {
    this.autoplaySpeed = Math.max(500, autoplaySpeed);
    if (autoplay) {
      stopAutoplay();
      startAutoplay();
    }
  }

  public void setDots(boolean dots) {
    this.dots = dots;
    rebuildDots();
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void rebuildDots() {
    // 移除旧的指示器面板
    for (Component c : getComponents()) {
      if (c != contentPanel) {
        remove(c);
      }
    }

    if (dots && slides.size() > 1) {
      ColorToken ct = colorToken();
      JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
      dotsPanel.setOpaque(false);
      dotsPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

      for (int i = 0; i < slides.size(); i++) {
        int idx = i;
        JPanel dot = new JPanel() {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            GraphicsUtils.setupAntialiasing(g2);
            g2.setColor(idx == currentIndex ? ct.getPrimaryColor()
                : ct.getTextQuaternaryColor());
            int w = idx == currentIndex ? DOT_SIZE * 2 : DOT_SIZE;
            g2.fillRoundRect(0, 0, w, DOT_SIZE, DOT_SIZE, DOT_SIZE);
            g2.dispose();
          }
        };
        int w = (i == currentIndex) ? DOT_SIZE * 2 : DOT_SIZE;
        dot.setPreferredSize(new Dimension(w, DOT_SIZE));
        dot.setOpaque(false);
        dot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dot.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            goTo(idx);
          }
        });
        dotsPanel.add(dot);
      }
      add(dotsPanel, BorderLayout.SOUTH);
    }

    revalidate();
    repaint();
  }

  private void startAutoplay() {
    if (autoplayTimer != null) {
      autoplayTimer.stop();
    }
    autoplayTimer = new Timer(autoplaySpeed, e -> next());
    autoplayTimer.start();
  }

  private void stopAutoplay() {
    if (autoplayTimer != null) {
      autoplayTimer.stop();
      autoplayTimer = null;
    }
  }
}
