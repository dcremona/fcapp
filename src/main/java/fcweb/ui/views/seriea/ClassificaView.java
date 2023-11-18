
package fcweb.ui.views.seriea;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.vaadin.addons.tatu.ColumnChart;
import org.vaadin.addons.tatu.ColumnChart.ColumnChartAxis;
import org.vaadin.addons.tatu.ColumnChart.ColumnChartValue;
import org.vaadin.olli.FileDownloadWrapper;

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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.ClassificaService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Classifica")
@Route(value = "classifica", layout = MainLayout.class)
@RolesAllowed("USER")
public class ClassificaView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private AccessoService accessoController;

	@PostConstruct
	void init() {
		LOG.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initLayout();
	}

	private void initLayout() {

		LOG.info("initLayout");

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		HorizontalLayout layoutGrid = new HorizontalLayout();
		layoutGrid.setMargin(false);
		layoutGrid.setPadding(false);
		layoutGrid.setSpacing(false);
		layoutGrid.setSizeFull();

		Grid<FcClassifica> grid;
		try {
			grid = buildTableClassifica(campionato);
			layoutGrid.add(grid);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			this.add(buildButtonPdf(campionato, p));
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
		this.add(layoutGrid);
		try {

//			Component comp = buildGrafico(campionato);
//			if (comp != null) {
//				this.add(comp);
//			}
//
//			Component comp2 = buildGraficoTuttiVsTutti(campionato);
//			if (comp2 != null) {
//				this.add(comp2);
//			}

			this.add(buildGrafico(campionato));
			this.add(buildGraficoTuttiVsTutti(campionato));

		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			this.add(buildTableInfoClassifica(campionato));
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
		// this.add(new Label("Tot pt 18 = quella attuale"));
		// this.add(new Label("Tot pt 11 = quella proposta da Rinc e Skizzo sui
		// 11 titolari schierati il sabato (senza la panchina e i 2 cambi "));
		// this.add(new Label("Tot pt Rosa = quella proposta da Greg sul totale
		// della rosa"));
		// this.add(new Label("Grand Prix G18 = quella proposta da Skizzo di
		// attribure 7 punti al migliore di giornate, poi a scendere
		// 6,5,4,3,2,1,0"));
		// this.add(new Label("Grand Prix G11 = stessa della precedente ma
		// calcolata solo sui 11 titolari (senza panchina e cambi)"));
		// this.add(new Label("Grand Prix F1 = stessa della proposta di Skizzo
		// ma i punti calcolati come gp di formula1 15 al vincitore, 10 al
		// secondo, etc"));
	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public Component buildGrafico(FcCampionato campionato) throws Exception {
//
//		String[] att = new String[8];
//		ArrayList<Double> data = new ArrayList<>();
//
//		List<FcClassifica> all = classificaController.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
//
//		int i = 0;
//		for (FcClassifica cl : all) {
//			String sq = cl.getFcAttore().getDescAttore();
//			double puntiRosa = (cl.getTotPuntiRosa() / Costants.DIVISORE_100);
//			att[i] = sq;
//			data.add(puntiRosa);
//			i++;
//		}
//
//		if (all.size() > 0) {
//
//			Series series = new Series("Tot Pt Rosa",data.get(0),data.get(1),data.get(2),data.get(3),data.get(4),data.get(5),data.get(6),data.get(7));
//
//			ApexCharts barChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.BAR).build())
//
//					.withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(false).build()).build())
//
//					.withTitle(TitleSubtitleBuilder.get().withText("Classifica per Totale Punti Rosa").withAlign(Align.LEFT).build())
//
//					.withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
//
//					.withSeries(series)
//
//					.withXaxis(XAxisBuilder.get().withCategories(att).build()).build();
//
//			// barChart.setWidth("600px");
//			// barChart.setHeight("400px");
//			// setWidth("80%");
//			barChart.setWidth("70%");
//
//			return barChart;
//		}
//
//		return null;
//
//	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public Component buildGraficoTuttiVsTutti(FcCampionato campionato)
//			throws Exception {
//
//		String[] att = new String[8];
//		ArrayList<Integer> data = new ArrayList<Integer>();
//
//		List<FcClassifica> all = classificaController.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);
//
//		int i = 0;
//		for (FcClassifica cl : all) {
//			String sq = cl.getFcAttore().getDescAttore();
//			int punti1vsT = cl.getTotPuntiTvsT();
//			att[i] = sq;
//			data.add(punti1vsT);
//			i++;
//		}
//
//		if (all.size() > 0) {
//
//			Series series = new Series("Tot Pt TvsT",data.get(0),data.get(1),data.get(2),data.get(3),data.get(4),data.get(5),data.get(6),data.get(7));
//
//			ApexCharts barChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.BAR).build())
//
//					.withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(false).build()).build())
//
//					.withTitle(TitleSubtitleBuilder.get().withText("Classifica per Totale Punti Tutti vs Tutti").withAlign(Align.LEFT).build())
//
//					.withDataLabels(DataLabelsBuilder.get().withEnabled(false).build())
//
//					.withSeries(series)
//
//					.withXaxis(XAxisBuilder.get().withCategories(att).build()).build();
//
//			// barChart.setWidth("600px");
//			// barChart.setHeight("400px");
//			// setWidth("80%");
//			barChart.setWidth("70%");
//
//			return barChart;
//		}
//
//		return null;
//
//	}

	private HorizontalLayout buildButtonPdf(FcCampionato campionato, Properties p) throws Exception {

		Button stampapdf = new Button("Classifica pdf");
		stampapdf.setIcon(VaadinIcon.DOWNLOAD.create());
		FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(new StreamResource("Classifica.pdf", () -> {
			try {
				Map<String, Object> hm = new HashMap<String, Object>();
				hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
				hm.put("DIVISORE", "" + Costants.DIVISORE_100);
				Resource resource = resourceLoader.getResource("classpath:reports/classifica.jasper");
				InputStream inputStream = resource.getInputStream();
				Connection conn = jdbcTemplate.getDataSource().getConnection();
				return JasperReporUtils.runReportToPdf(inputStream, hm, conn);
			} catch (Exception ex2) {
			}
			return null;
		}));
		button1Wrapper.wrapComponent(stampapdf);

		HorizontalLayout horLayout = new HorizontalLayout();
		horLayout.setSpacing(true);
		horLayout.add(button1Wrapper);

		return horLayout;
	}

	private Grid<FcClassifica> buildTableClassifica(FcCampionato campionato) throws Exception {

		List<FcClassifica> items = classificaController.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);

		Grid<FcClassifica> grid = new Grid<>();
		grid.setItems(items);
		// grid.setWidth("70%");
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);
		grid.setAllRowsVisible(true);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setMultiSort(true);

		Column<FcClassifica> posizioneColumn = grid.addColumn(classifica -> classifica.getIdPosiz());
		posizioneColumn.setSortable(false);

		Column<FcClassifica> squadraColumn = grid.addColumn(classifica -> classifica.getFcAttore().getDescAttore());
		squadraColumn.setSortable(false);
		squadraColumn.setHeader("Squadra");
		squadraColumn.setAutoWidth(true);

		Column<FcClassifica> puntiColumn = grid.addColumn(classifica -> classifica.getPunti());
		puntiColumn.setHeader("Punti");
		puntiColumn.setSortable(true);
		puntiColumn.setAutoWidth(true);

		Column<FcClassifica> vinteColumn = grid.addColumn(classifica -> classifica.getVinte());
		vinteColumn.setHeader("Vinte");
		vinteColumn.setSortable(true);
		vinteColumn.setAutoWidth(true);

		Column<FcClassifica> pariColumn = grid.addColumn(classifica -> classifica.getPari());
		pariColumn.setHeader("Pari");
		pariColumn.setSortable(true);
		pariColumn.setAutoWidth(true);

		Column<FcClassifica> perseColumn = grid.addColumn(classifica -> classifica.getPerse());
		perseColumn.setHeader("Perse");
		perseColumn.setSortable(true);
		perseColumn.setAutoWidth(true);

		Column<FcClassifica> gfColumn = grid.addColumn(classifica -> classifica.getGf());
		gfColumn.setHeader("Gf");
		gfColumn.setSortable(true);
		gfColumn.setAutoWidth(true);

		Column<FcClassifica> gsColumn = grid.addColumn(classifica -> classifica.getGs());
		gsColumn.setHeader("Gs");
		gsColumn.setSortable(true);
		gsColumn.setAutoWidth(true);

		Column<FcClassifica> drColumn = grid.addColumn(classifica -> classifica.getDr());
		drColumn.setHeader("Dr");
		drColumn.setSortable(true);
		drColumn.setAutoWidth(true);

		Column<FcClassifica> totPuntiRosaColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPuntiRosa() != null
					? classifica.getTotPuntiRosa() / Costants.DIVISORE_100
					: 0;
			String sTotPunti = myFormatter.format(dTotPunti);
			return new Span(sTotPunti);
		})).setHeader("Tot Pt Rosa");
		totPuntiRosaColumn.setSortable(true);
		totPuntiRosaColumn.setComparator((p1, p2) -> p1.getTotPuntiRosa().compareTo(p2.getTotPuntiRosa()));
		totPuntiRosaColumn.setAutoWidth(true);

		Column<FcClassifica> totPuntiTVsTColumn = grid.addColumn(classifica -> classifica.getTotPuntiTvsT());
		totPuntiTVsTColumn.setHeader("Tot Pt TvsT");
		totPuntiTVsTColumn.setSortable(true);
		totPuntiTVsTColumn.setAutoWidth(true);

		Column<FcClassifica> totfmColumn = grid.addColumn(classifica -> classifica.getTotFm());
		totfmColumn.setHeader("Tot FM");
		totfmColumn.setSortable(true);
		totfmColumn.setAutoWidth(true);

		// Column<FcClassifica> mercatofmColumn = grid.addColumn(classifica ->
		// classifica.getFmMercato());
		// mercatofmColumn.setHeader("Mercato FM");

		HeaderRow headerRow = grid.prependHeaderRow();
		HeaderCell headerCell = headerRow.join(squadraColumn, puntiColumn, vinteColumn, pariColumn, perseColumn,
				gfColumn, gsColumn, drColumn, totPuntiRosaColumn, totPuntiTVsTColumn, totfmColumn);
		headerCell.setText("Classifica Prima Fase");

		return grid;
	}

	private Grid<FcClassificaTotPt> buildTableInfoClassifica(FcCampionato campionato) throws Exception {

		String sql = " select a.desc_attore, ";
		sql += " sum(pt.tot_pt) as tot18, ";
		sql += " sum(pt.tot_pt_old) as tot11, ";
		sql += " sum(pt.tot_pt_rosa) as totRosa, ";
		sql += " sum(pt.pt_tvst) as pt_tvst, ";
		sql += " sum(pt.score) as score18, ";
		sql += " sum(pt.score_old) as score11, ";
		sql += " sum(pt.score_grand_prix) as score_grand_prix ";
		sql += " from fc_classifica_tot_pt pt, ";
		sql += " fc_attore a ";
		sql += " where pt.id_campionato= " + campionato.getIdCampionato();
		sql += " and a.id_attore=pt.id_attore ";
		sql += " group by a.desc_attore ";
		sql += " order by 3 desc ";

		List<FcClassificaTotPt> dm = new ArrayList<FcClassificaTotPt>();

		jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
			@Override
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				String descAttore = "";
				double tot18 = 0;
				double tot11 = 0;
				double totRosa = 0;
				int ptTvsT = 0;
				int score18 = 0;
				int score11 = 0;
				int scoreGrandPrix = 0;

				while (rs.next()) {
					descAttore = rs.getString(1);
					tot18 = rs.getDouble(2);
					tot11 = rs.getDouble(3);
					totRosa = rs.getDouble(4);
					ptTvsT = rs.getInt(5);
					score18 = rs.getInt(6);
					score11 = rs.getInt(7);
					scoreGrandPrix = rs.getInt(8);

					FcClassificaTotPt clasPt = new FcClassificaTotPt();
					FcAttore att = new FcAttore();
					att.setDescAttore(descAttore);
					clasPt.setFcAttore(att);
					clasPt.setTotPt(tot18);
					clasPt.setTotPtOld(tot11);
					clasPt.setTotPtRosa(totRosa);
					clasPt.setPtTvsT(ptTvsT);
					clasPt.setScore(score18);
					clasPt.setScoreOld(score11);
					clasPt.setScoreGrandPrix(scoreGrandPrix);

					dm.add(clasPt);
				}
				return "1";
			}
		});

		Grid<FcClassificaTotPt> grid = new Grid<>();
		grid.setItems(dm);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);
		grid.setAllRowsVisible(true);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setMultiSort(true);

		Column<FcClassificaTotPt> squadraColumn = grid
				.addColumn(classifica -> classifica.getFcAttore().getDescAttore());
		squadraColumn.setSortable(false);
		squadraColumn.setHeader("Squadra");

		Column<FcClassificaTotPt> totPtRosaColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPtRosa() != null ? classifica.getTotPtRosa() / Costants.DIVISORE_100
					: 0;
			String sTotPunti = myFormatter.format(dTotPunti);
			return new Span(sTotPunti);
		}));
		totPtRosaColumn.setHeader("Tot Pt Rosa");
		totPtRosaColumn.setSortable(true);
		totPtRosaColumn.setComparator((p1, p2) -> p1.getTotPtRosa().compareTo(p2.getTotPtRosa()));

		Column<FcClassificaTotPt> ptTvsTColumn = grid.addColumn(classifica -> classifica.getPtTvsT());
		ptTvsTColumn.setHeader("Tot Pt TvsT");
		ptTvsTColumn.setSortable(true);

		Column<FcClassificaTotPt> totpuntiColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPt() != null ? classifica.getTotPt() / Costants.DIVISORE_100 : 0;
			String sTotPunti = myFormatter.format(dTotPunti);
			return new Span(sTotPunti);
		}));
		totpuntiColumn.setHeader("Tot Pt 18");
		totpuntiColumn.setSortable(true);
		totpuntiColumn.setComparator((p1, p2) -> p1.getTotPt().compareTo(p2.getTotPt()));

		Column<FcClassificaTotPt> totpuntioldColumn = grid.addColumn(new ComponentRenderer<>(classifica -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dTotPunti = classifica.getTotPtOld() != null ? classifica.getTotPtOld() / Costants.DIVISORE_100 : 0;
			String sTotPunti = myFormatter.format(dTotPunti);
			return new Span(sTotPunti);
		}));
		totpuntioldColumn.setHeader("Tot Pt 11");
		totpuntioldColumn.setSortable(true);
		totpuntioldColumn.setComparator((p1, p2) -> p1.getTotPtOld().compareTo(p2.getTotPtOld()));

		Column<FcClassificaTotPt> scoreColumn = grid.addColumn(classifica -> classifica.getScore());
		scoreColumn.setHeader("GrandPrix G18");
		scoreColumn.setSortable(true);

		Column<FcClassificaTotPt> scoreoldColumn = grid.addColumn(classifica -> classifica.getScoreOld());
		scoreoldColumn.setHeader("GrandPrix G11");
		scoreoldColumn.setSortable(true);

		Column<FcClassificaTotPt> scoreGrandPrixColumn = grid.addColumn(classifica -> classifica.getScoreGrandPrix());
		scoreGrandPrixColumn.setHeader("GrandPrix F1");
		scoreGrandPrixColumn.setSortable(true);

		HeaderRow headerRow = grid.prependHeaderRow();
		HeaderCell headerCell = headerRow.join(squadraColumn, totpuntiColumn, totpuntioldColumn, totPtRosaColumn,
				ptTvsTColumn, scoreColumn, scoreoldColumn, scoreGrandPrixColumn);
		headerCell.setText("Info Classifiche Generali");

		return grid;
	}

	private ColumnChart buildGrafico(FcCampionato campionato) throws Exception {

		String[] att = new String[8];
		List<ColumnChartValue> values = new ArrayList<ColumnChartValue>();

		List<FcClassifica> all = classificaController.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
		int i = 0;
		for (FcClassifica cl : all) {
			String sq = cl.getFcAttore().getDescAttore();
			double puntiRosa = (cl.getTotPuntiRosa() / Costants.DIVISORE_100);
			att[i] = sq;
			// data.add(puntiRosa);
			values.add(new ColumnChartValue(puntiRosa, sq));
			i++;
		}

		ColumnChart columns = new ColumnChart();
		columns.setValues(values);
		columns.setWidth(600);
		columns.setHeight(400);
		columns.setAxis(ColumnChartAxis.RIGHT);
		columns.setLines(true);
		columns.setColored(true);
		columns.getStyle().set("fontSize", "smaller");

		return columns;
	}

	public ColumnChart buildGraficoTuttiVsTutti(FcCampionato campionato) throws Exception {

		String[] att = new String[8];
		List<ColumnChartValue> values = new ArrayList<ColumnChartValue>();

		List<FcClassifica> all = classificaController.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);

		int i = 0;
		for (FcClassifica cl : all) {
			String sq = cl.getFcAttore().getDescAttore();
			int punti1vsT = cl.getTotPuntiTvsT();
			att[i] = sq;
			// data.add(punti1vsT);
			values.add(new ColumnChartValue(punti1vsT, sq));
			i++;
		}

		ColumnChart columns = new ColumnChart();
		columns.setValues(values);
		columns.setWidth(600);
		columns.setHeight(400);
		columns.setAxis(ColumnChartAxis.RIGHT);
		columns.setLines(true);
		columns.setColored(true);
		columns.getStyle().set("fontSize", "smaller");

		return columns;

	}

}