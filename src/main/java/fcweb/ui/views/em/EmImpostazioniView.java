package fcweb.ui.views.em;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.RisultatoBean;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.job.EmJobProcessFileCsv;
import fcweb.backend.job.EmJobProcessGiornata;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ClassificaTotalePuntiService;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Impostazioni")
@Route(value = "emadmin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class EmImpostazioniView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EmailService emailService;

	@Autowired
	private Environment env;

	private List<FcAttore> squadre = null;
	private List<FcGiornataInfo> giornate = null;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private EmJobProcessGiornata emjobProcessGiornata;

	@Autowired
	private ClassificaTotalePuntiService classificaTotalePuntiController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private EmJobProcessFileCsv emjobProcessFileCsv;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private GiornataDettService giornataDettController;

	private ComboBox<FcGiornataInfo> comboGiornata;

    private Button initDb;
	private Button initDbAttore;

	private Button ultimaFormazione;
	private ComboBox<FcAttore> comboAttore;

	private Button downloadQuotaz;
	private Button updateGiocatori;
	private Checkbox chkUpdateQuotaz;
	private Grid<FcGiocatore> tableGiocatoreAdd;
	private Grid<FcGiocatore> tableGiocatoreDel;

	private Button init;
	private Button download;
	private Button calcola;
	private Button ricalcola;
	private Checkbox chkUfficiali;
	private NumberField txtPerc;
	private RadioButtonGroup<String> radioGroupVotiExcel = null;

	private Button calcolaStatistiche;
	private Button aggiornaFlagAttivoGiocatore;
	private Button pdfAndMail;
	private Checkbox chkSendMail;

	private TextArea messaggio;
	private Button notifica;

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
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		Integer from = campionato.getStart();
		Integer to = campionato.getEnd();
		giornate = giornataInfoController.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
	}

    private void initLayout() {

		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		comboGiornata = new ComboBox<>();
		comboGiornata.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
		comboGiornata.setItems(giornate);
		comboGiornata.setClearButtonVisible(true);
		comboGiornata.setPlaceholder("Seleziona la giornata");
		comboGiornata.addValueChangeListener(event -> {
		});
		comboGiornata.setValue(giornataInfo);
		comboGiornata.setWidthFull();

		this.add(comboGiornata);

        initDb = new Button("Init DB");
		initDb.setIcon(VaadinIcon.ADD_DOCK.create());
		initDb.addClickListener(this);

		initDbAttore = new Button("Init DB Attore");
		initDbAttore.setIcon(VaadinIcon.ADD_DOCK.create());
		initDbAttore.addClickListener(this);

		HorizontalLayout layoutSetup = new HorizontalLayout();
		layoutSetup.setMargin(true);
		layoutSetup.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
		// layoutSetup.add(upload, output);
		layoutSetup.add(initDb);
		layoutSetup.add(initDbAttore);

        Details panelSetup = new Details("Setup", layoutSetup);
		panelSetup.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

		this.add(panelSetup);

		comboAttore = new ComboBox<>();
		comboAttore.setItems(squadre);
		comboAttore.setPlaceholder("Seleziona attore");
		comboAttore.setItemLabelGenerator(FcAttore::getDescAttore);
		comboAttore.setClearButtonVisible(true);
		comboAttore.addValueChangeListener(evt -> {
			initDbAttore.setText("Init Db");
			if (evt.getValue() != null) {
				initDbAttore.setText("Init Db " + evt.getValue().getDescAttore());
			}
		});

		ultimaFormazione = new Button("Inserisci Ultima Formazione");
		ultimaFormazione.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		ultimaFormazione.addClickListener(this);

		HorizontalLayout layoutUpdateRow1 = new HorizontalLayout();
		layoutUpdateRow1.setMargin(true);

		layoutUpdateRow1.add(comboAttore);
		layoutUpdateRow1.add(ultimaFormazione);

		downloadQuotaz = new Button("Download Quotazioni");
		downloadQuotaz.setIcon(VaadinIcon.DOWNLOAD.create());
		downloadQuotaz.addClickListener(this);

		updateGiocatori = new Button("Update Giocatori");
		updateGiocatori.setIcon(VaadinIcon.PIN.create());
		updateGiocatori.addClickListener(this);

		chkUpdateQuotaz = new Checkbox("Update Quotazioni");

		txtPerc = new NumberField();
		txtPerc.setMin(0d);
		txtPerc.setMax(100d);
		// txtPerc.setHasControls(true);
		txtPerc.setValue(50d);

		HorizontalLayout layoutUpdateRow2 = new HorizontalLayout();
		layoutUpdateRow2.setMargin(true);

		layoutUpdateRow2.add(downloadQuotaz);
		layoutUpdateRow2.add(updateGiocatori);
		layoutUpdateRow2.add(txtPerc);
		layoutUpdateRow2.add(chkUpdateQuotaz);

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
		layoutUpdate.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

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

		radioGroupVotiExcel = new RadioButtonGroup<>();
		radioGroupVotiExcel.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		radioGroupVotiExcel.setLabel("Voti Excel");
        radioGroupVotiExcel.setItems("europei-voti-ufficiali", "europei-voti-ufficiali-fantacalcio");
		radioGroupVotiExcel.setValue("europei-voti-ufficiali");

		calcola = new Button("Calcola (Yes Algoritmo + Statistiche)");
		calcola.setIcon(VaadinIcon.PIN.create());
		calcola.addClickListener(this);

		ricalcola = new Button("Ri-Calcola (No Algoritmo + Statistiche");
		ricalcola.setIcon(VaadinIcon.PIN.create());
		ricalcola.addClickListener(this);

		calcolaStatistiche = new Button("Calcola Statistiche");
		calcolaStatistiche.setIcon(VaadinIcon.PRESENTATION.create());
		calcolaStatistiche.addClickListener(this);

		aggiornaFlagAttivoGiocatore = new Button("Aggiorna Flag Attivo Giocatore");
		aggiornaFlagAttivoGiocatore.setIcon(VaadinIcon.PRESENTATION.create());
		aggiornaFlagAttivoGiocatore.addClickListener(this);

		pdfAndMail = new Button("Crea Pdf - Invia email");
		pdfAndMail.setIcon(VaadinIcon.MAILBOX.create());
		pdfAndMail.addClickListener(this);

		chkSendMail = new Checkbox("Mail All");

		VerticalLayout layoutCalcola = new VerticalLayout();
		layoutCalcola.setMargin(true);
		layoutCalcola.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

		HorizontalLayout vHor = new HorizontalLayout();
		vHor.add(download);
		vHor.add(radioGroupVotiExcel);
		vHor.add(chkUfficiali);
		vHor.add(calcolaStatistiche);
		vHor.add(aggiornaFlagAttivoGiocatore);

		layoutCalcola.add(init);
		layoutCalcola.add(vHor);
		layoutCalcola.add(calcola);
		layoutCalcola.add(ricalcola);
		layoutCalcola.add(pdfAndMail);
		layoutCalcola.add(chkSendMail);

		Details panelCalcola = new Details("Calcola",layoutCalcola);
		panelCalcola.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelCalcola.setOpened(true);
		this.add(panelCalcola);

		notifica = new Button("Notifica");
		notifica.setIcon(VaadinIcon.ADD_DOCK.create());
		notifica.addClickListener(this);

		messaggio = new TextArea();

		VerticalLayout layoutNotifiche = new VerticalLayout();
		layoutNotifiche.setMargin(true);
		layoutNotifiche.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);

		layoutNotifiche.add(notifica);
		layoutNotifiche.add(messaggio);

		Details panelNotifica = new Details("Calcola",layoutNotifiche);
		panelNotifica.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
		panelNotifica.setOpened(true);
		this.add(panelNotifica);

	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
			FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
            LOG.info("campionato {}", campionato.getDescCampionato());

			FcGiornataInfo giornataInfo = null;
			int giornata = 0;
			if (!comboGiornata.isEmpty()) {
				giornataInfo = comboGiornata.getValue();
				giornata = giornataInfo.getCodiceGiornata();
			}
            LOG.info("giornata {}", giornata);

			String basePathData = (String) p.get("PATH_TMP");
            LOG.info("basePathData {}", basePathData);
			File f = new File(basePathData);
			if (!f.exists()) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Impossibile trovare il percorso specificato " + basePathData);
				return;
			}

			if (event.getSource() == initDb) {

				List<FcAttore> attori = attoreController.findAll();
				for (FcAttore a : attori) {
					if (a.isActive()) {
						for (int j = 1; j <= 23; j++) {
							formazioneController.createFormazione(a, campionato.getIdCampionato(), j);
						}
						classificaTotalePuntiController.createEm(a, campionato, (double) 0);
					}
				}

				if (giornata == 0) {
					giornata = 1;
				}
				emjobProcessGiornata.eminitDb(giornata);

			} else if (event.getSource() == initDbAttore) {

				FcAttore attore = comboAttore.getValue();
                LOG.info("attore {}", attore.getDescAttore());

				for (int j = 1; j <= 23; j++) {
					formazioneController.createFormazione(attore, campionato.getIdCampionato(), j);
				}
				classificaTotalePuntiController.createEm(attore, campionato, (double) 0);

			} else if (event.getSource() == ultimaFormazione) {

				FcAttore attore = comboAttore.getValue();
                LOG.info("attore {}", attore.getDescAttore());

				emjobProcessGiornata.eminserisciUltimaFormazione(attore.getIdAttore(), giornata);

			} else if (event.getSource() == init) {

				emjobProcessGiornata.eminitPagelle(giornata);

				try {
                    assert giornataInfo != null;
                    sendMailInfoGiornata(giornataInfo);
				} catch (Exception e) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, e.getMessage());
				}

			} else if (event.getSource() == downloadQuotaz) {

				// **************************************
				// DOWNLOAD FILE QUOTAZIONI
				// **************************************
				String urlFanta = (String) p.get("URL_FANTA");
                // String quotaz = "mondiale-giocatori-quotazioni-excel";
				String quotaz = "europei-giocatori-quotazioni-excel";
				// https://www.pianetafanta.it/mondiale-giocatori-quotazioni-excel.asp?giornata=0&Nome=&Squadre=&Ruolo=&Ruolo2=&Quota=&Quota1=
				String httpUrl = urlFanta + quotaz + ".asp?giornata=" + giornata;
                LOG.info("httpUrl {}", httpUrl);
				String fileName = "Q_" + giornata;
				EmJobProcessFileCsv jobCsv = new EmJobProcessFileCsv();
				jobCsv.downloadCsv(httpUrl, basePathData, fileName, 2);

			} else if (event.getSource() == updateGiocatori) {

				// **************************************
				// UPDATE GIOCATORI
				// **************************************
                String fileName = "Q_" + giornata;
				fileName = basePathData + fileName + ".csv";
				boolean updateQuotazioni = chkUpdateQuotaz.getValue();
				String percentuale = "" + txtPerc.getValue().intValue();
				HashMap<Object, Object> map = emjobProcessGiornata.initDbGiocatori(Costants.HTTP_URL_IMG, basePathData, fileName, updateQuotazioni, percentuale);

				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriAdd = (ArrayList<FcGiocatore>) map.get("listAdd");
				@SuppressWarnings("unchecked")
				ArrayList<FcGiocatore> listGiocatoriDel = (ArrayList<FcGiocatore>) map.get("listDel");

                LOG.info("listGiocatoriAdd {}", listGiocatoriAdd.size());
                LOG.info("listGiocatoriDel {}", listGiocatoriDel.size());

				tableGiocatoreAdd.setItems(listGiocatoriAdd);
				tableGiocatoreDel.setItems(listGiocatoriDel);

				tableGiocatoreAdd.getDataProvider().refreshAll();
				tableGiocatoreDel.getDataProvider().refreshAll();

			} else if (event.getSource() == download) {

				String urlFanta = (String) p.get("URL_FANTA");

				// String votiExcel = "Voti-Ufficiali-Europei-Excel";
				// votiExcel = "Voti-Ufficiosi-Mondiale-Excel";
				// if (chkUfficiali.getValue()) {
				// votiExcel = "Voti-Ufficiali-Mondiale-Excel";
				// }
				// String httpUrlExcel = urlFanta + votiExcel +
				// ".asp?giornataScelta=" + giornata;
				// votiExcel = "mondiale-voti-ufficiali";
				// if (chkUfficiali.getValue()) {
				// votiExcel = "mondiale-voti-ufficiali";
				// }
				// mondiale-voti-ufficiali-fantacalcio.asp?TipoVoti=&searchBonus=&GiornataA=1

				String votiExcel = radioGroupVotiExcel.getValue();
				String httpUrlExcel = urlFanta + votiExcel + ".asp?TipoVoti=&searchBonus=&GiornataA=" + giornata;

				String fileName = "voti_" + giornata;

				emjobProcessFileCsv.downloadCsvNoExcel(httpUrlExcel, basePathData, fileName, 2);

				fileName = basePathData + "voti_" + giornata + ".csv";
				emjobProcessGiornata.emaggiornamentoPFGiornataNoExcel(p, fileName, "" + giornata);

			} else if (event.getSource() == calcola) {

                assert giornataInfo != null;
                emjobProcessGiornata.emalgoritmo(giornataInfo.getCodiceGiornata(), campionato);
				emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == ricalcola) {

                assert giornataInfo != null;
                emjobProcessGiornata.ricalcolaTotPunti(giornataInfo.getCodiceGiornata(), campionato);
				emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == calcolaStatistiche) {

                assert giornataInfo != null;
                emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == aggiornaFlagAttivoGiocatore) {

                assert giornataInfo != null;
                emjobProcessGiornata.aggiornaFlagAttivoGiocatore(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == pdfAndMail) {

				String imgLog = env.getProperty("img.logo");
				String pathImg = "images/";

				Resource resource = resourceLoader.getResource("classpath:reports/em/risultati.jasper");
				InputStream inputStream = resource.getInputStream();
                assert giornataInfo != null;
                Map<String, Object> params = getMap(giornataInfo.getCodiceGiornata(), pathImg);
				Collection<RisultatoBean> collection = new ArrayList<>();
				collection.add(new RisultatoBean("P","S1", 6.0, 6.0, 6.0, 6.0));
				String destFileName1 = basePathData + giornataInfo.getDescGiornataFc() + ".pdf";
				FileOutputStream outputStream = new FileOutputStream(destFileName1);
                JasperReporUtils.runReportToPdfStream(inputStream, outputStream, params, collection);

				Resource resource2 = resourceLoader.getResource("classpath:reports/em/classifica.jasper");
				InputStream inputStream2 = resource2.getInputStream();
				Map<String, Object> params2 = new HashMap<>();
				params2.put("DIVISORE", "" + Costants.DIVISORE_10);
				params2.put("PATH_IMG", pathImg + imgLog);
				String destFileName2 = basePathData + "Classifica.pdf";
				FileOutputStream outputStream2 = new FileOutputStream(destFileName2);
                assert jdbcTemplate.getDataSource() != null;
                Connection conn = jdbcTemplate.getDataSource().getConnection();
                JasperReporUtils.runReportToPdfStream(inputStream2, outputStream2, params2, conn);

				StringBuilder email_destinatario = new StringBuilder();

				if (this.chkSendMail.getValue()) {
					List<FcAttore> attori = attoreController.findAll();
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
                String[] att = new String[] { destFileName1, destFileName2 };
				String subject = "Risultati " + giornataInfo.getDescGiornataFc();
				if (chkUfficiali.getValue()) {
					subject += " - Ufficiali";
				} else {
					subject += " - Parziali";
				}

				String message = getBody();

				try {

					try {
						String from = env.getProperty("spring.mail.secondary.username");
						emailService.sendMail(false, from, to, null, null, subject, message, "text/html", att);
					} catch (Exception e) {
                        String from = env.getProperty("spring.mail.primary.username");
                        emailService.sendMail(true, from, to, null, null, subject, message, "text/html", att);
                    }

				} catch (Exception e) {
					CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_MAIL_KO);
				}
			} else if (event.getSource() == notifica) {

				StringBuilder email_destinatario = new StringBuilder();
				List<FcAttore> attori = attoreController.findAll();
				for (FcAttore a : attori) {
					email_destinatario.append(a.getEmail()).append(";");
				}
				String[] to = null;
				if (!email_destinatario.toString().isEmpty()) {
					to = Utils.tornaArrayString(email_destinatario.toString(), ";");
				}

                String subject = "Avviso";
				String message = messaggio.getValue();

				try {

					try {
						String from = env.getProperty("spring.mail.secondary.username");
						emailService.sendMail(false, from, to, null, null, subject, message, "", null);
					} catch (Exception e) {
                        String from = env.getProperty("spring.mail.primary.username");
                        emailService.sendMail(true, from, to, null, null, subject, message, "", null);
                    }

				} catch (Exception e) {
					CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_MAIL_KO);
				}
			}
			CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private String getBody() {

		String msgHtml = "";
		msgHtml += "<html><head><title>FC</title></head>\n";
		msgHtml += "<body>\n";
		msgHtml += "<p>Sito aggiornato</p>\n";
		msgHtml += "<br>\n";
		msgHtml += "<br>\n";
		msgHtml += "<p>Saluti Davide</p>\n";
		msgHtml += "</BODY>\n";
		msgHtml += "<HTML>";

		return msgHtml;

	}

	private Map<String, Object> getMap(int giornata, String pathImg) {

		NumberFormat formatter = new DecimalFormat("#0.00");

		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(giornata);

		List<FcAttore> squadre = attoreController.findAll();

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("path_img", pathImg);
		parameters.put("titolo", giornataInfo.getDescGiornataFc());
		int conta = 1;
		for (FcAttore a : squadre) {

			List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(a, giornataInfo);
			FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcAttoreAndFcGiornataInfo(a, giornataInfo);

			int countD = 0;
			int countC = 0;
			int countA = 0;
			Collection<RisultatoBean> dm = new ArrayList<>();
			for (FcGiornataDett gd : lGiocatori) {
				if ("S".equals(gd.getFlagAttivo())) {
                    switch (gd.getFcGiocatore().getFcRuolo().getIdRuolo()) {
                        case "D" -> countD++;
                        case "C" -> countC++;
                        case "A" -> countA++;
                    }
				}

				RisultatoBean bean = getRisultatoBean(pathImg, gd);

				dm.add(bean);

			}

			String schema = countD + "-" + countC + "-" + countA;
			LOG.info(schema);

			String puntiTotali = "";
			if (totPunti != null) {
				puntiTotali = formatter.format(totPunti.getTotPt() / Double.parseDouble("" + Costants.DIVISORE_10));
			}

			parameters.put("sq" + conta, a.getDescAttore());
			parameters.put("data" + conta, dm);
			parameters.put("ris" + conta, puntiTotali);
			parameters.put("dataInfo" + conta, null);

			conta++;
		}

		return parameters;

	}

	private @NonNull RisultatoBean getRisultatoBean(String pathImg, FcGiornataDett gd) {
		RisultatoBean bean = new RisultatoBean();

		bean.setR(gd.getFcGiocatore().getFcRuolo().getIdRuolo());
		bean.setCalciatore(gd.getFcGiocatore().getCognGiocatore());

		if (gd.getVoto() != null) {
			bean.setV(gd.getVoto() / Double.parseDouble("" + Costants.DIVISORE_10));
		}

		if (gd.getFcPagelle().getG() != null) {
			bean.setG(gd.getFcPagelle().getG() / Double.parseDouble("" + Costants.DIVISORE_10));
		}
		if (gd.getFcPagelle().getCs() != null) {
			bean.setCs(gd.getFcPagelle().getCs() / Double.parseDouble("" + Costants.DIVISORE_10));
		}
		if (gd.getFcPagelle().getTs() != null) {
			bean.setTs(gd.getFcPagelle().getTs() / Double.parseDouble("" + Costants.DIVISORE_10));
		}

		bean.setFlag_attivo(gd.getFlagAttivo());
		bean.setOrdinamento(gd.getOrdinamento());
		bean.setGoal_realizzato(gd.getFcPagelle().getGoalRealizzato());
		bean.setGoal_subito(gd.getFcPagelle().getGoalSubito());
		bean.setAmmonizione(gd.getFcPagelle().getAmmonizione());
		bean.setEspulsione(gd.getFcPagelle().getEspulsione());
		bean.setRigore_segnato(gd.getFcPagelle().getRigoreSegnato());
		bean.setRigore_fallito(gd.getFcPagelle().getRigoreFallito());
		bean.setRigore_parato(gd.getFcPagelle().getRigoreParato());
		bean.setAutorete(gd.getFcPagelle().getAutorete());
		bean.setAssist(gd.getFcPagelle().getAssist());
		bean.setGv(gd.getFcPagelle().getGdv());
		bean.setPath_img(pathImg);
		return bean;
	}

	private void sendMailInfoGiornata(FcGiornataInfo ggInfo) throws Exception {

		String subject = "Avvio Giornata - " + ggInfo.getDescGiornataFc();
        LOG.info("subject {}", subject);
		String formazioneHtml = "";
		formazioneHtml += "<html><head><title>FC</title></head>\n";
		formazioneHtml += "<body>\n";
		formazioneHtml += "<p>Prossima Giornata: " + ggInfo.getDescGiornataFc() + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>Data Giornata: " + Utils.formatLocalDateTime(ggInfo.getDataGiornata(), Costants.DATA_FORMATTED) + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>Ciao Davide</p>\n";
		formazioneHtml += "</body>\n";
		formazioneHtml += "<html>";
        LOG.info("formazioneHtml {}", formazioneHtml);
		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		p.setProperty("ACTIVE_MAIL", this.chkSendMail.getValue().toString());

		StringBuilder email_destinatario = new StringBuilder();
		String ACTIVE_MAIL = p.getProperty("ACTIVE_MAIL");
        LOG.info("ACTIVE_MAIL {}", ACTIVE_MAIL);
		if ("true".equals(ACTIVE_MAIL)) {
			List<FcAttore> attori = attoreController.findAll();
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

        LOG.info(formazioneHtml);

		try {
			String from = env.getProperty("spring.mail.secondary.username");
			emailService.sendMail(false, from, to, null, null, subject, formazioneHtml, "text/html", null);
		} catch (Exception e) {
            String from = env.getProperty("spring.mail.primary.username");
            emailService.sendMail(true, from, to, null, null, subject, formazioneHtml, "text/html", null);
        }
	}

	private Grid<FcGiocatore> getTableGiocatori() {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(new ArrayList<>());
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		grid.setWidth("600px");

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
			if (g != null) {
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
				if (sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						LOG.error(e.getMessage());
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

		Column<FcGiocatore> quotazioneColumn = grid.addColumn(new ComponentRenderer<>(g -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);
			if (g != null) {
				String q = "" + g.getQuotazione();
				Span span = new Span();
				span.setText(q);
				cellLayout.add(span);
			}
			return cellLayout;
		}));
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		quotazioneColumn.setAutoWidth(true);

		return grid;
	}

}