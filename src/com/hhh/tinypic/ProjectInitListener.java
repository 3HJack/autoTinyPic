package com.hhh.tinypic;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

public class ProjectInitListener implements ProjectComponent, DumbAware {

    private final Project mProject;

    ProjectInitListener(Project project) {
        mProject = project;
    }

    @Override
    public void projectOpened() {
        TinyPicManager.getInstance().initTinyPic(mProject);
    }

    @Override
    public void projectClosed() {
        TinyPicManager.getInstance().unInitTinyPic();
    }
}
