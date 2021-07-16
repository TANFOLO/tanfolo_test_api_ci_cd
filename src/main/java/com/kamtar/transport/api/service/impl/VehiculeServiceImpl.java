package com.kamtar.transport.api.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.*;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.security.SecurityUtils;
import com.kamtar.transport.api.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.utils.JWTProvider;
import com.kamtar.transport.api.utils.UpdatableBCrypt;


@Service(value="VehiculeService")
public class VehiculeServiceImpl implements VehiculeService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(VehiculeServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private VehiculeRepository vehiculeRepository;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private EmailToSendService emailToSendService;

	@Autowired
	private UtilisateurProprietaireService utilisateurProprietaireService; 

	@Autowired
	private UtilisateurDriverService utilisateurDriverService; 

	@Autowired
	private CountryRepository countryRepository; 

	@Autowired
	private ActionAuditService actionAuditService; 

	@Autowired
	private VehiculePhotoService vehiculePhotoService; 

	@Autowired
	private UtilisateurProprietaireRepository utilisateurProprietaireRepository; 

	@Autowired
	private UtilisateurDriverRepository utilisateurDriverRepository; 

	@Value("${generic_password}")
	private String generic_password;

	public Vehicule create(CreateComptePublicParams postBody1, String code_pays) {

		UtilisateurDriver driver = utilisateurDriverService.login(postBody1.getChauffeur_numero_telephone_1(), postBody1.getChauffeur_password(), postBody1.getPays());
		if (driver == null) {
			driver = utilisateurDriverService.createUser(postBody1, code_pays);
		}
		UtilisateurProprietaire proprietaire = utilisateurProprietaireService.login(postBody1.getProprietaire_numero_telephone_1(), postBody1.getProprietaire_password(), postBody1.getPays());
		if (proprietaire == null) {
			proprietaire = utilisateurProprietaireService.createUser(postBody1, code_pays);
		}

		Country immatriculation_pays = null;
		if (postBody1.getImmatriculationPays() != null) {
			Optional<Country> immatriculation_pays_optional = countryRepository.findByCode(postBody1.getImmatriculationPays());
			if (immatriculation_pays_optional.isPresent()) {
				immatriculation_pays = immatriculation_pays_optional.get();
			}
		}

		// enregistre le user dans la base de données
		Vehicule vehicule = new Vehicule(postBody1, proprietaire, driver, immatriculation_pays);
		vehicule = vehiculeRepository.save(vehicule);

		// enregistrement de la photo en fichier
		vehiculePhotoService.savePhotoAssurance(vehicule, postBody1.getPhotoAssurance());

		// enregistrement de la photo en fichier
		vehiculePhotoService.savePhotoCarteGrise(vehicule, postBody1.getPhotoAssurance());

		// enregistre les photos du carroussel
		vehiculePhotoService.savePhoto(vehicule, postBody1.getPhotoAvant(), Integer.valueOf(1)) ;
		vehiculePhotoService.savePhoto(vehicule, postBody1.getPhotoArriere(), Integer.valueOf(2)) ;
		vehiculePhotoService.savePhoto(vehicule, postBody1.getPhotoCote(), Integer.valueOf(3)) ;


		return vehicule;
	}

	public Vehicule create(CreateVehiculeParams params, UtilisateurOperateurKamtar operateur, String token) {

		String code_pays = jwtProvider.getCodePays(token);

		// chargement des objets
		UtilisateurProprietaire proprietaire = null;
		if (params.getProprietaire() != null) {
			proprietaire = utilisateurProprietaireRepository.findByUUID(UUID.fromString(params.getProprietaire()), code_pays);

			if (SecurityUtils.proprietaire(jwtProvider, token)) {
				UtilisateurProprietaire proprietaire2 = utilisateurProprietaireRepository.findByUUID(jwtProvider.getUUIDFromJWT(token), code_pays);
				if (!proprietaire.equals(proprietaire2)) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous ne pouvez pas attacher un véhicule à un autre propriétaire.");
				}
			}

		}
		Country immatriculation_pays = null;
		if (params.getImmatriculationPays() != null) {
			Optional<Country> immatriculation_pays_optional = countryRepository.findByCode(params.getImmatriculationPays());
			if (immatriculation_pays_optional.isPresent()) {
				immatriculation_pays = immatriculation_pays_optional.get();
			}
		}
		UtilisateurDriver driver = null;
		if (params.getDriverPrincipal() != null) {
			driver = utilisateurDriverRepository.findByUUID(UUID.fromString(params.getDriverPrincipal()), code_pays);
		}

		// enregistre le véhicule dans la base de données
		Vehicule vehicule = new Vehicule(params, operateur, proprietaire, driver, immatriculation_pays);
		vehicule = vehiculeRepository.save(vehicule);

		// envoi une notification à Kamtar si c'est le propriétaire qui ajoute un driver
		if (proprietaire != null) {

			// envoi de la notification au backoffice
			Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), "Nouveau véhicule créé par un propriétaire", proprietaire.getUuid().toString(), vehicule.getUuid().toString(), code_pays, true);
			notificationService.create(notification, code_pays);

			// envoi d'un email
			emailToSendService.prevenirKamtarNouveauVehiculeCreeParProprietaire(vehicule, code_pays);

		}



		// enregistrement des photos
		vehiculePhotoService.savePhotoPrincipale(vehicule, params.getPhotoPrincipale());
		vehiculePhotoService.savePhotoAssurance(vehicule, params.getDocumentAssurance());
		vehiculePhotoService.savePhotoCarteGrise(vehicule, params.getDocumentCarteGrise());

		// enregistre les photos du carroussel
		String folderUuid = params.getFolderUuid();
		if (folderUuid != null && !"".equals(folderUuid)) {
			String tempDir = System.getProperty("java.io.tmpdir");
			File folderTempUUID = new File(tempDir + "/" + params.getFolderUuid());
			if (folderTempUUID.exists()) {
				// on envoit toutes les photos sur s3
				vehiculePhotoService.savePhotos(vehicule, folderTempUUID);
				folderTempUUID.delete();
			}
		}


		return vehicule;
	}



	@Override
	public Vehicule getByUUID(String uuid, String code_pays) {
		try {
			Vehicule u = vehiculeRepository.findByUUID(UUID.fromString(uuid), code_pays);
			return u;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}


	@Override
	public Page<Vehicule> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Vehicule> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return vehiculeRepository.findAll(conditions, pageable);
	}


	@Override
	public Long countAll(Specification<Vehicule> conditions) {
		return vehiculeRepository.count(conditions);
	}

	@Override
	public boolean update(EditVehiculeParams params, Vehicule vehicule, String code_pays) {

		// chargement des objets
		UtilisateurProprietaire proprietaire = null;
		if (params.getProprietaire() != null) {
			proprietaire = utilisateurProprietaireRepository.findByUUID(UUID.fromString(params.getProprietaire()), code_pays);
		}
		Country immatriculation_pays = null;
		if (params.getImmatriculationPays() != null) {
			Optional<Country> immatriculation_pays_optional = countryRepository.findByCode(params.getImmatriculationPays());
			if (immatriculation_pays_optional.isPresent()) {
				immatriculation_pays = immatriculation_pays_optional.get();
			}
		}
		UtilisateurDriver driver = null;
		if (params.getDriverPrincipal() != null) {
			driver = utilisateurDriverRepository.findByUUID(UUID.fromString(params.getDriverPrincipal()), code_pays);
		}

		vehicule.edit(params, proprietaire, driver, immatriculation_pays);
		vehiculeRepository.save(vehicule);

		// enregistrement de la photo en fichier
		vehiculePhotoService.savePhotoPrincipale(vehicule, params.getPhotoPrincipale());

		// enregistrement de la photo en fichier
		vehiculePhotoService.savePhotoAssurance(vehicule, params.getDocumentAssurance());

		// enregistrement de la photo en fichier
		vehiculePhotoService.savePhotoCarteGrise(vehicule, params.getDocumentCarteGrise());

		// enregistre les photos du carroussel
		String folderUuid = params.getFolderUuid();
		if (folderUuid != null && !"".equals(folderUuid)) {
			String tempDir = System.getProperty("java.io.tmpdir");
			File folderTempUUID = new File(tempDir + "/" + params.getFolderUuid());
			if (folderTempUUID.exists()) {
				// on envoit toutes les photos sur s3
				vehiculePhotoService.savePhotos(vehicule, folderTempUUID);
				folderTempUUID.delete();
			}
		}

		return true;
	}

	@Override
	public boolean update_disponibilite(DisponibiliteVehiculeParams params, Vehicule vehicule, String pays) {

		vehicule.setDisponible(Integer.valueOf(params.getDisponibilite()));
		vehiculeRepository.save(vehicule);

		return true;
	}

	@Override
	public List<Vehicule> getByProprietaire(UtilisateurProprietaire proprietaire, String code_pays) {
		return vehiculeRepository.findByProprietaire(proprietaire, code_pays);
	}

	@Override
	public List<Vehicule> getByCarrosseries(String carrosserie, String code_pays) {
		return vehiculeRepository.findByCarrosserie(carrosserie, code_pays);
	}


	@Override
	public boolean delete(Vehicule vehicule, String code_pays) {

		// impossible de supprimer si le véhicule est déjà attaché à une opération
		long nb = operationRepository.countOperationAvecVehicule(vehicule, code_pays);
		if (nb > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas supprimer ce véhicule car il est utilisé dans au moins une opération");
		}

		vehiculeRepository.delete(vehicule);
		return true;
	}

	@Override
	public boolean immatriculationExist(String code, String pays) {
		return vehiculeRepository.immatriculationExist(code, pays);
	}

	@Override
	public List<Vehicule> autocomplete(String query, String code_pays) {
		List<Vehicule> res = vehiculeRepository.filterByNom(query, code_pays);
		return res;
	}

	@Override
	public void setVehiculesIndispo() {
		vehiculeRepository.setVehiculesIndispo();
	}

	@Override
	public Long countOperationsVehiculesDriver(UtilisateurDriver transporteur, String code_pays) {
		return vehiculeRepository.countVehiculesDriver(transporteur, code_pays);
	}

	@Override
	public Long countOperationsVehiculesProprietaire(UtilisateurProprietaire proprietaire, String code_pays) {
		return vehiculeRepository.countVehiculesProprietaire(proprietaire, code_pays);
	}

	@Override
	public Vehicule signin(SigninImmatriculationParams params, String code_pays) {

		Vehicule vehicule = vehiculeRepository.siginin(params.getImmatriculation(), code_pays);
		if (vehicule == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plaque d'immatriculation introuvable");
		}
		if (!vehicule.isActivate()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le véhicule est désactivé");
		}

		return vehicule;

	}


}
