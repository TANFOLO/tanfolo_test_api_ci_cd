package com.kamtar.transport.api.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = NumeroDeTelephoneValidValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumeroDeTelephoneValidConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};
    
    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};


}