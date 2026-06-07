package fcapp.ui.views.em;

import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcMercatoDett;
import fcapp.backend.data.entity.FcMercatoDettInfo;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.MercatoInfoService;
import fcapp.backend.service.MercatoService;
import fcapp.backend.service.RuoloService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.ContentIdGenerator;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mercato")
@Route(value = "mercatoEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmMercatoView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String WIDTH = "100px";
    private static final String HEIGHT = "120px";
    private static final int MAX_CAMBI = 12;
    private static final int NUM_GIOCATORI = 23;

    private final transient Logger log = LoggerFactory.getLogger(this.getClass());
    private final transient Environment env;
    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient EmailService emailService;
    private final transient GiocatoreService giocatoreService;
    private final transient AttoreService attoreService;
    private final transient RuoloService ruoloService;
    private final transient SquadraService squadraService;
    private final transient FormazioneService formazioneService;
    private final transient MercatoService mercatoService;
    private final transient MercatoInfoService mercatoInfoService;
    private final transient AccessoService accessoService;

    private String creditiMercato;
    private int totCambiEffettuati = 0;
    private int checkTotCambiEffettuati = 0;

    private String currentGiornata = "";
    private String currentDescGiornata = "";

    private FcAttore attore;
    private FcCampionato campionato;
    private FcGiornataInfo giornataInfo;

    private AbsoluteLayout absLayout;
    private Button saveSendMail;

    private ComboBox<FcSquadra> comboNazione;
    private NumberField txtQuotazione;

    private Span txtCrediti;
    private Span txtCambi;
    private Span lblInfoP;
    private Span lblInfoD;
    private Span lblInfoC;
    private Span lblInfoA;

    private Grid<FcProperties> tableContaPlayer;
    private List<FcProperties> modelContaPlayer = new ArrayList<>();

    private List<FcAttore> attori;
    private List<FcRuolo> ruoli;
    private List<FcSquadra> squadre;
    private String nextDate;
    private long millisDiff = 0;
    private Properties p;

    private final List<FcGiocatore> modelFormazione = new ArrayList<>();

    private final boolean activeFilter = true;

    private Grid<FcGiocatore> tableGiocatori;
    private final List<FcGiocatore> modelPlayerG = new ArrayList<>();

    private Grid<FcGiocatore> tablePlayerP;
    private Grid<FcGiocatore> tablePlayerD;
    private Grid<FcGiocatore> tablePlayerC;
    private Grid<FcGiocatore> tablePlayerA;
    private final List<FcGiocatore> modelPlayerP = new ArrayList<>();
    private final List<FcGiocatore> modelPlayerD = new ArrayList<>();
    private final List<FcGiocatore> modelPlayerC = new ArrayList<>();
    private final List<FcGiocatore> modelPlayerA = new ArrayList<>();

    private final List<List<FcGiocatore>> squadSlots = new ArrayList<>();
    private final List<Grid<FcGiocatore>> squadTables = new ArrayList<>();

    public EmMercatoView(Environment env, JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader,
                         EmailService emailService,
                         GiocatoreService giocatoreService, AttoreService attoreService,
                         RuoloService ruoloService, SquadraService squadraService,
                         FormazioneService formazioneService, MercatoService mercatoService,
                         MercatoInfoService mercatoInfoService,
                         AccessoService accessoService) {
        log.info("EmMercatoView()");
        this.env = env;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.emailService = emailService;
        this.giocatoreService = giocatoreService;
        this.attoreService = attoreService;
        this.ruoloService = ruoloService;
        this.squadraService = squadraService;
        this.formazioneService = formazioneService;
        this.mercatoService = mercatoService;
        this.mercatoInfoService = mercatoInfoService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() throws Exception {
        log.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }
        accessoService.insertAccesso(this.getClass().getName());
        initData();
        initSlots();
        initLayout();
    }

    private void initData() {
        p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
        attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
        campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
        giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
        nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
        millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

        currentGiornata = String.valueOf(giornataInfo.getCodiceGiornata());
        currentDescGiornata = giornataInfo.getDescGiornataFc();

        creditiMercato = (String) p.get("CREDITI_MERCATO");

        attori = attoreService.findByActive(true);
        ruoli = ruoloService.findAll();
        squadre = squadraService.findAll();
    }

    private void initSlots() {
        squadSlots.clear();
        squadTables.clear();
        for (int i = 0; i < NUM_GIOCATORI; i++) {
            squadSlots.add(new ArrayList<>());
        }
    }

    private List<FcGiocatore> getSlot(int index) {
        return squadSlots.get(index);
    }

    private Grid<FcGiocatore> getSlotTable(int index) {
        return squadTables.get(index);
    }

    private FcGiocatore getPlayerInSlot(int index) {
        List<FcGiocatore> slot = getSlot(index);
        return slot.isEmpty() ? null : slot.get(0);
    }

    private boolean isSlotEmpty(int index) {
        return getSlot(index).isEmpty();
    }

    private void setPlayerInSlot(int index, FcGiocatore player) {
        List<FcGiocatore> slot = getSlot(index);
        slot.clear();
        if (player != null) {
            slot.add(player);
        }
        if (index < squadTables.size()) {
            getSlotTable(index).getDataProvider().refreshAll();
        }
    }

    private void clearSlot(int index) {
        setPlayerInSlot(index, null);
    }

    private void clearAllSlots() {
        for (int i = 0; i < NUM_GIOCATORI; i++) {
            clearSlot(i);
        }
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

        ComboBox<FcAttore> comboAttore = new ComboBox<>();
        comboAttore.setItems(attori);
        comboAttore.setItemLabelGenerator(FcAttore::getDescAttore);
        comboAttore.setClearButtonVisible(true);
        comboAttore.setPlaceholder("Attore");
        comboAttore.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                removeAllElementsList();
                setModelGiocatori(null);
                if (activeFilter) {
                    refreshAndSortGridTabsRuoli("");
                } else {
                    refreshAndSortGridGiocatori();
                }
                totCambiEffettuati = MAX_CAMBI;
                checkTotCambiEffettuati = totCambiEffettuati;
                txtCambi.setText(String.valueOf(checkTotCambiEffettuati));
                txtCrediti.setText(creditiMercato);
                lblInfoP.setText("0");
                lblInfoD.setText("0");
                lblInfoC.setText("0");
                lblInfoA.setText("0");
            } else if (event.getOldValue() != null) {
                attore = event.getValue();
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
                    totCambiEffettuati = MAX_CAMBI - cambiEff;
                    checkTotCambiEffettuati = totCambiEffettuati;
                    txtCambi.setText(String.valueOf(checkTotCambiEffettuati));
                } catch (Exception e) {
                    log.error(e.getMessage());
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

        ComboBox<FcRuolo> comboRuolo = new ComboBox<>();
        comboRuolo.setItems(ruoli);
        comboRuolo.setItemLabelGenerator(FcRuolo::getIdRuolo);
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
        comboNazione.setItemLabelGenerator(FcSquadra::getNomeSquadra);
        comboNazione.setClearButtonVisible(true);
        comboNazione.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();
            if (item.getImg() != null) {
                try {
                    Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
                    container.add(img);
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }
            container.add(new Span(item.getNomeSquadra()));
            return container;
        }));

        txtQuotazione = new NumberField("Quotazione <=");
        txtQuotazione.setMin(0d);
        txtQuotazione.setMax(500d);
        txtQuotazione.setStepButtonsVisible(true);

        setModelGiocatori(attore);

        if (activeFilter) {
            tablePlayerP = getTablePlayer(modelPlayerP);
            tablePlayerD = getTablePlayer(modelPlayerD);
            tablePlayerC = getTablePlayer(modelPlayerC);
            tablePlayerA = getTablePlayer(modelPlayerA);
        } else {
            tableGiocatori = getTablePlayer(modelPlayerG);
        }

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            squadTables.add(getTableGiocatore(getSlot(i), i));
        }

        HorizontalLayout layoutFilterRow1 = new HorizontalLayout();
        layoutFilterRow1.setMargin(false);
        layoutFilterRow1.add(comboNazione, txtQuotazione);

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

        layoutAvviso.add(new HorizontalLayout(new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato))));
        layoutAvviso.add(new HorizontalLayout(new Span("Consegna entro: " + nextDate)));

        if (millisDiff != 0) {
            SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
            timer.setHours(true);
            timer.setMinutes(true);
            timer.setFractions(false);
            timer.start();
            timer.addTimerEndEvent(ev -> showMessageStopInsert());
            layoutAvviso.add(timer);
        }

        left = 500;
        absLayout.add(layoutAvviso, left, top);

        top = 45;
        left = 1050;
        absLayout.add(saveSendMail, left, top);

        left = 1250;
        absLayout.add(comboAttore, left, top);

        final HorizontalLayout layoutInfoGenerali = new HorizontalLayout();
        layoutInfoGenerali.setPadding(true);
        layoutInfoGenerali.setSpacing(true);
        layoutInfoGenerali.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutInfoGenerali.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_GRAY);
        layoutInfoGenerali.setAlignItems(FlexComponent.Alignment.END);

        int cambiEff = getCambiEffettuati();
        totCambiEffettuati = MAX_CAMBI - cambiEff;
        checkTotCambiEffettuati = totCambiEffettuati;
        if (checkTotCambiEffettuati <= 0) {
            this.saveSendMail.setEnabled(false);
        }

        Span lblInfo = new Span("Hai ancora a disposizione:");

        Span lblCrediti = new Span("Crediti:");
        lblCrediti.getElement().getStyle().set("color", Costants.RED);
        lblCrediti.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);

        txtCrediti = new Span(creditiMercato);
        txtCrediti.getElement().getStyle().set("color", Costants.RED);
        txtCrediti.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);

        Span lblCambi = new Span("Cambi:");
        lblCambi.getElement().getStyle().set("color", Costants.BLUE);
        lblCambi.getElement().getStyle().set("-webkit-text-fill-color", Costants.BLUE);

        txtCambi = new Span(String.valueOf(checkTotCambiEffettuati));
        txtCambi.getElement().getStyle().set("color", Costants.BLUE);
        txtCambi.getElement().getStyle().set("-webkit-text-fill-color", Costants.BLUE);

        layoutInfoGenerali.add(lblInfo, lblCrediti, txtCrediti, lblCambi, txtCambi);

        top = 160;
        left = 500;
        absLayout.add(layoutInfoGenerali, left, top);

        final HorizontalLayout layoutInfoRuolo = new HorizontalLayout();
        layoutInfoRuolo.setClassName("sidemenu-header");
        layoutInfoRuolo.getThemeList().set("dark", true);
        layoutInfoRuolo.setPadding(true);
        layoutInfoRuolo.setSpacing(true);
        layoutInfoRuolo.setAlignItems(FlexComponent.Alignment.END);

        Image imgP = Utils.buildImage("p.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "p.png"));
        Image imgD = Utils.buildImage("d.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "d.png"));
        Image imgC = Utils.buildImage("c.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "c.png"));
        Image imgA = Utils.buildImage("a.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "a.png"));

        lblInfoP = new Span("0");
        lblInfoD = new Span("0");
        lblInfoC = new Span("0");
        lblInfoA = new Span("0");

        layoutInfoRuolo.add(imgP, lblInfoP, imgD, lblInfoD, imgC, lblInfoC, imgA, lblInfoA);

        left = 950;
        absLayout.add(layoutInfoRuolo, left, top);

        addSquadTablesToLayout();

        tableContaPlayer = buildTableContaPlayer(modelContaPlayer);

        top = 200;
        left = 1250;

        Span lblInfoGiocatori = new Span("Giocatori per Nazione:");
        lblInfoGiocatori.getStyle().set(Costants.FONT_SIZE, "16px");
        lblInfoGiocatori.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);

        absLayout.add(lblInfoGiocatori, left, top);
        absLayout.add(tableContaPlayer, left, 230);

        this.add(absLayout);

        try {
            loadFcFormazione(attore);
            updateTot();
        } catch (Exception e) {
            log.error(e.getMessage());
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }

        if ("0".equals(p.getProperty("ABILITA_MERCATO"))) {
            showMessageStopInsert();
        }
    }

    private void addSquadTablesToLayout() {
        int startLeft = 500;
        int startTop = 230;
        int colWidth = 120;
        int rowHeight = 150;

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            int row = i / 6;
            int col = i % 6;
            int left = startLeft + (col * colWidth);
            int top = startTop + (row * rowHeight);
            absLayout.add(getSlotTable(i), left, top);
        }
    }

    private void updateLabelCambi() {
        int totaleCambi = calcolaCambi();
        checkTotCambiEffettuati = totCambiEffettuati - totaleCambi;
        txtCambi.setText(String.valueOf(checkTotCambiEffettuati));
    }

    private int calcolaCambi() {
        int totCambi = 0;
        for (int i = 0; i < modelFormazione.size() && i < NUM_GIOCATORI; i++) {
            FcGiocatore original = modelFormazione.get(i);
            FcGiocatore current = getPlayerInSlot(i);
            if (current != null && original.getIdGiocatore() != current.getIdGiocatore()) {
                totCambi++;
            }
        }
        return totCambi;
    }

    private void updateTot() {
        int tot = 0;
        int countP = 0;
        int countD = 0;
        int countC = 0;
        int countA = 0;

        Map<String, Integer> countBySquadra = new HashMap<>();

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            if (bean == null) {
                continue;
            }

            tot += bean.getQuotazione();

            switch (bean.getFcRuolo().getIdRuolo()) {
                case "P" -> countP++;
                case "D" -> countD++;
                case "C" -> countC++;
                case "A" -> countA++;
                default -> { }
            }

            countBySquadra.merge(bean.getFcSquadra().getNomeSquadra(), 1, Integer::sum);
        }

        txtCrediti.setText(String.valueOf(Integer.parseInt(creditiMercato) - tot));
        lblInfoP.setText(String.valueOf(countP));
        lblInfoD.setText(String.valueOf(countD));
        lblInfoC.setText(String.valueOf(countC));
        lblInfoA.setText(String.valueOf(countA));

        List<FcProperties> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countBySquadra.entrySet()) {
            FcProperties prop = new FcProperties();
            prop.setKey(entry.getKey());
            prop.setValue(String.valueOf(entry.getValue()));
            list.add(prop);
        }

        list.sort((p1, p2) -> p2.getValue().compareToIgnoreCase(p1.getValue()));
        modelContaPlayer = list;
        tableContaPlayer.setItems(modelContaPlayer);
        tableContaPlayer.getDataProvider().refreshAll();
    }

    private void removeAllElementsList() {
        removeMercatoGiocatore();

        if (activeFilter) {
            modelPlayerP.clear();
            modelPlayerD.clear();
            modelPlayerC.clear();
            modelPlayerA.clear();
            refreshAndSortGridTabsRuoli("");
        } else {
            modelPlayerG.clear();
            refreshAndSortGridGiocatori();
        }

        modelContaPlayer.clear();
        tableContaPlayer.getDataProvider().refreshAll();
    }

    private void removeMercatoGiocatore() {
        clearAllSlots();
    }

    private void loadFcFormazione(FcAttore att) {
        modelFormazione.clear();
        clearAllSlots();

        List<FcFormazione> listFormazione =
                formazioneService.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, att);

        for (FcFormazione f : listFormazione) {
            FcGiocatore bean = f.getFcGiocatore();
            if (bean != null) {
                modelFormazione.add(bean);
                int ord = f.getId().getOrdinamento() - 1;
                if (ord >= 0 && ord < NUM_GIOCATORI) {
                    setPlayerInSlot(ord, bean);
                }
            }
        }
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        if (event.getSource() == saveSendMail && check()) {
            String msg;
            if (!currentGiornata.equals("1")) {
                msg = "Attenzione, una volta cliccato conferma il cambio è definitivo e non è possibile annullarlo.";
            } else {
                msg = "La tua rosa calciatori è stata completata con successo.";
            }
            msg += "Si ricorda di inserire la formazione per la giornata  <" + currentDescGiornata + ">";
            getConfirmDialog(msg).open();
        }
    }

    private @NonNull ConfirmDialog getConfirmDialog(String msg) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(CustomMessageDialog.TITLE_MSG_CONFIRM);
        dialog.setText(msg);
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        dialog.setRejectable(false);
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
                mercatoDettInfo.setTotCambi(currentGiornata.equals("1") ? 0 : totCambi);
                mercatoDettInfo.setFlagInvio("S");
                mercatoDettInfo.setDataInvio(new Date());
                mercatoInfoService.save(mercatoDettInfo);

                int cambiEff = getCambiEffettuati();
                totCambiEffettuati = MAX_CAMBI - cambiEff;
                checkTotCambiEffettuati = totCambiEffettuati;
                txtCambi.setText(String.valueOf(checkTotCambiEffettuati));

                this.saveSendMail.setEnabled(false);

                try {
                    sendNewMail();
                } catch (Exception except) {
                    CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, except.getMessage());
                    return;
                }

                String info = "Operazione effettuata con success.";
                info += "Se hai attiva la notifica email sul profilo, a breve riceverai una email di conferma.";
                CustomMessageDialog.showMessageInfo(info);
                Notification.show(CustomMessageDialog.LABEL_SALVA);

            } catch (Exception except) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, except.getMessage());
            }
        });
        return dialog;
    }

    private int getCambiEffettuati() {
        List<FcMercatoDettInfo> modelCambiInfo = mercatoInfoService.findByFcAttoreOrderByFcGiornataInfoAsc(attore);
        int tot = 0;
        for (FcMercatoDettInfo mi : modelCambiInfo) {
            tot += mi.getTotCambi();
        }
        return tot;
    }

    private boolean check() {
        for (int i = 0; i < NUM_GIOCATORI; i++) {
            if (isSlotEmpty(i)) {
                CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ERROR_INSERT_GIOCATORI);
                return false;
            }
        }

        int tot = 0;
        int countP = 0;
        int countD = 0;
        int countC = 0;
        int countA = 0;

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            if (bean == null) {
                continue;
            }

            tot += bean.getQuotazione();
            switch (bean.getFcRuolo().getIdRuolo()) {
                case "P" -> countP++;
                case "D" -> countD++;
                case "C" -> countC++;
                case "A" -> countA++;
                default -> { }
            }
        }

        if (tot > Integer.parseInt(creditiMercato)) {
            CustomMessageDialog.showMessageError("Attenzione, hai superato " + creditiMercato + " milioni di FM");
            return false;
        }
        if (countP < 2) {
            CustomMessageDialog.showMessageError("Attenzione, devi scegliere obbligatoriamente 2 portieri");
            return false;
        }
        if (countD < 5) {
            CustomMessageDialog.showMessageError("Attenzione, devi scegliere obbligatoriamente 5 difensori");
            return false;
        }
        if (countC < 5) {
            CustomMessageDialog.showMessageError("Attenzione, devi scegliere obbligatoriamente 5 centrocampisti");
            return false;
        }
        if (countA < 4) {
            CustomMessageDialog.showMessageError("Attenzione, devi scegliere obbligatoriamente 4 attaccanti");
            return false;
        }

        int maxChangeSquadra = 6;
        for (FcProperties bean : modelContaPlayer) {
            if (Integer.parseInt(bean.getValue()) > maxChangeSquadra) {
                CustomMessageDialog.showMessageError(
                        "Attenzione, si possono avere al massimo " + maxChangeSquadra
                                + " giocatori appartenenti ad una nazionale");
                return false;
            }
        }

        return true;
    }

    private int insertCambi() {
        List<FcGiocatore> listAcquisti = new ArrayList<>();
        List<FcGiocatore> listCessioni = new ArrayList<>();

        for (int i = 0; i < modelFormazione.size() && i < NUM_GIOCATORI; i++) {
            FcGiocatore original = modelFormazione.get(i);
            FcGiocatore current = getPlayerInSlot(i);

            if (current != null && original.getIdGiocatore() != current.getIdGiocatore()) {
                listAcquisti.add(current);
                listCessioni.add(original);
            }
        }

        for (FcGiocatore g : listCessioni) {
            FcMercatoDett mercato = new FcMercatoDett();
            mercato.setFcAttore(attore);
            mercato.setDataCambio(LocalDateTime.now());
            mercato.setFcGiocatoreByIdGiocVen(g);
            mercato.setFcGiornataInfo(giornataInfo);
            mercato.setNota("+" + g.getQuotazione());
            mercatoService.save(mercato);
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
            mercatoService.save(mercato);
        }

        return totCambi;
    }

    private void insertFormazione() {
        jdbcTemplate.update(
                "delete from fc_giornata_dett where id_attore=? AND id_giornata=?",
                attore.getIdAttore(),
                Integer.parseInt(currentGiornata)
        );

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            int ordinamento = i + 1;

            jdbcTemplate.update(
                    """
                    update fc_formazione
                    set id_giocatore=?, tot_pagato=?
                    where id_attore=? and ordinamento=?
                    """,
                    bean.getIdGiocatore(),
                    bean.getQuotazione(),
                    attore.getIdAttore(),
                    ordinamento
            );
        }
    }

    private void sendNewMail() throws Exception {
        String subject = "Mercato-Cambi " + attore.getDescAttore() + " - " + currentDescGiornata;
        StringBuilder formazioneHtml = new StringBuilder();
        formazioneHtml.append("<html><head><title>FC</title></head>\n");
        formazioneHtml.append("<body>\n");
        formazioneHtml.append("<p>").append(currentDescGiornata).append("</p>\n");
        formazioneHtml.append("<br>\n");
        formazioneHtml.append("<table>");

        Map<String, InputStream> listImg = new HashMap<>();

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            FcGiocatore bean = getPlayerInSlot(i);

            String idRuolo = bean.getFcRuolo().getIdRuolo();
            String color = switch (idRuolo) {
                case "P" -> "BGCOLOR=" + Costants.COLOR_P;
                case "D" -> "BGCOLOR=" + Costants.COLOR_D;
                case "C" -> "BGCOLOR=" + Costants.COLOR_C;
                case "A" -> "BGCOLOR=" + Costants.COLOR_A;
                default -> "BGCOLOR=\"#FF9331\"";
            };

            String cidNomeSq = ContentIdGenerator.getContentId();
            FcSquadra sq = bean.getFcSquadra();
            if (sq.getImg() != null) {
                try {
                    listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }

            formazioneHtml.append("<tr ").append(color).append(">");
            formazioneHtml.append("<td>").append(i + 1).append("</td>");
            formazioneHtml.append("<td>").append(bean.getFcRuolo().getDescRuolo()).append("</td>");
            formazioneHtml.append("<td>").append(bean.getCognGiocatore()).append("</td>");
            formazioneHtml.append("<td><img src=\"cid:").append(cidNomeSq).append("\" />")
                    .append(bean.getFcSquadra().getNomeSquadra()).append("</td>");
            formazioneHtml.append("<td>").append(bean.getQuotazione()).append("</td>");
            formazioneHtml.append("</tr>");
        }

        formazioneHtml.append("</table>\n");

        if (!currentGiornata.equals("1")) {
            List<FcMercatoDett> modelCambi =
                    mercatoService.findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(attore);

            if (!modelCambi.isEmpty()) {
                formazioneHtml.append("<BR>\n<BR>\n<table>\n");
                String color = "BGCOLOR=\"#FF9331\"";

                formazioneHtml.append("<tr ").append(color).append(">");
                formazioneHtml.append("<td>GIORNATA</td>");
                formazioneHtml.append("<td>DATA_CAMBIO</td>");
                formazioneHtml.append("<td>ACQUISTI</td>");
                formazioneHtml.append("<td></td>");
                formazioneHtml.append("<td>CESSIONI</td>");
                formazioneHtml.append("<td></td>");
                formazioneHtml.append("</tr>");

                for (FcMercatoDett m : modelCambi) {
                    String giocAcq = "";
                    String squadraAcq = "";
                    String cidNomeSqAcq = ContentIdGenerator.getContentId();
                    String squadraVen = "";
                    String giocVen = "";
                    String cidNomeSqVen = ContentIdGenerator.getContentId();

                    if (m.getFcGiocatoreByIdGiocAcq() != null) {
                        giocAcq = m.getFcGiocatoreByIdGiocAcq().getCognGiocatore();
                        FcSquadra sqAcq = m.getFcGiocatoreByIdGiocAcq().getFcSquadra();
                        squadraAcq = sqAcq.getNomeSquadra();
                        if (sqAcq.getImg() != null) {
                            try {
                                listImg.put(cidNomeSqAcq, sqAcq.getImg().getBinaryStream());
                            } catch (SQLException e) {
                                log.error(e.getMessage());
                            }
                        }
                    }

                    if (m.getFcGiocatoreByIdGiocVen() != null) {
                        giocVen = m.getFcGiocatoreByIdGiocVen().getCognGiocatore();
                        FcSquadra sqVen = m.getFcGiocatoreByIdGiocVen().getFcSquadra();
                        squadraVen = sqVen.getNomeSquadra();
                        if (sqVen.getImg() != null) {
                            try {
                                listImg.put(cidNomeSqVen, sqVen.getImg().getBinaryStream());
                            } catch (SQLException e) {
                                log.error(e.getMessage());
                            }
                        }
                    }

                    String dataCambio = Utils.formatLocalDateTime(m.getDataCambio(), Costants.DATA_FORMATTED);

                    formazioneHtml.append("<tr ").append(color).append(">");
                    formazioneHtml.append("<td>").append(m.getFcGiornataInfo().getIdGiornataFc()).append("</td>");
                    formazioneHtml.append("<td>").append(dataCambio).append("</td>");
                    formazioneHtml.append("<td>").append(giocAcq).append("</td>");
                    formazioneHtml.append("<td><img src=\"cid:").append(cidNomeSqAcq).append("\" />")
                            .append(squadraAcq).append("</td>");
                    formazioneHtml.append("<td>").append(giocVen).append("</td>");
                    formazioneHtml.append("<td><img src=\"cid:").append(cidNomeSqVen).append("\" />")
                            .append(squadraVen).append("</td>");
                    formazioneHtml.append("</tr>");
                }
                formazioneHtml.append("<table>\n");
            }
        }

        formazioneHtml.append("<BR>\n<BR>\n");
        formazioneHtml.append("<p>Ciao ").append(attore.getDescAttore()).append("</p>\n");
        formazioneHtml.append("</BODY>\n<HTML>");

        StringBuilder emailDestinatario = new StringBuilder();
        String activeMail = p.getProperty("ACTIVE_MAIL");
        if ("true".equals(activeMail)) {
            List<FcAttore> attoriList = attoreService.findByActive(true);
            for (FcAttore a : attoriList) {
                if (a.isNotifiche()) {
                    emailDestinatario.append(a.getEmail()).append(";");
                }
            }
        } else {
            emailDestinatario = new StringBuilder(p.getProperty("to"));
        }

        String[] to = null;
        if (!emailDestinatario.toString().isEmpty()) {
            to = Utils.tornaArrayString(emailDestinatario.toString(), ";");
        }

        try {
            String from = env.getProperty("spring.mail.secondary.username");
            emailService.sendMail2(false, from, to, null, null, subject,
                    formazioneHtml.toString(), "text/html", listImg);
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                String from = env.getProperty("spring.mail.primary.username");
                emailService.sendMail2(true, from, to, null, null, subject,
                        formazioneHtml.toString(), "text/html", listImg);
            } catch (Exception e2) {
                log.error(e2.getMessage());
                throw e2;
            }
        }
    }

//    private FcGiocatore getFcGiocatore(int i) {
//        return getPlayerInSlot(i);
//    }

    private Grid<FcGiocatore> getTableGiocatore(List<FcGiocatore> items, int slotIndex) {
        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.getStyle().set("--_lumo-grid-border-width", "0px");
        grid.setWidth(WIDTH);
        grid.setHeight(HEIGHT);

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

                switch (ruolo) {
                    case "P" -> cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_P);
                    case "D" -> cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_D);
                    case "C" -> cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_C);
                    case "A" -> cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_A);
                    default -> { }
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

                Span lblGiocatore = new Span(g.getCognGiocatore());
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
                            log.error(e.getMessage());
                        }
                    }
                    Span lblInfoNomeSquadra = new Span(sq.getNomeSquadra());
                    lblInfoNomeSquadra.getStyle().set(Costants.FONT_SIZE, "11px");
                    lblInfoNomeSquadra.setTitle(title);
                    cellLayout.add(lblInfoNomeSquadra);
                    cellLayout.setAlignSelf(Alignment.STRETCH, lblInfoNomeSquadra);
                }

                Span lblInfoQuotazione = new Span(String.valueOf(g.getQuotazione()));
                lblInfoQuotazione.getStyle().set(Costants.FONT_SIZE, "14px");
                lblInfoQuotazione.setTitle(title);
                cellLayout.add(lblInfoQuotazione);
                cellLayout.setAlignSelf(Alignment.CENTER, lblInfoQuotazione);

                Element element = cellLayout.getElement();
                element.addEventListener("click", e -> {
                    if (checkTotCambiEffettuati <= 0) {
                        CustomMessageDialog.showMessageError("Attenzione, cambi esauriti");
                        return;
                    }

                    if (activeFilter) {
                        String idRuolo = g.getFcRuolo().getIdRuolo();
                        switch (idRuolo) {
                            case "P" -> modelPlayerP.add(g);
                            case "D" -> modelPlayerD.add(g);
                            case "C" -> modelPlayerC.add(g);
                            case "A" -> modelPlayerA.add(g);
                            default -> { }
                        }
                        refreshAndSortGridTabsRuoli(idRuolo);
                    } else {
                        modelPlayerG.add(g);
                        refreshAndSortGridGiocatori();
                    }

                    clearSlot(slotIndex);
                    updateTot();
                });
            }
            return cellLayout;
        }));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setResizable(false);
        return grid;
    }

    private void refreshAndSortGridGiocatori() {
        modelPlayerG.sort((p1, p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
        tableGiocatori.getDataProvider().refreshAll();
    }

    private void refreshAndSortGridTabsRuoli(String idRuolo) {
        if (StringUtils.isEmpty(idRuolo)) {
            modelPlayerP.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerP.getDataProvider().refreshAll();

            modelPlayerD.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerD.getDataProvider().refreshAll();

            modelPlayerC.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerC.getDataProvider().refreshAll();

            modelPlayerA.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
            tablePlayerA.getDataProvider().refreshAll();
        } else {
            switch (idRuolo) {
                case "P" -> {
                    modelPlayerP.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                    tablePlayerP.getDataProvider().refreshAll();
                }
                case "D" -> {
                    modelPlayerD.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                    tablePlayerD.getDataProvider().refreshAll();
                }
                case "C" -> {
                    modelPlayerC.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                    tablePlayerC.getDataProvider().refreshAll();
                }
                case "A" -> {
                    modelPlayerA.sort((p1, p2) -> p2.getQuotazione().compareTo(p1.getQuotazione()));
                    tablePlayerA.getDataProvider().refreshAll();
                }
                default -> { }
            }
        }
    }

    private void setModelGiocatori(FcAttore att) {
        List<FcGiocatore> listGiocatore;
        if (att == null) {
            listGiocatore = giocatoreService.findAll();
        } else {
            List<FcFormazione> listFormazione =
                    formazioneService.findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(att);
            Collection<Integer> notIn = new ArrayList<>();
            for (FcFormazione f : listFormazione) {
                if (f.getFcGiocatore() != null) {
                    notIn.add(f.getFcGiocatore().getIdGiocatore());
                }
            }
            if (notIn.isEmpty()) {
                notIn.add(-1);
            }
            listGiocatore = giocatoreService.findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(notIn);
        }

        if (activeFilter) {
            modelPlayerP.clear();
            modelPlayerD.clear();
            modelPlayerC.clear();
            modelPlayerA.clear();
        } else {
            modelPlayerG.clear();
        }

        for (FcGiocatore g : listGiocatore) {
            if (activeFilter) {
                switch (g.getFcRuolo().getIdRuolo().toUpperCase()) {
                    case "P" -> modelPlayerP.add(g);
                    case "D" -> modelPlayerD.add(g);
                    case "C" -> modelPlayerC.add(g);
                    case "A" -> modelPlayerA.add(g);
                    default -> { }
                }
            } else {
                modelPlayerG.add(g);
            }
        }
    }

    private Grid<FcGiocatore> getTablePlayer(List<FcGiocatore> items) {
        Grid<FcGiocatore> grid = new Grid<>();
        ListDataProvider<FcGiocatore> dataProvider = new ListDataProvider<>(items);
        grid.setItems(dataProvider);

        comboNazione.addValueChangeListener(event -> applyFilter(dataProvider));
        txtQuotazione.addValueChangeListener(event -> applyFilter(dataProvider));

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
            if (g != null && g.getFcRuolo() != null) {
                String title = getInfoPlayer(g);
                Image img = Utils.buildImage(
                        g.getFcRuolo().getIdRuolo().toLowerCase() + ".png",
                        resourceLoader.getResource(
                                Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
                img.setTitle(title);
                cellLayout.add(img);
            }
            return cellLayout;
        }));
        ruoloColumn.setSortable(false);
        ruoloColumn.setHeader("R");
        ruoloColumn.setWidth("50px");

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
                cellLayout.add(new Span(g.getCognGiocatore()));
                cellLayout.getChildren().findFirst().ifPresent(c -> ((Span) c).setTitle(title));
            }
            return cellLayout;
        }));
        cognGiocatoreColumn.setSortable(true);
        cognGiocatoreColumn.setComparator(Comparator.comparing(FcGiocatore::getCognGiocatore));
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setWidth("150px");

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
                            log.error(e.getMessage());
                        }
                    }
                    Span lblSquadra = new Span(sq.getNomeSquadra());
                    lblSquadra.setTitle(title);
                    cellLayout.add(lblSquadra);
                }
            }
            return cellLayout;
        }));
        nomeSquadraColumn.setSortable(true);
        nomeSquadraColumn.setComparator(Comparator.comparing(p -> p.getFcSquadra().getNomeSquadra()));
        nomeSquadraColumn.setHeader("Naz");
        nomeSquadraColumn.setWidth("150px");

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
                Span lblQuotazione = new Span(String.valueOf(g.getQuotazione()));
                lblQuotazione.setTitle(title);
                cellLayout.add(lblQuotazione);
            }
            return cellLayout;
        }));
        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader("Q");
        quotazioneColumn.setWidth("50px");

        grid.addItemClickListener(event -> {
            FcGiocatore bean = event.getItem();
            if (existGiocatore(bean)) {
                return;
            }

            boolean added = addPlayerToFirstEmptySlot(bean);
            if (added) {
                if (activeFilter) {
                    String idRuolo = bean.getFcRuolo().getIdRuolo().toUpperCase();
                    switch (idRuolo) {
                        case "P" -> modelPlayerP.remove(bean);
                        case "D" -> modelPlayerD.remove(bean);
                        case "C" -> modelPlayerC.remove(bean);
                        case "A" -> modelPlayerA.remove(bean);
                        default -> { }
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
        });

        return grid;
    }

    private boolean addPlayerToFirstEmptySlot(FcGiocatore bean) {
        for (int i = 0; i < NUM_GIOCATORI; i++) {
            if (isSlotEmpty(i)) {
                setPlayerInSlot(i, bean);
                return true;
            }
        }
        return false;
    }

    private void applyFilter(ListDataProvider<FcGiocatore> dataProvider) {
        dataProvider.clearFilters();
        if (comboNazione.getValue() != null) {
            dataProvider.addFilter(s -> comboNazione.getValue().getIdSquadra() == s.getFcSquadra().getIdSquadra());
        }
        if (txtQuotazione.getValue() != null) {
            dataProvider.addFilter(s -> s.getQuotazione() <= txtQuotazione.getValue().intValue());
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
            if ("P".equalsIgnoreCase(bean.getFcRuolo().getIdRuolo())) {
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
                FcSquadra sq = squadraService.findByNomeSquadra(f.getKey());
                if (sq.getImg40() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg40().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        log.error(e.getMessage());
                    }
                }
                cellLayout.add(new Span(f.getKey()));
            }
            return cellLayout;
        }));
        keyColumn.setSortable(false);
        keyColumn.setAutoWidth(true);

        Column<FcProperties> valueColumn = grid.addColumn(FcProperties::getValue);
        valueColumn.setSortable(false);
        valueColumn.setAutoWidth(true);

        return grid;
    }

    private boolean existGiocatore(FcGiocatore g) {
        for (int i = 0; i < NUM_GIOCATORI; i++) {
            if (getSlot(i).contains(g)) {
                return true;
            }
        }
        return false;
    }

    private void ordinaMercato() {
        List<FcGiocatore> modelMercatoGiocatori = new ArrayList<>();

        for (int i = 0; i < NUM_GIOCATORI; i++) {
            FcGiocatore player = getPlayerInSlot(i);
            if (player != null) {
                modelMercatoGiocatori.add(player);
            }
        }

        clearAllSlots();

        modelMercatoGiocatori.sort(
                Comparator.comparing(FcGiocatore::isFlagAttivo).reversed()
                        .thenComparing(g -> g.getFcRuolo().getIdRuolo(), Comparator.reverseOrder())
                        .thenComparing(FcGiocatore::getQuotazione, Comparator.reverseOrder())
        );

        modelFormazione.clear();
        for (int i = 0; i < modelMercatoGiocatori.size(); i++) {
            FcGiocatore g = modelMercatoGiocatori.get(i);
            modelFormazione.add(g);
            setPlayerInSlot(i, g);
        }
    }
}
