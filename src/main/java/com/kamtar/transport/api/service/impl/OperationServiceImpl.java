package com.kamtar.transport.api.service.impl;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperation;
import com.kamtar.transport.api.utils.RRulesUtils;
import com.kamtar.transport.api.utils.UpdatableBCrypt;
import com.wbc.core.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.params.CreateOperationParClientParams;
import com.kamtar.transport.api.params.CreateOperationParams;
import com.kamtar.transport.api.params.EditOperationParams;
import com.kamtar.transport.api.utils.JWTProvider;

import javax.persistence.Column;


@Service(value="OperationService")
public class OperationServiceImpl implements OperationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(OperationServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private EtapeRepository etapeRepository;

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

	@Value("${predictif.temps_trajet}")
	private boolean predictif_temps_trajet;

	@Value("${predictif.meteo_trajet}")
	private boolean predictif_meteo_trajet;

	@Value("${predictif.refresh_auto}")
	private boolean predictif_refresh_auto;


	private void verificationsPassageOperationAValide(Operation operation) {

		if (operation.getTransporteur() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez indiquer un transporteur pour valider l'opération");
		} else if (operation.getClient() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez indiquer le client pour valider l'opération");
		} else if (operation.getVehicule() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez indiquer le véhicule pour valider l'opération");
		} 

	}

	public void refreshPredictifEveryDay() {

		// récupère la liste des opérations en validé qui sont à plus de 24h de commencer
		List<Operation> operations = operationService.getOperationsValideesDepartDansPlusDe24h();
		logger.info("refreshPredictifEveryDay de " + operations.size() + " opérations");

		for (Operation operation : operations) {
			logger.info("operation " + operation.getCode() + "");


			if (predictif_refresh_auto) {

				// prédiction du temps de trajet (avec météo si le départ est dans moins de 16 jours)
				operation = predictionTempsTrajet(operation, false);
				operation = preditionMeteo(operation, false);

			}

			operationService.save(operation);
		}



	}

	@Override
	public void refreshPredictifEveryHour() {


		// récupère la liste des opérations en validé qui sont à moins de 24h de commencer
		List<Operation> operations = operationService.getOperationsValideesDepartDansMoinsDe24h();
		logger.info("refreshPredictifEveryHour de " + operations.size() + " opérations");

		for (Operation operation : operations) {
			logger.info("operation " + operation.getCode() + "");


			if (predictif_refresh_auto) {

				// prédiction du temps de trajet (avec météo si le départ est dans moins de 16 jours)
				operation = predictionTempsTrajet(operation, false);
				operation = preditionMeteo(operation, false);

				operationService.save(operation);

			}


		}

	}

	@Override
	public void creerOperationsRecurrentes(Date date_reference, int nb_jour_dans_futur) {
		logger.info("creerOperationsRecurrentes nb_jour_dans_futur=" + nb_jour_dans_futur);

		// récupère la liste des opérations qui ont une recurrenceProchain demain
		List<Operation> operations = operationService.getOperationsRecurrenceProchainDemainAvenir(date_reference, nb_jour_dans_futur);
		logger.info("creerOperationsRecurrentes de " + operations.size() + " opérations");

		for (Operation operation : operations) {
			logger.info("operation " + operation.getCode() + "");


			Calendar date1 = Calendar.getInstance();
			date1.setTime(operation.getDepartDateProgrammeeOperation());
			date1.set(Calendar.HOUR_OF_DAY, 0);
			date1.set(Calendar.MINUTE, 0);
			date1.set(Calendar.SECOND, 0);
			Calendar date2 = Calendar.getInstance();
			date2.setTime(date_reference);
			date2.set(Calendar.HOUR_OF_DAY, 0);
			date2.set(Calendar.MINUTE, 0);
			date2.set(Calendar.SECOND, 0);

			int diffInDays = (int)( (date2.getTime().getTime() - date1.getTime().getTime()) / (1000 * 60 * 60 * 24) );
			logger.info("diffInDays = " + diffInDays);
			diffInDays = Math.abs(diffInDays);

			Calendar c = Calendar.getInstance();
			c.setTime(operation.getDepartDateProgrammeeOperation());
			c.add(Calendar.DATE, diffInDays + nb_jour_dans_futur);

			Operation nouvelle_operation = dupliquerOperationReccurente(operation, c.getTime());
			logger.info("création de l'opération " + nouvelle_operation.getCode() + " à partir de l'opération " + operation.getCode() + " avec date de ivraison calculée " + c.getTime());
			//operationService.save(nouvelle_operation);

			// calcule la prochaine date de récurrence
			try {

				Calendar c2 = Calendar.getInstance();
				c2.setTime(date_reference);
				c2.add(Calendar.DATE, nb_jour_dans_futur);
				c2.set(Calendar.HOUR_OF_DAY, 0);
				c2.set(Calendar.MINUTE, 0);
				c2.set(Calendar.SECOND, 0);

				Date until_date = RRulesUtils.getDateUtil(operation.getRecurrenceRrule(), c2.getTime());
				operation.setRecurrenceFin(until_date);

				Date prochaine_date = RRulesUtils.prochaineDate(operation.getRecurrenceRrule(), operation.getDepartDateProgrammeeOperation(), c2.getTime(), operation.getRecurrenceFin());
				logger.info("prochaine date recurrenceprochain calculée pour opération " + operation.getCode() + " : " + prochaine_date);
				operation.setRecurrenceProchain(prochaine_date);

				operationService.save(operation);
			} catch (InvalidRecurrenceRuleException e) {
				logger.error("erreur lors du calcul de la prochaine date de l'opération " + operation.getCode() + " avec rrule = " + operation.getRecurrenceRrule(), e);

			}

		}


	}

	@Override
	public List<Geoloc> findGeolocsOperation(String immatriculation, String pays, Operation operation) {

		Date dateMin = operation.getDepartDateOperation();
		Date dateMax = operation.getDerniereDateConnue();

		return geolocRepository.findAllGeoloc(immatriculation, pays, dateMin, dateMax);

	}

	public Operation create(CreateOperationParams params, UtilisateurOperateurKamtar operateur, String token) {

		UtilisateurDriver transporteur = null;
		Vehicule vehicule = null;
		VehiculeType vehicule_type = null;
		MarchandiseType marchandise_type = null;
		VehiculeTonnage tonnage_vehicule = null;

		String type_compte = jwtProvider.getTypeDeCompte(token);

		// chargement des objets
		Client client = clientRepository.findByUUID(UUID.fromString(params.getClientUuid()), jwtProvider.getCodePays(token));
		UtilisateurClientPersonnel client_personnel = null;
		logger.info("params.getClientPersonnelUuid() = " + params.getClientPersonnelUuid());
		if (params.getClientPersonnelUuid() != null && !"".equals(params.getClientPersonnelUuid())) {
			client_personnel = utilisateurClientPersonnelRepository.findByUUID(UUID.fromString(params.getClientPersonnelUuid()), jwtProvider.getCodePays(token));
		}
		if (params.getTransporteur() != null && !"".equals(params.getTransporteur())) {
			transporteur = transporteurRepository.findByUUID(UUID.fromString(params.getTransporteur()), jwtProvider.getCodePays(token));
			/*if (transporteur.isActivate()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le transporteur sélectionné est désactivé");
			}*/
		}
		if (params.getVehicule() != null && !"".equals(params.getVehicule())) {
			vehicule = vehiculeRepository.findByUUID(UUID.fromString(params.getVehicule()), jwtProvider.getCodePays(token));
			/*if (vehicule.isActivate()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le véhicule sélectionné est désactivé");
			}*/
		}
		if (params.getTypeMarchandise() != null && !"".equals(params.getTypeMarchandise())) {
			Optional<MarchandiseType> marchandise_obj = marchandiseTypeRepository.findByCode(params.getTypeMarchandise());
			if (marchandise_obj.isPresent()) {
				marchandise_type = marchandise_obj.get();
			}
		}
		if (params.getCategorieVehicule() != null && !"".equals(params.getCategorieVehicule())) {
			Optional<VehiculeType> vehicule_obj = vehiculeTypeRepository.findByCode(params.getCategorieVehicule());
			if (vehicule_obj.isPresent()) {
				vehicule_type = vehicule_obj.get();
			}
		}
		if (params.getTonnageVehicule() != null && !"".equals(params.getTonnageVehicule())) {
			Optional<VehiculeTonnage> tonnage_obj = vehiculeTonnageRepository.findByCode(params.getTonnageVehicule());
			if (tonnage_obj.isPresent()) {
				tonnage_vehicule = tonnage_obj.get();
			}
		}

		// génération du code
		Long max_code = operationRepository.getMaxCode(); 
		if (max_code == null) {
			max_code = new Long(0);
		}
		Long code = max_code + new Long(1);

		// enregistre le user dans la base de données
		Operation operation = new Operation(params, client, client_personnel, transporteur, operateur, code, vehicule, marchandise_type, tonnage_vehicule);

		//if (params.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString()) && (params.getPrixAPayerParClient() == null || (new Double(0).equals(params.getPrixAPayerParClient())))) {
		//	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez indiquer un prix à payer par le client pour lui proposer un prix");
		//}


		if (params.getStatut().equals(OperationStatut.VALIDE.toString())) {
			// enregistre la validation du operation par l'opérateur

			// vérifications
			verificationsPassageOperationAValide(operation);

			// enregistrement en bdd
			operation.setValideParOperateur(true);
			operation.setDateValideParOperateur(new Date());

			// prédiction du temps de trajet (avec météo si le départ est dans moins de 16 jours)
			operation = predictionTempsTrajet(operation, true);
			operation = preditionMeteo(operation, true);

			// envoi du sms au transporteur
			smsService.avertiTransporteurOperation(operation);

			// envoi du recap de commande au client
			emailToSendService.envoyerConfirmationOperation(operation, jwtProvider.getCodePays(token));



		} else if (params.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			// enregistre la proposition du prix auprès du client

			operation.setPrixProposeAuClientDate(new Date());

			// envoi du recap de commande au client
			emailToSendService.envoyerPropositionPrixOperation(operation, jwtProvider.getCodePays(token));

			operation.setStatut(OperationStatut.EN_COURS_DE_TRAITEMENT.toString());

		}

		// enregistrement des étapes au préalable
		etapeRepository.saveAll(operation.getEtapes());

		operation = operationRepository.save(operation);

		OperationChangementStatut operation_changement = new OperationChangementStatut(null, operation.getStatut(), operation, operateur, null, null, operation.getCodePays());
		operationChangementStatutService.create(operation_changement);

		// enregistre les documents de l'opération
		String folderUuid = params.getFolderUuid();
		if (folderUuid != null && !"".equals(folderUuid)) {
			String tempDir = System.getProperty("java.io.tmpdir");
			File folderTempUUID = new File(tempDir + "/" + params.getFolderUuid());
			if (folderTempUUID.exists()) {
				// on envoit toutes les photos sur s3
				operationDocumentService.saveDocuments(operation, folderTempUUID, type_compte);
				folderTempUUID.delete();
			}
		}

		return operation;
	}

	private Operation preditionMeteo(Operation operation, boolean forcer) {
		logger.info("preditionMeteo1");
		if (predictif_meteo_trajet) {
			logger.info("preditionMeteo2 " + forcer);

			// pas de prédictif sur trajet passé
			if (!forcer && new Date().after(operation.getDepartDateProgrammeeOperation())) {
				logger.info("preditionMeteo3 ");
				return operation;
			}

			Calendar date1 = Calendar.getInstance();
			date1.setTime(new Date());
			date1.set(Calendar.HOUR_OF_DAY, 0);
			date1.set(Calendar.MINUTE, 0);
			date1.set(Calendar.SECOND, 0);
			Calendar date2 = Calendar.getInstance();
			date2.setTime(operation.getDepartDateProgrammeeOperation());
			date2.set(Calendar.HOUR_OF_DAY, 0);
			date2.set(Calendar.MINUTE, 0);
			date2.set(Calendar.SECOND, 0);

			logger.info("date1 = " + new Date(date1.getTime().getTime()));
			logger.info("date2 = " + new Date(date2.getTime().getTime()));

			int diffInDays = (int)( (date2.getTime().getTime() - date1.getTime().getTime()) / (1000 * 60 * 60 * 24) ) + 1;
			logger.info("diffInDays = " + diffInDays);
			diffInDays = Math.abs(diffInDays);



			//long days_futurs = ChronoUnit.DAYS.between(new Date().toInstant(), operation.getDepartDateProgrammeeOperation().toInstant());
			logger.info("days_futurs = " + diffInDays);
			if (diffInDays < 16 && diffInDays > 0) {
				List<String> ret_depart = weatherAPIService.getWeather(operation.getDepartAdresseLatitude(), operation.getDepartAdresseLongitude(), (int) diffInDays);
				operation.setMeteoPrevueDepart(ret_depart.get(0));
				operation.setMeteoPrevueIconeDepart(ret_depart.get(1));
				operation.setMeteoPrevueIDDepart(ret_depart.get(2));
				operation.setMeteoPrevueDerniereMiseAJour(new Date());
				List<String> ret_arrivee = weatherAPIService.getWeather(operation.getArriveeAdresseLatitude(), operation.getArriveeAdresseLongitude(), (int) diffInDays);
				operation.setMeteoPrevueArrivee(ret_arrivee.get(0));
				operation.setMeteoPrevueIconeArrivee(ret_arrivee.get(1));
				operation.setMeteoPrevueIDArrivee(ret_depart.get(2));
				operation.setMeteoPrevueDerniereMiseAJour(new Date());
			}

		}

		return operation;

	}



	/*
	converti la distance retournée par google en nombre de km
	ex : "4,654.4 km" => 4654.4
	 */
	private Long predictonDistanceTrajetConvertion(String distance) {

		String[] arr = distance.split("km");
		distance = arr[0].trim();

		Number distance_long = null;
		try {
			distance_long = NumberFormat.getNumberInstance(Locale.US).parse(distance);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return distance_long.longValue();
	}


	/*
	converti la durée retournée par google en nombre de minutes
	ex : "2 days 3 hours 10 minutes" => 3070
	 */
	private Long predictonTempsTrajetConvertion(String duree) {

		Long minutes = new Long(0);
		if (duree.contains("days")) {
			String[] arr = duree.split("days");
			minutes = minutes + (new Long(arr[0].trim()) * 60 * 24);
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		if (duree.contains("day")) {
			String[] arr = duree.split("day");
			minutes = minutes + (new Long(arr[0].trim()) * 60 * 24);
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		if (duree.contains("hours")) {
			String[] arr = duree.split("hours");
			minutes = minutes + (new Long(arr[0].trim()) * 60);
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		if (duree.contains("hour")) {
			String[] arr = duree.split("hour");
			minutes = minutes + (new Long(arr[0].trim()) * 60);
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		if (duree.contains("mins")) {
			String[] arr = duree.split("mins");
			minutes = minutes + (new Long(arr[0].trim()) );
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		if (duree.contains("min")) {
			String[] arr = duree.split("min");
			minutes = minutes + (new Long(arr[0].trim()) );
			if (arr.length > 1) {
				duree = arr[1];
			} else {
				duree = "";
			}
		}
		return minutes;
	}

	private Operation predictionTempsTrajet(Operation operation, boolean forcer) {

		if (predictif_temps_trajet) {

			// pas de prédictif sur trajet passé
			if (!forcer && new Date().after(operation.getDepartDateProgrammeeOperation())) {
				return operation;
			}

			List<String> etapes = new ArrayList<String>();
			for (Etape etape : operation.getEtapes()) {
				etapes.add(etape.getAdresseComplete());
			}
			/*
			etapes.add(operation.getAdresse1Complete());
			etapes.add(operation.getAdresse2Complete());
			etapes.add(operation.getAdresse3Complete());
			etapes.add(operation.getAdresse4Complete());
			etapes.add(operation.getAdresse5Complete());*/
			List<Long> ret = directionAPIService.getDuration(operation.getDepartAdresseComplete(), operation.getArriveeAdresseComplete(), null, operation.getDepartDateProgrammeeOperation());
			operation.setDistanceTrajetKm(ret.get(0));
			//operation.setDistanceTrajetKm(predictonDistanceTrajetConvertion(ret.get(0)));
			operation.setDureePrevueTrajetMin(ret.get(1));
			//operation.setDureePrevueTrajetMin(predictonTempsTrajetConvertion(ret.get(1)));
			operation.setDureePrevueTrajetDerniereMiseAJour(new Date());

		}

		return operation;

	}


	private Operation createInterne(CreateOperationParClientParams params, UtilisateurClient utilisateur_client, UtilisateurClientPersonnel utilisateur_client_personnel, Client client, String token, boolean isFirst, Operation premiere_operation) {
		logger.info("createInterne isFirst=" + isFirst);

		// génération du code
		Long max_code = operationRepository.getMaxCode();
		if (max_code == null) {
			max_code = new Long(0);
		}
		Long code = max_code + new Long(1);

		MarchandiseType marchandise_type = null;
		VehiculeCarrosserie vehicule_carosserie = null;
		VehiculeTonnage vehicule_tonnage = null;

		String type_compte = jwtProvider.getTypeDeCompte(token);

		if (params.getTonnageVehicule()!= null && !"".equals(params.getTonnageVehicule())) {
			Optional<VehiculeTonnage> tonnage_obj = vehiculeTonnageRepository.findByCode(params.getTonnageVehicule());
			if (tonnage_obj.isPresent()) {
				vehicule_tonnage = tonnage_obj.get();
			}
		}

		if (params.getTypeMarchandise() != null && !"".equals(params.getTypeMarchandise())) {
			Optional<MarchandiseType> marchandise_obj = marchandiseTypeRepository.findByCode(params.getTypeMarchandise());
			if (marchandise_obj.isPresent()) {
				marchandise_type = marchandise_obj.get();
			}
		}
		if (params.getCarrosserieVehicule() != null && !"".equals(params.getCarrosserieVehicule())) {
			Optional<VehiculeCarrosserie> vehicule_obj = vehiculeCarrosserieRepository.findByCode(params.getCarrosserieVehicule());
			if (vehicule_obj.isPresent()) {
				vehicule_carosserie = vehicule_obj.get();
			}
		}

		// récurrence (que sur la première opération si il y en a plusieurs)
		String recurrenceRrule = null;
		Date recurrenceProchain = null;
		Date recurrenceProchainSuivant = null;
		Date until_date = null;
		if (isFirst && params.getRecurrenceRrule() != null && !"".equals(params.getRecurrenceRrule())) {
			recurrenceRrule = params.getRecurrenceRrule();
			logger.info("createInterne recurrenceRrule=" + recurrenceRrule);

			until_date = RRulesUtils.getDateUtil(recurrenceRrule, new Date());

			try {
				recurrenceProchain = RRulesUtils.prochaineDate(recurrenceRrule, params.getDepartDateProgrammeeOperation(), new Date(), until_date);
				logger.info("createInterne recurrenceProchain=" + recurrenceProchain);

				recurrenceProchainSuivant = RRulesUtils.prochaineDate(recurrenceRrule, params.getDepartDateProgrammeeOperation(), recurrenceProchain, until_date);
				logger.info("createInterne recurrenceProchainSuivant=" + recurrenceProchainSuivant);
			} catch (InvalidRecurrenceRuleException e) {
				logger.error("InvalidRecurrenceRuleException sur rrule " + recurrenceRrule);
			}
		}
		if (!isFirst && premiere_operation != null) {
			recurrenceProchain = premiere_operation.getDepartDateProgrammeeOperation();
			logger.info(" pas la première opération, on récupère la date " + recurrenceProchain);
		}

		// enregistre l'opération en bdd
		Operation operation = new Operation(params, client, utilisateur_client_personnel, code, marchandise_type, vehicule_tonnage, recurrenceRrule, recurrenceProchain, recurrenceProchainSuivant, until_date);

		// enregistrement des étapes au préalable
		etapeRepository.saveAll(operation.getEtapes());

		operation = operationRepository.save(operation);

		OperationChangementStatut operation_changement = new OperationChangementStatut(null, operation.getStatut(), operation, null, null, null, operation.getCodePays());
		operationChangementStatutService.create(operation_changement);

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Nouvelle commande créée par un expéditeur", operation.getUuid().toString(), client.getUuid().toString(), operation.getCodePays());
		notificationService.create(notification, jwtProvider.getCodePays(token));

		// prédiction du temps de trajet (avec météo si le départ est dans moins de 16 jours)
		operation = predictionTempsTrajet(operation, true);
		operation = preditionMeteo(operation, true);

		// enregistre les photos du carroussel
		String folderUuid = params.getFolderUuid();
		if (folderUuid != null && !"".equals(folderUuid)) {
			String tempDir = System.getProperty("java.io.tmpdir");
			File folderTempUUID = new File(tempDir + "/" + params.getFolderUuid());
			if (folderTempUUID.exists()) {
				// on envoit toutes les photos sur s3
				operationDocumentService.saveDocuments(operation, folderTempUUID, type_compte);
				folderTempUUID.delete();
			}
		}

		return operation;
	}


	public List<Operation> create(CreateOperationParClientParams params, UtilisateurClient utilisateur_client, UtilisateurClientPersonnel utilisateur_client_personnel, Client client, String token) {

		if (params.getNbCamions() != null && (params.getNbCamions() < 1 || params.getNbCamions() > 20)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le nombre de camions est invalide (valeurs autorisées : 1 à 20)");
		}

		List<Operation> operations = new ArrayList<Operation>();
		int nb_operation = 1;
		if (params.getNbCamions() != null ) {
			nb_operation = params.getNbCamions();
		}

		Operation premiere_operation = null;
		for (int c = 1; c<=nb_operation; c++) {
			Operation operation_unitaire = createInterne(params, utilisateur_client, utilisateur_client_personnel, client, token, (c==1), premiere_operation);
			if (c==1) {
				premiere_operation = operation_unitaire;
			}
			operations.add(operation_unitaire);

		}

		// envoi d'un email
		ListOperation liste_operation = new ListOperation();
		liste_operation.addAll(operations);
		emailToSendService.prevenirKamtarNouvelleCommandeClient(liste_operation, operations.get(0).getCodePays(), params.getNbCamions());

		// envoi SMS
		smsService.previensKamtarNouvelleOperationClient(liste_operation);

		return operations;
	}



	@Override
	public Operation getByUUID(String uuid, String code_pays) {
		try {
			return operationRepository.findByUUID(UUID.fromString(uuid), code_pays);
		} catch (IllegalArgumentException e) {
			logger.warn("uuid invalide : " + uuid);
		}
		return null;
	}


	@Override
	public Page<Operation> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions) {

		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return operationRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Operation> conditions) {
		return operationRepository.count(conditions);
	}

	@Override
	public boolean update(EditOperationParams params, Operation operation, String pays, String token) {

		UtilisateurDriver transporteur = null;
		Vehicule vehicule = null;
		MarchandiseType marchandise_type = null;
		VehiculeTonnage vehicule_tonnage = null;
		VehiculeCarrosserie vehicule_carrosserie = null;
		String type_compte = jwtProvider.getTypeDeCompte(token);

		// chargement des objets
		Client client = clientRepository.findByUUID(UUID.fromString(params.getClientUuid()), pays);
		UtilisateurClientPersonnel client_personnel = null;
		if (params.getClientPersonnelUuid() != null && !"".equals(params.getClientPersonnelUuid())) {
			client_personnel = utilisateurClientPersonnelRepository.findByUUID(UUID.fromString(params.getClientPersonnelUuid()), pays);
		}

		if (params.getTransporteur() != null && !"".equals(params.getTransporteur())) {
			transporteur = transporteurRepository.findByUUID(UUID.fromString(params.getTransporteur()), pays);
		}
		if (params.getVehicule() != null && !"".equals(params.getVehicule())) {
			vehicule = vehiculeRepository.findByUUID(UUID.fromString(params.getVehicule()), pays);
		}
		if (params.getTypeMarchandise() != null && !"".equals(params.getTypeMarchandise())) {
			Optional<MarchandiseType> marchandise_obj = marchandiseTypeRepository.findByCode(params.getTypeMarchandise());
			if (marchandise_obj.isPresent()) {
				marchandise_type = marchandise_obj.get();
			}
		}
		if (params.getTonnageVehicule() != null && !"".equals(params.getTonnageVehicule())) {
			Optional<VehiculeTonnage> tonnage_obj = vehiculeTonnageRepository.findByCode(params.getTonnageVehicule());
			if (tonnage_obj.isPresent()) {
				vehicule_tonnage = tonnage_obj.get();
			}
		}

		//if (params.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString()) && (params.getPrixAPayerParClient() == null || (new Double(0).equals(params.getPrixAPayerParClient())))) {
		//	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez indiquer un prix à payer par le client pour lui proposer un prix");
		//}

		// enregistrement des étapes au préalable
		etapeRepository.saveAll(operation.getEtapes());

		operationRepository.save(operation);


		if ((operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) || operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) && params.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			// si le statut passe en EN_COURS_DE_TRAITEMENT depuis ENREGISTRE ou EN_COURS_DE_TRAITEMENT

			operation.setPrixProposeAuClientDate(new Date());

			operation.edit(params, client, client_personnel, transporteur, vehicule, marchandise_type, vehicule_tonnage, false);

			operation.setStatut(OperationStatut.EN_COURS_DE_TRAITEMENT.toString());
			etapeRepository.saveAll(operation.getEtapes());
			operationRepository.save(operation);

			emailToSendService.envoyerPropositionPrixOperation(operation, pays);

		} else if ((operation.getStatut().equals(OperationStatut.ENREGISTRE.toString()) ||  operation.getStatut().equals(OperationStatut.EN_COURS_DE_TRAITEMENT.toString())) && params.getStatut().equals(OperationStatut.VALIDE.toString())) {
			// si le statut passe en VALIDE
			operation.edit(params, client, client_personnel, transporteur, vehicule, marchandise_type, vehicule_tonnage, true);

			// vérifications avant le passage à VALIDE
			verificationsPassageOperationAValide(operation);

			etapeRepository.saveAll(operation.getEtapes());
			operationRepository.save(operation);

			// si le compte du client a été créé via un devis, on lui regénère un mot de passe et on lui envoi par email
			String mot_de_passe = StringUtils.generateRandomAlphaNumericString(8);
			if (operation.getDevis() != null) {

				operation.setDevis(null);

				UtilisateurClient utilisateur_client = operation.getClient().getUtilisateur();
				utilisateur_client.setMotDePasse(UpdatableBCrypt.hashPassword(mot_de_passe));
				utilisateur_client.setActivate(true);
				utilisateur_client.setCreeParDevis(false);
				utilisateur_client = utilisateurClientService.save(utilisateur_client);
				emailToSendService.prevenirOperationAPartirDeDevis(operation, mot_de_passe, operation.getCodePays());

			}

			// prédiction du temps de trajet (avec météo si le départ est dans moins de 16 jours)
			operation = predictionTempsTrajet(operation, true);
			operation = preditionMeteo(operation, true);

			// envoi du sms au transporteur
			smsService.avertiTransporteurOperation(operation);

			// envoi du recap de commande au client
			emailToSendService.envoyerConfirmationOperation(operation, pays);

			operation.setValideParOperateur(true);
			operation.setDateValideParOperateur(new Date());

		} else {

			operation.edit(params, client, client_personnel, transporteur, vehicule, marchandise_type, vehicule_tonnage, false);

		}

		// enregistrement des étapes au préalable
		etapeRepository.saveAll(operation.getEtapes());

		operationRepository.save(operation);
		
		// enregistre les documents de l'opération
		String folderUuid = params.getFolderUuid();
		if (folderUuid != null && !"".equals(folderUuid)) {
			String tempDir = System.getProperty("java.io.tmpdir");
			File folderTempUUID = new File(tempDir + "/" + params.getFolderUuid());
			if (folderTempUUID.exists()) {
				// on envoit toutes les photos sur s3
				operationDocumentService.saveDocuments(operation, folderTempUUID, type_compte);
				folderTempUUID.delete();
			}
		}


		return true;
	}



	@Override
	public Page<Operation> getOperationsTransporteur(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions, UtilisateurDriver transporteur) {

		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);

		return operationRepository.findAll(conditions, pageable);
	}
	@Override
	public Page<Operation> getOperationsClient(String order_by, String order_dir, int page_number, int page_size, Specification<Operation> conditions, Client client) {

		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);

		return operationRepository.findAll(conditions, pageable);
	}

	@Override
	public List<Operation> getOperationsClient(Client client, String code_pays) {
		return operationRepository.findByClient(client, code_pays);
	}


	@Override
	public HashMap<String, List<Operation>> getOperationsTransporteurParJour(String order_dir, int page_number,
			int page_size, Specification<Operation> conditions, UtilisateurDriver transporteur, String code_pays) {

		// recherche toutes les operations triés par jour croissant
		List<String> statuts_transporteur = Arrays.asList(
				OperationStatut.VALIDE.toString(), 
				OperationStatut.ARRIVEE_CHEZ_CLIENT.toString(), 
				OperationStatut.CHARGEMENT_EN_COURS.toString(), 
				OperationStatut.CHARGEMENT_TERMINE.toString(), 
				OperationStatut.EN_DIRECTION_DESTINATION.toString(), 
				OperationStatut.ARRIVE_DESTINATION.toString(), 
				OperationStatut.DECHARGEMENT_EN_COURS.toString()

				);
		List<Operation> operations = operationRepository.getOperationsTransporteurParJour(transporteur, statuts_transporteur, code_pays);

		// map jour > operations
		HashMap<String, List<Operation>> operations_par_jour = new LinkedHashMap<String, List<Operation>>();
		for (Operation operation : operations) {
			if (operation.getTransporteur().equals(transporteur) && (statuts_transporteur.contains(operation.getStatut()))) {
				String date_formatted = operation.getDepartDateProgrammeeOperationSansHeure();
				if (date_formatted != null) {
					if (!operations_par_jour.containsKey(date_formatted)) {
						operations_par_jour.put(date_formatted, new ArrayList<Operation>());
					}
					operations_par_jour.get(date_formatted).add(operation);
				}
			}

		}
		return operations_par_jour;
	}



	@Override
	public List<Operation> getByIds(String[] uuids, String code_pays) {
		List<UUID> uuids_list = new ArrayList<UUID>();
		for (int i=0; i<uuids.length; i++) {
			uuids_list.add(UUID.fromString(uuids[i]));
		}
		return operationRepository.findByUuidIn(uuids_list, code_pays);
	}

	@Override
	public List<Operation> getByCodes(Long[] codes, String code_pays) {
		List<Long> codes_list = new ArrayList<Long>();
		for (int i=0; i<codes.length; i++) {
			codes_list.add(codes[i]);
		}
		return operationRepository.findByCodeIn(codes_list, code_pays);
	}

	@Override
	public List<Operation> getOperationsValideesDepartDansPlusDe24h() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		return operationRepository.findOperationsValideesDansPlusDe24h(c.getTime());
	}

	@Override
	public List<Operation> getOperationsValideesDepartDansMoinsDe24h() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		return operationRepository.findOperationsValideesDansMoinsDe24h(new Date(), c.getTime());
	}

	@Override
	public List<Operation> getOperationsRecurrenceProchainDemainAvenir(Date date_reference, int nb_jour_dans_futur) {
		logger.info("getOperationsRecurrenceProchainDemainAvenir date_reference " + date_reference);
		Calendar c = Calendar.getInstance();
		c.setTime(date_reference);
		c.add(Calendar.DATE, nb_jour_dans_futur-1);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		//c.set(Calendar.HOUR_OF_DAY, 0);
		//c.set(Calendar.MINUTE, 0);
		//c.set(Calendar.SECOND, 0);

		Calendar dateMax = new GregorianCalendar();
		dateMax.setTime(date_reference);
		dateMax.add(Calendar.DATE, nb_jour_dans_futur);
		dateMax.set(Calendar.HOUR_OF_DAY, 23);
		dateMax.set(Calendar.MINUTE, 59);
		dateMax.set(Calendar.SECOND, 59);

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		logger.info("recherche des opérations avec recurrence prochin entre " + formatter.format(c.getTime()) + " et " + formatter.format(dateMax.getTime()));
		return operationRepository.findOperationsRecurrenceProchain(c.getTime(), dateMax.getTime());
	}


	@Override
	public List<Operation> getOperationsTransporteurJour(String order_dir, int page_number,
			int page_size, Specification<Operation> conditions, List<Vehicule> vehicules, String journee, String code_pays) {
	
		SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");
		Date journee_debut = null;
		try {
			journee_debut = formatter.parse(journee);
		} catch (ParseException e) {
			logger.error("Impossible de parser la date " + journee, e);
			return null;
		} 

		Calendar c = Calendar.getInstance();
		c.setTime(journee_debut);
		c.add(Calendar.DATE, 1);  // number of days to add
		

		// recherche toutes les operations sur un jour
		List<Operation> operations = operationRepository.getOperationsDriverJour(vehicules, journee_debut, c.getTime(), code_pays);

		return operations;
	}


	@Override
	public Operation autocomplete(String query, String code_pays) {
		Operation res = operationRepository.findByCode(Long.valueOf(query), code_pays);
		return res;
	}

	@Override
	public Operation getByCode(Long code, String code_pays) {
		return operationRepository.findByCode(code, code_pays);
	}

	@Override
	public boolean delete(Operation operation) {
		operationChangementStatutRepository.setNullOperation(operation);
		operationRepository.delete(operation);
		return true;
	}

	@Override
	public long countNbOperationsProgrammees(Client client, String code_pays) {
		return operationRepository.countNbOperationsProgrammees(client, code_pays);
	}

	@Override
	public long countNbOperationsEnCours(Client client, String code_pays) {
		return operationRepository.countNbOperationsEnCours(client, code_pays);
	}

	@Override
	public long countNbOperationsProgrammees(UtilisateurClientPersonnel client, String code_pays) {
		return operationRepository.countNbOperationsProgrammees(client, code_pays);
	}

	@Override
	public long countNbOperationsTerminees(Client client, String code_pays) {
		return operationRepository.countNbOperationsTerminees(client, code_pays);
	}

	@Override
	public long countNbOperationsEnCours(UtilisateurClientPersonnel client, String code_pays) {
		return operationRepository.countNbOperationsEnCours(client, code_pays);
	}

	@Override
	public long countNbOperationsTerminees(UtilisateurClientPersonnel client, String code_pays) {
		return operationRepository.countNbOperationsTerminees(client, code_pays);
	}

	@Override
	public Operation save(Operation operation) {

		// enregistrement des étapes au préalable
		etapeRepository.saveAll(operation.getEtapes());

		return operationRepository.save(operation);
	}


	@Override
	public Operation dupliquer(Operation operation) {

		Operation cloned = operation.clone();
		cloned.setEtapes(new ArrayList<Etape>());
		int cpt = 1;
		for (Etape etape : operation.getEtapes()) {
			cloned.getEtapes().add(new Etape(etape, cpt++));
			cpt++;
		}

		// génération du code
		Long max_code = operationRepository.getMaxCode();
		Long code = max_code + new Long(1);
		cloned.setCode(code);

		// enregistrement des étapes au préalable
		if (cloned.getEtapes() != null && !cloned.getEtapes().isEmpty()) {
			etapeRepository.saveAll(cloned.getEtapes());
		}

		return operationRepository.save(cloned);
	}

	@Override
	public Operation dupliquerOperationReccurenteInterne(Operation operation, Date date_programmee_operation) {
		logger.info("dupliquerOperationReccurenteInterne = " + date_programmee_operation.toString());

		Operation cloned = new Operation();
		cloned.setNbDocuments(operation.getNbDocuments());
		cloned.setStatut(OperationStatut.ENREGISTRE.toString());

		cloned.setDepartDateProgrammeeOperation(date_programmee_operation);

		cloned.setDepartAdresseComplement(operation.getDepartAdresseComplement());
		cloned.setDepartAdresseComplete(operation.getDepartAdresseComplete());
		cloned.setDepartAdresseCountryCode(operation.getDepartAdresseCountryCode());
		cloned.setDepartAdresseLatitude(operation.getDepartAdresseLatitude());
		cloned.setDepartAdresseLongitude(operation.getDepartAdresseLongitude());
		cloned.setDepartAdresseRue(operation.getDepartAdresseRue());
		cloned.setDepartAdresseVille(operation.getDepartAdresseVille());

		List<String> etapes = new ArrayList<String>();
		cloned.setEtapes(new ArrayList<Etape>());
		int cpt = 1;
		for (Etape etape : operation.getEtapes()) {
			cloned.getEtapes().add(new Etape(etape, cpt));
			cpt++;
		}
/*
		cloned.setAdresse1Complement(operation.getAdresse1Complement());
		cloned.setAdresse1Complete(operation.getAdresse1Complete());
		cloned.setAdresse1CountryCode(operation.getAdresse1CountryCode());
		cloned.setAdresse1DestinataireNom(operation.getAdresse1DestinataireNom());
		cloned.setAdresse1DestinataireTelephone(operation.getAdresse1DestinataireTelephone());
		cloned.setAdresse1Rue(operation.getAdresse1Rue());
		cloned.setAdresse1Ville(operation.getAdresse1Ville());

		cloned.setAdresse2Complement(operation.getAdresse2Complement());
		cloned.setAdresse2Complete(operation.getAdresse2Complete());
		cloned.setAdresse2CountryCode(operation.getAdresse2CountryCode());
		cloned.setAdresse2DestinataireNom(operation.getAdresse2DestinataireNom());
		cloned.setAdresse2DestinataireTelephone(operation.getAdresse2DestinataireTelephone());
		cloned.setAdresse2Rue(operation.getAdresse2Rue());
		cloned.setAdresse2Ville(operation.getAdresse2Ville());

		cloned.setAdresse3Complement(operation.getAdresse3Complement());
		cloned.setAdresse3Complete(operation.getAdresse3Complete());
		cloned.setAdresse3CountryCode(operation.getAdresse3CountryCode());
		cloned.setAdresse3DestinataireNom(operation.getAdresse3DestinataireNom());
		cloned.setAdresse3DestinataireTelephone(operation.getAdresse3DestinataireTelephone());
		cloned.setAdresse3Rue(operation.getAdresse3Rue());
		cloned.setAdresse3Ville(operation.getAdresse3Ville());

		cloned.setAdresse4Complement(operation.getAdresse4Complement());
		cloned.setAdresse4Complete(operation.getAdresse4Complete());
		cloned.setAdresse4CountryCode(operation.getAdresse4CountryCode());
		cloned.setAdresse4DestinataireNom(operation.getAdresse4DestinataireNom());
		cloned.setAdresse4DestinataireTelephone(operation.getAdresse4DestinataireTelephone());
		cloned.setAdresse4Rue(operation.getAdresse4Rue());
		cloned.setAdresse4Ville(operation.getAdresse4Ville());

		cloned.setAdresse5Complement(operation.getAdresse5Complement());
		cloned.setAdresse5Complete(operation.getAdresse5Complete());
		cloned.setAdresse5CountryCode(operation.getAdresse5CountryCode());
		cloned.setAdresse5DestinataireNom(operation.getAdresse5DestinataireNom());
		cloned.setAdresse5DestinataireTelephone(operation.getAdresse5DestinataireTelephone());
		cloned.setAdresse5Rue(operation.getAdresse5Rue());
		cloned.setAdresse5Ville(operation.getAdresse5Ville());
*/
		cloned.setArriveeAdresseComplement(operation.getArriveeAdresseComplement());
		cloned.setArriveeAdresseComplete(operation.getArriveeAdresseComplete());
		cloned.setArriveeAdresseCountryCode(operation.getArriveeAdresseCountryCode());
		cloned.setArriveeAdresseLatitude(operation.getArriveeAdresseLatitude());
		cloned.setArriveeAdresseLongitude(operation.getArriveeAdresseLongitude());
		cloned.setArriveeAdresseRue(operation.getArriveeAdresseRue());
		cloned.setArriveeAdresseVille(operation.getArriveeAdresseVille());

		cloned.setAssurance(operation.getAssurance());
		cloned.setCaracteristiquesVehicule(operation.getCaracteristiquesVehicule());
		cloned.setCategorieVehicule(operation.getCategorieVehicule());
		cloned.setClient(operation.getClient());
		cloned.setClient_personnel(operation.getClient_personnel());
		cloned.setCodePays(operation.getCodePays());
		cloned.setCourseGeolocalisee(operation.isCourseGeolocalisee());
		cloned.setCreatedOn(new Date());
		cloned.setCreePar(operation.getCreePar());
		cloned.setInformationsComplementaires(operation.getInformationsComplementaires());
		cloned.setObservationsParClient(operation.getObservationsParClient());
		cloned.setPrixSouhaiteParClient(operation.getPrixSouhaiteParClient());
		cloned.setPrixSouhaiteParClientDevise(operation.getPrixSouhaiteParClientDevise());
		cloned.setServicesAdditionnels(operation.getServicesAdditionnels());
		cloned.setStatutTempsReel(operation.isStatutTempsReel());
		cloned.setTonnageVehicule(operation.getTonnageVehicule());
		cloned.setTypeMarchandise(operation.getTypeMarchandise());
		cloned.setUpdatedOn(new Date());

		cloned.setRecurrenceOperationOriginel(operation);

		// génération du code
		Long max_code = operationRepository.getMaxCode();
		Long code = max_code + new Long(1);
		cloned.setCode(code);

		// enregistrement des étapes au préalable
		if (cloned.getEtapes() != null && !cloned.getEtapes().isEmpty()) {
			etapeRepository.saveAll(cloned.getEtapes());
		}

		// enregistrement en bdd
		cloned = operationRepository.save(cloned);

		logger.info("dupliquer1");

		// enregistrement de la trace d'audit
		logger.info("cloned.getCodePays() = " + cloned.getCodePays());
		actionAuditService.creerOperationParDuplication(cloned, cloned.getCodePays());
		logger.info("dupliquer2");

		// duplication des PJ
		dupliquerOperationDocuments(cloned);
		logger.info("dupliquer3");

		return cloned;

	}

	@Override
	public void dupliquerOperationDocuments(Operation cloned) {
		logger.info("dupliquerOperationDocuments");
		List<OperationDocument> liste_documents = operationDocumentService.getOperationDocuments(cloned.getRecurrenceOperationOriginel());

		logger.info("dupliquerOperationDocuments4 = " + liste_documents.size());
		if (liste_documents != null && !liste_documents.isEmpty()) {
			int cpt_docs = 0;
			for (OperationDocument document : liste_documents) {
				logger.info("dupliquerOperationDocuments5 = " + document.getUuid().toString());
				//document.
				byte[] document_array_bytes = operationDocumentService.get(document.getUuid().toString());
				operationDocumentService.saveDocument(cloned, document_array_bytes, cpt_docs, document.getFilename(), document.getType_compte());
				cpt_docs++;
			}
		}


	}


	@Override
	public void listeOperationsRapportMensuel(Date date) {

		// récupère toutes les opérations pas encore terminées et les opérations qui se sont terminées aujourd'hui.
		List<UtilisateurClient> clients_utilisateur = utilisateurClientService.getAll();
		List<UtilisateurClientPersonnel> clients_personnels = utilisateurClientPersonnelService.getAll();

		// va chercher les opérations des UtilisateurClient, compte les opérations dans les 3 niveaux (Op. programmées, Op. en cours, Op. réalisées) et envoi le mail
		// puis fait pareil avec les opérations des UtilisateurClientPersonnel

		for (UtilisateurClient client_utilisateur : clients_utilisateur) {
			Client client = clientService.getByUtilisateur(client_utilisateur, client_utilisateur.getCodePays());
			Long nb_operations_programmees = operationRepository.countNbOperationsProgrammeesUtilisateur(client, client_utilisateur.getCodePays());
			Long nb_operations_en_cours = operationRepository.countNbOperationsEnCoursUtilisateur(client, client_utilisateur.getCodePays());
			Long nb_operations_terminees = operationRepository.countNbOperationsTermineesUtilisateur(client, client_utilisateur.getCodePays());

			emailToSendService.envoyerRapportHeboduClient(nb_operations_programmees, nb_operations_en_cours, nb_operations_terminees, client_utilisateur.getEmail(), client_utilisateur.getPrenomNom(), client_utilisateur.getCodePays(), client_utilisateur);

		}

		for (UtilisateurClientPersonnel client_utilisateur : clients_personnels) {
			Long nb_operations_programmees = operationRepository.countNbOperationsProgrammees(client_utilisateur, client_utilisateur.getCodePays());
			Long nb_operations_en_cours = operationRepository.countNbOperationsEnCours(client_utilisateur, client_utilisateur.getCodePays());
			Long nb_operations_terminees = operationRepository.countNbOperationsTerminees(client_utilisateur, client_utilisateur.getCodePays());

			emailToSendService.envoyerRapportHeboduClient(nb_operations_programmees, nb_operations_en_cours, nb_operations_terminees, client_utilisateur.getEmail(), client_utilisateur.getPrenomNom(), client_utilisateur.getCodePays(), client_utilisateur);

		}

	}


	@Override
	public void listeOperationsRapportJournalier(Date date) {

		Calendar journee_debut = Calendar.getInstance();
		journee_debut.setTime(date);
		journee_debut.set(Calendar.HOUR_OF_DAY, 0);
		journee_debut.set(Calendar.MINUTE, 0);
		journee_debut.set(Calendar.SECOND, 0);

		Calendar c = Calendar.getInstance();
		c.setTime(journee_debut.getTime());
		c.add(Calendar.DATE, 1);  // number of days to add

		// récupère toutes les opérations pas encore terminées et les opérations qui se sont terminées aujourd'hui.
		List<Operation> operations_ = operationRepository.findOperationsPasTermineesEtTermineesAujourdui(journee_debut.getTime(), c.getTime());
		logger.info("nb opérations = " + operations_.size());

		ListOperation operations = new ListOperation();
		for (Operation o : operations_) {
			operations.add(o);
		}

		// ventilation par client et client utilisateur
		Map<String, ListOperation> emailclient_listeoperations = new HashMap<String, ListOperation>();
		for (Operation operation : operations) {
			if (operation.getClient() != null) {
				String email = operation.getClient().getUtilisateur().getEmail();
				if (operation.getClient_personnel() != null) {
					email = operation.getClient_personnel().getEmail();
				}
				if (!emailclient_listeoperations.containsKey(email)) {
					emailclient_listeoperations.put(email, new ListOperation());
				}
				ListOperation operations_du_client = emailclient_listeoperations.get(email);
				operations_du_client.add(operation);
				emailclient_listeoperations.put(email, operations_du_client);
			}
		}
		logger.info("nb clients = " + emailclient_listeoperations.size());

		Iterator<Map.Entry<String, ListOperation>> iter = emailclient_listeoperations.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, ListOperation> next = iter.next();
			String client_email = next.getKey();
			String client_nom = operations.get(0).getClient().getUtilisateur().getPrenomNom();
			if (operations.get(0).getClient_personnel() != null) {
				client_nom = operations.get(0).getClient_personnel().getPrenomNom();
			}

			logger.info("client " + client_email);
			for (Operation operation : next.getValue()) {
				logger.info("  operation " + operation.getCode() + " - " + operation.getStatut());
			}

			emailToSendService.envoyerRapportJournalierClient(next.getValue(), client_email, client_nom, operations.get(0).getCodePays());


		}


	}

	@Override
	public Operation dupliquerOperationReccurente(Operation operation, Date date_programmee_operation) {
		logger.info("dupliquerOperationReccurente");

		int nbCamions = 1;
		if (operation.getNbCamions() != null) {
			nbCamions = operation.getNbCamions();
		}
		Operation operation2 = null;
		logger.info("dupliquerOperationReccurente nbCamions=" + nbCamions);
		for (int i=1; i<=nbCamions; i++) {
			Operation op = dupliquerOperationReccurenteInterne(operation, date_programmee_operation);
			if (operation2 == null) {
				operation2 = op;
			}

		}
		return operation2;

	}

	@Override
	public List<Operation> getOperationsTransporteurEnCours(String order_dir, int page_number, int page_size,
			UtilisateurDriver transporteur , UtilisateurProprietaire proprietaire, List<Vehicule> vehicules, boolean uniquement_a_venir, boolean uniquement_en_cours, boolean uniquement_terminees, String code_pays) {

		List<Operation> operations = new ArrayList<Operation>();

		// recherche toutes les operations non termines
		if (!uniquement_a_venir && !uniquement_en_cours && !uniquement_terminees) {
			if (transporteur == null) {
				operations = operationRepository.getOperationsNonTermines(vehicules, code_pays);
			} else {
				operations = operationRepository.getOperationsNonTermines(vehicules, transporteur, code_pays);
			}
		} else if (uniquement_a_venir) {
			if (transporteur == null) {
				operations = operationRepository.getOperationsAVenir(vehicules, code_pays);
			} else {
				operations = operationRepository.getOperationsAVenir(vehicules, transporteur, code_pays);
			}
		} else if (uniquement_en_cours) {
			if (transporteur == null) {
				operations = operationRepository.getOperationsEnCours(vehicules, code_pays);
			} else {
				operations = operationRepository.getOperationsEnCours(vehicules, transporteur, code_pays);
			}
		} else if (uniquement_terminees) {
			if (transporteur == null) {
				operations = operationRepository.getOperationsTermineesParDriver(vehicules, code_pays);
			} else {
				operations = operationRepository.getOperationsTermineesParDriver(vehicules, transporteur, code_pays);
			}
		}

		return operations;
		
	}

	@Override
	public long countOperationsClient(Client client, String code_pays) {
		return operationRepository.countOperationsClient(client, code_pays);
	}

	@Override
	public long countOperationsDriver(UtilisateurDriver driver, String code_pays) {
		return operationRepository.countOperationsDriver(driver, code_pays);
	}

	@Override
	public long countNbOperationsProgrammees(List<Vehicule> vehicules, String code_pays) {
		return operationRepository.countNbOperationsAVenir(vehicules, code_pays);
	}

	@Override
	public long countNbOperationsProgrammees(List<Vehicule> vehicules, String code_pays, UtilisateurDriver driver) {
		return operationRepository.countNbOperationsAVenir(vehicules, code_pays, driver);
	}

	@Override
	public long countNbOperationsEnCours(List<Vehicule> vehicules, String code_pays) {
		long nb = operationRepository.countNbOperationsEnCours(vehicules, code_pays);
		return nb;
	}

	@Override
	public long countNbOperationsEnCours(List<Vehicule> vehicules, String code_pays, UtilisateurDriver driver) {
		long nb = operationRepository.countNbOperationsEnCours(vehicules, code_pays, driver);
		return nb;
	}

	@Override
	public List<Operation> getOperationsNumeroFactureClient(String numeroFacture, String code_pays) {
		return operationRepository.getOperationsNumeroFactureClient(numeroFacture, code_pays);
	}

	@Override
	public List<Operation> getOperationsNumeroFactureProprietaire(String numeroFacture, String code_pays) {
		return operationRepository.getOperationsNumeroFactureProprietaire(numeroFacture, code_pays);
	}


	@Override
	public void setNullOperationsNumeroFactureClient(String numeroFacture, String code_pays) {
		List<Operation> operations = getOperationsNumeroFactureClient(numeroFacture, code_pays);
		for (Operation operation : operations) {
			operation.setFacture(null);
			operationService.save(operation);
		}
	}

	@Override
	public void setNullOperationsNumeroFactureProprietaire(String numeroFacture, String code_pays) {
		List<Operation> operations = getOperationsNumeroFactureProprietaire(numeroFacture, code_pays);
		for (Operation operation : operations) {
			operation.setFactureProprietaire(null);
			operationService.save(operation);
		}
	}


}
