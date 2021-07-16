package com.kamtar.transport.api.service.impl;

import java.util.List;
import java.util.UUID;

import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.EditClientPublicParams;
import com.kamtar.transport.api.service.*;
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

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.params.CreateClientAnonymeParams;
import com.kamtar.transport.api.params.CreateClientParams;
import com.kamtar.transport.api.params.EditClientParams;
import com.kamtar.transport.api.repository.ClientRepository;
import com.kamtar.transport.api.repository.MotDePassePerduRepository;
import com.kamtar.transport.api.repository.UtilisateurClientRepository;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="ClientService")
public class ClientServiceImpl implements ClientService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ClientServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private UtilisateurClientRepository utilisateurClientRepository; 

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private EmailToSendService emailToSendService;

	@Autowired
	private UtilisateurClientRepository clientUtilisateurRepository;

	@Autowired
	private UtilisateurOperateurKamtarService utilisateurOperateurKamtarService; 

	@Autowired
	private MotDePassePerduRepository motDePasseRepository;

	@Autowired
	private SMSService smsService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ClientPhotoService clientPhotoService;


	public Client create(CreateClientParams params, String token) {

		UtilisateurOperateurKamtar operateur = utilisateurOperateurKamtarService.getByUUID(jwtProvider.getUUIDFromJWT(token).toString(), jwtProvider.getCodePays(token));

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail_responsable() != null && !"".equals(params.getEmail_responsable()) && clientUtilisateurRepository.existEmail(params.getEmail_responsable(), jwtProvider.getCodePays(token))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone1_responsable() != null && !"".equals(params.getNumero_telephone1_responsable()) && clientUtilisateurRepository.telephone1Exist(params.getNumero_telephone1_responsable(), jwtProvider.getCodePays(token))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// enregistre l'utilisateur
		UtilisateurClient utilisateur = new UtilisateurClient(params);
		utilisateurClientRepository.save(utilisateur);

		// enregistre le user dans la base de données
		Client user = new Client(params, operateur);
		user.setUtilisateur(utilisateur);
		user = clientRepository.save(user);


		return user;
	}


	public Client create(CreateClientAnonymeParams params) {

		if (params.getType_compte().equals("B")) {
			if (params.getEntreprise_nom() == null || params.getEntreprise_nom().trim().length() == 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous devez renseigner le nom de l'entreprise");
			}
		}

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail() != null && !"".equals(params.getEmail()) && clientUtilisateurRepository.existEmail(params.getEmail(), params.getCode_pays())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getTelephone1() != null && !"".equals(params.getEmail()) && clientUtilisateurRepository.telephone1Exist(params.getTelephone1(), params.getCode_pays())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// enregistre l'utilisateur
		UtilisateurClient utilisateur = new UtilisateurClient(params);
		utilisateurClientRepository.save(utilisateur);

		// enregistre le user dans la base de données
		Client client = new Client(params);
		client.setUtilisateur(utilisateur);
		client = clientRepository.save(client);

		// enregistrement de la photo de profil
		enregistrePhotoProfil(utilisateur, params.getPhotoProfil());

		// envoi de la notification au backoffice
		Notification notification = new Notification(NotificationType.WEB_BACKOFFICE.toString(), null, "Nouveau compte expéditeur", null, client.getUuid().toString(), client.getCodePays());
		notificationService.create(notification, params.getCode_pays());

		// envoi d'un email
		emailToSendService.prevenirKamtarNouveauCompteClient(client, client.getCodePays());

		// envoi du SMS au client
		smsService.validerCompteClient(utilisateur);

		return client;
	}

	@Override
	public List<Client> autocomplete(String query, String code_pays) {
		List<Client> res = clientRepository.filterByNom(query, code_pays);
		return res;
	}



	@Override
	public Client getByUUID(String uuid, String pays) {
		try {
			Client u = clientRepository.findByUUID(UUID.fromString(uuid), pays);
			return u;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public boolean emailExist(String email, String code_pays) {
		return clientRepository.existEmail(email, code_pays);
	}

	@Override
	public boolean numeroDeTelephoneExist(String email, String pays) {
		return clientRepository.telephone1Exist(email, pays);
	}

	@Override
	public List<Client> getAll() {
		return clientRepository.findAll(null);
	}


	@Override
	public Page<Client> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Client> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return clientRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Client> conditions) {
		return clientRepository.count(conditions);
	}

	@Override
	public Client save(Client user) {
		return clientRepository.save(user);
	}

	@Override
	public boolean update(EditClientParams params, Client user, String code_pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail_responsable() != null && !"".equals(params.getEmail_responsable()) && clientUtilisateurRepository.existEmailForotherUser(params.getEmail_responsable(), user.getUtilisateur(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone1_responsable() != null && !"".equals(params.getNumero_telephone1_responsable()) && clientUtilisateurRepository.telephone1ExistForOtherUser(params.getNumero_telephone1_responsable(), user.getUtilisateur(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}


		// enregistre l'expéditeur
		user.edit(params);
		clientRepository.save(user);

		// enregistre l'utilisateur
		UtilisateurClient utilisateur = user.getUtilisateur();

		if (utilisateur == null) {
			utilisateur = new UtilisateurClient(params);
			user.setUtilisateur(utilisateur);
			clientRepository.save(user);
		} else {
			utilisateur.edit(params);
		}

		utilisateurClientRepository.save(utilisateur);

		return true;
	}


	@Override
	public boolean update(EditClientPublicParams params, Client user, String code_pays) {

		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getEmail_responsable() != null && !"".equals(params.getEmail_responsable()) && clientUtilisateurRepository.existEmailForotherUser(params.getEmail_responsable(), user.getUtilisateur(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'adresse e-mail est déjà utilisée.");
		}
		// vérifie si l'email ou le numéro de téléphone ne sont pas déjà utilisé
		if (params.getNumero_telephone1_responsable() != null && !"".equals(params.getNumero_telephone1_responsable()) && clientUtilisateurRepository.telephone1ExistForOtherUser(params.getNumero_telephone1_responsable(), user.getUtilisateur(), code_pays)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de téléphone est déjà utilisé.");
		}

		// enregistre l'expéditeur
		user.edit(params);
		clientRepository.save(user);

		// enregistre l'utilisateur
		UtilisateurClient utilisateur = user.getUtilisateur();
		if (utilisateur == null) {
			utilisateur = new UtilisateurClient(params);
			user.setUtilisateur(utilisateur);
			clientRepository.save(user);
		} else {
			utilisateur.edit(params);
		}

		utilisateurClientRepository.save(utilisateur);

		return true;
	}

	@Override
	public Client getByUtilisateur(UtilisateurClient utilisateur, String pays) {
		return clientRepository.findByUtilisateur(utilisateur, pays);
	}

	@Override
	public UtilisateurClient getByValidationCode(String validation, String telephone, String code_pays) {
		return clientUtilisateurRepository.findByCodeValidation(validation, telephone, code_pays);

	}


	@Override
	public boolean delete(Client client, String code_pays) {

		motDePasseRepository.setNullClient(client.getUtilisateur(), code_pays);
		clientUtilisateurRepository.delete(client.getUtilisateur());
		clientRepository.delete(client);
		
		return true;
	}

	public void enregistrePhotoProfil(UtilisateurClient user, String base64) {

		// enregistre la photo
		if (base64 != null && !"".equals(base64)) {
			clientPhotoService.savePhotoProfil(user, base64);
		}
	}


}
