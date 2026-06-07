package fcapp.ui.views.admin;

import java.io.InputStream;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Squadre")
@Route(value = "squadra", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcSquadraView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcSquadraView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String TYPE_SERIE_A = "1";

    private static final String FIELD_ID_SQUADRA = "idSquadra";
    private static final String FIELD_NOME_SQUADRA = "nomeSquadra";
    private static final String FIELD_NOME_IMG = "nomeImg";

    private static final String PATH_IMG_SQUADRE = "classpath:img/squadre/";
    private static final String PATH_IMG_NAZIONI_20 = "classpath:img/nazioni/w20/";
    private static final String PATH_IMG_NAZIONI_40 = "classpath:img/nazioni/w40/";

    private final transient ResourceLoader resourceLoader;
    private final transient SquadraService squadraService;
    private final transient AccessoService accessoService;

    private Button updateImagesButton;

    public FcSquadraView(
            ResourceLoader resourceLoader,
            SquadraService squadraService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcSquadraView.class.getSimpleName());
        this.resourceLoader = resourceLoader;
        this.squadraService = squadraService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcSquadraView.class.getSimpleName());

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        configureLayout();
        updateImagesButton = buildUpdateImagesButton();
        add(updateImagesButton, buildCrud());
    }

    private void configureLayout() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();
    }

    private Button buildUpdateImagesButton() {
        Button button = new Button("Update Img Squadre");
        button.setIcon(VaadinIcon.START_COG.create());
        button.addClickListener(event -> updateSquadreImages());
        return button;
    }

    private GridCrud<FcSquadra> buildCrud() {
        GridCrud<FcSquadra> crud =
                new GridCrud<>(FcSquadra.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d squadra(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcSquadra> crud) {
        DefaultCrudFormFactory<FcSquadra> formFactory =
                new DefaultCrudFormFactory<>(FcSquadra.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID_SQUADRA,
                FIELD_NOME_SQUADRA,
                FIELD_NOME_IMG);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID_SQUADRA,
                FIELD_NOME_SQUADRA,
                FIELD_NOME_IMG);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID_SQUADRA,
                FIELD_NOME_SQUADRA,
                FIELD_NOME_IMG);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID_SQUADRA);
    }

    private void configureGrid(GridCrud<FcSquadra> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : String.valueOf(item.getIdSquadra())))
                .setHeader("Id");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(this::buildSquadraCell))
                .setHeader("Squadra");

        crud.getGrid()
                .addColumn(new TextRenderer<>(item ->
                        item == null ? "" : defaultString(item.getNomeImg())))
                .setHeader("Nome Img");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(this::buildImg40Cell))
                .setHeader("Img 40");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private HorizontalLayout buildSquadraCell(FcSquadra squadra) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);

        if (squadra == null) {
            return cellLayout;
        }

        try {
            if (squadra.getImg() != null && squadra.getImg().getBinaryStream() != null) {
                Image image = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                cellLayout.add(image);
            }
        } catch (SQLException e) {
            LOG.error("Error loading image for team {}", squadra.getNomeSquadra(), e);
        }

        cellLayout.add(new Span(defaultString(squadra.getNomeSquadra())));
        return cellLayout;
    }

    private HorizontalLayout buildImg40Cell(FcSquadra squadra) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);

        if (squadra == null) {
            return cellLayout;
        }

        try {
            if (squadra.getImg40() != null && squadra.getImg40().getBinaryStream() != null) {
                Image image = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg40().getBinaryStream());
                cellLayout.add(image);
            }
        } catch (SQLException e) {
            LOG.error("Error loading 40px image for team {}", squadra.getNomeSquadra(), e);
        }

        return cellLayout;
    }

    private void configureOperations(GridCrud<FcSquadra> crud) {
        crud.setFindAllOperation(squadraService::findAll);
        crud.setAddOperation(squadraService::save);
        crud.setUpdateOperation(squadraService::save);
        crud.setDeleteOperation(squadraService::delete);
    }

    private void updateSquadreImages() {
        try {
            FcCampionato campionato =
                    (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);

            if (campionato == null) {
                CustomMessageDialog.showMessageErrorDetails(
                        CustomMessageDialog.MSG_ERROR_GENERIC,
                        "Campionato non disponibile in sessione");
                return;
            }

            List<FcSquadra> squadre = squadraService.findAll();
            for (FcSquadra squadra : squadre) {
                updateSquadraImages(squadra, campionato);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            LOG.error("Error updating team images", e);
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void updateSquadraImages(FcSquadra squadra, FcCampionato campionato) throws Exception {
        if (squadra == null || StringUtils.isBlank(squadra.getNomeImg())) {
            return;
        }

        if (isSerieACompetition(campionato)) {
            squadra.setImg(loadBlob(PATH_IMG_SQUADRE + squadra.getNomeImg()));
        } else {
            squadra.setImg(loadBlob(PATH_IMG_NAZIONI_20 + squadra.getNomeImg()));
            squadra.setImg40(loadBlob(PATH_IMG_NAZIONI_40 + squadra.getNomeImg()));
        }

        squadraService.save(squadra);
    }

    private java.sql.Blob loadBlob(String resourcePath) throws Exception {
        Resource resource = resourceLoader.getResource(resourcePath);

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return BlobProxy.generateProxy(bytes);
        }
    }

    private boolean isSerieACompetition(FcCampionato campionato) {
        return campionato != null && TYPE_SERIE_A.equals(campionato.getType());
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
