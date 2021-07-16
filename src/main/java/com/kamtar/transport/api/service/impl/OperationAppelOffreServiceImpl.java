package com.kamtar.transport.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationAppelOffre;
import com.kamtar.transport.api.model.UtilisateurOperateurKamtar;
import com.kamtar.transport.api.model.UtilisateurProprietaire;
import com.kamtar.transport.api.model.UtilisateurDriver;
import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.params.CreateEditOperationAppelOffreParams;
import com.kamtar.transport.api.repository.OperationAppelOffreRepository;
import com.kamtar.transport.api.repository.OperationRepository;
import com.kamtar.transport.api.repository.UtilisateurDriverRepository;
import com.kamtar.transport.api.repository.VehiculeRepository;
import com.kamtar.transport.api.service.EmailToSendService;
import com.kamtar.transport.api.service.OperationAppelOffreService;
import com.kamtar.transport.api.service.SMSService;
import com.kamtar.transport.api.utils.JWTProvider;


@Service(value="OperationAppelOffreService")
public class OperationAppelOffreServiceImpl implements OperationAppelOffreService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationAppelOffreServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private OperationRepository operationRepository; 
	
	@Autowired
	private VehiculeRepository vehiculeRepository; 

	@Autowired
	private OperationAppelOffreRepository operationAppelOffreRepository;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	SMSService smsService;


	public List<OperationAppelOffre> create(CreateEditOperationAppelOffreParams params, UtilisateurOperateurKamtar operateur, String token) {

		List<OperationAppelOffre> operation_appel_offres = new ArrayList<OperationAppelOffre>();

		// chargement des objets
		Operation operation = operationRepository.findByUUID(UUID.fromString(params.getId_operation()), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération");
		}
		List<String> uuid_vehicules = params.getId_vehicules();
		for (String uuid_vehicule : uuid_vehicules) {

			Vehicule vehicule = vehiculeRepository.findByUUID(UUID.fromString(uuid_vehicule), jwtProvider.getCodePays(token));
			if (vehicule == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le véhicule");
			}
			UtilisateurDriver transporteur = vehicule.getDriverPrincipal();
			if (transporteur == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver principal du véhicule");
			}
			UtilisateurProprietaire proprietaire = vehicule.getProprietaire();
			if (proprietaire == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propriétaire du véhicule");
			}
			// enregistre le user dans la base de données
			OperationAppelOffre operation_appel_offre = new OperationAppelOffre(operation, transporteur, operateur, params.getMontant(), params.getMontant_devise(), vehicule, operation.getCodePays());
			operation_appel_offre = operationAppelOffreRepository.save(operation_appel_offre);

			operation_appel_offres.add(operation_appel_offre);
		}

		return operation_appel_offres;

	}


	public List<OperationAppelOffre> edit(CreateEditOperationAppelOffreParams params, UtilisateurOperateurKamtar operateur, String token) {
		List<OperationAppelOffre> operation_appel_offres = new ArrayList<OperationAppelOffre>();

		// chargement des objets
		Operation operation = operationRepository.findByUUID(UUID.fromString(params.getId_operation()), jwtProvider.getCodePays(token));
		if (operation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver l'opération");
		}
		List<String> uuid_vehicules = params.getId_vehicules();
		for (String uuid_vehicule : uuid_vehicules) {
			if (uuid_vehicule != null && !"".equals(uuid_vehicule.trim())) {
				Vehicule vehicule = vehiculeRepository.findByUUID(UUID.fromString(uuid_vehicule), jwtProvider.getCodePays(token));
				if (vehicule == null) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le véhicule");
				}
				UtilisateurDriver transporteur = vehicule.getDriverPrincipal();
				if (transporteur == null) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le driver principal du véhicule");
				}
				UtilisateurProprietaire proprietaire = vehicule.getProprietaire();
				if (proprietaire == null) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le propriétaire du véhicule");
				}

				// chargement
				OperationAppelOffre operation_appel_offre = operationAppelOffreRepository.findByOperationEtVehicule(operation, vehicule, jwtProvider.getCodePays(token));
				boolean creation = false;
				if (operation_appel_offre == null) {
					creation = true;
					operation_appel_offre = new OperationAppelOffre(operation, transporteur, operateur, params.getMontant(), params.getMontant_devise(), vehicule, operation.getCodePays());

				} else {
					operation_appel_offre.edit(params);
				}
				operation_appel_offre = operationAppelOffreRepository.save(operation_appel_offre);
				operation_appel_offres.add(operation_appel_offre);
				if (creation) {
					// envoi du SMS au propriétaire
					smsService.avertiProprietaireAppelOffre(operation_appel_offre.getOperation(), operation_appel_offre.getVehicule());
				}
			}
		}

		return operation_appel_offres;

	}


	@Override
	public List<OperationAppelOffre> getOperationsAppelsOffre(List<Vehicule> vehicules, String code_pays) {
		return operationAppelOffreRepository.getOperationsAppelsOffre(vehicules, code_pays);
	}

	@Override
	public Page<OperationAppelOffre> getOperationsAppelsOffre(String order_by, String order_dir, int page_number, int page_size, Specification<OperationAppelOffre> conditions) {

		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return operationAppelOffreRepository.findAll(conditions, pageable);
	}
	
	


	@Override
	public List<OperationAppelOffre> findByVehiculeAndOperation(Operation operation, List<Vehicule> vehicules, boolean filtre_accepte, boolean filtre_refuse, String code_pays) {

		if (vehicules.isEmpty()) {
			return new ArrayList<OperationAppelOffre>();
		}
		 if (filtre_accepte && !filtre_refuse) {

			return operationAppelOffreRepository.findByVehiculeAndOperationAvecStatut(operation, vehicules, "1", code_pays);
		} else if (!filtre_accepte && filtre_refuse) {
			return operationAppelOffreRepository.findByVehiculeAndOperationAvecStatut(operation, vehicules, "3", code_pays);
		}
		return operationAppelOffreRepository.findByVehiculeAndOperation(operation, vehicules, code_pays);

	}


	@Override
	public List<OperationAppelOffre> findByOperation(Operation operation, boolean filtre_accepte, boolean filtre_refuse, String code_pays) {

		if (filtre_accepte && !filtre_refuse) {

			return operationAppelOffreRepository.findByOperationAvecStatut(operation, "1", code_pays);
		} else if (!filtre_accepte && filtre_refuse) {
			return operationAppelOffreRepository.findByOperationAvecStatut(operation, "3", code_pays);
		}
		return operationAppelOffreRepository.findByOperation(operation, code_pays);

	}

	@Override
	public List<OperationAppelOffre> findByOperation(Operation operation, String code_pays) {

		return operationAppelOffreRepository.findByOperation(operation, code_pays);

	}



	@Override
	public OperationAppelOffre getByUUID(String uuid, String code_pays) {
		try {
			return operationAppelOffreRepository.findByUUID(UUID.fromString(uuid), code_pays);
		} catch (IllegalArgumentException e) {
			logger.warn("uuid invalide : " + uuid);
		}
		return null;
	}


	@Override
	public OperationAppelOffre save(OperationAppelOffre objet) {
	return operationAppelOffreRepository.save(objet);
	}

	@Override
	public long countOperationsDriver(UtilisateurDriver driver, String code_pays) {
		return operationAppelOffreRepository.countOperationsDriver(driver, code_pays);
	}

	@Override
	public long countOperationsPropretaire(UtilisateurProprietaire proprietaire, String code_pays) {
		return operationAppelOffreRepository.countOperationsProprietaire(proprietaire, code_pays);
	}


	@Override
	public Long compterAppelOffresNonRepondus(List<Vehicule> vehicules, String code_pays) {
		return operationAppelOffreRepository.compterAppelOffresNonRepondus(vehicules, code_pays);
	}

}
