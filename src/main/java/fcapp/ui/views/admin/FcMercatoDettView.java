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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcMercatoDett;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.MercatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("MercatoDett")
@Route(value = "mercatoDett", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcMercatoDettView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcMercatoDettView.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_FC_GIORNATA_INFO = "fcGiornataInfo";
    private static final String FIELD_FC_ATTORE = "fcAttore";
    private static final String FIELD_GIOCATORE_VENDUTO = "fcGiocatoreByIdGiocVen";
    private static final String FIELD_GIOCATORE_ACQUISTATO = "fcGiocatoreByIdGiocAcq";
    private static final String FIELD_DATA_CAMBIO = "dataCambio";
    private static final String FIELD_NOTA = "nota";

    private final transient MercatoService mercatoService;
    private final transient GiornataInfoService giornataInfoService;
    private final transient AttoreService attoreService;
    private final transient GiocatoreService giocatoreService;
    private final transient AccessoService accessoService;

    public FcMercatoDettView(
            MercatoService mercatoService,
            GiornataInfoService giornataInfoService,
            AttoreService attoreService,
            GiocatoreService giocatoreService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcMercatoDettView.class.getSimpleName());
        this.mercatoService = mercatoService;
        this.giornataInfoService = giornataInfoService;
        this.attoreService = attoreService;
        this.giocatoreService = giocatoreService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcMercatoDettView.class.getSimpleName());

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

    private GridCrud<FcMercatoDett> buildCrud() {
        GridCrud<FcMercatoDett> crud =
                new GridCrud<>(FcMercatoDett.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Mercato(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcMercatoDett> crud) {
        DefaultCrudFormFactory<FcMercatoDett> formFactory =
                new DefaultCrudFormFactory<>(FcMercatoDett.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        formFactory.setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_GIOCATORE_VENDUTO,
                FIELD_GIOCATORE_ACQUISTATO,
                FIELD_DATA_CAMBIO,
                FIELD_NOTA);

        formFactory.setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_GIOCATORE_VENDUTO,
                FIELD_GIOCATORE_ACQUISTATO,
                FIELD_DATA_CAMBIO,
                FIELD_NOTA);

        formFactory.setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_GIOCATORE_VENDUTO,
                FIELD_GIOCATORE_ACQUISTATO,
                FIELD_DATA_CAMBIO,
                FIELD_NOTA);

        formFactory.setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_FC_GIORNATA_INFO,
                FIELD_FC_ATTORE,
                FIELD_GIOCATORE_VENDUTO,
                FIELD_GIOCATORE_ACQUISTATO);

        List<FcGiornataInfo> giornate = giornataInfoService.findAll();
        List<FcAttore> attori = attoreService.findByActive(true);
        List<FcGiocatore> giocatori = giocatoreService.findAll();

        formFactory.setFieldProvider(
                FIELD_FC_GIORNATA_INFO,
                new ComboBoxProvider<>(
                        "Giornata",
                        giornate,
                        new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),
                        FcGiornataInfo::getDescGiornataFc));

        formFactory.setFieldProvider(
                FIELD_FC_ATTORE,
                new ComboBoxProvider<>(
                        "Attore",
                        attori,
                        new TextRenderer<>(FcAttore::getDescAttore),
                        FcAttore::getDescAttore));

        formFactory.setFieldProvider(
                FIELD_GIOCATORE_VENDUTO,
                new ComboBoxProvider<>(
                        "Giocatore Ven",
                        giocatori,
                        new TextRenderer<>(FcGiocatore::getCognGiocatore),
                        FcGiocatore::getCognGiocatore));

        formFactory.setFieldProvider(
                FIELD_GIOCATORE_ACQUISTATO,
                new ComboBoxProvider<>(
                        "Giocatore Acq",
                        giocatori,
                        new TextRenderer<>(FcGiocatore::getCognGiocatore),
                        FcGiocatore::getCognGiocatore));

        formFactory.setFieldProvider(FIELD_DATA_CAMBIO, field -> new DateTimePicker());
    }

    private void configureGrid(GridCrud<FcMercatoDett> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null ? String.valueOf(item.getId()) : ""))
                .setHeader("Id");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiornataInfo() != null
                                ? item.getFcGiornataInfo().getDescGiornataFc()
                                : ""))
                .setHeader("Giornata");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcAttore() != null
                                ? item.getFcAttore().getDescAttore()
                                : ""))
                .setHeader("Attore");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiocatoreByIdGiocVen() != null
                                ? item.getFcGiocatoreByIdGiocVen().getCognGiocatore()
                                : ""))
                .setHeader("Giocatore Ven");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getFcGiocatoreByIdGiocAcq() != null
                                ? item.getFcGiocatoreByIdGiocAcq().getCognGiocatore()
                                : ""))
                .setHeader("Giocatore Acq");

        Column<FcMercatoDett> dataColumn = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcMercatoDett::getDataCambio,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataColumn.setHeader("Data Cambio");
        dataColumn.setSortable(false);
        dataColumn.setAutoWidth(true);
        dataColumn.setFlexGrow(2);

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getNota() != null ? item.getNota() : ""))
                .setHeader("Nota");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcMercatoDett> crud) {
        crud.setFindAllOperation(mercatoService::findAll);
        crud.setAddOperation(mercatoService::save);
        crud.setUpdateOperation(mercatoService::save);
        crud.setDeleteOperation(mercatoService::delete);
    }
}
