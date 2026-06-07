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
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CampionatoService;
import fcapp.backend.service.ClassificaService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Classifica")
@Route(value = "classificaAdmin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcClassificaView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcClassificaView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";

    private static final String FIELD_ID = "id";
    private static final String FIELD_FC_CAMPIONATO = "fcCampionato";
    private static final String FIELD_FC_ATTORE = "fcAttore";
    private static final String FIELD_PUNTI = "punti";
    private static final String FIELD_ID_POSIZ = "idPosiz";
    private static final String FIELD_ID_POSIZ_FINAL = "idPosizFinal";
    private static final String FIELD_TOT_PUNTI = "totPunti";
    private static final String FIELD_TOT_PUNTI_OLD = "totPuntiOld";
    private static final String FIELD_TOT_PUNTI_ROSA = "totPuntiRosa";

    private final transient ClassificaService classificaService;
    private final transient AttoreService attoreService;
    private final transient CampionatoService campionatoService;
    private final transient AccessoService accessoService;

    private final ComboBox<FcCampionato> campionatoFilter = new ComboBox<>("Campionato");

    public FcClassificaView(
            ClassificaService classificaService,
            AttoreService attoreService,
            CampionatoService campionatoService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcClassificaView.class.getSimpleName());
        this.classificaService = classificaService;
        this.attoreService = attoreService;
        this.campionatoService = campionatoService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcClassificaView.class.getSimpleName());

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

    private GridCrud<FcClassifica> buildCrud() {
        GridCrud<FcClassifica> crud =
                new GridCrud<>(FcClassifica.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureFilter(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Classifica(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcClassifica> crud) {
        DefaultCrudFormFactory<FcClassifica> formFactory =
                new DefaultCrudFormFactory<>(FcClassifica.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        String[] detailFields = {
                FIELD_ID,
                FIELD_FC_CAMPIONATO,
                FIELD_FC_ATTORE,
                FIELD_PUNTI,
                FIELD_ID_POSIZ,
                FIELD_ID_POSIZ_FINAL,
                FIELD_TOT_PUNTI,
                FIELD_TOT_PUNTI_OLD,
                FIELD_TOT_PUNTI_ROSA
        };

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_FC_CAMPIONATO,
                FIELD_FC_ATTORE,
                FIELD_PUNTI);

        List<FcCampionato> campionati = campionatoService.findAll();
        List<FcAttore> attori = attoreService.findByActive(true);

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_CAMPIONATO,
                new ComboBoxProvider<>(
                        "Campionato",
                        campionati,
                        new TextRenderer<>(FcCampionato::getDescCampionato),
                        FcCampionato::getDescCampionato));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_ATTORE,
                new ComboBoxProvider<>(
                        "Attore",
                        attori,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));
    }

    private void configureGrid(GridCrud<FcClassifica> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getId() != null
                                ? String.valueOf(item.getId().getIdCampionato())
                                : ""))
                .setHeader("Id");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcCampionato() != null
                                ? item.getFcCampionato().getDescCampionato()
                                : ""))
                .setHeader("Campionato");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcAttore() != null
                                ? item.getFcAttore().getDescAttore()
                                : ""))
                .setHeader("Attore");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getPunti()) : ""))
                .setHeader("Punti");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getIdPosiz()) : ""))
                .setHeader("idPosiz");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getIdPosizFinal()) : ""))
                .setHeader("idPosizFinal");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getTotPunti() != null
                                ? item.getTotPunti().toString()
                                : ""))
                .setHeader("TotPunti");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getTotPuntiOld() != null
                                ? item.getTotPuntiOld().toString()
                                : ""))
                .setHeader("TotPunti Old");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getTotPuntiRosa() != null
                                ? item.getTotPuntiRosa().toString()
                                : ""))
                .setHeader("TotPunti Rosa");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureFilter(GridCrud<FcClassifica> crud) {
        List<FcCampionato> campionati = campionatoService.findAll();

        campionatoFilter.setItems(campionati);
        campionatoFilter.setItemLabelGenerator(FcCampionato::getDescCampionato);
        campionatoFilter.setClearButtonVisible(true);
        campionatoFilter.addValueChangeListener(event -> crud.refreshGrid());

        FcCampionato currentCampionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);
        if (currentCampionato != null) {
            campionatoFilter.setValue(currentCampionato);
        }

        Button clearFilters = new Button("clear");
        clearFilters.addClickListener(event -> campionatoFilter.clear());

        crud.getCrudLayout().addFilterComponent(campionatoFilter);
        crud.getCrudLayout().addFilterComponent(clearFilters);
    }

    private void configureOperations(GridCrud<FcClassifica> crud) {
        crud.setFindAllOperation(() ->
                classificaService.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionatoFilter.getValue()));
        crud.setAddOperation(classificaService::save);
        crud.setUpdateOperation(classificaService::save);
        crud.setDeleteOperation(classificaService::delete);
    }
}
