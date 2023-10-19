package fcweb.ui.views.seriea;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.MercatoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Rose")
@Route(value = "squadre", layout = MainLayout.class)
@RolesAllowed("USER")
public class SquadreView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private MercatoService mercatoController;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResourceLoader resourceLoader;

	private List<FcAttore> squadre = new ArrayList<FcAttore>();

	@Autowired
	private AccessoService accessoController;

	public SquadreView() {
		LOG.info("SquadreView()");
	}

	@PostConstruct
	void init() {
		LOG.info("init");

		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());

		initData();
		initLayout();
	}

	private void initData() {
		squadre = attoreController.findByActive(true);
	}

	private void initLayout() {

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcGiornataInfo giornataInfo = (FcGiornataInfo) VaadinSession.getCurrent().getAttribute("GIORNATA_INFO");

//		VerticalLayout container = new VerticalLayout();
//		PagedTabs tabs = new PagedTabs(container);
		TabSheet tabSheet = new TabSheet();
		for (FcAttore attore : squadre) {

			if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {

				final HorizontalLayout layoutBtn = new HorizontalLayout();

				try {
					layoutBtn.add(buildButtonRosa(campionato, attore));
				} catch (Exception e) {
					LOG.error(e.getMessage());
					e.printStackTrace();
				}

				try {
					layoutBtn.add(buildButtonVotiRosa(campionato, attore, giornataInfo));
				} catch (Exception e) {
					LOG.error(e.getMessage());
					e.printStackTrace();
				}

				List<FcFormazione> listFormazione = formazioneController.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, true);
				Double somma = 0d;
				for (FcFormazione f : listFormazione) {
					if (f.getTotPagato() != null) {
						somma += f.getTotPagato();
					}
				}

				Integer from = campionato.getStart();
				Integer to = campionato.getEnd();
				FcGiornataInfo start = new FcGiornataInfo();
				start.setCodiceGiornata(from);
				FcGiornataInfo end = new FcGiornataInfo();
				end.setCodiceGiornata(to);

				List<FcMercatoDett> listMercato = mercatoController.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(start, end, attore);

				Grid<FcFormazione> tableFormazione = getTableFormazione(listFormazione, somma.intValue());
				Grid<FcMercatoDett> tableMercato = getTableMercato(listMercato);

				final VerticalLayout layout = new VerticalLayout();
				layout.setMargin(false);
				layout.setPadding(false);
				layout.setSpacing(false);
				layout.add(layoutBtn);
				layout.add(tableFormazione);
				layout.add(tableMercato);

//				tabs.add(attore.getDescAttore(), layout, false);
				tabSheet.add(attore.getDescAttore(), layout);
			}
		}
//		this.add(tabs, container);
		this.add(tabSheet);
	}

	private FileDownloadWrapper buildButtonRosa(FcCampionato campionato,
			FcAttore attore) {

		String idAttore = "" + attore.getIdAttore();
		String descAttore = attore.getDescAttore();

		Button stampaPdfRosa = new Button("Rosa pdf");
		stampaPdfRosa.setIcon(VaadinIcon.DOWNLOAD.create());
		FileDownloadWrapper button1Wrapper = new FileDownloadWrapper(new StreamResource("Rosa_" + descAttore + ".pdf",() -> {
			try {
				Connection conn = jdbcTemplate.getDataSource().getConnection();
				Map<String, Object> hm = new HashMap<String, Object>();
				hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
				hm.put("ATTORE", idAttore);
				hm.put("DIVISORE", "" + Costants.DIVISORE_100);
				hm.put("PATH_IMG", "img/");
				Resource resource = resourceLoader.getResource("classpath:reports/roseFc.jasper");
				InputStream inputStream = resource.getInputStream();
				return JasperReporUtils.runReportToPdf(inputStream, hm, conn);
			} catch (Exception ex2) {
				LOG.error(ex2.toString());
			}
			return null;
		}));
		button1Wrapper.wrapComponent(stampaPdfRosa);

		return button1Wrapper;
	}

	private FileDownloadWrapper buildButtonVotiRosa(FcCampionato campionato,
			FcAttore attore, FcGiornataInfo giornataInfo) {

		String idAttore = "" + attore.getIdAttore();
		String descAttore = attore.getDescAttore();

		Button stampaVotiRosa = new Button("Voti Rosa pdf");
		stampaVotiRosa.setIcon(VaadinIcon.DOWNLOAD.create());
		FileDownloadWrapper button2Wrapper = new FileDownloadWrapper(new StreamResource("Voti_Rosa_" + descAttore + ".pdf",() -> {
			try {
				String START = campionato.getStart().toString();
				String CURRENT_GIORNATA = "" + giornataInfo.getCodiceGiornata();
				LOG.info("START " + START);
				LOG.info("END " + CURRENT_GIORNATA);
				LOG.info("ID_ATTORE " + idAttore);
				Connection conn = jdbcTemplate.getDataSource().getConnection();
				final Map<String, Object> hm = new HashMap<String, Object>();
				hm.put("ID_CAMPIONATO", "" + campionato.getIdCampionato());
				hm.put("START", START);
				hm.put("END", CURRENT_GIORNATA);
				hm.put("ID_ATTORE", idAttore);
				hm.put("DIVISORE", "" + Costants.DIVISORE_100);
				final Resource resource = resourceLoader.getResource("classpath:reports/statistica.jasper");
				final InputStream inputStream = resource.getInputStream();
				return JasperReporUtils.runReportToPdf(inputStream, hm, conn);
			} catch (Exception ex2) {
			}
			return null;
		}));
		button2Wrapper.wrapComponent(stampaVotiRosa);

		return button2Wrapper;
	}

	private Grid<FcFormazione> getTableFormazione(List<FcFormazione> items,
			Integer somma) {

		Grid<FcFormazione> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		// grid.setSizeFull();

		Column<FcFormazione> ruoloColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			// cellLayout.setMargin(false);
			// cellLayout.setPadding(false);
			// cellLayout.setSpacing(false);
			// cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (f != null && f.getFcGiocatore() != null && !StringUtils.isEmpty(f.getFcGiocatore().getFcRuolo().getIdRuolo())) {
				Image img = buildImage("classpath:images/", f.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("R");
		// ruoloColumn.setWidth("50px");
		ruoloColumn.setAutoWidth(true);

		Column<FcFormazione> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(f -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			// cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();

			if (f != null && f.getFcGiocatore() != null && !StringUtils.isEmpty(f.getFcGiocatore().getNomeImg())) {

				if (f.getFcGiocatore().getImgSmall() != null) {
					StreamResource resource = new StreamResource(f.getFcGiocatore().getNomeImg(),() -> {
						InputStream inputStream = null;
						try {
							inputStream = f.getFcGiocatore().getImgSmall().getBinaryStream();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return inputStream;
					});
					Image img = new Image(resource,"");
					img.setSrc(resource);
					cellLayout.add(img);
				}
				Span lblGiocatore = new Span(f.getFcGiocatore().getCognGiocatore());
				cellLayout.add(lblGiocatore);
			}
			return cellLayout;
		}));
		cognGiocatoreColumn.setSortable(false);
		cognGiocatoreColumn.setHeader("Giocatore");
		// cognGiocatoreColumn.setWidth("150px");
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcFormazione> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(f -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			// cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (f != null && f.getFcGiocatore() != null && f.getFcGiocatore().getFcSquadra() != null) {
				// Image img = buildImage("classpath:/img/squadre/",
				// f.getFcGiocatore().getFcSquadra().getNomeSquadra() + ".png");
				// cellLayout.add(img);
				FcSquadra sq = f.getFcGiocatore().getFcSquadra();
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				Span lblSquadra = new Span(f.getFcGiocatore().getFcSquadra().getNomeSquadra());
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		nomeSquadraColumn.setSortable(true);
		nomeSquadraColumn.setComparator((p1,
				p2) -> p1.getFcGiocatore().getFcSquadra().getNomeSquadra().compareTo(p2.getFcGiocatore().getFcSquadra().getNomeSquadra()));
		nomeSquadraColumn.setHeader("Squadra");
		// nomeSquadraColumn.setWidth("100px");
		nomeSquadraColumn.setAutoWidth(true);

		Column<FcFormazione> mediaVotoColumn = grid.addColumn(new ComponentRenderer<>(f -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			if (f != null && f.getFcGiocatore() != null) {

				FcGiocatore g = f.getFcGiocatore();
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

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getMediaVoto() / Costants.DIVISORE_100;
				}
				String sTotPunti = myFormatter.format(d);
				Span lbl = new Span(sTotPunti);

				cellLayout.add(img);
				cellLayout.add(lbl);

			}
			return cellLayout;
		}));
		mediaVotoColumn.setSortable(true);
		mediaVotoColumn.setComparator((p1,
				p2) -> p1.getFcGiocatore().getFcStatistiche().getMediaVoto().compareTo(p2.getFcGiocatore().getFcStatistiche().getMediaVoto()));
		mediaVotoColumn.setHeader("Mv");
		mediaVotoColumn.setAutoWidth(true);
		mediaVotoColumn.setKey("fcStatistiche.mediaVoto");

		Column<FcFormazione> fmVotoColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (f != null && f.getFcGiocatore() != null) {

				FcGiocatore g = f.getFcGiocatore();
				FcStatistiche s = g.getFcStatistiche();

				String imgThink = "2.png";
				if (s != null && s.getFantaMedia() != 0) {
					if (s.getFantaMedia() > Costants.RANGE_MAX_MV) {
						imgThink = "1.png";
					} else if (s.getFantaMedia() < Costants.RANGE_MIN_MV) {
						imgThink = "3.png";
					}
				}
				Image img = buildImage("classpath:images/", imgThink);

				DecimalFormat myFormatter = new DecimalFormat("#0.00");
				Double d = Double.valueOf(0);
				if (s != null) {
					d = s.getFantaMedia() / Costants.DIVISORE_100;
				}
				String sTotPunti = myFormatter.format(d);
				Span lbl = new Span(sTotPunti);

				cellLayout.add(img);
				cellLayout.add(lbl);

			}
			return cellLayout;
		}));
		fmVotoColumn.setSortable(true);
		fmVotoColumn.setComparator((p1,
				p2) -> p1.getFcGiocatore().getFcStatistiche().getFantaMedia().compareTo(p2.getFcGiocatore().getFcStatistiche().getFantaMedia()));
		fmVotoColumn.setHeader("FMv");
		fmVotoColumn.setAutoWidth(true);
		fmVotoColumn.setKey("fcStatistiche.fantaMedia");

		Column<FcFormazione> quotazioneColumn = grid.addColumn(formazione -> formazione.getFcGiocatore() != null ? formazione.getFcGiocatore().getQuotazione() : 0);
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		// quotazioneColumn.setWidth("60px");
		quotazioneColumn.setAutoWidth(true);

		Column<FcFormazione> totPagatoColumn = grid.addColumn(formazione -> formazione.getFcGiocatore() != null ? formazione.getTotPagato().intValue() : 0);
		totPagatoColumn.setSortable(true);
		totPagatoColumn.setHeader("P");
		// totPagatoColumn.setWidth("60px");
		totPagatoColumn.setAutoWidth(true);

		HeaderRow topRow = grid.prependHeaderRow();
		HeaderCell informationCell = topRow.join(ruoloColumn, cognGiocatoreColumn, nomeSquadraColumn, mediaVotoColumn, fmVotoColumn, quotazioneColumn, totPagatoColumn);
		Div lblTitle = new Div();
		lblTitle.setText("Rosa Ufficiale");
		lblTitle.getStyle().set("font-size", "16px");
		lblTitle.getStyle().set("background", Costants.LIGHT_BLUE);
		informationCell.setComponent(lblTitle);

		FooterRow footerRow = grid.appendFooterRow();
		Div lblCreditiSpesi0 = new Div();
		lblCreditiSpesi0.setText("Totale");
		lblCreditiSpesi0.getStyle().set("font-size", "20px");
		lblCreditiSpesi0.getStyle().set("background", Costants.LIGHT_GRAY);
		Div lblCreditiSpesi1 = new Div();
		lblCreditiSpesi1.setText("" + somma);
		lblCreditiSpesi1.getStyle().set("font-size", "20px");
		lblCreditiSpesi1.getStyle().set("background", Costants.LIGHT_GRAY);
		footerRow.getCell(quotazioneColumn).setComponent(lblCreditiSpesi0);
		footerRow.getCell(totPagatoColumn).setComponent(lblCreditiSpesi1);

		return grid;

	}

	private Grid<FcMercatoDett> getTableMercato(List<FcMercatoDett> items) {

		Grid<FcMercatoDett> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);

		Column<FcMercatoDett> giornataColumn = grid.addColumn(mercato -> mercato.getFcGiornataInfo().getCodiceGiornata());
		giornataColumn.setSortable(false);
		giornataColumn.setHeader("Giornata");
		giornataColumn.setAutoWidth(true);

		Column<FcMercatoDett> dataCambioColumn = grid.addColumn(new LocalDateTimeRenderer<>(FcMercatoDett::getDataCambio,() -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)));
		dataCambioColumn.setSortable(false);
		dataCambioColumn.setHeader("Data");
		dataCambioColumn.setAutoWidth(true);

		Column<FcMercatoDett> ruoloAcqColumn = grid.addColumn(new ComponentRenderer<>(m -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (m != null && m.getFcGiocatoreByIdGiocAcq() != null) {
				Image imgR = buildImage("classpath:images/", m.getFcGiocatoreByIdGiocAcq().getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(imgR);
			}
			return cellLayout;
		}));
		ruoloAcqColumn.setHeader("");
		ruoloAcqColumn.setAutoWidth(true);

		Column<FcMercatoDett> gAcqColumn = grid.addColumn(new ComponentRenderer<>(m -> {
			FlexLayout cellLayout = new FlexLayout();
			if (m != null && m.getFcGiocatoreByIdGiocAcq() != null) {

				if (m.getFcGiocatoreByIdGiocAcq().getImgSmall() != null) {
					StreamResource resource = new StreamResource(m.getFcGiocatoreByIdGiocAcq().getNomeImg(),() -> {
						InputStream inputStream = null;
						try {
							inputStream = m.getFcGiocatoreByIdGiocAcq().getImgSmall().getBinaryStream();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return inputStream;
					});
					Image img = new Image(resource,"");
					cellLayout.add(img);
					img.setSrc(resource);
				}

				Span lblGiocatore = new Span(m.getFcGiocatoreByIdGiocAcq().getCognGiocatore());
				cellLayout.add(lblGiocatore);

				Span lblSquadra = new Span(" (" + m.getFcGiocatoreByIdGiocAcq().getFcSquadra().getNomeSquadra().substring(0, 3) + ")");
				lblSquadra.getStyle().set("font-size", "10px");
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		gAcqColumn.setSortable(false);
		gAcqColumn.setHeader("Acquisti");
		gAcqColumn.setAutoWidth(true);

		Column<FcMercatoDett> ruoloVenColumn = grid.addColumn(new ComponentRenderer<>(m -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (m != null && m.getFcGiocatoreByIdGiocVen() != null) {
				Image imgR = buildImage("classpath:images/", m.getFcGiocatoreByIdGiocVen().getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(imgR);
			}
			return cellLayout;
		}));
		ruoloVenColumn.setHeader("");
		ruoloVenColumn.setAutoWidth(true);

		Column<FcMercatoDett> gVenColumn = grid.addColumn(new ComponentRenderer<>(m -> {

			FlexLayout cellLayout = new FlexLayout();
			// cellLayout.setMargin(false);
			// cellLayout.setPadding(false);
			// cellLayout.setSpacing(false);
			// cellLayout.setAlignItems(Alignment.STRETCH);
			// cellLayout.setSizeFull();
			if (m != null && m.getFcGiocatoreByIdGiocVen() != null) {

				if (m.getFcGiocatoreByIdGiocVen().getImgSmall() != null) {
					StreamResource resource = new StreamResource(m.getFcGiocatoreByIdGiocVen().getNomeImg(),() -> {
						InputStream inputStream = null;
						try {
							inputStream = m.getFcGiocatoreByIdGiocVen().getImgSmall().getBinaryStream();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return inputStream;
					});
					Image img = new Image(resource,"");
					img.setSrc(resource);
					cellLayout.add(img);
				}

				Span lblGiocatore = new Span(m.getFcGiocatoreByIdGiocVen().getCognGiocatore());
				cellLayout.add(lblGiocatore);

				Span lblSquadra = new Span(" (" + m.getFcGiocatoreByIdGiocVen().getFcSquadra().getNomeSquadra().substring(0, 3) + ")");
				lblSquadra.getStyle().set("font-size", "10px");
				cellLayout.add(lblSquadra);
			}
			return cellLayout;
		}));
		gVenColumn.setSortable(false);
		gVenColumn.setHeader("Cessioni");
		gVenColumn.setAutoWidth(true);

		Column<FcMercatoDett> notaColumn = grid.addColumn(mercato -> mercato.getNota());
		notaColumn.setSortable(false);
		notaColumn.setHeader("Nota");
		notaColumn.setAutoWidth(true);

		HeaderRow topRow = grid.prependHeaderRow();

		HeaderCell informationCell = topRow.join(giornataColumn, dataCambioColumn, ruoloAcqColumn, gAcqColumn, ruoloVenColumn, gVenColumn, notaColumn);
		Div lblTitle = new Div();
		lblTitle.setText("Cambi Rosa");
		lblTitle.getStyle().set("font-size", "16px");
		lblTitle.getStyle().set("background", Costants.LIGHT_BLUE);
		informationCell.setComponent(lblTitle);

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