package com.kamtar.transport.api.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = CodeParrainageProprietaireValidValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeParrainageProprietaireValidConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};

    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};


}