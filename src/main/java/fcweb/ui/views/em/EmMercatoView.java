package fcweb.ui.views.em;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.ronny.AbsoluteLayout;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.ContentIdGenerator;
import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;
import fcweb.backend.data.entity.FcMercatoDettInfo;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.MercatoInfoService;
import fcweb.backend.service.MercatoService;
import fcweb.backend.service.RuoloService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mercato")
@Route(value = "emmercato", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmMercatoView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    private static final long serialVersionUID = 1L;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment env;

    @Autowired
    private EmailService emailService;

    private static final String width = "100px";
    private static final String height = "120px";

    private final int MAX_CAMBI = 12;
    private final int MAX_CHANGE_SQUADRA = 6;
    private final int NUM_GIOCATORI = 23;

    private String CREDITI_MERCATO = null;
    private int TOT_CAMBI_EFFETTUATI = 0;
    private int CHECK_TOT_CAMBI_EFFETTUATI = 0;

    private String currentGiornata = "";
    private String currentDescGiornata = "";

    private FcAttore attore = null;
    private FcCampionato campionato = null;
    private FcGiornataInfo giornataInfo = null;

    private AbsoluteLayout absLayout;
    private Button saveSendMail;
    // private Button sortBy;
    private ComboBox<FcAttore> comboAttore;

    // FILTER
    private ComboBox<FcRuolo> comboRuolo;
    private ComboBox<FcSquadra> comboNazione;
    private NumberField txtQuotaz;

    private Span txtCrediti;
    private Span txtCambi;
    private Span lblInfoP;
    private Span lblInfoD;
    private Span lblInfoC;
    private Span lblInfoA;

    private Grid<FcGiocatore> tablePlayer1;
    private Grid<FcGiocatore> tablePlayer2;
    private Grid<FcGiocatore> tablePlayer3;
    private Grid<FcGiocatore> tablePlayer4;
    private Grid<FcGiocatore> tablePlayer5;
    private Grid<FcGiocatore> tablePlayer6;
    private Grid<FcGiocatore> tablePlayer7;
    private Grid<FcGiocatore> tablePlayer8;
    private Grid<FcGiocatore> tablePlayer9;
    private Grid<FcGiocatore> tablePlayer10;
    private Grid<FcGiocatore> tablePlayer11;
    private Grid<FcGiocatore> tablePlayer12;
    private Grid<FcGiocatore> tablePlayer13;
    private Grid<FcGiocatore> tablePlayer14;
    private Grid<FcGiocatore> tablePlayer15;
    private Grid<FcGiocatore> tablePlayer16;
    private Grid<FcGiocatore> tablePlayer17;
    private Grid<FcGiocatore> tablePlayer18;
    private Grid<FcGiocatore> tablePlayer19;
    private Grid<FcGiocatore> tablePlayer20;
    private Grid<FcGiocatore> tablePlayer21;
    private Grid<FcGiocatore> tablePlayer22;
    private Grid<FcGiocatore> tablePlayer23;

    private Grid<FcProperties> tableContaPlayer;
    private List<FcProperties> modelContaPlayer = new ArrayList<>();

    private List<FcAttore> attori = null;
    private List<FcRuolo> ruoli = null;
    private List<FcSquadra> squadre = null;
    private String nextDate = null;
    private long millisDiff = 0;
    private Properties p = null;

    private List<FcGiocatore> modelFormazione = new ArrayList<>();

    private boolean activeFilter = true;

    private Grid<FcGiocatore> tableGiocatori;
    private List<FcGiocatore> modelPlayerG = new ArrayList<>();

    private Grid<FcGiocatore> tablePlayerP;
    private Grid<FcGiocatore> tablePlayerD;
    private Grid<FcGiocatore> tablePlayerC;
    private Grid<FcGiocatore> tablePlayerA;
    private List<FcGiocatore> modelPlayerP = new ArrayList<>();
    private List<FcGiocatore> modelPlayerD = new ArrayList<>();
    private List<FcGiocatore> modelPlayerC = new ArrayList<>();
    private List<FcGiocatore> modelPlayerA = new ArrayList<>();

    private List<FcGiocatore> modelPlayer1 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer2 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer3 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer4 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer5 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer6 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer7 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer8 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer9 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer10 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer11 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer12 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer13 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer14 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer15 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer16 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer17 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer18 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer19 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer20 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer21 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer22 = new ArrayList<>();
    private List<FcGiocatore> modelPlayer23 = new ArrayList<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GiocatoreService giocatoreController;

    @Autowired
    private AttoreService attoreController;

    @Autowired
    private RuoloService ruoloController;

    @Autowired
    private SquadraService squadraController;

    @Autowired
    private FormazioneService formazioneController;

    @Autowired
    private MercatoService mercatoController;

    @Autowired
    private MercatoInfoService mercatoInfoController;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AccessoService accessoController;

    @PostConstruct
    void init() throws Exception {
        LOG.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }
        accessoController.insertAccesso(this.getClass().getName());
        initData();
        initLayout();
    }

    private void initData() {

        p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
        attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
        campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
        giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
        nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
        millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

        currentGiornata = "" + giornataInfo.getCodiceGiornata();
        currentDescGiornata = giornataInfo.getDescGiornataFc();

        CREDITI_MERCATO = (String) p.get("CREDITI_MERCATO");

        attori = attoreController.findByActive(true);
        ruoli = ruoloController.findAll();
        squadre = squadraController.findAll();
    }

    private void showMessageStopInsert() {
        absLayout.setEnabled(false);
        CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ADMIN_MERCATO_KO);
    }

    public void initLayout() {

        absLayout = new AbsoluteLayout(1600, 1200);
        absLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        absLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);

        saveSendMail = new Button("Salva e Invia Mail");
        saveSendMail.addClickListener(this);

        // sortBy = new Button("Ordina per");
        // sortBy.addClickListener(this);

        comboAttore = new ComboBox<>();
        comboAttore.setItems(attori);
        comboAttore.setItemLabelGenerator(a -> a.getDescAttore());
        comboAttore.setClearButtonVisible(true);
        comboAttore.setPlaceholder("Attore");
        comboAttore.addValueChangeListener(event -> {
            FcAttore attoreSel = null;
            if (event.getSource().isEmpty()) {
                LOG.info("event.getSource().isEmpty()");
                removeAllElementsList();
                setModelGiocatori(null);
                if (activeFilter) {
                    refreshAndSortGridTabsRuoli("");
                } else {
                    refreshAndSortGridGiocatori();
                }
                TOT_CAMBI_EFFETTUATI = MAX_CAMBI;
                CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI;
                txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);
                txtCrediti.setText("" + CREDITI_MERCATO);
                lblInfoP.setText("0");
                lblInfoD.setText("0");
                lblInfoC.setText("0");
                lblInfoA.setText("0");
                return;
            } else if (event.getOldValue() == null) {
                LOG.info("event.getOldValue()");
                attoreSel = event.getValue();
            } else {
                LOG.info("new Value attore");
                attoreSel = event.getValue();
                attore = attoreSel;
                try {
                    removeAllElementsList();
                    setModelGiocatori(attore);
                    if (activeFilter) {
                        refreshAndSortGridTabsRuoli("");
                    } else {
                        refreshAndSortGridGiocatori();
                    }
                    loadFcFormazione(attore);
                    updateTot();
                    int cambiEff = getCambiEffettuati();
                    TOT_CAMBI_EFFETTUATI = MAX_CAMBI - cambiEff;
                    CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI;
                    txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
                }
            }
        });
        comboAttore.setValue(attore);
        comboAttore.setVisible(false);
        for (Role r : attore.getRoles()) {
            if (r.equals(Role.ADMIN)) {
                comboAttore.setVisible(true);
                break;
            }
        }

        comboRuolo = new ComboBox<>();
        comboRuolo.setItems(ruoli);
        comboRuolo.setItemLabelGenerator(a -> a.getIdRuolo());
        comboRuolo.setClearButtonVisible(true);
        comboRuolo.setPlaceholder(Costants.RUOLO);
        comboRuolo.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();
            Image imgR = Utils.buildImage(item.getIdRuolo().toLowerCase() + ".png",
                    resourceLoader.getResource(Costants.CLASSPATH_IMAGES + item.getIdRuolo().toLowerCase() + ".png"));
            container.add(imgR);
            return container;
        }));

        comboNazione = new ComboBox<>("Nazione");
        comboNazione.setItems(squadre);
        comboNazione.setItemLabelGenerator(s -> s.getNomeSquadra());
        comboNazione.setClearButtonVisible(true);
        // comboNazione.setPlaceholder("Nazione");
        comboNazione.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();
            // Image imgSq = buildImage("classpath:/img/nazioni/",
            // item.getNomeSquadra() + ".png");
            // container.add(imgSq);
            if (item.getImg() != null) {
                try {
                    Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
                    container.add(img);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            Span lblSquadra = new Span(item.getNomeSquadra());
            container.add(lblSquadra);
            return container;
        }));

        txtQuotaz = new NumberField("Quotazione <=");
        txtQuotaz.setMin(0d);
        txtQuotaz.setMax(500d);
        txtQuotaz.setStepButtonsVisible(true);

        setModelGiocatori(attore);

        if (activeFilter) {
            tablePlayerP = getTablePlayer(modelPlayerP);
            tablePlayerD = getTablePlayer(modelPlayerD);
            tablePlayerC = getTablePlayer(modelPlayerC);
            tablePlayerA = getTablePlayer(modelPlayerA);
        } else {
            tableGiocatori = getTablePlayer(modelPlayerG);
        }

        tablePlayer1 = getTableGiocatore(modelPlayer1);
        tablePlayer2 = getTableGiocatore(modelPlayer2);
        tablePlayer3 = getTableGiocatore(modelPlayer3);
        tablePlayer4 = getTableGiocatore(modelPlayer4);
        tablePlayer5 = getTableGiocatore(modelPlayer5);
        tablePlayer6 = getTableGiocatore(modelPlayer6);
        tablePlayer7 = getTableGiocatore(modelPlayer7);
        tablePlayer8 = getTableGiocatore(modelPlayer8);
        tablePlayer9 = getTableGiocatore(modelPlayer9);
        tablePlayer10 = getTableGiocatore(modelPlayer10);
        tablePlayer11 = getTableGiocatore(modelPlayer11);
        tablePlayer12 = getTableGiocatore(modelPlayer12);
        tablePlayer13 = getTableGiocatore(modelPlayer13);
        tablePlayer14 = getTableGiocatore(modelPlayer14);
        tablePlayer15 = getTableGiocatore(modelPlayer15);
        tablePlayer16 = getTableGiocatore(modelPlayer16);
        tablePlayer17 = getTableGiocatore(modelPlayer17);
        tablePlayer18 = getTableGiocatore(modelPlayer18);
        tablePlayer19 = getTableGiocatore(modelPlayer19);
        tablePlayer20 = getTableGiocatore(modelPlayer20);
        tablePlayer21 = getTableGiocatore(modelPlayer21);
        tablePlayer22 = getTableGiocatore(modelPlayer22);
        tablePlayer23 = getTableGiocatore(modelPlayer23);

        HorizontalLayout layoutFilterRow1 = new HorizontalLayout();
        layoutFilterRow1.setMargin(false);
        layoutFilterRow1.add(comboNazione);
        layoutFilterRow1.add(txtQuotaz);

        VerticalLayout layoutFilter = new VerticalLayout();
        layoutFilter.setMargin(false);
        layoutFilter.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

        layoutFilter.add(layoutFilterRow1);

        Details panelFilter = new Details("Filtra per", layoutFilter);
        panelFilter.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        panelFilter.setOpened(true);

        int top = 5;
        int left = 10;

        absLayout.add(panelFilter, left, top);

        if (activeFilter) {
            // VerticalLayout container = new VerticalLayout();
            // PagedTabs tabsRuoli = new PagedTabs(container);
            // tabsRuoli.add("P", tablePlayerP, false);
            // tabsRuoli.add("D", tablePlayerD, false);
            // tabsRuoli.add("C", tablePlayerC, false);
            // tabsRuoli.add("A", tablePlayerA, false);
            // absLayout.add(tabsRuoli, 10, 170);
            // absLayout.add(container, 10, 200);
            // add(tabSheet);

            TabSheet tabSheet = new TabSheet();
            tabSheet.add("P", tablePlayerP);
            tabSheet.add("D", tablePlayerD);
            tabSheet.add("C", tablePlayerC);
            tabSheet.add("A", tablePlayerA);
            absLayout.add(tabSheet, 10, 170);

        } else {
            absLayout.add(tableGiocatori, 10, 250);
        }

        final VerticalLayout layoutAvviso = new VerticalLayout();
        layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);
        layoutAvviso.setWidth("500px");

        HorizontalLayout cssLayout = new HorizontalLayout();
        Span lblRow1Info = new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato));
        cssLayout.add(lblRow1Info);
        layoutAvviso.add(cssLayout);

        HorizontalLayout cssLayout2 = new HorizontalLayout();
        Span lblRow2Info = new Span("Consegna entro: " + nextDate);
        cssLayout2.add(lblRow2Info);
        layoutAvviso.add(cssLayout2);

        if (millisDiff == 0) {

        } else {
            SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
            timer.setHours(true);
            timer.setMinutes(true);
            timer.setFractions(false);
            timer.start();
            timer.isRunning();
            timer.addTimerEndEvent(ev -> showMessageStopInsert());
            layoutAvviso.add(timer);
        }

        left = 500;
        absLayout.add(layoutAvviso, left, top);

        top = 45;
        left = 1050;
        absLayout.add(saveSendMail, left, top);

        top = 45;
        left = 1250;
        absLayout.add(comboAttore, left, top);

        // top = 75;
        // left = 1050;
        // absLayout.add(sortBy, left, top);

        final HorizontalLayout layoutInfoGenerali = new HorizontalLayout();
        layoutInfoGenerali.setPadding(true);
        layoutInfoGenerali.setSpacing(true);
        layoutInfoGenerali.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutInfoGenerali.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_GRAY);
        layoutInfoGenerali.setAlignItems(FlexComponent.Alignment.END);

        int cambiEff = getCambiEffettuati();
        TOT_CAMBI_EFFETTUATI = MAX_CAMBI - cambiEff;
        CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI;
        LOG.info("DESC_ATTORE " + attore.getDescAttore() + " cambi " + CHECK_TOT_CAMBI_EFFETTUATI);
        if (CHECK_TOT_CAMBI_EFFETTUATI <= 0) {
            this.saveSendMail.setEnabled(false);
        }

        Span lblInfo = new Span();
        lblInfo.setText("Hai ancora a disposizione:");

        Span lblCrediti = new Span();
        lblCrediti.setText("Crediti:");
        lblCrediti.getElement().getStyle().set("color", Costants.RED);
        lblCrediti.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);

        txtCrediti = new Span();
        txtCrediti.setText("" + CREDITI_MERCATO);
        // txtCrediti.setReadOnly(true);
        // txtCrediti.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        txtCrediti.getElement().getStyle().set("color", Costants.RED);
        txtCrediti.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);

        Span lblCanmbi = new Span();
        lblCanmbi.setText("Cambi:");
        lblCanmbi.getElement().getStyle().set("color", Costants.BLUE);
        lblCanmbi.getElement().getStyle().set("-webkit-text-fill-color", Costants.BLUE);

        txtCambi = new Span();
        txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);
        // txtCambi.setReadOnly(true);
        // txtCambi.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        txtCambi.getElement().getStyle().set("color", Costants.BLUE);
        txtCambi.getElement().getStyle().set("-webkit-text-fill-color", Costants.BLUE);

        layoutInfoGenerali.add(lblInfo);
        layoutInfoGenerali.add(lblCrediti);
        layoutInfoGenerali.add(txtCrediti);
        layoutInfoGenerali.add(lblCanmbi);
        layoutInfoGenerali.add(txtCambi);

        top = 160;
        left = 500;

        absLayout.add(layoutInfoGenerali, left, top);

        final HorizontalLayout layoutInfoRuolo = new HorizontalLayout();
        layoutInfoRuolo.setClassName("sidemenu-header");
        layoutInfoRuolo.getThemeList().set("dark", true);
        layoutInfoRuolo.setPadding(true);
        layoutInfoRuolo.setSpacing(true);
        // layoutInfoRuolo.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        // layoutInfoRuolo.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
        layoutInfoRuolo.setAlignItems(FlexComponent.Alignment.END);

        Image imgP = Utils.buildImage("p.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "p.png"));
        Image imgD = Utils.buildImage("d.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "d.png"));
        Image imgC = Utils.buildImage("c.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "c.png"));
        Image imgA = Utils.buildImage("a.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "a.png"));

        lblInfoP = new Span();
        lblInfoD = new Span();
        lblInfoC = new Span();
        lblInfoA = new Span();
        lblInfoP.setText("0");
        lblInfoD.setText("0");
        lblInfoC.setText("0");
        lblInfoA.setText("0");

        layoutInfoRuolo.add(imgP);
        layoutInfoRuolo.add(lblInfoP);
        layoutInfoRuolo.add(imgD);
        layoutInfoRuolo.add(lblInfoD);
        layoutInfoRuolo.add(imgC);
        layoutInfoRuolo.add(lblInfoC);
        layoutInfoRuolo.add(imgA);
        layoutInfoRuolo.add(lblInfoA);

        top = 160;
        left = 950;

        absLayout.add(layoutInfoRuolo, left, top);

        top = 230;
        left = 500;

        absLayout.add(tablePlayer1, left, top);

        left = left + 120;

        absLayout.add(tablePlayer2, left, top);

        left = left + 120;

        absLayout.add(tablePlayer3, left, top);

        left = left + 120;

        absLayout.add(tablePlayer4, left, top);

        left = left + 120;

        absLayout.add(tablePlayer5, left, top);

        left = left + 120;

        absLayout.add(tablePlayer6, left, top);

        top = 380;
        left = 500;

        absLayout.add(tablePlayer7, left, top);

        left = left + 120;

        absLayout.add(tablePlayer8, left, top);

        left = left + 120;

        absLayout.add(tablePlayer9, left, top);

        left = left + 120;

        absLayout.add(tablePlayer10, left, top);

        left = left + 120;

        absLayout.add(tablePlayer11, left, top);

        left = left + 120;

        absLayout.add(tablePlayer12, left, top);

        top = 530;
        left = 500;

        absLayout.add(tablePlayer13, left, top);

        left = left + 120;

        absLayout.add(tablePlayer14, left, top);

        left = left + 120;

        absLayout.add(tablePlayer15, left, top);

        left = left + 120;

        absLayout.add(tablePlayer16, left, top);

        left = left + 120;

        absLayout.add(tablePlayer17, left, top);

        left = left + 120;

        absLayout.add(tablePlayer18, left, top);

        top = 680;
        left = 500;

        absLayout.add(tablePlayer19, left, top);

        left = left + 120;

        absLayout.add(tablePlayer20, left, top);

        left = left + 120;

        absLayout.add(tablePlayer21, left, top);

        left = left + 120;

        absLayout.add(tablePlayer22, left, top);

        left = left + 120;

        absLayout.add(tablePlayer23, left, top);

        tableContaPlayer = buildTableContaPlayer(modelContaPlayer);

        top = 200;
        left = 1250;

        Span lblInfoGiocatori = new Span();
        lblInfoGiocatori.setText("Giocatori per Nazione:");
        lblInfoGiocatori.getStyle().set(Costants.FONT_SIZE, "16px");
        lblInfoGiocatori.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);

        absLayout.add(lblInfoGiocatori, left, top);

        top = 230;
        left = 1250;

        absLayout.add(tableContaPlayer, left, top);

        this.add(absLayout);

        try {
            loadFcFormazione(attore);
            updateTot();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }

        if ("0".equals(p.getProperty("ABILITA_MERCATO"))) {
            showMessageStopInsert();
        } else {
            // if (millisDiff == 0) {
            // showMessageStopInsert();
            // }
        }
    }

    private void updateLabelCambi() {

        LOG.info("START updateLabelCambi");

        int totaleCambi = calcolaCambi();

        LOG.debug("totaleCambi " + totaleCambi);
        CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI - totaleCambi;
        txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);

        LOG.info("END updateLabelCambi ");
    }

    private int calcolaCambi() {

        LOG.info("START calcolaCambi");

        int totCambi = 0;
        for (int i = 0; i < modelFormazione.size(); i++) {
            FcGiocatore beanPlayer = modelFormazione.get(i);
            if (i == 0) {
                if (modelPlayer1.size() != 0) {
                    FcGiocatore bean = modelPlayer1.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 1) {
                if (modelPlayer2.size() != 0) {
                    FcGiocatore bean = modelPlayer2.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 2) {
                if (modelPlayer3.size() != 0) {
                    FcGiocatore bean = modelPlayer3.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 3) {
                if (modelPlayer4.size() != 0) {
                    FcGiocatore bean = modelPlayer4.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 4) {
                if (modelPlayer5.size() != 0) {
                    FcGiocatore bean = modelPlayer5.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 5) {
                if (modelPlayer6.size() != 0) {
                    FcGiocatore bean = modelPlayer6.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 6) {
                if (modelPlayer7.size() != 0) {
                    FcGiocatore bean = modelPlayer7.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 7) {
                if (modelPlayer8.size() != 0) {
                    FcGiocatore bean = modelPlayer8.get(0);
                    if (beanPlayer.getIdGiocatore() != (bean.getIdGiocatore())) {
                        totCambi++;
                    }
                }
            } else if (i == 8) {
                if (modelPlayer9.size() != 0) {
                    FcGiocatore bean = modelPlayer9.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 9) {
                if (modelPlayer10.size() != 0) {
                    FcGiocatore bean = modelPlayer10.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 10) {
                if (modelPlayer11.size() != 0) {
                    FcGiocatore bean = modelPlayer11.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 11) {
                if (modelPlayer12.size() != 0) {
                    FcGiocatore bean = modelPlayer12.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 12) {
                if (modelPlayer13.size() != 0) {
                    FcGiocatore bean = modelPlayer13.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 13) {
                if (modelPlayer14.size() != 0) {
                    FcGiocatore bean = modelPlayer14.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 14) {
                if (modelPlayer15.size() != 0) {
                    FcGiocatore bean = modelPlayer15.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 15) {
                if (modelPlayer16.size() != 0) {
                    FcGiocatore bean = modelPlayer16.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 16) {
                if (modelPlayer17.size() != 0) {
                    FcGiocatore bean = modelPlayer17.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 17) {
                if (modelPlayer18.size() != 0) {
                    FcGiocatore bean = modelPlayer18.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 18) {
                if (modelPlayer19.size() != 0) {
                    FcGiocatore bean = modelPlayer19.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 19) {
                if (modelPlayer20.size() != 0) {
                    FcGiocatore bean = modelPlayer20.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 20) {
                if (modelPlayer21.size() != 0) {
                    FcGiocatore bean = modelPlayer21.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 21) {
                if (modelPlayer22.size() != 0) {
                    FcGiocatore bean = modelPlayer22.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            } else if (i == 22) {
                if (modelPlayer23.size() != 0) {
                    FcGiocatore bean = modelPlayer23.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        totCambi++;
                    }
                }
            }
        }

        LOG.info("END calcolaCambi");

        return totCambi;
    }

    private void updateTot() {

        LOG.info("START updateTot");

        int tot = 0;

        int countP = 0;
        int countD = 0;
        int countC = 0;
        int countA = 0;

        HashMap<String, String> map = new HashMap<>();

        if (modelPlayer1.size() != 0) {
            FcGiocatore bean = modelPlayer1.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer2.size() != 0) {
            FcGiocatore bean = modelPlayer2.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer3.size() != 0) {
            FcGiocatore bean = modelPlayer3.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer4.size() != 0) {
            FcGiocatore bean = modelPlayer4.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer5.size() != 0) {
            FcGiocatore bean = modelPlayer5.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer6.size() != 0) {
            FcGiocatore bean = modelPlayer6.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer7.size() != 0) {
            FcGiocatore bean = modelPlayer7.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer8.size() != 0) {
            FcGiocatore bean = modelPlayer8.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer9.size() != 0) {
            FcGiocatore bean = modelPlayer9.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer10.size() != 0) {
            FcGiocatore bean = modelPlayer10.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer11.size() != 0) {
            FcGiocatore bean = modelPlayer11.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer12.size() != 0) {
            FcGiocatore bean = modelPlayer12.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer13.size() != 0) {
            FcGiocatore bean = modelPlayer13.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer14.size() != 0) {
            FcGiocatore bean = modelPlayer14.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer15.size() != 0) {
            FcGiocatore bean = modelPlayer15.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer16.size() != 0) {
            FcGiocatore bean = modelPlayer16.get(0);
            tot += bean.getQuotazione();

            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer17.size() != 0) {
            FcGiocatore bean = modelPlayer17.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer18.size() != 0) {
            FcGiocatore bean = modelPlayer18.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer19.size() != 0) {
            FcGiocatore bean = modelPlayer19.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer20.size() != 0) {
            FcGiocatore bean = modelPlayer20.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer21.size() != 0) {
            FcGiocatore bean = modelPlayer21.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer22.size() != 0) {
            FcGiocatore bean = modelPlayer22.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }
        if (modelPlayer23.size() != 0) {
            FcGiocatore bean = modelPlayer23.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
            refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
        }

        txtCrediti.setText("" + (Integer.parseInt(CREDITI_MERCATO) - tot));
        lblInfoP.setText("" + countP);
        lblInfoD.setText("" + countD);
        lblInfoC.setText("" + countC);
        lblInfoA.setText("" + countA);

        List<FcProperties> list = new ArrayList<>();
        if (!map.isEmpty()) {
            Iterator<?> it = map.entrySet().iterator();
            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry pairs = (Map.Entry) it.next();
                FcProperties p = new FcProperties();
                p.setKey((String) pairs.getKey());
                p.setValue((String) pairs.getValue());
                list.add(p);
            }
        }
        modelContaPlayer = list;
        modelContaPlayer.sort((p1, p2) -> p2.getValue().compareToIgnoreCase(p1.getValue()));
        tableContaPlayer.setItems(modelContaPlayer);
        tableContaPlayer.getDataProvider().refreshAll();

        LOG.info("END updateTot");
    }

    private void refreshContaGiocatori(HashMap<String, String> m, String sq) {

        if (m.containsKey(sq)) {
            String v = m.get(sq);
            int newValue = Integer.parseInt(v) + 1;
            m.put(sq, "" + newValue);
        } else {
            m.put(sq, "1");
        }
    }

    private void removeAllElementsList() {
        LOG.info("removeAllElementsList");

        removeMercatoGiocatore();

        if (activeFilter) {
            if (modelPlayerP.size() != 0) {
                modelPlayerP.clear();
            }
            if (modelPlayerD.size() != 0) {
                modelPlayerD.clear();
            }
            if (modelPlayerC.size() != 0) {
                modelPlayerC.clear();
            }
            if (modelPlayerA.size() != 0) {
                modelPlayerA.clear();
            }
            refreshAndSortGridTabsRuoli("");
        } else {
            if (modelPlayerG.size() != 0) {
                modelPlayerG.clear();
            }
            refreshAndSortGridGiocatori();
        }

        if (!modelContaPlayer.isEmpty()) {
            modelContaPlayer.clear();
        }
        tableContaPlayer.getDataProvider().refreshAll();
    }

    private void removeMercatoGiocatore() {
        LOG.info("removeMercatoGiocatore");
        if (modelPlayer1.size() != 0) {
            modelPlayer1.clear();
            tablePlayer1.getDataProvider().refreshAll();
        }
        if (modelPlayer2.size() != 0) {
            modelPlayer2.clear();
            tablePlayer2.getDataProvider().refreshAll();
        }
        if (modelPlayer3.size() != 0) {
            modelPlayer3.clear();
            tablePlayer3.getDataProvider().refreshAll();
        }
        if (modelPlayer4.size() != 0) {
            modelPlayer4.clear();
            tablePlayer4.getDataProvider().refreshAll();
        }
        if (modelPlayer5.size() != 0) {
            modelPlayer5.clear();
            tablePlayer5.getDataProvider().refreshAll();
        }
        if (modelPlayer6.size() != 0) {
            modelPlayer6.clear();
            tablePlayer6.getDataProvider().refreshAll();
        }
        if (modelPlayer7.size() != 0) {
            modelPlayer7.clear();
            tablePlayer7.getDataProvider().refreshAll();
        }
        if (modelPlayer8.size() != 0) {
            modelPlayer8.clear();
            tablePlayer8.getDataProvider().refreshAll();
        }
        if (modelPlayer9.size() != 0) {
            modelPlayer9.clear();
            tablePlayer9.getDataProvider().refreshAll();
        }
        if (modelPlayer10.size() != 0) {
            modelPlayer10.clear();
            tablePlayer10.getDataProvider().refreshAll();
        }
        if (modelPlayer11.size() != 0) {
            modelPlayer11.clear();
            tablePlayer11.getDataProvider().refreshAll();
        }
        if (modelPlayer12.size() != 0) {
            modelPlayer12.clear();
            tablePlayer12.getDataProvider().refreshAll();
        }
        if (modelPlayer13.size() != 0) {
            modelPlayer13.clear();
            tablePlayer13.getDataProvider().refreshAll();
        }
        if (modelPlayer14.size() != 0) {
            modelPlayer14.clear();
            tablePlayer14.getDataProvider().refreshAll();
        }
        if (modelPlayer15.size() != 0) {
            modelPlayer15.clear();
            tablePlayer15.getDataProvider().refreshAll();
        }
        if (modelPlayer16.size() != 0) {
            modelPlayer16.clear();
            tablePlayer16.getDataProvider().refreshAll();
        }
        if (modelPlayer17.size() != 0) {
            modelPlayer17.clear();
            tablePlayer17.getDataProvider().refreshAll();
        }
        if (modelPlayer18.size() != 0) {
            modelPlayer18.clear();
            tablePlayer18.getDataProvider().refreshAll();
        }
        if (modelPlayer19.size() != 0) {
            modelPlayer19.clear();
            tablePlayer19.getDataProvider().refreshAll();
        }
        if (modelPlayer20.size() != 0) {
            modelPlayer20.clear();
            tablePlayer20.getDataProvider().refreshAll();
        }
        if (modelPlayer21.size() != 0) {
            modelPlayer21.clear();
            tablePlayer21.getDataProvider().refreshAll();
        }
        if (modelPlayer22.size() != 0) {
            modelPlayer22.clear();
            tablePlayer22.getDataProvider().refreshAll();
        }
        if (modelPlayer23.size() != 0) {
            modelPlayer23.clear();
            tablePlayer23.getDataProvider().refreshAll();
        }
    }

    private void loadFcFormazione(FcAttore att) throws Exception {
        LOG.info("loadFcFormazione " + att.getDescAttore());
        if (modelFormazione.size() != 0) {
            modelFormazione.clear();
        }
        List<FcFormazione> listFormazione = formazioneController
                .findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, att);
        for (FcFormazione f : listFormazione) {
            FcGiocatore bean = f.getFcGiocatore();
            if (bean != null) {
                modelFormazione.add(bean);
                int ord = f.getId().getOrdinamento();
                if (ord == 1) {
                    modelPlayer1.clear();
                    modelPlayer1.add(bean);
                    tablePlayer1.getDataProvider().refreshAll();
                } else if (ord == 2) {
                    modelPlayer2.clear();
                    modelPlayer2.add(bean);
                    tablePlayer2.getDataProvider().refreshAll();
                } else if (ord == 3) {
                    modelPlayer3.clear();
                    modelPlayer3.add(bean);
                    tablePlayer3.getDataProvider().refreshAll();
                } else if (ord == 4) {
                    modelPlayer4.clear();
                    modelPlayer4.add(bean);
                    tablePlayer4.getDataProvider().refreshAll();
                } else if (ord == 5) {
                    modelPlayer5.clear();
                    modelPlayer5.add(bean);
                    tablePlayer5.getDataProvider().refreshAll();
                } else if (ord == 6) {
                    modelPlayer6.clear();
                    modelPlayer6.add(bean);
                    tablePlayer6.getDataProvider().refreshAll();
                } else if (ord == 7) {
                    modelPlayer7.clear();
                    modelPlayer7.add(bean);
                    tablePlayer7.getDataProvider().refreshAll();
                } else if (ord == 8) {
                    modelPlayer8.clear();
                    modelPlayer8.add(bean);
                    tablePlayer8.getDataProvider().refreshAll();
                } else if (ord == 9) {
                    modelPlayer9.clear();
                    modelPlayer9.add(bean);
                    tablePlayer9.getDataProvider().refreshAll();
                } else if (ord == 10) {
                    modelPlayer10.clear();
                    modelPlayer10.add(bean);
                    tablePlayer10.getDataProvider().refreshAll();
                } else if (ord == 11) {
                    modelPlayer11.clear();
                    modelPlayer11.add(bean);
                    tablePlayer11.getDataProvider().refreshAll();
                } else if (ord == 12) {
                    modelPlayer12.clear();
                    modelPlayer12.add(bean);
                    tablePlayer12.getDataProvider().refreshAll();
                } else if (ord == 13) {
                    modelPlayer13.clear();
                    modelPlayer13.add(bean);
                    tablePlayer13.getDataProvider().refreshAll();
                } else if (ord == 14) {
                    modelPlayer14.clear();
                    modelPlayer14.add(bean);
                    tablePlayer14.getDataProvider().refreshAll();
                } else if (ord == 15) {
                    modelPlayer15.clear();
                    modelPlayer15.add(bean);
                    tablePlayer15.getDataProvider().refreshAll();
                } else if (ord == 16) {
                    modelPlayer16.clear();
                    modelPlayer16.add(bean);
                    tablePlayer16.getDataProvider().refreshAll();
                } else if (ord == 17) {
                    modelPlayer17.clear();
                    modelPlayer17.add(bean);
                    tablePlayer17.getDataProvider().refreshAll();
                } else if (ord == 18) {
                    modelPlayer18.clear();
                    modelPlayer18.add(bean);
                    tablePlayer18.getDataProvider().refreshAll();
                } else if (ord == 19) {
                    modelPlayer19.clear();
                    modelPlayer19.add(bean);
                    tablePlayer19.getDataProvider().refreshAll();
                } else if (ord == 20) {
                    modelPlayer20.clear();
                    modelPlayer20.add(bean);
                    tablePlayer20.getDataProvider().refreshAll();
                } else if (ord == 21) {
                    modelPlayer21.clear();
                    modelPlayer21.add(bean);
                    tablePlayer21.getDataProvider().refreshAll();
                } else if (ord == 22) {
                    modelPlayer22.clear();
                    modelPlayer22.add(bean);
                    tablePlayer22.getDataProvider().refreshAll();
                } else if (ord == 23) {
                    modelPlayer23.clear();
                    modelPlayer23.add(bean);
                    tablePlayer23.getDataProvider().refreshAll();
                }
            }
        }
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {

        if (event.getSource() == saveSendMail) {

            if (check()) {

                String msg = "";
                if (!currentGiornata.equals("1")) {
                    msg = "Attenzione, una volta cliccato conferma il cambio è definitivo e non è possibile annullarlo.";
                } else {
                    msg += "La tua rosa calciatori è stata completata con successo.";
                }
                msg += "Si ricorda di inserire la formazione per la giornata  <" + currentDescGiornata + ">";

                // MessageDialog messageDialog = new
                // MessageDialog().setTitle(CustomMessageDialog.TITLE_MSG_CONFIRM,
                // VaadinIcon.QUESTION.create()).setMessage(msg);
                // messageDialog.addButton().text(CustomMessageDialog.LABEL_ANNULLA).primary().onClick(ev
                // -> Notification.show(CustomMessageDialog.LABEL_ANNULLA)).closeOnClick();
                // messageDialog.addButton().text(CustomMessageDialog.LABEL_SALVA).primary().onClick(ev
                // -> {
                // try {
                //
                // int totCambi = 0;
                // if (!currentGiornata.equals("1")) {
                // totCambi = insertCambi();
                // }
                //
                // ordinaMercato();
                // insertFormazione();
                //
                // FcMercatoDettInfo mercatoDettInfo = new FcMercatoDettInfo();
                // mercatoDettInfo.setFcAttore(attore);
                // mercatoDettInfo.setFcGiornataInfo(giornataInfo);
                // if (currentGiornata.equals("1")) {
                // mercatoDettInfo.setTotCambi(0);
                // } else {
                // mercatoDettInfo.setTotCambi(totCambi);
                // }
                // mercatoDettInfo.setFlagInvio("S");
                // mercatoDettInfo.setDataInvio(new Date());
                // mercatoInfoController.insertMercatoDettInfo(mercatoDettInfo);
                //
                // LOG.info("insert MercatoDettInfo OK");
                //
                // int cambiEff = getCambiEffettuati();
                // TOT_CAMBI_EFFETTUATI = MAX_CAMBI - cambiEff;
                // CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI;
                // txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);
                //
                // String info = "Operazione effettuata con succcesso.";
                // info += "Se hai attiva la notifica email sul profilo, a breve riceverai una
                // email di conferma.";
                //
                // this.saveSendMail.setEnabled(false);
                // // RELOAD ???
                // // loadFcFormazione(attore);
                //
                // try {
                // sendNewMail();
                // LOG.info("send_mail OK");
                // } catch (Exception excpt) {
                // LOG.error(excpt.getMessage());
                // CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO,
                // excpt.getMessage());
                // return;
                // }
                //
                // CustomMessageDialog.showMessageInfo(info);
                // Notification.show(CustomMessageDialog.LABEL_SALVA);
                //
                // } catch (Exception excpt) {
                // CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC,
                // excpt.getMessage());
                // }
                //
                // }).closeOnClick();
                // messageDialog.open();

                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader(CustomMessageDialog.TITLE_MSG_CONFIRM);
                dialog.setText(msg);

                dialog.setCancelable(true);
                dialog.setCancelText("Annulla");
                // dialog.addCancelListener(event -> System.out.println("Canceled"));

                dialog.setRejectable(false);
                // dialog.setRejectText("Discard");
                // dialog.addRejectListener(event -> System.out.println("Discarded"));

                dialog.setConfirmText("Conferma");
                dialog.addConfirmListener(e -> {

                    try {

                        int totCambi = 0;
                        if (!currentGiornata.equals("1")) {
                            totCambi = insertCambi();
                        }

                        ordinaMercato();
                        insertFormazione();

                        FcMercatoDettInfo mercatoDettInfo = new FcMercatoDettInfo();
                        mercatoDettInfo.setFcAttore(attore);
                        mercatoDettInfo.setFcGiornataInfo(giornataInfo);
                        if (currentGiornata.equals("1")) {
                            mercatoDettInfo.setTotCambi(0);
                        } else {
                            mercatoDettInfo.setTotCambi(totCambi);
                        }
                        mercatoDettInfo.setFlagInvio("S");
                        mercatoDettInfo.setDataInvio(new Date());
                        mercatoInfoController.insertMercatoDettInfo(mercatoDettInfo);

                        LOG.info("insert MercatoDettInfo OK");

                        int cambiEff = getCambiEffettuati();
                        TOT_CAMBI_EFFETTUATI = MAX_CAMBI - cambiEff;
                        CHECK_TOT_CAMBI_EFFETTUATI = TOT_CAMBI_EFFETTUATI;
                        txtCambi.setText("" + CHECK_TOT_CAMBI_EFFETTUATI);

                        String info = "Operazione effettuata con succcesso.";
                        info += "Se hai attiva la notifica email sul profilo, a breve riceverai una email di conferma.";

                        this.saveSendMail.setEnabled(false);
                        // RELOAD ???
                        // loadFcFormazione(attore);

                        try {
                            sendNewMail();
                            LOG.info("send_mail OK");
                        } catch (Exception excpt) {
                            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO,
                                    excpt.getMessage());
                            return;
                        }

                        CustomMessageDialog.showMessageInfo(info);
                        Notification.show(CustomMessageDialog.LABEL_SALVA);

                    } catch (Exception excpt) {
                        CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC,
                                excpt.getMessage());
                    }
                });

                dialog.open();

            }
        }
    }

    private int getCambiEffettuati() {

        List<FcMercatoDettInfo> modelCambiInfo = mercatoInfoController.findByFcAttoreOrderByFcGiornataInfoAsc(attore);
        int tot = 0;
        for (FcMercatoDettInfo mi : modelCambiInfo) {
            tot = tot + mi.getTotCambi().intValue();
        }
        return tot;
    }

    private boolean check() {

        if (modelPlayer1.size() == 0 || modelPlayer2.size() == 0 || modelPlayer3.size() == 0 || modelPlayer4.size() == 0
                || modelPlayer5.size() == 0 || modelPlayer6.size() == 0 || modelPlayer7.size() == 0
                || modelPlayer8.size() == 0 || modelPlayer9.size() == 0 || modelPlayer10.size() == 0
                || modelPlayer11.size() == 0 || modelPlayer12.size() == 0 || modelPlayer13.size() == 0
                || modelPlayer14.size() == 0 || modelPlayer15.size() == 0 || modelPlayer16.size() == 0
                || modelPlayer17.size() == 0 || modelPlayer18.size() == 0 || modelPlayer19.size() == 0
                || modelPlayer20.size() == 0 || modelPlayer21.size() == 0 || modelPlayer22.size() == 0
                || modelPlayer23.size() == 0) {
            CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ERROR_INSERT_GIOCATORI);
            return false;
        }

        int tot = 0;
        int countP = 0;
        int countD = 0;
        int countC = 0;
        int countA = 0;

        if (modelPlayer1.size() != 0) {
            FcGiocatore bean = modelPlayer1.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer2.size() != 0) {
            FcGiocatore bean = modelPlayer2.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer3.size() != 0) {
            FcGiocatore bean = modelPlayer3.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer4.size() != 0) {
            FcGiocatore bean = modelPlayer4.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer5.size() != 0) {
            FcGiocatore bean = modelPlayer5.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer6.size() != 0) {
            FcGiocatore bean = modelPlayer6.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer7.size() != 0) {
            FcGiocatore bean = modelPlayer7.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer8.size() != 0) {
            FcGiocatore bean = modelPlayer8.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer9.size() != 0) {
            FcGiocatore bean = modelPlayer9.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer10.size() != 0) {
            FcGiocatore bean = modelPlayer10.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer11.size() != 0) {
            FcGiocatore bean = modelPlayer11.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer12.size() != 0) {
            FcGiocatore bean = modelPlayer12.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer13.size() != 0) {
            FcGiocatore bean = modelPlayer13.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer14.size() != 0) {
            FcGiocatore bean = modelPlayer14.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer15.size() != 0) {
            FcGiocatore bean = modelPlayer15.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer16.size() != 0) {
            FcGiocatore bean = modelPlayer16.get(0);
            tot += bean.getQuotazione();

            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer17.size() != 0) {
            FcGiocatore bean = modelPlayer17.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer18.size() != 0) {
            FcGiocatore bean = modelPlayer18.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer19.size() != 0) {
            FcGiocatore bean = modelPlayer19.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer20.size() != 0) {
            FcGiocatore bean = modelPlayer20.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer21.size() != 0) {
            FcGiocatore bean = modelPlayer21.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer22.size() != 0) {
            FcGiocatore bean = modelPlayer22.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }
        if (modelPlayer23.size() != 0) {
            FcGiocatore bean = modelPlayer23.get(0);
            tot += bean.getQuotazione();
            if (bean.getFcRuolo().getIdRuolo().equals("P")) {
                countP++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
                countD++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
                countC++;
            } else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
                countA++;
            }
        }

        if (tot > Integer.parseInt(CREDITI_MERCATO)) {
            String msgErr = "Attenzione, hai superato " + CREDITI_MERCATO + " milioni di FM";
            CustomMessageDialog.showMessageError(msgErr);
            return false;
        }

        if (countP < 2) {
            String msgErr = "Attenzione, devi scegliere obbligatoriamente 2 portieri";
            CustomMessageDialog.showMessageError(msgErr);
            return false;
        }

        if (countD < 5) {
            String msgErr = "Attenzione, devi scegliere obbligatoriamente 5 difensori";
            CustomMessageDialog.showMessageError(msgErr);
            return false;
        }

        if (countC < 5) {
            String msgErr = "Attenzione, devi scegliere obbligatoriamente 5 centrocampisti";
            CustomMessageDialog.showMessageError(msgErr);
            return false;
        }

        if (countA < 4) {
            String msgErr = "Attenzione, devi scegliere obbligatoriamente 4 attaccanti";
            CustomMessageDialog.showMessageError(msgErr);
            return false;
        }

        int numRec = modelContaPlayer.size();
        for (int i = 0; i < numRec; i++) {
            FcProperties bean = modelContaPlayer.get(i);
            if (Integer.parseInt(bean.getValue()) > MAX_CHANGE_SQUADRA) {
                String msgErr = "Attenzione, si possono avere al massimo " + MAX_CHANGE_SQUADRA
                        + " giocatori appartenenti ad una nazionale";
                CustomMessageDialog.showMessageError(msgErr);
                return false;
            }
        }

        return true;
    }

    private int insertCambi() throws Exception {

        ArrayList<FcGiocatore> listAcquisti = new ArrayList<>();
        ArrayList<FcGiocatore> listCessioni = new ArrayList<>();

        for (int i = 0; i < modelFormazione.size(); i++) {
            FcGiocatore beanPlayer = modelFormazione.get(i);
            if (i == 0) {
                if (modelPlayer1.size() != 0) {
                    FcGiocatore bean = modelPlayer1.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 1) {
                if (modelPlayer2.size() != 0) {
                    FcGiocatore bean = modelPlayer2.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 2) {
                if (modelPlayer3.size() != 0) {
                    FcGiocatore bean = modelPlayer3.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 3) {
                if (modelPlayer4.size() != 0) {
                    FcGiocatore bean = modelPlayer4.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 4) {
                if (modelPlayer5.size() != 0) {
                    FcGiocatore bean = modelPlayer5.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 5) {
                if (modelPlayer6.size() != 0) {
                    FcGiocatore bean = modelPlayer6.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 6) {
                if (modelPlayer7.size() != 0) {
                    FcGiocatore bean = modelPlayer7.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 7) {
                if (modelPlayer8.size() != 0) {
                    FcGiocatore bean = modelPlayer8.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 8) {
                if (modelPlayer9.size() != 0) {
                    FcGiocatore bean = modelPlayer9.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 9) {
                if (modelPlayer10.size() != 0) {
                    FcGiocatore bean = modelPlayer10.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 10) {
                if (modelPlayer11.size() != 0) {
                    FcGiocatore bean = modelPlayer11.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 11) {
                if (modelPlayer12.size() != 0) {
                    FcGiocatore bean = modelPlayer12.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 12) {
                if (modelPlayer13.size() != 0) {
                    FcGiocatore bean = modelPlayer13.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 13) {
                if (modelPlayer14.size() != 0) {
                    FcGiocatore bean = modelPlayer14.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 14) {
                if (modelPlayer15.size() != 0) {
                    FcGiocatore bean = modelPlayer15.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 15) {
                if (modelPlayer16.size() != 0) {
                    FcGiocatore bean = modelPlayer16.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 16) {
                if (modelPlayer17.size() != 0) {
                    FcGiocatore bean = modelPlayer17.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 17) {
                if (modelPlayer18.size() != 0) {
                    FcGiocatore bean = modelPlayer18.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 18) {
                if (modelPlayer19.size() != 0) {
                    FcGiocatore bean = modelPlayer19.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 19) {
                if (modelPlayer20.size() != 0) {
                    FcGiocatore bean = modelPlayer20.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 20) {
                if (modelPlayer21.size() != 0) {
                    FcGiocatore bean = modelPlayer21.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 21) {
                if (modelPlayer22.size() != 0) {
                    FcGiocatore bean = modelPlayer22.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            } else if (i == 22) {
                if (modelPlayer23.size() != 0) {
                    FcGiocatore bean = modelPlayer23.get(0);
                    if (beanPlayer.getIdGiocatore() != bean.getIdGiocatore()) {
                        listAcquisti.add(bean);
                        listCessioni.add(beanPlayer);
                    }
                }
            }
        }

        for (FcGiocatore g : listCessioni) {
            FcMercatoDett mercato = new FcMercatoDett();
            mercato.setFcAttore(attore);
            mercato.setDataCambio(LocalDateTime.now());
            mercato.setFcGiocatoreByIdGiocVen(g);
            mercato.setFcGiornataInfo(giornataInfo);
            mercato.setNota("+" + g.getQuotazione());
            mercatoController.insertMercatoDett(mercato);

            LOG.info("insertMercatoDett CESSIONI ok");
        }

        int totCambi = 0;
        for (FcGiocatore g : listAcquisti) {
            totCambi++;
            FcMercatoDett mercato = new FcMercatoDett();
            mercato.setFcAttore(attore);
            mercato.setDataCambio(LocalDateTime.now().plusSeconds(1));
            mercato.setFcGiocatoreByIdGiocAcq(g);
            mercato.setFcGiornataInfo(giornataInfo);
            mercato.setNota("-" + g.getQuotazione());
            mercatoController.insertMercatoDett(mercato);

            LOG.info("insertMercatoDett ACQUISTI ok");
        }

        LOG.info("totCambi " + totCambi);
        return totCambi;

    }

    private void insertFormazione() throws Exception {

        LOG.info("START insertFormazione");
        String query = "";
        String del = "delete from fc_giornata_dett where id_attore=" + attore.getIdAttore() + " AND ID_GIORNATA="
                + currentGiornata;
        jdbcTemplate.update(del);

        String ID_GIOCATORE = "";
        String TOT_PAGATO = "";
        String ORDINAMENTO = "";
        int ord = 1;
        for (int i = 0; i < NUM_GIOCATORI; i++) {

            ORDINAMENTO = "" + ord;
            ord++;
            if (i == 0) {
                FcGiocatore bean = modelPlayer1.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 1) {
                FcGiocatore bean = modelPlayer2.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 2) {
                FcGiocatore bean = modelPlayer3.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 3) {
                FcGiocatore bean = modelPlayer4.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 4) {
                FcGiocatore bean = modelPlayer5.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 5) {
                FcGiocatore bean = modelPlayer6.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 6) {
                FcGiocatore bean = modelPlayer7.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 7) {
                FcGiocatore bean = modelPlayer8.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 8) {
                FcGiocatore bean = modelPlayer9.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 9) {
                FcGiocatore bean = modelPlayer10.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 10) {
                FcGiocatore bean = modelPlayer11.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 11) {
                FcGiocatore bean = modelPlayer12.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 12) {
                FcGiocatore bean = modelPlayer13.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 13) {
                FcGiocatore bean = modelPlayer14.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 14) {
                FcGiocatore bean = modelPlayer15.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 15) {
                FcGiocatore bean = modelPlayer16.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 16) {
                FcGiocatore bean = modelPlayer17.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 17) {
                FcGiocatore bean = modelPlayer18.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 18) {
                FcGiocatore bean = modelPlayer19.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 19) {
                FcGiocatore bean = modelPlayer20.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 20) {
                FcGiocatore bean = modelPlayer21.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 21) {
                FcGiocatore bean = modelPlayer22.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            } else if (i == 22) {
                FcGiocatore bean = modelPlayer23.get(0);
                ID_GIOCATORE = "" + bean.getIdGiocatore();
                TOT_PAGATO = "" + bean.getQuotazione();
            }

            query = " UPDATE FC_FORMAZIONE SET ID_GIOCATORE=" + ID_GIOCATORE + ", TOT_PAGATO=" + TOT_PAGATO
                    + " WHERE ID_ATTORE = " + attore.getIdAttore() + " AND ORDINAMENTO = " + ORDINAMENTO;
            jdbcTemplate.update(query.toLowerCase());

        }

        LOG.info("END insertFormazione");

    }

    private void sendNewMail() throws Exception {
        LOG.info("START sendNewMail");

        String subject = "Mercato-Cambi " + attore.getDescAttore() + " - " + currentDescGiornata;
        LOG.info("subject " + subject);
        String formazioneHtml = "";
        formazioneHtml += "<html><head><title>FC</title></head>\n";
        formazioneHtml += "<body>\n";
        formazioneHtml += "<p>" + currentDescGiornata + "</p>\n";
        formazioneHtml += "<br>\n";

        formazioneHtml += "<table>";

        String ORDINAMENTO = "";
        String RUOLO = "";
        String NOME_GIOCATORE = "";
        String SQUADRA = "";
        String Q = "";

        int ord = 1;
        Map<String, InputStream> listImg = new HashMap<>();
        for (int i = 0; i < NUM_GIOCATORI; i++) {

            ORDINAMENTO = "" + ord;
            FcGiocatore bean = null;

            if (i == 0) {
                bean = modelPlayer1.get(0);
            } else if (i == 1) {
                bean = modelPlayer2.get(0);
            } else if (i == 2) {
                bean = modelPlayer3.get(0);
            } else if (i == 3) {
                bean = modelPlayer4.get(0);
            } else if (i == 4) {
                bean = modelPlayer5.get(0);
            } else if (i == 5) {
                bean = modelPlayer6.get(0);
            } else if (i == 6) {
                bean = modelPlayer7.get(0);
            } else if (i == 7) {
                bean = modelPlayer8.get(0);
            } else if (i == 8) {
                bean = modelPlayer9.get(0);
            } else if (i == 9) {
                bean = modelPlayer10.get(0);
            } else if (i == 10) {
                bean = modelPlayer11.get(0);
            } else if (i == 11) {
                bean = modelPlayer12.get(0);
            } else if (i == 12) {
                bean = modelPlayer13.get(0);
            } else if (i == 13) {
                bean = modelPlayer14.get(0);
            } else if (i == 14) {
                bean = modelPlayer15.get(0);
            } else if (i == 15) {
                bean = modelPlayer16.get(0);
            } else if (i == 16) {
                bean = modelPlayer17.get(0);
            } else if (i == 17) {
                bean = modelPlayer18.get(0);
            } else if (i == 18) {
                bean = modelPlayer19.get(0);
            } else if (i == 19) {
                bean = modelPlayer20.get(0);
            } else if (i == 20) {
                bean = modelPlayer21.get(0);
            } else if (i == 21) {
                bean = modelPlayer22.get(0);
            } else if (i == 22) {
                bean = modelPlayer23.get(0);
            }

            RUOLO = bean.getFcRuolo().getDescRuolo();
            NOME_GIOCATORE = bean.getCognGiocatore();
            SQUADRA = bean.getFcSquadra().getNomeSquadra();
            Q = "" + bean.getQuotazione();

            /*
             * String color = "BGCOLOR=\"#FF9331\""; if (Integer.parseInt(ORDINAMENTO) >= 1
             * && Integer.parseInt(ORDINAMENTO) <= 11) { color = "BGCOLOR=\"#ABFF73\""; }
             * else if (Integer.parseInt(ORDINAMENTO) >= 12 && Integer.parseInt(ORDINAMENTO)
             * <= 23) { color = "BGCOLOR=\"#FFFF84\""; }
             */
            String ruolo = bean.getFcRuolo().getIdRuolo();
            String color = "BGCOLOR=\"#FF9331\"";
            if ("P".equals(ruolo)) {
                color = "BGCOLOR=" + Costants.COLOR_P;
            } else if ("D".equals(ruolo)) {
                color = "BGCOLOR=" + Costants.COLOR_D;
            } else if ("C".equals(ruolo)) {
                color = "BGCOLOR=" + Costants.COLOR_C;
            } else if ("A".equals(ruolo)) {
                color = "BGCOLOR=" + Costants.COLOR_A;
            }

            String cidNomeSq = ContentIdGenerator.getContentId();
            FcSquadra sq = bean.getFcSquadra();
            if (sq.getImg() != null) {
                try {
                    listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            formazioneHtml += "<tr " + color + ">";

            formazioneHtml += "<td>";
            formazioneHtml += ORDINAMENTO;
            formazioneHtml += "</td>";

            formazioneHtml += "<td>";
            formazioneHtml += RUOLO;
            formazioneHtml += "</td>";

            formazioneHtml += "<td>";
            formazioneHtml += NOME_GIOCATORE;
            formazioneHtml += "</td>";

            formazioneHtml += "<td><img src=\"cid:" + cidNomeSq + "\" />";
            formazioneHtml += SQUADRA;
            formazioneHtml += "</td>";

            formazioneHtml += "<td>";
            formazioneHtml += Q;
            formazioneHtml += "</td>";

            formazioneHtml += "</tr>";

            ord++;
        }

        formazioneHtml += "</table>\n";

        if (!currentGiornata.equals("1")) {

            List<FcMercatoDett> modelCambi = mercatoController
                    .findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(attore);

            if (modelCambi.size() > 0) {
                formazioneHtml += "<BR>\n";
                formazioneHtml += "<BR>\n";
                formazioneHtml += "<table>\n";

                String color = "BGCOLOR=\"#FF9331\"";

                formazioneHtml += "<tr " + color + ">";
                formazioneHtml += "<td>";
                formazioneHtml += "GIORNATA";
                formazioneHtml += "</td>";
                formazioneHtml += "<td>";
                formazioneHtml += "DATA_CAMBIO";
                formazioneHtml += "</td>";
                formazioneHtml += "<td>";
                formazioneHtml += "ACQUISTI";
                formazioneHtml += "</td>";
                formazioneHtml += "<td>";
                formazioneHtml += "";
                formazioneHtml += "</td>";
                formazioneHtml += "<td>";
                formazioneHtml += "CESSIONI";
                formazioneHtml += "</td>";
                formazioneHtml += "<td>";
                formazioneHtml += "";
                formazioneHtml += "</td>";
                formazioneHtml += "</tr>";

                // ACQUISTI - CESSIONI
                for (FcMercatoDett m : modelCambi) {

                    String GIOC_ACQ = "";
                    String SQ_ACQ = "";
                    String cidNomeSqAcq = ContentIdGenerator.getContentId();
                    String SQ_VEN = "";
                    String GIOC_VEN = "";
                    String cidNomeSqVen = ContentIdGenerator.getContentId();

                    String ID_GIORNATA = "" + m.getFcGiornataInfo().getIdGiornataFc();

                    if (m.getFcGiocatoreByIdGiocAcq() != null) {
                        GIOC_ACQ = m.getFcGiocatoreByIdGiocAcq().getCognGiocatore();
                        FcSquadra sqAcq = m.getFcGiocatoreByIdGiocAcq().getFcSquadra();
                        SQ_ACQ = sqAcq.getNomeSquadra();
                        if (sqAcq.getImg() != null) {
                            try {
                                listImg.put(cidNomeSqAcq, sqAcq.getImg().getBinaryStream());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (m.getFcGiocatoreByIdGiocVen() != null) {
                        GIOC_VEN = m.getFcGiocatoreByIdGiocVen().getCognGiocatore();
                        FcSquadra sqVen = m.getFcGiocatoreByIdGiocVen().getFcSquadra();
                        SQ_VEN = sqVen.getNomeSquadra();
                        if (sqVen.getImg() != null) {
                            try {
                                listImg.put(cidNomeSqVen, sqVen.getImg().getBinaryStream());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    String DATA_CAMBIO = Utils.formatLocalDateTime(m.getDataCambio(), "dd/MM/yyyy HH:mm");

                    formazioneHtml += "<tr " + color + ">";

                    formazioneHtml += "<td>";
                    formazioneHtml += ID_GIORNATA;
                    formazioneHtml += "</td>";

                    formazioneHtml += "<td>";
                    formazioneHtml += DATA_CAMBIO;
                    formazioneHtml += "</td>";

                    formazioneHtml += "<td>";
                    formazioneHtml += GIOC_ACQ;
                    formazioneHtml += "</td>";

                    formazioneHtml += "<td><img src=\"cid:" + cidNomeSqAcq + "\" />";
                    formazioneHtml += SQ_ACQ;
                    formazioneHtml += "</td>";

                    formazioneHtml += "<td>";
                    formazioneHtml += GIOC_VEN;
                    formazioneHtml += "</td>";

                    formazioneHtml += "<td><img src=\"cid:" + cidNomeSqVen + "\" />";
                    formazioneHtml += SQ_VEN;
                    formazioneHtml += "</td>";

                    formazioneHtml += "</tr>";

                }
                formazioneHtml += "<table>\n";
            }
        }

        formazioneHtml += "<BR>\n";
        formazioneHtml += "<BR>\n";
        formazioneHtml += "<p>Ciao " + attore.getDescAttore() + "</p>\n";
        formazioneHtml += "</BODY>\n";
        formazioneHtml += "<HTML>";

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

        try {
            String from = env.getProperty("spring.mail.secondary.username");
            emailService.sendMail2(false, from, to, cc, bcc, subject, formazioneHtml, "text/html", "3", listImg);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            try {
                String from = env.getProperty("spring.mail.primary.username");
                emailService.sendMail2(true, from, to, cc, bcc, subject, formazioneHtml, "text/html", "3", listImg);
            } catch (Exception e2) {
                LOG.error(e2.getMessage());
                throw e2;
            }
        }

        LOG.info("END sendNewMail");

    }

    private Grid<FcGiocatore> getTableGiocatore(List<FcGiocatore> items) {

        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.getStyle().set("--_lumo-grid-border-width", "0px");
        // grid.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        grid.setWidth(width);
        grid.setHeight(height);

        Column<FcGiocatore> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            VerticalLayout cellLayout = new VerticalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setSizeUndefined();
            cellLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            if (g != null) {
                String title = getInfoPlayer(g);

                String ruolo = g.getFcRuolo().getIdRuolo();
                if ("P".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_P);
                } else if ("D".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_D);
                } else if ("C".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_C);
                } else if ("A".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_A);
                }

                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }

                Image imgR = Utils.buildImage(ruolo.toLowerCase() + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo.toLowerCase() + ".png"));
                imgR.setTitle(title);
                cellLayout.add(imgR);
                cellLayout.setAlignSelf(Alignment.CENTER, imgR);

                // Label lblGiocatore = new Label(g.getCognGiocatore());
                Span lblGiocatore = new Span();
                lblGiocatore.setText(g.getCognGiocatore());
                lblGiocatore.getStyle().set(Costants.FONT_SIZE, "11px");
                lblGiocatore.setTitle(title);
                cellLayout.add(lblGiocatore);
                cellLayout.setAlignSelf(Alignment.STRETCH, lblGiocatore);

                if (g.getFcSquadra() != null) {
                    FcSquadra sq = g.getFcSquadra();
                    if (sq.getImg40() != null) {
                        try {
                            Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg40().getBinaryStream());
                            img.setTitle(title);
                            cellLayout.add(img);
                            cellLayout.setAlignSelf(Alignment.START, img);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    // Label lblInfoNomeSquadra = new
                    // Label(sq.getNomeSquadra());
                    Span lblInfoNomeSquadra = new Span();
                    lblInfoNomeSquadra.setText(sq.getNomeSquadra());
                    lblInfoNomeSquadra.getStyle().set(Costants.FONT_SIZE, "11px");
                    lblInfoNomeSquadra.setTitle(title);
                    cellLayout.add(lblInfoNomeSquadra);
                    cellLayout.setAlignSelf(Alignment.STRETCH, lblInfoNomeSquadra);
                }

                // Label lblInfoQuotazione = new Label("" + g.getQuotazione());
                Span lblInfoQuotazione = new Span();
                lblInfoQuotazione.setText("" + g.getQuotazione());

                lblInfoQuotazione.getStyle().set(Costants.FONT_SIZE, "14px");
                lblInfoQuotazione.setTitle(title);
                cellLayout.add(lblInfoQuotazione);
                cellLayout.setAlignSelf(Alignment.CENTER, lblInfoQuotazione);

                Element element = cellLayout.getElement(); // DOM element
                element.addEventListener("click", e -> {
                    FcGiocatore bean = g;
                    if (CHECK_TOT_CAMBI_EFFETTUATI > 0) {
                        LOG.info("click " + bean.getCognGiocatore());

                        if (activeFilter) {
                            String idRuolo = bean.getFcRuolo().getIdRuolo();
                            if ("P".equals(idRuolo)) {
                                modelPlayerP.add(bean);
                            } else if ("D".equals(idRuolo)) {
                                modelPlayerD.add(bean);
                            } else if ("C".equals(idRuolo)) {
                                modelPlayerC.add(bean);
                            } else if ("A".equals(idRuolo)) {
                                modelPlayerA.add(bean);
                            }
                            refreshAndSortGridTabsRuoli(idRuolo);
                        } else {
                            modelPlayerG.add(bean);
                            refreshAndSortGridGiocatori();
                        }

                        if (grid == tablePlayer1) {
                            modelPlayer1.remove(bean);
                            tablePlayer1.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer2) {
                            modelPlayer2.remove(bean);
                            tablePlayer2.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer3) {
                            modelPlayer3.remove(bean);
                            tablePlayer3.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer4) {
                            modelPlayer4.remove(bean);
                            tablePlayer4.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer5) {
                            modelPlayer5.remove(bean);
                            tablePlayer5.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer6) {
                            modelPlayer6.remove(bean);
                            tablePlayer6.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer7) {
                            modelPlayer7.remove(bean);
                            tablePlayer7.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer8) {
                            modelPlayer8.remove(bean);
                            tablePlayer8.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer9) {
                            modelPlayer9.remove(bean);
                            tablePlayer9.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer10) {
                            modelPlayer10.remove(bean);
                            tablePlayer10.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer11) {
                            modelPlayer11.remove(bean);
                            tablePlayer11.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer12) {
                            modelPlayer12.remove(bean);
                            tablePlayer12.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer13) {
                            modelPlayer13.remove(bean);
                            tablePlayer13.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer14) {
                            modelPlayer14.remove(bean);
                            tablePlayer14.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer15) {
                            modelPlayer15.remove(bean);
                            tablePlayer15.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer16) {
                            modelPlayer16.remove(bean);
                            tablePlayer16.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer17) {
                            modelPlayer17.remove(bean);
                            tablePlayer17.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer18) {
                            modelPlayer18.remove(bean);
                            tablePlayer18.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer19) {
                            modelPlayer19.remove(bean);
                            tablePlayer19.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer20) {
                            modelPlayer20.remove(bean);
                            tablePlayer20.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer21) {
                            modelPlayer21.remove(bean);
                            tablePlayer21.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer22) {
                            modelPlayer22.remove(bean);
                            tablePlayer22.getDataProvider().refreshAll();
                        } else if (grid == tablePlayer23) {
                            modelPlayer23.remove(bean);
                            tablePlayer23.getDataProvider().refreshAll();
                        }
                        updateTot();

                    } else {
                        String msgErr = "Attenzione, cambi esauriti";
                        CustomMessageDialog.showMessageError(msgErr);
                    }
                });
            }
            return cellLayout;
        }));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setResizable(false);
        return grid;
    }

    private void refreshAndSortGridGiocatori() {
        LOG.info("refreshAndSortGridGiocatori1");
        modelPlayerG.sort((p1, p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
        tableGiocatori.getDataProvider().refreshAll();
    }

    private void refreshAndSortGridTabsRuoli(String idRuolo) {

        if (StringUtils.isEmpty(idRuolo)) {
            LOG.info("refreshAndSortGridTabsRuoli ruolo=" + idRuolo);
            modelPlayerP.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerP.getDataProvider().refreshAll();

            modelPlayerD.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerD.getDataProvider().refreshAll();

            modelPlayerC.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerC.getDataProvider().refreshAll();

            modelPlayerA.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerA.getDataProvider().refreshAll();

        } else {
            LOG.info("refreshAndSortGridTabsRuoli ruolo=" + idRuolo);
            if ("P".equals(idRuolo)) {
                modelPlayerP.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                tablePlayerP.getDataProvider().refreshAll();
            } else if ("D".equals(idRuolo)) {
                modelPlayerD.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                tablePlayerD.getDataProvider().refreshAll();
            } else if ("C".equals(idRuolo)) {
                modelPlayerC.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                tablePlayerC.getDataProvider().refreshAll();
            } else if ("A".equals(idRuolo)) {
                modelPlayerA.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                tablePlayerA.getDataProvider().refreshAll();
            }
        }
    }

    private void setModelGiocatori(FcAttore att) {
        LOG.info("START setModelGiocatori ");
        List<FcGiocatore> listGiocatore = null;
        if (att == null) {
            listGiocatore = giocatoreController.findAll();
        } else {
            LOG.info("attore " + att.getDescAttore());
            List<FcFormazione> listFormazione = formazioneController
                    .findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(att);
            Collection<Integer> notIn = new ArrayList<>();
            for (FcFormazione f : listFormazione) {
                if (f.getFcGiocatore() != null) {
                    notIn.add(f.getFcGiocatore().getIdGiocatore());
                }
            }
            if (notIn.size() == 0) {
                notIn.add(Integer.valueOf(-1));
            }
            listGiocatore = giocatoreController.findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(notIn);

        }
        LOG.info("listGiocatore.sze " + listGiocatore.size());

        if (activeFilter) {
            if (!modelPlayerP.isEmpty()) {
                modelPlayerP.clear();
            }
            if (!modelPlayerD.isEmpty()) {
                modelPlayerD.clear();
            }
            if (!modelPlayerC.isEmpty()) {
                modelPlayerC.clear();
            }
            if (!modelPlayerA.isEmpty()) {
                modelPlayerA.clear();
            }
        } else {
            if (!modelPlayerG.isEmpty()) {
                modelPlayerG.clear();
            }
        }

        for (FcGiocatore g : listGiocatore) {
            if (activeFilter) {
                String r = g.getFcRuolo().getIdRuolo().toUpperCase();
                if ("P".equals(r)) {
                    modelPlayerP.add(g);
                } else if ("D".equals(r)) {
                    modelPlayerD.add(g);
                } else if ("C".equals(r)) {
                    modelPlayerC.add(g);
                } else if ("A".equals(r)) {
                    modelPlayerA.add(g);
                }
            } else {
                modelPlayerG.add(g);
            }
        }

        LOG.info("modelPlayerG " + modelPlayerG.size());
        LOG.info("modelPlayerP " + modelPlayerP.size());
        LOG.info("modelPlayerD " + modelPlayerD.size());
        LOG.info("modelPlayerC " + modelPlayerC.size());
        LOG.info("modelPlayerA " + modelPlayerA.size());

        LOG.info("END setModelGiocatori");
    }

    private Grid<FcGiocatore> getTablePlayer(List<FcGiocatore> items) {

        Grid<FcGiocatore> grid = new Grid<>();

        ListDataProvider<FcGiocatore> dataProvider = new ListDataProvider<>(items);
        // grid.setDataProvider(dataProvider);
        grid.setItems(dataProvider);

        // comboRuolo.addValueChangeListener(event -> {
        // applyFilter(dataProvider);
        // });
        comboNazione.addValueChangeListener(event -> {
            applyFilter(dataProvider);
        });
        txtQuotaz.addValueChangeListener(event -> {
            applyFilter(dataProvider);
        });

        // grid.setHeightByRows(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setWidth("450px");
        grid.setHeight("600px");

        Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            // cellLayout.setSizeFull();
            if (g != null) {
                String title = getInfoPlayer(g);
                if (g.getFcRuolo() != null) {
                    Image img = Utils.buildImage(g.getFcRuolo().getIdRuolo().toLowerCase() + ".png",
                            resourceLoader.getResource(
                                    Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
                    img.setTitle(title);
                    cellLayout.add(img);
                }
            }
            return cellLayout;
        }));
        ruoloColumn.setSortable(false);
        ruoloColumn.setHeader("R");
        ruoloColumn.setWidth("50px");
        // ruoloColumn.setComparator((p1,p2) ->
        // p1.getFcRuolo().getIdRuolo().compareTo(p2.getFcRuolo().getIdRuolo()));
        // ruoloColumn.setAutoWidth(true);

        // Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(g -> g !=
        // null ? g.getCognGiocatore() : "");
        Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (g != null) {
                String title = getInfoPlayer(g);
                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }
                if (g.getCognGiocatore() != null) {
                    // Label lblGiocatore = new Label(g.getCognGiocatore());
                    Span lblGiocatore = new Span();
                    lblGiocatore.setText(g.getCognGiocatore());
                    lblGiocatore.setTitle(title);
                    cellLayout.add(lblGiocatore);
                }
            }
            return cellLayout;
        }));
        cognGiocatoreColumn.setSortable(true);
        cognGiocatoreColumn.setComparator((p1, p2) -> p1.getCognGiocatore().compareTo(p2.getCognGiocatore()));
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setWidth("150px");
        // cognGiocatoreColumn.setAutoWidth(true);

        Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (g != null) {
                String title = getInfoPlayer(g);
                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }
                if (g.getFcSquadra() != null) {
                    FcSquadra sq = g.getFcSquadra();
                    if (sq.getImg() != null) {
                        try {
                            Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                            img.setTitle(title);
                            cellLayout.add(img);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    // Label lblSquadra = new Label(sq.getNomeSquadra());
                    Span lblSquadra = new Span();
                    lblSquadra.setText(sq.getNomeSquadra());
                    lblSquadra.setTitle(title);
                    cellLayout.add(lblSquadra);
                }
            }
            return cellLayout;
        }));
        nomeSquadraColumn.setSortable(true);
        nomeSquadraColumn.setComparator(
                (p1, p2) -> p1.getFcSquadra().getNomeSquadra().compareTo(p2.getFcSquadra().getNomeSquadra()));
        nomeSquadraColumn.setHeader("Naz");
        nomeSquadraColumn.setWidth("150px");
        // nomeSquadraColumn.setAutoWidth(true);

        // Column<FcGiocatore> quotazioneColumn = grid.addColumn(g -> g != null
        // ? g.getQuotazione() : 0);
        Column<FcGiocatore> quotazioneColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (g != null) {
                String title = getInfoPlayer(g);
                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }
                String q = "" + g.getQuotazione();
                // Label lblQuotazione = new Label(q);
                Span lblQuotazione = new Span();
                lblQuotazione.setText(q);
                lblQuotazione.setTitle(title);
                cellLayout.add(lblQuotazione);
            }
            return cellLayout;
        }));

        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader("Q");
        quotazioneColumn.setWidth("50px");
        // quotazioneColumn.setAutoWidth(true);

        grid.addItemClickListener(event -> {
            FcGiocatore bean = event.getItem();
            LOG.info("click " + bean.getCognGiocatore());
            if (bean != null) {
                if (existGiocatore(bean)) {
                    LOG.info("existGiocatore true");
                    return;
                }
                boolean bDel = false;
                if (modelPlayer1.size() == 0) {
                    modelPlayer1.add(bean);
                    tablePlayer1.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer2.size() == 0) {
                    modelPlayer2.add(bean);
                    tablePlayer2.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer3.size() == 0) {
                    modelPlayer3.add(bean);
                    tablePlayer3.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer4.size() == 0) {
                    modelPlayer4.add(bean);
                    tablePlayer4.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer5.size() == 0) {
                    modelPlayer5.add(bean);
                    tablePlayer5.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer6.size() == 0) {
                    modelPlayer6.add(bean);
                    tablePlayer6.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer7.size() == 0) {
                    modelPlayer7.add(bean);
                    tablePlayer7.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer8.size() == 0) {
                    modelPlayer8.add(bean);
                    tablePlayer8.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer9.size() == 0) {
                    modelPlayer9.add(bean);
                    tablePlayer9.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer10.size() == 0) {
                    modelPlayer10.add(bean);
                    tablePlayer10.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer11.size() == 0) {
                    modelPlayer11.add(bean);
                    tablePlayer11.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer12.size() == 0) {
                    modelPlayer12.add(bean);
                    tablePlayer12.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer13.size() == 0) {
                    modelPlayer13.add(bean);
                    tablePlayer13.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer14.size() == 0) {
                    modelPlayer14.add(bean);
                    tablePlayer14.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer15.size() == 0) {
                    modelPlayer15.add(bean);
                    tablePlayer15.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer16.size() == 0) {
                    modelPlayer16.add(bean);
                    tablePlayer16.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer17.size() == 0) {
                    modelPlayer17.add(bean);
                    tablePlayer17.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer18.size() == 0) {
                    modelPlayer18.add(bean);
                    tablePlayer18.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer19.size() == 0) {
                    modelPlayer19.add(bean);
                    tablePlayer19.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer20.size() == 0) {
                    modelPlayer20.add(bean);
                    tablePlayer20.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer21.size() == 0) {
                    modelPlayer21.add(bean);
                    tablePlayer21.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer22.size() == 0) {
                    modelPlayer22.add(bean);
                    tablePlayer22.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer23.size() == 0) {
                    modelPlayer23.add(bean);
                    tablePlayer23.getDataProvider().refreshAll();
                    bDel = true;
                }

                if (bDel) {
                    LOG.info("REMOVE ITEM ");
                    if (activeFilter) {
                        String idRuolo = bean.getFcRuolo().getIdRuolo().toUpperCase();
                        if ("P".equals(idRuolo)) {
                            modelPlayerP.remove(bean);
                        } else if ("D".equals(idRuolo)) {
                            modelPlayerD.remove(bean);
                        } else if ("C".equals(idRuolo)) {
                            modelPlayerC.remove(bean);
                        } else if ("A".equals(idRuolo)) {
                            modelPlayerA.remove(bean);
                        }
                        refreshAndSortGridTabsRuoli(idRuolo);
                    } else {
                        modelPlayerG.remove(bean);
                        refreshAndSortGridGiocatori();
                    }

                    updateTot();
                    if (!currentGiornata.equals("1")) {
                        updateLabelCambi();
                    }
                }
            }
        });

        return grid;
    }

    private void applyFilter(ListDataProvider<FcGiocatore> dataProvider) {

        dataProvider.clearFilters();
        // if (comboRuolo.getValue() != null) {
        // dataProvider.addFilter(g ->
        // comboRuolo.getValue().getIdRuolo().equals(g.getFcRuolo().getIdRuolo()));
        // }
        if (comboNazione.getValue() != null) {
            dataProvider.addFilter(s -> comboNazione.getValue().getIdSquadra() == s.getFcSquadra().getIdSquadra());
        }
        if (txtQuotaz.getValue() != null) {
            dataProvider.addFilter(s -> s.getQuotazione().intValue() <= txtQuotaz.getValue().intValue());
        }

    }

    private String getInfoPlayer(FcGiocatore bean) {
        String info = "N.D.";
        if (bean != null && bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / Costants.DIVISORE_10);
            String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / Costants.DIVISORE_10);

            info = bean.getCognGiocatore() + "\n";
            info += "Nazione: " + bean.getFcSquadra().getNomeSquadra() + "\n";
            info += "Giocate: " + bean.getFcStatistiche().getGiocate() + "\n";
            info += "MV: " + mv + "\n";
            info += "FV: " + fv + "\n";
            info += "Goal: " + bean.getFcStatistiche().getGoalFatto() + "\n";
            info += "Assist: " + bean.getFcStatistiche().getAssist() + "\n";
            info += "Ammonizione: " + bean.getFcStatistiche().getAmmonizione() + "\n";
            info += "Espulsione: " + bean.getFcStatistiche().getEspulsione() + "\n";
            if ("P".equals(bean.getFcRuolo().getIdRuolo().toUpperCase())) {
                info += "Goal Subito: " + bean.getFcStatistiche().getGoalSubito() + "\n";
            }
        }

        return info;
    }

    private Grid<FcProperties> buildTableContaPlayer(List<FcProperties> items) {

        Grid<FcProperties> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth("240px");

        Column<FcProperties> keyColumn = grid.addColumn(new ComponentRenderer<>(f -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (f != null && f.getKey() != null) {
                // Image img = buildImage("classpath:/img/nazioni/", f.getKey()
                // + ".png");
                // cellLayout.add(img);
                FcSquadra sq = squadraController.findByNomeSquadra(f.getKey());
                if (sq.getImg40() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg40().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                Span lblSquadra = new Span(f.getKey());
                cellLayout.add(lblSquadra);
            }
            return cellLayout;
        }));
        keyColumn.setSortable(false);
        keyColumn.setAutoWidth(true);

        Column<FcProperties> valueColumn = grid.addColumn(p -> p.getValue());
        valueColumn.setSortable(false);
        valueColumn.setAutoWidth(true);

        return grid;
    }

    private boolean existGiocatore(FcGiocatore g) {

        if (modelPlayer1.size() != 0) {
            if (modelPlayer1.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer2.size() != 0) {
            if (modelPlayer2.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer3.size() != 0) {
            if (modelPlayer3.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer4.size() != 0) {
            if (modelPlayer4.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer5.size() != 0) {
            if (modelPlayer5.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer6.size() != 0) {
            if (modelPlayer6.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer7.size() != 0) {
            if (modelPlayer7.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer8.size() != 0) {
            if (modelPlayer8.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer9.size() != 0) {
            if (modelPlayer9.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer10.size() != 0) {
            if (modelPlayer10.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer11.size() != 0) {
            if (modelPlayer11.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer12.size() != 0) {
            if (modelPlayer12.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer13.size() != 0) {
            if (modelPlayer13.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer14.size() != 0) {
            if (modelPlayer14.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer15.size() != 0) {
            if (modelPlayer15.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer16.size() != 0) {
            if (modelPlayer16.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer17.size() != 0) {
            if (modelPlayer17.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer18.size() != 0) {
            if (modelPlayer18.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer19.size() != 0) {
            if (modelPlayer19.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer20.size() != 0) {
            if (modelPlayer20.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer21.size() != 0) {
            if (modelPlayer21.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer22.size() != 0) {
            if (modelPlayer22.indexOf(g) != -1) {
                return true;
            }
        }
        if (modelPlayer23.size() != 0) {
            if (modelPlayer23.indexOf(g) != -1) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void ordinaMercato() throws Exception {

        List<FcGiocatore> modelMercatoGiocatori = new ArrayList<>();
        if (modelPlayer1.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer1.get(0));
        }
        if (modelPlayer2.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer2.get(0));
        }
        if (modelPlayer3.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer3.get(0));
        }
        if (modelPlayer4.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer4.get(0));
        }
        if (modelPlayer5.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer5.get(0));
        }
        if (modelPlayer6.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer6.get(0));
        }
        if (modelPlayer7.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer7.get(0));
        }
        if (modelPlayer8.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer8.get(0));
        }
        if (modelPlayer9.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer9.get(0));
        }
        if (modelPlayer10.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer10.get(0));
        }
        if (modelPlayer11.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer11.get(0));
        }
        if (modelPlayer12.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer12.get(0));
        }
        if (modelPlayer13.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer13.get(0));
        }
        if (modelPlayer14.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer14.get(0));
        }
        if (modelPlayer15.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer15.get(0));
        }
        if (modelPlayer16.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer16.get(0));
        }
        if (modelPlayer17.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer17.get(0));
        }
        if (modelPlayer18.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer18.get(0));
        }
        if (modelPlayer19.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer19.get(0));
        }
        if (modelPlayer20.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer20.get(0));
        }
        if (modelPlayer21.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer21.get(0));
        }
        if (modelPlayer22.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer22.get(0));
        }
        if (modelPlayer23.size() != 0) {
            modelMercatoGiocatori.add(modelPlayer23.get(0));
        }
        removeMercatoGiocatore();
        ComparatorChain chain = new ComparatorChain(Arrays.asList(new BeanComparator("flagAttivo").reversed(),
                new BeanComparator("fcRuolo.idRuolo").reversed(), new BeanComparator("quotazione").reversed()));
        Collections.sort(modelMercatoGiocatori, chain);
        modelFormazione.clear();
        int ord = 1;
        for (int i = 0; i < modelMercatoGiocatori.size(); i++) {
            FcGiocatore g = modelMercatoGiocatori.get(i);
            // System.out.println(ord + " " + g.isFlagAttivo() + " " +
            // g.getFcRuolo().getIdRuolo() + " " + g.getCognGiocatore() + " " +
            // g.getQuotazione());
            modelFormazione.add(g);
            if (ord == 1) {
                modelPlayer1.clear();
                modelPlayer1.add(g);
                tablePlayer1.getDataProvider().refreshAll();
            } else if (ord == 2) {
                modelPlayer2.clear();
                modelPlayer2.add(g);
                tablePlayer2.getDataProvider().refreshAll();
            } else if (ord == 3) {
                modelPlayer3.clear();
                modelPlayer3.add(g);
                tablePlayer3.getDataProvider().refreshAll();
            } else if (ord == 4) {
                modelPlayer4.clear();
                modelPlayer4.add(g);
                tablePlayer4.getDataProvider().refreshAll();
            } else if (ord == 5) {
                modelPlayer5.clear();
                modelPlayer5.add(g);
                tablePlayer5.getDataProvider().refreshAll();
            } else if (ord == 6) {
                modelPlayer6.clear();
                modelPlayer6.add(g);
                tablePlayer6.getDataProvider().refreshAll();
            } else if (ord == 7) {
                modelPlayer7.clear();
                modelPlayer7.add(g);
                tablePlayer7.getDataProvider().refreshAll();
            } else if (ord == 8) {
                modelPlayer8.clear();
                modelPlayer8.add(g);
                tablePlayer8.getDataProvider().refreshAll();
            } else if (ord == 9) {
                modelPlayer9.clear();
                modelPlayer9.add(g);
                tablePlayer9.getDataProvider().refreshAll();
            } else if (ord == 10) {
                modelPlayer10.clear();
                modelPlayer10.add(g);
                tablePlayer10.getDataProvider().refreshAll();
            } else if (ord == 11) {
                modelPlayer11.clear();
                modelPlayer11.add(g);
                tablePlayer11.getDataProvider().refreshAll();
            } else if (ord == 12) {
                modelPlayer12.clear();
                modelPlayer12.add(g);
                tablePlayer12.getDataProvider().refreshAll();
            } else if (ord == 13) {
                modelPlayer13.clear();
                modelPlayer13.add(g);
                tablePlayer13.getDataProvider().refreshAll();
            } else if (ord == 14) {
                modelPlayer14.clear();
                modelPlayer14.add(g);
                tablePlayer14.getDataProvider().refreshAll();
            } else if (ord == 15) {
                modelPlayer15.clear();
                modelPlayer15.add(g);
                tablePlayer15.getDataProvider().refreshAll();
            } else if (ord == 16) {
                modelPlayer16.clear();
                modelPlayer16.add(g);
                tablePlayer16.getDataProvider().refreshAll();
            } else if (ord == 17) {
                modelPlayer17.clear();
                modelPlayer17.add(g);
                tablePlayer17.getDataProvider().refreshAll();
            } else if (ord == 18) {
                modelPlayer18.clear();
                modelPlayer18.add(g);
                tablePlayer18.getDataProvider().refreshAll();
            } else if (ord == 19) {
                modelPlayer19.clear();
                modelPlayer19.add(g);
                tablePlayer19.getDataProvider().refreshAll();
            } else if (ord == 20) {
                modelPlayer20.clear();
                modelPlayer20.add(g);
                tablePlayer20.getDataProvider().refreshAll();
            } else if (ord == 21) {
                modelPlayer21.clear();
                modelPlayer21.add(g);
                tablePlayer21.getDataProvider().refreshAll();
            } else if (ord == 22) {
                modelPlayer22.clear();
                modelPlayer22.add(g);
                tablePlayer22.getDataProvider().refreshAll();
            } else if (ord == 23) {
                modelPlayer23.clear();
                modelPlayer23.add(g);
                tablePlayer23.getDataProvider().refreshAll();
            }
            ord++;
        }
    }

}