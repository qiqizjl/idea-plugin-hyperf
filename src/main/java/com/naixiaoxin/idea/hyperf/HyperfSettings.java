package com.naixiaoxin.idea.hyperf;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
@State(
        name = "HyperfPluginSettings",
        storages = {
                @Storage("hyperf-plugin.xml")
        }
)
public class HyperfSettings implements PersistentStateComponent<HyperfSettings> {

    public boolean pluginEnabled = false;


    public boolean dismissEnableNotification = false;

    public String translationLang = "zh_CN";
    public String translationPath = "/storage/languages";


    public static HyperfSettings getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, HyperfSettings.class);
    }


    @Nullable
    @Override
    public HyperfSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HyperfSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

}