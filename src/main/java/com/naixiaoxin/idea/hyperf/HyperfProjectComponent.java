package com.naixiaoxin.idea.hyperf;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiElement;
import com.naixiaoxin.idea.hyperf.util.IdeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Objects;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class HyperfProjectComponent implements ProjectComponent {


    private Project project;

    public HyperfProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        notifyPluginEnableDialog();
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }


    @NotNull
    @Override
    public String getComponentName() {
        return "HyperfProjectComponent";
    }

    public static boolean isEnabled(Project project) {
        return HyperfSettings.getInstance(project).pluginEnabled;
    }

    public static boolean isEnabled(@Nullable PsiElement psiElement) {
        return psiElement != null && isEnabled(psiElement.getProject());
    }

    public static boolean isEnabledForIndex(@Nullable Project project) {

        if (project == null) {
            return false;
        }

        if (isEnabled((PsiElement) project)) {
            return true;
        }

        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(project.getBasePath()));
        return VfsUtil.findRelativeFile(baseDir, "vendor", "hyperf") != null;
    }

    private void notifyPluginEnableDialog() {
        // Enable Project dialog
        if (!isEnabled(this.project) && !HyperfSettings.getInstance(this.project).dismissEnableNotification) {
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(this.project.getBasePath()));
            if (VfsUtil.findRelativeFile(baseDir, "app") != null
                    && VfsUtil.findRelativeFile(baseDir, "vendor", "hyperf") != null
            ) {
                IdeHelper.notifyEnableMessage(project);
            }
        }
    }
}
