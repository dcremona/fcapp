package fcweb.ui.views.admin;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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

import common.util.Utils;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.job.JobProcessFileCsv;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.CalendarioCompetizioneService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Calendario Competizione")
@Route(value = "calelndarioCompetizione", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcCalendarioCompetizioneView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    private static final long serialVersionUID = 1L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CalendarioCompetizioneService calendarioTimController;

    @Autowired
    public Environment env;

    private Button initDb;
    private Button updateGiornata;

    @Autowired
    private JobProcessGiornata jobProcessGiornata;

    @Autowired
    private GiornataInfoService giornataInfoController;

    private ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>();

    @Autowired
    private AccessoService accessoController;

    @Autowired
    private SquadraService squadraController;

    public FcCalendarioCompetizioneView() {
        log.info("FcCalendarioCompetizioneView()");
    }

    @PostConstruct
    void init() {
        log.info("init");
        if (!Utils.isValidVaadinSession()) {
            return;
        }
        accessoController.insertAccesso(this.getClass().getName());
        initLayout();
    }

    private void initLayout() {

        this.setMargin(true);
        this.setSpacing(true);
        this.setSizeFull();

        FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
        FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

        initDb = new Button("Init Db Calendario");
        initDb.setIcon(VaadinIcon.START_COG.create());
        initDb.addClickListener(this);

        updateGiornata = new Button("Aggiorna Giornata");
        updateGiornata.setIcon(VaadinIcon.START_COG.create());
        updateGiornata.addClickListener(this);

        GridCrud<FcCalendarioCompetizione> crud = new GridCrud<>(FcCalendarioCompetizione.class,
                new HorizontalSplitCrudLayout());

        DefaultCrudFormFactory<FcCalendarioCompetizione> formFactory = new DefaultCrudFormFactory<>(
                FcCalendarioCompetizione.class);
        crud.setCrudFormFactory(formFactory);
        formFactory.setUseBeanValidation(false);

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "idGiornata", "data", "idSquadraCasa",
                "squadraCasa", "idSquadraFuori", "squadraFuori", "risultato");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "idGiornata", "data", "idSquadraCasa",
                "squadraCasa", "idSquadraFuori", "squadraFuori", "risultato");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "idGiornata", "data",
                "idSquadraCasa", "squadraCasa", "idSquadraFuori", "squadraFuori", "risultato");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id", "idGiornata");
        
        
        crud.getGrid().removeAllColumns();
        crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : "" + g.getIdGiornata()));
        Column<FcCalendarioCompetizione> dataColumn = crud.getGrid()
                .addColumn(new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,
                        () -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
        dataColumn.setSortable(false);
        dataColumn.setAutoWidth(true);
        dataColumn.setFlexGrow(2);

        Column<FcCalendarioCompetizione> sqCasaColumn = crud.getGrid().addColumn(new ComponentRenderer<>(s -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (s != null && s.getSquadraCasa() != null) {
                FcSquadra sq = squadraController.findByIdSquadra(s.getIdSquadraCasa());
                if (sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                Span lblSquadra = new Span(s.getSquadraCasa());
                cellLayout.add(lblSquadra);
            }
            return cellLayout;
        }));
        sqCasaColumn.setSortable(false);
        sqCasaColumn.setAutoWidth(true);

        Column<FcCalendarioCompetizione> sqFuoriColumn = crud.getGrid().addColumn(new ComponentRenderer<>(s -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            if (s != null && s.getSquadraFuori() != null) {
                FcSquadra sq = squadraController.findByIdSquadra(s.getIdSquadraFuori());
                if (sq.getImg() != null) {
                    try {
                        Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
                        cellLayout.add(img);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                Span lblSquadra = new Span(s.getSquadraFuori());
                cellLayout.add(lblSquadra);
            }
            return cellLayout;
        }));
        sqFuoriColumn.setSortable(false);
        sqFuoriColumn.setAutoWidth(true);

        crud.getGrid().setColumnReorderingAllowed(true);

        crud.getCrudFormFactory().setFieldProvider("data", a -> {
            return new DateTimePicker();
        });

        crud.getGrid()
                .addColumn(new TextRenderer<>(g -> g == null || g.getRisultato() == null ? "" : "" + g.getRisultato()));

        crud.setRowCountCaption("%d GiornataInfo(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        giornataInfoFilter.setPlaceholder("Giornata");
        giornataInfoFilter.setItems(giornataInfoController.findAll());
        if ("1".equals(campionato.getType())) {
            giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornata(g));
        } else {
            giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
        }

        giornataInfoFilter.addValueChangeListener(e -> crud.refreshGrid());
        giornataInfoFilter.setClearButtonVisible(true);
        giornataInfoFilter.setValue(giornataInfo);
        crud.getCrudLayout().addFilterComponent(giornataInfoFilter);

        Button clearFilters = new Button("clear");
        clearFilters.addClickListener(event -> {
            giornataInfoFilter.clear();
        });
        crud.getCrudLayout().addFilterComponent(clearFilters);

        crud.setFindAllOperation(() -> calendarioTimController.findCustom(giornataInfoFilter.getValue()));
        crud.setAddOperation(user -> calendarioTimController.updateCalendarioTim(user));
        crud.setUpdateOperation(user -> calendarioTimController.updateCalendarioTim(user));
        crud.setDeleteOperation(user -> calendarioTimController.deleteCalendarioTim(user));

        add(initDb);
        add(updateGiornata);
        add(crud);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {

        try {
            Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
            FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

            String basePathData = (String) p.get("PATH_TMP");
            log.info("basePathData " + basePathData);
            File f = new File(basePathData);
            if (!f.exists()) {
                CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC,
                        "Impossibile trovare il percorso specificato " + basePathData);
                return;
            }

            if (event.getSource() == initDb) {

                if ("1".equals(campionato.getType())) {

                    jobProcessGiornata.deleteAllCalendarioTim();

                    for (int g = 1; g <= 38; g++) {
                        // **************************************
                        // DOWNLOAD FILE TIM
                        // **************************************
                        String giornata = "" + g;
                        String urlFanta = (String) p.get("URL_FANTA");
                        String basePath = basePathData;
                        String calendario = "Serie-A-Calendario";
                        String httpUrl = urlFanta + calendario + ".asp?GiornataA=" + giornata + "&Tipolink=0";
                        log.info("httpUrl " + httpUrl);
                        String fileName = "TIM_" + giornata;
                        JobProcessFileCsv jobCsv = new JobProcessFileCsv();
                        jobCsv.downloadCsv(httpUrl, basePath, fileName, 0);

                        fileName = basePathData + fileName + ".csv";
                        jobProcessGiornata.insertCalendarioTim(fileName, g);

                    }

                } else {

                    jobProcessGiornata.initDbCalendarioCompetizione(basePathData + "calendarioMondiale2022.csv");

                    // jobProcessGiornata.deleteAllCalendarioTim();
                    // for (int g = 1; g <= 7; g++) {
                    // // **************************************
                    // // DOWNLOAD FILE MONDIALE
                    // // **************************************
                    // String giornata = "" + g;
                    // String urlFanta = (String) p.get("URL_FANTA");
                    // String basePath = basePathData;
                    // String calendario = "Mondiale-Calendario";
                    // String httpUrl = urlFanta + calendario + ".asp?GiornataA=" + giornata +
                    // "&Tipolink=0";
                    // LOG.info("httpUrl " + httpUrl);
                    // String fileName = "MONDIALE_" + giornata;
                    // JobProcessFileCsv jobCsv = new JobProcessFileCsv();
                    // jobCsv.downloadCsv(httpUrl, basePath, fileName, 0);
                    //
                    // fileName = basePathData + fileName + ".csv";
                    // jobProcessGiornata.insertCalendarioTim(fileName, g);
                    //
                    // }
                }

            } else if (event.getSource() == updateGiornata) {

                if ("1".equals(campionato.getType())) {

                    // **************************************
                    // DOWNLOAD FILE TIM
                    // **************************************
                    String giornata = "" + giornataInfoFilter.getValue().getCodiceGiornata();
                    String urlFanta = (String) p.get("URL_FANTA");
                    String basePath = basePathData;
                    String quotaz = "Serie-A-Calendario";
                    String httpUrl = urlFanta + quotaz + ".asp?GiornataA=" + giornata + "&Tipolink=0";
                    // ="https://www.pianetafanta.it/Serie-A-Calendario.asp?GiornataA=5&Tipolink=0";
                    log.info("httpUrl " + httpUrl);
                    String fileName = "TIM_" + giornata;
                    JobProcessFileCsv jobCsv = new JobProcessFileCsv();
                    jobCsv.downloadCsv(httpUrl, basePath, fileName, 0);

                    fileName = basePathData + fileName + ".csv";

                    jobProcessGiornata.updateCalendarioTim(fileName, giornataInfoFilter.getValue().getCodiceGiornata());

                } else {

                    // **************************************
                    // DOWNLOAD FILE MONDIALE
                    // **************************************
                    String giornata = "" + giornataInfoFilter.getValue().getCodiceGiornata();
                    String urlFanta = (String) p.get("URL_FANTA");
                    String basePath = basePathData;
                    // String quotaz = "Mondiale-Calendario";
                    String quotaz = "europei-calendario";
                    String httpUrl = urlFanta + quotaz + ".asp?GiornataAM=" + giornata + "&Tipolink=0";
                    log.info("httpUrl " + httpUrl);
                    // String fileName = "MONDIALE_" + giornata;
                    String fileName = "EUROPEI_" + giornata;
                    JobProcessFileCsv jobCsv = new JobProcessFileCsv();
                    jobCsv.downloadCsv(httpUrl, basePath, fileName, 0);

                    fileName = basePathData + fileName + ".csv";

                    jobProcessGiornata.updateCalendarioMondiale(fileName,
                            giornataInfoFilter.getValue().getCodiceGiornata());

                }

            }
            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
        }

    }

}