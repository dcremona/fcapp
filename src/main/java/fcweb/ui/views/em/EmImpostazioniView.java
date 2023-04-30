package fcweb.ui.views.em;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.naming.NamingException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.mail.MailClient;
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
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;


@PageTitle("Impostazioni")
@Route(value = "emadmin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class EmImpostazioniView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JavaMailSender javaMailSender;

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

	// @Autowired
	// private GiornataController giornataController;

	@Autowired
	private EmJobProcessFileCsv emjobProcessFileCsv;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private GiornataDettService giornataDettController;

	private ComboBox<FcGiornataInfo> comboGiornata;

	private Details panelSetup;
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

	private Component createComponent(String mimeType, String fileName,
			InputStream stream) {
		if (mimeType.startsWith("text")) {
			return createTextComponent(stream);
		} else if (mimeType.startsWith("image")) {
			Image image = new Image();
			try {

				byte[] bytes = IOUtils.toByteArray(stream);
				image.getElement().setAttribute("src", new StreamResource(fileName,() -> new ByteArrayInputStream(bytes)));
				try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
					final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
					if (readers.hasNext()) {
						ImageReader reader = readers.next();
						try {
							reader.setInput(in);
							image.setWidth(reader.getWidth(0) + "px");
							image.setHeight(reader.getHeight(0) + "px");
						} finally {
							reader.dispose();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			image.setSizeFull();
			return image;
		}
		Div content = new Div();
		String text = String.format("Mime type: '%s'\nSHA-256 hash: '%s'", mimeType, MessageDigestUtil.sha256(stream.toString()));
		content.setText(text);
		return content;

	}

	private Component createTextComponent(InputStream stream) {
		String text;
		try {
			text = IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			text = "exception reading stream";
		}
		return new Text(text);
	}

	private void showOutput(String text, Component content,
			HasComponents outputContainer) {
		HtmlComponent p = new HtmlComponent(Tag.P);
		p.getElement().setText(text);
		outputContainer.add(p);
		outputContainer.add(content);
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

		MemoryBuffer buffer = new MemoryBuffer();
		Upload upload = new Upload(buffer);
		Div output = new Div();
		upload.addSucceededListener(event -> {
			try {

				HashMap<Object, Object> map = emjobProcessGiornata.initDbGiocatoriExcel(buffer.getInputStream());

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

				Component component = createComponent(event.getMIMEType(), event.getFileName(), buffer.getInputStream());
				output.removeAll();
				showOutput(event.getFileName(), component, output);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		});

		upload.addFileRejectedListener(event -> {
			Paragraph component = new Paragraph();
			output.removeAll();
			showOutput(event.getErrorMessage(), component, output);
		});
		upload.getElement().addEventListener("file-remove", event -> {
			output.removeAll();
		});

		// this.add(upload, output);

		initDb = new Button("Init DB");
		initDb.setIcon(VaadinIcon.ADD_DOCK.create());
		initDb.addClickListener(this);

		initDbAttore = new Button("Init DB Attre");
		initDbAttore.setIcon(VaadinIcon.ADD_DOCK.create());
		initDbAttore.addClickListener(this);

		HorizontalLayout layoutSetup = new HorizontalLayout();
		layoutSetup.setMargin(true);
		layoutSetup.getStyle().set("border", Costants.BORDER_COLOR);
		layoutSetup.add(upload, output);
		layoutSetup.add(initDb);
		layoutSetup.add(initDbAttore);

		panelSetup = new Details("Setup",layoutSetup);
		panelSetup.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

		this.add(panelSetup);

		comboAttore = new ComboBox<>();
		comboAttore.setItems(squadre);
		comboAttore.setPlaceholder("Seleziona attore");
		comboAttore.setItemLabelGenerator(p -> p.getDescAttore());
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
		txtPerc.setStepButtonsVisible(true);
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

		radioGroupVotiExcel = new RadioButtonGroup<>();
		radioGroupVotiExcel.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		radioGroupVotiExcel.setLabel("Voti Excel");
		radioGroupVotiExcel.setItems("mondiale-voti-ufficiali", "mondiale-voti-ufficiali-fantacalcio");
		radioGroupVotiExcel.setValue("mondiale-voti-ufficiali");

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
		layoutCalcola.getStyle().set("border", Costants.BORDER_COLOR);

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
		layoutNotifiche.getStyle().set("border", Costants.BORDER_COLOR);

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
			LOG.info("campionato " + campionato.getDescCampionato());

			FcGiornataInfo giornataInfo = null;
			int giornata = 0;
			if (!comboGiornata.isEmpty()) {
				giornataInfo = (FcGiornataInfo) comboGiornata.getValue();
				giornata = giornataInfo.getCodiceGiornata();
			}
			LOG.info("giornata " + giornata);

			String basePathData = (String) p.get("PATH_TMP");
			LOG.info("basePathData " + basePathData);
			File f = new File(basePathData);
			if (!f.exists()) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Impossibile trovare il percorso specificato " + basePathData);
				return;
			}

			if (event.getSource() == initDb) {

				List<FcAttore> attori = attoreController.findByActive(true);
				for (FcAttore a : attori) {
					if (a.isActive()) {
						for (int j = 1; j <= 23; j++) {
							formazioneController.createFormazione(a, campionato.getIdCampionato(), Integer.valueOf(j));
						}
						classificaTotalePuntiController.createEm(a, campionato, Double.valueOf(0));
					}
				}

				if (giornata == 0) {
					giornata = 1;
				}
				emjobProcessGiornata.eminitDb(giornata);

			} else if (event.getSource() == initDbAttore) {

				FcAttore attore = (FcAttore) comboAttore.getValue();
				LOG.info("attore " + attore.getDescAttore());

				for (int j = 1; j <= 23; j++) {
					formazioneController.createFormazione(attore, campionato.getIdCampionato(), Integer.valueOf(j));
				}
				classificaTotalePuntiController.createEm(attore, campionato, Double.valueOf(0));

			} else if (event.getSource() == ultimaFormazione) {

				FcAttore attore = (FcAttore) comboAttore.getValue();
				LOG.info("attore " + attore.getDescAttore());

				emjobProcessGiornata.eminserisciUltimaFormazione(attore.getIdAttore(), giornata);

			} else if (event.getSource() == init) {

				emjobProcessGiornata.eminitPagelle(giornata);

				try {
					sendMailInfoGiornata(giornataInfo);
				} catch (Exception e) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_MAIL_KO, e.getMessage());
				}

			} else if (event.getSource() == downloadQuotaz) {

				// **************************************
				// DOWNLOAD FILE QUOTAZIONI
				// **************************************
				String urlFanta = (String) p.get("URL_FANTA");
				String basePath = basePathData;
				String quotaz = "mondiale-giocatori-quotazioni-excel";
				// https://www.pianetafanta.it/mondiale-giocatori-quotazioni-excel.asp?giornata=0&Nome=&Squadre=&Ruolo=&Ruolo2=&Quota=&Quota1=
				String httpUrl = urlFanta + quotaz + ".asp?giornata=" + giornata;
				LOG.info("httpUrl " + httpUrl);
				String fileName = "Q_" + giornata;
				EmJobProcessFileCsv jobCsv = new EmJobProcessFileCsv();
				jobCsv.downloadCsv(httpUrl, basePath, fileName, 2);

			} else if (event.getSource() == updateGiocatori) {

				// **************************************
				// UPDATE GIOCATORI
				// **************************************
				String imgPath = basePathData;
				String fileName = "Q_" + giornata;
				fileName = basePathData + fileName + ".csv";
				boolean updateQuotazioni = chkUpdateQuotaz.getValue().booleanValue();
				String percentuale = "" + txtPerc.getValue().intValue();
				HashMap<Object, Object> map = emjobProcessGiornata.initDbGiocatori(Costants.HTTP_URL_IMG, imgPath, fileName, updateQuotazioni, percentuale);

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

				emjobProcessGiornata.emalgoritmo(giornataInfo.getCodiceGiornata(), campionato);
				emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == ricalcola) {

				emjobProcessGiornata.ricalcolaTotPunti(giornataInfo.getCodiceGiornata(), campionato);
				emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == calcolaStatistiche) {

				emjobProcessGiornata.emstatistiche(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == aggiornaFlagAttivoGiocatore) {

				emjobProcessGiornata.aggiornaFlagAttivoGiocatore(giornataInfo.getCodiceGiornata());

			} else if (event.getSource() == pdfAndMail) {

				String imgLog = (String) env.getProperty("img.logo");
				String pathImg = "images/";

				Resource resource = resourceLoader.getResource("classpath:reports/em/risultati.jasper");
				InputStream inputStream = resource.getInputStream();
				Map<String, Object> params = getMap(giornataInfo.getCodiceGiornata(), pathImg);
				Collection<RisultatoBean> collection = new ArrayList<RisultatoBean>();
				collection.add(new RisultatoBean("P","S1",Double.valueOf(6),Double.valueOf(6),Double.valueOf(6),Double.valueOf(6)));
				String destFileName1 = basePathData + giornataInfo.getDescGiornataFc() + ".pdf";
				FileOutputStream outputStream = new FileOutputStream(new File(destFileName1));
				// JasperRunManager.runReportToPdfStream(inputStream,
				// outputStream, params, new JRBeanCollectionDataSource(l));
				JasperReporUtils.runReportToPdfStream(inputStream, outputStream, params, collection);

				Resource resource2 = resourceLoader.getResource("classpath:reports/em/classifica.jasper");
				InputStream inputStream2 = resource2.getInputStream();
				Map<String, Object> params2 = new HashMap<String, Object>();
				params2.put("DIVISORE", "" + Costants.DIVISORE_10);
				params2.put("PATH_IMG", pathImg + imgLog);
				String destFileName2 = basePathData + "Classifica.pdf";
				FileOutputStream outputStream2 = new FileOutputStream(new File(destFileName2));
				Connection conn = jdbcTemplate.getDataSource().getConnection();
				// JasperRunManager.runReportToPdfStream(inputStream2,
				// outputStream2, params2, conn);
				JasperReporUtils.runReportToPdfStream(inputStream2, outputStream2, params2, conn);

				MailClient client = new MailClient(javaMailSender);
				String email_destinatario = "";

				if (this.chkSendMail.getValue()) {
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
				String[] att = new String[] { destFileName1, destFileName2 };
				String subject = "Risultati " + giornataInfo.getDescGiornataFc();
				if (chkUfficiali.getValue()) {
					subject += " - Ufficiali";
				} else {
					subject += " - Parziali";
				}

				String message = getBody();

				try {
					String from = (String) env.getProperty("spring.mail.username");
					client.sendMail(from, to, cc, bcc, subject, message, "text/html", "3", att);
				} catch (Exception e) {
					CustomMessageDialog.showMessageError(CustomMessageDialog.MSG_MAIL_KO);
				}
			} else if (event.getSource() == notifica) {

				MailClient client = new MailClient(javaMailSender);
				String email_destinatario = "";
				List<FcAttore> attori = attoreController.findByActive(true);
				for (FcAttore a : attori) {
					email_destinatario += a.getEmail() + ";";
				}
				String[] to = null;
				if (email_destinatario != null && !email_destinatario.equals("")) {
					to = Utils.tornaArrayString(email_destinatario, ";");
				}
				String[] cc = null;
				String[] bcc = null;

				String subject = "Avviso";
				String message = messaggio.getValue();

				try {
					String from = (String) env.getProperty("spring.mail.username");

					client.sendMail(from, to, cc, bcc, subject, message, "", "3", null);
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

		FcGiornataInfo giornataInfo = giornataInfoController.findByCodiceGiornata(Integer.valueOf(giornata));

		List<FcAttore> squadre = attoreController.findByActive(true);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("path_img", pathImg);
		parameters.put("titolo", giornataInfo.getDescGiornataFc());
		int conta = 1;
		for (FcAttore a : squadre) {

			List<FcGiornataDett> lGiocatori = giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(a, giornataInfo);
			FcClassificaTotPt totPunti = classificaTotalePuntiController.findByFcAttoreAndFcGiornataInfo(a, giornataInfo);

			int countD = 0;
			int countC = 0;
			int countA = 0;
			Collection<RisultatoBean> dm = new ArrayList<RisultatoBean>();
			for (FcGiornataDett gd : lGiocatori) {
				if ("S".equals(gd.getFlagAttivo())) {
					if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("D")) {
						countD++;
					} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("C")) {
						countC++;
					} else if (gd.getFcGiocatore().getFcRuolo().getIdRuolo().equals("A")) {
						countA++;
					}
				}

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

				dm.add(bean);

			}

			String schema = countD + "-" + countC + "-" + countA;
			LOG.info(schema);

			String puntiTotali = "";
			if (totPunti != null) {
				puntiTotali = formatter.format(totPunti.getTotPt().doubleValue() / Double.parseDouble("" + Costants.DIVISORE_10));
			}

			parameters.put("sq" + conta, a.getDescAttore());
			parameters.put("data" + conta, dm);
			parameters.put("ris" + conta, puntiTotali);
			parameters.put("dataInfo" + conta, null);

			conta++;
		}

		return parameters;

	}

	private void sendMailInfoGiornata(FcGiornataInfo ggInfo)
			throws AddressException, IOException, MessagingException,
			NamingException {

		String subject = "Avvio Giornata - " + ggInfo.getDescGiornataFc();
		LOG.info("subject " + subject);
		String formazioneHtml = "";
		formazioneHtml += "<html><head><title>FC</title></head>\n";
		formazioneHtml += "<body>\n";
		formazioneHtml += "<p>Prossima Giornata: " + ggInfo.getDescGiornataFc() + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>Data Giornata: " + Utils.formatLocalDateTime(ggInfo.getDataGiornata(), "dd/MM/yyyy HH:mm") + "</p>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<br>\n";
		formazioneHtml += "<p>Ciao Davide</p>\n";
		formazioneHtml += "</body>\n";
		formazioneHtml += "<html>";
		LOG.info("formazioneHtml " + formazioneHtml);
		Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
		p.setProperty("ACTIVE_MAIL", this.chkSendMail.getValue().toString());

		MailClient client = new MailClient(javaMailSender);
		String email_destinatario = "";
		String ACTIVE_MAIL = (String) p.getProperty("ACTIVE_MAIL");
		LOG.info("ACTIVE_MAIL " + ACTIVE_MAIL);
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

	private Grid<FcGiocatore> getTableGiocatori() {

		Grid<FcGiocatore> grid = new Grid<>();
		grid.setItems(new ArrayList<FcGiocatore>());
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
			if (g != null) {
				Label lblGiocatore = new Label(g.getCognGiocatore());
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
				if (sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Label lblSquadra = new Label(g.getFcSquadra().getNomeSquadra());
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		nomeSquadraColumn.setSortable(false);
		nomeSquadraColumn.setHeader("Squadra");
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