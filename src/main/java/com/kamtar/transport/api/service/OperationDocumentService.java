package com.kamtar.transport.api.service;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kamtar.transport.api.model.Operation;
import com.kamtar.transport.api.model.OperationDocument;
import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculePhoto;


@Service
public interface OperationDocumentService {
	
	public void saveDocuments(Operation operation, File folder, String type_compte);
	public void saveDocument(Operation operation, File photo, Integer ordre, String type_compte);
	public void saveDocument(Operation operation, String base64, Integer ordre, String type_compte);
	public void saveDocument(Operation operation, byte[] array_image, Integer ordre, String filename, String type_compte);

	public List<OperationDocument> getOperationDocuments(Operation operation);
	public void delete(OperationDocument photo);
	public Optional<OperationDocument> get(Operation operation, String filename);
	public Optional<OperationDocument> load(String uuid);
	Long countOperationDocuments(Operation operation);
	byte[] get(String uuid);
	

}
