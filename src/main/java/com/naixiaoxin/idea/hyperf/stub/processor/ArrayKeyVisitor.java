package com.naixiaoxin.idea.hyperf.stub.processor;

import com.intellij.psi.PsiElement;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public interface ArrayKeyVisitor {
    void visit(String key, PsiElement psiKey, boolean isRootElement);
}
