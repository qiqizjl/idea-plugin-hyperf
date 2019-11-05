package com.naixiaoxin.idea.hyperf.util;

import com.intellij.openapi.project.Project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.naixiaoxin.idea.hyperf.HyperfSettings;
import com.naixiaoxin.idea.hyperf.ui.HyperfProjectSettingsForm;
import com.sun.istack.NotNull;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class IdeHelper {
    public static void notifyEnableMessage(final Project project) {
        Notification notification = new Notification("Hyperf Plugin", "Hyperf Plugin", "Enable the Hyperf Plugin <a href=\"enable\">with auto configuration now</a>, open <a href=\"config\">Project Settings</a> or <a href=\"dismiss\">dismiss</a> further messages", NotificationType.INFORMATION, (notification1, event) -> {
            // handle html click events
            if("config".equals(event.getDescription())) {

                // open settings dialog and show panel
                HyperfProjectSettingsForm.show(project);
            } else if("enable".equals(event.getDescription())) {
                enablePluginAndConfigure(project);
                Notifications.Bus.notify(new Notification("Hyperf Plugin", "Hyperf Plugin", "Plugin enabled", NotificationType.INFORMATION), project);
            } else if("dismiss".equals(event.getDescription())) {
                // user dont want to show notification again
                HyperfSettings.getInstance(project).dismissEnableNotification = true;
            }

            notification1.expire();
        });

        Notifications.Bus.notify(notification, project);
    }

    private static void enablePluginAndConfigure(@NotNull Project project) {
        HyperfSettings.getInstance(project).pluginEnabled = true;
    }
}
