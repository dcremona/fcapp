package fcweb.ui.views.seriea;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.FormazioneJasper;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.FormazioneService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.JasperReporUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;



@PageTitle("Tutte le Rose")
@Route(value = "squadreAll", layout = MainLayout.class)
@RolesAllowed("USER")
public class SquadreAllView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private ResourceLoader resourceLoader;

	private List<FcAttore> squadre = new ArrayList<FcAttore>();

	@Autowired
	private AccessoService accessoController;
	
	public SquadreAllView() {
		LOG.info("SquadreAllView()");
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

		try {
			add(buildButtonRose(campionato));
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}

		final HorizontalLayout layoutSq = new HorizontalLayout();
		layoutSq.setMargin(false);
		layoutSq.setPadding(false);
		layoutSq.setSpacing(false);
		layoutSq.setSizeFull();

		final HorizontalLayout layoutSq2 = new HorizontalLayout();
		layoutSq2.setMargin(false);
		layoutSq2.setPadding(false);
		layoutSq2.setSpacing(false);
		layoutSq2.setSizeFull();

		final HorizontalLayout layoutSq3 = new HorizontalLayout();
		layoutSq3.setMargin(false);
		layoutSq3.setPadding(false);
		layoutSq3.setSpacing(false);
		layoutSq3.setSizeFull();

		final HorizontalLayout layoutSq4 = new HorizontalLayout();
		layoutSq4.setMargin(false);
		layoutSq4.setPadding(false);
		layoutSq4.setSpacing(false);
		layoutSq4.setSizeFull();

		for (FcAttore attore : squadre) {

			List<FcFormazione> listFormazione = formazioneController.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, true);
			Double somma = 0d;
			for (FcFormazione f : listFormazione) {
				if (f.getTotPagato() != null) {
					somma += f.getTotPagato();
				}
			}

			Grid<FcFormazione> tableFormazione = getTableFormazione(listFormazione, somma.intValue(), attore.getDescAttore());

			if (attore.getIdAttore() == 1 || attore.getIdAttore() == 2) {
				layoutSq.add(tableFormazione);
			} else if (attore.getIdAttore() == 3 || attore.getIdAttore() == 4) {
				layoutSq2.add(tableFormazione);
			} else if (attore.getIdAttore() == 5 || attore.getIdAttore() == 6) {
				layoutSq3.add(tableFormazione);
			} else if (attore.getIdAttore() == 7 || attore.getIdAttore() == 8) {
				layoutSq4.add(tableFormazione);
			}
		}

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.add(layoutSq);
		mainLayout.add(layoutSq2);
		mainLayout.add(layoutSq3);
		mainLayout.add(layoutSq4);

		add(mainLayout);

	}

	private FileDownloadWrapper buildButtonRose(FcCampionato campionato) {

		Button stampapdfRose = new Button("Tutte le Rose pdf");
		stampapdfRose.setIcon(VaadinIcon.DOWNLOAD.create());
		FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(new StreamResource("RoseFcAll.pdf",() -> {
			try {
				Map<String, Object> hm = getMapRoseFcAll(campionato);
				hm.put("titolo", "Rose Fc");
				Collection<FormazioneJasper> collection = new ArrayList<FormazioneJasper>();
				collection.add(new FormazioneJasper("P","G","Sq",0,0));
				Resource resource = resourceLoader.getResource("classpath:reports/roseFcAll.jasper");
				InputStream inputStream = resource.getInputStream();
				return JasperReporUtils.runReportToPdf(inputStream, hm, collection);
			} catch (Exception ex2) {
				LOG.error(ex2.toString());
			}
			return null;
		}));
		buttonWrapper.wrapComponent(stampapdfRose);

		return buttonWrapper;
	}

	private Grid<FcFormazione> getTableFormazione(List<FcFormazione> items,
			Integer somma, String attore) {

		Grid<FcFormazione> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);

		Column<FcFormazione> ruoloColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (f != null && f.getFcGiocatore() != null && !StringUtils.isEmpty(f.getFcGiocatore().getFcRuolo().getIdRuolo())) {
				Image img = buildImage("classpath:images/", f.getFcGiocatore().getFcRuolo().getIdRuolo().toLowerCase() + ".png");
				cellLayout.add(img);
			}
			return cellLayout;
		}));
		ruoloColumn.setSortable(true);
		ruoloColumn.setHeader("R");
		ruoloColumn.setAutoWidth(true);

		Column<FcFormazione> cognGiocatoreColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
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
		cognGiocatoreColumn.setAutoWidth(true);

		Column<FcFormazione> nomeSquadraColumn = grid.addColumn(new ComponentRenderer<>(f -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (f != null && f.getFcGiocatore() != null && f.getFcGiocatore().getFcSquadra() != null) {
//				Image img = buildImage("classpath:/img/squadre/", f.getFcGiocatore().getFcSquadra().getNomeSquadra() + ".png");
//				cellLayout.add(img);
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

		Column<FcFormazione> quotazioneColumn = grid.addColumn(formazione -> formazione.getFcGiocatore() != null ? formazione.getFcGiocatore().getQuotazione() : 0);
		quotazioneColumn.setSortable(true);
		quotazioneColumn.setHeader("Q");
		quotazioneColumn.setAutoWidth(true);

		Column<FcFormazione> totPagatoColumn = grid.addColumn(formazione -> formazione.getFcGiocatore() != null ? formazione.getTotPagato().intValue() : 0);
		totPagatoColumn.setSortable(true);
		totPagatoColumn.setHeader("P");
		totPagatoColumn.setAutoWidth(true);

		HeaderRow topRow = grid.prependHeaderRow();
		HeaderCell informationCell = topRow.join(ruoloColumn, cognGiocatoreColumn, nomeSquadraColumn, mediaVotoColumn, quotazioneColumn, totPagatoColumn);
		Div lblTitle = new Div();
		lblTitle.setText(attore);
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

	private Map<String, Object> getMapRoseFcAll(FcCampionato campionato) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (FcAttore attore : squadre) {
			Collection<FormazioneJasper> lSq = new ArrayList<FormazioneJasper>();
			List<FcFormazione> listFormazione = formazioneController.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore, true);
			Double somma = Double.valueOf(0);
			FormazioneJasper fj = null;
			for (FcFormazione f : listFormazione) {
				if (f.getFcGiocatore() != null && f.getFcGiocatore().getFcRuolo() != null && f.getFcGiocatore().getFcSquadra() != null) {
					somma += f.getTotPagato();
					fj = new FormazioneJasper(f.getFcGiocatore().getFcRuolo().getIdRuolo(),f.getFcGiocatore().getCognGiocatore(),f.getFcGiocatore().getFcSquadra().getNomeSquadra(),f.getFcGiocatore().getQuotazione(),f.getTotPagato().intValue());
				} else {
					fj = new FormazioneJasper("","","",0,0);
				}
				lSq.add(fj);
			}
			fj = new FormazioneJasper("","","Totale",0,somma.intValue());
			lSq.add(fj);

			parameters.put("data" + attore.getIdAttore(), lSq);
			parameters.put("sq" + attore.getIdAttore(), attore.getDescAttore());
			parameters.put("tot" + attore.getIdAttore(), somma.toString());
		}
		return parameters;
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