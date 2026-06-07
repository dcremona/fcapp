package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.klaudeta.PaginatedGrid;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataGiocatoreService;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Free Players")
@Route(value = "freePlayers")
@RolesAllowed("ADMIN")
public class FreePlayersView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String TAB_PORTIERI = "Portieri";
    private static final String TAB_DIFENSORI = "Difensori";
    private static final String TAB_CENTROCAMPISTI = "Centrocampisti";
    private static final String TAB_ATTACCANTI = "Attaccanti";
    private static final String ALL = "All";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ResourceLoader resourceLoader;
    private final GiocatoreService giocatoreService;
    private final FormazioneService formazioneService;
    private final AccessoService accessoService;
    private final GiornataGiocatoreService giornataGiocatoreService;

    private RadioButtonGroup<String> radioGroup;
    private TabSheet tabs;

    private PaginatedGrid<FcGiocatore, ?> gridP;
    private PaginatedGrid<FcGiocatore, ?> gridD;
    private PaginatedGrid<FcGiocatore, ?> gridC;
    private PaginatedGrid<FcGiocatore, ?> gridA;

    private List<FcGiornataGiocatore> listSqualificatiInfortunati = new ArrayList<>();

    public FreePlayersView(
            ResourceLoader resourceLoader,
            GiocatoreService giocatoreService,
            FormazioneService formazioneService,
            AccessoService accessoService,
            GiornataGiocatoreService giornataGiocatoreService) {
        this.resourceLoader = resourceLoader;
        this.giocatoreService = giocatoreService;
        this.formazioneService = formazioneService;
        this.accessoService = accessoService;
        this.giornataGiocatoreService = giornataGiocatoreService;
        log.info("FreePlayersView");
    }

    @PostConstruct
    void init() {
        log.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initData();
        initLayout();
    }

    private void initData() {
        FcGiornataInfo giornataInfo = getSessionAttribute("GIORNATA_INFO", FcGiornataInfo.class);
        if (giornataInfo != null) {
            listSqualificatiInfortunati = giornataGiocatoreService.findByCustonm(giornataInfo, null);
        }
    }

    private void initLayout() {
        add(buildToolbar());

        VerticalLayout layoutP = createRoleLayout(Costants.P, "Export to Excel P");
        VerticalLayout layoutD = createRoleLayout(Costants.D, "Export to Excel D");
        VerticalLayout layoutC = createRoleLayout(Costants.C, "Export to Excel C");
        VerticalLayout layoutA = createRoleLayout(Costants.A, "Export to Excel A");

        tabs = new TabSheet();
        tabs.add(TAB_PORTIERI, layoutP);
        tabs.add(TAB_DIFENSORI, layoutD);
        tabs.add(TAB_CENTROCAMPISTI, layoutC);
        tabs.add(TAB_ATTACCANTI, layoutA);

        add(tabs);
    }

    private HorizontalLayout buildToolbar() {
        Button homeButton = new Button("Home");
        RouterLink homeLink = new RouterLink("", HomeView.class);
        homeLink.getElement().appendChild(homeButton.getElement());

        Button mercatoButton = new Button("Mercato");
        RouterLink mercatoLink = new RouterLink("", MercatoView.class);
        mercatoLink.getElement().appendChild(mercatoButton.getElement());

        Button loadButton = new Button("Aggiorna");
        loadButton.addClickListener(this);

        radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Tipo Aggiornamento");
        radioGroup.setItems(ALL, Costants.RUOLO);
        radioGroup.setValue(ALL);

        HorizontalLayout layout = new HorizontalLayout(homeLink, mercatoLink, loadButton, radioGroup);
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layout.setSpacing(true);

        return layout;
    }

    private VerticalLayout createRoleLayout(String ruolo, String exportLabel) {
        PaginatedGrid<FcGiocatore, ?> grid = getTableGiocatore(getModelAsta(ruolo));
        assignGridByRole(ruolo, grid);

        GridExporter<FcGiocatore> exporter = createExporter(grid, ruolo);
        Anchor excelLink = new Anchor("", exportLabel);
        excelLink.setHref(exporter.getExcelStreamResource());
        excelLink.getElement().setAttribute("download", true);

        VerticalLayout layout = new VerticalLayout();
        layout.add(new HorizontalLayout(excelLink), grid);
        return layout;
    }

    private GridExporter<FcGiocatore> createExporter(Grid<FcGiocatore> grid, String ruolo) {
        GridExporter<FcGiocatore> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle(ruolo);
        exporter.setFileName(ruolo + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
        return exporter;
    }

    private void assignGridByRole(String ruolo, PaginatedGrid<FcGiocatore, ?> grid) {
        switch (ruolo) {
            case Costants.P -> gridP = grid;
            case Costants.D -> gridD = grid;
            case Costants.C -> gridC = grid;
            case Costants.A -> gridA = grid;
            default -> throw new IllegalArgumentException("Ruolo non gestito: " + ruolo);
        }
    }

    private List<FcGiocatore> getModelAsta(String ruolo) {
        log.info("START getModelAsta {}", ruolo);

        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        List<Integer> excludedPlayerIds = getOccupiedPlayerIds(campionato);

        FcRuolo fcRuolo = new FcRuolo();
        fcRuolo.setIdRuolo(ruolo);

        List<FcGiocatore> players = excludedPlayerIds.isEmpty()
                ? giocatoreService.findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(fcRuolo, true)
                : giocatoreService.findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(fcRuolo, true, excludedPlayerIds);

        players.forEach(this::ensureStatistiche);

        log.info("END getModelAsta {}", ruolo);
        return players;
    }

    private List<Integer> getOccupiedPlayerIds(FcCampionato campionato) {
        List<FcFormazione> allFormazione = formazioneService.findByFcCampionato(campionato);
        List<Integer> ids = new ArrayList<>();

        for (FcFormazione formazione : allFormazione) {
            if (formazione.getFcGiocatore() != null) {
                ids.add(formazione.getFcGiocatore().getIdGiocatore());
            }
        }

        return ids;
    }

    private void ensureStatistiche(FcGiocatore giocatore) {
        if (giocatore.getFcStatistiche() != null) {
            return;
        }

        FcStatistiche statistiche = new FcStatistiche();
        statistiche.setMediaVoto(0d);
        statistiche.setFantaMedia(0d);
        giocatore.setFcStatistiche(statistiche);
    }

    private PaginatedGrid<FcGiocatore, ?> getTableGiocatore(List<FcGiocatore> items) {
        PaginatedGrid<FcGiocatore, ?> grid = new PaginatedGrid<>();
        grid.setDataProvider(new ListDataProvider<>(items));

        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setMultiSort(true);
        grid.setAllRowsVisible(true);

        addRuoloColumn(grid);
        addCognomeColumn(grid);
        addSquadraColumn(grid);
        addQuotazioneColumn(grid);
        addInfoColumn(grid);
        addPercentualeColumn(grid);
        addOutColumn(grid);
        addStatisticheColumns(grid);

        grid.setPageSize(25);
        grid.setPaginatorSize(5);

        return grid;
    }

    private void addRuoloColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout layout = createCompactCellLayout();
            if (g != null && g.getFcRuolo() != null) {
                String ruolo = g.getFcRuolo().getIdRuolo().toLowerCase();
                Image img = Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png"));
                layout.add(img);
            }
            return layout;
        }));

        column.setKey("fcRuolo.idRuolo");
        column.setHeader(Costants.R);
        column.setSortable(true);
        column.setAutoWidth(true);
    }

    private void addCognomeColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(g -> g != null ? g.getCognGiocatore() : "-");
        column.setKey("cognomeGiocatore");
        column.setHeader(Costants.GIOCATORE);
        column.setSortable(true);
        column.setAutoWidth(true);
    }

    private void addSquadraColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout layout = createCompactCellLayout();

            if (g == null || g.getFcSquadra() == null) {
                return layout;
            }

            FcSquadra squadra = g.getFcSquadra();
            if (squadra.getImg() != null) {
                try {
                    Image img = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                    layout.add(img);
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine squadra {}", squadra.getNomeSquadra(), e);
                }
            }

            layout.add(new Span(squadra.getNomeSquadra()));
            return layout;
        }));

        column.setKey("fcSquadra.nomeSquadra");
        column.setHeader(Costants.SQUADRA);
        column.setSortable(true);
        column.setComparator(Comparator.comparing(
                p -> p.getFcSquadra().getNomeSquadra(),
                Comparator.nullsLast(String::compareTo)));
        column.setAutoWidth(true);
    }

    private void addQuotazioneColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(g -> g != null ? g.getQuotazione() : 0);
        column.setKey("quotazione");
        column.setHeader(Costants.QUOTAZIONE);
        column.setSortable(true);
        column.setAutoWidth(true);
    }

    private void addInfoColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(g -> g != null ? g.getNomeGiocatore() : "-");
        column.setKey("nomeGiocatore");
        column.setHeader(Costants.INFO);
        column.setSortable(true);
        column.setAutoWidth(true);
    }

    private void addPercentualeColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout layout = createCompactCellLayout();

            if (g == null) {
                return layout;
            }

            String title = Utils.getInfoPlayer(g);
            int percentuale = Objects.requireNonNullElse(g.getPercentuale(), 0);

            ProgressBar progressBar = new ProgressBar();
            progressBar.setValue(percentuale / 100.0);

            Span label = new Span(percentuale + "%");
            label.setTitle(title);

            if (percentuale > 60) {
                progressBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
                label.addClassNames(LumoUtility.TextColor.SUCCESS);
            } else if (percentuale > 39) {
                progressBar.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
                label.addClassNames(LumoUtility.TextColor.ERROR);
            } else {
                progressBar.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
                label.addClassNames(LumoUtility.TextColor.DISABLED);
            }

            layout.add(progressBar, label);
            return layout;
        }));

        column.setHeader("");
        column.setSortable(false);
        column.setWidth("135px");
    }

    private void addOutColumn(PaginatedGrid<FcGiocatore, ?> grid) {
        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout layout = createCompactCellLayout();

            if (g == null) {
                return layout;
            }

            FcGiornataGiocatore giornataGiocatore = isGiocatoreOut(g);
            if (giornataGiocatore != null) {
                layout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
                layout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
                Image image = getImageGiocatoreOut(giornataGiocatore);
                if (image != null) {
                    layout.add(image);
                }
            }

            return layout;
        }));

        column.setHeader("");
        column.setSortable(false);
        column.setAutoWidth(true);
    }

    private void addStatisticheColumns(PaginatedGrid<FcGiocatore, ?> grid) {
        addNumberColumn(grid, Costants.GIOCATE, "fcStatistiche.giocate",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGiocate() : 0);

        addTrendScoreColumn(grid, Costants.MV, "fcStatistiche.mediaVoto",
                g -> g.getFcStatistiche().getMediaVoto());

        addTrendScoreColumn(grid, Costants.FMV, "fcStatistiche.fantaMedia",
                g -> g.getFcStatistiche().getFantaMedia());

        addNumberColumn(grid, Costants.ASSIST, "fcStatistiche.assist",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getAssist() : 0);

        addNumberColumn(grid, Costants.GF, "fcStatistiche.goalFatto",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGoalFatto() : 0);

        addNumberColumn(grid, Costants.GS, "fcStatistiche.goalSubito",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGoalSubito() : 0);

        addNumberColumn(grid, Costants.RS, "RS",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getRigoreSegnato() : 0);

        addNumberColumn(grid, Costants.AMM, "fcStatistiche.ammonizione",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getAmmonizione() : 0);

        addNumberColumn(grid, Costants.ESP, "fcStatistiche.espulsione",
                g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getEspulsione() : 0);
    }

    private void addNumberColumn(
            PaginatedGrid<FcGiocatore, ?> grid,
            String header,
            String key,
            Function<FcGiocatore, Integer> valueProvider) {

        Column<FcGiocatore> column = grid.addColumn(valueProvider::apply);
        column.setHeader(header);
        column.setKey(key);
        column.setSortable(true);
        column.setAutoWidth(true);
    }

    private void addTrendScoreColumn(
            PaginatedGrid<FcGiocatore, ?> grid,
            String header,
            String key,
            Function<FcGiocatore, Double> valueProvider) {

        Column<FcGiocatore> column = grid.addColumn(new ComponentRenderer<>(g -> {
            HorizontalLayout layout = createCompactCellLayout();
            FcStatistiche statistiche = g != null ? g.getFcStatistiche() : null;

            double value = statistiche != null ? valueProvider.apply(g) : 0d;
            String imageName = getTrendImageName(value);

            Image img = Utils.buildImage(
                    imageName,
                    resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));

            Span label = new Span(formatScore(value / Costants.DIVISORE_100));
            layout.add(img, label);
            return layout;
        }));

        column.setHeader(header);
        column.setKey(key);
        column.setSortable(true);
        column.setComparator(Comparator.comparing(
                p -> valueProvider.apply(p),
                Comparator.nullsLast(Double::compareTo)));
        column.setAutoWidth(true);
    }

    private String getTrendImageName(double value) {
        if (value == 0) {
            return "2.png";
        }
        if (value > Costants.RANGE_MAX_MV) {
            return "1.png";
        }
        if (value < Costants.RANGE_MIN_MV) {
            return "3.png";
        }
        return "2.png";
    }

    private String formatScore(double value) {
        return new DecimalFormat("#0.00").format(value);
    }

    private HorizontalLayout createCompactCellLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(Alignment.STRETCH);
        return layout;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            log.info("START AGGIORNA");

            if (Costants.RUOLO.equals(radioGroup.getValue())) {
                refreshSelectedTab();
            } else {
                refreshAllGrids();
            }

            log.info("END AGGIORNA");
        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void refreshSelectedTab() {
        String selectedTab = tabs.getSelectedTab().getLabel();
        log.info("selectedTab {}", selectedTab);

        switch (selectedTab) {
            case TAB_PORTIERI -> refreshGrid(gridP, Costants.P);
            case TAB_DIFENSORI -> refreshGrid(gridD, Costants.D);
            case TAB_CENTROCAMPISTI -> refreshGrid(gridC, Costants.C);
            case TAB_ATTACCANTI -> refreshGrid(gridA, Costants.A);
            default -> log.warn("Tab non gestita: {}", selectedTab);
        }
    }

    private void refreshAllGrids() {
        refreshGrid(gridP, Costants.P);
        refreshGrid(gridD, Costants.D);
        refreshGrid(gridC, Costants.C);
        refreshGrid(gridA, Costants.A);
    }

    private void refreshGrid(Grid<FcGiocatore> grid, String ruolo) {
        grid.setItems(getModelAsta(ruolo));
        grid.getDataProvider().refreshAll();
    }

    private FcGiornataGiocatore isGiocatoreOut(FcGiocatore giocatore) {
        if (giocatore == null) {
            return null;
        }

        for (FcGiornataGiocatore giornataGiocatore : listSqualificatiInfortunati) {
            if (giornataGiocatore.getFcGiocatore() != null
                    && Objects.equals(
                            giornataGiocatore.getFcGiocatore().getIdGiocatore(),
                            giocatore.getIdGiocatore())) {
                return giornataGiocatore;
            }
        }
        return null;
    }

    private Image getImageGiocatoreOut(FcGiornataGiocatore giornataGiocatore) {
        if (giornataGiocatore == null) {
            return null;
        }

        if (giornataGiocatore.isInfortunato()) {
            if (giornataGiocatore.getNote() != null && giornataGiocatore.getNote().contains("INCERTO")) {
                Image img = Utils.buildImage(
                        "help.png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "icons/16/help.png"));
                img.setTitle(giornataGiocatore.getNote());
                return img;
            }

            Image img = Utils.buildImage(
                    "ospedale_s.png",
                    resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "ospedale_s.png"));
            img.setTitle(giornataGiocatore.getNote());
            return img;
        }

        if (giornataGiocatore.isSqualificato()) {
            Image img = Utils.buildImage(
                    "esp_s.png",
                    resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "esp_s.png"));
            img.setTitle(giornataGiocatore.getNote());
            return img;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}
