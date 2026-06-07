package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.job.JobProcessFileCsv;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiornataGiocatoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "SqualificatiIndisponibili", layout = MainLayout.class)
@RolesAllowed("USER")
@PageTitle("Squalificati-Indisponibili")
public class SqualificatiIndisponibiliView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SESSION_ATTORE = "ATTORE";
    private static final String SESSION_GIORNATA_INFO = "GIORNATA_INFO";
    private static final String SESSION_PROPERTIES = "PROPERTIES";

    private static final String FILE_SQUALIFICATI = "SQUALIFICATI_";
    private static final String FILE_INFORTUNATI = "INFORTUNATI_";
    private static final String FILE_PROBABILI = "PROBABILI_";
    private static final String FILE_SQUALIFICATI_INFORTUNATI_FG = "SQUALIFICATI_INFORTUNATI_FANTA_GAZZETTA_";
    private static final String FILE_PROBABILI_FG = "PROBABILI_FANTA_GAZZETTA_";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient JobProcessGiornata jobProcessGiornata;
    private final transient ResourceLoader resourceLoader;
    private final transient Environment env;
    private final transient AccessoService accessoService;
    private final transient GiornataGiocatoreService giornataGiocatoreService;

    private Button salvaDb;
    private Grid<FcGiornataGiocatore> tableSqualificati;
    private Grid<FcGiornataGiocatore> tableInfortunati;

    public SqualificatiIndisponibiliView(
            JobProcessGiornata jobProcessGiornata,
            ResourceLoader resourceLoader,
            Environment env,
            AccessoService accessoService,
            GiornataGiocatoreService giornataGiocatoreService) {

        this.jobProcessGiornata = jobProcessGiornata;
        this.resourceLoader = resourceLoader;
        this.env = env;
        this.accessoService = accessoService;
        this.giornataGiocatoreService = giornataGiocatoreService;

        log.info("SqualificatiIndisponibiliView()");
    }

    @PostConstruct
    void init() {
        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initLayout();
    }

    private void initLayout() {
        FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);
        FcGiornataInfo giornataInfo = getSessionAttribute(SESSION_GIORNATA_INFO, FcGiornataInfo.class);

        if (attore == null || giornataInfo == null) {
            return;
        }

        salvaDb = new Button("Salva " + giornataInfo.getDescGiornata());
        salvaDb.setIcon(VaadinIcon.DATABASE.create());
        salvaDb.addClickListener(this);
        salvaDb.setVisible(isAdmin(attore));
        add(salvaDb);

        tableSqualificati = createGridSqualificatiInfortunati();
        tableInfortunati = createGridSqualificatiInfortunati();

        add(buildDetailsPanel("Squalificati", tableSqualificati));
        add(buildDetailsPanel("Infortunati", tableInfortunati));

        refreshTables(giornataInfo);
    }

    private boolean isAdmin(FcAttore attore) {
        for (Role role : attore.getRoles()) {
            if (role.equals(Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    private Details buildDetailsPanel(String title, Grid<FcGiornataGiocatore> grid) {
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        content.add(grid);

        Details panel = new Details(title, content);
        panel.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        panel.setOpened(true);

        return panel;
    }

    private void refreshTables(FcGiornataInfo giornataInfo) {
        try {
            List<FcGiornataGiocatore> items = giornataGiocatoreService.findByCustonm(giornataInfo, null);

            List<FcGiornataGiocatore> squalificati = new ArrayList<>();
            List<FcGiornataGiocatore> infortunati = new ArrayList<>();

            for (FcGiornataGiocatore item : items) {
                if (item.isSqualificato()) {
                    squalificati.add(item);
                } else if (item.isInfortunato()) {
                    infortunati.add(item);
                }
            }

            log.info("listaSqualificati {}", squalificati.size());
            tableSqualificati.setItems(squalificati);
            tableSqualificati.getDataProvider().refreshAll();

            log.info("listaInfortunati {}", infortunati.size());
            tableInfortunati.setItems(infortunati);
            tableInfortunati.getDataProvider().refreshAll();

        } catch (Exception e) {
            log.error("Errore refreshTables", e);
        }
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            if (event.getSource() != salvaDb) {
                return;
            }

            log.info("SALVA");

            Properties properties = getSessionAttribute(SESSION_PROPERTIES, Properties.class);
            FcGiornataInfo giornataInfo = getSessionAttribute(SESSION_GIORNATA_INFO, FcGiornataInfo.class);

            if (properties == null || giornataInfo == null) {
                return;
            }

            String basePathData = env.getProperty("PATH_TMP");
            String urlFanta = (String) properties.get("URL_FANTA");

            giornataGiocatoreService.deleteByCustonm(giornataInfo);

            JobProcessFileCsv jobCsv = new JobProcessFileCsv();
            boolean useFantaGazzetta = true;

            if (useFantaGazzetta) {
                processFantaGazzettaFiles(jobCsv, giornataInfo, basePathData);
            } else {
                processLegacyFiles(jobCsv, giornataInfo, basePathData, urlFanta);
            }

            refreshTables(giornataInfo);
            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void processLegacyFiles(
            JobProcessFileCsv jobCsv,
            FcGiornataInfo giornataInfo,
            String basePathData,
            String urlFanta) throws Exception {

        String fileName;

        String urlSqualificati = urlFanta + "giocatori-squalificati.asp";
        log.info("httpUrlSqualificati {}", urlSqualificati);
        String fileNameSqualificati = FILE_SQUALIFICATI + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvSqualificatiInfortunati(urlSqualificati, basePathData, fileNameSqualificati);
        fileName = buildCsvPath(basePathData, fileNameSqualificati);
        jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, true, false);

        String urlInfortunati = urlFanta + "giocatori-infortunati.asp";
        log.info("httpUrlInfortunati {}", urlInfortunati);
        String fileNameInfortunati = FILE_INFORTUNATI + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvSqualificatiInfortunati(urlInfortunati, basePathData, fileNameInfortunati);
        fileName = buildCsvPath(basePathData, fileNameInfortunati);
        jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, false, true);

        String urlProbabili = urlFanta + "probabili-formazioni-complete-serie-a-live.asp";
        log.info("httpUrlProbabili {}", urlProbabili);
        String fileNameProbabili = FILE_PROBABILI + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvProbabili(urlProbabili, basePathData, fileNameProbabili);
        fileName = buildCsvPath(basePathData, fileNameProbabili);
        jobProcessGiornata.initDbProbabili(fileName);
    }

    private void processFantaGazzettaFiles(
            JobProcessFileCsv jobCsv,
            FcGiornataInfo giornataInfo,
            String basePathData) throws Exception {

        String fileNameSqualificatiInfortunati =
                FILE_SQUALIFICATI_INFORTUNATI_FG + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvSqualificatiInfortunatiFantaGazzetta(
                Costants.HTTP_URL_FANTAGAZZETTA_PROBABILI,
                basePathData,
                fileNameSqualificatiInfortunati);

        String fileName = buildCsvPath(basePathData, fileNameSqualificatiInfortunati);
        jobProcessGiornata.initDbSqualificatiInfortunatiFantaGazzetta(giornataInfo, fileName);

        String fileNameProbabili = FILE_PROBABILI_FG + giornataInfo.getCodiceGiornata();
        jobCsv.downloadCsvProbabiliFantaGazzetta(
                Costants.HTTP_URL_FANTAGAZZETTA_PROBABILI,
                basePathData,
                fileNameProbabili);

        fileName = buildCsvPath(basePathData, fileNameProbabili);
        jobProcessGiornata.initDbProbabiliFantaGazzetta(fileName);
    }

    private String buildCsvPath(String basePathData, String fileName) {
        return basePathData + fileName + ".csv";
    }

    private Grid<FcGiornataGiocatore> createGridSqualificatiInfortunati() {
        Grid<FcGiornataGiocatore> grid = new Grid<>();
        grid.setItems(new ArrayList<>());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);

        Column<FcGiornataGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(this::buildRuoloCell));
        ruoloColumn.setSortable(false);
        ruoloColumn.setHeader(Costants.RUOLO);
        ruoloColumn.setAutoWidth(true);

        Column<FcGiornataGiocatore> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildGiocatoreCell));
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setHeader(Costants.GIOCATORE);
        giocatoreColumn.setAutoWidth(true);

        Column<FcGiornataGiocatore> squadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildSquadraCell));
        squadraColumn.setSortable(false);
        squadraColumn.setHeader(Costants.SQUADRA);
        squadraColumn.setAutoWidth(true);

        Column<FcGiornataGiocatore> noteColumn = grid.addColumn(FcGiornataGiocatore::getNote);
        noteColumn.setSortable(false);
        noteColumn.setHeader(Costants.NOTE);
        noteColumn.setAutoWidth(true);

        return grid;
    }

    private HorizontalLayout buildRuoloCell(FcGiornataGiocatore giornataGiocatore) {
        HorizontalLayout cellLayout = new HorizontalLayout();

        FcGiocatore giocatore = giornataGiocatore.getFcGiocatore();
        if (giocatore != null && giocatore.getFcRuolo() != null) {
            String ruolo = giocatore.getFcRuolo().getIdRuolo().toLowerCase();
            cellLayout.add(buildImage(Costants.CLASSPATH_IMAGES + ruolo + ".png", ruolo + ".png"));
        }

        return cellLayout;
    }

    private HorizontalLayout buildGiocatoreCell(FcGiornataGiocatore giornataGiocatore) {
        HorizontalLayout cellLayout = new HorizontalLayout();

        FcGiocatore giocatore = giornataGiocatore.getFcGiocatore();
        if (giocatore != null) {
            if (giocatore.getImgSmall() != null) {
                try {
                    cellLayout.add(Utils.getImage(
                            giocatore.getNomeImg(),
                            giocatore.getImgSmall().getBinaryStream()));
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine giocatore {}", giocatore.getCognGiocatore(), e);
                }
            }
            cellLayout.add(new Span(giocatore.getCognGiocatore()));
        }

        return cellLayout;
    }

    private HorizontalLayout buildSquadraCell(FcGiornataGiocatore giornataGiocatore) {
        HorizontalLayout cellLayout = new HorizontalLayout();

        FcGiocatore giocatore = giornataGiocatore.getFcGiocatore();
        if (giocatore != null && giocatore.getFcSquadra() != null) {
            FcSquadra squadra = giocatore.getFcSquadra();

            if (squadra.getImg() != null) {
                try {
                    cellLayout.add(Utils.getImage(
                            squadra.getNomeSquadra(),
                            squadra.getImg().getBinaryStream()));
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine squadra {}", squadra.getNomeSquadra(), e);
                }
            }

            cellLayout.add(new Span(squadra.getNomeSquadra()));
        }

        return cellLayout;
    }

    private Image buildImage(String resourcePath, String imageName) {
        return Utils.buildImage(imageName, resourceLoader.getResource(resourcePath));
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
