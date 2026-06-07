package fcapp.ui.views.admin;

import java.io.File;
import java.io.Serial;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.job.JobProcessFileCsv;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Calendario Competizione")
@Route(value = "calendarioCompetizione", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcCalendarioCompetizioneView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcCalendarioCompetizioneView.class);

    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String SESSION_GIORNATA_INFO = "GIORNATA_INFO";
    private static final String SESSION_PROPERTIES = "PROPERTIES";
    private static final String PATH_TMP = "PATH_TMP";
    private static final String URL_FANTA = "URL_FANTA";
    private static final String CAMPIONATO_TYPE_SERIE_A = "1";

    private static final String FIELD_ID = "id";
    private static final String FIELD_ID_GIORNATA = "idGiornata";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_ID_SQUADRA_CASA = "idSquadraCasa";
    private static final String FIELD_SQUADRA_CASA = "squadraCasa";
    private static final String FIELD_ID_SQUADRA_FUORI = "idSquadraFuori";
    private static final String FIELD_SQUADRA_FUORI = "squadraFuori";
    private static final String FIELD_RISULTATO = "risultato";

    private final transient Environment env;
    private final transient JobProcessGiornata jobProcessGiornata;
    private final transient CalendarioCompetizioneService calendarioCompetizioneService;
    private final transient GiornataInfoService giornataInfoService;
    private final transient AccessoService accessoService;
    private final transient SquadraService squadraService;

    private final ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>();

    private Button initDbButton;
    private Button updateGiornataButton;

    public FcCalendarioCompetizioneView(
            Environment env,
            JobProcessGiornata jobProcessGiornata,
            CalendarioCompetizioneService calendarioCompetizioneService,
            GiornataInfoService giornataInfoService,
            AccessoService accessoService,
            SquadraService squadraService) {
        LOG.info("Initializing {}", FcCalendarioCompetizioneView.class.getSimpleName());
        this.env = env;
        this.jobProcessGiornata = jobProcessGiornata;
        this.calendarioCompetizioneService = calendarioCompetizioneService;
        this.giornataInfoService = giornataInfoService;
        this.accessoService = accessoService;
        this.squadraService = squadraService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcCalendarioCompetizioneView.class.getSimpleName());

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());

        configureLayout();

        FcCampionato campionato = getCurrentCampionato();
        FcGiornataInfo giornataInfo = getCurrentGiornataInfo();

        initDbButton = buildInitDbButton();
        updateGiornataButton = buildUpdateGiornataButton();

        GridCrud<FcCalendarioCompetizione> crud = buildCrud(campionato, giornataInfo);

        add(initDbButton, updateGiornataButton, crud);
    }

    private void configureLayout() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();
    }

    private Button buildInitDbButton() {
        Button button = new Button("Init Db Calendario");
        button.setIcon(VaadinIcon.START_COG.create());
        button.addClickListener(event -> handleInitDb());
        return button;
    }

    private Button buildUpdateGiornataButton() {
        Button button = new Button("Aggiorna Giornata");
        button.setIcon(VaadinIcon.START_COG.create());
        button.addClickListener(event -> handleUpdateGiornata());
        return button;
    }

    private GridCrud<FcCalendarioCompetizione> buildCrud(FcCampionato campionato, FcGiornataInfo giornataInfo) {
        GridCrud<FcCalendarioCompetizione> crud =
                new GridCrud<>(FcCalendarioCompetizione.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureFilter(crud, campionato, giornataInfo);
        configureCrudOperations(crud);

        crud.setRowCountCaption("%d GiornataInfo(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcCalendarioCompetizione> crud) {
        DefaultCrudFormFactory<FcCalendarioCompetizione> formFactory =
                new DefaultCrudFormFactory<>(FcCalendarioCompetizione.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID,
                FIELD_ID_GIORNATA,
                FIELD_DATA,
                FIELD_ID_SQUADRA_CASA,
                FIELD_SQUADRA_CASA,
                FIELD_ID_SQUADRA_FUORI,
                FIELD_SQUADRA_FUORI,
                FIELD_RISULTATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID,
                FIELD_ID_GIORNATA,
                FIELD_DATA,
                FIELD_ID_SQUADRA_CASA,
                FIELD_SQUADRA_CASA,
                FIELD_ID_SQUADRA_FUORI,
                FIELD_SQUADRA_FUORI,
                FIELD_RISULTATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_ID,
                FIELD_ID_GIORNATA,
                FIELD_DATA,
                FIELD_ID_SQUADRA_CASA,
                FIELD_SQUADRA_CASA,
                FIELD_ID_SQUADRA_FUORI,
                FIELD_SQUADRA_FUORI,
                FIELD_RISULTATO);

        crud.getCrudFormFactory().setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_ID_GIORNATA);

        crud.getCrudFormFactory().setFieldProvider(FIELD_DATA, field -> new DateTimePicker());
    }

    private void configureGrid(GridCrud<FcCalendarioCompetizione> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid()
                .addColumn(new TextRenderer<>(item -> item == null ? "" : String.valueOf(item.getIdGiornata())))
                .setHeader("Giornata");

        Column<FcCalendarioCompetizione> dataColumn = crud.getGrid().addColumn(
                new LocalDateTimeRenderer<>(
                        FcCalendarioCompetizione::getData,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataColumn.setHeader("Data");
        dataColumn.setSortable(false);
        dataColumn.setAutoWidth(true);
        dataColumn.setFlexGrow(2);

        Column<FcCalendarioCompetizione> sqCasaColumn = crud.getGrid().addColumn(
                new ComponentRenderer<>(item -> buildSquadraCell(item, true)));
        sqCasaColumn.setHeader("Squadra Casa");
        sqCasaColumn.setSortable(false);
        sqCasaColumn.setAutoWidth(true);

        Column<FcCalendarioCompetizione> sqFuoriColumn = crud.getGrid().addColumn(
                new ComponentRenderer<>(item -> buildSquadraCell(item, false)));
        sqFuoriColumn.setHeader("Squadra Fuori");
        sqFuoriColumn.setSortable(false);
        sqFuoriColumn.setAutoWidth(true);

        crud.getGrid()
                .addColumn(new TextRenderer<>(item -> item == null ? "" : safe(item.getRisultato())))
                .setHeader("Risultato")
                .setAutoWidth(true);

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private HorizontalLayout buildSquadraCell(FcCalendarioCompetizione item, boolean casa) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.setAlignItems(Alignment.CENTER);

        if (item == null) {
            return cellLayout;
        }

        String nomeSquadra = casa ? item.getSquadraCasa() : item.getSquadraFuori();
        Integer idSquadra = casa ? item.getIdSquadraCasa() : item.getIdSquadraFuori();

        if (nomeSquadra == null || idSquadra == null) {
            return cellLayout;
        }

        FcSquadra squadra = squadraService.findByIdSquadra(idSquadra);
        if (squadra != null && squadra.getImg() != null) {
            try {
                Image img = Utils.getImage(squadra.getNomeSquadra(), squadra.getImg().getBinaryStream());
                cellLayout.add(img);
            } catch (SQLException e) {
                LOG.error("Errore caricando immagine squadra {}", squadra.getNomeSquadra(), e);
            }
        }

        cellLayout.add(new Span(nomeSquadra));
        return cellLayout;
    }

    private void configureFilter(
            GridCrud<FcCalendarioCompetizione> crud,
            FcCampionato campionato,
            FcGiornataInfo giornataInfo) {

        List<FcGiornataInfo> giornate = giornataInfoService.findAll();

        giornataInfoFilter.setPlaceholder("Giornata");
        giornataInfoFilter.setItems(giornate);
        giornataInfoFilter.setClearButtonVisible(true);

        if (campionato != null && CAMPIONATO_TYPE_SERIE_A.equals(campionato.getType())) {
            giornataInfoFilter.setItemLabelGenerator(Utils::buildInfoGiornata);
        } else {
            giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
        }

        giornataInfoFilter.addValueChangeListener(event -> crud.refreshGrid());
        giornataInfoFilter.setValue(giornataInfo);

        Button clearFiltersButton = new Button("clear");
        clearFiltersButton.addClickListener(event -> giornataInfoFilter.clear());

        crud.getCrudLayout().addFilterComponent(giornataInfoFilter);
        crud.getCrudLayout().addFilterComponent(clearFiltersButton);
    }

    private void configureCrudOperations(GridCrud<FcCalendarioCompetizione> crud) {
        crud.setFindAllOperation(() -> calendarioCompetizioneService.findCustom(giornataInfoFilter.getValue()));
        crud.setAddOperation(calendarioCompetizioneService::save);
        crud.setUpdateOperation(calendarioCompetizioneService::save);
        crud.setDeleteOperation(calendarioCompetizioneService::delete);
    }

    private void handleInitDb() {
        try {
            Properties properties = getSessionProperties();
            FcCampionato campionato = getCurrentCampionato();
            String basePathData = getBasePathData();

            if (campionato == null) {
                CustomMessageDialog.showMessageErrorDetails(
                        CustomMessageDialog.MSG_ERROR_GENERIC,
                        "Campionato non disponibile in sessione");
                return;
            }

            if (isSerieA(campionato)) {
                initSerieACalendar(properties, basePathData);
            } else {
                initInternationalCalendar(basePathData);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            LOG.error("Errore durante init db calendario", e);
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void handleUpdateGiornata() {
        try {
            Properties properties = getSessionProperties();
            FcCampionato campionato = getCurrentCampionato();
            FcGiornataInfo giornataInfo = giornataInfoFilter.getValue();
            String basePathData = getBasePathData();

            if (campionato == null) {
                CustomMessageDialog.showMessageErrorDetails(
                        CustomMessageDialog.MSG_ERROR_GENERIC,
                        "Campionato non disponibile in sessione");
                return;
            }

            if (giornataInfo == null) {
                CustomMessageDialog.showMessageErrorDetails(
                        CustomMessageDialog.MSG_ERROR_GENERIC,
                        "Selezionare una giornata");
                return;
            }

            if (isSerieA(campionato)) {
                updateSerieAGiornata(properties, basePathData, giornataInfo);
            } else {
                updateInternationalGiornata(properties, basePathData, giornataInfo);
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            LOG.error("Errore durante aggiornamento giornata", e);
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    private void initSerieACalendar(Properties properties, String basePathData) throws Exception {
        jobProcessGiornata.deleteAllCalendarioTim();

        for (int giornata = 1; giornata <= 38; giornata++) {
            String filePath = downloadCalendarioSerieA(properties, basePathData, giornata);
            jobProcessGiornata.insertCalendarioTim(filePath, giornata);
        }
    }

    private void initInternationalCalendar(String basePathData) throws Exception {
        jobProcessGiornata.initDbCalendarioCompetizione(basePathData + "calendarioMondiale2022.csv");
    }

    private void updateSerieAGiornata(
            Properties properties,
            String basePathData,
            FcGiornataInfo giornataInfo) throws Exception {

        int codiceGiornata = giornataInfo.getCodiceGiornata();
        String filePath = downloadCalendarioSerieA(properties, basePathData, codiceGiornata);
        jobProcessGiornata.updateCalendarioTim(filePath, codiceGiornata);
    }

    private void updateInternationalGiornata(
            Properties properties,
            String basePathData,
            FcGiornataInfo giornataInfo) throws Exception {

        int codiceGiornata = giornataInfo.getCodiceGiornata();
        String giornata = String.valueOf(codiceGiornata);
        String urlFanta = (String) properties.get(URL_FANTA);
        String calendario = "europei-calendario";
        String httpUrl = urlFanta + calendario + ".asp?GiornataAM=" + giornata + "&Tipolink=0";

        LOG.info("httpUrl {}", httpUrl);

        String fileName = "EUROPEI_" + giornata;
        downloadCsv(httpUrl, basePathData, fileName);

        String filePath = basePathData + fileName + ".csv";
        jobProcessGiornata.updateCalendarioMondiale(filePath, codiceGiornata);
    }

    private String downloadCalendarioSerieA(Properties properties, String basePathData, int giornata) throws Exception {
        String giornataValue = String.valueOf(giornata);
        String urlFanta = (String) properties.get(URL_FANTA);
        String calendario = "Serie-A-Calendario";
        String httpUrl = urlFanta + calendario + ".asp?GiornataA=" + giornataValue + "&Tipolink=0";

        LOG.info("url {}", httpUrl);

        String fileName = "TIM_" + giornataValue;
        downloadCsv(httpUrl, basePathData, fileName);

        return basePathData + fileName + ".csv";
    }

    private void downloadCsv(String httpUrl, String basePathData, String fileName) throws Exception {
        JobProcessFileCsv jobCsv = new JobProcessFileCsv();
        jobCsv.downloadCsv(httpUrl, basePathData, fileName, 0);
    }

    private String getBasePathData() {
        String basePathData = env.getProperty(PATH_TMP);
        LOG.info("basePathData {}", basePathData);

        if (basePathData == null || basePathData.isBlank()) {
            throw new IllegalStateException("Percorso temporaneo non configurato");
        }

        File directory = new File(basePathData);
        if (!directory.exists()) {
            throw new IllegalStateException("Impossibile trovare il percorso specificato " + basePathData);
        }

        return basePathData;
    }

    private Properties getSessionProperties() {
        return (Properties) VaadinSession.getCurrent().getAttribute(SESSION_PROPERTIES);
    }

    private FcCampionato getCurrentCampionato() {
        return (FcCampionato) VaadinSession.getCurrent().getAttribute(SESSION_CAMPIONATO);
    }

    private FcGiornataInfo getCurrentGiornataInfo() {
        return (FcGiornataInfo) VaadinSession.getCurrent().getAttribute(SESSION_GIORNATA_INFO);
    }

    private boolean isSerieA(FcCampionato campionato) {
        return campionato != null && CAMPIONATO_TYPE_SERIE_A.equals(campionato.getType());
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
