package com.antdesign.swing.display;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Ant Design 日历组件。
 *
 * <p>对应 Ant Design {@code <Calendar>}，按照日历形式展示数据的容器。
 * 支持月份导航和日期选中。
 *
 * <pre>{@code
 * AntCalendar calendar = new AntCalendar();
 * calendar.addChangeListener(e -> System.out.println("Selected: " + e.getNewValue()));
 * }</pre>
 */
public class AntCalendar extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;
  private static final String[] WEEK_DAYS = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

  @Getter private LocalDate selectedDate;
  @Getter private YearMonth displayMonth;
  @Getter private boolean fullscreen;

  private AntChangeListener<LocalDate> changeListener;

  /** 创建显示当前月份的日历。 */
  public AntCalendar() {
    this.selectedDate = LocalDate.now();
    this.displayMonth = YearMonth.now();
    this.fullscreen = true;
    setLayout(new BorderLayout());
    rebuildUi();
    setThemeListener(theme -> rebuildUi());
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setSelectedDate(LocalDate date) {
    LocalDate old = this.selectedDate;
    this.selectedDate = (date != null) ? date : LocalDate.now();
    this.displayMonth = YearMonth.from(this.selectedDate);
    rebuildUi();
    if (changeListener != null) {
      changeListener.valueChanged(new AntChangeEvent<>(this, old, this.selectedDate));
    }
  }

  public void setDisplayMonth(YearMonth month) {
    this.displayMonth = (month != null) ? month : YearMonth.now();
    rebuildUi();
  }

  public void setFullscreen(boolean fullscreen) {
    this.fullscreen = fullscreen;
    rebuildUi();
  }

  public void addChangeListener(AntChangeListener<LocalDate> listener) {
    this.changeListener = listener;
  }

  /** 导航到上一月。 */
  public void previousMonth() {
    displayMonth = displayMonth.minusMonths(1);
    rebuildUi();
  }

  /** 导航到下一月。 */
  public void nextMonth() {
    displayMonth = displayMonth.plusMonths(1);
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

    // 头部导航
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);
    header.setBorder(BorderFactory.createEmptyBorder(
        st.getPaddingSm(), st.getPadding(), st.getPaddingSm(), st.getPadding()));

    JLabel prevBtn = createNavLabel("◀");
    prevBtn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        previousMonth();
      }
    });
    header.add(prevBtn, BorderLayout.WEST);

    String monthYear = displayMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        + " " + displayMonth.getYear();
    JLabel titleLabel = new JLabel(monthYear, SwingConstants.CENTER);
    titleLabel.setFont(ft.createFont(ft.getFontSizeLg(), Font.BOLD));
    titleLabel.setForeground(ct.getTextColor());
    header.add(titleLabel, BorderLayout.CENTER);

    JLabel nextBtn = createNavLabel("▶");
    nextBtn.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        nextMonth();
      }
    });
    header.add(nextBtn, BorderLayout.EAST);
    add(header, BorderLayout.NORTH);

    // 日历网格
    JPanel grid = new JPanel(new GridLayout(0, 7, 1, 1));
    grid.setOpaque(false);
    grid.setBorder(BorderFactory.createEmptyBorder(0, st.getPadding(), st.getPadding(), st.getPadding()));

    // 星期头
    for (String day : WEEK_DAYS) {
      JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
      dayLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.BOLD));
      dayLabel.setForeground(ct.getTextTertiaryColor());
      dayLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
      grid.add(dayLabel);
    }

    // 计算日期
    LocalDate firstDay = displayMonth.atDay(1);
    int startDow = firstDay.getDayOfWeek().getValue() % 7; // Sunday=0
    int daysInMonth = displayMonth.lengthOfMonth();
    LocalDate today = LocalDate.now();

    // 填充前置空白
    for (int i = 0; i < startDow; i++) {
      grid.add(new JLabel());
    }

    // 日期按钮
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = displayMonth.atDay(day);
      JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
      int cellSize = fullscreen ? 36 : 24;
      dayLabel.setPreferredSize(new Dimension(cellSize, cellSize));
      dayLabel.setFont(ft.createFont(fullscreen ? ft.getFontSize() : ft.getFontSizeSm(), Font.PLAIN));
      dayLabel.setOpaque(true);
      dayLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      if (date.equals(selectedDate)) {
        dayLabel.setBackground(ct.getPrimaryColor());
        dayLabel.setForeground(Color.WHITE);
      } else if (date.equals(today)) {
        dayLabel.setBackground(ct.getPrimaryBgColor());
        dayLabel.setForeground(ct.getPrimaryColor());
      } else {
        dayLabel.setBackground(ct.getBgContainer());
        dayLabel.setForeground(ct.getTextColor());
      }

      LocalDate clickDate = date;
      dayLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          setSelectedDate(clickDate);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          if (!clickDate.equals(selectedDate)) {
            dayLabel.setBackground(ct.getFillSecondaryColor());
          }
        }

        @Override
        public void mouseExited(MouseEvent e) {
          if (clickDate.equals(selectedDate)) {
            dayLabel.setBackground(ct.getPrimaryColor());
          } else if (clickDate.equals(today)) {
            dayLabel.setBackground(ct.getPrimaryBgColor());
          } else {
            dayLabel.setBackground(ct.getBgContainer());
          }
        }
      });
      grid.add(dayLabel);
    }

    add(grid, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  private JLabel createNavLabel(String text) {
    ColorToken ct = colorToken();
    JLabel label = new JLabel(text);
    label.setForeground(ct.getTextSecondaryColor());
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    return label;
  }
}
