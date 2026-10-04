package fcapp.backend.job;

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

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;

import fcapp.backend.data.RisultatoBean;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataDettInfo;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.GiornataDettInfoService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.utils.Costants;
import fcapp.utils.JasperReportUtils;
import fcapp.utils.Utils;

@Controller
public class JobProcessSendMail {

    private static final Logger log = LoggerFactory.getLogger(JobProcessSendMail.class);

    private static final String REPORT_RISULTATI = "classpath:reports/risultati.jasper";
    private static final String REPORT_CLASSIFICA = "classpath:reports/classifica.jasper";

    private static final String DATA_KEY = "data";
    private static final String DATA_INFO_KEY = "dataInfo";

    private static final int MAX_PLAYERS = 26;
    private static final int TITOLARI_END = 12;
    private static final int PANCHINA_END = 19;

    private final Environment env;
    private final EmailService emailService;
    private final GiornataInfoService giornataInfoService;
    private final AttoreService attoreService;
    private final GiornataService giornataService;
    private final GiornataDettService giornataDettService;
    private final ClassificaService classificaService;
    private final ClassificaTotalePuntiService classificaTotalePuntiService;
    private final GiornataDettInfoService giornataDettInfoService;
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    public JobProcessSendMail(
            Environment env,
            EmailService emailService,
            GiornataInfoService giornataInfoService,
            AttoreService attoreService,
            GiornataService giornataService,
            GiornataDettService giornataDettService,
            ClassificaService classificaService,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            GiornataDettInfoService giornataDettInfoService,
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader) {

        this.env = env;
        this.emailService = emailService;
        this.giornataInfoService = giornataInfoService;
        this.attoreService = attoreService;
        this.giornataService = giornataService;
        this.giornataDettService = giornataDettService;
        this.classificaService = classificaService;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
        this.giornataDettInfoService = giornataDettInfoService;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    public byte[] getJasperRisultati(FcCampionato campionato, FcGiornataInfo giornataInfo, String pathImg) {
        try {
            Map<String, Object> parameters =
                    buildReportParameters(giornataInfo.getCodiceGiornata(), pathImg, campionato);

            Collection<RisultatoBean> data = createTestResultData();

            Resource resource = resourceLoader.getResource(REPORT_RISULTATI);
            try (InputStream inputStream = resource.getInputStream()) {
                return JasperReportUtils.getReportByteCollectionDataSource(inputStream, parameters, data);
            }
        } catch (Exception ex) {
            log.error("Errore nella generazione del report risultati", ex);
            return null;
        }
    }

    public void writePdfAndSendMail(
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            Properties properties,
            String pathOutputPdf) throws SQLException, IOException {

        log.info("writePdfAndSendMail START");

        try {
            String risultatiFile = writeRisultatiPdf(campionato, giornataInfo, pathOutputPdf);
            String classificaFile = writeClassificaPdf(campionato, pathOutputPdf);

            sendResultMail(
                    giornataInfo,
                    properties,
                    risultatiFile,
                    classificaFile);

        } finally {
            log.info("writePdfAndSendMail END");
        }
    }

    private String writeRisultatiPdf(
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            String pathOutputPdf) {

        String fileName = pathOutputPdf + giornataInfo.getDescGiornataFc() + ".pdf";

        try {
            Map<String, Object> parameters =
                    buildReportParameters(giornataInfo.getCodiceGiornata(), Costants.PATH_IMAGES, campionato);

            Collection<RisultatoBean> data = createTestResultData();
            Resource resource = resourceLoader.getResource(REPORT_RISULTATI);

            try (
                    InputStream inputStream = resource.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(fileName)) {

                JasperReportUtils.runReportToPdfStream(inputStream, outputStream, parameters, data);
            }
        } catch (Exception ex) {
            log.error("Errore nella generazione del PDF risultati: {}", fileName, ex);
        }

        return fileName;
    }

    private String writeClassificaPdf(FcCampionato campionato, String pathOutputPdf) {
        String fileName = pathOutputPdf + "Classifica.pdf";

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
            parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));

            Resource resource = resourceLoader.getResource(REPORT_CLASSIFICA);
            DataSource dataSource = jdbcTemplate.getDataSource();

            if (dataSource == null) {
                return fileName;
            }

            try (
                    InputStream inputStream = resource.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    Connection connection = dataSource.getConnection()) {

                JasperReportUtils.runReportToPdfStream(inputStream, outputStream, parameters, connection);
            }
        } catch (Exception ex) {
            log.error("Errore nella generazione del PDF classifica: {}", fileName, ex);
        }

        return fileName;
    }

    private void sendResultMail(
            FcGiornataInfo giornataInfo,
            Properties properties,
            String risultatiFile,
            String classificaFile) {

        try {
            DataSource dataSource = jdbcTemplate.getDataSource();
            if (dataSource == null) {
                return;
            }

            String[] recipients = resolveRecipients(properties);
            String[] attachments = {risultatiFile, classificaFile};

            String subject = "Risultati "
                    + properties.getProperty("INFO_RESULT")
                    + " "
                    + giornataInfo.getDescGiornataFc();

            String message = getBody();
            sendMailWithFallback(recipients, subject, message, attachments);

        } catch (Exception ex) {
            log.error("Errore nell'invio della mail dei risultati", ex);
        }
    }

    private String[] resolveRecipients(Properties properties) {
        StringBuilder recipients = new StringBuilder();

        if ("true".equals(properties.getProperty("ACTIVE_MAIL"))) {
            List<FcAttore> attori = attoreService.findByActive(true);

            for (FcAttore attore : attori) {
                if (attore.isNotifiche()) {
                    recipients.append(attore.getEmail()).append(";");
                }
            }
        } else {
            recipients.append(properties.getProperty("to"));
        }

        if (StringUtils.isEmpty(recipients.toString())) {
            return null;
        }

        return Utils.tornaArrayString(recipients.toString(), ";");
    }

    private void sendMailWithFallback(
            String[] recipients,
            String subject,
            String message,
            String[] attachments) {

        String secondaryFrom = env.getProperty("spring.mail.secondary.username");

        try {
            emailService.sendMail(
                    false,
                    secondaryFrom,
                    recipients,
                    null,
                    null,
                    subject,
                    message,
                    "text/html",
                    attachments);
        } catch (Exception secondaryException) {
            log.error("Invio mail con account secondario fallito", secondaryException);

            try {
                String primaryFrom = env.getProperty("spring.mail.primary.username");

                emailService.sendMail(
                        true,
                        primaryFrom,
                        recipients,
                        null,
                        null,
                        subject,
                        message,
                        "text/html",
                        attachments);
            } catch (Exception primaryException) {
                log.error("Invio mail con account primario fallito", primaryException);
            }
        }
    }

    private String getBody() {
        return """
                <html>
                <head><title>FC</title></head>
                <body>
                <p>Sito aggiornato.</p>
                <br>
                <br>
                <p>Ciao Davide</p>
                </body>
                <html>
                """;
    }

    private Map<String, Object> buildReportParameters(
            int giornata,
            String pathImg,
            FcCampionato campionato) {

        FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(giornata);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path_img", pathImg);
        parameters.put("titolo", giornataInfo.getDescGiornataFc());

        List<FcGiornata> calendario =
                giornataService.findByFcGiornataInfo(giornataInfo);

        int partita = 0;
        int attoreIndex = 0;

        for (FcGiornata g : calendario) {
            attoreIndex = addTeamData(
                    parameters,
                    campionato,
                    g.getFcAttoreByIdAttoreCasa(),
                    g.getTotCasa(),
                    giornataInfo,
                    pathImg,
                    true,
                    attoreIndex);

            attoreIndex = addTeamData(
                    parameters,
                    campionato,
                    g.getFcAttoreByIdAttoreFuori(),
                    g.getTotFuori(),
                    giornataInfo,
                    pathImg,
                    false,
                    attoreIndex);

            partita++;
            parameters.put(
                    "ris" + partita,
                    g.getGolCasa() + " - " + g.getGolFuori());
        }

        return parameters;
    }

    private int addTeamData(
            Map<String, Object> parameters,
            FcCampionato campionato,
            FcAttore attore,
            Double totaleGiornata,
            FcGiornataInfo giornataInfo,
            String pathImg,
            boolean fattoreCampo,
            int currentIndex) {

        try {
            Map<String, Collection<RisultatoBean>> teamData =
                    buildData(
                            campionato,
                            attore,
                            totaleGiornata,
                            giornataInfo,
                            pathImg,
                            fattoreCampo);

            int index = currentIndex + 1;

            parameters.put("sq" + index, attore.getDescAttore());
            parameters.put("data" + index, teamData.get(DATA_KEY));
            parameters.put("dataInfo" + index, teamData.get(DATA_INFO_KEY));

            return index;
        } catch (Exception ex) {
            log.error("Errore nella costruzione dei dati per la squadra", ex);
            return currentIndex;
        }
    }

    private HashMap<String, Collection<RisultatoBean>> buildData(
            FcCampionato campionato,
            FcAttore attore,
            Double totGiornata,
            FcGiornataInfo giornataInfo,
            String pathImg,
            boolean fattoreCampo) {

        NumberFormat formatter = new DecimalFormat("#0.00");
        Collection<RisultatoBean> data =
                buildPlayersData(attore, giornataInfo, pathImg);

        PlayerCounters counters = countPlayers(data);
        ReportData report = addReportSections(data);

        String schema = counters.schema();
        String modificatoreDifesa = getModificatoreDifesa(schema);

        Collection<RisultatoBean> dataInfo = buildDataInfo(
                campionato,
                attore,
                giornataInfo,
                fattoreCampo,
                schema,
                modificatoreDifesa,
                totGiornata,
                formatter,
                report.malus());

        HashMap<String, Collection<RisultatoBean>> result = new HashMap<>();
        result.put(DATA_KEY, report.data());
        result.put(DATA_INFO_KEY, dataInfo);

        return result;
    }

    private Collection<RisultatoBean> buildPlayersData(
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            String pathImg) {

        List<FcGiornataDett> giocatori =
                giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(
                        attore,
                        giornataInfo);

        Collection<RisultatoBean> data = new ArrayList<>();

        for (FcGiornataDett giornataDett : giocatori) {
            RisultatoBean bean = createPlayerBean(giornataDett, pathImg);

            if (bean != null) {
                data.add(bean);
            }
        }

        fillPlayers(data);
        return data;
    }

    private RisultatoBean createPlayerBean(FcGiornataDett giornataDett, String pathImg) {
        RisultatoBean bean = new RisultatoBean();
        FcGiocatore giocatore = giornataDett.getFcGiocatore();

        if (giocatore == null) {
            return bean;
        }

        FcPagelle pagelle = giornataDett.getFcPagelle();

        bean.setR(giocatore.getFcRuolo().getIdRuolo());
        bean.setCalciatore(getPlayerDescription(giornataDett, giocatore));
        bean.setV(toReportValue(giornataDett.getVoto()));
        bean.setFlag_attivo(
                giornataDett.getFlagAttivo() == null
                        ? "N"
                        : giornataDett.getFlagAttivo());
        bean.setOrdinamento(giornataDett.getOrdinamento());

        bean.setGoal_realizzato(pagelle.getGoalRealizzato());
        bean.setGoal_subito(pagelle.getGoalSubito());
        bean.setAmmonizione(pagelle.getAmmonizione());
        bean.setEspulsione(pagelle.getEspulsione());
        bean.setRigore_segnato(pagelle.getRigoreSegnato());
        bean.setRigore_fallito(pagelle.getRigoreFallito());
        bean.setRigore_parato(pagelle.getRigoreParato());
        bean.setAutorete(pagelle.getAutorete());
        bean.setAssist(pagelle.getAssist());

        bean.setG(toReportValue(pagelle.getG()));
        bean.setCs(toReportValue(pagelle.getCs()));
        bean.setTs(toReportValue(pagelle.getTs()));
        bean.setPath_img(pathImg);

        return bean;
    }

    /*
     * The original implementation counted the roles while iterating over the
     * players. The counters are now calculated separately to keep the player
     * mapping focused only on creating RisultatoBean instances.
     */
    private PlayerCounters countPlayers(Collection<RisultatoBean> data) {
        int defenders = 0;
        int midfielders = 0;
        int forwards = 0;

        for (RisultatoBean bean : data) {
            if (!"S".equals(bean.getFlag_attivo())) {
                continue;
            }

            switch (bean.getR()) {
                case "D" -> defenders++;
                case "C" -> midfielders++;
                case "A" -> forwards++;
                default -> {
                    // Same effective behavior as the previous switch.
                }
            }
        }

        return new PlayerCounters(defenders, midfielders, forwards);
    }


    private String getPlayerDescription(
            FcGiornataDett giornataDett,
            FcGiocatore giocatore) {

        if ("S".equals(giornataDett.getFlagAttivo())
                && isSecondChange(giornataDett.getOrdinamento())) {

            String description = "-0,5 " + giocatore.getCognGiocatore();
            return description.length() > 13
                    ? description.substring(0, 13)
                    : description;
        }

        return giocatore.getCognGiocatore();
    }

    private boolean isSecondChange(int ordinamento) {
        return ordinamento == 14
                || ordinamento == 16
                || ordinamento == 18;
    }

    private Double toReportValue(Double value) {
        return value == null
                ? null
                : value / Double.parseDouble(String.valueOf(Costants.DIVISORE_100));
    }

    private void fillPlayers(Collection<RisultatoBean> data) {
        if (data.size() >= MAX_PLAYERS) {
            return;
        }

        int ordinamento = data.size();

        while (data.size() < MAX_PLAYERS) {
            RisultatoBean bean = new RisultatoBean();
            bean.setOrdinamento(ordinamento++);
            bean.setFlag_attivo("N");
            data.add(bean);
        }
    }

    private ReportData addReportSections(Collection<RisultatoBean> data) {
        Collection<RisultatoBean> reportData = new ArrayList<>();
        reportData.add(createSectionBean("TITOLARI", "TIT"));

        double malus = 0;

        for (RisultatoBean bean : data) {
            addSectionIfNeeded(reportData, bean.getOrdinamento());
            malus = calculateMalus(bean, malus);
            reportData.add(bean);
        }

        return new ReportData(reportData, malus);
    }

    private void addSectionIfNeeded(Collection<RisultatoBean> data, int ordinamento) {
        if (ordinamento == TITOLARI_END) {
            data.add(createSectionBean("PANCHINA", "PAN"));
        } else if (ordinamento == PANCHINA_END) {
            data.add(createSectionBean("TRIBUNA", "TRI"));
        }
    }

    private double calculateMalus(RisultatoBean bean, double currentMalus) {
        if ("S".equals(bean.getFlag_attivo())
                && isSecondChange(bean.getOrdinamento())) {
            return currentMalus + 0.5;
        }

        return currentMalus;
    }

    private Collection<RisultatoBean> buildDataInfo(
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            boolean fattoreCampo,
            String schema,
            String modificatoreDifesa,
            Double totGiornata,
            NumberFormat formatter,
            double malus) {

        Collection<RisultatoBean> dataInfo = new ArrayList<>();

        dataInfo.add(createInfoBean("Modulo:", schema));

        addFattoreCampo(dataInfo, giornataInfo, fattoreCampo);
        addBonusQuarti(dataInfo, campionato, attore, giornataInfo);
        addBonusSemifinali(dataInfo, campionato, attore, giornataInfo);

        dataInfo.add(createInfoBean("Modificatore Difesa:", modificatoreDifesa));
        dataInfo.add(createMalusBean(formatMalus(malus, formatter)));
        dataInfo.add(createInfoBean("Totale Giornata:", formatValue(totGiornata, formatter)));

        addTotalPointsInfo(dataInfo, campionato, attore, giornataInfo, formatter);
        addSentAtInfo(dataInfo, attore, giornataInfo);

        return dataInfo;
    }

    private void addFattoreCampo(
            Collection<RisultatoBean> dataInfo,
            FcGiornataInfo giornataInfo,
            boolean fattoreCampo) {

        if (giornataInfo.getIdGiornataFc() < 15) {
            dataInfo.add(createInfoBean(
                    "Fattore Campo:",
                    fattoreCampo ? "1,5" : "0,00"));
        }
    }

    private void addBonusQuarti(
            Collection<RisultatoBean> dataInfo,
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {

        if (giornataInfo.getIdGiornataFc() != 15) {
            return;
        }

        FcClassifica classifica =
                classificaService.findByFcCampionatoAndFcAttore(campionato, attore);

        String bonus = switch (classifica.getIdPosiz()) {
            case 1 -> "8";
            case 2 -> "6";
            case 3 -> "4";
            case 4 -> "2";
            default -> "0";
        };

        dataInfo.add(createInfoBean("Bonus Quarti:", bonus));
    }

    private void addBonusSemifinali(
            Collection<RisultatoBean> dataInfo,
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {

        if (giornataInfo.getIdGiornataFc() == 17) {
            FcClassifica classifica =
                    classificaService.findByFcCampionatoAndFcAttore(campionato, attore);

            dataInfo.add(createInfoBean(
                    "Bonus Semifinali:",
                    String.valueOf(classifica.getVinte())));
        }
    }

    private String formatMalus(double malus, NumberFormat formatter) {
        String value = formatter.format(malus);
        return malus == 0 ? value : "-" + value;
    }

    private void addTotalPointsInfo(
            Collection<RisultatoBean> dataInfo,
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            NumberFormat formatter) {

        FcClassificaTotPt totalPoints =
                classificaTotalePuntiService.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(
                        campionato,
                        attore,
                        giornataInfo);

        String totalTeamPoints = "";
        String totalVsT = "";

        if (totalPoints != null) {
            totalTeamPoints = formatValue(totalPoints.getTotPtRosa(), formatter);
            totalVsT = String.valueOf(totalPoints.getPtTvsT());
        }

        dataInfo.add(createInfoBean("Totale Punteggio Rosa:", totalTeamPoints));
        dataInfo.add(createInfoBean("Totale Punteggio TvsT:", totalVsT));
    }

    private void addSentAtInfo(
            Collection<RisultatoBean> dataInfo,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {

        FcGiornataDettInfo info =
                giornataDettInfoService.findByFcAttoreAndFcGiornataInfo(
                        attore,
                        giornataInfo);

        String sentAt = info == null
                ? ""
                : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss");

        dataInfo.add(createInfoBean("Inviata alle:", sentAt));
    }

    private RisultatoBean createSectionBean(String description, String flag) {
        RisultatoBean bean = new RisultatoBean();
        bean.setCalciatore(description);
        bean.setFlag_attivo(flag);
        return bean;
    }

    private RisultatoBean createInfoBean(String description, String value) {
        RisultatoBean bean = new RisultatoBean();
        bean.setDesc(description);
        bean.setValue(value);
        return bean;
    }

    private RisultatoBean createMalusBean(String value) {
        return createInfoBean("Malus Secondo Cambio:", value);
    }

    private String formatValue(Double value, NumberFormat formatter) {
        if (value == null) {
            return "";
        }

        return formatter.format(
                value / Double.parseDouble(String.valueOf(Costants.DIVISORE_100)));
    }

    private Collection<RisultatoBean> createTestResultData() {
        Collection<RisultatoBean> data = new ArrayList<>();
        data.add(new RisultatoBean("P", "S1", 6.0, 6.0, 6.0, 6.0));
        return data;
    }

    private String getModificatoreDifesa(String value) {
        return switch (value) {
            case "5-4-1" -> "2";
            case "5-3-2", "4-5-1" -> "1";
            case "4-3-3" -> "-1";
            case "3-4-3" -> "-2";
            default -> "0";
        };
    }

    private record ReportData(Collection<RisultatoBean> data, double malus) {
    }

    private record PlayerCounters(int defenders, int midfielders, int forwards) {

        private String schema() {
            return defenders + "-" + midfielders + "-" + forwards;
        }
    }
}
