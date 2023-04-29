package test;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellStyle;

//Here is a bean 
public class Risposta implements Serializable{

	private static final long serialVersionUID = 1L;

	private int id;
	private String descrizione;
	private String tipoRisposta;
	private CellStyle cellStyle = null;

	public Risposta() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getTipoRisposta() {
		return tipoRisposta;
	}

	public void setTipoRisposta(String tipoRisposta) {
		this.tipoRisposta = tipoRisposta;
	}

	public CellStyle getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(CellStyle cellStyle) {
		this.cellStyle = cellStyle;
	}

}