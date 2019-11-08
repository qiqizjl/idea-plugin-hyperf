package com.naixiaoxin.idea.hyperf.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;

import java.util.ArrayList;
import java.util.Collection;

public class PhpClassUtil {


    /**
     * 根据Namespace获取类 = PhpIndex.getAllClassByFqn()
     *
     * @param phpIndex
     * @param namespace
     * @return
     */
    public static Collection<PhpClass> getClassByNamespace(PhpIndex phpIndex, String namespace) {
        Collection<PhpClass> phpClass = new ArrayList<>();
        // 先循环Class
        PsiElement[] test = null;
        for (PhpNamespace phpNamespace : phpIndex.getNamespacesByName(namespace.toLowerCase())) {
            phpClass.addAll(PsiTreeUtil.getChildrenOfTypeAsList(phpNamespace.getStatements(), PhpClass.class));
        }
        for (String ns : phpIndex.getChildNamespacesByParentName(namespace + "\\")) {
            phpClass.addAll(getClassByNamespace(phpIndex, namespace + "\\" + ns));
        }
        return phpClass;
    }
}
