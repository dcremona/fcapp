package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.FormazioneJasper;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.FormazioneService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Tutte le Rose")
@Route(value = "squadreAll", layout = MainLayout.class)
@RolesAllowed("USER")
public class SquadreAllView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String REPORT_ROSE_ALL = "classpath:reports/roseFcAll.jasper";
    private static final String DECIMAL_PATTERN = "#0.00";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient ResourceLoader resourceLoader;
    private final transient AttoreService attoreService;
    private final transient FormazioneService formazioneService;
    private final transient AccessoService accessoService;

    private List<FcAttore> squadre = new ArrayList<>();

    public SquadreAllView(
            ResourceLoader resourceLoader,
            AttoreService attoreService,
            FormazioneService formazioneService,
            AccessoService accessoService) {

        this.resourceLoader = resourceLoader;
        this.attoreService = attoreService;
        this.formazioneService = formazioneService;
        this.accessoService = accessoService;

        log.info("SquadreAllView()");
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
        if (campionato == null) {
            return;
        }

        try {
            FileDownloadWrapper buttonRose = buildButtonRose(campionato);
            if (buttonRose != null) {
                add(buttonRose);
            }
        } catch (Exception e) {
            log.error("Errore nella creazione del pulsante pdf rose", e);
        }

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.add(buildRowLayout(campionato, 1, 2));
        mainLayout.add(buildRowLayout(campionato, 3, 4));
        mainLayout.add(buildRowLayout(campionato, 5, 6));
        mainLayout.add(buildRowLayout(campionato, 7, 8));

        add(mainLayout);
    }

    private HorizontalLayout buildRowLayout(FcCampionato campionato, int firstAttoreId, int secondAttoreId) {
        HorizontalLayout rowLayout = new HorizontalLayout();
        rowLayout.setMargin(false);
        rowLayout.setPadding(false);
        rowLayout.setSpacing(false);
        rowLayout.setSizeFull();

        for (FcAttore attore : squadre) {
            if (attore.getIdAttore() == firstAttoreId || attore.getIdAttore() == secondAttoreId) {
                List<FcFormazione> formazioneList =
                        formazioneService.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
                                campionato, attore, true);

                int totalePagato = calculateTotalePagato(formazioneList);

                rowLayout.add(getTableFormazione(formazioneList, totalePagato, attore.getDescAttore()));
            }
        }

        return rowLayout;
    }

    private int calculateTotalePagato(List<FcFormazione> listFormazione) {
        double somma = 0d;
        for (FcFormazione formazione : listFormazione) {
            if (formazione.getTotPagato() != null) {
                somma += formazione.getTotPagato();
            }
        }
        return (int) somma;
    }

    private FileDownloadWrapper buildButtonRose(FcCampionato campionato) {
        try {
            Button stampaPdfRose = new Button("Tutte le Rose pdf");
            stampaPdfRose.setIcon(VaadinIcon.DOWNLOAD.create());

            Map<String, Object> parameters = getMapRoseFcAll(campionato);
            parameters.put("titolo", "Rose Fc");

            ArrayList<FormazioneJasper> collection = new ArrayList<>();
            collection.add(new FormazioneJasper("P", "G", "Sq", 0, 0));

            Resource resource = resourceLoader.getResource(REPORT_ROSE_ALL);

            FileDownloadWrapper wrapper = new FileDownloadWrapper(
                    Utils.getStreamResource(
                            "RoseFcAll.pdf",
                            collection,
                            parameters,
                            resource.getInputStream()));

            wrapper.wrapComponent(stampaPdfRose);
            return wrapper;

        } catch (Exception e) {
            log.error("Errore nella generazione del pdf di tutte le rose", e);
            return null;
        }
    }

    private Grid<FcFormazione> getTableFormazione(List<FcFormazione> items, Integer somma, String attore) {
        Grid<FcFormazione> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);

        Column<FcFormazione> ruoloColumn = grid.addColumn(new ComponentRenderer<>(this::buildRuoloComponent));
        ruoloColumn.setSortable(true);
        ruoloColumn.setHeader(Costants.R);
        ruoloColumn.setAutoWidth(true);

        Column<FcFormazione> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildGiocatoreComponent));
        cognGiocatoreColumn.setSortable(false);
        cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
        cognGiocatoreColumn.setAutoWidth(true);

        Column<FcFormazione> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildSquadraComponent));
        nomeSquadraColumn.setSortable(true);
        nomeSquadraColumn.setComparator(Comparator.comparing(this::getNomeSquadraSafe));
        nomeSquadraColumn.setHeader(Costants.SQUADRA);
        nomeSquadraColumn.setAutoWidth(true);

        Column<FcFormazione> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(this::buildMediaVotoComponent));
        mediaVotoColumn.setSortable(true);
        mediaVotoColumn.setComparator(Comparator.comparing(this::getMediaVotoSafe));
        mediaVotoColumn.setHeader(Costants.MV);
        mediaVotoColumn.setAutoWidth(true);

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
                cognGiocatoreColumn,
                nomeSquadraColumn,
                mediaVotoColumn,
                quotazioneColumn,
                totPagatoColumn);
        informationCell.setComponent(buildSectionHeader(attore));

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(quotazioneColumn).setComponent(buildFooterCell("Totale"));
        footerRow.getCell(totPagatoColumn).setComponent(buildFooterCell(String.valueOf(somma)));

        return grid;
    }

    private Map<String, Object> getMapRoseFcAll(FcCampionato campionato) {
        Map<String, Object> parameters = new HashMap<>();

        for (FcAttore attore : squadre) {
            Collection<FormazioneJasper> rosaJasper = new ArrayList<>();
            List<FcFormazione> listFormazione =
                    formazioneService.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
                            campionato, attore, true);

            double somma = 0d;

            for (FcFormazione formazione : listFormazione) {
                FormazioneJasper item = toFormazioneJasper(formazione);
                if (formazione.getTotPagato() != null) {
                    somma += formazione.getTotPagato();
                }
                rosaJasper.add(item);
            }

            rosaJasper.add(new FormazioneJasper("", "", "Totale", 0, (int) somma));

            parameters.put("data" + attore.getIdAttore(), rosaJasper);
            parameters.put("sq" + attore.getIdAttore(), attore.getDescAttore());
            parameters.put("tot" + attore.getIdAttore(), String.valueOf(somma));
        }

        return parameters;
    }

    private FormazioneJasper toFormazioneJasper(FcFormazione formazione) {
        if (formazione == null
                || formazione.getFcGiocatore() == null
                || formazione.getFcGiocatore().getFcRuolo() == null
                || formazione.getFcGiocatore().getFcSquadra() == null) {
            return new FormazioneJasper("", "", "", 0, 0);
        }

        FcGiocatore giocatore = formazione.getFcGiocatore();

        return new FormazioneJasper(
                giocatore.getFcRuolo().getIdRuolo(),
                giocatore.getCognGiocatore(),
                giocatore.getFcSquadra().getNomeSquadra(),
                giocatore.getQuotazione(),
                formazione.getTotPagato());
    }

    private Component buildRuoloComponent(FcFormazione formazione) {
        HorizontalLayout layout = new HorizontalLayout();

        if (formazione != null
                && formazione.getFcGiocatore() != null
                && formazione.getFcGiocatore().getFcRuolo() != null
                && StringUtils.isNotBlank(formazione.getFcGiocatore().getFcRuolo().getIdRuolo())) {

            String ruolo = formazione.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase();
            layout.add(buildImage(ruolo + ".png"));
        }

        return layout;
    }

    private Component buildGiocatoreComponent(FcFormazione formazione) {
        HorizontalLayout layout = new HorizontalLayout();

        if (formazione != null
                && formazione.getFcGiocatore() != null
                && StringUtils.isNotBlank(formazione.getFcGiocatore().getNomeImg())) {

            if (formazione.getFcGiocatore().getImgSmall() != null) {
                try {
                    layout.add(Utils.getImage(
                            formazione.getFcGiocatore().getNomeImg(),
                            formazione.getFcGiocatore().getImgSmall().getBinaryStream()));
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine giocatore", e);
                }
            }

            layout.add(new Span(formazione.getFcGiocatore().getCognGiocatore()));
        }

        return layout;
    }

    private Component buildSquadraComponent(FcFormazione formazione) {
        HorizontalLayout layout = new HorizontalLayout();

        if (formazione != null
                && formazione.getFcGiocatore() != null
                && formazione.getFcGiocatore().getFcSquadra() != null) {

            FcSquadra squadra = formazione.getFcGiocatore().getFcSquadra();

            if (squadra.getImg() != null) {
                try {
                    layout.add(Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream()));
                } catch (SQLException e) {
                    log.error("Errore caricamento immagine squadra", e);
                }
            }

            layout.add(new Span(squadra.getNomeSquadra()));
        }

        return layout;
    }

    private Component buildMediaVotoComponent(FcFormazione formazione) {
        HorizontalLayout layout = new HorizontalLayout();

        if (formazione == null || formazione.getFcGiocatore() == null) {
            return layout;
        }

        FcStatistiche statistiche = formazione.getFcGiocatore().getFcStatistiche();
        double mediaVoto = statistiche != null ? statistiche.getMediaVoto() : 0;

        layout.add(buildImage(resolveTrendImage(mediaVoto)));
        layout.add(new Span(formatDecimal(mediaVoto)));

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

    private Image buildImage(String imageName) {
        return Utils.buildImage(imageName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));
    }

    private String resolveTrendImage(double mediaVoto) {
        if (mediaVoto == 0) {
            return "2.png";
        }
        if (mediaVoto > Costants.RANGE_MAX_MV) {
            return "1.png";
        }
        if (mediaVoto < Costants.RANGE_MIN_MV) {
            return "3.png";
        }
        return "2.png";
    }

    private String formatDecimal(double value) {
        DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
        return formatter.format( value / Costants.DIVISORE_100);
    }

    private String getNomeSquadraSafe(FcFormazione formazione) {
        if (formazione == null
                || formazione.getFcGiocatore() == null
                || formazione.getFcGiocatore().getFcSquadra() == null) {
            return "";
        }
        return formazione.getFcGiocatore().getFcSquadra().getNomeSquadra();
    }

    private Double getMediaVotoSafe(FcFormazione formazione) {
        if (formazione == null
                || formazione.getFcGiocatore() == null
                || formazione.getFcGiocatore().getFcStatistiche() == null) {
            return 0d;
        }
        return formazione.getFcGiocatore().getFcStatistiche().getMediaVoto();
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
