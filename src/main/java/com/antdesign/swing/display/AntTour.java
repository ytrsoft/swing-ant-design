package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntActionEvent;
import com.antdesign.swing.event.AntActionListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 漫游式引导组件。
 *
 * <p>对应 Ant Design {@code <Tour>}，用于分步引导用户了解产品。
 *
 * <pre>{@code
 * AntTour tour = new AntTour();
 * tour.addStep(button1, "Upload", "Click to upload your file.");
 * tour.addStep(button2, "Save", "Click to save your work.");
 * tour.start();
 * }</pre>
 */
public class AntTour extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final int OFFSET = 12;

  /** 引导步骤。 */
  @Getter
  public static class Step {
    private final Component target;
    private final String title;
    private final String description;

    public Step(Component target, String title, String description) {
      this.target = target;
      this.title = title;
      this.description = description;
    }
  }

  @Getter private int currentStep;
  @Getter private boolean open;

  private final List<Step> steps = new ArrayList<>();
  private final List<AntActionListener> finishListeners = new ArrayList<>();
  private Popup popup;

  /** 创建引导组件。 */
  public AntTour() {
  }

  // =========================================================================
  // 步骤操作
  // =========================================================================

  /**
   * 添加引导步骤。
   *
   * @param target      目标组件
   * @param title       步骤标题
   * @param description 步骤描述
   */
  public void addStep(Component target, String title, String description) {
    steps.add(new Step(target, title, description));
  }

  /** 开始引导。 */
  public void start() {
    if (steps.isEmpty()) {
      return;
    }
    currentStep = 0;
    open = true;
    showCurrentStep();
  }

  /** 下一步。 */
  public void next() {
    hidePopup();
    if (currentStep < steps.size() - 1) {
      currentStep++;
      showCurrentStep();
    } else {
      finish();
    }
  }

  /** 上一步。 */
  public void prev() {
    hidePopup();
    if (currentStep > 0) {
      currentStep--;
      showCurrentStep();
    }
  }

  /** 完成引导。 */
  public void finish() {
    hidePopup();
    open = false;
    AntActionEvent evt = new AntActionEvent(this, "finish");
    for (AntActionListener l : finishListeners) {
      l.actionPerformed(evt);
    }
  }

  /** 关闭引导。 */
  public void close() {
    hidePopup();
    open = false;
  }

  /** 添加完成监听器。 */
  public void addFinishListener(AntActionListener listener) {
    if (listener != null) {
      finishListeners.add(listener);
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void showCurrentStep() {
    if (currentStep < 0 || currentStep >= steps.size()) {
      return;
    }
    Step step = steps.get(currentStep);
    if (step.target == null || !step.target.isShowing()) {
      return;
    }

    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    JPanel card = new JPanel(new BorderLayout()) {      @Override
  protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        GraphicsUtils.setupAntialiasing(g2);
        int arc = st.getBorderRadiusLg();
        GraphicsUtils.drawShadow(g2, 0, 0, getWidth(), getHeight(), arc,
            new Color(0, 0, 0, 40), 4, 16);
        GraphicsUtils.fillRoundRect(g2, 0, 0, getWidth(), getHeight(), arc,
            ct.getBgElevated());
        g2.dispose();
      }
    };
    card.setOpaque(false);
    int pad = st.getPadding();
    card.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    // 标题
    JLabel titleLabel = new JLabel(step.title);
    titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
    titleLabel.setForeground(ct.getTextColor());
    titleLabel.setAlignmentX(LEFT_ALIGNMENT);
    content.add(titleLabel);
    content.add(Box.createVerticalStrut(4));

    // 描述
    JLabel descLabel = new JLabel(
        "<html><body style='width:220px'>" + step.description + "</body></html>");
    descLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
    descLabel.setForeground(ct.getTextSecondaryColor());
    descLabel.setAlignmentX(LEFT_ALIGNMENT);
    content.add(descLabel);
    content.add(Box.createVerticalStrut(st.getPaddingSm()));

    // 底部按钮和计数
    JPanel footer = new JPanel(new BorderLayout());
    footer.setOpaque(false);
    footer.setAlignmentX(LEFT_ALIGNMENT);

    JLabel counter = new JLabel((currentStep + 1) + " / " + steps.size());
    counter.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
    counter.setForeground(ct.getTextTertiaryColor());
    footer.add(counter, BorderLayout.WEST);

    JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
    btnPanel.setOpaque(false);

    if (currentStep > 0) {
      JLabel prevBtn = createLinkButton("Prev", ct);
      prevBtn.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          prev();
        }
      });
      btnPanel.add(prevBtn);
    }

    boolean isLast = (currentStep == steps.size() - 1);
    JLabel nextBtn = createPrimaryButton(isLast ? "Finish" : "Next", ct);
    nextBtn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        next();
      }
    });
    btnPanel.add(nextBtn);
    footer.add(btnPanel, BorderLayout.EAST);

    content.add(footer);
    card.add(content, BorderLayout.CENTER);

    // 定位
    Dimension pref = card.getPreferredSize();
    card.setSize(pref);
    Point screen = step.target.getLocationOnScreen();
    int px = screen.x + (step.target.getWidth() - pref.width) / 2;
    int py = screen.y + step.target.getHeight() + OFFSET;

    popup = PopupFactory.getSharedInstance().getPopup(step.target, card, px, py);
    popup.show();
  }

  private void hidePopup() {
    if (popup != null) {
      popup.hide();
      popup = null;
    }
  }

  private JLabel createLinkButton(String text, ColorToken ct) {
    JLabel label = new JLabel(text);
    label.setForeground(ct.getTextSecondaryColor());
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return label;
  }

  private JLabel createPrimaryButton(String text, ColorToken ct) {
    JLabel label = new JLabel(text);
    label.setForeground(ct.getPrimaryColor());
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return label;
  }
}
