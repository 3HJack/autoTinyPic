package com.hhh.tinypic;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.util.prefs.Preferences;

public class TinyPicSwitchAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    String apiKey = Messages.showInputDialog(e.getProject(), "Enable(valid key) or Disable(empty key) TinyPic",
        TinyPicManager.KEY_DIALOG_TITLE, Messages.getInformationIcon());
    if (apiKey != null) {
      Preferences.userRoot().put(TinyPicManager.KEY_API, apiKey);
      TinyPicManager.getInstance().initTinyPic(e.getProject());
    }
  }
}
