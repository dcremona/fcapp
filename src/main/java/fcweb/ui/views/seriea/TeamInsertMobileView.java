package fcweb.ui.views.seriea;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.vaadin.ronny.AbsoluteLayout;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import common.util.ContentIdGenerator;
import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.CalendarioCompetizioneService;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataGiocatoreService;
import fcweb.backend.service.SquadraService;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mobile")
@Route(value = "mobile")
@RolesAllowed("USER")
public class TeamInsertMobileView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AttoreService attoreController;

	private static final int WINWIDTH = 400;
	private static final int WINHEIGHT = 800;

	private static final String WIDTH = "85px";
	private static final String HEIGHT = "105px";

	private static final int PX_P = 60;
	private static final int PX_D = 180;
	private static final int PX_C = 300;
	private static final int PX_A = 420;

	private static final int PX_0 = 0;
	private static final int PX_20 = 20;
	private static final int PX_70 = 70;
	private static final int PX_80 = 80;
	private static final int PX_110 = 110;
	private static final int PX_160 = 160;
	private static final int PX_200 = 200;
	private static final int PX_240 = 240;
	private static final int PX_250 = 250;
	private static final int PX_290 = 290;
	private static final int PX_320 = 320;

	private FcAttore attore = null;
	private FcGiornataInfo giornataInfo = null;
	private FcCampionato campionato = null;
	private String nextDate = null;
	private long millisDiff = 0;
	private String idAttore = "";
	private String descAttore = "";
	private Properties p = null;

	// COMPONENT
	private Button rosa;
	private Button save;
	private Button viewPartite;
	private ToggleButton checkMail;
	private ComboBox<String> comboModulo;

	private Dialog dialogTribuna = null;
	private Grid<FcGiocatore> tableFormazione;

	private Dialog dialogPartite = null;
	private List<FcCalendarioCompetizione> listPartiteGiocate = new ArrayList<>();
	private List<FcCalendarioCompetizione> listPartite = new ArrayList<>();

	private List<FcGiornataGiocatore> listSqualificatiInfortunati = new ArrayList<>();

	private AbsoluteLayout absLayout;

	private Grid<FcGiocatore> tablePlayer1;
	private Grid<FcGiocatore> tablePlayer2;
	private Grid<FcGiocatore> tablePlayer3;
	private Grid<FcGiocatore> tablePlayer4;
	private Grid<FcGiocatore> tablePlayer5;
	private Grid<FcGiocatore> tablePlayer6;
	private Grid<FcGiocatore> tablePlayer7;
	private Grid<FcGiocatore> tablePlayer8;
	private Grid<FcGiocatore> tablePlayer9;
	private Grid<FcGiocatore> tablePlayer10;
	private Grid<FcGiocatore> tablePlayer11;
	private Grid<FcGiocatore> tablePlayer12;
	private Grid<FcGiocatore> tablePlayer13;
	private Grid<FcGiocatore> tablePlayer14;
	private Grid<FcGiocatore> tablePlayer15;
	private Grid<FcGiocatore> tablePlayer16;
	private Grid<FcGiocatore> tablePlayer17;
	private Grid<FcGiocatore> tablePlayer18;

	// DATA
	private List<FcGiocatore> modelFormazione = new ArrayList<>();
	private List<FcGiocatore> modelPlayer1 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer2 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer3 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer4 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer5 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer6 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer7 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer8 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer9 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer10 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer11 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer12 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer13 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer14 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer15 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer16 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer17 = new ArrayList<>();
	private List<FcGiocatore> modelPlayer18 = new ArrayList<>();

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private CalendarioCompetizioneService calendarioTimController;

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private SquadraService squadraController;

	@Autowired
	private GiornataGiocatoreService giornataGiocatoreService;

	@PostConstruct
	void init() throws Exception {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());

		initData();

		initLayout();

	}

	private void initData() throws Exception {

		p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
		millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

		idAttore = "" + attore.getIdAttore();
		descAttore = attore.getDescAttore();

		modelFormazione = getModelFormazione();

		LocalDateTime now = LocalDateTime.now();
		listPartiteGiocate = calendarioTimController.findByIdGiornataAndDataLessThanEqual(giornataInfo.getCodiceGiornata(), now);
		listPartite = calendarioTimController.findByIdGiornataOrderByDataAsc(giornataInfo.getCodiceGiornata());

		listSqualificatiInfortunati = giornataGiocatoreService.findByCustonm(giornataInfo, null);
	}

	private void initLayout() throws Exception {

		absLayout = new AbsoluteLayout(WINWIDTH,WINHEIGHT);
		dialogTribuna = new Dialog();
		dialogPartite = new Dialog();

		UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
			int resX = event.getScreenWidth();
			int resY = event.getScreenHeight();
			log.info("resX " + resX);
			log.info("resY " + resY);
			log.info("Math.max " + Math.max(resX, resY));
			if (Math.max(resX, resY) < 800) {
				log.info("small screen detected ");
			}

			absLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
			absLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);

			dialogTribuna.setWidth(WINWIDTH - 50 + "px");
			dialogTribuna.setHeight(WINHEIGHT - 150 + "px");

			dialogPartite.setWidth(WINWIDTH - 50 + "px");
			dialogPartite.setHeight(WINHEIGHT - 250 + "px");
		});

		UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> {
			int winWidth = e.getWidth();
			int winHeight = e.getHeight();
			log.info("winWidth " + winWidth);
			log.info("winHeight " + winHeight);
			if (Math.max(winWidth, winHeight) < 800) {
				log.info("small screen detected ");
			} else {
				absLayout.setWidth(winWidth + "px");
				absLayout.setHeight(winHeight + "px");

				dialogTribuna.setWidth(winWidth - 50 + "px");
				dialogTribuna.setHeight(winHeight - 150 + "px");

				dialogPartite.setWidth(winWidth - 50 + "px");
				dialogPartite.setHeight(winHeight - 250 + "px");
			}
		});

		Button cancelButtonPartite = new Button("Chiudi",event -> {
			dialogPartite.close();
		});

		Button cancelButtonTribuna = new Button("Chiudi",event -> {
			dialogTribuna.close();
		});

		save = new Button("Salva");
		save.setIcon(VaadinIcon.DATABASE.create());
		save.addClickListener(this);

		rosa = new Button("Rosa");
		rosa.setIcon(VaadinIcon.PLUS.create());
		rosa.addClickListener(this);

		viewPartite = new Button(Utils.buildInfoGiornataMobile(giornataInfo));
		viewPartite.setIcon(VaadinIcon.CALENDAR_CLOCK.create());
		viewPartite.addClickListener(this);

		checkMail = new ToggleButton();
		checkMail.setLabel("Email");
		checkMail.setValue(true);

		comboModulo = new ComboBox<>();
		comboModulo.setItems(Costants.SCHEMI);
		comboModulo.getElement().setAttribute("theme", "small");
		comboModulo.setClearButtonVisible(true);
		comboModulo.setPlaceholder("Modulo");
		comboModulo.addValueChangeListener(evt -> {

			removeAllElementsList();

			if (evt.getValue() != null) {

				String modulo = evt.getValue();

				absLayout.add(tablePlayer1, PX_160, PX_P);

				absLayout.add(tablePlayer12, PX_80, 600);
				absLayout.add(tablePlayer13, PX_160, 600);
				absLayout.add(tablePlayer14, PX_240, 600);
				absLayout.add(tablePlayer15, PX_320, 600);
				absLayout.add(tablePlayer16, PX_80, 700);
				absLayout.add(tablePlayer17, PX_160, 700);
				absLayout.add(tablePlayer18, PX_240, 700);

				// 5-4-1 5-3-2 4-5-1 4-4-2 4-3-3 3-5-2 3-4-3
				if (Costants.SCHEMA_541.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_0, PX_D);
					absLayout.add(tablePlayer3, PX_80, PX_D);
					absLayout.add(tablePlayer4, PX_160, PX_D);
					absLayout.add(tablePlayer5, PX_240, PX_D);
					absLayout.add(tablePlayer6, PX_320, PX_D);

					absLayout.add(tablePlayer7, PX_20, PX_C);
					absLayout.add(tablePlayer8, PX_110, PX_C);
					absLayout.add(tablePlayer9, PX_200, PX_C);
					absLayout.add(tablePlayer10, PX_290, PX_C);

					absLayout.add(tablePlayer11, PX_160, PX_A);

				} else if (Costants.SCHEMA_532.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_0, PX_D);
					absLayout.add(tablePlayer3, PX_80, PX_D);
					absLayout.add(tablePlayer4, PX_160, PX_D);
					absLayout.add(tablePlayer5, PX_240, PX_D);
					absLayout.add(tablePlayer6, PX_320, PX_D);

					absLayout.add(tablePlayer7, PX_70, PX_C);
					absLayout.add(tablePlayer8, PX_160, PX_C);
					absLayout.add(tablePlayer9, PX_250, PX_C);

					absLayout.add(tablePlayer10, PX_110, PX_A);
					absLayout.add(tablePlayer11, PX_200, PX_A);

				} else if (Costants.SCHEMA_451.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_20, PX_D);
					absLayout.add(tablePlayer3, PX_110, PX_D);
					absLayout.add(tablePlayer4, PX_200, PX_D);
					absLayout.add(tablePlayer5, PX_290, PX_D);

					absLayout.add(tablePlayer6, PX_0, PX_C);
					absLayout.add(tablePlayer7, PX_80, PX_C);
					absLayout.add(tablePlayer8, PX_160, PX_C);
					absLayout.add(tablePlayer9, PX_240, PX_C);
					absLayout.add(tablePlayer10, PX_320, PX_C);

					absLayout.add(tablePlayer11, PX_160, PX_A);

				} else if (Costants.SCHEMA_442.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_20, PX_D);
					absLayout.add(tablePlayer3, PX_110, PX_D);
					absLayout.add(tablePlayer4, PX_200, PX_D);
					absLayout.add(tablePlayer5, PX_290, PX_D);

					absLayout.add(tablePlayer6, PX_20, PX_C);
					absLayout.add(tablePlayer7, PX_110, PX_C);
					absLayout.add(tablePlayer8, PX_200, PX_C);
					absLayout.add(tablePlayer9, PX_290, PX_C);

					absLayout.add(tablePlayer10, PX_110, PX_A);
					absLayout.add(tablePlayer11, PX_200, PX_A);

				} else if (Costants.SCHEMA_433.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_20, PX_D);
					absLayout.add(tablePlayer3, PX_110, PX_D);
					absLayout.add(tablePlayer4, PX_200, PX_D);
					absLayout.add(tablePlayer5, PX_290, PX_D);

					absLayout.add(tablePlayer6, PX_70, PX_C);
					absLayout.add(tablePlayer7, PX_160, PX_C);
					absLayout.add(tablePlayer8, PX_250, PX_C);

					absLayout.add(tablePlayer9, PX_70, PX_A);
					absLayout.add(tablePlayer10, PX_160, PX_A);
					absLayout.add(tablePlayer11, PX_250, PX_A);

				} else if (Costants.SCHEMA_352.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_70, PX_D);
					absLayout.add(tablePlayer3, PX_160, PX_D);
					absLayout.add(tablePlayer4, PX_250, PX_D);

					absLayout.add(tablePlayer5, PX_0, PX_C);
					absLayout.add(tablePlayer6, PX_80, PX_C);
					absLayout.add(tablePlayer7, PX_160, PX_C);
					absLayout.add(tablePlayer8, PX_240, PX_C);
					absLayout.add(tablePlayer9, PX_320, PX_C);

					absLayout.add(tablePlayer10, PX_110, PX_A);
					absLayout.add(tablePlayer11, PX_200, PX_A);

				} else if (Costants.SCHEMA_343.equals(modulo)) {

					absLayout.add(tablePlayer2, PX_70, PX_D);
					absLayout.add(tablePlayer3, PX_160, PX_D);
					absLayout.add(tablePlayer4, PX_250, PX_D);

					absLayout.add(tablePlayer5, PX_20, PX_C);
					absLayout.add(tablePlayer6, PX_110, PX_C);
					absLayout.add(tablePlayer7, PX_200, PX_C);
					absLayout.add(tablePlayer8, PX_290, PX_C);

					absLayout.add(tablePlayer9, PX_70, PX_A);
					absLayout.add(tablePlayer10, PX_160, PX_A);
					absLayout.add(tablePlayer11, PX_250, PX_A);
				}

				String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
				if ("true".equals(activeCheckFormazione)) {
					try {
						impostaGiocatoriConVoto(modulo);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
			}
		});

		tableFormazione = getTableFormazione(modelFormazione);

		VerticalLayout mainLayoutTribuna = new VerticalLayout();
		mainLayoutTribuna.setMargin(false);
		mainLayoutTribuna.setPadding(false);
		mainLayoutTribuna.setSpacing(false);

		mainLayoutTribuna.add(tableFormazione);
		mainLayoutTribuna.add(cancelButtonTribuna);
		mainLayoutTribuna.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, cancelButtonTribuna);
		dialogTribuna.add(mainLayoutTribuna);

		tablePlayer1 = getTableGiocatore(modelPlayer1);
		tablePlayer2 = getTableGiocatore(modelPlayer2);
		tablePlayer3 = getTableGiocatore(modelPlayer3);
		tablePlayer4 = getTableGiocatore(modelPlayer4);
		tablePlayer5 = getTableGiocatore(modelPlayer5);
		tablePlayer6 = getTableGiocatore(modelPlayer6);
		tablePlayer7 = getTableGiocatore(modelPlayer7);
		tablePlayer8 = getTableGiocatore(modelPlayer8);
		tablePlayer9 = getTableGiocatore(modelPlayer9);
		tablePlayer10 = getTableGiocatore(modelPlayer10);
		tablePlayer11 = getTableGiocatore(modelPlayer11);
		tablePlayer12 = getTableGiocatore(modelPlayer12);
		tablePlayer13 = getTableGiocatore(modelPlayer13);
		tablePlayer14 = getTableGiocatore(modelPlayer14);
		tablePlayer15 = getTableGiocatore(modelPlayer15);
		tablePlayer16 = getTableGiocatore(modelPlayer16);
		tablePlayer17 = getTableGiocatore(modelPlayer17);
		tablePlayer18 = getTableGiocatore(modelPlayer18);

		final VerticalLayout layoutPartite = new VerticalLayout();
		layoutPartite.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutPartite.getStyle().set(Costants.BACKGROUND, Costants.LIGHT_BLUE);
		layoutPartite.setWidth(Costants.WIDTH_300);
		layoutPartite.setMargin(false);
		layoutPartite.setPadding(false);
		layoutPartite.setSpacing(false);

		HorizontalLayout cssLayout = new HorizontalLayout();
		Span lblInfo = new Span(Utils.buildInfoGiornata(giornataInfo));
		lblInfo.getStyle().set(Costants.FONT_SIZE, "14px");
		cssLayout.add(lblInfo);
		layoutPartite.add(cssLayout);

		HorizontalLayout cssLayout2 = new HorizontalLayout();
		Span lblInfo2 = new Span("Formazione entro: " + nextDate);
		lblInfo2.getStyle().set(Costants.FONT_SIZE, "12px");
		cssLayout2.add(lblInfo2);
		layoutPartite.add(cssLayout2);

		Grid<FcCalendarioCompetizione> tablePartite = getTablePartite(listPartite);

		VerticalLayout mainLayoutPartite = new VerticalLayout();
		mainLayoutPartite.setMargin(false);
		mainLayoutPartite.setPadding(false);
		mainLayoutPartite.setSpacing(false);

		mainLayoutPartite.add(layoutPartite);
		mainLayoutPartite.add(tablePartite);
		mainLayoutPartite.add(cancelButtonPartite);
		mainLayoutPartite.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, cancelButtonPartite);
		dialogPartite.add(mainLayoutPartite);

		final VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);
		layoutAvviso.setWidth("100px");
		layoutAvviso.setMargin(false);
		layoutAvviso.setPadding(false);
		layoutAvviso.setSpacing(false);
		layoutAvviso.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

		Image campo = Utils.buildImage("small-campo.jpg", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "small-campo.jpg"));
		Image panchina = Utils.buildImage("small-panchina.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "small-panchina.png"));

		absLayout.add(comboModulo, 0, 0);
		absLayout.add(rosa, 180, 0);
		absLayout.add(layoutAvviso, 290, 10);
		absLayout.add(campo, 0, 50);
		absLayout.add(save, 0, 550);
		absLayout.add(checkMail, 100, 560);
		absLayout.add(viewPartite, 200, 550);
		absLayout.add(panchina, 0, 600);

		Button home = new Button("Home");
		RouterLink menuHome = new RouterLink("",HomeView.class);
		menuHome.getElement().appendChild(home.getElement());

		absLayout.add(menuHome, 0, 700);

		add(absLayout);

		try {
			loadFcGiornatadett();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		if (millisDiff == 0) {
			showMessageStopInsert();
		} else {
			SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
			timer.setHours(true);
			timer.setMinutes(true);
			timer.setFractions(false);
			timer.start();
			timer.isRunning();
			timer.addTimerEndEvent(ev -> showMessageStopInsert());
			layoutAvviso.add(timer);
		}

	}

	private void showMessageStopInsert() {
		String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
		if ("true".equals(activeCheckFormazione)) {
			log.info("showMessageStopInsert");
			enabledComponent(false);
			CustomMessageDialog.showMessageInfo("Impossibile inserire la formazione, tempo scaduto!");
		}
	}

	private void enabledComponent(boolean enabled) {
		comboModulo.setEnabled(enabled);
		save.setEnabled(enabled);
		checkMail.setEnabled(enabled);
		tablePlayer1.setEnabled(enabled);
		tablePlayer2.setEnabled(enabled);
		tablePlayer3.setEnabled(enabled);
		tablePlayer4.setEnabled(enabled);
		tablePlayer5.setEnabled(enabled);
		tablePlayer6.setEnabled(enabled);
		tablePlayer7.setEnabled(enabled);
		tablePlayer8.setEnabled(enabled);
		tablePlayer9.setEnabled(enabled);
		tablePlayer10.setEnabled(enabled);
		tablePlayer11.setEnabled(enabled);
		tablePlayer12.setEnabled(enabled);
		tablePlayer13.setEnabled(enabled);
		tablePlayer14.setEnabled(enabled);
		tablePlayer15.setEnabled(enabled);
		tablePlayer16.setEnabled(enabled);
		tablePlayer17.setEnabled(enabled);
		tablePlayer18.setEnabled(enabled);
	}

	private String getInfoPlayer(FcGiocatore bean) {
		String info = bean.getCognGiocatore() + "\n";
		info += "Squadra: " + bean.getFcSquadra().getNomeSquadra() + "\n";
		info += "Giocate: " + bean.getFcStatistiche().getGiocate() + "\n";
		if (bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
			NumberFormat formatter = new DecimalFormat("#0.00");
			String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / Costants.DIVISORE_100);
			String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / Costants.DIVISORE_100);

			info += "MV: " + mv + "\n";
			info += "FV: " + fv + "\n";
			info += "Goal: " + bean.getFcStatistiche().getGoalFatto() + "\n";
			info += "Assist: " + bean.getFcStatistiche().getAssist() + "\n";
			info += "Ammonizione: " + bean.getFcStatistiche().getAmmonizione() + "\n";
			info += "Espulsione: " + bean.getFcStatistiche().getEspulsione() + "\n";
			if (Costants.P.equalsIgnoreCase(bean.getFcRuolo().getIdRuolo())) {
				info += "Goal Subito: " + bean.getFcStatistiche().getGoalSubito() + "\n";
			}
		}
		info += "Probabile: " + (StringUtils.isNotEmpty(bean.getNomeGiocatore()) ? bean.getNomeGiocatore() : "N.D.") + "\n";
		return info;
	}

	private void refreshAndSortGridFormazione() {
		modelFormazione.sort((p1,
				p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
		tableFormazione.getDataProvider().refreshAll();
	}

	private void removeAllElementsList() {

		if (!modelPlayer1.isEmpty()) {
			FcGiocatore bean = modelPlayer1.get(0);
			modelFormazione.add(bean);
			modelPlayer1.clear();
			tablePlayer1.getDataProvider().refreshAll();
		}
		if (!modelPlayer2.isEmpty()) {
			FcGiocatore bean = modelPlayer2.get(0);
			modelFormazione.add(bean);
			modelPlayer2.clear();
			tablePlayer2.getDataProvider().refreshAll();
		}
		if (!modelPlayer3.isEmpty()) {
			FcGiocatore bean = modelPlayer3.get(0);
			modelFormazione.add(bean);
			modelPlayer3.clear();
			tablePlayer3.getDataProvider().refreshAll();
		}
		if (!modelPlayer4.isEmpty()) {
			FcGiocatore bean = modelPlayer4.get(0);
			modelFormazione.add(bean);
			modelPlayer4.clear();
			tablePlayer4.getDataProvider().refreshAll();
		}
		if (!modelPlayer5.isEmpty()) {
			FcGiocatore bean = modelPlayer5.get(0);
			modelFormazione.add(bean);
			modelPlayer5.clear();
			tablePlayer5.getDataProvider().refreshAll();
		}
		if (!modelPlayer6.isEmpty()) {
			FcGiocatore bean = modelPlayer6.get(0);
			modelFormazione.add(bean);
			modelPlayer6.clear();
			tablePlayer6.getDataProvider().refreshAll();
		}
		if (!modelPlayer7.isEmpty()) {
			FcGiocatore bean = modelPlayer7.get(0);
			modelFormazione.add(bean);
			modelPlayer7.clear();
			tablePlayer7.getDataProvider().refreshAll();
		}
		if (!modelPlayer8.isEmpty()) {
			FcGiocatore bean = modelPlayer8.get(0);
			modelFormazione.add(bean);
			modelPlayer8.clear();
			tablePlayer8.getDataProvider().refreshAll();
		}
		if (!modelPlayer9.isEmpty()) {
			FcGiocatore bean = modelPlayer9.get(0);
			modelFormazione.add(bean);
			modelPlayer9.clear();
			tablePlayer9.getDataProvider().refreshAll();
		}
		if (!modelPlayer10.isEmpty()) {
			FcGiocatore bean = modelPlayer10.get(0);
			modelFormazione.add(bean);
			modelPlayer10.clear();
			tablePlayer10.getDataProvider().refreshAll();
		}
		if (!modelPlayer11.isEmpty()) {
			FcGiocatore bean = modelPlayer11.get(0);
			modelFormazione.add(bean);
			modelPlayer11.clear();
			tablePlayer11.getDataProvider().refreshAll();
		}
		if (!modelPlayer12.isEmpty()) {
			FcGiocatore bean = modelPlayer12.get(0);
			modelFormazione.add(bean);
			modelPlayer12.clear();
			tablePlayer12.getDataProvider().refreshAll();
		}
		if (!modelPlayer13.isEmpty()) {
			FcGiocatore bean = modelPlayer13.get(0);
			modelFormazione.add(bean);
			modelPlayer13.clear();
			tablePlayer13.getDataProvider().refreshAll();
		}
		if (!modelPlayer14.isEmpty()) {
			FcGiocatore bean = modelPlayer14.get(0);
			modelFormazione.add(bean);
			modelPlayer14.clear();
			tablePlayer14.getDataProvider().refreshAll();
		}
		if (!modelPlayer15.isEmpty()) {
			FcGiocatore bean = modelPlayer15.get(0);
			modelFormazione.add(bean);
			modelPlayer15.clear();
			tablePlayer15.getDataProvider().refreshAll();
		}
		if (!modelPlayer16.isEmpty()) {
			FcGiocatore bean = modelPlayer16.get(0);
			modelFormazione.add(bean);
			modelPlayer16.clear();
			tablePlayer16.getDataProvider().refreshAll();
		}
		if (!modelPlayer17.isEmpty()) {
			FcGiocatore bean = modelPlayer17.get(0);
			modelFormazione.add(bean);
			modelPlayer17.clear();
			tablePlayer17.getDataProvider().refreshAll();
		}
		if (!modelPlayer18.isEmpty()) {
			FcGiocatore bean = modelPlayer18.get(0);
			modelFormazione.add(bean);
			modelPlayer18.clear();
			tablePlayer18.getDataProvider().refreshAll();
		}

		refreshAndSortGridFormazione();

		absLayout.remove(tablePlayer1);
		absLayout.remove(tablePlayer2);
		absLayout.remove(tablePlayer3);
		absLayout.remove(tablePlayer4);
		absLayout.remove(tablePlayer5);
		absLayout.remove(tablePlayer6);
		absLayout.remove(tablePlayer7);
		absLayout.remove(tablePlayer8);
		absLayout.remove(tablePlayer9);
		absLayout.remove(tablePlayer10);
		absLayout.remove(tablePlayer11);
		absLayout.remove(tablePlayer12);
		absLayout.remove(tablePlayer13);
		absLayout.remove(tablePlayer14);
		absLayout.remove(tablePlayer15);
		absLayout.remove(tablePlayer16);
		absLayout.remove(tablePlayer17);
		absLayout.remove(tablePlayer18);
	}

	private ArrayList<FcGiocatore> getModelFormazione() {

		List<FcFormazione> listFormazione = formazioneController.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, false);

		ArrayList<FcGiocatore> beans = new ArrayList<>();
		for (FcFormazione f : listFormazione) {
			if (f.getFcGiocatore() != null) {
				beans.add(f.getFcGiocatore());
			}
		}

		return beans;
	}

	private Grid<FcGiocatore> getTableGiocatore(List<FcGiocatore> items) {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.getStyle().set("--_lumo-grid-border-width", "0px");
		grid.setAllRowsVisible(true);
		grid.setWidth(WIDTH);
		grid.setHeight(HEIGHT);

		Column<FcGiocatore> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(p -> {

			VerticalLayout cellLayout = new VerticalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setSizeUndefined();
			cellLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

			if (p != null) {

				String title = getInfoPlayer(p);

				String ruolo = p.getFcRuolo().getIdRuolo();
				if (Costants.P.equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_P);
				} else if (Costants.D.equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_D);
				} else if (Costants.C.equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_C);
				} else if (Costants.A.equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_A);
				}

				if (isGiocatoreOut(p) != null) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}

				HorizontalLayout cellLayoutImg = new HorizontalLayout();
				cellLayoutImg.setMargin(false);
				cellLayoutImg.setPadding(false);
				cellLayoutImg.setSpacing(false);
				cellLayoutImg.setSizeUndefined();
				cellLayoutImg.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

				Image imgR = Utils.buildImage(p.getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + p.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
				imgR.setTitle(title);
				cellLayoutImg.add(imgR);

				FcSquadra sq = p.getFcSquadra();
				if (sq != null && sq.getImg() != null) {
					try {
						Image imgSq = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						imgSq.setTitle(title);
						cellLayoutImg.add(imgSq);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
				FcStatistiche s = p.getFcStatistiche();
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image imgMv = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
				imgMv.setTitle(title);
				cellLayoutImg.add(imgMv);

				FcGiornataGiocatore gg = isGiocatoreOut(p);
				if (gg != null) {
					cellLayoutImg.add(getImageGiocatoreOut(gg));
				}

				Span lblGiocatore = new Span(p.getCognGiocatore());
				lblGiocatore.getStyle().set(Costants.FONT_SIZE, "9px");
				lblGiocatore.setTitle(title);
				lblGiocatore.setWidth("60px");

				cellLayout.add(cellLayoutImg);
				try {
					Image img = Utils.getImage(p.getNomeImg(), p.getImgSmall().getBinaryStream());
					img.setTitle(title);
					cellLayout.add(img);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				cellLayout.add(lblGiocatore);

				Element element = cellLayout.getElement(); // DOM element
				element.addEventListener("click", e -> {

					FcGiocatore bean = p;

					if (isGiocatorePartitaGiocata(bean)) {
						CustomMessageDialog.showMessageError("Impossibile muovere il giocatore!");
						return;
					}

					modelFormazione.add(bean);
					refreshAndSortGridFormazione();

					if (grid == tablePlayer1) {
						modelPlayer1.remove(bean);
						tablePlayer1.getDataProvider().refreshAll();
					} else if (grid == tablePlayer2) {
						modelPlayer2.remove(bean);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (grid == tablePlayer3) {
						modelPlayer3.remove(bean);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (grid == tablePlayer4) {
						modelPlayer4.remove(bean);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (grid == tablePlayer5) {
						modelPlayer5.remove(bean);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (grid == tablePlayer6) {
						modelPlayer6.remove(bean);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (grid == tablePlayer7) {
						modelPlayer7.remove(bean);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (grid == tablePlayer8) {
						modelPlayer8.remove(bean);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (grid == tablePlayer9) {
						modelPlayer9.remove(bean);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (grid == tablePlayer10) {
						modelPlayer10.remove(bean);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (grid == tablePlayer11) {
						modelPlayer11.remove(bean);
						tablePlayer11.getDataProvider().refreshAll();
					} else if (grid == tablePlayer12) {
						modelPlayer12.remove(bean);
						tablePlayer12.getDataProvider().refreshAll();
					} else if (grid == tablePlayer13) {
						modelPlayer13.remove(bean);
						tablePlayer13.getDataProvider().refreshAll();
					} else if (grid == tablePlayer14) {
						modelPlayer14.remove(bean);
						tablePlayer14.getDataProvider().refreshAll();
					} else if (grid == tablePlayer15) {
						modelPlayer15.remove(bean);
						tablePlayer15.getDataProvider().refreshAll();
					} else if (grid == tablePlayer16) {
						modelPlayer16.remove(bean);
						tablePlayer16.getDataProvider().refreshAll();
					} else if (grid == tablePlayer17) {
						modelPlayer17.remove(bean);
						tablePlayer17.getDataProvider().refreshAll();
					} else if (grid == tablePlayer18) {
						modelPlayer18.remove(bean);
						tablePlayer18.getDataProvider().refreshAll();
					}
				});
			}
			return cellLayout;

		}));
		giocatoreColumn.setSortable(false);
		giocatoreColumn.setResizable(false);
		giocatoreColumn.setWidth("80px");

		return grid;

	}

	private Grid<FcGiocatore> getTableFormazione(List<FcGiocatore> items) {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth(Costants.WIDTH_300);

		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (g.getFcRuolo() != null) {
					Image img = Utils.buildImage(g.getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
					img.setTitle(title);
					cellLayout.add(img);
				}
				if (g.getCognGiocatore() != null) {
					Span lblGiocatore = new Span();
					lblGiocatore.setTitle(title);
					lblGiocatore.setText(g.getCognGiocatore());
					cellLayout.add(lblGiocatore);
				}
				FcGiornataGiocatore gg = isGiocatoreOut(g);
				if (gg != null) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
					cellLayout.add(getImageGiocatoreOut(gg));
				}
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
		cognGiocatoreColumn.setWidth("160px");

		Column<FcGiocatore> infoPercColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				Integer perc = g.getPercentuale() == null ? 0 : g.getPercentuale();
				double value = Double.parseDouble(perc.toString()) / Double.parseDouble("100");

				ProgressBar progressBarPerc = new ProgressBar();
				progressBarPerc.setValue(value);

				Span lblPerc = new Span();
				lblPerc.setText(perc + "%");
				lblPerc.setTitle(title);

				if (perc > 60) {
					progressBarPerc.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
					lblPerc.addClassNames(LumoUtility.TextColor.SUCCESS);
				} else if (perc > 39 && perc < 61) {
					progressBarPerc.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
					lblPerc.addClassNames(LumoUtility.TextColor.ERROR);
				} else {
					progressBarPerc.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
					lblPerc.addClassNames(LumoUtility.TextColor.DISABLED);
				}

				cellLayout.add(progressBarPerc);
				cellLayout.add(lblPerc);
			}
			return cellLayout;
		}));
		infoPercColumn.setSortable(false);
		infoPercColumn.setHeader("");
		infoPercColumn.setWidth("135px");

		// Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new
		// ComponentRenderer<>(g -> {
		// HorizontalLayout cellLayout = new HorizontalLayout();
		// cellLayout.setMargin(false);
		// cellLayout.setPadding(false);
		// cellLayout.setSpacing(false);
		// cellLayout.setAlignItems(Alignment.STRETCH);
		// if (g != null) {
		// String title = getInfoPlayer(g);
		// if (isGiocatoreOut(g) != null) {
		// cellLayout.getElement().getStyle().set(Costants.BACKGROUND,
		// Costants.LOWER_GRAY);
		// cellLayout.getElement().getStyle().set("-webkit-text-fill-color",
		// Costants.RED);
		// }
		// if (g.getFcSquadra() != null) {
		// FcSquadra sq = g.getFcSquadra();
		// if (sq.getImg() != null) {
		// try {
		// Image img = Utils.getImage(sq.getNomeSquadra(),
		// sq.getImg().getBinaryStream());
		// img.setTitle(title);
		// cellLayout.add(img);
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		// }
		// Span lblSquadra = new Span();
		// lblSquadra.setText(sq.getNomeSquadra().substring(0, 3));
		// lblSquadra.setTitle(title);
		// cellLayout.add(lblSquadra);
		// }
		// }
		// return cellLayout;
		// }));
		// nomeSquadraColumn.setSortable(true);
		// nomeSquadraColumn.setComparator(
		// (p1, p2) ->
		// p1.getFcSquadra().getNomeSquadra().compareTo(p2.getFcSquadra().getNomeSquadra()));
		// nomeSquadraColumn.setHeader("Sq");
		// nomeSquadraColumn.setWidth("70px");
		//
		// Column<FcGiocatore> mediaVotoColumn = grid.addColumn(new
		// ComponentRenderer<>(g -> {
		// HorizontalLayout cellLayout = new HorizontalLayout();
		// cellLayout.setMargin(false);
		// cellLayout.setPadding(false);
		// cellLayout.setSpacing(false);
		// if (g != null) {
		// String title = getInfoPlayer(g);
		// if (isGiocatoreOut(g) != null) {
		// cellLayout.getElement().getStyle().set(Costants.BACKGROUND,
		// Costants.LOWER_GRAY);
		// cellLayout.getElement().getStyle().set("-webkit-text-fill-color",
		// Costants.RED);
		// }
		//
		// FcStatistiche s = g.getFcStatistiche();
		// String imgThink = "2.png";
		// if (s != null && s.getMediaVoto() != 0) {
		// if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
		// imgThink = "1.png";
		// } else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
		// imgThink = "3.png";
		// }
		// }
		// Image img = Utils.buildImage(imgThink,
		// resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
		// img.setTitle(title);
		//
		// DecimalFormat myFormatter = new DecimalFormat("#0.00");
		// Double d = Double.valueOf(0);
		// if (s != null) {
		// d = s.getMediaVoto() / Costants.DIVISORE_100;
		// }
		// String sTotPunti = myFormatter.format(d);
		// Span lbl = new Span(sTotPunti);
		// lbl.setTitle(title);
		//
		// cellLayout.add(img);
		// cellLayout.add(lbl);
		// }
		// return cellLayout;
		// }));
		// mediaVotoColumn.setSortable(true);
		// mediaVotoColumn.setComparator(
		// (p1, p2) ->
		// p1.getFcStatistiche().getMediaVoto().compareTo(p2.getFcStatistiche().getMediaVoto()));
		// mediaVotoColumn.setHeader("Mv");
		// mediaVotoColumn.setWidth("70px");

		grid.addItemClickListener(event -> {
			String valModulo = comboModulo.getValue();
			if (valModulo == null) {
				log.info("valModulo null");
				return;
			}

			FcGiocatore bean = event.getItem();

			if (bean != null) {

				if (isGiocatorePartitaGiocata(bean)) {
					CustomMessageDialog.showMessageError("Impossibile muovere il giocatore!");
					return;
				}

				if (existGiocatore(bean)) {
					log.info("existGiocatore true");
					return;
				}

				boolean bDel = false;
				if (bean.getFcRuolo().getIdRuolo().equals(Costants.P)) {
					if (modelPlayer1.isEmpty()) {
						modelPlayer1.add(bean);
						tablePlayer1.getDataProvider().refreshAll();
						bDel = true;
					} else {
						if (modelPlayer12.isEmpty()) {
							modelPlayer12.add(bean);
							tablePlayer12.getDataProvider().refreshAll();
							bDel = true;
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {

					if (Costants.SCHEMA_541.equals(valModulo) || Costants.SCHEMA_532.equals(valModulo)) {

						if (modelPlayer2.isEmpty()) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.isEmpty()) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.isEmpty()) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer5.isEmpty()) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.isEmpty()) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.isEmpty()) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_451.equals(valModulo) || Costants.SCHEMA_442.equals(valModulo) || Costants.SCHEMA_433.equals(valModulo)) {

						if (modelPlayer2.isEmpty()) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.isEmpty()) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.isEmpty()) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer5.isEmpty()) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.isEmpty()) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.isEmpty()) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_352.equals(valModulo) || Costants.SCHEMA_343.equals(valModulo)) {

						if (modelPlayer2.isEmpty()) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.isEmpty()) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.isEmpty()) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.isEmpty()) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.isEmpty()) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {

					if (Costants.SCHEMA_451.equals(valModulo)) {

						if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.isEmpty()) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_352.equals(valModulo)) {

						if (modelPlayer5.isEmpty()) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_541.equals(valModulo)) {

						if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.isEmpty()) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_442.equals(valModulo)) {

						if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_343.equals(valModulo)) {

						if (modelPlayer5.isEmpty()) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_532.equals(valModulo)) {

						if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_433.equals(valModulo)) {
						if (modelPlayer6.isEmpty()) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.isEmpty()) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.isEmpty()) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.isEmpty()) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.isEmpty()) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {

					if (Costants.SCHEMA_451.equals(valModulo) || Costants.SCHEMA_541.equals(valModulo)) {

						if (modelPlayer11.isEmpty()) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.isEmpty()) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.isEmpty()) {
								modelPlayer18.add(bean);
								tablePlayer18.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_352.equals(valModulo) || Costants.SCHEMA_442.equals(valModulo) || Costants.SCHEMA_532.equals(valModulo)) {

						if (modelPlayer10.isEmpty()) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer11.isEmpty()) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.isEmpty()) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.isEmpty()) {
								modelPlayer18.add(bean);
								tablePlayer18.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (Costants.SCHEMA_343.equals(valModulo) || Costants.SCHEMA_433.equals(valModulo)) {

						if (modelPlayer9.isEmpty()) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.isEmpty()) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer11.isEmpty()) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.isEmpty()) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.isEmpty()) {
								modelPlayer18.add(bean);
								tablePlayer18.getDataProvider().refreshAll();
								bDel = true;
							}
						}
					}
				}

				if (bDel) {
					modelFormazione.remove(bean);
					refreshAndSortGridFormazione();
				}
			}
		});

		return grid;
	}

	private boolean existGiocatore(FcGiocatore g) {

		if (!modelPlayer1.isEmpty() && modelPlayer1.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer2.isEmpty() && modelPlayer2.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer3.isEmpty() && modelPlayer3.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer4.isEmpty() && modelPlayer4.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer5.isEmpty() && modelPlayer5.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer6.isEmpty() && modelPlayer6.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer7.isEmpty() && modelPlayer7.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer8.isEmpty() && modelPlayer8.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer9.isEmpty() && modelPlayer9.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer10.isEmpty() && modelPlayer10.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer11.isEmpty() && modelPlayer11.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer12.isEmpty() && modelPlayer12.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer13.isEmpty() && modelPlayer13.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer14.isEmpty() && modelPlayer14.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer15.isEmpty() && modelPlayer15.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer16.isEmpty() && modelPlayer16.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer17.isEmpty() && modelPlayer17.indexOf(g) != -1) {
			return true;
		} else if (!modelPlayer18.isEmpty() && modelPlayer18.indexOf(g) != -1) {
			return true;
		}
		return false;
	}

	private void loadFcGiornatadett() throws Exception {

		log.info("loadFcGiornatadett");

		List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

		if (lGiocatori.isEmpty()) {
			comboModulo.setValue(null);
			removeAllElementsList();
			return;
		}

		int countD = 0;
		int countC = 0;
		int countA = 0;

		for (FcGiornataDett gd : lGiocatori) {

			if (gd.getOrdinamento() < 12) {
				if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.D)) {
					countD++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.C)) {
					countC++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals(Costants.A)) {
					countA++;
				}
			}
		}

		String schema = countD + "-" + countC + "-" + countA;

		comboModulo.setValue(schema);

		modelFormazione.clear();
		refreshAndSortGridFormazione();

		for (FcGiornataDett gd : lGiocatori) {

			FcGiocatore bean = gd.getFcGiocatore();
			if (gd.getOrdinamento() == 1) {
				modelPlayer1.clear();
				modelPlayer1.add(bean);
				tablePlayer1.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 2) {
				modelPlayer2.clear();
				modelPlayer2.add(bean);
				tablePlayer2.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 3) {
				modelPlayer3.clear();
				modelPlayer3.add(bean);
				tablePlayer3.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 4) {
				modelPlayer4.clear();
				modelPlayer4.add(bean);
				tablePlayer4.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 5) {
				modelPlayer5.clear();
				modelPlayer5.add(bean);
				tablePlayer5.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 6) {
				modelPlayer6.clear();
				modelPlayer6.add(bean);
				tablePlayer6.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 7) {
				modelPlayer7.clear();
				modelPlayer7.add(bean);
				tablePlayer7.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 8) {
				modelPlayer8.clear();
				modelPlayer8.add(bean);
				tablePlayer8.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 9) {
				modelPlayer9.clear();
				modelPlayer9.add(bean);
				tablePlayer9.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 10) {
				modelPlayer10.clear();
				modelPlayer10.add(bean);
				tablePlayer10.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 11) {
				modelPlayer11.clear();
				modelPlayer11.add(bean);
				tablePlayer11.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 12) {
				modelPlayer12.clear();
				modelPlayer12.add(bean);
				tablePlayer12.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 13) {
				modelPlayer13.clear();
				modelPlayer13.add(bean);
				tablePlayer13.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 14) {
				modelPlayer14.clear();
				modelPlayer14.add(bean);
				tablePlayer14.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 15) {
				modelPlayer15.clear();
				modelPlayer15.add(bean);
				tablePlayer15.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 16) {
				modelPlayer16.clear();
				modelPlayer16.add(bean);
				tablePlayer16.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 17) {
				modelPlayer17.clear();
				modelPlayer17.add(bean);
				tablePlayer17.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 18) {
				modelPlayer18.clear();
				modelPlayer18.add(bean);
				tablePlayer18.getDataProvider().refreshAll();
			} else {
				modelFormazione.add(bean);
				refreshAndSortGridFormazione();
			}
		}
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		if (event.getSource() == rosa) {

			dialogTribuna.open();

		} else if (event.getSource() == viewPartite) {

			dialogPartite.open();

		} else if (event.getSource() == save) {

			if (check()) {

				int giornataSeriea = giornataInfo.getCodiceGiornata();
				String descGiornata = giornataInfo.getDescGiornataFc();

				try {
					insert(giornataSeriea);
				} catch (Exception exi) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exi.getMessage());
					return;
				}

				if (checkMail.getValue().booleanValue()) {
					try {
						String dataora = getSysdate();

						sendNewMail(descGiornata);

						log.info("send_mail OK");

						try {
							insertDettInfo(giornataSeriea, dataora);
							log.info("insert_dett_info OK");
						} catch (Exception exd) {
							log.error(exd.getMessage());
							CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exd.getMessage());
						}

						CustomMessageDialog.showMessageInfo("Formazione inserita, email inviata con successo!");

					} catch (Exception excpt) {
						CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, excpt.getMessage());
					}

				} else {
					CustomMessageDialog.showMessageInfo("Formazione salvata con successo! Per rendere effettiva la formazione, inviare email.");
				}
			}
		}
	}

	private String getSysdate() {

		String sql = "select sysdate() from dual";
		return jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				if (rs.next()) {
					return rs.getString(1);
				}
				return null;
			}
		});
	}

	private boolean check() {

		if (modelPlayer1.isEmpty() || modelPlayer2.isEmpty() || modelPlayer3.isEmpty() || modelPlayer4.isEmpty() || modelPlayer5.isEmpty() || modelPlayer6.isEmpty() || modelPlayer7.isEmpty() || modelPlayer8.isEmpty() || modelPlayer9.isEmpty() || modelPlayer10.isEmpty() || modelPlayer11.isEmpty() || modelPlayer12.isEmpty() || modelPlayer13.isEmpty() || modelPlayer14.isEmpty() || modelPlayer15.isEmpty() || modelPlayer16.isEmpty() || modelPlayer17.isEmpty() || modelPlayer18.isEmpty()) {
			CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ERROR_INSERT_GIOCATORI);
			return false;
		}
		return true;
	}

	private void insert(int giornata) throws Exception {

		String query = "";
		try {
			query = " DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
			jdbcTemplate.update(query);

			String idGiornata = "" + giornata;
			String idGiocatore = "";
			String idStatoGiocatore = "";
			String ordinamento = "";
			int ord = 1;
			for (int i = 0; i < 18; i++) {

				ordinamento = "" + ord;
				ord++;
				if (i == 0) {
					FcGiocatore bean = modelPlayer1.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 1) {
					FcGiocatore bean = modelPlayer2.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 2) {
					FcGiocatore bean = modelPlayer3.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 3) {
					FcGiocatore bean = modelPlayer4.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 4) {
					FcGiocatore bean = modelPlayer5.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 5) {
					FcGiocatore bean = modelPlayer6.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 6) {
					FcGiocatore bean = modelPlayer7.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 7) {
					FcGiocatore bean = modelPlayer8.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 8) {
					FcGiocatore bean = modelPlayer9.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 9) {
					FcGiocatore bean = modelPlayer10.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 10) {
					FcGiocatore bean = modelPlayer11.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "T";
				} else if (i == 11) {
					FcGiocatore bean = modelPlayer12.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 12) {
					FcGiocatore bean = modelPlayer13.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 13) {
					FcGiocatore bean = modelPlayer14.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 14) {
					FcGiocatore bean = modelPlayer15.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 15) {
					FcGiocatore bean = modelPlayer16.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 16) {
					FcGiocatore bean = modelPlayer17.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				} else if (i == 17) {
					FcGiocatore bean = modelPlayer18.get(0);
					idGiocatore = "" + bean.getIdGiocatore();
					idStatoGiocatore = "R";
				}

				query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + idGiornata + ",";
				query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ordinamento + ",0)";
				jdbcTemplate.update(query);
			}

			for (FcGiocatore bean : modelFormazione) {
				ordinamento = "" + ord;
				ord++;
				idGiocatore = "" + bean.getIdGiocatore();
				idStatoGiocatore = "N";

				query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + idGiornata + ",";
				query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ordinamento + ",0)";

				jdbcTemplate.update(query);
			}

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void insertDettInfo(int giornata, String dataora) throws Exception {

		String query = "";
		try {
			query = " DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
			jdbcTemplate.update(query);

			query = " INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES (" + giornata + ",";
			query += idAttore + ",1, '" + dataora + "')";

			jdbcTemplate.update(query);

		} catch (Exception e) {
			CustomMessageDialog.showMessageError("insert_dett_info " + e.getMessage());
		}
	}

	private void sendNewMail(String descGiornata) throws Exception {

		String subject = "Formazione " + descAttore + " - " + descGiornata;

		String modulo = comboModulo.getValue();

		StringBuilder formazioneHtml = new StringBuilder();
		formazioneHtml.append("<html><head><title>FC</title></head>\n");
		formazioneHtml.append("<body>\n");
		formazioneHtml.append("<p>");
		formazioneHtml.append(descGiornata);
		formazioneHtml.append("</p>\n");
		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<p>");
		formazioneHtml.append(modulo);
		formazioneHtml.append("</p>\n");
		formazioneHtml.append("<br>\n");

		formazioneHtml.append("<table>");

		String nomeGiocatore = "";
		String ruolo = "";
		String stato = "";
		String ordinamento = "";
		String squadra = "";
		int ord = 1;
		Map<String, InputStream> listImg = new HashMap<>();
		for (int i = 0; i < 18; i++) {

			ordinamento = "" + ord;
			FcGiocatore bean = null;

			if (i == 0) {
				bean = modelPlayer1.get(0);
			} else if (i == 1) {
				bean = modelPlayer2.get(0);
			} else if (i == 2) {
				bean = modelPlayer3.get(0);
			} else if (i == 3) {
				bean = modelPlayer4.get(0);
			} else if (i == 4) {
				bean = modelPlayer5.get(0);
			} else if (i == 5) {
				bean = modelPlayer6.get(0);
			} else if (i == 6) {
				bean = modelPlayer7.get(0);
			} else if (i == 7) {
				bean = modelPlayer8.get(0);
			} else if (i == 8) {
				bean = modelPlayer9.get(0);
			} else if (i == 9) {
				bean = modelPlayer10.get(0);
			} else if (i == 10) {
				bean = modelPlayer11.get(0);
			} else if (i == 11) {
				bean = modelPlayer12.get(0);
			} else if (i == 12) {
				bean = modelPlayer13.get(0);
			} else if (i == 13) {
				bean = modelPlayer14.get(0);
			} else if (i == 14) {
				bean = modelPlayer15.get(0);
			} else if (i == 15) {
				bean = modelPlayer16.get(0);
			} else if (i == 16) {
				bean = modelPlayer17.get(0);
			} else if (i == 17) {
				bean = modelPlayer18.get(0);
			}

			nomeGiocatore = bean.getCognGiocatore();
			ruolo = bean.getFcRuolo().getDescRuolo();
			squadra = bean.getFcSquadra().getNomeSquadra();

			String cidNomeSq = ContentIdGenerator.getContentId();
			FcSquadra sq = bean.getFcSquadra();
			if (sq.getImg() != null) {
				try {
					listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}

			String cidNomeImg = ContentIdGenerator.getContentId();
			InputStream inputStream = null;
			try {
				inputStream = bean.getImg().getBinaryStream();
				listImg.put(cidNomeImg, inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (i < 11) {
				stato = "Titolare";
			} else {
				stato = "Riserva";
			}
			String color = "BGCOLOR=\"" + Costants.BG_N + "\"";
			if (Integer.parseInt(ordinamento) >= 1 && Integer.parseInt(ordinamento) <= 11) {
				color = "BGCOLOR=\"" + Costants.BG_T + "\"";
			} else if (Integer.parseInt(ordinamento) >= 12 && Integer.parseInt(ordinamento) <= 18) {
				color = "BGCOLOR=\"" + Costants.BG_R + "\"";
			}

			formazioneHtml.append("<tr ");
			formazioneHtml.append(color);
			formazioneHtml.append(">");
			formazioneHtml.append("<td>");
			formazioneHtml.append(ordinamento);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td><img src=\"cid:");
			formazioneHtml.append(cidNomeImg);
			formazioneHtml.append("\" />");
			formazioneHtml.append(nomeGiocatore);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(ruolo);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td><img src=\"cid:");
			formazioneHtml.append(cidNomeSq);
			formazioneHtml.append("\" />");
			formazioneHtml.append(squadra);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(stato);
			formazioneHtml.append("</td>");
			formazioneHtml.append("</tr>");

			ord++;
		}

		for (FcGiocatore bean : modelFormazione) {
			ordinamento = "" + ord;
			nomeGiocatore = bean.getCognGiocatore();
			ruolo = bean.getFcRuolo().getDescRuolo();
			squadra = bean.getFcSquadra().getNomeSquadra();
			stato = "Non Convocato";

			String cidNomeSq = ContentIdGenerator.getContentId();
			FcSquadra sq = bean.getFcSquadra();
			if (sq.getImg() != null) {
				try {
					listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}

			String cidNomeImg = ContentIdGenerator.getContentId();
			InputStream inputStream = null;
			try {
				inputStream = bean.getImg().getBinaryStream();
				listImg.put(cidNomeImg, inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String color = "BGCOLOR=\"" + Costants.BG_N + "\"";
			if (Integer.parseInt(ordinamento) >= 1 && Integer.parseInt(ordinamento) <= 11) {
				color = "BGCOLOR=\"" + Costants.BG_T + "\"";
			} else if (Integer.parseInt(ordinamento) >= 12 && Integer.parseInt(ordinamento) <= 18) {
				color = "BGCOLOR=\"" + Costants.BG_R + "\"";
			}

			formazioneHtml.append("<tr ");
			formazioneHtml.append(color);
			formazioneHtml.append(">");
			formazioneHtml.append("<td>");
			formazioneHtml.append(ordinamento);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td><img src=\"cid:");
			formazioneHtml.append(cidNomeImg);
			formazioneHtml.append("\" />");
			formazioneHtml.append(nomeGiocatore);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(ruolo);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td><img src=\"cid:");
			formazioneHtml.append(cidNomeSq);
			formazioneHtml.append("\" />");
			formazioneHtml.append(squadra);
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(stato);
			formazioneHtml.append("</td>");
			formazioneHtml.append("</tr>");

			ord++;
		}

		formazioneHtml.append("</table>\n");

		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<p>Ciao ");
		formazioneHtml.append(descAttore);
		formazioneHtml.append("</p>\n");
		formazioneHtml.append("</body>\n");
		formazioneHtml.append("<html");

		StringBuilder emailDestinatario = new StringBuilder();
		String activeMail = p.getProperty("ACTIVE_MAIL");
		if ("true".equals(activeMail)) {
			List<FcAttore> attori = attoreController.findByActive(true);
			for (FcAttore a : attori) {
				if (a.isNotifiche()) {
					emailDestinatario.append(a.getEmail());
					emailDestinatario.append(";");
				}
			}
		} else {
			emailDestinatario.append(p.getProperty("to"));
		}

		String[] to = null;
		if (StringUtils.isNotEmpty(emailDestinatario.toString())) {
			to = Utils.tornaArrayString(emailDestinatario.toString(), ";");
		}

		String[] cc = null;
		String[] bcc = null;

		try {
			String from = env.getProperty("spring.mail.secondary.username");
			emailService.sendMail2(false, from, to, cc, bcc, subject, formazioneHtml.toString(), "text/html", "3", listImg);
		} catch (Exception e) {
			log.error(e.getMessage());
			try {
				String from = env.getProperty("spring.mail.primary.username");
				emailService.sendMail2(true, from, to, cc, bcc, subject, formazioneHtml.toString(), "text/html", "3", listImg);
			} catch (Exception e2) {
				log.error(e2.getMessage());
				throw e2;
			}
		}
	}

	private Grid<FcCalendarioCompetizione> getTablePartite(
			List<FcCalendarioCompetizione> listPartite) {

		Grid<FcCalendarioCompetizione> grid = new Grid<>();
		grid.setItems(listPartite);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth(Costants.WIDTH_300);

		Column<FcCalendarioCompetizione> nomeSquadraCasaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
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
					} catch (Exception e) {
						log.error(e.getMessage());
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
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);

			if (s != null && s.getSquadraCasa() != null) {
				FcSquadra sq = squadraController.findByNomeSquadra(s.getSquadraFuori());
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
				Span lblSquadra = new Span(s.getSquadraFuori().substring(0, 3));
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		nomeSquadraFuoriColumn.setSortable(false);
		nomeSquadraFuoriColumn.setAutoWidth(true);

		Column<FcCalendarioCompetizione> dataColumn = grid.addColumn(new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);
		dataColumn.setFlexGrow(2);

		return grid;
	}

	private boolean isGiocatorePartitaGiocata(FcGiocatore giocatore) {
		String activeCheckFormazione = p.getProperty("ACTIVE_CHECK_FORMAZIONE");
		if ("true".equals(activeCheckFormazione)) {
			String squadra = giocatore.getFcSquadra().getNomeSquadra();
			for (FcCalendarioCompetizione partita : listPartiteGiocate) {
				String sqCasa = partita.getSquadraCasa();
				String sqFuori = partita.getSquadraFuori();
				if (squadra.equals(sqCasa) || squadra.equals(sqFuori)) {
					return true;
				}
			}
		}
		return false;
	}

	private void impostaGiocatoriConVoto(String modulo) throws Exception {

		if (listPartiteGiocate != null && !listPartiteGiocate.isEmpty()) {

			enabledComponent(true);

			List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

			modelFormazione.clear();
			refreshAndSortGridFormazione();

			List<FcGiocatore> lGiocatoriD = new ArrayList<>();
			List<FcGiocatore> lGiocatoriC = new ArrayList<>();
			List<FcGiocatore> lGiocatoriA = new ArrayList<>();

			for (FcGiornataDett gd : lGiocatori) {

				FcGiocatore bean = gd.getFcGiocatore();
				if (gd.getOrdinamento() > 18 || !isGiocatorePartitaGiocata(bean)) {
					modelFormazione.add(bean);
					refreshAndSortGridFormazione();
					continue;
				}

				if (gd.getOrdinamento() == 1) {
					modelPlayer1.clear();
					modelPlayer1.add(bean);
					tablePlayer1.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 2) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 3) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 4) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 5) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 6) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 7) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 8) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 9) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 10) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 11) {
					if (bean.getFcRuolo().getIdRuolo().equals(Costants.D)) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.C)) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals(Costants.A)) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 12) {
					modelPlayer12.clear();
					modelPlayer12.add(bean);
					tablePlayer12.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 13) {
					modelPlayer13.clear();
					modelPlayer13.add(bean);
					tablePlayer13.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 14) {
					modelPlayer14.clear();
					modelPlayer14.add(bean);
					tablePlayer14.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 15) {
					modelPlayer15.clear();
					modelPlayer15.add(bean);
					tablePlayer15.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 16) {
					modelPlayer16.clear();
					modelPlayer16.add(bean);
					tablePlayer16.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 17) {
					modelPlayer17.clear();
					modelPlayer17.add(bean);
					tablePlayer17.getDataProvider().refreshAll();
				} else if (gd.getOrdinamento() == 18) {
					modelPlayer18.clear();
					modelPlayer18.add(bean);
					tablePlayer18.getDataProvider().refreshAll();
				}
			}

			// 5-4-1 5-3-2 4-5-1 4-4-2 4-3-3 3-5-2 3-4-3
			if (Costants.SCHEMA_541.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (countD == 4) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (countD == 5) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (countC == 4) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_532.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (countD == 4) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (countD == 5) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (countA == 2) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_451.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (countD == 4) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (countC == 4) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (countC == 5) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_442.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (countD == 4) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (countC == 4) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (countA == 2) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_433.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (countD == 4) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (countA == 2) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (countA == 3) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_352.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 4) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (countC == 5) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (countA == 2) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}

			} else if (Costants.SCHEMA_343.equals(modulo)) {

				int countD = 1;
				for (FcGiocatore g : lGiocatoriD) {
					if (countD == 1) {
						modelPlayer2.clear();
						modelPlayer2.add(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (countD == 2) {
						modelPlayer3.clear();
						modelPlayer3.add(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (countD == 3) {
						modelPlayer4.clear();
						modelPlayer4.add(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countD++;
				}

				int countC = 1;
				for (FcGiocatore g : lGiocatoriC) {
					if (countC == 1) {
						modelPlayer5.clear();
						modelPlayer5.add(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (countC == 2) {
						modelPlayer6.clear();
						modelPlayer6.add(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (countC == 3) {
						modelPlayer7.clear();
						modelPlayer7.add(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (countC == 4) {
						modelPlayer8.clear();
						modelPlayer8.add(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countC++;
				}

				int countA = 1;
				for (FcGiocatore g : lGiocatoriA) {
					if (countA == 1) {
						modelPlayer9.clear();
						modelPlayer9.add(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (countA == 2) {
						modelPlayer10.clear();
						modelPlayer10.add(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (countA == 3) {
						modelPlayer11.clear();
						modelPlayer11.add(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else {
						showMessageErrorChangeModulo(g);
					}
					countA++;
				}
			}
		}
	}

	private void showMessageErrorChangeModulo(FcGiocatore g) {
		enabledComponent(false);
		modelFormazione.add(g);
		refreshAndSortGridFormazione();
		CustomMessageDialog.showMessageError("Cambio modulo incorretto! Impossibile muovere il giocatore " + g.getCognGiocatore());
	}

	private FcGiornataGiocatore isGiocatoreOut(FcGiocatore giocatore) {
		for (FcGiornataGiocatore gg : listSqualificatiInfortunati) {
			if (gg.getFcGiocatore().getIdGiocatore() == giocatore.getIdGiocatore()) {
				return gg;
			}
		}
		return null;
	}

	private Image getImageGiocatoreOut(FcGiornataGiocatore gg) {
		Image img = null;
		if (gg != null) {
			if (gg.isInfortunato()) {
				if (gg.getNote().indexOf("INCERTO") != -1) {
					img = Utils.buildImage("help.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "icons/16/" + "help.png"));
					img.setTitle(gg.getNote());
				} else {
					img = Utils.buildImage("ospedale_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "ospedale_s.png"));
					img.setTitle(gg.getNote());
				}

			} else if (gg.isSqualificato()) {
				img = Utils.buildImage("esp_s.png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "esp_s.png"));
				img.setTitle(gg.getNote());

			}
		}
		return img;
	}

}