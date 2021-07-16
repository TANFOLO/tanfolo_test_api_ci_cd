package com.kamtar.transport.api.utils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.kamtar.transport.api.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.SimpleDateFormat;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;    

@Component
public class JWTProvider {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(JWTProvider.class); 

	@Value("${jwt.secret}")
	private String jwt_secret;

	@Value("${jwt.ttl_ms}")
	private Long jwt_ttl;

	@Value("${jwt.subject}")
	private String jwt_subject;

	@Value("${jwt.issuer}")
	private String jwt_issuer;

	private static String CODE_PAYS = "code_pays";
	private static String TYPE_DE_COMPTE = "type_de_compte";
	private static String PLATEFORME = "plateforme";

	//Sample method to construct a JWT
	public String createJWT(Utilisateur user, List<String> additional_informations) {

		//The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		//We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwt_secret);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder()
				.setId(user.getUuid().toString())
				.setIssuedAt(now)
				.setSubject(jwt_subject)
				.setIssuer(jwt_issuer)
				.claim("email", user.getEmail())
				.claim("nom", user.getNom())
				.claim("prenom", user.getPrenom())
				.claim("uuid", user.getUuid())
				.claim("locale", user.getLocale())
				.claim(CODE_PAYS, user.getCodePays())
				.claim("numero_de_telephone_1", user.getNumeroTelephone1())
				.claim(TYPE_DE_COMPTE, user.getTypeDeCompte())
				.claim(PLATEFORME, "KAMTAR_TRANSPORT");


		// ajoute la liste des droits dans le jeton si il s'agit d'un pérateur kamtar
		if(user instanceof UtilisateurOperateurKamtar) {
			UtilisateurOperateurKamtar operateur = (UtilisateurOperateurKamtar) user;
			builder.claim("liste_droits", operateur.getListe_droits());
			builder.claim("service", operateur.getService() == null ? "": operateur.getService());
		}

		// ajout du nom du client si c'est un expéditeur
		if(user instanceof UtilisateurClient) {
			builder.claim("uuid_client", additional_informations.get(1));
			builder.claim("type_compte", additional_informations.get(2));
			builder.claim("nom_entreprise", additional_informations.get(3));
		}

		// ajout du nom du client si c'est un client personnel
		if(user instanceof UtilisateurClientPersonnel) {
			builder.claim("uuid_client", additional_informations.get(1));
			builder.claim("type_compte", additional_informations.get(2));
			builder.claim("nom_entreprise", additional_informations.get(3));
			builder.claim("liste_droits", additional_informations.get(4));
		}

		// ajout de l'id du véhicule si il s'agit d'un driver ou d'un propriétaire
		if(user instanceof UtilisateurDriver) {
			if (additional_informations != null) {
				builder.claim("uuid_vehicule", additional_informations.get(0));
				builder.claim("immatriculation_vehicule", additional_informations.get(1));
				builder.claim("webview", additional_informations.get(2));
				builder.claim("informations_manquantes", additional_informations.get(3));
			}
		}
		if(user instanceof UtilisateurProprietaire) {
			builder.claim("uuid_vehicule", additional_informations.get(0));
			builder.claim("immatriculation_vehicule", additional_informations.get(1));
			builder.claim("webview", additional_informations.get(2));
		}

		builder = builder.signWith(signatureAlgorithm, signingKey);

		//if it has been specified, let's add the expiration
		long expMillis = nowMillis + jwt_ttl;
		Date exp = new Date(expMillis);
		builder.setExpiration(exp);

		//Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	/**
	 * Retourne le type de compte à partir du jeton
	 * @param jwt
	 * @return
	 */
	public String getClaimsValue(String keyclem, String token) {

		//This line will throw an exception if it is not a signed JWS (as expected)
		Claims claims = Jwts.parser()         
				.setSigningKey(DatatypeConverter.parseBase64Binary(jwt_secret))
				.parseClaimsJws(token).getBody();

		if (claims != null) {

			boolean trouve = false;
			Iterator<Entry<String, Object>> iter = claims.entrySet().iterator();
			while (!trouve && iter.hasNext()) {
				Entry<String, Object> next = iter.next();
				if (keyclem.equals(next.getKey())) {
					return next.getValue().toString();
				}
			}
		}

		return null;
	}

	//Sample method to validate and read the JWT
	public Map<String, String> getClaims(String jwt) {

		Map<String, String> ret = null;
		if (jwt == null || "".equals(jwt.trim())) {
			return ret;
		}

		//This line will throw an exception if it is not a signed JWS (as expected)
		try {
			Claims claims = Jwts.parser()
					.setSigningKey(DatatypeConverter.parseBase64Binary(jwt_secret))
					.parseClaimsJws(jwt).getBody();

			if (claims != null) {
				ret = new HashMap<String, String>();
			}

			ret.put("id", claims.getId());

			Iterator<Entry<String, Object>> iter = claims.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Object> next = iter.next();
				ret.put(next.getKey(), next.getValue().toString());
			}


		} catch (io.jsonwebtoken.MalformedJwtException e) {
			return null;
		}

		return ret;
	}

	/**
	 * Retourne le code du pays à partir du jeton
	 * @param jwt
	 * @return
	 */
	public String getCodePays(String jwt) {
		if (jwt != null && !"".equals(jwt)) {

			try {
				Claims claims = Jwts.parser()
						.setSigningKey(DatatypeConverter.parseBase64Binary(jwt_secret))
						.parseClaimsJws(jwt).getBody();

				if (claims != null) {

					boolean trouve = false;
					Iterator<Entry<String, Object>> iter = claims.entrySet().iterator();
					while (!trouve && iter.hasNext()) {
						Entry<String, Object> next = iter.next();
						if (CODE_PAYS.equals(next.getKey())) {
							return next.getValue().toString();
						}
					}

					if (!trouve) {
						return "CI";
					}
				}
			} catch (io.jsonwebtoken.MalformedJwtException e) {
				return null;
			}
		}
		return null;
	}


	/**
	 * Retourne le type de compte à partir du jeton
	 * @param jwt
	 * @return
	 */
	public String getTypeDeCompte(String jwt) {
		if (jwt == null || "".equals(jwt)) {
			return null;
		}
		Claims claims = Jwts.parser()         
				.setSigningKey(DatatypeConverter.parseBase64Binary(jwt_secret))
				.parseClaimsJws(jwt).getBody();

		if (claims != null) {

			boolean trouve = false;
			Iterator<Entry<String, Object>> iter = claims.entrySet().iterator();
			while (!trouve && iter.hasNext()) {
				Entry<String, Object> next = iter.next();
				if (TYPE_DE_COMPTE.equals(next.getKey())) {
					return next.getValue().toString();
				}
			}
		}

		return null;
	}

	//Sample method to validate and read the JWT
	public UUID getUUIDFromJWT(String jwt) {

		Map<String, String> ret = getClaims(jwt);
		if (ret != null && ret.containsKey("id") && isValidJWT(jwt)) {
			String id = ret.get("id");
			UUID uuid = java.util.UUID.fromString(id);
			return uuid;
		}

		return null;
	}

	public boolean isValidJWT(String jwt) {

		try {
			Claims claims = Jwts.parser()
					.setSigningKey(DatatypeConverter.parseBase64Binary(jwt_secret))
					.parseClaimsJws(jwt).getBody();

			if (claims != null) {

				if (claims.getExpiration().before(new Date())) {
					return false;
				}

				return true;

			}

		} catch (java.lang.IllegalArgumentException e ) {
			return false;
		}



		return false;
	}

}
