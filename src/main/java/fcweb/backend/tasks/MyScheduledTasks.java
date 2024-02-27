package fcweb.backend.tasks;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import common.util.Utils;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.job.JobProcessFileCsv;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.job.JobProcessSendMail;
import fcweb.backend.service.CampionatoService;
import fcweb.backend.service.GiornataGiocatoreService;
import fcweb.backend.service.PagelleService;
import fcweb.backend.service.ProprietaService;

@Component
public class MyScheduledTasks{

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private static final String fileSep = System.getProperty("file.separator");

	@Autowired
	private Environment env;

	@Autowired
	private ProprietaService proprietaController;

	@Autowired
	private CampionatoService campionatoController;

	@Autowired
	private PagelleService pagelleController;

	@Autowired
	private JobProcessFileCsv jobProcessFileCsv;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	@Autowired
	private JobProcessSendMail jobProcessSendMail;

	@Autowired
	private GiornataGiocatoreService giornataGiocatoreService;

	@Bean
	public String getCronValueUfficiosi() {
		FcProperties p = proprietaController.findByKey("ufficiosi.cron.expression");
		if (p != null) {
			LOG.info("Ufficiosi cron " + p.getValue());
			return p.getValue();
		} else {
			return "0 30 16 * * *";
		}
	}

	@Scheduled(cron = "#{@getCronValueUfficiosi}")
	// @Scheduled(cron = "${ufficiosi.cron.expression}")
	// @Scheduled(fixedRate = 6000)
	// @Scheduled(cron = "0 30 8 * * *")
	public void jobUfficiosi() throws Exception {

		LOG.info("jobUfficiosi start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

		processResult(false);

		LOG.info("jobUfficiosi end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
	}

	@Bean
	public String getCronValueUfficiali() {
		FcProperties p = proprietaController.findByKey("ufficiali.cron.expression");
		if (p != null) {
			LOG.info("Ufficiali cron " + p.getValue());
			return p.getValue();
		} else {
			return "0 30 16 * * *";
		}
	}

	@Scheduled(cron = "#{@getCronValueUfficiali}")
	// @Scheduled(cron = "${ufficiali.cron.expression}")
	// @Scheduled(cron = "*/60 * * * * *")
	// @Scheduled(cron = "0 30 16 * * *")
	public void jobUfficiali() throws Exception {

		LOG.info("jobUfficiali start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

		processResult(true);

		LOG.info("jobUfficiali end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
	}

	private void processResult(boolean flagUfficiali) throws Exception {

		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
		LOG.info("DAY_OF_WEEK " + day_of_week);

		String votiExcel = "Voti-Ufficiosi-Excel";
		String info_result = "UFFICIOSI";
		if (flagUfficiali) {
			votiExcel = "Voti-Ufficiali-Excel";
			info_result = "UFFICIALI";
		}

		List<FcProperties> lProprieta = proprietaController.findAll();
		if (lProprieta.size() == 0) {
			LOG.error("error lProprieta size" + lProprieta.size());
			return;
		}
		Properties p = new Properties();
		for (FcProperties prop : lProprieta) {
			p.setProperty(prop.getKey(), prop.getValue());
		}
		p.setProperty("INFO_RESULT", info_result);

		String springMailPassword = (String) env.getProperty("spring.mail.password");
		p.setProperty("mail.password", springMailPassword);

		String startJob = day_of_week + "_" + info_result;
		LOG.info("startJob " + startJob);
		String valueStart = p.getProperty(startJob);
		LOG.info("VALUE_START " + valueStart);
		if ("0".equals(valueStart)) {
			LOG.info("NOT ACTIVE JOB " + startJob);
			return;
		}

		FcPagelle currentGG = pagelleController.findCurrentGiornata();
		FcGiornataInfo giornataInfo = currentGG.getFcGiornataInfo();

		LOG.info("currentGG: " + giornataInfo.getCodiceGiornata());
		FcCampionato campionato = campionatoController.findByActive(true);

		String rootPathOutputPdf = (String) p.get("PATH_OUTPUT_PDF");
		String idCampionato = "" + campionato.getIdCampionato();
		String pathCampionato = (String) env.getProperty("spring.datasource.username");
		String pathOutput = rootPathOutputPdf + pathCampionato + fileSep + "Campionato" + idCampionato;

		int ggFc = giornataInfo.getCodiceGiornata();
		if (idCampionato.equals("2")) {
			ggFc = ggFc - 19;
		}
		String pathOutputPdf = pathOutput + fileSep + ggFc;
		File f = new File(pathOutputPdf);
		if (!f.exists()) {
			boolean flag = f.mkdir();
			if (!flag) {
				LOG.info("NO pathOutputPdf exist" + pathOutputPdf);
				return;
			}
		}

		String pathImg = "images/";

		String basePathData = (String) p.get("PATH_TMP");
		LOG.info("basePathData " + basePathData);

		Thread.sleep(60000L);

		String urlFanta = (String) p.get("URL_FANTA");
		String httpurl = urlFanta + votiExcel + ".asp?giornataScelta=" + giornataInfo.getCodiceGiornata();
		jobProcessFileCsv.downloadCsv(httpurl, basePathData, "voti_" + giornataInfo.getCodiceGiornata(), 3);

		String fileName = basePathData + "/voti_" + giornataInfo.getCodiceGiornata() + ".csv";
		jobProcessGiornata.aggiornamentoPFGiornata(p, fileName, "" + giornataInfo.getCodiceGiornata());

		jobProcessGiornata.checkSeiPolitico(giornataInfo.getCodiceGiornata());

		Thread.sleep(60000L);

		jobProcessGiornata.algoritmo(giornataInfo.getCodiceGiornata(), campionato, -1, true);
		jobProcessGiornata.statistiche(campionato);

		jobProcessGiornata.aggiornaVotiGiocatori(giornataInfo.getCodiceGiornata(), -1, true);
		jobProcessGiornata.aggiornaTotRosa(idCampionato, giornataInfo.getCodiceGiornata());
		jobProcessGiornata.aggiornaScore(giornataInfo.getCodiceGiornata(), "tot_pt", "score");
		jobProcessGiornata.aggiornaScore(giornataInfo.getCodiceGiornata(), "tot_pt_old", "score_old");
		jobProcessGiornata.aggiornaScore(giornataInfo.getCodiceGiornata(), "tot_pt_old", "score_grand_prix");

		Thread.sleep(60000L);

		jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, p, pathImg, pathOutputPdf + fileSep);

	}
	
	//@Scheduled(cron = "*/60 * * * * *")
	@Scheduled(cron = "0 0 6 * * *")
	public void jobSqualificaInfortunati() throws Exception {

		LOG.info("jobSqualificaInfortunati start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

		FcPagelle currentGG = pagelleController.findCurrentGiornata();
		FcGiornataInfo giornataInfo = currentGG.getFcGiornataInfo();

		LOG.info("currentGG: " + giornataInfo.getCodiceGiornata());

		giornataGiocatoreService.deleteByCustonm(giornataInfo);
		
		List<FcProperties> lProprieta = proprietaController.findAll();
		if (lProprieta.size() == 0) {
			LOG.error("error lProprieta size" + lProprieta.size());
			return;
		}
		Properties p = new Properties();
		for (FcProperties prop : lProprieta) {
			p.setProperty(prop.getKey(), prop.getValue());
		}
		String urlFanta = (String) p.get("URL_FANTA");
		String basePathData = (String) p.get("PATH_TMP");
		String basePath = basePathData;
		LOG.info("basePathData " + basePathData);
		
		// **************************************
		// DOWNLOAD FILE SQUALIFICATI
		// **************************************
		String httpUrlSqualificati = urlFanta + "giocatori-squalificati.asp";
		LOG.info("httpUrlSqualificati " + httpUrlSqualificati);
		String fileName1 = "SQUALIFICATI_" + giornataInfo.getCodiceGiornata();
		JobProcessFileCsv jobCsv = new JobProcessFileCsv();
		jobCsv.downloadCsvSqualificatiInfortunati(httpUrlSqualificati, basePath, fileName1);

		String fileName = basePathData + fileName1 + ".csv";
		jobProcessGiornata.initDbGiornataGiocatore(giornataInfo,fileName,true,false);
		
		// **************************************
		// DOWNLOAD FILE INFORTUNATI
		// **************************************
		String httpUrlInfortunati = urlFanta + "giocatori-infortunati.asp";
		LOG.info("httpUrlInfortunati " + httpUrlInfortunati);
		String fileName2 = "INFORTUNATI_" + giornataInfo.getCodiceGiornata();
		jobCsv.downloadCsvSqualificatiInfortunati(httpUrlInfortunati, basePath, fileName2);

		fileName = basePathData + fileName2 + ".csv";
		jobProcessGiornata.initDbGiornataGiocatore(giornataInfo,fileName,false,true);

		LOG.info("jobSqualificaInfortunati end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
	}
	
}