package fcapp.ui.views.em;

import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.ronny.AbsoluteLayout;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.ContentIdGenerator;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Schiera Formazione")
@Route(value = "insertEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmTeamInsertView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String WIDTH = "100px";
    private static final String HEIGHT = "120px";

    private static final int TOTAL_SLOTS = 23;
    private static final int STARTER_SLOTS = 11;

    private static final String[] SCHEMI = { "5-4-1", "5-3-2", "4-5-1", "4-4-2", "4-3-3", "3-5-2", "3-4-3" };

    private static final int PX_P = 210;
    private static final int PX_D = 360;
    private static final int PX_C = 510;
    private static final int PX_A = 660;

    private static final int PX_350 = 350;
    private static final int PX_400 = 400;
    private static final int PX_450 = 450;
    private static final int PX_500 = 500;
    private static final int PX_550 = 550;
    private static final int PX_600 = 600;
    private static final int PX_650 = 650;
    private static final int PX_700 = 700;
    private static final int PX_750 = 750;
    private static final int PX_860 = 860;
    private static final int PX_960 = 960;
    private static final int PX_1060 = 1060;
    private static final int PX_1200 = 1200;

    private final transient Logger log = LoggerFactory.getLogger(this.getClass());
    private final transient Environment env;
    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient EmailService emailService;
    private final transient AttoreService attoreService;
    private final transient FormazioneService formazioneService;
    private final transient GiornataDettService giornataDettService;
    private final transient CalendarioCompetizioneService calendarioCompetizioneService;
    private final transient AccessoService accessoService;
    private final transient SquadraService squadraService;

    private AbsoluteLayout absLayout;

    private FcAttore attore;
    private FcGiornataInfo giornataInfo;
    private FcCampionato campionato;
    private String nextDate;
    private long millisDiff = 0;
    private String idAttore = "";
    private String descAttore = "";
    private Properties p;

    private ToggleButton checkMail;
    private ComboBox<String> comboModulo;

    private Grid<FcGiocatore> tableFormazione;

    private final List<List<FcGiocatore>> squadSlots = new ArrayList<>();
    private final List<Grid<FcGiocatore>> squadTables = new ArrayList<>();

    private List<FcGiocatore> modelFormazione = new ArrayList<>();

    private record SlotPosition(int left, int top) {}

    private record ModuleSlots(
            List<Integer> defenders,
            List<Integer> midfielders,
            List<Integer> attackers
    ) {}

    private static final Map<String, ModuleSlots> MODULE_SLOTS = Map.of(
            "5-4-1", new ModuleSlots(
                    List.of(1, 2, 3, 4, 5),
                    List.of(6, 7, 8, 9),
                    List.of(10)
            ),
            "5-3-2", new ModuleSlots(
                    List.of(1, 2, 3, 4, 5),
                    List.of(6, 7, 8),
                    List.of(9, 10)
            ),
            "4-5-1", new ModuleSlots(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7, 8, 9),
                    List.of(10)
            ),
            "4-4-2", new ModuleSlots(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7, 8),
                    List.of(9, 10)
            ),
            "4-3-3", new ModuleSlots(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7),
                    List.of(8, 9, 10)
            ),
            "3-5-2", new ModuleSlots(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6, 7, 8),
                    List.of(9, 10)
            ),
            "3-4-3", new ModuleSlots(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6, 7),
                    List.of(8, 9, 10)
            )
    );

    public EmTeamInsertView(Environment env, JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader,
                            EmailService emailService,
                            AttoreService attoreService, FormazioneService formazioneService,
                            GiornataDettService giornataDettService,
                            CalendarioCompetizioneService calendarioCompetizioneService,
                            AccessoService accessoService, SquadraService squadraService) {
        log.info("EmTeamInsertView()");
        this.env = env;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.emailService = emailService;
        this.attoreService = attoreService;
        this.formazioneService = formazioneService;
        this.giornataDettService = giornataDettService;
        this.calendarioCompetizioneService = calendarioCompetizioneService;
        this.accessoService = accessoService;
        this.squadraService = squadraService;
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
        giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
        campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
        nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
        millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

        idAttore = String.valueOf(attore.getIdAttore());
        descAttore = attore.getDescAttore();

        modelFormazione = getModelFormazione(attore, campionato);
    }

    private void initSlots() {
        squadSlots.clear();
        squadTables.clear();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
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
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            clearSlot(i);
        }
    }

    private void initLayout() {
        absLayout = new AbsoluteLayout(1600, 1200);
        absLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        absLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);

        Button save = new Button("Save");
        save.setIcon(VaadinIcon.DATABASE.create());
        save.addClickListener(this);

        checkMail = new ToggleButton();
        checkMail.setLabel("Invia Email");
        checkMail.setValue(true);

        comboModulo = new ComboBox<>();
        comboModulo.setItems(SCHEMI);
        comboModulo.setClearButtonVisible(true);
        comboModulo.setPlaceholder("Modulo");
        comboModulo.addValueChangeListener(evt -> {
            log.info("addValueChangeListener {}", evt.getValue());
            renderModulo(evt.getValue());
        });

        tableFormazione = getTableFormazione(modelFormazione);

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            squadTables.add(getTableGiocatore(getSlot(i), i));
        }

        List<FcCalendarioCompetizione> listPartite =
                calendarioCompetizioneService.findByIdGiornataOrderByDataAsc(giornataInfo.getCodiceGiornata());
        Grid<FcCalendarioCompetizione> tablePartite = getTablePartite(listPartite);

        Image panchina = Utils.buildImage("panchina.jpg",
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "panchina.jpg"));

        final VerticalLayout layoutAvviso = new VerticalLayout();
        layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);
        layoutAvviso.setWidth("500px");

        HorizontalLayout cssLayout = new HorizontalLayout();
        cssLayout.add(new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato)));
        layoutAvviso.add(cssLayout);

        HorizontalLayout cssLayout2 = new HorizontalLayout();
        cssLayout2.add(new Span("Consegna Formazione entro: " + nextDate));
        layoutAvviso.add(cssLayout2);

        int top = 5;
        absLayout.add(save, 20, top);
        absLayout.add(checkMail, 110, top + 5);
        absLayout.add(layoutAvviso, PX_350, top);
        absLayout.add(panchina, PX_860, top);
        absLayout.add(tablePartite, PX_1200, top);

        absLayout.add(comboModulo, 20, 50);
        absLayout.add(tableFormazione, 10, 150);

        Image campo = Utils.buildImage("campo.jpg",
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "campo.jpg"));
        absLayout.add(campo, PX_350, 150);

        this.add(absLayout);

        try {
            loadFcGiornataDett(attore, giornataInfo);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (millisDiff == 0) {
            showMessageStopInsert();
        } else {
            SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
            timer.setHours(true);
            timer.setMinutes(true);
            timer.setFractions(false);
            timer.start();
            timer.addTimerEndEvent(ev -> showMessageStopInsert());
            layoutAvviso.add(timer);
        }
    }

    private void renderModulo(String modulo) {
        removeAllElementsList();
        if (modulo == null) {
            return;
        }

        Map<Integer, SlotPosition> positions = getSlotPositions(modulo);
        positions.forEach((slotIndex, pos) -> absLayout.add(getSlotTable(slotIndex), pos.left(), pos.top()));
    }

    private Map<Integer, SlotPosition> getSlotPositions(String modulo) {
        Map<Integer, SlotPosition> map = new HashMap<>();

        map.put(0, new SlotPosition(PX_550, PX_P));

        map.put(11, new SlotPosition(PX_860, PX_P));
        map.put(12, new SlotPosition(PX_960, PX_P));
        map.put(13, new SlotPosition(PX_1060, PX_P));

        map.put(14, new SlotPosition(PX_860, PX_D));
        map.put(15, new SlotPosition(PX_960, PX_D));
        map.put(16, new SlotPosition(PX_1060, PX_D));

        map.put(17, new SlotPosition(PX_860, PX_C));
        map.put(18, new SlotPosition(PX_960, PX_C));
        map.put(19, new SlotPosition(PX_1060, PX_C));

        map.put(20, new SlotPosition(PX_860, PX_A));
        map.put(21, new SlotPosition(PX_960, PX_A));
        map.put(22, new SlotPosition(PX_1060, PX_A));

        switch (modulo) {
            case "5-4-1" -> {
                map.put(1, new SlotPosition(PX_350, PX_D));
                map.put(2, new SlotPosition(PX_450, PX_D));
                map.put(3, new SlotPosition(PX_550, PX_D));
                map.put(4, new SlotPosition(PX_650, PX_D));
                map.put(5, new SlotPosition(PX_750, PX_D));

                map.put(6, new SlotPosition(PX_400, PX_C));
                map.put(7, new SlotPosition(PX_500, PX_C));
                map.put(8, new SlotPosition(PX_600, PX_C));
                map.put(9, new SlotPosition(PX_700, PX_C));

                map.put(10, new SlotPosition(PX_550, PX_A));
            }
            case "5-3-2" -> {
                map.put(1, new SlotPosition(PX_350, PX_D));
                map.put(2, new SlotPosition(PX_450, PX_D));
                map.put(3, new SlotPosition(PX_550, PX_D));
                map.put(4, new SlotPosition(PX_650, PX_D));
                map.put(5, new SlotPosition(PX_750, PX_D));

                map.put(6, new SlotPosition(PX_450, PX_C));
                map.put(7, new SlotPosition(PX_550, PX_C));
                map.put(8, new SlotPosition(PX_650, PX_C));

                map.put(9, new SlotPosition(PX_500, PX_A));
                map.put(10, new SlotPosition(PX_600, PX_A));
            }
            case "4-5-1" -> {
                map.put(1, new SlotPosition(PX_400, PX_D));
                map.put(2, new SlotPosition(PX_500, PX_D));
                map.put(3, new SlotPosition(PX_600, PX_D));
                map.put(4, new SlotPosition(PX_700, PX_D));

                map.put(5, new SlotPosition(PX_350, PX_C));
                map.put(6, new SlotPosition(PX_450, PX_C));
                map.put(7, new SlotPosition(PX_550, PX_C));
                map.put(8, new SlotPosition(PX_650, PX_C));
                map.put(9, new SlotPosition(PX_750, PX_C));

                map.put(10, new SlotPosition(PX_550, PX_A));
            }
            case "4-4-2" -> {
                map.put(1, new SlotPosition(PX_400, PX_D));
                map.put(2, new SlotPosition(PX_500, PX_D));
                map.put(3, new SlotPosition(PX_600, PX_D));
                map.put(4, new SlotPosition(PX_700, PX_D));

                map.put(5, new SlotPosition(PX_400, PX_C));
                map.put(6, new SlotPosition(PX_500, PX_C));
                map.put(7, new SlotPosition(PX_600, PX_C));
                map.put(8, new SlotPosition(PX_700, PX_C));

                map.put(9, new SlotPosition(PX_500, PX_A));
                map.put(10, new SlotPosition(PX_600, PX_A));
            }
            case "4-3-3" -> {
                map.put(1, new SlotPosition(PX_400, PX_D));
                map.put(2, new SlotPosition(PX_500, PX_D));
                map.put(3, new SlotPosition(PX_600, PX_D));
                map.put(4, new SlotPosition(PX_700, PX_D));

                map.put(5, new SlotPosition(PX_450, PX_C));
                map.put(6, new SlotPosition(PX_550, PX_C));
                map.put(7, new SlotPosition(PX_650, PX_C));

                map.put(8, new SlotPosition(PX_450, PX_A));
                map.put(9, new SlotPosition(PX_550, PX_A));
                map.put(10, new SlotPosition(PX_650, PX_A));
            }
            case "3-5-2" -> {
                map.put(1, new SlotPosition(PX_450, PX_D));
                map.put(2, new SlotPosition(PX_550, PX_D));
                map.put(3, new SlotPosition(PX_650, PX_D));

                map.put(4, new SlotPosition(PX_350, PX_C));
                map.put(5, new SlotPosition(PX_450, PX_C));
                map.put(6, new SlotPosition(PX_550, PX_C));
                map.put(7, new SlotPosition(PX_650, PX_C));
                map.put(8, new SlotPosition(PX_750, PX_C));

                map.put(9, new SlotPosition(PX_500, PX_A));
                map.put(10, new SlotPosition(PX_600, PX_A));
            }
            case "3-4-3" -> {
                map.put(1, new SlotPosition(PX_450, PX_D));
                map.put(2, new SlotPosition(PX_550, PX_D));
                map.put(3, new SlotPosition(PX_650, PX_D));

                map.put(4, new SlotPosition(PX_400, PX_C));
                map.put(5, new SlotPosition(PX_500, PX_C));
                map.put(6, new SlotPosition(PX_600, PX_C));
                map.put(7, new SlotPosition(PX_700, PX_C));

                map.put(8, new SlotPosition(PX_450, PX_A));
                map.put(9, new SlotPosition(PX_550, PX_A));
                map.put(10, new SlotPosition(PX_650, PX_A));
            }
            default -> {
            }
        }

        return map;
    }

    private void showMessageStopInsert() {
        setEnabled(false);
        CustomMessageDialog.showMessageError("Impossibile inserire la formazione, tempo scaduto!");
    }

    private String getInfoPlayer(FcGiocatore bean) {
        String info = "N.D.";
        if (bean != null && bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / Costants.DIVISORE_10);
            String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / Costants.DIVISORE_10);

            info = bean.getCognGiocatore() + "\n";
            info += "Squadra: " + bean.getFcSquadra().getNomeSquadra() + "\n";
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

    private void refreshAndSortGridFormazione() {
        modelFormazione.sort((p1, p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
        tableFormazione.getDataProvider().refreshAll();
    }

    private void removeAllElementsList() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            if (bean != null) {
                modelFormazione.add(bean);
                clearSlot(i);
            }
        }

        refreshAndSortGridFormazione();

        for (Grid<FcGiocatore> grid : squadTables) {
            absLayout.remove(grid);
        }
    }

    private ArrayList<FcGiocatore> getModelFormazione(FcAttore attore, FcCampionato campionato) {
        List<FcFormazione> listFormazione =
                formazioneService.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
                        campionato, attore, false);

        ArrayList<FcGiocatore> beans = new ArrayList<>();
        for (FcFormazione f : listFormazione) {
            if (f.getFcGiocatore() != null) {
                beans.add(f.getFcGiocatore());
            }
        }
        return beans;
    }

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

                if ("P".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_P);
                } else if ("D".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_D);
                } else if ("C".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_C);
                } else if ("A".equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_A);
                }

                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }

                Span lblOrdinamento = new Span(String.valueOf(slotIndex + 1));
                lblOrdinamento.getStyle().set(Costants.FONT_SIZE, "14px");
                lblOrdinamento.setTitle(title);
                cellLayout.add(lblOrdinamento);
                cellLayout.setAlignSelf(Alignment.CENTER, lblOrdinamento);

                Image imgR = Utils.buildImage(
                        ruolo.toLowerCase() + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo.toLowerCase() + ".png"));
                imgR.setTitle(title);
                cellLayout.add(imgR);

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

                Element element = cellLayout.getElement();
                element.addEventListener("click", e -> {
                    log.info("giocatore {}", g.getCognGiocatore());
                    modelFormazione.add(g);
                    refreshAndSortGridFormazione();
                    clearSlot(slotIndex);
                });
            }
            return cellLayout;
        }));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setResizable(false);

        return grid;
    }

    private Grid<FcGiocatore> getTableFormazione(List<FcGiocatore> items) {
        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth("330px");

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
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader("R");
        ruoloColumn.setWidth("35px");
        ruoloColumn.setComparator(Comparator.comparing(p -> p.getFcRuolo().getIdRuolo()));

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
                Span lblGiocatore = new Span(g.getCognGiocatore());
                lblGiocatore.setTitle(title);
                cellLayout.add(lblGiocatore);
            }
            return cellLayout;
        }));
        cognGiocatoreColumn.setSortable(false);
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setWidth("145px");

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
                    Span lblSquadra = new Span(sq.getNomeSquadra().substring(0, 3));
                    lblSquadra.setTitle(title);
                    cellLayout.add(lblSquadra);
                }
            }
            return cellLayout;
        }));
        nomeSquadraColumn.setSortable(true);
        nomeSquadraColumn.setComparator(Comparator.comparing(p -> p.getFcSquadra().getNomeSquadra()));
        nomeSquadraColumn.setHeader("Naz");
        nomeSquadraColumn.setWidth("70px");

        Column<FcGiocatore> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            if (g != null) {
                String title = getInfoPlayer(g);
                if (!g.isFlagAttivo()) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }
                FcStatistiche s = g.getFcStatistiche();
                String imgThink = "2.png";
                if (s != null && s.getMediaVoto() != 0) {
                    if (s.getMediaVoto() > Costants.EM_RANGE_MAX_MV) {
                        imgThink = "1.png";
                    } else if (s.getMediaVoto() < Costants.EM_RANGE_MIN_MV) {
                        imgThink = "3.png";
                    }
                }

                Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
                img.setTitle(title);
                cellLayout.add(img);

                DecimalFormat myFormatter = new DecimalFormat("#0.00");
                Double d = 0d;
                if (s != null) {
                    d = s.getMediaVoto() / Costants.DIVISORE_10;
                }
                String sTotPunti = myFormatter.format(d);
                Span lbl = new Span(sTotPunti);
                lbl.setTitle(title);
                cellLayout.add(lbl);
            }
            return cellLayout;
        }));
        mediaVotoColumn.setSortable(true);
        mediaVotoColumn.setComparator(Comparator.comparing(p -> p.getFcStatistiche().getMediaVoto()));
        mediaVotoColumn.setHeader("Mv");
        mediaVotoColumn.setWidth("70px");

        grid.addItemClickListener(event -> {
            String valModulo = comboModulo.getValue();
            if (valModulo == null) {
                return;
            }

            FcGiocatore bean = event.getItem();
            log.info("click {}", bean.getCognGiocatore());

            if (existGiocatore(bean)) {
                log.info("existGiocatore true");
                return;
            }

            boolean added = assignPlayerToFormation(bean, valModulo);
            if (added) {
                modelFormazione.remove(bean);
                refreshAndSortGridFormazione();
            }
        });

        return grid;
    }

    private boolean assignPlayerToFormation(FcGiocatore bean, String modulo) {
        ModuleSlots moduleSlots = MODULE_SLOTS.get(modulo);
        if (moduleSlots == null) {
            return false;
        }

        String ruolo = bean.getFcRuolo().getIdRuolo();
        return switch (ruolo) {
            case "P" -> assignToFirstEmpty(bean, List.of(0), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
            case "D" -> assignToFirstEmpty(bean, moduleSlots.defenders(), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
            case "C" -> assignToFirstEmpty(bean, moduleSlots.midfielders(), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
            case "A" -> assignToFirstEmpty(bean, moduleSlots.attackers(), List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
            default -> false;
        };
    }

    private boolean assignToFirstEmpty(FcGiocatore bean, List<Integer> preferredSlots, List<Integer> reserveSlots) {
        for (Integer index : preferredSlots) {
            if (isSlotEmpty(index)) {
                setPlayerInSlot(index, bean);
                return true;
            }
        }
        for (Integer index : reserveSlots) {
            if (isSlotEmpty(index)) {
                setPlayerInSlot(index, bean);
                return true;
            }
        }
        return false;
    }

    private boolean existGiocatore(FcGiocatore g) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (getSlot(i).contains(g)) {
                return true;
            }
        }
        return false;
    }

    private void loadFcGiornataDett(FcAttore attore, FcGiornataInfo giornataInfo) {
        log.info("loadFcGiornatadett");

        List<FcGiornataDett> lGiocatori =
                giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

        if (lGiocatori.isEmpty()) {
            comboModulo.setValue(null);
            removeAllElementsList();
            return;
        }

        int countD = 0;
        int countC = 0;
        int countA = 0;

        for (FcGiornataDett gd : lGiocatori) {
            if (gd.getOrdinamento() < 12) {
                switch (gd.getFcGiocatore().getFcRuolo().getIdRuolo()) {
                    case "D" -> countD++;
                    case "C" -> countC++;
                    case "A" -> countA++;
                    default -> { }
                }
            }
        }

        String schema = countD + "-" + countC + "-" + countA;
        comboModulo.setValue(schema);

        modelFormazione.clear();
        refreshAndSortGridFormazione();
        clearAllSlots();

        for (FcGiornataDett gd : lGiocatori) {
            FcGiocatore bean = gd.getFcGiocatore();
            int slotIndex = gd.getOrdinamento() - 1;

            if (slotIndex >= 0 && slotIndex < TOTAL_SLOTS) {
                setPlayerInSlot(slotIndex, bean);
            } else {
                modelFormazione.add(bean);
            }
        }

        refreshAndSortGridFormazione();
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        if (!check()) {
            return;
        }

        try {
            insert(giornataInfo.getCodiceGiornata());
        } catch (Exception exi) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exi.getMessage());
            return;
        }

        if (Boolean.TRUE.equals(checkMail.getValue())) {
            try {
                String dataOra = getSysdate();

                sendNewMail(giornataInfo.getDescGiornataFc());
                log.info("send_mail OK");

                try {
                    insertDettInfo(giornataInfo.getCodiceGiornata(), dataOra);
                    log.info("insert_dett_info OK");
                } catch (Exception exd) {
                    log.error(exd.getMessage());
                    CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exd.getMessage());
                }

                CustomMessageDialog.showMessageInfo("Formazione inserita, email inviata con successo!");

            } catch (Exception exception) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, exception.getMessage());
            }
        } else {
            CustomMessageDialog.showMessageInfo(
                    "Formazione inserita con successo! \nPer rendere effettiva la formazione abilitare invio email.");
        }
    }

    private String getSysdate() {
        String sql = "select sysdate() from dual";
        return jdbcTemplate.query(sql, rs -> rs.next() ? rs.getString(1) : null);
    }

    private boolean check() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (isSlotEmpty(i)) {
                CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ERROR_INSERT_GIOCATORI);
                return false;
            }
        }
        return true;
    }

    private void insert(int giornata) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=? AND ID_ATTORE=?",
                    giornata,
                    Integer.parseInt(idAttore)
            );

            int ord = 1;
            for (int i = 0; i < TOTAL_SLOTS; i++) {
                FcGiocatore bean = getPlayerInSlot(i);
                String stato = i < STARTER_SLOTS ? "T" : "R";

                jdbcTemplate.update(
                        """
                        INSERT INTO fc_giornata_dett
                        (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO)
                        VALUES (?, ?, ?, ?, ?, 0)
                        """,
                        giornata,
                        Integer.parseInt(idAttore),
                        bean.getIdGiocatore(),
                        stato,
                        ord++
                );
            }

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private void insertDettInfo(int giornata, String dataOra) {
        try {
            jdbcTemplate.update(
                    "DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=? AND ID_ATTORE=?",
                    giornata,
                    Integer.parseInt(idAttore)
            );

            jdbcTemplate.update(
                    """
                    INSERT INTO fc_giornata_dett_info
                    (ID_GIORNATA, ID_ATTORE, FLAG_INVIO, DATA_INVIO)
                    VALUES (?, ?, 1, ?)
                    """,
                    giornata,
                    Integer.parseInt(idAttore),
                    dataOra
            );
        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private void sendNewMail(String descGiornata) throws Exception {
        String subject = "Formazione " + descAttore + " - " + descGiornata;
        String modulo = comboModulo.getValue();

        StringBuilder formazioneHtml = new StringBuilder();
        formazioneHtml.append("<html><head><title>FC</title></head>\n");
        formazioneHtml.append("<body>\n");
        formazioneHtml.append("<p>").append(descGiornata).append("</p>\n");
        formazioneHtml.append("<br>\n");
        formazioneHtml.append("<p>").append(modulo).append("</p>\n");
        formazioneHtml.append("<br>\n");
        formazioneHtml.append("<table>");

        Map<String, InputStream> listImg = new HashMap<>();

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            String stato = i < STARTER_SLOTS ? "Titolare" : "Riserva";
            String color = i < STARTER_SLOTS ? "BGCOLOR=\"#ABFF73\"" : "BGCOLOR=\"#FFFF84\"";

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
            formazioneHtml.append("<td>").append(stato).append("</td>");
            formazioneHtml.append("</tr>");
        }

        formazioneHtml.append("</table>\n");
        formazioneHtml.append("<br>\n");
        formazioneHtml.append("<br>\n");
        formazioneHtml.append("<p>Ciao ").append(descAttore).append("</p>\n");
        formazioneHtml.append("</body>\n");
        formazioneHtml.append("<html>");

        StringBuilder emailDestinatario = new StringBuilder();
        String activeMail = p.getProperty("ACTIVE_MAIL");
        if ("true".equals(activeMail)) {
            List<FcAttore> attori = attoreService.findByActive(true);
            for (FcAttore a : attori) {
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

    private FcGiocatore getFcGiocatore(int i) {
        return getPlayerInSlot(i);
    }

    private Grid<FcCalendarioCompetizione> getTablePartite(List<FcCalendarioCompetizione> listPartite) {
        Grid<FcCalendarioCompetizione> grid = new Grid<>();
        grid.setItems(listPartite);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth("300px");

        Column<FcCalendarioCompetizione> nomeSquadraCasaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (s != null && s.getSquadraCasa() != null) {
                FcSquadra sq = squadraService.findByIdSquadra(s.getIdSquadraCasa());
                if (sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        log.error(e.getMessage());
                    }
                }
                cellLayout.add(new Span(s.getSquadraCasa().substring(0, 3)));
            }

            return cellLayout;
        }));
        nomeSquadraCasaColumn.setSortable(false);
        nomeSquadraCasaColumn.setAutoWidth(true);

        Column<FcCalendarioCompetizione> nomeSquadraFuoriColumn = grid.addColumn(new ComponentRenderer<>(s -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (s != null && s.getSquadraCasa() != null) {
                Span lblSquadra = new Span(s.getSquadraFuori().substring(0, 3));
                FcSquadra sq = squadraService.findByIdSquadra(s.getIdSquadraFuori());
                if (sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        log.error(e.getMessage());
                    }
                }
                cellLayout.add(lblSquadra);
            }

            return cellLayout;
        }));
        nomeSquadraFuoriColumn.setSortable(false);
        nomeSquadraFuoriColumn.setAutoWidth(true);

        Column<FcCalendarioCompetizione> dataColumn = grid.addColumn(
                new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataColumn.setSortable(false);
        dataColumn.setAutoWidth(true);
        dataColumn.setFlexGrow(2);

        return grid;
    }
}
