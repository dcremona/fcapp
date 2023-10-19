package fcweb.ui.views.em;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.klaudeta.PaginatedGrid;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.SquadraService;
import fcweb.backend.service.StatisticheService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;


@PageTitle("Statistiche")
@Route(value = "emstatistiche", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmStatisticheView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

//	@Autowired
//	private ClassificaTotalePuntiService classificaTotalePuntiController;

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

	public List<FcAttore> squadreA = new ArrayList<FcAttore>();
	public List<FcAttore> squadreB = new ArrayList<FcAttore>();
	private ComboBox<FcAttore> comboAttoreA;
	private ComboBox<FcAttore> comboAttoreB;
	private ComboBox<String> comboPunti;

	private List<FcSquadra> squadre = null;
	private Button salvaStat = null;

	private ToggleButton toggleP = null;
	private ToggleButton toggleD = null;
	private ToggleButton toggleC = null;
	private ToggleButton toggleA = null;
	// FILTER
	private ComboBox<FcSquadra> comboNazione;
	private NumberField txtQuotaz;
	private RadioButtonGroup<String> radioGroup = null;

	private VerticalLayout verticalLayoutGrafico = new VerticalLayout();

	@Autowired
	private AccessoService accessoController;

	@PostConstruct
	void init() {
		LOG.info("init");
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
		squadre = squadraController.findAll();
	}

	private void initLayout() {

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		final VerticalLayout layoutStat = new VerticalLayout();
		setStatisticheA(layoutStat, campionato, p, att);

		final VerticalLayout layoutConfornti = new VerticalLayout();
		setConfronti(layoutConfornti, campionato, att);

//		VerticalLayout container = new VerticalLayout();
//		PagedTabs tabs = new PagedTabs(container);
//		tabs.add("Statistiche", layoutStat, false);
//		tabs.add("Confronti", layoutConfornti, false);
//		add(tabs, container);

		TabSheet tabSheet = new TabSheet();
		tabSheet.add("Statistiche", layoutStat);
		tabSheet.add("Confronti", layoutConfornti);
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
		comboAttoreA.setPlaceholder("Seleziona Attore");
		comboAttoreA.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
//			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboAttoreB = new ComboBox<>();
		comboAttoreB.setItems(squadreB);
		comboAttoreB.setItemLabelGenerator(p -> p.getDescAttore());
		comboAttoreB.setValue(att);
		comboAttoreB.setPlaceholder("Seleziona Attore");
		comboAttoreB.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
//			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboPunti = new ComboBox<>();
		comboPunti.setItems("TOTALE_PUNTI");
		comboPunti.setValue("TOTALE_PUNTI");
		comboPunti.setPlaceholder("Claasifica per");
		comboPunti.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
//			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		hlayout1.add(comboAttoreA);
		hlayout1.add(comboAttoreB);
		hlayout1.add(comboPunti);

		layout.add(hlayout1);

		verticalLayoutGrafico.removeAll();
//		verticalLayoutGrafico.add(buildGrafico(campionato));
		layout.add(verticalLayoutGrafico);

	}

//	@SuppressWarnings("rawtypes")
//	public Component buildGrafico(FcCampionato campionato) {
//
//		String idAttoreA = "" + comboAttoreA.getValue().getIdAttore();
//		String descAttoreA = "" + comboAttoreA.getValue().getDescAttore();
//		String idAttoreB = "" + comboAttoreB.getValue().getIdAttore();
//		String descAttoreB = "" + comboAttoreB.getValue().getDescAttore();
//		String sPunti = comboPunti.getValue();
//		List<ClassificaBean> items = classificaTotalePuntiController.getModelGraficoEm(idAttoreA, idAttoreB, campionato);
//
//		ArrayList<String> giornate = new ArrayList<>();
//		ArrayList<Double> dataA = new ArrayList<>();
//		ArrayList<Double> dataB = new ArrayList<>();
//
//		for (ClassificaBean c : items) {
//			String squadra = c.getSquadra();
//			String gg = c.getGiornata();
//			double totPunti = c.getTotPunti();
//
//			giornate.add(gg);
//
//			if (squadra.equals(descAttoreA)) {
//				dataA.add(totPunti);
//			} else if (squadra.equals(descAttoreB)) {
//				dataB.add(totPunti);
//			}
//		}
//
//		Series primaSerie = new Series<>(descAttoreA,dataA.toArray());
//		Series secondaSerie = new Series<>(descAttoreB,dataB.toArray());
//
//		ApexCharts lineChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.LINE).withZoom(ZoomBuilder.get().withEnabled(false).build()).build()).withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).build()).withTitle(TitleSubtitleBuilder.get().withText("Classifica per " + sPunti).withAlign(Align.LEFT).build()).withGrid(GridBuilder.get().withRow(RowBuilder.get().withColors("#f3f3f3", "transparent").withOpacity(0.5).build()).build()).withXaxis(XAxisBuilder.get().withCategories(giornate).build()).withSeries(primaSerie, secondaSerie).build();
//
//		lineChart.setWidth("80%");
//
//		return lineChart;
//	}

	private void setStatisticheA(VerticalLayout layout, FcCampionato campionato,
			Properties p, FcAttore att) {

		HorizontalLayout hlayout1 = new HorizontalLayout();
		hlayout1.setSpacing(true);

		try {

			Button stampaPdf = new Button("Statistiche Voti pdf");
			FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(new StreamResource("StatisticheVoti.pdf",() -> {
				try {
					Connection conn = jdbcTemplate.getDataSource().getConnection();
					Map<String, Object> hm = new HashMap<String, Object>();
					hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
					hm.put("DIVISORE", "" + Costants.DIVISORE_100);
					Resource resource = resourceLoader.getResource("classpath:reports/statisticheVoti.jasper");
					InputStream inputStream = resource.getInputStream();
					return JasperReporUtils.runReportToPdf(inputStream, hm, conn);
				} catch (Exception ex2) {
					LOG.error(ex2.toString());
				}
				return null;
			}));
			button1Wrapper.wrapComponent(stampaPdf);
			hlayout1.add(button1Wrapper);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
		
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				salvaStat = new Button("Aggiorna Statistiche");
				salvaStat.setIcon(VaadinIcon.DATABASE.create());
				salvaStat.addClickListener(this);
				hlayout1.add(salvaStat);

				break;
			}
		}
		
		layout.add(hlayout1);

		HorizontalLayout hlayoutFilter = new HorizontalLayout();
		hlayoutFilter.setSpacing(true);

		toggleP = new ToggleButton();
		toggleP.setLabel("P");
		toggleP.setValue(true);
		toggleD = new ToggleButton();
		toggleD.setLabel("D");
		toggleD.setValue(true);
		toggleC = new ToggleButton();
		toggleC.setLabel("C");
		toggleC.setValue(true);
		toggleA = new ToggleButton();
		toggleA.setLabel("A");
		toggleA.setValue(true);

		comboNazione = new ComboBox<>("Nazione");
		comboNazione.setItems(squadre);
		comboNazione.setItemLabelGenerator(s -> s.getNomeSquadra());
		comboNazione.setClearButtonVisible(true);
		comboNazione.setPlaceholder("Nazione");
		comboNazione.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			if (item.getImg() != null) {
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
		txtQuotaz.setStepButtonsVisible(true);

		radioGroup = new RadioButtonGroup<>();
		radioGroup.setLabel("Giocatori");
		radioGroup.setItems("Tutti", "Attivi", "Non Attivi");
		radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		radioGroup.setValue("Tutti");

		hlayoutFilter.add(toggleP);
		hlayoutFilter.add(toggleD);
		hlayoutFilter.add(toggleC);
		hlayoutFilter.add(toggleA);
		hlayoutFilter.add(comboNazione);
		hlayoutFilter.add(txtQuotaz);
		hlayoutFilter.add(radioGroup);

		layout.add(hlayoutFilter);

		List<FcStatistiche> items = statisticheController.findAll();

		PaginatedGrid<FcStatistiche,?> grid = new PaginatedGrid<>();
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
		comboNazione.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		txtQuotaz.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});
		radioGroup.addValueChangeListener(event -> {
			applyFilter(dataProvider);
		});

		grid.setSelectionMode(Grid.SelectionMode.NONE);
		//grid.setHeightByRows(true);
		// grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
		// GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setMultiSort(true);
		grid.setAllRowsVisible(true);
		// grid.setSizeFull();

		Column<FcStatistiche> ruoloColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null && s.getIdRuolo() != null) {
				Image img = buildImage("classpath:images/", s.getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setKey("ruolo");
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("R");
		ruoloColumn.setAutoWidth(true);

		// Column<FcStatistiche> giocatoreColumn = grid.addColumn(s ->
		// s.getCognGiocatore()).setKey("giocatore");
		Column<FcStatistiche> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				if (s.getCognGiocatore() != null) {
					Span span = new Span();
					span.setText(s.getCognGiocatore());
					cellLayout.add(span);
				}
			}
			return cellLayout;
		}));
		giocatoreColumn.setSortable(true);
		giocatoreColumn.setHeader("Giocatore");
		// giocatoreColumn.setWidth("150px");
		giocatoreColumn.setAutoWidth(true);

		Column<FcStatistiche> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				if (s.getNomeSquadra() != null) {
					FcSquadra sq = squadraController.findByNomeSquadra(s.getNomeSquadra());
					if (sq.getImg() != null) {
						try {
							Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
							cellLayout.add(img);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					Span span = new Span();
					span.setText(sq.getNomeSquadra());
					cellLayout.add(span);
				}
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setSortable(true);
		nomeSquadraColumn.setComparator((p1,
				p2) -> p1.getNomeSquadra().compareTo(p2.getNomeSquadra()));
		nomeSquadraColumn.setHeader("Squadra");
		nomeSquadraColumn.setWidth("100px");
		nomeSquadraColumn.setAutoWidth(true);

		// Column<FcStatistiche> quotazioneColumn = grid.addColumn(s ->
		// s.getFcGiocatore() != null ? s.getFcGiocatore().getQuotazione() : 0);
		Column<FcStatistiche> quotazioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getFcGiocatore().getQuotazione();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		quotazioneColumn.setAutoWidth(true);

		// Column<FcStatistiche> giocateColumn = grid.addColumn(s ->
		// s.getGiocate()).setKey("giocate");
		Column<FcStatistiche> giocateColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getGiocate();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		giocateColumn.setSortable(true);
		giocateColumn.setHeader("Giocate");
		giocateColumn.setAutoWidth(true);

		Column<FcStatistiche> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(true);
			if (s != null && s.getFcGiocatore() != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.EM_RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.EM_RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getMediaVoto() / Costants.DIVISORE_10;
				}
				String sTotPunti = myFormatter.format(d);
				Span span = new Span();
				span.setText(sTotPunti);
				span.add(img);
				cellLayout.add(span);

			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator((p1,
				p2) -> p1.getMediaVoto().compareTo(p2.getMediaVoto()));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setAutoWidth(true);

		Column<FcStatistiche> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(true);
			if (s != null && s.getFcGiocatore() != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String imgThink = "2.png";
				if (s != null && s.getFantaMedia() != 0) {
					if (s.getFantaMedia() > Costants.EM_RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getFantaMedia() < Costants.EM_RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getFantaMedia() / Costants.DIVISORE_10;
				}
				String sTotPunti = myFormatter.format(d);
				Span span = new Span();
				span.setText(sTotPunti);
				span.add(img);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		fantaMediaColumn.setSortable(true);
		fantaMediaColumn.setComparator((p1,
				p2) -> p1.getFantaMedia().compareTo(p2.getFantaMedia()));
		fantaMediaColumn.setHeader("FMv");
		fantaMediaColumn.setAutoWidth(true);

		// Column<FcStatistiche> golFattoColumn = grid.addColumn(s ->
		// s.getGoalFatto()).setKey("golFatto");
		Column<FcStatistiche> golFattoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getGoalFatto();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		golFattoColumn.setSortable(true);
		golFattoColumn.setHeader("G+");
		golFattoColumn.setAutoWidth(true);

		// Column<FcStatistiche> golSubitoColumn = grid.addColumn(s ->
		// s.getGoalSubito()).setKey("golSubito");
		Column<FcStatistiche> golSubitoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getGoalSubito();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		golSubitoColumn.setSortable(true);
		golSubitoColumn.setHeader("G-");
		golSubitoColumn.setAutoWidth(true);

		// Column<FcStatistiche> assistColumn = grid.addColumn(s ->
		// s.getAssist()).setKey("assist");
		Column<FcStatistiche> assistColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getAssist();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		assistColumn.setSortable(true);
		assistColumn.setHeader("Ass");
		assistColumn.setAutoWidth(true);

		// Column<FcStatistiche> ammonizioneColumn = grid.addColumn(s ->
		// s.getAmmonizione()).setKey("ammonizione");
		Column<FcStatistiche> ammonizioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getAmmonizione();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		ammonizioneColumn.setSortable(true);
		ammonizioneColumn.setHeader("Amm");
		ammonizioneColumn.setAutoWidth(true);

		// Column<FcStatistiche> espulsioneColumn = grid.addColumn(s ->
		// s.getEspulsione()).setKey("espulsione");
		Column<FcStatistiche> espulsioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String q = "" + s.getEspulsione();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		espulsioneColumn.setSortable(true);
		espulsioneColumn.setHeader("Esp");
		espulsioneColumn.setAutoWidth(true);

		// Sets the max number of items to be rendered on the grid for each page
		grid.setPageSize(16);

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

		if (toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("C") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P"));
		} else if (!toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("D"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("C"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("A"));
		} else if (toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("D"));
		} else if (toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("C"));
		} else if (!toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("C") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (!toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("C"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("C") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (!toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("C"));
		} else if (toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("D") || s.getIdRuolo().toUpperCase().equals("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("P") || s.getIdRuolo().toUpperCase().equals("C") || s.getIdRuolo().toUpperCase().equals("A"));
		} else {
			dataProvider.addFilter(s -> s.getIdRuolo().toUpperCase().equals("-"));
		}

		if (comboNazione.getValue() != null) {
			dataProvider.addFilter(s -> comboNazione.getValue().getNomeSquadra().equals(s.getNomeSquadra()));
		}
		if (txtQuotaz.getValue() != null) {
			dataProvider.addFilter(s -> s.getFcGiocatore().getQuotazione().intValue() <= txtQuotaz.getValue().intValue());
		}

		if ("Attivi".equals(radioGroup.getValue())) {
			dataProvider.addFilter(s -> s.isFlagAttivo());
		} else if ("Non Attivi".equals(radioGroup.getValue())) {
			dataProvider.addFilter(s -> !s.isFlagAttivo());
		}
	}

	private Image buildImage(String path, String nomeImg) {
		StreamResource resource = new StreamResource(nomeImg,() -> {
			Resource r = resourceLoader.getResource(path + nomeImg);
			InputStream inputStream = null;
			try {
				inputStream = r.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return inputStream;
		});

		Image img = new Image(resource,"");
		return img;
	}

}