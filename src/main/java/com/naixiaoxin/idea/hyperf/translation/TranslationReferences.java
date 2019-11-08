package com.naixiaoxin.idea.hyperf.translation;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
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
import com.naixiaoxin.idea.hyperf.HyperfSettings;
import com.naixiaoxin.idea.hyperf.stub.TranslationKeyStubIndex;
import com.naixiaoxin.idea.hyperf.stub.processor.CollectProjectUniqueKeys;
import com.naixiaoxin.idea.hyperf.util.ArrayReturnPsiRecursiveVisitor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionProvider;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TranslationReferences implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] TRANSLATION_KEY = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Hyperf\\Contract\\TranslatorInterface", "trans"),
    };

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), psiElement -> {
            if (!HyperfProjectComponent.isEnabled(psiElement)) {
                return null;
            }
            // only install hyperf/translation
            VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(psiElement.getProject().getBasePath()));
            if (VfsUtil.findRelativeFile(baseDir, "vendor", "hyperf", "translation") == null
            ) {
                return null;
            }
            PsiElement parent = psiElement.getParent();
            if (parent != null && (
                    MethodMatcher.getMatchedSignatureWithDepth(parent, TRANSLATION_KEY) != null || PhpElementsUtil.isFunctionReference(parent, 0, "trans", "__")
            )) {
                return new TranslationKey(parent);
            }
            return null;
        });
    }

    public static class TranslationKey extends GotoCompletionProvider {

        public TranslationKey(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<>();

            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), TranslationKeyStubIndex.KEY);
            FileBasedIndex.getInstance().processAllKeys(TranslationKeyStubIndex.KEY, ymlProjectProcessor, getProject());
            for (String key : ymlProjectProcessor.getResult()) {
                lookupElements.add(LookupElementBuilder.create(key).withIcon(HyperfIcons.TRANSLATION));
            }

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
            final String contents = element.getContents();
            if (StringUtils.isBlank(contents)) {
                return Collections.emptyList();
            }

            final String priorityTemplate = "/" + HyperfSettings.getInstance(element.getProject()).translationLang + "/";

            final Set<PsiElement> priorityTargets = new LinkedHashSet<>();
            final Set<PsiElement> targets = new LinkedHashSet<>();

            FileBasedIndex.getInstance().getFilesWithKey(TranslationKeyStubIndex.KEY, Collections.singleton(contents), virtualFile -> {
                PsiFile psiFileTarget = PsiManager.getInstance(getProject()).findFile(virtualFile);
                if (psiFileTarget == null) {
                    return true;
                }

                String namespace = TranslationUtil.getNamespaceFromFilePath(virtualFile.getPath(),getProject());
                if (namespace == null) {
                    return true;
                }

                psiFileTarget.acceptChildren(new ArrayReturnPsiRecursiveVisitor(namespace, (key, psiKey, isRootElement) -> {
                    if (!isRootElement && key.equalsIgnoreCase(contents)) {
                        if (virtualFile.getPath().contains(priorityTemplate)) {
                            priorityTargets.add(psiKey);
                        } else {
                            targets.add(psiKey);
                        }
                    }
                }));

                return true;
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            priorityTargets.addAll(targets);
            return priorityTargets;

        }
    }
}
