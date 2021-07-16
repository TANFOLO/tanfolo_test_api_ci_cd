package com.kamtar.transport.api.utils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import com.github.opendevl.JFlat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.kamtar.transport.api.controller.ClientController;
import com.kamtar.transport.api.model.Vehicule;
import com.kamtar.transport.api.model.VehiculeCarrosserie;
import com.kamtar.transport.api.repository.VehiculeCarrosserieRepository;
import io.gsonfire.GsonFireBuilder;
import io.gsonfire.PostProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;


public class ExportUtils {

    /**
     * Logger de la classe
     */
    private static Logger logger = LogManager.getLogger(ExportUtils.class);

    @Autowired
    private static VehiculeCarrosserieRepository vehiculeCarrosserieRepository;


    public static byte[] listToExcel(Page leads) throws Exception {

        // export CSV
        String file_without_extension = "/tmp/" + UUID.randomUUID();
        String file_csv = file_without_extension + ".csv";
        String file_xls = file_without_extension + ".xls";
        Gson gson = new GsonFireBuilder().registerPostProcessor(Vehicule.class, new PostProcessor<Vehicule>() {
            @Override
            public void postDeserialize(Vehicule vehicule, JsonElement jsonElement, Gson gson) {
                if (vehicule.getCarrosserie() != null && !"".equals(vehicule.getCarrosserie())) {
                    Optional<VehiculeCarrosserie> o = vehiculeCarrosserieRepository.findByCode(vehicule.getCarrosserie());
                    if (o.isPresent()) {
                        vehicule.setCarrosserieTextuel(o.get().getName());
                    }
                }
            }

            @Override
            public void postSerialize(JsonElement jsonElement, Vehicule vehicule, Gson gson) {
                if (vehicule.getCarrosserie() != null && !"".equals(vehicule.getCarrosserie())) {
                    Optional<VehiculeCarrosserie> o = vehiculeCarrosserieRepository.findByCode(vehicule.getCarrosserie());
                    if (o.isPresent()) {
                        vehicule.setCarrosserieTextuel(o.get().getName());
                    }
                }

            }

        }).enableExposeMethodResult().createGsonBuilder().setDateFormat("dd-MM-yyyy HH:mm:ss").create();
        String numbersJson = gson.toJson(leads.getContent());
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


        ExportUtils.convertCSV2Excel(file_csv, file_xls);

        byte[] bytesArray = new byte[(int) new File(file_xls).length()];
        FileInputStream fis = new FileInputStream(new File(file_xls));
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        // suppression des  fichiers
        new File(file_csv).delete();
        new File(file_xls).delete();

        return bytesArray;

    }

    public static void convertCSV2Excel(String csvFilePath, String xlsFilePath) {

        ArrayList arList=null;
        ArrayList al=null;
        String thisLine;
        int count=0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(csvFilePath);
            DataInputStream myInput = new DataInputStream(fis);
            int i=0;
            arList = new ArrayList();
            while ((thisLine = myInput.readLine()) != null)
            {
                al = new ArrayList();
                //String strar[] = thisLine.split("\";\"");
                String[] strar = thisLine.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // pour ne pas séparer par les ; qui se trouvent dans les doubles quotes

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
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException 4");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException 5");
            e.printStackTrace();
        }


        try
        {
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
                        //data=data.replaceAll(";", ":");
                       // cell.setCellValue(data);
                       data = new String(data.getBytes("ISO-8859-1"), "UTF-8");
                        cell.setCellValue(data);
                    }else if(data.startsWith("\"")){
                        data=data.replaceAll("\"", "");
                       // data=data.replaceAll(";", ":");
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
        } catch (UnsupportedEncodingException e ) {
            logger.error("IOException", e);
            e.printStackTrace();
        } catch (FileNotFoundException e ) {
            logger.error("FileNotFoundException", e);
            e.printStackTrace();
        }//main method ends
        catch (IOException e) {
            logger.error("IOException", e);
            e.printStackTrace();
        }
    }

}
