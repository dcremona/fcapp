package fcapp.backend.tasks;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.job.JobProcessFileCsv;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.job.JobProcessSendMail;
import fcapp.backend.service.CampionatoService;
import fcapp.backend.service.GiornataGiocatoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.PagelleService;
import fcapp.backend.service.ProprietaService;
import fcapp.utils.Costants;
import fcapp.utils.Utils;

@Component
public class MyScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(MyScheduledTasks.class);

	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	private static final String PROPERTY_URL_FANTA = "URL_FANTA";
	private static final String PROPERTY_PATH_OUTPUT_PDF = "PATH_OUTPUT_PDF";
	private static final String PROPERTY_FOLDER_PDF = "folderPdf";
	private static final String PROPERTY_PATH_TMP = "PATH_TMP";
	private static final String PROPERTY_FUSO_ORARIO = "FUSO_ORARIO";
//	private static final String PROPERTY_INFO_RESULT = "INFO_RESULT";

	private static final String RESULT_UFFICIOSI = "UFFICIOSI";
	private static final String RESULT_UFFICIALI = "UFFICIALI";

	private static final String FOLDER_CAMPIONATO = "Campionato";

	private static final String CSV_EXTENSION = ".csv";

	private static final String FILE_VOTI_PREFIX = "voti_";
	private static final String FILE_SQUALIFICATI_PREFIX = "SQUALIFICATI_";
	private static final String FILE_INFORTUNATI_PREFIX = "INFORTUNATI_";
	private static final String FILE_PROBABILI_PREFIX = "PROBABILI_";

	private static final String FILE_SQUALIFICATI_INFORTUNATI_FANTA_GAZZETTA_PREFIX = "SQUALIFICATI_INFORTUNATI_FANTA_GAZZETTA_";

	private static final String FILE_PROBABILI_FANTA_GAZZETTA_PREFIX = "PROBABILI_FANTA_GAZZETTA_";

	private static final String URL_SQUALIFICATI = "giocatori-squalificati.asp";

	private static final String URL_INFORTUNATI = "giocatori-infortunati.asp";

	private static final String URL_PROBABILI = "probabili-formazioni-complete-serie-a-live.asp";

	private static final String VOTI_EXPORT_PATH = "api/voti/export";

	private static final String VOTI_EXPORT_PARAMETERS = "?variante=ufficiali" + "&stagione=2026_2027"
			+ "&bonusTipo=standard" + "&giornata=";

	private static final String SCORE_TOTAL = "tot_pt";
	private static final String SCORE_TOTAL_OLD = "tot_pt_old";
	private static final String SCORE = "score";
	private static final String SCORE_OLD = "score_old";
	private static final String SCORE_GRAND_PRIX = "score_grand_prix";

//	private static final String FANTA_GAZZETTA_ENABLED = "FANTA_GAZZETTA";

	private static final long WAIT_TIME_MILLIS = 60_000L;

	private static final int DEFAULT_GIORNATA = 1;
	private static final String CAMPIONATO_SPECIAL = "2";
	private static final int CAMPIONATO_SPECIAL_OFFSET = 19;

//	private static final int CSV_GIOCATORI_SQUALIFICATI = 1;
//	private static final int CSV_GIOCATORI_INFORTUNATI = 2;

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	private final Environment env;
	private final ProprietaService proprietaService;
	private final CampionatoService campionatoService;
	private final PagelleService pagelleService;
	private final GiornataGiocatoreService giornataGiocatoreService;
	private final GiornataInfoService giornataInfoService;
	private final JobProcessFileCsv jobProcessFileCsv;
	private final JobProcessGiornata jobProcessGiornata;
	private final JobProcessSendMail jobProcessSendMail;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	public MyScheduledTasks(Environment env, ProprietaService proprietaService, CampionatoService campionatoService,
			PagelleService pagelleService, GiornataGiocatoreService giornataGiocatoreService,
			GiornataInfoService giornataInfoService, JobProcessFileCsv jobProcessFileCsv,
			JobProcessGiornata jobProcessGiornata, JobProcessSendMail jobProcessSendMail) {

		this.env = env;
		this.proprietaService = proprietaService;
		this.campionatoService = campionatoService;
		this.pagelleService = pagelleService;
		this.giornataGiocatoreService = giornataGiocatoreService;
		this.giornataInfoService = giornataInfoService;
		this.jobProcessFileCsv = jobProcessFileCsv;
		this.jobProcessGiornata = jobProcessGiornata;
		this.jobProcessSendMail = jobProcessSendMail;
	}

	// -------------------------------------------------------------------------
	// Scheduled jobs
	// -------------------------------------------------------------------------

	@Scheduled(cron = "#{@getCronValueUfficiosi}")
	// @Scheduled(cron = "${ufficiosi.cron.expression}")
	// @Scheduled(fixedRate = 6000)
	// @Scheduled(cron = "0 18 19 * * *")
	public void jobUfficiosi() throws Exception {

		log.info("jobUfficiosi start at {}", getCurrentDateTime());

		processResult(false);

		log.info("jobUfficiosi end at {}", getCurrentDateTime());
	}

	@Scheduled(cron = "#{@getCronValueUfficiali}")
	// @Scheduled(cron = "${ufficiali.cron.expression}")
	// @Scheduled(cron = "*/60 * * * * *")
	// @Scheduled(cron = "0 30 16 * * *")
	public void jobUfficiali() throws Exception {

		log.info("jobUfficiali start at {}", getCurrentDateTime());

		processResult(true);

		log.info("jobUfficiali end at {}", getCurrentDateTime());
	}

	@Scheduled(cron = "#{@getCronValueInfoGiocatore}")
	// @Scheduled(cron = "*/60 * * * * *")
	// @Scheduled(cron = "0 0 6 * * *")
	public void jobSqualificaInfortunati() throws Exception {

		log.info("jobSqualificaInfortunati start at {}", getCurrentDateTime());

		processSqualificaInfortunati();

		log.info("jobSqualificaInfortunati end at {}", getCurrentDateTime());
	}

	// -------------------------------------------------------------------------
	// Main result processing
	// -------------------------------------------------------------------------

	private void processResult(boolean ufficiali) throws Exception {

		int dayOfWeek = getCurrentDayOfWeek();
		String infoResult = getResultType(ufficiali);

		log.info("dayOfWeek {}", dayOfWeek);

		Properties properties = loadProperties();

		if (!isJobActive(properties, dayOfWeek, infoResult)) {

			return;
		}

		FcPagelle currentGG = pagelleService.findCurrentGiornata();

		FcGiornataInfo giornataInfo = currentGG.getFcGiornataInfo();

		log.info("currentGG: {}", giornataInfo.getCodiceGiornata());

		FcCampionato campionato = campionatoService.findByActive(true);

		String idCampionato = String.valueOf(campionato.getIdCampionato());

		String pathOutputPdf = prepareOutputDirectory(campionato, giornataInfo);

		String basePathData = env.getProperty(PROPERTY_PATH_TMP);

		log.info("basePathData {}", basePathData);

		waitBeforeProcessing();

		downloadAndProcessVoti(properties, giornataInfo, basePathData);

		processGiornata(giornataInfo, campionato, idCampionato);

		waitBeforeSendingMail();

		sendResultMail(properties, campionato, giornataInfo, pathOutputPdf);
	}

	// -------------------------------------------------------------------------
	// Properties
	// -------------------------------------------------------------------------

	private Properties loadProperties() {

		List<FcProperties> properties = proprietaService.findAll();

		if (properties.isEmpty()) {

			log.error("error lProprietà size 0");

			return new Properties();
		}

		Properties result = new Properties();

		for (FcProperties property : properties) {

			result.setProperty(property.getKey(), property.getValue());
		}

		return result;
	}

	private boolean isJobActive(Properties properties, int dayOfWeek, String infoResult) {

		String startJob = dayOfWeek + "_" + infoResult;

		log.info("startJob {}", startJob);

		String valueStart = properties.getProperty(startJob);

		log.info("VALUE_START {}", valueStart);

		if ("0".equals(valueStart)) {

			log.info("NOT ACTIVE JOB {}", startJob);

			return false;
		}

		return true;
	}

	// -------------------------------------------------------------------------
	// Giornata / output directory
	// -------------------------------------------------------------------------

	private String prepareOutputDirectory(FcCampionato campionato, FcGiornataInfo giornataInfo) {

		String rootPathOutputPdf = env.getProperty(PROPERTY_PATH_OUTPUT_PDF);

		String folderPdf = env.getProperty(PROPERTY_FOLDER_PDF);

		String idCampionato = String.valueOf(campionato.getIdCampionato());

		String pathOutput = rootPathOutputPdf + folderPdf + File.separator + FOLDER_CAMPIONATO + idCampionato;

		int ggFc = giornataInfo.getCodiceGiornata();

		if (CAMPIONATO_SPECIAL.equals(idCampionato)) {

			ggFc -= CAMPIONATO_SPECIAL_OFFSET;
		}

		String pathOutputPdf = pathOutput + File.separator + ggFc;

		createOutputDirectory(pathOutputPdf);

		return pathOutputPdf;
	}

	private void createOutputDirectory(String pathOutputPdf) {

		File directory = new File(pathOutputPdf);

		if (directory.exists()) {
			return;
		}

		boolean created = directory.mkdir();

		if (!created) {

			log.info("NO pathOutputPdf exist{}", pathOutputPdf);
		}
	}

	// -------------------------------------------------------------------------
	// Download / processing voti
	// -------------------------------------------------------------------------

	private void downloadAndProcessVoti(Properties properties, FcGiornataInfo giornataInfo, String basePathData)
			throws Exception {

		int codiceGiornata = giornataInfo.getCodiceGiornata();

		String urlFanta = properties.getProperty(PROPERTY_URL_FANTA);

		String httpUrl = buildVotiUrl(urlFanta, codiceGiornata);

		String filePrefix = FILE_VOTI_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadVotiXlsxCsv(httpUrl, basePathData, filePrefix);

		String fileName = basePathData + "/" + filePrefix + CSV_EXTENSION;

		jobProcessGiornata.aggiornamentoPFGiornata(properties, fileName, String.valueOf(codiceGiornata));

		jobProcessGiornata.checkSeiPolitico(codiceGiornata);
	}

	private String buildVotiUrl(String urlFanta, int codiceGiornata) {

		return urlFanta + VOTI_EXPORT_PATH + VOTI_EXPORT_PARAMETERS + codiceGiornata;
	}

	// -------------------------------------------------------------------------
	// Giornata processing
	// -------------------------------------------------------------------------

	private void processGiornata(FcGiornataInfo giornataInfo, FcCampionato campionato, String idCampionato)
			throws Exception {

		int codiceGiornata = giornataInfo.getCodiceGiornata();

		waitBeforeProcessing();

		jobProcessGiornata.algoritmo(codiceGiornata, campionato, -1, true);

		jobProcessGiornata.statistiche(campionato);

		jobProcessGiornata.aggiornaVotiGiocatori(codiceGiornata, -1, true);

		jobProcessGiornata.aggiornaTotRosa(idCampionato, codiceGiornata);

		updateScores(codiceGiornata);
	}

	private void updateScores(int codiceGiornata) {

		jobProcessGiornata.aggiornaScore(codiceGiornata, SCORE_TOTAL, SCORE);

		jobProcessGiornata.aggiornaScore(codiceGiornata, SCORE_TOTAL_OLD, SCORE_OLD);

		jobProcessGiornata.aggiornaScore(codiceGiornata, SCORE_TOTAL_OLD, SCORE_GRAND_PRIX);
	}

	// -------------------------------------------------------------------------
	// Mail
	// -------------------------------------------------------------------------

	private void sendResultMail(Properties properties, FcCampionato campionato, FcGiornataInfo giornataInfo,
			String pathOutputPdf) throws Exception {

		jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, properties, pathOutputPdf + File.separator);
	}

	// -------------------------------------------------------------------------
	// Squalificati / Infortunati / Probabili
	// -------------------------------------------------------------------------

	private void processSqualificaInfortunati() throws Exception {

		Properties properties = loadProperties();

		if (properties.isEmpty()) {
			return;
		}

		String urlFanta = properties.getProperty(PROPERTY_URL_FANTA);

		String basePathData = env.getProperty(PROPERTY_PATH_TMP);

		FcGiornataInfo giornataInfo = findCurrentGiornataInfo();

		String fusoOrario = properties.getProperty(PROPERTY_FUSO_ORARIO);

		Map<?, ?> nextDate = Utils.getNextDate(giornataInfo);

		String nextDateFormat = String.valueOf(nextDate.get("2"));

		long millisDiff = calculateMillisDiff(nextDateFormat, fusoOrario);

		log.info("millisDiff : {}", millisDiff);

		if (millisDiff == 0) {

			log.error("jobSqualificaInfortunati STOP NO PROCESS");

			return;
		}

		giornataGiocatoreService.deleteByCustonm(giornataInfo);

		log.info("basePathData {}", basePathData);

		processPlayerFiles(urlFanta, basePathData, giornataInfo);
	}

	private FcGiornataInfo findCurrentGiornataInfo() {

		FcPagelle currentGG = pagelleService.findCurrentGiornata();

		if (currentGG != null) {

			FcGiornataInfo giornataInfo = currentGG.getFcGiornataInfo();

			log.info("currentGG: {}", giornataInfo.getCodiceGiornata());

			return giornataInfo;
		}

		return giornataInfoService.findByCodiceGiornata(DEFAULT_GIORNATA);
	}

	private long calculateMillisDiff(String nextDateFormat, String fusoOrario) {

		try {

			return Utils.getMillisDiff(nextDateFormat, fusoOrario);

		} catch (Exception e) {

			log.error("Error calculating millisDiff", e);

			return 0;
		}
	}

	private void processPlayerFiles(String urlFanta, String basePathData, FcGiornataInfo giornataInfo)
			throws Exception {

		/*
		 * Manteniamo il comportamento originale: attualmente viene utilizzata
		 * FantaGazzetta.
		 */
		boolean bFantaGazzetta = true;

		if (bFantaGazzetta) {

			processFantaGazzettaFiles(basePathData, giornataInfo);

		} else {

			processLegacyPlayerFiles(urlFanta, basePathData, giornataInfo);
		}
	}

	// -------------------------------------------------------------------------
	// FantaGazzetta
	// -------------------------------------------------------------------------

	private void processFantaGazzettaFiles(String basePathData, FcGiornataInfo giornataInfo) throws Exception {

		int codiceGiornata = giornataInfo.getCodiceGiornata();

		processSqualificatiInfortunatiFantaGazzetta(basePathData, giornataInfo, codiceGiornata);

		processProbabiliFantaGazzetta(basePathData, codiceGiornata);
	}

	private void processSqualificatiInfortunatiFantaGazzetta(String basePathData, FcGiornataInfo giornataInfo,
			int codiceGiornata) throws Exception {

		String filePrefix = FILE_SQUALIFICATI_INFORTUNATI_FANTA_GAZZETTA_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadCsvSqualificatiInfortunatiFantaGazzetta(Costants.HTTP_URL_FANTAGAZZETTA_PROBABILI,
				basePathData, filePrefix);

		String fileName = buildCsvFileName(basePathData, filePrefix);

		jobProcessGiornata.initDbSqualificatiInfortunatiFantaGazzetta(giornataInfo, fileName);
	}

	private void processProbabiliFantaGazzetta(String basePathData, int codiceGiornata) throws Exception {

		String filePrefix = FILE_PROBABILI_FANTA_GAZZETTA_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadCsvProbabiliFantaGazzetta(Costants.HTTP_URL_FANTAGAZZETTA_PROBABILI, basePathData,
				filePrefix);

		String fileName = buildCsvFileName(basePathData, filePrefix);

		jobProcessGiornata.initDbProbabiliFantaGazzetta(fileName);
	}

	// -------------------------------------------------------------------------
	// Legacy source
	// -------------------------------------------------------------------------

	private void processLegacyPlayerFiles(String urlFanta, String basePathData, FcGiornataInfo giornataInfo)
			throws Exception {

		int codiceGiornata = giornataInfo.getCodiceGiornata();

		processLegacySqualificati(urlFanta, basePathData, giornataInfo, codiceGiornata);

		processLegacyInfortunati(urlFanta, basePathData, giornataInfo, codiceGiornata);

		processLegacyProbabili(urlFanta, basePathData, giornataInfo, codiceGiornata);
	}

	private void processLegacySqualificati(String urlFanta, String basePathData, FcGiornataInfo giornataInfo,
			int codiceGiornata) throws Exception {

		String httpUrl = urlFanta + URL_SQUALIFICATI;

		log.info("httpUrlSqualificati {}", httpUrl);

		String filePrefix = FILE_SQUALIFICATI_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadCsvSqualificatiInfortunati(httpUrl, basePathData, filePrefix);

		String fileName = buildCsvFileName(basePathData, filePrefix);

		jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, true, false);
	}

	private void processLegacyInfortunati(String urlFanta, String basePathData, FcGiornataInfo giornataInfo,
			int codiceGiornata) throws Exception {

		String httpUrl = urlFanta + URL_INFORTUNATI;

		log.info("httpUrlInfortunati {}", httpUrl);

		String filePrefix = FILE_INFORTUNATI_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadCsvSqualificatiInfortunati(httpUrl, basePathData, filePrefix);

		String fileName = buildCsvFileName(basePathData, filePrefix);

		jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, false, true);
	}

	private void processLegacyProbabili(String urlFanta, String basePathData, FcGiornataInfo giornataInfo,
			int codiceGiornata) throws Exception {

		String httpUrl = urlFanta + URL_PROBABILI;

		log.info("httpUrlProbabili {}", httpUrl);

		String filePrefix = FILE_PROBABILI_PREFIX + codiceGiornata;

		jobProcessFileCsv.downloadCsvProbabili(httpUrl, basePathData, filePrefix);

		String fileName = buildCsvFileName(basePathData, filePrefix);

		jobProcessGiornata.initDbProbabili(fileName);
	}

	// -------------------------------------------------------------------------
	// Utility methods
	// -------------------------------------------------------------------------

	private String buildCsvFileName(String basePathData, String filePrefix) {

		return basePathData + filePrefix + CSV_EXTENSION;
	}

	private int getCurrentDayOfWeek() {

		Calendar calendar = Calendar.getInstance();

		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	private String getResultType(boolean ufficiali) {

		return ufficiali ? RESULT_UFFICIALI : RESULT_UFFICIOSI;
	}

	private String getCurrentDateTime() {

		return Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss");
	}

	private void waitBeforeProcessing() throws InterruptedException {

		Thread.sleep(WAIT_TIME_MILLIS);
	}

	private void waitBeforeSendingMail() throws InterruptedException {

		Thread.sleep(WAIT_TIME_MILLIS);
	}

	// -------------------------------------------------------------------------
	// Test job - disabled
	// -------------------------------------------------------------------------
//      @Scheduled(cron = "*/120 * * * * *")
//      public void jobTest() throws Exception {
//     
//          log.info(
//              "jobTest start at {}",
//              getCurrentDateTime()
//          );
//     
//          log.info(
//              "jobTest end at {}",
//              getCurrentDateTime()
//          );
//      }

}