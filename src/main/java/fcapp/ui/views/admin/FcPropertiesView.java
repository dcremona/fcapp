package fcapp.ui.views.admin;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ProprietaService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Proprietà")
@Route(value = "proprietà", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcPropertiesView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcPropertiesView.class);

    private static final String FIELD_KEY = "key";
    private static final String FIELD_VALUE = "value";

    private final transient ProprietaService proprietaService;
    private final transient AccessoService accessoService;

    public FcPropertiesView(
            ProprietaService proprietaService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcPropertiesView.class.getSimpleName());
        this.proprietaService = proprietaService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcPropertiesView.class.getSimpleName());

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

    private GridCrud<FcProperties> buildCrud() {
        GridCrud<FcProperties> crud =
                new GridCrud<>(FcProperties.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d property(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcProperties> crud) {
        DefaultCrudFormFactory<FcProperties> formFactory =
                new DefaultCrudFormFactory<>(FcProperties.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, FIELD_KEY, FIELD_VALUE);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, FIELD_KEY, FIELD_VALUE);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, FIELD_KEY, FIELD_VALUE);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, FIELD_KEY);
    }

    private void configureGrid(GridCrud<FcProperties> crud) {
        crud.getGrid().removeAllColumns();
        crud.getGrid().addColumn(FcProperties::getKey).setHeader("Key");
        crud.getGrid().addColumn(FcProperties::getValue).setHeader("Value");
        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcProperties> crud) {
        crud.setFindAllOperation(proprietaService::findAll);
        crud.setAddOperation(proprietaService::save);
        crud.setUpdateOperation(proprietaService::save);
        crud.setDeleteOperation(proprietaService::delete);
    }
}
