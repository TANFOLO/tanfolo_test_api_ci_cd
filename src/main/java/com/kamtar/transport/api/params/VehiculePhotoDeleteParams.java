package com.kamtar.transport.api.params;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;




public class VehiculePhotoDeleteParams extends ParentParams {

	@NotNull(message = "{err.offrephoto.folderuuid}")
	private String folderUuid;
	
	@NotNull(message = "{err.offrephoto.filename}")
	private String filename;
	
	/**
	 * Véhicule à laquelle est attachée la photo (uniquement valide dans la suppression d'une photo depuis une modifcation)
	 */
	private String vehicule;

	

	public String getVehicule() {
		return vehicule;
	}

	public void setVehicule(String vehicule) {
		this.vehicule = vehicule;
	}

	public String getFolderUuid() {
		return folderUuid;
	}

	public void setFolderUuid(String folderUuid) {
		this.folderUuid = folderUuid;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	
	
	
}
