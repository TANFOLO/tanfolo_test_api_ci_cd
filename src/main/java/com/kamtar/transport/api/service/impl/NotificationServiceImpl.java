package com.kamtar.transport.api.service.impl;

import java.util.Date;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.Notification;
import com.kamtar.transport.api.repository.NotificationRepository;
import com.kamtar.transport.api.service.ActionAuditService;
import com.kamtar.transport.api.service.NotificationService;
import com.kamtar.transport.api.sockets.classes.Message;
import com.kamtar.transport.api.utils.JWTProvider;

@Service(value="NotificationService")
public class NotificationServiceImpl implements NotificationService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(NotificationServiceImpl.class); 

	@Autowired
	JWTProvider jwtProvider;

	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private ActionAuditService actionAuditService; 
	
	@Autowired
    private SimpMessagingTemplate template;


	public Notification create(Notification notification, String code_pays) {

		// sauvegarde en bdd
		notification = notificationRepository.save(notification);
		
		long nbNotificationsNonLues = notificationRepository.countNotificationsNonLues(notification.getType(), code_pays);

		// envoi en websocket
		Message m = new Message();
		m.setNb_notifications(Long.valueOf(nbNotificationsNonLues).intValue());
		m.setText("");
		this.template.convertAndSend("/backoffice/notifications", m);

		return notification;
	}


	@Override
	public Long nbNotificationsNonLues(NotificationType type, String code_pays) {
		return notificationRepository.countNotificationsNonLues(type.toString(), code_pays);
	}

	@Override
	public Page<Notification> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Notification> conditions) {
		Direction SortDirection = order_dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page_number, page_size, SortDirection, order_by);
		return notificationRepository.findAll(conditions, pageable);
	}

	@Override
	public Long countAll(Specification<Notification> conditions) {
		return notificationRepository.count(conditions);
	}


	@Override
	public Notification getByUUID(String uuid, String code_pays) {
		return notificationRepository.findByUUID(UUID.fromString(uuid), code_pays);
	}


	@Override
	public Notification traiter(Notification notification, String token) {
		
		if (notification.getDateIndiqueeTraitee() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La notification a déjà été traitée.");
		}
		
		notification.setDateIndiqueeTraitee(new Date());
		notification = notificationRepository.save(notification);
		
		
		actionAuditService.getNotificationsBackoffice(token);
		
		return notification;
	}

}
