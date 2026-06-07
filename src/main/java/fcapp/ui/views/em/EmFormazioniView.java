package fcapp.ui.views.em;

import java.io.Serial;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataDettInfo;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.GiornataDettInfoService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Formazioni")
@Route(value = "formazioniEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmFormazioniView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String TITOLARI = "Titolari";
    private static final String PANCHINA = "Panchina";
    private static final String FLAG_SUBENTRATO = "S";
    private static final String FLAG_USCITO = "N";
    private static final int LAST_TITOLARE_POSITION = 11;

    private final transient Logger log = LoggerFactory.getLogger(getClass());
    private final transient ResourceLoader resourceLoader;
    private final transient GiornataInfoService giornataInfoService;
    private final transient GiornataDettService giornataDettService;
    private final transient GiornataDettInfoService giornataDettInfoService;
    private final transient ClassificaTotalePuntiService classificaTotalePuntiService;
    private final transient AttoreService attoreService;
    private final transient AccessoService accessoService;

    private final VerticalLayout mainLayout = new VerticalLayout();

    private FcGiornataInfo giornataInfo;
    private FcCampionato campionato;
    private List<FcGiornataInfo> giornate;
    private List<FcAttore> squadre = new ArrayList<>();

    private final Map<String, Image> legendIcons = new HashMap<>();

    public EmFormazioniView(
            ResourceLoader resourceLoader,
            GiornataInfoService giornataInfoService,
            GiornataDettService giornataDettService,
            GiornataDettInfoService giornataDettInfoService,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            AttoreService attoreService,
            AccessoService accessoService) {
        log.info("EmFormazioniView()");
        this.resourceLoader = resourceLoader;
        this.giornataInfoService = giornataInfoService;
        this.giornataDettService = giornataDettService;
        this.giornataDettInfoService = giornataDettInfoService;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
        this.attoreService = attoreService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        log.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initImg();
        initData();
        initLayout();
    }

    private void initData() {
        VaadinSession session = VaadinSession.getCurrent();
        giornataInfo = (FcGiornataInfo) session.getAttribute("GIORNATA_INFO");
        campionato = (FcCampionato) session.getAttribute("CAMPIONATO");

        Integer from = campionato.getStart();
        Integer to = campionato.getEnd();

        giornate = giornataInfoService
                .findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
        squadre = attoreService.findByActive(true);
    }

    private void initImg() {
        log.info("initImg()");

        legendIcons.put("amm", buildImage("amm.png"));
        legendIcons.put("esp", buildImage("esp.png"));
        legendIcons.put("assist", buildImage("assist.png"));
        legendIcons.put("autogol", buildImage("autogol.png"));
        legendIcons.put("entrato", buildImage("entrato.png"));
        legendIcons.put("golfatto", buildImage("golfatto.png"));
        legendIcons.put("golsubito", buildImage("golsubito.png"));
        legendIcons.put("uscito", buildImage("uscito.png"));
        legendIcons.put("rigoresbagliato", buildImage("rigoresbagliato.png"));
        legendIcons.put("rigoresegnato", buildImage("rigoresegnato.png"));
        legendIcons.put("rigoreparato", buildImage("rigoreparato.png"));
        legendIcons.put("golvittoria", buildImage("golvittoria.png"));
    }

    private void initLayout() {
        log.info("initLayout()");

        Button stampaPdf = new Button("Risultati pdf");
        stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());

        ComboBox<FcGiornataInfo> comboGiornata = new ComboBox<>();
        comboGiornata.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
        comboGiornata.setItems(giornate);
        comboGiornata.setClearButtonVisible(true);
        comboGiornata.setPlaceholder("Seleziona la giornata");
        comboGiornata.setWidthFull();

        comboGiornata.addValueChangeListener(event -> {
            mainLayout.removeAll();
            stampaPdf.setEnabled(false);

            if (event.getSource().isEmpty()) {
                log.info("Nessuna giornata selezionata");
                return;
            }

            FcGiornataInfo selectedGiornata = event.getValue();
            log.info("giornata {}", selectedGiornata.getCodiceGiornata());
            buildTabGiornata(mainLayout, selectedGiornata.getCodiceGiornata());
            stampaPdf.setEnabled(true);
        });

        add(comboGiornata, mainLayout, buildLegenda());
        comboGiornata.setValue(giornataInfo);
    }

    private void buildTabGiornata(VerticalLayout layout, Integer codiceGiornata) {
        FcGiornataInfo selectedGiornataInfo = giornataInfoService.findByCodiceGiornata(codiceGiornata);

        Accordion accordion = new Accordion();
        accordion.setSizeFull();

        for (FcAttore attore : squadre) {
            VerticalLayout squadraLayout = new VerticalLayout();
            squadraLayout.setSizeFull();

            try {
                SquadData squadData = buildData(attore, selectedGiornataInfo);

                squadraLayout.add(
                        buildResultSquadra(squadData.itemsTitolari(), TITOLARI, squadData.schema()),
                        buildResultSquadra(squadData.itemsPanchina(), PANCHINA, "")
                );
            } catch (Exception e) {
                log.info("NO DATA {}", attore.getDescAttore(), e);
            }

            squadraLayout.add(buildTotaliInfo(campionato, attore, selectedGiornataInfo));
            accordion.add(attore.getDescAttore(), squadraLayout);
        }

        layout.add(accordion);
        layout.setSizeFull();
    }

    private SquadData buildData(FcAttore attore, FcGiornataInfo giornataInfo) {
        log.info("START buildData {}", attore.getDescAttore());

        List<FcGiornataDett> allItems = giornataDettService
                .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

        List<FcGiornataDett> titolari = new ArrayList<>();
        List<FcGiornataDett> panchina = new ArrayList<>();

        int countDifensori = 0;
        int countCentrocampisti = 0;
        int countAttaccanti = 0;

        for (FcGiornataDett dettaglio : allItems) {
            if (isTitolare(dettaglio)) {
                titolari.add(dettaglio);

                String ruolo = dettaglio.getFcGiocatore().getFcRuolo().getIdRuolo();
                switch (ruolo) {
                    case "D" -> countDifensori++;
                    case "C" -> countCentrocampisti++;
                    case "A" -> countAttaccanti++;
                    default -> {
                    }
                }
            } else {
                panchina.add(dettaglio);
            }
        }

        String schema = countDifensori + "-" + countCentrocampisti + "-" + countAttaccanti;

        log.info("END buildData {}", attore.getDescAttore());
        return new SquadData(titolari, panchina, schema);
    }

    private Grid<FcGiornataDett> buildResultSquadra(
            List<FcGiornataDett> items,
            String statoGiocatore,
            String schema) {

        Grid<FcGiornataDett> grid = new Grid<>();
        grid.setItems(items);
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        Column<FcGiornataDett> ruoloColumn = grid.addColumn(new ComponentRenderer<>(this::buildRuoloCell));
        configureColumn(ruoloColumn, "");

        Column<FcGiornataDett> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildGiocatoreCell));
        configureColumn(cognGiocatoreColumn, "");

        Column<FcGiornataDett> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildSquadraCell));
        configureColumn(nomeSquadraColumn, "");

        Column<FcGiornataDett> resultGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildEventiCell));
        configureColumn(resultGiocatoreColumn, "");

        Column<FcGiornataDett> votoColumn = grid.addColumn(new ComponentRenderer<>(this::buildVotoCell));
        configureColumn(votoColumn, "FV");

        HeaderRow headerRow = grid.prependHeaderRow();

        HeaderCell headerCellStatoGiocatore = headerRow.join(ruoloColumn, cognGiocatoreColumn);
        headerCellStatoGiocatore.setText(statoGiocatore);

        HeaderCell headerCellModulo = headerRow.join(resultGiocatoreColumn, votoColumn);
        if (TITOLARI.equals(statoGiocatore)) {
            headerCellModulo.setText("Modulo: " + schema);
        }

        return grid;
    }

    private void configureColumn(Column<FcGiornataDett> column, String header) {
        column.setSortable(false);
        column.setResizable(false);
        column.setHeader(header);
        column.setAutoWidth(true);
    }

    private HorizontalLayout buildRuoloCell(FcGiornataDett dettaglio) {
        HorizontalLayout cellLayout = createBaseCellLayout();

        if (dettaglio != null && dettaglio.getFcGiocatore() != null) {
            String ruolo = dettaglio.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase();
            cellLayout.add(buildImage(ruolo + ".png"));
        }

        return cellLayout;
    }

    private HorizontalLayout buildGiocatoreCell(FcGiornataDett dettaglio) {
        HorizontalLayout cellLayout = createStyledPlayerCell(dettaglio);

        FcGiocatore giocatore = dettaglio.getFcGiocatore();
        if (giocatore == null) {
            return cellLayout;
        }

        Span lblGiocatore = new Span(giocatore.getCognGiocatore());
        lblGiocatore.getStyle().set("fontSize", "smaller");
        cellLayout.add(lblGiocatore);

        if (isTitolare(dettaglio) && isFlag(dettaglio, FLAG_USCITO)) {
            cellLayout.add(buildSmallImage("uscito_s.png"));
        }

        if (isPanchinaro(dettaglio) && isFlag(dettaglio, FLAG_SUBENTRATO)) {
            cellLayout.add(buildSmallImage("entrato_s.png"));
        }

        return cellLayout;
    }

    private HorizontalLayout buildSquadraCell(FcGiornataDett dettaglio) {
        HorizontalLayout cellLayout = createStyledPlayerCell(dettaglio);

        FcGiocatore giocatore = dettaglio.getFcGiocatore();
        if (giocatore == null) {
            return cellLayout;
        }

        FcSquadra squadra = giocatore.getFcSquadra();
        if (squadra != null && squadra.getImg() != null) {
            try {
                Image img = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                cellLayout.add(img);
            } catch (SQLException e) {
                log.error("Errore caricamento immagine squadra {}", squadra.getNomeSquadra(), e);
            }
        }

        String nomeSquadra = squadra != null && squadra.getNomeSquadra() != null
                ? squadra.getNomeSquadra()
                : "";
        String label = nomeSquadra.length() >= 3 ? nomeSquadra.substring(0, 3) : nomeSquadra;

        Span lblSquadra = new Span(label);
        lblSquadra.getStyle().set("fontSize", "smaller");
        cellLayout.add(lblSquadra);

        return cellLayout;
    }

    private HorizontalLayout buildEventiCell(FcGiornataDett dettaglio) {
        HorizontalLayout cellLayout = createStyledPlayerCell(dettaglio);

        if (dettaglio.getFcGiocatore() == null || dettaglio.getFcPagelle() == null) {
            return cellLayout;
        }

        addRepeatedImages(cellLayout, "amm_s.png", dettaglio.getFcPagelle().getAmmonizione());
        addRepeatedImages(cellLayout, "esp_s.png", dettaglio.getFcPagelle().getEspulsione());
        addRepeatedImages(cellLayout, "golsubito_s.png", dettaglio.getFcPagelle().getGoalSubito());
        addRepeatedImages(
                cellLayout,
                "golfatto_s.png",
                dettaglio.getFcPagelle().getGoalRealizzato() - dettaglio.getFcPagelle().getRigoreSegnato()
        );
        addRepeatedImages(cellLayout, "autogol_s.png", dettaglio.getFcPagelle().getAutorete());
        addRepeatedImages(cellLayout, "rigoresbagliato_s.png", dettaglio.getFcPagelle().getRigoreFallito());
        addRepeatedImages(cellLayout, "rigoresegnato_s.png", dettaglio.getFcPagelle().getRigoreSegnato());
        addRepeatedImages(cellLayout, "rigoreparato_s.png", dettaglio.getFcPagelle().getRigoreParato());
        addRepeatedImages(cellLayout, "assist_s.png", dettaglio.getFcPagelle().getAssist());
        addRepeatedImages(cellLayout, "golvittoria_s.png", dettaglio.getFcPagelle().getGdv());

        return cellLayout;
    }

    private Span buildVotoCell(FcGiornataDett dettaglio) {
        FcGiocatore giocatore = dettaglio.getFcGiocatore();
        if (giocatore == null) {
            return null;
        }

        Span label = new Span(formatVoto(dettaglio.getVoto()));
        applyPlayerStyle(label, dettaglio);
        label.getStyle().set("fontSize", "smaller");
        return label;
    }

    private HorizontalLayout createBaseCellLayout() {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.setAlignItems(Alignment.STRETCH);
        cellLayout.setSizeFull();
        return cellLayout;
    }

    private HorizontalLayout createStyledPlayerCell(FcGiornataDett dettaglio) {
        HorizontalLayout cellLayout = createBaseCellLayout();
        applyPlayerStyle(cellLayout, dettaglio);
        return cellLayout;
    }

    private void applyPlayerStyle(com.vaadin.flow.component.Component component, FcGiornataDett dettaglio) {
        component.getStyle().set("color", getTextColor(dettaglio));

        FcGiocatore giocatore = dettaglio.getFcGiocatore();
        if (giocatore != null && !giocatore.isFlagAttivo()) {
            component.getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
            component.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
        }
    }

    private String getTextColor(FcGiornataDett dettaglio) {
        if (isFlag(dettaglio, FLAG_SUBENTRATO)) {
            return Costants.GRAY;
        }
        return Costants.LIGHT_GRAY;
    }

    private boolean isFlag(FcGiornataDett dettaglio, String flag) {
        return StringUtils.isNotEmpty(dettaglio.getFlagAttivo())
                && flag.equalsIgnoreCase(dettaglio.getFlagAttivo());
    }

    private boolean isTitolare(FcGiornataDett dettaglio) {
        return dettaglio.getOrdinamento() < LAST_TITOLARE_POSITION + 1;
    }

    private boolean isPanchinaro(FcGiornataDett dettaglio) {
        return dettaglio.getOrdinamento() > LAST_TITOLARE_POSITION;
    }

    private void addRepeatedImages(HorizontalLayout layout, String imageName, int times) {
        for (int i = 0; i < Math.max(0, times); i++) {
            layout.add(buildSmallImage(imageName));
        }
    }

    private String formatVoto(Double voto) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        double value = voto != null ? voto / Costants.DIVISORE_10 : 0D;
        return formatter.format(value);
    }

    private VerticalLayout buildTotaliInfo(
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo) {

        VerticalLayout layoutMain = new VerticalLayout();
        layoutMain.setWidth("80%");

        FcGiornataDettInfo info = giornataDettInfoService.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
        FcClassificaTotPt totPunti = classificaTotalePuntiService
                .findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);

        NumberFormat formatter = new DecimalFormat("#0.00");
        String totaleGiornata = "";
        if (totPunti != null && totPunti.getTotPt() != null) {
            totaleGiornata = formatter.format(totPunti.getTotPt() / Costants.DIVISORE_10);
        }

        Span lblTotGiornata = new Span("Totale Giornata: " + totaleGiornata);
        lblTotGiornata.getStyle().set(Costants.FONT_SIZE, "24px");
        lblTotGiornata.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
        lblTotGiornata.setSizeFull();

        String dataInvio = info == null
                ? ""
                : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss");
        Span lblInvio = new Span("Inviata alle: " + dataInvio);
        lblInvio.setSizeFull();

        layoutMain.add(lblTotGiornata, lblInvio);
        return layoutMain;
    }

    private VerticalLayout buildLegenda() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layout.setMargin(false);

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setSpacing(true);
        addLegendItem(row1, "golfatto", "Gol Fatto");
        addLegendItem(row1, "golsubito", "Gol Subito");
        addLegendItem(row1, "amm", "Ammonizione");
        addLegendItem(row1, "esp", "Espulsione");
        addLegendItem(row1, "assist", "Assist");
        addLegendItem(row1, "entrato", "Entrato");

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setSpacing(true);
        addLegendItem(row2, "uscito", "Uscito");
        addLegendItem(row2, "autogol", "Autogol");
        addLegendItem(row2, "rigoresegnato", "Rigore segnato");
        addLegendItem(row2, "rigoresbagliato", "Rigore sbagliato");
        addLegendItem(row2, "rigoreparato", "Rigore parato");
        addLegendItem(row2, "golvittoria", "Gol Vittoria");

        layout.add(row1, row2);
        return layout;
    }

    private void addLegendItem(HorizontalLayout layout, String iconKey, String text) {
        Image icon = legendIcons.get(iconKey);
        if (icon != null) {
            layout.add(icon, new Span(text));
        }
    }

    private Image buildImage(String fileName) {
        return Utils.buildImage(fileName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + fileName));
    }

    private Image buildSmallImage(String fileName) {
        return Utils.buildImage(fileName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + fileName));
    }

    private record SquadData(
            List<FcGiornataDett> itemsTitolari,
            List<FcGiornataDett> itemsPanchina,
            String schema) {
    }
}
