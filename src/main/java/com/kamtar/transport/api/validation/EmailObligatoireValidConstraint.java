package com.kamtar.transport.api.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailObligatoireValidValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailObligatoireValidConstraint {
	
	@JsonProperty("measurementMetaData")
    String message() default "Invalid phone number";
	
	@JsonIgnoreProperties
    Class<?>[] groups() default {};
    
    @JsonIgnoreProperties
    Class<? extends Payload>[] payload() default {};
}