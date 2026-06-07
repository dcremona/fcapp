package fcapp.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.Component;
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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataDettInfo;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.job.JobProcessSendMail;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ClassificaService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.GiornataDettInfoService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Formazioni")
@Route(value = "formazioni", layout = MainLayout.class)
@RolesAllowed("USER")
public class FormazioniView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String ATTIVO_SI = "S";
    private static final String ATTIVO_NO = "N";

    private static final String STATO_TITOLARI = "Titolari";
    private static final String STATO_PANCHINA = "Panchina";
    private static final String STATO_TRIBUNA = "Tribuna";

//    private static final int TITOLARI_END = 11;
//    private static final int PANCHINA_END = 18;
    private static final int ROSA_SIZE = 26;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient ResourceLoader resourceLoader;
    private final transient JobProcessSendMail jobProcessSendMail;
    private final transient GiornataInfoService giornataInfoService;
    private final transient GiornataService giornataService;
    private final transient GiornataDettService giornataDettService;
    private final transient GiornataDettInfoService giornataDettInfoService;
    private final transient ClassificaTotalePuntiService classificaTotalePuntiService;
    private final transient ClassificaService classificaService;
    private final transient AccessoService accessoService;

    private final Map<LegendIcon, Image> legendIcons = new EnumMap<>(LegendIcon.class);

    private final VerticalLayout mainLayout = new VerticalLayout();
    private final ComboBox<FcGiornataInfo> comboGiornata = new ComboBox<>();

    public FormazioniView(
            ResourceLoader resourceLoader,
            JobProcessSendMail jobProcessSendMail,
            GiornataInfoService giornataInfoService,
            GiornataService giornataService,
            GiornataDettService giornataDettService,
            GiornataDettInfoService giornataDettInfoService,
            ClassificaTotalePuntiService classificaTotalePuntiService,
            ClassificaService classificaService,
            AccessoService accessoService) {

        this.resourceLoader = resourceLoader;
        this.jobProcessSendMail = jobProcessSendMail;
        this.giornataInfoService = giornataInfoService;
        this.giornataService = giornataService;
        this.giornataDettService = giornataDettService;
        this.giornataDettInfoService = giornataDettInfoService;
        this.classificaTotalePuntiService = classificaTotalePuntiService;
        this.classificaService = classificaService;
        this.accessoService = accessoService;

        log.info("FormazioniView()");
    }

    @PostConstruct
    void init() {
        log.info("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initImages();
        initLayout();
    }

    private void initImages() {
        log.info("initImages()");

        legendIcons.put(LegendIcon.AMMONIZIONE, buildImage("amm.png"));
        legendIcons.put(LegendIcon.ESPULSIONE, buildImage("esp.png"));
        legendIcons.put(LegendIcon.ASSIST, buildImage("assist.png"));
        legendIcons.put(LegendIcon.AUTOGOL, buildImage("autogol.png"));
        legendIcons.put(LegendIcon.ENTRATO, buildImage("entrato.png"));
        legendIcons.put(LegendIcon.GOAL_FATTO, buildImage("golfatto.png"));
        legendIcons.put(LegendIcon.GOAL_SUBITO, buildImage("golsubito.png"));
        legendIcons.put(LegendIcon.USCITO, buildImage("uscito.png"));
        legendIcons.put(LegendIcon.RIGORE_SBAGLIATO, buildImage("rigoresbagliato.png"));
        legendIcons.put(LegendIcon.RIGORE_SEGNATO, buildImage("rigoresegnato.png"));
        legendIcons.put(LegendIcon.RIGORE_PARATO, buildImage("rigoreparato.png"));
        legendIcons.put(LegendIcon.BONUS_PORTIERE, buildImage("portiereImbattuto.png"));
    }

    private void initLayout() {
        log.info("initLayout()");

        FcGiornataInfo giornataInfo = getSessionAttribute("GIORNATA_INFO", FcGiornataInfo.class);
        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);

        List<FcGiornataInfo> giornate = giornataInfoService
                .findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(
                        campionato.getStart(),
                        campionato.getEnd());

        Button stampaPdf = buildPdfDownloadButton(campionato);

        comboGiornata.setItemLabelGenerator(Utils::buildInfoGiornata);
        comboGiornata.setItems(giornate);
        comboGiornata.setClearButtonVisible(true);
        comboGiornata.setPlaceholder("Seleziona la giornata");
        comboGiornata.setWidthFull();
        comboGiornata.addValueChangeListener(event -> onGiornataChanged(event.getValue(), stampaPdf));

        mainLayout.setSizeFull();

        add(wrapDownloadButton(stampaPdf));
        add(comboGiornata);
        add(mainLayout);
        add(buildLegenda());

        comboGiornata.setValue(giornataInfo);
    }

    private Button buildPdfDownloadButton(FcCampionato campionato) {
        Button stampaPdf = new Button("Risultati pdf");
        stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());
        stampaPdf.setEnabled(false);
        return stampaPdf;
    }

    private FileDownloadWrapper wrapDownloadButton(Button button) {
        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);

        StreamResource resource = new StreamResource("Risultati.pdf", () -> {
            String pathImg = "images/";
            byte[] bytes = jobProcessSendMail.getJasperRisultati(campionato, comboGiornata.getValue(), pathImg);
            return new ByteArrayInputStream(bytes);
        });

        FileDownloadWrapper wrapper = new FileDownloadWrapper(resource);
        wrapper.wrapComponent(button);
        return wrapper;
    }

    private void onGiornataChanged(FcGiornataInfo fcGiornataInfo, Button stampaPdf) {
        log.info("addValueChangeListener");

        mainLayout.removeAll();
        stampaPdf.setEnabled(false);

        if (fcGiornataInfo == null) {
            log.info("giornata non selezionata");
            return;
        }

        log.info("giornata {}", fcGiornataInfo.getCodiceGiornata());
        buildTabGiornata(mainLayout, fcGiornataInfo.getCodiceGiornata());
        stampaPdf.setEnabled(true);
    }

    private void buildTabGiornata(VerticalLayout layout, Integer codiceGiornata) {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(codiceGiornata);
        List<FcGiornata> partite = giornataService.findByFcGiornataInfo(giornataInfo);

        for (FcGiornata partita : partite) {
            MatchTeamSection casa = buildMatchTeamSection(
                    campionato,
                    partita.getFcAttoreByIdAttoreCasa(),
                    giornataInfo,
                    true,
                    partita.getTotCasa());

            MatchTeamSection fuori = buildMatchTeamSection(
                    campionato,
                    partita.getFcAttoreByIdAttoreFuori(),
                    giornataInfo,
                    false,
                    partita.getTotFuori());

            VerticalLayout layoutTab = new VerticalLayout();
            layoutTab.setWidthFull();
            layoutTab.setMargin(false);
            layoutTab.setPadding(false);
            layoutTab.setSpacing(false);
            layoutTab.add(buildMatchHeader(partita, casa.teamName(), fuori.teamName()));
            layoutTab.add(buildMatchBody(casa.content(), fuori.content()));

            tabSheet.add(casa.teamName() + " [*] " + fuori.teamName(), layoutTab);
        }

        layout.add(tabSheet);
    }

    private MatchTeamSection buildMatchTeamSection(
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            boolean fattoreCampo,
            Double puntiGiornata) {

        TeamData teamData = buildData(attore, giornataInfo);
        String modificatoreDifesa = getModificatoreDifesa(teamData.schema());

        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.add(buildResultSquadra(teamData.titolari(), STATO_TITOLARI, teamData.schema()));
        content.add(buildResultSquadra(teamData.panchina(), STATO_PANCHINA, ""));
        content.add(buildResultSquadra(teamData.tribuna(), STATO_TRIBUNA, ""));
        content.add(buildAltriPunteggiInfo(
                campionato,
                attore,
                giornataInfo,
                fattoreCampo,
                modificatoreDifesa,
                teamData.panchina()));
        content.add(buildTotaliInfo(campionato, attore, giornataInfo, puntiGiornata));

        return new MatchTeamSection(attore.getDescAttore(), content);
    }

    private HorizontalLayout buildMatchHeader(FcGiornata partita, String squadraCasa, String squadraFuori) {
        HorizontalLayout layoutRisultato = new HorizontalLayout();
        layoutRisultato.add(buildScoreImage(partita.getGolCasa()));
        layoutRisultato.add(buildScoreImage(partita.getGolFuori()));

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        header.add(new Span(squadraCasa));
        header.add(layoutRisultato);
        header.add(new Span(squadraFuori));

        return header;
    }

    private HorizontalLayout buildMatchBody(Component casaContent, Component fuoriContent) {
        HorizontalLayout body = new HorizontalLayout();
        body.setWidthFull();
        body.add(casaContent, fuoriContent);
        return body;
    }

    private TeamData buildData(FcAttore attore, FcGiornataInfo giornataInfo) {
        log.info("START buildData {}", attore.getDescAttore());

        List<FcGiornataDett> all = giornataDettService
                .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

        List<FcGiornataDett> normalizedItems = normalizeRoster(all);
        FormationCounter formationCounter = countFormation(normalizedItems);

        List<FcGiornataDett> titolari = new ArrayList<>();
        List<FcGiornataDett> panchina = new ArrayList<>();
        List<FcGiornataDett> tribuna = new ArrayList<>();

        for (FcGiornataDett item : normalizedItems) {
            if (isStarter(item)) {
                titolari.add(item);
            } else if (isBench(item)) {
                panchina.add(item);
            } else {
                tribuna.add(item);
            }
        }

        String schema = formationCounter.defenders() + "-" + formationCounter.midfielders() + "-" + formationCounter.attackers();

        log.info("END buildData {}", attore.getDescAttore());

        return new TeamData(normalizedItems, titolari, panchina, tribuna, schema);
    }

    private List<FcGiornataDett> normalizeRoster(List<FcGiornataDett> items) {
        List<FcGiornataDett> normalized = new ArrayList<>(items);

        if (normalized.size() >= ROSA_SIZE) {
            return normalized;
        }

        int nextOrder = normalized.size();
        int missingPlayers = ROSA_SIZE - normalized.size();

        for (int i = 0; i < missingPlayers; i++) {
            FcGiornataDett emptyPlayer = new FcGiornataDett();
            emptyPlayer.setOrdinamento(nextOrder++);
            normalized.add(emptyPlayer);
        }

        return normalized;
    }

    private FormationCounter countFormation(List<FcGiornataDett> items) {
        int defenders = 0;
        int midfielders = 0;
        int attackers = 0;

        for (FcGiornataDett item : items) {
            if (!isStarter(item) || item.getFcGiocatore() == null || item.getFcGiocatore().getFcRuolo() == null) {
                continue;
            }

            String ruolo = item.getFcGiocatore().getFcRuolo().getIdRuolo();
            if (Costants.D.equals(ruolo)) {
                defenders++;
            } else if (Costants.C.equals(ruolo)) {
                midfielders++;
            } else if (Costants.A.equals(ruolo)) {
                attackers++;
            }
        }

        return new FormationCounter(defenders, midfielders, attackers);
    }

    private Grid<FcGiornataDett> buildResultSquadra(List<FcGiornataDett> items, String statoGiocatore, String schema) {
        Grid<FcGiornataDett> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);

        Column<FcGiornataDett> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildPlayerCell));
        cognGiocatoreColumn.setSortable(false);
        cognGiocatoreColumn.setResizable(false);
        cognGiocatoreColumn.setHeader("");

        Column<FcGiornataDett> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(this::buildTeamCell));
        nomeSquadraColumn.setSortable(false);
        nomeSquadraColumn.setResizable(false);
        nomeSquadraColumn.setHeader("");
        nomeSquadraColumn.setWidth("5rem").setFlexGrow(0);

        Column<FcGiornataDett> resultGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(this::buildEventsCell));
        resultGiocatoreColumn.setSortable(false);
        resultGiocatoreColumn.setResizable(false);
        resultGiocatoreColumn.setHeader("");

        Column<FcGiornataDett> votoColumn = grid.addColumn(new ComponentRenderer<>(gd -> buildValueSpan(gd, gd.getVoto(), Costants.FV)));
        votoColumn.setSortable(false);
        votoColumn.setResizable(false);
        votoColumn.setHeader(Costants.FV);
        votoColumn.setWidth("5rem").setFlexGrow(0);

        Column<FcGiornataDett> gColumn = grid.addColumn(new ComponentRenderer<>(gd ->
                buildValueSpan(gd, gd.getFcPagelle() != null ? gd.getFcPagelle().getG() : null, Costants.G)));
        gColumn.setSortable(false);
        gColumn.setResizable(false);
        gColumn.setHeader(Costants.G);
        gColumn.setWidth("5rem").setFlexGrow(0);

        Column<FcGiornataDett> csColumn = grid.addColumn(new ComponentRenderer<>(gd ->
                buildValueSpan(gd, gd.getFcPagelle() != null ? gd.getFcPagelle().getCs() : null, Costants.CS)));
        csColumn.setSortable(false);
        csColumn.setResizable(false);
        csColumn.setHeader(Costants.CS);
        csColumn.setWidth("5rem").setFlexGrow(0);

        Column<FcGiornataDett> tsColumn = grid.addColumn(new ComponentRenderer<>(gd ->
                buildValueSpan(gd, gd.getFcPagelle() != null ? gd.getFcPagelle().getTs() : null, Costants.TS)));
        tsColumn.setSortable(false);
        tsColumn.setResizable(false);
        tsColumn.setHeader(Costants.TS);
        tsColumn.setWidth("5rem").setFlexGrow(0);

        HeaderRow headerRow = grid.prependHeaderRow();

        HeaderCell headerCellStatoGiocatore = headerRow.join(cognGiocatoreColumn, nomeSquadraColumn);
        headerCellStatoGiocatore.setText(statoGiocatore);

        HeaderCell headerCellModulo = headerRow.join(csColumn, tsColumn);
        if (STATO_TITOLARI.equals(statoGiocatore)) {
            headerCellModulo.setText("Modulo: " + schema);
        }

        return grid;
    }

    private Component buildPlayerCell(FcGiornataDett gd) {
        HorizontalLayout cellLayout = buildBaseCellLayout(gd);

        if (gd.getFcGiocatore() == null) {
            return cellLayout;
        }

        String ruolo = gd.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase();
        cellLayout.add(buildImage(ruolo + ".png"));

        String descGiocatore = gd.getFcGiocatore().getCognGiocatore();
        if (hasSecondoCambioMalus(gd)) {
            descGiocatore = "(-0,5) " + descGiocatore;
        }

        Span lblGiocatore = new Span(descGiocatore);
        lblGiocatore.getStyle().set("fontSize", "smaller");
        cellLayout.add(lblGiocatore);

        if (isStarter(gd) && ATTIVO_NO.equalsIgnoreCase(gd.getFlagAttivo())) {
            cellLayout.add(buildImage("uscito_s.png"));
        }

        if (isBench(gd) && ATTIVO_SI.equalsIgnoreCase(gd.getFlagAttivo())) {
            cellLayout.add(buildImage("entrato_s.png"));
        }

        return cellLayout;
    }

    private Component buildTeamCell(FcGiornataDett gd) {
        HorizontalLayout cellLayout = buildBaseCellLayout(gd);

        if (gd.getFcGiocatore() != null && gd.getFcGiocatore().getFcSquadra() != null) {
            String nomeSquadra = gd.getFcGiocatore().getFcSquadra().getNomeSquadra();
            Span lblSquadra = new Span(nomeSquadra.substring(0, Math.min(3, nomeSquadra.length())));
            lblSquadra.getStyle().set("fontSize", "smaller");
            cellLayout.add(lblSquadra);
        }

        return cellLayout;
    }

    private Component buildEventsCell(FcGiornataDett gd) {
        HorizontalLayout cellLayout = buildBaseCellLayout(gd);

        if (gd.getFcGiocatore() == null || gd.getFcPagelle() == null) {
            return cellLayout;
        }

        addRepeatedIcons(cellLayout, "amm_s.png", gd.getFcPagelle().getAmmonizione());
        addRepeatedIcons(cellLayout, "esp_s.png", gd.getFcPagelle().getEspulsione());
        addRepeatedIcons(cellLayout, "golsubito_s.png", gd.getFcPagelle().getGoalSubito());
        addRepeatedIcons(
                cellLayout,
                "golfatto_s.png",
                Math.max(0, gd.getFcPagelle().getGoalRealizzato() - gd.getFcPagelle().getRigoreSegnato()));
        addRepeatedIcons(cellLayout, "autogol_s.png", gd.getFcPagelle().getAutorete());
        addRepeatedIcons(cellLayout, "rigoresbagliato_s.png", gd.getFcPagelle().getRigoreFallito());
        addRepeatedIcons(cellLayout, "rigoresegnato_s.png", gd.getFcPagelle().getRigoreSegnato());
        addRepeatedIcons(cellLayout, "rigoreparato_s.png", gd.getFcPagelle().getRigoreParato());
        addRepeatedIcons(cellLayout, "assist_s.png", gd.getFcPagelle().getAssist());

        if (hasPortiereImbattuto(gd)) {
            cellLayout.add(buildImage("portiereImbattuto_s.png"));
        }

        return cellLayout;
    }

    private Span buildValueSpan(FcGiornataDett gd, Number value, String header) {
        String pattern = Costants.TS.equals(header) ? "#0.00" : Costants.NUMBER_DECIMAL;
        DecimalFormat formatter = new DecimalFormat(pattern);

        double normalized = value == null ? 0d : value.doubleValue() / Costants.DIVISORE_100;
        Span lbl = new Span(formatter.format(normalized));
        applyTextColor(lbl, gd);
        lbl.getStyle().set("fontSize", "smaller");
        return lbl;
    }

    private Grid<FcProperties> buildAltriPunteggiInfo(
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            boolean fattoreCampo,
            String modificatoreDifesa,
            List<FcGiornataDett> itemsPanchina) {

        List<FcProperties> items = new ArrayList<>();
        NumberFormat formatter = new DecimalFormat("#0.00");

        items.add(property("ALTRI PUNTEGGI", ""));

        if (giornataInfo.getIdGiornataFc() == 15) {
            FcClassifica classifica = classificaService.findByFcCampionatoAndFcAttore(campionato, attore);
            items.add(property("Bonus Quarti:", getBonusQuarti(classifica)));
        } else if (giornataInfo.getIdGiornataFc() == 17) {
            FcClassifica classifica = classificaService.findByFcCampionatoAndFcAttore(campionato, attore);
            items.add(property("Bonus Semifinali:", String.valueOf(classifica.getVinte())));
        }

        if (giornataInfo.getIdGiornataFc() < 15) {
            items.add(property("Fattore Campo:", fattoreCampo ? "1,50" : "0,00"));
        }

        items.add(property("Modificatore Difesa:", modificatoreDifesa));

        double malus = calculateMalusSecondoCambio(itemsPanchina);
        items.add(property(
                "Malus Secondo Cambio:",
                malus == 0 ? formatter.format(malus) : "-" + formatter.format(malus)));

        Grid<FcProperties> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setItems(items);

        grid.addColumn(FcProperties::getKey);
        grid.addColumn(FcProperties::getValue);

        return grid;
    }

    private VerticalLayout buildTotaliInfo(
            FcCampionato campionato,
            FcAttore attore,
            FcGiornataInfo giornataInfo,
            Double puntiGiornata) {

        VerticalLayout layoutMain = new VerticalLayout();
        layoutMain.setWidthFull();

        FcGiornataDettInfo info = giornataDettInfoService.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
        FcClassificaTotPt totPunti = classificaTotalePuntiService
                .findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);

        NumberFormat formatter = new DecimalFormat("#0.00");

        String totaleGiornata = formatDoubleValue(puntiGiornata, formatter);
        String totalePunteggioRosa = "0";
        String totalePunteggioTvst = "0";

        try {
            if (totPunti != null) {
                totalePunteggioRosa = formatDoubleValue(totPunti.getTotPtRosa(), formatter);
                totalePunteggioTvst = String.valueOf(Objects.requireNonNullElse(totPunti.getPtTvsT(), 0));
            }
        } catch (Exception e) {
            log.error("Errore in buildTotaliInfo", e);
        }

        layoutMain.add(buildSummarySpan("Totale Giornata: " + totaleGiornata, "24px", Costants.LIGHT_BLUE));
        layoutMain.add(buildSummarySpan("Totale Punteggio Rosa: " + totalePunteggioRosa, "16px", Costants.LIGHT_YELLOW));
        layoutMain.add(buildSummarySpan("Totale Punteggio TvsT: " + totalePunteggioTvst, "16px", Costants.LIGHT_GRAY));

        Span lblInvio = new Span("Inviata alle: " +
                (info == null ? "" : Utils.formatDate(info.getDataInvio(), "dd/MM/yyyy HH:mm:ss")));
        lblInvio.setSizeFull();
        layoutMain.add(lblInvio);

        return layoutMain;
    }

    private FormLayout buildLegenda() {
        FormLayout layout = new FormLayout();
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

        layout.addFormItem(legendIcons.get(LegendIcon.GOAL_FATTO), "Goal Fatto (+3)");
        layout.addFormItem(legendIcons.get(LegendIcon.GOAL_SUBITO), "Goal Subito (-1)");
        layout.addFormItem(legendIcons.get(LegendIcon.AMMONIZIONE), "Ammonizione (-0.5)");
        layout.addFormItem(legendIcons.get(LegendIcon.ESPULSIONE), "Espulsione (-1)");
        layout.addFormItem(legendIcons.get(LegendIcon.ASSIST), "Assist (+1)");
        layout.addFormItem(legendIcons.get(LegendIcon.AUTOGOL), "Autogol (-2)");
        layout.addFormItem(legendIcons.get(LegendIcon.RIGORE_SEGNATO), "Rigore segnato (+3)");
        layout.addFormItem(legendIcons.get(LegendIcon.RIGORE_PARATO), "Rigore parato (+3)");
        layout.addFormItem(legendIcons.get(LegendIcon.RIGORE_SBAGLIATO), "Rigore sbagliato (-3)");
        layout.addFormItem(legendIcons.get(LegendIcon.BONUS_PORTIERE), "Portiere imbattuto (+1)");
        layout.addFormItem(legendIcons.get(LegendIcon.ENTRATO), "Entrato");
        layout.addFormItem(legendIcons.get(LegendIcon.USCITO), "Uscito");

        layout.setResponsiveSteps(
                new ResponsiveStep("1px", 1),
                new ResponsiveStep("600px", 2),
                new ResponsiveStep("700px", 3),
                new ResponsiveStep("800px", 4));

        return layout;
    }

    private String getModificatoreDifesa(String value) {
        return switch (value) {
            case Costants.SCHEMA_541 -> "2";
            case Costants.SCHEMA_532, Costants.SCHEMA_451 -> "1";
            case Costants.SCHEMA_433 -> "-1";
            case Costants.SCHEMA_343 -> "-2";
            default -> "0";
        };
    }

    private HorizontalLayout buildBaseCellLayout(FcGiornataDett gd) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        applyTextColor(cellLayout, gd);
        return cellLayout;
    }

    private void applyTextColor(Component component, FcGiornataDett gd) {
        if (ATTIVO_SI.equals(gd.getFlagAttivo())) {
            component.getStyle().set("color", Costants.GRAY);
        } else {
            component.getStyle().set("color", Costants.LIGHT_GRAY);
        }
    }

    private void addRepeatedIcons(HorizontalLayout layout, String imageName, Integer count) {
        int safeCount = count == null ? 0 : count;
        for (int i = 0; i < safeCount; i++) {
            layout.add(buildImage(imageName));
        }
    }

    private boolean hasPortiereImbattuto(FcGiornataDett gd) {
        return gd.getFcGiocatore() != null
                && gd.getFcPagelle() != null
                && Costants.P.equals(gd.getFcGiocatore().getFcRuolo().getIdRuolo())
                && gd.getFcPagelle().getGoalSubito() == 0
                && gd.getFcPagelle().getEspulsione() == 0
                && gd.getFcPagelle().getVotoGiocatore() != 0
                && gd.getFcPagelle().getG() != 0
                && gd.getFcPagelle().getCs() != 0
                && gd.getFcPagelle().getTs() != 0;
    }

    private boolean hasSecondoCambioMalus(FcGiornataDett gd) {
        return ATTIVO_SI.equals(gd.getFlagAttivo())
                && (gd.getOrdinamento() == 14 || gd.getOrdinamento() == 16 || gd.getOrdinamento() == 18);
    }

    private double calculateMalusSecondoCambio(List<FcGiornataDett> itemsPanchina) {
        double malus = 0.0;

        for (FcGiornataDett gd : itemsPanchina) {
            if (hasSecondoCambioMalus(gd)) {
                malus += 0.5;
            }
        }

        return malus;
    }

    private String getBonusQuarti(FcClassifica classifica) {
        if (classifica == null || classifica.getIdPosiz() == 0) {
            return "0";
        }

        return switch (classifica.getIdPosiz()) {
            case 1 -> "8";
            case 2 -> "6";
            case 3 -> "4";
            case 4 -> "2";
            default -> "0";
        };
    }

    private FcProperties property(String key, String value) {
        FcProperties property = new FcProperties();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    private String formatDoubleValue(Number value, NumberFormat formatter) {
        if (value == null) {
            return "0";
        }
        return formatter.format(value.doubleValue() / Costants.DIVISORE_100);
    }

    private Span buildSummarySpan(String text, String fontSize, String background) {
        Span span = new Span(text);
        span.getStyle().set(Costants.FONT_SIZE, fontSize);
        span.getStyle().set(Costants.BACKGROUND, background);
        span.setSizeFull();
        return span;
    }

    private Image buildImage(String imageName) {
        return Utils.buildImage(imageName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imageName));
    }

    private Image buildScoreImage(Integer goal) {
        String imageName = (goal == null ? 0 : goal) + ".png";
        return Utils.buildImage(imageName, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "number/" + imageName));
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }

    private boolean isStarter(FcGiornataDett gd) {
        return gd.getOrdinamento() < 12;
    }

    private boolean isBench(FcGiornataDett gd) {
        return gd.getOrdinamento() > 11 && gd.getOrdinamento() < 19;
    }

    private enum LegendIcon {
        AMMONIZIONE,
        ESPULSIONE,
        ASSIST,
        AUTOGOL,
        ENTRATO,
        GOAL_FATTO,
        GOAL_SUBITO,
        USCITO,
        RIGORE_SBAGLIATO,
        RIGORE_SEGNATO,
        RIGORE_PARATO,
        BONUS_PORTIERE
    }

    private record TeamData(
            List<FcGiornataDett> items,
            List<FcGiornataDett> titolari,
            List<FcGiornataDett> panchina,
            List<FcGiornataDett> tribuna,
            String schema) {
    }

    private record FormationCounter(int defenders, int midfielders, int attackers) {
    }

    private record MatchTeamSection(String teamName, VerticalLayout content) {
    }
}
