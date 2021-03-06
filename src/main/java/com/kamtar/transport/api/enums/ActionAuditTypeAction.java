package com.kamtar.transport.api.enums;

public enum ActionAuditTypeAction {
	LOGIN,
	LOGIN_TRANSPORTEUR,
	LOGIN_PROPRIETAIRE,
	LOGIN_EXPEDITEUR,
	CREER_ADMIN_KAMTAR,
	CREER_OPERATEUR_KAMTAR,
	SUPPRIMER_OPERATEUR_KAMTAR,
	EDITER_ADMIN_KAMTAR,
	SUPPRIMER_ADMIN_KAMTAR,
	EDITER_OPERATEUR_KAMTAR,
	CONSULTATION_ADMIN_KAMTAR,
	CONSULTATION_OPERATEUR_KAMTAR,
	CONSULTATION_ADMINS_KAMTAR,
	EXPORT_ADMINS_KAMTAR,
	CONSULTATION_OPERATEURS_KAMTAR,
	EXPORT_OPERATEURS_KAMTAR,
	SUPPRIMER_EXPEDITEUR,

	CREER_PROPRIETAIRE,
	SUPPRIMER_PROPRIETAIRE,
	EDITER_PROPRIETAIRE,
	CONSULTATION_PROPRIETAIRE,
	CONSULTATION_PROPRIETAIRES,
	EXPORT_PROPRIETAIRES,

	CREER_VEHICULE,
	SUPPRIMER_VEHICULE,
	EDITER_VEHICULE,
	CHANGER_DISPONIBILITE_VEHICULE,
	CONSULTATION_VEHICULE,
	CONSULTATION_GEOLOC_VEHICULE,
	CONSULTATION_GEOLOC_VEHICULES,
	CONSULTATION_GEOLOCS_VEHICULE,
	CONSULTATION_PHOTOS_VEHICULE,
	CONSULTATION_VEHICULES,
	EXPORT_VEHICULES,

	CREER_DRIVER,
	SUPPRIMER_DRIVER,
	EDITER_CREER_DRIVER,
	CONSULTATION_CREER_DRIVER,
	CONSULTATION_CREER_DRIVERS,
	
	CREER_EXPEDITEUR,
	EDITER_EXPEDITEUR,
	CONSULTATION_EXPEDITEUR,
	CONSULTATION_EXPEDITEURS,
	EXPORT_EXPEDITEURS,
	
	CREER_TRANSPORTEUR,
	EDITER_TRANSPORTEUR,
	SUPPRIMER_TRANSPORTEUR,
	CONSULTATION_TRANSPORTEUR,
	CONSULTATION_TRANSPORTEURS,
	EXPORT_TRANSPORTEURS,

	CREER_LANGUAGE,
	CONSULTATION_LANGUAGES,
	
	CREER_COUNTRY,
	CONSULTATION_COUNTRIES,
	
	CONTACT,
	MOT_DE_PASSE_PERDU ,
	
	CONSULTATION_AUDIT,
	CONSULTATION_SMS,
	CONSULTATION_EMAILS,
	
	CONSULTE_NOTIFICATIONS,
	TRAITER_NOTIFICATION,

	AFFECTER_OPERATION,
	CREER_OPERATION,
	CREER_OPERATION_PAR_CLIENT,
	CREER_OPERATION_PAR_DUPLICATION,
	EDITER_OPERATION,
	SUPPRIMER_OPERATION,
	DUPLIQUER_OPERATION,
	CONSULTATION_OPERATION,
	CHANGEMENT_ORDRE_OPERATIONS,
	CONSULTATION_OPERATIONS,
	EXPORT_OPERATIONS,
	
	CREER_OPERATION_APPEL_OFFRE_DRIVER,
	CONSULTATION_OPERATION_APPELS_OFFRE_DRIVER,
	NOUVELLE_PROPOSITION_APPEL_OFFRE,

	CONSULTATION_DOCUMENT_OPERATION,
	
	OPERATION_CHANGEMENT_STATUT,

	VALIDATION_CLIENT,
	DECLARER_INCIDENT,
	SATISFACTION_OPERATION,
	ANNULER_OPERATION,
	GEOLOC_OPERATION,

	LISTE_FACTURES,
	SUPPRIMER_FACTURE,
	CONSULTER_FACTURE,
	EXPORT_FACTURES_CLIENT,
	EXPORT_FACTURES_PROPRIETAIRE,
	EXPORT_FACTURES_DU_CLIENT,

	STATS_TOP_DESTINATIONS,

	CREER_CLIENT_PERSONNEL,
	EDITER_CLIENT_PERSONNEL,
	SUPPRIMER_CLIENT_PERSONNEL,
	CONSULTATION_CLIENT_PERSONNEL,
	CONSULTATION_CLIENTS_PERSONNELS,
	LOGIN_CLIENT_PERSONNEL,

	CONSULTATION_DEVIS,
	CONSULTATION_LISTE_DEVIS,
	SUPPRIMER_DEVIS,
	EXPORT_DEVIS,
	CREER_DEVIS,

	CREER_RECLAMATION,
	CREER_RECLAMATION_ECHANGE,
	CONSULTATION_RECLAMATIONS,
	CONSULTATION_RECLAMATION,
	EXPORT_RECLAMATIONS,
	AJOUT_ECHANGE_RECLAMATION,
	CHANGEMENT_STATUT_OPERATION
}
