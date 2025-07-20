package fcweb.ui.views.seriea;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.job.JobProcessFileCsv;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.GiornataGiocatoreService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "squalind", layout = MainLayout.class)
@RolesAllowed("USER")
@PageTitle("Squalificati-Indisponibili")
public class SqualificatiIndisponibiliView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>> {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private GiornataGiocatoreService giornataGiocatoreService;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	private Button salvaDb;
	private Grid<FcGiornataGiocatore> tableSqualificati;
	private Grid<FcGiornataGiocatore> tableInfortunati;

	@Autowired
	private ResourceLoader resourceLoader;

	public SqualificatiIndisponibiliView() {
		log.info("SqualificatiIndisponibiliView()");
	}

	@PostConstruct
	void init() {
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initLayout();
	}

	private void initLayout() {

		FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

		boolean isAdmin = false;
		for (Role r : attore.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				isAdmin = true;
				break;
			}
		}

		salvaDb = new Button("Salva " + giornataInfo.getDescGiornata());
		salvaDb.setIcon(VaadinIcon.DATABASE.create());
		salvaDb.addClickListener(this);
		salvaDb.setVisible(isAdmin);

		this.add(salvaDb);

		tableSqualificati = getTableSqualificatiInfortunati();

		VerticalLayout layoutSqualificati = new VerticalLayout();
		layoutSqualificati.setMargin(true);
		layoutSqualificati.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutSqualificati.add(tableSqualificati);
		Details panelSqualificati = new Details("Squalificati", layoutSqualificati);
		panelSqualificati.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelSqualificati.setOpened(true);

		this.add(panelSqualificati);

		tableInfortunati = getTableSqualificatiInfortunati();

		VerticalLayout layoutInfortunati = new VerticalLayout();
		layoutInfortunati.setMargin(true);
		layoutInfortunati.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutInfortunati.add(tableInfortunati);
		Details panelInfortunati = new Details("Infortunati", layoutInfortunati);
		panelInfortunati.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelInfortunati.setOpened(true);

		this.add(panelInfortunati);

		try {

			List<FcGiornataGiocatore> listSqualificatiInfortunati = giornataGiocatoreService.findByCustonm(giornataInfo,
					null);
			ArrayList<FcGiornataGiocatore> listSqualificati = new ArrayList<>();
			ArrayList<FcGiornataGiocatore> listInfortunati = new ArrayList<>();

			for (FcGiornataGiocatore gg : listSqualificatiInfortunati) {
				if (gg.isSqualificato()) {
					listSqualificati.add(gg);
				} else if (gg.isInfortunato()) {
					listInfortunati.add(gg);
				}
			}

			log.info("listSqualificati " + listSqualificati.size());
			tableSqualificati.setItems(listSqualificati);
			tableSqualificati.getDataProvider().refreshAll();

			log.info("listInfortunati " + listInfortunati.size());
			tableInfortunati.setItems(listInfortunati);
			tableInfortunati.getDataProvider().refreshAll();

		} catch (Exception ex2) {
			log.error("ex2 " + ex2.getMessage());
		}

	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		try {
			if (event.getSource() == salvaDb) {
				log.info("SALVA");
				Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
				FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

				String basePathData = (String) p.get("PATH_TMP");
				String urlFanta = (String) p.get("URL_FANTA");
				String basePath = basePathData;

				giornataGiocatoreService.deleteByCustonm(giornataInfo);

				// **************************************
				// DOWNLOAD FILE SQUALIFICATI
				// **************************************
				String httpUrlSqualificati = urlFanta + "giocatori-squalificati.asp";
				log.info("httpUrlSqualificati " + httpUrlSqualificati);
				String fileName1 = "SQUALIFICATI_" + giornataInfo.getCodiceGiornata();
				JobProcessFileCsv jobCsv = new JobProcessFileCsv();
				jobCsv.downloadCsvSqualificatiInfortunati(httpUrlSqualificati, basePath, fileName1);

				String fileName = basePathData + fileName1 + ".csv";
				jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, true, false);

				// **************************************
				// DOWNLOAD FILE INFORTUNATI
				// **************************************
				String httpUrlInfortunati = urlFanta + "giocatori-infortunati.asp";
				log.info("httpUrlInfortunati " + httpUrlInfortunati);
				String fileName2 = "INFORTUNATI_" + giornataInfo.getCodiceGiornata();
				jobCsv.downloadCsvSqualificatiInfortunati(httpUrlInfortunati, basePath, fileName2);

				fileName = basePathData + fileName2 + ".csv";
				jobProcessGiornata.initDbGiornataGiocatore(giornataInfo, fileName, false, true);

				List<FcGiornataGiocatore> listSqualificatiInfortunati = giornataGiocatoreService
						.findByCustonm(giornataInfo, null);
				ArrayList<FcGiornataGiocatore> listSqualificati = new ArrayList<>();
				ArrayList<FcGiornataGiocatore> listInfortunati = new ArrayList<>();

				for (FcGiornataGiocatore gg : listSqualificatiInfortunati) {
					if (gg.isSqualificato()) {
						listSqualificati.add(gg);
					} else if (gg.isInfortunato()) {
						listInfortunati.add(gg);
					}
				}

				log.info("listSqualificati " + listSqualificati.size());
				tableSqualificati.setItems(listSqualificati);
				tableSqualificati.getDataProvider().refreshAll();

				log.info("listInfortunati " + listInfortunati.size());
				tableInfortunati.setItems(listInfortunati);
				tableInfortunati.getDataProvider().refreshAll();

			}
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private Grid<FcGiornataGiocatore> getTableSqualificatiInfortunati() {

		Grid<FcGiornataGiocatore> grid = new Grid<>();
		grid.setItems(new ArrayList<>());
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);

		Column<FcGiornataGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(gg -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			FcGiocatore g = gg.getFcGiocatore();
			if (g != null) {
				Image img = Utils.buildImage(g.getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader
						.getResource(Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(false);
		ruoloColumn.setHeader(Costants.RUOLO);
		ruoloColumn.setAutoWidth(true);

		Column<FcGiornataGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(gg -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			FcGiocatore g = gg.getFcGiocatore();
			if (g != null) {
				try {
					Image img = Utils.getImage(g.getNomeImg(), g.getImgSmall().getBinaryStream());
					cellLayout.add(img);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				Span lblGiocatore = new Span(g.getCognGiocatore());
				cellLayout.add(lblGiocatore);
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcGiornataGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(gg -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			FcGiocatore g = gg.getFcGiocatore();
			if (g != null && g.getFcSquadra() != null) {
				FcSquadra sq = g.getFcSquadra();
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Span lblSquadra = new Span(g.getFcSquadra().getNomeSquadra());
				cellLayout.add(lblSquadra);
			}
			return cellLayout;

		}));
		nomeSquadraColumn.setSortable(false);
		nomeSquadraColumn.setHeader(Costants.SQUADRA);
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcGiornataGiocatore> noteColumn = grid.addColumn(g -> g.getNote());
		noteColumn.setSortable(false);
		noteColumn.setHeader("Note");
		noteColumn.setAutoWidth(true);

		return grid;
	}

}