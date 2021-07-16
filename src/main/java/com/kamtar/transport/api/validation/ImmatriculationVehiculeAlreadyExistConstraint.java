package com.kamtar.transport.api.validation;

import java.lang.annotation.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Constraint(validatedBy = ImmatriculationVehiculeAlreadyExistValidator.class)
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImmatriculationVehiculeAlreadyExistConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};
    
    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};


}