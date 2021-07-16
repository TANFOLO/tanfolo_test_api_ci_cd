package com.kamtar.transport.api.validation;

import java.lang.annotation.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Documented
@Constraint(validatedBy = NumeroDeTelephoneAlreadyExistValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NumeroDeTelephoneAlreadyExistConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};
    
    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};


}