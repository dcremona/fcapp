package fcweb.ui.views.em;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcExpRosea;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ExpRoseAService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;


@PageTitle("Download")
@Route(value = "emdownnload", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmDownloadView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private Grid<FcExpRosea> gridRosea = new Grid<>();

	@Autowired
	private ExpRoseAService expRoseAController;

//	@Autowired
//	private StatisticheController statisticheController;

	@Autowired
	private AttoreService attoreController;

//	@Autowired
//	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	@Autowired
	private ResourceLoader resourceLoader;

	public List<FcAttore> squadre = new ArrayList<>();

	private Button salvaRoseA = null;
	private Button salvaStat = null;

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
		squadre = attoreController.findByActive(true);
	}

	private void initLayout() {

//		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		//FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		salvaRoseA = new Button("Aggiorna");
		salvaRoseA.setIcon(VaadinIcon.DATABASE.create());
		salvaRoseA.addClickListener(this);

		salvaStat = new Button("Aggiorna");
		salvaStat.setIcon(VaadinIcon.DATABASE.create());
		salvaStat.addClickListener(this);

		final VerticalLayout layout1 = new VerticalLayout();
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				layout1.add(salvaRoseA);
				break;
			}
		}
		setRoseA(layout1);

//		final VerticalLayout layout3 = new VerticalLayout();
//		if (att.isAdmin()) {
//			layout3.add(salvaStat);
//		}
//		setStatisticheA(layout3, campionato, p);

//		VerticalLayout container = new VerticalLayout();
//		PagedTabs tabs = new PagedTabs(container);
//		tabs.add("Rose Nazionali", layout1, false);
//		add(tabs, container);

		TabSheet tabSheet = new TabSheet();
		tabSheet.add("Rose Nazionali", layout1);
		tabSheet.setSizeFull();
		add(tabSheet);

	}

	private void setRoseA(VerticalLayout layout) {

		List<FcExpRosea> items = expRoseAController.findAll();

//		gridRosea.setItems(items);
//		gridRosea.setSelectionMode(Grid.SelectionMode.NONE);
//		gridRosea.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
//		gridRosea.setAllRowsVisible(true);
//		gridRosea.getStyle().set("fontSize", "smaller");

		gridRosea.setItems(items);
		gridRosea.setSelectionMode(Grid.SelectionMode.SINGLE);
		gridRosea.setAllRowsVisible(true);
		gridRosea.addThemeVariants(GridVariant.LUMO_COMPACT);

		for (int i = 1; i < 11; i++) {

			Column<FcExpRosea> sxColumn = null;
			Column<FcExpRosea> rxColumn = null;
			Column<FcExpRosea> qxColumn = null;
			if (i == 1) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS1());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ1());
			} else if (i == 2) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS2());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ2());
			} else if (i == 3) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS3());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ3());
			} else if (i == 4) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS4());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ4());
			} else if (i == 5) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS5());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ5());
			} else if (i == 6) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS6());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ6());
			} else if (i == 7) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS7());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ7());
			} else if (i == 8) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS8());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ8());
			} else if (i == 9) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS9());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ9());
			} else if (i == 10) {
				sxColumn = gridRosea.addColumn(expRosea -> expRosea.getS10());
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(expRosea -> expRosea.getQ10());
			}

			sxColumn.setKey("s" + i);
			// s1Column.setHeader("s1");
			// sxColumn.setFlexGrow(0);
			sxColumn.setWidth("110px");
			// sxColumn.setResizable(false);
			sxColumn.setAutoWidth(true);

			rxColumn.setKey("r" + i);
			// r1Column.setHeader("r1");
			// rxColumn.setFlexGrow(0);
			rxColumn.setWidth("60px");
			// rxColumn.setResizable(false);
			rxColumn.setAutoWidth(true);

			qxColumn.setKey("q" + i);
			// q1Column.setHeader("r1");
			// qxColumn.setFlexGrow(0);
			qxColumn.setWidth("30px");
			// qxColumn.setResizable(false);
			qxColumn.setAutoWidth(true);

		}

//		Anchor downloadAsExcel = new Anchor(new StreamResource("Nazionali.xlsx",Exporter.exportAsExcel(gridRosea)),"Download As Excel");
//		Anchor downloadAsCSV = new Anchor(new StreamResource("Nazionali.csv",Exporter.exportAsCSV(gridRosea)),"Download As CSV");
//		layout.add(new HorizontalLayout(downloadAsExcel,downloadAsCSV));

		GridExporter<FcExpRosea> exporter = GridExporter.createFor(gridRosea);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Nazionali");
		exporter.setFileName("Nazionali" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("", "Export to Excel");
		excelLink.setHref(exporter.getExcelStreamResource());
		excelLink.getElement().setAttribute("download", true);
		layout.add(new HorizontalLayout(excelLink));

		layout.add(gridRosea);
	}

	private Column<FcExpRosea> getColumnR(Grid<FcExpRosea> grid, int i) {

		Column<FcExpRosea> rxColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();

			String ruolo = null;
			if (f != null) {
				if (i == 1) {
					ruolo = f.getR1();
				} else if (i == 2) {
					ruolo = f.getR2();
				} else if (i == 3) {
					ruolo = f.getR3();
				} else if (i == 4) {
					ruolo = f.getR4();
				} else if (i == 5) {
					ruolo = f.getR5();
				} else if (i == 6) {
					ruolo = f.getR6();
				} else if (i == 7) {
					ruolo = f.getR7();
				} else if (i == 8) {
					ruolo = f.getR8();
				} else if (i == 9) {
					ruolo = f.getR9();
				} else if (i == 10) {
					ruolo = f.getR10();
				}
			}

			if (ruolo != null && ("P".equals(ruolo) || "D".equals(ruolo) || "C".equals(ruolo) || "A".equals(ruolo))) {
				Image img = Utils.buildImage(ruolo.toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+ruolo.toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));

		return rxColumn;

	}

//	private void setStatisticheA(VerticalLayout layout, FcCampionato campionato,
//			Properties p) {
//
//		HorizontalLayout hlayout1 = new HorizontalLayout();
//		hlayout1.setSpacing(true);
//
//		try {
//
//			Button stampaPdf = new Button("Statistiche Voti pdf");
//			FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(new StreamResource("StatisticheVoti.pdf",() -> {
//				byte[] b = null;
//				try {
//					Connection conn = jdbcTemplate.getDataSource().getConnection();
//					Map<String, Object> hm = new HashMap<String, Object>();
//					hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
//					hm.put("DIVISORE", ""+Costants.DIVISORE_10);
//					Resource resource = resourceLoader.getResource("classpath:reports/statisticheVoti.jasper");
//					InputStream inputStream = resource.getInputStream();
//					b = JasperRunManager.runReportToPdf(inputStream, hm, conn);
//				} catch (JRException ex) {
//					ex.printStackTrace();
//				} catch (Exception ex2) {
//					ex2.printStackTrace();
//				}
//				return new ByteArrayInputStream(b);
//			}));
//			button1Wrapper.wrapComponent(stampaPdf);
//
//			hlayout1.add(button1Wrapper);
//
//		} catch (Exception e) {
//			LOG.error(e.getMessage());
//			e.printStackTrace();
//		}
//
//		layout.add(hlayout1);
//
//		List<FcStatistiche> items = statisticheController.findAll();
//
//		Grid<FcStatistiche> grid = new Grid<>();
//		grid.setItems(items);
//		grid.setSelectionMode(Grid.SelectionMode.NONE);
//		grid.setAllRowsVisible(true);
//		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
//		grid.setMultiSort(true);
//		// grid.setSizeFull();
//
//		// grid.addColumn(s -> s.getIdGiocatore()).setKey("id");
//
//		Column<FcStatistiche> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
//			HorizontalLayout cellLayout = new HorizontalLayout();
//			cellLayout.setMargin(false);
//			cellLayout.setPadding(false);
//			cellLayout.setSpacing(false);
//			cellLayout.setAlignItems(Alignment.STRETCH);
//			cellLayout.setSizeFull();
//			if (g != null && g.getIdRuolo() != null) {
//				Image img = buildImage(Costants.CLASSPATH_IMAGES, g.getIdRuolo().toLowerCase() + ".png");
//				cellLayout.add(img);
//			}
//			return cellLayout;
//		}));
//		ruoloColumn.setKey(Costants.RUOLO);
//		ruoloColumn.setSortable(true);
//		ruoloColumn.setHeader("R");
//		ruoloColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> giocatoreColumn = grid.addColumn(s -> s.getCognGiocatore()).setKey(Costants.GIOCATORE);
//		giocatoreColumn.setSortable(true);
//		giocatoreColumn.setHeader(Costants.GIOCATORE);
//		giocatoreColumn.setWidth("150px");
//		giocatoreColumn.setAutoWidth(true);
//
//		// Column<FcStatistiche> squadraColumn = grid.addColumn(s ->
//		// s.getNomeSquadra()).setKey(Costants.SQUADRA);
//		// squadraColumn.setSortable(true);
//		// squadraColumn.setHeader(Costants.SQUADRA);
//
//		Column<FcStatistiche> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(s -> {
//
//			HorizontalLayout cellLayout = new HorizontalLayout();
//			cellLayout.setMargin(false);
//			cellLayout.setPadding(false);
//			cellLayout.setSpacing(false);
//			cellLayout.setAlignItems(Alignment.STRETCH);
//			// cellLayout.setSizeFull();
//
//			if (s != null && s.getNomeSquadra() != null) {
//
//				StreamResource resource = new StreamResource(s.getNomeSquadra(),() -> {
//					Resource r = resourceLoader.getResource("classpath:" + "/img/nazioni/" + s.getNomeSquadra() + ".png");
//					InputStream inputStream = null;
//					try {
//						inputStream = r.getInputStream();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					return inputStream;
//				});
//
//				Image img = new Image(resource,"");
//				img.setSrc(resource);
//
//				Label lblSquadra = new Label(s.getNomeSquadra());
//				// lblSquadra.getStyle().set(Costants.FONT_SIZE, "11px");
//
//				cellLayout.add(img);
//				cellLayout.add(lblSquadra);
//			}
//
//			return cellLayout;
//
//		}));
//		nomeSquadraColumn.setSortable(true);
//		nomeSquadraColumn.setComparator((p1,
//				p2) -> p1.getNomeSquadra().compareTo(p2.getNomeSquadra()));
//		nomeSquadraColumn.setHeader(Costants.SQUADRA);
//		nomeSquadraColumn.setWidth("100px");
//		nomeSquadraColumn.setAutoWidth(true);
//
//		// Column<FcStatistiche> proprietarioColumn = grid.addColumn(s ->
//		// s.getProprietario()).setKey("proprietario");
//		// proprietarioColumn.setSortable(true);
//		// proprietarioColumn.setHeader("Proprietario");
//		// proprietarioColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> giocateColumn = grid.addColumn(s -> s.getGiocate()).setKey("giocate");
//		giocateColumn.setSortable(true);
//		giocateColumn.setHeader("Giocate");
//		giocateColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
//
//			HorizontalLayout cellLayout = new HorizontalLayout();
//			cellLayout.setMargin(false);
//			cellLayout.setPadding(false);
//			cellLayout.setSpacing(true);
//
//			if (s != null && s.getFcGiocatore() != null) {
//				String imgThink = "2.png";
//				if (s != null && s.getMediaVoto() != 0) {
//					if (s.getMediaVoto() > Costants.EM_RANGE_MAX_MV) {
//						imgThink = "1.png";
//					} else if (s.getMediaVoto() < Costants.EM_RANGE_MIN_MV) {
//						imgThink = "3.png";
//					}
//				}
//				Image img = buildImage(Costants.CLASSPATH_IMAGES, imgThink);
//
//				DecimalFormat myFormatter = new DecimalFormat("#0.00");
//				Double d = Double.valueOf(0);
//				if (s != null) {
//					d = s.getMediaVoto() / Costants.DIVISORE_10;
//				}
//				String sTotPunti = myFormatter.format(d);
//				Label lbl = new Label(sTotPunti);
//
//				cellLayout.add(img);
//				cellLayout.add(lbl);
//
//			}
//			return cellLayout;
//		}));
//		mediaVotoColumn.setSortable(true);
//		mediaVotoColumn.setComparator((p1,
//				p2) -> p1.getMediaVoto().compareTo(p2.getMediaVoto()));
//		mediaVotoColumn.setHeader("Mv");
//		mediaVotoColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
//			DecimalFormat myFormatter = new DecimalFormat("#0.00");
//			Double d = Double.valueOf(0);
//			if (s.getFantaMedia() != null) {
//				d = s.getFantaMedia() / Costants.DIVISORE_10;
//			}
//			String sD = myFormatter.format(d);
//			return new Label(sD);
//		}));
//		fantaMediaColumn.setSortable(true);
//		fantaMediaColumn.setComparator((p1,
//				p2) -> p1.getFantaMedia().compareTo(p2.getFantaMedia()));
//		fantaMediaColumn.setHeader("FMv");
//		fantaMediaColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> golFattoColumn = grid.addColumn(s -> s.getGoalFatto()).setKey("golFatto");
//		golFattoColumn.setSortable(true);
//		golFattoColumn.setHeader("G+");
//		golFattoColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> golSubitoColumn = grid.addColumn(s -> s.getGoalSubito()).setKey("golSubito");
//		golSubitoColumn.setSortable(true);
//		golSubitoColumn.setHeader("G-");
//		golSubitoColumn.setAutoWidth(true);
//
//		// grid.addColumn(s -> s.getRigoreSegnato()).setKey("rigoreSegnato");
//		// grid.addColumn(s ->
//		// s.getRigoreSbagliato()).setKey("rigoreSbagliato");
//
//		Column<FcStatistiche> assistColumn = grid.addColumn(s -> s.getAssist()).setKey("assist");
//		assistColumn.setSortable(true);
//		assistColumn.setHeader("Ass");
//		assistColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> ammonizioneColumn = grid.addColumn(s -> s.getAmmonizione()).setKey("ammonizione");
//		ammonizioneColumn.setSortable(true);
//		ammonizioneColumn.setHeader("Amm");
//		ammonizioneColumn.setAutoWidth(true);
//
//		Column<FcStatistiche> espulsioneColumn = grid.addColumn(s -> s.getEspulsione()).setKey("espulsione");
//		espulsioneColumn.setSortable(true);
//		espulsioneColumn.setHeader("Esp");
//		espulsioneColumn.setAutoWidth(true);
//
//		layout.add(grid);
//
//	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
			if (event.getSource() == salvaRoseA) {
				jobProcessGiornata.executeUpdateDbFcExpRoseA(false, campionato.getIdCampionato());

				List<FcExpRosea> items = expRoseAController.findAll();
				gridRosea.setItems(items);
				gridRosea.getDataProvider().refreshAll();

			} else if (event.getSource() == salvaStat) {
				jobProcessGiornata.statistiche(campionato);
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC,e.getMessage());
		}
	}


}