package com.hhh.tinypic;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends DialogWrapper {
  private int mMaxImages = 0;
  private JProgressBar mJProgressBar;

  protected ProgressDialog(@Nullable Project project, boolean canBeParent) {
    super(project, canBeParent);
  }


  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    mJProgressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, mMaxImages);
    mJProgressBar.setString(0 + "/" + mMaxImages);
    mJProgressBar.setValue(0);
    mJProgressBar.setStringPainted(true);

    JPanel jPanel = new JPanel();
    jPanel.setLayout(new GridLayout(1, 1));
    jPanel.add(mJProgressBar);
    return jPanel;
  }

  public void setMaxImages(int maxImages) {
    mMaxImages = maxImages;
  }

  public void setCurrentIndex(int currentIndex) {
    mJProgressBar.setString(currentIndex + "/" + mMaxImages);
    mJProgressBar.setValue(currentIndex);
  }

  @Override
  public void show() {
    init();
    super.show();
  }
}
