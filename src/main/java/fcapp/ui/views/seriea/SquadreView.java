package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

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

    private static final String REPORT_ROSE = "classpath:reports/roseFc.jasper";
    private static final String REPORT_STATISTICA = "classpath:reports/statistica.jasper";
    private static final String DECIMAL_PATTERN = "#0.00";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient AttoreService attoreService;
    private final transient FormazioneService formazioneService;
    private final transient MercatoService mercatoService;
    private final transient AccessoService accessoService;

    private List<FcAttore> squadre = new ArrayList<>();

    public SquadreView(
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            AttoreService attoreService,
            FormazioneService formazioneService,
            MercatoService mercatoService,
            AccessoService accessoService) {

        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.attoreService = attoreService;
        this.formazioneService = formazioneService;
        this.mercatoService = mercatoService;
        this.accessoService = accessoService;

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

    private void initData() {
        squadre = attoreService.findByActive(true);
    }

    private void initLayout() {
        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        FcGiornataInfo giornataInfo = getSessionAttribute("GIORNATA_INFO", FcGiornataInfo.class);

        if (campionato == null || giornataInfo == null) {
            return;
        }

        try (Connection connection = getConnection()) {
            TabSheet tabSheet = new TabSheet();

            for (FcAttore attore : squadre) {
                if (!isValidAttore(attore)) {
                    continue;
                }

                tabSheet.add(attore.getDescAttore(), buildAttoreTab(connection, campionato, giornataInfo, attore));
            }

            tabSheet.setSizeFull();
            add(tabSheet);

        } catch (SQLException e) {
            log.error("Errore durante la creazione della vista squadre", e);
        }
    }

    private VerticalLayout buildAttoreTab(
            Connection connection,
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            FcAttore attore) {

        HorizontalLayout buttonsLayout = new HorizontalLayout();

        FileDownloadWrapper rosaButton = buildButtonRosa(connection, campionato, attore);
        if (rosaButton != null) {
            buttonsLayout.add(rosaButton);
        }

        FileDownloadWrapper votiRosaButton = buildButtonVotiRosa(connection, campionato, attore, giornataInfo);
        if (votiRosaButton != null) {
            buttonsLayout.add(votiRosaButton);
        }

        List<FcFormazione> rosa = formazioneService
                .findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, true);

        int totalePagato = calculateTotalePagato(rosa);

        List<FcMercatoDett> movimentiMercato = findMercatoDettagli(campionato, attore);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(buttonsLayout);
        layout.add(getTableFormazione(rosa, totalePagato));
        layout.add(getTableMercato(movimentiMercato));

        return layout;
    }

    private Connection getConnection() throws SQLException {
        if (jdbcTemplate.getDataSource() == null) {
            throw new SQLException("DataSource non disponibile");
        }
        return jdbcTemplate.getDataSource().getConnection();
    }

    private boolean isValidAttore(FcAttore attore) {
        return attore != null && attore.getIdAttore() > 0 && attore.getIdAttore() < 9;
    }

    private int calculateTotalePagato(List<FcFormazione> formazioni) {
        double somma = 0d;
        for (FcFormazione formazione : formazioni) {
            if (formazione.getTotPagato() != null) {
                somma += formazione.getTotPagato();
            }
        }
        return (int) somma;
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

    private FileDownloadWrapper buildButtonRosa(Connection connection, FcCampionato campionato, FcAttore attore) {
        try {
            Button button = new Button("Rosa pdf");
            button.setIcon(VaadinIcon.DOWNLOAD.create());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
            parameters.put("ATTORE", String.valueOf(attore.getIdAttore()));
            parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));
            parameters.put("PATH_IMG", "img/");

            Resource resource = resourceLoader.getResource(REPORT_ROSE);

            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                    Utils.getStreamResource(
                            "Rosa_" + attore.getDescAttore() + ".pdf",
                            connection,
                            parameters,
                            resource.getInputStream()));

            wrapper.wrapComponent(button);
            return wrapper;

        } catch (Exception e) {
            log.error("Errore nella creazione del pdf rosa per {}", attore.getDescAttore(), e);
            return null;
        }
    }

    private FileDownloadWrapper buildButtonVotiRosa(
            Connection connection,
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {

        try {
            Button button = new Button("Voti Rosa pdf");
            button.setIcon(VaadinIcon.DOWNLOAD.create());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
            parameters.put("START", String.valueOf(campionato.getStart()));
            parameters.put("END", String.valueOf(giornataInfo.getCodiceGiornata()));
            parameters.put("ID_ATTORE", String.valueOf(attore.getIdAttore()));
            parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_100));

            Resource resource = resourceLoader.getResource(REPORT_STATISTICA);

            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                    Utils.getStreamResource(
                            "Voti_Rosa_" + attore.getDescAttore() + ".pdf",
                            connection,
                            parameters,
                            resource.getInputStream()));

            wrapper.wrapComponent(button);
            return wrapper;

        } catch (Exception e) {
            log.error("Errore nella creazione del pdf voti rosa per {}", attore.getDescAttore(), e);
            return null;
        }
    }

    private Grid<FcFormazione> getTableFormazione(List<FcFormazione> items, Integer totalePagato) {
        Grid<FcFormazione> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        Column<FcFormazione> ruoloColumn = grid.addColumn(new ComponentRenderer<>(this::buildRuoloComponent));
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader("R");
        ruoloColumn.setAutoWidth(true);

        Column<FcFormazione> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildGiocatoreComponent));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setHeader(Costants.GIOCATORE);
        giocatoreColumn.setAutoWidth(true);

        Column<FcFormazione> squadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildSquadraComponent));
        squadraColumn.setSortable(true);
        squadraColumn.setComparator(Comparator.comparing(this::getNomeSquadraSafe));
        squadraColumn.setHeader(Costants.SQUADRA);
        squadraColumn.setAutoWidth(true);

        Column<FcFormazione> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(f ->
                buildStatisticaComponent(f, true)));
        mediaVotoColumn.setSortable(true);
        mediaVotoColumn.setComparator(Comparator.comparing(this::getMediaVotoSafe));
        mediaVotoColumn.setHeader(Costants.MV);
        mediaVotoColumn.setAutoWidth(true);
        mediaVotoColumn.setKey("fcStatistiche.mediaVoto");

        Column<FcFormazione> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(f ->
                buildStatisticaComponent(f, false)));
        fantaMediaColumn.setSortable(true);
        fantaMediaColumn.setComparator(Comparator.comparing(this::getFantaMediaSafe));
        fantaMediaColumn.setHeader(Costants.FMV);
        fantaMediaColumn.setAutoWidth(true);
        fantaMediaColumn.setKey("fcStatistiche.fantaMedia");

        Column<FcFormazione> quotazioneColumn = grid.addColumn(f ->
                f.getFcGiocatore() != null ? f.getFcGiocatore().getQuotazione() : 0);
        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader(Costants.Q);
        quotazioneColumn.setAutoWidth(true);

        Column<FcFormazione> totPagatoColumn = grid.addColumn(f ->
                f.getFcGiocatore() != null ? f.getTotPagato() : 0);
        totPagatoColumn.setSortable(true);
        totPagatoColumn.setHeader(Costants.P);
        totPagatoColumn.setAutoWidth(true);

        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell informationCell = topRow.join(
                ruoloColumn,
                giocatoreColumn,
                squadraColumn,
                mediaVotoColumn,
                fantaMediaColumn,
                quotazioneColumn,
                totPagatoColumn);
        informationCell.setComponent(buildSectionHeader("Rosa Ufficiale"));

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(quotazioneColumn).setComponent(buildFooterCell(Costants.TOTALE));
        footerRow.getCell(totPagatoColumn).setComponent(buildFooterCell(String.valueOf(totalePagato)));

        return grid;
    }

    private Grid<FcMercatoDett> getTableMercato(List<FcMercatoDett> items) {
        Grid<FcMercatoDett> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        Column<FcMercatoDett> giornataColumn = grid.addColumn(m -> m.getFcGiornataInfo().getCodiceGiornata());
        giornataColumn.setSortable(false);
        giornataColumn.setHeader(Costants.GIORNATA);
        giornataColumn.setAutoWidth(true);

        Column<FcMercatoDett> dataCambioColumn = grid.addColumn(
                new LocalDateTimeRenderer<>(
                        FcMercatoDett::getDataCambio,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataCambioColumn.setSortable(false);
        dataCambioColumn.setHeader(Costants.DATA);
        dataCambioColumn.setAutoWidth(true);

        Column<FcMercatoDett> ruoloAcqColumn = grid.addColumn(new ComponentRenderer<>(m ->
                buildRuoloMercatoComponent(m.getFcGiocatoreByIdGiocAcq())));
        ruoloAcqColumn.setHeader("");
        ruoloAcqColumn.setAutoWidth(true);

        Column<FcMercatoDett> acquistoColumn = grid.addColumn(new ComponentRenderer<>(m ->
                buildMercatoGiocatoreComponent(m.getFcGiocatoreByIdGiocAcq())));
        acquistoColumn.setSortable(false);
        acquistoColumn.setHeader(Costants.ACQUISTI);
        acquistoColumn.setAutoWidth(true);

        Column<FcMercatoDett> ruoloVenColumn = grid.addColumn(new ComponentRenderer<>(m ->
                buildRuoloMercatoComponent(m.getFcGiocatoreByIdGiocVen())));
        ruoloVenColumn.setHeader("");
        ruoloVenColumn.setAutoWidth(true);

        Column<FcMercatoDett> cessioneColumn = grid.addColumn(new ComponentRenderer<>(m ->
                buildMercatoGiocatoreComponent(m.getFcGiocatoreByIdGiocVen())));
        cessioneColumn.setSortable(false);
        cessioneColumn.setHeader(Costants.CESSIONI);
        cessioneColumn.setAutoWidth(true);

        Column<FcMercatoDett> notaColumn = grid.addColumn(FcMercatoDett::getNota);
        notaColumn.setSortable(false);
        notaColumn.setHeader(Costants.NOTA);
        notaColumn.setAutoWidth(true);

        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell informationCell = topRow.join(
                giornataColumn,
                dataCambioColumn,
                ruoloAcqColumn,
                acquistoColumn,
                ruoloVenColumn,
                cessioneColumn,
                notaColumn);
        informationCell.setComponent(buildSectionHeader("Cambi Rosa"));

        return grid;
    }

    private Component buildRuoloComponent(FcFormazione formazione) {
        HorizontalLayout layout = new HorizontalLayout();

        String ruolo = getRuoloSafe(formazione);
        if (StringUtils.isNotBlank(ruolo)) {
            layout.add(buildRoleImage(ruolo));
        }

        return layout;
    }

    private Component buildGiocatoreComponent(FcFormazione formazione) {
        HorizontalLayout layout = buildCompactHorizontalLayout();

        FcGiocatore giocatore = formazione != null ? formazione.getFcGiocatore() : null;
        if (giocatore == null) {
            return layout;
        }

        if (StringUtils.isNotBlank(giocatore.getNomeImg()) && giocatore.getImgSmall() != null) {
            try {
                layout.add(Utils.getImage(giocatore.getNomeImg(), giocatore.getImgSmall().getBinaryStream()));
            } catch (SQLException e) {
                log.error("Errore caricamento immagine giocatore {}", giocatore.getCognGiocatore(), e);
            }
        }

        layout.add(new Span(giocatore.getCognGiocatore()));
        return layout;
    }

    private Component buildSquadraComponent(FcFormazione formazione) {
        HorizontalLayout layout = buildCompactHorizontalLayout();

        FcGiocatore giocatore = formazione != null ? formazione.getFcGiocatore() : null;
        FcSquadra squadra = giocatore != null ? giocatore.getFcSquadra() : null;
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
        HorizontalLayout layout = new HorizontalLayout();

        FcGiocatore giocatore = formazione != null ? formazione.getFcGiocatore() : null;
        FcStatistiche statistiche = giocatore != null ? giocatore.getFcStatistiche() : null;

        double valore = 0;
        if (statistiche != null) {
            valore = mediaVoto ? statistiche.getMediaVoto() : statistiche.getFantaMedia();
        }

        layout.add(buildTrendImage(valore));
        layout.add(new Span(formatStatisticValue(valore)));

        return layout;
    }

    private Component buildRuoloMercatoComponent(FcGiocatore giocatore) {
        HorizontalLayout layout = new HorizontalLayout();
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

        if (giocatore.getImgSmall() != null) {
            try {
                layout.add(Utils.getImage(giocatore.getNomeImg(), giocatore.getImgSmall().getBinaryStream()));
            } catch (SQLException e) {
                log.error("Errore caricamento immagine giocatore {}", giocatore.getCognGiocatore(), e);
            }
        }

        layout.add(new Span(giocatore.getCognGiocatore()));

        String nomeSquadra = giocatore.getFcSquadra() != null ? giocatore.getFcSquadra().getNomeSquadra() : "";
        if (StringUtils.isNotBlank(nomeSquadra)) {
            Span lblSquadra = new Span(" (" + nomeSquadra.substring(0, Math.min(3, nomeSquadra.length())) + ")");
            lblSquadra.getStyle().set(Costants.FONT_SIZE, "10px");
            layout.add(lblSquadra);
        }

        return layout;
    }

    private HorizontalLayout buildCompactHorizontalLayout() {
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
        if (value == 0) {
            return "2.png";
        }
        if (value > Costants.RANGE_MAX_MV) {
            return "1.png";
        }
        if (value < Costants.RANGE_MIN_MV) {
            return "3.png";
        }
        return "2.png";
    }

    private String formatStatisticValue(double value) {
        DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
        return formatter.format(value / Costants.DIVISORE_100);
    }

    private String getRuoloSafe(FcFormazione formazione) {
        if (formazione == null || formazione.getFcGiocatore() == null || formazione.getFcGiocatore().getFcRuolo() == null) {
            return null;
        }
        return formazione.getFcGiocatore().getFcRuolo().getIdRuolo();
    }

    private String getNomeSquadraSafe(FcFormazione formazione) {
        if (formazione == null || formazione.getFcGiocatore() == null || formazione.getFcGiocatore().getFcSquadra() == null) {
            return "";
        }
        return formazione.getFcGiocatore().getFcSquadra().getNomeSquadra();
    }

    private Double getMediaVotoSafe(FcFormazione formazione) {
        FcStatistiche statistiche = getStatisticheSafe(formazione);
        return statistiche != null ? statistiche.getMediaVoto() : 0;
    }

    private Double getFantaMediaSafe(FcFormazione formazione) {
        FcStatistiche statistiche = getStatisticheSafe(formazione);
        return statistiche != null ? statistiche.getFantaMedia() : 0;
    }

    private FcStatistiche getStatisticheSafe(FcFormazione formazione) {
        if (formazione == null || formazione.getFcGiocatore() == null) {
            return null;
        }
        return formazione.getFcGiocatore().getFcStatistiche();
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
