package fcapp.ui.views.admin;

import java.io.Serial;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.PagelleService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Pagelle")
@Route(value = "pagelle", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcPagelleView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcPagelleView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String TYPE_SERIE_A = "1";
    private static final String CLEAR_LABEL = "clear";

    private static final String FIELD_FC_GIORNATA_INFO = "fcGiornataInfo";
    private static final String FIELD_FC_GIOCATORE = "fcGiocatore";
    private static final String FIELD_AMMONIZIONE = "ammonizione";
    private static final String FIELD_ASSIST = "assist";
    private static final String FIELD_AUTORETE = "autorete";
    private static final String FIELD_CS = "cs";
    private static final String FIELD_ESPULSIONE = "espulsione";
    private static final String FIELD_G = "g";
    private static final String FIELD_GOAL_REALIZZATO = "goalRealizzato";
    private static final String FIELD_GOAL_SUBITO = "goalSubito";
    private static final String FIELD_RIGORE_FALLITO = "rigoreFallito";
    private static final String FIELD_RIGORE_PARATO = "rigoreParato";
    private static final String FIELD_RIGORE_SEGNATO = "rigoreSegnato";
    private static final String FIELD_TS = "ts";
    private static final String FIELD_VOTO_GIOCATORE = "votoGiocatore";
    private static final String FIELD_GDV = "gdv";

    private final transient GiornataInfoService giornataInfoService;
    private final transient GiocatoreService giocatoreService;
    private final transient PagelleService pagelleService;
    private final transient AccessoService accessoService;

    private final ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>("Giornata");
    private final ComboBox<FcGiocatore> giocatoreFilter = new ComboBox<>(Costants.GIOCATORE);

    public FcPagelleView(
            GiornataInfoService giornataInfoService,
            GiocatoreService giocatoreService,
            PagelleService pagelleService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcPagelleView.class.getSimpleName());
        this.giornataInfoService = giornataInfoService;
        this.giocatoreService = giocatoreService;
        this.pagelleService = pagelleService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcPagelleView.class.getSimpleName());

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        configureLayout();
        add(buildCrud());
    }

    private void configureLayout() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();
    }

    private GridCrud<FcPagelle> buildCrud() {
        GridCrud<FcPagelle> crud =
                new GridCrud<>(FcPagelle.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureFilters(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Pagelle(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcPagelle> crud) {
        DefaultCrudFormFactory<FcPagelle> formFactory =
                new DefaultCrudFormFactory<>(FcPagelle.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        String[] visibleFields = {
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_GIOCATORE,
                FIELD_AMMONIZIONE,
                FIELD_ASSIST,
                FIELD_AUTORETE,
                FIELD_CS,
                FIELD_ESPULSIONE,
                FIELD_G,
                FIELD_GOAL_REALIZZATO,
                FIELD_GOAL_SUBITO,
                FIELD_RIGORE_FALLITO,
                FIELD_RIGORE_PARATO,
                FIELD_RIGORE_SEGNATO,
                FIELD_TS,
                FIELD_VOTO_GIOCATORE,
                FIELD_GDV
        };

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, visibleFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, visibleFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, visibleFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, visibleFields);

        List<FcGiornataInfo> giornate = giornataInfoService.findAll();
        List<FcGiocatore> giocatori = giocatoreService.findAll();

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_GIORNATA_INFO,
                new ComboBoxProvider<>(
                        "Giornata",
                        giornate,
                        new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),
                        FcGiornataInfo::getDescGiornataFc));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_GIOCATORE,
                new ComboBoxProvider<>(
                        Costants.GIOCATORE,
                        giocatori,
                        new TextRenderer<>(FcGiocatore::getCognGiocatore),
                        FcGiocatore::getCognGiocatore));
    }

    private void configureGrid(GridCrud<FcPagelle> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiornataInfo() != null
                                ? item.getFcGiornataInfo().getDescGiornataFc()
                                : ""))
                .setHeader("Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiocatore() != null
                                ? item.getFcGiocatore().getCognGiocatore()
                                : ""))
                .setHeader(Costants.GIOCATORE);

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getVotoGiocatore()) : ""))
                .setHeader("Voto");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getG()) : ""))
                .setHeader("G");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getTs()) : ""))
                .setHeader("Ts");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getCs()) : ""))
                .setHeader("Cs");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureFilters(GridCrud<FcPagelle> crud) {
        FcCampionato campionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);

        giornataInfoFilter.setItems(giornataInfoService.findAll());
        giornataInfoFilter.setItemLabelGenerator(giornata ->
                isSerieACompetition(campionato)
                        ? Utils.buildInfoGiornata(giornata)
                        : Utils.buildInfoGiornataEm(giornata, campionato));
        giornataInfoFilter.setClearButtonVisible(true);
        giornataInfoFilter.addValueChangeListener(event -> crud.refreshGrid());

        giocatoreFilter.setItems(giocatoreService.findAll());
        giocatoreFilter.setItemLabelGenerator(FcGiocatore::getCognGiocatore);
        giocatoreFilter.setClearButtonVisible(true);
        giocatoreFilter.setRenderer(new ComponentRenderer<>(this::buildGiocatoreRenderer));
        giocatoreFilter.addValueChangeListener(event -> crud.refreshGrid());

        Button clearFilters = new Button(CLEAR_LABEL);
        clearFilters.addClickListener(event -> {
            giornataInfoFilter.clear();
            giocatoreFilter.clear();
        });

        crud.getCrudLayout().addFilterComponent(giornataInfoFilter);
        crud.getCrudLayout().addFilterComponent(giocatoreFilter);
        crud.getCrudLayout().addFilterComponent(clearFilters);
    }

    private VerticalLayout buildGiocatoreRenderer(FcGiocatore giocatore) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(false);

        if (giocatore == null) {
            return container;
        }

        Span nome = new Span(giocatore.getCognGiocatore());
        container.add(nome);

        String ruoloSquadra = "";
        if (giocatore.getFcRuolo() != null && giocatore.getFcSquadra() != null) {
            ruoloSquadra =
                    giocatore.getFcRuolo().getIdRuolo() + " - " + giocatore.getFcSquadra().getNomeSquadra();
        } else if (giocatore.getFcRuolo() != null) {
            ruoloSquadra = giocatore.getFcRuolo().getIdRuolo();
        } else if (giocatore.getFcSquadra() != null) {
            ruoloSquadra = giocatore.getFcSquadra().getNomeSquadra();
        }

        Span dettaglio = new Span(ruoloSquadra);
        dettaglio.getStyle().set("font-size", "smaller");
        container.add(dettaglio);

        Span quotazione = new Span("Q " + giocatore.getQuotazione());
        quotazione.getStyle().set("font-size", "smaller");
        container.add(quotazione);

        return container;
    }

    private void configureOperations(GridCrud<FcPagelle> crud) {
        crud.setFindAllOperation(() ->
                pagelleService.findByCustonm(giornataInfoFilter.getValue(), giocatoreFilter.getValue()));
        crud.setAddOperation(pagelleService::save);
        crud.setUpdateOperation(pagelleService::save);
        crud.setDeleteOperation(pagelleService::delete);
    }

    private boolean isSerieACompetition(FcCampionato campionato) {
        return campionato != null && TYPE_SERIE_A.equals(campionato.getType());
    }
}
