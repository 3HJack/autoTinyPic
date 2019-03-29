package com.hhh.tinypic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.tinify.Tinify;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class TinyPicManager {
    public static final int MAX_COMPRESS_COUNT_PER_MONTH = 500;
    public static final long MAX_SIZE_FOR_WARNING = 1024 * 10L;

    public static final String KEY_DIALOG_TITLE = "autoTinyPic";
    public static final String KEY_API = "tiny_pic_api";

    private static TinyPicManager ourInstance = new TinyPicManager();
    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();

    private Project mProject;
    private VirtualFileListener mFileListener;

    private TinyPicManager() {
    }

    public static TinyPicManager getInstance() {
        return ourInstance;
    }

    public void initTinyPic(Project project) {
        mProject = project;
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
        if (Tinify.compressionCount() > MAX_COMPRESS_COUNT_PER_MONTH) {
            Messages.showMessageDialog(project, "your compression limit is reached!!!", KEY_DIALOG_TITLE, Messages.getWarningIcon());
            return;
        }
        mFileListener = new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                mExecutorService.submit(() -> compressPicFile(event.getFile()));
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

    public void compressPicFile(VirtualFile file) {
        String fileName = file.getName();
        if (file.isDirectory() || !(fileName.endsWith(".png") || fileName.endsWith(".jpg"))) {
            return;
        }
        try {
            String filePath = file.getPath();
            Tinify.fromFile(filePath).toFile(filePath);
            if (FileUtils.sizeOf(new File(filePath)) > MAX_SIZE_FOR_WARNING) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    int result = Messages.showYesNoDialog(mProject, filePath + " is greater than 10KB !!! Are you sure to keep it ?", KEY_DIALOG_TITLE, Messages.getQuestionIcon());
                    if (result == Messages.NO) {
                        try {
                            file.delete(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
