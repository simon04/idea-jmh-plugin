package ru.artyushov.jmhPlugin.configuration.producer;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.PathUtil;
import ru.artyushov.jmhPlugin.configuration.ConfigurationUtils;
import ru.artyushov.jmhPlugin.configuration.JmhConfiguration;

import static ru.artyushov.jmhPlugin.configuration.JmhConfiguration.Type.METHOD;

/**
 * User: nikart
 * Date: 14/07/14
 * Time: 23:06
 */
public class JmhMethodConfigurationProducer extends JmhConfigurationProducer {

    @Override
    protected boolean setupConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiMethod method = ConfigurationUtils.getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        sourceElement.set(method);
        setupConfigurationModule(context, configuration);
        final Module originalModule = configuration.getConfigurationModule().getModule();
        configuration.restoreOriginalModule(originalModule);

        PsiClass benchmarkClass = method.getContainingClass();
        if (benchmarkClass == null) {
            return false;
        }
        configuration.setBenchmarkClass(benchmarkClass.getQualifiedName());
        configuration.setType(METHOD);
        configuration.setProgramParameters(
                createProgramParameters(toRunParams(benchmarkClass, method), configuration.getProgramParameters()));
        configuration.setName(getNameForConfiguration(benchmarkClass, method));
        configuration.setWorkingDirectory(PathUtil.getLocalPath(context.getProject().getBaseDir()));
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(JmhConfiguration configuration, ConfigurationContext context) {
        if (configuration.getBenchmarkType() != METHOD) {
            return false;
        }
        PsiMethod method = ConfigurationUtils.getAnnotatedMethod(context);
        if (method == null) {
            return false;
        }
        PsiClass benchmarkClass = method.getContainingClass();
        if (benchmarkClass == null) {
            return false;
        }
        if (benchmarkClass.getQualifiedName() == null
                || !benchmarkClass.getQualifiedName().equals(configuration.getBenchmarkClass())) {
            return false;
        }
        String configurationName = getNameForConfiguration(benchmarkClass, method);
        return isConfigurationFromContext(configuration, context, configurationName);
    }

    private String getNameForConfiguration(PsiClass benchmarkClass, PsiMethod method) {
        return benchmarkClass.getName() + '.' + method.getName();
    }

    private String toRunParams(PsiClass benchmarkClass, PsiMethod method) {
        return benchmarkClass.getQualifiedName() + '.' + method.getName();
    }
}
