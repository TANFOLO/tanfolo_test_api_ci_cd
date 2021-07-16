package com.kamtar.transport.api.security;

import java.util.HashMap;
import java.util.Map;

import com.kamtar.transport.api.enums.OperateurListeDeDroits;

public class ListeDroitOperateursKamtar {

	public static Map<OperateurListeDeDroits, Integer> map = createMap();

	private static Map<OperateurListeDeDroits, Integer> createMap() {
		Map<OperateurListeDeDroits,Integer> map = new HashMap<OperateurListeDeDroits,Integer>();

		map.put(OperateurListeDeDroits.AFFICHAGE_CLIENTS, 0);
		map.put(OperateurListeDeDroits.GESTION_CLIENTS, 1);

		map.put(OperateurListeDeDroits.AFFICHAGE_TRANSPORTEURS, 2);
		map.put(OperateurListeDeDroits.GESTION_TRANSPORTEURS, 3);

		map.put(OperateurListeDeDroits.GESTION_OPERATIONS, 5);
		map.put(OperateurListeDeDroits.AFFICHAGE_OPERATIONS, 4);

		//map.put(OperateurListeDeDroits.GESTION_FACTURES_ENTRANTES, 6);
		//map.put(OperateurListeDeDroits.GESTION_FACTURES_SORTANTES, 7);

		map.put(OperateurListeDeDroits.AFFICHAGE_SMS, 8);
		map.put(OperateurListeDeDroits.AFFICHAGE_EMAILS, 9);
		map.put(OperateurListeDeDroits.AFFICHAGE_NOTIFICATIONS, 10);

		map.put(OperateurListeDeDroits.GESTION_VEHICULE, 11);
		map.put(OperateurListeDeDroits.AFFICHAGE_VEHICULE, 12);

		map.put(OperateurListeDeDroits.GESTION_PROPRIETAIRE, 13);
		map.put(OperateurListeDeDroits.AFFICHAGE_PROPRIETAIRE, 14);

		map.put(OperateurListeDeDroits.CONSULTER_FACTURES_CLIENT, 15);
		map.put(OperateurListeDeDroits.DECLENCHER_FACTURATION_CLIENT, 16);
		map.put(OperateurListeDeDroits.DECLENCHER_FACTURATION_PROPRIETAIRE, 18);

		map.put(OperateurListeDeDroits.SUPPRESSION_PROPRIETAIRE, 17);
		map.put(OperateurListeDeDroits.SUPPRESSION_CLIENT, 19);
		map.put(OperateurListeDeDroits.SUPPRESSION_DRIVER, 20);
		map.put(OperateurListeDeDroits.SUPPRESSION_OPERATION, 21);
		map.put(OperateurListeDeDroits.SUPPRESSION_VEHICULE, 22);

		map.put(OperateurListeDeDroits.SUPPRESSION_FACTURATION_CLIENT, 23);
		map.put(OperateurListeDeDroits.SUPPRESSION_FACTURATION_PROPRIETAIRE, 24);
		map.put(OperateurListeDeDroits.CONSULTER_FACTURES_PROPRIETAIRE, 25);

		map.put(OperateurListeDeDroits.CONSULTER_STATISTIQUES, 26);

		map.put(OperateurListeDeDroits.GESTION_DEVIS, 27);
		map.put(OperateurListeDeDroits.AFFICHAGE_DEVIS, 28);

		map.put(OperateurListeDeDroits.GESTION_RECLAMATIONS, 29);

		return map;
	}

}
