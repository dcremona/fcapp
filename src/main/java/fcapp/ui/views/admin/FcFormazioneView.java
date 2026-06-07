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
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Formazione")
@Route(value = "formazione", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcFormazioneView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcFormazioneView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";

    private static final String FIELD_ID = "id";
    private static final String FIELD_FC_ATTORE = "fcAttore";
    private static final String FIELD_FC_GIOCATORE = "fcGiocatore";
    private static final String FIELD_TOT_PAGATO = "totPagato";

    private final transient AttoreService attoreService;
    private final transient FormazioneService formazioneService;
    private final transient GiocatoreService giocatoreService;
    private final transient AccessoService accessoService;

    public FcFormazioneView(
            AttoreService attoreService,
            FormazioneService formazioneService,
            GiocatoreService giocatoreService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcFormazioneView.class.getSimpleName());
        this.attoreService = attoreService;
        this.formazioneService = formazioneService;
        this.giocatoreService = giocatoreService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcFormazioneView.class.getSimpleName());

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

    private GridCrud<FcFormazione> buildCrud() {
        GridCrud<FcFormazione> crud =
                new GridCrud<>(FcFormazione.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Formazione(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcFormazione> crud) {
        DefaultCrudFormFactory<FcFormazione> formFactory =
                new DefaultCrudFormFactory<>(FcFormazione.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE,
                FIELD_TOT_PAGATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE,
                FIELD_TOT_PAGATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID,
                FIELD_FC_ATTORE,
                FIELD_FC_GIOCATORE,
                FIELD_TOT_PAGATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_FC_GIOCATORE);

        List<FcAttore> attori = attoreService.findByActive(true);
        List<FcGiocatore> giocatori = giocatoreService.findAll();

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
    }

    private void configureGrid(GridCrud<FcFormazione> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item != null && item.getId() != null
                                ? String.valueOf(item.getId().getOrdinamento())
                                : ""))
                .setHeader("Id");

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
                        item != null && item.getTotPagato() != null
                                ? item.getTotPagato().toString()
                                : ""))
                .setHeader("Pagato");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void configureOperations(GridCrud<FcFormazione> crud) {
        FcCampionato campionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);

        crud.setFindAllOperation(() -> formazioneService.findByFcCampionato(campionato));
        crud.setAddOperation(formazioneService::save);
        crud.setUpdateOperation(formazioneService::save);
        crud.setDeleteOperation(formazioneService::delete);
    }
}
