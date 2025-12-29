package fcweb.ui.views.em;

import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

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
@Route(value = "emstatistiche", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmStatisticheView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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

	public List<FcAttore> squadreA = new ArrayList<>();
	public List<FcAttore> squadreB = new ArrayList<>();
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

	private final VerticalLayout verticalLayoutGrafico = new VerticalLayout();

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

        VaadinSession.getCurrent().getAttribute("PROPERTIES");
        FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		final VerticalLayout layoutStat = new VerticalLayout();
		setStatisticheA(layoutStat, campionato, att);

		final VerticalLayout layoutConfornti = new VerticalLayout();
		setConfronti(layoutConfornti, campionato, att);

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
		comboAttoreA.setItemLabelGenerator(FcAttore::getDescAttore);
		comboAttoreA.setValue(att);
		comboAttoreA.setPlaceholder("Seleziona Attore");
		comboAttoreA.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboAttoreB = new ComboBox<>();
		comboAttoreB.setItems(squadreB);
		comboAttoreB.setItemLabelGenerator(FcAttore::getDescAttore);
		comboAttoreB.setValue(att);
		comboAttoreB.setPlaceholder("Seleziona Attore");
		comboAttoreB.addValueChangeListener(event -> {
			verticalLayoutGrafico.removeAll();
			verticalLayoutGrafico.add(buildGrafico(campionato));
		});

		comboPunti = new ComboBox<>();
		comboPunti.setItems("TOTALE_PUNTI");
		comboPunti.setValue("TOTALE_PUNTI");
		comboPunti.setPlaceholder("Claasifica per");
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
		layout.add(verticalLayoutGrafico);

	}

	@SuppressWarnings("rawtypes")
	public Component buildGrafico(FcCampionato campionato) {

		String idAttoreA = "" + comboAttoreA.getValue().getIdAttore();
		String descAttoreA = comboAttoreA.getValue().getDescAttore();
		String idAttoreB = "" + comboAttoreB.getValue().getIdAttore();
		String descAttoreB = comboAttoreB.getValue().getDescAttore();
		String sPunti = comboPunti.getValue();
		List<ClassificaBean> items = classificaTotalePuntiController.getModelGraficoEm(idAttoreA, idAttoreB, campionato);

		ArrayList<String> giornate = new ArrayList<>();
		ArrayList<Double> dataA = new ArrayList<>();
		ArrayList<Double> dataB = new ArrayList<>();

		for (ClassificaBean c : items) {
			String squadra = c.getSquadra();
			String gg = c.getGiornata();
			double totPunti = c.getTotPunti();

			giornate.add(gg);

			if (squadra.equals(descAttoreA)) {
				dataA.add(totPunti);
			} else if (squadra.equals(descAttoreB)) {
				dataB.add(totPunti);
			}
		}

		Series primaSerie = new Series<>(descAttoreA,dataA.toArray());
		Series secondaSerie = new Series<>(descAttoreB,dataB.toArray());

		ApexCharts lineChart = ApexChartsBuilder.get().withChart(ChartBuilder.get().withType(Type.LINE).withZoom(ZoomBuilder.get().withEnabled(false).build()).build()).withStroke(StrokeBuilder.get().withCurve(Curve.STRAIGHT).build()).withTitle(TitleSubtitleBuilder.get().withText("Classifica per " + sPunti).withAlign(Align.LEFT).build()).withGrid(GridBuilder.get().withRow(RowBuilder.get().withColors("#f3f3f3", "transparent").withOpacity(0.5).build()).build()).withXaxis(XAxisBuilder.get().withCategories(giornate).build()).withSeries(primaSerie, secondaSerie).build();

		lineChart.setWidth("80%");

		return lineChart;
	}

	private void setStatisticheA(VerticalLayout layout, FcCampionato campionato,
								 FcAttore att) {

		HorizontalLayout hlayout1 = new HorizontalLayout();
		hlayout1.setSpacing(true);

        try {

			Button stampaPdf = new Button("Statistiche Voti pdf");
            assert jdbcTemplate.getDataSource() != null;
            Connection conn = jdbcTemplate.getDataSource().getConnection();
			Map<String, Object> hm = new HashMap<>();
			hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
			hm.put("DIVISORE", "" + Costants.DIVISORE_10);
			Resource resource = resourceLoader.getResource("classpath:reports/statisticheVoti.jasper");
			FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(Utils.getStreamResource("StatisticheVoti.pdf", conn, hm, resource.getInputStream()));

			button1Wrapper.wrapComponent(stampaPdf);
			hlayout1.add(button1Wrapper);

		} catch (Exception e) {
			LOG.error(e.getMessage());
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
		comboNazione.setItemLabelGenerator(FcSquadra::getNomeSquadra);
		comboNazione.setClearButtonVisible(true);
		comboNazione.setPlaceholder("Nazione");
		comboNazione.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			if (item.getImg() != null) {
				try {
					Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
					container.add(img);
				} catch (SQLException e) {
					LOG.error(e.getMessage());
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

		PaginatedGrid<FcStatistiche, ?> grid = new PaginatedGrid<>();
		ListDataProvider<FcStatistiche> dataProvider = new ListDataProvider<>(items);
		grid.setDataProvider(dataProvider);

		toggleP.addValueChangeListener(event -> applyFilter(dataProvider));
		toggleD.addValueChangeListener(event -> applyFilter(dataProvider));
		toggleC.addValueChangeListener(event -> applyFilter(dataProvider));
		toggleA.addValueChangeListener(event -> applyFilter(dataProvider));
		comboNazione.addValueChangeListener(event -> applyFilter(dataProvider));
		txtQuotaz.addValueChangeListener(event -> applyFilter(dataProvider));
		radioGroup.addValueChangeListener(event -> applyFilter(dataProvider));

		grid.setSelectionMode(Grid.SelectionMode.NONE);
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
				Image img = Utils.buildImage(s.getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + s.getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setKey(Costants.RUOLO);
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("R");
		ruoloColumn.setAutoWidth(true);

        Column<FcStatistiche> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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
		giocatoreColumn.setHeader(Costants.GIOCATORE);
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
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				if (s.getNomeSquadra() != null) {
					FcSquadra sq = squadraController.findByNomeSquadra(s.getNomeSquadra());
					if (sq.getImg() != null) {
						try {
							Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
							cellLayout.add(img);
						} catch (SQLException e) {
							LOG.error(e.getMessage());
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
		nomeSquadraColumn.setComparator(Comparator.comparing(FcStatistiche::getNomeSquadra));
		nomeSquadraColumn.setHeader(Costants.SQUADRA);
		nomeSquadraColumn.setWidth("100px");
		nomeSquadraColumn.setAutoWidth(true);

        Column<FcStatistiche> quotazioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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

        Column<FcStatistiche> giocateColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String imgThink = "2.png";
				if (s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.EM_RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.EM_RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d;
                d = s.getMediaVoto() / Costants.DIVISORE_10;
                String sTotPunti = myFormatter.format(d);
				Span span = new Span();
				span.setText(sTotPunti);
				span.add(img);
				cellLayout.add(span);

			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator(Comparator.comparing(FcStatistiche::getMediaVoto));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setAutoWidth(true);

		Column<FcStatistiche> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(true);
			if (s != null && s.getFcGiocatore() != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				String imgThink = "2.png";
				if (s.getFantaMedia() != 0) {
					if (s.getFantaMedia() > Costants.EM_RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getFantaMedia() < Costants.EM_RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}

				Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d;
                d = s.getFantaMedia() / Costants.DIVISORE_10;
                String sTotPunti = myFormatter.format(d);
				Span span = new Span();
				span.setText(sTotPunti);
				span.add(img);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		fantaMediaColumn.setSortable(true);
		fantaMediaColumn.setComparator(Comparator.comparing(FcStatistiche::getFantaMedia));
		fantaMediaColumn.setHeader("FMv");
		fantaMediaColumn.setAutoWidth(true);

        Column<FcStatistiche> golFattoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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

        Column<FcStatistiche> golSubitoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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

        Column<FcStatistiche> assistColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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

        Column<FcStatistiche> ammonizioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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

        Column<FcStatistiche> espulsioneColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (s != null) {
				if (!s.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("C") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P"));
		} else if (!toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("D"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("C"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("D"));
		} else if (toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("C"));
		} else if (!toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("C") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (!toggleP.getValue() && toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("C"));
		} else if (!toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("C") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (!toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && !toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("C"));
		} else if (toggleP.getValue() && !toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (toggleP.getValue() && toggleD.getValue() && !toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("D") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else if (toggleP.getValue() && !toggleD.getValue() && toggleC.getValue() && toggleA.getValue()) {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("P") || s.getIdRuolo().equalsIgnoreCase("C") || s.getIdRuolo().equalsIgnoreCase("A"));
		} else {
			dataProvider.addFilter(s -> s.getIdRuolo().equalsIgnoreCase("-"));
		}

		if (comboNazione.getValue() != null) {
			dataProvider.addFilter(s -> comboNazione.getValue().getNomeSquadra().equals(s.getNomeSquadra()));
		}
		if (txtQuotaz.getValue() != null) {
			dataProvider.addFilter(s -> s.getFcGiocatore().getQuotazione() <= txtQuotaz.getValue().intValue());
		}

		if ("Attivi".equals(radioGroup.getValue())) {
			dataProvider.addFilter(FcStatistiche::isFlagAttivo);
		} else if ("Non Attivi".equals(radioGroup.getValue())) {
			dataProvider.addFilter(s -> !s.isFlagAttivo());
		}
	}

}