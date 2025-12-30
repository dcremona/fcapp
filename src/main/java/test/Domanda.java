package test;

import java.io.Serial;
import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellStyle;

//Here is a bean
public class Domanda implements Serializable{

	@Serial
    private static final long serialVersionUID = 1L;

	private int id;
	private String descrizione;
	private String descrizioneOrig;
	private int rigaXlx;
	private CellStyle cellStyle = null;

	private Risposta risposta;

	public Domanda() {
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

	public Risposta getRisposta() {
		return risposta;
	}

	public void setRisposta(Risposta risposta) {
		this.risposta = risposta;
	}

	public String getDescrizioneOrig() {
		return descrizioneOrig;
	}

	public void setDescrizioneOrig(String descrizioneOrig) {
		this.descrizioneOrig = descrizioneOrig;
	}

	public int getRigaXlx() {
		return rigaXlx;
	}

	public void setRigaXlx(int rigaXlx) {
		this.rigaXlx = rigaXlx;
	}

	public CellStyle getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(CellStyle cellStyle) {
		this.cellStyle = cellStyle;
	}

}