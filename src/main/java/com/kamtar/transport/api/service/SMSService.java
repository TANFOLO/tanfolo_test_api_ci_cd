package com.kamtar.transport.api.service;



import java.util.Map;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.swagger.ListOperation;
import org.springframework.stereotype.Service;


@Service
public interface SMSService {

	void send(SMS sms, String code_pays, boolean extension_deja_dans_numero);
	public Map<String, Object> getAllPagined(String order_by, String order_dir, int page_number, int page_size, String destinataire, String code_pays, Integer statut);
	void avertiTransporteurOperation(Operation operation);
	void avertiProprietaireAppelOffre(Operation operation, Vehicule vehicule);
	void validerCompteClient(UtilisateurClient client);
	void previensKamtarNouvelleOperationClient(ListOperation operation);
	void previensKamtarNouveauDevis(Devis devis);
}
