package fcapp.ui.views.seriea;

import java.io.File;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.vaadin.filesystemdataprovider.FileSelect;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;

import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcExpFreePl;
import fcapp.backend.data.entity.FcExpRosea;
import fcapp.backend.job.JobProcessGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.ExpFreePlService;
import fcapp.backend.service.ExpRoseAService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Download")
@Route(value = "download", layout = MainLayout.class)
@RolesAllowed("USER")
public class DownloadView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SESSION_ATTORE = "ATTORE";
    private static final String SESSION_CAMPIONATO = "CAMPIONATO";
    private static final String PATH_OUTPUT_PDF = "PATH_OUTPUT_PDF";
    private static final String DATE_PATTERN = "yyyyddMM";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient JobProcessGiornata jobProcessGiornata;
    private final transient Environment env;
    private final transient ExpRoseAService expRoseAService;
    private final transient ExpFreePlService expFreePlService;
    private final transient AccessoService accessoService;

    private final Grid<FcExpFreePl> gridFreePl = new Grid<>();
    private final Grid<FcExpRosea> gridRosea = new Grid<>();

    private Button salvaRoseA;
    private Button salvaFreePl;

    private int resX;
    private int resY;

    public DownloadView(
            JobProcessGiornata jobProcessGiornata,
            Environment env,
            ExpRoseAService expRoseAService,
            ExpFreePlService expFreePlService,
            AccessoService accessoService) {

        this.jobProcessGiornata = jobProcessGiornata;
        this.env = env;
        this.expRoseAService = expRoseAService;
        this.expFreePlService = expFreePlService;
        this.accessoService = accessoService;

        log.info("DownloadView()");
    }

    @PostConstruct
    void init() {
        log.info("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initLayout();
    }

    private void initLayout() {
        UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
            resX = event.getScreenWidth();
            resY = event.getScreenHeight();
            log.info("resX {}", resX);
            log.info("resY {}", resY);
        });

        FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);
        if (attore == null) {
            return;
        }

        salvaRoseA = buildUpdateButton();
        salvaFreePl = buildUpdateButton();

        VerticalLayout roseALayout = new VerticalLayout();
        if (isAdmin(attore)) {
            roseALayout.add(salvaRoseA);
        }
        setRoseA(roseALayout);

        VerticalLayout freePlayersLayout = new VerticalLayout();
        if (isAdmin(attore)) {
            freePlayersLayout.add(salvaFreePl);
        }
        setFreePlayer(freePlayersLayout);

        FileSelect fileSelect = getFileSelect(resolvePdfRootDirectory());

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Rose A", roseALayout);
        tabSheet.add("Free Players", freePlayersLayout);
        tabSheet.add("Pdf", fileSelect);
        tabSheet.setSizeFull();

        add(tabSheet);
    }

    private Button buildUpdateButton() {
        Button button = new Button("Aggiorna");
        button.setIcon(VaadinIcon.DATABASE.create());
        button.addClickListener(this);
        return button;
    }

    private boolean isAdmin(FcAttore attore) {
        return attore.getRoles().stream().anyMatch(role -> role == Role.ADMIN);
    }

    private File resolvePdfRootDirectory() {
        String pathPdf = env.getProperty(PATH_OUTPUT_PDF);
        File rootFile = pathPdf != null ? new File(pathPdf) : null;

        if (rootFile != null) {
            log.info("pathPdf exists {}", rootFile.exists());
        }

        if (rootFile == null || !rootFile.exists()) {
            rootFile = new File(System.getProperty("user.dir"));
            log.info("fallback path exists {}", rootFile.exists());
        }

        return rootFile;
    }

    private @NonNull FileSelect getFileSelect(File rootFile) {
        FileSelect fileSelect = new FileSelect(rootFile);
        fileSelect.addValueChangeListener(event -> openSelectedFile(fileSelect.getValue()));
        fileSelect.setWidth(resX + "px");
        fileSelect.setHeight(resY + "px");
        fileSelect.setLabel("Select file");
        return fileSelect;
    }

    private void openSelectedFile(File file) {
        if (file == null) {
            return;
        }

        if (!file.isDirectory()) {
            Dialog dialog = new Dialog();
            dialog.add(createDialogLayout(dialog, file));
            dialog.open();

            Notification.show(file.getPath() + ", " + new java.util.Date(file.lastModified()) + ", " + file.length());
        } else {
            Notification.show(file.getPath() + ", " + new java.util.Date(file.lastModified()));
        }
    }

    private VerticalLayout createDialogLayout(Dialog dialog, File file) {
        int width = Math.max(resX - 200, 600);
        int height = Math.max(resY - 200, 400);

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(DownloadHandler.forFile(file));
        pdfViewer.setSizeFull();

        Button closeButton = new Button("Chiudi");
        closeButton.addClickListener(e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(pdfViewer, closeButton);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", width + "px").set("max-width", "100%");
        dialogLayout.getStyle().set("height", height + "px").set("max-height", "100%");
        dialogLayout.setAlignSelf(FlexComponent.Alignment.END, closeButton);

        return dialogLayout;
    }

    private void setRoseA(VerticalLayout layout) {
        List<FcExpRosea> items = expRoseAService.findAll();

        gridRosea.setItems(items);
        gridRosea.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridRosea.setAllRowsVisible(true);
        gridRosea.addThemeVariants(GridVariant.LUMO_COMPACT);

        addRoseAColumns(gridRosea);
        layout.add(new HorizontalLayout(buildExcelLink(gridRosea, "rosea")));
        layout.add(gridRosea);
    }

    private void addRoseAColumns(Grid<FcExpRosea> grid) {
        for (int i = 1; i <= 10; i++) {
            Column<FcExpRosea> ruoloColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpRosea::getR1);
                case 2 -> grid.addColumn(FcExpRosea::getR2);
                case 3 -> grid.addColumn(FcExpRosea::getR3);
                case 4 -> grid.addColumn(FcExpRosea::getR4);
                case 5 -> grid.addColumn(FcExpRosea::getR5);
                case 6 -> grid.addColumn(FcExpRosea::getR6);
                case 7 -> grid.addColumn(FcExpRosea::getR7);
                case 8 -> grid.addColumn(FcExpRosea::getR8);
                case 9 -> grid.addColumn(FcExpRosea::getR9);
                default -> grid.addColumn(FcExpRosea::getR10);
            };

            Column<FcExpRosea> squadraColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpRosea::getS1);
                case 2 -> grid.addColumn(FcExpRosea::getS2);
                case 3 -> grid.addColumn(FcExpRosea::getS3);
                case 4 -> grid.addColumn(FcExpRosea::getS4);
                case 5 -> grid.addColumn(FcExpRosea::getS5);
                case 6 -> grid.addColumn(FcExpRosea::getS6);
                case 7 -> grid.addColumn(FcExpRosea::getS7);
                case 8 -> grid.addColumn(FcExpRosea::getS8);
                case 9 -> grid.addColumn(FcExpRosea::getS9);
                default -> grid.addColumn(FcExpRosea::getS10);
            };

            Column<FcExpRosea> quotazioneColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpRosea::getQ1);
                case 2 -> grid.addColumn(FcExpRosea::getQ2);
                case 3 -> grid.addColumn(FcExpRosea::getQ3);
                case 4 -> grid.addColumn(FcExpRosea::getQ4);
                case 5 -> grid.addColumn(FcExpRosea::getQ5);
                case 6 -> grid.addColumn(FcExpRosea::getQ6);
                case 7 -> grid.addColumn(FcExpRosea::getQ7);
                case 8 -> grid.addColumn(FcExpRosea::getQ8);
                case 9 -> grid.addColumn(FcExpRosea::getQ9);
                default -> grid.addColumn(FcExpRosea::getQ10);
            };

            configureTripleColumns(ruoloColumn, squadraColumn, quotazioneColumn, i);
        }
    }

    private void setFreePlayer(VerticalLayout layout) {
        List<FcExpFreePl> items = expFreePlService.findAll();

        gridFreePl.setItems(items);
        gridFreePl.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridFreePl.setAllRowsVisible(true);
        gridFreePl.addThemeVariants(GridVariant.LUMO_COMPACT);

        addFreePlayerColumns(gridFreePl);
        layout.add(new HorizontalLayout(buildExcelLink(gridFreePl, "freePlayers")));
        layout.add(gridFreePl);
    }

    private void addFreePlayerColumns(Grid<FcExpFreePl> grid) {
        for (int i = 1; i <= 10; i++) {
            Column<FcExpFreePl> ruoloColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpFreePl::getR1);
                case 2 -> grid.addColumn(FcExpFreePl::getR2);
                case 3 -> grid.addColumn(FcExpFreePl::getR3);
                case 4 -> grid.addColumn(FcExpFreePl::getR4);
                case 5 -> grid.addColumn(FcExpFreePl::getR5);
                case 6 -> grid.addColumn(FcExpFreePl::getR6);
                case 7 -> grid.addColumn(FcExpFreePl::getR7);
                case 8 -> grid.addColumn(FcExpFreePl::getR8);
                case 9 -> grid.addColumn(FcExpFreePl::getR9);
                default -> grid.addColumn(FcExpFreePl::getR10);
            };

            Column<FcExpFreePl> squadraColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpFreePl::getS1);
                case 2 -> grid.addColumn(FcExpFreePl::getS2);
                case 3 -> grid.addColumn(FcExpFreePl::getS3);
                case 4 -> grid.addColumn(FcExpFreePl::getS4);
                case 5 -> grid.addColumn(FcExpFreePl::getS5);
                case 6 -> grid.addColumn(FcExpFreePl::getS6);
                case 7 -> grid.addColumn(FcExpFreePl::getS7);
                case 8 -> grid.addColumn(FcExpFreePl::getS8);
                case 9 -> grid.addColumn(FcExpFreePl::getS9);
                default -> grid.addColumn(FcExpFreePl::getS10);
            };

            Column<FcExpFreePl> quotazioneColumn = switch (i) {
                case 1 -> grid.addColumn(FcExpFreePl::getQ1);
                case 2 -> grid.addColumn(FcExpFreePl::getQ2);
                case 3 -> grid.addColumn(FcExpFreePl::getQ3);
                case 4 -> grid.addColumn(FcExpFreePl::getQ4);
                case 5 -> grid.addColumn(FcExpFreePl::getQ5);
                case 6 -> grid.addColumn(FcExpFreePl::getQ6);
                case 7 -> grid.addColumn(FcExpFreePl::getQ7);
                case 8 -> grid.addColumn(FcExpFreePl::getQ8);
                case 9 -> grid.addColumn(FcExpFreePl::getQ9);
                default -> grid.addColumn(FcExpFreePl::getQ10);
            };

            configureTripleColumns(ruoloColumn, squadraColumn, quotazioneColumn, i);
        }
    }

    private <T> void configureTripleColumns(
            Column<T> ruoloColumn,
            Column<T> squadraColumn,
            Column<T> quotazioneColumn,
            int index) {

        ruoloColumn.setKey("r" + index);
        ruoloColumn.setWidth("2rem").setFlexGrow(0);

        squadraColumn.setKey("s" + index);

        quotazioneColumn.setKey("q" + index);
        quotazioneColumn.setWidth("2rem").setFlexGrow(0);
    }

    private <T> Anchor buildExcelLink(Grid<T> grid, String title) {
        GridExporter<T> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setAutoSizeColumns(false);
        exporter.setTitle(title);
        exporter.setFileName(title + new SimpleDateFormat(DATE_PATTERN).format(Calendar.getInstance().getTime()));

        Anchor excelLink = new Anchor("", "Export to Excel");
        excelLink.setHref(exporter.getExcelStreamResource());
        excelLink.getElement().setAttribute("download", true);

        return excelLink;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            FcCampionato campionato = getSessionAttribute(SESSION_CAMPIONATO, FcCampionato.class);
            if (campionato == null) {
                return;
            }

            if (event.getSource() == salvaRoseA) {
                jobProcessGiornata.executeUpdateDbFcExpRoseA(false, campionato.getIdCampionato());
                gridRosea.setItems(expRoseAService.findAll());
                gridRosea.getDataProvider().refreshAll();

            } else if (event.getSource() == salvaFreePl) {
                jobProcessGiornata.executeUpdateDbFcExpRoseA(true, campionato.getIdCampionato());
                gridFreePl.setItems(expFreePlService.findAll());
                gridFreePl.getDataProvider().refreshAll();
            }

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
