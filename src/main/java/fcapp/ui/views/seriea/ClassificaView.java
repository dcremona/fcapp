package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.olli.FileDownloadWrapper;

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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ClassificaService;
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

    private static final String REPORT_CLASSIFICA = "classpath:reports/classifica.jasper";
    private static final String DECIMAL_PATTERN = "#0.00";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient ClassificaService classificaService;
    private final transient AccessoService accessoService;

    public ClassificaView(
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            ClassificaService classificaService,
            AccessoService accessoService) {

        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.classificaService = classificaService;
        this.accessoService = accessoService;

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

        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        if (campionato == null) {
            return;
        }

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setMargin(false);
        layoutGrid.setPadding(false);
        layoutGrid.setSpacing(false);
        layoutGrid.setSizeFull();

        try {
            layoutGrid.add(buildTableClassifica(campionato));
        } catch (Exception e) {
            log.error("Errore nella costruzione della tabella classifica", e);
        }

        try {
            HorizontalLayout buttonPdf = buildButtonPdf(campionato);
            if (buttonPdf != null) {
                add(buttonPdf);
            }
        } catch (Exception e) {
            log.error("Errore nella costruzione del pulsante pdf classifica", e);
        }

        add(layoutGrid);

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

    public Component buildGrafico(FcCampionato campionato) {
        List<FcClassifica> classifica = classificaService.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
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
        List<FcClassifica> classifica = classificaService.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);
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

    private Component buildBarChart(String title, String seriesName, String[] categories, Object[] values) {
        Series series = new Series(seriesName, values);

        ApexCharts barChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get().withHorizontal(false).build())
                        .build())
                .withTitle(TitleSubtitleBuilder.get().withText(title).withAlign(Align.LEFT).build())
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

    private HorizontalLayout buildButtonPdf(FcCampionato campionato) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        try {
            Button stampaPdf = new Button("Classifica pdf");
            stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());

            if (jdbcTemplate.getDataSource() != null) {
                Connection connection = jdbcTemplate.getDataSource().getConnection();

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
                parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));

                Resource resource = resourceLoader.getResource(REPORT_CLASSIFICA);

                FileDownloadWrapper wrapper = new FileDownloadWrapper(
                        Utils.getStreamResource(
                                "Classifica.pdf",
                                connection,
                                parameters,
                                resource.getInputStream()));

                wrapper.wrapComponent(stampaPdf);
                layout.add(wrapper);
            }

        } catch (Exception e) {
            log.error("Errore nella creazione del pdf classifica", e);
        }

        return layout;
    }

    private Grid<FcClassifica> buildTableClassifica(FcCampionato campionato) {
        List<FcClassifica> items = classificaService.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);

        Grid<FcClassifica> grid = new Grid<>();
        grid.setItems(items);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMultiSort(true);

        Column<FcClassifica> posizioneColumn = grid.addColumn(FcClassifica::getIdPosiz);
        posizioneColumn.setSortable(false);

        Column<FcClassifica> squadraColumn = grid.addColumn(c -> c.getFcAttore().getDescAttore());
        squadraColumn.setSortable(false);
        squadraColumn.setHeader(Costants.SQUADRA);
        squadraColumn.setAutoWidth(true);

        Column<FcClassifica> puntiColumn = grid.addColumn(FcClassifica::getPunti);
        puntiColumn.setHeader("Punti");
        puntiColumn.setSortable(true);
        puntiColumn.setAutoWidth(true);

        Column<FcClassifica> vinteColumn = grid.addColumn(FcClassifica::getVinte);
        vinteColumn.setHeader("Vinte");
        vinteColumn.setSortable(true);
        vinteColumn.setAutoWidth(true);

        Column<FcClassifica> pariColumn = grid.addColumn(FcClassifica::getPari);
        pariColumn.setHeader("Pari");
        pariColumn.setSortable(true);
        pariColumn.setAutoWidth(true);

        Column<FcClassifica> perseColumn = grid.addColumn(FcClassifica::getPerse);
        perseColumn.setHeader("Perse");
        perseColumn.setSortable(true);
        perseColumn.setAutoWidth(true);

        Column<FcClassifica> gfColumn = grid.addColumn(FcClassifica::getGf);
        gfColumn.setHeader("Gf");
        gfColumn.setSortable(true);
        gfColumn.setAutoWidth(true);

        Column<FcClassifica> gsColumn = grid.addColumn(FcClassifica::getGs);
        gsColumn.setHeader("Gs");
        gsColumn.setSortable(true);
        gsColumn.setAutoWidth(true);

        Column<FcClassifica> drColumn = grid.addColumn(FcClassifica::getDr);
        drColumn.setHeader("Dr");
        drColumn.setSortable(true);
        drColumn.setAutoWidth(true);

        Column<FcClassifica> totPuntiRosaColumn = grid.addColumn(
                new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPuntiRosa()))));
        totPuntiRosaColumn.setHeader("Tot Pt Rosa");
        totPuntiRosaColumn.setSortable(true);
        totPuntiRosaColumn.setComparator(Comparator.comparing(FcClassifica::getTotPuntiRosa));
        totPuntiRosaColumn.setAutoWidth(true);

        Column<FcClassifica> totPuntiTVsTColumn = grid.addColumn(FcClassifica::getTotPuntiTvsT);
        totPuntiTVsTColumn.setHeader("Tot Pt TvsT");
        totPuntiTVsTColumn.setSortable(true);
        totPuntiTVsTColumn.setAutoWidth(true);

        Column<FcClassifica> totfmColumn = grid.addColumn(FcClassifica::getTotFm);
        totfmColumn.setHeader("Tot FM");
        totfmColumn.setSortable(true);
        totfmColumn.setAutoWidth(true);

        HeaderRow headerRow = grid.prependHeaderRow();
        HeaderCell headerCell = headerRow.join(
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
                totfmColumn);
        headerCell.setText("Classifica Prima Fase");

        return grid;
    }

    private Grid<FcClassificaTotPt> buildTableInfoClassifica(FcCampionato campionato) {
        List<FcClassificaTotPt> items = loadInfoClassifica(campionato);

        Grid<FcClassificaTotPt> grid = new Grid<>();
        grid.setItems(items);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMultiSort(true);

        Column<FcClassificaTotPt> squadraColumn = grid.addColumn(c -> c.getFcAttore().getDescAttore());
        squadraColumn.setSortable(false);
        squadraColumn.setHeader(Costants.SQUADRA);

        Column<FcClassificaTotPt> totPtRosaColumn = grid.addColumn(
                new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPtRosa()))));
        totPtRosaColumn.setHeader("Tot Pt Rosa");
        totPtRosaColumn.setSortable(true);
        totPtRosaColumn.setComparator(Comparator.comparing(FcClassificaTotPt::getTotPtRosa));

        Column<FcClassificaTotPt> ptTvsTColumn = grid.addColumn(FcClassificaTotPt::getPtTvsT);
        ptTvsTColumn.setHeader("Tot Pt TvsT");
        ptTvsTColumn.setSortable(true);

        Column<FcClassificaTotPt> totPuntiColumn = grid.addColumn(
                new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPt()))));
        totPuntiColumn.setHeader("Tot Pt 18");
        totPuntiColumn.setSortable(true);
        totPuntiColumn.setComparator(Comparator.comparing(FcClassificaTotPt::getTotPt));

        Column<FcClassificaTotPt> totPuntiOldColumn = grid.addColumn(
                new ComponentRenderer<>(c -> new Span(formatDecimal(c.getTotPtOld()))));
        totPuntiOldColumn.setHeader("Tot Pt 11");
        totPuntiOldColumn.setSortable(true);
        totPuntiOldColumn.setComparator(Comparator.comparing(FcClassificaTotPt::getTotPtOld));

        Column<FcClassificaTotPt> scoreColumn = grid.addColumn(FcClassificaTotPt::getScore);
        scoreColumn.setHeader("GrandPrix G18");
        scoreColumn.setSortable(true);

        Column<FcClassificaTotPt> scoreOldColumn = grid.addColumn(FcClassificaTotPt::getScoreOld);
        scoreOldColumn.setHeader("GrandPrix G11");
        scoreOldColumn.setSortable(true);

        Column<FcClassificaTotPt> scoreGrandPrixColumn = grid.addColumn(FcClassificaTotPt::getScoreGrandPrix);
        scoreGrandPrixColumn.setHeader("GrandPrix F1");
        scoreGrandPrixColumn.setSortable(true);

        HeaderRow headerRow = grid.prependHeaderRow();
        HeaderCell headerCell = headerRow.join(
                squadraColumn,
                totPuntiColumn,
                totPuntiOldColumn,
                totPtRosaColumn,
                ptTvsTColumn,
                scoreColumn,
                scoreOldColumn,
                scoreGrandPrixColumn);
        headerCell.setText("Info Classifiche Generali");

        return grid;
    }

    private List<FcClassificaTotPt> loadInfoClassifica(FcCampionato campionato) {
        List<FcClassificaTotPt> result = new ArrayList<>();

        jdbcTemplate.query(getString(campionato), rs -> {
            while (rs.next()) {
                FcClassificaTotPt item = new FcClassificaTotPt();

                FcAttore attore = new FcAttore();
                attore.setDescAttore(rs.getString(1));

                item.setFcAttore(attore);
                item.setTotPt(rs.getDouble(2));
                item.setTotPtOld(rs.getDouble(3));
                item.setTotPtRosa(rs.getDouble(4));
                item.setPtTvsT(rs.getInt(5));
                item.setScore(rs.getInt(6));
                item.setScoreOld(rs.getInt(7));
                item.setScoreGrandPrix(rs.getInt(8));

                result.add(item);
            }
            return "1";
        });

        return result;
    }

    private String formatDecimal(Number value) {
        DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
        double normalized = value == null ? 0d : value.doubleValue() / Costants.DIVISORE_100;
        return formatter.format(normalized);
    }

    private @NonNull String getString(FcCampionato campionato) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select a.desc_attore, ");
        sql.append(" sum(pt.tot_pt) as tot18, ");
        sql.append(" sum(pt.tot_pt_old) as tot11, ");
        sql.append(" sum(pt.tot_pt_rosa) as totRosa, ");
        sql.append(" sum(pt.pt_tvst) as pt_tvst, ");
        sql.append(" sum(pt.score) as score18, ");
        sql.append(" sum(pt.score_old) as score11, ");
        sql.append(" sum(pt.score_grand_prix) as score_grand_prix ");
        sql.append(" from fc_classifica_tot_pt pt, ");
        sql.append(" fc_attore a ");
        sql.append(" where pt.id_campionato = ").append(campionato.getIdCampionato());
        sql.append(" and a.id_attore = pt.id_attore ");
        sql.append(" group by a.desc_attore ");
        sql.append(" order by 3 desc ");
        return sql.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
