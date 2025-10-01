package fcweb.ui.views.seriea;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.ClassificaBean;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.backend.service.SquadraService;
import fcweb.backend.service.StatisticheService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Statistiche")
@Route(value = "statistiche", layout = MainLayout.class)
@RolesAllowed("USER")
public class StatisticheView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private StatisticheService statisticheController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	@Autowired
	private SquadraService squadraController;

	@Autowired
	private ResourceLoader resourceLoader;

	private List<FcAttore> squadreA = new ArrayList<>();
	private List<FcAttore> squadreB = new ArrayList<>();
	private ComboBox<FcAttore> comboAttoreA;
	private ComboBox<FcAttore> comboAttoreB;
	private ComboBox<String> comboPunti;

	private List<FcAttore> propretari = new ArrayList<>();
	private List<FcSquadra> squadreSerieA = null;
	private Button salvaStat = null;

	// FILTER
	private ToggleButton toggleP = null;
	private ToggleButton toggleD = null;
	private ToggleButton toggleC = null;
	private ToggleButton toggleA = null;
	private ComboBox<FcSquadra> comboSqudreA;
	private NumberField txtQuotaz;
	private ToggleButton freePlayers = null;
	private ComboBox<FcAttore> comboProprietario;

	private VerticalLayout verticalLayoutGrafico = new VerticalLayout();

	@Autowired
	private AccessoService accessoController;

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initData();
		initLayout();
	}

	private void initData() {
		squadreA = attoreController.findByActive(true);
		squadreB = squadreA;
		squadreSerieA = squadraController.findAll();
		propretari = squadreA;
	}

	private void initLayout() {

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		final VerticalLayout layoutStat = new VerticalLayout();
		setStatisticheA(layoutStat, campionato, att);

		final VerticalLayout layoutConfornti = new VerticalLayout();
		setConfronti(layoutConfornti, campionato, att);

		TabSheet tabSheet = new TabSheet();
		tabSheet.add("Statistiche", layoutStat);
		tabSheet.add("Confronti", layoutConfornti);
		tabSheet.setSizeFull();
		add(tabSheet);

	}

	private void setConfronti(VerticalLayout layout, FcCampionato campionato,
			FcAttore att) {

		HorizontalLayout hlayout1 = new HorizontalLayout();
		hlayout1.setSpacing(true);

		comboAttoreA = new ComboBox<>();
		comboAttoreA.setItems(squadreA);
		comboAttoreA.setItemLabelGenerator(p -> p.getDescAttore());
		comboAttoreA.setValue(att);
		comboAttoreA.setPlaceholder(Costants.SELEZIONA_ATTORE);
		comboAttoreA.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboAttoreB = new ComboBox<>();
		comboAttoreB.setItems(squadreB);
		comboAttoreB.setItemLabelGenerator(p -> p.getDescAttore());
		comboAttoreB.setValue(att);
		comboAttoreB.setPlaceholder(Costants.SELEZIONA_ATTORE);
		comboAttoreB.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboPunti = new ComboBox<>();
		comboPunti.setItems(Costants.PUNTI, Costants.TOTALE_PUNTI, Costants.PT_TVST);
		comboPunti.setValue(Costants.PUNTI);
		comboPunti.setPlaceholder(Costants.CLASSIFICA_PER);
		comboPunti.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		hlayout1.add(comboAttoreA);
		hlayout1.add(comboAttoreB);
		hlayout1.add(comboPunti);

		layout.add(hlayout1);

		verticalLayoutGrafico.removeAll();
		verticalLayoutGrafico.add(buildGrafico(campionato));
		verticalLayoutGrafico.setSizeFull();
		layout.add(verticalLayoutGrafico);

	}

	@SuppressWarnings("rawtypes")
	public Component buildGrafico(FcCampionato campionato) {

		String idAttoreA = "" + comboAttoreA.getValue().getIdAttore();
		String descAttoreA = "" + comboAttoreA.getValue().getDescAttore();
		String idAttoreB = "" + comboAttoreB.getValue().getIdAttore();
		String descAttoreB = "" + comboAttoreB.getValue().getDescAttore();
		String sPunti = comboPunti.getValue();
		List<ClassificaBean> items = classificaTotalePuntiController.getModelGrafico(idAttoreA, idAttoreB, campionato);

		ArrayList<String> giornate = new ArrayList<>();
		ArrayList<Double> dataA = new ArrayList<>();
		ArrayList<Double> dataB = new ArrayList<>();

		for (ClassificaBean c : items) {
			String squadra = c.getSquadra();
			String gg = c.getGiornata();
			double punti = c.getPunti();
			double totPunti = c.getTotPunti();
			double ptTvst = c.getPtTvst();

			giornate.add(gg);

			if (squadra.equals(descAttoreA)) {
				if (sPunti.equals(Costants.PUNTI)) {
					dataA.add(punti);
				} else if (sPunti.equals(Costants.TOTALE_PUNTI)) {
					dataA.add(totPunti);
				} else if (sPunti.equals(Costants.PT_TVST)) {
					dataA.add(ptTvst);
				}

			} else if (squadra.equals(descAttoreB)) {
				if (sPunti.equals(Costants.PUNTI)) {
					dataB.add(punti);
				} else if (sPunti.equals(Costants.TOTALE_PUNTI)) {
					dataB.add(totPunti);
				} else if (sPunti.equals(Costants.PT_TVST)) {
					dataB.add(ptTvst);
				}
			}
		}

		Series primaSerie = new Series<>(descAttoreA,dataA.toArray());
		Series secondaSerie = new Series<>(descAttoreB,dataB.toArray());

		ApexCharts lineChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.LINE).withZoom(ZoomBuilder.get().withEnabled(false).build()).build()).withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).build()).withTitle(TitleSubtitleBuilder.get().withText(Costants.CLASSIFICA_PER + " " + sPunti).withAlign(Align.LEFT).build()).withGrid(GridBuilder.get().withRow(RowBuilder.get().withColors("#f3f3f3", "transparent").withOpacity(0.5).build()).build()).withXaxis(XAxisBuilder.get().withCategories(giornate).build()).withSeries(primaSerie, secondaSerie).build();
		lineChart.setWidth("600px");
		lineChart.setHeight("400px");
		lineChart.setWidth("70%");

		return lineChart;
	}

	private void setStatisticheA(VerticalLayout layout, FcCampionato campionato,
			FcAttore att) {

		HorizontalLayout hlayout1 = new HorizontalLayout();
		hlayout1.setSpacing(true);

		try {

			Button stampaPdf = new Button("Statistiche Voti pdf");
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			Map<String, Object> hm = new HashMap<String, Object>();
			hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
			hm.put("DIVISORE", "" + Costants.DIVISORE_100);
			Resource resource = resourceLoader.getResource("classpath:reports/statisticheVoti.jasper");
			FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(Utils.getStreamResource("StatisticheVoti.pdf", conn, hm, resource.getInputStream()));

			button1Wrapper.wrapComponent(stampaPdf);
			hlayout1.add(button1Wrapper);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			Button stampaPdf2 = new Button("Statistiche Voti Free Players pdf");
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			Map<String, Object> hm = new HashMap<String, Object>();
			hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
			hm.put("DIVISORE", "" + Costants.DIVISORE_100);
			Resource resource = resourceLoader.getResource("classpath:reports/statisticheVotiFreePlayers.jasper");
			FileDownloadWrapper button1Wrapper2 = new FileDownloadWrapper(Utils.getStreamResource("StatisticheVotiFreePlayers.pdf", conn, hm, resource.getInputStream()));

			button1Wrapper2.wrapComponent(stampaPdf2);
			hlayout1.add(button1Wrapper2);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		boolean isAdmin = false;
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				isAdmin = true;
				break;
			}
		}

		if (isAdmin) {
			salvaStat = new Button("Aggiorna Statistiche");
			salvaStat.setIcon(VaadinIcon.DATABASE.create());
			salvaStat.addClickListener(this);
			hlayout1.add(salvaStat);
		}

		layout.add(hlayout1);

		HorizontalLayout hlayoutFilter = new HorizontalLayout();
		hlayoutFilter.setSpacing(true);

		toggleP = new ToggleButton();
		toggleP.setLabel(Costants.P);
		toggleP.setValue(true);
		toggleD = new ToggleButton();
		toggleD.setLabel(Costants.D);
		toggleD.setValue(true);
		toggleC = new ToggleButton();
		toggleC.setLabel(Costants.C);
		toggleC.setValue(true);
		toggleA = new ToggleButton();
		toggleA.setLabel(Costants.A);
		toggleA.setValue(true);

		comboSqudreA = new ComboBox<>(Costants.SQUADRA);
		comboSqudreA.setItems(squadreSerieA);
		comboSqudreA.setItemLabelGenerator(ss -> ss.getNomeSquadra());
		comboSqudreA.setClearButtonVisible(true);
		comboSqudreA.setPlaceholder(Costants.SQUADRA);
		comboSqudreA.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			if (item != null && item.getImg() != null) {
				try {
					Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
					container.add(img);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			Span lblSquadra = new Span(item.getNomeSquadra());
			container.add(lblSquadra);
			return container;
		}));

		txtQuotaz = new NumberField("Quotazione <=");
		txtQuotaz.setMin(0d);
		txtQuotaz.setMax(500d);

		freePlayers = new ToggleButton();
		freePlayers.setLabel("Free Players");
		freePlayers.setValue(false);

		comboProprietario = new ComboBox<>(Costants.PROPETARIO);
		comboProprietario.setItems(propretari);
		comboProprietario.setItemLabelGenerator(ss -> ss.getDescAttore());
		comboProprietario.setClearButtonVisible(true);
		comboProprietario.setPlaceholder(Costants.PROPETARIO);
		comboProprietario.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			Span lblProp = new Span(item.getDescAttore());
			container.add(lblProp);
			return container;
		}));

		hlayoutFilter.add(toggleP);
		hlayoutFilter.add(toggleD);
		hlayoutFilter.add(toggleC);
		hlayoutFilter.add(toggleA);
		hlayoutFilter.add(comboSqudreA);
		hlayoutFilter.add(txtQuotaz);
		hlayoutFilter.add(freePlayers);
		hlayoutFilter.add(comboProprietario);

		layout.add(hlayoutFilter);

		List<FcStatistiche> items = statisticheController.findByFlagAttivo(true);

		PaginatedGrid<FcStatistiche, ?> grid = new PaginatedGrid<>();
		ListDataProvider<FcStatistiche> dataProvider = new ListDataProvider<>(items);
		grid.setDataProvider(dataProvider);

		toggleP.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		toggleD.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		toggleC.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		toggleA.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		comboSqudreA.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		txtQuotaz.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		freePlayers.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		comboProprietario.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});

		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setMultiSort(true);
		grid.setAllRowsVisible(true);

		Column<FcStatistiche> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();
			if (g != null && g.getIdRuolo() != null) {
				Image img = Utils.buildImage(g.getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + g.getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setKey(Costants.RUOLO);
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader(Costants.R);
		ruoloColumn.setAutoWidth(true);

		Column<FcStatistiche> giocatoreColumn = grid.addColumn(s -> s.getCognGiocatore()).setKey(Costants.GIOCATORE);
		giocatoreColumn.setSortable(true);
		giocatoreColumn.setHeader(Costants.GIOCATORE);
		giocatoreColumn.setAutoWidth(true);

		Column<FcStatistiche> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(s -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null && s.getNomeSquadra() != null) {
				Image img = Utils.buildImage(s.getNomeSquadra() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMG_SQUADRE + s.getNomeSquadra() + ".png"));
				Span lblSquadra = new Span(s.getNomeSquadra());
				cellLayout.add(img);
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		nomeSquadraColumn.setSortable(true);
		nomeSquadraColumn.setComparator((p1,
				p2) -> p1.getNomeSquadra().compareTo(p2.getNomeSquadra()));
		nomeSquadraColumn.setHeader(Costants.SQUADRA);
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcStatistiche> quotazioneColumn = grid.addColumn(s -> s.getFcGiocatore() != null ? s.getFcGiocatore().getQuotazione() : 0);
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader(Costants.Q);
		quotazioneColumn.setAutoWidth(true);

		Column<FcStatistiche> proprietarioColumn = grid.addColumn(s -> s.getProprietario()).setKey(Costants.PROPETARIO);
		proprietarioColumn.setSortable(true);
		proprietarioColumn.setHeader(Costants.PROPETARIO);
		proprietarioColumn.setAutoWidth(true);

		Column<FcStatistiche> giocateColumn = grid.addColumn(s -> s.getGiocate()).setKey(Costants.GIOCATE);
		giocateColumn.setSortable(true);
		giocateColumn.setHeader(Costants.GIOCATE);
		giocateColumn.setAutoWidth(true);

		Column<FcStatistiche> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			if (s != null && s.getFcGiocatore() != null) {
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getMediaVoto() / Costants.DIVISORE_100;
				}
				String sTotPunti = myFormatter.format(d);
				Span lbl = new Span(sTotPunti);

				cellLayout.add(img);
				cellLayout.add(lbl);

			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator((p1,
				p2) -> p1.getMediaVoto().compareTo(p2.getMediaVoto()));
		mediaVotoColumn.setHeader(Costants.MV);
		mediaVotoColumn.setAutoWidth(true);

		Column<FcStatistiche> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			if (s != null && s.getFcGiocatore() != null) {
				String imgThink = "2.png";
				if (s != null && s.getFantaMedia() != 0) {
					if (s.getFantaMedia() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getFantaMedia() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getFantaMedia() / Costants.DIVISORE_100;
				}
				String sTotPunti = myFormatter.format(d);
				Span lbl = new Span(sTotPunti);

				cellLayout.add(img);
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		fantaMediaColumn.setSortable(true);
		fantaMediaColumn.setComparator((p1,
				p2) -> p1.getFantaMedia().compareTo(p2.getFantaMedia()));
		fantaMediaColumn.setHeader(Costants.FMV);
		fantaMediaColumn.setAutoWidth(true);

		Column<FcStatistiche> golFattoColumn = grid.addColumn(s -> s.getGoalFatto()).setKey("golFatto");
		golFattoColumn.setSortable(true);
		golFattoColumn.setHeader(Costants.G + "+");
		golFattoColumn.setAutoWidth(true);

		Column<FcStatistiche> golSubitoColumn = grid.addColumn(s -> s.getGoalSubito()).setKey("golSubito");
		golSubitoColumn.setSortable(true);
		golSubitoColumn.setHeader(Costants.G + "-");
		golSubitoColumn.setAutoWidth(true);

		Column<FcStatistiche> assistColumn = grid.addColumn(s -> s.getAssist()).setKey("assist");
		assistColumn.setSortable(true);
		assistColumn.setHeader(Costants.ASSIST);
		assistColumn.setAutoWidth(true);

		Column<FcStatistiche> ammonizioneColumn = grid.addColumn(s -> s.getAmmonizione()).setKey("ammonizione");
		ammonizioneColumn.setSortable(true);
		ammonizioneColumn.setHeader(Costants.AMM);
		ammonizioneColumn.setAutoWidth(true);

		Column<FcStatistiche> espulsioneColumn = grid.addColumn(s -> s.getEspulsione()).setKey("espulsione");
		espulsioneColumn.setSortable(true);
		espulsioneColumn.setHeader(Costants.ESP);
		espulsioneColumn.setAutoWidth(true);

		// Sets the max number of items to be rendered on the grid for each page
		grid.setPageSize(25);

		// Sets how many pages should be visible on the pagination before and/or
		// after the current selected page
		grid.setPaginatorSize(5);

		layout.add(grid);

	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
			if (event.getSource() == salvaStat) {
				jobProcessGiornata.statistiche(campionato);
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void applyFilter(ListDataProvider<FcStatistiche> dataProvider) {

		dataProvider.clearFilters();

		if (toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equals(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.C) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P));
		} else if (!toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.D));
		} else if (!toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.C));
		} else if (!toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.D));
		} else if (toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.C));
		} else if (!toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.C) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (!toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.C));
		} else if (!toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.C) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (!toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && !toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.C));
		} else if (toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (toggleP.getValue().booleanValue() && toggleD.getValue().booleanValue() && !toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.D) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else if (toggleP.getValue().booleanValue() && !toggleD.getValue().booleanValue() && toggleC.getValue().booleanValue() && toggleA.getValue().booleanValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase(Costants.P) || s.getIdRuolo().equalsIgnoreCase(Costants.C) || s.getIdRuolo().equalsIgnoreCase(Costants.A));
		} else {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("-"));
		}

		if (comboSqudreA.getValue() != null) {
			dataProvider.addFilter(s -> comboSqudreA.getValue().getNomeSquadra().equals(s.getNomeSquadra()));
		}

		if (txtQuotaz.getValue() != null) {
			dataProvider.addFilter(s -> s.getFcGiocatore().getQuotazione().intValue() <= txtQuotaz.getValue().intValue());
		}

		if (freePlayers.getValue().booleanValue()) {
			dataProvider.addFilter(s -> StringUtils.isEmpty(s.getProprietario()));
		}

		if (comboProprietario.getValue() != null) {
			dataProvider.addFilter(s -> comboProprietario.getValue().getDescAttore().equals(s.getProprietario()));
		}

	}

}