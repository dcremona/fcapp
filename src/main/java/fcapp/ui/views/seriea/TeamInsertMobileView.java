package fcapp.ui.views.seriea;

import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.ronny.AbsoluteLayout;

import com.flowingcode.vaadin.addons.relativetime.Format;
import com.flowingcode.vaadin.addons.relativetime.RelativeTime;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataGiocatoreService;
import fcapp.backend.service.SquadraService;
import fcapp.utils.ContentIdGenerator;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mobile")
@Route(value = "mobile")
@RolesAllowed("USER")
public class TeamInsertMobileView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int WINWIDTH = 400;
    private static final int WINHEIGHT = 800;

    private static final String WIDTH = "85px";
    private static final String HEIGHT = "105px";

    private static final int TOTAL_SLOTS = 18;
    private static final int STARTER_SLOTS = 11;
    //private static final int BENCH_START_INDEX = 11;

    private static final int GK_STARTER_SLOT = 0;
    private static final int GK_BENCH_SLOT = 11;

    private static final List<Integer> DEF_BENCH_SLOTS = List.of(12, 13);
    private static final List<Integer> MID_BENCH_SLOTS = List.of(14, 15);
    private static final List<Integer> ATT_BENCH_SLOTS = List.of(16, 17);

    private static final int PX_P = 60;
    private static final int PX_D = 180;
    private static final int PX_C = 300;
    private static final int PX_A = 420;

    private static final int PX_0 = 0;
    private static final int PX_20 = 20;
    private static final int PX_70 = 70;
    private static final int PX_80 = 80;
    private static final int PX_110 = 110;
    private static final int PX_160 = 160;
    private static final int PX_200 = 200;
    private static final int PX_240 = 240;
    private static final int PX_250 = 250;
    private static final int PX_290 = 290;
    private static final int PX_320 = 320;

    private final transient Logger log = LoggerFactory.getLogger(this.getClass());
    private final transient Environment env;
    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient FormazioneService formazioneService;
    private final transient GiornataDettService giornataDettService;
    private final transient CalendarioCompetizioneService calendarioCompetizioneService;
    private final transient AccessoService accessoService;
    private final transient SquadraService squadraService;
    private final transient GiornataGiocatoreService giornataGiocatoreService;
    private final transient AttoreService attoreService;
    private final transient EmailService emailService;

    private FcAttore attore;
    private FcGiornataInfo giornataInfo;
    private FcCampionato campionato;
    private String nextDate = null;
    private long millisDiff = 0;
    private LocalDateTime dateTime = null;
    private String idAttore = "";
    private String descAttore = "";
    private Properties p;

    private Button rosa;
    private Button save;
    private Button viewPartite;
    private ToggleButton checkMail;
    private ComboBox<String> comboModulo;

    private Dialog dialogTribuna;
    private Grid<FcGiocatore> tableFormazione;

    private Dialog dialogPartite;
    private List<FcCalendarioCompetizione> listPartiteGiocate = new ArrayList<>();
    private List<FcCalendarioCompetizione> listPartite = new ArrayList<>();

    private List<FcGiornataGiocatore> listSqualificatiInfortunati = new ArrayList<>();

    private AbsoluteLayout absLayout;

    private final List<List<FcGiocatore>> slotModels = new ArrayList<>();
    private final List<Grid<FcGiocatore>> slotTables = new ArrayList<>();

    private List<FcGiocatore> modelFormazione = new ArrayList<>();

    private record SlotPosition(int left, int top) {}
    private record ModuleConfig(List<Integer> difensori, List<Integer> centrocampisti, List<Integer> attaccanti) {}

    private static final Map<String, ModuleConfig> MODULE_CONFIGS = Map.of(
            Costants.SCHEMA_541, new ModuleConfig(
                    List.of(1, 2, 3, 4, 5),
                    List.of(6, 7, 8, 9),
                    List.of(10)
            ),
            Costants.SCHEMA_532, new ModuleConfig(
                    List.of(1, 2, 3, 4, 5),
                    List.of(6, 7, 8),
                    List.of(9, 10)
            ),
            Costants.SCHEMA_451, new ModuleConfig(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7, 8, 9),
                    List.of(10)
            ),
            Costants.SCHEMA_442, new ModuleConfig(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7, 8),
                    List.of(9, 10)
            ),
            Costants.SCHEMA_433, new ModuleConfig(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7),
                    List.of(8, 9, 10)
            ),
            Costants.SCHEMA_352, new ModuleConfig(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6, 7, 8),
                    List.of(9, 10)
            ),
            Costants.SCHEMA_343, new ModuleConfig(
                    List.of(1, 2, 3),
                    List.of(4, 5, 6, 7),
                    List.of(8, 9, 10)
            )
    );

    public TeamInsertMobileView(
            Environment env,
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            FormazioneService formazioneService,
            GiornataDettService giornataDettService,
            CalendarioCompetizioneService calendarioCompetizioneService,
            AccessoService accessoService,
            SquadraService squadraService,
            GiornataGiocatoreService giornataGiocatoreService,
            AttoreService attoreService,
            EmailService emailService) {
        log.info("TeamInsertMobileView()");
        this.env = env;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.formazioneService = formazioneService;
        this.giornataDettService = giornataDettService;
        this.calendarioCompetizioneService = calendarioCompetizioneService;
        this.accessoService = accessoService;
        this.squadraService = squadraService;
        this.giornataGiocatoreService = giornataGiocatoreService;
        this.attoreService = attoreService;
        this.emailService = emailService;
    }

    @PostConstruct
    void init() {
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
        dateTime  = (LocalDateTime) VaadinSession.getCurrent().getAttribute("FUTURE");

        idAttore = String.valueOf(attore.getIdAttore());
        descAttore = attore.getDescAttore();

        modelFormazione = getModelFormazione();

        LocalDateTime now = LocalDateTime.now();
        listPartiteGiocate = calendarioCompetizioneService.findByIdGiornataAndDataLessThanEqual(
                giornataInfo.getCodiceGiornata(), now);
        listPartite = calendarioCompetizioneService.findByIdGiornataOrderByDataAsc(giornataInfo.getCodiceGiornata());
        listSqualificatiInfortunati = giornataGiocatoreService.findByCustonm(giornataInfo, null);
    }

    private void initSlots() {
        slotModels.clear();
        slotTables.clear();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slotModels.add(new ArrayList<>());
        }
    }

    private List<FcGiocatore> getSlot(int index) {
        return slotModels.get(index);
    }

    private Grid<FcGiocatore> getSlotTable(int index) {
        return slotTables.get(index);
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
        if (index < slotTables.size()) {
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

    private boolean isStarterSlot(int index) {
        return index < STARTER_SLOTS;
    }

    private String getStatoGiocatoreBySlot(int index) {
        return isStarterSlot(index) ? "T" : "R";
    }

    private void initLayout() {
        absLayout = new AbsoluteLayout(WINWIDTH, WINHEIGHT);
        dialogTribuna = new Dialog();
        dialogPartite = new Dialog();

        UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
            int resX = event.getScreenWidth();
            int resY = event.getScreenHeight();
            log.info("resx {}", resX);
            log.info("resY {}", resY);

            absLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
            absLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);

            dialogTribuna.setWidth(WINWIDTH - 50 + "px");
            dialogTribuna.setHeight(WINHEIGHT - 150 + "px");

            dialogPartite.setWidth(WINWIDTH - 50 + "px");
            dialogPartite.setHeight(WINHEIGHT - 250 + "px");
        });

        UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> {
            int winWidth = e.getWidth();
            int winHeight = e.getHeight();
            log.info("winWidth {}", winWidth);
            log.info("winHeight {}", winHeight);

            if (Math.max(winWidth, winHeight) >= 800) {
                absLayout.setWidth(winWidth + "px");
                absLayout.setHeight(winHeight + "px");

                dialogTribuna.setWidth(winWidth - 50 + "px");
                dialogTribuna.setHeight(winHeight - 150 + "px");

                dialogPartite.setWidth(winWidth - 50 + "px");
                dialogPartite.setHeight(winHeight - 250 + "px");
            }
        });

        Button cancelButtonPartite = new Button("Chiudi", event -> dialogPartite.close());
        Button cancelButtonTribuna = new Button("Chiudi", event -> dialogTribuna.close());

        save = new Button("Salva");
        save.setIcon(VaadinIcon.DATABASE.create());
        save.addClickListener(this);

        rosa = new Button("Rosa");
        rosa.setIcon(VaadinIcon.PLUS.create());
        rosa.addClickListener(this);

        viewPartite = new Button(Utils.buildInfoGiornataMobile(giornataInfo));
        viewPartite.setIcon(VaadinIcon.CALENDAR_CLOCK.create());
        viewPartite.addClickListener(this);

        checkMail = new ToggleButton();
        checkMail.setLabel("Email");
        checkMail.setValue(true);

        comboModulo = new ComboBox<>();
        comboModulo.setItems(Costants.SCHEMI);
        comboModulo.getElement().setAttribute("theme", "small");
        comboModulo.setClearButtonVisible(true);
        comboModulo.setPlaceholder("Modulo");
        comboModulo.addValueChangeListener(evt -> {
            String modulo = evt.getValue();
            renderModulo(modulo);

            String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
            if ("true".equals(activeCheckFormazione) && modulo != null) {
                try {
                    impostaGiocatoriConVoto(modulo);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });

        tableFormazione = getTableFormazione(modelFormazione);

        VerticalLayout mainLayoutTribuna = new VerticalLayout();
        mainLayoutTribuna.setMargin(false);
        mainLayoutTribuna.setPadding(false);
        mainLayoutTribuna.setSpacing(false);
        mainLayoutTribuna.add(tableFormazione);
        mainLayoutTribuna.add(cancelButtonTribuna);
        mainLayoutTribuna.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, cancelButtonTribuna);
        dialogTribuna.add(mainLayoutTribuna);

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slotTables.add(getTableGiocatore(getSlot(i), i));
        }

        final VerticalLayout layoutPartite = new VerticalLayout();
        layoutPartite.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutPartite.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
        layoutPartite.setWidth(Costants.WIDTH_300);
        layoutPartite.setMargin(false);
        layoutPartite.setPadding(false);
        layoutPartite.setSpacing(false);

        HorizontalLayout cssLayout = new HorizontalLayout();
        Span lblInfo = new Span(Utils.buildInfoGiornata(giornataInfo));
        lblInfo.getStyle().set(Costants.FONT_SIZE, "14px");
        cssLayout.add(lblInfo);
        layoutPartite.add(cssLayout);

        HorizontalLayout cssLayout2 = new HorizontalLayout();
        Span lblInfo2 = new Span("Formazione entro: " + nextDate);
        lblInfo2.getStyle().set(Costants.FONT_SIZE, "12px");
        cssLayout2.add(lblInfo2);
        layoutPartite.add(cssLayout2);

        Grid<FcCalendarioCompetizione> tablePartite = getTablePartite(listPartite);

        VerticalLayout mainLayoutPartite = new VerticalLayout();
        mainLayoutPartite.setMargin(false);
        mainLayoutPartite.setPadding(false);
        mainLayoutPartite.setSpacing(false);
        mainLayoutPartite.add(layoutPartite);
        mainLayoutPartite.add(tablePartite);
        mainLayoutPartite.add(cancelButtonPartite);
        mainLayoutPartite.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, cancelButtonPartite);
        dialogPartite.add(mainLayoutPartite);

        final VerticalLayout layoutAvviso = new VerticalLayout();
        layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);
        layoutAvviso.setWidth("100px");
        layoutAvviso.setMargin(false);
        layoutAvviso.setPadding(false);
        layoutAvviso.setSpacing(false);
        layoutAvviso.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Image campo = Utils.buildImage("small-campo.jpg",
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "small-campo.jpg"));
        Image panchina = Utils.buildImage("small-panchina.png",
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "small-panchina.png"));

        absLayout.add(comboModulo, 0, 0);
        absLayout.add(rosa, 180, 0);
        absLayout.add(layoutAvviso, 290, 10);
        absLayout.add(campo, 0, 50);
        absLayout.add(save, 0, 550);
        absLayout.add(checkMail, 100, 560);
        absLayout.add(viewPartite, 200, 550);
        absLayout.add(panchina, 0, 600);

        Button home = new Button("Home");
        RouterLink menuHome = new RouterLink("", HomeView.class);
        menuHome.getElement().appendChild(home.getElement());
        absLayout.add(menuHome, 0, 700);

        add(absLayout);

        try {
            loadFcGiornataDett();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if (millisDiff == 0) {
            showMessageStopInsert();
        } else {
			Instant future = dateTime.atZone(ZoneId.of("UTC")).toInstant();
			layoutAvviso.add(new RelativeTime(future).setFormat(Format.DURATION));
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

        map.put(GK_STARTER_SLOT, new SlotPosition(PX_160, PX_P));

        map.put(11, new SlotPosition(PX_80, 600));
        map.put(12, new SlotPosition(PX_160, 600));
        map.put(13, new SlotPosition(PX_240, 600));
        map.put(14, new SlotPosition(PX_320, 600));
        map.put(15, new SlotPosition(PX_80, 700));
        map.put(16, new SlotPosition(PX_160, 700));
        map.put(17, new SlotPosition(PX_240, 700));

        switch (modulo) {
            case Costants.SCHEMA_541 -> {
                map.put(1, new SlotPosition(PX_0, PX_D));
                map.put(2, new SlotPosition(PX_80, PX_D));
                map.put(3, new SlotPosition(PX_160, PX_D));
                map.put(4, new SlotPosition(PX_240, PX_D));
                map.put(5, new SlotPosition(PX_320, PX_D));

                map.put(6, new SlotPosition(PX_20, PX_C));
                map.put(7, new SlotPosition(PX_110, PX_C));
                map.put(8, new SlotPosition(PX_200, PX_C));
                map.put(9, new SlotPosition(PX_290, PX_C));

                map.put(10, new SlotPosition(PX_160, PX_A));
            }
            case Costants.SCHEMA_532 -> {
                map.put(1, new SlotPosition(PX_0, PX_D));
                map.put(2, new SlotPosition(PX_80, PX_D));
                map.put(3, new SlotPosition(PX_160, PX_D));
                map.put(4, new SlotPosition(PX_240, PX_D));
                map.put(5, new SlotPosition(PX_320, PX_D));

                map.put(6, new SlotPosition(PX_70, PX_C));
                map.put(7, new SlotPosition(PX_160, PX_C));
                map.put(8, new SlotPosition(PX_250, PX_C));

                map.put(9, new SlotPosition(PX_110, PX_A));
                map.put(10, new SlotPosition(PX_200, PX_A));
            }
            case Costants.SCHEMA_451 -> {
                map.put(1, new SlotPosition(PX_20, PX_D));
                map.put(2, new SlotPosition(PX_110, PX_D));
                map.put(3, new SlotPosition(PX_200, PX_D));
                map.put(4, new SlotPosition(PX_290, PX_D));

                map.put(5, new SlotPosition(PX_0, PX_C));
                map.put(6, new SlotPosition(PX_80, PX_C));
                map.put(7, new SlotPosition(PX_160, PX_C));
                map.put(8, new SlotPosition(PX_240, PX_C));
                map.put(9, new SlotPosition(PX_320, PX_C));

                map.put(10, new SlotPosition(PX_160, PX_A));
            }
            case Costants.SCHEMA_442 -> {
                map.put(1, new SlotPosition(PX_20, PX_D));
                map.put(2, new SlotPosition(PX_110, PX_D));
                map.put(3, new SlotPosition(PX_200, PX_D));
                map.put(4, new SlotPosition(PX_290, PX_D));

                map.put(5, new SlotPosition(PX_20, PX_C));
                map.put(6, new SlotPosition(PX_110, PX_C));
                map.put(7, new SlotPosition(PX_200, PX_C));
                map.put(8, new SlotPosition(PX_290, PX_C));

                map.put(9, new SlotPosition(PX_110, PX_A));
                map.put(10, new SlotPosition(PX_200, PX_A));
            }
            case Costants.SCHEMA_433 -> {
                map.put(1, new SlotPosition(PX_20, PX_D));
                map.put(2, new SlotPosition(PX_110, PX_D));
                map.put(3, new SlotPosition(PX_200, PX_D));
                map.put(4, new SlotPosition(PX_290, PX_D));

                map.put(5, new SlotPosition(PX_70, PX_C));
                map.put(6, new SlotPosition(PX_160, PX_C));
                map.put(7, new SlotPosition(PX_250, PX_C));

                map.put(8, new SlotPosition(PX_70, PX_A));
                map.put(9, new SlotPosition(PX_160, PX_A));
                map.put(10, new SlotPosition(PX_250, PX_A));
            }
            case Costants.SCHEMA_352 -> {
                map.put(1, new SlotPosition(PX_70, PX_D));
                map.put(2, new SlotPosition(PX_160, PX_D));
                map.put(3, new SlotPosition(PX_250, PX_D));

                map.put(4, new SlotPosition(PX_0, PX_C));
                map.put(5, new SlotPosition(PX_80, PX_C));
                map.put(6, new SlotPosition(PX_160, PX_C));
                map.put(7, new SlotPosition(PX_240, PX_C));
                map.put(8, new SlotPosition(PX_320, PX_C));

                map.put(9, new SlotPosition(PX_110, PX_A));
                map.put(10, new SlotPosition(PX_200, PX_A));
            }
            case Costants.SCHEMA_343 -> {
                map.put(1, new SlotPosition(PX_70, PX_D));
                map.put(2, new SlotPosition(PX_160, PX_D));
                map.put(3, new SlotPosition(PX_250, PX_D));

                map.put(4, new SlotPosition(PX_20, PX_C));
                map.put(5, new SlotPosition(PX_110, PX_C));
                map.put(6, new SlotPosition(PX_200, PX_C));
                map.put(7, new SlotPosition(PX_290, PX_C));

                map.put(8, new SlotPosition(PX_70, PX_A));
                map.put(9, new SlotPosition(PX_160, PX_A));
                map.put(10, new SlotPosition(PX_250, PX_A));
            }
            default -> {
            }
        }

        return map;
    }

    private void showMessageStopInsert() {
        String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
        if ("true".equals(activeCheckFormazione)) {
            log.info("showMessageStopInsert");
            enabledComponent(false);
            CustomMessageDialog.showMessageInfo("Impossibile inserire la formazione, tempo scaduto!");
        }
    }

    private void enabledComponent(boolean enabled) {
        comboModulo.setEnabled(enabled);
        save.setEnabled(enabled);
        checkMail.setEnabled(enabled);
        for (Grid<FcGiocatore> grid : slotTables) {
            grid.setEnabled(enabled);
        }
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

        for (Grid<FcGiocatore> grid : slotTables) {
            absLayout.remove(grid);
        }
    }

    private ArrayList<FcGiocatore> getModelFormazione() {
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
        grid.setSizeUndefined();
        grid.setWidth(WIDTH);
        grid.setHeight(HEIGHT);

        Column<FcGiocatore> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(player -> {
            VerticalLayout cellLayout = new VerticalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setSizeUndefined();

            if (player != null) {
                String title = Utils.getInfoPlayer(player);

                String ruolo = player.getFcRuolo().getIdRuolo();
                if (Costants.P.equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_P);
                } else if (Costants.D.equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_D);
                } else if (Costants.C.equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_C);
                } else if (Costants.A.equals(ruolo)) {
                    cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_A);
                }

                FcGiornataGiocatore ggOut = isGiocatoreOut(player);
                if (ggOut != null) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                }

                HorizontalLayout cellLayoutImg = new HorizontalLayout();
                cellLayoutImg.setMargin(false);
                cellLayoutImg.setPadding(false);
                cellLayoutImg.setSpacing(false);
                cellLayoutImg.setSizeUndefined();

                Image imgR = Utils.buildImage(
                        player.getFcRuolo().getIdRuolo().toLowerCase() + ".png",
                        resourceLoader.getResource(
                                Costants.CLASSPATH_IMAGES + player.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
                imgR.setTitle(title);
                cellLayoutImg.add(imgR);

                FcSquadra sq = player.getFcSquadra();
                if (sq != null && sq.getImg() != null) {
                    try {
                        Image imgSq = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        imgSq.setTitle(title);
                        cellLayoutImg.add(imgSq);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }

                FcStatistiche s = player.getFcStatistiche();
                String imgThink = "2.png";
                if (s != null && s.getMediaVoto() != 0) {
                    if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
                        imgThink = "1.png";
                    } else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
                        imgThink = "3.png";
                    }
                }
                Image imgMv = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
                imgMv.setTitle(title);
                cellLayoutImg.add(imgMv);

                if (ggOut != null) {
                    cellLayoutImg.add(getImageGiocatoreOut(ggOut));
                }

                Span lblGiocatore = new Span(player.getCognGiocatore());
                lblGiocatore.getStyle().set(Costants.FONT_SIZE, "9px");
                lblGiocatore.setTitle(title);
                lblGiocatore.setWidth("60px");

                cellLayout.add(cellLayoutImg);
                try {
                    Image img = Utils.getImage(player.getNomeImg(), player.getImgSmall().getBinaryStream());
                    img.setTitle(title);
                    cellLayout.add(img);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                cellLayout.add(lblGiocatore);

                Element element = cellLayout.getElement();
                element.addEventListener("click", e -> {
                    if (isGiocatorePartitaGiocata(player)) {
                        CustomMessageDialog.showMessageError("Impossibile muovere il giocatore!");
                        return;
                    }

                    modelFormazione.add(player);
                    refreshAndSortGridFormazione();
                    clearSlot(slotIndex);
                });
            }
            return cellLayout;
        }));

        giocatoreColumn.setSortable(false);
        giocatoreColumn.setResizable(false);
        giocatoreColumn.setWidth("80px");

        return grid;
    }

    private Grid<FcGiocatore> getTableFormazione(List<FcGiocatore> items) {
        Grid<FcGiocatore> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth(Costants.WIDTH_300);

        Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (g != null) {
                String title = Utils.getInfoPlayer(g);
                if (g.getFcRuolo() != null) {
                    Image img = Utils.buildImage(
                            g.getFcRuolo().getIdRuolo().toLowerCase() + ".png",
                            resourceLoader.getResource(
                                    Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
                    img.setTitle(title);
                    cellLayout.add(img);
                }
                if (g.getCognGiocatore() != null) {
                    Span lblGiocatore = new Span();
                    lblGiocatore.setTitle(title);
                    lblGiocatore.setText(g.getCognGiocatore());
                    cellLayout.add(lblGiocatore);
                }

                FcGiornataGiocatore gg = isGiocatoreOut(g);
                if (gg != null) {
                    cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                    cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                    cellLayout.add(getImageGiocatoreOut(gg));
                }
            }
            return cellLayout;
        }));
        cognGiocatoreColumn.setSortable(false);
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setWidth("160px");

        Column<FcGiocatore> infoPercColumn = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (g != null) {
                String title = Utils.getInfoPlayer(g);
                int percentuale = g.getPercentuale() == null ? 0 : g.getPercentuale();
                double value = percentuale / 100.0;

                ProgressBar progressBarPercentuale = new ProgressBar();
                progressBarPercentuale.setValue(value);

                Span lblPercentuale = new Span();
                lblPercentuale.setText(percentuale + "%");
                lblPercentuale.setTitle(title);

                if (percentuale > 60) {
                    progressBarPercentuale.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
                    lblPercentuale.addClassNames(LumoUtility.TextColor.SUCCESS);
                } else if (percentuale > 39) {
                    progressBarPercentuale.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                    lblPercentuale.addClassNames(LumoUtility.TextColor.ERROR);
                } else {
                    progressBarPercentuale.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
                    lblPercentuale.addClassNames(LumoUtility.TextColor.DISABLED);
                }

                cellLayout.add(progressBarPercentuale);
                cellLayout.add(lblPercentuale);
            }
            return cellLayout;
        }));
        infoPercColumn.setSortable(false);
        infoPercColumn.setHeader("");
        infoPercColumn.setWidth("135px");

        grid.addItemClickListener(event -> {
            String valModulo = comboModulo.getValue();
            if (valModulo == null) {
                log.info("valModulo null");
                return;
            }

            FcGiocatore bean = event.getItem();
            if (bean == null) {
                return;
            }

            if (isGiocatorePartitaGiocata(bean)) {
                CustomMessageDialog.showMessageError("Impossibile muovere il giocatore!");
                return;
            }

            if (existGiocatore(bean)) {
                log.info("existGiocatore true");
                return;
            }

            boolean assigned = assignPlayerToSlotByModulo(bean, valModulo);
            if (assigned) {
                modelFormazione.remove(bean);
                refreshAndSortGridFormazione();
            }
        });

        return grid;
    }

    private boolean assignPlayerToSlotByModulo(FcGiocatore bean, String modulo) {
        ModuleConfig config = MODULE_CONFIGS.get(modulo);
        if (config == null) {
            return false;
        }

        String ruolo = bean.getFcRuolo().getIdRuolo();
        return switch (ruolo) {
            case Costants.P -> assignToFirstEmpty(bean, List.of(GK_STARTER_SLOT, GK_BENCH_SLOT));
            case Costants.D -> assignToFirstEmpty(bean, merge(config.difensori(), DEF_BENCH_SLOTS));
            case Costants.C -> assignToFirstEmpty(bean, merge(config.centrocampisti(), MID_BENCH_SLOTS));
            case Costants.A -> assignToFirstEmpty(bean, merge(config.attaccanti(), ATT_BENCH_SLOTS));
            default -> false;
        };
    }

    private boolean assignToFirstEmpty(FcGiocatore bean, List<Integer> slotIndexes) {
        for (Integer slotIndex : slotIndexes) {
            if (isSlotEmpty(slotIndex)) {
                setPlayerInSlot(slotIndex, bean);
                return true;
            }
        }
        return false;
    }

    private List<Integer> merge(List<Integer> first, List<Integer> second) {
        List<Integer> merged = new ArrayList<>(first);
        merged.addAll(second);
        return merged;
    }

    private boolean existGiocatore(FcGiocatore g) {
        for (List<FcGiocatore> slot : slotModels) {
            if (slot.contains(g)) {
                return true;
            }
        }
        return false;
    }

    private void loadFcGiornataDett() {
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
                    case Costants.D -> countD++;
                    case Costants.C -> countC++;
                    case Costants.A -> countA++;
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
        if (event.getSource() == rosa) {
            dialogTribuna.open();
        } else if (event.getSource() == viewPartite) {
            dialogPartite.open();
        } else if (event.getSource() == save) {
            if (check()) {
                int giornataSerieA = giornataInfo.getCodiceGiornata();
                String descGiornata = giornataInfo.getDescGiornataFc();

                try {
                    insert(giornataSerieA);
                } catch (Exception exi) {
                    CustomMessageDialog.showMessageErrorDetails(
                            CustomMessageDialog.MSG_ERROR_GENERIC, exi.getMessage());
                    return;
                }

                if (Boolean.TRUE.equals(checkMail.getValue())) {
                    try {
                        String dataOra = getSysdate();
                        sendNewMail(descGiornata);
                        log.info("send_mail OK");

                        try {
                            insertDettInfo(giornataSerieA, dataOra);
                            log.info("insert_dett_info OK");
                        } catch (Exception exd) {
                            log.error(exd.getMessage());
                            CustomMessageDialog.showMessageErrorDetails(
                                    CustomMessageDialog.MSG_ERROR_GENERIC, exd.getMessage());
                        }

                        CustomMessageDialog.showMessageInfo("Formazione inserita, email inviata con successo!");
                    } catch (Exception exception) {
                        CustomMessageDialog.showMessageErrorDetails(
                                CustomMessageDialog.MSG_MAIL_KO, exception.getMessage());
                    }
                } else {
                    CustomMessageDialog.showMessageInfo(
                            "Formazione salvata con successo! Per rendere effettiva la formazione, inviare email.");
                }
            }
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
                    "DELETE FROM fc_giornata_dett WHERE ID_GIORNATA = ? AND ID_ATTORE = ?",
                    giornata,
                    Integer.parseInt(idAttore)
            );

            int ord = 1;
            for (int i = 0; i < TOTAL_SLOTS; i++) {
                FcGiocatore bean = getPlayerInSlot(i);
                jdbcTemplate.update(
                        """
                        INSERT INTO fc_giornata_dett
                        (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO)
                        VALUES (?, ?, ?, ?, ?, 0)
                        """,
                        giornata,
                        Integer.parseInt(idAttore),
                        bean.getIdGiocatore(),
                        getStatoGiocatoreBySlot(i),
                        ord++
                );
            }

            for (FcGiocatore bean : modelFormazione) {
                jdbcTemplate.update(
                        """
                        INSERT INTO fc_giornata_dett
                        (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO)
                        VALUES (?, ?, ?, ?, ?, 0)
                        """,
                        giornata,
                        Integer.parseInt(idAttore),
                        bean.getIdGiocatore(),
                        "N",
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
                    "DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA = ? AND ID_ATTORE = ?",
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
            CustomMessageDialog.showMessageError("insert_dett_info " + e.getMessage());
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

        int ord = 1;
        Map<String, InputStream> listImg = new HashMap<>();

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            FcGiocatore bean = getPlayerInSlot(i);
            appendMailRow(formazioneHtml, listImg, bean, String.valueOf(ord), isStarterSlot(i) ? "Titolare" : "Riserva");
            ord++;
        }

        for (FcGiocatore bean : modelFormazione) {
            appendMailRow(formazioneHtml, listImg, bean, String.valueOf(ord), "Non Convocato");
            ord++;
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
            emailDestinatario.append(p.getProperty("to"));
        }

        String[] to = null;
        if (StringUtils.isNotEmpty(emailDestinatario.toString())) {
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

    private void appendMailRow(StringBuilder html, Map<String, InputStream> listImg,
                               FcGiocatore bean, String ordinamento, String stato) {
        String nomeGiocatore = bean.getCognGiocatore();
        String ruolo = bean.getFcRuolo().getDescRuolo();
        String squadra = bean.getFcSquadra().getNomeSquadra();

        String cidNomeSq = ContentIdGenerator.getContentId();
        FcSquadra sq = bean.getFcSquadra();
        if (sq.getImg() != null) {
            try {
                listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        String cidNomeImg = ContentIdGenerator.getContentId();
        try {
            listImg.put(cidNomeImg, bean.getImg().getBinaryStream());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        String color = "BGCOLOR=\"" + Costants.BG_N + "\"";
        int ord = Integer.parseInt(ordinamento);
        if (ord >= 1 && ord <= 11) {
            color = "BGCOLOR=\"" + Costants.BG_T + "\"";
        } else if (ord >= 12 && ord <= 18) {
            color = "BGCOLOR=\"" + Costants.BG_R + "\"";
        }

        html.append("<tr ").append(color).append(">");
        html.append("<td>").append(ordinamento).append("</td>");
        html.append("<td><img src=\"cid:").append(cidNomeImg).append("\" />").append(nomeGiocatore).append("</td>");
        html.append("<td>").append(ruolo).append("</td>");
        html.append("<td><img src=\"cid:").append(cidNomeSq).append("\" />").append(squadra).append("</td>");
        html.append("<td>").append(stato).append("</td>");
        html.append("</tr>");
    }

//    private FcGiocatore getFcGiocatore(int i) {
//        return getPlayerInSlot(i);
//    }

    private Grid<FcCalendarioCompetizione> getTablePartite(List<FcCalendarioCompetizione> listPartite) {
        Grid<FcCalendarioCompetizione> grid = new Grid<>();
        grid.setItems(listPartite);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.setWidth(Costants.WIDTH_300);

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
                    } catch (Exception e) {
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
                FcSquadra sq = squadraService.findByNomeSquadra(s.getSquadraFuori());
                if (sq != null && sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                cellLayout.add(new Span(s.getSquadraFuori().substring(0, 3)));
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

    private boolean isGiocatorePartitaGiocata(FcGiocatore giocatore) {
        String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
        if ("true".equals(activeCheckFormazione)) {
            String squadra = giocatore.getFcSquadra().getNomeSquadra();
            for (FcCalendarioCompetizione partita : listPartiteGiocate) {
                if (squadra.equals(partita.getSquadraCasa()) || squadra.equals(partita.getSquadraFuori())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void impostaGiocatoriConVoto(String modulo) {
        if (listPartiteGiocate == null || listPartiteGiocate.isEmpty()) {
            return;
        }

        enabledComponent(true);

        List<FcGiornataDett> lGiocatori =
                giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

        modelFormazione.clear();
        refreshAndSortGridFormazione();
        clearAllSlots();

        List<FcGiocatore> giocatoriD = new ArrayList<>();
        List<FcGiocatore> giocatoriC = new ArrayList<>();
        List<FcGiocatore> giocatoriA = new ArrayList<>();

        for (FcGiornataDett gd : lGiocatori) {
            FcGiocatore bean = gd.getFcGiocatore();

            if (gd.getOrdinamento() > TOTAL_SLOTS || !isGiocatorePartitaGiocata(bean)) {
                modelFormazione.add(bean);
                refreshAndSortGridFormazione();
                continue;
            }

            int ord = gd.getOrdinamento();
            if (ord == 1) {
                setPlayerInSlot(GK_STARTER_SLOT, bean);
            } else if (ord >= 2 && ord <= 11) {
                addPlayerToRoleBucket(bean, giocatoriD, giocatoriC, giocatoriA);
            } else if (ord >= 12 && ord <= 18) {
                setPlayerInSlot(ord - 1, bean);
            }
        }

        ModuleConfig config = MODULE_CONFIGS.get(modulo);
        if (config == null) {
            return;
        }

        assignPlayersToSlotsOrFail(giocatoriD, config.difensori());
        assignPlayersToSlotsOrFail(giocatoriC, config.centrocampisti());
        assignPlayersToSlotsOrFail(giocatoriA, config.attaccanti());
    }

    private void addPlayerToRoleBucket(
            FcGiocatore bean,
            List<FcGiocatore> giocatoriD,
            List<FcGiocatore> giocatoriC,
            List<FcGiocatore> giocatoriA) {
        switch (bean.getFcRuolo().getIdRuolo()) {
            case Costants.D -> giocatoriD.add(bean);
            case Costants.C -> giocatoriC.add(bean);
            case Costants.A -> giocatoriA.add(bean);
            default -> { }
        }
    }

    private void assignPlayersToSlotsOrFail(List<FcGiocatore> players, List<Integer> slotIndexes) {
        for (int i = 0; i < players.size(); i++) {
            FcGiocatore g = players.get(i);
            if (i < slotIndexes.size()) {
                setPlayerInSlot(slotIndexes.get(i), g);
            } else {
                showMessageErrorChangeModulo(g);
            }
        }
    }

    private void showMessageErrorChangeModulo(FcGiocatore g) {
        enabledComponent(false);
        modelFormazione.add(g);
        refreshAndSortGridFormazione();
        CustomMessageDialog.showMessageError(
                "Cambio modulo incorretto! Impossibile muovere il giocatore " + g.getCognGiocatore());
    }

    private FcGiornataGiocatore isGiocatoreOut(FcGiocatore giocatore) {
        for (FcGiornataGiocatore gg : listSqualificatiInfortunati) {
            if (gg.getFcGiocatore().getIdGiocatore() == giocatore.getIdGiocatore()) {
                return gg;
            }
        }
        return null;
    }

    private Image getImageGiocatoreOut(FcGiornataGiocatore gg) {
        Image img = null;
        if (gg != null) {
            if (gg.isInfortunato()) {
                if (gg.getNote().contains("INCERTO")) {
                    img = Utils.buildImage("help.png",
                            resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "icons/16/help.png"));
                } else {
                    img = Utils.buildImage("ospedale_s.png",
                            resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "ospedale_s.png"));
                }
                img.setTitle(gg.getNote() != null ? gg.getNote() : "ND");
            } else if (gg.isSqualificato()) {
                img = Utils.buildImage("esp_s.png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "esp_s.png"));
                img.setTitle(gg.getNote() != null ? gg.getNote() : "ND");
            }
        }
        return img;
    }
}
