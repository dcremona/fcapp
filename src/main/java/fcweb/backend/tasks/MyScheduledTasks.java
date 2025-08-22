package fcweb.backend.tasks;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import fcweb.backend.service.GiornataInfoRepository;
import fcweb.backend.service.PagelleService;
import fcweb.backend.service.ProprietaService;

@Component
public class MyScheduledTasks {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String FILE_SEP = System.getProperty("file.separator");

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
    
    @Autowired
    private GiornataInfoRepository giornataInfoRepository;

    @Scheduled(cron = "#{@getCronValueUfficiosi}")
    // @Scheduled(cron = "${ufficiosi.cron.expression}")
    // @Scheduled(fixedRate = 6000)
    // @Scheduled(cron = "0 59 12 * * *")
    public void jobUfficiosi() throws Exception {

        log.info("jobUfficiosi start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

        processResult(false);

        log.info("jobUfficiosi end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
    }

    @Scheduled(cron = "#{@getCronValueUfficiali}")
    // @Scheduled(cron = "${ufficiali.cron.expression}")
    // @Scheduled(cron = "*/60 * * * * *")
    // @Scheduled(cron = "0 30 16 * * *")
    public void jobUfficiali() throws Exception {

        log.info("jobUfficiali start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

        processResult(true);

        log.info("jobUfficiali end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
    }

    private void processResult(boolean flagUfficiali) throws Exception {

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        log.info("dayOfWeek " +  dayOfWeek);

        String votiExcel = "Voti-Ufficiosi-Excel";
        String infoResult = "UFFICIOSI";
        if (flagUfficiali) {
            votiExcel = "Voti-Ufficiali-Excel";
            infoResult = "UFFICIALI";
        }

        List<FcProperties> lProprieta = proprietaController.findAll();
        if (lProprieta.isEmpty()) {
            log.error("error lProprieta size" + lProprieta.size());
            return;
        }
        Properties p = new Properties();
        for (FcProperties prop : lProprieta) {
            p.setProperty(prop.getKey(), prop.getValue());
        }
        p.setProperty("INFO_RESULT", infoResult);

        String startJob =  dayOfWeek + "_" + infoResult;
        log.info("startJob " + startJob);
        String valueStart = p.getProperty(startJob);
        log.info("VALUE_START " + valueStart);
        if ("0".equals(valueStart)) {
            log.info("NOT ACTIVE JOB " + startJob);
            return;
        }

        FcPagelle currentGG = pagelleController.findCurrentGiornata();
        FcGiornataInfo giornataInfo = currentGG.getFcGiornataInfo();

        log.info("currentGG: " + giornataInfo.getCodiceGiornata());
        FcCampionato campionato = campionatoController.findByActive(true);

        String rootPathOutputPdf = (String) p.get("PATH_OUTPUT_PDF");
        String idCampionato = "" + campionato.getIdCampionato();
        String folderPdf = env.getProperty("folderPdf");
        String pathOutput = rootPathOutputPdf + folderPdf + FILE_SEP + "Campionato" + idCampionato;

        int ggFc = giornataInfo.getCodiceGiornata();
        if (idCampionato.equals("2")) {
            ggFc = ggFc - 19;
        }
        String pathOutputPdf = pathOutput + FILE_SEP + ggFc;
        File f = new File(pathOutputPdf);
        if (!f.exists()) {
            boolean flag = f.mkdir();
            if (!flag) {
                log.info("NO pathOutputPdf exist" + pathOutputPdf);
                return;
            }
        }

        String pathImg = "images/";

        String basePathData = (String) p.get("PATH_TMP");
        log.info("basePathData " + basePathData);

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

        jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, p, pathImg, pathOutputPdf + FILE_SEP);

    }

    // @Scheduled(cron = "*/60 * * * * *")
    //@Scheduled(cron = "0 0 6 * * *")
    @Scheduled(cron = "#{@getCronValueInfoGiocatore}")
    public void jobSqualificaInfortunati() throws Exception {

        log.info("jobSqualificaInfortunati start at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));

        List<FcProperties> lProprieta = proprietaController.findAll();
        if (lProprieta.isEmpty()) {
            log.error("error lProprieta size" + lProprieta.size());
            return;
        }
        Properties p = new Properties();
        for (FcProperties prop : lProprieta) {
            p.setProperty(prop.getKey(), prop.getValue());
        }
        String urlFanta = (String) p.get("URL_FANTA");
        String basePathData = (String) p.get("PATH_TMP");

        FcPagelle currentGG = pagelleController.findCurrentGiornata();
        FcGiornataInfo giornataInfo = null;
        if (currentGG != null) {
            giornataInfo = currentGG.getFcGiornataInfo();
            log.info("currentGG: " + giornataInfo.getCodiceGiornata());
        } else {
            giornataInfo = giornataInfoRepository.findByCodiceGiornata(Integer.valueOf(1));
        }

        String fusoOrario = p.getProperty("FUSO_ORARIO");
        String nextDate = Utils.getNextDate(giornataInfo);
        long millisDiff = 0;
        try {
            millisDiff = Utils.getMillisDiff(nextDate, fusoOrario);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("millisDiff : " + millisDiff);

        if (millisDiff == 0) {
            log.error("jobSqualificaInfortunati STOP NO PROCESS");
            return;
        }

        giornataGiocatoreService.deleteByCustonm(giornataInfo);

        String basePath = basePathData;
        log.info("basePathData " + basePathData);

        // **************************************
        // DOWNLOAD FILE SQUALIFICATI
        // **************************************
        String httpUrlSqualificati = urlFanta + "giocatori-squalificati.asp";
        log.info("httpUrlSqualificati " + httpUrlSqualificati);
        String fileName1 = "SQUALIFICATI_" + giornataInfo.getCodiceGiornata();
        JobProcessFileCsv jobCsv = new JobProcessFileCsv();
        jobCsv.downloadCsvSqualificatiInfortunati(httpUrlSqualificati, basePath, fileName1);

        String fileName = basePathData + fileName1 + ".csv";
        jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, true, false);

        // **************************************
        // DOWNLOAD FILE INFORTUNATI
        // **************************************
        String httpUrlInfortunati = urlFanta + "giocatori-infortunati.asp";
        log.info("httpUrlInfortunati " + httpUrlInfortunati);
        String fileName2 = "INFORTUNATI_" + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvSqualificatiInfortunati(httpUrlInfortunati, basePath, fileName2);

        fileName = basePathData + fileName2 + ".csv";
        jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, false, true);
        
        
        // **************************************
        // DOWNLOAD FILE PROBABILI
        // **************************************
        String httpUrlProbabili = urlFanta + "probabili-formazioni-complete-serie-a-live.asp";
        log.info("httpUrlProbabili " + httpUrlProbabili);
        String fileName3 = "PROBABILI_" + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvProbabili(httpUrlProbabili, basePath, fileName3);

        fileName = basePathData + fileName3 + ".csv";
        jobProcessGiornata.initDbProbabili(giornataInfo, fileName);


        log.info("jobSqualificaInfortunati end at " + Utils.formatDate(new Date(), "dd/MM/yyyy HH:mm:ss"));
    }

}