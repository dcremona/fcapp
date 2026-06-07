package fcapp.ui.views.em;

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
import java.util.function.Function;

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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
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
import fcapp.backend.data.entity.FcGiocatore;
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
@Route(value = "statisticheEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmStatisticheView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String REPORT_STATISTICHE_VOTI = "classpath:reports/statisticheVoti.jasper";
    private static final String PUNTI_TOTALI = "TOTALE_PUNTI";
    private static final String TUTTI = "Tutti";
    private static final String ATTIVI = "Attivi";
    private static final String NON_ATTIVI = "Non Attivi";
    private static final Set<String> RUOLI_VALIDI = Set.of("P", "D", "C", "A");

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
    private ComboBox<FcAttore> comboAttoreA;
    private ComboBox<FcAttore> comboAttoreB;
    private ComboBox<String> comboPunti;

    private List<FcSquadra> squadre = new ArrayList<>();
    private Button salvaStat;

    private ToggleButton toggleP;
    private ToggleButton toggleD;
    private ToggleButton toggleC;
    private ToggleButton toggleA;

    private ComboBox<FcSquadra> comboNazione;
    private NumberField txtQuotazione;
    private RadioButtonGroup<String> radioGroup;

    private final VerticalLayout verticalLayoutGrafico = new VerticalLayout();

    public EmStatisticheView(
            JdbcTemplate jdbcTemplate,
            JobProcessGiornata jobProcessGiornata,
            ResourceLoader resourceLoader,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            StatisticheService statisticheService,
            AttoreService attoreService,
            SquadraService squadraService,
            AccessoService accessoService) {
        log.info("EmStatisticheView()");
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
        squadre = squadraService.findAll();
    }

    private void initLayout() {
        VaadinSession.getCurrent().getAttribute("PROPERTIES");

        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        FcAttore attore = getSessionAttribute("ATTORE", FcAttore.class);

        if (campionato == null || attore == null) {
            log.warn("Campionato o attore non presenti in sessione");
            return;
        }

        VerticalLayout layoutStat = new VerticalLayout();
        setStatistiche(layoutStat, campionato, attore);

        VerticalLayout layoutConfronti = new VerticalLayout();
        setConfronti(layoutConfronti, campionato, attore);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Statistiche", layoutStat);
        tabSheet.add("Confronti", layoutConfronti);

        add(tabSheet);
    }

    private void setConfronti(VerticalLayout layout, FcCampionato campionato, FcAttore attore) {
        HorizontalLayout filtersLayout = new HorizontalLayout();
        filtersLayout.setSpacing(true);

        comboAttoreA = createAttoreCombo(attore, campionato);
        comboAttoreB = createAttoreCombo(attore, campionato);

        comboPunti = new ComboBox<>();
        comboPunti.setItems(PUNTI_TOTALI);
        comboPunti.setValue(PUNTI_TOTALI);
        comboPunti.setPlaceholder("Classifica per");
        comboPunti.addValueChangeListener(event -> refreshGrafico(campionato));

        filtersLayout.add(comboAttoreA, comboAttoreB, comboPunti);
        layout.add(filtersLayout);

        refreshGrafico(campionato);
        layout.add(verticalLayoutGrafico);
    }

    private ComboBox<FcAttore> createAttoreCombo(FcAttore selected, FcCampionato campionato) {
        ComboBox<FcAttore> combo = new ComboBox<>();
        combo.setItems(squadreA);
        combo.setItemLabelGenerator(FcAttore::getDescAttore);
        combo.setValue(selected);
        combo.setPlaceholder("Seleziona Attore");
        combo.addValueChangeListener(event -> refreshGrafico(campionato));
        return combo;
    }

    private void refreshGrafico(FcCampionato campionato) {
        verticalLayoutGrafico.removeAll();
        Component grafico = buildGrafico(campionato);
        if (grafico != null) {
            verticalLayoutGrafico.add(grafico);
        }
    }

    @SuppressWarnings("rawtypes")
    public Component buildGrafico(FcCampionato campionato) {
        if (comboAttoreA.getValue() == null || comboAttoreB.getValue() == null || comboPunti.getValue() == null) {
            return new Span("Selezionare i parametri di confronto");
        }

        FcAttore attoreA = comboAttoreA.getValue();
        FcAttore attoreB = comboAttoreB.getValue();
        String punti = comboPunti.getValue();

        List<ClassificaBean> items = classificaTotalePuntiService.getModelGraficoEm(
                String.valueOf(attoreA.getIdAttore()),
                String.valueOf(attoreB.getIdAttore()),
                campionato);

        List<String> giornate = new ArrayList<>();
        List<Double> dataA = new ArrayList<>();
        List<Double> dataB = new ArrayList<>();

        for (ClassificaBean item : items) {
            giornate.add(item.getGiornata());

            if (attoreA.getDescAttore().equals(item.getSquadra())) {
                dataA.add(item.getTotPunti());
            } else if (attoreB.getDescAttore().equals(item.getSquadra())) {
                dataB.add(item.getTotPunti());
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
                        .withText("Classifica per " + punti)
                        .withAlign(Align.LEFT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5)
                                .build())
                        .build())
                .withXaxis(XAxisBuilder.get().withCategories(new ArrayList<>(giornate)).build())
                .withSeries(primaSerie, secondaSerie)
                .build();

        lineChart.setHeight("400px");
        lineChart.setWidth("100%");

        return lineChart;
    }

    private void setStatistiche(VerticalLayout layout, FcCampionato campionato, FcAttore attore) {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);

        addIfNotNull(actionsLayout, buildStatistichePdfButton(campionato));
        addAdminActions(actionsLayout, attore);

        layout.add(actionsLayout);

        HorizontalLayout filterLayout = buildFilterLayout();
        layout.add(filterLayout);

        List<FcStatistiche> items = statisticheService.findAll();
        PaginatedGrid<FcStatistiche, ?> grid = buildStatisticheGrid(items);

        layout.add(grid);
    }

    private FileDownloadWrapper buildStatistichePdfButton(FcCampionato campionato) {
        try {
            Button stampaPdf = new Button("Statistiche Voti pdf");
            stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());

            try (Connection conn = getConnection()) {
                Map<String, Object> params = new HashMap<>();
                params.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
                params.put("DIVISORE", String.valueOf(Costants.DIVISORE_10));

                Resource resource = resourceLoader.getResource(REPORT_STATISTICHE_VOTI);
                FileDownloadWrapper wrapper = new FileDownloadWrapper(
                        Utils.getStreamResource("StatisticheVoti.pdf", conn, params, resource.getInputStream()));
                wrapper.wrapComponent(stampaPdf);
                return wrapper;
            }
        } catch (Exception e) {
            log.error("Errore creazione pulsante download statistiche", e);
            return null;
        }
    }

    private void addAdminActions(HorizontalLayout layout, FcAttore attore) {
        if (attore == null || attore.getRoles() == null) {
            return;
        }

        boolean isAdmin = attore.getRoles().stream().anyMatch(Role.ADMIN::equals);
        if (!isAdmin) {
            return;
        }

        salvaStat = new Button("Aggiorna Statistiche");
        salvaStat.setIcon(VaadinIcon.DATABASE.create());
        salvaStat.addClickListener(this);
        layout.add(salvaStat);
    }

    private HorizontalLayout buildFilterLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        toggleP = createRoleToggle("P");
        toggleD = createRoleToggle("D");
        toggleC = createRoleToggle("C");
        toggleA = createRoleToggle("A");

        comboNazione = new ComboBox<>("Nazione");
        comboNazione.setItems(squadre);
        comboNazione.setItemLabelGenerator(FcSquadra::getNomeSquadra);
        comboNazione.setClearButtonVisible(true);
        comboNazione.setPlaceholder("Nazione");
        comboNazione.setRenderer(new ComponentRenderer<>(this::buildSquadraRenderer));

        txtQuotazione = new NumberField("Quotazione <=");
        txtQuotazione.setMin(0d);
        txtQuotazione.setMax(500d);
        txtQuotazione.setStepButtonsVisible(true);

        radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Giocatori");
        radioGroup.setItems(TUTTI, ATTIVI, NON_ATTIVI);
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setValue(TUTTI);

        layout.add(toggleP, toggleD, toggleC, toggleA, comboNazione, txtQuotazione, radioGroup);
        return layout;
    }

    private ToggleButton createRoleToggle(String label) {
        ToggleButton toggle = new ToggleButton();
        toggle.setLabel(label);
        toggle.setValue(true);
        return toggle;
    }

    private Component buildSquadraRenderer(FcSquadra item) {
        VerticalLayout container = new VerticalLayout();
        if (item.getImg() != null) {
            try {
                container.add(Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream()));
            } catch (SQLException e) {
                log.error("Errore lettura immagine squadra {}", item.getNomeSquadra(), e);
            }
        }
        container.add(new Span(item.getNomeSquadra()));
        return container;
    }

    private PaginatedGrid<FcStatistiche, ?> buildStatisticheGrid(List<FcStatistiche> items) {
        PaginatedGrid<FcStatistiche, ?> grid = new PaginatedGrid<>();
        ListDataProvider<FcStatistiche> dataProvider = new ListDataProvider<>(items);
        grid.setDataProvider(dataProvider);

        registerFilters(dataProvider);

        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMultiSort(true);
        grid.setAllRowsVisible(true);

        addStatisticheColumns(grid);

        grid.setPageSize(16);
        grid.setPaginatorSize(5);

        return grid;
    }

    private void registerFilters(ListDataProvider<FcStatistiche> dataProvider) {
        toggleP.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleD.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleC.addValueChangeListener(event -> applyFilter(dataProvider));
        toggleA.addValueChangeListener(event -> applyFilter(dataProvider));
        comboNazione.addValueChangeListener(event -> applyFilter(dataProvider));
        txtQuotazione.addValueChangeListener(event -> applyFilter(dataProvider));
        radioGroup.addValueChangeListener(event -> applyFilter(dataProvider));
    }

    private void addStatisticheColumns(PaginatedGrid<FcStatistiche, ?> grid) {
        Column<FcStatistiche> ruoloColumn = grid.addColumn(new ComponentRenderer<>(this::buildRuoloCell));
        ruoloColumn.setKey(Costants.RUOLO);
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader("R");
        ruoloColumn.setAutoWidth(true);

        Column<FcStatistiche> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, FcStatistiche::getCognGiocatore)));
        giocatoreColumn.setSortable(true);
        giocatoreColumn.setHeader(Costants.GIOCATORE);
        giocatoreColumn.setAutoWidth(true);

        Column<FcStatistiche> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildSquadraCell));
        nomeSquadraColumn.setSortable(true);
        nomeSquadraColumn.setComparator(Comparator.comparing(s -> valueOrEmpty(s.getNomeSquadra())));
        nomeSquadraColumn.setHeader(Costants.SQUADRA);
        nomeSquadraColumn.setWidth("100px");
        nomeSquadraColumn.setAutoWidth(true);

        Column<FcStatistiche> quotazioneColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(getQuotazione(stat)))));
        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader("Q");
        quotazioneColumn.setAutoWidth(true);

        Column<FcStatistiche> giocateColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getGiocate()))));
        giocateColumn.setSortable(true);
        giocateColumn.setHeader("Giocate");
        giocateColumn.setAutoWidth(true);

        Column<FcStatistiche> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildMediaCell(s, FcStatistiche::getMediaVoto)));
        mediaVotoColumn.setSortable(true);
        mediaVotoColumn.setComparator(Comparator.comparing(FcStatistiche::getMediaVoto));
        mediaVotoColumn.setHeader("Mv");
        mediaVotoColumn.setAutoWidth(true);

        Column<FcStatistiche> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildMediaCell(s, FcStatistiche::getFantaMedia)));
        fantaMediaColumn.setSortable(true);
        fantaMediaColumn.setComparator(Comparator.comparing(FcStatistiche::getFantaMedia));
        fantaMediaColumn.setHeader("FMv");
        fantaMediaColumn.setAutoWidth(true);

        Column<FcStatistiche> golFattoColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getGoalFatto()))));
        golFattoColumn.setSortable(true);
        golFattoColumn.setHeader("G+");
        golFattoColumn.setAutoWidth(true);

        Column<FcStatistiche> golSubitoColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getGoalSubito()))));
        golSubitoColumn.setSortable(true);
        golSubitoColumn.setHeader("G-");
        golSubitoColumn.setAutoWidth(true);

        Column<FcStatistiche> assistColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getAssist()))));
        assistColumn.setSortable(true);
        assistColumn.setHeader("Ass");
        assistColumn.setAutoWidth(true);

        Column<FcStatistiche> ammonizioneColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getAmmonizione()))));
        ammonizioneColumn.setSortable(true);
        ammonizioneColumn.setHeader("Amm");
        ammonizioneColumn.setAutoWidth(true);

        Column<FcStatistiche> espulsioneColumn = grid.addColumn(new ComponentRenderer<>(s ->
                buildTextCell(s, stat -> String.valueOf(stat.getEspulsione()))));
        espulsioneColumn.setSortable(true);
        espulsioneColumn.setHeader("Esp");
        espulsioneColumn.setAutoWidth(true);
    }

    private HorizontalLayout buildRuoloCell(FcStatistiche stat) {
        HorizontalLayout cellLayout = createCellLayout(false);

        if (stat != null) {
            applyInactiveStyle(cellLayout, stat);
            if (stat.getIdRuolo() != null) {
                cellLayout.add(buildRoleImage(stat.getIdRuolo()));
            }
        }

        return cellLayout;
    }

    private HorizontalLayout buildSquadraCell(FcStatistiche stat) {
        HorizontalLayout cellLayout = createCellLayout(false);

        if (stat == null) {
            return cellLayout;
        }

        applyInactiveStyle(cellLayout, stat);

        String nomeSquadra = stat.getNomeSquadra();
        if (nomeSquadra == null) {
            return cellLayout;
        }

        FcSquadra squadra = squadraService.findByNomeSquadra(nomeSquadra);
        if (squadra != null && squadra.getImg() != null) {
            try {
                cellLayout.add(Utils.getImage(nomeSquadra, squadra.getImg().getBinaryStream()));
            } catch (SQLException e) {
                log.error("Errore lettura immagine squadra {}", nomeSquadra, e);
            }
        }

        cellLayout.add(new Span(nomeSquadra));
        return cellLayout;
    }

    private HorizontalLayout buildTextCell(FcStatistiche stat, Function<FcStatistiche, String> extractor) {
        HorizontalLayout cellLayout = createCellLayout(false);

        if (stat != null) {
            applyInactiveStyle(cellLayout, stat);
            cellLayout.add(new Span(valueOrEmpty(extractor.apply(stat))));
        }

        return cellLayout;
    }

    private HorizontalLayout buildMediaCell(FcStatistiche stat, Function<FcStatistiche, Double> extractor) {
        HorizontalLayout cellLayout = createCellLayout(true);

        if (stat != null && stat.getFcGiocatore() != null) {
            applyInactiveStyle(cellLayout, stat);

            Double value = safeInt(extractor.apply(stat));
            Image img = buildTrendImage(value);

            Span span = new Span(formatDecimal(value / Costants.DIVISORE_10));
            span.add(img);
            cellLayout.add(span);
        }

        return cellLayout;
    }

    private HorizontalLayout createCellLayout(boolean spacing) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(spacing);
        cellLayout.setAlignItems(Alignment.STRETCH);
        return cellLayout;
    }

    private void applyInactiveStyle(HorizontalLayout cellLayout, FcStatistiche stat) {
        if (!stat.isFlagAttivo()) {
            cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
            cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
        }
    }

    private Image buildRoleImage(String ruolo) {
        String fileName = ruolo.toLowerCase() + ".png";
        return Utils.buildImage(fileName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + fileName));
    }

    private Image buildTrendImage(double value) {
        String imgThink = "2.png";
        if (value != 0) {
            if (value > Costants.EM_RANGE_MAX_MV) {
                imgThink = "1.png";
            } else if (value < Costants.EM_RANGE_MIN_MV) {
                imgThink = "3.png";
            }
        }
        return Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
    }

    private int getQuotazione(FcStatistiche stat) {
        FcGiocatore giocatore = stat.getFcGiocatore();
        return giocatore != null && giocatore.getQuotazione() != null ? giocatore.getQuotazione() : 0;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private double safeInt(Double value) {
        return value != null ? value : 0;
    }

    private String formatDecimal(double value) {
        return new DecimalFormat("#0.00").format(value);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
            if (event.getSource() == salvaStat && campionato != null) {
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

        Set<String> selectedRoles = getSelectedRoles();
        if (selectedRoles.isEmpty()) {
            dataProvider.addFilter(s -> false);
            return;
        }

        dataProvider.addFilter(s -> selectedRoles.contains(normalizeRole(s.getIdRuolo())));

        if (comboNazione.getValue() != null) {
            String selectedTeam = comboNazione.getValue().getNomeSquadra();
            dataProvider.addFilter(s -> selectedTeam.equals(s.getNomeSquadra()));
        }

        if (txtQuotazione.getValue() != null) {
            int maxQuotazione = txtQuotazione.getValue().intValue();
            dataProvider.addFilter(s -> getQuotazione(s) <= maxQuotazione);
        }

        if (ATTIVI.equals(radioGroup.getValue())) {
            dataProvider.addFilter(FcStatistiche::isFlagAttivo);
        } else if (NON_ATTIVI.equals(radioGroup.getValue())) {
            dataProvider.addFilter(s -> !s.isFlagAttivo());
        }
    }

    private Set<String> getSelectedRoles() {
        Set<String> selected = new java.util.HashSet<>();
        if (Boolean.TRUE.equals(toggleP.getValue())) {
            selected.add("P");
        }
        if (Boolean.TRUE.equals(toggleD.getValue())) {
            selected.add("D");
        }
        if (Boolean.TRUE.equals(toggleC.getValue())) {
            selected.add("C");
        }
        if (Boolean.TRUE.equals(toggleA.getValue())) {
            selected.add("A");
        }
        return selected;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.toUpperCase();
        return RUOLI_VALIDI.contains(normalized) ? normalized : "";
    }

    private void addIfNotNull(HorizontalLayout layout, Component component) {
        if (component != null) {
            layout.add(component);
        }
    }

    private Connection getConnection() throws SQLException {
        if (jdbcTemplate.getDataSource() == null) {
            throw new SQLException("DataSource non disponibile");
        }
        return jdbcTemplate.getDataSource().getConnection();
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return type.isInstance(value) ? (T) value : null;
    }
}
