package com.kamtar.transport.api.validation;

import java.lang.annotation.Documented;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = FloatValidator.class)
@Documented
public @interface FloatPattern {

    String message() default "Float contraint voilated";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}