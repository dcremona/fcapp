package fcapp.ui.views.em;

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
@Route(value = "squadreEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmSquadreView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EmSquadreView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String SESSION_GIORNATA_INFO = "GIORNATA_INFO";

    private static final String REPORT_ROSE = "classpath:reports/roseFc.jasper";
    private static final String REPORT_STATISTICA = "classpath:reports/statistica.jasper";
    private static final String REPORT_PATH_IMG = "img/";

    private static final String LABEL_ROSA_UFFICIALE = "Rosa Ufficiale";
    private static final String LABEL_CAMBI_ROSA = "Cambi Rosa";

    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient AttoreService attoreService;
    private final transient FormazioneService formazioneService;
    private final transient MercatoService mercatoService;
    private final transient AccessoService accessoService;

    private List<FcAttore> squadre = new ArrayList<>();

    public EmSquadreView(
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            AttoreService attoreService,
            FormazioneService formazioneService,
            MercatoService mercatoService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", EmSquadreView.class.getSimpleName());
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.attoreService = attoreService;
        this.formazioneService = formazioneService;
        this.mercatoService = mercatoService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", EmSquadreView.class.getSimpleName());

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initData();
        configureLayout();
        buildView();
    }

    private void initData() {
        squadre = attoreService.findByActive(true);
    }

    private void configureLayout() {
        setMargin(true);
        setSpacing(true);
        setPadding(false);
        setSizeFull();
    }

    private void buildView() {
        FcCampionato campionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);
        FcGiornataInfo giornataInfo =
                (FcGiornataInfo) VaadinSession.getCurrent().getAttribute(SESSION_GIORNATA_INFO);

        try (Connection connection = getConnection()) {
            TabSheet tabSheet = new TabSheet();

            for (FcAttore attore : squadre) {
                tabSheet.add(attore.getDescAttore(), buildAttoreTab(connection, campionato, giornataInfo, attore));
            }

            add(tabSheet);
        } catch (SQLException e) {
            LOG.error("Error creating database connection", e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (jdbcTemplate.getDataSource() == null) {
            throw new SQLException("Datasource non disponibile");
        }
        return jdbcTemplate.getDataSource().getConnection();
    }

    private VerticalLayout buildAttoreTab(
            Connection connection,
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            FcAttore attore) {

        HorizontalLayout buttonsLayout = buildButtonsLayout(connection, campionato, giornataInfo, attore);

        List<FcFormazione> formazione =
                formazioneService.findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(attore);

        int totalePagato = calculateTotalePagato(formazione);

        List<FcMercatoDett> mercato =
                loadMercatoDettagli(campionato, attore);

        Grid<FcFormazione> formazioneGrid = buildFormazioneGrid(formazione, totalePagato);
        Grid<FcMercatoDett> mercatoGrid = buildMercatoGrid(mercato);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(buttonsLayout, formazioneGrid, mercatoGrid);

        return layout;
    }

    private HorizontalLayout buildButtonsLayout(
            Connection connection,
            FcCampionato campionato,
            FcGiornataInfo giornataInfo,
            FcAttore attore) {

        HorizontalLayout layout = new HorizontalLayout();

        FileDownloadWrapper rosaButton = buildButtonRosa(connection, campionato, attore);
        if (rosaButton != null) {
            layout.add(rosaButton);
        }

        FileDownloadWrapper votiRosaButton = buildButtonVotiRosa(connection, campionato, attore, giornataInfo);
        if (votiRosaButton != null) {
            layout.add(votiRosaButton);
        }

        return layout;
    }

    private int calculateTotalePagato(List<FcFormazione> formazione) {
        double somma = 0d;

        for (FcFormazione item : formazione) {
            if (item.getTotPagato() != null) {
                somma += item.getTotPagato();
            }
        }

        return (int) somma;
    }

    private List<FcMercatoDett> loadMercatoDettagli(FcCampionato campionato, FcAttore attore) {
        FcGiornataInfo start = new FcGiornataInfo();
        start.setCodiceGiornata(campionato.getStart());

        FcGiornataInfo end = new FcGiornataInfo();
        end.setCodiceGiornata(campionato.getEnd());

        return mercatoService
                .findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
                        start, end, attore);
    }

    private FileDownloadWrapper buildButtonRosa(
            Connection connection,
            FcCampionato campionato,
            FcAttore attore) {
        try {
            String idAttore = String.valueOf(attore.getIdAttore());
            String descAttore = attore.getDescAttore();

            Button downloadButton = new Button("Rosa pdf");
            downloadButton.setIcon(VaadinIcon.DOWNLOAD.create());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
            parameters.put("ATTORE", idAttore);
            parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_10));
            parameters.put("PATH_IMG", REPORT_PATH_IMG);

            Resource resource = resourceLoader.getResource(REPORT_ROSE);
            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                    Utils.getStreamResource(
                            "Rosa_" + descAttore + ".pdf",
                            connection,
                            parameters,
                            resource.getInputStream()));

            wrapper.wrapComponent(downloadButton);
            return wrapper;
        } catch (Exception e) {
            LOG.error("Error building rosa pdf button for {}", attore != null ? attore.getDescAttore() : null, e);
            return null;
        }
    }

    private FileDownloadWrapper buildButtonVotiRosa(
            Connection connection,
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {
        try {
            String idAttore = String.valueOf(attore.getIdAttore());
            String descAttore = attore.getDescAttore();
            String start = String.valueOf(campionato.getStart());
            String currentGiornata = giornataInfo != null
                    ? String.valueOf(giornataInfo.getCodiceGiornata())
                    : start;

            LOG.info("START {}", start);
            LOG.info("END {}", currentGiornata);
            LOG.info("ID_ATTORE {}", idAttore);

            Button downloadButton = new Button("Voti Rosa pdf");
            downloadButton.setIcon(VaadinIcon.DOWNLOAD.create());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_CAMPIONATO", String.valueOf(campionato.getIdCampionato()));
            parameters.put("START", start);
            parameters.put("END", currentGiornata);
            parameters.put("ID_ATTORE", idAttore);
            parameters.put("DIVISORE", String.valueOf(Costants.DIVISORE_10));

            Resource resource = resourceLoader.getResource(REPORT_STATISTICA);
            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                    Utils.getStreamResource(
                            "Voti_Rosa_" + descAttore + ".pdf",
                            connection,
                            parameters,
                            resource.getInputStream()));

            wrapper.wrapComponent(downloadButton);
            return wrapper;
        } catch (Exception e) {
            LOG.error("Error building voti rosa pdf button for {}", attore != null ? attore.getDescAttore() : null, e);
            return null;
        }
    }

    private Grid<FcFormazione> buildFormazioneGrid(List<FcFormazione> items, Integer somma) {
        Grid<FcFormazione> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        Column<FcFormazione> ruoloColumn = grid.addColumn(new ComponentRenderer<>(formazione -> {
            HorizontalLayout cellLayout = createCompactHorizontalLayout();
            if (formazione != null
                    && formazione.getFcGiocatore() != null
                    && formazione.getFcGiocatore().getFcRuolo() != null
                    && !StringUtils.isEmpty(formazione.getFcGiocatore().getFcRuolo().getIdRuolo())) {
                String ruolo = formazione.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase();
                Image image = Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png"));
                cellLayout.add(image);
            }
            return cellLayout;
        }));
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader("R");
        ruoloColumn.setAutoWidth(true);

        Column<FcFormazione> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(formazione -> {
            HorizontalLayout cellLayout = createCompactHorizontalLayout();
            if (formazione != null && formazione.getFcGiocatore() != null) {
                cellLayout.add(new Span(formazione.getFcGiocatore().getCognGiocatore()));
            }
            return cellLayout;
        }));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setHeader(Costants.GIOCATORE);
        giocatoreColumn.setAutoWidth(true);

        Column<FcFormazione> squadraColumn = grid.addColumn(new ComponentRenderer<>(formazione -> {
            HorizontalLayout cellLayout = createCompactHorizontalLayout();

            if (formazione != null
                    && formazione.getFcGiocatore() != null
                    && formazione.getFcGiocatore().getFcSquadra() != null) {

                FcSquadra squadra = formazione.getFcGiocatore().getFcSquadra();

                if (squadra.getImg() != null) {
                    try {
                        Image image = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                        cellLayout.add(image);
                    } catch (SQLException e) {
                        LOG.error("Error loading team image for {}", squadra.getNomeSquadra(), e);
                    }
                }

                cellLayout.add(new Span(squadra.getNomeSquadra()));
            }

            return cellLayout;
        }));
        squadraColumn.setSortable(true);
        squadraColumn.setComparator(Comparator.comparing(item ->
                item.getFcGiocatore().getFcSquadra().getNomeSquadra()));
        squadraColumn.setHeader(Costants.SQUADRA);
        squadraColumn.setAutoWidth(true);

        Column<FcFormazione> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(formazione -> {
            HorizontalLayout cellLayout = new HorizontalLayout();

            if (formazione != null && formazione.getFcGiocatore() != null) {
                FcStatistiche statistiche = formazione.getFcGiocatore().getFcStatistiche();
                cellLayout.add(buildTrendImage(statistiche != null ? statistiche.getMediaVoto() : 0));
                cellLayout.add(new Span(formatStatValue(statistiche != null ? statistiche.getMediaVoto() : 0)));
            }

            return cellLayout;
        }));
        mediaVotoColumn.setSortable(true);
        mediaVotoColumn.setComparator(Comparator.comparing(item ->
                item.getFcGiocatore().getFcStatistiche().getMediaVoto()));
        mediaVotoColumn.setHeader("Mv");
        mediaVotoColumn.setAutoWidth(true);
        mediaVotoColumn.setKey("fcStatistiche.mediaVoto");

        Column<FcFormazione> fantaMediaColumn = grid.addColumn(new ComponentRenderer<>(formazione -> {
            HorizontalLayout cellLayout = new HorizontalLayout();

            if (formazione != null && formazione.getFcGiocatore() != null) {
                FcStatistiche statistiche = formazione.getFcGiocatore().getFcStatistiche();
                cellLayout.add(buildTrendImage(statistiche != null ? statistiche.getFantaMedia() : 0));
                cellLayout.add(new Span(formatStatValue(statistiche != null ? statistiche.getFantaMedia() : 0)));
            }

            return cellLayout;
        }));
        fantaMediaColumn.setSortable(true);
        fantaMediaColumn.setComparator(Comparator.comparing(item ->
                item.getFcGiocatore().getFcStatistiche().getFantaMedia()));
        fantaMediaColumn.setHeader("FMv");
        fantaMediaColumn.setAutoWidth(true);
        fantaMediaColumn.setKey("fcStatistiche.fantaMedia");

        Column<FcFormazione> quotazioneColumn = grid.addColumn(item ->
                item.getFcGiocatore() != null ? item.getFcGiocatore().getQuotazione() : 0);
        quotazioneColumn.setSortable(true);
        quotazioneColumn.setHeader("Q");
        quotazioneColumn.setAutoWidth(true);

        Column<FcFormazione> totPagatoColumn = grid.addColumn(item ->
                item.getFcGiocatore() != null ? item.getTotPagato() : 0);
        totPagatoColumn.setSortable(true);
        totPagatoColumn.setHeader("P");
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
        informationCell.setComponent(buildSectionTitle(LABEL_ROSA_UFFICIALE));

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(quotazioneColumn).setComponent(buildFooterCell("Totale"));
        footerRow.getCell(totPagatoColumn).setComponent(buildFooterCell(String.valueOf(somma)));

        return grid;
    }

    private Grid<FcMercatoDett> buildMercatoGrid(List<FcMercatoDett> items) {
        Grid<FcMercatoDett> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        Column<FcMercatoDett> giornataColumn = grid.addColumn(item ->
                item.getFcGiornataInfo().getCodiceGiornata());
        giornataColumn.setSortable(false);
        giornataColumn.setHeader("Giornata");
        giornataColumn.setAutoWidth(true);

        Column<FcMercatoDett> dataCambioColumn = grid.addColumn(
                new LocalDateTimeRenderer<>(
                        FcMercatoDett::getDataCambio,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataCambioColumn.setSortable(false);
        dataCambioColumn.setHeader("Data");
        dataCambioColumn.setAutoWidth(true);

        Column<FcMercatoDett> ruoloAcqColumn = grid.addColumn(new ComponentRenderer<>(item -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            if (item != null && item.getFcGiocatoreByIdGiocAcq() != null) {
                String ruolo = item.getFcGiocatoreByIdGiocAcq().getFcRuolo().getIdRuolo().toLowerCase();
                Image image = Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png"));
                cellLayout.add(image);
            }
            return cellLayout;
        }));
        ruoloAcqColumn.setHeader("");
        ruoloAcqColumn.setAutoWidth(true);

        Column<FcMercatoDett> acquistiColumn = grid.addColumn(new ComponentRenderer<>(item ->
                buildMercatoGiocatoreCell(item != null ? item.getFcGiocatoreByIdGiocAcq() : null)));
        acquistiColumn.setSortable(false);
        acquistiColumn.setHeader("Acquisti");
        acquistiColumn.setAutoWidth(true);

        Column<FcMercatoDett> ruoloVenColumn = grid.addColumn(new ComponentRenderer<>(item -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            if (item != null && item.getFcGiocatoreByIdGiocVen() != null) {
                String ruolo = item.getFcGiocatoreByIdGiocVen().getFcRuolo().getIdRuolo().toLowerCase();
                Image image = Utils.buildImage(
                        ruolo + ".png",
                        resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo + ".png"));
                cellLayout.add(image);
            }
            return cellLayout;
        }));
        ruoloVenColumn.setHeader("");
        ruoloVenColumn.setAutoWidth(true);

        Column<FcMercatoDett> cessioniColumn = grid.addColumn(new ComponentRenderer<>(item ->
                buildMercatoGiocatoreCell(item != null ? item.getFcGiocatoreByIdGiocVen() : null)));
        cessioniColumn.setSortable(false);
        cessioniColumn.setHeader("Cessioni");
        cessioniColumn.setAutoWidth(true);

        Column<FcMercatoDett> notaColumn = grid.addColumn(FcMercatoDett::getNota);
        notaColumn.setSortable(false);
        notaColumn.setHeader("Nota");
        notaColumn.setAutoWidth(true);

        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell informationCell = topRow.join(
                giornataColumn,
                dataCambioColumn,
                ruoloAcqColumn,
                acquistiColumn,
                ruoloVenColumn,
                cessioniColumn,
                notaColumn);
        informationCell.setComponent(buildSectionTitle(LABEL_CAMBI_ROSA));

        return grid;
    }

    private FlexLayout buildMercatoGiocatoreCell(FcGiocatore giocatore) {
        FlexLayout cellLayout = new FlexLayout();

        if (giocatore == null) {
            return cellLayout;
        }

        if (giocatore.getImgSmall() != null) {
            try {
                Image image = Utils.getImage(giocatore.getNomeImg(), giocatore.getImgSmall().getBinaryStream());
                cellLayout.add(image);
            } catch (SQLException e) {
                LOG.error("Error loading small image for player {}", giocatore.getCognGiocatore(), e);
            }
        }

        cellLayout.add(new Span(giocatore.getCognGiocatore()));

        if (giocatore.getFcSquadra() != null && giocatore.getFcSquadra().getNomeSquadra() != null) {
            String nomeSquadra = giocatore.getFcSquadra().getNomeSquadra();
            String suffix = nomeSquadra.length() >= 3 ? nomeSquadra.substring(0, 3) : nomeSquadra;
            Span squadra = new Span(" (" + suffix + ")");
            squadra.getStyle().set(Costants.FONT_SIZE, "10px");
            cellLayout.add(squadra);
        }

        return cellLayout;
    }

    private HorizontalLayout createCompactHorizontalLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(Alignment.STRETCH);
        layout.setSizeFull();
        return layout;
    }

    private Image buildTrendImage(double value) {
        String imageName = "2.png";

        if (value != 0) {
            if (value > Costants.EM_RANGE_MAX_MV) {
                imageName = "1.png";
            } else if (value < Costants.EM_RANGE_MIN_MV) {
                imageName = "3.png";
            }
        }

        return Utils.buildImage(
                imageName,
                resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));
    }

    private String formatStatValue(double value) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(value / Costants.DIVISORE_10);
    }

    private Div buildSectionTitle(String text) {
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
}
