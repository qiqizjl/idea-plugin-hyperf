package fr.adrienbrault.idea.symfony2plugin.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.naixiaoxin.idea.hyperf.HyperfProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.utils.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.dic.MethodReferenceBag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MethodMatcher {

    @Nullable
    public static MethodMatcher.MethodMatchParameter getMatchedSignatureWithDepth(PsiElement psiElement, MethodMatcher.CallToSignature[] callToSignatures) {
        return getMatchedSignatureWithDepth(psiElement, callToSignatures, 0);
    }

    @Nullable
    public static MethodMatcher.MethodMatchParameter getMatchedSignatureWithDepth(PsiElement psiElement, MethodMatcher.CallToSignature[] callToSignatures, int defaultParameterIndex) {

        if (!HyperfProjectComponent.isEnabled(psiElement)) {
            return null;
        }

        MethodMatcher.MethodMatchParameter methodMatchParameter = new MethodMatcher.StringParameterMatcher(psiElement, defaultParameterIndex)
            .withSignature(callToSignatures)
            .match();

        if(methodMatchParameter != null) {
            return methodMatchParameter;
        }

        // try on resolved method
        return new MethodMatcher.StringParameterRecursiveMatcher(psiElement, defaultParameterIndex)
            .withSignature(callToSignatures)
            .match();
    }

    public static class CallToSignature {

        private final String instance;
        private final String method;

        public CallToSignature(String instance, String method) {
            this.instance = instance;
            this.method = method;
        }

        public String getInstance() {
            return instance;
        }

        public String getMethod() {
            return method;
        }
    }

    public static class MethodMatchParameter {

        final private CallToSignature signature;
        final private ParameterBag parameterBag;
        final private PsiElement[] parameters;
        final private MethodReference methodReference;

        public MethodMatchParameter(CallToSignature signature, ParameterBag parameterBag, PsiElement[] parameters, MethodReference methodReference) {
            this.signature = signature;
            this.parameterBag = parameterBag;
            this.parameters = parameters;
            this.methodReference = methodReference;
        }

        @Nullable
        public CallToSignature getSignature() {
            return signature;
        }

        public ParameterBag getParameterBag() {
            return this.parameterBag;
        }

        public PsiElement[] getParameters() {
            return parameters;
        }

        public MethodReference getMethodReference() {
            return methodReference;
        }

    }

    public static class StringParameterMatcher extends AbstractMethodParameterMatcher {

        public StringParameterMatcher(PsiElement psiElement, int parameterIndex) {
            super(psiElement, parameterIndex);
        }

        @Nullable
        public MethodMatchParameter match() {

            if (!HyperfProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            MethodReferenceBag bag = PhpElementsUtil.getMethodParameterReferenceBag(psiElement, this.parameterIndex);
            if(bag == null) {
                return null;
            }

            CallToSignature matchedMethodSignature = this.isCallTo(bag.getMethodReference());
            if(matchedMethodSignature == null) {
                return null;
            }

            return new MethodMatchParameter(matchedMethodSignature, bag.getParameterBag(), bag.getParameterList().getParameters(), bag.getMethodReference());
        }

    }

    public static class StringParameterRecursiveMatcher extends AbstractMethodParameterMatcher {

        public StringParameterRecursiveMatcher(PsiElement psiElement, int parameterIndex) {
            super(psiElement, parameterIndex);
        }

        @Nullable
        public MethodMatchParameter match() {

            if (!HyperfProjectComponent.isEnabled(psiElement)) {
                return null;
            }

            MethodReferenceBag bag = PhpElementsUtil.getMethodParameterReferenceBag(psiElement);
            if(bag == null) {
                return null;
            }

            // try on current method
            MethodMatcher.MethodMatchParameter methodMatchParameter = new StringParameterMatcher(psiElement, parameterIndex)
                .withSignature(this.signatures)
                .match();

            if(methodMatchParameter != null) {
                return methodMatchParameter;
            }

            // walk down next method
            MethodReference methodReference = bag.getMethodReference();
            Method method = Symfony2InterfacesUtil.getMultiResolvedMethod(methodReference);
            if(method == null) {
                return null;
            }

            PsiElement[] parameterReferences = PhpElementsUtil.getMethodParameterReferences(method, bag.getParameterBag().getIndex());
            if(parameterReferences == null || parameterReferences.length == 0) {
                return null;
            }

            for(PsiElement var: parameterReferences) {

                MethodMatcher.MethodMatchParameter methodMatchParameterRef = new MethodMatcher.StringParameterMatcher(var, parameterIndex)
                    .withSignature(this.signatures)
                    .match();

                if(methodMatchParameterRef != null) {
                    return methodMatchParameterRef;
                }

            }

            return null;

        }

    }

    public interface MethodParameterMatcherInterface {
        @Nullable
        public MethodMatchParameter match();
    }

    public abstract static class AbstractMethodParameterMatcher implements MethodParameterMatcherInterface {

        final protected List<CallToSignature> signatures;
        final protected int parameterIndex;
        final protected PsiElement psiElement;

        public AbstractMethodParameterMatcher(PsiElement psiElement, int parameterIndex) {
            this.signatures = new ArrayList<CallToSignature>();
            this.parameterIndex = parameterIndex;
            this.psiElement = psiElement;
        }

        public AbstractMethodParameterMatcher withSignature(String instance, String method) {
            this.signatures.add(new CallToSignature(instance, method));
            return this;
        }

        public AbstractMethodParameterMatcher withSignature(Collection<CallToSignature> signatures) {
            this.signatures.addAll(signatures);
            return this;
        }

        public AbstractMethodParameterMatcher withSignature(CallToSignature[] callToSignatures) {
            this.signatures.addAll(Arrays.asList(callToSignatures));
            return this;
        }

        @Nullable
        protected CallToSignature isCallTo(MethodReference methodReference) {
            Symfony2InterfacesUtil interfacesUtil = new Symfony2InterfacesUtil();

            for(CallToSignature signature: this.signatures) {
                if(interfacesUtil.isCallTo(methodReference, signature.getInstance(), signature.getMethod())) {
                    return signature;
                }
            }

            return null;
        }
    }
}
