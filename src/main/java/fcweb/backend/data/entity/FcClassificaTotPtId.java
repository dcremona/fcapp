package fcweb.backend.data.entity;
// Generated 31-ott-2018 12.15.29 by Hibernate Tools 5.1.7.Final

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * FcClassificaTotPtId generated by hbm2java
 */
@Embeddable
public class FcClassificaTotPtId implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int idAttore;
	private int idCampionato;
	private int idGiornata;

	public FcClassificaTotPtId() {
	}

	public FcClassificaTotPtId(int idAttore, int idCampionato, int idGiornata) {
		this.idAttore = idAttore;
		this.idCampionato = idCampionato;
		this.idGiornata = idGiornata;
	}

	@Column(name = "id_attore", nullable = false)
	public int getIdAttore() {
		return this.idAttore;
	}

	public void setIdAttore(int idAttore) {
		this.idAttore = idAttore;
	}

	@Column(name = "id_campionato", nullable = false)
	public int getIdCampionato() {
		return this.idCampionato;
	}

	public void setIdCampionato(int idCampionato) {
		this.idCampionato = idCampionato;
	}

	@Column(name = "id_giornata", nullable = false)
	public int getIdGiornata() {
		return this.idGiornata;
	}

	public void setIdGiornata(int idGiornata) {
		this.idGiornata = idGiornata;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof FcClassificaTotPtId))
			return false;
		FcClassificaTotPtId castOther = (FcClassificaTotPtId) other;

		return (this.getIdAttore() == castOther.getIdAttore()) && (this.getIdCampionato() == castOther.getIdCampionato()) && (this.getIdGiornata() == castOther.getIdGiornata());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getIdAttore();
		result = 37 * result + this.getIdCampionato();
		result = 37 * result + this.getIdGiornata();
		return result;
	}

}
