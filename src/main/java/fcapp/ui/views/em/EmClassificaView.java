package fcapp.ui.views.em;

import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.ClassificaBean;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Classifica")
@Route(value = "classificaEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmClassificaView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String HEADER_TOTALE_PUNTI = "Totale Punti";
    private static final String HEADER_PARZIALE_PUNTI = "Parziale Punti";
    private static final String BLUE = Costants.BLUE;
    private static final String FONT_SIZE = Costants.FONT_SIZE;
    private static final String FONT_SIZE_VALUE = "14px";

    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private final transient Environment env;
    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient ClassificaTotalePuntiService classificaTotalePuntiService;
    private final transient AccessoService accessoService;

    private List<ClassificaBean> items;
    private FcGiornataInfo giornataInfo;

    public EmClassificaView(
            Environment env,
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            AccessoService accessoService) {
        log.info("EmClassificaView()");
        this.env = env;
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
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
        VaadinSession session = VaadinSession.getCurrent();
        session.getAttribute("PROPERTIES");
        giornataInfo = (FcGiornataInfo) session.getAttribute("GIORNATA_INFO");
        items = classificaTotalePuntiService.getModelClassifica(giornataInfo.getIdGiornataFc());
    }

    private void initLayout() {
        log.info("initLayout");

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setMargin(false);
        layoutGrid.setPadding(false);
        layoutGrid.setSpacing(false);
        layoutGrid.setSizeFull();

        try {
            layoutGrid.add(buildTableClassifica(items, giornataInfo));
        } catch (Exception e) {
            log.error("Errore durante la costruzione della tabella classifica", e);
        }

        try {
            add(buildButtonPdf());
        } catch (Exception e) {
            log.error("Errore durante la costruzione del pulsante PDF", e);
        }

        add(layoutGrid);

        try {
            add(buildGrafico(items));
        } catch (Exception e) {
            log.error("Errore durante la costruzione del grafico", e);
        }
    }

    public Component buildGrafico(List<ClassificaBean> items) {
        String[] categories = new String[items.size()];
        String[] data = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
            ClassificaBean classifica = items.get(i);
            categories[i] = classifica.getSquadra();
            data[i] = String.valueOf(toDisplayValue(classifica.getTotPunti()));
        }

        Series<String> series = new Series<>("Tot Pt", data);

        ApexCharts barChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get().withType(Type.BAR).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText(HEADER_TOTALE_PUNTI)
                        .withAlign(Align.LEFT)
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get().withHorizontal(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
                .withSeries(series)
                .withXaxis(XAxisBuilder.get().withCategories(categories).build())
                .build();

        barChart.setWidth("800px");
        barChart.setHeight("600px");

        return barChart;
    }

    private HorizontalLayout buildButtonPdf() {
        HorizontalLayout horLayout = new HorizontalLayout();
        horLayout.setSpacing(true);

        try {
            Button stampaPdf = new Button("Classifica pdf");
            stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());

            if (jdbcTemplate.getDataSource() == null) {
                throw new IllegalStateException("DataSource non disponibile");
            }

            Map<String, Object> parameters = buildPdfParameters();
            Resource resource = resourceLoader.getResource("classpath:reports/em/classifica.jasper");

            try (Connection connection = jdbcTemplate.getDataSource().getConnection();
                 InputStream inputStream = resource.getInputStream()) {

                FileDownloadWrapper wrapper = new FileDownloadWrapper(
                        Utils.getStreamResource("Classifica.pdf", connection, parameters, inputStream)
                );

                wrapper.wrapComponent(stampaPdf);
                horLayout.add(wrapper);
            }
        } catch (Exception e) {
            log.error("Errore durante la costruzione del PDF", e);
        }

        return horLayout;
    }

    private Map<String, Object> buildPdfParameters() {
        Map<String, Object> parameters = new HashMap<>();
        String imgLogo = env.getProperty("img.logo");

        parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_10));
        parameters.put("PATH_IMG", "images/" + imgLogo);

        return parameters;
    }

    private Grid<ClassificaBean> buildTableClassifica(List<ClassificaBean> items, FcGiornataInfo giornataInfo) {
        Grid<ClassificaBean> grid = new Grid<>();
        grid.setItems(items);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES
        );
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setMultiSort(true);

        addPositionColumn(grid, items);
        addSquadraColumn(grid);
        addStyledNumericColumn(grid, HEADER_TOTALE_PUNTI, ClassificaBean::getTotPunti, true);
        addNumericColumn(grid, HEADER_PARZIALE_PUNTI, ClassificaBean::getTotPuntiParziale, true);

        addGiornataColumns(grid, giornataInfo.getIdGiornataFc());

        return grid;
    }

    private void addPositionColumn(Grid<ClassificaBean> grid, List<ClassificaBean> items) {
        grid.addComponentColumn(classifica -> new Span(String.valueOf(items.indexOf(classifica) + 1)))
                .setHeader("")
                .setSortable(false);
    }

    private void addSquadraColumn(Grid<ClassificaBean> grid) {
        grid.addColumn(ClassificaBean::getSquadra)
                .setHeader(Costants.SQUADRA)
                .setSortable(false);
    }

    private void addGiornataColumns(Grid<ClassificaBean> grid, int idGiornataFc) {
        List<Function<ClassificaBean, Double>> getters = List.of(
                ClassificaBean::getPuntiGiornata1,
                ClassificaBean::getPuntiGiornata2,
                ClassificaBean::getPuntiGiornata3,
                ClassificaBean::getPuntiGiornata4,
                ClassificaBean::getPuntiGiornata5,
                ClassificaBean::getPuntiGiornata6,
                ClassificaBean::getPuntiGiornata7
        );

        for (int i = 0; i < getters.size() && i < idGiornataFc; i++) {
            addNumericColumn(grid, "Punti_" + (i + 1), getters.get(i), true);
        }
    }

    private Column<ClassificaBean> addNumericColumn(
            Grid<ClassificaBean> grid,
            String header,
            Function<ClassificaBean, Double> valueProvider,
            boolean sortable) {

        Column<ClassificaBean> column = grid.addComponentColumn(item ->
                new Span(formatDecimal(valueProvider.apply(item))))
                .setHeader(header)
                .setSortable(sortable);

        if (sortable) {
            column.setComparator((a, b) -> Double.compare(
                    safeValue(valueProvider.apply(a)),
                    safeValue(valueProvider.apply(b))
            ));
        }

        return column;
    }

    private Column<ClassificaBean> addStyledNumericColumn(
            Grid<ClassificaBean> grid,
            String header,
            Function<ClassificaBean, Double> valueProvider,
            boolean sortable) {

        Column<ClassificaBean> column = grid.addComponentColumn(item -> {
            Span span = new Span(formatDecimal(valueProvider.apply(item)));
            span.getStyle().set(FONT_SIZE, FONT_SIZE_VALUE);
            span.getStyle().set("color", BLUE);
            span.getElement().getStyle().set("-webkit-text-fill-color", BLUE);
            return span;
        }).setHeader(header).setSortable(sortable);

        if (sortable) {
            column.setComparator((a, b) -> Double.compare(
                    safeValue(valueProvider.apply(a)),
                    safeValue(valueProvider.apply(b))
            ));
        }

        return column;
    }

    private String formatDecimal(Double value) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(toDisplayValue(value));
    }

    private double toDisplayValue(Double value) {
        return safeValue(value) / Costants.DIVISORE_10;
    }

    private double safeValue(Double value) {
        return value != null ? value : 0D;
    }
}
