package fcweb.backend.data;

import java.io.Serial;
import java.io.Serializable;

//Here is a bean
public class Calendario implements Serializable{

	@Serial
    private static final long serialVersionUID = 1L;

	private int id;
	private String attoreCasa;
	private String punteggio;
	private String attoreFuori;
	private String risultato;

	public Calendario() {
		// TODO document why this constructor is empty
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAttoreCasa() {
		return attoreCasa;
	}

	public String getPunteggio() {
		return punteggio;
	}

	public String getAttoreFuori() {
		return attoreFuori;
	}

	public String getRisultato() {
		return risultato;
	}

	public void setAttoreCasa(String attoreCasa) {
		this.attoreCasa = attoreCasa;
	}

	public void setPunteggio(String punteggio) {
		this.punteggio = punteggio;
	}

	public void setAttoreFuori(String attoreFuori) {
		this.attoreFuori = attoreFuori;
	}

	public void setRisultato(String risultato) {
		this.risultato = risultato;
	}

}