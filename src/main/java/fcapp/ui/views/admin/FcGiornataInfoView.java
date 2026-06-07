package fcapp.ui.views.admin;

import java.io.Serial;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("GiornataInfo")
@Route(value = "giornataInfo", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataInfoView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcGiornataInfoView.class);

    private static final String FIELD_CODICE_GIORNATA = "codiceGiornata";
    private static final String FIELD_DATA_ANTICIPO_1 = "dataAnticipo1";
    private static final String FIELD_DATA_ANTICIPO_2 = "dataAnticipo2";
    private static final String FIELD_DATA_GIORNATA = "dataGiornata";
    private static final String FIELD_DATA_POSTICIPO = "dataPosticipo";
    private static final String FIELD_DESC_GIORNATA = "descGiornata";
    private static final String FIELD_DESC_GIORNATA_FC = "descGiornataFc";
    private static final String FIELD_ID_GIORNATA_FC = "idGiornataFc";

    private final transient GiornataInfoService giornataInfoService;
    private final transient AccessoService accessoService;

    public FcGiornataInfoView(
            GiornataInfoService giornataInfoService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcGiornataInfoView.class.getSimpleName());
        this.giornataInfoService = giornataInfoService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcGiornataInfoView.class.getSimpleName());

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

    private GridCrud<FcGiornataInfo> buildCrud() {
        GridCrud<FcGiornataInfo> crud =
                new GridCrud<>(FcGiornataInfo.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d GiornataInfo(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcGiornataInfo> crud) {
        DefaultCrudFormFactory<FcGiornataInfo> formFactory =
                new DefaultCrudFormFactory<>(FcGiornataInfo.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_CODICE_GIORNATA,
                FIELD_DATA_ANTICIPO_1,
                FIELD_DATA_ANTICIPO_2,
                FIELD_DATA_GIORNATA,
                FIELD_DATA_POSTICIPO,
                FIELD_DESC_GIORNATA,
                FIELD_DESC_GIORNATA_FC,
                FIELD_ID_GIORNATA_FC);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_CODICE_GIORNATA,
                FIELD_DATA_ANTICIPO_1,
                FIELD_DATA_ANTICIPO_2,
                FIELD_DATA_GIORNATA,
                FIELD_DATA_POSTICIPO,
                FIELD_DESC_GIORNATA,
                FIELD_DESC_GIORNATA_FC,
                FIELD_ID_GIORNATA_FC);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_DATA_ANTICIPO_1,
                FIELD_DATA_ANTICIPO_2,
                FIELD_DATA_GIORNATA,
                FIELD_DATA_POSTICIPO,
                FIELD_DESC_GIORNATA,
                FIELD_DESC_GIORNATA_FC,
                FIELD_ID_GIORNATA_FC);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_CODICE_GIORNATA,
                FIELD_DESC_GIORNATA);

        crud.getCrudFormFactory().setFieldProvider(FIELD_DATA_ANTICIPO_1, field -> new DateTimePicker());
        crud.getCrudFormFactory().setFieldProvider(FIELD_DATA_ANTICIPO_2, field -> new DateTimePicker());
        crud.getCrudFormFactory().setFieldProvider(FIELD_DATA_GIORNATA, field -> new DateTimePicker());
        crud.getCrudFormFactory().setFieldProvider(FIELD_DATA_POSTICIPO, field -> new DateTimePicker());
    }

    private void configureGrid(GridCrud<FcGiornataInfo> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : String.valueOf(item.getCodiceGiornata())))
                .setHeader("Codice");

        Column<FcGiornataInfo> dataAnticipoColumn1 = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcGiornataInfo::getDataAnticipo1,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataAnticipoColumn1.setHeader("Anticipo 1");
        dataAnticipoColumn1.setSortable(false);
        dataAnticipoColumn1.setAutoWidth(true);

        Column<FcGiornataInfo> dataAnticipoColumn2 = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcGiornataInfo::getDataAnticipo2,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataAnticipoColumn2.setHeader("Anticipo 2");
        dataAnticipoColumn2.setSortable(false);
        dataAnticipoColumn2.setAutoWidth(true);

        Column<FcGiornataInfo> dataGiornataColumn = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcGiornataInfo::getDataGiornata,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataGiornataColumn.setHeader("Giornata");
        dataGiornataColumn.setSortable(false);
        dataGiornataColumn.setAutoWidth(true);

        Column<FcGiornataInfo> dataPosticipoColumn = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcGiornataInfo::getDataPosticipo,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataPosticipoColumn.setHeader("Posticipo");
        dataPosticipoColumn.setSortable(false);
        dataPosticipoColumn.setAutoWidth(true);

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : defaultString(item.getDescGiornata())))
                .setHeader("Descrizione");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : defaultString(item.getDescGiornataFc())))
                .setHeader("Descrizione FC");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : String.valueOf(item.getIdGiornataFc())))
                .setHeader("Id Giornata FC");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcGiornataInfo> crud) {
        crud.setFindAllOperation(giornataInfoService::findAll);
        crud.setAddOperation(giornataInfoService::save);
        crud.setUpdateOperation(giornataInfoService::save);
        crud.setDeleteOperation(giornataInfoService::delete);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
