package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.klaudeta.PaginatedGrid;
import org.vaadin.olli.FileDownloadWrapper;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.GridBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.ClassificaBean;
import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.SquadraService;
import fcapp.backend.service.StatisticheService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Statistiche")
@Route(value = "statistiche", layout = MainLayout.class)
@RolesAllowed("USER")
public class StatisticheView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String GIOCATORI_TUTTI = "Tutti";
    private static final String GIOCATORI_ATTIVI = "Attivi";
    private static final String GIOCATORI_NON_ATTIVI = "Non Attivi";

    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private final transient JdbcTemplate jdbcTemplate;
    private final transient JobProcessGiornata jobProcessGiornata;
    private final transient ResourceLoader resourceLoader;
    private final transient ClassificaTotalePuntiService classificaTotalePuntiService;
    private final transient StatisticheService statisticheService;
    private final transient AttoreService attoreService;
    private final transient SquadraService squadraService;
    private final transient AccessoService accessoService;

    private List<FcAttore> squadreA = new ArrayList<>();
    private List<FcAttore> squadreB = new ArrayList<>();
    private List<FcAttore> proprietari = new ArrayList<>();
    private List<FcSquadra> squadreSerieA = new ArrayList<>();

    private ComboBox<FcAttore> comboAttoreA;
    private ComboBox<FcAttore> comboAttoreB;
    private ComboBox<String> comboPunti;

    private Button salvaStat;

    private ToggleButton toggleP;
    private ToggleButton toggleD;
    private ToggleButton toggleC;
    private ToggleButton toggleA;
    private ComboBox<FcSquadra> comboSquadreA;
    private NumberField txtQuotazione;
    private ToggleButton freePlayers;
    private ComboBox<FcAttore> comboProprietario;
    private RadioButtonGroup<String> radioGroup;

    private final VerticalLayout verticalLayoutGrafico = new VerticalLayout();

    public StatisticheView(
            JdbcTemplate jdbcTemplate,
            JobProcessGiornata jobProcessGiornata,
            ResourceLoader resourceLoader,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            StatisticheService statisticheService,
            AttoreService attoreService,
            SquadraService squadraService,
            AccessoService accessoService) {
        log.info("StatisticheView()");
        this.jdbcTemplate = jdbcTemplate;
        this.jobProcessGiornata = jobProcessGiornata;
        this.resourceLoader = resourceLoader;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
        this.statisticheService = statisticheService;
        this.attoreService = attoreService;
        this.squadraService = squadraService;
        this.accessoService = accessoService;
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
        squadreA = attoreService.findByActive(true);
        squadreB = new ArrayList<>(squadreA);
        proprietari = new ArrayList<>(squadreA);
        squadreSerieA = squadraService.findAll();
    }

    private void initLayout() {
        FcCampionato campionato = getCurrentCampionato();
        FcAttore attore = getCurrentAttore();

        VerticalLayout layoutStat = new VerticalLayout();
        setStatisticheA(layoutStat, campionato, attore);

        VerticalLayout layoutConfronti = new VerticalLayout();
        setConfronti(layoutConfronti, campionato, attore);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Statistiche", layoutStat);
        tabSheet.add("Confronti", layoutConfronti);
        tabSheet.setSizeFull();

        add(tabSheet);
    }

    private FcCampionato getCurrentCampionato() {
        return (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
    }

    private FcAttore getCurrentAttore() {
        return (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
    }

    private void setConfronti(VerticalLayout layout, FcCampionato campionato, FcAttore attore) {
        HorizontalLayout filtersLayout = new HorizontalLayout();
        filtersLayout.setSpacing(true);

        comboAttoreA = buildAttoreCombo(attore);
        comboAttoreB = buildAttoreCombo(attore);

        comboPunti = new ComboBox<>();
        comboPunti.setItems(Costants.PUNTI, Costants.TOTALE_PUNTI, Costants.PT_TVST);
        comboPunti.setValue(Costants.PUNTI);
        comboPunti.setPlaceholder(Costants.CLASSIFICA_PER);
        comboPunti.addValueChangeListener(event -> refreshGrafico(campionato));

        filtersLayout.add(comboAttoreA, comboAttoreB, comboPunti);
        layout.add(filtersLayout);

        verticalLayoutGrafico.setSizeFull();
        refreshGrafico(campionato);
        layout.add(verticalLayoutGrafico);
    }

    private ComboBox<FcAttore> buildAttoreCombo(FcAttore defaultValue) {
        ComboBox<FcAttore> combo = new ComboBox<>();
        combo.setItems(squadreA);
        combo.setItemLabelGenerator(FcAttore::getDescAttore);
        combo.setValue(defaultValue);
        combo.setPlaceholder(Costants.SELEZIONA_ATTORE);
        combo.addValueChangeListener(event -> refreshGrafico(getCurrentCampionato()));
        return combo;
    }

    private void refreshGrafico(FcCampionato campionato) {
        verticalLayoutGrafico.removeAll();
        verticalLayoutGrafico.add(buildGrafico(campionato));
    }

    @SuppressWarnings("rawtypes")
    public Component buildGrafico(FcCampionato campionato) {
        FcAttore attoreA = comboAttoreA.getValue();
        FcAttore attoreB = comboAttoreB.getValue();
        String tipoPunti = comboPunti.getValue();

        if (attoreA == null || attoreB == null || tipoPunti == null) {
            return new Span("Seleziona i parametri per visualizzare il grafico");
        }

        List<ClassificaBean> items = classificaTotalePuntiService.getModelGrafico(
                String.valueOf(attoreA.getIdAttore()),
                String.valueOf(attoreB.getIdAttore()),
                campionato);

        List<String> giornate = new ArrayList<>();
        List<Double> dataA = new ArrayList<>();
        List<Double> dataB = new ArrayList<>();

        for (ClassificaBean item : items) {
            giornate.add(item.getGiornata());

            if (attoreA.getDescAttore().equals(item.getSquadra())) {
                dataA.add(extractPoints(item, tipoPunti));
            } else if (attoreB.getDescAttore().equals(item.getSquadra())) {
                dataB.add(extractPoints(item, tipoPunti));
            }
        }

        Series primaSerie = new Series<>(attoreA.getDescAttore(), dataA.toArray());
        Series secondaSerie = new Series<>(attoreB.getDescAttore(), dataB.toArray());

        ApexCharts lineChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(Costants.CLASSIFICA_PER + " " + tipoPunti)
                        .withAlign(Align.LEFT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5)
                                .build())
                        .build())
                .withXaxis(XAxisBuilder.get().withCategories(giornate).build())
                .withSeries(primaSerie, secondaSerie)
                .build();

        lineChart.setHeight("400px");
        lineChart.setWidth("70%");
        return lineChart;
    }

    private double extractPoints(ClassificaBean item, String tipoPunti) {
        return switch (tipoPunti) {
            case Costants.PUNTI -> item.getPunti();
            case Costants.TOTALE_PUNTI -> item.getTotPunti();
            case Costants.PT_TVST -> item.getPtTvst();
            default -> 0d;
        };
    }

    private void setStatisticheA(VerticalLayout layout, FcCampionato campionato, FcAttore attore) {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);

        addPdfDownloadButton(
                actionsLayout,
                "Statistiche Voti pdf",
                "StatisticheVoti.pdf",
                "classpath:reports/statisticheVoti.jasper",
                campionato);

        addPdfDownloadButton(
                actionsLayout,
                "Statistiche Voti Free Players pdf",
                "StatisticheVotiFreePlayers.pdf",
                "classpath:reports/statisticheVotiFreePlayers.jasper",
                campionato);

        if (isAdmin(attore)) {
            salvaStat = new Button("Aggiorna Statistiche");
            salvaStat.setIcon(VaadinIcon.DATABASE.create());
            salvaStat.addClickListener(this);
            actionsLayout.add(salvaStat);
        }

        layout.add(actionsLayout);

        HorizontalLayout filterLayout = buildFilterLayout();
        layout.add(filterLayout);

        List<FcStatistiche> items = statisticheService.findAll();
        PaginatedGrid<FcStatistiche, ?> grid = buildGrid(items);

        layout.add(grid);
    }

    private void addPdfDownloadButton(
            HorizontalLayout layout,
            String buttonText,
            String fileName,
            String reportPath,
            FcCampionato campionato) {
        try {
            Button button = new Button(buttonText);
            FileDownloadWrapper wrapper = createPdfWrapper(fileName, reportPath, campionato);
            wrapper.wrapComponent(button);
            layout.add(wrapper);
        } catch (Exception e) {
            log.error("Errore nella creazione del download PDF {}: {}", fileName, e.getMessage(), e);
        }
    }

    private FileDownloadWrapper createPdfWrapper(String fileName, String reportPath, FcCampionato campionato) throws Exception {
        if (jdbcTemplate.getDataSource() == null) {
            throw new IllegalStateException("DataSource non disponibile");
        }

        Connection conn = jdbcTemplate.getDataSource().getConnection();
        Map<String, Object> params = new HashMap<>();
        params.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
        params.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));

        Resource resource = resourceLoader.getResource(reportPath);

        return new FileDownloadWrapper(
                Utils.getStreamResource(fileName, conn, params, resource.getInputStream()));
    }

    private boolean isAdmin(FcAttore attore) {
        return attore != null && attore.getRoles().stream().anyMatch(Role.ADMIN::equals);
    }

    private HorizontalLayout buildFilterLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        toggleP = buildRoleToggle(Costants.P, true);
        toggleD = buildRoleToggle(Costants.D, true);
        toggleC = buildRoleToggle(Costants.C, true);
        toggleA = buildRoleToggle(Costants.A, true);

        comboSquadreA = buildSquadraCombo();
        txtQuotazione = buildQuotazioneField();
        freePlayers = buildRoleToggle("Free Players", false);
        comboProprietario = buildProprietarioCombo();
        radioGroup = buildGiocatoriRadioGroup();

        layout.add(
                toggleP,
                toggleD,
                toggleC,
                toggleA,
                comboSquadreA,
                txtQuotazione,
                freePlayers,
                comboProprietario,
                radioGroup);

        return layout;
    }

    private ToggleButton buildRoleToggle(String label, boolean defaultValue) {
        ToggleButton toggle = new ToggleButton();
        toggle.setLabel(label);
        toggle.setValue(defaultValue);
        return toggle;
    }

    private ComboBox<FcSquadra> buildSquadraCombo() {
        ComboBox<FcSquadra> combo = new ComboBox<>(Costants.SQUADRA);
        combo.setItems(squadreSerieA);
        combo.setItemLabelGenerator(FcSquadra::getNomeSquadra);
        combo.setClearButtonVisible(true);
        combo.setPlaceholder(Costants.SQUADRA);
        combo.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();

            if (item != null && item.getImg() != null) {
                try {
                    Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
                    container.add(img);
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine squadra {}: {}", item.getNomeSquadra(), e.getMessage(), e);
                }
            }

            if (item != null) {
                container.add(new Span(item.getNomeSquadra()));
            }

            return container;
        }));
        return combo;
    }

    private NumberField buildQuotazioneField() {
        NumberField field = new NumberField("Quotazione <=");
        field.setMin(0d);
        field.setMax(500d);
        return field;
    }

    private ComboBox<FcAttore> buildProprietarioCombo() {
        ComboBox<FcAttore> combo = new ComboBox<>(Costants.PROPETARIO);
        combo.setItems(proprietari);
        combo.setItemLabelGenerator(FcAttore::getDescAttore);
        combo.setClearButtonVisible(true);
        combo.setPlaceholder(Costants.PROPETARIO);
        combo.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();
            if (item != null) {
                container.add(new Span(item.getDescAttore()));
            }
            return container;
        }));
        return combo;
    }

    private RadioButtonGroup<String> buildGiocatoriRadioGroup() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("Giocatori");
        group.setItems(GIOCATORI_TUTTI, GIOCATORI_ATTIVI, GIOCATORI_NON_ATTIVI);
        group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        group.setValue(GIOCATORI_TUTTI);
        return group;
    }

    private PaginatedGrid<FcStatistiche, ?> buildGrid(List<FcStatistiche> items) {
        PaginatedGrid<FcStatistiche, ?> grid = new PaginatedGrid<>();
        ListDataProvider<FcStatistiche> dataProvider = new ListDataProvider<>(items);

        grid.setDataProvider(dataProvider);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setMultiSort(true);
        grid.setAllRowsVisible(true);
        grid.setPageSize(25);
        grid.setPaginatorSize(5);

        bindFilters(dataProvider);
        configureGridColumns(grid);

        return grid;
    }

    private void bindFilters(ListDataProvider<FcStatistiche> dataProvider) {
        toggleP.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleD.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleC.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleA.addValueChangeListener(event -> applyFilter(dataProvider));
        comboSquadreA.addValueChangeListener(event -> applyFilter(dataProvider));
        txtQuotazione.addValueChangeListener(event -> applyFilter(dataProvider));
        freePlayers.addValueChangeListener(event -> applyFilter(dataProvider));
        comboProprietario.addValueChangeListener(event -> applyFilter(dataProvider));
        radioGroup.addValueChangeListener(event -> applyFilter(dataProvider));
    }

    private void configureGridColumns(PaginatedGrid<FcStatistiche, ?> grid) {
        addRuoloColumn(grid);
        addGiocatoreColumn(grid);
        addSquadraColumn(grid);
        addQuotazioneColumn(grid);
        addProprietarioColumn(grid);
        addGiocateColumn(grid);
        addMediaVotoColumn(grid);
        addFantaMediaColumn(grid);
        addSimpleColumn(grid, FcStatistiche::getGoalFatto, "golFatto", Costants.G + "+");
        addSimpleColumn(grid, FcStatistiche::getGoalSubito, "golSubito", Costants.G + "-");
        addSimpleColumn(grid, FcStatistiche::getAssist, "assist", Costants.ASSIST);
        addSimpleColumn(grid, FcStatistiche::getAmmonizione, "ammonizione", Costants.AMM);
        addSimpleColumn(grid, FcStatistiche::getEspulsione, "espulsione", Costants.ESP);
    }

    private void addRuoloColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        Column<FcStatistiche> column = grid.addColumn(new ComponentRenderer<>(stat -> {
            HorizontalLayout cellLayout = createCompactCellLayout();

            if (stat != null && stat.getIdRuolo() != null) {
                Checkbox check = new Checkbox();
                check.setValue(stat.isFlagAttivo());
                check.setEnabled(false);

                Image img = Utils.buildImage(
                        stat.getIdRuolo().toLowerCase() + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + stat.getIdRuolo().toLowerCase() + ".png"));

                cellLayout.add(check, img);
            }

            return cellLayout;
        }));

        column.setKey(Costants.RUOLO);
        column.setSortable(true);
        column.setHeader(Costants.R);
        column.setAutoWidth(true);
    }

    private void addGiocatoreColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        grid.addColumn(FcStatistiche::getCognGiocatore)
                .setKey(Costants.GIOCATORE)
                .setSortable(true)
                .setHeader(Costants.GIOCATORE)
                .setAutoWidth(true);
    }

    private void addSquadraColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        Column<FcStatistiche> column = grid.addColumn(new ComponentRenderer<>(stat -> {
            HorizontalLayout cellLayout = createCompactCellLayout();

            if (stat != null && stat.getNomeSquadra() != null) {
                Image img = Utils.buildImage(
                        stat.getNomeSquadra() + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMG_SQUADRE + stat.getNomeSquadra() + ".png"));
                Span label = new Span(stat.getNomeSquadra());
                cellLayout.add(img, label);
            }

            return cellLayout;
        }));

        column.setSortable(true);
        column.setComparator(Comparator.comparing(FcStatistiche::getNomeSquadra));
        column.setHeader(Costants.SQUADRA);
        column.setAutoWidth(true);
    }

    private void addQuotazioneColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        grid.addColumn(stat -> stat.getFcGiocatore() != null ? stat.getFcGiocatore().getQuotazione() : 0)
                .setSortable(true)
                .setHeader(Costants.Q)
                .setAutoWidth(true);
    }

    private void addProprietarioColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        grid.addColumn(FcStatistiche::getProprietario)
                .setKey(Costants.PROPETARIO)
                .setSortable(true)
                .setHeader(Costants.PROPETARIO)
                .setAutoWidth(true);
    }

    private void addGiocateColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        grid.addColumn(FcStatistiche::getGiocate)
                .setKey(Costants.GIOCATE)
                .setSortable(true)
                .setHeader(Costants.GIOCATE)
                .setAutoWidth(true);
    }

    private void addMediaVotoColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        Column<FcStatistiche> column = grid.addColumn(new ComponentRenderer<>(stat -> buildValueWithTrend(stat, true)));
        column.setSortable(true);
        column.setComparator(Comparator.comparing(FcStatistiche::getMediaVoto));
        column.setHeader(Costants.MV);
        column.setAutoWidth(true);
    }

    private void addFantaMediaColumn(PaginatedGrid<FcStatistiche, ?> grid) {
        Column<FcStatistiche> column = grid.addColumn(new ComponentRenderer<>(stat -> buildValueWithTrend(stat, false)));
        column.setSortable(true);
        column.setComparator(Comparator.comparing(FcStatistiche::getFantaMedia));
        column.setHeader(Costants.FMV);
        column.setAutoWidth(true);
    }

    private <T> void addSimpleColumn(
            PaginatedGrid<FcStatistiche, ?> grid,
            com.vaadin.flow.function.ValueProvider<FcStatistiche, T> valueProvider,
            String key,
            String header) {
        grid.addColumn(valueProvider)
                .setKey(key)
                .setSortable(true)
                .setHeader(header)
                .setAutoWidth(true);
    }

    private HorizontalLayout buildValueWithTrend(FcStatistiche stat, boolean mediaVoto) {
        HorizontalLayout cellLayout = createCompactCellLayout();

        if (stat == null || stat.getFcGiocatore() == null) {
            return cellLayout;
        }

        double value = mediaVoto ? stat.getMediaVoto() : stat.getFantaMedia();
        String trendIcon = getTrendIcon(value);

        Image img = Utils.buildImage(trendIcon, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + trendIcon));
        Span label = new Span(formatStatValue(value));

        cellLayout.add(img, label);
        return cellLayout;
    }

    private String getTrendIcon(double value) {
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

    private String formatStatValue(double value) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(value / Costants.DIVISORE_100);
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
            FcCampionato campionato = getCurrentCampionato();

            if (event.getSource() == salvaStat) {
                jobProcessGiornata.statistiche(campionato);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void applyFilter(ListDataProvider<FcStatistiche> dataProvider) {
        dataProvider.clearFilters();

        applyRoleFilter(dataProvider);
        applySquadraFilter(dataProvider);
        applyQuotazioneFilter(dataProvider);
        applyFreePlayersFilter(dataProvider);
        applyProprietarioFilter(dataProvider);
        applyAttivitaFilter(dataProvider);
    }

    private void applyRoleFilter(ListDataProvider<FcStatistiche> dataProvider) {
        Set<String> selectedRoles = getSelectedRoles();

        if (selectedRoles.isEmpty()) {
            dataProvider.addFilter(stat -> false);
            return;
        }

        dataProvider.addFilter(stat ->
                stat.getIdRuolo() != null && selectedRoles.contains(stat.getIdRuolo().toUpperCase()));
    }

    private Set<String> getSelectedRoles() {
        Set<String> roles = new java.util.HashSet<>();

        if (Boolean.TRUE.equals(toggleP.getValue())) {
            roles.add(Costants.P.toUpperCase());
        }
        if (Boolean.TRUE.equals(toggleD.getValue())) {
            roles.add(Costants.D.toUpperCase());
        }
        if (Boolean.TRUE.equals(toggleC.getValue())) {
            roles.add(Costants.C.toUpperCase());
        }
        if (Boolean.TRUE.equals(toggleA.getValue())) {
            roles.add(Costants.A.toUpperCase());
        }

        return roles;
    }

    private void applySquadraFilter(ListDataProvider<FcStatistiche> dataProvider) {
        if (comboSquadreA.getValue() != null) {
            String squadra = comboSquadreA.getValue().getNomeSquadra();
            dataProvider.addFilter(stat -> squadra.equals(stat.getNomeSquadra()));
        }
    }

    private void applyQuotazioneFilter(ListDataProvider<FcStatistiche> dataProvider) {
        if (txtQuotazione.getValue() != null) {
            int quotazioneMax = txtQuotazione.getValue().intValue();
            dataProvider.addFilter(stat ->
                    stat.getFcGiocatore() != null && stat.getFcGiocatore().getQuotazione() <= quotazioneMax);
        }
    }

    private void applyFreePlayersFilter(ListDataProvider<FcStatistiche> dataProvider) {
        if (Boolean.TRUE.equals(freePlayers.getValue())) {
            dataProvider.addFilter(stat -> StringUtils.isBlank(stat.getProprietario()));
        }
    }

    private void applyProprietarioFilter(ListDataProvider<FcStatistiche> dataProvider) {
        if (comboProprietario.getValue() != null) {
            String proprietario = comboProprietario.getValue().getDescAttore();
            dataProvider.addFilter(stat -> proprietario.equals(stat.getProprietario()));
        }
    }

    private void applyAttivitaFilter(ListDataProvider<FcStatistiche> dataProvider) {
        String selected = radioGroup.getValue();

        if (GIOCATORI_ATTIVI.equals(selected)) {
            dataProvider.addFilter(FcStatistiche::isFlagAttivo);
        } else if (GIOCATORI_NON_ATTIVI.equals(selected)) {
            dataProvider.addFilter(stat -> !stat.isFlagAttivo());
        }
    }
}
