package fcweb.ui.views.em;

import java.io.Serial;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.ClassificaBean;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Classifica")
@Route(value = "emclassifica", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmClassificaView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private Environment env;

	private List<ClassificaBean> items = null;
	private FcGiornataInfo giornataInfo = null;

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

        VaadinSession.getCurrent().getAttribute("PROPERTIES");
        giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

		items = classificaTotalePuntiController.getModelClassifica(giornataInfo.getIdGiornataFc());
	}

	private void initLayout() {

		LOG.info("initLayout");

		HorizontalLayout layoutGrid = new HorizontalLayout();
		layoutGrid.setMargin(false);
		layoutGrid.setPadding(false);
		layoutGrid.setSpacing(false);
		layoutGrid.setSizeFull();

		Grid<ClassificaBean> grid;
		try {
			grid = buildTableClassifica(items, giornataInfo);
			layoutGrid.add(grid);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		try {
			this.add(buildButtonPdf());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		this.add(layoutGrid);
		try {
			this.add(buildGrafico(items));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Component buildGrafico(List<ClassificaBean> items) {

		String[] att = new String[items.size()];
		String[] data = new String[items.size()];

		int i = 0;
		Series series = new Series("Tot Pt");
		for (ClassificaBean cl : items) {
			String sq = cl.getSquadra();
			double puntiRosa = (cl.getTotPunti() / Costants.DIVISORE_10);
			att[i] = sq;
			data[i] = "" + puntiRosa;
			i++;
		}
		series.setData(data);

		ApexCharts barChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.BAR).build())

				.withTitle(TitleSubtitleBuilder.get().withText("Totale Punti").withAlign(Align.LEFT).build()).withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(false).build()).build())

				.withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())

				.withSeries(series)

				.withXaxis(XAxisBuilder.get().withCategories(att).build()).build();

		barChart.setWidth("800px");
		barChart.setHeight("600px");

		return barChart;
	}

	private HorizontalLayout buildButtonPdf() {

        HorizontalLayout horLayout = new HorizontalLayout();
		horLayout.setSpacing(true);

		try {
			Button stampapdf = new Button("Classifica pdf");
			stampapdf.setIcon(VaadinIcon.DOWNLOAD.create());

            assert jdbcTemplate.getDataSource() != null;
            Connection conn = jdbcTemplate.getDataSource().getConnection();
			Map<String, Object> hm = new HashMap<>();
			String imgLog = env.getProperty("img.logo");
			hm.put("DIVISORE", "" + Costants.DIVISORE_10);
			hm.put("PATH_IMG", "images/" + imgLog);
			Resource resource = resourceLoader.getResource("classpath:reports/em/classifica.jasper");
			FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(Utils.getStreamResource("Classifica.pdf", conn, hm, resource.getInputStream()));

			button1Wrapper.wrapComponent(stampapdf);
			horLayout.add(button1Wrapper);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return horLayout;
	}

	private Grid<ClassificaBean> buildTableClassifica(
			List<ClassificaBean> items, FcGiornataInfo giornataInfo) {

		Grid<ClassificaBean> grid = new Grid<>();
		grid.setItems(items);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setAllRowsVisible(true);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setMultiSort(true);

		Column<ClassificaBean> posizioneColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			int x = items.indexOf(classifica) + 1;
            return new Span("" + x);
		})).setHeader("");
		posizioneColumn.setSortable(false);

		Column<ClassificaBean> squadraColumn = grid.addColumn(ClassificaBean::getSquadra);
		squadraColumn.setSortable(false);
		squadraColumn.setHeader(Costants.SQUADRA);

		Column<ClassificaBean> totPuntiColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPunti() != null ? classifica.getTotPunti() / Costants.DIVISORE_10 : 0;
			String sTotPunti = myFormatter.format(dTotPunti);

			Span lblTotPunti = new Span(sTotPunti);

			lblTotPunti.getStyle().set(Costants.FONT_SIZE, "14px");
			lblTotPunti.getStyle().set("color", Costants.BLUE);
			lblTotPunti.getElement().getStyle().set("-webkit-text-fill-color", Costants.BLUE);
			return lblTotPunti;

		})).setHeader("Totale Punti");
		totPuntiColumn.setSortable(true);
		totPuntiColumn.setComparator(Comparator.comparing(ClassificaBean::getTotPunti));

		Column<ClassificaBean> parzialePuntiColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPuntiParziale() != null ? classifica.getTotPuntiParziale() / Costants.DIVISORE_10 : 0;
			String sTotPunti = myFormatter.format(dTotPunti);
			return new Span(sTotPunti);
		})).setHeader("Parziale Punti");
		parzialePuntiColumn.setSortable(true);
		parzialePuntiColumn.setComparator(Comparator.comparing(ClassificaBean::getTotPuntiParziale));

		if (giornataInfo.getIdGiornataFc() >= 1) {
			Column<ClassificaBean> punti1Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata1() != null ? classifica.getPuntiGiornata1() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_1");
			punti1Column.setSortable(true);
			punti1Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata1));
		}

		if (giornataInfo.getIdGiornataFc() >= 2) {
			Column<ClassificaBean> punti2Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata2() != null ? classifica.getPuntiGiornata2() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_2");
			punti2Column.setSortable(true);
			punti2Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata2));
		}

		if (giornataInfo.getIdGiornataFc() >= 3) {
			Column<ClassificaBean> punti3Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata3() != null ? classifica.getPuntiGiornata3() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_3");
			punti3Column.setSortable(true);
			punti3Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata3));
		}

		if (giornataInfo.getIdGiornataFc() >= 4) {
			Column<ClassificaBean> punti4Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata4() != null ? classifica.getPuntiGiornata4() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_4");
			punti4Column.setSortable(true);
			punti4Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata4));
		}

		if (giornataInfo.getIdGiornataFc() >= 5) {
			Column<ClassificaBean> punti5Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata5() != null ? classifica.getPuntiGiornata5() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_5");
			punti5Column.setSortable(true);
			punti5Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata5));
		}

		if (giornataInfo.getIdGiornataFc() >= 6) {
			Column<ClassificaBean> punti6Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata6() != null ? classifica.getPuntiGiornata6() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_6");
			punti6Column.setSortable(true);
			punti6Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata6));
		}

		if (giornataInfo.getIdGiornataFc() >= 7) {
			Column<ClassificaBean> punti7Column = grid.addColumn(new ComponentRenderer<>(classifica -> {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotPunti = classifica.getPuntiGiornata7() != null ? classifica.getPuntiGiornata7() / Costants.DIVISORE_10 : 0;
				String sTotPunti = myFormatter.format(dTotPunti);
				return new Span(sTotPunti);
			})).setHeader("Punti_7");
			punti7Column.setSortable(true);
			punti7Column.setComparator(Comparator.comparing(ClassificaBean::getPuntiGiornata7));
		}

		return grid;
	}
}