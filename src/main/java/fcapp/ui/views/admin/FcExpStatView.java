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

import fcapp.backend.data.entity.FcExpStat;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ExpStatService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("ExpStat")
@Route(value = "fcExpStat", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcExpStatView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcExpStatView.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_ANNO = "anno";
    private static final String FIELD_CAMPIONATO = "campionato";
    private static final String FIELD_P2 = "p2";
    private static final String FIELD_P3 = "p3";
    private static final String FIELD_P4 = "p4";
    private static final String FIELD_P5 = "p5";
    private static final String FIELD_P6 = "p6";
    private static final String FIELD_P7 = "p7";
    private static final String FIELD_P8 = "p8";
    private static final String FIELD_SCUDETTO = "scudetto";
    private static final String FIELD_WIN_CLAS_PT = "winClasPt";
    private static final String FIELD_WIN_CLAS_REG = "winClasReg";
    private static final String FIELD_WIN_CLAS_TVS_T = "winClasTvsT";

    private final transient ExpStatService expStatService;
    private final transient AccessoService accessoService;

    public FcExpStatView(
            ExpStatService expStatService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcExpStatView.class.getSimpleName());
        this.expStatService = expStatService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcExpStatView.class.getSimpleName());

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

    private GridCrud<FcExpStat> buildCrud() {
        GridCrud<FcExpStat> crud =
                new GridCrud<>(FcExpStat.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureOperations(crud);

        crud.getGrid().setColumnReorderingAllowed(true);
        crud.setRowCountCaption("%d ExpStat(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcExpStat> crud) {
        DefaultCrudFormFactory<FcExpStat> formFactory =
                new DefaultCrudFormFactory<>(FcExpStat.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        String[] readFields = {
                FIELD_ID,
                FIELD_ANNO,
                FIELD_CAMPIONATO,
                FIELD_P2,
                FIELD_P3,
                FIELD_P4,
                FIELD_P5,
                FIELD_P6,
                FIELD_P7,
                FIELD_P8,
                FIELD_SCUDETTO,
                FIELD_WIN_CLAS_PT,
                FIELD_WIN_CLAS_REG,
                FIELD_WIN_CLAS_TVS_T
        };

        String[] addFields = readFields;

        String[] updateFields = {
                FIELD_ANNO,
                FIELD_CAMPIONATO,
                FIELD_P2,
                FIELD_P3,
                FIELD_P4,
                FIELD_P5,
                FIELD_P6,
                FIELD_P7,
                FIELD_P8,
                FIELD_SCUDETTO,
                FIELD_WIN_CLAS_PT,
                FIELD_WIN_CLAS_REG,
                FIELD_WIN_CLAS_TVS_T
        };

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, readFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, addFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, updateFields);
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, FIELD_ID);
    }

    private void configureOperations(GridCrud<FcExpStat> crud) {
        crud.setFindAllOperation(expStatService::findAll);
        crud.setAddOperation(expStatService::save);
        crud.setUpdateOperation(expStatService::save);
        crud.setDeleteOperation(expStatService::delete);
    }
}
