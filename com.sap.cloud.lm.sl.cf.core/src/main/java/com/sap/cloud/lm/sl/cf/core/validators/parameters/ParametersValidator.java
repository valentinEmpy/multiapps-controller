package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.List;
import java.util.Map;

import com.sap.cloud.lm.sl.mta.util.ValidatorUtil;

public abstract class ParametersValidator<T> {

    protected final ParametersValidatorHelper helper;
    protected final String prefix;
    protected final Class<?> containerClass;

    protected ParametersValidator(String prefix, String objectName, ParametersValidatorHelper helper, Class<?> containerClass) {
        this.containerClass = containerClass;
        this.helper = helper;
        this.prefix = ValidatorUtil.getPrefixedName(prefix, objectName);
    }

    protected ParametersValidator(String prefix, String objectName, List<ParameterValidator> parameterValidators, Class<?> containerClass,
                                  boolean doNotCorrect) {
        this(prefix, objectName, new ParametersValidatorHelper(parameterValidators, doNotCorrect), containerClass);
    }

    public abstract T validate();

    protected Map<String, Object> validateParameters(Map<String, Object> parameters) {
        return helper.validate(prefix, containerClass, parameters);
    }

}
