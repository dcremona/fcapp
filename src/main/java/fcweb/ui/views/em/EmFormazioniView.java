package fcweb.ui.views.em;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.backend.service.GiornataDettInfoService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;;



@PageTitle("Formazioni")
@Route(value = "emformazioni", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmFormazioniView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private Image iconAmm_ = null;
	private Image iconEsp_ = null;
	private Image iconAssist_ = null;
	private Image iconAutogol_ = null;
	private Image iconEntrato_ = null;
	private Image iconGolfatto_ = null;
	private Image iconGolsubito_ = null;
	private Image iconUscito_ = null;
	private Image iconRigoreSbagliato_ = null;
	private Image iconRigoreSegnato_ = null;
	private Image iconRigoreParato_ = null;
	private Image iconGolVittoria_ = null;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	private GiornataDettInfoService giornataDettInfoController;

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private ResourceLoader resourceLoader;

	// @Autowired
	// private JobProcessSendMail jobProcessSendMail;

	private VerticalLayout mainLayout = new VerticalLayout();
	private ComboBox<FcGiornataInfo> comboGiornata;

	@Autowired
	private AttoreService attoreController;

	public List<FcAttore> squadre = new ArrayList<>();

	@Autowired
	private AccessoService accessoController;

	public EmFormazioniView() throws Exception {
		LOG.info("EmFormazioniView()");
	}

	@PostConstruct
	void init() throws Exception {
		LOG.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());

		initImg();
		initData();
		initLayout();
	}

	private FcGiornataInfo giornataInfo = null;
	private FcCampionato campionato = null;
	private List<FcGiornataInfo> giornate = null;

	private void initData() throws Exception {

		giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		giornate = giornataInfoController.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);

		squadre = attoreController.findByActive(true);
	}

	private void initImg() throws Exception {

		LOG.info("initImg()");
		
		iconAmm_ = Utils.buildImage("amm.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"amm.png"));
		iconEsp_ = Utils.buildImage("esp.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"esp.png"));
		iconAssist_ = Utils.buildImage("assist.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"assist.png"));
		iconAutogol_ = Utils.buildImage("autogol.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"autogol.png"));
		iconEntrato_ = Utils.buildImage("entrato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"entrato.png"));
		iconGolfatto_ = Utils.buildImage("golfatto.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golfatto.png"));
		iconGolsubito_ = Utils.buildImage("golsubito.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golsubito.png"));
		iconUscito_ = Utils.buildImage("uscito.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"uscito.png"));
		iconRigoreSbagliato_ = Utils.buildImage("rigoresbagliato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoresbagliato.png"));
		iconRigoreSegnato_ = Utils.buildImage("rigoresegnato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoresegnato.png"));
		iconRigoreParato_ = Utils.buildImage("rigoreparato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoreparato.png"));
		iconGolVittoria_ = Utils.buildImage("golvittoria.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golvittoria.png"));
		
	}

	private void initLayout() {

		LOG.info("initLayout()");

		Button stampapdf = new Button("Risultati pdf");
		stampapdf.setIcon(VaadinIcon.DOWNLOAD.create());

		comboGiornata = new ComboBox<>();
		comboGiornata.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
		comboGiornata.setItems(giornate);
		comboGiornata.setClearButtonVisible(true);
		comboGiornata.setPlaceholder("Seleziona la giornata");
		comboGiornata.addValueChangeListener(event -> {
			mainLayout.removeAll();
			stampapdf.setEnabled(false);
			if (event.getSource().isEmpty()) {
				LOG.info("event.getSource().isEmpty()");
			} else if (event.getOldValue() == null) {
				LOG.info("event.getOldValue()");
				FcGiornataInfo fcGiornataInfo = event.getValue();
				LOG.info("gioranta " + "" + fcGiornataInfo.getCodiceGiornata());
				buildTabGiornata(mainLayout, "" + fcGiornataInfo.getCodiceGiornata());
				stampapdf.setEnabled(true);
			} else {
				FcGiornataInfo fcGiornataInfo = event.getValue();
				LOG.info("gioranta " + "" + fcGiornataInfo.getCodiceGiornata());
				buildTabGiornata(mainLayout, "" + fcGiornataInfo.getCodiceGiornata());
				stampapdf.setEnabled(true);
			}
		});
		comboGiornata.setWidthFull();

		add(comboGiornata);

		add(mainLayout);

		add(buildLegenda());

		comboGiornata.setValue(giornataInfo);

	}

	@SuppressWarnings("unchecked")
	private void buildTabGiornata(VerticalLayout layout, String giornata) {

		Integer currGG = Integer.valueOf(giornata);
		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(currGG);

		Accordion accordion = new Accordion();
		accordion.setSizeFull();
		for (FcAttore a : squadre) {

			VerticalLayout vCasa = new VerticalLayout();
			HashMap<String, Object> mapCasa;
			try {
				mapCasa = buildData(a, giornataInfo);

				List<FcGiornataDett> itemsCasaTitolari = (List<FcGiornataDett>) mapCasa.get("itemsTitolari");
				List<FcGiornataDett> itemsCasaPanchina = (List<FcGiornataDett>) mapCasa.get("itemsPanchina");
				String schemaCasa = (String) mapCasa.get("schema");

				Grid<FcGiornataDett> tableSqCasaTitolari = buildResultSquadra(itemsCasaTitolari, "Titolari", schemaCasa);
				Grid<FcGiornataDett> tableSqCasaPanchina = buildResultSquadra(itemsCasaPanchina, "Panchina", "");

				vCasa.add(tableSqCasaTitolari);
				vCasa.add(tableSqCasaPanchina);

			} catch (Exception e) {
				LOG.info("NO DATA " + a.getDescAttore());
			}
			VerticalLayout layoutTotaliCasa = buildTotaliInfo(campionato, a, giornataInfo);
			vCasa.add(layoutTotaliCasa);
			vCasa.setSizeFull();

			accordion.add(a.getDescAttore(), vCasa);
		}

		layout.add(accordion);
		layout.setSizeFull();
	}

	private HashMap<String, Object> buildData(FcAttore attore,
			FcGiornataInfo giornataInfo) throws Exception {

		LOG.info("START buildData " + attore.getDescAttore());

		HashMap<String, Object> map = new HashMap<>();

		List<FcGiornataDett> all = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
		List<FcGiornataDett> items = new ArrayList<>();

		int countD = 0;
		int countC = 0;
		int countA = 0;
		for (FcGiornataDett gd : all) {
			items.add(gd);
			if (gd.getOrdinamento() < 12) {
				if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("D")) {
					countD++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("C")) {
					countC++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("A")) {
					countA++;
				}
			}
		}

		String schema = countD + "-" + countC + "-" + countA;

		List<FcGiornataDett> itemsTitolari = new ArrayList<>();
		List<FcGiornataDett> itemsPanchina = new ArrayList<>();
		for (FcGiornataDett gd2 : items) {
			if (gd2.getOrdinamento() < 12) {
				itemsTitolari.add(gd2);
			} else if (gd2.getOrdinamento() > 11) {
				itemsPanchina.add(gd2);
			}
		}

		map.put("items", items);
		map.put("itemsTitolari", itemsTitolari);
		map.put("itemsPanchina", itemsPanchina);
		map.put("schema", schema);

		LOG.info("END buildData " + attore.getDescAttore());

		return map;
	}

	private Grid<FcGiornataDett> buildResultSquadra(List<FcGiornataDett> items,
			String statoGiocatore, String schema) {

		Grid<FcGiornataDett> grid = new Grid<>();
		grid.setItems(items);
		grid.setAllRowsVisible(true);
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		Column<FcGiornataDett> ruoloColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();
			if (f != null && f.getFcGiocatore() != null) {
				Image img = Utils.buildImage(f.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+f.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(false);
		ruoloColumn.setResizable(false);
		ruoloColumn.setHeader("");
		ruoloColumn.setAutoWidth(true);

		Column<FcGiornataDett> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			FcGiocatore g = gd.getFcGiocatore();
			if (gd != null && g != null) {
				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				Span lblGiocatore = new Span(g.getCognGiocatore());
				lblGiocatore.getStyle().set("fontSize", "smaller");
				cellLayout.add(lblGiocatore);

				ArrayList<Image> info = new ArrayList<Image>();
				if (gd.getOrdinamento() < 12 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "N".equals(gd.getFlagAttivo().toUpperCase())) {
					info.add(Utils.buildImage("uscito_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"uscito_s.png")));
				}

				if (gd.getOrdinamento() > 11 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "S".equals(gd.getFlagAttivo().toUpperCase())) {
					info.add(Utils.buildImage("entrato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"entrato_s.png")));
				}

				if (info.size() > 0) {
					for (Image e : info) {
						cellLayout.add(e);
					}
				}

			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setResizable(false);
		cognGiocatoreColumn.setHeader("");
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcGiornataDett> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			FcGiocatore g = gd.getFcGiocatore();
			if (gd != null && g != null) {
				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				Span lblSquadra = new Span(g.getFcSquadra().getNomeSquadra().substring(0, 3));
				lblSquadra.getStyle().set("fontSize", "smaller");
				FcSquadra sq = gd.getFcGiocatore().getFcSquadra();
				if (sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setSortable(false);
		nomeSquadraColumn.setResizable(false);
		nomeSquadraColumn.setHeader("");
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcGiornataDett> resultGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			}

			if (gd != null && gd.getFcGiocatore() != null) {

				ArrayList<Image> info = new ArrayList<Image>();

				for (int a = 0; a < gd.getFcPagelle().getAmmonizione(); a++) {
					info.add(Utils.buildImage("amm_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"amm_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getEspulsione(); a++) {
					info.add(Utils.buildImage("esp_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"esp_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalSubito(); a++) {
					info.add(Utils.buildImage("golsubito_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golsubito_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalRealizzato() - gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(Utils.buildImage("golfatto_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golfatto_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getAutorete(); a++) {
					info.add(Utils.buildImage("autogol_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"autogol_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreFallito(); a++) {
					info.add(Utils.buildImage("rigoresbagliato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoresbagliato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(Utils.buildImage("rigoresegnato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoresegnato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreParato(); a++) {
					info.add(Utils.buildImage("rigoreparato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"rigoreparato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getAssist(); a++) {
					info.add(Utils.buildImage("assist_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"assist_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getGdv(); a++) {
					info.add(Utils.buildImage("golvittoria_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES+"golvittoria_s.png")));
				}

				if (info.size() > 0) {
					for (Image e : info) {
						cellLayout.add(e);
					}
				}
			}

			return cellLayout;

		}));
		resultGiocatoreColumn.setSortable(false);
		resultGiocatoreColumn.setResizable(false);
		resultGiocatoreColumn.setHeader("");
		resultGiocatoreColumn.setAutoWidth(true);

		Column<FcGiornataDett> votoColumn = grid.addColumn(new ComponentRenderer<>(gd -> {

			Span lbl = null;
			FcGiocatore g = gd.getFcGiocatore();
			if (gd != null && g != null) {

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (gd.getVoto() != null) {
					d = gd.getVoto() / Costants.DIVISORE_10;
				}
				String sVoto = myFormatter.format(d);

				lbl = new Span(sVoto);
				lbl.getStyle().set("color", Costants.LIGHT_GRAY);
				if ("S".equals(gd.getFlagAttivo())) {
					lbl.getStyle().set("color", Costants.GRAY);
				} else if ("N".equals(gd.getFlagAttivo())) {
					lbl.getStyle().set("color", Costants.LIGHT_GRAY);
				}
				lbl.getStyle().set("fontSize", "smaller");

				if (!g.isFlagAttivo()) {
					lbl.getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					lbl.getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
			}
			return lbl;

		}));
		votoColumn.setSortable(false);
		votoColumn.setResizable(false);
		votoColumn.setHeader("FV");
		votoColumn.setAutoWidth(true);

		// Column<FcGiornataDett> cognGiocatoreColumn = grid.addColumn(new
		// ComponentRenderer<>(gd -> {
		//
		// HorizontalLayout cellLayout = new HorizontalLayout();
		// cellLayout.setMargin(false);
		// cellLayout.setPadding(false);
		// cellLayout.setSpacing(false);
		//
		// cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
		// if ("S".equals(gd.getFlagAttivo())) {
		// cellLayout.getStyle().set("color", Costants.GRAY);
		// } else if ("N".equals(gd.getFlagAttivo())) {
		// cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
		// }
		//
		// if (gd != null && gd.getFcGiocatore() != null) {
		//
		// Label lblGiocatore = new
		// Label(gd.getFcGiocatore().getCognGiocatore());
		// lblGiocatore.getStyle().set("fontSize", "smaller");
		// cellLayout.add(lblGiocatore);
		//
		// ArrayList<Image> info = new ArrayList<Image>();
		//
		// if (gd.getOrdinamento() < 12 &&
		// StringUtils.isNotEmpty(gd.getFlagAttivo()) &&
		// "N".equals(gd.getFlagAttivo().toUpperCase())) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "uscito_s.png", "Uscito"));
		// }
		//
		// if (gd.getOrdinamento() > 11 &&
		// StringUtils.isNotEmpty(gd.getFlagAttivo()) &&
		// "S".equals(gd.getFlagAttivo().toUpperCase())) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "entrato_s.png",
		// "Entrato"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getAmmonizione(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "amm_s.png", "Ammonizione
		// (-0,5)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getEspulsione(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "esp_s.png", "Espulsione
		// (-1)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getGoalSubito(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "golsubito_s.png", "Gol
		// subito (-1)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getGoalRealizzato() -
		// gd.getFcPagelle().getRigoreSegnato(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "golfatto_s.png", "Gol fatto
		// (+3)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getAutorete(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "autogol_s.png", "Autogol
		// (-2)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getRigoreFallito(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "rigoresbagliato_s.png",
		// "Rigore sbagliato (-3)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getRigoreSegnato(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "rigoresegnato_s.png",
		// "Rigore segnato (+3)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getRigoreParato(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "rigoreparato_s.png",
		// "Rigore parato (+3)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getAssist(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "assist_s.png", "Assist
		// (+1)"));
		// }
		//
		// for (int a = 0; a < gd.getFcPagelle().getGdv(); a++) {
		// info.add(buildImage(Costants.CLASSPATH_IMAGES, "golvittoria_s.png", "Bonus
		// goal vittoria (+1)"));
		// }
		//
		// if (info.size() > 0) {
		// for (Image e : info) {
		// cellLayout.add(e);
		// }
		// }
		// }
		//
		// return cellLayout;
		//
		// }));
		// cognGiocatoreColumn.setSortable(false);
		// cognGiocatoreColumn.setResizable(false);
		// cognGiocatoreColumn.setHeader("Giocatore");
		// // cognGiocatoreColumn.setFlexGrow(0);
		// cognGiocatoreColumn.setWidth("240px");

		HeaderRow headerRow = grid.prependHeaderRow();

		HeaderCell headerCellStatoGiocatore = headerRow.join(ruoloColumn, cognGiocatoreColumn);
		headerCellStatoGiocatore.setText(statoGiocatore);

		HeaderCell headerCellModulo = headerRow.join(resultGiocatoreColumn, votoColumn);
		if (statoGiocatore.equals("Titolari")) {
			headerCellModulo.setText("Modulo: " + schema);
		}

		return grid;
	}

	private VerticalLayout buildTotaliInfo(FcCampionato campionato,
			FcAttore attore, FcGiornataInfo giornataInfo) {

		VerticalLayout layoutMain = new VerticalLayout();
		layoutMain.setWidth("80%");

		FcGiornataDettInfo info = giornataDettInfoController.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
		FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);

		NumberFormat formatter = new DecimalFormat("#0.00");
		String totG = "";
		if (totPunti != null && totPunti.getTotPt() != null) {
			totG = formatter.format(totPunti.getTotPt().doubleValue() / Costants.DIVISORE_10);
		}

		Span lblTotGiornata = new Span();
		lblTotGiornata.setText("Totale Giornata: " + totG);
		lblTotGiornata.getStyle().set("font-size", "24px");
		lblTotGiornata.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
		lblTotGiornata.setSizeFull();

		Span lblInvio = new Span();
		lblInvio.setText("Inviata alle: " + (info == null ? "" : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss")));
		lblInvio.setSizeFull();

		layoutMain.add(lblTotGiornata);
		layoutMain.add(lblInvio);

		return layoutMain;
	}

	private VerticalLayout buildLegenda() {

		VerticalLayout layout = new VerticalLayout();
		layout.getStyle().set("border", Costants.BORDER_COLOR);
		// layout.setSizeFull();
		layout.setMargin(false);

		HorizontalLayout horizontalLayout1 = new HorizontalLayout();
		horizontalLayout1.setSpacing(true);

		horizontalLayout1.add(iconGolfatto_);
		Span lbl = new Span("Gol Fatto");
		horizontalLayout1.add(lbl);

		horizontalLayout1.add(iconGolsubito_);
		lbl = new Span("Gol Subito");
		horizontalLayout1.add(lbl);

		horizontalLayout1.add(iconAmm_);
		lbl = new Span("Ammonizione");
		horizontalLayout1.add(lbl);

		horizontalLayout1.add(iconEsp_);
		lbl = new Span("Espulsione");
		horizontalLayout1.add(lbl);

		horizontalLayout1.add(iconAssist_);
		lbl = new Span("Assist");
		horizontalLayout1.add(lbl);

		horizontalLayout1.add(iconEntrato_);
		lbl = new Span("Entrato");
		horizontalLayout1.add(lbl);

		HorizontalLayout horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.setSpacing(true);

		horizontalLayout2.add(iconUscito_);
		lbl = new Span("Uscito");
		horizontalLayout2.add(lbl);

		horizontalLayout2.add(iconAutogol_);
		lbl = new Span("Autogol");
		horizontalLayout2.add(lbl);

		horizontalLayout2.add(iconRigoreSegnato_);
		lbl = new Span("Rigore segnato");
		horizontalLayout2.add(lbl);

		horizontalLayout2.add(iconRigoreSbagliato_);
		lbl = new Span("Rigore sbagliato");
		horizontalLayout2.add(lbl);

		horizontalLayout2.add(iconRigoreParato_);
		lbl = new Span("Rigore parato");
		horizontalLayout2.add(lbl);

		horizontalLayout2.add(iconGolVittoria_);
		lbl = new Span("Gol Vittoria");
		horizontalLayout2.add(lbl);

		layout.add(horizontalLayout1);
		layout.add(horizontalLayout2);

		return layout;

	}

}