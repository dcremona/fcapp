package fcapp.ui.views.em;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.RisultatoBean;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.job.EmJobProcessFileCsv;
import fcapp.backend.job.EmJobProcessGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.JasperReportUtils;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Impostazioni")
@Route(value = "adminEm", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class EmImpostazioniView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String ATTR_PROPERTIES = "PROPERTIES";
    private static final String ATTR_CAMPIONATO = "CAMPIONATO";
    private static final String ATTR_GIORNATA_INFO = "GIORNATA_INFO";

    private static final String PATH_TMP = "PATH_TMP";
    private static final String URL_FANTA = "URL_FANTA";
    private static final String MAIL_PRIMARY = "spring.mail.primary.username";
    private static final String MAIL_SECONDARY = "spring.mail.secondary.username";

    private static final String VOTI_DEFAULT = "europei-voti-ufficiali";
    private static final String QUOTAZIONI_ENDPOINT = "europei-giocatori-quotazioni-excel";
    private static final String REPORT_RISULTATI = "classpath:reports/em/risultati.jasper";
    private static final String REPORT_CLASSIFICA = "classpath:reports/em/classifica.jasper";
    private static final String REPORT_IMG_PATH = "images/";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient EmailService emailService;
    private final transient Environment env;
    private final transient EmJobProcessFileCsv emjobProcessFileCsv;
    private final transient ResourceLoader resourceLoader;
    private final transient JdbcTemplate jdbcTemplate;
    private final transient EmJobProcessGiornata emjobProcessGiornata;
    private final transient GiornataInfoService giornataInfoService;
    private final transient AttoreService attoreService;
    private final transient ClassificaTotalePuntiService classificaTotalePuntiService;
    private final transient FormazioneService formazioneService;
    private final transient GiornataDettService giornataDettService;
    private final transient AccessoService accessoService;

    private List<FcAttore> squadre = new ArrayList<>();
    private List<FcGiornataInfo> giornate = new ArrayList<>();

    private ComboBox<FcGiornataInfo> comboGiornata;
    private Button initDb;
    private Button initDbAttore;
    private Button ultimaFormazione;
    private ComboBox<FcAttore> comboAttore;
    private Button downloadQuotazione;
    private Button updateGiocatori;
    private Checkbox chkUpdateQuotazione;
    private Grid<FcGiocatore> tableGiocatoreAdd;
    private Grid<FcGiocatore> tableGiocatoreDel;
    private Button init;
    private Button download;
    private Button calcola;
    private Button ricalcola;
    private Checkbox chkUfficiali;
    private NumberField txtPercentuale;
    private RadioButtonGroup<String> radioGroupVotiExcel;
    private Button calcolaStatistiche;
    private Button aggiornaFlagAttivoGiocatore;
    private Button pdfAndMail;
    private Checkbox chkSendMail;
    private TextArea messaggio;
    private Button notifica;

    public EmImpostazioniView(
            EmailService emailService,
            Environment env,
            EmJobProcessFileCsv emjobProcessFileCsv,
            ResourceLoader resourceLoader,
            JdbcTemplate jdbcTemplate,
            EmJobProcessGiornata emjobProcessGiornata,
            GiornataInfoService giornataInfoService,
            AttoreService attoreService,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            FormazioneService formazioneService,
            GiornataDettService giornataDettService,
            AccessoService accessoService) {
        this.emailService = emailService;
        this.env = env;
        this.emjobProcessFileCsv = emjobProcessFileCsv;
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
        this.emjobProcessGiornata = emjobProcessGiornata;
        this.giornataInfoService = giornataInfoService;
        this.attoreService = attoreService;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
        this.formazioneService = formazioneService;
        this.giornataDettService = giornataDettService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        log.debug("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        loadData();
        buildLayout();
    }

    private void loadData() {
        squadre = attoreService.findByActive(true);

        FcCampionato campionato = getRequiredSessionAttribute(ATTR_CAMPIONATO, FcCampionato.class);
        giornate = giornataInfoService.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(
                campionato.getStart(),
                campionato.getEnd());
    }

    private void buildLayout() {
        FcGiornataInfo giornataInfo = getSessionAttribute(ATTR_GIORNATA_INFO, FcGiornataInfo.class);
        FcCampionato campionato = getRequiredSessionAttribute(ATTR_CAMPIONATO, FcCampionato.class);

        comboGiornata = buildComboGiornata(campionato, giornataInfo);
        add(comboGiornata);

        add(buildSetupPanel());
        add(buildUpdatePanel());
        add(buildCalcolaPanel());
        add(buildNotificaPanel());
    }

    private ComboBox<FcGiornataInfo> buildComboGiornata(FcCampionato campionato, FcGiornataInfo giornataInfo) {
        ComboBox<FcGiornataInfo> combo = new ComboBox<>();
        combo.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
        combo.setItems(giornate);
        combo.setClearButtonVisible(true);
        combo.setPlaceholder("Seleziona la giornata");
        combo.setValue(giornataInfo);
        combo.setWidthFull();
        return combo;
    }

    private Details buildSetupPanel() {
        initDb = buildButton("Init DB", VaadinIcon.ADD_DOCK);
        initDbAttore = buildButton("Init DB Attore", VaadinIcon.ADD_DOCK);

        HorizontalLayout layout = createRow();
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layout.add(initDb, initDbAttore);

        return createDetails("Setup", layout, false);
    }

    private Details buildUpdatePanel() {
        comboAttore = new ComboBox<>();
        comboAttore.setItems(squadre);
        comboAttore.setPlaceholder("Seleziona attore");
        comboAttore.setItemLabelGenerator(FcAttore::getDescAttore);
        comboAttore.setClearButtonVisible(true);
        comboAttore.addValueChangeListener(evt -> {
            initDbAttore.setText("Init Db");
            if (evt.getValue() != null) {
                initDbAttore.setText("Init Db " + evt.getValue().getDescAttore());
            }
        });

        ultimaFormazione = buildButton("Inserisci Ultima Formazione", VaadinIcon.PLUS_SQUARE_O);
        downloadQuotazione = buildButton("Download Quotazioni", VaadinIcon.DOWNLOAD);
        updateGiocatori = buildButton("Update Giocatori", VaadinIcon.PIN);

        chkUpdateQuotazione = new Checkbox("Update Quotazioni");

        txtPercentuale = new NumberField();
        txtPercentuale.setMin(0d);
        txtPercentuale.setMax(100d);
        txtPercentuale.setValue(50d);

        tableGiocatoreAdd = createGiocatoriGrid();
        tableGiocatoreDel = createGiocatoriGrid();

        HorizontalLayout row1 = createRow();
        row1.add(comboAttore, ultimaFormazione);

        HorizontalLayout row2 = createRow();
        row2.add(downloadQuotazione, updateGiocatori, txtPercentuale, chkUpdateQuotazione);

        HorizontalLayout row3 = createRow();
        row3.add(tableGiocatoreAdd);

        HorizontalLayout row4 = createRow();
        row4.add(tableGiocatoreDel);

        VerticalLayout content = createSectionLayout();
        content.add(row1, row2, row3, row4);

        return createDetails("Update", content, true);
    }

    private Details buildCalcolaPanel() {
        init = buildButton("Avvia", VaadinIcon.ADD_DOCK);
        download = buildButton("Download Voti", VaadinIcon.DOWNLOAD);
        calcola = buildButton("Calcola (Yes Algoritmo + Statistiche)", VaadinIcon.PIN);
        ricalcola = buildButton("Ri-Calcola (No Algoritmo + Statistiche)", VaadinIcon.PIN);
        calcolaStatistiche = buildButton("Calcola Statistiche", VaadinIcon.PRESENTATION);
        aggiornaFlagAttivoGiocatore = buildButton("Aggiorna Flag Attivo Giocatore", VaadinIcon.PRESENTATION);
        pdfAndMail = buildButton("Crea Pdf - Invia email", VaadinIcon.MAILBOX);

        chkUfficiali = new Checkbox("Ufficiali");
        chkSendMail = new Checkbox("Mail All");

        radioGroupVotiExcel = new RadioButtonGroup<>();
        radioGroupVotiExcel.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupVotiExcel.setLabel("Voti Excel");
        radioGroupVotiExcel.setItems("europei-voti-ufficiali", "europei-voti-ufficiali-fantacalcio");
        radioGroupVotiExcel.setValue(VOTI_DEFAULT);

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(download, radioGroupVotiExcel, chkUfficiali, calcolaStatistiche, aggiornaFlagAttivoGiocatore);

        VerticalLayout content = createSectionLayout();
        content.add(init, actions, calcola, ricalcola, pdfAndMail, chkSendMail);

        return createDetails("Calcola", content, true);
    }

    private Details buildNotificaPanel() {
        notifica = buildButton("Notifica", VaadinIcon.ADD_DOCK);
        messaggio = new TextArea();
        messaggio.setWidthFull();

        VerticalLayout content = createSectionLayout();
        content.add(notifica, messaggio);

        return createDetails("Notifica", content, true);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            ActionContext context = buildActionContext();

            if (event.getSource() == initDb) {
                handleInitDb(context);
            } else if (event.getSource() == initDbAttore) {
                handleInitDbAttore(context);
            } else if (event.getSource() == ultimaFormazione) {
                handleUltimaFormazione(context);
            } else if (event.getSource() == init) {
                handleInit(context);
            } else if (event.getSource() == downloadQuotazione) {
                handleDownloadQuotazione(context);
            } else if (event.getSource() == updateGiocatori) {
                handleUpdateGiocatori(context);
            } else if (event.getSource() == download) {
                handleDownloadVoti(context);
            } else if (event.getSource() == calcola) {
                handleCalcola(context);
            } else if (event.getSource() == ricalcola) {
                handleRicalcola(context);
            } else if (event.getSource() == calcolaStatistiche) {
                handleCalcolaStatistiche();
            } else if (event.getSource() == aggiornaFlagAttivoGiocatore) {
                handleAggiornaFlagAttivoGiocatore(context);
            } else if (event.getSource() == pdfAndMail) {
                handlePdfAndMail(context);
            } else if (event.getSource() == notifica) {
                handleNotifica(context);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            log.error("Errore durante l'esecuzione dell'azione", e);
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private ActionContext buildActionContext() {
        Properties properties = getRequiredSessionAttribute(ATTR_PROPERTIES, Properties.class);
        FcCampionato campionato = getRequiredSessionAttribute(ATTR_CAMPIONATO, FcCampionato.class);
        FcGiornataInfo giornataInfo = comboGiornata.getValue();
        int giornata = giornataInfo != null ? giornataInfo.getCodiceGiornata() : 0;
        String basePathData = getRequiredProperty(PATH_TMP);

        log.info("campionato {}", campionato.getDescCampionato());
        log.info("giornata {}", giornata);
        log.info("basePathData {}", basePathData);

        validateBasePath(basePathData);

        return new ActionContext(properties, campionato, giornataInfo, giornata, basePathData);
    }

    private void handleInitDb(ActionContext context) {
        List<FcAttore> attori = attoreService.findAll();

        for (FcAttore attore : attori) {
            if (attore.isActive()) {
                createFormazioniForAllGiornate(attore, context.campionato());
                classificaTotalePuntiService.createEm(attore, context.campionato(), 0d);
            }
        }

        int giornata = context.giornata() == 0 ? 1 : context.giornata();
        emjobProcessGiornata.eminitDb(giornata);
    }

    private void handleInitDbAttore(ActionContext context) {
        FcAttore attore = requireComboValue(comboAttore, "Seleziona un attore");
        log.info("getDescAttore {}", attore.getDescAttore());

        createFormazioniForAllGiornate(attore, context.campionato());
        classificaTotalePuntiService.createEm(attore, context.campionato(), 0d);
    }

    private void handleUltimaFormazione(ActionContext context) {
        FcAttore attore = requireComboValue(comboAttore, "Seleziona un attore");
        requireGiornata(context);

        log.info("descAttore {}", attore.getDescAttore());
        emjobProcessGiornata.eminserisciUltimaFormazione(attore.getIdAttore(), context.giornata());
    }

    private void handleInit(ActionContext context) throws Exception {
        FcGiornataInfo giornataInfo = requireGiornataInfo(context);

        emjobProcessGiornata.eminitPagelle(context.giornata());
        sendMailInfoGiornata(giornataInfo);
    }

    private void handleDownloadQuotazione(ActionContext context) throws Exception {
        String urlFanta = context.properties().getProperty(URL_FANTA);
        String httpUrl = urlFanta + QUOTAZIONI_ENDPOINT + ".asp?giornata=" + context.giornata();
        String fileName = "Q_" + context.giornata();

        log.info("httpUrl {}", httpUrl);

        EmJobProcessFileCsv jobCsv = new EmJobProcessFileCsv();
        jobCsv.downloadCsv(httpUrl, context.basePathData(), fileName, 2);
    }

    private void handleUpdateGiocatori(ActionContext context) throws Exception {
        String fileName = context.basePathData() + "Q_" + context.giornata() + ".csv";
        boolean updateQuotazioni = Boolean.TRUE.equals(chkUpdateQuotazione.getValue());
        String percentuale = String.valueOf(txtPercentuale.getValue().intValue());

        HashMap<Object, Object> map = emjobProcessGiornata.initDbGiocatori(fileName, updateQuotazioni, percentuale);

        @SuppressWarnings("unchecked")
        ArrayList<FcGiocatore> listGiocatoriAdd = (ArrayList<FcGiocatore>) map.get("listAdd");
        @SuppressWarnings("unchecked")
        ArrayList<FcGiocatore> listGiocatoriDel = (ArrayList<FcGiocatore>) map.get("listDel");

        log.info("listGiocatoriAdd {}", listGiocatoriAdd.size());
        log.info("listGiocatoriDel {}", listGiocatoriDel.size());

        tableGiocatoreAdd.setItems(listGiocatoriAdd);
        tableGiocatoreDel.setItems(listGiocatoriDel);
        tableGiocatoreAdd.getDataProvider().refreshAll();
        tableGiocatoreDel.getDataProvider().refreshAll();
    }

    private void handleDownloadVoti(ActionContext context) throws Exception {
        String urlFanta = context.properties().getProperty(URL_FANTA);
        String votiExcel = radioGroupVotiExcel.getValue();
        String httpUrlExcel = urlFanta + votiExcel + ".asp?TipoVoti=&searchBonus=&GiornataA=" + context.giornata();
        String fileName = "voti_" + context.giornata();

        emjobProcessFileCsv.downloadCsvNoExcel(httpUrlExcel, context.basePathData(), fileName, 2);

        String fullFileName = context.basePathData() + fileName + ".csv";
        emjobProcessGiornata.emaggiornamentoPFGiornataNoExcel(context.properties(), fullFileName, String.valueOf(context.giornata()));
    }

    private void handleCalcola(ActionContext context) {
        FcGiornataInfo giornataInfo = requireGiornataInfo(context);
        emjobProcessGiornata.emalgoritmo(giornataInfo.getCodiceGiornata(), context.campionato());
        emjobProcessGiornata.emstatistiche();
    }

    private void handleRicalcola(ActionContext context) {
        FcGiornataInfo giornataInfo = requireGiornataInfo(context);
        emjobProcessGiornata.ricalcolaTotPunti(giornataInfo.getCodiceGiornata(), context.campionato());
        emjobProcessGiornata.emstatistiche();
    }

    private void handleCalcolaStatistiche() {
        emjobProcessGiornata.emstatistiche();
    }

    private void handleAggiornaFlagAttivoGiocatore(ActionContext context) {
        FcGiornataInfo giornataInfo = requireGiornataInfo(context);
        emjobProcessGiornata.aggiornaFlagAttivoGiocatore(giornataInfo.getCodiceGiornata());
    }

    private void handlePdfAndMail(ActionContext context) throws Exception {
        FcGiornataInfo giornataInfo = requireGiornataInfo(context);
        String[] attachments = generatePdfAttachments(context, giornataInfo);
        String[] to = resolveRecipientEmails(context.properties(), chkSendMail.getValue(), true);

        String subject = "Risultati " + giornataInfo.getDescGiornataFc()
                + (chkUfficiali.getValue() ? " - Ufficiali" : " - Parziali");

        sendEmailWithFallback(to, subject, buildGenericBodyHtml(), "text/html", attachments);
    }

    private void handleNotifica(ActionContext context) {
        String[] to = resolveRecipientEmails(context.properties(), true, false);
        sendEmailWithFallback(to, "Avviso", messaggio.getValue(), "", null);
    }

    private String[] generatePdfAttachments(ActionContext context, FcGiornataInfo giornataInfo) throws Exception {
        String imgLogo = env.getProperty("img.logo");

        Resource risultatiResource = resourceLoader.getResource(REPORT_RISULTATI);
        try (InputStream inputStream = risultatiResource.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(context.basePathData() + giornataInfo.getDescGiornataFc() + ".pdf")) {

            Map<String, Object> params = buildReportParams(giornataInfo.getCodiceGiornata(), REPORT_IMG_PATH);
            Collection<RisultatoBean> collection = new ArrayList<>();
            collection.add(new RisultatoBean("P", "S1", 6.0, 6.0, 6.0, 6.0));
            JasperReportUtils.runReportToPdfStream(inputStream, outputStream, params, collection);
        }

        Resource classificaResource = resourceLoader.getResource(REPORT_CLASSIFICA);
        try (InputStream inputStream = classificaResource.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(context.basePathData() + "Classifica.pdf");
             Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {

            Map<String, Object> params = new HashMap<>();
            params.put("DIVISORE", String.valueOf(Costants.DIVISORE_10));
            params.put("PATH_IMG", REPORT_IMG_PATH + imgLogo);
            JasperReportUtils.runReportToPdfStream(inputStream, outputStream, params, conn);
        }

        return new String[] {
                context.basePathData() + giornataInfo.getDescGiornataFc() + ".pdf",
                context.basePathData() + "Classifica.pdf"
        };
    }

    private void sendEmailWithFallback(String[] to, String subject, String message, String contentType, String[] attachments) {
        try {
            try {
                String from = env.getProperty(MAIL_SECONDARY);
                emailService.sendMail(false, from, to, null, null, subject, message, contentType, attachments);
            } catch (Exception ex) {
                log.warn("Invio con mail secondaria fallito, provo con primaria", ex);
                String from = env.getProperty(MAIL_PRIMARY);
                emailService.sendMail(true, from, to, null, null, subject, message, contentType, attachments);
            }
        } catch (Exception e) {
            throw new IllegalStateException(CustomMessageDialog.MSG_MAIL_KO, e);
        }
    }

    private String[] resolveRecipientEmails(Properties properties, boolean sendAll, boolean onlyNotifiche) {
        StringBuilder emailDestinatario = new StringBuilder();

        if (sendAll) {
            List<FcAttore> attori = attoreService.findAll();
            for (FcAttore attore : attori) {
                boolean canSend = !onlyNotifiche || attore.isNotifiche();
                if (canSend && attore.getEmail() != null && !attore.getEmail().isBlank()) {
                    emailDestinatario.append(attore.getEmail()).append(';');
                }
            }
        } else {
            emailDestinatario.append(properties.getProperty("to", ""));
        }

        if (emailDestinatario.isEmpty()) {
            return null;
        }

        return Utils.tornaArrayString(emailDestinatario.toString(), ";");
    }

    private void createFormazioniForAllGiornate(FcAttore attore, FcCampionato campionato) {
        for (int j = 1; j <= 23; j++) {
            formazioneService.createFormazione(attore, campionato.getIdCampionato(), j);
        }
    }

    private String buildGenericBodyHtml() {
        return """
                <html>
                <head><title>FC</title></head>
                <body>
                <p>Sito aggiornato</p>
                <br>
                <br>
                <p>Saluti Davide</p>
                </body>
                </html>
                """;
    }

    private Map<String, Object> buildReportParams(int giornata, String pathImg) {
        NumberFormat formatter = new DecimalFormat("#0.00");

        FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(giornata);
        List<FcAttore> allSquadre = attoreService.findAll();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path_img", pathImg);
        parameters.put("titolo", giornataInfo.getDescGiornataFc());

        int conta = 1;
        for (FcAttore attore : allSquadre) {
            List<FcGiornataDett> giocatori = giornataDettService
                    .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

            FcClassificaTotPt totPunti = classificaTotalePuntiService
                    .findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);

            int countD = 0;
            int countC = 0;
            int countA = 0;
            Collection<RisultatoBean> data = new ArrayList<>();

            for (FcGiornataDett dettaglio : giocatori) {
                if ("S".equals(dettaglio.getFlagAttivo())) {
                    switch (dettaglio.getFcGiocatore().getFcRuolo().getIdRuolo()) {
                        case "D" -> countD++;
                        case "C" -> countC++;
                        case "A" -> countA++;
                        default -> {
                        }
                    }
                }

                data.add(buildRisultatoBean(pathImg, dettaglio));
            }

            String schema = countD + "-" + countC + "-" + countA;
            log.info("schema {}", schema);

            String puntiTotali = "";
            if (totPunti != null) {
                puntiTotali = formatter.format(totPunti.getTotPt() / Double.parseDouble(String.valueOf(Costants.DIVISORE_10)));
            }

            parameters.put("sq" + conta, attore.getDescAttore());
            parameters.put("data" + conta, data);
            parameters.put("ris" + conta, puntiTotali);
            parameters.put("dataInfo" + conta, null);
            conta++;
        }

        return parameters;
    }

    private @NonNull RisultatoBean buildRisultatoBean(String pathImg, FcGiornataDett dettaglio) {
        RisultatoBean bean = new RisultatoBean();

        bean.setR(dettaglio.getFcGiocatore().getFcRuolo().getIdRuolo());
        bean.setCalciatore(dettaglio.getFcGiocatore().getCognGiocatore());

        if (dettaglio.getVoto() != null) {
            bean.setV(dettaglio.getVoto() / Double.parseDouble(String.valueOf(Costants.DIVISORE_10)));
        }

        if (dettaglio.getFcPagelle().getG() != null) {
            bean.setG(dettaglio.getFcPagelle().getG() / Double.parseDouble(String.valueOf(Costants.DIVISORE_10)));
        }
        if (dettaglio.getFcPagelle().getCs() != null) {
            bean.setCs(dettaglio.getFcPagelle().getCs() / Double.parseDouble(String.valueOf(Costants.DIVISORE_10)));
        }
        if (dettaglio.getFcPagelle().getTs() != null) {
            bean.setTs(dettaglio.getFcPagelle().getTs() / Double.parseDouble(String.valueOf(Costants.DIVISORE_10)));
        }

        bean.setFlag_attivo(dettaglio.getFlagAttivo());
        bean.setOrdinamento(dettaglio.getOrdinamento());
        bean.setGoal_realizzato(dettaglio.getFcPagelle().getGoalRealizzato());
        bean.setGoal_subito(dettaglio.getFcPagelle().getGoalSubito());
        bean.setAmmonizione(dettaglio.getFcPagelle().getAmmonizione());
        bean.setEspulsione(dettaglio.getFcPagelle().getEspulsione());
        bean.setRigore_segnato(dettaglio.getFcPagelle().getRigoreSegnato());
        bean.setRigore_fallito(dettaglio.getFcPagelle().getRigoreFallito());
        bean.setRigore_parato(dettaglio.getFcPagelle().getRigoreParato());
        bean.setAutorete(dettaglio.getFcPagelle().getAutorete());
        bean.setAssist(dettaglio.getFcPagelle().getAssist());
        bean.setGv(dettaglio.getFcPagelle().getGdv());
        bean.setPath_img(pathImg);

        return bean;
    }

    private void sendMailInfoGiornata(FcGiornataInfo ggInfo) {
        String subject = "Avvio Giornata - " + ggInfo.getDescGiornataFc();
        log.info("subject {}", subject);

        String formazioneHtml = """
                <html>
                <head><title>FC</title></head>
                <body>
                <p>Prossima Giornata: %s</p>
                <br>
                <br>
                <p>Data Giornata: %s</p>
                <br>
                <br>
                <p>Ciao Davide</p>
                </body>
                </html>
                """.formatted(
                ggInfo.getDescGiornataFc(),
                Utils.formatLocalDateTime(ggInfo.getDataGiornata(), Costants.DATA_FORMATTED)
        );

        log.info("formazioneHtml {}", formazioneHtml);

        Properties properties = getRequiredSessionAttribute(ATTR_PROPERTIES, Properties.class);
        properties.setProperty("ACTIVE_MAIL", String.valueOf(chkSendMail.getValue()));
        log.info("ACTIVE_MAIL {}", properties.getProperty("ACTIVE_MAIL"));

        boolean activeMail = Boolean.parseBoolean(properties.getProperty("ACTIVE_MAIL"));
        String[] to = resolveRecipientEmails(properties, activeMail, true);

        sendEmailWithFallback(to, subject, formazioneHtml, "text/html", null);
    }

    private Grid<FcGiocatore> createGiocatoriGrid() {
        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(new ArrayList<>());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth("600px");

        addRuoloColumn(grid);
        addGiocatoreColumn(grid);
        addSquadraColumn(grid);
        addQuotazioneColumn(grid);

        return grid;
    }

    private void addRuoloColumn(Grid<FcGiocatore> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = createGridCellLayout();
            if (g != null) {
                String ruolo = g.getFcRuolo().getIdRuolo().toLowerCase();
                Image img = Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png"));
                cellLayout.add(img);
            }
            return cellLayout;
        }));

        column.setSortable(true);
        column.setHeader(Costants.RUOLO);
        column.setAutoWidth(true);
    }

    private void addGiocatoreColumn(Grid<FcGiocatore> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = createGridCellLayout();
            if (g != null) {
                cellLayout.add(new Span(g.getCognGiocatore()));
            }
            return cellLayout;
        }));

        column.setSortable(false);
        column.setHeader(Costants.GIOCATORE);
        column.setAutoWidth(true);
    }

    private void addSquadraColumn(Grid<FcGiocatore> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = createGridCellLayout();

            if (g != null && g.getFcSquadra() != null) {
                FcSquadra sq = g.getFcSquadra();

                if (sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        log.error("Errore caricamento immagine squadra {}", sq.getNomeSquadra(), e);
                    }
                }

                cellLayout.add(new Span(sq.getNomeSquadra()));
            }

            return cellLayout;
        }));

        column.setSortable(false);
        column.setHeader(Costants.SQUADRA);
        column.setAutoWidth(true);
    }

    private void addQuotazioneColumn(Grid<FcGiocatore> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = createGridCellLayout();
            if (g != null) {
                cellLayout.add(new Span(String.valueOf(g.getQuotazione())));
            }
            return cellLayout;
        }));

        column.setSortable(true);
        column.setHeader("Q");
        column.setAutoWidth(true);
    }

    private HorizontalLayout createGridCellLayout() {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.setAlignItems(Alignment.STRETCH);
        return cellLayout;
    }

    private HorizontalLayout createRow() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(true);
        return layout;
    }

    private VerticalLayout createSectionLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        return layout;
    }

    private Details createDetails(String summary, com.vaadin.flow.component.Component content, boolean opened) {
        Details details = new Details(summary, content);
        details.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        details.setOpened(opened);
        return details;
    }

    private Button buildButton(String text, VaadinIcon icon) {
        Button button = new Button(text);
        button.setIcon(icon.create());
        button.addClickListener(this);
        return button;
    }

    private void validateBasePath(String basePathData) {
        File folder = new File(basePathData);
        if (!folder.exists()) {
            throw new IllegalArgumentException("Impossibile trovare il percorso specificato " + basePathData);
        }
    }

    private FcGiornataInfo requireGiornataInfo(ActionContext context) {
        if (context.giornataInfo() == null) {
            throw new IllegalArgumentException("Seleziona una giornata");
        }
        return context.giornataInfo();
    }

    private void requireGiornata(ActionContext context) {
        if (context.giornata() <= 0) {
            throw new IllegalArgumentException("Seleziona una giornata");
        }
    }

    private <T> T requireComboValue(ComboBox<T> combo, String message) {
        T value = combo.getValue();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String getRequiredProperty(String key) {
        String value = env.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Property mancante: " + key);
        }
        return value;
    }

    private <T> T getRequiredSessionAttribute(String key, Class<T> type) {
        T value = getSessionAttribute(key, type);
        if (value == null) {
            throw new IllegalStateException("Attributo di sessione mancante: " + key);
        }
        return value;
    }

    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Attributo di sessione non valido: " + key);
        }
        return type.cast(value);
    }

    private record ActionContext(
            Properties properties,
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            int giornata,
            String basePathData) {
    }
}
