package fcweb.backend.data;

import java.io.Serializable;

//Here is a bean
public class RisultatoBean implements Serializable{

	private static final long serialVersionUID = 1L;

	private String r = null;
	private String calciatore = "";
	private Double v;
	private String flag_attivo = "";
	private int ordinamento = 0;
	private int goal_realizzato = 0;
	private int goal_subito = 0;
	private int ammonizione = 0;
	private int espulsione = 0;
	private int rigore_segnato = 0;
	private int rigore_fallito = 0;
	private int rigore_parato = 0;
	private int autorete = 0;
	private int assist = 0;
	private int gv = 0;
	private Double g;
	private Double cs;
	private Double ts;
	private String desc = "";
	private String value = "";
	private String path_img = "";

	public RisultatoBean() {
		super();
	}

	public RisultatoBean(String r, String calciatore, Double v, Double g,
			Double cs, Double ts) {
		super();
		this.r = r;
		this.calciatore = calciatore;
		this.v = v;
		this.g = g;
		this.cs = cs;
		this.ts = ts;
	}

	public String getR() {
		return r;
	}

	public void setR(String r) {
		this.r = r;
	}

	public String getCalciatore() {
		return calciatore;
	}

	public void setCalciatore(String calciatore) {
		this.calciatore = calciatore;
	}

	public Double getV() {
		return v;
	}

	public void setV(Double v) {
		this.v = v;
	}

	public String getFlag_attivo() {
		return flag_attivo;
	}

	public void setFlag_attivo(String flag_attivo) {
		this.flag_attivo = flag_attivo;
	}

	public int getOrdinamento() {
		return ordinamento;
	}

	public void setOrdinamento(int ordinamento) {
		this.ordinamento = ordinamento;
	}

	public int getGoal_realizzato() {
		return goal_realizzato;
	}

	public void setGoal_realizzato(int goal_realizzato) {
		this.goal_realizzato = goal_realizzato;
	}

	public int getGoal_subito() {
		return goal_subito;
	}

	public void setGoal_subito(int goal_subito) {
		this.goal_subito = goal_subito;
	}

	public int getAmmonizione() {
		return ammonizione;
	}

	public void setAmmonizione(int ammonizione) {
		this.ammonizione = ammonizione;
	}

	public int getEspulsione() {
		return espulsione;
	}

	public void setEspulsione(int espulsione) {
		this.espulsione = espulsione;
	}

	public int getRigore_segnato() {
		return rigore_segnato;
	}

	public void setRigore_segnato(int rigore_segnato) {
		this.rigore_segnato = rigore_segnato;
	}

	public int getRigore_fallito() {
		return rigore_fallito;
	}

	public void setRigore_fallito(int rigore_fallito) {
		this.rigore_fallito = rigore_fallito;
	}

	public int getRigore_parato() {
		return rigore_parato;
	}

	public void setRigore_parato(int rigore_parato) {
		this.rigore_parato = rigore_parato;
	}

	public int getAutorete() {
		return autorete;
	}

	public void setAutorete(int autorete) {
		this.autorete = autorete;
	}

	public int getAssist() {
		return assist;
	}

	public void setAssist(int assist) {
		this.assist = assist;
	}

	public Double getG() {
		return g;
	}

	public void setG(Double g) {
		this.g = g;
	}

	public Double getCs() {
		return cs;
	}

	public void setCs(Double cs) {
		this.cs = cs;
	}

	public Double getTs() {
		return ts;
	}

	public void setTs(Double ts) {
		this.ts = ts;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getGv() {
		return gv;
	}

	public void setGv(int gv) {
		this.gv = gv;
	}

	public String getPath_img() {
		return path_img;
	}

	public void setPath_img(String path_img) {
		this.path_img = path_img;
	}

}
