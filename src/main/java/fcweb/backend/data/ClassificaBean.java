package fcweb.backend.data;

import java.io.Serializable;

//Here is a bean 
public class ClassificaBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private String giornata;
	private String squadra;
	private Double punti;
	private Double totPunti;
	private Double totPuntiParziale;
	private Double puntiGiornata1;
	private Double puntiGiornata2;
	private Double puntiGiornata3;
	private Double puntiGiornata4;
	private Double puntiGiornata5;
	private Double puntiGiornata6;
	private Double puntiGiornata7;
	private Double ptTvst;

	public String getSquadra() {
		return squadra;
	}

	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}

	public Double getPunti() {
		return punti;
	}

	public void setPunti(Double punti) {
		this.punti = punti;
	}

	public Double getTotPunti() {
		return totPunti;
	}

	public void setTotPunti(Double totPunti) {
		this.totPunti = totPunti;
	}

	public Double getTotPuntiParziale() {
		return totPuntiParziale;
	}

	public void setTotPuntiParziale(Double totPuntiParziale) {
		this.totPuntiParziale = totPuntiParziale;
	}

	public Double getPuntiGiornata1() {
		return puntiGiornata1;
	}

	public void setPuntiGiornata1(Double puntiGiornata1) {
		this.puntiGiornata1 = puntiGiornata1;
	}

	public Double getPuntiGiornata2() {
		return puntiGiornata2;
	}

	public void setPuntiGiornata2(Double puntiGiornata2) {
		this.puntiGiornata2 = puntiGiornata2;
	}

	public Double getPuntiGiornata3() {
		return puntiGiornata3;
	}

	public void setPuntiGiornata3(Double puntiGiornata3) {
		this.puntiGiornata3 = puntiGiornata3;
	}

	public Double getPuntiGiornata4() {
		return puntiGiornata4;
	}

	public void setPuntiGiornata4(Double puntiGiornata4) {
		this.puntiGiornata4 = puntiGiornata4;
	}

	public Double getPuntiGiornata5() {
		return puntiGiornata5;
	}

	public void setPuntiGiornata5(Double puntiGiornata5) {
		this.puntiGiornata5 = puntiGiornata5;
	}

	public Double getPuntiGiornata6() {
		return puntiGiornata6;
	}

	public void setPuntiGiornata6(Double puntiGiornata6) {
		this.puntiGiornata6 = puntiGiornata6;
	}

	public Double getPuntiGiornata7() {
		return puntiGiornata7;
	}

	public void setPuntiGiornata7(Double puntiGiornata7) {
		this.puntiGiornata7 = puntiGiornata7;
	}

	public String getGiornata() {
		return giornata;
	}

	public void setGiornata(String giornata) {
		this.giornata = giornata;
	}

	public Double getPtTvst() {
		return ptTvst;
	}

	public void setPtTvst(Double ptTvst) {
		this.ptTvst = ptTvst;
	}
	

}