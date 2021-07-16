package com.kamtar.transport.api.utils;

import com.kamtar.transport.api.controller.OperationController;
import com.wbc.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RRulesUtils {

    /**
     * Logger de la classe
     */
    private static Logger logger = LogManager.getLogger(RRulesUtils.class);

    public static Date prochaineDate(String rruleX, Date date_programmee_operation, Date depart, Date fin) throws InvalidRecurrenceRuleException {
        Date prochaineDate = null;

        if (date_programmee_operation != null && depart != null) {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            logger.info("prochaineDate date_programmee_operation=" + formatter.format(date_programmee_operation) + " depart=" + formatter.format(depart));

            DateFormat formatter_y = new SimpleDateFormat("yyyy");
            DateFormat formatter_m = new SimpleDateFormat("MM");
            DateFormat formatter_d = new SimpleDateFormat("dd");

            // suppression du "UNTIL=20201222T230000Z;" dans la chaine rrule
            String[] rrule_exploded = rruleX.split(";");
            List<String> rrules_2 = new ArrayList<String>();
            Date date_until = null;
            for (int i=0; i<rrule_exploded.length; i++) {
                if (!rrule_exploded[i].startsWith("UNTIL=")) {
                    rrules_2.add(rrule_exploded[i]);
                } else {
                    String until = rrule_exploded[i].split("T")[0];
                    try {
                        date_until = new SimpleDateFormat("yyyyMMdd").parse(until);
                    } catch (ParseException e) {
                        // erreur silencieuse
                    }


                }
            }
            rruleX = StringUtils.join(rrules_2, ";");
            logger.info("rruleX = " + rruleX);
            logger.info("date_until = " + date_until);

            RecurrenceRule rule = new RecurrenceRule(rruleX, RecurrenceRule.RfcMode.RFC2445_LAX);
            DateTime start = new DateTime(Integer.valueOf(formatter_y.format(date_programmee_operation)), Integer.valueOf(formatter_m.format(date_programmee_operation)) - 1 /* 0-based month numbers! */, Integer.valueOf(formatter_d.format(date_programmee_operation)));

            RecurrenceRuleIterator it = rule.iterator(start);
            int maxInstances = 100; // limit instances for rules that recur forever


            while (it.hasNext() && (!rule.isInfinite() || maxInstances-- > 0)) {
                DateTime nextInstance = it.nextDateTime();
                Date date_prochain = new Date(nextInstance.getTimestamp());


                //logger.info("date calculée = " + formatter.format(date_prochain));



                if (prochaineDate == null && date_prochain.after(depart) && (fin == null || (fin != null && date_prochain.before(fin)))) {
                    prochaineDate = date_prochain;
                    logger.info("date prochaine trouvée = " + formatter.format(prochaineDate));
                }

            }
        }
        return prochaineDate;
    }


    public static Date getDateUtil(String rruleX, Date depart)  {
        logger.info("getDateUtil début");
        Date date_until = null;

        if (depart != null) {
            logger.info("getDateUtil depart != null");
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


            DateFormat formatter_y = new SimpleDateFormat("yyyy");
            DateFormat formatter_m = new SimpleDateFormat("MM");
            DateFormat formatter_d = new SimpleDateFormat("dd");

            // suppression du "UNTIL=20201222T230000Z;" dans la chaine rrule
            String[] rrule_exploded = rruleX.split(";");
            List<String> rrules_2 = new ArrayList<String>();
            Map<String, String> map_rrules = new HashMap<String, String>();
            for (int i=0; i<rrule_exploded.length; i++) {
                String[] cle_valeur = rrule_exploded[i].split("=");
                map_rrules.put(cle_valeur[0], cle_valeur[1]);
            }
            Iterator<Map.Entry<String, String>> iter = map_rrules.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> ext = iter.next();
            //for (int i=0; i<rrule_exploded.length; i++) {
                if (ext.getKey().equals("UNTIL")) {
                    String until = ext.getValue().split("T")[0];
                    logger.info("getDateUtil until=" + until);
                    try {
                        date_until = new SimpleDateFormat("yyyyMMdd").parse(until);

                        Calendar now = new GregorianCalendar();
                        now.setTime(date_until);
                        now.set(Calendar.HOUR_OF_DAY, 23);
                        now.set(Calendar.MINUTE, 59);
                        now.set(Calendar.SECOND, 59);
                        date_until = now.getTime();

                        logger.info("getDateUtil date_until=" + date_until);
                    } catch (ParseException e) {
                        logger.info("getDateUtil ParseException", e);
                        // erreur silencieuse
                    }


                }
            }

        }

        logger.info("getDateUtil / date_until = " + date_until);
        return date_until;
    }
}
