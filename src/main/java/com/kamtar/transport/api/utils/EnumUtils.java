package com.kamtar.transport.api.utils;

import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.enums.ReclamationStatut;

public class EnumUtils {

	public static boolean operationStatutContains(String test) {

	    for (OperationStatut c : OperationStatut.values()) {
	        if (c.name().equals(test)) {
	            return true;
	        }
	    }

	    return false;
	}

	public static boolean reclamationStatutContains(String test) {

		for (ReclamationStatut c : ReclamationStatut.values()) {
			if (c.name().equals(test)) {
				return true;
			}
		}

		return false;
	}
	
}
