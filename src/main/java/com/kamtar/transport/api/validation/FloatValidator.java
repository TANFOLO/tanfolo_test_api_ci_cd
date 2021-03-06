package com.kamtar.transport.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FloatValidator implements ConstraintValidator<FloatPattern, Float> {

    @Override
    public void initialize(final FloatPattern constraintAnnotation) {

    }

    @Override
    public boolean isValid(final Float value,
            final ConstraintValidatorContext context) {
        return value > 11.11;
    }
}