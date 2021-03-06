package com.naixiaoxin.idea.hyperf.ui;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.naixiaoxin.idea.hyperf.HyperfSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class HyperfProjectSettingsForm implements Configurable {

    private Project project;

    public HyperfProjectSettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    private JCheckBox enabled;
    private JPanel panel1;

    private JTextField textTranslationLang;
    private JTextField textTranslationPath;

    @Nls
    @Override
    public String getDisplayName() {
        return "Hyperf Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return panel1;
    }

    @Override
    public boolean isModified() {
        return !enabled.isSelected() == getSettings().pluginEnabled
                || !textTranslationLang.getText().equals(getSettings().translationLang)
                || !textTranslationPath.getText().equals(getSettings().translationPath)
                ;

    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = enabled.isSelected();
        getSettings().translationLang = textTranslationLang.getText();
        getSettings().translationPath = textTranslationPath.getText();

    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private void updateUIFromSettings() {
        enabled.setSelected(getSettings().pluginEnabled);
        textTranslationLang.setText(getSettings().translationLang);
        textTranslationPath.setText(getSettings().translationPath);
    }

    @Override
    public void disposeUIResources() {
    }

    private HyperfSettings getSettings() {
        return HyperfSettings.getInstance(this.project);
    }

    public static void show(@NotNull Project project) {
        ShowSettingsUtilImpl.showSettingsDialog(project, "Hyperf.SettingsForm", null);
    }
}