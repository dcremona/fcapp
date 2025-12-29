package fcweb.ui.views.seriea;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;

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
import fcweb.backend.service.EmailService;
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

@PageTitle("Impostazioni")
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ImpostazioniView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private List<FcAttore> squadre = null;
	private List<FcSquadra> squadreSerieA = null;
	private List<FcGiornataInfo> giornate = null;

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

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

	private Button testMailPrimary;
	private Button testMailSecondary;

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
		log.debug("init");
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
		log.info(String.format("from {0} and then {1}", from, to));
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
		comboGiornata.setItemLabelGenerator(Utils::buildInfoGiornata);
		comboGiornata.setItems(giornate);
		comboGiornata.setClearButtonVisible(true);
		comboGiornata.setPlaceholder("Seleziona la giornata");
		comboGiornata.addValueChangeListener(event -> {
			FcGiornataInfo fcGiornataInfo2 = null;
			if (event.getSource().isEmpty()) {
				log.info("event.getSource().isEmpty()");
			} else if (event.getOldValue() == null) {
				log.info("event.getOldValue()");
				fcGiornataInfo2 = event.getValue();
			} else {
				fcGiornataInfo2 = event.getValue();
			}
			if (fcGiornataInfo2 != null && da1 != null && da2 != null && dg != null && dp != null) {
                log.info("gioranta {}", fcGiornataInfo2.getCodiceGiornata());
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
                log.info("getCodiceGiornata {}", fcGiornataInfo2.getCodiceGiornata());
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
		layoutSetup.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		layoutSetup.add(initDb);
		layoutSetup.add(generaCalendar);

		panelSetup = new Details("Setup",layoutSetup);
		panelSetup.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

		this.add(panelSetup);

		panelSetup.setOpened(false);
		initDb.setEnabled(false);
		generaCalendar.setEnabled(false);
		if (giornataInfo.getCodiceGiornata() == 1 || giornataInfo.getCodiceGiornata() == 20) {
			panelSetup.setOpened(true);
			initDb.setEnabled(true);
			generaCalendar.setEnabled(true);
		}

		comboAttore = new ComboBox<>();
		comboAttore.setItems(squadre);
		comboAttore.setItemLabelGenerator(FcAttore::getDescAttore);
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
		formazione422.setEnabled(true);

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
		txtPerc.setValue(70d);

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

		testMailPrimary = new Button("Test Mail Primary");
		testMailPrimary.setIcon(VaadinIcon.MAILBOX.create());
		testMailPrimary.addClickListener(this);

		testMailSecondary = new Button("Test Mail Secondary");
		testMailSecondary.setIcon(VaadinIcon.MAILBOX.create());
		testMailSecondary.addClickListener(this);

		VerticalLayout layoutUpdate = new VerticalLayout();
		layoutUpdate.setMargin(true);
		layoutUpdate.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

		layoutUpdate.add(layoutUpdateRow1);
		layoutUpdate.add(layoutUpdateRow2);
		layoutUpdate.add(layoutUpdateRow3);
		layoutUpdate.add(layoutUpdateRow4);
		layoutUpdate.add(testMailPrimary);
		layoutUpdate.add(testMailSecondary);

		InMemoryUploadHandler inMemoryHandler = UploadHandler.inMemory((
				metadata, data) -> {
			// Get other information about the file.

            try {
				InputStream is = new ByteArrayInputStream(data);
				jobProcessGiornata.updateImgGiocatore(is);

				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
			} catch (Exception e) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
			}
		});
		Upload upload = new Upload(inMemoryHandler);

		layoutUpdate.add(upload);

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
		comboSqudreA.setItemLabelGenerator(FcSquadra::getNomeSquadra);
		comboSqudreA.setClearButtonVisible(true);
		comboSqudreA.setPlaceholder(Costants.SQUADRA);
		comboSqudreA.setRenderer(new ComponentRenderer<>(item -> {
			VerticalLayout container = new VerticalLayout();
			if (item != null && item.getImg() != null) {
				try {
					Image img = Utils.getImage(item.getNomeSquadra(), item.getImg().getBinaryStream());
					container.add(img);
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
			Span lblSquadra = new Span(Objects.requireNonNull(item).getNomeSquadra());
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
		layoutCalcola.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

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
			dg.setValue(giornataInfo.getDataGiornata());
		}

		dp = new DateTimePicker("Data Posticipo");
		if (giornataInfo.getDataPosticipo() != null) {
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
		layoutDate.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

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
		boolean val = "1".equals(p.getProperty(key));
		check.setValue(val);

		check.addValueChangeListener(event -> {
			try {
				Boolean value = event.getValue();
				FcProperties proprieta = new FcProperties();
				proprieta.setKey(key);
				proprieta.setValue(value ? "1" : "0");
				proprietaController.updateProprieta(proprieta);
				p.setProperty(key, value ? "1" : "0");
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
			int codiceGiornata = 0;
			if (!comboGiornata.isEmpty()) {
				giornataInfo = comboGiornata.getValue();
				codiceGiornata = giornataInfo.getCodiceGiornata();
			}
			FcAttore attore = comboAttore.getValue();
            log.info("giornata {}", codiceGiornata);

			String basePathData = (String) p.get("PATH_TMP");
            log.info("basePathData {}", basePathData);
			File f = new File(basePathData);
			if (!f.exists()) {
				CustomMessageDialog.showMessageError("Impossibile trovare il percorso specificato " + basePathData);
				return;
			}

			if (event.getSource() == initDb) {

				List<FcAttore> attori = attoreController.findAll();
				for (FcAttore a : attori) {
					if (a.isActive()) {
						for (int j = 1; j <= 26; j++) {
							formazioneController.createFormazione(a, campionato.getIdCampionato(), j);
						}
						classificaController.create(a, campionato, (double) 0);
					}
				}

			} else if (event.getSource() == testMailPrimary) {

				try {
					// String fromPrimary = "notifiche-fclt@hostingtt.it";
					String fromPrimary = env.getProperty("spring.mail.primary.username");
					String toPrimary = "davide.cremona@gmail.com";
					String subjectPrimary = "Testing from Spring Boot sendEmailPrimary";
					String textPrimary = "Testing from Spring Boot sendEmailPrimary";
					this.emailService.sendPrimaryEmail(fromPrimary, toPrimary, subjectPrimary, textPrimary);
				} catch (Exception e) {
					log.error(e.getMessage());
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
					return;
				}

			} else if (event.getSource() == testMailSecondary) {

				try {
					// String fromSecondary = "notifichefclt@gmail.com";
					String fromSecondary = env.getProperty("spring.mail.secondary.username");
					String toSecondary = "davide.cremona@gmail.com";
					String subjectSecondary = "Testing from Spring Boot sendEmailSecondary";
					String textSecondary = "Testing from Spring Boot sendEmailSecondary";
					this.emailService.sendSecondaryEmail(fromSecondary, toSecondary, subjectSecondary, textSecondary);
				} catch (Exception e2) {
					log.error(e2.getMessage());
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e2.getMessage());
					return;
				}

			} else if (event.getSource() == downloadQuotaz) {

				// **************************************
				// DOWNLOAD FILE QUOTAZIONI
				// **************************************

				String urlFanta = (String) p.get("URL_FANTA");
                String quotaz = "Giocatori-Quotazioni-Excel";
				String httpUrl = urlFanta + quotaz + ".asp?giornata=" + codiceGiornata;

                log.info("httpUrl {}", httpUrl);
				String fileName = "Q_" + codiceGiornata;
				JobProcessFileCsv jobCsv = new JobProcessFileCsv();
				jobCsv.downloadCsv(httpUrl, basePathData, fileName, 2);

			} else if (event.getSource() == updateGiocatori) {

				// **************************************
				// UPDATE GIOCATORI
				// **************************************

				log.info("httpUrlImg " + Costants.HTTP_URL_IMG);
                String fileName = "Q_" + codiceGiornata;
				fileName = basePathData + fileName + ".csv";
				boolean updateQuotazioni = chkUpdateQuotaz.getValue();
				boolean updateImg = chkUpdateImg.getValue();
				String percentuale = "" + txtPerc.getValue().intValue();
				HashMap<Object, Object> map = jobProcessGiornata.initDbGiocatori(Costants.HTTP_URL_IMG, basePathData, fileName, updateQuotazioni, updateImg, percentuale);

				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriAdd = (ArrayList<FcGiocatore>) map.get("listAdd");
				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriDel = (ArrayList<FcGiocatore>) map.get("listDel");

                log.info("listGiocatoriAdd {}", listGiocatoriAdd.size());
                log.info("listGiocatoriDel {}", listGiocatoriDel.size());

				tableGiocatoreAdd.setItems(listGiocatoriAdd);
				tableGiocatoreDel.setItems(listGiocatoriDel);

				tableGiocatoreAdd.getDataProvider().refreshAll();
				tableGiocatoreDel.getDataProvider().refreshAll();

			} else if (event.getSource() == generaCalendar) {

				jobProcessGiornata.generaCalendario(campionato);

			} else if (event.getSource() == formazione422) {

				if (codiceGiornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				ConfirmDialog dialog = getConfirmDialog(codiceGiornata, campionato);
				dialog.open();

				return;

			} else if (event.getSource() == resetFormazione) {

				if (codiceGiornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				if (attore == null) {
					CustomMessageDialog.showMessageError("Attore obbligario");
					return;
				}

				jobProcessGiornata.resetFormazione(attore.getIdAttore(), codiceGiornata);

			} else if (event.getSource() == ultimaFormazione) {

				if (codiceGiornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				if (attore == null) {
					CustomMessageDialog.showMessageError("Attore obbligario");
					return;
				}

				jobProcessGiornata.inserisciUltimaFormazione(attore.getIdAttore(), codiceGiornata);

			} else if (event.getSource() == init) {

				if (codiceGiornata == 0) {
					CustomMessageDialog.showMessageError("Giornata obbligaria");
					return;
				}

				jobProcessGiornata.initPagelle(codiceGiornata);

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

				String httpurl = urlFanta + votiExcel + ".asp?giornataScelta=" + codiceGiornata;
				String fileName = "voti_" + codiceGiornata;
				jobProcessFileCsv.downloadCsv(httpurl, basePathData, fileName, 3);

				fileName = basePathData + "voti_" + codiceGiornata + ".csv";
				jobProcessGiornata.aggiornamentoPFGiornata(p, fileName, "" + codiceGiornata);

                assert giornataInfo != null;
                jobProcessGiornata.checkSeiPolitico(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == seiPolitico) {

				FcSquadra squadra = this.comboSqudreA.getValue();
				if (squadra == null) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Squadra obbligaria");
					return;
				}

				jobProcessGiornata.seiPolitico(codiceGiornata, squadra);

			} else if (event.getSource() == calcola) {

				int forzaVotoGiocatore = -1;
				if (chkForzaVotoGiocatore.getValue()) {
					forzaVotoGiocatore = 0;
				}
				jobProcessGiornata.algoritmo(codiceGiornata, campionato, forzaVotoGiocatore, chkRoundVotoGiocatore.getValue());
				jobProcessGiornata.statistiche(campionato);

				jobProcessGiornata.aggiornaVotiGiocatori(codiceGiornata, forzaVotoGiocatore, chkRoundVotoGiocatore.getValue());
				jobProcessGiornata.aggiornaTotRosa("" + campionato.getIdCampionato(), codiceGiornata);
				jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt", "score");
				jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt_old", "score_old");
				jobProcessGiornata.aggiornaScore(codiceGiornata, "tot_pt_old", "score_grand_prix");

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
                assert giornataInfo != null;
                jobProcessSendMail.writePdfAndSendMail(campionato, giornataInfo, p, pathImg, basePathData);

			} else if (event.getSource() == salva) {
                log.info("da1 {}", da1.getValue());
                log.info("da2 {}", da2.getValue());
                log.info("dg {}", dg.getValue());
                log.info("dp {}", dp.getValue());
                assert giornataInfo != null;
                giornataInfo.setDataAnticipo1(da1.getValue());
				giornataInfo.setDataAnticipo2(da2.getValue());
				giornataInfo.setDataGiornata(dg.getValue());
				giornataInfo.setDataPosticipo(dp.getValue());
                log.info("getDataAnticipo2 {}", giornataInfo.getDataAnticipo2());
                log.info("getDataGiornata {}", giornataInfo.getDataGiornata());
                log.info("getDataPosticipo {}", giornataInfo.getDataPosticipo());
				giornataInfoController.updateGiornataInfo(giornataInfo);
			} else if (event.getSource() == resetDate) {
				da1.setValue(null);
				da2.setValue(null);
				dg.setValue(null);
				dp.setValue(null);
                log.info("1 {}", da1.getValue());
                log.info("1 {}", da2.getValue());
                log.info("1 {}", dg.getValue());
                log.info("1 {}", dp.getValue());

				List<FcCalendarioCompetizione> listCalend = calendarioTimController.findCustom(giornataInfo);
				LocalDateTime appo = listCalend.get(0).getData();
				ArrayList<LocalDateTime> listDate = new ArrayList<>();
				for (FcCalendarioCompetizione c : listCalend) {
                    log.info("{}", appo.getDayOfWeek());
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
					dg.setValue(localDateTime1.minusMinutes(1));
					dp.setValue(null);
				} else if (listDate.size() == 2) {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					da1.setValue(null);
					da2.setValue(localDateTime1.minusMinutes(1));
					dg.setValue(localDateTime2.minusMinutes(1));
					dp.setValue(null);
				} else if (listDate.size() == 3) {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					LocalDateTime localDateTime3 = listDate.get(2);
					da1.setValue(null);
					da2.setValue(localDateTime1.minusMinutes(1));
					dg.setValue(localDateTime2.minusMinutes(1));
					dp.setValue(localDateTime3.minusMinutes(1));
				} else {
					LocalDateTime localDateTime1 = listDate.get(0);
					LocalDateTime localDateTime2 = listDate.get(1);
					LocalDateTime localDateTime3 = listDate.get(2);
					LocalDateTime localDateTime4 = listDate.get(3);
					da1.setValue(localDateTime1.minusMinutes(1));
					da2.setValue(localDateTime2.minusMinutes(1));
					dg.setValue(localDateTime3.minusMinutes(1));
					dp.setValue(localDateTime4.minusMinutes(1));
				}

                log.info("2 {}", da1.getValue());
                log.info("2 {}", da2.getValue());
                log.info("2 {}", dg.getValue());
                log.info("2 {}", dp.getValue());
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private @NonNull ConfirmDialog getConfirmDialog(int codiceGiornata, FcCampionato campionato) {
		String msg = "Confermi inserimento formazioni 422 per la giornata " + codiceGiornata;

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setHeader(CustomMessageDialog.TITLE_MSG_CONFIRM);
		dialog.setText(msg);
		dialog.setCancelable(true);
		dialog.setCancelText("Annulla");
		dialog.setRejectable(false);
		dialog.setConfirmText("Conferma");
		dialog.addConfirmListener(e -> {
			try {
				int giornata = 0;
				if (!comboGiornata.isEmpty()) {
					FcGiornataInfo ggInfo = comboGiornata.getValue();
					giornata = ggInfo.getCodiceGiornata();
				}
				for (FcAttore a : squadre) {
					jobProcessGiornata.inserisciFormazione442(campionato, a, giornata);
				}

				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
			} catch (Exception excpt) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, excpt.getMessage());
			}
		});
		return dialog;
	}

	@Autowired
	private GiornataService giornataController;

	private void sendMailInfoGiornata(FcGiornataInfo ggInfo) throws Exception {

		String subject = "Avvio Giornata - " + Utils.buildInfoGiornataHtml(ggInfo);

		StringBuilder formazioneHtml = new StringBuilder();
		formazioneHtml.append("<html><head><title>FC</title></head>\n");
		formazioneHtml.append("<body>\n");
		formazioneHtml.append("<p>Prossima Giornata: ").append(Utils.buildInfoGiornataHtml(ggInfo)).append("</p>\n");
		formazioneHtml.append("<br>\n");
		formazioneHtml.append("<br>\n");

		formazioneHtml.append("<table>");

		List<FcGiornata> all = giornataController.findByFcGiornataInfo(ggInfo);
		for (FcGiornata g : all) {
			formazioneHtml.append("<tr>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(g.getFcAttoreByIdAttoreCasa().getDescAttore());
			formazioneHtml.append("</td>");
			formazioneHtml.append("<td>");
			formazioneHtml.append(g.getFcAttoreByIdAttoreFuori().getDescAttore());
			formazioneHtml.append("</td>");
			formazioneHtml.append("</tr>");
		}

		formazioneHtml.append("</table>\n");

		formazioneHtml.append("<br>");
		formazioneHtml.append("<br>");
		formazioneHtml.append("<p>Data Anticipo1:  ").append(ggInfo.getDataAnticipo1() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataAnticipo1(), Costants.DATA_FORMATTED)).append("</p>");
		formazioneHtml.append("<p>Data Anticipo2:  ").append(ggInfo.getDataAnticipo2() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataAnticipo2(), Costants.DATA_FORMATTED)).append("</p>");
		formazioneHtml.append("<p>Data Giornata:  ").append(ggInfo.getDataGiornata() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataGiornata(), Costants.DATA_FORMATTED)).append("</p>");
		formazioneHtml.append("<p>Data Posticipo: ").append(ggInfo.getDataPosticipo() == null ? "" : Utils.formatLocalDateTime(ggInfo.getDataPosticipo(), Costants.DATA_FORMATTED)).append("</p>");
		formazioneHtml.append("<br>");
		formazioneHtml.append("<br>");
		formazioneHtml.append("<p>Ciao Davide</p>");
		formazioneHtml.append("</body>");
		formazioneHtml.append("<html>");

		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		p.setProperty("ACTIVE_MAIL", this.chkSendMail.getValue().toString());

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

        // log.info(formazioneHtml);
		try {
			String from = env.getProperty("spring.mail.secondary.username");
			emailService.sendMail(false, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
		} catch (Exception e) {
			log.error(e.getMessage());
			try {
				String from = env.getProperty("spring.mail.primary.username");
				emailService.sendMail(true, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
			} catch (Exception e2) {
				log.error(e2.getMessage());
				throw e2;
			}
		}
	}

	@Autowired
	private ResourceLoader resourceLoader;

	private Grid<FcGiocatore> getTableGiocatori() {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(new ArrayList<>());
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
				Image img = Utils.buildImage(g.getFcRuolo().getIdRuolo().toLowerCase() + ".png", resourceLoader.getResource(Costants.CLASSPATH_IMAGES + g.getFcRuolo().getIdRuolo().toLowerCase() + ".png"));
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader(Costants.RUOLO);
		ruoloColumn.setAutoWidth(true);

		Column<FcGiocatore> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			cellLayout.setSizeFull();
			if (g != null) {
				try {
					Image img = Utils.getImage(g.getNomeImg(), g.getImgSmall().getBinaryStream());
					cellLayout.add(img);
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
				Span lblGiocatore = new Span(g.getCognGiocatore());
				cellLayout.add(lblGiocatore);
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader(Costants.GIOCATORE);
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
						log.error(e.getMessage());
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

		Column<FcGiocatore> quotazioneColumn = grid.addColumn(FcGiocatore::getQuotazione);
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		quotazioneColumn.setAutoWidth(true);

		return grid;
	}

}