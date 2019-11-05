package com.naixiaoxin.idea.hyperf.controller;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
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
        PrefixMatcher prefix = new PrefixMatcher("\\App\\Controller") {
            @Override
            public boolean prefixMatches(@NotNull String name) {
                return name.startsWith(this.myPrefix);
            }

            @NotNull
            @Override
            public PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
                return null;
            }
        };
        Collection<PhpClass> allControllerClass = new HashSet<PhpClass>() {{
        }};
        Collection<String> allController = PhpIndex.getInstance(project).getAllClassFqns(prefix);
        for (String controllerName : allController) {
            allControllerClass.addAll(PhpIndex.getInstance(project).getClassesByFQN(controllerName));
        }
        for (PhpClass phpClass : allControllerClass) {
            if (!phpClass.isAbstract()) {
                for (Method method : phpClass.getMethods()) {
                    String className = phpClass.getPresentableFQN();
                    String methodName = method.getName();
                    if (!method.isStatic() && method.getAccess().isPublic() && !methodName.startsWith("__")) {
                        PhpClass phpTrait = method.getContainingClass();
                        if (phpTrait == null) {
                            if (StringUtils.isNotBlank(className)) {
                                visitor.visit(phpClass, method);
                            }
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
