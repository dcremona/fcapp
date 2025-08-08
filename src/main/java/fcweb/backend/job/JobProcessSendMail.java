package fcweb.backend.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import common.util.Utils;
import fcweb.backend.data.RisultatoBean;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ClassificaService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.GiornataDettInfoService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.GiornataService;
import fcweb.utils.Costants;
import fcweb.utils.JasperReporUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

@Controller
public class JobProcessSendMail{

	private static final Logger LOG = LoggerFactory.getLogger(JobProcessSendMail.class);

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private GiornataService giornataController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private GiornataDettInfoService giornataDettInfoController;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResourceLoader resourceLoader;

	public byte[] getJasperRisultati(FcCampionato campionato,
			FcGiornataInfo giornataInfo, Properties p, String pathImg) {
		byte[] b = null;
		try {
			Map<String, Object> params = getMap(giornataInfo.getCodiceGiornata(), pathImg, p, campionato);
			Collection<RisultatoBean> collection = new ArrayList<>();
			collection.add(new RisultatoBean("P","S1",Double.valueOf(6),Double.valueOf(6),Double.valueOf(6),Double.valueOf(6)));
			Resource resource = resourceLoader.getResource("classpath:reports/risultati.jasper");
			InputStream inputStream = resource.getInputStream();
			//b = JasperRunManager.runReportToPdf(inputStream, params, new JRBeanCollectionDataSource(l));
			b = JasperReporUtils.getReportByteCollectionDataSource(inputStream, params, collection);
		} catch (Exception ex2) {
			ex2.printStackTrace();
			LOG.error(ex2.getMessage());
		}
		return b;
	}

	@RequestMapping(value = "/sendMail", method = RequestMethod.POST)
	@ResponseBody
	public void writePdfAndSendMail(FcCampionato campionato,
			FcGiornataInfo giornataInfo, Properties p, String pathImg,
			String pathOutputPdf) throws SQLException,
			AddressException, IOException, MessagingException, NamingException {

		LOG.info("writePdfAndSendMail START");

		Map<String, Object> params = getMap(giornataInfo.getCodiceGiornata(), pathImg, p, campionato);
		Collection<RisultatoBean> l = new ArrayList<>();
		l.add(new RisultatoBean("P","S1",Double.valueOf(6),Double.valueOf(6),Double.valueOf(6),Double.valueOf(6)));
		String destFileName1 = pathOutputPdf + giornataInfo.getDescGiornataFc() + ".pdf";

		Resource resource = resourceLoader.getResource("classpath:reports/risultati.jasper");
		InputStream inputStream = resource.getInputStream();
		FileOutputStream outputStream = new FileOutputStream(new File(destFileName1));
		//JasperRunManager.runReportToPdfStream(inputStream, outputStream, params, new JRBeanCollectionDataSource(l));
		JasperReporUtils.runReportToPdfStream(inputStream, outputStream, params, l);

		Connection conn = null;
		FileOutputStream outputStream2 = null;
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
			parameters.put("DIVISORE", "" + Costants.DIVISORE_100);
			String destFileName2 = pathOutputPdf + "Classifica.pdf";
			Resource resource2 = resourceLoader.getResource("classpath:reports/classifica.jasper");
			InputStream inputStream2 = resource2.getInputStream();

			outputStream2 = new FileOutputStream(new File(destFileName2));
			conn = jdbcTemplate.getDataSource().getConnection();
			//JasperRunManager.runReportToPdfStream(inputStream2, outputStream2, parameters, conn);
			JasperReporUtils.runReportToPdfStream(inputStream2, outputStream2, parameters, conn);

			String email_destinatario = "";
			String ACTIVE_MAIL = p.getProperty("ACTIVE_MAIL");
			if ("true".equals(ACTIVE_MAIL)) {
				List<FcAttore> attori = attoreController.findByActive(true);
				for (FcAttore a : attori) {
					if (a.isNotifiche()) {
						email_destinatario += a.getEmail() + ";";
					}
				}
			} else {
				email_destinatario = p.getProperty("to");
			}

			String[] to = null;
			if (email_destinatario != null && !email_destinatario.equals("")) {
				to = Utils.tornaArrayString(email_destinatario, ";");
			}

			String[] cc = null;
			String[] bcc = null;
			String[] att = new String[] { destFileName1, destFileName2 };
			String subject = "Risultati " + p.getProperty("INFO_RESULT") + " " + giornataInfo.getDescGiornataFc();
			String message = getBody();

			try {
				String from = env.getProperty("spring.mail.secondary.username");
				emailService.sendMail(false,from,to, cc, bcc, subject, message, "text/html", "3", att);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				try {
					String from = env.getProperty("spring.mail.primary.username");
					emailService.sendMail(true,from,to, cc, bcc, subject, message, "text/html", "3", att);
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (outputStream2 != null) {
				outputStream2.close();
			}
		}
		LOG.info("writePdfAndSendMail END");
	}

	private String getBody() {

		String msgHtml = "";
		msgHtml += "<html><head><title>FC</title></head>\n";
		msgHtml += "<body>\n";
		msgHtml += "<p>Sito aggiornato.</p>\n";
		msgHtml += "<br>\n";
		msgHtml += "<br>\n";
		msgHtml += "<p>Ciao Davide</p>\n";
		msgHtml += "</body>\n";
		msgHtml += "<html>";

		return msgHtml;
	}

	private Map<String, Object> getMap(int giornata, String pathImg,
			Properties p, FcCampionato campionato) {

		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(Integer.valueOf(giornata));

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("path_img", pathImg);
		parameters.put("titolo", giornataInfo.getDescGiornataFc());

		List<FcGiornata> listCalen = giornataController.findByFcGiornataInfo(giornataInfo);

		int partita = 0;
		int att = 0;
		for (FcGiornata cal : listCalen) {

			HashMap<String, Collection<RisultatoBean>> mapCasa;
			try {
				mapCasa = buildData(giornata, campionato, cal.getFcAttoreByIdAttoreCasa(), cal.getTotCasa(), giornataInfo, pathImg, true);
				att++;
				parameters.put("sq" + att, cal.getFcAttoreByIdAttoreCasa().getDescAttore());
				parameters.put("data" + att, mapCasa.get("data"));
				parameters.put("dataInfo" + att, mapCasa.get("dataInfo"));
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}

			HashMap<String, Collection<RisultatoBean>> mapFuori;
			try {
				mapFuori = buildData(giornata, campionato, cal.getFcAttoreByIdAttoreFuori(), cal.getTotFuori(), giornataInfo, pathImg, false);
				att++;
				parameters.put("sq" + att, cal.getFcAttoreByIdAttoreFuori().getDescAttore());
				parameters.put("data" + att, mapFuori.get("data"));
				parameters.put("dataInfo" + att, mapFuori.get("dataInfo"));

			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}

			partita++;
			parameters.put("ris" + partita, cal.getGolCasa() + " - " + cal.getGolFuori());

		}

		return parameters;

	}

	private HashMap<String, Collection<RisultatoBean>> buildData(int giornata,
			FcCampionato campionato, FcAttore attore, Double totGiornata,
			FcGiornataInfo giornataInfo, String pathImg, boolean fc)
			throws Exception {

		NumberFormat formatter = new DecimalFormat("#0.00");

		final Collection<RisultatoBean> data = new ArrayList<>();

		List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
		int countD = 0;
		int countC = 0;
		int countA = 0;

		for (FcGiornataDett gd : lGiocatori) {

			final RisultatoBean bean = new RisultatoBean();

			FcGiocatore giocatore = gd.getFcGiocatore();

			if (giocatore != null) {
				FcPagelle pagelle = gd.getFcPagelle();
				if ("S".equals(gd.getFlagAttivo())) {
					if (giocatore.getFcRuolo().getIdRuolo().equals("D")) {
						countD++;
					} else if (giocatore.getFcRuolo().getIdRuolo().equals("C")) {
						countC++;
					} else if (giocatore.getFcRuolo().getIdRuolo().equals("A")) {
						countA++;
					}
				}

				bean.setR(giocatore.getFcRuolo().getIdRuolo());

				if ("S".equals(gd.getFlagAttivo()) && (gd.getOrdinamento() == 14 || gd.getOrdinamento() == 16 || gd.getOrdinamento() == 18)) {
					String descGiocatore = "-0,5 " + giocatore.getCognGiocatore();
					if (descGiocatore.length() > 13) {
						descGiocatore = descGiocatore.substring(0, 13);
					}
					bean.setCalciatore(descGiocatore);
				} else {
					bean.setCalciatore(giocatore.getCognGiocatore());
				}

				if (gd.getVoto() != null) {
					bean.setV(gd.getVoto() / Double.parseDouble("" + Costants.DIVISORE_100));
				}

				bean.setFlag_attivo(gd.getFlagAttivo() == null ? "N" : gd.getFlagAttivo());
				bean.setOrdinamento(gd.getOrdinamento());
				bean.setGoal_realizzato(pagelle.getGoalRealizzato());
				bean.setGoal_subito(pagelle.getGoalSubito());
				bean.setAmmonizione(pagelle.getAmmonizione());
				bean.setEspulsione(pagelle.getEspulsione());
				bean.setRigore_segnato(pagelle.getRigoreSegnato());
				bean.setRigore_fallito(pagelle.getRigoreFallito());
				bean.setRigore_parato(pagelle.getRigoreParato());
				bean.setAutorete(pagelle.getAutorete());
				bean.setAssist(pagelle.getAssist());

				if (pagelle.getG() != null) {
					bean.setG(pagelle.getG() / Double.parseDouble("" + Costants.DIVISORE_100));
				}
				if (pagelle.getCs() != null) {
					bean.setCs(pagelle.getCs() / Double.parseDouble("" + Costants.DIVISORE_100));
				}
				if (pagelle.getTs() != null) {
					bean.setTs(pagelle.getTs() / Double.parseDouble("" + Costants.DIVISORE_100));
				}
				bean.setPath_img(pathImg);
			}
			data.add(bean);
		}

		if (data.size() != 26) {
			int addGioc = 26 - data.size();
			int incr = data.size();
			for (int g = 0; g < addGioc; g++) {
				RisultatoBean r = new RisultatoBean();
				r.setOrdinamento(incr);
				r.setFlag_attivo("N");
				data.add(r);
				incr++;
			}
		}

		final Collection<RisultatoBean> newData = new ArrayList<>();
		RisultatoBean r = new RisultatoBean();
		r.setCalciatore("TITOLARI");
		r.setFlag_attivo("TIT");
		newData.add(r);

		double malus = 0;

		for (RisultatoBean rb : data) {
			if (rb.getOrdinamento() == 12) {
				r = new RisultatoBean();
				r.setCalciatore("PANCHINA");
				r.setFlag_attivo("PAN");
				newData.add(r);
			} else if (rb.getOrdinamento() == 19) {
				r = new RisultatoBean();
				r.setCalciatore("TRIBUNA");
				r.setFlag_attivo("TRI");
				newData.add(r);
			}

			if ("S".equals(rb.getFlag_attivo()) && (rb.getOrdinamento() == 14 || rb.getOrdinamento() == 16 || rb.getOrdinamento() == 18)) {
				malus += 0.5;
			}

			if (rb.getOrdinamento() < 12) {
				newData.add(rb);
			} else if (rb.getOrdinamento() > 11 && rb.getOrdinamento() < 19) {
				newData.add(rb);
			} else {
				newData.add(rb);
			}

		}

		String schema = countD + "-" + countC + "-" + countA;
		String md = getModificatoreDifesa(schema);

		final Collection<RisultatoBean> dataInfo = new ArrayList<>();

		RisultatoBean b = new RisultatoBean();
		b.setDesc("Modulo:");
		b.setValue(schema);
		dataInfo.add(b);

		if (giornataInfo.getIdGiornataFc() < 15) {
			b = new RisultatoBean();
			if (fc) {
				b.setDesc("Fattore Campo:");
				b.setValue("1,5");
			} else {
				b.setDesc("Fattore Campo:");
				b.setValue("0,00");
			}
			dataInfo.add(b);
		}

		if (giornataInfo.getIdGiornataFc() == 15) {
			FcClassifica cl = classificaController.findByFcCampionatoAndFcAttore(campionato, attore);
			String res = "0";
			if (cl.getIdPosiz() == 1) {
				res = "8";
			} else if (cl.getIdPosiz() == 2) {
				res = "6";
			} else if (cl.getIdPosiz() == 3) {
				res = "4";
			} else if (cl.getIdPosiz() == 4) {
				res = "2";
			}
			b = new RisultatoBean();
			b.setDesc("Bonus Quarti:");
			b.setValue(res);
			dataInfo.add(b);
		}

		if (giornataInfo.getIdGiornataFc() == 17) {
			FcClassifica cl = classificaController.findByFcCampionatoAndFcAttore(campionato, attore);
			b = new RisultatoBean();
			b.setDesc("Bonus Semifinali:");
			b.setValue("" + cl.getVinte());
			dataInfo.add(b);
		}

		b = new RisultatoBean();
		b.setDesc("Modificatore Difesa:");
		b.setValue(md);
		dataInfo.add(b);

		b = new RisultatoBean();
		b.setDesc("Malus Secondo Cambio:");
		if (malus == 0) {
			b.setValue(formatter.format(malus));
		} else {
			b.setValue("-" + formatter.format(malus));
		}
		dataInfo.add(b);

		String totaleGiornata = "";
		if (totGiornata != null) {
			totaleGiornata = formatter.format(totGiornata.doubleValue() / Double.parseDouble("" + Costants.DIVISORE_100));
		}

		b = new RisultatoBean();
		b.setDesc("Totale Giornata:");
		b.setValue(totaleGiornata);
		dataInfo.add(b);

		FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
		String puntiTotali = "";
		if (totPunti != null) {
			puntiTotali = formatter.format(totPunti.getTotPtRosa() / Double.parseDouble("" + Costants.DIVISORE_100));
		}

		b = new RisultatoBean();
		b.setDesc("Totale Punteggio Rosa:");
		b.setValue(puntiTotali);
		dataInfo.add(b);

		b = new RisultatoBean();
		b.setDesc("Totale Punteggio TvsT:");
		b.setValue(""+totPunti.getPtTvsT());
		dataInfo.add(b);

		FcGiornataDettInfo info = giornataDettInfoController.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);

		b = new RisultatoBean();
		b.setDesc("Inviata alle:");
		b.setValue((info == null ? "" : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss")));
		dataInfo.add(b);

		HashMap<String, Collection<RisultatoBean>> result = new HashMap<>();
		result.put("data", newData);
		result.put("dataInfo", dataInfo);

		return result;
	}

	private String getModificatoreDifesa(String value) {
		String ret = "";

		if (value.equals("5-4-1")) {
			ret = "2";
		} else if (value.equals("5-3-2")) {
			ret = "1";
		} else if (value.equals("4-5-1")) {
			ret = "1";
		} else if (value.equals("4-3-3")) {
			ret = "-1";
		} else if (value.equals("3-4-3")) {
			ret = "-2";
		} else {
			ret = "0";
		}

		return ret;
	}

}
