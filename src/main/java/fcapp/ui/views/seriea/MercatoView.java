package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.SquadraService;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "mercato")
@PageTitle("Mercato")
@RolesAllowed("ADMIN")
public class MercatoView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String BG_ERROR = "#EC7063";
    private static final String BG_HEADER = "#D2E6F0";
    private static final String BG_CREDITO = "#F5E37F";
    private static final String BG_PAGATO = "#D7DBDD";
    private static final String BG_RESIDUO_OK = "#ABEBC6";
    private static final String BG_RUOLI = "#AED6F1";

    private static final int MIN_RANDOM_QUOTAZIONE = 5;
    private static final int BASE_CREDITI = 500;

    private static final int REQUIRED_P = 3;
    private static final int REQUIRED_D = 8;
    private static final int REQUIRED_C = 8;
    private static final int REQUIRED_A = 6;

    private final transient Logger log = LoggerFactory.getLogger(getClass());

    private final transient JdbcTemplate jdbcTemplate;
    private final transient AttoreService attoreService;
    private final transient GiocatoreService giocatoreService;
    private final transient FormazioneService formazioneService;
    private final transient ClassificaService classificaService;
    private final transient AccessoService accessoService;
    private final transient SquadraService squadraService;

    private String idCampionato;

    private Button randomSaveButton;
    private Button saveButton;
    private Span lblError;

    private Grid<FcFormazione>[] tablePlayer;
    private Span[] lblCreditoPlayer;
    private Span[] lblTotPagatoPlayer;
    private Span[] lblResiduoPlayer;
    private Span[] lblRuoliPlayer;
    private Grid<FcProperties>[] tableContaPlayer;

    private List<FcAttore> squadre = new ArrayList<>();
    private List<FcGiocatore> giocatori = new ArrayList<>();
    private List<FcClassifica> creditiFm = new ArrayList<>();

    public MercatoView(
            JdbcTemplate jdbcTemplate,
            AttoreService attoreService,
            GiocatoreService giocatoreService,
            FormazioneService formazioneService,
            ClassificaService classificaService,
            AccessoService accessoService,
            SquadraService squadraService) {
        this.jdbcTemplate = jdbcTemplate;
        this.attoreService = attoreService;
        this.giocatoreService = giocatoreService;
        this.formazioneService = formazioneService;
        this.classificaService = classificaService;
        this.accessoService = accessoService;
        this.squadraService = squadraService;
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
        log.info("initData");

        squadre = attoreService.findByActive(true);
        giocatori = giocatoreService.findAll();

        FcCampionato campionato = getCampionatoFromSession();
        if (campionato != null) {
            idCampionato = String.valueOf(campionato.getIdCampionato());
            creditiFm = classificaService.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);
        }
    }

    @SuppressWarnings("unchecked")
    private void initLayout() {
        try {
            add(buildTopActions());

            if (giocatori.isEmpty()) {
                return;
            }

            HorizontalLayout headerLayout = createCompactRow();
            HorizontalLayout playerTablesLayout = createCompactRow();
            HorizontalLayout infoLayout = createCompactRow();
            HorizontalLayout counterTablesLayout = createCompactRow();

            Span[] lblAttore = new Span[squadre.size()];
            tablePlayer = new Grid[squadre.size()];
            lblRuoliPlayer = new Span[squadre.size()];
            lblCreditoPlayer = new Span[squadre.size()];
            lblTotPagatoPlayer = new Span[squadre.size()];
            lblResiduoPlayer = new Span[squadre.size()];
            tableContaPlayer = new Grid[squadre.size()];

            for (int index = 0; index < squadre.size(); index++) {
                FcAttore attore = squadre.get(index);

                lblAttore[index] = buildAttoreLabel(attore);
                headerLayout.add(wrap(lblAttore[index]));

                tablePlayer[index] = buildTable(attore);
                playerTablesLayout.add(tablePlayer[index]);

                VerticalLayout summary = buildSummaryLayout(index);
                infoLayout.add(summary);

                tableContaPlayer[index] = buildTableContaPlayer();
                counterTablesLayout.add(tableContaPlayer[index]);
            }

            add(headerLayout, playerTablesLayout, infoLayout, counterTablesLayout);
            updateInfoAttore();

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private HorizontalLayout buildTopActions() {
        Button homeButton = new Button("Home");
        RouterLink menuHome = new RouterLink("", HomeView.class);
        menuHome.getElement().appendChild(homeButton.getElement());

        Button freePlayersButton = new Button("FreePlayers");
        RouterLink menuFreePlayers = new RouterLink("", FreePlayersView.class);
        menuFreePlayers.getElement().appendChild(freePlayersButton.getElement());

        saveButton = new Button("Save");
        saveButton.addClickListener(this);
        saveButton.setEnabled(!giocatori.isEmpty());

        randomSaveButton = new Button("Random Save");
        randomSaveButton.addClickListener(this);
        randomSaveButton.setVisible(true);

        lblError = new Span();
        lblError.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        lblError.getStyle().set(Costants.BACKGROUND, BG_ERROR);
        lblError.setVisible(false);

        HorizontalLayout layout = new HorizontalLayout(menuHome, menuFreePlayers, saveButton, randomSaveButton, lblError);
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layout.setSpacing(true);

        return layout;
    }

    private HorizontalLayout createCompactRow() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        return layout;
    }

    private VerticalLayout wrap(Span component) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.add(component);
        return layout;
    }

    private Span buildAttoreLabel(FcAttore attore) {
        Span label = new Span(attore.getDescAttore());
        label.setWidth(Costants.WIDTH_205);
        label.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        label.getStyle().set(Costants.BACKGROUND, BG_HEADER);
        return label;
    }

    private VerticalLayout buildSummaryLayout(int index) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);

        lblCreditoPlayer[index] = buildInfoLabel("Credito", BG_CREDITO);
        lblTotPagatoPlayer[index] = buildInfoLabel("Pagato", BG_PAGATO);
        lblResiduoPlayer[index] = buildInfoLabel("Residuo", BG_RESIDUO_OK);
        lblRuoliPlayer[index] = buildInfoLabel("P D C A", BG_RUOLI);

        layout.add(
                lblCreditoPlayer[index],
                lblTotPagatoPlayer[index],
                lblResiduoPlayer[index],
                lblRuoliPlayer[index]);

        return layout;
    }

    private Span buildInfoLabel(String text, String background) {
        Span label = new Span(text);
        label.setWidth(Costants.WIDTH_205);
        label.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        label.getStyle().set(Costants.BACKGROUND, background);
        return label;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            if (event.getSource() == randomSaveButton) {
                openRandomConfirmDialog();
                return;
            }

            if (event.getSource() == saveButton) {
                saveFormazioni();
                CustomMessageDialog.showMessageInfo("Formazioni aggiornate con successo!");
            }
        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }
    }

    private void openRandomConfirmDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(CustomMessageDialog.TITLE_MSG_CONFIRM);
        dialog.setText("Confermi inserimento giocatori random?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        dialog.setRejectable(false);
        dialog.setConfirmText("Conferma");
        dialog.addConfirmListener(e -> {
            randomFormazioni();
            CustomMessageDialog.showMessageInfo("Formazioni aggiornate con successo!");
        });
        dialog.open();
    }

    private void saveFormazioni() {
        for (int index = 0; index < squadre.size(); index++) {
            FcAttore attore = squadre.get(index);
            if (!attore.isActive()) {
                continue;
            }

            List<FcFormazione> formazioneList = getGridItems(tablePlayer[index]);
            for (FcFormazione formazione : formazioneList) {
                updateFormazione(attore.getIdAttore(), formazione);
            }
        }
    }

    private void randomFormazioni() {
        Random rand = new Random();

        Map<String, List<Integer>> giocatoriPerRuolo = giocatori.stream()
                .filter(g -> g.getQuotazione() >= MIN_RANDOM_QUOTAZIONE)
                .collect(Collectors.groupingBy(
                        g -> g.getFcRuolo().getIdRuolo(),
                        Collectors.mapping(FcGiocatore::getIdGiocatore, Collectors.toList())));

        List<Integer> portieri = giocatoriPerRuolo.getOrDefault(Costants.P, List.of());
        List<Integer> difensori = giocatoriPerRuolo.getOrDefault(Costants.D, List.of());
        List<Integer> centrocampisti = giocatoriPerRuolo.getOrDefault(Costants.C, List.of());
        List<Integer> attaccanti = giocatoriPerRuolo.getOrDefault(Costants.A, List.of());

        for (FcAttore attore : squadre) {
            List<Integer> selectedPlayers = new ArrayList<>();
            selectedPlayers.addAll(extractRandomDistinct(portieri, REQUIRED_P, rand));
            selectedPlayers.addAll(extractRandomDistinct(difensori, REQUIRED_D, rand));
            selectedPlayers.addAll(extractRandomDistinct(centrocampisti, REQUIRED_C, rand));
            selectedPlayers.addAll(extractRandomDistinct(attaccanti, REQUIRED_A, rand));

            for (int ordinamento = 0; ordinamento < selectedPlayers.size(); ordinamento++) {
                updateFormazioneRandom(attore.getIdAttore(), ordinamento + 1, selectedPlayers.get(ordinamento));
            }
        }
    }

    private List<Integer> extractRandomDistinct(List<Integer> source, int requiredSize, Random rand) {
        if (source.size() < requiredSize) {
            throw new IllegalStateException("Giocatori insufficienti per completare l'estrazione casuale.");
        }

        Set<Integer> selected = new LinkedHashSet<>();
        while (selected.size() < requiredSize) {
            selected.add(source.get(rand.nextInt(source.size())));
        }
        return new ArrayList<>(selected);
    }

    private void updateFormazioneRandom(int idAttore, int ordinamento, Integer idGiocatore) {
        String update = """
                UPDATE fc_formazione
                   SET ID_GIOCATORE = ?,
                       TOT_PAGATO = ?
                 WHERE ID_CAMPIONATO = ?
                   AND ID_ATTORE = ?
                   AND ORDINAMENTO = ?
                """;

        jdbcTemplate.update(update, idGiocatore, 1, idCampionato, idAttore, ordinamento);
    }

    private void updateFormazione(int idAttore, FcFormazione formazione) {
        String updateWithPlayer = """
                UPDATE fc_formazione
                   SET ID_GIOCATORE = ?,
                       TOT_PAGATO = ?
                 WHERE ID_CAMPIONATO = ?
                   AND ID_ATTORE = ?
                   AND ORDINAMENTO = ?
                """;

        String updateWithoutPlayer = """
                UPDATE fc_formazione
                   SET ID_GIOCATORE = NULL,
                       TOT_PAGATO = NULL
                 WHERE ID_CAMPIONATO = ?
                   AND ID_ATTORE = ?
                   AND ORDINAMENTO = ?
                """;

        FcGiocatore giocatore = formazione.getFcGiocatore();
        Integer ordinamento = formazione.getId().getOrdinamento();

        if (giocatore != null && formazione.getTotPagato() != null) {
            jdbcTemplate.update(
                    updateWithPlayer,
                    giocatore.getIdGiocatore(),
                    formazione.getTotPagato(),
                    idCampionato,
                    idAttore,
                    ordinamento);
            return;
        }

        jdbcTemplate.update(updateWithoutPlayer, idCampionato, idAttore, ordinamento);
    }

    private void updateInfoAttore() {
        log.info("START updateInfoAttore");

        StringBuilder descError = new StringBuilder();

        for (int index = 0; index < tablePlayer.length; index++) {
            FcAttore attore = squadre.get(index);
            List<FcFormazione> data = getGridItems(tablePlayer[index]);

            AttoreSummary summary = calculateSummary(attore, data);
            updateSummaryLabels(index, summary);
            updateCounterTable(index, summary.squadreList);

            appendValidationErrors(descError, summary);
        }

        applyValidationState(descError);

        log.info("END updateInfoAttore");
    }

    private AttoreSummary calculateSummary(FcAttore attore, List<FcFormazione> data) {
        AttoreSummary summary = new AttoreSummary();
        summary.descAttore = "[" + attore.getDescAttore() + "]";
        summary.credito = getTotCrediti(attore.getIdAttore());

        for (FcFormazione formazione : data) {
            FcGiocatore giocatore = formazione.getFcGiocatore();
            if (giocatore == null || formazione.getTotPagato() == null) {
                continue;
            }

            summary.pagato += formazione.getTotPagato();
            incrementRoleCounter(summary, giocatore.getFcRuolo().getIdRuolo());
            incrementSquadraCounter(summary.countBySquadra, giocatore.getFcSquadra().getNomeSquadra());
        }

        summary.residuo = summary.credito - summary.pagato;
        summary.squadreList = toPropertiesList(summary.countBySquadra);

        for (Map.Entry<String, Integer> entry : summary.countBySquadra.entrySet()) {
            if (entry.getValue() <= 5) {
                continue;
            }

            int extraNonPortieri = getExtraNonPortieriForSquadra(data, entry.getKey(), entry.getValue());
            if (extraNonPortieri > 5) {
                summary.errors.add(summary.descAttore + " Troppi giocatori per la squadra " + entry.getKey());
            }
        }

        if (summary.residuo < 0) {
            summary.errors.add(summary.descAttore + " Residuo minore di 0 - Residuo attuale " + summary.residuo);
        }

        return summary;
    }

    private int getTotCrediti(int idAttore) {
        for (FcClassifica fc : creditiFm) {
            if (fc.getFcAttore().getIdAttore() == idAttore) {
                return BASE_CREDITI + fc.getTotFm();
            }
        }
        return 0;
    }

    private void incrementRoleCounter(AttoreSummary summary, String ruolo) {
        switch (ruolo) {
            case Costants.P -> summary.countP++;
            case Costants.D -> summary.countD++;
            case Costants.C -> summary.countC++;
            case Costants.A -> summary.countA++;
            default -> {
                // nessuna azione
            }
        }
    }

    private void incrementSquadraCounter(Map<String, Integer> map, String squadra) {
        map.merge(squadra, 1, Integer::sum);
    }

    private List<FcProperties> toPropertiesList(Map<String, Integer> countBySquadra) {
        return countBySquadra.entrySet().stream()
                .map(entry -> {
                    FcProperties properties = new FcProperties();
                    properties.setKey(entry.getKey());
                    properties.setValue(String.valueOf(entry.getValue()));
                    return properties;
                })
                .sorted(Comparator.comparing(FcProperties::getValue, String.CASE_INSENSITIVE_ORDER).reversed())
                .toList();
    }

    private int getExtraNonPortieriForSquadra(List<FcFormazione> data, String squadra, int totalForSquadra) {
        int countPortieri = 0;

        for (FcFormazione formazione : data) {
            FcGiocatore giocatore = formazione.getFcGiocatore();
            if (giocatore == null || formazione.getTotPagato() == null) {
                continue;
            }

            boolean sameSquadra = squadra.equals(giocatore.getFcSquadra().getNomeSquadra());
            boolean isPortiere = Costants.P.equals(giocatore.getFcRuolo().getIdRuolo());

            if (sameSquadra && isPortiere) {
                countPortieri++;
            }
        }

        return totalForSquadra - countPortieri;
    }

    private void updateSummaryLabels(int index, AttoreSummary summary) {
        lblCreditoPlayer[index].setText("Credito  = " + summary.credito);
        lblTotPagatoPlayer[index].setText("Pagato   = " + summary.pagato);
        lblResiduoPlayer[index].setText("Residuo  = " + summary.residuo);
        lblRuoliPlayer[index].setText(
                "P=" + summary.countP +
                " D=" + summary.countD +
                " C=" + summary.countC +
                " A=" + summary.countA);

        lblResiduoPlayer[index].getStyle().set(Costants.BACKGROUND, summary.residuo < 0 ? BG_ERROR : BG_RESIDUO_OK);
    }

    private void updateCounterTable(int index, List<FcProperties> list) {
        tableContaPlayer[index].setItems(list);
        tableContaPlayer[index].getDataProvider().refreshAll();
    }

    private void appendValidationErrors(StringBuilder descError, AttoreSummary summary) {
        for (String error : summary.errors) {
            if (!descError.isEmpty()) {
                descError.append(" - ");
            }
            descError.append(error);
        }
    }

    private void applyValidationState(StringBuilder descError) {
        saveButton.setEnabled(true);
        lblError.setVisible(false);

        if (StringUtils.isNotBlank(descError)) {
            saveButton.setEnabled(false);
            lblError.setText(descError.toString());
            lblError.setVisible(true);
        }
    }

    private Grid<FcFormazione> buildTable(FcAttore attore) {
        FcCampionato campionato = getCampionatoFromSession();
        List<FcFormazione> listFormazione =
                formazioneService.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);

        Grid<FcFormazione> grid = new Grid<>();
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setWidth(Costants.WIDTH_240);
        grid.setItems(listFormazione);

        if (listFormazione.isEmpty()) {
            return grid;
        }

        Binder<FcFormazione> binder = new Binder<>(FcFormazione.class);
        grid.getEditor().setBinder(binder);

        ComboBox<FcGiocatore> giocatoreField = buildGiocatoreEditor(grid);
        IntegerField totPagatoField = buildTotPagatoEditor(grid);

        Column<FcFormazione> cognGiocatoreColumn = grid.addColumn(
                formazione -> formazione.getFcGiocatore() != null ? formazione.getFcGiocatore().getCognGiocatore() : null);
        cognGiocatoreColumn.setKey("fcGiocatore");
        cognGiocatoreColumn.setEditorComponent(giocatoreField);
        binder.bind(giocatoreField, "fcGiocatore");

        Column<FcFormazione> totPagatoColumn = grid.addColumn(f -> f.getFcGiocatore() != null ? f.getTotPagato() : 0);
        totPagatoColumn.setKey("totPagato");
        totPagatoColumn.setEditorComponent(totPagatoField);
        totPagatoColumn.setTextAlign(ColumnTextAlign.END);
        binder.bind(totPagatoField, "totPagato");

        binder.addValueChangeListener(evt -> {
            if (evt.getValue() instanceof FcGiocatore selected) {
                applyDefaultQuotazione(grid, selected);
            }
        });

        binder.addValueChangeListener(event -> {
            grid.getEditor().refresh();
            try {
                updateInfoAttore();
            } catch (Exception e) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
            }
        });

        grid.addItemDoubleClickListener(event -> grid.getEditor().editItem(event.getItem()));

        return grid;
    }

    private ComboBox<FcGiocatore> buildGiocatoreEditor(Grid<FcFormazione> grid) {
        ComboBox<FcGiocatore> giocatore = new ComboBox<>();
        giocatore.setItemLabelGenerator(FcGiocatore::getCognGiocatore);
        giocatore.setClearButtonVisible(true);
        giocatore.setPlaceholder(Costants.GIOCATORE);
        giocatore.setItems(giocatori);
        giocatore.setWidth(Costants.WIDTH_140);
        giocatore.getStyle().set("--vaadin-combo-box-overlay-width", "16em");

        giocatore.setRenderer(new ComponentRenderer<>(g -> {
            VerticalLayout container = new VerticalLayout();
            container.setPadding(false);
            container.setSpacing(false);

            Span c1 = new Span(g.getCognGiocatore());
            Span c2 = new Span(g.getFcRuolo().getIdRuolo() + " - " + g.getFcSquadra().getNomeSquadra());
            Span c3 = new Span("Q " + g.getQuotazione());

            c2.getStyle().set("fontSize", "smaller");
            c3.getStyle().set("fontSize", "smaller");

            container.add(c1, c2, c3);
            return container;
        }));

        giocatore.getElement()
                .addEventListener("keydown", event -> grid.getEditor().cancel())
                .setFilter("event.key === 'Tab' && !event.shiftKey");

        return giocatore;
    }

    private IntegerField buildTotPagatoEditor(Grid<FcFormazione> grid) {
        IntegerField totPagato = new IntegerField();
        totPagato.setMin(0);
        totPagato.setMax(500);
        totPagato.setStepButtonsVisible(true);
        totPagato.setWidth(Costants.WIDTH_100);

        totPagato.getElement()
                .addEventListener("keydown", event -> grid.getEditor().cancel())
                .setFilter("event.key === 'Tab' && event.shiftKey");

        return totPagato;
    }

    private void applyDefaultQuotazione(Grid<FcFormazione> grid, FcGiocatore selectedGiocatore) {
        List<FcFormazione> data = getGridItems(grid);

        for (FcFormazione formazione : data) {
            if (formazione.getFcGiocatore() == null) {
                continue;
            }

            if (formazione.getFcGiocatore().getCognGiocatore().equals(selectedGiocatore.getCognGiocatore())) {
                formazione.setTotPagato(selectedGiocatore.getQuotazione());
                grid.getDataProvider().refreshItem(formazione);
                break;
            }
        }
    }

    private Grid<FcProperties> buildTableContaPlayer() {
        Grid<FcProperties> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setWidth(Costants.WIDTH_240);

        Column<FcProperties> keyColumn = grid.addColumn(new ComponentRenderer<>(property -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);

            if (property != null && property.getKey() != null) {
                FcSquadra squadra = squadraService.findByNomeSquadra(property.getKey());
                if (squadra != null && squadra.getImg() != null) {
                    try {
                        Image img = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    }
                }

                cellLayout.add(new Span(property.getKey()));
            }

            return cellLayout;
        }));
        keyColumn.setSortable(false);
        keyColumn.setAutoWidth(true);

        Column<FcProperties> valueColumn = grid.addColumn(FcProperties::getValue);
        valueColumn.setSortable(false);
        valueColumn.setAutoWidth(true);

        return grid;
    }

    private FcCampionato getCampionatoFromSession() {
        return (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
    }

    private List<FcFormazione> getGridItems(Grid<FcFormazione> grid) {
        return grid.getDataProvider().fetch(new Query<>()).toList();
    }

    private static class AttoreSummary {
        private String descAttore;
        private int credito;
        private int pagato;
        private int residuo;
        private int countP;
        private int countD;
        private int countC;
        private int countA;
        private final Map<String, Integer> countBySquadra = new HashMap<>();
        private List<FcProperties> squadreList = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
    }
}
