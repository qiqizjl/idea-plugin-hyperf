package com.naixiaoxin.idea.hyperf.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.naixiaoxin.idea.hyperf.stub.processor.ArrayKeyVisitor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author NaiXiaoXin(SeanWang) <i@naixiaoxin.com>
 */
public class ArrayReturnPsiRecursiveVisitor extends PsiRecursiveElementWalkingVisitor {

    private final String fileNameWithoutExtension;
    private final ArrayKeyVisitor arrayKeyVisitor;

    public ArrayReturnPsiRecursiveVisitor(String fileNameWithoutExtension, ArrayKeyVisitor arrayKeyVisitor) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
        this.arrayKeyVisitor = arrayKeyVisitor;
    }

    @Override
    public void visitElement(PsiElement element) {

        if (element instanceof PhpReturn) {
            visitPhpReturn((PhpReturn) element);
        }

        super.visitElement(element);
    }

    public void visitPhpReturn(PhpReturn phpReturn) {
        PsiElement arrayCreation = phpReturn.getFirstPsiChild();
        if (arrayCreation instanceof ArrayCreationExpression) {
            collectConfigKeys((ArrayCreationExpression) arrayCreation, this.arrayKeyVisitor,fileNameWithoutExtension);

        }
    }


    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ArrayKeyVisitor arrayKeyVisitor, String configName) {
        // 兼容当config/config.php的情况 默认无前缀
        if (configName.equals("")){
            List<String> context = Collections.emptyList();
            collectConfigKeys(creationExpression, arrayKeyVisitor, context);
        }else{
            collectConfigKeys(creationExpression, arrayKeyVisitor, Collections.singletonList(configName));
        }
    }

    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ArrayKeyVisitor arrayKeyVisitor, List<String> context) {

        for (ArrayHashElement hashElement : PsiTreeUtil.getChildrenOfTypeAsList(creationExpression, ArrayHashElement.class)) {

            PsiElement arrayKey = hashElement.getKey();
            PsiElement arrayValue = hashElement.getValue();

            if (arrayKey instanceof StringLiteralExpression) {
                List<String> myContext = new ArrayList<>(context);
                myContext.add(((StringLiteralExpression) arrayKey).getContents());
                String keyName = StringUtils.join(myContext, ".");

                if (arrayValue instanceof ArrayCreationExpression) {
                    arrayKeyVisitor.visit(keyName, arrayKey, true);
                    collectConfigKeys((ArrayCreationExpression) arrayValue, arrayKeyVisitor, myContext);
                } else {
                    arrayKeyVisitor.visit(keyName, arrayKey, false);
                }

            }
        }

    }
}
