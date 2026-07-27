package fcapp.ui.views.seriea;

import java.io.Serial;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.relativetime.Format;
import com.flowingcode.vaadin.addons.relativetime.RelativeTime;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.Calendario;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcGiornataRis;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataRisService;
import fcapp.backend.service.GiornataService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Home")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("USER")
public class HomeView extends VerticalLayout {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String SESSION_GIORNATA_INFO = "GIORNATA_INFO";
	private static final String SESSION_NEXT_DATE = "NEXTDATE";
	private static final String SESSION_FUTURE = "FUTURE";
	private static final String SESSION_ATTORE = "ATTORE";
	private static final String SESSION_CAMPIONATO = "CAMPIONATO";

	private static final String DECIMAL_PATTERN = "#0.00";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final transient GiornataService giornataService;
	private final transient GiornataInfoService giornataInfoService;
	private final transient GiornataRisService giornataRisService;
	private final transient AccessoService accessoService;

	public HomeView(GiornataService giornataService, GiornataInfoService giornataInfoService,
			GiornataRisService giornataRisService, AccessoService accessoService) {

		this.giornataService = giornataService;
		this.giornataInfoService = giornataInfoService;
		this.giornataRisService = giornataRisService;
		this.accessoService = accessoService;

		log.info("HomeView()");
	}

	@PostConstruct
	void init() {
		log.info("init");

		try {
			if (!Utils.isValidVaadinSession()) {
				return;
			}

			accessoService.insertAccesso(getClass().getName());

			add(buildInfoGiornate());
			add(buildLayoutAvviso());
			add(buildLayoutRisultati());

		} catch (Exception e) {
			log.error("Errore durante l'inizializzazione della HomeView", e);
		}
	}

	private HorizontalLayout buildInfoGiornate() {
		HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		wrapper.setSizeFull();

		FcGiornataInfo giornataCorrente = getSessionAttribute(SESSION_GIORNATA_INFO, FcGiornataInfo.class);
		if (giornataCorrente == null) {
			return wrapper;
		}

		if (giornataCorrente.getCodiceGiornata() > 1) {
			FcGiornataInfo giornataPrecedente = giornataInfoService
					.findByCodiceGiornata(giornataCorrente.getCodiceGiornata() - 1);

			wrapper.add(buildGiornataPanel("Ultima Giornata - " + Utils.buildInfoGiornata(giornataPrecedente),
					giornataPrecedente));
		}

		wrapper.add(buildGiornataPanel("Prossima Giornata - " + Utils.buildInfoGiornata(giornataCorrente),
				giornataCorrente));

		return wrapper;
	}

	private VerticalLayout buildGiornataPanel(String title, FcGiornataInfo giornataInfo) {
		VerticalLayout layout = new VerticalLayout();
		layout.add(buildSectionTitle(title));
		layout.add(createGridGiornata(getDataTable(giornataInfo)));
		return layout;
	}

	private Div buildSectionTitle(String text) {
		Div title = new Div();
		title.setText(text);
		title.getStyle().set(Costants.FONT_SIZE, "16px");
		title.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
		title.setSizeFull();
		return title;
	}

	private List<Calendario> getDataTable(FcGiornataInfo giornataInfo) {
		List<FcGiornata> giornate = giornataService.findByFcGiornataInfo(giornataInfo);
		List<Calendario> calendarioItems = new ArrayList<>();

		int id = 1;
		for (FcGiornata giornata : giornate) {
			Calendario calendario = new Calendario();
			calendario.setId(id++);
			calendario.setAttoreCasa(giornata.getFcAttoreByIdAttoreCasa().getDescAttore());
			calendario.setAttoreFuori(giornata.getFcAttoreByIdAttoreFuori().getDescAttore());
			calendario.setRisultato(formatTotaliPartita(giornata));
			calendario.setPunteggio(formatGolPartita(giornata));
			calendarioItems.add(calendario);
		}

		return calendarioItems;
	}

	private Grid<Calendario> createGridGiornata(List<Calendario> items) {
		Grid<Calendario> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);

		grid.addColumn(Calendario::getAttoreCasa).setAutoWidth(true);
		grid.addColumn(Calendario::getPunteggio).setAutoWidth(true);
		grid.addColumn(Calendario::getAttoreFuori).setAutoWidth(true);
		grid.addColumn(Calendario::getRisultato).setAutoWidth(true);

		return grid;
	}

	private VerticalLayout buildLayoutAvviso() {
		FcGiornataInfo giornataInfo = getSessionAttribute(SESSION_GIORNATA_INFO, FcGiornataInfo.class);
		String nextDate = getSessionAttribute(SESSION_NEXT_DATE, String.class);
		LocalDateTime dateTime = getSessionAttribute(SESSION_FUTURE, LocalDateTime.class);

		VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);

		if (giornataInfo != null) {
			layoutAvviso.add(buildRow("Prossima Giornata: " + Utils.buildInfoGiornata(giornataInfo)));
		}

		layoutAvviso.add(buildRow("Consegna Formazione entro: " + Objects.toString(nextDate, "")));
		
		Instant future = dateTime.atZone(ZoneId.of("UTC")).toInstant();
		layoutAvviso.add(new RelativeTime(future).setFormat(Format.DURATION));
		
		return layoutAvviso;
	}

	private HorizontalLayout buildRow(String text) {
		HorizontalLayout row = new HorizontalLayout();
		row.add(new Span(text));
		return row;
	}

	private VerticalLayout buildLayoutRisultati() {
		FormLayout layout = new FormLayout();
		layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layout.setResponsiveSteps(new ResponsiveStep("1px", 1), new ResponsiveStep("500px", 2),
				new ResponsiveStep("600px", 3), new ResponsiveStep("700px", 4), new ResponsiveStep("800px", 5));

		FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);
		FcCampionato campionato = getSessionAttribute(SESSION_CAMPIONATO, FcCampionato.class);

		if (attore == null || campionato == null) {
			return new VerticalLayout(layout);
		}

		List<FcGiornataRis> risultati = giornataRisService.findByFcAttoreOrderByFcGiornataInfoAsc(attore);
		Integer from = campionato.getStart();

		for (FcGiornataRis risultato : risultati) {
			int codiceGiornata = risultato.getFcGiornataInfo().getCodiceGiornata();
			if (codiceGiornata >= from) {
				FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(codiceGiornata);
				layout.add(buildCard(giornataInfo, attore, risultato));
			}
		}

		return new VerticalLayout(layout);
	}

	private Card buildCard(FcGiornataInfo giornataInfo, FcAttore attore, FcGiornataRis fcGiornataRis) {
		Card card = new Card();

		BadgeInfo badgeInfo = resolveBadge(fcGiornataRis.getIdRisPartita());
		MatchInfo matchInfo = findMatchInfo(giornataInfo, attore);

		card.setTitle(new Div(giornataInfo.getDescGiornataFc()));
		card.setSubtitle(new Div(Utils.buildInfoGiornataRight(giornataInfo)));

		Span badge = new Span(badgeInfo.text());
		badge.getElement().getThemeList().add(badgeInfo.theme());
		card.setHeaderSuffix(badge);

		card.add(matchInfo.description() + " " + matchInfo.score());

		Span footer = new Span(matchInfo.totalScore());
		footer.getElement().getThemeList().add("badge contrast pill");
		card.addToFooter(footer);

		return card;
	}

	private BadgeInfo resolveBadge(Integer idRisPartita) {
		if (idRisPartita == null) {
			return new BadgeInfo("", "");
		}

		return switch (idRisPartita) {
		case 0 -> new BadgeInfo("Pareggio", "badge pill");
		case 1 -> new BadgeInfo("Vinta", "badge success");
		case 2 -> new BadgeInfo("Persa", "badge error");
		default -> new BadgeInfo("", "");
		};
	}

	private MatchInfo findMatchInfo(FcGiornataInfo giornataInfo, FcAttore attore) {
		List<FcGiornata> partite = giornataService.findByFcGiornataInfo(giornataInfo);

		for (FcGiornata partita : partite) {
			boolean isCasa = attore.getIdAttore() == partita.getFcAttoreByIdAttoreCasa().getIdAttore();
			boolean isFuori = attore.getIdAttore() == partita.getFcAttoreByIdAttoreFuori().getIdAttore();

			if (isCasa || isFuori) {
				String description = partita.getFcAttoreByIdAttoreCasa().getDescAttore() + " "
						+ partita.getFcAttoreByIdAttoreFuori().getDescAttore();

				return new MatchInfo(description, formatGolPartita(partita), formatTotaliPartita(partita));
			}
		}

		return new MatchInfo("", "-", "");
	}

	private String formatTotaliPartita(FcGiornata partita) {
		return formatScore(partita.getTotCasa()) + " - " + formatScore(partita.getTotFuori());
	}

	private String formatGolPartita(FcGiornata partita) {
		return partita.getGolCasa() != null ? partita.getGolCasa() + " - " + partita.getGolFuori() : "-";
	}

	private String formatScore(Number value) {
		DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
		double score = value == null ? 0d : value.doubleValue() / Costants.DIVISORE_100;
		return formatter.format(score);
	}

	@SuppressWarnings("unchecked")
	private <T> T getSessionAttribute(String key, Class<T> type) {
		Object value = VaadinSession.getCurrent().getAttribute(key);
		return value == null ? null : (T) value;
	}

	private record BadgeInfo(String text, String theme) {
	}

	private record MatchInfo(String description, String score, String totalScore) {
	}
}
