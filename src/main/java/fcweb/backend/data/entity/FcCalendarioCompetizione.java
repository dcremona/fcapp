package fcweb.backend.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fc_calendario_tim")
public class FcCalendarioCompetizione implements Serializable{

	/**
	 *
	 */
	@Serial
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "codice_giornata")
	private int idGiornata;

	@Column(name = "data")
	private LocalDateTime data;

	@Column(name = "id_squadra_casa")
	private int idSquadraCasa;

	@Column(name = "squadra_casa")
	private String squadraCasa;

	@Column(name = "squadra_fuori")
	private String squadraFuori;

	@Column(name = "id_squadra_fuori")
	private int idSquadraFuori;

	@Column(name = "risultato")
	private String risultato;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getIdGiornata() {
		return idGiornata;
	}

	public void setIdGiornata(int idGiornata) {
		this.idGiornata = idGiornata;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	public int getIdSquadraCasa() {
		return idSquadraCasa;
	}

	public void setIdSquadraCasa(int idSquadraCasa) {
		this.idSquadraCasa = idSquadraCasa;
	}

	public String getSquadraCasa() {
		return squadraCasa;
	}

	public void setSquadraCasa(String squadraCasa) {
		this.squadraCasa = squadraCasa;
	}

	public String getSquadraFuori() {
		return squadraFuori;
	}

	public void setSquadraFuori(String squadraFuori) {
		this.squadraFuori = squadraFuori;
	}

	public int getIdSquadraFuori() {
		return idSquadraFuori;
	}

	public void setIdSquadraFuori(int idSquadraFuori) {
		this.idSquadraFuori = idSquadraFuori;
	}

	public String getRisultato() {
		return risultato;
	}

	public void setRisultato(String risultato) {
		this.risultato = risultato;
	}

}