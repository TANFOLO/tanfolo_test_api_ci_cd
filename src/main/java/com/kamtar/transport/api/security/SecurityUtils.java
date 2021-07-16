package com.kamtar.transport.api.security;

import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kamtar.transport.api.enums.OperateurListeDeDroits;
import com.kamtar.transport.api.enums.UtilisateurTypeDeCompte;
import com.kamtar.transport.api.utils.JWTProvider;

public class SecurityUtils {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(SecurityUtils.class); 

	public static boolean admin(JWTProvider jwtProvider, String token) {

		// si c'est admin, on laisse passer
		if (UtilisateurTypeDeCompte.ADMIN_KAMTAR.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}

		return false;

	}
	
	public static boolean operateur(JWTProvider jwtProvider, String token) {

		// si c'est admin, on laisse passer
		if (UtilisateurTypeDeCompte.OPERATEUR_KAMTAR.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}

		return false;

	}

	public static boolean transporteur(JWTProvider jwtProvider, String token) {
		// si c'est un transporteur, on laisse passer
		if (UtilisateurTypeDeCompte.DRIVER.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}
		return false;

	}

	public static boolean proprietaire(JWTProvider jwtProvider, String token) {

		// si c'est un transporteur, on laisse passer
		if (UtilisateurTypeDeCompte.PROPRIETAIRE.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}

		return false;

	}
	
	public static boolean client(JWTProvider jwtProvider, String token) {

		// si c'est un transporteur, on laisse passer
		if (UtilisateurTypeDeCompte.EXPEDITEUR.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}

		return false;

	}

	public static boolean client_personnel(JWTProvider jwtProvider, String token) {

		// si c'est un transporteur, on laisse passer
		if (UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			return true;
		}

		return false;

	}

	public static boolean client_personnelWithDroit(JWTProvider jwtProvider, String token, ClientPersonnelListeDeDroits droit) {

		// si c'est un transporteur, on laisse passer
		if (UtilisateurTypeDeCompte.EXPEDITEUR_PERSONNEL.toString().equals(jwtProvider.getTypeDeCompte(token))) {
			String lists_droits = jwtProvider.getClaims(token).get("liste_droits");
			try {
				if (lists_droits.charAt(ListeDroitClientPersonnel.map.get(droit)) == '1') {
					return true;
				}
			} catch (StringIndexOutOfBoundsException e) {
				return false;
			}
		}

		return false;

	}

	public static boolean adminOrOperateurWithDroit(JWTProvider jwtProvider, String token, OperateurListeDeDroits droit) {

		String type_compte = jwtProvider.getTypeDeCompte(token);
		
		// si c'est admin, on laisse passer
		if (UtilisateurTypeDeCompte.ADMIN_KAMTAR.toString().equals(type_compte)) {
			return true;
		}

		// si c'est opérateur, on regarde qu'il ai bien le droit de gestion sur le droit demandé
		if (UtilisateurTypeDeCompte.OPERATEUR_KAMTAR.toString().equals(type_compte)) {
			String lists_droits = jwtProvider.getClaims(token).get("liste_droits");
			try {
				if (lists_droits.charAt(ListeDroitOperateursKamtar.map.get(droit)) == '1') {
					return true;
				}
			} catch (StringIndexOutOfBoundsException e) {
				return false;
			}

		}

		return false;

	}
	
	

}
