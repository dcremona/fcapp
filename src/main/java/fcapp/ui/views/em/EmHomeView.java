package fcapp.ui.views.em;

import java.io.Serial;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.flowingcode.vaadin.addons.relativetime.Format;
import com.flowingcode.vaadin.addons.relativetime.RelativeTime;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "homeEm", layout = MainLayout.class)
@PageTitle("Home")
@RolesAllowed("USER")
public class EmHomeView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String ATTR_GIORNATA_INFO = "GIORNATA_INFO";
    private static final String ATTR_CAMPIONATO = "CAMPIONATO";
    private static final String ATTR_NEXT_DATE = "NEXTDATE";
    private static final String ATTR_MILLIS_DIFF = "MILLISDIFF";
    private static final String ATTR_FUTURE = "FUTURE";
    private static final int TEAM_LABEL_LENGTH = 3;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient Environment env;
    private final transient ResourceLoader resourceLoader;
    private final transient GiornataInfoService giornataInfoService;
    private final transient CalendarioCompetizioneService calendarioCompetizioneService;
    private final transient AccessoService accessoService;
    private final transient SquadraService squadraService;

    public EmHomeView(
            Environment env,
            ResourceLoader resourceLoader,
            GiornataInfoService giornataInfoService,
            CalendarioCompetizioneService calendarioCompetizioneService,
            AccessoService accessoService,
            SquadraService squadraService) {
        this.env = env;
        this.resourceLoader = resourceLoader;
        this.giornataInfoService = giornataInfoService;
        this.calendarioCompetizioneService = calendarioCompetizioneService;
        this.accessoService = accessoService;
        this.squadraService = squadraService;
    }

    @PostConstruct
    void init() {
        log.info("Initializing {}", getClass().getSimpleName());

        try {
            if (!Utils.isValidVaadinSession()) {
                log.warn("Invalid Vaadin session");
                return;
            }

            accessoService.insertAccesso(getClass().getName());
            configureLayout();
            addLogo();
            add(buildLayoutAvviso());
            add(buildGiornateTabSheet());

        } catch (Exception e) {
            log.error("Error during view initialization", e);
        }
    }

    private void configureLayout() {
        setSpacing(true);
        setPadding(true);
    }

    private void addLogo() {
        String logoName = env.getProperty("img.logo");
        if (logoName == null || logoName.isBlank()) {
            log.warn("Logo property 'img.logo' not configured");
            return;
        }

        Image logo = Utils.buildImage(
                logoName,
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + logoName));

        add(logo);
        setHorizontalComponentAlignment(Alignment.CENTER, logo);
    }

    private TabSheet buildGiornateTabSheet() {
        FcGiornataInfo currentGiornata = getSessionAttribute(ATTR_GIORNATA_INFO, FcGiornataInfo.class);
        FcCampionato campionato = getSessionAttribute(ATTR_CAMPIONATO, FcCampionato.class);

        TabSheet tabSheet = new TabSheet();

        if (currentGiornata == null || campionato == null) {
            log.warn("Missing session data for giornate tab creation");
            return tabSheet;
        }

        List<FcGiornataInfo> giornate = giornataInfoService
                .findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(
                        campionato.getStart(),
                        campionato.getEnd());

        for (FcGiornataInfo giornata : giornate) {
            Tab tab = tabSheet.add(giornata.getDescGiornata(), buildGiornataContent(giornata));

            if (Objects.equals(giornata.getDescGiornata(), currentGiornata.getDescGiornata())) {
                log.info("Selected tab {}", currentGiornata.getDescGiornata());
                tabSheet.setSelectedTab(tab);
            }
        }

        return tabSheet;
    }

    private VerticalLayout buildGiornataContent(FcGiornataInfo giornata) {
        List<FcCalendarioCompetizione> partite =
                calendarioCompetizioneService.findByIdGiornataOrderByDataAsc(giornata.getCodiceGiornata());

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(buildPartiteGrid(partite));

        return layout;
    }

    private Grid<FcCalendarioCompetizione> buildPartiteGrid(List<FcCalendarioCompetizione> partite) {
        Grid<FcCalendarioCompetizione> grid = new Grid<>();
        grid.setItems(partite);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);

        addDataColumn(grid);
        addSquadraColumn(grid, true);
        addSquadraColumn(grid, false);
        addRisultatoColumn(grid);

        return grid;
    }

    private void addDataColumn(Grid<FcCalendarioCompetizione> grid) {
        Column<FcCalendarioCompetizione> column = grid.addColumn(
                new LocalDateTimeRenderer<>(
                        FcCalendarioCompetizione::getData,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));

        column.setSortable(false);
        column.setAutoWidth(true);
    }

    private void addSquadraColumn(Grid<FcCalendarioCompetizione> grid, boolean isCasa) {
        Column<FcCalendarioCompetizione> column = grid.addColumn(
                new ComponentRenderer<>(match -> buildSquadraCell(match, isCasa)));

        column.setSortable(false);
        column.setAutoWidth(true);
    }

    private HorizontalLayout buildSquadraCell(FcCalendarioCompetizione match, boolean isCasa) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setSpacing(true);
        cellLayout.setPadding(false);
        cellLayout.setMargin(false);

        if (match == null) {
            return cellLayout;
        }

        String nomeSquadra = isCasa ? match.getSquadraCasa() : match.getSquadraFuori();
        if (nomeSquadra == null || nomeSquadra.isBlank()) {
            return cellLayout;
        }

        addSquadraImage(cellLayout, nomeSquadra);
        cellLayout.add(new Span(abbreviateTeamName(nomeSquadra)));

        return cellLayout;
    }

    private void addSquadraImage(HorizontalLayout layout, String nomeSquadra) {
        FcSquadra squadra = squadraService.findByNomeSquadra(nomeSquadra);
        if (squadra == null || squadra.getImg() == null) {
            return;
        }

        try {
            Image img = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
            layout.add(img);
        } catch (SQLException e) {
            log.error("Error loading image for squadra {}", nomeSquadra, e);
        }
    }

    private void addRisultatoColumn(Grid<FcCalendarioCompetizione> grid) {
        Column<FcCalendarioCompetizione> column = grid.addColumn(
                match -> match != null && match.getRisultato() != null ? match.getRisultato() : "");

        column.setSortable(false);
        column.setAutoWidth(true);
    }

    private VerticalLayout buildLayoutAvviso() {
        FcCampionato campionato = getSessionAttribute(ATTR_CAMPIONATO, FcCampionato.class);
        FcGiornataInfo giornataInfo = getSessionAttribute(ATTR_GIORNATA_INFO, FcGiornataInfo.class);
        String nextDate = getSessionAttribute(ATTR_NEXT_DATE, String.class);
        Long millisDiff = getSessionAttribute(ATTR_MILLIS_DIFF, Long.class);
        LocalDateTime dateTime = getSessionAttribute(ATTR_FUTURE, LocalDateTime.class);
        
        VerticalLayout layoutAvviso = new VerticalLayout();
        layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);

        if (campionato == null || giornataInfo == null || nextDate == null || millisDiff == null) {
            log.warn("Missing session data for warning layout");
            return layoutAvviso;
        }

        log.info("millisDiff {}", millisDiff);

        layoutAvviso.add(new HorizontalLayout(
                new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato))));

        layoutAvviso.add(new HorizontalLayout(
                new Span("Consegna Formazione entro: " + nextDate)));

		Instant future = dateTime.atZone(ZoneId.of("UTC")).toInstant();
		layoutAvviso.add(new RelativeTime(future).setFormat(Format.DURATION));


        return layoutAvviso;
    }


    private String abbreviateTeamName(String teamName) {
        return teamName.length() <= TEAM_LABEL_LENGTH
                ? teamName
                : teamName.substring(0, TEAM_LABEL_LENGTH);
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String attributeName, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(attributeName);
        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            log.warn("Session attribute {} is not of type {}", attributeName, type.getSimpleName());
            return null;
        }

        return (T) value;
    }
}
