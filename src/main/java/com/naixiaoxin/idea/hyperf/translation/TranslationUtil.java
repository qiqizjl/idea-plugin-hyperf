package com.naixiaoxin.idea.hyperf.translation;

import com.intellij.openapi.project.Project;
import com.naixiaoxin.idea.hyperf.HyperfSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TranslationUtil {

    private static final Pattern[] PATTERNS = new Pattern[]{
            Pattern.compile(".*/lang/(\\w{2}|\\w{2}[_|-]\\w{2})/(.*)\\.php$"),
            Pattern.compile(".*/lang/packages/(\\w{2}|\\w{2}[_|-]\\w{2})/\\w+/(.*)\\.php$")
    };

    /**
     * app/lang/fr_FR/messages.php
     * app/lang/fr/messages.php
     * app/lang/packages/en/hearthfire/messages.php
     * app/lang/packages/fr_FR/hearthfire/messages.php
     * app/lang/packages/fr_FR/admin/hearthfire/messages.php
     *
     * @return "hearthfire/messages"
     */
    @Nullable
    public static String getNamespaceFromFilePath(@NotNull String path, Project project) {
        Pattern pattern = Pattern.compile(".*" + HyperfSettings.getInstance(project).translationPath + "/(\\w{2}|\\w{2}[_|-]\\w{2})/(.*)\\.php$");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            return null;
        }

        String namespace = matcher.group(2);

        // invalid nested translation secure check
        // eg project name conflicts with pattern
        if (namespace.split("/").length > 3) {
            return null;
        }

        return namespace;

    }
}
