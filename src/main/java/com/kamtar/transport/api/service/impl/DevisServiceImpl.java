package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.enums.DevisStatut;
import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.enums.OperationStatut;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.swagger.ListOperation;
import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.RRulesUtils;
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
import java.util.*;


@Service(value="DevisService")
public class DevisServiceImpl implements DevisService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DevisServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	EmailToSendService emailToSendService;

	@Autowired
	private SMSService smsService;

	@Autowired
	private DevisChangementStatutService devisChangementStatutService;

	@Autowired
	private UtilisateurClientService utilisateurClientService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private OperationChangementStatutService operationChangementStatutService;

	@Autowired
	private OperationService operationService;

	@Autowired
	private VehiculeCarrosserieRepository vehiculeCarrosserieRepository;

	@Autowired
	private VehiculeTonnageRepository vehiculeTonnageRepository;

	@Autowired
	private MarchandiseTypeRepository marchandiseTypeRepository;

	@Autowired
	private DevisRepository devisRepository;

	@Autowired
	private EtapeRepository etapeRepository;

	@Autowired
	private CountryRepository countryRepository; 

	@Autowired
	private ActionAuditService actionAuditService;


	@Override
	public Devis getByUUID(String uuid, String code_pays) {
		try {
			Devis u = devisRepository.findByUUID(UUID.fromString(uuid), code_pays);
			return u;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}


	@Override
	public Page<Devis> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Devis> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return devisRepository.findAll(conditions, pageable);
	}


	@Override
	public Long countAll(Specification<Devis> conditions) {
		return devisRepository.count(conditions);
	}



	@Override
	public boolean delete(Devis devis, String code_pays) {
		devis.setEtapes(null);
		devisRepository.delete(devis);
		return true;
	}

	public Operation create(CreateDevisParams params, String code_pays) {

		logger.info("params.getDepartDateProgrammeeOperation()1 = " + params.getDepartDateProgrammeeOperation());
		if (params.getDepartDateProgrammeeOperation() != null) {
			Calendar date = new GregorianCalendar();
			date.setTime(params.getDepartDateProgrammeeOperation());
			date.add(Calendar.DAY_OF_MONTH, 1);
			date.set(Calendar.HOUR_OF_DAY, 23);
			date.set(Calendar.MINUTE, 59);
			date.set(Calendar.SECOND, 59);
			logger.info("params.getDepartDateProgrammeeOperation()2 = " + date.getTime());
			if (date.getTime().before(new Date())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas demander un devis pour une date passée");
			}
		}


		// génération du code
		Long max_code = devisRepository.getMaxCode();
		if (max_code == null) {
			max_code = new Long(0);
		}
		Long code = max_code + new Long(1);

		MarchandiseType marchandise_type = null;
		VehiculeCarrosserie vehicule_carosserie = null;
		VehiculeTonnage vehicule_tonnage = null;


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

		// enregistre les étapes en bdd, puis le devis
		Devis devis = new Devis(params, code, marchandise_type, vehicule_tonnage, code_pays);

		int cpt = 0;
		List<Etape> etapes_saved = new ArrayList<Etape>();
		for (Etape etape : devis.getEtapes()) {
			Etape etape_enregistree = etapeRepository.save(etape);
			etapes_saved.add(etape_enregistree);
		}
		devis.setEtapes(etapes_saved);
		devis = devisRepository.save(devis);

		DevisChangementStatut operation_changement = new DevisChangementStatut(null, devis.getStatut(), devis, null, null, null, devis.getCodePays());
		devisChangementStatutService.create(operation_changement);

		actionAuditService.creerDevis(devis, code_pays);

		// transformation du devis en opération
		Operation operation = convertionEnOperation(devis, code_pays);

		// envoi d'un email
		ListOperation operations = new ListOperation();
		operations.add(operation);

		// envoi d'un email
		emailToSendService.prevenirKamtarNouveauDevis(operations, code_pays);
		//emailToSendService.prevenirKamtarNouvelleCommandeClient(operations, code_pays, 1);

		// envoi SMS
		//smsService.previensKamtarNouvelleOperationClient(devis);
		smsService.previensKamtarNouvelleOperationClient(operations);

		return operation;
	}

	@Override
	public Devis passerAEnCoursDeTraitement(Devis devis, String code_pays) {
		logger.info("passerAEnCoursDeTraitement1 " + devis.getCode());

		if (!devis.getStatut().toString().equals(DevisStatut.ENREGISTRE.toString())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas passer ce devis au statut 'En cours de traitement'");
		}

		String nouveau_statut = DevisStatut.EN_COURS_DE_TRAITEMENT.toString();
		DevisChangementStatut devis_changement = new DevisChangementStatut(devis.getStatut(), nouveau_statut, devis, null, null, null, devis.getCodePays());
		devisChangementStatutService.create(devis_changement);

		devis.setStatut(nouveau_statut);
		devis = devisRepository.save(devis);

		return devis;
	}

	@Override
	public Devis passerAReponseImpossible(Devis devis, String code_pays) {

		if (!devis.getStatut().toString().equals(DevisStatut.ENREGISTRE.toString())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas passer ce devis au statut 'Réponse impossible'");
		}

		// enregistrement du changement de statut du devis
		String nouveau_statut = DevisStatut.REPONSE_IMPOSSIBLE.toString();
		DevisChangementStatut operation_changement = new DevisChangementStatut(devis.getStatut(), nouveau_statut, devis, null, null, null, devis.getCodePays());
		devisChangementStatutService.create(operation_changement);

		// enregistrement du nouveau statut
		devis.setStatut(nouveau_statut);
		devis = devisRepository.save(devis);

		return devis;

	}

	@Override
	public Operation convertionEnOperation(Devis devis, String code_pays) {
		logger.info("convertionEnOperation1 " + devis.getCode());

		if (!devis.getStatut().toString().equals(DevisStatut.ENREGISTRE.toString()) && !devis.getStatut().toString().equals(DevisStatut.EN_COURS_DE_TRAITEMENT.toString())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas convertir ce devis en opération");
		}

		// création du compte client
		String mot_de_passe = StringUtils.generateRandomAlphaNumericString(8);
		UtilisateurClient utilisateur_client = new UtilisateurClient(devis, mot_de_passe);
		utilisateur_client = utilisateurClientService.save(utilisateur_client);
		Client client = new Client(devis, mot_de_passe, utilisateur_client);
		client = clientService.save(client);

		// enregistrement du changement de statut du devis
		String nouveau_statut = DevisStatut.PASSE_EN_OPERATION.toString();
		DevisChangementStatut devis_changement = new DevisChangementStatut(devis.getStatut(), nouveau_statut, devis, null, null, null, devis.getCodePays());
		devisChangementStatutService.create(devis_changement);

		// enregistrement du nouveau statut
		devis.setStatut(nouveau_statut);
		devis = devisRepository.save(devis);

		// génération du code de l'opération
		Long max_code = operationRepository.getMaxCode();
		if (max_code == null) {
			max_code = new Long(0);
		}
		Long code = max_code + new Long(1);

		Operation operation = new Operation(devis, code, client, OperationStatut.ENREGISTRE.toString());

		int cpt = 0;
		List<Etape> etapes_saved = new ArrayList<Etape>();
		for (Etape etape : devis.getEtapes()) {
			Etape etape_enregistree = etapeRepository.save(etape);
			etapes_saved.add(etape_enregistree);
		}
		operation.setEtapes(etapes_saved);

		operation = operationService.save(operation);

		OperationChangementStatut operation_changement = new OperationChangementStatut(null, operation.getStatut(), operation, null, null, null, operation.getCodePays());
		operationChangementStatutService.create(operation_changement);

		actionAuditService.creerOperation(operation, code_pays, operation.getClient().getUtilisateur().getUuid().toString(), operation.getClient().getUtilisateur().getPrenom(), operation.getClient().getUtilisateur().getNom());


		return operation;


	}



}
