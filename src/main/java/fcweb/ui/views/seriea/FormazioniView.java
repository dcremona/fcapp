package fcweb.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.core.io.Resource;
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
	private Image iconBonusPortiere_ = null;

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
		LOG.info("FormazioniView()");
		initImg();
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

	private void initImg() {

		LOG.info("initImg()");

		iconAmm_ = buildImage("classpath:images/", "amm.png", "Ammonizione (-0.5)");
		iconEsp_ = buildImage("classpath:images/", "esp.png", "Espulso (-1)");
		iconAssist_ = buildImage("classpath:images/", "assist.png", "Assist (+1)");
		iconAutogol_ = buildImage("classpath:images/", "autogol.png", "Autogol (-2)");
		iconEntrato_ = buildImage("classpath:images/", "entrato.png", "Entrato");
		iconGolfatto_ = buildImage("classpath:images/", "golfatto.png", "Gol Fatto (+3)");
		iconGolsubito_ = buildImage("classpath:images/", "golsubito.png", "Gol subito (-1)");
		iconUscito_ = buildImage("classpath:images/", "uscito.png", "Uscito");
		iconRigoreSbagliato_ = buildImage("classpath:images/", "rigoresbagliato.png", "Rigore sbagliato (-3)");
		iconRigoreSegnato_ = buildImage("classpath:images/", "rigoresegnato.png", "Rigore segnato (+3)");
		iconRigoreParato_ = buildImage("classpath:images/", "rigoreparato.png", "Rigore parato (+3)");
		iconBonusPortiere_ = buildImage("classpath:images/", "portiereImbattuto.png", "Portiere imbattuto (+1)");
	}

	private void initLayout() {

		LOG.info("initLayout()");

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
			LOG.info("addValueChangeListener ");
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
		
		mainLayout.setSizeFull();

		add(comboGiornata);

		add(mainLayout);

		add(buildLegenda());

		comboGiornata.setValue(giornataInfo);
	}

	@SuppressWarnings("unchecked")
	private void buildTabGiornata(VerticalLayout layout, String giornata) {

//		VerticalLayout container = new VerticalLayout();
//		PagedTabs tabs = new PagedTabs(container);
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer currGG = 0;
		if (campionato.getIdCampionato() == 1) {
			currGG = Integer.valueOf(giornata);
		} else if (campionato.getIdCampionato() == 2) {
			// currGG = new Integer(giornata) + 19;
			currGG = Integer.valueOf(giornata);
		}

		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(currGG);
		List<FcGiornata> partite = giornataController.findByFcGiornataInfo(giornataInfo);

		for (FcGiornata p : partite) {

			// CASA
			FcAttore attoreCasa = p.getFcAttoreByIdAttoreCasa();
			HashMap<String, Object> mapCasa = buildData(attoreCasa, giornataInfo);

			// List<FcGiornataDett> itemsCasa = (List<FcGiornataDett>)
			// mapCasa.get("items");
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

			// List<FcGiornataDett> itemsFuori = (List<FcGiornataDett>)/
			// mapFuori.get("items");
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
			Image imgCasa = buildImage("classpath:images/number/", p.getGolCasa() == null ? "0.png" : p.getGolCasa() + ".png", "Media voto");
			Image imgFuori = buildImage("classpath:images/number/", p.getGolFuori() == null ? "0.png" : p.getGolFuori() + ".png", "Media voto");
			layoutRisultato.add(imgCasa);
			layoutRisultato.add(imgFuori);

			HorizontalLayout horizontalLayout0 = new HorizontalLayout();
			horizontalLayout0.setWidth("100%");
			horizontalLayout0.getStyle().set("border", Costants.BORDER_COLOR);
			// horizontalLayout0.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
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
			// vCasa.setSizeFull();

			VerticalLayout vFuori = new VerticalLayout();
			vFuori.add(tableSqFuoriTitolari);
			vFuori.add(tableSqFuoriPanchina);
			vFuori.add(tableSqFuoriTribuna);
			vFuori.add(tableAltriPunteggiFuori);
			vFuori.add(layoutTotaliFuori);
			// vFuori.setSizeFull();

			HorizontalLayout horizontalLayout1 = new HorizontalLayout();
			horizontalLayout1.setWidth("100%");
			horizontalLayout1.add(vCasa);
			horizontalLayout1.add(vFuori);
			// horizontalLayout1.setSizeFull();

			// layout.add(horizontalLayout0);
			// layout.add(horizontalLayout1);
			// layout.setSizeFull();

			final VerticalLayout layoutTab = new VerticalLayout();
			layoutTab.setMargin(false);
			layoutTab.setPadding(false);
			layoutTab.setSpacing(false);
			layoutTab.add(horizontalLayout0);
			layoutTab.add(horizontalLayout1);

			//tabs.add(attoreCasa.getDescAttore() + " [*] " + attoreFuori.getDescAttore(), layoutTab, false);
			tabSheet.add(attoreCasa.getDescAttore() + " [*] " + attoreFuori.getDescAttore(), layoutTab);

		}

		//layout.add(tabs, container);
		layout.add(tabSheet);
		
	}

	private HashMap<String, Object> buildData(FcAttore attore,
			FcGiornataInfo giornataInfo) {

		LOG.info("START buildData " + attore.getDescAttore());

		HashMap<String, Object> map = new HashMap<>();

		List<FcGiornataDett> all = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
		List<FcGiornataDett> items = new ArrayList<FcGiornataDett>();

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

		List<FcGiornataDett> itemsTitolari = new ArrayList<FcGiornataDett>();
		List<FcGiornataDett> itemsPanchina = new ArrayList<FcGiornataDett>();
		List<FcGiornataDett> itemsTribuna = new ArrayList<FcGiornataDett>();
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

		LOG.info("END buildData " + attore.getDescAttore());

		return map;
	}

	private Grid<FcGiornataDett> buildResultSquadra(List<FcGiornataDett> items,
			String statoGiocatore, String schema) {

		Grid<FcGiornataDett> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);

		Column<FcGiornataDett> ruoloColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (f != null && f.getFcGiocatore() != null) {
				Image img = buildImage("classpath:images/", f.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png", f.getFcGiocatore().getFcRuolo().getDescRuolo());
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
			if (gd != null && gd.getFcGiocatore() != null) {

				String descGiocatore = gd.getFcGiocatore().getCognGiocatore();
				if ("S".equals(gd.getFlagAttivo()) && (gd.getOrdinamento() == 14 || gd.getOrdinamento() == 16 || gd.getOrdinamento() == 18)) {
					descGiocatore = "(-0,5) " + gd.getFcGiocatore().getCognGiocatore();
				}

				Span lblGiocatore = new Span(descGiocatore);
				lblGiocatore.getStyle().set("fontSize", "smaller");
				cellLayout.add(lblGiocatore);

				ArrayList<Image> info = new ArrayList<Image>();
				if (gd.getOrdinamento() < 12 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "N".equals(gd.getFlagAttivo().toUpperCase())) {
					info.add(buildImage("classpath:images/", "uscito_s.png", "Uscito"));
				}

				if (gd.getOrdinamento() > 11 && StringUtils.isNotEmpty(gd.getFlagAttivo()) && "S".equals(gd.getFlagAttivo().toUpperCase())) {
					info.add(buildImage("classpath:images/", "entrato_s.png", "Entrato"));
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
					info.add(buildImage("classpath:images/", "amm_s.png", "Ammonizione (-0,5)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getEspulsione(); a++) {
					info.add(buildImage("classpath:images/", "esp_s.png", "Espulsione (-1)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalSubito(); a++) {
					info.add(buildImage("classpath:images/", "golsubito_s.png", "Gol subito (-1)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getGoalRealizzato() - gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(buildImage("classpath:images/", "golfatto_s.png", "Gol fatto (+3)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getAutorete(); a++) {
					info.add(buildImage("classpath:images/", "autogol_s.png", "Autogol (-2)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreFallito(); a++) {
					info.add(buildImage("classpath:images/", "rigoresbagliato_s.png", "Rigore sbagliato (-3)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreSegnato(); a++) {
					info.add(buildImage("classpath:images/", "rigoresegnato_s.png", "Rigore segnato (+3)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getRigoreParato(); a++) {
					info.add(buildImage("classpath:images/", "rigoreparato_s.png", "Rigore parato (+3)"));
				}

				for (int a = 0; a < gd.getFcPagelle().getAssist(); a++) {
					info.add(buildImage("classpath:images/", "assist_s.png", "Assist (+1)"));
				}

				if ("P".equals(gd.getFcGiocatore().getFcRuolo().getIdRuolo()) && gd.getFcPagelle().getGoalSubito() == 0 && gd.getFcPagelle().getEspulsione() == 0 && gd.getFcPagelle().getVotoGiocatore() != 0) {
					if (gd.getFcPagelle().getG() != 0 && gd.getFcPagelle().getCs() != 0 && gd.getFcPagelle().getTs() != 0) {
						info.add(buildImage("classpath:images/", "portiereImbattuto_s.png", "Portiere imbattuto (+1)"));
					}
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
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
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
		votoColumn.setHeader("FV");
		votoColumn.setAutoWidth(true);

		Column<FcGiornataDett> gColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
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
		gColumn.setHeader("G");
		gColumn.setAutoWidth(true);

		Column<FcGiornataDett> csColumn = grid.addColumn(new ComponentRenderer<>(gd -> {
			DecimalFormat myFormatter = new DecimalFormat("#0.00");
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
		csColumn.setHeader("Cs");
		csColumn.setAutoWidth(true);

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
		tsColumn.setHeader("Ts");
		tsColumn.setAutoWidth(true);

		HeaderRow headerRow = grid.prependHeaderRow();

		HeaderCell headerCellStatoGiocatore = headerRow.join(ruoloColumn, cognGiocatoreColumn);
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

		List<FcProperties> items = new ArrayList<FcProperties>();
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
		// grid.setSizeFull();
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
		}

		Span lblTotGiornata = new Span();
		lblTotGiornata.setText("Totale Giornata: " + totG);
		lblTotGiornata.getStyle().set("font-size", "24px");
		lblTotGiornata.getStyle().set("background", Costants.LIGHT_BLUE);
		lblTotGiornata.setSizeFull();

		Span lblTotPuntiRosa = new Span();
		lblTotPuntiRosa.setText("Totale Punteggio Rosa: " + totPuntiRosa);
		lblTotPuntiRosa.getStyle().set("font-size", "16px");
		lblTotPuntiRosa.getStyle().set("background", Costants.LIGHT_YELLOW);
		lblTotPuntiRosa.setSizeFull();

		Span lblTotPuntiTvsT = new Span();
		lblTotPuntiTvsT.setText("Totale Punteggio TvsT: " + totPuntiTvsT);
		lblTotPuntiTvsT.getStyle().set("font-size", "16px");
		lblTotPuntiTvsT.getStyle().set("background", Costants.LIGHT_GRAY);
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
		layout.getStyle().set("border", Costants.BORDER_COLOR);

		layout.addFormItem(iconGolfatto_, "Gol Fatto (+3)");
		layout.addFormItem(iconGolsubito_, "Gol Subito (-1)");
		layout.addFormItem(iconAmm_, "Ammonizione (-0.5)");
		layout.addFormItem(iconEsp_, "Espulsione (-1)");
		layout.addFormItem(iconAssist_, "Assist (+1)");
		layout.addFormItem(iconAutogol_, "Autogol (-2)");
		layout.addFormItem(iconRigoreSegnato_, "Rigore segnato (+3)");
		layout.addFormItem(iconRigoreParato_, "Rigore parato (+3)");
		layout.addFormItem(iconRigoreSbagliato_, "Rigore sbagliato (-3)");
		layout.addFormItem(iconBonusPortiere_, "Portiere imbattuto (+1)");
		layout.addFormItem(iconEntrato_, "Entrato");
		layout.addFormItem(iconUscito_, "Uscito");

		layout.setResponsiveSteps(new ResponsiveStep("1px",1), new ResponsiveStep("600px",2), new ResponsiveStep("700px",3), new ResponsiveStep("800px",4));

		// VerticalLayout layout = new VerticalLayout();
		// layout.getStyle().set("border", Costants.BORDER_COLOR);
		// // layout.setSizeFull();
		// layout.setMargin(false);
		//
		// HorizontalLayout horizontalLayout1 = new HorizontalLayout();
		// horizontalLayout1.setSpacing(true);
		//
		// horizontalLayout1.add(iconGolfatto_);
		// Label lbl = new Label("Gol Fatto");
		// horizontalLayout1.add(lbl);
		//
		// horizontalLayout1.add(iconGolsubito_);
		// lbl = new Label("Gol Subito");
		// horizontalLayout1.add(lbl);
		//
		// horizontalLayout1.add(iconAmm_);
		// lbl = new Label("Ammonizione");
		// horizontalLayout1.add(lbl);
		//
		// horizontalLayout1.add(iconEsp_);
		// lbl = new Label("Espulsione");
		// horizontalLayout1.add(lbl);
		//
		// horizontalLayout1.add(iconAssist_);
		// lbl = new Label("Assist");
		// horizontalLayout1.add(lbl);
		//
		// horizontalLayout1.add(iconEntrato_);
		// lbl = new Label("Entrato");
		// horizontalLayout1.add(lbl);
		//
		// HorizontalLayout horizontalLayout2 = new HorizontalLayout();
		// horizontalLayout2.setSpacing(true);
		//
		// horizontalLayout2.add(iconUscito_);
		// lbl = new Label("Uscito");
		// horizontalLayout2.add(lbl);
		//
		// horizontalLayout2.add(iconAutogol_);
		// lbl = new Label("Autogol");
		// horizontalLayout2.add(lbl);
		//
		// horizontalLayout2.add(iconRigoreSegnato_);
		// lbl = new Label("Rigore segnato");
		// horizontalLayout2.add(lbl);
		//
		// horizontalLayout2.add(iconRigoreSbagliato_);
		// lbl = new Label("Rigore sbagliato");
		// horizontalLayout2.add(lbl);
		//
		// horizontalLayout2.add(iconRigoreParato_);
		// lbl = new Label("Rigore parato");
		// horizontalLayout2.add(lbl);
		//
		// horizontalLayout2.add(iconBonusPortiere_);
		// lbl = new Label("Bonus Portiere");
		// horizontalLayout2.add(lbl);
		//
		// layout.add(horizontalLayout1);
		// layout.add(horizontalLayout2);

		return layout;

	}

	private String getModificatoreDifesa(String value) {
		String ret = "";

		if (value.equals("5-4-1")) {
			ret = "2";
		} else if (value.equals("5-3-2")) {
			ret = "1";
		} else if (value.equals("4-5-1")) {
			ret = "1";
		} else if (value.equals("4-3-3")) {
			ret = "-1";
		} else if (value.equals("3-4-3")) {
			ret = "-2";
		} else {
			ret = "0";
		}

		return ret;
	}

	private Image buildImage(String path, String nomeImg, String title) {
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
		img.setTitle(title);
		return img;
	}

}