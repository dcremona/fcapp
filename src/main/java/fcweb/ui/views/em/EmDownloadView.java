package fcweb.ui.views.em;

import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcExpRosea;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ExpRoseAService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Download")
@Route(value = "emdownnload", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmDownloadView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final Grid<FcExpRosea> gridRosea = new Grid<>();

	@Autowired
	private ExpRoseAService expRoseAController;

    @Autowired
	private AttoreService attoreController;

    @Autowired
	private JobProcessGiornata jobProcessGiornata;

	@Autowired
	private ResourceLoader resourceLoader;

	public List<FcAttore> squadre = new ArrayList<>();

	private Button salvaRoseA = null;
	private Button salvaStat = null;

	@Autowired
	private AccessoService accessoController;

	@PostConstruct
	void init() {
		LOG.info("init");
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

        FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		salvaRoseA = new Button("Aggiorna");
		salvaRoseA.setIcon(VaadinIcon.DATABASE.create());
		salvaRoseA.addClickListener(this);

		salvaStat = new Button("Aggiorna");
		salvaStat.setIcon(VaadinIcon.DATABASE.create());
		salvaStat.addClickListener(this);

		final VerticalLayout layout1 = new VerticalLayout();
		for (Role r : att.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				layout1.add(salvaRoseA);
				break;
			}
		}
		setRoseA(layout1);

        TabSheet tabSheet = new TabSheet();
		tabSheet.add("Rose Nazionali", layout1);
		tabSheet.setSizeFull();
		add(tabSheet);

	}

	private void setRoseA(VerticalLayout layout) {

		List<FcExpRosea> items = expRoseAController.findAll();

        gridRosea.setItems(items);
		gridRosea.setSelectionMode(Grid.SelectionMode.SINGLE);
		gridRosea.setAllRowsVisible(true);
		gridRosea.addThemeVariants(GridVariant.LUMO_COMPACT);

		for (int i = 1; i < 11; i++) {

			Column<FcExpRosea> sxColumn;
			Column<FcExpRosea> rxColumn;
			Column<FcExpRosea> qxColumn;
			if (i == 1) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS1);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ1);
			} else if (i == 2) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS2);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ2);
			} else if (i == 3) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS3);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ3);
			} else if (i == 4) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS4);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ4);
			} else if (i == 5) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS5);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ5);
			} else if (i == 6) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS6);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ6);
			} else if (i == 7) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS7);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ7);
			} else if (i == 8) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS8);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ8);
			} else if (i == 9) {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS9);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ9);
			} else {
				sxColumn = gridRosea.addColumn(FcExpRosea::getS10);
				rxColumn = getColumnR(gridRosea, i);
				qxColumn = gridRosea.addColumn(FcExpRosea::getQ10);
			}

			sxColumn.setKey("s" + i);
            sxColumn.setWidth("110px");
			// sxColumn.setResizable(false);
			sxColumn.setAutoWidth(true);

			rxColumn.setKey("r" + i);
            rxColumn.setWidth("60px");
			// rxColumn.setResizable(false);
			rxColumn.setAutoWidth(true);

			qxColumn.setKey("q" + i);
            qxColumn.setWidth("30px");
			// qxColumn.setResizable(false);
			qxColumn.setAutoWidth(true);

		}

		// Anchor downloadAsExcel = new Anchor(new
		// StreamResource("Nazionali.xlsx",Exporter.exportAsExcel(gridRosea)),"Download
		// As Excel");
		// Anchor downloadAsCSV = new Anchor(new
		// StreamResource("Nazionali.csv",Exporter.exportAsCSV(gridRosea)),"Download
		// As
		// CSV");
		// layout.add(new HorizontalLayout(downloadAsExcel,downloadAsCSV));

		GridExporter<FcExpRosea> exporter = GridExporter.createFor(gridRosea);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Nazionali");
		exporter.setFileName("Nazionali" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("","Export to Excel");
		excelLink.setHref(exporter.getExcelStreamResource());
		excelLink.getElement().setAttribute("download", true);
		layout.add(new HorizontalLayout(excelLink));

		layout.add(gridRosea);
	}

	private Column<FcExpRosea> getColumnR(Grid<FcExpRosea> grid, int i) {

        return grid.addColumn(new ComponentRenderer<>(f -> {
            HorizontalLayout cellLayout = new HorizontalLayout();
            cellLayout.setMargin(false);
            cellLayout.setPadding(false);
            cellLayout.setSpacing(false);
            cellLayout.setAlignItems(Alignment.STRETCH);
            cellLayout.setSizeFull();

			String ruolo = getString(i, f);

			if (("P".equals(ruolo) || "D".equals(ruolo) || "C".equals(ruolo) || "A".equals(ruolo))) {
                Image img = Utils.buildImage(ruolo.toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo.toLowerCase() + ".png"));
                cellLayout.add(img);
            }
            return cellLayout;
        }));

	}

	private @Nullable String getString(int i, FcExpRosea f) {
		String ruolo = null;
		if (f != null) {
			if (i == 1) {
				ruolo = f.getR1();
			} else if (i == 2) {
				ruolo = f.getR2();
			} else if (i == 3) {
				ruolo = f.getR3();
			} else if (i == 4) {
				ruolo = f.getR4();
			} else if (i == 5) {
				ruolo = f.getR5();
			} else if (i == 6) {
				ruolo = f.getR6();
			} else if (i == 7) {
				ruolo = f.getR7();
			} else if (i == 8) {
				ruolo = f.getR8();
			} else if (i == 9) {
				ruolo = f.getR9();
			} else if (i == 10) {
				ruolo = f.getR10();
			}
		}
		return ruolo;
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

			} else if (event.getSource() == salvaStat) {
				jobProcessGiornata.statistiche(campionato);
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

}