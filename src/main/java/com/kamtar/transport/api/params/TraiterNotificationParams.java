package com.kamtar.transport.api.params;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kamtar.transport.api.validation.UUIDObligatoireConstraint;

import io.swagger.annotations.ApiModelProperty;

public class TraiterNotificationParams extends ParentParams {


	@ApiModelProperty(notes = "Identifiant de la notification", allowEmptyValue = false, required = true, dataType = "UUID")
	@NotNull(message = "{err.notification.uuid}")
	@UUIDObligatoireConstraint(message = "{err.notification.id_invalid}")
	private String notification;

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	



}
