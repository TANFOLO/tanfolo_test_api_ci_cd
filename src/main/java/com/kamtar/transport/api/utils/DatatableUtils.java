package com.kamtar.transport.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import com.kamtar.transport.api.model.ActionAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;

import com.kamtar.transport.api.criteria.ParentSpecificationsBuilder;
import com.kamtar.transport.api.criteria.PredicateUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


public class DatatableUtils {
	/**
	 * Logger de la classe
	 */
	private static Logger logger = LogManager.getLogger(DatatableUtils.class);  

	/**
	 * Fabrique une map position => nom de colonne depuis le json envoyé par datatable
	 * @param postBody
	 * @return
	 */
	public static Map<Integer, String> getMapPositionNomColonnes(MultiValueMap postBody) {

		// trouve le nom de la colonne datatable à partir de l'index dans le json envoyé par datatable en ajax
		boolean fin_colonnes = false;
		int cpt = 0;
		Map<Integer, String> indexColumn_nomColonne = new HashMap<Integer, String>();
		if (postBody != null) {
			while (!fin_colonnes) {
				String column = (String) postBody.getFirst("columns[" + cpt + "][data]");
				if (column == null) {
					fin_colonnes = true;
				} else if (!"".equals(column)) {
					indexColumn_nomColonne.put(cpt, column);
				}
				cpt++;
			}


		}


		return indexColumn_nomColonne;

	}

	/**
	 * Récupère la colonne de tri à partir de la position renvoyé par datatable
	 * @param position
	 * @param default_colonne
	 * @param colonnes_autorises
	 * @param postBody
	 * @return
	 */
	public static String getOrderColonne(String default_colonne, List<String> colonnes_autorises, MultiValueMap postBody) {
		Map<Integer, String> indexColumn_nomColonne = getMapPositionNomColonnes(postBody);

		Integer position_int = -1;
		try {
			position_int = Integer.valueOf(postBody.getFirst("order[0][column]").toString());
		} catch (NumberFormatException e) {
			// ne rien faire
		}
		if (indexColumn_nomColonne.containsKey(position_int) && colonnes_autorises.contains(indexColumn_nomColonne.get(position_int).toString())) {
			return indexColumn_nomColonne.get(position_int).toString();
		}
		return default_colonne;

	}

	/**
	 * Retourne le sens du tri en fonction des paramètres envoyés par datatable
	 * @param postBody
	 * @return
	 */
	public static String getSort(MultiValueMap postBody) {
		String order_order = postBody.getFirst("order[0][dir]").toString();
		if (order_order.equals("desc")) {
			return "desc";
		}
		return "asc";

	}

	/**
	 * Retourne le numéro de la page compatible base de données à partir des paramètres envoyés par datatable
	 * @param postBody
	 * @param length
	 * @return
	 */
	public static Integer getNumeroPage(MultiValueMap postBody, Integer length) {

		Integer start = Integer.valueOf(postBody.getFirst("start").toString());
		Integer numero_page = 0;
		if (start != 0) {
			Double d = (double) (start/length);
			numero_page = (int) Math.floor(d);
		}
		return numero_page;
	}

	public static Specification fitrageEntier(Specification spec, MultiValueMap postBody, String nomParametreDatatable, String nomChampDatabase) {

		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, nomParametreDatatable);
		if (position_colonne != null) {
			String createdOn = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			Specification spec_entier = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					if (createdOn.contains("-")) {
						String[] entier_splitted = createdOn.split("-");
						try {
						predicates.add(builder.greaterThanOrEqualTo(root.get(nomChampDatabase), Integer.valueOf(entier_splitted[0].replaceAll("[^\\d.]", ""))));
						predicates.add(builder.lessThanOrEqualTo(root.get(nomChampDatabase), Integer.valueOf(entier_splitted[1].replaceAll("[^\\d.]", ""))));
						} catch (NumberFormatException e) {
							// erreur silencieuse
						} catch (ArrayIndexOutOfBoundsException e) {
							// erreur silencieuse
						}
					} else {
						try {
							predicates.add(builder.equal(root.get(nomChampDatabase), Integer.valueOf(createdOn.replaceAll("[^\\d.]", ""))));
						} catch (NumberFormatException e) {
							// erreur silencieuse
						}

					}
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			return spec.and(spec_entier);
		}
		return spec;
	}


	public static Date fitrageDateDebut(MultiValueMap postBody, String nomParametreDatatable, String nomChampDatabase) {

		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, nomParametreDatatable);
		if (position_colonne != null) {
			String createdOn = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			if (createdOn.contains("-")) {
				String[] date_splitted = createdOn.split("-");
				try {
					Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date_splitted[0] + " 00:00:00");
					return date1;
				} catch (ParseException e) {
					// erreur silencieuses
				} catch (ArrayIndexOutOfBoundsException e) {
					// erreur silencieuse
				}
			} else {
				try {
					Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(createdOn + " 00:00:00");
					return date1;
				} catch (ParseException e) {
					// erreur silencieuses
				}
			}
		}

		return null;

	}
	public static Date fitrageDateFin(MultiValueMap postBody, String nomParametreDatatable, String nomChampDatabase) {

		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, nomParametreDatatable);
		if (position_colonne != null) {
			String createdOn = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();

			if (createdOn.contains("-")) {
				String[] date_splitted = createdOn.split("-");
				try {
					Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date_splitted[1] + " 23:59:59");
					return date2;
				} catch (ParseException e) {
					// erreur silencieuses
				} catch (ArrayIndexOutOfBoundsException e) {
					// erreur silencieuse
				}
			} else {
				try {
					Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(createdOn + " 23:59:59");
					return date2;
				} catch (ParseException e) {
					// erreur silencieuses
				}
			}

		};

		return null;

	}

	public static Specification fitrageDate(Specification spec, MultiValueMap postBody, String nomParametreDatatable, String nomChampDatabase) {
		Map<Integer, String> indexColumn_nomColonne = DatatableUtils.getMapPositionNomColonnes(postBody);
		Iterator<Entry<Integer, String>> iter = indexColumn_nomColonne.entrySet().iterator();

		Integer position_colonne = DatatableUtils.getKeyByValue(indexColumn_nomColonne, nomParametreDatatable);
		if (position_colonne != null) {
			String createdOn = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
			Specification spec_date = new Specification<ActionAudit>() {
				public Predicate toPredicate(Root<ActionAudit> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					List<Predicate> predicates = new ArrayList<Predicate>();
					if (createdOn.contains("-")) {
						String[] date_splitted = createdOn.split("-");
						try {
							Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date_splitted[0] + " 00:00:00");
							predicates.add(builder.greaterThanOrEqualTo(root.get(nomChampDatabase), date1));
							Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date_splitted[1] + " 23:59:59");
							predicates.add(builder.lessThanOrEqualTo(root.get(nomChampDatabase), date2));
						} catch (ParseException e) {
							// erreur silencieuses
						} catch (ArrayIndexOutOfBoundsException e) {
						// erreur silencieuse
						}
					} else {
						try {
							Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(createdOn + " 00:00:00");
							predicates.add(builder.greaterThanOrEqualTo(root.get(nomChampDatabase), date1));
							Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(createdOn + " 23:59:59");
							predicates.add(builder.lessThanOrEqualTo(root.get(nomChampDatabase), date2));
						} catch (ParseException e) {
							// erreur silencieuses
						}
					}
					return builder.and(predicates.toArray(new Predicate[predicates.size()]));
				}
			};
			return spec.and(spec_date);
		}
		return spec;
	}


	public static Specification buildFiltres(List<String> colonnes_filtrage, MultiValueMap postBody, ParentSpecificationsBuilder builder, String valeur_a_ne_pas_filtrer) {

		Map<Integer, String> indexColumn_nomColonne = getMapPositionNomColonnes(postBody);
		if (colonnes_filtrage != null) {
			for (String colonne_filtrage : colonnes_filtrage) {

				Integer position_colonne = getKeyByValue(indexColumn_nomColonne, colonne_filtrage);

				if (position_colonne == null) {
					// çà arrive si le nom de la colonne filtré demandée ne fait pas partie des colonnes (cas où plusieurs données dans même colonne datatable)

				} else {

					String filter_activee = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
					// filtrage activées
					if (filter_activee != null && !"".equals(filter_activee) && !filter_activee.equals(valeur_a_ne_pas_filtrer)) {

						// est ce un boolean ou une chaine de caractère ?
						if (filter_activee.equals("true") || filter_activee.equals("false")) {
							builder.with(colonne_filtrage, PredicateUtils.OPERATEUR_EGAL, "true".equals(filter_activee), false);
						} else if (!filter_activee.equals("") && !filter_activee.equals("%%")) {
							builder.with(colonne_filtrage, PredicateUtils.OPERATEUR_EGAL, filter_activee, false);
						}

					}

				}

			}
		}
		Specification spec = builder.build();

		return spec;


	}

	public static String getFiltrageValeur(List<String> colonnes_filtrage, MultiValueMap postBody) {

		Map<Integer, String> indexColumn_nomColonne = getMapPositionNomColonnes(postBody);

		if (colonnes_filtrage != null) {
			for (String colonne_filtrage : colonnes_filtrage) {
				Integer position_colonne = getKeyByValue(indexColumn_nomColonne, colonne_filtrage);

				if (position_colonne == null) {
					// çà arrive si le nom de la colonne filtré demandée ne fait pas partie des colonnes (cas où plusieurs données dans même colonne datatable)

				} else {

					String filter_activee = postBody.getFirst("columns[" + position_colonne + "][search][value]").toString();
					return filter_activee;
				}

			}
		}
		return null;

	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
