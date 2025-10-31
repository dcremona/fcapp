package fcweb.ui.views.seriea;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcExpFreePl;
import fcweb.backend.data.entity.FcExpRosea;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ExpFreePlService;
import fcweb.backend.service.ExpRoseAService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Download")
@Route(value = "downnload", layout = MainLayout.class)
@RolesAllowed("USER")
public class DownloadView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Grid<FcExpFreePl> gridFreePl = new Grid<>();
	private Grid<FcExpRosea> gridRosea = new Grid<>();

	@Autowired
	private ExpRoseAService expRoseAController;

	@Autowired
	private ExpFreePlService expFreePlController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	public List<FcAttore> squadre = new ArrayList<FcAttore>();

	private Button salvaRoseA = null;
	private Button salvaFreePl = null;

	@Autowired
	private AccessoService accessoController;

	int resX = 0;
	int resY = 0;

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initData();
		initLayout();
	}

	private void initData() {
		squadre = attoreController.findByActive(true);
	}

	private void initLayout() {

		UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
			resX = event.getScreenWidth();
			resY = event.getScreenHeight();
			log.info("resX " + resX);
			log.info("resY " + resY);
		});

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");

		salvaRoseA = new Button("Aggiorna");
		salvaRoseA.setIcon(VaadinIcon.DATABASE.create());
		salvaRoseA.addClickListener(this);

		salvaFreePl = new Button("Aggiorna");
		salvaFreePl.setIcon(VaadinIcon.DATABASE.create());
		salvaFreePl.addClickListener(this);

		final VerticalLayout layout1 = new VerticalLayout();
		FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				layout1.add(salvaRoseA);
			}
		}

		setRoseA(layout1);

		final VerticalLayout layout2 = new VerticalLayout();
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				layout2.add(salvaFreePl);
			}
		}
		setFreePlayer(layout2);

		String pathPdf = (String) p.get("PATH_OUTPUT_PDF");
		File rootFile3 = new File(pathPdf);
		log.info(" pathPdf " + rootFile3.exists());
		if (!rootFile3.exists()) {
			String basePathData = System.getProperty("user.dir");
			rootFile3 = new File(basePathData);
			log.info(" pathPdf " + rootFile3.exists());
		}
		FileSelect fileSelect = new FileSelect(rootFile3);
		fileSelect.addValueChangeListener(event -> {
			File file = fileSelect.getValue();
			Date date = new Date(file.lastModified());
			if (!file.isDirectory()) {
				Dialog dialog = new Dialog();
				VerticalLayout dialogLayout = createDialogLayout(dialog, file);
				dialog.add(dialogLayout);
				dialog.open();

				Notification.show(file.getPath() + ", " + date + ", " + file.length());

			} else {
				Notification.show(file.getPath() + ", " + date);
			}
		});
		fileSelect.setWidth(resX + "px");
		fileSelect.setHeight(resY + "px");
		fileSelect.setLabel("Select file");

		TabSheet tabSheet = new TabSheet();
		tabSheet.add("Rose A", layout1);
		tabSheet.add("Free Players", layout2);
		tabSheet.add("Pdf", fileSelect);
		tabSheet.setSizeFull();
		this.add(tabSheet);
	}

	private VerticalLayout createDialogLayout(Dialog dialog, File f) {
		VerticalLayout dialogLayout = null;

		int resX2 = resX - 200;
		int resY2 = resY - 200;

		PdfViewer pdfViewer = new PdfViewer();
		pdfViewer.setSrc(DownloadHandler.forFile(f));
		pdfViewer.setSizeFull();

		Button closeButton = new Button("Chiudi");
		closeButton.addClickListener(e -> dialog.close());

		dialogLayout = new VerticalLayout(pdfViewer,closeButton);
		dialogLayout.setPadding(false);
		dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
		dialogLayout.getStyle().set("width", resX2 + "px").set("max-width", "100%");
		dialogLayout.getStyle().set("height", resY2 + "px").set("max-height", "100%");
		dialogLayout.setAlignSelf(FlexComponent.Alignment.END, closeButton);

		return dialogLayout;
	}

	private void setRoseA(VerticalLayout layout) {

		List<FcExpRosea> items = expRoseAController.findAll();

		gridRosea.setItems(items);
		gridRosea.setSelectionMode(Grid.SelectionMode.SINGLE);
		gridRosea.setAllRowsVisible(true);
		gridRosea.addThemeVariants(GridVariant.LUMO_COMPACT);

		for (int i = 1; i < 11; i++) {

			Column<FcExpRosea> rxColumn = null;
			Column<FcExpRosea> sxColumn = null;
			Column<FcExpRosea> qxColumn = null;
			if (i == 1) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR1);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS1);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ1);
			} else if (i == 2) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR2);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS2);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ2);
			} else if (i == 3) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR3);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS3);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ3);
			} else if (i == 4) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR4);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS4);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ4);
			} else if (i == 5) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR5);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS5);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ5);
			} else if (i == 6) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR6);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS6);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ6);
			} else if (i == 7) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR7);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS7);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ7);
			} else if (i == 8) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR8);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS8);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ8);
			} else if (i == 9) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR9);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS9);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ9);
			} else if (i == 10) {
				rxColumn = gridRosea.addColumn(FcExpRosea::getR10);
				sxColumn = gridRosea.addColumn(FcExpRosea::getS10);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ10);
			}
			if (rxColumn != null) {
				rxColumn.setKey("r" + i);
				rxColumn.setWidth("2rem").setFlexGrow(0);
			}
			if (sxColumn != null) {
				sxColumn.setKey("s" + i);
			}
			if (qxColumn != null) {
				qxColumn.setKey("q" + i);
				qxColumn.setWidth("2rem").setFlexGrow(0);
			}
		}

		GridExporter<FcExpRosea> exporter = GridExporter.createFor(gridRosea);
		exporter.setAutoAttachExportButtons(false);
		exporter.setAutoSizeColumns(false);
		exporter.setTitle("roseA");
		exporter.setFileName("roseA" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("","Export to Excel");
		excelLink.setHref(exporter.getExcelStreamResource());
		excelLink.getElement().setAttribute("download", true);
		layout.add(new HorizontalLayout(excelLink));

		layout.add(gridRosea);
	}

	private void setFreePlayer(VerticalLayout layout) {

		List<FcExpFreePl> items = expFreePlController.findAll();
		gridFreePl.setItems(items);
		gridFreePl.setSelectionMode(Grid.SelectionMode.SINGLE);
		gridFreePl.setAllRowsVisible(true);
		gridFreePl.addThemeVariants(GridVariant.LUMO_COMPACT);

		for (int i = 1; i < 11; i++) {

			Column<FcExpFreePl> rxColumn = null;
			Column<FcExpFreePl> sxColumn = null;
			Column<FcExpFreePl> qxColumn = null;
			if (i == 1) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR1);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS1);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ1);
			} else if (i == 2) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR2);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS2);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ2);
			} else if (i == 3) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR3);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS3);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ3);
			} else if (i == 4) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR4);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS4);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ4);
			} else if (i == 5) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR5);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS5);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ5);
			} else if (i == 6) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR6);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS6);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ6);
			} else if (i == 7) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR7);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS7);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ7);
			} else if (i == 8) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR8);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS8);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ8);
			} else if (i == 9) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR9);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS9);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ9);
			} else if (i == 10) {
				rxColumn = gridFreePl.addColumn(FcExpFreePl::getR10);
				sxColumn = gridFreePl.addColumn(FcExpFreePl::getS10);
				qxColumn = gridFreePl.addColumn(FcExpFreePl::getQ10);
			}

			if (rxColumn != null) {
				rxColumn.setKey("r" + i);
				rxColumn.setWidth("2rem").setFlexGrow(0);
			}
			if (sxColumn != null) {
				sxColumn.setKey("s" + i);
				//sxColumn.setAutoWidth(true);
			}
			if (qxColumn != null) {
				qxColumn.setKey("q" + i);
				qxColumn.setWidth("2rem").setFlexGrow(0);
			}
		}

		GridExporter<FcExpFreePl> exporter = GridExporter.createFor(gridFreePl);
		exporter.setAutoAttachExportButtons(false);
		exporter.setAutoSizeColumns(false);
		exporter.setTitle("freePlayers");
		exporter.setFileName("freePlayers" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("","Export to Excel");
		excelLink.setHref(exporter.getExcelStreamResource());
		excelLink.getElement().setAttribute("download", true);
		layout.add(new HorizontalLayout(excelLink));

		layout.add(gridFreePl);

	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

			if (event.getSource() == salvaRoseA) {
				jobProcessGiornata.executeUpdateDbFcExpRoseA(false, campionato.getIdCampionato());

				List<FcExpRosea> items = expRoseAController.findAll();
				gridRosea.setItems(items);
				gridRosea.getDataProvider().refreshAll();

			} else if (event.getSource() == salvaFreePl) {
				jobProcessGiornata.executeUpdateDbFcExpRoseA(true, campionato.getIdCampionato());

				List<FcExpFreePl> items = expFreePlController.findAll();
				gridFreePl.setItems(items);
				gridFreePl.getDataProvider().refreshAll();
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

}