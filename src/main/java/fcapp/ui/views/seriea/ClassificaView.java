package fcapp.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ClassificaService;
import fcapp.backend.service.JasperPdfService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Classifica")
@Route(value = "classifica", layout = MainLayout.class)
@RolesAllowed("USER")
public class ClassificaView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String REPORT_CLASSIFICA = "/reports/classifica.jasper";
    private static final String DECIMAL_PATTERN = "#0.00";
    private static final String SESSION_CAMPIONATO = "CAMPIONATO";

    private static final Logger log = LoggerFactory.getLogger(ClassificaView.class);

    private final transient JdbcTemplate jdbcTemplate;
    private final transient ClassificaService classificaService;
    private final transient AccessoService accessoService;
    private final transient JasperPdfService jasperPdfService;

    public ClassificaView(
            JdbcTemplate jdbcTemplate,
            ClassificaService classificaService,
            AccessoService accessoService,
            JasperPdfService jasperPdfService) {
        this.jdbcTemplate = jdbcTemplate;
        this.classificaService = classificaService;
        this.accessoService = accessoService;
        this.jasperPdfService = jasperPdfService;
        log.info("ClassificaView()");
    }

    @PostConstruct
    void init() {
        log.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }
        accessoService.insertAccesso(getClass().getName());
        initLayout();
    }

    private void initLayout() {
        log.info("initLayout");

        FcCampionato campionato = getSessionAttribute(SESSION_CAMPIONATO, FcCampionato.class);
        if (campionato == null) {
            log.warn("Campionato non presente in sessione");
            return;
        }

        try {
            HorizontalLayout pdfButtons = buildButtonPdf(campionato);
            if (pdfButtons != null) {
                add(pdfButtons);
            }
        } catch (Exception e) {
            log.error("Errore nella costruzione del pulsante PDF classifica", e);
        }

        try {
            HorizontalLayout gridLayout = new HorizontalLayout(buildTableClassifica(campionato));
            gridLayout.setMargin(false);
            gridLayout.setPadding(false);
            gridLayout.setSpacing(false);
            gridLayout.setSizeFull();
            add(gridLayout);
        } catch (Exception e) {
            log.error("Errore nella costruzione della tabella classifica", e);
        }

        try {
            Component graficoRosa = buildGrafico(campionato);
            if (graficoRosa != null) {
                add(graficoRosa);
            }
            Component graficoTvst = buildGraficoTuttiVsTutti(campionato);
            if (graficoTvst != null) {
                add(graficoTvst);
            }
        } catch (Exception e) {
            log.error("Errore nella costruzione dei grafici", e);
        }

        try {
            add(buildTableInfoClassifica(campionato));
        } catch (Exception e) {
            log.error("Errore nella costruzione della tabella info classifica", e);
        }
    }

    // -------------------------------------------------------------------------
    // Charts
    // -------------------------------------------------------------------------

    public Component buildGrafico(FcCampionato campionato) {
        List<FcClassifica> classifica =
                classificaService.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
        if (classifica.isEmpty()) {
            return null;
        }

        String[] categories = extractAttori(classifica);
        Object[] values = classifica.stream()
                .map(item -> item.getTotPuntiRosa() / Costants.DIVISORE_100)
                .toArray();

        return buildBarChart(
                "Classifica per Totale Punti Rosa",
                "Tot Pt Rosa",
                categories,
                values);
    }

    public Component buildGraficoTuttiVsTutti(FcCampionato campionato) {
        List<FcClassifica> classifica =
                classificaService.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);
        if (classifica.isEmpty()) {
            return null;
        }

        String[] categories = extractAttori(classifica);
        Object[] values = classifica.stream()
                .map(FcClassifica::getTotPuntiTvsT)
                .toArray();

        return buildBarChart(
                "Classifica per Totale Punti Tutti vs Tutti",
                "Tot Pt TvsT",
                categories,
                values);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Component buildBarChart(
            String title, String seriesName, String[] categories, Object[] values) {

        Series series = new Series(seriesName, values);

        ApexCharts barChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get().withHorizontal(false).build())
                        .build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(title)
                        .withAlign(Align.LEFT)
                        .build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withSeries(series)
                .withXaxis(XAxisBuilder.get().withCategories(categories).build())
                .build();

        barChart.setHeight("400px");
        barChart.setWidth("70%");
        return barChart;
    }

    private String[] extractAttori(List<FcClassifica> classifica) {
        return classifica.stream()
                .map(item -> item.getFcAttore().getDescAttore())
                .toArray(String[]::new);
    }

    // -------------------------------------------------------------------------
    // PDF download
    // -------------------------------------------------------------------------

    private HorizontalLayout buildButtonPdf(FcCampionato campionato) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        try {
            layout.add(createPdfDownloadLink(
                    "Download Classifica Pdf",
                    "Classifica.pdf",
                    REPORT_CLASSIFICA,
                    params -> params.put("PATH_IMG", "img/"),
                    campionato));
        } catch (Exception e) {
            log.error("Errore nella creazione del PDF classifica", e);
        }

        return layout;
    }

    private Anchor createPdfDownloadLink(
            String linkText,
            String fileName,
            String reportPath,
            Consumer<Map<String, Object>> extraParams,
            FcCampionato campionato) {

        return new Anchor(DownloadHandler.fromInputStream(event -> {
            try {
                Map<String, Object> params = baseReportParams(campionato);
                extraParams.accept(params);

                byte[] pdf = jasperPdfService.generaPdf(reportPath, params);
                return new DownloadResponse(
                        new ByteArrayInputStream(pdf),
                        fileName,
                        "application/pdf",
                        pdf.length);
            } catch (Exception e) {
                log.error("Errore generazione PDF {}", fileName, e);
                return DownloadResponse.error(500);
            }
        }), linkText);
    }

    private Map<String, Object> baseReportParams(FcCampionato campionato) {
        Map<String, Object> params = new HashMap<>();
        params.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
        params.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));
        return params;
    }

    // -------------------------------------------------------------------------
    // Main standings grid
    // -------------------------------------------------------------------------

    private Grid<FcClassifica> buildTableClassifica(FcCampionato campionato) {
        List<FcClassifica> items =
                classificaService.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);

        Grid<FcClassifica> grid = createBaseGrid();
        grid.setItems(items);

        grid.addColumn(FcClassifica::getIdPosiz).setSortable(false);

        Column<FcClassifica> squadraColumn = grid.addColumn(c -> c.getFcAttore().getDescAttore())
                .setHeader(Costants.SQUADRA)
                .setSortable(false)
                .setAutoWidth(true);

        Column<FcClassifica> puntiColumn = addSortableColumn(grid, "Punti", FcClassifica::getPunti);
        Column<FcClassifica> vinteColumn = addSortableColumn(grid, "Vinte", FcClassifica::getVinte);
        Column<FcClassifica> pariColumn = addSortableColumn(grid, "Pari", FcClassifica::getPari);
        Column<FcClassifica> perseColumn = addSortableColumn(grid, "Perse", FcClassifica::getPerse);
        Column<FcClassifica> gfColumn = addSortableColumn(grid, "Gf", FcClassifica::getGf);
        Column<FcClassifica> gsColumn = addSortableColumn(grid, "Gs", FcClassifica::getGs);
        Column<FcClassifica> drColumn = addSortableColumn(grid, "Dr", FcClassifica::getDr);

        Column<FcClassifica> totPuntiRosaColumn = grid.addColumn(
                        new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPuntiRosa()))))
                .setHeader("Tot Pt Rosa")
                .setSortable(true)
                .setComparator(Comparator.comparing(FcClassifica::getTotPuntiRosa))
                .setAutoWidth(true);

        Column<FcClassifica> totPuntiTVsTColumn =
                addSortableColumn(grid, "Tot Pt TvsT", FcClassifica::getTotPuntiTvsT);
        Column<FcClassifica> totFmColumn =
                addSortableColumn(grid, "Tot FM", FcClassifica::getTotFm);

        HeaderRow headerRow = grid.prependHeaderRow();
        headerRow.join(
                        squadraColumn,
                        puntiColumn,
                        vinteColumn,
                        pariColumn,
                        perseColumn,
                        gfColumn,
                        gsColumn,
                        drColumn,
                        totPuntiRosaColumn,
                        totPuntiTVsTColumn,
                        totFmColumn)
                .setText("Classifica Prima Fase");

        // posizioneColumn is intentionally not joined into the group header
        // (keeps the original layout: only the other columns share the title)
        return grid;
    }

    // -------------------------------------------------------------------------
    // Info / aggregated standings grid
    // -------------------------------------------------------------------------

    private Grid<FcClassificaTotPt> buildTableInfoClassifica(FcCampionato campionato) {
        List<FcClassificaTotPt> items = loadInfoClassifica(campionato);

        Grid<FcClassificaTotPt> grid = createBaseGrid();
        grid.setItems(items);

        Column<FcClassificaTotPt> squadraColumn =
                grid.addColumn(c -> c.getFcAttore().getDescAttore())
                        .setHeader(Costants.SQUADRA)
                        .setSortable(false);

        Column<FcClassificaTotPt> totPtRosaColumn = grid.addColumn(
                        new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPtRosa()))))
                .setHeader("Tot Pt Rosa")
                .setSortable(true)
                .setComparator(Comparator.comparing(FcClassificaTotPt::getTotPtRosa));

        Column<FcClassificaTotPt> ptTvsTColumn = grid.addColumn(FcClassificaTotPt::getPtTvsT)
                .setHeader("Tot Pt TvsT")
                .setSortable(true);

        Column<FcClassificaTotPt> totPuntiColumn = grid.addColumn(
                        new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPt()))))
                .setHeader("Tot Pt 18")
                .setSortable(true)
                .setComparator(Comparator.comparing(FcClassificaTotPt::getTotPt));

        Column<FcClassificaTotPt> totPuntiOldColumn = grid.addColumn(
                        new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPtOld()))))
                .setHeader("Tot Pt 11")
                .setSortable(true)
                .setComparator(Comparator.comparing(FcClassificaTotPt::getTotPtOld));

        Column<FcClassificaTotPt> scoreColumn = grid.addColumn(FcClassificaTotPt::getScore)
                .setHeader("GrandPrix G18")
                .setSortable(true);

        Column<FcClassificaTotPt> scoreOldColumn = grid.addColumn(FcClassificaTotPt::getScoreOld)
                .setHeader("GrandPrix G11")
                .setSortable(true);

        Column<FcClassificaTotPt> scoreGrandPrixColumn =
                grid.addColumn(FcClassificaTotPt::getScoreGrandPrix)
                        .setHeader("GrandPrix F1")
                        .setSortable(true);

        HeaderRow headerRow = grid.prependHeaderRow();
        headerRow.join(
                        squadraColumn,
                        totPuntiColumn,
                        totPuntiOldColumn,
                        totPtRosaColumn,
                        ptTvsTColumn,
                        scoreColumn,
                        scoreOldColumn,
                        scoreGrandPrixColumn)
                .setText("Info Classifiche Generali");

        return grid;
    }

    private List<FcClassificaTotPt> loadInfoClassifica(FcCampionato campionato) {
        String sql = """
                SELECT a.desc_attore,
                       SUM(pt.tot_pt)            AS tot18,
                       SUM(pt.tot_pt_old)        AS tot11,
                       SUM(pt.tot_pt_rosa)       AS totRosa,
                       SUM(pt.pt_tvst)           AS pt_tvst,
                       SUM(pt.score)             AS score18,
                       SUM(pt.score_old)         AS score11,
                       SUM(pt.score_grand_prix)  AS score_grand_prix
                FROM fc_classifica_tot_pt pt
                JOIN fc_attore a ON a.id_attore = pt.id_attore
                WHERE pt.id_campionato = ?
                GROUP BY a.desc_attore
                ORDER BY 3 DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            FcClassificaTotPt item = new FcClassificaTotPt();

            FcAttore attore = new FcAttore();
            attore.setDescAttore(rs.getString("desc_attore"));
            item.setFcAttore(attore);

            item.setTotPt(rs.getDouble("tot18"));
            item.setTotPtOld(rs.getDouble("tot11"));
            item.setTotPtRosa(rs.getDouble("totRosa"));
            item.setPtTvsT(rs.getInt("pt_tvst"));
            item.setScore(rs.getInt("score18"));
            item.setScoreOld(rs.getInt("score11"));
            item.setScoreGrandPrix(rs.getInt("score_grand_prix"));

            return item;
        }, campionato.getIdCampionato());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private <T> Grid<T> createBaseGrid() {
        Grid<T> grid = new Grid<>();
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMultiSort(true);
        return grid;
    }

    private <T, V> Column<T> addSortableColumn(
            Grid<T> grid, String header, Function<T, V> valueProvider) {
        return grid.addColumn(valueProvider::apply)
                .setHeader(header)
                .setSortable(true)
                .setAutoWidth(true);
    }

    private String formatDecimal(Number value) {
        DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
        double normalized = value == null ? 0d : value.doubleValue() / Costants.DIVISORE_100;
        return formatter.format(normalized);
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            log.warn("Session attribute '{}' is not of type {}", key, type.getName());
            return null;
        }
        return (T) value;
    }
}