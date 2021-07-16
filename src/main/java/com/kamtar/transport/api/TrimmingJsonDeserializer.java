package com.kamtar.transport.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.jackson.JsonComponent;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;

import java.io.IOException;

/**
 * Supprime les espaces avant et après les chaines de caractères
 * @author Aurelien
 *
 */
@JsonComponent
class TrimmingJsonDeserializer extends JsonDeserializer<String> {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(TrimmingJsonDeserializer.class);  

	@Override
	public String deserialize(JsonParser parser, DeserializationContext context) {
		try {
			return parser.hasToken(VALUE_STRING) ? parser.getText().trim() : null;
		} catch (IOException e) {
			logger.error("deserialize IOException", e);
		}
		return null;
	}
}