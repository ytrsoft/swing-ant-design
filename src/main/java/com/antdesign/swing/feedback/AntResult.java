package com.antdesign.swing.feedback;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.model.Status;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.AntIcons;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Ant Design 结果页面组件。
 *
 * <p>对应 Ant Design {@code <Result>}，用于反馈一系列操作任务的处理结果，
 * 包含状态图标、标题、副标题和额外操作区域。
 *
 * <pre>{@code
 * AntResult result = new AntResult(Status.SUCCESS, "操作成功", "提交的内容已审核通过");
 *
 * // 带自定义操作
 * AntResult result = new AntResult(Status.ERROR, "提交失败", "请检查修改后重新提交");
 * result.setExtra(retryButton);
 * }</pre>
 */
public class AntResult extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private Status status;
  @Getter private String title;
  @Getter private String subTitle;
  @Getter private JComponent extra;

  // =========================================================================
  // 构造方法
  // =========================================================================

  /**
   * 创建结果页面。
   *
   * @param status   状态类型
   * @param title    标题
   * @param subTitle 副标题
   */
  public AntResult(Status status, String title, String subTitle) {
    this.status = (status != null) ? status : Status.INFO;
    this.title = (title != null) ? title : "";
    this.subTitle = subTitle;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    rebuildUi();

    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setStatus(Status status) {
    this.status = (status != null) ? status : Status.INFO;
    rebuildUi();
  }

  public void setTitle(String title) {
    this.title = (title != null) ? title : "";
    rebuildUi();
  }

  public void setSubTitle(String subTitle) {
    this.subTitle = subTitle;
    rebuildUi();
  }

  /** 设置额外操作区域（如按钮）。 */
  public void setExtra(JComponent extra) {
    this.extra = extra;
    rebuildUi();
  }

  // =========================================================================
  // 内部构建
  // =========================================================================

  private void rebuildUi() {
    removeAll();

    FontToken ft = fontToken();
    ColorToken ct = colorToken();
    SizeToken st = sizeToken();

    setBorder(BorderFactory.createEmptyBorder(
        st.getPaddingXl(), st.getPaddingXl(), st.getPaddingXl(), st.getPaddingXl()));

    // 图标
    int iconSize = 72;
    ImageIcon icon = AntIcons.filled(status.getDefaultIconName(), iconSize, status.getDefaultColor());
    JLabel iconLabel = new JLabel(icon);
    iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(iconLabel);

    add(Box.createVerticalStrut(24));

    // 标题
    JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
    titleLabel.setFont(ft.createFont(ft.getFontSizeHeading3(), Font.BOLD));
    titleLabel.setForeground(ct.getTextColor());
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(titleLabel);

    // 副标题
    if (subTitle != null && !subTitle.isEmpty()) {
      add(Box.createVerticalStrut(8));
      JLabel subLabel = new JLabel(subTitle, SwingConstants.CENTER);
      subLabel.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      subLabel.setForeground(ct.getTextSecondaryColor());
      subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      add(subLabel);
    }

    // 额外区域
    if (extra != null) {
      add(Box.createVerticalStrut(24));
      extra.setAlignmentX(Component.CENTER_ALIGNMENT);
      add(extra);
    }

    revalidate();
    repaint();
  }
}
