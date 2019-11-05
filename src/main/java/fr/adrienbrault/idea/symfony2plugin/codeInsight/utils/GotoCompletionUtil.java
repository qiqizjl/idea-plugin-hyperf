package fr.adrienbrault.idea.symfony2plugin.codeInsight.utils;

import com.intellij.psi.PsiElement;
import com.naixiaoxin.idea.hyperf.config.ConfigReferences;
import com.naixiaoxin.idea.hyperf.controller.ControllerReferences;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionContributor;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionLanguageRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrar;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.GotoCompletionRegistrarParameter;

import java.util.ArrayList;
import java.util.Collection;

public class GotoCompletionUtil {

    private static GotoCompletionRegistrar[] CONTRIBUTORS = new GotoCompletionRegistrar[]{
            new ControllerReferences(),
            new ConfigReferences(),

    };

    public static Collection<GotoCompletionContributor> getContributors(final PsiElement psiElement) {
        Collection<GotoCompletionContributor> contributors = new ArrayList<>();

        GotoCompletionRegistrarParameter registrar = (pattern, contributor) -> {
            if (pattern.accepts(psiElement)) {
                contributors.add(contributor);
            }
        };

        for (GotoCompletionRegistrar register : CONTRIBUTORS) {
            // filter on language
            if (register instanceof GotoCompletionLanguageRegistrar) {
                if (((GotoCompletionLanguageRegistrar) register).support(psiElement.getLanguage())) {
                    register.register(registrar);
                }
            } else {
                register.register(registrar);
            }
        }

        return contributors;
    }
}
