package fcweb.backend.data.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fc_mercato_dett_info")
public class FcMercatoDettInfo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

//	@Id
//	@GeneratedValue(strategy = IDENTITY)
//	@Column(name = "id", unique = true, nullable = false)
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "id_giornata", referencedColumnName = "codice_giornata")
	private FcGiornataInfo fcGiornataInfo; // immutable

	@ManyToOne
	@JoinColumn(name = "id_attore", referencedColumnName = "id_attore")
	private FcAttore fcAttore; // immutable

	@Column(name = "tot_cambi", nullable = false)
	private Integer totCambi;

	@Column(name = "flag_invio", nullable = false)
	private String flagInvio;

	@Column(name = "data_invio", nullable = false)
	private Date dataInvio;

	public FcGiornataInfo getFcGiornataInfo() {
		return fcGiornataInfo;
	}

	public void setFcGiornataInfo(FcGiornataInfo fcGiornataInfo) {
		this.fcGiornataInfo = fcGiornataInfo;
	}

	public FcAttore getFcAttore() {
		return fcAttore;
	}

	public void setFcAttore(FcAttore fcAttore) {
		this.fcAttore = fcAttore;
	}

	public String getFlagInvio() {
		return flagInvio;
	}

	public void setFlagInvio(String flagInvio) {
		this.flagInvio = flagInvio;
	}

	public Date getDataInvio() {
		return dataInvio;
	}

	public void setDataInvio(Date dataInvio) {
		this.dataInvio = dataInvio;
	}

	public Integer getTotCambi() {
		return totCambi;
	}

	public void setTotCambi(Integer totCambi) {
		this.totCambi = totCambi;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}