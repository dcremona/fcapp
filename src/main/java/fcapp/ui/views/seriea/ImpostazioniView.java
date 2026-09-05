package fcapp.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.job.JobProcessFileCsv;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.job.JobProcessSendMail;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.ClassificaService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.backend.service.ProprietaService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Impostazioni")
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ImpostazioniView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SESSION_PROPERTIES = "PROPERTIES";
    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String SESSION_GIORNATA_INFO = "GIORNATA_INFO";

    private static final String PATH_TMP = "PATH_TMP";
    private static final String URL_FANTA = "URL_FANTA";
    private static final String MAIL_PRIMARY_USERNAME = "spring.mail.primary.username";
    private static final String MAIL_SECONDARY_USERNAME = "spring.mail.secondary.username";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient Environment env;
    private final transient JobProcessFileCsv jobProcessFileCsv;
    private final transient JobProcessGiornata jobProcessGiornata;
    private final transient JobProcessSendMail jobProcessSendMail;
    private final transient CalendarioCompetizioneService calendarioCompetizioneService;
    private final transient GiornataInfoService giornataInfoService;
    private final transient AttoreService attoreService;
    private final transient SquadraService squadraService;
    private final transient ClassificaService classificaService;
    private final transient FormazioneService formazioneService;
    private final transient ProprietaService proprietaService;
    private final transient AccessoService accessoService;
    private final transient EmailService emailService;
    private final transient GiornataService giornataService;
    private final transient ResourceLoader resourceLoader;

    private List<FcAttore> squadre = new ArrayList<>();
    private List<FcSquadra> squadreSerieA = new ArrayList<>();
    private List<FcGiornataInfo> giornate = new ArrayList<>();

    private Button initDb;
    private Button generaCalendar;
    private ComboBox<FcGiornataInfo> comboGiornata;

    private ComboBox<FcAttore> comboAttore;
    private Button resetFormazione;
    private Button ultimaFormazione;
    private Button formazione422;

    //private Button downloadQuotazioni;
    private Button updateGiocatori;
    private Checkbox chkUpdateQuotazioni;
    private Checkbox chkUpdateImg;
    private NumberField txtPercentuale;
    private Grid<FcGiocatore> tableGiocatoreAdd;
    private Grid<FcGiocatore> tableGiocatoreDel;

    private Button testMailPrimary;
    private Button testMailSecondary;

    private Button init;
    //private Button download;
    private Button seiPolitico;
    private ComboBox<FcSquadra> comboSquadreA;
    private Button calcola;
    private ToggleButton chkForzaVotoGiocatore;
    private ToggleButton chkRoundVotoGiocatore;
    private Button calcolaStatistiche;
    private Button pdfAndMail;

    private Button salva;
    private Button resetDate;
    private Checkbox chkUfficiali;
    private Checkbox chkSendMail;

    private Details panelSetup;
    private DateTimePicker da1;
    private DateTimePicker da2;
    private DateTimePicker dg;
    private DateTimePicker dp;

    public ImpostazioniView(
            Environment env,
            JobProcessFileCsv jobProcessFileCsv,
            JobProcessGiornata jobProcessGiornata,
            JobProcessSendMail jobProcessSendMail,
            GiornataInfoService giornataInfoService,
            CalendarioCompetizioneService calendarioCompetizioneService,
            AttoreService attoreService,
            SquadraService squadraService,
            ClassificaService classificaService,
            FormazioneService formazioneService,
            ProprietaService proprietaService,
            AccessoService accessoService,
            EmailService emailService,
            GiornataService giornataService,
            ResourceLoader resourceLoader) {

        this.env = env;
        this.jobProcessFileCsv = jobProcessFileCsv;
        this.jobProcessGiornata = jobProcessGiornata;
        this.jobProcessSendMail = jobProcessSendMail;
        this.calendarioCompetizioneService = calendarioCompetizioneService;
        this.giornataInfoService = giornataInfoService;
        this.attoreService = attoreService;
        this.squadraService = squadraService;
        this.classificaService = classificaService;
        this.formazioneService = formazioneService;
        this.proprietaService = proprietaService;
        this.accessoService = accessoService;
        this.emailService = emailService;
        this.giornataService = giornataService;
        this.resourceLoader = resourceLoader;

        log.info("ImpostazioniView()");
    }

    @PostConstruct
    void init() {
        log.debug("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initData();
        initLayout();
    }

    private void initData() {
        squadre = attoreService.findByActive(true);
        squadreSerieA = squadraService.findAll();

        FcCampionato campionato = getSessionAttribute(SESSION_CAMPIONATO, FcCampionato.class);
        if (campionato == null) {
            return;
        }

        Integer from = campionato.getStart();
        Integer to = campionato.getEnd();

        log.info("from and to ({},{})", from, to);
        giornate = giornataInfoService
                .findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
    }

    private void initLayout() {
        FcGiornataInfo giornataInfo = getSessionAttribute(SESSION_GIORNATA_INFO, FcGiornataInfo.class);
        if (giornataInfo == null) {
            return;
        }

        buildSetupSection(giornataInfo);
        buildUpdateSection();
        buildCalcolaSection();
        buildDateSection(giornataInfo);
    }

    private void buildSetupSection(FcGiornataInfo giornataInfo) {
        initDb = createButton("Init Db Formazioni/Classifica", VaadinIcon.START_COG, this);
        generaCalendar = createButton("Genera Calendario", VaadinIcon.CALENDAR, this);

        comboGiornata = new ComboBox<>();
        comboGiornata.setItemLabelGenerator(Utils::buildInfoGiornata);
        comboGiornata.setItems(giornate);
        comboGiornata.setClearButtonVisible(true);
        comboGiornata.setPlaceholder("Seleziona la giornata");
        comboGiornata.setValue(giornataInfo);
        comboGiornata.setWidthFull();
        comboGiornata.addValueChangeListener(event -> onGiornataChanged(event.getValue()));

        add(comboGiornata);

        HorizontalLayout layoutSetup = new HorizontalLayout();
        layoutSetup.setMargin(true);
        layoutSetup.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutSetup.add(initDb, generaCalendar);

        panelSetup = new Details("Setup", layoutSetup);
        panelSetup.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        add(panelSetup);

        updateSetupState(giornataInfo);
    }

    private void buildUpdateSection() {
        comboAttore = new ComboBox<>();
        comboAttore.setItems(squadre);
        comboAttore.setItemLabelGenerator(FcAttore::getDescAttore);
        comboAttore.setClearButtonVisible(true);
        comboAttore.setPlaceholder("Seleziona attore");

        resetFormazione = createButton("Reset Formazione", VaadinIcon.PLUS_SQUARE_O, this);
        ultimaFormazione = createButton("Inserisci Ultima Formazione", VaadinIcon.PLUS_SQUARE_O, this);
        formazione422 = createButton("Formazione 422", VaadinIcon.PLUS_SQUARE_O, this);

        HorizontalLayout layoutUpdateRow1 = new HorizontalLayout(comboAttore, resetFormazione, ultimaFormazione, formazione422);
        layoutUpdateRow1.setMargin(true);

        //downloadQuotazioni = createButton("Download Quotazioni", VaadinIcon.DOWNLOAD, this);
        updateGiocatori = createButton("Update Giocatori", VaadinIcon.PIN, this);

        txtPercentuale = new NumberField();
        txtPercentuale.setMin(0d);
        txtPercentuale.setMax(100d);
        txtPercentuale.setStepButtonsVisible(true);
        txtPercentuale.setValue(70d);

        chkUpdateQuotazioni = new Checkbox("Update Quotazioni");
        chkUpdateImg = new Checkbox("Update Img");

        HorizontalLayout layoutUpdateRow2 = new HorizontalLayout(
//                downloadQuotazioni,
                buildDownloadQuotazioni(),
                updateGiocatori,
                txtPercentuale,
                chkUpdateQuotazioni,
                chkUpdateImg);
        layoutUpdateRow2.setMargin(true);

        tableGiocatoreAdd = getTableGiocatori();
        tableGiocatoreDel = getTableGiocatori();

        HorizontalLayout layoutUpdateRow3 = new HorizontalLayout(tableGiocatoreAdd);
        layoutUpdateRow3.setMargin(true);

        HorizontalLayout layoutUpdateRow4 = new HorizontalLayout(tableGiocatoreDel);
        layoutUpdateRow4.setMargin(true);

        testMailPrimary = createButton("Test Mail Primary", VaadinIcon.MAILBOX, this);
        testMailSecondary = createButton("Test Mail Secondary", VaadinIcon.MAILBOX, this);

        VerticalLayout layoutUpdate = new VerticalLayout();
        layoutUpdate.setMargin(true);
        layoutUpdate.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutUpdate.add(layoutUpdateRow1, layoutUpdateRow2, layoutUpdateRow3, layoutUpdateRow4,
                testMailPrimary, testMailSecondary, buildUploadUpdateImg());

        Details panelUpdate = new Details("Update", layoutUpdate);
        panelUpdate.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        panelUpdate.setOpened(true);
        add(panelUpdate);
    }

    private void buildCalcolaSection() {
        init = createButton("Avvia", VaadinIcon.ADD_DOCK, this);
//        download = createButton("Download Voti", VaadinIcon.DOWNLOAD, this);
        seiPolitico = createButton("Sei Politico", VaadinIcon.PIN, this);
        calcola = createButton("Calcola", VaadinIcon.PIN, this);
        calcolaStatistiche = createButton("Calcola Statistiche", VaadinIcon.PRESENTATION, this);
        pdfAndMail = createButton("Crea Pdf - Invia email", VaadinIcon.MAILBOX, this);

        chkUfficiali = new Checkbox("Ufficiali");
        chkSendMail = new Checkbox("Invia Email a tutti");

        comboSquadreA = new ComboBox<>();
        comboSquadreA.setItems(squadreSerieA);
        comboSquadreA.setItemLabelGenerator(FcSquadra::getNomeSquadra);
        comboSquadreA.setClearButtonVisible(true);
        comboSquadreA.setPlaceholder(Costants.SQUADRA);
        comboSquadreA.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();
            if (item != null && item.getImg() != null) {
                try {
                    container.add(Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream()));
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (item != null) {
                container.add(new Span(item.getNomeSquadra()));
            }
            return container;
        }));

        chkForzaVotoGiocatore = new ToggleButton();
        chkForzaVotoGiocatore.setLabel("Forza Voto 0");
        chkForzaVotoGiocatore.setValue(false);

        chkRoundVotoGiocatore = new ToggleButton();
        chkRoundVotoGiocatore.setLabel("Round Voto");
        chkRoundVotoGiocatore.setValue(true);

        HorizontalLayout row1 = new HorizontalLayout(buildUploadUpdateVoti(), chkUfficiali, seiPolitico, comboSquadreA);
        HorizontalLayout row2 = new HorizontalLayout(calcola, chkForzaVotoGiocatore, chkRoundVotoGiocatore, calcolaStatistiche);

        VerticalLayout layoutCalcola = new VerticalLayout();
        layoutCalcola.setMargin(true);
        layoutCalcola.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutCalcola.add(init, row1, row2, pdfAndMail, chkSendMail);

        Details panelCalcola = new Details("Calcola", layoutCalcola);
        panelCalcola.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        panelCalcola.setOpened(true);
        add(panelCalcola);
    }

    private void buildDateSection(FcGiornataInfo giornataInfo) {
        da1 = new DateTimePicker("Data Anticipo1");
        da1.setValue(giornataInfo.getDataAnticipo1());

        da2 = new DateTimePicker("Data Anticipo2");
        da2.setValue(giornataInfo.getDataAnticipo2());

        dg = new DateTimePicker("Data Giornata");
        dg.setValue(giornataInfo.getDataGiornata());

        dp = new DateTimePicker("Data Posticipo");
        dp.setValue(giornataInfo.getDataPosticipo());

        salva = createButton("Salva", VaadinIcon.DATABASE, this);
        resetDate = createButton("Reset", VaadinIcon.REFRESH, this);

        HorizontalLayout layoutRow1 = new HorizontalLayout(salva, resetDate);
        HorizontalLayout layoutRow2 = new HorizontalLayout(da1, da2);
        HorizontalLayout layoutRow22 = new HorizontalLayout(dg, dp);

        VerticalLayout pnlUfficiali = new VerticalLayout(
                getCheck("1_UFFICIALI", "DOM_Ufficiali"),
                getCheck("2_UFFICIALI", "LUN_Ufficiali"),
                getCheck("3_UFFICIALI", "MAR_Ufficiali"),
                getCheck("4_UFFICIALI", "MER_Ufficiali"),
                getCheck("5_UFFICIALI", "GIO_Ufficiali"),
                getCheck("6_UFFICIALI", "VEN_Ufficiali"),
                getCheck("7_UFFICIALI", "SAB_Ufficiali"));
        pnlUfficiali.setSizeUndefined();

        VerticalLayout pnlUfficiosi = new VerticalLayout(
                getCheck("1_UFFICIOSI", "DOM_Ufficiosi"),
                getCheck("2_UFFICIOSI", "LUN_Ufficiosi"),
                getCheck("3_UFFICIOSI", "MAR_Ufficiosi"),
                getCheck("4_UFFICIOSI", "MER_Ufficiosi"),
                getCheck("5_UFFICIOSI", "GIO_Ufficiosi"),
                getCheck("6_UFFICIOSI", "VEN_Ufficiosi"),
                getCheck("7_UFFICIOSI", "SAB_Ufficiosi"));
        pnlUfficiosi.setSizeUndefined();

        HorizontalLayout layoutRow3 = new HorizontalLayout(pnlUfficiali, pnlUfficiosi);

        VerticalLayout layoutDate = new VerticalLayout(layoutRow1, layoutRow2, layoutRow22, layoutRow3);
        layoutDate.setMargin(true);
        layoutDate.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

        Details panelGiornata = new Details("Imposta Date", layoutDate);
        panelGiornata.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        panelGiornata.setOpened(true);
        add(panelGiornata);
    }
    
    private Upload buildDownloadQuotazioni() {
        InMemoryUploadHandler inMemoryHandler = UploadHandler.inMemory((metadata, data) -> {
            try {
            	
                String basePathData = env.getProperty(PATH_TMP);
                log.info("basePathData {}", basePathData);

                FcGiornataInfo giornataInfo = comboGiornata != null ? comboGiornata.getValue() : null;
                int codiceGiornata = giornataInfo != null ? giornataInfo.getCodiceGiornata() : 0;

                String fileName = "Q_" + codiceGiornata;
                
                InputStream is = new ByteArrayInputStream(data);

                jobProcessFileCsv.downloadQuotazioniCsvFromXlsx(is,basePathData,fileName);
                
              	CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
            
            } catch (Exception e) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
            }
        });
        return new Upload(inMemoryHandler);
    }


    private Upload buildUploadUpdateImg() {
        InMemoryUploadHandler inMemoryHandler = UploadHandler.inMemory((metadata, data) -> {
            try {
                InputStream is = new ByteArrayInputStream(data);
                jobProcessGiornata.updateImgGiocatore(is);
                CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
            } catch (Exception e) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
            }
        });
        return new Upload(inMemoryHandler);
    }

    private Upload buildUploadUpdateVoti() {
        InMemoryUploadHandler inMemoryHandler = UploadHandler.inMemory((metadata, data) -> {
            try {
            	Properties properties = getSessionAttribute(SESSION_PROPERTIES, Properties.class);
            	
                String basePathData = env.getProperty(PATH_TMP);
                log.info("basePathData {}", basePathData);

                FcGiornataInfo giornataInfo = comboGiornata != null ? comboGiornata.getValue() : null;
                int codiceGiornata = giornataInfo != null ? giornataInfo.getCodiceGiornata() : 0;

                String fileName = "voti_" + codiceGiornata;
                
                InputStream is = new ByteArrayInputStream(data);
                
                jobProcessFileCsv.downloadCsvFromXlsx(is,basePathData,fileName);
                
                fileName = basePathData + "voti_" + codiceGiornata + ".csv";
                jobProcessGiornata.aggiornamentoPFGiornata(properties, fileName, String.valueOf(codiceGiornata));

                if (giornataInfo != null) {
                	jobProcessGiornata.checkSeiPolitico(giornataInfo.getCodiceGiornata());
                }
                
              	CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
            
            } catch (Exception e) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
            }
        });
        return new Upload(inMemoryHandler);
    }

    private void onGiornataChanged(FcGiornataInfo giornata) {
        if (giornata == null || da1 == null || da2 == null || dg == null || dp == null) {
            return;
        }

        log.info("giornata {}", giornata.getCodiceGiornata());

        da1.setValue(giornata.getDataAnticipo1());
        da2.setValue(giornata.getDataAnticipo2());
        dg.setValue(giornata.getDataGiornata());
        dp.setValue(giornata.getDataPosticipo());

        updateSetupState(giornata);
    }

    private void updateSetupState(FcGiornataInfo giornataInfo) {
        boolean enabled = giornataInfo != null
                && (giornataInfo.getCodiceGiornata() == 1 || giornataInfo.getCodiceGiornata() == 20);

        panelSetup.setOpened(enabled);
        initDb.setEnabled(enabled);
        generaCalendar.setEnabled(enabled);
    }

    private Button createButton(String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text);
        button.setIcon(icon.create());
        button.addClickListener(listener);
        return button;
    }

    private Checkbox getCheck(String key, String label) {
        Properties properties = getSessionAttribute(SESSION_PROPERTIES, Properties.class);

        Checkbox check = new Checkbox(label);
        boolean value = properties != null && "1".equals(properties.getProperty(key));
        check.setValue(value);

        check.addValueChangeListener(event -> {
            try {
                boolean checked = Boolean.TRUE.equals(event.getValue());

                FcProperties proprieta = new FcProperties();
                proprieta.setKey(key);
                proprieta.setValue(checked ? "1" : "0");
                proprietaService.save(proprieta);

                if (properties != null) {
                    properties.setProperty(key, checked ? "1" : "0");
                }

                CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
            } catch (Exception e) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
            }
        });

        return check;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            Properties properties = getSessionAttribute(SESSION_PROPERTIES, Properties.class);
            FcCampionato campionato = getSessionAttribute(SESSION_CAMPIONATO, FcCampionato.class);

            FcGiornataInfo giornataInfo = comboGiornata != null ? comboGiornata.getValue() : null;
            int codiceGiornata = giornataInfo != null ? giornataInfo.getCodiceGiornata() : 0;
            FcAttore attore = comboAttore != null ? comboAttore.getValue() : null;

            log.info("codice giornata {}", codiceGiornata);

            String basePathData = env.getProperty(PATH_TMP);
            log.info("basePathData {}", basePathData);

            validateBasePath(basePathData);

            if (event.getSource() == initDb) {
                handleInitDb(campionato);
            } else if (event.getSource() == testMailPrimary) {
                handleTestMailPrimary();
            } else if (event.getSource() == testMailSecondary) {
                handleTestMailSecondary();
//            } else if (event.getSource() == downloadQuotazioni) {
//                handleDownloadQuotazioni(properties, basePathData, codiceGiornata);
            } else if (event.getSource() == updateGiocatori) {
                handleUpdateGiocatori(basePathData, codiceGiornata);
            } else if (event.getSource() == generaCalendar) {
                jobProcessGiornata.generaCalendario(campionato);
            } else if (event.getSource() == formazione422) {
                validateGiornata(codiceGiornata);
                getConfirmDialog(codiceGiornata, campionato).open();
                return;
            } else if (event.getSource() == resetFormazione) {
                validateGiornata(codiceGiornata);
                validateAttore(attore);
                jobProcessGiornata.resetFormazione(attore.getIdAttore(), codiceGiornata);
            } else if (event.getSource() == ultimaFormazione) {
                validateGiornata(codiceGiornata);
                validateAttore(attore);
                jobProcessGiornata.inserisciUltimaFormazione(attore.getIdAttore(), codiceGiornata);
            } else if (event.getSource() == init) {
                validateGiornata(codiceGiornata);
                jobProcessGiornata.initPagelle(codiceGiornata);
                try {
                    sendMailInfoGiornata(giornataInfo);
                } catch (Exception e) {
                    CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, e.getMessage());
                }
//            } else if (event.getSource() == download) {
//                handleDownloadVoti(properties, basePathData, codiceGiornata, giornataInfo);
            } else if (event.getSource() == seiPolitico) {
                handleSeiPolitico(codiceGiornata);
            } else if (event.getSource() == calcola) {
                handleCalcola(codiceGiornata, campionato);
            } else if (event.getSource() == calcolaStatistiche) {
                jobProcessGiornata.statistiche(campionato);
            } else if (event.getSource() == pdfAndMail) {
                handlePdfAndMail(campionato, giornataInfo, properties, basePathData);
            } else if (event.getSource() == salva) {
                handleSalvaDate(giornataInfo);
            } else if (event.getSource() == resetDate) {
                handleResetDate(giornataInfo);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private void handleInitDb(FcCampionato campionato) {
        List<FcAttore> attori = attoreService.findAll();
        for (FcAttore a : attori) {
            if (a.isActive()) {
                for (int j = 1; j <= 26; j++) {
                    formazioneService.createFormazione(a, campionato.getIdCampionato(), j);
                }
                classificaService.create(a, campionato, 0d);
            }
        }
    }

    private void handleTestMailPrimary() {
        String from = env.getProperty(MAIL_PRIMARY_USERNAME);
        emailService.sendPrimaryEmail(
                from,
                "davide.cremona@gmail.com",
                "Testing from Spring Boot sendEmailPrimary",
                "Testing from Spring Boot sendEmailPrimary");
    }

    private void handleTestMailSecondary() {
        String from = env.getProperty(MAIL_SECONDARY_USERNAME);
        emailService.sendSecondaryEmail(
                from,
                "davide.cremona@gmail.com",
                "Testing from Spring Boot sendEmailSecondary",
                "Testing from Spring Boot sendEmailSecondary");
    }

//    private void handleDownloadQuotazioni(Properties properties, String basePathData, int codiceGiornata) throws Exception {
//        String urlFanta = (String) properties.get(URL_FANTA);
//        String httpUrl = urlFanta + "Giocatori-Quotazioni-Excel.asp?giornata=" + codiceGiornata;
//
//        log.info("httpUrl {}", httpUrl);
//
//        String fileName = "Q_" + codiceGiornata;
//        new JobProcessFileCsv().downloadCsv(httpUrl, basePathData, fileName, 2);
//    }

    private void handleUpdateGiocatori(String basePathData, int codiceGiornata) throws Exception {
        String fileName = basePathData + "Q_" + codiceGiornata + ".csv";
        boolean updateQuotazioni = chkUpdateQuotazioni.getValue();
        boolean updateImg = chkUpdateImg.getValue();
        String percentuale = String.valueOf(txtPercentuale.getValue().intValue());

        HashMap<Object, Object> map = jobProcessGiornata.initDbGiocatori(
                Costants.HTTP_URL_IMG,
                basePathData,
                fileName,
                updateQuotazioni,
                updateImg,
                percentuale);

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

//    private void handleDownloadVoti(
//            Properties properties,
//            String basePathData,
//            int codiceGiornata,
//            FcGiornataInfo giornataInfo) throws Exception {
//
//        String urlFanta = (String) properties.get(URL_FANTA);
//        String votiExcel = Boolean.TRUE.equals(chkUfficiali.getValue())
//                ? "Voti-Ufficiali-Excel"
//                : "Voti-Ufficiosi-Excel";
//
//        String httpUrl = urlFanta + votiExcel + ".asp?giornataScelta=" + codiceGiornata;
//        String fileName = "voti_" + codiceGiornata;
//
//        jobProcessFileCsv.downloadCsv(httpUrl, basePathData, fileName, 3);
//
//        fileName = basePathData + "voti_" + codiceGiornata + ".csv";
//        jobProcessGiornata.aggiornamentoPFGiornata(properties, fileName, String.valueOf(codiceGiornata));
//
//        if (giornataInfo != null) {
//            jobProcessGiornata.checkSeiPolitico(giornataInfo.getCodiceGiornata());
//        }
//    }

    private void handleSeiPolitico(int codiceGiornata) {
        FcSquadra squadra = comboSquadreA.getValue();
        if (squadra == null) {
            throw new IllegalArgumentException("Squadra obbligatoria");
        }

        jobProcessGiornata.seiPolitico(codiceGiornata, squadra);
    }

    private void handleCalcola(int codiceGiornata, FcCampionato campionato) {
        int forzaVotoGiocatore = Boolean.TRUE.equals(chkForzaVotoGiocatore.getValue()) ? 0 : -1;

        jobProcessGiornata.algoritmo(
                codiceGiornata,
                campionato,
                forzaVotoGiocatore,
                chkRoundVotoGiocatore.getValue());

        jobProcessGiornata.statistiche(campionato);
        jobProcessGiornata.aggiornaVotiGiocatori(codiceGiornata, forzaVotoGiocatore, chkRoundVotoGiocatore.getValue());
        jobProcessGiornata.aggiornaTotRosa(String.valueOf(campionato.getIdCampionato()), codiceGiornata);
        jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt", "score");
        jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt_old", "score_old");
        jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt_old", "score_grand_prix");
    }

    private void handlePdfAndMail(
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            Properties properties,
            String basePathData) throws SQLException, IOException {

        String pathImg = "images/";
        properties.setProperty("ACTIVE_MAIL", String.valueOf(chkSendMail.getValue()));
        properties.setProperty("INFO_RESULT", Boolean.TRUE.equals(chkUfficiali.getValue()) ? "UFFICIALI" : "UFFICIOSI");

        jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, properties, pathImg, basePathData);
    }

    private void handleSalvaDate(FcGiornataInfo giornataInfo) {
        if (giornataInfo == null) {
            return;
        }

        log.info("da1 {}", da1.getValue());
        log.info("da2 {}", da2.getValue());
        log.info("dg {}", dg.getValue());
        log.info("dp {}", dp.getValue());

        giornataInfo.setDataAnticipo1(da1.getValue());
        giornataInfo.setDataAnticipo2(da2.getValue());
        giornataInfo.setDataGiornata(dg.getValue());
        giornataInfo.setDataPosticipo(dp.getValue());

        giornataInfoService.save(giornataInfo);
    }

    private void handleResetDate(FcGiornataInfo giornataInfo) {
        da1.setValue(null);
        da2.setValue(null);
        dg.setValue(null);
        dp.setValue(null);

        if (giornataInfo == null) {
            return;
        }

        List<FcCalendarioCompetizione> listCalendario = calendarioCompetizioneService.findCustom(giornataInfo);
        if (listCalendario.isEmpty()) {
            return;
        }

        LocalDateTime tmpData = listCalendario.get(0).getData();
        ArrayList<LocalDateTime> listDate = new ArrayList<>();

        for (FcCalendarioCompetizione c : listCalendario) {
            log.info("{}", tmpData.getDayOfWeek());
            if (tmpData.getDayOfWeek() != c.getData().getDayOfWeek()) {
                listDate.add(tmpData);
                tmpData = c.getData();
            }
        }
        listDate.add(tmpData);

        if (listDate.size() == 1) {
            dg.setValue(listDate.get(0).minusMinutes(1));
        } else if (listDate.size() == 2) {
            da2.setValue(listDate.get(0).minusMinutes(1));
            dg.setValue(listDate.get(1).minusMinutes(1));
        } else if (listDate.size() == 3) {
            da2.setValue(listDate.get(0).minusMinutes(1));
            dg.setValue(listDate.get(1).minusMinutes(1));
            dp.setValue(listDate.get(2).minusMinutes(1));
        } else if (listDate.size() >= 4) {
            da1.setValue(listDate.get(0).minusMinutes(1));
            da2.setValue(listDate.get(1).minusMinutes(1));
            dg.setValue(listDate.get(2).minusMinutes(1));
            dp.setValue(listDate.get(3).minusMinutes(1));
        }

        log.info("2 {}", da1.getValue());
        log.info("2 {}", da2.getValue());
        log.info("2 {}", dg.getValue());
        log.info("2 {}", dp.getValue());
    }

    private void validateBasePath(String basePathData) {
        if (basePathData == null) {
            throw new IllegalArgumentException("PATH_TMP non configurato");
        }

        File file = new File(basePathData);
        if (!file.exists()) {
            CustomMessageDialog.showMessageError("Impossibile trovare il percorso specificato " + basePathData);
            throw new IllegalArgumentException("Percorso non valido");
        }
    }

    private void validateGiornata(int codiceGiornata) {
        if (codiceGiornata == 0) {
            throw new IllegalArgumentException("Giornata obbligatoria");
        }
    }

    private void validateAttore(FcAttore attore) {
        if (attore == null) {
            throw new IllegalArgumentException("Attore obbligatorio");
        }
    }

    private @NonNull ConfirmDialog getConfirmDialog(int codiceGiornata, FcCampionato campionato) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(CustomMessageDialog.TITLE_MSG_CONFIRM);
        dialog.setText("Confermi inserimento formazioni 422 per la giornata " + codiceGiornata);
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        dialog.setRejectable(false);
        dialog.setConfirmText("Conferma");
        dialog.addConfirmListener(e -> {
            try {
                int giornata = comboGiornata != null && !comboGiornata.isEmpty()
                        ? comboGiornata.getValue().getCodiceGiornata()
                        : 0;

                for (FcAttore a : squadre) {
                    jobProcessGiornata.inserisciFormazione442(campionato, a, giornata);
                }

                CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
            } catch (Exception exception) {
                CustomMessageDialog.showMessageErrorDetails(
                        CustomMessageDialog.MSG_ERROR_GENERIC,
                        exception.getMessage());
            }
        });
        return dialog;
    }

    private void sendMailInfoGiornata(FcGiornataInfo ggInfo) throws Exception {
        String subject = "Avvio Giornata - " + Utils.buildInfoGiornataHtml(ggInfo);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>FC</title></head>");
        html.append("<body>");
        html.append("<p>Prossima Giornata: ").append(Utils.buildInfoGiornataHtml(ggInfo)).append("</p>");
        html.append("<br><br>");
        html.append("<table>");

        List<FcGiornata> partite = giornataService.findByFcGiornataInfo(ggInfo);
        for (FcGiornata partita : partite) {
            html.append("<tr>")
                    .append("<td>").append(partita.getFcAttoreByIdAttoreCasa().getDescAttore()).append("</td>")
                    .append("<td>").append(partita.getFcAttoreByIdAttoreFuori().getDescAttore()).append("</td>")
                    .append("</tr>");
        }

        html.append("</table>");
        html.append("<br><br>");
        html.append("<p>Data Anticipo1: ").append(formatDate(ggInfo.getDataAnticipo1())).append("</p>");
        html.append("<p>Data Anticipo2: ").append(formatDate(ggInfo.getDataAnticipo2())).append("</p>");
        html.append("<p>Data Giornata: ").append(formatDate(ggInfo.getDataGiornata())).append("</p>");
        html.append("<p>Data Posticipo: ").append(formatDate(ggInfo.getDataPosticipo())).append("</p>");
        html.append("<br><br>");
        html.append("<p>Ciao Davide</p>");
        html.append("</body><html>");

        Properties properties = getSessionAttribute(SESSION_PROPERTIES, Properties.class);
        properties.setProperty("ACTIVE_MAIL", String.valueOf(chkSendMail.getValue()));

        String[] to = buildMailRecipients(properties);

        try {
            String from = env.getProperty(MAIL_SECONDARY_USERNAME);
            emailService.sendMail(false, from, to, null, null, subject, html.toString(), "text/html", null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String from = env.getProperty(MAIL_PRIMARY_USERNAME);
            emailService.sendMail(true, from, to, null, null, subject, html.toString(), "text/html", null);
        }
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : Utils.formatLocalDateTime(value, Costants.DATA_FORMATTED);
    }

    private String[] buildMailRecipients(Properties properties) {
        StringBuilder destinatari = new StringBuilder();
        String activeMail = properties.getProperty("ACTIVE_MAIL");

        if ("true".equals(activeMail)) {
            List<FcAttore> attori = attoreService.findByActive(true);
            for (FcAttore a : attori) {
                if (a.isNotifiche()) {
                    destinatari.append(a.getEmail()).append(";");
                }
            }
        } else {
            destinatari.append(properties.getProperty("to"));
        }

        return StringUtils.isNotEmpty(destinatari.toString())
                ? Utils.tornaArrayString(destinatari.toString(), ";")
                : null;
    }

    private Grid<FcGiocatore> getTableGiocatori() {
        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(new ArrayList<>());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth("550px");

        Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = buildCompactRow();
            cellLayout.setAlignItems(Alignment.STRETCH);
            cellLayout.setSizeFull();

            if (g != null && g.getFcRuolo() != null) {
                String ruolo = g.getFcRuolo().getIdRuolo().toLowerCase();
                cellLayout.add(Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png")));
            }
            return cellLayout;
        }));
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader(Costants.RUOLO);
        ruoloColumn.setAutoWidth(true);

        Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = buildCompactRow();
            cellLayout.setAlignItems(Alignment.STRETCH);
            cellLayout.setSizeFull();

            if (g != null &&  g.getImgSmall() != null) {
                try {
                    cellLayout.add(Utils.getImage(g.getNomeImg(), g.getImgSmall().getBinaryStream()));
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            cellLayout.add(new Span(""+g.getIdGiocatore()));
            cellLayout.add(new Span(" - "));
            cellLayout.add(new Span(g.getCognGiocatore()));
            
            return cellLayout;
        }));
        cognGiocatoreColumn.setSortable(false);
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setAutoWidth(true);

        Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = buildCompactRow();
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (g != null && g.getFcSquadra() != null) {
                FcSquadra sq = g.getFcSquadra();
                if (sq.getImg() != null) {
                    try {
                        cellLayout.add(Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream()));
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                cellLayout.add(new Span(sq.getNomeSquadra()));
            }
            return cellLayout;
        }));
        nomeSquadraColumn.setSortable(false);
        nomeSquadraColumn.setHeader(Costants.SQUADRA);
        nomeSquadraColumn.setAutoWidth(true);

        Column<FcGiocatore> quotazioneColumn = grid.addColumn(FcGiocatore::getQuotazione);
        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader("Q");
        quotazioneColumn.setAutoWidth(true);

        return grid;
    }

    private HorizontalLayout buildCompactRow() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
