package fcweb.ui.views.seriea;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.badge.Badge;
import org.vaadin.addons.badge.Badge.BadgeVariant;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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
public class HomeView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GiornataService giornataController;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiornataRisService giornataRisController;

	@Autowired
	private AccessoService accessoController;

//	@Autowired
//	private ClassificaTotalePuntiService classificaTotalePuntiController;

	public HomeView() {
		LOG.info("HomeView()");
	}

	@PostConstruct
	void init() {

		LOG.info("init");

		try {

			if (!Utils.isValidVaadinSession()) {
				return;
			}

			accessoController.insertAccesso(this.getClass().getName());

			add(buildInfoGiornate());

			add(builLayoutAvviso());

			add(builLayoutRisultati());

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	private HorizontalLayout buildInfoGiornate() throws Exception {

		HorizontalLayout gridWrapper = new HorizontalLayout();
		gridWrapper.getStyle().set("border", Costants.BORDER_COLOR);
		gridWrapper.setSizeFull();

		if (VaadinSession.getCurrent().getAttribute("GIORNATA_INFO") != null) {
			FcGiornataInfo giornataInfoCurr = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

			String title = null;
			if (giornataInfoCurr.getCodiceGiornata() > 1) {
				FcGiornataInfo giornataInfoPrev = giornataInfoController.findByCodiceGiornata(giornataInfoCurr.getCodiceGiornata() - 1);

				final VerticalLayout layoutSx = new VerticalLayout();
				title = "Ultima Giornata - " + Utils.buildInfoGiornata(giornataInfoPrev);
				Div lblInfoSx = new Div();
				lblInfoSx.setText(title);
				lblInfoSx.getStyle().set("font-size", "16px");
				lblInfoSx.getStyle().set("background", Costants.LIGHT_BLUE);
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
			lblInfoDx.getStyle().set("background", Costants.LIGHT_BLUE);
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
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		// grid.setSizeFull();

		grid.addColumn(c -> c.getAttoreCasa()).setAutoWidth(true);
		grid.addColumn(c -> c.getPunteggio()).setAutoWidth(true);
		grid.addColumn(c -> c.getAttoreFuori()).setAutoWidth(true);
		grid.addColumn(c -> c.getRisultato()).setAutoWidth(true);

		return grid;
	}

	private VerticalLayout builLayoutAvviso() throws Exception {

		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		String nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
		long millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");
		LOG.info("millisDiff " + millisDiff);

		final VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set("border", Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set("background", Costants.YELLOW);

		HorizontalLayout cssLayout = new HorizontalLayout();

		Span lblInfo = new Span	("Prossima Giornata: " + Utils.buildInfoGiornata(giornataInfo));
		cssLayout.add(lblInfo);
		layoutAvviso.add(cssLayout);

		HorizontalLayout cssLayout2 = new HorizontalLayout();
		Span lblInfo2 = new Span("Consegna Formazione entro: " + nextDate);
		cssLayout2.add(lblInfo2);
		layoutAvviso.add(cssLayout2);

//		SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
//		timer.setHours(true);
//		timer.setMinutes(true);
//		timer.setFractions(false);
//		timer.start();
//		timer.isRunning();
//		timer.addTimerEndEvent(ev -> Notification.show("Timer ended"));
//		layoutAvviso.add(timer);

		return layoutAvviso;
	}

	private VerticalLayout builLayoutRisultati() throws Exception {

		FormLayout layout = new FormLayout();
		layout.getStyle().set("border", Costants.BORDER_COLOR);
		layout.setResponsiveSteps(new ResponsiveStep("1px",1), new ResponsiveStep("500px",2), new ResponsiveStep("600px",3), new ResponsiveStep("700px",4), new ResponsiveStep("800px",5));

		FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		List<FcGiornataRis> l = giornataRisController.findByFcAttoreOrderByFcGiornataInfoAsc(attore);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		LOG.info("from " + "" + from);
		LOG.info("to " + "" + to);

		for (FcGiornataRis fcGiornataRis : l) {
			int cg = fcGiornataRis.getFcGiornataInfo().getCodiceGiornata();
			if (cg >= from) {
				FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(cg);
//				FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
//				Svg svg = getSvg(giornataInfo, attore, fcGiornataRis, totPunti);
//				layout.add(svg);
				Badge badge = getBadge(giornataInfo, attore, fcGiornataRis);
				layout.add(badge);

			}
		}

		final VerticalLayout layoutRis = new VerticalLayout();
		layoutRis.getStyle().set("border", Costants.BORDER_COLOR);
		layoutRis.getStyle().set("background", Costants.GREEN);

		Span lblInfoRisSx = new Span("Ultime Partite");
		lblInfoRisSx.getStyle().set("font-size", "14px");
		layoutRis.add(lblInfoRisSx);

		layoutRis.add(layout);

		return layoutRis;
	}
	
	private Badge getBadge(FcGiornataInfo giornataInfo, FcAttore attore,FcGiornataRis fcGiornataRis) {
		
		String text = ""+giornataInfo.getIdGiornataFc();
		
		Badge badge = new Badge(text);
		
		if (fcGiornataRis.getIdRisPartita() == 0) {
			// nomeImg = "pareggio.png";
			badge.setVariant(BadgeVariant.NORMAL);
		} else if (fcGiornataRis.getIdRisPartita() == 1) {
			// "vinta.png";
			badge.setVariant(BadgeVariant.SUCCESS);
		} else if (fcGiornataRis.getIdRisPartita() == 2) {
			// "persa.png";
			badge.setVariant(BadgeVariant.ERROR);
		}
		//badge.setIcon(VaadinIcon.VAADIN_H.create());
		badge.setPill(true);
		badge.setPrimary(true);

		
		Calendario c = getInfoPartita(giornataInfo,attore);
		
		String p = c.getAttoreCasa() + " - " + c.getAttoreFuori() ;
		String ris = c.getPunteggio();
		badge.setTooltipText(p + " " + ris);
		
		return badge;
	}
	

//	private Svg getSvg(FcGiornataInfo giornataInfo, FcAttore attore,
//			FcGiornataRis fcGiornataRis, FcClassificaTotPt totPunti) {
//
//		Svg draw = new Svg();
//
//		String fillColor = "";
//		if (fcGiornataRis.getIdRisPartita() == 0) {
//			// nomeImg = "pareggio.png";
//			fillColor = "#e6e600";
//		} else if (fcGiornataRis.getIdRisPartita() == 1) {
//			// "vinta.png";
//			fillColor = "#008000";
//		} else if (fcGiornataRis.getIdRisPartita() == 2) {
//			// "persa.png";
//			fillColor = "#ff5050";
//		}
//
//		int x = 45;
//		int y = 40;
//		int codiceGiornata = giornataInfo.getCodiceGiornata();
//		if (codiceGiornata > 9) {
//			x = 40;
//		}
//
//		String ris = "";
//		if (fcGiornataRis.getCasaFuori() == null) {
//
//		} else {
//			if (fcGiornataRis.getCasaFuori() == 1) {
//				ris = fcGiornataRis.getGf() + " - " + fcGiornataRis.getGs();
//			} else {
//				ris = fcGiornataRis.getGs() + " - " + fcGiornataRis.getGf();
//			}
//		}
//
//		NumberFormat formatter = new DecimalFormat("#0.00");
//		String totPuntiRosa = "0";
//		try {
//			if (totPunti != null && totPunti.getTotPtOld() != null) {
//				totPuntiRosa = formatter.format(totPunti.getTotPtOld() == 0 ? "0" : totPunti.getTotPtOld() / Costants.DIVISORE_100);
//			}
//		} catch (Exception e) {
//			totPuntiRosa = "0";
//		}
//		
//		Circle circle = new Circle("circle",50);
//
//		circle.center(50, 50);
//		circle.setRadius(25);
//		circle.setFillColor(fillColor);
//
//		// Text Giornata
//		Text textGiornata = new Text("text","" + codiceGiornata);
//		textGiornata.setFontFamily("'Roboto', 'Noto', sans-serif");
//		textGiornata.setFillColor("#ffffff");
//		textGiornata.move(x, y);
//
//		// Text Risultato
//		Text textRis = new Text("text",ris);
//		textRis.setFontFamily("'Roboto', 'Noto', sans-serif");
//		textRis.setFillColor("#000000");
//		textRis.move(35, 85);
//
//		// Text Punteggio
//		Text textPt = new Text("text",totPuntiRosa);
//		textPt.setFontFamily("'Roboto', 'Noto', sans-serif");
//		textPt.setFillColor("#808080");
//		textPt.move(35, 115);
//
//		draw.add(circle);
//		draw.add(textGiornata);
//		draw.add(textRis);
//		draw.add(textPt);
//
//		draw.addClickListener(e -> {
//			if (e.getElement() != null) {
//
//				List<FcGiornata> all = giornataController.findByFcGiornataInfo(giornataInfo);
//
//				for (FcGiornata g : all) {
//
//					if (attore.getIdAttore() == g.getFcAttoreByIdAttoreCasa().getIdAttore() || attore.getIdAttore() == g.getFcAttoreByIdAttoreFuori().getIdAttore()) {
//						DecimalFormat myFormatter = new DecimalFormat("#0.00");
//						Double dTotCasa = g.getTotCasa() != null ? g.getTotCasa().doubleValue() / Costants.DIVISORE_100 : 0;
//						String sTotCasa = myFormatter.format(dTotCasa);
//						Double dTotFuori = g.getTotFuori() != null ? g.getTotFuori().doubleValue() / Costants.DIVISORE_100 : 0;
//						String sTotFuori = myFormatter.format(dTotFuori);
//
//						String descPartita = g.getFcAttoreByIdAttoreCasa().getDescAttore() + " " + g.getFcAttoreByIdAttoreFuori().getDescAttore();
//						String punteggio = g.getGolCasa() != null ? g.getGolCasa() + " - " + g.getGolFuori() : "-";
//						String totPunteggio = sTotCasa + " - " + sTotFuori;
//						Notification.show(descPartita + " " + punteggio + " " + totPunteggio);
//						break;
//					}
//				}
//
//			} else {
//				Notification.show("Mo Element clicked");
//			}
//		});
//
//		return draw;
//	}
	
	private Calendario getInfoPartita(FcGiornataInfo ggInfo,FcAttore attore) {

		List<FcGiornata> all = giornataController.findByFcGiornataInfo(ggInfo);
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
			
			int idAttore = attore.getIdAttore(); 
			int idAttoreCasa = g.getFcAttoreByIdAttoreCasa().getIdAttore();
			int idAttoreFuori = g.getFcAttoreByIdAttoreFuori().getIdAttore();
			
			if (idAttore == idAttoreCasa || idAttore == idAttoreFuori) {
				return calendario;	
			}
			id++;
		}

		return null;
	}


}