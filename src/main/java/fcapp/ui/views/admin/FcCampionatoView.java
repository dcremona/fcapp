package fcapp.ui.views.admin;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.CampionatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Campionato")
@Route(value = "campionato", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcCampionatoView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcCampionatoView.class);

    private static final String FIELD_ID_CAMPIONATO = "idCampionato";
    private static final String FIELD_DESC_CAMPIONATO = "descCampionato";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_DATA_INIZIO = "dataInizio";
    private static final String FIELD_DATA_FINE = "dataFine";
    private static final String FIELD_START = "start";
    private static final String FIELD_END = "end";
    private static final String FIELD_ACTIVE = "active";

    private final transient AccessoService accessoService;
    private final transient CampionatoService campionatoService;

    public FcCampionatoView(
            AccessoService accessoService,
            CampionatoService campionatoService) {
        LOG.info("Initializing {}", FcCampionatoView.class.getSimpleName());
        this.accessoService = accessoService;
        this.campionatoService = campionatoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcCampionatoView.class.getSimpleName());

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

    private GridCrud<FcCampionato> buildCrud() {
        GridCrud<FcCampionato> crud =
                new GridCrud<>(FcCampionato.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d campionato(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcCampionato> crud) {
        DefaultCrudFormFactory<FcCampionato> formFactory =
                new DefaultCrudFormFactory<>(FcCampionato.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        String[] detailFields = {
                FIELD_ID_CAMPIONATO,
                FIELD_DESC_CAMPIONATO,
                FIELD_TYPE,
                FIELD_DATA_INIZIO,
                FIELD_DATA_FINE,
                FIELD_START,
                FIELD_END,
                FIELD_ACTIVE
        };

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, detailFields);
        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID_CAMPIONATO,
                FIELD_DESC_CAMPIONATO);
    }

    private void configureGrid(GridCrud<FcCampionato> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid().addColumn(FcCampionato::getIdCampionato).setHeader("Id");
        crud.getGrid().addColumn(FcCampionato::getDescCampionato).setHeader("Descrizione");
        crud.getGrid().addColumn(FcCampionato::getType).setHeader("Tipo");
        crud.getGrid().addColumn(FcCampionato::getDataInizio).setHeader("Data Inizio");
        crud.getGrid().addColumn(FcCampionato::getDataFine).setHeader("Data Fine");
        crud.getGrid().addColumn(FcCampionato::getStart).setHeader("Start");
        crud.getGrid().addColumn(FcCampionato::getEnd).setHeader("End");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(campionato -> {
                    Checkbox checkbox = new Checkbox();
                    checkbox.setValue(campionato != null && campionato.isActive());
                    checkbox.setReadOnly(true);
                    return checkbox;
                }))
                .setHeader("Attivo");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcCampionato> crud) {
        crud.setFindAllOperation(campionatoService::findAll);
        crud.setAddOperation(campionatoService::save);
        crud.setUpdateOperation(campionatoService::save);
        crud.setDeleteOperation(campionatoService::delete);
    }
}
