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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcTipoGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.backend.service.TipoGiornataService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Giornata")
@Route(value = "giornata", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcGiornataView.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_FC_TIPO_GIORNATA = "fcTipoGiornata";
    private static final String FIELD_FC_GIORNATA_INFO = "fcGiornataInfo";
    private static final String FIELD_ATTORE_CASA = "fcAttoreByIdAttoreCasa";
    private static final String FIELD_ATTORE_FUORI = "fcAttoreByIdAttoreFuori";
    private static final String FIELD_GOL_CASA = "golCasa";
    private static final String FIELD_GOL_FUORI = "golFuori";
    private static final String FIELD_TOT_CASA = "totCasa";
    private static final String FIELD_TOT_FUORI = "totFuori";

    private final transient GiornataService giornataService;
    private final transient AttoreService attoreService;
    private final transient GiornataInfoService giornataInfoService;
    private final transient TipoGiornataService tipoGiornataService;
    private final transient AccessoService accessoService;

    public FcGiornataView(
            GiornataService giornataService,
            AttoreService attoreService,
            GiornataInfoService giornataInfoService,
            TipoGiornataService tipoGiornataService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcGiornataView.class.getSimpleName());
        this.giornataService = giornataService;
        this.attoreService = attoreService;
        this.giornataInfoService = giornataInfoService;
        this.tipoGiornataService = tipoGiornataService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcGiornataView.class.getSimpleName());

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

    private GridCrud<FcGiornata> buildCrud() {
        GridCrud<FcGiornata> crud =
                new GridCrud<>(FcGiornata.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Giornata(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcGiornata> crud) {
        DefaultCrudFormFactory<FcGiornata> formFactory =
                new DefaultCrudFormFactory<>(FcGiornata.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID,
                FIELD_FC_TIPO_GIORNATA,
                FIELD_FC_GIORNATA_INFO,
                FIELD_ATTORE_CASA,
                FIELD_ATTORE_FUORI,
                FIELD_GOL_CASA,
                FIELD_GOL_FUORI,
                FIELD_TOT_CASA,
                FIELD_TOT_FUORI);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID,
                FIELD_FC_TIPO_GIORNATA,
                FIELD_FC_GIORNATA_INFO,
                FIELD_ATTORE_CASA,
                FIELD_ATTORE_FUORI,
                FIELD_GOL_CASA,
                FIELD_GOL_FUORI,
                FIELD_TOT_CASA,
                FIELD_TOT_FUORI);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID,
                FIELD_FC_TIPO_GIORNATA,
                FIELD_FC_GIORNATA_INFO,
                FIELD_ATTORE_CASA,
                FIELD_ATTORE_FUORI,
                FIELD_GOL_CASA,
                FIELD_GOL_FUORI,
                FIELD_TOT_CASA,
                FIELD_TOT_FUORI);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_FC_TIPO_GIORNATA,
                FIELD_FC_GIORNATA_INFO,
                FIELD_ATTORE_CASA);

        List<FcGiornataInfo> giornateInfo = giornataInfoService.findAll();
        List<FcAttore> attori = attoreService.findByActive(true);
        List<FcTipoGiornata> tipiGiornata = tipoGiornataService.findAll();

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_GIORNATA_INFO,
                new ComboBoxProvider<>(
                        "Giornata",
                        giornateInfo,
                        new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),
                        FcGiornataInfo::getDescGiornataFc));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_ATTORE_CASA,
                new ComboBoxProvider<>(
                        "Attore Casa",
                        attori,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_ATTORE_FUORI,
                new ComboBoxProvider<>(
                        "Attore Fuori",
                        attori,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));

        crud.getCrudFormFactory().setFieldProvider(
                FIELD_FC_TIPO_GIORNATA,
                new ComboBoxProvider<>(
                        "Tipo Giornata",
                        tipiGiornata,
                        new TextRenderer<>(FcTipoGiornata::getDescTipoGiornata),
                        FcTipoGiornata::getDescTipoGiornata));
    }

    private void configureGrid(GridCrud<FcGiornata> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getId() != null
                                ? String.valueOf(item.getId().getIdGiornata())
                                : ""))
                .setHeader("Id Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiornataInfo() != null
                                ? item.getFcGiornataInfo().getDescGiornataFc()
                                : ""))
                .setHeader("Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcTipoGiornata() != null
                                ? item.getFcTipoGiornata().getDescTipoGiornata()
                                : ""))
                .setHeader("Tipo Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcAttoreByIdAttoreCasa() != null
                                ? item.getFcAttoreByIdAttoreCasa().getDescAttore()
                                : ""))
                .setHeader("Attore Casa");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcAttoreByIdAttoreFuori() != null
                                ? item.getFcAttoreByIdAttoreFuori().getDescAttore()
                                : ""))
                .setHeader("Attore Fuori");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getGolCasa() != null
                                ? item.getGolCasa().toString()
                                : ""))
                .setHeader("Gol Casa");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getGolFuori() != null
                                ? item.getGolFuori().toString()
                                : ""))
                .setHeader("Gol Fuori");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getTotCasa() != null
                                ? item.getTotCasa().toString()
                                : ""))
                .setHeader("Tot Casa");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getTotFuori() != null
                                ? item.getTotFuori().toString()
                                : ""))
                .setHeader("Tot Fuori");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcGiornata> crud) {
        crud.setFindAllOperation(giornataService::findAll);
        crud.setAddOperation(giornataService::save);
        crud.setUpdateOperation(giornataService::save);
        crud.setDeleteOperation(giornataService::delete);
    }
}
