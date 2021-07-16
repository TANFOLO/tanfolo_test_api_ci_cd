package com.kamtar.transport.api.utils;

import com.kamtar.transport.api.repository.UtilisateurProprietaireRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kamtar.transport.api.controller.AuditController;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;

public class ParrainageUtils {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ParrainageUtils.class);  

	public static String generateCodeParrainnage(UtilisateurDriverRepository utilisateurDriverRepository, String code_pays) {

		// génère des chaines aléatoires jusqu'à trouver une chaine qui n'est pas encore en bdd
		boolean token_disponible = false;
		String token = "";
		int cpt = 0;
		while (!token_disponible) {
			token = RandomStringUtils.random(6, true, true).toUpperCase();
			if (!utilisateurDriverRepository.codeParrainageExisteDeja(token, code_pays)) {
				token_disponible = true;
			} else {
				cpt++;
			}
		}
		if (cpt > 3) {
			logger.error("attention, plus beaucoup de code de parraingaes disponibles");
		}
		return token;
	}


	public static String generateCodeParrainnage(UtilisateurProprietaireRepository utilisateurProprietaireRepository, String code_pays) {

		// génère des chaines aléatoires jusqu'à trouver une chaine qui n'est pas encore en bdd
		boolean token_disponible = false;
		String token = "";
		int cpt = 0;
		while (!token_disponible) {
			token = RandomStringUtils.random(6, true, true).toUpperCase();
			if (!utilisateurProprietaireRepository.codeParrainageExisteDeja(token, code_pays)) {
				token_disponible = true;
			} else {
				cpt++;
			}
		}
		if (cpt > 3) {
			logger.error("attention, plus beaucoup de code de parraingaes disponibles");
		}
		return token;
	}

}
