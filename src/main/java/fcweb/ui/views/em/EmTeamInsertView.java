package fcweb.ui.views.em;

import java.io.InputStream;
import java.io.Serial;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.ronny.AbsoluteLayout;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.server.VaadinSession;

import common.util.ContentIdGenerator;
import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.CalendarioCompetizioneService;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Schiera Formazione")
@Route(value = "eminsert", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmTeamInsertView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String width = "100px";
	private static final String height = "120px";

	private static final int _P = 210;
	private static final int _D = 360;
	private static final int _C = 510;
	private static final int _A = 660;

	private static final int _350px = 350;
	private static final int _400px = 400;
	private static final int _450px = 450;
	private static final int _500px = 500;
	private static final int _550px = 550;
	private static final int _600px = 600;
	private static final int _650px = 650;
	private static final int _700px = 700;
	private static final int _750px = 750;
	private static final int _860px = 860;
	private static final int _960px = 960;
	private static final int _1060px = 1060;
	private static final int _1200px = 1200;

	private FcAttore attore = null;
	private FcGiornataInfo giornataInfo = null;
	private FcCampionato campionato = null;
	private String nextDate = null;
	private long millisDiff = 0;
	private String idAttore = "";
	private String descAttore = "";
	private Properties p = null;

    private ToggleButton checkMail;
	private ComboBox<String> comboModulo;

	private Grid<FcGiocatore> tableFormazione;
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
	private Grid<FcGiocatore> tablePlayer19;
	private Grid<FcGiocatore> tablePlayer20;
	private Grid<FcGiocatore> tablePlayer21;
	private Grid<FcGiocatore> tablePlayer22;
	private Grid<FcGiocatore> tablePlayer23;

	// DATA
	private static final String[] schemi = new String[] { "5-4-1", "5-3-2", "4-5-1", "4-4-2", "4-3-3", "3-5-2", "3-4-3" };
	private List<FcGiocatore> modelFormazione = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer1 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer2 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer3 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer4 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer5 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer6 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer7 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer8 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer9 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer10 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer11 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer12 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer13 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer14 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer15 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer16 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer17 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer18 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer19 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer20 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer21 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer22 = new ArrayList<>();
	private final List<FcGiocatore> modelPlayer23 = new ArrayList<>();

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private CalendarioCompetizioneService calendarioTimController;

    private AbsoluteLayout absLayout;

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private SquadraService squadraController;

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

	private void initData() {

		p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
		giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		nextDate = (String) VaadinSession.getCurrent().getAttribute("NEXTDATE");
		millisDiff = (long) VaadinSession.getCurrent().getAttribute("MILLISDIFF");

		idAttore = "" + attore.getIdAttore();
		descAttore = attore.getDescAttore();

		modelFormazione = getModelFormazione(attore, campionato);
	}

	private void initLayout() {

		absLayout = new AbsoluteLayout(1600,1200);
		absLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		absLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);

        Button save = new Button("Save");
		save.setIcon(VaadinIcon.DATABASE.create());
		save.addClickListener(this);

		checkMail = new ToggleButton();
		checkMail.setLabel("Invia Email");
		checkMail.setValue(true);

		comboModulo = new ComboBox<>();
		comboModulo.setItems(schemi);
		comboModulo.setClearButtonVisible(true);
		comboModulo.setPlaceholder("Modulo");
		comboModulo.addValueChangeListener(evt -> {

            LOG.info(" addValueChangeListener {}", evt.getValue());
			removeAllElementsList();

			if (evt.getValue() != null) {
				String modulo = evt.getValue();

				absLayout.add(tablePlayer1, _550px, _P);

				absLayout.add(tablePlayer12, _860px, _P);
				absLayout.add(tablePlayer13, _960px, _P);
				absLayout.add(tablePlayer14, _1060px, _P);

				absLayout.add(tablePlayer15, _860px, _D);
				absLayout.add(tablePlayer16, _960px, _D);
				absLayout.add(tablePlayer17, _1060px, _D);

				absLayout.add(tablePlayer18, _860px, _C);
				absLayout.add(tablePlayer19, _960px, _C);
				absLayout.add(tablePlayer20, _1060px, _C);

				absLayout.add(tablePlayer21, _860px, _A);
				absLayout.add(tablePlayer22, _960px, _A);
				absLayout.add(tablePlayer23, _1060px, _A);

				// 5-4-1 5-3-2 4-5-1 4-4-2 4-3-3 3-5-2 3-4-3
                switch (modulo) {
                    case "5-4-1" -> {

                        absLayout.add(tablePlayer2, _350px, _D);
                        absLayout.add(tablePlayer3, _450px, _D);
                        absLayout.add(tablePlayer4, _550px, _D);
                        absLayout.add(tablePlayer5, _650px, _D);
                        absLayout.add(tablePlayer6, _750px, _D);

                        absLayout.add(tablePlayer7, _400px, _C);
                        absLayout.add(tablePlayer8, _500px, _C);
                        absLayout.add(tablePlayer9, _600px, _C);
                        absLayout.add(tablePlayer10, _700px, _C);

                        absLayout.add(tablePlayer11, _550px, _A);
                    }
                    case "5-3-2" -> {

                        absLayout.add(tablePlayer2, _350px, _D);
                        absLayout.add(tablePlayer3, _450px, _D);
                        absLayout.add(tablePlayer4, _550px, _D);
                        absLayout.add(tablePlayer5, _650px, _D);
                        absLayout.add(tablePlayer6, _750px, _D);

                        absLayout.add(tablePlayer7, _450px, _C);
                        absLayout.add(tablePlayer8, _550px, _C);
                        absLayout.add(tablePlayer9, _650px, _C);

                        absLayout.add(tablePlayer10, _500px, _A);
                        absLayout.add(tablePlayer11, _600px, _A);
                    }
                    case "4-5-1" -> {

                        absLayout.add(tablePlayer2, _400px, _D);
                        absLayout.add(tablePlayer3, _500px, _D);
                        absLayout.add(tablePlayer4, _600px, _D);
                        absLayout.add(tablePlayer5, _700px, _D);

                        absLayout.add(tablePlayer6, _350px, _C);
                        absLayout.add(tablePlayer7, _450px, _C);
                        absLayout.add(tablePlayer8, _550px, _C);
                        absLayout.add(tablePlayer9, _650px, _C);
                        absLayout.add(tablePlayer10, _750px, _C);

                        absLayout.add(tablePlayer11, _550px, _A);
                    }
                    case "4-4-2" -> {

                        absLayout.add(tablePlayer2, _400px, _D);
                        absLayout.add(tablePlayer3, _500px, _D);
                        absLayout.add(tablePlayer4, _600px, _D);
                        absLayout.add(tablePlayer5, _700px, _D);

                        absLayout.add(tablePlayer6, _400px, _C);
                        absLayout.add(tablePlayer7, _500px, _C);
                        absLayout.add(tablePlayer8, _600px, _C);
                        absLayout.add(tablePlayer9, _700px, _C);

                        absLayout.add(tablePlayer10, _500px, _A);
                        absLayout.add(tablePlayer11, _600px, _A);
                    }
                    case "4-3-3" -> {

                        absLayout.add(tablePlayer2, _400px, _D);
                        absLayout.add(tablePlayer3, _500px, _D);
                        absLayout.add(tablePlayer4, _600px, _D);
                        absLayout.add(tablePlayer5, _700px, _D);

                        absLayout.add(tablePlayer6, _450px, _C);
                        absLayout.add(tablePlayer7, _550px, _C);
                        absLayout.add(tablePlayer8, _650px, _C);

                        absLayout.add(tablePlayer9, _450px, _A);
                        absLayout.add(tablePlayer10, _550px, _A);
                        absLayout.add(tablePlayer11, _650px, _A);
                    }
                    case "3-5-2" -> {

                        absLayout.add(tablePlayer2, _450px, _D);
                        absLayout.add(tablePlayer3, _550px, _D);
                        absLayout.add(tablePlayer4, _650px, _D);

                        absLayout.add(tablePlayer5, _350px, _C);
                        absLayout.add(tablePlayer6, _450px, _C);
                        absLayout.add(tablePlayer7, _550px, _C);
                        absLayout.add(tablePlayer8, _650px, _C);
                        absLayout.add(tablePlayer9, _750px, _C);

                        absLayout.add(tablePlayer10, _500px, _A);
                        absLayout.add(tablePlayer11, _600px, _A);
                    }
                    case "3-4-3" -> {

                        absLayout.add(tablePlayer2, _450px, _D);
                        absLayout.add(tablePlayer3, _550px, _D);
                        absLayout.add(tablePlayer4, _650px, _D);

                        absLayout.add(tablePlayer5, _400px, _C);
                        absLayout.add(tablePlayer6, _500px, _C);
                        absLayout.add(tablePlayer7, _600px, _C);
                        absLayout.add(tablePlayer8, _700px, _C);

                        absLayout.add(tablePlayer9, _450px, _A);
                        absLayout.add(tablePlayer10, _550px, _A);
                        absLayout.add(tablePlayer11, _650px, _A);
                    }
                }
			}
		});

		tableFormazione = getTableFormazione(modelFormazione);

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
		tablePlayer19 = getTableGiocatore(modelPlayer19);
		tablePlayer20 = getTableGiocatore(modelPlayer20);
		tablePlayer21 = getTableGiocatore(modelPlayer21);
		tablePlayer22 = getTableGiocatore(modelPlayer22);
		tablePlayer23 = getTableGiocatore(modelPlayer23);

		List<FcCalendarioCompetizione> listPartite = calendarioTimController.findByIdGiornataOrderByDataAsc(giornataInfo.getCodiceGiornata());
        Grid<FcCalendarioCompetizione> tablePartite = getTablePartite(listPartite);

		Image panchina = Utils.buildImage("panchina.jpg", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + "panchina.jpg"));

		final VerticalLayout layoutAvviso = new VerticalLayout();
		layoutAvviso.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutAvviso.getStyle().set(Costants.BACKGROUND, Costants.YELLOW);
		layoutAvviso.setWidth("500px");

		HorizontalLayout cssLayout = new HorizontalLayout();
		Span lblInfo = new Span("Prossima Giornata: " + Utils.buildInfoGiornataEm(giornataInfo, campionato));
		cssLayout.add(lblInfo);
		layoutAvviso.add(cssLayout);

		HorizontalLayout cssLayout2 = new HorizontalLayout();
		Span lblInfo2 = new Span("Consegna Formazione entro: " + nextDate);
		cssLayout2.add(lblInfo2);
		layoutAvviso.add(cssLayout2);

		int top = 5;
		absLayout.add(save, 20, top);
		absLayout.add(checkMail, 110, top + 5);
		absLayout.add(layoutAvviso, _350px, top);
		absLayout.add(panchina, _860px, top);
		absLayout.add(tablePartite, _1200px, top);

		absLayout.add(comboModulo, 20, 50);

		absLayout.add(tableFormazione, 10, 150);
		Image campo = Utils.buildImage("campo.jpg", resourceLoader.getResource("Costants.CLASSPATH_IMAGES/campo.jpg"));
		absLayout.add(campo, _350px, 150);

		this.add(absLayout);

		try {
			loadFcGiornatadett(attore, giornataInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
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
		setEnabled(false);
		CustomMessageDialog.showMessageError("Impossibile inserire la formazione, tempo scaduto!");
	}

	private String getInfoPlayer(FcGiocatore bean) {
		String info = "N.D.";
		if (bean != null && bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
			NumberFormat formatter = new DecimalFormat("#0.00");
			String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / Costants.DIVISORE_10);
			String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / Costants.DIVISORE_10);

			info = bean.getCognGiocatore() + "\n";
			info += "Squadra: " + bean.getFcSquadra().getNomeSquadra() + "\n";
			info += "Giocate: " + bean.getFcStatistiche().getGiocate() + "\n";
			info += "MV: " + mv + "\n";
			info += "FV: " + fv + "\n";
			info += "Goal: " + bean.getFcStatistiche().getGoalFatto() + "\n";
			info += "Assist: " + bean.getFcStatistiche().getAssist() + "\n";
			info += "Ammonizione: " + bean.getFcStatistiche().getAmmonizione() + "\n";
			info += "Espulsione: " + bean.getFcStatistiche().getEspulsione() + "\n";
			if ("P".equalsIgnoreCase(bean.getFcRuolo().getIdRuolo())) {
				info += "Goal Subito: " + bean.getFcStatistiche().getGoalSubito() + "\n";
			}
		}

		return info;
	}

	private void refreshAndSortGridFormazione() {
		LOG.info("refreshAndSortGridFormazione");
		modelFormazione.sort((p1,
				p2) -> p2.getFcRuolo().getIdRuolo().compareToIgnoreCase(p1.getFcRuolo().getIdRuolo()));
		tableFormazione.getDataProvider().refreshAll();
	}

	private void removeAllElementsList() {

		LOG.info("removeAllElementsList");
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
		if (!modelPlayer19.isEmpty()) {
			FcGiocatore bean = modelPlayer19.get(0);
			modelFormazione.add(bean);
			modelPlayer19.clear();
			tablePlayer19.getDataProvider().refreshAll();
		}
		if (!modelPlayer20.isEmpty()) {
			FcGiocatore bean = modelPlayer20.get(0);
			modelFormazione.add(bean);
			modelPlayer20.clear();
			tablePlayer20.getDataProvider().refreshAll();
		}
		if (!modelPlayer21.isEmpty()) {
			FcGiocatore bean = modelPlayer21.get(0);
			modelFormazione.add(bean);
			modelPlayer21.clear();
			tablePlayer21.getDataProvider().refreshAll();
		}
		if (!modelPlayer22.isEmpty()) {
			FcGiocatore bean = modelPlayer22.get(0);
			modelFormazione.add(bean);
			modelPlayer22.clear();
			tablePlayer22.getDataProvider().refreshAll();
		}
		if (!modelPlayer23.isEmpty()) {
			FcGiocatore bean = modelPlayer23.get(0);
			modelFormazione.add(bean);
			modelPlayer23.clear();
			tablePlayer23.getDataProvider().refreshAll();
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
		absLayout.remove(tablePlayer19);
		absLayout.remove(tablePlayer20);
		absLayout.remove(tablePlayer21);
		absLayout.remove(tablePlayer22);
		absLayout.remove(tablePlayer23);
	}

	private ArrayList<FcGiocatore> getModelFormazione(FcAttore attore,
			FcCampionato campionato) {

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
		// grid.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		grid.setWidth(width);
		grid.setHeight(height);

		Column<FcGiocatore> giocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			VerticalLayout cellLayout = new VerticalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setSizeUndefined();
			cellLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			if (g != null) {

				String title = getInfoPlayer(g);

				String ruolo = g.getFcRuolo().getIdRuolo();
				if ("P".equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_P);
				} else if ("D".equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_D);
				} else if ("C".equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_C);
				} else if ("A".equals(ruolo)) {
					cellLayout.getElement().getStyle().set(Costants.BORDER, Costants.BORDER_COLOR_2_A);
				}

				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}

				Span lblOrdinamento = new Span();
				lblOrdinamento.setText("" + getOrdinamento(g));
				lblOrdinamento.getStyle().set(Costants.FONT_SIZE, "14px");
				lblOrdinamento.setTitle(title);
				cellLayout.add(lblOrdinamento);
				cellLayout.setAlignSelf(Alignment.CENTER, lblOrdinamento);

				Image imgR = Utils.buildImage(ruolo.toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + ruolo.toLowerCase() + ".png"));
				imgR.setTitle(title);
				cellLayout.add(imgR);

				// Label lblGiocatore = new Label(g.getCognGiocatore());
				Span lblGiocatore = new Span();
				lblGiocatore.setText(g.getCognGiocatore());
				lblGiocatore.getStyle().set(Costants.FONT_SIZE, "11px");
				lblGiocatore.setTitle(title);
				cellLayout.add(lblGiocatore);
				cellLayout.setAlignSelf(Alignment.STRETCH, lblGiocatore);

				if (g.getFcSquadra() != null) {

					FcSquadra sq = g.getFcSquadra();
					if (sq.getImg40() != null) {
						try {
							Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg40().getBinaryStream());
							cellLayout.add(img);
							cellLayout.setAlignSelf(Alignment.START, img);
						} catch (SQLException e) {
							LOG.error(e.getMessage());
						}
					}
                    Span lblInfoNomeSquadra = new Span();
					lblInfoNomeSquadra.setText(sq.getNomeSquadra());
					lblInfoNomeSquadra.getStyle().set(Costants.FONT_SIZE, "11px");
					lblInfoNomeSquadra.setTitle(title);
					cellLayout.add(lblInfoNomeSquadra);
					cellLayout.setAlignSelf(Alignment.STRETCH, lblInfoNomeSquadra);
				}

				Element element = cellLayout.getElement(); // DOM element
				element.addEventListener("click", e -> {

                    LOG.info("click {}", g.getCognGiocatore());
					modelFormazione.add(g);
					refreshAndSortGridFormazione();

					if (grid == tablePlayer1) {
						modelPlayer1.remove(g);
						tablePlayer1.getDataProvider().refreshAll();
					} else if (grid == tablePlayer2) {
						modelPlayer2.remove(g);
						tablePlayer2.getDataProvider().refreshAll();
					} else if (grid == tablePlayer3) {
						modelPlayer3.remove(g);
						tablePlayer3.getDataProvider().refreshAll();
					} else if (grid == tablePlayer4) {
						modelPlayer4.remove(g);
						tablePlayer4.getDataProvider().refreshAll();
					} else if (grid == tablePlayer5) {
						modelPlayer5.remove(g);
						tablePlayer5.getDataProvider().refreshAll();
					} else if (grid == tablePlayer6) {
						modelPlayer6.remove(g);
						tablePlayer6.getDataProvider().refreshAll();
					} else if (grid == tablePlayer7) {
						modelPlayer7.remove(g);
						tablePlayer7.getDataProvider().refreshAll();
					} else if (grid == tablePlayer8) {
						modelPlayer8.remove(g);
						tablePlayer8.getDataProvider().refreshAll();
					} else if (grid == tablePlayer9) {
						modelPlayer9.remove(g);
						tablePlayer9.getDataProvider().refreshAll();
					} else if (grid == tablePlayer10) {
						modelPlayer10.remove(g);
						tablePlayer10.getDataProvider().refreshAll();
					} else if (grid == tablePlayer11) {
						modelPlayer11.remove(g);
						tablePlayer11.getDataProvider().refreshAll();
					} else if (grid == tablePlayer12) {
						modelPlayer12.remove(g);
						tablePlayer12.getDataProvider().refreshAll();
					} else if (grid == tablePlayer13) {
						modelPlayer13.remove(g);
						tablePlayer13.getDataProvider().refreshAll();
					} else if (grid == tablePlayer14) {
						modelPlayer14.remove(g);
						tablePlayer14.getDataProvider().refreshAll();
					} else if (grid == tablePlayer15) {
						modelPlayer15.remove(g);
						tablePlayer15.getDataProvider().refreshAll();
					} else if (grid == tablePlayer16) {
						modelPlayer16.remove(g);
						tablePlayer16.getDataProvider().refreshAll();
					} else if (grid == tablePlayer17) {
						modelPlayer17.remove(g);
						tablePlayer17.getDataProvider().refreshAll();
					} else if (grid == tablePlayer18) {
						modelPlayer18.remove(g);
						tablePlayer18.getDataProvider().refreshAll();
					} else if (grid == tablePlayer19) {
						modelPlayer19.remove(g);
						tablePlayer19.getDataProvider().refreshAll();
					} else if (grid == tablePlayer20) {
						modelPlayer20.remove(g);
						tablePlayer20.getDataProvider().refreshAll();
					} else if (grid == tablePlayer21) {
						modelPlayer21.remove(g);
						tablePlayer21.getDataProvider().refreshAll();
					} else if (grid == tablePlayer22) {
						modelPlayer22.remove(g);
						tablePlayer22.getDataProvider().refreshAll();
					} else if (grid == tablePlayer23) {
						modelPlayer23.remove(g);
						tablePlayer23.getDataProvider().refreshAll();
					}

				});
			}
			return cellLayout;

		}));
		giocatoreColumn.setSortable(false);
		giocatoreColumn.setResizable(false);

		return grid;

	}

	private Grid<FcGiocatore> getTableFormazione(List<FcGiocatore> items) {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth("330px");

		Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (g != null) {
				String title = getInfoPlayer(g);
				if (g.getFcRuolo() != null) {
					Image img = Utils.buildImage(g.getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
					img.setTitle(title);
					cellLayout.add(img);
				}
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("R");
		ruoloColumn.setWidth("35px");
		ruoloColumn.setComparator(Comparator.comparing(p -> p.getFcRuolo().getIdRuolo()));

        Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				if (g.getCognGiocatore() != null) {
					// Label lblGiocatore = new Label(g.getCognGiocatore());
					Span lblGiocatore = new Span();
					lblGiocatore.setText(g.getCognGiocatore());
					lblGiocatore.setTitle(title);
					cellLayout.add(lblGiocatore);
				}
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
		cognGiocatoreColumn.setWidth("145px");
		// cognGiocatoreColumn.setAutoWidth(true);

		Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
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
							LOG.error(e.getMessage());
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
		nomeSquadraColumn.setComparator(Comparator.comparing(p -> p.getFcSquadra().getNomeSquadra()));
		nomeSquadraColumn.setHeader("Naz");
		nomeSquadraColumn.setWidth("70px");
		// nomeSquadraColumn.setAutoWidth(true);

		Column<FcGiocatore> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			if (g != null) {
				String title = getInfoPlayer(g);
				if (!g.isFlagAttivo()) {
					cellLayout.getElement().getStyle().set(Costants.BACKGROUND, Costants.LOWER_GRAY);
					cellLayout.getElement().getStyle().set("-webkit-text-fill-color", Costants.RED);
				}
				FcStatistiche s = g.getFcStatistiche();
				String imgThink = "2.png";
				if (s != null && s.getMediaVoto() != 0) {
					if (s.getMediaVoto() > Costants.EM_RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getMediaVoto() < Costants.EM_RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}

				Image img = Utils.buildImage(imgThink, resourceLoader.getResource(Costants.CLASSPATH_IMAGES + imgThink));
				img.setTitle(title);
				cellLayout.add(img);

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = (double) 0;
				if (s != null) {
					d = s.getMediaVoto() / Costants.DIVISORE_10;
				}
				String sTotPunti = myFormatter.format(d);
				// Label lbl = new Label(sTotPunti);
				Span lbl = new Span();
				lbl.setText(sTotPunti);
				lbl.setTitle(title);
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator(Comparator.comparing(p -> p.getFcStatistiche().getMediaVoto()));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setWidth("70px");
		// mediaVotoColumn.setAutoWidth(true);

		grid.addItemClickListener(event -> {
			String valModulo = comboModulo.getValue();
			if (valModulo == null) {
				return;
			}
			FcGiocatore bean = event.getItem();
            LOG.info("click {}", bean.getCognGiocatore());

            if (existGiocatore(bean)) {
                LOG.info("existGiocatore true");
                return;
            }

            boolean bDel = false;
            switch (bean.getFcRuolo().getIdRuolo()) {
                case "P" -> {
                    if (modelPlayer1.isEmpty()) {
                        modelPlayer1.add(bean);
                        tablePlayer1.getDataProvider().refreshAll();
                        bDel = true;
                    }
                }
                case "D" -> {

                    switch (valModulo) {
                        case "5-4-1", "5-3-2" -> {

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
                            }
                        }
                        case "4-5-1", "4-4-2", "4-3-3" -> {

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
                            }
                        }
                        case "3-5-2", "3-4-3" -> {

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
                            }
                        }
                    }
                }
                case "C" -> {

                    switch (valModulo) {
                        case "4-5-1" -> {

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
                            }
                        }
                        case "3-5-2" -> {

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
                            }
                        }
                        case "5-4-1" -> {

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
                            }
                        }
                        case "4-4-2" -> {

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
                            }
                        }
                        case "3-4-3" -> {

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
                            }
                        }
                        case "5-3-2" -> {

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
                            }
                        }
                        case "4-3-3" -> {
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
                            }
                        }
                    }
                }
                case "A" -> {

                    switch (valModulo) {
                        case "4-5-1", "5-4-1" -> {

                            if (modelPlayer11.isEmpty()) {
                                modelPlayer11.add(bean);
                                tablePlayer11.getDataProvider().refreshAll();
                                bDel = true;
                            }
                        }
                        case "3-5-2", "4-4-2", "5-3-2" -> {

                            if (modelPlayer10.isEmpty()) {
                                modelPlayer10.add(bean);
                                tablePlayer10.getDataProvider().refreshAll();
                                bDel = true;
                            } else if (modelPlayer11.isEmpty()) {
                                modelPlayer11.add(bean);
                                tablePlayer11.getDataProvider().refreshAll();
                                bDel = true;
                            }
                        }
                        case "3-4-3", "4-3-3" -> {

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
                            }
                        }
                    }
                }
            }

            if (!bDel) {

                if (modelPlayer12.isEmpty()) {
                    modelPlayer12.add(bean);
                    tablePlayer12.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer13.isEmpty()) {
                    modelPlayer13.add(bean);
                    tablePlayer13.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer14.isEmpty()) {
                    modelPlayer14.add(bean);
                    tablePlayer14.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer15.isEmpty()) {
                    modelPlayer15.add(bean);
                    tablePlayer15.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer16.isEmpty()) {
                    modelPlayer16.add(bean);
                    tablePlayer16.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer17.isEmpty()) {
                    modelPlayer17.add(bean);
                    tablePlayer17.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer18.isEmpty()) {
                    modelPlayer18.add(bean);
                    tablePlayer18.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer19.isEmpty()) {
                    modelPlayer19.add(bean);
                    tablePlayer19.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer20.isEmpty()) {
                    modelPlayer20.add(bean);
                    tablePlayer20.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer21.isEmpty()) {
                    modelPlayer21.add(bean);
                    tablePlayer21.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer22.isEmpty()) {
                    modelPlayer22.add(bean);
                    tablePlayer22.getDataProvider().refreshAll();
                    bDel = true;
                } else if (modelPlayer23.isEmpty()) {
                    modelPlayer23.add(bean);
                    tablePlayer23.getDataProvider().refreshAll();
                    bDel = true;
                }
            }

            if (bDel) {
                modelFormazione.remove(bean);
                refreshAndSortGridFormazione();
            }

        });

		return grid;
	}

	private boolean existGiocatore(FcGiocatore g) {

		if (!modelPlayer1.isEmpty()) {
			if (modelPlayer1.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer2.isEmpty()) {
			if (modelPlayer2.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer3.isEmpty()) {
			if (modelPlayer3.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer4.isEmpty()) {
			if (modelPlayer4.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer5.isEmpty()) {
			if (modelPlayer5.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer6.isEmpty()) {
			if (modelPlayer6.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer7.isEmpty()) {
			if (modelPlayer7.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer8.isEmpty()) {
			if (modelPlayer8.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer9.isEmpty()) {
			if (modelPlayer9.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer10.isEmpty()) {
			if (modelPlayer10.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer11.isEmpty()) {
			if (modelPlayer11.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer12.isEmpty()) {
			if (modelPlayer12.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer13.isEmpty()) {
			if (modelPlayer13.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer14.isEmpty()) {
			if (modelPlayer14.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer15.isEmpty()) {
			if (modelPlayer15.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer16.isEmpty()) {
			if (modelPlayer16.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer17.isEmpty()) {
			if (modelPlayer17.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer18.isEmpty()) {
			if (modelPlayer18.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer19.isEmpty()) {
			if (modelPlayer19.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer20.isEmpty()) {
			if (modelPlayer20.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer21.isEmpty()) {
			if (modelPlayer21.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer22.isEmpty()) {
			if (modelPlayer22.contains(g)) {
				return true;
			}
		}
		if (!modelPlayer23.isEmpty()) {
            return modelPlayer23.contains(g);
		}

		return false;
	}

	private int getOrdinamento(FcGiocatore g) {

		if (!modelPlayer1.isEmpty()) {
			if (modelPlayer1.contains(g)) {
				return 1;
			}
		}
		if (!modelPlayer2.isEmpty()) {
			if (modelPlayer2.contains(g)) {
				return 2;
			}
		}
		if (!modelPlayer3.isEmpty()) {
			if (modelPlayer3.contains(g)) {
				return 3;
			}
		}
		if (!modelPlayer4.isEmpty()) {
			if (modelPlayer4.contains(g)) {
				return 4;
			}
		}
		if (!modelPlayer5.isEmpty()) {
			if (modelPlayer5.contains(g)) {
				return 5;
			}
		}
		if (!modelPlayer6.isEmpty()) {
			if (modelPlayer6.contains(g)) {
				return 6;
			}
		}
		if (!modelPlayer7.isEmpty()) {
			if (modelPlayer7.contains(g)) {
				return 7;
			}
		}
		if (!modelPlayer8.isEmpty()) {
			if (modelPlayer8.contains(g)) {
				return 8;
			}
		}
		if (!modelPlayer9.isEmpty()) {
			if (modelPlayer9.contains(g)) {
				return 9;
			}
		}
		if (modelPlayer10.size() != 0) {
			if (modelPlayer10.contains(g)) {
				return 10;
			}
		}
		if (modelPlayer11.size() != 0) {
			if (modelPlayer11.contains(g)) {
				return 11;
			}
		}
		if (modelPlayer12.size() != 0) {
			if (modelPlayer12.contains(g)) {
				return 12;
			}
		}
		if (modelPlayer13.size() != 0) {
			if (modelPlayer13.contains(g)) {
				return 13;
			}
		}
		if (modelPlayer14.size() != 0) {
			if (modelPlayer14.contains(g)) {
				return 14;
			}
		}
		if (modelPlayer15.size() != 0) {
			if (modelPlayer15.contains(g)) {
				return 15;
			}
		}
		if (modelPlayer16.size() != 0) {
			if (modelPlayer16.contains(g)) {
				return 16;
			}
		}
		if (modelPlayer17.size() != 0) {
			if (modelPlayer17.contains(g)) {
				return 17;
			}
		}
		if (modelPlayer18.size() != 0) {
			if (modelPlayer18.contains(g)) {
				return 18;
			}
		}
		if (modelPlayer19.size() != 0) {
			if (modelPlayer19.contains(g)) {
				return 19;
			}
		}
		if (modelPlayer20.size() != 0) {
			if (modelPlayer20.contains(g)) {
				return 20;
			}
		}
		if (modelPlayer21.size() != 0) {
			if (modelPlayer21.contains(g)) {
				return 21;
			}
		}
		if (modelPlayer22.size() != 0) {
			if (modelPlayer22.contains(g)) {
				return 22;
			}
		}
		if (modelPlayer23.size() != 0) {
			if (modelPlayer23.contains(g)) {
				return 23;
			}
		}

		return 0;
	}

	private void loadFcGiornatadett(FcAttore attore,
			FcGiornataInfo giornataInfo) {

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
                switch (gd.getFcGiocatore().getFcRuolo().getIdRuolo()) {
                    case "D" -> countD++;
                    case "C" -> countC++;
                    case "A" -> countA++;
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
			} else if (gd.getOrdinamento() == 19) {
				modelPlayer19.clear();
				modelPlayer19.add(bean);
				tablePlayer19.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 20) {
				modelPlayer20.clear();
				modelPlayer20.add(bean);
				tablePlayer20.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 21) {
				modelPlayer21.clear();
				modelPlayer21.add(bean);
				tablePlayer21.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 22) {
				modelPlayer22.clear();
				modelPlayer22.add(bean);
				tablePlayer22.getDataProvider().refreshAll();
			} else if (gd.getOrdinamento() == 23) {
				modelPlayer23.clear();
				modelPlayer23.add(bean);
				tablePlayer23.getDataProvider().refreshAll();
			} else {
				modelFormazione.add(bean);
				refreshAndSortGridFormazione();
			}
		}
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		if (check()) {

			try {
				insert(giornataInfo.getCodiceGiornata());
			} catch (Exception exi) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exi.getMessage());
				return;
			}

			if (checkMail.getValue()) {
				try {
					String dataora = getSysdate();

					sendNewMail(giornataInfo.getDescGiornataFc());

					LOG.info("send_mail OK");

					try {
						insert_dett_info(giornataInfo.getCodiceGiornata(), dataora);
						LOG.info("insert_dett_info OK");
					} catch (Exception exd) {
						LOG.error(exd.getMessage());
						CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, exd.getMessage());
					}

					CustomMessageDialog.showMessageInfo("Formazione inserita, email inviata con successo!");

				} catch (Exception excpt) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, excpt.getMessage());
                }

			} else {
				CustomMessageDialog.showMessageInfo("Formazione inserita con successo! \nPer rendere effettiva la formazione abilitare invio email.");
			}
		}
	}

	private String getSysdate() {

		String sql = "select sysdate() from dual";
		return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {

                return rs.getString(1);
            }
            return null;
        });
	}

	private boolean check() {

		if (modelPlayer1.size() == 0 || modelPlayer2.size() == 0 || modelPlayer3.size() == 0 || modelPlayer4.size() == 0 || modelPlayer5.size() == 0 || modelPlayer6.size() == 0 || modelPlayer7.size() == 0 || modelPlayer8.size() == 0 || modelPlayer9.size() == 0 || modelPlayer10.size() == 0 || modelPlayer11.size() == 0 || modelPlayer12.size() == 0 || modelPlayer13.size() == 0 || modelPlayer14.size() == 0 || modelPlayer15.size() == 0 || modelPlayer16.size() == 0 || modelPlayer17.size() == 0 || modelPlayer18.size() == 0 || modelPlayer19.size() == 0 || modelPlayer20.size() == 0 || modelPlayer21.size() == 0 || modelPlayer22.size() == 0 || modelPlayer23.size() == 0) {
			CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_ERROR_INSERT_GIOCATORI);
			return false;
		}
		return true;
	}

	private void insert(int giornata) {

		String query;
		try {
			query = " DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
			jdbcTemplate.update(query);

			String ID_GIORNATA = "" + giornata;
			String ID_GIOCATORE;
			String ID_STATO_GIOCATORE;
			String ORDINAMENTO;
			int ord = 1;
			for (int i = 0; i < 23; i++) {

				ORDINAMENTO = "" + ord;
				ord++;
				if (i == 0) {
					FcGiocatore bean = modelPlayer1.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 1) {
					FcGiocatore bean = modelPlayer2.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 2) {
					FcGiocatore bean = modelPlayer3.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 3) {
					FcGiocatore bean = modelPlayer4.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 4) {
					FcGiocatore bean = modelPlayer5.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 5) {
					FcGiocatore bean = modelPlayer6.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 6) {
					FcGiocatore bean = modelPlayer7.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 7) {
					FcGiocatore bean = modelPlayer8.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 8) {
					FcGiocatore bean = modelPlayer9.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 9) {
					FcGiocatore bean = modelPlayer10.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 10) {
					FcGiocatore bean = modelPlayer11.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "T";
				} else if (i == 11) {
					FcGiocatore bean = modelPlayer12.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 12) {
					FcGiocatore bean = modelPlayer13.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 13) {
					FcGiocatore bean = modelPlayer14.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 14) {
					FcGiocatore bean = modelPlayer15.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 15) {
					FcGiocatore bean = modelPlayer16.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 16) {
					FcGiocatore bean = modelPlayer17.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 17) {
					FcGiocatore bean = modelPlayer18.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 18) {
					FcGiocatore bean = modelPlayer19.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 19) {
					FcGiocatore bean = modelPlayer20.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 20) {
					FcGiocatore bean = modelPlayer21.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else if (i == 21) {
					FcGiocatore bean = modelPlayer22.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				} else {
					FcGiocatore bean = modelPlayer23.get(0);
					ID_GIOCATORE = "" + bean.getIdGiocatore();
					ID_STATO_GIOCATORE = "R";
				}

				query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + ID_GIORNATA + ",";
				query += idAttore + "," + ID_GIOCATORE + ",'" + ID_STATO_GIOCATORE + "'," + ORDINAMENTO + ",0)";

				jdbcTemplate.update(query);
			}

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void insert_dett_info(int giornata, String dataora) {

		String query;
		try {
			query = " DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
			jdbcTemplate.update(query);

			String ID_GIORNATA = "" + giornata;
			query = " INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES (" + ID_GIORNATA + ",";
			query += idAttore + ",1,'" + dataora + "')";

			jdbcTemplate.update(query);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void sendNewMail(String desc_giornata) throws Exception {

		String subject = "Formazione " + descAttore + " - " + desc_giornata;

		String modulo = this.comboModulo.getValue();

		StringBuilder formazioneHtml = new StringBuilder();
		formazioneHtml.append("<html><head><title>FC</title></head>\n");
		formazioneHtml.append("<body>\n");
		formazioneHtml.append("<p>").append(desc_giornata).append("</p>\n");
		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<p>").append(modulo).append("</p>\n");
		formazioneHtml.append("<br>\n");

		formazioneHtml.append("<table>");

		String NOME_GIOCATORE;
		String RUOLO;
		String STATO;
		String ORDINAMENTO;
		String SQUADRA;
		int ord = 1;
		Map<String, InputStream> listImg = new HashMap<>();
		for (int i = 0; i < 23; i++) {

			ORDINAMENTO = "" + ord;
			FcGiocatore bean = getFcGiocatore(i);

			NOME_GIOCATORE = bean.getCognGiocatore();
			RUOLO = bean.getFcRuolo().getDescRuolo();
			SQUADRA = bean.getFcSquadra().getNomeSquadra();

			if (i < 11) {
				STATO = "Titolare";
			} else {
				STATO = "Riserva";
			}
			String color = "BGCOLOR=\"#FF9331\"";
			if (Integer.parseInt(ORDINAMENTO) >= 1 && Integer.parseInt(ORDINAMENTO) <= 11) {
				color = "BGCOLOR=\"#ABFF73\"";
			} else if (Integer.parseInt(ORDINAMENTO) >= 12 && Integer.parseInt(ORDINAMENTO) <= 23) {
				color = "BGCOLOR=\"#FFFF84\"";
			}

			String cidNomeSq = ContentIdGenerator.getContentId();
			FcSquadra sq = bean.getFcSquadra();
			if (sq.getImg() != null) {
				try {
					listImg.put(cidNomeSq, sq.getImg().getBinaryStream());
				} catch (SQLException e) {
					LOG.error(e.getMessage());
				}
			}

			formazioneHtml.append("<tr ").append(color).append(">");

			formazioneHtml.append("<td>");
			formazioneHtml.append(ORDINAMENTO);
			formazioneHtml.append("</td>");

			formazioneHtml.append("<td>");
			formazioneHtml.append(RUOLO);
			formazioneHtml.append("</td>");

			formazioneHtml.append("<td>");
			formazioneHtml.append(NOME_GIOCATORE);
			formazioneHtml.append("</td>");

			formazioneHtml.append("<td><img src=\"cid:").append(cidNomeSq).append("\" />");
			formazioneHtml.append(SQUADRA);
			formazioneHtml.append("</td>");

			formazioneHtml.append("<td>");
			formazioneHtml.append(STATO);
			formazioneHtml.append("</td>");

			formazioneHtml.append("</tr>");

			ord++;
		}

		formazioneHtml.append("</table>\n");

		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<p>Ciao ").append(descAttore).append("</p>\n");
		formazioneHtml.append("</body>\n");
		formazioneHtml.append("<html>");

		StringBuilder email_destinatario = new StringBuilder();
		String ACTIVE_MAIL = p.getProperty("ACTIVE_MAIL");
		if ("true".equals(ACTIVE_MAIL)) {
			List<FcAttore> attori = attoreController.findByActive(true);
			for (FcAttore a : attori) {
				if (a.isNotifiche()) {
					email_destinatario.append(a.getEmail()).append(";");
				}
			}
		} else {
			email_destinatario = new StringBuilder(p.getProperty("to"));
		}

		String[] to = null;
		if (!email_destinatario.toString().isEmpty()) {
			to = Utils.tornaArrayString(email_destinatario.toString(), ";");
		}

        try {
			String from = env.getProperty("spring.mail.secondary.username");
			emailService.sendMail2(false, from, to, null, null, subject, formazioneHtml.toString(), "text/html", listImg);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			try {
				String from = env.getProperty("spring.mail.primary.username");
				emailService.sendMail2(true, from, to, null, null, subject, formazioneHtml.toString(), "text/html", listImg);
			} catch (Exception e2) {
				LOG.error(e2.getMessage());
				throw e2;
			}
		}
	}

	private FcGiocatore getFcGiocatore(int i) {
		FcGiocatore bean;

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
		} else if (i == 18) {
			bean = modelPlayer19.get(0);
		} else if (i == 19) {
			bean = modelPlayer20.get(0);
		} else if (i == 20) {
			bean = modelPlayer21.get(0);
		} else if (i == 21) {
			bean = modelPlayer22.get(0);
		} else {
			bean = modelPlayer23.get(0);
		}
		return bean;
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
				FcSquadra sq = squadraController.findByIdSquadra(s.getIdSquadraCasa());
				if (sq.getImg() != null) {
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
				Span lblSquadra = new Span(s.getSquadraFuori().substring(0, 3));
				FcSquadra sq = squadraController.findByIdSquadra(s.getIdSquadraFuori());
				if (sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						LOG.error(e.getMessage());
					}
				}
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		nomeSquadraFuoriColumn.setSortable(false);
		// nomeSquadraFuoriColumn.setHeader("Fuori");
		nomeSquadraFuoriColumn.setAutoWidth(true);

        Column<FcCalendarioCompetizione> dataColumn = grid.addColumn(new LocalDateTimeRenderer<>(FcCalendarioCompetizione::getData,() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);
		dataColumn.setFlexGrow(2);

		return grid;
	}

}
