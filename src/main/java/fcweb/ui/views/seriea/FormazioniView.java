package fcweb.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
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
import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.job.JobProcessSendMail;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.ClassificaService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.backend.service.GiornataDettInfoService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.GiornataService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;;

@PageTitle("Formazioni")
@Route(value = "formazioni", layout = MainLayout.class)
@RolesAllowed("USER")
public class FormazioniView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Image iconAmm = null;
	private Image iconEsp = null;
	private Image iconAssist = null;
	private Image iconAutogol = null;
	private Image iconEntrato = null;
	private Image iconGolfatto = null;
	private Image iconGolsubito = null;
	private Image iconUscito = null;
	private Image iconRigoreSbagliato = null;
	private Image iconRigoreSegnato = null;
	private Image iconRigoreParato = null;
	private Image iconBonusPortiere = null;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiornataService giornataController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	private GiornataDettInfoService giornataDettInfoController;

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private JobProcessSendMail jobProcessSendMail;

	private VerticalLayout mainLayout = new VerticalLayout();
	private ComboBox<FcGiornataInfo> comboGiornata;

	@Autowired
	private AccessoService accessoController;

	public FormazioniView() {
	}

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initImg();
		initLayout();
	}

	private void initImg() {

		log.info("initImg()");

		iconAmm = Utils.buildImage("amm.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "amm.png"));
		iconEsp = Utils.buildImage("esp.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "esp.png"));
		iconAssist = Utils.buildImage("assist.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "assist.png"));
		iconAutogol = Utils.buildImage("autogol.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "autogol.png"));
		iconEntrato = Utils.buildImage("entrato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "entrato.png"));
		iconGolfatto = Utils.buildImage("golfatto.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "golfatto.png"));
		iconGolsubito = Utils.buildImage("golsubito.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "golsubito.png"));
		iconUscito = Utils.buildImage("uscito.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "uscito.png"));
		iconRigoreSbagliato = Utils.buildImage("rigoresbagliato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoresbagliato.png"));
		iconRigoreSegnato = Utils.buildImage("rigoresegnato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoresegnato.png"));
		iconRigoreParato = Utils.buildImage("rigoreparato.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoreparato.png"));
		iconBonusPortiere = Utils.buildImage("portiereImbattuto.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "portiereImbattuto.png"));
	}

	private void initLayout() {

		log.info("initLayout()");

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		List<FcGiornataInfo> giornate = giornataInfoController.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);

		FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(new StreamResource("Risultati.pdf",() -> {
			String pathImg = "images/";
			byte[] b = jobProcessSendMail.getJasperRisultati(campionato, comboGiornata.getValue(), p, pathImg);
			return new ByteArrayInputStream(b);
		}));
		Button stampapdf = new Button("Risultati pdf");
		stampapdf.setIcon(VaadinIcon.DOWNLOAD.create());
		button1Wrapper.wrapComponent(stampapdf);
		add(button1Wrapper);

		comboGiornata = new ComboBox<>();
		comboGiornata.setItemLabelGenerator(g -> Utils.buildInfoGiornata(g));
		comboGiornata.setItems(giornate);
		comboGiornata.setClearButtonVisible(true);
		comboGiornata.setPlaceholder("Seleziona la giornata");
		comboGiornata.addValueChangeListener(event -> {
			log.info("addValueChangeListener ");
			mainLayout.removeAll();
			stampapdf.setEnabled(false);
			if (event.getSource().isEmpty()) {
				log.info("event.getSource().isEmpty()");
			} else if (event.getOldValue() == null) {
				log.info("event.getOldValue()");
				FcGiornataInfo fcGiornataInfo = event.getValue();
				log.info("gioranta " + "" + fcGiornataInfo.getCodiceGiornata());
				buildTabGiornata(mainLayout, "" + fcGiornataInfo.getCodiceGiornata());
				stampapdf.setEnabled(true);
			} else {
				FcGiornataInfo fcGiornataInfo = event.getValue();
				log.info("gioranta " + "" + fcGiornataInfo.getCodiceGiornata());
				buildTabGiornata(mainLayout, "" + fcGiornataInfo.getCodiceGiornata());
				stampapdf.setEnabled(true);
			}
		});
		comboGiornata.setWidthFull();

		mainLayout.setSizeFull();

		add(comboGiornata);

		add(mainLayout);

		add(buildLegenda());

		comboGiornata.setValue(giornataInfo);
	}

	@SuppressWarnings("unchecked")
	private void buildTabGiornata(VerticalLayout layout, String giornata) {

		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer currGG = Integer.valueOf(giornata);
		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(currGG);
		List<FcGiornata> partite = giornataController.findByFcGiornataInfo(giornataInfo);

		for (FcGiornata p : partite) {

			// CASA
			FcAttore attoreCasa = p.getFcAttoreByIdAttoreCasa();
			HashMap<String, Object> mapCasa = buildData(attoreCasa, giornataInfo);

			List<FcGiornataDett> itemsCasaTitolari = (List<FcGiornataDett>) mapCasa.get("itemsTitolari");
			List<FcGiornataDett> itemsCasaPanchina = (List<FcGiornataDett>) mapCasa.get("itemsPanchina");
			List<FcGiornataDett> itemsCasaTribuna = (List<FcGiornataDett>) mapCasa.get("itemsTribuna");
			String schemaCasa = (String) mapCasa.get("schema");
			String mdCasa = getModificatoreDifesa(schemaCasa);
			Span labelCasa = new Span(attoreCasa.getDescAttore());
			Grid<FcGiornataDett> tableSqCasaTitolari = buildResultSquadra(itemsCasaTitolari, "Titolari", schemaCasa);
			Grid<FcGiornataDett> tableSqCasaPanchina = buildResultSquadra(itemsCasaPanchina, "Panchina", "");
			Grid<FcGiornataDett> tableSqCasaTribuna = buildResultSquadra(itemsCasaTribuna, "Tribuna", "");
			Grid<FcProperties> tableAltriPunteggiCasa = buildAltriPunteggiInfo(campionato, attoreCasa, giornataInfo, true, mdCasa, itemsCasaPanchina);
			VerticalLayout layoutTotaliCasa = buildTotaliInfo(campionato, attoreCasa, giornataInfo, p.getTotCasa());

			// FUORI
			FcAttore attoreFuori = p.getFcAttoreByIdAttoreFuori();
			HashMap<String, Object> mapFuori = buildData(attoreFuori, giornataInfo);

			List<FcGiornataDett> itemsFuoriTitolari = (List<FcGiornataDett>) mapFuori.get("itemsTitolari");
			List<FcGiornataDett> itemsFuoriPanchina = (List<FcGiornataDett>) mapFuori.get("itemsPanchina");
			List<FcGiornataDett> itemsFuoriTribuna = (List<FcGiornataDett>) mapFuori.get("itemsTribuna");
			String schemaFuori = (String) mapFuori.get("schema");
			String mdFuori = getModificatoreDifesa(schemaFuori);
			Span labelFuori = new Span(attoreFuori.getDescAttore());
			Grid<FcGiornataDett> tableSqFuoriTitolari = buildResultSquadra(itemsFuoriTitolari, "Titolari", schemaFuori);
			Grid<FcGiornataDett> tableSqFuoriPanchina = buildResultSquadra(itemsFuoriPanchina, "Panchina", "");
			Grid<FcGiornataDett> tableSqFuoriTribuna = buildResultSquadra(itemsFuoriTribuna, "Tribuna", "");
			Grid<FcProperties> tableAltriPunteggiFuori = buildAltriPunteggiInfo(campionato, attoreFuori, giornataInfo, false, mdFuori, itemsFuoriPanchina);
			VerticalLayout layoutTotaliFuori = buildTotaliInfo(campionato, attoreFuori, giornataInfo, p.getTotFuori());

			HorizontalLayout layoutRisultato = new HorizontalLayout();
			String s1 = p.getGolCasa() == null ? "0.png" : p.getGolCasa() + ".png";
			Image imgCasa = Utils.buildImage(s1, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "number/" + s1));
			String s2 = p.getGolCasa() == null ? "0.png" : p.getGolFuori() + ".png";
			Image imgFuori = Utils.buildImage(s2, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "number/" + s2));

			layoutRisultato.add(imgCasa);
			layoutRisultato.add(imgFuori);

			HorizontalLayout horizontalLayout0 = new HorizontalLayout();
			horizontalLayout0.setWidth("100%");
			horizontalLayout0.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
			horizontalLayout0.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			horizontalLayout0.add(labelCasa);
			horizontalLayout0.add(layoutRisultato);
			horizontalLayout0.add(labelFuori);

			VerticalLayout vCasa = new VerticalLayout();
			vCasa.add(tableSqCasaTitolari);
			vCasa.add(tableSqCasaPanchina);
			vCasa.add(tableSqCasaTribuna);
			vCasa.add(tableAltriPunteggiCasa);
			vCasa.add(layoutTotaliCasa);

			VerticalLayout vFuori = new VerticalLayout();
			vFuori.add(tableSqFuoriTitolari);
			vFuori.add(tableSqFuoriPanchina);
			vFuori.add(tableSqFuoriTribuna);
			vFuori.add(tableAltriPunteggiFuori);
			vFuori.add(layoutTotaliFuori);

			HorizontalLayout horizontalLayout1 = new HorizontalLayout();
			horizontalLayout1.setWidth("100%");
			horizontalLayout1.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			horizontalLayout1.add(vCasa);
			horizontalLayout1.add(vFuori);

			final VerticalLayout layoutTab = new VerticalLayout();
			layoutTab.setMargin(false);
			layoutTab.setPadding(false);
			layoutTab.setSpacing(false);
			layoutTab.add(horizontalLayout0);
			layoutTab.add(horizontalLayout1);

			tabSheet.add(attoreCasa.getDescAttore() + " [*] " + attoreFuori.getDescAttore(), layoutTab);

		}

		layout.add(tabSheet);

	}

	private HashMap<String, Object> buildData(FcAttore attore,
			FcGiornataInfo giornataInfo) {

		log.info("START buildData " + attore.getDescAttore());

		HashMap<String, Object> map = new HashMap<>();

		List<FcGiornataDett> all = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
		List<FcGiornataDett> items = new ArrayList<>();

		int countD = 0;
		int countC = 0;
		int countA = 0;
		for (FcGiornataDett gd : all) {
			items.add(gd);
			if (gd.getOrdinamento() < 12) {
				if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.D)) {
					countD++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.C)) {
					countC++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.A)) {
					countA++;
				}
			}
		}

		if (items.size() != 26) {
			int addGioc = 26 - items.size();
			int incr = items.size();
			for (int g = 0; g < addGioc; g++) {
				FcGiornataDett gDett = new FcGiornataDett();
				gDett.setOrdinamento(incr);
				items.add(gDett);
				incr++;
			}
		}

		String schema = countD + "-" + countC + "-" + countA;

		List<FcGiornataDett> itemsTitolari = new ArrayList<>();
		List<FcGiornataDett> itemsPanchina = new ArrayList<>();
		List<FcGiornataDett> itemsTribuna = new ArrayList<>();
		for (FcGiornataDett gd2 : items) {
			if (gd2.getOrdinamento() < 12) {
				itemsTitolari.add(gd2);
			} else if (gd2.getOrdinamento() > 11 && gd2.getOrdinamento() < 19) {
				itemsPanchina.add(gd2);
			} else {
				itemsTribuna.add(gd2);
			}
		}

		map.put("items", items);
		map.put("itemsTitolari", itemsTitolari);
		map.put("itemsPanchina", itemsPanchina);
		map.put("itemsTribuna", itemsTribuna);
		map.put("schema", schema);

		log.info("END buildData " + attore.getDescAttore());

		return map;
	}

	private Grid<FcGiornataDett> buildResultSquadra(List<FcGiornataDett> items,
			String statoGiocatore, String schema) {

		Grid<FcGiornataDett> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);

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
			if (gd != null && gd.getFcGiocatore() != null) {

				Image img = Utils.buildImage(gd.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + gd.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);

				String descGiocatore = gd.getFcGiocatore().getCognGiocatore();
				if ("S".equals(gd.getFlagAttivo()) && (gd.getOrdinamento() == 14 || gd.getOrdinamento() == 16 || gd.getOrdinamento() == 18)) {
					descGiocatore = "(-0,5) " + gd.getFcGiocatore().getCognGiocatore();
				}

				Span lblGiocatore = new Span(descGiocatore);
				lblGiocatore.getStyle().set("fontSize", "smaller");
				cellLayout.add(lblGiocatore);

				ArrayList<Image> info = new ArrayList<Image>();
				if (gd.getOrdinamento() < 12 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "N".equalsIgnoreCase(gd.getFlagAttivo())) {
					info.add(Utils.buildImage("uscito_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "uscito_s.png")));
				}

				if (gd.getOrdinamento() > 11 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "S".equalsIgnoreCase(gd.getFlagAttivo())) {
					info.add(Utils.buildImage("entrato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "entrato_s.png")));
				}

				if (!info.isEmpty()) {
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
			if (gd != null && gd.getFcGiocatore() != null) {
				Span lblSquadra = new Span(gd.getFcGiocatore().getFcSquadra().getNomeSquadra().substring(0, 3));
				lblSquadra.getStyle().set("fontSize", "smaller");
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setSortable(false);
		nomeSquadraColumn.setResizable(false);
		nomeSquadraColumn.setHeader("");
		nomeSquadraColumn.setWidth("5rem").setFlexGrow(0);

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
					info.add(Utils.buildImage("amm_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "amm_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getEspulsione(); a++) {
					info.add(Utils.buildImage("esp_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "esp_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalSubito(); a++) {
					info.add(Utils.buildImage("golsubito_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "golsubito_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalRealizzato() - gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(Utils.buildImage("golfatto_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "golfatto_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getAutorete(); a++) {
					info.add(Utils.buildImage("autogol_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "autogol_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreFallito(); a++) {
					info.add(Utils.buildImage("rigoresbagliato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoresbagliato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(Utils.buildImage("rigoresegnato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoresegnato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreParato(); a++) {
					info.add(Utils.buildImage("rigoreparato_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "rigoreparato_s.png")));
				}

				for (int a = 0; a < gd.getFcPagelle().getAssist(); a++) {
					info.add(Utils.buildImage("assist_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "assist_s.png")));
				}

				if (Costants.P.equals(gd.getFcGiocatore().getFcRuolo().getIdRuolo()) && gd.getFcPagelle().getGoalSubito() == 0 && gd.getFcPagelle().getEspulsione() == 0 && gd.getFcPagelle().getVotoGiocatore() != 0) {
					if (gd.getFcPagelle().getG() != 0 && gd.getFcPagelle().getCs() != 0 && gd.getFcPagelle().getTs() != 0) {
						info.add(Utils.buildImage("portiereImbattuto_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "portiereImbattuto_s.png")));
					}
				}
				if (!info.isEmpty()) {
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

		Column<FcGiornataDett> votoColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat(Costants.NUMBER_DECIMAL);
			Double d = Double.valueOf(0);
			if (gd.getVoto() != null) {
				d = gd.getVoto() / Costants.DIVISORE_100;
			}
			String sVoto = myFormatter.format(d);

			Span lbl = new Span(sVoto);
			lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			lbl.getStyle().set("fontSize", "smaller");
			return lbl;

		}));
		votoColumn.setSortable(false);
		votoColumn.setResizable(false);
		votoColumn.setHeader(Costants.FV);
		votoColumn.setWidth("5rem").setFlexGrow(0);

		Column<FcGiornataDett> gColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat(Costants.NUMBER_DECIMAL);
			Double dg = Double.valueOf(0);
			if (gd.getFcPagelle() != null && gd.getFcPagelle().getG() != null) {
				dg = gd.getFcPagelle().getG() / Costants.DIVISORE_100;
			}
			String sG = myFormatter.format(dg);

			Span lbl = new Span(sG);
			lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			lbl.getStyle().set("fontSize", "smaller");

			return lbl;
		}));
		gColumn.setSortable(false);
		gColumn.setResizable(false);
		gColumn.setHeader(Costants.G);
		gColumn.setWidth("5rem").setFlexGrow(0);

		Column<FcGiornataDett> csColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat(Costants.NUMBER_DECIMAL);
			Double dcs = Double.valueOf(0);
			if (gd.getFcPagelle() != null && gd.getFcPagelle().getCs() != null) {
				dcs = gd.getFcPagelle().getCs() / Costants.DIVISORE_100;
			}
			String sCs = myFormatter.format(dcs);

			Span lbl = new Span(sCs);
			lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			lbl.getStyle().set("fontSize", "smaller");
			return lbl;

		}));
		csColumn.setSortable(false);
		csColumn.setResizable(false);
		csColumn.setHeader(Costants.CS);
		csColumn.setWidth("5rem").setFlexGrow(0);

		Column<FcGiornataDett> tsColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
			Double dts = Double.valueOf(0);
			if (gd.getFcPagelle() != null && gd.getFcPagelle().getTs() != null) {
				dts = gd.getFcPagelle().getTs() / Costants.DIVISORE_100;
			}
			String sTs = myFormatter.format(dts);

			Span lbl = new Span(sTs);
			lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			if ("S".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.GRAY);
			} else if ("N".equals(gd.getFlagAttivo())) {
				lbl.getStyle().set("color", Costants.LIGHT_GRAY);
			}
			lbl.getStyle().set("fontSize", "smaller");
			return lbl;
		}));
		tsColumn.setSortable(false);
		tsColumn.setResizable(false);
		tsColumn.setHeader(Costants.TS);
		tsColumn.setWidth("5rem").setFlexGrow(0);

		HeaderRow headerRow = grid.prependHeaderRow();

		HeaderCell headerCellStatoGiocatore = headerRow.join(cognGiocatoreColumn, nomeSquadraColumn);
		headerCellStatoGiocatore.setText(statoGiocatore);

		HeaderCell headerCellModulo = headerRow.join(csColumn, tsColumn);
		if (statoGiocatore.equals("Titolari")) {
			headerCellModulo.setText("Modulo: " + schema);
		}

		return grid;
	}

	private Grid<FcProperties> buildAltriPunteggiInfo(FcCampionato campionato,
			FcAttore attore, FcGiornataInfo giornataInfo, boolean fc, String md,
			List<FcGiornataDett> itemsPanchina) {

		List<FcProperties> items = new ArrayList<>();
		FcProperties b = null;

		NumberFormat formatter = new DecimalFormat("#0.00");

		b = new FcProperties();
		b.setKey("ALTRI PUNTEGGI");
		b.setValue("");
		items.add(b);

		if (giornataInfo.getIdGiornataFc() == 15) {
			FcClassifica cl = classificaController.findByFcCampionatoAndFcAttore(campionato, attore);
			String res = "0";
			if (cl.getIdPosiz() == 1) {
				res = "8";
			} else if (cl.getIdPosiz() == 2) {
				res = "6";
			} else if (cl.getIdPosiz() == 3) {
				res = "4";
			} else if (cl.getIdPosiz() == 4) {
				res = "2";
			}
			b = new FcProperties();
			b.setKey("Bonus Quarti:");
			b.setValue(res);
			items.add(b);
		} else if (giornataInfo.getIdGiornataFc() == 17) {
			FcClassifica cl = classificaController.findByFcCampionatoAndFcAttore(campionato, attore);
			b = new FcProperties();
			b.setKey("Bonus Semifinali:");
			b.setValue("" + cl.getVinte());
			items.add(b);
		}

		if (giornataInfo.getIdGiornataFc() < 15) {
			b = new FcProperties();
			b.setKey("Fattore Campo:");
			if (fc) {
				b.setValue("1,50");
			} else {
				b.setValue("0,00");
			}
			items.add(b);
		}

		b = new FcProperties();
		b.setKey("Modificatore Difesa:");
		b.setValue(md);
		items.add(b);

		double malus = 0.0;
		for (FcGiornataDett gd : itemsPanchina) {
			if ("S".equals(gd.getFlagAttivo()) && (gd.getOrdinamento() == 14 || gd.getOrdinamento() == 16 || gd.getOrdinamento() == 18)) {
				malus += 0.5;
			}
		}
		b = new FcProperties();
		b.setKey("Malus Secondo Cambio:");
		if (malus == 0) {
			b.setValue(formatter.format(malus));
		} else {
			b.setValue("-" + formatter.format(malus));
		}
		items.add(b);

		Grid<FcProperties> grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setItems(items);

		grid.addColumn(proprieta -> proprieta.getKey());
		grid.addColumn(proprieta -> proprieta.getValue());

		return grid;
	}

	private VerticalLayout buildTotaliInfo(FcCampionato campionato,
			FcAttore attore, FcGiornataInfo giornataInfo,
			Double puntiGiornata) {

		VerticalLayout layoutMain = new VerticalLayout();
		layoutMain.setWidth("100%");

		FcGiornataDettInfo info = giornataDettInfoController.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
		FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);

		NumberFormat formatter = new DecimalFormat("#0.00");
		String totG = "0";
		if (puntiGiornata != null) {
			totG = formatter.format(puntiGiornata.doubleValue() / Costants.DIVISORE_100);
		}

		String totPuntiRosa = "0";
		String totPuntiTvsT = "0";
		try {
			if (totPunti != null && totPunti.getTotPtRosa() != null) {
				totPuntiRosa = formatter.format(totPunti.getTotPtRosa() == 0 ? "0" : totPunti.getTotPtRosa() / Costants.DIVISORE_100);
				totPuntiTvsT = "" + totPunti.getPtTvsT();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		Span lblTotGiornata = new Span();
		lblTotGiornata.setText("Totale Giornata: " + totG);
		lblTotGiornata.getStyle().set(Costants.FONT_SIZE, "24px");
		lblTotGiornata.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
		lblTotGiornata.setSizeFull();

		Span lblTotPuntiRosa = new Span();
		lblTotPuntiRosa.setText("Totale Punteggio Rosa: " + totPuntiRosa);
		lblTotPuntiRosa.getStyle().set(Costants.FONT_SIZE, "16px");
		lblTotPuntiRosa.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_YELLOW);
		lblTotPuntiRosa.setSizeFull();

		Span lblTotPuntiTvsT = new Span();
		lblTotPuntiTvsT.setText("Totale Punteggio TvsT: " + totPuntiTvsT);
		lblTotPuntiTvsT.getStyle().set(Costants.FONT_SIZE, "16px");
		lblTotPuntiTvsT.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_GRAY);
		lblTotPuntiTvsT.setSizeFull();

		Span lblInvio = new Span();
		lblInvio.setText("Inviata alle: " + (info == null ? "" : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss")));
		lblInvio.setSizeFull();

		layoutMain.add(lblTotGiornata);
		layoutMain.add(lblTotPuntiRosa);
		layoutMain.add(lblTotPuntiTvsT);
		layoutMain.add(lblInvio);

		return layoutMain;
	}

	private FormLayout buildLegenda() {

		FormLayout layout = new FormLayout();
		layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

		layout.addFormItem(iconGolfatto, "Gol Fatto (+3)");
		layout.addFormItem(iconGolsubito, "Gol Subito (-1)");
		layout.addFormItem(iconAmm, "Ammonizione (-0.5)");
		layout.addFormItem(iconEsp, "Espulsione (-1)");
		layout.addFormItem(iconAssist, "Assist (+1)");
		layout.addFormItem(iconAutogol, "Autogol (-2)");
		layout.addFormItem(iconRigoreSegnato, "Rigore segnato (+3)");
		layout.addFormItem(iconRigoreParato, "Rigore parato (+3)");
		layout.addFormItem(iconRigoreSbagliato, "Rigore sbagliato (-3)");
		layout.addFormItem(iconBonusPortiere, "Portiere imbattuto (+1)");
		layout.addFormItem(iconEntrato, "Entrato");
		layout.addFormItem(iconUscito, "Uscito");

		layout.setResponsiveSteps(new ResponsiveStep("1px",1), new ResponsiveStep("600px",2), new ResponsiveStep("700px",3), new ResponsiveStep("800px",4));

		return layout;

	}

	private String getModificatoreDifesa(String value) {
		String ret = "";

		if (Costants.SCHEMA_541.equals(value)) {
			ret = "2";
		} else if (Costants.SCHEMA_532.equals(value)) {
			ret = "1";
		} else if (Costants.SCHEMA_451.equals(value)) {
			ret = "1";
		} else if (Costants.SCHEMA_433.equals(value)) {
			ret = "-1";
		} else if (Costants.SCHEMA_343.equals(value)) {
			ret = "-2";
		} else {
			ret = "0";
		}

		return ret;
	}

}