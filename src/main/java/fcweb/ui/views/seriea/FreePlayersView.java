package fcweb.ui.views.seriea;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.klaudeta.PaginatedGrid;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiocatoreService;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Free Players")
@Route(value = "freePlayers")
@RolesAllowed("ADMIN")
public class FreePlayersView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private ResourceLoader resourceLoader;

	private Button loadButton;

	private RadioButtonGroup<String> radioGroup = null;
	private TabSheet tabs = null;
	private Grid<FcGiocatore> gridP = new Grid<FcGiocatore>();
	private Grid<FcGiocatore> gridD = new Grid<FcGiocatore>();
	private Grid<FcGiocatore> gridC = new Grid<FcGiocatore>();
	private Grid<FcGiocatore> gridA = new Grid<FcGiocatore>();

	@Autowired
	private AccessoService accessoController;

	public FreePlayersView() {
		LOG.info("FreePlayersView");
	}

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

		// this.getStyle().set("border", Costants.BORDER_COLOR);
		// this.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		// this.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

		Button button = new Button("Home");
		RouterLink menuHome = new RouterLink("",HomeView.class);
		menuHome.getElement().appendChild(button.getElement());

		Button button2 = new Button("Mercato");
		RouterLink menuMercato = new RouterLink("",MercatoView.class);
		menuMercato.getElement().appendChild(button2.getElement());

		loadButton = new Button("Aggiorna");
		loadButton.addClickListener(this);

		radioGroup = new RadioButtonGroup<>();
		radioGroup.setLabel("Tipo Aggiornamento");
		radioGroup.setItems("All", "Ruolo");
		radioGroup.setValue("All");

		HorizontalLayout layoutButton = new HorizontalLayout();
		layoutButton.getStyle().set("border", Costants.BORDER_COLOR);
		layoutButton.setSpacing(true);
		layoutButton.add(menuHome);
		layoutButton.add(menuMercato);
		layoutButton.add(loadButton);
		layoutButton.add(radioGroup);

		this.add(layoutButton);

		final VerticalLayout layoutP = new VerticalLayout();
		gridP = getTableGiocatore(getModelAsta("P"));
		// Anchor downloadAsExcelP = new Anchor(new
		// StreamResource("P.xlsx",Exporter.exportAsExcel(gridP)),"Download P");
		// layoutP.add(new HorizontalLayout(downloadAsExcelP));
		GridExporter<FcGiocatore> exporterP = GridExporter.createFor(gridP);
		exporterP.setAutoAttachExportButtons(false);
		exporterP.setTitle("P");
		exporterP.setFileName("P" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLinkP = new Anchor("","Export to Excel P");
		excelLinkP.setHref(exporterP.getExcelStreamResource());
		excelLinkP.getElement().setAttribute("download", true);
		layoutP.add(new HorizontalLayout(excelLinkP));
		layoutP.add(gridP);

		final VerticalLayout layoutD = new VerticalLayout();
		gridD = getTableGiocatore(getModelAsta("D"));
		// Anchor downloadAsExcelD = new Anchor(new
		// StreamResource("D.xlsx",Exporter.exportAsExcel(gridD)),"Download D");
		// layoutD.add(new HorizontalLayout(downloadAsExcelD));
		GridExporter<FcGiocatore> exporterD = GridExporter.createFor(gridD);
		exporterD.setAutoAttachExportButtons(false);
		exporterD.setTitle("D");
		exporterD.setFileName("D" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLinkD = new Anchor("","Export to Excel D");
		excelLinkD.setHref(exporterD.getExcelStreamResource());
		excelLinkD.getElement().setAttribute("download", true);
		layoutD.add(new HorizontalLayout(excelLinkD));
		layoutD.add(gridD);

		final VerticalLayout layoutC = new VerticalLayout();
		gridC = getTableGiocatore(getModelAsta("C"));
		// Anchor downloadAsExcelC = new Anchor(new
		// StreamResource("C.xlsx",Exporter.exportAsExcel(gridC)),"Download C");
		// layoutC.add(new HorizontalLayout(downloadAsExcelC));
		GridExporter<FcGiocatore> exporterC = GridExporter.createFor(gridC);
		exporterC.setAutoAttachExportButtons(false);
		exporterC.setTitle("C");
		exporterC.setFileName("C" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLinkC = new Anchor("","Export to Excel C");
		excelLinkC.setHref(exporterC.getExcelStreamResource());
		excelLinkC.getElement().setAttribute("download", true);
		layoutC.add(new HorizontalLayout(excelLinkC));
		layoutC.add(gridC);

		final VerticalLayout layoutA = new VerticalLayout();
		gridA = getTableGiocatore(getModelAsta("A"));
		// Anchor downloadAsExcelA = new Anchor(new
		// StreamResource("A.xlsx",Exporter.exportAsExcel(gridA)),"Download A");
		// layoutA.add(new HorizontalLayout(downloadAsExcelA));
		GridExporter<FcGiocatore> exporterA = GridExporter.createFor(gridA);
		exporterA.setAutoAttachExportButtons(false);
		exporterA.setTitle("A");
		exporterA.setFileName("A" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLinkA = new Anchor("","Export to Excel A");
		excelLinkA.setHref(exporterA.getExcelStreamResource());
		excelLinkA.getElement().setAttribute("download", true);
		layoutA.add(new HorizontalLayout(excelLinkA));
		layoutA.add(gridA);

		tabs = new TabSheet();
		tabs.add("Portieri", layoutP);
		tabs.add("Difensori", layoutD);
		tabs.add("Centrocampisti", layoutC);
		tabs.add("Attaccanti", layoutA);
		// tabs.setSizeFull();

		add(tabs);

	}

	private List<FcGiocatore> getModelAsta(String ruolo) {

		LOG.info("START getModelAsta " + ruolo);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		List<FcFormazione> allFormaz = formazioneController.findByFcCampionato(campionato);
		List<Integer> listNotIn = new ArrayList<Integer>();
		for (FcFormazione f : allFormaz) {
			if (f.getFcGiocatore() != null) {
				listNotIn.add(f.getFcGiocatore().getIdGiocatore());
			}
		}

		FcRuolo r = new FcRuolo();
		r.setIdRuolo(ruolo);

		// load data
		List<FcGiocatore> all = null;
		if (listNotIn.size() > 0) {
			all = giocatoreController.findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(r, true, listNotIn);
		} else {
			all = giocatoreController.findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(r, true);
		}

		// FIX
		for (FcGiocatore g : all) {
			if (g.getFcStatistiche() == null) {
				FcStatistiche s = new FcStatistiche();
				s.setMediaVoto(Double.valueOf(0));
				s.setFantaMedia(Double.valueOf(0));
				g.setFcStatistiche(s);
			}
		}

		LOG.info("END getModelAsta " + ruolo);

		return all;
	}

	private PaginatedGrid<FcGiocatore, ?> getTableGiocatore(
			List<FcGiocatore> items) {

		PaginatedGrid<FcGiocatore, ?> grid = new PaginatedGrid<>();
		ListDataProvider<FcGiocatore> dataProvider = new ListDataProvider<>(items);
		grid.setDataProvider(dataProvider);

		grid.setSelectionMode(Grid.SelectionMode.NONE);
		// grid.setHeightByRows(true);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setMultiSort(true);
		grid.setAllRowsVisible(true);

		// Grid<FcGiocatore> grid = new Grid<>();
		// grid.setItems(items);
		// grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		// grid.setAllRowsVisible(true);
		// grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
		// GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		// grid.setMultiSort(true);
		// grid.setSizeFull();

		Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();
			if (g != null && g.getFcRuolo() != null) {
				Image img = buildImage("classpath:images/", g.getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setKey("fcRuolo.idRuolo");
		ruoloColumn.setHeader("R");
		ruoloColumn.setSortable(true);
		ruoloColumn.setAutoWidth(true);

		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(g -> g != null ? g.getCognGiocatore() : "-");
		cognGiocatoreColumn.setKey("cognGiocatore");
		cognGiocatoreColumn.setHeader("Giocatore");
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				// Image img = buildImage("classpath:/img/squadre/",
				// g.getFcSquadra().getNomeSquadra() + ".png");
				// cellLayout.add(img);
				FcSquadra sq = g.getFcSquadra();
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Span lblSquadra = new Span(g.getFcSquadra().getNomeSquadra());
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setKey("fcSquadra.nomeSquadra");
		nomeSquadraColumn.setHeader("Squadra");
		nomeSquadraColumn.setSortable(true);
		nomeSquadraColumn.setComparator((p1,
				p2) -> p1.getFcSquadra().getNomeSquadra().compareTo(p2.getFcSquadra().getNomeSquadra()));
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcGiocatore> quotazioneColumn = grid.addColumn(g -> g != null ? g.getQuotazione() : 0);
		quotazioneColumn.setKey("quotazione");
		quotazioneColumn.setHeader("Quotazione");
		quotazioneColumn.setAutoWidth(true);
		quotazioneColumn.setSortable(true);

		Column<FcGiocatore> giocateColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGiocate() : 0);
		giocateColumn.setHeader("Giocate");
		giocateColumn.setKey("fcStatistiche.giocate");
		giocateColumn.setAutoWidth(true);
		giocateColumn.setSortable(true);

		Column<FcGiocatore> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(true);
			if (g != null) {
				FcStatistiche s = g.getFcStatistiche();
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);

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
				p2) -> p1.getFcStatistiche().getMediaVoto().compareTo(p2.getFcStatistiche().getMediaVoto()));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setAutoWidth(true);
		mediaVotoColumn.setKey("fcStatistiche.mediaVoto");

		Column<FcGiocatore> fmVotoColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(true);
			if (g != null) {
				FcStatistiche s = g.getFcStatistiche();
				String imgThink = "2.png";
				if (s != null && s.getFantaMedia() != 0) {
					if (s.getFantaMedia() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getFantaMedia() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);

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
		fmVotoColumn.setSortable(true);
		fmVotoColumn.setComparator((p1,
				p2) -> p1.getFcStatistiche().getFantaMedia().compareTo(p2.getFcStatistiche().getFantaMedia()));
		fmVotoColumn.setHeader("FMv");
		fmVotoColumn.setAutoWidth(true);
		fmVotoColumn.setKey("fcStatistiche.fantaMedia");

		Column<FcGiocatore> assistColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getAssist() : 0);
		assistColumn.setSortable(true);
		assistColumn.setHeader("Assist");
		assistColumn.setAutoWidth(true);
		assistColumn.setKey("fcStatistiche.assist");

		Column<FcGiocatore> gfColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGoalFatto() : 0);
		gfColumn.setSortable(true);
		gfColumn.setHeader("GF");
		gfColumn.setAutoWidth(true);
		gfColumn.setKey("fcStatistiche.goalFatto");

		Column<FcGiocatore> gsColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getGoalSubito() : 0);
		gsColumn.setSortable(true);
		gsColumn.setHeader("GS");
		gsColumn.setAutoWidth(true);
		gsColumn.setKey("fcStatistiche.goalSubito");

		Column<FcGiocatore> rsColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getRigoreSegnato() : 0);
		rsColumn.setSortable(true);
		rsColumn.setHeader("RS");
		rsColumn.setAutoWidth(true);
		rsColumn.setKey("RS");

		Column<FcGiocatore> ammonizColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getAmmonizione() : 0);
		ammonizColumn.setSortable(true);
		ammonizColumn.setHeader("A");
		ammonizColumn.setAutoWidth(true);
		ammonizColumn.setKey("fcStatistiche.ammonizione");

		Column<FcGiocatore> espulsColumn = grid.addColumn(g -> g != null && g.getFcStatistiche() != null ? g.getFcStatistiche().getEspulsione() : 0);
		espulsColumn.setSortable(true);
		espulsColumn.setHeader("E");
		espulsColumn.setAutoWidth(true);
		espulsColumn.setKey("fcStatistiche.espulsione");

		// Sets the max number of items to be rendered on the grid for each page
		grid.setPageSize(16);

		// Sets how many pages should be visible on the pagination before and/or
		// after the current selected page
		grid.setPaginatorSize(5);

		return grid;
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		try {
			LOG.info("START AGGIORNA");

			LOG.info("selAggion " + radioGroup.getValue());
			if ("Ruolo".equals(radioGroup.getValue())) {
				String selTab = tabs.getSelectedTab().getLabel();
				LOG.info("selTab " + selTab);
				if ("Portieri".equals(selTab)) {
					gridP.setItems(getModelAsta("P"));
					gridP.getDataProvider().refreshAll();
				} else if ("Difensori".equals(selTab)) {
					gridD.setItems(getModelAsta("D"));
					gridD.getDataProvider().refreshAll();
				} else if ("Centrocampisti".equals(selTab)) {
					gridC.setItems(getModelAsta("C"));
					gridC.getDataProvider().refreshAll();
				} else if ("Attaccanti".equals(selTab)) {
					gridA.setItems(getModelAsta("A"));
					gridA.getDataProvider().refreshAll();
				}

			} else {
				gridP.setItems(getModelAsta("P"));
				gridP.getDataProvider().refreshAll();
				gridD.setItems(getModelAsta("D"));
				gridD.getDataProvider().refreshAll();
				gridC.setItems(getModelAsta("C"));
				gridC.getDataProvider().refreshAll();
				gridA.setItems(getModelAsta("A"));
				gridA.getDataProvider().refreshAll();
			}

			LOG.info("END AGGIORNA");

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
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