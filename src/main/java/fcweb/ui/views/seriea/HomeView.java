package fcweb.ui.views.seriea;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.Calendario;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcGiornataRis;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.GiornataRisService;
import fcweb.backend.service.GiornataService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Home")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("USER")
public class HomeView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GiornataService giornataController;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiornataRisService giornataRisController;

	@Autowired
	private AccessoService accessoController;

	public HomeView() {
		log.info("HomeView()");
	}

	@PostConstruct
	void init() {

		log.info("init");

		try {

			if (!Utils.isValidVaadinSession()) {
				return;
			}

			accessoController.insertAccesso(this.getClass().getName());

			add(buildInfoGiornate());

			add(builLayoutAvviso());

			add(builLayoutRisultati());

		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private HorizontalLayout buildInfoGiornate() {

		HorizontalLayout gridWrapper = new HorizontalLayout();
		gridWrapper.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		gridWrapper.setSizeFull();

		if (VaadinSession.getCurrent().getAttribute("GIORNATA_INFO") != null) {
			FcGiornataInfo giornataInfoCurr = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

			String title = null;
			if (giornataInfoCurr.getCodiceGiornata() > 1) {
				FcGiornataInfo giornataInfoPrev = giornataInfoController
						.findByCodiceGiornata(giornataInfoCurr.getCodiceGiornata() - 1);

				final VerticalLayout layoutSx = new VerticalLayout();
				title = "Ultima Giornata - " + Utils.buildInfoGiornata(giornataInfoPrev);
				Div lblInfoSx = new Div();
				lblInfoSx.setText(title);
				lblInfoSx.getStyle().set("font-size", "16px");
				lblInfoSx.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
				lblInfoSx.setSizeFull();
				layoutSx.add(lblInfoSx);
				layoutSx.add(createGridGiornata(getDataTable(giornataInfoPrev)));

				gridWrapper.add(layoutSx);
			}

			final VerticalLayout layoutDx = new VerticalLayout();
			title = "Prossima Giornata - " + Utils.buildInfoGiornata(giornataInfoCurr);
			Div lblInfoDx = new Div();
			lblInfoDx.setText(title);
			lblInfoDx.getStyle().set("font-size", "16px");
			lblInfoDx.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
			lblInfoDx.setSizeFull();
			layoutDx.add(lblInfoDx);
			layoutDx.add(createGridGiornata(getDataTable(giornataInfoCurr)));

			gridWrapper.add(layoutDx);

		}

		return gridWrapper;
	}

	private List<Calendario> getDataTable(FcGiornataInfo ggInfo) {

		List<FcGiornata> all = giornataController.findByFcGiornataInfo(ggInfo);

		List<Calendario> list = new ArrayList<>();

		int id = 1;
		for (FcGiornata g : all) {

			DecimalFormat myFormatter = new DecimalFormat("#0.00");

			Double dTotCasa = g.getTotCasa() != null ? g.getTotCasa().doubleValue() / Costants.DIVISORE_100 : 0;
			String sTotCasa = myFormatter.format(dTotCasa);

			Double dTotFuori = g.getTotFuori() != null ? g.getTotFuori().doubleValue() / Costants.DIVISORE_100 : 0;
			String sTotFuori = myFormatter.format(dTotFuori);

			Calendario calendario = new Calendario();
			calendario.setId(id);
			calendario.setAttoreCasa(g.getFcAttoreByIdAttoreCasa().getDescAttore());
			calendario.setRisultato(sTotCasa + " - " + sTotFuori);
			calendario.setAttoreFuori(g.getFcAttoreByIdAttoreFuori().getDescAttore());
			calendario.setPunteggio(g.getGolCasa() != null ? g.getGolCasa() + " - " + g.getGolFuori() : "-");

			list.add(calendario);

			id++;
		}

		return list;
	}

	private Grid<Calendario> createGridGiornata(List<Calendario> items) {

		Grid<Calendario> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);

		grid.addColumn(c -> c.getAttoreCasa()).setAutoWidth(true);
		grid.addColumn(c -> c.getPunteggio()).setAutoWidth(true);
		grid.addColumn(c -> c.getAttoreFuori()).setAutoWidth(true);
		grid.addColumn(c -> c.getRisultato()).setAutoWidth(true);

		return grid;
	}

	private VerticalLayout builLayoutAvviso() {

		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		String nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
		long millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

		final VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);

		HorizontalLayout cssLayout = new HorizontalLayout();

		Span lblInfo = new Span("Prossima Giornata: " + Utils.buildInfoGiornata(giornataInfo));
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

	private VerticalLayout builLayoutRisultati() {

		FormLayout layout = new FormLayout();
		layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layout.setResponsiveSteps(new ResponsiveStep("1px", 1), new ResponsiveStep("500px", 2),
				new ResponsiveStep("600px", 3), new ResponsiveStep("700px", 4), new ResponsiveStep("800px", 5));

		FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		List<FcGiornataRis> l = giornataRisController.findByFcAttoreOrderByFcGiornataInfoAsc(attore);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer from = campionato.getStart();

		for (FcGiornataRis fcGiornataRis : l) {
			int cg = fcGiornataRis.getFcGiornataInfo().getCodiceGiornata();
			if (cg >= from) {
				FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(cg);
				Card card = getCard(giornataInfo, attore, fcGiornataRis);
				layout.add(card);
			}
		}

		final VerticalLayout layoutRis = new VerticalLayout();
		layoutRis.add(layout);

		return layoutRis;
	}

	private Card getCard(FcGiornataInfo giornataInfo, FcAttore attore, FcGiornataRis fcGiornataRis) {

		Card card = new Card();

		String badgeText = "";
		String badgeTheme = "";
		if (fcGiornataRis.getIdRisPartita() == 0) {
			badgeText = "Pareggio";
			badgeTheme = "badge pill";
		} else if (fcGiornataRis.getIdRisPartita() == 1) {
			badgeText = "Vinta";
			badgeTheme = "badge success";
		} else if (fcGiornataRis.getIdRisPartita() == 2) {
			badgeText = "Persa";
			badgeTheme = "badge error";
		}

		String descPartita = "";
		String punteggio = "";
		String totPunteggio = "";

		List<FcGiornata> all = giornataController.findByFcGiornataInfo(giornataInfo);
		for (FcGiornata g : all) {
			if (attore.getIdAttore() == g.getFcAttoreByIdAttoreCasa().getIdAttore()
					|| attore.getIdAttore() == g.getFcAttoreByIdAttoreFuori().getIdAttore()) {
				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double dTotCasa = g.getTotCasa() != null ? g.getTotCasa().doubleValue() / Costants.DIVISORE_100 : 0;
				String sTotCasa = myFormatter.format(dTotCasa);
				Double dTotFuori = g.getTotFuori() != null ? g.getTotFuori().doubleValue() / Costants.DIVISORE_100 : 0;
				String sTotFuori = myFormatter.format(dTotFuori);
				descPartita = g.getFcAttoreByIdAttoreCasa().getDescAttore() + " "
						+ g.getFcAttoreByIdAttoreFuori().getDescAttore();
				punteggio = g.getGolCasa() != null ? g.getGolCasa() + " - " + g.getGolFuori() : "-";
				totPunteggio = sTotCasa + " - " + sTotFuori;

				break;
			}
		}

		card.setTitle(new Div(giornataInfo.getDescGiornataFc()));
		card.setSubtitle(new Div(Utils.buildInfoGiornataRight(giornataInfo)));

		Span badge = new Span(badgeText);
		badge.getElement().getThemeList().add(badgeTheme);
		card.setHeaderSuffix(badge);

		card.add(descPartita + " " + punteggio);

		Span footer = new Span(totPunteggio);
		footer.getElement().getThemeList().add("badge contrast pill");
		card.addToFooter(footer);

		return card;
	}

}