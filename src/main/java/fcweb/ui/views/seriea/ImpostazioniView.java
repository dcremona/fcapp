package fcweb.ui.views.seriea;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.mail.MailClient;
import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.job.JobProcessFileCsv;
import fcweb.backend.job.JobProcessGiornata;
import fcweb.backend.job.JobProcessSendMail;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.CalendarioCompetizioneService;
import fcweb.backend.service.ClassificaService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.GiornataService;
import fcweb.backend.service.ProprietaService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

@PageTitle("Impostazioni")
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ImpostazioniView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private List<FcAttore> squadre = null;
	private List<FcSquadra> squadreSerieA = null;
	private List<FcGiornataInfo> giornate = null;

	@Autowired
	private Environment env;

	@Autowired
	private JavaMailSenderImpl javaMailSender;

	@Autowired
	private CalendarioCompetizioneService calendarioTimController;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private JobProcessFileCsv jobProcessFileCsv;

	@Autowired
	private JobProcessGiornata jobProcessGiornata;

	@Autowired
	private JobProcessSendMail jobProcessSendMail;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private SquadraService squadraController;

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private ProprietaService proprietaController;

	private Button initDb;
	private Button generaCalendar;
	private ComboBox<FcGiornataInfo> comboGiornata;

	private ComboBox<FcAttore> comboAttore;
	private Button resetFormazione;
	private Button ultimaFormazione;
	private Button formazione422;

	private Button downloadQuotaz;
	private Button updateGiocatori;
	private Checkbox chkUpdateQuotaz;
	private Checkbox chkUpdateImg;
	private NumberField txtPerc;
	private Grid<FcGiocatore> tableGiocatoreAdd;
	private Grid<FcGiocatore> tableGiocatoreDel;

	private Button init;
	private Button download;
	private Button seiPolitico;
	private ComboBox<FcSquadra> comboSqudreA;
	private Button calcola;
	private ToggleButton chkForzaVotoGiocatore;
	private ToggleButton chkRoundVotoGiocatore;
	private Button calcolaStatistiche;
	private Button pdfAndMail;

	private Button salva;
	private Button resetDate;
	private Checkbox chkUfficiali;
	private Checkbox chkSendMail;

	private Details panelSetup;
	private DateTimePicker da1;
	private DateTimePicker da2;
	private DateTimePicker dg;
	private DateTimePicker dp;

	@Autowired
	private AccessoService accessoController;

	@PostConstruct
	void init() {
		LOG.debug("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initData();
		initLayout();
	}

	private void initData() {
		squadre = attoreController.findByActive(true);
		squadreSerieA = squadraController.findAll();
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		LOG.info("from " + "" + from);
		LOG.info("to " + "" + to);
		giornate = giornataInfoController.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
	}

	private void initLayout() {

		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

		initDb = new Button("Init Db Formazioni/Classifica");
		initDb.setIcon(VaadinIcon.START_COG.create());
		initDb.addClickListener(this);

		generaCalendar = new Button("Genera Calendario");
		generaCalendar.setIcon(VaadinIcon.CALENDAR.create());
		generaCalendar.addClickListener(this);

		comboGiornata = new ComboBox<>();
		comboGiornata.setItemLabelGenerator(g -> Utils.buildInfoGiornata(g));
		comboGiornata.setItems(giornate);
		comboGiornata.setClearButtonVisible(true);
		comboGiornata.setPlaceholder("Seleziona la giornata");
		comboGiornata.addValueChangeListener(event -> {
			FcGiornataInfo fcGiornataInfo2 = null;
			if (event.getSource().isEmpty()) {
				LOG.info("event.getSource().isEmpty()");
			} else if (event.getOldValue() == null) {
				LOG.info("event.getOldValue()");
				fcGiornataInfo2 = event.getValue();
			} else {
				fcGiornataInfo2 = event.getValue();
			}
			if (fcGiornataInfo2 != null && da1 != null && da2 != null && dg != null && dp != null) {
				LOG.info("gioranta " + "" + fcGiornataInfo2.getCodiceGiornata());
				if (fcGiornataInfo2.getDataAnticipo1() != null) {
					da1.setValue(fcGiornataInfo2.getDataAnticipo1());
				}

				if (fcGiornataInfo2.getDataAnticipo2() != null) {
					da2.setValue(fcGiornataInfo2.getDataAnticipo2());
				}

				if (fcGiornataInfo2.getDataGiornata() != null) {
					dg.setValue(fcGiornataInfo2.getDataGiornata());
				}
				if (fcGiornataInfo2.getDataPosticipo() != null) {
					dp.setValue(fcGiornataInfo2.getDataPosticipo());
				}
				panelSetup.setOpened(false);
				initDb.setEnabled(false);
				generaCalendar.setEnabled(false);
				LOG.info("getCodiceGiornata " + "" + fcGiornataInfo2.getCodiceGiornata());
				if (fcGiornataInfo2.getCodiceGiornata() == 1 || fcGiornataInfo2.getCodiceGiornata() == 20) {
					panelSetup.setOpened(true);
					initDb.setEnabled(true);
					generaCalendar.setEnabled(true);
				}
			}
		});
		comboGiornata.setValue(giornataInfo);
		comboGiornata.setWidthFull();

		this.add(comboGiornata);

		HorizontalLayout layoutSetup = new HorizontalLayout();
		layoutSetup.setMargin(true);
		layoutSetup.getStyle().set("border", Costants.BORDER_COLOR);
		layoutSetup.add(initDb);
		layoutSetup.add(generaCalendar);

		panelSetup = new Details("Setup",layoutSetup);
		panelSetup.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

		this.add(panelSetup);

		panelSetup.setOpened(false);
		initDb.setEnabled(false);
		generaCalendar.setEnabled(false);
		LOG.info("getCodiceGiornata " + "" + giornataInfo.getCodiceGiornata());
		if (giornataInfo.getCodiceGiornata() == 1 || giornataInfo.getCodiceGiornata() == 20) {
			panelSetup.setOpened(true);
			initDb.setEnabled(true);
			generaCalendar.setEnabled(true);
		}

		comboAttore = new ComboBox<>();
		comboAttore.setItems(squadre);
		comboAttore.setItemLabelGenerator(p -> p.getDescAttore());
		comboAttore.setClearButtonVisible(true);
		comboAttore.setPlaceholder("Seleziona attore");

		resetFormazione = new Button("Reset Formazione");
		resetFormazione.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		resetFormazione.addClickListener(this);

		ultimaFormazione = new Button("Inserisci Ultima Formazione");
		ultimaFormazione.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		ultimaFormazione.addClickListener(this);

		formazione422 = new Button("Formazione 422");
		formazione422.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		formazione422.addClickListener(this);
		formazione422.setEnabled(false);

		HorizontalLayout layoutUpdateRow1 = new HorizontalLayout();
		layoutUpdateRow1.setMargin(true);

		layoutUpdateRow1.add(comboAttore);
		layoutUpdateRow1.add(resetFormazione);
		layoutUpdateRow1.add(ultimaFormazione);
		layoutUpdateRow1.add(formazione422);

		downloadQuotaz = new Button("Download Quotazioni");
		downloadQuotaz.setIcon(VaadinIcon.DOWNLOAD.create());
		downloadQuotaz.addClickListener(this);

		updateGiocatori = new Button("Update Giocatori");
		updateGiocatori.setIcon(VaadinIcon.PIN.create());
		updateGiocatori.addClickListener(this);

		txtPerc = new NumberField();
		txtPerc.setMin(0d);
		txtPerc.setMax(100d);
		txtPerc.setStepButtonsVisible(true);
		txtPerc.setValue(50d);

		chkUpdateQuotaz = new Checkbox("Update Quotazioni");
		chkUpdateImg = new Checkbox("Update Img");

		HorizontalLayout layoutUpdateRow2 = new HorizontalLayout();
		layoutUpdateRow2.setMargin(true);

		layoutUpdateRow2.add(downloadQuotaz);
		layoutUpdateRow2.add(updateGiocatori);
		layoutUpdateRow2.add(txtPerc);
		layoutUpdateRow2.add(chkUpdateQuotaz);
		layoutUpdateRow2.add(chkUpdateImg);

		HorizontalLayout layoutUpdateRow3 = new HorizontalLayout();
		layoutUpdateRow3.setMargin(true);

		tableGiocatoreAdd = getTableGiocatori();
		layoutUpdateRow3.add(tableGiocatoreAdd);

		HorizontalLayout layoutUpdateRow4 = new HorizontalLayout();
		layoutUpdateRow4.setMargin(true);

		tableGiocatoreDel = getTableGiocatori();
		layoutUpdateRow4.add(tableGiocatoreDel);

		VerticalLayout layoutUpdate = new VerticalLayout();
		layoutUpdate.setMargin(true);
		layoutUpdate.getStyle().set("border", Costants.BORDER_COLOR);

		layoutUpdate.add(layoutUpdateRow1);
		layoutUpdate.add(layoutUpdateRow2);
		layoutUpdate.add(layoutUpdateRow3);
		layoutUpdate.add(layoutUpdateRow4);

		Details panelUpdate = new Details("Update",layoutUpdate);
		panelUpdate.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelUpdate.setOpened(true);
		this.add(panelUpdate);

		init = new Button("Avvia");
		init.setIcon(VaadinIcon.ADD_DOCK.create());
		init.addClickListener(this);

		download = new Button("Download Voti");
		download.setIcon(VaadinIcon.DOWNLOAD.create());
		download.addClickListener(this);

		chkUfficiali = new Checkbox("Ufficiali");

		seiPolitico = new Button("Sei Politico");
		seiPolitico.setIcon(VaadinIcon.PIN.create());
		seiPolitico.addClickListener(this);

		comboSqudreA = new ComboBox<>();
		comboSqudreA.setItems(squadreSerieA);
		comboSqudreA.setItemLabelGenerator(p -> p.getNomeSquadra());
		comboSqudreA.setClearButtonVisible(true);
		comboSqudreA.setPlaceholder("Squadra");
		comboSqudreA.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			// Image img = buildImage("classpath:/img/squadre/",
			// item.getNomeSquadra() + ".png");
			// container.add(img);
			if (item != null && item.getImg() != null) {
				try {
					Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
					container.add(img);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			Span lblSquadra = new Span(item.getNomeSquadra());
			container.add(lblSquadra);
			return container;
		}));

		calcola = new Button("Calcola");
		calcola.setIcon(VaadinIcon.PIN.create());
		calcola.addClickListener(this);

		chkForzaVotoGiocatore = new ToggleButton();
		chkForzaVotoGiocatore.setLabel("Forza Voto 0");
		chkForzaVotoGiocatore.setValue(false);

		chkRoundVotoGiocatore = new ToggleButton();
		chkRoundVotoGiocatore.setLabel("Round Voto");
		chkRoundVotoGiocatore.setValue(true);

		calcolaStatistiche = new Button("Calcola Statistiche");
		calcolaStatistiche.setIcon(VaadinIcon.PRESENTATION.create());
		calcolaStatistiche.addClickListener(this);

		pdfAndMail = new Button("Crea Pdf - Invia email");
		pdfAndMail.setIcon(VaadinIcon.MAILBOX.create());
		pdfAndMail.addClickListener(this);

		chkSendMail = new Checkbox("Invia Email a tutti");

		VerticalLayout layoutCalcola = new VerticalLayout();
		layoutCalcola.setMargin(true);
		layoutCalcola.getStyle().set("border", Costants.BORDER_COLOR);

		HorizontalLayout vHor = new HorizontalLayout();
		vHor.add(download);
		vHor.add(chkUfficiali);
		vHor.add(seiPolitico);
		vHor.add(comboSqudreA);

		HorizontalLayout vHor2 = new HorizontalLayout();
		vHor2.add(calcola);
		vHor2.add(chkForzaVotoGiocatore);
		vHor2.add(chkRoundVotoGiocatore);
		vHor2.add(calcolaStatistiche);

		layoutCalcola.add(init);
		layoutCalcola.add(vHor);
		layoutCalcola.add(vHor2);
		layoutCalcola.add(pdfAndMail);
		layoutCalcola.add(chkSendMail);

		Details panelCalcola = new Details("Calcola",layoutCalcola);
		panelCalcola.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelCalcola.setOpened(true);
		this.add(panelCalcola);

		da1 = new DateTimePicker("Data Anticipo1");
		if (giornataInfo.getDataAnticipo1() != null) {
			da1.setValue(giornataInfo.getDataAnticipo1());
		}

		da2 = new DateTimePicker("Data Anticipo2");
		if (giornataInfo.getDataAnticipo2() != null) {
			da2.setValue(giornataInfo.getDataAnticipo2());
		}

		dg = new DateTimePicker("Data Giornata");
		if (giornataInfo.getDataGiornata() != null) {
			// dg.setValue(DateConvertUtils.asLocalDateTime(giornataInfo.getDataGiornata()));
			dg.setValue(giornataInfo.getDataGiornata());
		}

		dp = new DateTimePicker("Data Posticipo");
		if (giornataInfo.getDataPosticipo() != null) {
			// dp.setValue(DateConvertUtils.asLocalDateTime(giornataInfo.getDataPosticipo()));
			dp.setValue(giornataInfo.getDataPosticipo());
		}

		salva = new Button("Salva");
		salva.setIcon(VaadinIcon.DATABASE.create());
		salva.addClickListener(this);

		resetDate = new Button("Reset");
		resetDate.setIcon(VaadinIcon.REFRESH.create());
		resetDate.addClickListener(this);

		HorizontalLayout layoutRow1 = new HorizontalLayout();
		layoutRow1.add(salva);
		layoutRow1.add(resetDate);

		HorizontalLayout layoutRow2 = new HorizontalLayout();
		layoutRow2.add(da1);
		layoutRow2.add(da2);

		HorizontalLayout layoutRow22 = new HorizontalLayout();
		layoutRow22.add(dg);
		layoutRow22.add(dp);

		VerticalLayout pnlUfficiali = new VerticalLayout();
		pnlUfficiali.setSizeUndefined();
		pnlUfficiali.add(getCheck("1_UFFICIALI", "DOM_Ufficiali"));
		pnlUfficiali.add(getCheck("2_UFFICIALI", "LUN_Ufficiali"));
		pnlUfficiali.add(getCheck("3_UFFICIALI", "MAR_Ufficiali"));
		pnlUfficiali.add(getCheck("4_UFFICIALI", "MER_Ufficiali"));
		pnlUfficiali.add(getCheck("5_UFFICIALI", "GIO_Ufficiali"));
		pnlUfficiali.add(getCheck("6_UFFICIALI", "VEN_Ufficiali"));
		pnlUfficiali.add(getCheck("7_UFFICIALI", "SAB_Ufficiali"));

		VerticalLayout pnlUfficiosi = new VerticalLayout();
		pnlUfficiosi.setSizeUndefined();
		pnlUfficiosi.add(getCheck("1_UFFICIOSI", "DOM_Ufficiosi"));
		pnlUfficiosi.add(getCheck("2_UFFICIOSI", "LUN_Ufficiosi"));
		pnlUfficiosi.add(getCheck("3_UFFICIOSI", "MAR_Ufficiosi"));
		pnlUfficiosi.add(getCheck("4_UFFICIOSI", "MER_Ufficiosi"));
		pnlUfficiosi.add(getCheck("5_UFFICIOSI", "GIO_Ufficiosi"));
		pnlUfficiosi.add(getCheck("6_UFFICIOSI", "VEN_Ufficiosi"));
		pnlUfficiosi.add(getCheck("7_UFFICIOSI", "SAB_Ufficiosi"));

		HorizontalLayout layoutRow3 = new HorizontalLayout();
		layoutRow3.add(pnlUfficiali);
		layoutRow3.add(pnlUfficiosi);

		VerticalLayout layoutDate = new VerticalLayout();
		layoutDate.setMargin(true);
		layoutDate.getStyle().set("border", Costants.BORDER_COLOR);

		layoutDate.add(layoutRow1);
		layoutDate.add(layoutRow2);
		layoutDate.add(layoutRow22);
		layoutDate.add(layoutRow3);

		Details panelGiorn = new Details("Imposta Date",layoutDate);
		panelGiorn.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelGiorn.setOpened(true);
		this.add(panelGiorn);
	}

	private Checkbox getCheck(String key, String label) {

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");

		Checkbox check = new Checkbox(label);
		check.setValue("1".equals((String) p.getProperty(key)) ? true : false);

		check.addValueChangeListener(event -> {
			try {
				Boolean value = (Boolean) event.getValue();
				FcProperties proprieta = new FcProperties();
				proprieta.setKey(key);
				proprieta.setValue(value == true ? "1" : "0");
				proprietaController.updateProprieta(proprieta);
				p.setProperty(key, value == true ? "1" : "0");
				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
			} catch (Exception e) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
			}
		});

		return check;
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
			FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

			FcGiornataInfo giornataInfo = null;
			int giornata = 0;
			if (!comboGiornata.isEmpty()) {
				giornataInfo = (FcGiornataInfo) comboGiornata.getValue();
				giornata = giornataInfo.getCodiceGiornata();
			}
			LOG.info("giornata " + giornata);

			FcAttore attore = (FcAttore) comboAttore.getValue();
			LOG.info("giornata " + giornata);

			String basePathData = (String) p.get("PATH_TMP");
			LOG.info("basePathData " + basePathData);
			File f = new File(basePathData);
			if (!f.exists()) {
				CustomMessageDialog.showMessageError("Impossibile trovare il percorso specificato " + basePathData);
				return;
			}

			if (event.getSource() == initDb) {

				List<FcAttore> attori = attoreController.findByActive(true);
				for (FcAttore a : attori) {
					if (a.isActive()) {
						for (int j = 1; j <= 26; j++) {
							formazioneController.createFormazione(a, campionato.getIdCampionato(), Integer.valueOf(j));
						}
						classificaController.create(a, campionato, Double.valueOf(0));
					}
				}

			} else if (event.getSource() == downloadQuotaz) {

				// **************************************
				// DOWNLOAD FILE QUOTAZIONI
				// **************************************

				String urlFanta = (String) p.get("URL_FANTA");
				String basePath = basePathData;
				String quotaz = "Giocatori-Quotazioni-Excel";
				String httpUrl = urlFanta + quotaz + ".asp?giornata=" + giornata;
				LOG.info("httpUrl " + httpUrl);
				String fileName = "Q_" + giornata;
				JobProcessFileCsv jobCsv = new JobProcessFileCsv();
				jobCsv.downloadCsv(httpUrl, basePath, fileName, 2);

			} else if (event.getSource() == updateGiocatori) {

				// **************************************
				// UPDATE GIOCATORI
				// **************************************

				LOG.info("httpUrlImg " + Costants.HTTP_URL_IMG);
				String imgPath = basePathData;
				String fileName = "Q_" + giornata;
				fileName = basePathData + fileName + ".csv";
				boolean updateQuotazioni = chkUpdateQuotaz.getValue().booleanValue();
				boolean updateImg = chkUpdateImg.getValue().booleanValue();
				String percentuale = "" + txtPerc.getValue().intValue();
				HashMap<Object, Object> map = jobProcessGiornata.initDbGiocatori(Costants.HTTP_URL_IMG, imgPath, fileName, updateQuotazioni, updateImg, percentuale);

				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriAdd = (ArrayList<FcGiocatore>) map.get("listAdd");
				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriDel = (ArrayList<FcGiocatore>) map.get("listDel");

				LOG.info("listGiocatoriAdd " + listGiocatoriAdd.size());
				LOG.info("listGiocatoriDel " + listGiocatoriDel.size());

				tableGiocatoreAdd.setItems(listGiocatoriAdd);
				tableGiocatoreDel.setItems(listGiocatoriDel);

				tableGiocatoreAdd.getDataProvider().refreshAll();
				tableGiocatoreDel.getDataProvider().refreshAll();

			} else if (event.getSource() == generaCalendar) {

				jobProcessGiornata.generaCalendario(campionato);

			} else if (event.getSource() == formazione422) {

				if (giornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				for (FcAttore a : squadre) {
					jobProcessGiornata.inserisciFormazione442(campionato, a, giornata);
				}

			} else if (event.getSource() == resetFormazione) {

				if (giornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				if (attore == null) {
					CustomMessageDialog.showMessageError("Attore obbligario");
					return;
				}

				jobProcessGiornata.resetFormazione(attore.getIdAttore(), giornata);

			} else if (event.getSource() == ultimaFormazione) {

				if (giornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				if (attore == null) {
					CustomMessageDialog.showMessageError("Attore obbligario");
					return;
				}

				jobProcessGiornata.inserisciUltimaFormazione(attore.getIdAttore(), giornata);

			} else if (event.getSource() == init) {

				if (giornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				jobProcessGiornata.initPagelle(giornata);

				try {
					sendMailInfoGiornata(giornataInfo);
				} catch (Exception e) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, e.getMessage());
				}

			} else if (event.getSource() == download) {

				String urlFanta = (String) p.get("URL_FANTA");

				String votiExcel = "Voti-Ufficiosi-Excel";
				if (chkUfficiali.getValue()) {
					votiExcel = "Voti-Ufficiali-Excel";
				}

				String httpurl = urlFanta + votiExcel + ".asp?giornataScelta=" + giornata;
				String fileName = "voti_" + giornata;
				jobProcessFileCsv.downloadCsv(httpurl, basePathData, fileName, 3);

				fileName = basePathData + "voti_" + giornata + ".csv";
				jobProcessGiornata.aggiornamentoPFGiornata(p, fileName, "" + giornata);

				jobProcessGiornata.checkSeiPolitico(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == seiPolitico) {

				FcSquadra squadra = (FcSquadra) this.comboSqudreA.getValue();
				if (squadra == null) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Squadra obbligaria");
					return;
				}

				jobProcessGiornata.seiPolitico(giornata, squadra);

			} else if (event.getSource() == calcola) {

				int forzaVotoGiocatore = -1;
				if (chkForzaVotoGiocatore.getValue()) {
					forzaVotoGiocatore = 0;
				}
				jobProcessGiornata.algoritmo(giornata, campionato, forzaVotoGiocatore, chkRoundVotoGiocatore.getValue());
				jobProcessGiornata.statistiche(campionato);

				jobProcessGiornata.aggiornaVotiGiocatori(giornata, forzaVotoGiocatore, chkRoundVotoGiocatore.getValue());
				jobProcessGiornata.aggiornaTotRosa("" + campionato.getIdCampionato(), giornata);
				jobProcessGiornata.aggiornaScore(giornata, "tot_pt", "score");
				jobProcessGiornata.aggiornaScore(giornata, "tot_pt_old", "score_old");
				jobProcessGiornata.aggiornaScore(giornata, "tot_pt_old", "score_grand_prix");

			} else if (event.getSource() == calcolaStatistiche) {

				jobProcessGiornata.statistiche(campionato);

			} else if (event.getSource() == pdfAndMail) {

				String pathImg = "images/";
				p.setProperty("ACTIVE_MAIL", this.chkSendMail.getValue().toString());
				if (chkUfficiali.getValue()) {
					p.setProperty("INFO_RESULT", "UFFICIALI");
				} else {
					p.setProperty("INFO_RESULT", "UFFICIOSI");
				}
				jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, p, pathImg, basePathData);

			} else if (event.getSource() == salva) {
				LOG.info("da1 " + da1.getValue());
				LOG.info("da2 " + da2.getValue());
				LOG.info("dg " + dg.getValue());
				LOG.info("dp " + dp.getValue());
				giornataInfo.setDataAnticipo1(da1.getValue());
				giornataInfo.setDataAnticipo2(da2.getValue());
				giornataInfo.setDataGiornata(dg.getValue());
				giornataInfo.setDataPosticipo(dp.getValue());
				LOG.info("getDataAnticipo1 " + giornataInfo.getDataAnticipo1());
				LOG.info("getDataAnticipo2 " + giornataInfo.getDataAnticipo2());
				LOG.info("getDataGiornata " + giornataInfo.getDataGiornata());
				LOG.info("getDataPosticipo " + giornataInfo.getDataPosticipo());
				giornataInfoController.updateGiornataInfo(giornataInfo);
			} else if (event.getSource() == resetDate) {
				da1.setValue(null);
				da2.setValue(null);
				dg.setValue(null);
				dp.setValue(null);
				LOG.info("1 " + da1.getValue());
				LOG.info("1 " + da2.getValue());
				LOG.info("1 " + dg.getValue());
				LOG.info("1 " + dp.getValue());

				List<FcCalendarioCompetizione> listCalend = calendarioTimController.findCustom(giornataInfo);
				LocalDateTime appo = listCalend.get(0).getData();
				ArrayList<LocalDateTime> listDate = new ArrayList<LocalDateTime>();
				for (FcCalendarioCompetizione c : listCalend) {
					LOG.info("" + appo.getDayOfWeek());
					if (appo.getDayOfWeek() != (c.getData().getDayOfWeek())) {
						listDate.add(appo);
						appo = c.getData();
					}
				}

				listDate.add(appo);

				if (listDate.size() == 1) {
					LocalDateTime localDateTime1 = listDate.get(0);
					da1.setValue(null);
					da2.setValue(null);
					dg.setValue(localDateTime1.minus(1, ChronoUnit.MINUTES));
					dp.setValue(null);
				} else if (listDate.size() == 2) {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					da1.setValue(null);
					da2.setValue(localDateTime1.minus(1, ChronoUnit.MINUTES));
					dg.setValue(localDateTime2.minus(1, ChronoUnit.MINUTES));
					dp.setValue(null);
				} else if (listDate.size() == 3) {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					LocalDateTime localDateTime3 = listDate.get(2);
					da1.setValue(null);
					da2.setValue(localDateTime1.minus(1, ChronoUnit.MINUTES));
					dg.setValue(localDateTime2.minus(1, ChronoUnit.MINUTES));
					dp.setValue(localDateTime3.minus(1, ChronoUnit.MINUTES));
				} else if (listDate.size() > 3) {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					LocalDateTime localDateTime3 = listDate.get(2);
					LocalDateTime localDateTime4 = listDate.get(3);
					da1.setValue(localDateTime1.minus(1, ChronoUnit.MINUTES));
					da2.setValue(localDateTime2.minus(1, ChronoUnit.MINUTES));
					dg.setValue(localDateTime3.minus(1, ChronoUnit.MINUTES));
					dp.setValue(localDateTime4.minus(1, ChronoUnit.MINUTES));
				}

				LOG.info("2 " + da1.getValue());
				LOG.info("2 " + da2.getValue());
				LOG.info("2 " + dg.getValue());
				LOG.info("2 " + dp.getValue());
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	@Autowired
	private GiornataService giornataController;

	private void sendMailInfoGiornata(FcGiornataInfo ggInfo)
			throws AddressException, IOException, MessagingException,
			NamingException {

		String subject = "Avvio Giornata - " + Utils.buildInfoGiornataHtml(ggInfo);

		String formazioneHtml = "";
		formazioneHtml += "<html><head><title>FC</title></head>\n";
		formazioneHtml += "<body>\n";
		formazioneHtml += "<p>Prossima Giornata: " + Utils.buildInfoGiornataHtml(ggInfo) + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";

		formazioneHtml += "<table>";

		List<FcGiornata> all = giornataController.findByFcGiornataInfo(ggInfo);
		for (FcGiornata g : all) {
			formazioneHtml += "<tr>";
			formazioneHtml += "<td>";
			formazioneHtml += g.getFcAttoreByIdAttoreCasa().getDescAttore();
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += g.getFcAttoreByIdAttoreFuori().getDescAttore();
			formazioneHtml += "</td>";
			formazioneHtml += "</tr>";
		}

		formazioneHtml += "</table>\n";

		formazioneHtml += "<br>";
		formazioneHtml += "<br>";
		formazioneHtml += "<p>Data Anticipo1:  " + (ggInfo.getDataAnticipo1() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataAnticipo1(), "dd/MM/yyyy HH:mm")) + "</p>";
		formazioneHtml += "<p>Data Anticipo2:  " + (ggInfo.getDataAnticipo2() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataAnticipo2(), "dd/MM/yyyy HH:mm")) + "</p>";
		formazioneHtml += "<p>Data Giornata:  " + (ggInfo.getDataGiornata() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataGiornata(), "dd/MM/yyyy HH:mm")) + "</p>";
		formazioneHtml += "<p>Data Posticipo: " + (ggInfo.getDataPosticipo() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataPosticipo(), "dd/MM/yyyy HH:mm")) + "</p>";
		formazioneHtml += "<br>";
		formazioneHtml += "<br>";
		formazioneHtml += "<p>Ciao Davide</p>";
		formazioneHtml += "</body>";
		formazioneHtml += "<html>";

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		p.setProperty("ACTIVE_MAIL", this.chkSendMail.getValue().toString());

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

		LOG.info(formazioneHtml);

		String from = (String) env.getProperty("spring.mail.username");

		client.sendMail(from, to, cc, bcc, subject, formazioneHtml, "text/html", "3", null);

	}

	@Autowired
	private ResourceLoader resourceLoader;

	private Grid<FcGiocatore> getTableGiocatori() {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(new ArrayList<FcGiocatore>());
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth("550px");

		Column<FcGiocatore> ruoloColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();
			if (g != null) {
				Image img = buildImage("classpath:images/", g.getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("Ruolo");
		ruoloColumn.setAutoWidth(true);

		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();

			if (g != null) {

				StreamResource resource = new StreamResource(g.getNomeImg(),() -> {
					InputStream inputStream = null;
					try {
						inputStream = g.getImgSmall().getBinaryStream();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return inputStream;
				});
				Image img = new Image(resource,"");
				img.setSrc(resource);

				Span lblGiocatore = new Span(g.getCognGiocatore());

				cellLayout.add(img);
				cellLayout.add(lblGiocatore);
			}

			return cellLayout;

		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader("Giocatore");
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcGiocatore> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(g -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);

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
		nomeSquadraColumn.setHeader("Squadra");
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcGiocatore> quotazioneColumn = grid.addColumn(g -> g.getQuotazione());
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		quotazioneColumn.setAutoWidth(true);

		return grid;
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

}