package fcapp.ui.views.admin;

import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcAccesso;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CampionatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accesso")
@Route(value = "fcAccesso", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcAccessoView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcAccessoView.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_ATTORE = "fcAttore";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_NOTE = "note";
    private static final String FIELD_CAMPIONATO = "fcCampionato";

    private final transient AccessoService accessoService;
    private final transient AttoreService attoreService;
    private final transient CampionatoService campionatoService;

    public FcAccessoView(
            AccessoService accessoService,
            AttoreService attoreService,
            CampionatoService campionatoService) {
        LOG.info("Initializing {}", FcAccessoView.class.getSimpleName());
        this.accessoService = accessoService;
        this.attoreService = attoreService;
        this.campionatoService = campionatoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcAccessoView.class.getSimpleName());

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

    private GridCrud<FcAccesso> buildCrud() {
        GridCrud<FcAccesso> crud = new GridCrud<>(FcAccesso.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Accesso(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcAccesso> crud) {
        DefaultCrudFormFactory<FcAccesso> formFactory = new DefaultCrudFormFactory<>(FcAccesso.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID, FIELD_ATTORE, FIELD_DATA, FIELD_NOTE, FIELD_CAMPIONATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID, FIELD_ATTORE, FIELD_DATA, FIELD_NOTE, FIELD_CAMPIONATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID, FIELD_ATTORE, FIELD_DATA, FIELD_NOTE, FIELD_CAMPIONATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID, FIELD_ATTORE);

        List<FcAttore> attoriAttivi = attoreService.findByActive(true);
        List<FcCampionato> campionati = campionatoService.findAll();

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_ATTORE,
                new ComboBoxProvider<>(
                        "Attore",
                        attoriAttivi,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_DATA,
                field -> new DateTimePicker());

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_CAMPIONATO,
                new ComboBoxProvider<>(
                        "Campionato",
                        campionati,
                        new TextRenderer<>(FcCampionato::getDescCampionato),
                        FcCampionato::getDescCampionato));
    }

    private void configureGrid(GridCrud<FcAccesso> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(accesso -> accesso != null ? String.valueOf(accesso.getId()) : ""))
                .setHeader("Id");

        crud.getGrid()
                .addColumn(new TextRenderer<>(accesso ->
                        accesso != null && accesso.getFcAttore() != null
                                ? accesso.getFcAttore().getDescAttore()
                                : ""))
                .setHeader("Attore");

        Column<FcAccesso> dataColumn = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcAccesso::getData,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataColumn.setHeader("Data");
        dataColumn.setSortable(false);
        dataColumn.setAutoWidth(true);

        Column<FcAccesso> noteColumn = crud.getGrid()
                .addColumn(new TextRenderer<>(accesso -> accesso != null ? safe(accesso.getNote()) : ""))
                .setHeader("Info");
        noteColumn.setSortable(false);
        noteColumn.setAutoWidth(true);

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcAccesso> crud) {
        crud.setFindAllOperation(accessoService::findAll);
        crud.setAddOperation(accessoService::save);
        crud.setUpdateOperation(accessoService::save);
        crud.setDeleteOperation(accessoService::delete);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
