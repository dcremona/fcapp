package fcweb.ui.views.seriea;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.vaadin.ronny.AbsoluteLayout;

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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.mail.ContentIdGenerator;
import common.mail.MailClient;
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
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataGiocatoreService;
import fcweb.backend.service.SquadraService;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

@PageTitle("Mobile")
@Route(value = "mobile")
@RolesAllowed("USER")
public class TeamInsertMobileView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment env;

	@Autowired
	private JavaMailSenderImpl javaMailSender;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private AttoreService attoreController;

	private int WIN_WIDTH = 400;
	private int WIN_HEIGHT = 800;

	private static final String width = "85px";
	private static final String height = "105px";

	private static final int _P = 60;
	private static final int _D = 180;
	private static final int _C = 300;
	private static final int _A = 420;

	private static final int _0px = 0;
	private static final int _20px = 20;
	private static final int _70px = 70;
	private static final int _80px = 80;
	private static final int _110px = 110;
	private static final int _160px = 160;
	private static final int _240px = 240;
	private static final int _250px = 250;
	private static final int _200px = 200;
	private static final int _290px = 290;
	private static final int _320px = 320;

	private FcAttore attore = null;
	private FcGiornataInfo giornataInfo = null;
	private FcCampionato campionato = null;
	private String nextDate = null;
	private long millisDiff = 0;
	private String idAttore = "";
	private String descAttore = "";
	private Properties p = null;

	// COMPONENT
	private Button cancelButtonPartite;
	private Button cancelButtonTribuna;
	private Button rosa;
	private Button save;
	private Button viewPartite;
	private ToggleButton checkMail;
	private ComboBox<String> comboModulo;

	private Dialog dialogTribuna = null;
	private Grid<FcGiocatore> tableFormazione;

	private Dialog dialogPartite = null;
	private Grid<FcCalendarioCompetizione> tablePartite;
	private List<FcCalendarioCompetizione> listPartiteGiocate = new ArrayList<FcCalendarioCompetizione>();
	private List<FcCalendarioCompetizione> listPartite = new ArrayList<FcCalendarioCompetizione>();
	
	private List<FcGiornataGiocatore> listSqualificatiInfortunati = new ArrayList<FcGiornataGiocatore>();

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
	private static final String[] schemi = new String[] { "5-4-1", "5-3-2", "4-5-1", "4-4-2", "4-3-3", "3-5-2", "3-4-3" };
	private List<FcGiocatore> modelFormazione = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer1 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer2 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer3 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer4 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer5 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer6 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer7 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer8 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer9 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer10 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer11 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer12 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer13 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer14 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer15 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer16 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer17 = new ArrayList<FcGiocatore>();
	private List<FcGiocatore> modelPlayer18 = new ArrayList<FcGiocatore>();

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
		LOG.info("init");
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

	// private void initLayoutNew() throws Exception {
	//
	// FormLayout layout = new FormLayout();
	// layout.getStyle().set("border", Costants.BORDER_COLOR);
	// // layout.setResponsiveSteps(new ResponsiveStep("1px",1), new
	// // ResponsiveStep("800px",2), new ResponsiveStep("1200px",3), new
	// // ResponsiveStep("1600px",4));
	//
	// // TRIBUNA
	// // VerticalLayout layoutTribuna = new VerticalLayout();
	// // layoutTribuna.getStyle().set("border", Costants.BORDER_COLOR);
	// // tableFormazione = getTableFormazione(modelFormazione);
	// // layoutTribuna.add(tableFormazione);
	// // layout.add(layoutTribuna);
	//
	// // TITOLARE
	// VerticalLayout layoutTitolare = new VerticalLayout();
	// layoutTitolare.getStyle().set("border", Costants.BORDER_COLOR);
	//
	// HorizontalLayout layoutTitolareNorth = new HorizontalLayout();
	// save = new Button("Save");
	// save.setIcon(VaadinIcon.DATABASE.create());
	// save.addClickListener(this);
	// checkMail = new ToggleButton();
	// checkMail.setLabel("Invia Email");
	// checkMail.setValue(true);
	// comboModulo = new ComboBox<>("Inserisci il modulo");
	// comboModulo.setItems(schemi);
	// comboModulo.setClearButtonVisible(true);
	// layoutTitolareNorth.add(comboModulo);
	// layoutTitolareNorth.add(save);
	// layoutTitolareNorth.add(checkMail);
	// layoutTitolare.add(layoutTitolareNorth);
	//
	// Image campo = buildImage("classpath:images/", "campo.jpg");
	// layoutTitolare.add(campo);
	// layout.add(layoutTitolare);
	//
	// // PANCHINA
	// VerticalLayout layoutPanchina = new VerticalLayout();
	// layoutPanchina.getStyle().set("border", Costants.BORDER_COLOR);
	// Image panchina = buildImage("classpath:images/", "panchina.jpg");
	// layoutPanchina.add(panchina);
	// layout.add(layoutPanchina);
	//
	// // VerticalLayout layoutPartite = new VerticalLayout();
	// // layoutPartite.getStyle().set("border", Costants.BORDER_COLOR);
	// // tablePartite = getTablePartite(listPartite);
	// // layoutPartite.add(tablePartite);
	// // layout.add(layoutPartite);
	//
	// this.add(layout);
	// }

	private void initLayout() throws Exception {

		absLayout = new AbsoluteLayout(WIN_WIDTH,WIN_HEIGHT);
		dialogTribuna = new Dialog();
		dialogPartite = new Dialog();

		UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
			int resX = event.getScreenWidth();
			int resY = event.getScreenHeight();
			LOG.info("resX " + resX);
			LOG.info("resY " + resY);
			LOG.info("Math.max " + Math.max(resX, resY));
			if (Math.max(resX, resY) < 800) {
				LOG.info("small screen detected ");
			}

			absLayout.getElement().getStyle().set("border", Costants.BORDER_COLOR);
			absLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);

			dialogTribuna.setWidth(WIN_WIDTH - 50 + "px");
			dialogTribuna.setHeight(WIN_HEIGHT - 150 + "px");

			dialogPartite.setWidth(WIN_WIDTH - 50 + "px");
			dialogPartite.setHeight(WIN_HEIGHT - 250 + "px");
		});

		UI.getCurrent().getPage().addBrowserWindowResizeListener(e -> {
			int winWidth = e.getWidth();
			int winHeight = e.getHeight();
			LOG.info("winWidth " + winWidth);
			LOG.info("winHeight " + winHeight);
			if (Math.max(winWidth, winHeight) < 800) {
				LOG.info("small screen detected ");
			} else {
				absLayout.setWidth(winWidth + "px");
				absLayout.setHeight(winHeight + "px");

				dialogTribuna.setWidth(winWidth - 50 + "px");
				dialogTribuna.setHeight(winHeight - 150 + "px");

				dialogPartite.setWidth(winWidth - 50 + "px");
				dialogPartite.setHeight(winHeight - 250 + "px");
			}
		});

		cancelButtonPartite = new Button("Chiudi",event -> {
			dialogPartite.close();
		});

		cancelButtonTribuna = new Button("Chiudi",event -> {
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
		comboModulo.setItems(schemi);
		comboModulo.getElement().setAttribute("theme", "small");
		comboModulo.setClearButtonVisible(true);
		comboModulo.setPlaceholder("Modulo");
		comboModulo.addValueChangeListener(evt -> {

			// LOG.info(" addValueChangeListener " + evt.getValue());
			removeAllElementsList();

			if (evt.getValue() != null) {

				String modulo = (String) evt.getValue();

				absLayout.add(tablePlayer1, _160px, _P);

				absLayout.add(tablePlayer12, _80px, 600);
				absLayout.add(tablePlayer13, _160px, 600);
				absLayout.add(tablePlayer14, _240px, 600);
				absLayout.add(tablePlayer15, _320px, 600);
				absLayout.add(tablePlayer16, _80px, 700);
				absLayout.add(tablePlayer17, _160px, 700);
				absLayout.add(tablePlayer18, _240px, 700);

				// 5-4-1 5-3-2 4-5-1 4-4-2 4-3-3 3-5-2 3-4-3
				if (modulo.equals("5-4-1")) {

					absLayout.add(tablePlayer2, _0px, _D);
					absLayout.add(tablePlayer3, _80px, _D);
					absLayout.add(tablePlayer4, _160px, _D);
					absLayout.add(tablePlayer5, _240px, _D);
					absLayout.add(tablePlayer6, _320px, _D);

					absLayout.add(tablePlayer7, _20px, _C);
					absLayout.add(tablePlayer8, _110px, _C);
					absLayout.add(tablePlayer9, _200px, _C);
					absLayout.add(tablePlayer10, _290px, _C);

					absLayout.add(tablePlayer11, _160px, _A);

				} else if (modulo.equals("5-3-2")) {

					absLayout.add(tablePlayer2, _0px, _D);
					absLayout.add(tablePlayer3, _80px, _D);
					absLayout.add(tablePlayer4, _160px, _D);
					absLayout.add(tablePlayer5, _240px, _D);
					absLayout.add(tablePlayer6, _320px, _D);

					absLayout.add(tablePlayer7, _70px, _C);
					absLayout.add(tablePlayer8, _160px, _C);
					absLayout.add(tablePlayer9, _250px, _C);

					absLayout.add(tablePlayer10, _110px, _A);
					absLayout.add(tablePlayer11, _200px, _A);

				} else if (modulo.equals("4-5-1")) {

					absLayout.add(tablePlayer2, _20px, _D);
					absLayout.add(tablePlayer3, _110px, _D);
					absLayout.add(tablePlayer4, _200px, _D);
					absLayout.add(tablePlayer5, _290px, _D);

					absLayout.add(tablePlayer6, _0px, _C);
					absLayout.add(tablePlayer7, _80px, _C);
					absLayout.add(tablePlayer8, _160px, _C);
					absLayout.add(tablePlayer9, _240px, _C);
					absLayout.add(tablePlayer10, _320px, _C);

					absLayout.add(tablePlayer11, _160px, _A);

				} else if (modulo.equals("4-4-2")) {

					absLayout.add(tablePlayer2, _20px, _D);
					absLayout.add(tablePlayer3, _110px, _D);
					absLayout.add(tablePlayer4, _200px, _D);
					absLayout.add(tablePlayer5, _290px, _D);

					absLayout.add(tablePlayer6, _20px, _C);
					absLayout.add(tablePlayer7, _110px, _C);
					absLayout.add(tablePlayer8, _200px, _C);
					absLayout.add(tablePlayer9, _290px, _C);

					absLayout.add(tablePlayer10, _110px, _A);
					absLayout.add(tablePlayer11, _200px, _A);

				} else if (modulo.equals("4-3-3")) {

					absLayout.add(tablePlayer2, _20px, _D);
					absLayout.add(tablePlayer3, _110px, _D);
					absLayout.add(tablePlayer4, _200px, _D);
					absLayout.add(tablePlayer5, _290px, _D);

					absLayout.add(tablePlayer6, _70px, _C);
					absLayout.add(tablePlayer7, _160px, _C);
					absLayout.add(tablePlayer8, _250px, _C);

					absLayout.add(tablePlayer9, _70px, _A);
					absLayout.add(tablePlayer10, _160px, _A);
					absLayout.add(tablePlayer11, _250px, _A);

				} else if (modulo.equals("3-5-2")) {

					absLayout.add(tablePlayer2, _70px, _D);
					absLayout.add(tablePlayer3, _160px, _D);
					absLayout.add(tablePlayer4, _250px, _D);

					absLayout.add(tablePlayer5, _0px, _C);
					absLayout.add(tablePlayer6, _80px, _C);
					absLayout.add(tablePlayer7, _160px, _C);
					absLayout.add(tablePlayer8, _240px, _C);
					absLayout.add(tablePlayer9, _320px, _C);

					absLayout.add(tablePlayer10, _110px, _A);
					absLayout.add(tablePlayer11, _200px, _A);

				} else if (modulo.equals("3-4-3")) {

					absLayout.add(tablePlayer2, _70px, _D);
					absLayout.add(tablePlayer3, _160px, _D);
					absLayout.add(tablePlayer4, _250px, _D);

					absLayout.add(tablePlayer5, _20px, _C);
					absLayout.add(tablePlayer6, _110px, _C);
					absLayout.add(tablePlayer7, _200px, _C);
					absLayout.add(tablePlayer8, _290px, _C);

					absLayout.add(tablePlayer9, _70px, _A);
					absLayout.add(tablePlayer10, _160px, _A);
					absLayout.add(tablePlayer11, _250px, _A);
				}

				String ACTIVE_CHECK_FORMAZIONE = (String) p.getProperty("ACTIVE_CHECK_FORMAZIONE");
				if ("true".equals(ACTIVE_CHECK_FORMAZIONE)) {
					try {
						impostaGiocatoriConVoto(modulo);
					} catch (Exception e) {
						LOG.error(e.getMessage());
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
		layoutPartite.getStyle().set("border", Costants.BORDER_COLOR);
		layoutPartite.getStyle().set("background", Costants.LIGHT_BLUE);
		layoutPartite.setWidth("300px");
		layoutPartite.setMargin(false);
		layoutPartite.setPadding(false);
		layoutPartite.setSpacing(false);

		HorizontalLayout cssLayout = new HorizontalLayout();
		Span lblInfo = new Span(Utils.buildInfoGiornata(giornataInfo));
		lblInfo.getStyle().set("font-size", "14px");
		cssLayout.add(lblInfo);
		layoutPartite.add(cssLayout);

		HorizontalLayout cssLayout2 = new HorizontalLayout();
		Span lblInfo2 = new Span("Formazione entro: " + nextDate);
		lblInfo2.getStyle().set("font-size", "12px");
		cssLayout2.add(lblInfo2);
		layoutPartite.add(cssLayout2);

		tablePartite = getTablePartite(listPartite);

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
		layoutAvviso.getStyle().set("border", Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set("background", Costants.YELLOW);
		layoutAvviso.setWidth("100px");
		layoutAvviso.setMargin(false);
		layoutAvviso.setPadding(false);
		layoutAvviso.setSpacing(false);
		layoutAvviso.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

		Image campo = buildImage("classpath:images/", "small-campo.jpg");
		Image panchina = buildImage("classpath:images/", "small-panchina.png");

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

		this.add(absLayout);

		try {
			loadFcGiornatadett();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		if (millisDiff == 0) {
			showMessageStopInsert();
		} else {
//			SimpleTimer timer = new SimpleTimer(new BigDecimal(millisDiff / 1000));
//			timer.setHours(true);
//			timer.setMinutes(true);
//			timer.setFractions(false);
//			timer.start();
//			timer.isRunning();
//			timer.addTimerEndEvent(ev -> showMessageStopInsert());
//			layoutAvviso.add(timer);
		}

	}

	private void showMessageStopInsert() {
		String ACTIVE_CHECK_FORMAZIONE = (String) p.getProperty("ACTIVE_CHECK_FORMAZIONE");
		if ("true".equals(ACTIVE_CHECK_FORMAZIONE)) {
			LOG.info("showMessageStopInsert");
			enabledComponent(false);
			CustomMessageDialog.showMessageError("Impossibile inserire la formazione, tempo scaduto!");
		}
	}

	private void enabledComponent(boolean enabled) {
		comboModulo.setEnabled(enabled);
		save.setEnabled(enabled);
		checkMail.setEnabled(enabled);
		// viewPartite.setEnabled(enabled);
		// rosa.setEnabled(enabled);
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
		String info = "N.D.";
		if (bean != null && bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
			NumberFormat formatter = new DecimalFormat("#0.00");
			String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / Costants.DIVISORE_100);
			String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / Costants.DIVISORE_100);

			info = bean.getCognGiocatore() + "\n";
			info += "Squadra: " + bean.getFcSquadra().getNomeSquadra() + "\n";
			info += "Giocate: " + bean.getFcStatistiche().getGiocate() + "\n";
			info += "MV: " + mv + "\n";
			info += "FV: " + fv + "\n";
			info += "Goal: " + bean.getFcStatistiche().getGoalFatto() + "\n";
			info += "Assist: " + bean.getFcStatistiche().getAssist() + "\n";
			info += "Ammonizione: " + bean.getFcStatistiche().getAmmonizione() + "\n";
			info += "Espulsione: " + bean.getFcStatistiche().getEspulsione() + "\n";
			if ("P".equals(bean.getFcRuolo().getIdRuolo().toUpperCase())) {
				info += "Goal Subito: " + bean.getFcStatistiche().getGoalSubito() + "\n";
			}
		}

		return info;
	}

	private void refreshAndSortGridFormazione() {
		// LOG.debug("refreshAndSortGridFormazione");
		modelFormazione.sort((p1,
				p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
		tableFormazione.getDataProvider().refreshAll();
	}

	private void removeAllElementsList() {

		if (modelPlayer1.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer1.get(0);
			modelFormazione.add(bean);
			modelPlayer1.clear();
			tablePlayer1.getDataProvider().refreshAll();
		}
		if (modelPlayer2.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer2.get(0);
			modelFormazione.add(bean);
			modelPlayer2.clear();
			tablePlayer2.getDataProvider().refreshAll();
		}
		if (modelPlayer3.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer3.get(0);
			modelFormazione.add(bean);
			modelPlayer3.clear();
			tablePlayer3.getDataProvider().refreshAll();
		}
		if (modelPlayer4.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer4.get(0);
			modelFormazione.add(bean);
			modelPlayer4.clear();
			tablePlayer4.getDataProvider().refreshAll();
		}
		if (modelPlayer5.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer5.get(0);
			modelFormazione.add(bean);
			modelPlayer5.clear();
			tablePlayer5.getDataProvider().refreshAll();
		}
		if (modelPlayer6.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer6.get(0);
			modelFormazione.add(bean);
			modelPlayer6.clear();
			tablePlayer6.getDataProvider().refreshAll();
		}
		if (modelPlayer7.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer7.get(0);
			modelFormazione.add(bean);
			modelPlayer7.clear();
			tablePlayer7.getDataProvider().refreshAll();
		}
		if (modelPlayer8.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer8.get(0);
			modelFormazione.add(bean);
			modelPlayer8.clear();
			tablePlayer8.getDataProvider().refreshAll();
		}
		if (modelPlayer9.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer9.get(0);
			modelFormazione.add(bean);
			modelPlayer9.clear();
			tablePlayer9.getDataProvider().refreshAll();
		}
		if (modelPlayer10.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer10.get(0);
			modelFormazione.add(bean);
			modelPlayer10.clear();
			tablePlayer10.getDataProvider().refreshAll();
		}
		if (modelPlayer11.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer11.get(0);
			modelFormazione.add(bean);
			modelPlayer11.clear();
			tablePlayer11.getDataProvider().refreshAll();
		}
		if (modelPlayer12.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer12.get(0);
			modelFormazione.add(bean);
			modelPlayer12.clear();
			tablePlayer12.getDataProvider().refreshAll();
		}
		if (modelPlayer13.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer13.get(0);
			modelFormazione.add(bean);
			modelPlayer13.clear();
			tablePlayer13.getDataProvider().refreshAll();
		}
		if (modelPlayer14.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer14.get(0);
			modelFormazione.add(bean);
			modelPlayer14.clear();
			tablePlayer14.getDataProvider().refreshAll();
		}
		if (modelPlayer15.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer15.get(0);
			modelFormazione.add(bean);
			modelPlayer15.clear();
			tablePlayer15.getDataProvider().refreshAll();
		}
		if (modelPlayer16.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer16.get(0);
			modelFormazione.add(bean);
			modelPlayer16.clear();
			tablePlayer16.getDataProvider().refreshAll();
		}
		if (modelPlayer17.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer17.get(0);
			modelFormazione.add(bean);
			modelPlayer17.clear();
			tablePlayer17.getDataProvider().refreshAll();
		}
		if (modelPlayer18.size() != 0) {
			FcGiocatore bean = (FcGiocatore) modelPlayer18.get(0);
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

		ArrayList<FcGiocatore> beans = new ArrayList<FcGiocatore>();
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
		// grid.getStyle().set("border", Costants.BORDER_COLOR);
		grid.setAllRowsVisible(true);
		grid.setWidth(width);
		grid.setHeight(height);

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
				if ("P".equals(ruolo)) {
					cellLayout.getElement().getStyle().set("border", Costants.BORDER_COLOR_P);
				} else if ("D".equals(ruolo)) {
					cellLayout.getElement().getStyle().set("border", Costants.BORDER_COLOR_D);
				} else if ("C".equals(ruolo)) {
					cellLayout.getElement().getStyle().set("border", Costants.BORDER_COLOR_C);
				} else if ("A".equals(ruolo)) {
					cellLayout.getElement().getStyle().set("border", Costants.BORDER_COLOR_A);
				}
				
				if (isGiocatoreOut(p) != null) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}

				HorizontalLayout cellLayoutImg = new HorizontalLayout();
				cellLayoutImg.setMargin(false);
				cellLayoutImg.setPadding(false);
				cellLayoutImg.setSpacing(false);
				cellLayoutImg.setSizeUndefined();
				cellLayoutImg.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

				Image imgR = buildImage("classpath:images/", p.getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				imgR.setTitle(title);
				cellLayoutImg.add(imgR);

				FcSquadra sq = p.getFcSquadra();
				if (sq != null && sq.getImg() != null) {
					try {
						Image imgSq = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						imgSq.setTitle(title);
						cellLayoutImg.add(imgSq);
					} catch (SQLException e) {
						e.printStackTrace();
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
				Image imgMv = buildImage("classpath:images/", imgThink);
				imgMv.setTitle(title);
				cellLayoutImg.add(imgMv);

				StreamResource resource = new StreamResource(p.getNomeImg(),() -> {
					InputStream inputStream = null;
					try {
						inputStream = p.getImgSmall().getBinaryStream();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return inputStream;
				});
				Image img = new Image(resource,"");
				img.setTitle(title);
				img.setSrc(resource);
				
				Span lblGiocatore = new Span(p.getCognGiocatore());
				lblGiocatore.getStyle().set("font-size", "9px");
				lblGiocatore.setTitle(title);
				lblGiocatore.setWidth("60px");

				cellLayout.add(cellLayoutImg);
				cellLayout.add(img);
				cellLayout.add(lblGiocatore);

				Element element = cellLayout.getElement(); // DOM element
				element.addEventListener("click", e -> {

					FcGiocatore bean = (FcGiocatore) p;

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
		grid.setWidth("300px");

//		Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(f -> {
//			HorizontalLayout cellLayout = new HorizontalLayout();
//			cellLayout.setMargin(false);
//			cellLayout.setPadding(false);
//			cellLayout.setSpacing(false);
//			cellLayout.setAlignItems(Alignment.STRETCH);
//			cellLayout.setSizeFull();
//			if (f != null && f.getFcRuolo() != null) {
//				Image img = buildImage("classpath:images/", f.getFcRuolo().getIdRuolo().toLowerCase() + ".png");
//				cellLayout.add(img);
//			}
//			return cellLayout;
//		}));
//		ruoloColumn.setSortable(true);
//		ruoloColumn.setHeader("R");
//		ruoloColumn.setWidth("30px");
//		ruoloColumn.setComparator((p1,
//				p2) -> p1.getFcRuolo().getIdRuolo().compareTo(p2.getFcRuolo().getIdRuolo()));
//		// ruoloColumn.setAutoWidth(true);
//
//		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(g -> g != null ? g.getCognGiocatore() : "");
//		cognGiocatoreColumn.setSortable(false);
//		cognGiocatoreColumn.setHeader("Giocatore");
//		cognGiocatoreColumn.setWidth("150px");
//		// cognGiocatoreColumn.setAutoWidth(true);
		
		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (g.getFcRuolo() != null) {
					Image img = buildImage("classpath:images/", g.getFcRuolo().getIdRuolo().toLowerCase() + ".png");
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
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
					if (gg.isInfortunato()) {
						Image img = buildImage("classpath:images/", "ospedale_s.png");
						img.setTitle(gg.getNote());
						cellLayout.add(img);
					} else if (gg.isSqualificato()) {
						Image img = buildImage("classpath:images/", "esp_s.png");
						img.setTitle(gg.getNote());
						cellLayout.add(img);
					}
				}
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader("Giocatore");
		cognGiocatoreColumn.setWidth("160px");
		// cognGiocatoreColumn.setAutoWidth(true);
		
		Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (isGiocatoreOut(g) != null) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				if (g.getFcSquadra() != null) {
					FcSquadra sq = g.getFcSquadra();
					if (sq.getImg() != null) {
						try {
							Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
							img.setTitle(title);
							cellLayout.add(img);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					Span lblSquadra = new Span();
					lblSquadra.setText(sq.getNomeSquadra().substring(0, 3));
					lblSquadra.setTitle(title);
					cellLayout.add(lblSquadra);
				}
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setSortable(true);
		nomeSquadraColumn.setComparator((p1,p2) -> p1.getFcSquadra().getNomeSquadra().compareTo(p2.getFcSquadra().getNomeSquadra()));
		nomeSquadraColumn.setHeader("Sq");
		nomeSquadraColumn.setWidth("70px");
		// nomeSquadraColumn.setAutoWidth(true);		

		Column<FcGiocatore> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (isGiocatoreOut(g) != null) {
					cellLayout.getElement().getStyle().set("background", Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}

				FcStatistiche s = g.getFcStatistiche();
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);
				img.setTitle(title);

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getMediaVoto() / Costants.DIVISORE_100;
				}
				String sTotPunti = myFormatter.format(d);
				Span lbl = new Span(sTotPunti);
				lbl.setTitle(title);

				cellLayout.add(img);
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator((p1,p2) -> p1.getFcStatistiche().getMediaVoto().compareTo(p2.getFcStatistiche().getMediaVoto()));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setWidth("70px");
		// mediaVotoColumn.setAutoWidth(true);

		grid.addItemClickListener(event -> {
			// LOG.info("addItemClickListener");
			if (comboModulo.getValue() == null) {
				LOG.info("valModulo null");
				return;
			}
			String valModulo = (String) comboModulo.getValue();
			if (valModulo == null) {
				LOG.info("valModulo null");
				return;
			}

			FcGiocatore bean = (FcGiocatore) event.getItem();

			if (bean != null) {

				if (isGiocatorePartitaGiocata(bean)) {
					CustomMessageDialog.showMessageError("Impossibile muovere il giocatore!");
					return;
				}

				if (existGiocatore(bean)) {
					LOG.info("existGiocatore true");
					return;
				}

				boolean bDel = false;
				if (bean.getFcRuolo().getIdRuolo().equals("P")) {
					if (modelPlayer1.size() == 0) {
						modelPlayer1.add(bean);
						tablePlayer1.getDataProvider().refreshAll();
						bDel = true;
					} else {
						if (modelPlayer12.size() == 0) {
							modelPlayer12.add(bean);
							tablePlayer12.getDataProvider().refreshAll();
							bDel = true;
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals("D")) {

					if (valModulo.equals("5-4-1") || valModulo.equals("5-3-2")) {

						if (modelPlayer2.size() == 0) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.size() == 0) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.size() == 0) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer5.size() == 0) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.size() == 0) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.size() == 0) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("4-5-1") || valModulo.equals("4-4-2") || valModulo.equals("4-3-3")) {

						if (modelPlayer2.size() == 0) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.size() == 0) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.size() == 0) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer5.size() == 0) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.size() == 0) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.size() == 0) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("3-5-2") || valModulo.equals("3-4-3")) {

						if (modelPlayer2.size() == 0) {
							modelPlayer2.add(bean);
							tablePlayer2.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer3.size() == 0) {
							modelPlayer3.add(bean);
							tablePlayer3.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer4.size() == 0) {
							modelPlayer4.add(bean);
							tablePlayer4.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer13.size() == 0) {
								modelPlayer13.add(bean);
								tablePlayer13.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer14.size() == 0) {
								modelPlayer14.add(bean);
								tablePlayer14.getDataProvider().refreshAll();
								bDel = true;
							}
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {

					if (valModulo.equals("4-5-1")) {

						if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.size() == 0) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("3-5-2")) {

						if (modelPlayer5.size() == 0) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("5-4-1")) {

						if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.size() == 0) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("4-4-2")) {

						if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("3-4-3")) {

						if (modelPlayer5.size() == 0) {
							modelPlayer5.add(bean);
							tablePlayer5.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("5-3-2")) {

						if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("4-3-3")) {
						if (modelPlayer6.size() == 0) {
							modelPlayer6.add(bean);
							tablePlayer6.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer7.size() == 0) {
							modelPlayer7.add(bean);
							tablePlayer7.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer8.size() == 0) {
							modelPlayer8.add(bean);
							tablePlayer8.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer15.size() == 0) {
								modelPlayer15.add(bean);
								tablePlayer15.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer16.size() == 0) {
								modelPlayer16.add(bean);
								tablePlayer16.getDataProvider().refreshAll();
								bDel = true;
							}
						}
					}

				} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {

					if (valModulo.equals("4-5-1") || valModulo.equals("5-4-1")) {

						if (modelPlayer11.size() == 0) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.size() == 0) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.size() == 0) {
								modelPlayer18.add(bean);
								tablePlayer18.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("3-5-2") || valModulo.equals("4-4-2") || valModulo.equals("5-3-2")) {

						if (modelPlayer10.size() == 0) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer11.size() == 0) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.size() == 0) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.size() == 0) {
								modelPlayer18.add(bean);
								tablePlayer18.getDataProvider().refreshAll();
								bDel = true;
							}
						}

					} else if (valModulo.equals("3-4-3") || valModulo.equals("4-3-3")) {

						if (modelPlayer9.size() == 0) {
							modelPlayer9.add(bean);
							tablePlayer9.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer10.size() == 0) {
							modelPlayer10.add(bean);
							tablePlayer10.getDataProvider().refreshAll();
							bDel = true;
						} else if (modelPlayer11.size() == 0) {
							modelPlayer11.add(bean);
							tablePlayer11.getDataProvider().refreshAll();
							bDel = true;
						} else {
							if (modelPlayer17.size() == 0) {
								modelPlayer17.add(bean);
								tablePlayer17.getDataProvider().refreshAll();
								bDel = true;
							} else if (modelPlayer18.size() == 0) {
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

		if (modelPlayer1.size() != 0) {
			if (modelPlayer1.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer2.size() != 0) {
			if (modelPlayer2.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer3.size() != 0) {
			if (modelPlayer3.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer4.size() != 0) {
			if (modelPlayer4.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer5.size() != 0) {
			if (modelPlayer5.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer6.size() != 0) {
			if (modelPlayer6.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer7.size() != 0) {
			if (modelPlayer7.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer8.size() != 0) {
			if (modelPlayer8.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer9.size() != 0) {
			if (modelPlayer9.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer10.size() != 0) {
			if (modelPlayer10.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer11.size() != 0) {
			if (modelPlayer11.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer12.size() != 0) {
			if (modelPlayer12.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer13.size() != 0) {
			if (modelPlayer13.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer14.size() != 0) {
			if (modelPlayer14.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer15.size() != 0) {
			if (modelPlayer15.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer16.size() != 0) {
			if (modelPlayer16.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer17.size() != 0) {
			if (modelPlayer17.indexOf(g) != -1) {
				return true;
			}
		}
		if (modelPlayer18.size() != 0) {
			if (modelPlayer18.indexOf(g) != -1) {
				return true;
			}
		}
		return false;
	}

	private void loadFcGiornatadett() throws Exception {

		LOG.info("loadFcGiornatadett");

		List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

		if (lGiocatori.size() == 0) {
			this.comboModulo.setValue(null);
			removeAllElementsList();
			return;
		}

		int countD = 0;
		int countC = 0;
		int countA = 0;

		for (FcGiornataDett gd : lGiocatori) {

			if (gd.getOrdinamento() < 12) {
				if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("D")) {
					countD++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("C")) {
					countC++;
				} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("A")) {
					countA++;
				}
			}
		}

		String schema = countD + "-" + countC + "-" + countA;

		this.comboModulo.setValue(schema);

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

				int GIORNATA_SERIEA = giornataInfo.getCodiceGiornata();
				String DESC_GIORNATA = giornataInfo.getDescGiornataFc();

				boolean bError = false;
				try {
					insert(GIORNATA_SERIEA);
				} catch (Exception exi) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exi.getMessage());
					bError = true;
					return;
				}

				if (!bError && checkMail.getValue()) {
					try {
						String dataora = getSysdate();

						sendNewMail(DESC_GIORNATA);

						LOG.info("send_mail OK");

						try {
							insert_dett_info(GIORNATA_SERIEA, dataora);
							LOG.info("insert_dett_info OK");
						} catch (Exception exd) {
							LOG.error(exd.getMessage());
							CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exd.getMessage());
						}

						CustomMessageDialog.showMessageInfo("Formazione inserita, email inviata con successo!");

					} catch (Exception excpt) {
						LOG.error(excpt.getMessage());
						CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, excpt.getMessage());
						return;
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

					String dataora = rs.getString(1);
					return dataora;
				}
				return null;
			}
		});
	}

	private boolean check() {

		if (modelPlayer1.size() == 0 || modelPlayer2.size() == 0 || modelPlayer3.size() == 0 || modelPlayer4.size() == 0 || modelPlayer5.size() == 0 || modelPlayer6.size() == 0 || modelPlayer7.size() == 0 || modelPlayer8.size() == 0 || modelPlayer9.size() == 0 || modelPlayer10.size() == 0 || modelPlayer11.size() == 0 || modelPlayer12.size() == 0 || modelPlayer13.size() == 0 || modelPlayer14.size() == 0 || modelPlayer15.size() == 0 || modelPlayer16.size() == 0 || modelPlayer17.size() == 0 || modelPlayer18.size() == 0) {
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

			String ID_GIORNATA = "" + giornata;
			String ID_GIOCATORE = "";
			String ID_STATO_GIOCATORE = "";
			String ORDINAMENTO = "";
			int ord = 1;
			for (int i = 0; i < 18; i++) {

				ORDINAMENTO = "" + ord;
				ord++;
				if (i == 0) {
					FcGiocatore bean = (FcGiocatore) modelPlayer1.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 1) {
					FcGiocatore bean = (FcGiocatore) modelPlayer2.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 2) {
					FcGiocatore bean = (FcGiocatore) modelPlayer3.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 3) {
					FcGiocatore bean = (FcGiocatore) modelPlayer4.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 4) {
					FcGiocatore bean = (FcGiocatore) modelPlayer5.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 5) {
					FcGiocatore bean = (FcGiocatore) modelPlayer6.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 6) {
					FcGiocatore bean = (FcGiocatore) modelPlayer7.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 7) {
					FcGiocatore bean = (FcGiocatore) modelPlayer8.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 8) {
					FcGiocatore bean = (FcGiocatore) modelPlayer9.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 9) {
					FcGiocatore bean = (FcGiocatore) modelPlayer10.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 10) {
					FcGiocatore bean = (FcGiocatore) modelPlayer11.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 11) {
					FcGiocatore bean = (FcGiocatore) modelPlayer12.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 12) {
					FcGiocatore bean = (FcGiocatore) modelPlayer13.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 13) {
					FcGiocatore bean = (FcGiocatore) modelPlayer14.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 14) {
					FcGiocatore bean = (FcGiocatore) modelPlayer15.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 15) {
					FcGiocatore bean = (FcGiocatore) modelPlayer16.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 16) {
					FcGiocatore bean = (FcGiocatore) modelPlayer17.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 17) {
					FcGiocatore bean = (FcGiocatore) modelPlayer18.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				}

				query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + ID_GIORNATA + ",";
				query += idAttore + "," + ID_GIOCATORE + ",'" + ID_STATO_GIOCATORE + "'," + ORDINAMENTO + ",0)";
				jdbcTemplate.update(query);
			}

			for (int i = 0; i < modelFormazione.size(); i++) {
				ORDINAMENTO = "" + ord;
				ord++;
				FcGiocatore bean = (FcGiocatore) modelFormazione.get(i);
				ID_GIOCATORE = "" + bean.getIdGiocatore();
				ID_STATO_GIOCATORE = "N";

				query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + ID_GIORNATA + ",";
				query += idAttore + "," + ID_GIOCATORE + ",'" + ID_STATO_GIOCATORE + "'," + ORDINAMENTO + ",0)";

				jdbcTemplate.update(query);
			}

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void insert_dett_info(int giornata, String dataora)
			throws Exception {

		String query = "";
		try {
			query = " DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
			jdbcTemplate.update(query);

			String ID_GIORNATA = "" + giornata;
			query = " INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES (" + ID_GIORNATA + ",";
			query += idAttore + ",1, '" + dataora + "')";

			jdbcTemplate.update(query);

		} catch (Exception e) {
			CustomMessageDialog.showMessageError("insert_dett_info " + e.getMessage());
		}
	}

	private void sendNewMail(String desc_giornata) throws AddressException,
			IOException, MessagingException, NamingException {

		String subject = "Formazione " + descAttore + " - " + desc_giornata;

		String modulo = this.comboModulo.getValue().toString();

		String formazioneHtml = "";
		formazioneHtml += "<html><head><title>FC</title></head>\n";
		formazioneHtml += "<body>\n";
		formazioneHtml += "<p>" + desc_giornata + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>" + modulo + "</p>\n";
		formazioneHtml += "<br>\n";

		formazioneHtml += "<table>";

		String NOME_GIOCATORE = "";
		String RUOLO = "";
		String STATO = "";
		String ORDINAMENTO = "";
		String SQUADRA = "";
		int ord = 1;
		Map<String, InputStream> listImg = new HashMap<String, InputStream>();
		for (int i = 0; i < 18; i++) {

			ORDINAMENTO = "" + ord;
			FcGiocatore bean = null;

			if (i == 0) {
				bean = (FcGiocatore) modelPlayer1.get(0);
			} else if (i == 1) {
				bean = (FcGiocatore) modelPlayer2.get(0);
			} else if (i == 2) {
				bean = (FcGiocatore) modelPlayer3.get(0);
			} else if (i == 3) {
				bean = (FcGiocatore) modelPlayer4.get(0);
			} else if (i == 4) {
				bean = (FcGiocatore) modelPlayer5.get(0);
			} else if (i == 5) {
				bean = (FcGiocatore) modelPlayer6.get(0);
			} else if (i == 6) {
				bean = (FcGiocatore) modelPlayer7.get(0);
			} else if (i == 7) {
				bean = (FcGiocatore) modelPlayer8.get(0);
			} else if (i == 8) {
				bean = (FcGiocatore) modelPlayer9.get(0);
			} else if (i == 9) {
				bean = (FcGiocatore) modelPlayer10.get(0);
			} else if (i == 10) {
				bean = (FcGiocatore) modelPlayer11.get(0);
			} else if (i == 11) {
				bean = (FcGiocatore) modelPlayer12.get(0);
			} else if (i == 12) {
				bean = (FcGiocatore) modelPlayer13.get(0);
			} else if (i == 13) {
				bean = (FcGiocatore) modelPlayer14.get(0);
			} else if (i == 14) {
				bean = (FcGiocatore) modelPlayer15.get(0);
			} else if (i == 15) {
				bean = (FcGiocatore) modelPlayer16.get(0);
			} else if (i == 16) {
				bean = (FcGiocatore) modelPlayer17.get(0);
			} else if (i == 17) {
				bean = (FcGiocatore) modelPlayer18.get(0);
			}

			NOME_GIOCATORE = bean.getCognGiocatore();
			RUOLO = bean.getFcRuolo().getDescRuolo();
			SQUADRA = bean.getFcSquadra().getNomeSquadra();

			//Resource resourceNomeSq = resourceLoader.getResource("classpath:img/squadre/" + bean.getFcSquadra().getNomeSquadra() + ".png");
//			listImg.put(cidNomeSq, resourceNomeSq.getInputStream());
			
			String cidNomeImg = ContentIdGenerator.getContentId();
			InputStream inputStream = null;
			try {
				inputStream = bean.getImg().getBinaryStream();
				listImg.put(cidNomeImg, inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String cidNomeSq = ContentIdGenerator.getContentId();
			FcSquadra sq = bean.getFcSquadra();
			if (sq.getImg() != null) {
				try {
					listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (i < 11) {
				STATO = "Titolare";
			} else {
				STATO = "Riserva";
			}
			String color = "BGCOLOR=\"" + Costants.BG_N + "\"";
			if (Integer.parseInt(ORDINAMENTO) >= 1 && Integer.parseInt(ORDINAMENTO) <= 11) {
				color = "BGCOLOR=\"" + Costants.BG_T + "\"";
			} else if (Integer.parseInt(ORDINAMENTO) >= 12 && Integer.parseInt(ORDINAMENTO) <= 18) {
				color = "BGCOLOR=\"" + Costants.BG_R + "\"";
			}

			formazioneHtml += "<tr " + color + ">";
			formazioneHtml += "<td>";
			formazioneHtml += ORDINAMENTO;
			formazioneHtml += "</td>";
			formazioneHtml += "<td><img src=\"cid:" + cidNomeImg + "\" />";
			formazioneHtml += NOME_GIOCATORE;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += RUOLO;
			formazioneHtml += "</td>";
			formazioneHtml += "<td><img src=\"cid:" + cidNomeSq + "\" />";
			formazioneHtml += SQUADRA;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += STATO;
			formazioneHtml += "</td>";
			formazioneHtml += "</tr>";

			ord++;
		}

		for (int i = 0; i < modelFormazione.size(); i++) {
			ORDINAMENTO = "" + ord;
			FcGiocatore bean = (FcGiocatore) modelFormazione.get(i);

			NOME_GIOCATORE = bean.getCognGiocatore();
			RUOLO = bean.getFcRuolo().getDescRuolo();
			SQUADRA = bean.getFcSquadra().getNomeSquadra();
			STATO = "Non Convocato";
			
//			Resource resourceNomeSq = resourceLoader.getResource("classpath:img/squadre/" + bean.getFcSquadra().getNomeSquadra() + ".png");
//			listImg.put(cidNomeSq, resourceNomeSq.getInputStream());

			String cidNomeImg = ContentIdGenerator.getContentId();
			InputStream inputStream = null;
			try {
				inputStream = bean.getImg().getBinaryStream();
				listImg.put(cidNomeImg, inputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String cidNomeSq = ContentIdGenerator.getContentId();
			FcSquadra sq = bean.getFcSquadra();
			if (sq.getImg() != null) {
				try {
					listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			String color = "BGCOLOR=\"" + Costants.BG_N + "\"";
			if (Integer.parseInt(ORDINAMENTO) >= 1 && Integer.parseInt(ORDINAMENTO) <= 11) {
				color = "BGCOLOR=\"" + Costants.BG_T + "\"";
			} else if (Integer.parseInt(ORDINAMENTO) >= 12 && Integer.parseInt(ORDINAMENTO) <= 18) {
				color = "BGCOLOR=\"" + Costants.BG_R + "\"";
			}

			formazioneHtml += "<tr " + color + ">";
			formazioneHtml += "<td>";
			formazioneHtml += ORDINAMENTO;
			formazioneHtml += "</td>";
			formazioneHtml += "<td><img src=\"cid:" + cidNomeImg + "\" />";
			formazioneHtml += NOME_GIOCATORE;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += RUOLO;
			formazioneHtml += "</td>";
			formazioneHtml += "<td><img src=\"cid:" + cidNomeSq + "\" />";
			formazioneHtml += SQUADRA;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += STATO;
			formazioneHtml += "</td>";
			formazioneHtml += "</tr>";

			ord++;
		}

		formazioneHtml += "</table>\n";

		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>Ciao " + descAttore + "</p>\n";
		formazioneHtml += "</body>\n";
		formazioneHtml += "<html>";

		MailClient client = new MailClient(javaMailSender);
		String email_destinatario = "";
		String ACTIVE_MAIL = (String) p.getProperty("ACTIVE_MAIL");
		if ("true".equals(ACTIVE_MAIL)) {
			List<FcAttore> attori = attoreController.findByActive(true);
			for (FcAttore a : attori) {
				if (a.isNotifiche()) {
					email_destinatario += a.getEmail() + ";";
				}
			}
		} else {
			email_destinatario = (String) p.getProperty("to");
		}

		String[] to = null;
		if (email_destinatario != null && !email_destinatario.equals("")) {
			to = Utils.tornaArrayString(email_destinatario, ";");
		}

		String[] cc = null;
		String[] bcc = null;

		String from = (String) env.getProperty("spring.mail.username");
		
		client.sendMail2(from,to, cc, bcc, subject, formazioneHtml, "text/html", "3", listImg);

	}

	private Image buildImage(String path, String nomeImg) {
		StreamResource resource = new StreamResource(nomeImg,() -> {
			Resource r = resourceLoader.getResource(path + nomeImg);
			InputStream inputStream = null;
			try {
				inputStream = r.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return inputStream;
		});

		Image img = new Image(resource,"");
		return img;
	}

	private Grid<FcCalendarioCompetizione> getTablePartite(
			List<FcCalendarioCompetizione> listPartite) {

		Grid<FcCalendarioCompetizione> grid = new Grid<>();
		grid.setItems(listPartite);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth("300px");

		Column<FcCalendarioCompetizione> nomeSquadraCasaColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (s != null && s.getSquadraCasa() != null) {
//				Image img = buildImage("classpath:/img/squadre/", s.getSquadraCasa() + ".png");
//				cellLayout.add(img);
				FcSquadra sq = squadraController.findByIdSquadra(s.getIdSquadraCasa());
				if (sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Span lblSquadra = new Span(s.getSquadraCasa().substring(0, 3));
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraCasaColumn.setSortable(false);
		// nomeSquadraCasaColumn.setHeader("Casa");
		nomeSquadraCasaColumn.setAutoWidth(true);

		Column<FcCalendarioCompetizione> nomeSquadraFuoriColumn = grid.addColumn(new ComponentRenderer<>(s -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();

			if (s != null && s.getSquadraCasa() != null) {
//				Image img = buildImage("classpath:/img/squadre/", s.getSquadraFuori() + ".png");
//				cellLayout.add(img);
				FcSquadra sq = squadraController.findByNomeSquadra(s.getSquadraFuori());
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Span lblSquadra = new Span(s.getSquadraFuori().substring(0, 3));
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		nomeSquadraFuoriColumn.setSortable(false);
		// nomeSquadraFuoriColumn.setHeader("Fuori");
		nomeSquadraFuoriColumn.setAutoWidth(true);

		Column<FcCalendarioCompetizione> dataColumn = grid.addColumn(new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,() -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)));
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);
		dataColumn.setFlexGrow(2);

		return grid;
	}

	private boolean isGiocatorePartitaGiocata(FcGiocatore giocatore) {
		String ACTIVE_CHECK_FORMAZIONE = (String) p.getProperty("ACTIVE_CHECK_FORMAZIONE");
		if ("true".equals(ACTIVE_CHECK_FORMAZIONE)) {
			// LOG.debug("isGiocatorePartitaGiocata");
			String squadra = giocatore.getFcSquadra().getNomeSquadra();
			// LOG.info("squadra " + squadra);
			for (FcCalendarioCompetizione partita : listPartiteGiocate) {
				String sqCasa = partita.getSquadraCasa();
				String sqFuori = partita.getSquadraFuori();
				// LOG.info("sqCasa " + sqCasa);
				// LOG.info("sqFuori " + sqFuori);
				if (squadra.equals(sqCasa) || squadra.equals(sqFuori)) {
					return true;
				}
			}
		}
		return false;
	}

	private void impostaGiocatoriConVoto(String modulo) throws Exception {

		// LOG.info("impostaGiocatoriConVoto");

		if (listPartiteGiocate != null && listPartiteGiocate.size() > 0) {

			enabledComponent(true);

			List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

			modelFormazione.clear();
			refreshAndSortGridFormazione();

			List<FcGiocatore> lGiocatoriD = new ArrayList<FcGiocatore>();
			List<FcGiocatore> lGiocatoriC = new ArrayList<FcGiocatore>();
			List<FcGiocatore> lGiocatoriA = new ArrayList<FcGiocatore>();

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
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 3) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 4) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 5) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 6) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 7) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 8) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 9) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 10) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						lGiocatoriA.add(bean);
					}
				} else if (gd.getOrdinamento() == 11) {
					if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						lGiocatoriD.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						lGiocatoriC.add(bean);
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
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
			if (modulo.equals("5-4-1")) {

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

			} else if (modulo.equals("5-3-2")) {

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

			} else if (modulo.equals("4-5-1")) {

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

			} else if (modulo.equals("4-4-2")) {

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

			} else if (modulo.equals("4-3-3")) {

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

			} else if (modulo.equals("3-5-2")) {

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

			} else if (modulo.equals("3-4-3")) {

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
}