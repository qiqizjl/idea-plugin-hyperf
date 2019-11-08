package com.naixiaoxin.idea.hyperf.config;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.naixiaoxin.idea.hyperf.HyperfIcons;
import com.naixiaoxin.idea.hyperf.HyperfProjectComponent;
import com.naixiaoxin.idea.hyperf.stub.ConfigKeyStubIndex;
import com.naixiaoxin.idea.hyperf.stub.processor.CollectProjectUniqueKeys;
import com.naixiaoxin.idea.hyperf.util.ArrayReturnPsiRecursiveVisitor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConfigReferences implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Hyperf\\Contract\\ConfigInterface", "get"),
            new MethodMatcher.CallToSignature("\\Hyperf\\Contract\\ConfigInterface", "has"),
    };

    @Override
    public boolean support(@NotNull Language language) {
        // 只有PHP才执行
        return PhpLanguage.INSTANCE == language;
    }

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if (!HyperfProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            PsiElement parent = psiElement.getParent();
            if (parent != null && MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG) != null) {
                return new ConfigKeyProvider(parent);
            }

            return null;
        });
    }

    private static class ConfigKeyProvider extends GotoCompletionProvider {

        public ConfigKeyProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<>();
            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), ConfigKeyStubIndex.KEY);
            FileBasedIndex.getInstance().processAllKeys(ConfigKeyStubIndex.KEY, ymlProjectProcessor, getProject());
            for (String key : ymlProjectProcessor.getResult()) {
                lookupElements.add(LookupElementBuilder.create(key).withIcon(HyperfIcons.CONFIG));
            }


            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final Set<PsiElement> targets = new HashSet<>();
            final String contents = element.getContents();
            if (StringUtils.isBlank(contents)) {
                return targets;
            }

            FileBasedIndex.getInstance().getFilesWithKey(ConfigKeyStubIndex.KEY, Collections.singleton(contents), virtualFile -> {
                PsiFile psiFileTarget = PsiManager.getInstance(getProject()).findFile(virtualFile);
                if (psiFileTarget == null) {
                    return true;
                }

                psiFileTarget.acceptChildren(new ArrayReturnPsiRecursiveVisitor(ConfigFileUtil.matchConfigFile(getProject(), virtualFile).getKeyPrefix(), (key, psiKey, isRootElement) -> {
                    if (!isRootElement && key.equals(contents)) {
                        targets.add(psiKey);
                    }
                }));

                return true;
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }
    }
}
