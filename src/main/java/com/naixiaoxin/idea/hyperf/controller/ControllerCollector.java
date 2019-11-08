package com.naixiaoxin.idea.hyperf.controller;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.naixiaoxin.idea.hyperf.util.PhpClassUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class ControllerCollector {


    public static void visitControllerActions(@NotNull final Project project, @NotNull ControllerActionVisitor visitor) {
        // 只查询App\Controller中的路由
        Collection<PhpClass> allControllerClass = PhpClassUtil.getClassByNamespace(PhpIndex.getInstance(project), "\\App\\Controller");
        for (PhpClass phpClass : allControllerClass) {
            if (!phpClass.isAbstract()) {
                for (Method method : phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    String methodName = method.getName();
                    if (!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                        if (StringUtils.isNotBlank(className)) {
                            visitor.visit(phpClass, method);
                        }
                    }
                }
            }
        }
    }


    public interface ControllerVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull String name);
    }

    public interface ControllerActionVisitor {
        void visit(@NotNull PhpClass phpClass, @NotNull Method method);
    }
}
