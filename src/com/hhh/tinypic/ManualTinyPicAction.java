package com.hhh.tinypic;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.prefs.Preferences;

public class ManualTinyPicAction extends AnAction {
  private final ArrayList<VirtualFile> mPictureFiles = new ArrayList<>();
  private ProgressDialog mProgressDialog;

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (!mPictureFiles.isEmpty()) {
      Messages.showMessageDialog(project, "Please wait for the last compression to complete!!!",
          TinyPicManager.KEY_DIALOG_TITLE, Messages.getInformationIcon());
      return;
    }
    String apiKey = Preferences.userRoot().get(TinyPicManager.KEY_API, "");
    if (StringUtils.isEmpty(apiKey)) {
      Messages.showMessageDialog(project, "ApiKey is empty!!!", TinyPicManager.KEY_DIALOG_TITLE, Messages.getInformationIcon());
      return;
    }
    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, true);
    VirtualFile[] selectedFiles = FileChooser.chooseFiles(descriptor, project, null);
    if (selectedFiles.length == 0) {
      return;
    }
    mProgressDialog = new ProgressDialog(project, false);
    TinyPicManager.getInstance().getExecutorService().submit(() -> {
      filterAllPictures(selectedFiles);
      if (mPictureFiles.isEmpty()) {
        ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(project,
            "No images available for compression!!!", TinyPicManager.KEY_DIALOG_TITLE, Messages.getInformationIcon()));
      } else {
        ApplicationManager.getApplication().invokeLater(() -> showProgressDialog());
        compressPicFile();
      }
    });
  }

  private void filterAllPictures(VirtualFile[] selectedFiles) {
    for (VirtualFile virtualFile : selectedFiles) {
      if (virtualFile.isDirectory()) {
        VirtualFile[] directoryChildren = virtualFile.getChildren();
        filterAllPictures(directoryChildren);
      } else if (TinyPicManager.isPicFile(virtualFile)) {
        mPictureFiles.add(virtualFile);
      }
    }
  }

  private void compressPicFile() {
    int size = mPictureFiles.size();
    for (int i = 0; i < size; ) {
      TinyPicManager.getInstance().compressPicFile(mPictureFiles.get(i));
      ++i;
      if (i == size) {
        ApplicationManager.getApplication().invokeLater(() -> mProgressDialog.close(DialogWrapper.OK_EXIT_CODE));
      } else {
        mProgressDialog.setCurrentIndex(i);
      }
    }
    mPictureFiles.clear();
  }

  private void showProgressDialog() {
    mProgressDialog.setTitle(TinyPicManager.KEY_DIALOG_TITLE);
    mProgressDialog.setMaxImages(mPictureFiles.size());
    mProgressDialog.setModal(false);
    mProgressDialog.show();
  }
}
