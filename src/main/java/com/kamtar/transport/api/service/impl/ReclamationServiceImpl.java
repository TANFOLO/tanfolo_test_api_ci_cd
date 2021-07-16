package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.enums.ReclamationStatut;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperation;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.RRulesUtils;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import com.wbc.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service(value="ReclamationService")
public class ReclamationServiceImpl implements ReclamationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ReclamationServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private ReclamationRepository reclamationRepository;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private UtilisateurClientService utilisateurClientService;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private GeolocRepository geolocRepository;

	@Autowired
	private UtilisateurDriverRepository transporteurRepository; 

	@Autowired
	private VehiculeRepository vehiculeRepository; 

	@Autowired
	private VehiculeTypeRepository vehiculeTypeRepository;

	@Autowired
	private VehiculeCarrosserieRepository vehiculeCarrosserieRepository;

	@Autowired
	private VehiculeTonnageRepository vehiculeTonnageRepository;

	@Autowired
	private MarchandiseTypeRepository marchandiseTypeRepository; 

	@Autowired
	private OperationDocumentService operationDocumentService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private DirectionAPIService directionAPIService;

	@Autowired
	private UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	private SMSService smsService;

	@Autowired
	private DeviseService deviseService;

	@Autowired
	private OperationService operationService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ActionAuditService actionAuditService;

	@Autowired
	private WeatherAPIService weatherAPIService;

	@Autowired
	private OperationChangementStatutService operationChangementStatutService; 

	@Autowired
	private OperationChangementStatutRepository operationChangementStatutRepository; 

	@Autowired
	EmailToSendService emailToSendService;

	@Value("${reclamation.destinataire.sn}")
	private String reclamation_destinataire_sn;

	@Value("${reclamation.destinataire.ci}")
	private String reclamation_destinataire_ci;



	public Reclamation create(CreateReclamationParams params, Operation operation, String token) {

		String code_pays = jwtProvider.getCodePays(token);
		String type_compte = jwtProvider.getTypeDeCompte(token);

		// chargement du user et client
		UtilisateurClient utilisateur_client = utilisateurClientService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		UtilisateurClientPersonnel utilisateur_client_personnel = utilisateurClientPersonnelService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));
		Client client = null;
		if (utilisateur_client != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		} else if (utilisateur_client_personnel != null) {
			String uuid_client = jwtProvider.getClaimsValue("uuid_client", token);
			client = clientService.getByUUID(uuid_client, jwtProvider.getCodePays(token));
		}
		if (client == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger le client à partir de l'utilisateur");
		}



		if (!operation.getClient().equals(client)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous n'avez pas le droit d'enregistrer une réclamation pour cette opération");
		}

		// génération du code
		Long max_code = reclamationRepository.getMaxCode();
		if (max_code == null) {
			max_code = new Long(0);
		}
		Long code = max_code + new Long(1);

		String destinataire = reclamation_destinataire_ci;
		if (code_pays.equals("SN")) {
			destinataire = reclamation_destinataire_sn;
		}

		// enregistre la réclamation
		Reclamation reclamation = new Reclamation(params, operation, destinataire, code, client, utilisateur_client_personnel, utilisateur_client, code_pays);
		reclamation = reclamationRepository.save(reclamation);

		// envoi d'un email à kamtar
		emailToSendService.prevenirKamtarNouvelleReclamation(reclamation, jwtProvider.getCodePays(token));

		// envoi d'un email de confirmation au client
		emailToSendService.confirmerClientNouvelleReclamation(reclamation, jwtProvider.getCodePays(token));

		return reclamation;
	}



	@Override
	public Reclamation getByUUID(String uuid, String code_pays) {
		try {
			return reclamationRepository.findByUUID(UUID.fromString(uuid), code_pays);
		} catch (IllegalArgumentException e) {
			logger.warn("uuid invalide : " + uuid);
		}
		return null;
	}


	@Override
	public Reclamation changer_statut(EditReclamationParams postBody, Reclamation reclamation, String token) {

		if (ReclamationStatut.valueOf(postBody.getStatut()) == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de trouver le statut");
		}
		reclamation.setStatut(ReclamationStatut.valueOf(postBody.getStatut()).toString());
		reclamationRepository.save(reclamation);
		return reclamation;
	}

	@Override
	public Page<Reclamation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Reclamation> conditions) {

		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return reclamationRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Reclamation> conditions) {
		return reclamationRepository.count(conditions);
	}




	@Override
	public Reclamation getByCode(Long code, String code_pays) {
		return reclamationRepository.findByCode(code, code_pays);
	}

	@Override
	public Reclamation save(Reclamation reclamation) {
		return reclamationRepository.save(reclamation);
	}



}
