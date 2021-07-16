package com.kamtar.transport.api.enums;

import java.util.HashMap;
import java.util.Map;

public class OperationTypeTVAValeur {

	public static Map<OperationTypeTVA, Boolean> map = createMap();

	private static Map<OperationTypeTVA, Boolean> createMap() {
		Map<OperationTypeTVA,Boolean> map = new HashMap<OperationTypeTVA,Boolean>();

		map.put(OperationTypeTVA.LIVRAISON, true);
		map.put(OperationTypeTVA.TRANSPORT_DE_MARCHANDISE, false);
		map.put(OperationTypeTVA.LOCATION, true);
		map.put(OperationTypeTVA.DÉMÉNAGEMENT, true);
		map.put(OperationTypeTVA.MANUTENTION, false);

		map.put(OperationTypeTVA.IMMOBILISATION, false);
		map.put(OperationTypeTVA.FRAIS_DE_CARBURANT, false);
		map.put(OperationTypeTVA.TRANSFERT_DE_DOSSIER, false);


		return map;
	}

}
