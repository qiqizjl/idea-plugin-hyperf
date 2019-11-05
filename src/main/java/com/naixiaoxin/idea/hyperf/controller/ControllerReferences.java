package com.naixiaoxin.idea.hyperf.controller;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.PhpPresentationUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.naixiaoxin.idea.hyperf.HyperfIcons;
import com.naixiaoxin.idea.hyperf.HyperfProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class ControllerReferences implements GotoCompletionLanguageRegistrar {

    // 定义路由函数
    private static MethodMatcher.CallToSignature[] ROUTE = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Hyperf\\HttpServer\\Router\\Router", "get"),
            new MethodMatcher.CallToSignature("\\yperf\\HttpServer\\Router\\Router", "post"),
            new MethodMatcher.CallToSignature("\\yperf\\HttpServer\\Router\\Router", "put"),
            new MethodMatcher.CallToSignature("\\yperf\\HttpServer\\Router\\Router", "patch"),
            new MethodMatcher.CallToSignature("\\yperf\\HttpServer\\Router\\Router", "delete"),
            new MethodMatcher.CallToSignature("\\yperf\\HttpServer\\Router\\Router", "options"),
    };

    private static MethodMatcher.CallToSignature[] RouteAddRoute = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Hyperf\\HttpServer\\Router\\Router", "addRoute"),

    };

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new GotoCompletionContributor() {

            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {
                if (!HyperfProjectComponent.isEnabled(psiElement)) {
                    return null;
                }
                PsiElement parent = psiElement.getParent();
                if (!(parent instanceof StringLiteralExpression)) {
                    return null;
                }
                //Router::get('/hello-hyperf', 'App\Controller\IndexController::hello');
                //Router::get('/hello-hyperf', 'App\Controller\IndexController@hello');
                if (MethodMatcher.getMatchedSignatureWithDepth(parent, ROUTE, 1) != null) {
                    return createRouteCompletion(parent);
                }
                //Router::addRoute(['GET', 'POST', 'HEAD'], '/', 'App\Controller\IndexController@index');
                if (MethodMatcher.getMatchedSignatureWithDepth(parent, RouteAddRoute, 2) != null) {
                    return createRouteCompletion(parent);
                }
                return null;

            }
        });
    }

    private ControllerRoute createRouteCompletion(@NotNull PsiElement element) {
        return new ControllerRoute(element);
    }

    private static class ControllerRoute extends GotoCompletionProvider {


        ControllerRoute(PsiElement element) {
            super(element);

        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            // 自动补全
            final Collection<LookupElement> lookupElements = new ArrayList<>();
            ControllerCollector.visitControllerActions(getProject(), (phpClass, method) -> {
                        String controllerFunction = phpClass.getFQN() + "@" + method.getName();
                        if (StringUtils.startsWith(controllerFunction, "\\")) {
                            controllerFunction = StringUtils.stripStart(controllerFunction, "\\");
                        }
                        LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(controllerFunction)
                                .withIcon(HyperfIcons.ROUTE)
                                .withTypeText(phpClass.getPresentableFQN(), true);

                        Parameter[] parameters = method.getParameters();
                        if (parameters.length > 0) {
                            lookupElementBuilder = lookupElementBuilder.withTailText(PhpPresentationUtil.formatParameters(null, parameters).toString());
                        }

                        LookupElement lookupElement = lookupElementBuilder;

                        lookupElements.add(lookupElement);
                    }
            );

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(final StringLiteralExpression element) {

            final String content = element.getContents();
            if (StringUtils.isBlank(content)) {
                return Collections.emptyList();
            }

            String[] controllerSplit = null;
            final Collection<PsiElement> targets = new ArrayList<>();
            // 判断是否存在:: 或者是@
            if (content.contains("@")) {
                // 存在@
                controllerSplit = content.split("@");

            }
            if (content.contains("::")) {
                // 存在::
                controllerSplit = content.split("::");
            }

            // 如果存在controller
            if (controllerSplit == null || controllerSplit.length != 2) {
                return targets;
            }
            String controllerName = controllerSplit[0];
            // 补全Controller的类名
            if (!StringUtils.startsWith(controllerName, "\\")) {
                controllerName = "\\" + controllerName;
            }
            Collection<PhpClass> controllerClass = PhpIndex.getInstance(getProject()).getClassesByFQN(controllerName);
            for (PhpClass phpClass : controllerClass) {
                Method method = phpClass.findMethodByName(controllerSplit[1]);
                if (method == null) {
                    continue;
                }
                if (method.isStatic() || !method.getAccess().isPublic()) {
                    continue;
                }
                targets.add(method);
            }
            return targets;
        }
    }

}
