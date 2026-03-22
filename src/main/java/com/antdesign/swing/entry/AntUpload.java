package com.antdesign.swing.entry;

import com.antdesign.swing.base.AbstractAntComponent;
import com.antdesign.swing.event.AntChangeEvent;
import com.antdesign.swing.event.AntChangeListener;
import com.antdesign.swing.theme.token.ColorToken;
import com.antdesign.swing.theme.token.FontToken;
import com.antdesign.swing.theme.token.SizeToken;
import com.antdesign.swing.util.GraphicsUtils;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Design 上传组件。
 *
 * <p>对应 Ant Design {@code <Upload>}，文件选择/拖拽上传。
 * 支持点击选择和拖拽上传，显示文件列表。
 *
 * <pre>{@code
 * AntUpload upload = new AntUpload();
 * upload.setDragger(true);
 * upload.addChangeListener(e -> System.out.println("Files: " + e.getNewValue()));
 * }</pre>
 */
public class AntUpload extends AbstractAntComponent {

  private static final long serialVersionUID = 1L;

  @Getter private boolean dragger;
  @Getter private boolean multiple;
  @Getter private String accept;
  @Getter private String text;

  private final List<File> fileList = new ArrayList<>();
  private boolean hovered;
  private final List<AntChangeListener<List<File>>> changeListeners = new ArrayList<>();

  /** 创建上传组件。 */
  public AntUpload() {
    this.text = "Click or drag file to this area to upload";
    this.multiple = false;
    this.dragger = true;

    setLayout(new BorderLayout());
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
      @Override
      public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
      @Override
      public void mouseClicked(MouseEvent e) { openFileChooser(); }
    });

    // 拖放
    setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
      @Override
      @SuppressWarnings("unchecked")
      public void drop(DropTargetDropEvent dtde) {
        try {
          dtde.acceptDrop(DnDConstants.ACTION_COPY);
          List<File> files = (List<File>) dtde.getTransferable()
              .getTransferData(DataFlavor.javaFileListFlavor);
          if (files != null) {
            if (multiple) {
              fileList.addAll(files);
            } else {
              fileList.clear();
              if (!files.isEmpty()) {
                fileList.add(files.get(0));
              }
            }
            fireChange();
            rebuildFileList();
          }
          dtde.dropComplete(true);
        } catch (Exception ex) {
          dtde.dropComplete(false);
        }
      }
    }));

    // Theme listener handled by base class
  }

  // =========================================================================
  // Setter
  // =========================================================================

  public void setDragger(boolean dragger) { this.dragger = dragger; repaint(); }
  public void setMultiple(boolean multiple) { this.multiple = multiple; }
  public void setAccept(String accept) { this.accept = accept; }
  public void setText(String text) { this.text = text; repaint(); }

  /** 获取已选文件列表。 */
  public List<File> getFileList() { return new ArrayList<>(fileList); }

  /** 清空文件列表。 */
  public void clearFiles() {
    fileList.clear();
    rebuildFileList();
    fireChange();
  }

  public void addChangeListener(AntChangeListener<List<File>> listener) {
    if (listener != null) {
      changeListeners.add(listener);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    }
    int listH = fileList.size() * 24;
    return dragger ? new Dimension(400, 120 + listH) : new Dimension(200, 32 + listH);
  }

  // =========================================================================
  // 绘制
  // =========================================================================  @Override
  protected void paintAnt(Graphics2D g2, int width, int height) {
    ColorToken ct = colorToken();
    FontToken ft = fontToken();
    SizeToken st = sizeToken();

    if (dragger) {
      int dropH = 100;
      int arc = st.getBorderRadius();
      Color bgColor = hovered ? ct.getPrimaryBgColor() : ct.getBgContainer();
      Color borderColor = hovered ? ct.getPrimaryColor() : ct.getBorderColor();

      g2.setColor(bgColor);
      g2.fillRoundRect(0, 0, width, dropH, arc, arc);
      GraphicsUtils.drawDashedRoundRect(g2, 0, 0, width - 1, dropH - 1,
          arc, borderColor);

      g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      g2.setColor(ct.getTextSecondaryColor());
      GraphicsUtils.drawCenteredText(g2, text, 0, 0, width, dropH);
    } else {
      g2.setFont(ft.createFont(ft.getFontSize(), Font.PLAIN));
      g2.setColor(ct.getPrimaryColor());
      g2.drawString("📎 " + (text != null ? text : "Upload"), 0,
          g2.getFontMetrics().getAscent());
    }
  }

  // =========================================================================
  // 内部
  // =========================================================================

  private void openFileChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(multiple);
    if (accept != null && !accept.isEmpty()) {
      chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
          accept, accept.replace(".", "").split(",")));
    }
    int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
    if (result == JFileChooser.APPROVE_OPTION) {
      if (multiple) {
        File[] files = chooser.getSelectedFiles();
        for (File f : files) {
          fileList.add(f);
        }
      } else {
        fileList.clear();
        fileList.add(chooser.getSelectedFile());
      }
      fireChange();
      rebuildFileList();
    }
  }

  private void rebuildFileList() {
    // 移除之前的文件列表面板
    for (java.awt.Component c : getComponents()) {
      remove(c);
    }

    if (!fileList.isEmpty()) {
      ColorToken ct = colorToken();
      FontToken ft = fontToken();

      JPanel listPanel = new JPanel();
      listPanel.setOpaque(false);
      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
      listPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

      for (File f : fileList) {
        JLabel fileLabel = new JLabel("📄 " + f.getName());
        fileLabel.setFont(ft.createFont(ft.getFontSizeSm(), Font.PLAIN));
        fileLabel.setForeground(ct.getTextColor());
        fileLabel.setAlignmentX(LEFT_ALIGNMENT);
        listPanel.add(fileLabel);
        listPanel.add(Box.createVerticalStrut(2));
      }
      add(listPanel, BorderLayout.SOUTH);
    }

    revalidate();
    repaint();
  }

  private void fireChange() {
    List<File> copy = getFileList();
    AntChangeEvent<List<File>> evt = new AntChangeEvent<>(this, null, copy);
    for (AntChangeListener<List<File>> l : changeListeners) {
      l.valueChanged(evt);
    }
  }
}
