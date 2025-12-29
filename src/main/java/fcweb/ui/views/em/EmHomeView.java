package fcweb.ui.views.em;

import java.io.Serial;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.CalendarioCompetizioneService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "emhome", layout = MainLayout.class)
// @RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Home")
@RolesAllowed("USER")
public class EmHomeView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment env;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private CalendarioCompetizioneService calendarioTimController;

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private SquadraService squadraController;

	public EmHomeView() {
		LOG.info("EmHomeView()");
	}

	@PostConstruct
	void init() {
		try {
			LOG.info("init");
			if (!Utils.isValidVaadinSession()) {
				return;
			}
			accessoController.insertAccesso(this.getClass().getName());

			Image img = Utils.buildImage(env.getProperty("img.logo"), resourceLoader.getResource(Costants.CLASSPATH_IMAGES + env.getProperty("img.logo")));
			this.add(img);
			setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, img);

			this.add(builLayoutAvviso());

			buildGiornate();

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	private void buildGiornate() {

		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		List<FcGiornataInfo> giornate = giornataInfoController.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);

        TabSheet tabSheet = new TabSheet();
		for (FcGiornataInfo g : giornate) {
			List<FcCalendarioCompetizione> listPartite = calendarioTimController.findByIdGiornataOrderByDataAsc(g.getCodiceGiornata());
			Grid<FcCalendarioCompetizione> tablePartite = getTablePartite(listPartite);
			final VerticalLayout layout = new VerticalLayout();
			layout.setMargin(false);
			layout.setPadding(false);
			layout.setSpacing(false);
			layout.add(tablePartite);

			// Tab tab = tabs.add(g.getDescGiornata(), layout, false);
			Tab tab = tabSheet.add(g.getDescGiornata(), layout);
			if (g.getDescGiornata().equals(giornataInfo.getDescGiornata())) {
                LOG.info(" selected tab {}", giornataInfo.getDescGiornata());
				// tabs.select(tab);
				tabSheet.setSelectedTab(tab);
			}
		}

		// this.add(tabs, container);
		this.add(tabSheet);
	}

	private Grid<FcCalendarioCompetizione> getTablePartite(
			List<FcCalendarioCompetizione> listPartite) {

		Grid<FcCalendarioCompetizione> grid = new Grid<>();
		grid.setItems(listPartite);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);

        Column<FcCalendarioCompetizione> dataColumn = grid.addColumn(new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);
		// dataColumn.setFlexGrow(2);

		Column<FcCalendarioCompetizione> nomeSquadraCasaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
            if (s != null && s.getSquadraCasa() != null) {
                FcSquadra sq = squadraController.findByNomeSquadra(s.getSquadraCasa());
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						LOG.error(e.getMessage());
					}
				}
				Span lblSquadra = new Span(s.getSquadraCasa().substring(0, 3));
				cellLayout.add(lblSquadra);
			}
			return cellLayout;

		}));
		nomeSquadraCasaColumn.setSortable(false);
		nomeSquadraCasaColumn.setAutoWidth(true);

		Column<FcCalendarioCompetizione> nomeSquadraFuoriColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
            if (s != null && s.getSquadraCasa() != null) {
                FcSquadra sq = squadraController.findByNomeSquadra(s.getSquadraFuori());
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						LOG.error(e.getMessage());
					}
				}
				Span lblSquadra = new Span(s.getSquadraFuori().substring(0, 3));
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraFuoriColumn.setSortable(false);
		nomeSquadraFuoriColumn.setAutoWidth(true);

		Column<FcCalendarioCompetizione> risultatoColumn = grid.addColumn(c -> c != null && c.getRisultato() != null ? c.getRisultato() : "");
		risultatoColumn.setSortable(false);
		risultatoColumn.setAutoWidth(true);
		// risultatoColumn.setFlexGrow(2);

		return grid;
	}

	private VerticalLayout builLayoutAvviso() {

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		String nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
		long millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");
        LOG.info("millisDiff {}", millisDiff);

		final VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);

		HorizontalLayout cssLayout = new HorizontalLayout();
		Span lblInfo = new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato));
		cssLayout.add(lblInfo);
		layoutAvviso.add(cssLayout);

		HorizontalLayout cssLayout2 = new HorizontalLayout();
		Span lblInfo2 = new Span("Consegna Formazione entro: " + nextDate);
		cssLayout2.add(lblInfo2);
		layoutAvviso.add(cssLayout2);

		SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
		timer.setHours(true);
		timer.setMinutes(true);
		timer.setFractions(false);
		timer.start();
		timer.isRunning();
		timer.addTimerEndEvent(ev -> Notification.show("Timer ended"));
		layoutAvviso.add(timer);

		return layoutAvviso;
	}

}