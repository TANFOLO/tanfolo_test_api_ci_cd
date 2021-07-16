package com.kamtar.transport.api.params;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public class OperationDocumentDeleteParams extends ParentParams {

	@NotNull(message = "{err.offrephoto.folderuuid}")
	private String folderUuid;
	
	@NotNull(message = "{err.offrephoto.filename}")
	private String filename;
	
	/**
	 * Opération à laquelle est attachée le document (uniquement valide dans la suppression d'un document depuis une modifcation)
	 */
	private String operation;

	


	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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
