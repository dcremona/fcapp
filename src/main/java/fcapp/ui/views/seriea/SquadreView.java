package fcapp.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcMercatoDett;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.JasperPdfService;
import fcapp.backend.service.MercatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Rose")
@Route(value = "squadre", layout = MainLayout.class)
@RolesAllowed("USER")
public class SquadreView extends VerticalLayout {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(SquadreView.class);

	private static final String DECIMAL_PATTERN = "#0.00";
	private static final DecimalFormat STAT_FORMATTER = new DecimalFormat(DECIMAL_PATTERN);

	/** Legacy constraint: active attori are in 1..8. */
	private static final int MIN_ATTORE_ID = 1;
	private static final int MAX_ATTORE_ID = 8;

	private static final String TREND_UP = "1.png";
	private static final String TREND_FLAT = "2.png";
	private static final String TREND_DOWN = "3.png";

	private static final String REPORT_ROSA = "/reports/roseFc.jasper";
	private static final String REPORT_STATISTICA = "/reports/statistica.jasper";

	private final transient ResourceLoader resourceLoader;
	private final transient AttoreService attoreService;
	private final transient FormazioneService formazioneService;
	private final transient MercatoService mercatoService;
	private final transient AccessoService accessoService;
	private final transient JasperPdfService jasperPdfService;

	private List<FcAttore> squadre = new ArrayList<>();

	public SquadreView(ResourceLoader resourceLoader,
			AttoreService attoreService,
			FormazioneService formazioneService,
			MercatoService mercatoService,
			AccessoService accessoService,
			JasperPdfService jasperPdfService) {
		this.resourceLoader = resourceLoader;
		this.attoreService = attoreService;
		this.formazioneService = formazioneService;
		this.mercatoService = mercatoService;
		this.accessoService = accessoService;
		this.jasperPdfService = jasperPdfService;
		log.info("SquadreView()");
	}

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoService.insertAccesso(getClass().getName());
		initData();
		initLayout();
	}

	// -------------------------------------------------------------------------
	// Lifecycle / data
	// -------------------------------------------------------------------------

	private void initData() {
		squadre = attoreService.findByActive(true);
	}

	private void initLayout() {
		FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
		FcGiornataInfo giornataInfo = getSessionAttribute("GIORNATA_INFO", FcGiornataInfo.class);
		if (campionato == null || giornataInfo == null) {
			return;
		}

		TabSheet tabSheet = new TabSheet();
		squadre.stream()
				.filter(this::isValidAttore)
				.forEach(attore -> tabSheet.add(attore.getDescAttore(),
						buildAttoreTab(campionato, giornataInfo, attore)));
		tabSheet.setSizeFull();
		add(tabSheet);
	}

	private VerticalLayout buildAttoreTab(FcCampionato campionato, FcGiornataInfo giornataInfo, FcAttore attore) {
		List<FcFormazione> rosa = formazioneService
				.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, true);
		List<FcMercatoDett> movimenti = findMercatoDettagli(campionato, attore);

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.add(buildDownloadButtons(campionato, giornataInfo, attore));
		layout.add(buildFormazioneGrid(rosa, calculateTotalePagato(rosa)));
		layout.add(buildMercatoGrid(movimenti));
		return layout;
	}

	private HorizontalLayout buildDownloadButtons(FcCampionato campionato, FcGiornataInfo giornataInfo,
			FcAttore attore) {
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.add(createPdfDownloadLink(
				"Download Rosa Pdf",
				"Rosa_" + attore.getDescAttore() + ".pdf",
				REPORT_ROSA,
				params -> {
					params.put("ATTORE", String.valueOf(attore.getIdAttore()));
					params.put("PATH_IMG", "img/");
				},
				campionato));
		buttons.add(createPdfDownloadLink(
				"Download Voti Rosa Pdf",
				"Voti_Rosa_" + attore.getDescAttore() + ".pdf",
				REPORT_STATISTICA,
				params -> {
					params.put("START", String.valueOf(campionato.getStart()));
					params.put("END", String.valueOf(giornataInfo.getCodiceGiornata()));
					params.put("ID_ATTORE", String.valueOf(attore.getIdAttore()));
				},
				campionato));
		return buttons;
	}

	private boolean isValidAttore(FcAttore attore) {
		return attore != null
				&& attore.getIdAttore() >= MIN_ATTORE_ID
				&& attore.getIdAttore() <= MAX_ATTORE_ID;
	}

	private int calculateTotalePagato(List<FcFormazione> formazioni) {
		return formazioni.stream()
				.map(FcFormazione::getTotPagato)
				.filter(Objects::nonNull)
				.mapToInt(Integer::intValue)
				.sum();
	}

	private List<FcMercatoDett> findMercatoDettagli(FcCampionato campionato, FcAttore attore) {
		FcGiornataInfo start = new FcGiornataInfo();
		start.setCodiceGiornata(campionato.getStart());

		FcGiornataInfo end = new FcGiornataInfo();
		end.setCodiceGiornata(campionato.getEnd());

		return mercatoService
				.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
						start, end, attore);
	}

	// -------------------------------------------------------------------------
	// Report downloads
	// -------------------------------------------------------------------------

	private Anchor createPdfDownloadLink(String linkText, String fileName, String reportPath,
			java.util.function.Consumer<Map<String, Object>> extraParams, FcCampionato campionato) {
		return new Anchor(DownloadHandler.fromInputStream(event -> {
			try (InputStream report = getClass().getResourceAsStream(reportPath)) {
				if (report == null) {
					throw new IllegalStateException("File report non trovato: " + reportPath);
				}
				Map<String, Object> params = baseReportParams(campionato);
				extraParams.accept(params);
				byte[] pdf = jasperPdfService.generaPdf(reportPath, params);
				return new DownloadResponse(
						new ByteArrayInputStream(pdf),
						fileName,
						"application/pdf",
						pdf.length);
			} catch (Exception e) {
				log.error("Errore generazione PDF {}", fileName, e);
				return DownloadResponse.error(500);
			}
		}), linkText);
	}

	private Map<String, Object> baseReportParams(FcCampionato campionato) {
		Map<String, Object> params = new HashMap<>();
		params.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
		params.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));
		return params;
	}

	// -------------------------------------------------------------------------
	// Formazione grid
	// -------------------------------------------------------------------------

	private Grid<FcFormazione> buildFormazioneGrid(List<FcFormazione> items, int totalePagato) {
		Grid<FcFormazione> grid = createBaseGrid(items);

		Column<FcFormazione> ruoloCol = grid
				.addColumn(new ComponentRenderer<>(this::buildRuoloComponent))
				.setSortable(true)
				.setHeader("R")
				.setAutoWidth(true);

		Column<FcFormazione> giocatoreCol = grid
				.addColumn(new ComponentRenderer<>(this::buildGiocatoreComponent))
				.setSortable(false)
				.setHeader(Costants.GIOCATORE)
				.setAutoWidth(true);

		Column<FcFormazione> squadraCol = grid
				.addColumn(new ComponentRenderer<>(this::buildSquadraComponent))
				.setSortable(true)
				.setComparator(Comparator.comparing(this::getNomeSquadraSafe))
				.setHeader(Costants.SQUADRA)
				.setAutoWidth(true);

		Column<FcFormazione> mediaVotoCol = grid
				.addColumn(new ComponentRenderer<>(f -> buildStatisticaComponent(f, true)))
				.setSortable(true)
				.setComparator(Comparator.comparing(this::getMediaVotoSafe))
				.setHeader(Costants.MV)
				.setAutoWidth(true)
				.setKey("fcStatistiche.mediaVoto");

		Column<FcFormazione> fantaMediaCol = grid
				.addColumn(new ComponentRenderer<>(f -> buildStatisticaComponent(f, false)))
				.setSortable(true)
				.setComparator(Comparator.comparing(this::getFantaMediaSafe))
				.setHeader(Costants.FMV)
				.setAutoWidth(true)
				.setKey("fcStatistiche.fantaMedia");

		Column<FcFormazione> quotazioneCol = grid
				.addColumn(f -> Optional.ofNullable(f.getFcGiocatore())
						.map(FcGiocatore::getQuotazione)
						.orElse(0))
				.setSortable(true)
				.setHeader(Costants.Q)
				.setAutoWidth(true);

		Column<FcFormazione> totPagatoCol = grid
				.addColumn(f -> Optional.ofNullable(f.getTotPagato()).orElse(0))
				.setSortable(true)
				.setHeader(Costants.P)
				.setAutoWidth(true);

		HeaderRow topRow = grid.prependHeaderRow();
		HeaderCell header = topRow.join(ruoloCol, giocatoreCol, squadraCol, mediaVotoCol, fantaMediaCol, quotazioneCol,
				totPagatoCol);
		header.setComponent(buildSectionHeader("Rosa Ufficiale"));

		FooterRow footer = grid.appendFooterRow();
		footer.getCell(quotazioneCol).setComponent(buildFooterCell(Costants.TOTALE));
		footer.getCell(totPagatoCol).setComponent(buildFooterCell(String.valueOf(totalePagato)));

		return grid;
	}

	// -------------------------------------------------------------------------
	// Mercato grid
	// -------------------------------------------------------------------------

	private Grid<FcMercatoDett> buildMercatoGrid(List<FcMercatoDett> items) {
		Grid<FcMercatoDett> grid = createBaseGrid(items);

		Column<FcMercatoDett> giornataCol = grid
				.addColumn(m -> m.getFcGiornataInfo().getCodiceGiornata())
				.setSortable(false)
				.setHeader(Costants.GIORNATA)
				.setAutoWidth(true);

		Column<FcMercatoDett> dataCol = grid
				.addColumn(new LocalDateTimeRenderer<>(FcMercatoDett::getDataCambio,
						() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)))
				.setSortable(false)
				.setHeader(Costants.DATA)
				.setAutoWidth(true);

		Column<FcMercatoDett> ruoloAcqCol = grid
				.addColumn(new ComponentRenderer<>(m -> buildRuoloMercatoComponent(m.getFcGiocatoreByIdGiocAcq())))
				.setHeader("")
				.setAutoWidth(true);

		Column<FcMercatoDett> acquistoCol = grid
				.addColumn(new ComponentRenderer<>(m -> buildMercatoGiocatoreComponent(m.getFcGiocatoreByIdGiocAcq())))
				.setSortable(false)
				.setHeader(Costants.ACQUISTI)
				.setAutoWidth(true);

		Column<FcMercatoDett> ruoloVenCol = grid
				.addColumn(new ComponentRenderer<>(m -> buildRuoloMercatoComponent(m.getFcGiocatoreByIdGiocVen())))
				.setHeader("")
				.setAutoWidth(true);

		Column<FcMercatoDett> cessioneCol = grid
				.addColumn(new ComponentRenderer<>(m -> buildMercatoGiocatoreComponent(m.getFcGiocatoreByIdGiocVen())))
				.setSortable(false)
				.setHeader(Costants.CESSIONI)
				.setAutoWidth(true);

		Column<FcMercatoDett> notaCol = grid
				.addColumn(FcMercatoDett::getNota)
				.setSortable(false)
				.setHeader(Costants.NOTA)
				.setAutoWidth(true);

		HeaderRow topRow = grid.prependHeaderRow();
		HeaderCell header = topRow.join(giornataCol, dataCol, ruoloAcqCol, acquistoCol, ruoloVenCol, cessioneCol,
				notaCol);
		header.setComponent(buildSectionHeader("Cambi Rosa"));

		return grid;
	}

	private <T> Grid<T> createBaseGrid(List<T> items) {
		Grid<T> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		return grid;
	}

	// -------------------------------------------------------------------------
	// Cell components – formazione
	// -------------------------------------------------------------------------

	private Component buildRuoloComponent(FcFormazione formazione) {
		HorizontalLayout layout = compactHorizontalLayout();
		String ruolo = getRuoloSafe(formazione);
		if (StringUtils.isNotBlank(ruolo)) {
			layout.add(buildRoleImage(ruolo));
		}
		return layout;
	}

	private Component buildGiocatoreComponent(FcFormazione formazione) {
		HorizontalLayout layout = compactHorizontalLayout();
		FcGiocatore giocatore = getGiocatore(formazione);
		if (giocatore == null) {
			return layout;
		}
		addPlayerImage(layout, giocatore);
		layout.add(new Span(giocatore.getCognGiocatore()));
		return layout;
	}

	private Component buildSquadraComponent(FcFormazione formazione) {
		HorizontalLayout layout = compactHorizontalLayout();
		FcSquadra squadra = Optional.ofNullable(getGiocatore(formazione))
				.map(FcGiocatore::getFcSquadra)
				.orElse(null);
		if (squadra == null) {
			return layout;
		}
		if (squadra.getImg() != null) {
			try {
				layout.add(Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream()));
			} catch (SQLException e) {
				log.error("Errore caricamento immagine squadra {}", squadra.getNomeSquadra(), e);
			}
		}
		layout.add(new Span(squadra.getNomeSquadra()));
		return layout;
	}

	private Component buildStatisticaComponent(FcFormazione formazione, boolean mediaVoto) {
		HorizontalLayout layout = compactHorizontalLayout();
		double value = mediaVoto ? getMediaVotoSafe(formazione) : getFantaMediaSafe(formazione);
		layout.add(buildTrendImage(value));
		layout.add(new Span(formatStatistic(value)));
		return layout;
	}

	// -------------------------------------------------------------------------
	// Cell components – mercato
	// -------------------------------------------------------------------------

	private Component buildRuoloMercatoComponent(FcGiocatore giocatore) {
		HorizontalLayout layout = compactHorizontalLayout();
		if (giocatore != null && giocatore.getFcRuolo() != null) {
			layout.add(buildRoleImage(giocatore.getFcRuolo().getIdRuolo()));
		}
		return layout;
	}

	private Component buildMercatoGiocatoreComponent(FcGiocatore giocatore) {
		FlexLayout layout = new FlexLayout();
		if (giocatore == null) {
			return layout;
		}
		addPlayerImage(layout, giocatore);
		layout.add(new Span(giocatore.getCognGiocatore()));

		String nomeSquadra = Optional.ofNullable(giocatore.getFcSquadra())
				.map(FcSquadra::getNomeSquadra)
				.orElse("");
		if (StringUtils.isNotBlank(nomeSquadra)) {
			Span badge = new Span(" (" + nomeSquadra.substring(0, Math.min(3, nomeSquadra.length())) + ")");
			badge.getStyle().set(Costants.FONT_SIZE, "10px");
			layout.add(badge);
		}
		return layout;
	}

	private void addPlayerImage(HasComponents layout, FcGiocatore giocatore) {
		if (StringUtils.isBlank(giocatore.getNomeImg()) || giocatore.getImgSmall() == null) {
			return;
		}
		try {
			layout.add(Utils.getImage(giocatore.getNomeImg(), giocatore.getImgSmall().getBinaryStream()));
		} catch (SQLException e) {
			log.error("Errore caricamento immagine giocatore {}", giocatore.getCognGiocatore(), e);
		}
	}

	// -------------------------------------------------------------------------
	// Shared UI helpers
	// -------------------------------------------------------------------------

	private HorizontalLayout compactHorizontalLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setSpacing(false);
		return layout;
	}

	private Div buildSectionHeader(String text) {
		Div title = new Div();
		title.setText(text);
		title.getStyle().set(Costants.FONT_SIZE, "16px");
		title.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
		return title;
	}

	private Div buildFooterCell(String text) {
		Div footer = new Div();
		footer.setText(text);
		footer.getStyle().set(Costants.FONT_SIZE, "20px");
		footer.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_GRAY);
		return footer;
	}

	private Image buildRoleImage(String ruolo) {
		String imageName = ruolo.toLowerCase() + ".png";
		return Utils.buildImage(imageName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));
	}

	private Image buildTrendImage(double value) {
		String imageName = resolveTrendImage(value);
		return Utils.buildImage(imageName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));
	}

	private String resolveTrendImage(double value) {
		if (value == 0 || (value >= Costants.RANGE_MIN_MV && value <= Costants.RANGE_MAX_MV)) {
			return TREND_FLAT;
		}
		return value > Costants.RANGE_MAX_MV ? TREND_UP : TREND_DOWN;
	}

	private String formatStatistic(double value) {
		return STAT_FORMATTER.format(value / Costants.DIVISORE_100);
	}

	// -------------------------------------------------------------------------
	// Null-safe accessors
	// -------------------------------------------------------------------------

	private FcGiocatore getGiocatore(FcFormazione formazione) {
		return formazione != null ? formazione.getFcGiocatore() : null;
	}

	private String getRuoloSafe(FcFormazione formazione) {
		return Optional.ofNullable(getGiocatore(formazione))
				.map(FcGiocatore::getFcRuolo)
				.map(r -> r.getIdRuolo())
				.orElse(null);
	}

	private String getNomeSquadraSafe(FcFormazione formazione) {
		return Optional.ofNullable(getGiocatore(formazione))
				.map(FcGiocatore::getFcSquadra)
				.map(FcSquadra::getNomeSquadra)
				.orElse("");
	}

	private double getMediaVotoSafe(FcFormazione formazione) {
		FcStatistiche stats = getStatisticheSafe(formazione);
		return stats != null ? stats.getMediaVoto() : 0d;
	}

	private double getFantaMediaSafe(FcFormazione formazione) {
		FcStatistiche stats = getStatisticheSafe(formazione);
		return stats != null ? stats.getFantaMedia() : 0d;
	}

	private FcStatistiche getStatisticheSafe(FcFormazione formazione) {
		return Optional.ofNullable(getGiocatore(formazione))
				.map(FcGiocatore::getFcStatistiche)
				.orElse(null);
	}

	@SuppressWarnings("unchecked")
	private <T> T getSessionAttribute(String key, Class<T> type) {
		Object value = VaadinSession.getCurrent().getAttribute(key);
		return value == null ? null : (T) value;
	}
}