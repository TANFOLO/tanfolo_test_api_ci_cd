package com.kamtar.transport.api.service.impl;

import com.kamtar.transport.api.enums.ReclamationStatut;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.CreateReclamationEchangeParams;
import com.kamtar.transport.api.params.CreateReclamationParams;
import com.kamtar.transport.api.params.EditReclamationParams;
import com.kamtar.transport.api.repository.*;
import com.kamtar.transport.api.service.*;
import com.kamtar.transport.api.utils.JWTProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service(value="ReclamationEchangeService")
public class ReclamationEchangeServiceImpl implements ReclamationEchangeService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ReclamationEchangeServiceImpl.class);

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private ReclamationRepository reclamationRepository;

	@Autowired
	private ReclamationEchangeRepository reclamationEchangeRepository;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private UtilisateurClientService utilisateurClientService;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private UtilisateurClientPersonnelRepository utilisateurClientPersonnelRepository;

	@Autowired
	private UtilisateurDriverRepository transporteurRepository;

	@Autowired
	private ClientService clientService;

	@Autowired
	private ReclamationEchangeService reclamationEchangeService;

	@Autowired
	private UtilisateurClientPersonnelService utilisateurClientPersonnelService;

	@Autowired
	private OperationService operationService;

	@Autowired
	private ActionAuditService actionAuditService;

	@Autowired
	EmailToSendService emailToSendService;

	@Value("${reclamation.destinataire.sn}")
	private String reclamation_destinataire_sn;

	@Value("${reclamation.destinataire.ci}")
	private String reclamation_destinataire_ci;



	public ReclamationEchange create(CreateReclamationEchangeParams params, String token) {

		String code_pays = jwtProvider.getCodePays(token);

		Reclamation reclamation = reclamationRepository.findByUUID(UUID.fromString(params.getReclamation()), code_pays);
		if (reclamation == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossible de charger la réclamation");
		}

		// génération du code
		Long max_code = reclamationEchangeRepository.getMaxCode();
		if (max_code == null) {
			max_code = new Long(0);
		}
		logger.info("max_code1 = " + max_code);
		Long code = max_code + new Long(1);

		logger.info("max_code2 = " + code);
		ReclamationEchange reclamation_echange = new ReclamationEchange(params, code, reclamation, true, code_pays);
		reclamation_echange = reclamationEchangeService.save(reclamation_echange);

		// mets à jour la date de dernière modification de la réclamation
		reclamation.setUpdatedOn(new Date());
		reclamationRepository.save(reclamation);

		// envoi d'un email à kamtar
		emailToSendService.prevenirClientNouvelleReclamationEchange(reclamation_echange, reclamation, jwtProvider.getCodePays(token));

		return reclamation_echange;
	}



	@Override
	public Page<ReclamationEchange> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<ReclamationEchange> conditions) {

		Direction SortDirection = order_dir.equals("desc") ? Direction.DESC : Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return reclamationEchangeRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<ReclamationEchange> conditions) {
		return reclamationEchangeRepository.count(conditions);
	}


	@Override
	public ReclamationEchange save(ReclamationEchange reclamation) {
		return reclamationEchangeRepository.save(reclamation);
	}

	@Override
	public List<ReclamationEchange> get(Reclamation reclamation, String code_pays) {
		return reclamationEchangeRepository.get(reclamation, code_pays);
	}



}
