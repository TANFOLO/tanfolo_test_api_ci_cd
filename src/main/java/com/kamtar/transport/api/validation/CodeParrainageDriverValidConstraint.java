package com.kamtar.transport.api.validation;

import java.lang.annotation.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = CodeParrainageDriverValidValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeParrainageDriverValidConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};

    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};


}