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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcStatoGiocatore;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.StatoGiocatoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("GiornataDett")
@Route(value = "giornataDett", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataDettView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcGiornataDettView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String TYPE_SERIE_A = "1";
    private static final String CLEAR_LABEL = "clear";

    private static final String FIELD_ORDINAMENTO = "ordinamento";
    private static final String FIELD_FC_GIORNATA_INFO = "fcGiornataInfo";
    private static final String FIELD_FC_ATTORE = "fcAttore";
    private static final String FIELD_FC_GIOCATORE = "fcGiocatore";
    private static final String FIELD_FC_STATO_GIOCATORE = "fcStatoGiocatore";
    private static final String FIELD_VOTO = "voto";
    private static final String FIELD_FLAG_ATTIVO = "flagAttivo";

    private final transient AttoreService attoreService;
    private final transient GiornataInfoService giornataInfoService;
    private final transient GiocatoreService giocatoreService;
    private final transient StatoGiocatoreService statoGiocatoreService;
    private final transient GiornataDettService giornataDettService;
    private final transient AccessoService accessoService;

    private final ComboBox<FcAttore> attoreFilter = new ComboBox<>("Attore");
    private final ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>("Giornata");

    public FcGiornataDettView(
            AttoreService attoreService,
            GiornataInfoService giornataInfoService,
            GiocatoreService giocatoreService,
            StatoGiocatoreService statoGiocatoreService,
            GiornataDettService giornataDettService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcGiornataDettView.class.getSimpleName());
        this.attoreService = attoreService;
        this.giornataInfoService = giornataInfoService;
        this.giocatoreService = giocatoreService;
        this.statoGiocatoreService = statoGiocatoreService;
        this.giornataDettService = giornataDettService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcGiornataDettView.class.getSimpleName());

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

    private GridCrud<FcGiornataDett> buildCrud() {
        GridCrud<FcGiornataDett> crud =
                new GridCrud<>(FcGiornataDett.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureFilters(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Giornata(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcGiornataDett> crud) {
        DefaultCrudFormFactory<FcGiornataDett> formFactory =
                new DefaultCrudFormFactory<>(FcGiornataDett.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ORDINAMENTO,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE,
                FIELD_FC_STATO_GIOCATORE,
                FIELD_VOTO,
                FIELD_FLAG_ATTIVO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ORDINAMENTO,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE,
                FIELD_FC_STATO_GIOCATORE,
                FIELD_VOTO,
                FIELD_FLAG_ATTIVO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ORDINAMENTO,
                FIELD_FC_STATO_GIOCATORE,
                FIELD_VOTO,
                FIELD_FLAG_ATTIVO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ORDINAMENTO,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE);

        List<FcGiornataInfo> giornate = giornataInfoService.findAll();
        List<FcAttore> attori = attoreService.findByActive(true);
        List<FcGiocatore> giocatori = giocatoreService.findAll();
        List<FcStatoGiocatore> stati = statoGiocatoreService.findAll();

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_GIORNATA_INFO,
                new ComboBoxProvider<>(
                        "Giornata",
                        giornate,
                        new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),
                        FcGiornataInfo::getDescGiornataFc));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_ATTORE,
                new ComboBoxProvider<>(
                        "Attore",
                        attori,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_GIOCATORE,
                new ComboBoxProvider<>(
                        Costants.GIOCATORE,
                        giocatori,
                        new TextRenderer<>(FcGiocatore::getCognGiocatore),
                        FcGiocatore::getCognGiocatore));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_STATO_GIOCATORE,
                new ComboBoxProvider<>(
                        "Stato",
                        stati,
                        new TextRenderer<>(FcStatoGiocatore::getDescStatoGiocatore),
                        FcStatoGiocatore::getDescStatoGiocatore));
    }

    private void configureGrid(GridCrud<FcGiornataDett> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiornataInfo() != null
                                ? item.getFcGiornataInfo().getDescGiornataFc()
                                : ""))
                .setHeader("Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getOrdinamento()) : ""))
                .setHeader("Ordinamento");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcAttore() != null
                                ? item.getFcAttore().getDescAttore()
                                : ""))
                .setHeader("Attore");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiocatore() != null
                                ? item.getFcGiocatore().getCognGiocatore()
                                : ""))
                .setHeader(Costants.GIOCATORE);

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcStatoGiocatore() != null
                                ? item.getFcStatoGiocatore().getDescStatoGiocatore()
                                : ""))
                .setHeader("Stato");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getVoto() != null
                                ? item.getVoto().toString()
                                : ""))
                .setHeader("Voto");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFlagAttivo() != null
                                ? item.getFlagAttivo()
                                : ""))
                .setHeader("Attivo");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureFilters(GridCrud<FcGiornataDett> crud) {
        FcCampionato campionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);

        giornataInfoFilter.setItems(giornataInfoService.findAll());
        giornataInfoFilter.setItemLabelGenerator(giornata ->
                isSerieACompetition(campionato)
                        ? Utils.buildInfoGiornata(giornata)
                        : Utils.buildInfoGiornataEm(giornata, campionato));
        giornataInfoFilter.setClearButtonVisible(true);
        giornataInfoFilter.addValueChangeListener(event -> crud.refreshGrid());

        attoreFilter.setItems(attoreService.findByActive(true));
        attoreFilter.setItemLabelGenerator(FcAttore::getDescAttore);
        attoreFilter.setClearButtonVisible(true);
        attoreFilter.addValueChangeListener(event -> crud.refreshGrid());

        Button clearFilters = new Button(CLEAR_LABEL);
        clearFilters.addClickListener(event -> {
            giornataInfoFilter.clear();
            attoreFilter.clear();
        });

        crud.getCrudLayout().addFilterComponent(giornataInfoFilter);
        crud.getCrudLayout().addFilterComponent(attoreFilter);
        crud.getCrudLayout().addFilterComponent(clearFilters);
    }

    private void configureOperations(GridCrud<FcGiornataDett> crud) {
        crud.setFindAllOperation(() ->
                giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(
                        attoreFilter.getValue(),
                        giornataInfoFilter.getValue()));
        crud.setAddOperation(giornataDettService::save);
        crud.setUpdateOperation(giornataDettService::save);
        crud.setDeleteOperation(giornataDettService::delete);
    }

    private boolean isSerieACompetition(FcCampionato campionato) {
        return campionato != null && TYPE_SERIE_A.equals(campionato.getType());
    }
}
