package com.kamtar.transport.api.service;



import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.kamtar.transport.api.enums.NotificationType;
import com.kamtar.transport.api.model.Notification;


@Service
public interface NotificationService {
	
	Notification create(Notification notification, String code_pays);
	Long nbNotificationsNonLues(NotificationType type, String code_pays);
	
	public Page<Notification> getAllPagined(String order_by, String order_dir, int page_number, int page_size, Specification<Notification> conditions);
	public Long countAll(Specification<Notification> conditions);
	Notification getByUUID(String uuid, String code_pays);
	Notification traiter(Notification notification, String token);
}
