package com.hhh.tinypic;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.tinify.Tinify;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

public class TinyPicManager {
    public static final String KEY_DIALOG_TITLE = "autoTinyPic";
    public static final String KEY_API = "tiny_pic_api";

    private static TinyPicManager ourInstance = new TinyPicManager();

    private VirtualFileListener mFileListener;

    private TinyPicManager() {
    }

    public static TinyPicManager getInstance() {
        return ourInstance;
    }

    public void initTinyPic(Project project) {
        String apiKey = Preferences.userRoot().get(KEY_API, "");
        if (StringUtils.isEmpty(apiKey)) {
            unInitTinyPic();
            return;
        }
        if (mFileListener != null || project == null) {
            return;
        }
        Tinify.setKey(apiKey);
        if (!Tinify.validate()) {
            Messages.showMessageDialog(project, "invalid key!!!", KEY_DIALOG_TITLE, Messages.getErrorIcon());
            return;
        }
        mFileListener = new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                compressPicFile(event.getFile());
            }

            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                compressPicFile(event.getFile());
            }
        };
        VirtualFileManager.getInstance().addVirtualFileListener(mFileListener);
    }

    public void unInitTinyPic() {
        if (mFileListener != null) {
            VirtualFileManager.getInstance().removeVirtualFileListener(mFileListener);
            mFileListener = null;
        }
    }

    private void compressPicFile(VirtualFile file) {
        String fileName = file.getName();
        if (file.isDirectory() || !(fileName.endsWith(".png") || fileName.endsWith(".jpg"))) {
            return;
        }
        try {
            String filePath = file.getPath();
            Tinify.fromFile(filePath).toFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
