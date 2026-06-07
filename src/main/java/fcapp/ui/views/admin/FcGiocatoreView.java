package fcapp.ui.views.admin;

import java.io.File;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.RuoloService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle(Costants.GIOCATORE)
@Route(value = Costants.GIOCATORE, layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiocatoreView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcGiocatoreView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String ENV_PATH_TMP = "PATH_TMP";
    private static final String TYPE_SERIE_A = "1";
    private static final String SMALL_PREFIX = "small-";
    private static final String CLEAR_LABEL = "clear";

    private static final String FIELD_ID_GIOCATORE = "idGiocatore";
    private static final String FIELD_COGN_GIOCATORE = "cognGiocatore";
    private static final String FIELD_NOME_IMG = "nomeImg";
    private static final String FIELD_FC_SQUADRA = "fcSquadra";
    private static final String FIELD_FC_RUOLO = "fcRuolo";
    private static final String FIELD_FLAG_ATTIVO = "flagAttivo";
    private static final String FIELD_QUOTAZIONE = "quotazione";

    private final transient Environment env;
    private final transient GiocatoreService giocatoreService;
    private final transient SquadraService squadraService;
    private final transient RuoloService ruoloService;
    private final transient AccessoService accessoService;

    private final ComboBox<FcRuolo> ruoloFilter = new ComboBox<>(Costants.RUOLO);
    private final ComboBox<FcSquadra> squadraFilter = new ComboBox<>(Costants.SQUADRA);

    public FcGiocatoreView(
            Environment env,
            GiocatoreService giocatoreService,
            SquadraService squadraService,
            RuoloService ruoloService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcGiocatoreView.class.getSimpleName());
        this.env = env;
        this.giocatoreService = giocatoreService;
        this.squadraService = squadraService;
        this.ruoloService = ruoloService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcGiocatoreView.class.getSimpleName());

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

    private GridCrud<FcGiocatore> buildCrud() {
        GridCrud<FcGiocatore> crud =
                new GridCrud<>(FcGiocatore.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureFilters(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d Giocatore(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcGiocatore> crud) {
        DefaultCrudFormFactory<FcGiocatore> formFactory =
                new DefaultCrudFormFactory<>(FcGiocatore.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        formFactory.setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID_GIOCATORE,
                FIELD_COGN_GIOCATORE,
                FIELD_QUOTAZIONE,
                FIELD_NOME_IMG,
                FIELD_FC_SQUADRA,
                FIELD_FC_RUOLO,
                FIELD_FLAG_ATTIVO,
                FIELD_QUOTAZIONE);

        formFactory.setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID_GIOCATORE,
                FIELD_COGN_GIOCATORE,
                FIELD_NOME_IMG,
                FIELD_FC_SQUADRA,
                FIELD_FC_RUOLO,
                FIELD_FLAG_ATTIVO,
                FIELD_QUOTAZIONE);

        formFactory.setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_COGN_GIOCATORE,
                FIELD_QUOTAZIONE,
                FIELD_NOME_IMG,
                FIELD_FC_SQUADRA,
                FIELD_FC_RUOLO,
                FIELD_FLAG_ATTIVO,
                FIELD_QUOTAZIONE);

        formFactory.setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID_GIOCATORE,
                FIELD_COGN_GIOCATORE);

        List<FcSquadra> squadre = squadraService.findAll();
        List<FcRuolo> ruoli = ruoloService.findAll();

        formFactory.setFieldProvider(
                FIELD_FC_SQUADRA,
                new ComboBoxProvider<>(
                        FIELD_FC_SQUADRA,
                        squadre,
                        new TextRenderer<>(FcSquadra::getNomeSquadra),
                        FcSquadra::getNomeSquadra));

        formFactory.setFieldProvider(
                FIELD_FC_RUOLO,
                new ComboBoxProvider<>(
                        FIELD_FC_RUOLO,
                        ruoli,
                        new TextRenderer<>(FcRuolo::getDescRuolo),
                        FcRuolo::getDescRuolo));
    }

    private void configureGrid(GridCrud<FcGiocatore> crud) {
        crud.getGrid().removeAllColumns();

        if (isSerieACompetition()) {
            addImageColumn(crud);
        }

        crud.getGrid()
                .addColumn(new TextRenderer<>(g ->
                        g == null ? "" : String.valueOf(g.getIdGiocatore())))
                .setHeader("Id");

        crud.getGrid()
                .addColumn(new TextRenderer<>(g ->
                        g != null && g.getFcRuolo() != null ? g.getFcRuolo().getIdRuolo() : ""))
                .setHeader(Costants.RUOLO);

        Column<FcGiocatore> giocatoreColumn = crud.getGrid()
                .addColumn(new TextRenderer<>(g ->
                        g == null ? "" : defaultString(g.getCognGiocatore())))
                .setHeader(Costants.GIOCATORE);
        giocatoreColumn.setSortable(false);
        giocatoreColumn.setAutoWidth(true);

        Column<FcGiocatore> squadraColumn = crud.getGrid()
                .addColumn(new TextRenderer<>(g ->
                        g != null && g.getFcSquadra() != null ? g.getFcSquadra().getNomeSquadra() : ""))
                .setHeader(Costants.SQUADRA);
        squadraColumn.setSortable(false);
        squadraColumn.setAutoWidth(true);

        crud.getGrid()
                .addColumn(new TextRenderer<>(g ->
                        g == null ? "" : String.valueOf(g.getQuotazione())))
                .setHeader("Quotazione");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(g -> {
                    Checkbox check = new Checkbox();
                    check.setValue(g != null && g.isFlagAttivo());
                    check.setReadOnly(true);
                    return check;
                }))
                .setHeader("Attivo");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private void addImageColumn(GridCrud<FcGiocatore> crud) {
        Column<FcGiocatore> imageColumn = crud.getGrid().addColumn(
                new ComponentRenderer<>(this::buildImageCell));
        imageColumn.setWidth("350px");
    }

    private HorizontalLayout buildImageCell(FcGiocatore giocatore) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setSizeFull();

        if (giocatore == null || giocatore.getNomeImg() == null) {
            return cellLayout;
        }

        addStoredImage(cellLayout, giocatore);
        addOnlineImage(cellLayout, giocatore);
        cellLayout.add(createSaveImageButton(giocatore));

        return cellLayout;
    }

    private void addStoredImage(HorizontalLayout layout, FcGiocatore giocatore) {
        try {
            if (giocatore.getImg() != null) {
                Image image = Utils.getImage(giocatore.getNomeImg(), giocatore.getImg().getBinaryStream());
                layout.add(image);
            }
        } catch (SQLException e) {
            LOG.error("Error loading stored image for {}", giocatore.getNomeImg(), e);
        }
    }

    private void addOnlineImage(HorizontalLayout layout, FcGiocatore giocatore) {
        Image onlineImage =
                new Image(Costants.HTTP_URL_IMG + giocatore.getNomeImg(), giocatore.getNomeImg());
        layout.add(onlineImage);
    }

    private Button createSaveImageButton(FcGiocatore giocatore) {
        Button updateImg = new Button("Salva");
        updateImg.setIcon(VaadinIcon.DATABASE.create());
        updateImg.addClickListener(event -> savePlayerImages(giocatore));
        return updateImg;
    }

    private void savePlayerImages(FcGiocatore giocatore) {
        try {
            String basePath = getValidatedBasePath();
            String imageName = giocatore.getNomeImg();

            LOG.info("Saving image {}", imageName);
            LOG.info("httpUrlImg {}", Costants.HTTP_URL_IMG);

            boolean downloaded =
                    Utils.downloadFile(Costants.HTTP_URL_IMG + imageName, basePath + imageName);
            LOG.info("Download result {}", downloaded);

            boolean resized =
                    Utils.buildFileSmall(basePath + imageName, basePath + SMALL_PREFIX + imageName);
            LOG.info("Resize result {}", resized);

            giocatore.setImg(BlobProxy.generateProxy(Utils.getImage(basePath + imageName)));
            giocatore.setImg(BlobProxy.generateProxy(Utils.getImage(basePath + imageName)));
            giocatore.setImgSmall(BlobProxy.generateProxy(Utils.getImage(basePath + SMALL_PREFIX + imageName)));

            giocatoreService.save(giocatore);

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            LOG.error("Error saving image for {}", giocatore != null ? giocatore.getNomeImg() : null, e);
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    defaultString(e.getMessage()));
        }
    }

    private void configureFilters(GridCrud<FcGiocatore> crud) {
        ruoloFilter.setItems(ruoloService.findAll());
        ruoloFilter.setItemLabelGenerator(FcRuolo::getIdRuolo);
        ruoloFilter.setClearButtonVisible(true);
        ruoloFilter.addValueChangeListener(event -> crud.refreshGrid());

        squadraFilter.setItems(squadraService.findAll());
        squadraFilter.setItemLabelGenerator(FcSquadra::getNomeSquadra);
        squadraFilter.setClearButtonVisible(true);
        squadraFilter.addValueChangeListener(event -> crud.refreshGrid());

        Button clearFilters = new Button(CLEAR_LABEL);
        clearFilters.addClickListener(event -> {
            ruoloFilter.clear();
            squadraFilter.clear();
        });

        crud.getCrudLayout().addFilterComponent(ruoloFilter);
        crud.getCrudLayout().addFilterComponent(squadraFilter);
        crud.getCrudLayout().addFilterComponent(clearFilters);
    }

    private void configureOperations(GridCrud<FcGiocatore> crud) {
        crud.setFindAllOperation(() ->
                giocatoreService.findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(
                        ruoloFilter.getValue(),
                        squadraFilter.getValue()));
        crud.setAddOperation(giocatoreService::save);
        crud.setUpdateOperation(giocatoreService::save);
        crud.setDeleteOperation(giocatoreService::delete);
    }

    private boolean isSerieACompetition() {
        FcCampionato campionato =
                (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);
        return campionato != null && Objects.equals(TYPE_SERIE_A, campionato.getType());
    }

    private String getValidatedBasePath() {
        String basePath = env.getProperty(ENV_PATH_TMP);
        LOG.info("basePathData {}", basePath);

        if (basePath == null || basePath.isBlank()) {
            throw new IllegalStateException("Percorso temporaneo non configurato");
        }

        File directory = new File(basePath);
        if (!directory.exists()) {
            throw new IllegalStateException("Impossibile trovare il percorso specificato " + basePath);
        }

        return basePath;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
