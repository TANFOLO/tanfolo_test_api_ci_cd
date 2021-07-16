package com.kamtar.transport.api.service.impl;

import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.kamtar.transport.api.enums.TemplateEmail;
import com.kamtar.transport.api.model.*;
import com.kamtar.transport.api.params.ContactParams;
import com.kamtar.transport.api.repository.ContactRepository;
import com.kamtar.transport.api.repository.VehiculeCarrosserieRepository;
import com.kamtar.transport.api.repository.VehiculeTypeRepository;
import com.kamtar.transport.api.service.ContactService;
import com.kamtar.transport.api.service.ExportExcelService;
import com.kamtar.transport.api.utils.ExportUtils;
import io.gsonfire.GsonFireBuilder;
import io.gsonfire.PostProcessor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service(value="ExportExcelService")
public class ExportExcelServiceImpl implements ExportExcelService {

	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(ExportExcelServiceImpl.class);

	@Autowired
	private VehiculeCarrosserieRepository vehiculeCarrosserieRepository;

	@Autowired
	private VehiculeTypeRepository vehiculeTypeRepository;



	public byte[] export(Page leads, Map<String, String> entetes_a_remplacer) throws Exception {

		boolean supprimer_doublon = true;

		// adaptations en fonction du type d'object
		List list_objects = leads.getContent();
		if (list_objects != null && !list_objects.isEmpty()) {

			// si c'est véhicule, on ajoute la carrosserie et le type textuel à partir du code
			if (list_objects.get(0).getClass().equals(Vehicule.class)) {
				for (Object v : list_objects) {
					Vehicule vehicule = (Vehicule) v;
					if (vehicule.getCarrosserie() != null && !"".equals(vehicule.getCarrosserie())) {
						Optional<VehiculeCarrosserie> vc = vehiculeCarrosserieRepository.findByCode(vehicule.getCarrosserie());
						if (vc.isPresent()) {
							((Vehicule) v).setCarrosserieTextuel(vc.get().getName());
						}
					}
					if (vehicule.getTypeVehicule() != null && !"".equals(vehicule.getTypeVehicule())) {
						Optional<VehiculeType> vt = vehiculeTypeRepository.findByCode(vehicule.getTypeVehicule());
						if (vt.isPresent()) {
							((Vehicule) v).setTypeTextuel(vt.get().getName());
						}
					}
				}
			}

			// supprimer les doublons sur certaines classes
			if (list_objects.get(0).getClass().equals(UtilisateurProprietaire.class)) {
				supprimer_doublon = false;
			}
			if (list_objects.get(0).getClass().equals(UtilisateurOperateurKamtar.class)) {
				supprimer_doublon = false;
			}

		}

		// export CSV
		String file_without_extension = "/tmp/" + UUID.randomUUID();
		String file_csv = file_without_extension + ".csv";
		String file_xls = file_without_extension + ".xls";

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.setDateFormat("dd-MM-yyyy HH:mm:ss").create();
		String numbersJson = gson.toJson(list_objects);
		if (leads.getContent().isEmpty()) {
			// si aucun résultat, on créé le fichier vide
			File file = new File(file_csv);
			file.createNewFile();
		} else {
			try {
				JFlat flatMe = new JFlat(numbersJson);
				flatMe.json2Sheet().getJsonAsSheet();
				flatMe.write2csv(file_csv, ';');

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	if (supprimer_doublon) {

		// supprime les doublons d'opérations (jflat produit des doublons)
		BufferedReader csvReader = new BufferedReader(new FileReader(file_csv));
		String row = null;
		LinkedHashMap<String, String> map_uuid_operation = new LinkedHashMap<String, String>();
		while ((row = csvReader.readLine()) != null) {
			String[] data = row.split(";");
			map_uuid_operation.put(data[0], row);
		}
		csvReader.close();
		FileWriter csvWriter = new FileWriter(file_csv);
		Iterator<Map.Entry<String, String>> iter = map_uuid_operation.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> rowData = iter.next();
			csvWriter.append(rowData.getValue());
			csvWriter.append("\n");
		}

		csvWriter.flush();
		csvWriter.close();

	} else {

		/*BufferedReader csvReader = new BufferedReader(new FileReader(file_csv));
		String row = null;
		FileWriter csvWriter = new FileWriter(file_csv);

		while ((row = csvReader.readLine()) != null) {
			logger.info("dedans");
			String[] data = row.split(";");

			csvWriter.append(row);
			csvWriter.append("\n");

			//map_uuid_operation.put(data[0], row);
		}
		csvReader.close();*/

	}



		// modification des colonnes si export factures clients
		if (entetes_a_remplacer != null && !entetes_a_remplacer.isEmpty()) {
			String contents = new String(Files.readAllBytes(Paths.get(file_csv)));
			Iterator<Map.Entry<String, String>> iter = entetes_a_remplacer.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> next = iter.next();
				contents = contents.replaceAll(next.getKey(), next.getValue());
			}
			FileUtils.write(new File(file_csv), contents, "UTF-8");
		}

		ExportUtils.convertCSV2Excel(file_csv, file_xls);

		byte[] bytesArray = new byte[(int) new File(file_xls).length()];
		FileInputStream fis = new FileInputStream(new File(file_xls));
		fis.read(bytesArray); //read file into bytes[]
		fis.close();

		// suppression des  fichiers
		//new File(file_csv).delete();
		//new File(file_xls).delete();

		return bytesArray;

	}

	public static void convertCSV2Excel(String csvFilePath, String xlsFilePath) throws IOException {

		ArrayList arList=null;
		ArrayList al=null;
		String thisLine;
		int count=0;
		FileInputStream fis = new FileInputStream(csvFilePath);
		DataInputStream myInput = new DataInputStream(fis);
		int i=0;
		arList = new ArrayList();
		while ((thisLine = myInput.readLine()) != null)
		{
			al = new ArrayList();
			String strar[] = thisLine.split(";");
			for(int j=0;j<strar.length;j++)
			{
				al.add(strar[j]);
			}
			arList.add(al);
			//System.out.println();
			i++;
		}
		myInput.close();
		fis.close();

		try
		{
			//org.apache.poi.ss.usermodel.Workbook hwb = org.apache.poi.xssf.usermodel.XSSFWorkbook.create(inp);
			Workbook hwb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
			org.apache.poi.ss.usermodel.Sheet sheet = hwb.createSheet("new sheet");
			for(int k=0;k<arList.size();k++)
			{
				ArrayList ardata = (ArrayList)arList.get(k);
				org.apache.poi.ss.usermodel.Row row = sheet.createRow((short) 0+k);
				for(int p=0;p<ardata.size();p++) {
					org.apache.poi.ss.usermodel.Cell cell = row.createCell((short) p);
					String data = ardata.get(p).toString();
					if(data.startsWith("=")){
						cell.setCellType(Cell.CELL_TYPE_STRING);
						data=data.replaceAll("\"", "");
						data=data.replaceAll("=", "");
						// cell.setCellValue(data);
						data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
						cell.setCellValue(data);
					}else if(data.startsWith("\"")){
						data=data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_STRING);
						// cell.setCellValue(data);
						data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
						cell.setCellValue(data);
					}else{
						data=data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						// cell.setCellValue(data);
						data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
						cell.setCellValue(data);
					}
					//*/
					//   cell.setCellValue(ardata.get(p).toString());
				}
				//System.out.println();
			}
			FileOutputStream fileOut = new FileOutputStream(xlsFilePath);
			hwb.write(fileOut);
			fileOut.close();
			// System.out.println("Your excel file has been generated");
		} catch ( Exception ex ) {
			ex.printStackTrace();
		} //main method ends
	}





} 





