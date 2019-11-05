package com.naixiaoxin.idea.hyperf.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileUtil {
    private static final Pattern configFilePattern = Pattern.compile(".*/config/autoload/([\\w-/]+).php$");

    private static final Pattern configFile = Pattern.compile(".*/config/config.php$");


    public static ConfigFileMatchResult matchConfigFile(Project project, VirtualFile virtualFile) {
        String projectPath = project.getBaseDir().getPath();
        String path = StringUtil.trimStart(virtualFile.getPath(), projectPath);

        Matcher m = configFilePattern.matcher(path);

        // config/config.php
        // config/autoload/app.php
        if (m.matches()) {
            return new ConfigFileMatchResult(true, m.group(1).replace('/', '.'));
        }
        m = configFile.matcher(path);

        if (m.matches()) {
            return new ConfigFileMatchResult(true, "");
        }

        return ConfigFileMatchResult.NO_MATCH;
    }

    public static class ConfigFileMatchResult {
        static final ConfigFileMatchResult NO_MATCH = new ConfigFileMatchResult(false, "");

        private boolean matches;

        private String keyPrefix;

        ConfigFileMatchResult(boolean matches, @NotNull String keyPrefix) {
            this.matches = matches;
            this.keyPrefix = keyPrefix;
        }

        public boolean matches() {
            return matches;
        }

        @NotNull
        public String getKeyPrefix() {
            return keyPrefix;
        }
    }
}
