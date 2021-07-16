package com.kamtar.transport.api.params;

public class TestOperationRecurrence extends ParentParams {


	private String cle;
	private Integer nb_jours;
	private String date_appel;

	public String getDate_appel() {
		return date_appel;
	}

	public void setDate_appel(String date_appel) {
		this.date_appel = date_appel;
	}

	public String getCle() {
		return cle;
	}
	public void setCle(String cle) {
		this.cle = cle;
	}

	public Integer getNb_jours() {
		return nb_jours;
	}

	public void setNb_jours(Integer nb_jours) {
		this.nb_jours = nb_jours;
	}
}
