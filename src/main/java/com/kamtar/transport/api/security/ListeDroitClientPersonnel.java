package com.kamtar.transport.api.security;

import com.kamtar.transport.api.enums.ClientPersonnelListeDeDroits;
import com.kamtar.transport.api.enums.OperateurListeDeDroits;

import java.util.HashMap;
import java.util.Map;

public class ListeDroitClientPersonnel {

	public static Map<ClientPersonnelListeDeDroits, Integer> map = createMap();

	private static Map<ClientPersonnelListeDeDroits, Integer> createMap() {
		Map<ClientPersonnelListeDeDroits,Integer> map = new HashMap<ClientPersonnelListeDeDroits,Integer>();

		map.put(ClientPersonnelListeDeDroits.COMMANDER, 0);
		map.put(ClientPersonnelListeDeDroits.VOIR_OPERATIONS, 1);
		map.put(ClientPersonnelListeDeDroits.VOIR_FACTURES, 2);
		map.put(ClientPersonnelListeDeDroits.VOIR_TOUTES_OPERATIONS, 3);
		map.put(ClientPersonnelListeDeDroits.GESTION_UTILISATEURS, 4);
		map.put(ClientPersonnelListeDeDroits.RECEPTION_NOTIFICATIONS, 5);

		return map;
	}

}
