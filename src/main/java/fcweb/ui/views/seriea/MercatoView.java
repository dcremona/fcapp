package fcweb.ui.views.seriea;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.ClassificaService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.SquadraService;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "mercato")
@PageTitle("Mercato")
@RolesAllowed("ADMIN")
public class MercatoView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{
	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private static final String widthG = "240px";
	private static final String widthc1 = "150px";
	private static final String widthc2 = "90px";
	private static final String widthI = "205px";

	private String idCampionato = null;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Button randomSaveButton;
	private Button saveButton;
	private Span lblError;

	private Span[] lblAttore;
	private Grid<FcFormazione>[] tablePlayer;
	private Span[] lblCreditoPlayer;
	private Span[] lblTotPagatoPlayer;
	private Span[] lblResiduoPlayer;
	private Span[] lblRuoliPlayer;
	private Grid<FcProperties>[] tableContaPlayer;

	public List<FcAttore> squadre = new ArrayList<FcAttore>();
	public List<FcGiocatore> giocatori = new ArrayList<FcGiocatore>();
	public List<FcClassifica> creditiFm = new ArrayList<FcClassifica>();

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private SquadraService squadraController;

	public MercatoView() {
		LOG.info("MercatoView");
	}

	public void randomFormazioni() {
		Random rand = new Random();

		List<Integer> p = new ArrayList<Integer>();
		List<Integer> d = new ArrayList<Integer>();
		List<Integer> c = new ArrayList<Integer>();
		List<Integer> a = new ArrayList<Integer>();

		for (FcGiocatore g : giocatori) {

			if (g.getQuotazione() < 5) {
				continue;
			}

			if ("P".equals(g.getFcRuolo().getIdRuolo())) {
				p.add(g.getIdGiocatore());
			} else if ("D".equals(g.getFcRuolo().getIdRuolo())) {
				d.add(g.getIdGiocatore());
			} else if ("C".equals(g.getFcRuolo().getIdRuolo())) {
				c.add(g.getIdGiocatore());
			} else if ("A".equals(g.getFcRuolo().getIdRuolo())) {
				a.add(g.getIdGiocatore());
			}
		}

		for (FcAttore attore : squadre) {

			List<Integer> list = new ArrayList<Integer>();

			int numberOfElementsP = 1;
			while (numberOfElementsP <= 3) {
				int randomIndex = rand.nextInt(p.size());
				Integer randomElement = p.get(randomIndex);
				if (list.indexOf(randomElement) == -1) {
					list.add(randomElement);
					numberOfElementsP++;
				}
			}

			int numberOfElementsD = 1;
			while (numberOfElementsD <= 8) {
				int randomIndex = rand.nextInt(d.size());
				Integer randomElement = d.get(randomIndex);
				if (list.indexOf(randomElement) == -1) {
					list.add(randomElement);
					numberOfElementsD++;
				}
			}

			int numberOfElementsC = 1;
			while (numberOfElementsC <= 8) {
				int randomIndex = rand.nextInt(c.size());
				Integer randomElement = c.get(randomIndex);
				if (list.indexOf(randomElement) == -1) {
					list.add(randomElement);
					numberOfElementsC++;
				}
			}

			int numberOfElementsA = 1;
			while (numberOfElementsA <= 6) {
				int randomIndex = rand.nextInt(a.size());
				Integer randomElement = a.get(randomIndex);
				if (list.indexOf(randomElement) == -1) {
					list.add(randomElement);
					numberOfElementsA++;
				}
			}

			int ordinamento = 1;
			for (Integer id : list) {
				String update = "";
				update += "UPDATE fc_formazione SET";
				update += " ID_GIOCATORE=" + id.toString() + ",";
				update += " TOT_PAGATO=1";
				update += " WHERE ID_CAMPIONATO = " + idCampionato;
				update += " AND ID_ATTORE = " + attore.getIdAttore();
				update += " AND ORDINAMENTO = " + ordinamento;
				jdbcTemplate.update(update);
				ordinamento++;
			}
		}
	}

	@PostConstruct
	void init() {
		LOG.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initData();
		if (giocatori.size() > 0) {
			initLayout();
		}
	}

	private void initData() {
		LOG.info("initData");
		squadre = attoreController.findByActive(true);
		giocatori = giocatoreController.findAll();
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		creditiFm = classificaController.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);
	}

	@SuppressWarnings("unchecked")
	private void initLayout() {

		try {

			Button button = new Button("Home");
			RouterLink menuHome = new RouterLink("",HomeView.class);
			menuHome.getElement().appendChild(button.getElement());

			Button button2 = new Button("FreePlayers");
			RouterLink menuFreePlayers = new RouterLink("",FreePlayersView.class);
			menuFreePlayers.getElement().appendChild(button2.getElement());

			saveButton = new Button("Save");
			saveButton.addClickListener(this);

			randomSaveButton = new Button("Random Save");
			randomSaveButton.addClickListener(this);
			randomSaveButton.setVisible(false);

			lblError = new Span();
			lblError.getStyle().set("border", Costants.BORDER_COLOR);
			lblError.getStyle().set("background", "#EC7063");
			lblError.setVisible(false);

			HorizontalLayout layoutButton = new HorizontalLayout();
			layoutButton.getStyle().set("border", Costants.BORDER_COLOR);
			layoutButton.setSpacing(true);
			layoutButton.add(menuHome);
			layoutButton.add(menuFreePlayers);
			layoutButton.add(saveButton);
			layoutButton.add(randomSaveButton);
			layoutButton.add(lblError);

			this.add(layoutButton);

			HorizontalLayout layout0 = new HorizontalLayout();
			layout0.setMargin(false);
			layout0.setSpacing(false);
			HorizontalLayout layout1 = new HorizontalLayout();
			layout1.setMargin(false);
			layout1.setSpacing(false);
			HorizontalLayout layout2 = new HorizontalLayout();
			layout2.setMargin(false);
			layout2.setSpacing(false);
			HorizontalLayout layout3 = new HorizontalLayout();
			layout3.setMargin(false);
			layout3.setSpacing(false);

			lblAttore = new Span[squadre.size()];
			tablePlayer = new Grid[squadre.size()];
			lblRuoliPlayer = new Span[squadre.size()];
			lblCreditoPlayer = new Span[squadre.size()];
			lblTotPagatoPlayer = new Span[squadre.size()];
			lblResiduoPlayer = new Span[squadre.size()];
			tableContaPlayer = new Grid[squadre.size()];

			int att = 0;
			for (FcAttore a : squadre) {
				VerticalLayout layoutHeaderInfo = new VerticalLayout();
				layoutHeaderInfo.setMargin(false);
				layoutHeaderInfo.setSpacing(false);

				lblAttore[att] = new Span(a.getDescAttore());
				lblAttore[att].setWidth(widthI);
				lblAttore[att].getStyle().set("border", Costants.BORDER_COLOR);
				lblAttore[att].getStyle().set("background", "#D2E6F0");
				layoutHeaderInfo.add(lblAttore[att]);

				layout0.add(layoutHeaderInfo);

				tablePlayer[att] = buildTable(a);
				layout1.add(tablePlayer[att]);

				VerticalLayout layoutInfo = new VerticalLayout();
				layoutInfo.setMargin(false);
				layoutInfo.setSpacing(false);

				lblCreditoPlayer[att] = new Span("Credito");
				lblCreditoPlayer[att].setWidth(widthI);
				lblCreditoPlayer[att].getStyle().set("border", Costants.BORDER_COLOR);
				lblCreditoPlayer[att].getStyle().set("background", "#F5E37F");
				layoutInfo.add(lblCreditoPlayer[att]);

				lblTotPagatoPlayer[att] = new Span("Pagato");
				lblTotPagatoPlayer[att].setWidth(widthI);
				lblTotPagatoPlayer[att].getStyle().set("border", Costants.BORDER_COLOR);
				lblTotPagatoPlayer[att].getStyle().set("background", "#D7DBDD");
				layoutInfo.add(lblTotPagatoPlayer[att]);

				lblResiduoPlayer[att] = new Span("Residuo");
				lblResiduoPlayer[att].setWidth(widthI);
				lblResiduoPlayer[att].getStyle().set("border", Costants.BORDER_COLOR);
				lblResiduoPlayer[att].getStyle().set("background", "#ABEBC6");
				layoutInfo.add(lblResiduoPlayer[att]);

				lblRuoliPlayer[att] = new Span("P D C A");
				lblRuoliPlayer[att].getStyle().set("border", Costants.BORDER_COLOR);
				lblRuoliPlayer[att].getStyle().set("background", "#AED6F1");
				lblRuoliPlayer[att].setWidth(widthI);
				layoutInfo.add(lblRuoliPlayer[att]);

				layout2.add(layoutInfo);

				tableContaPlayer[att] = buildTableContaPlayer();

				layout3.add(tableContaPlayer[att]);

				att++;
			}

			this.add(layout0);
			this.add(layout1);
			this.add(layout2);
			this.add(layout3);

			updateInfoAttore();

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

		try {
			if (event.getSource() == randomSaveButton) {
				randomFormazioni();
				CustomMessageDialog.showMessageInfo("Formazioni aggiornate con successo!");
			} else if (event.getSource() == saveButton) {
				int att = 0;
				for (FcAttore a : squadre) {

					if (a.isActive()) {
						List<FcFormazione> data = tablePlayer[att].getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
						for (FcFormazione f : data) {
							FcGiocatore bean = f.getFcGiocatore();
							String ordinamento = "" + f.getId().getOrdinamento();

							String update = "";
							if (bean != null && f.getTotPagato() != null) {
								String valoreIdGiocatore = "" + bean.getIdGiocatore();
								String valorePagato = f.getTotPagato().toString();

								update += "\n UPDATE fc_formazione SET";
								update += " ID_GIOCATORE=" + valoreIdGiocatore + ",";
								update += " TOT_PAGATO=" + valorePagato;
								update += " WHERE ID_CAMPIONATO = " + idCampionato;
								update += " AND ID_ATTORE = " + a.getIdAttore();
								update += " AND ORDINAMENTO = " + ordinamento;
							} else {
								update += "\n UPDATE fc_formazione SET";
								update += " ID_GIOCATORE=null,";
								update += " TOT_PAGATO=null";
								update += " WHERE ID_CAMPIONATO = " + idCampionato;
								update += " AND ID_ATTORE = " + a.getIdAttore();
								update += " AND ORDINAMENTO = " + ordinamento;
							}
							jdbcTemplate.update(update);
						}
					}
					att++;
				}
				// updateInfoAttore();
				CustomMessageDialog.showMessageInfo("Formazioni aggiornate con successo!");
			}
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private void updateInfoAttore() throws Exception {

		LOG.info("START updateInfoAttore");

		int att = 1;
		String descError = "";
		for (int i = 0; i < tablePlayer.length; i++) {

			int countP = 0;
			int countD = 0;
			int countC = 0;
			int countA = 0;

			HashMap<String, String> map = new HashMap<String, String>();
			List<FcFormazione> data = tablePlayer[i].getDataProvider().fetch(new Query<>()).collect(Collectors.toList());

			Double totCrediti = null;
			for (FcClassifica fc : creditiFm) {
				if (fc.getFcAttore().getIdAttore() == att) {
					totCrediti = Double.valueOf(500 + fc.getTotFm());
				}
			}

			Double somma = Double.valueOf(0);
			String descAttore = "";
			for (FcFormazione f : data) {
				FcGiocatore bean = f.getFcGiocatore();
				descAttore = "[" + f.getFcAttore().getDescAttore() + "]";
				if (bean != null && f.getTotPagato() != null) {
					somma += f.getTotPagato();
					if (bean.getFcRuolo().getIdRuolo().equals("P")) {
						countP++;
					} else if (bean.getFcRuolo().getIdRuolo().equals("D")) {
						countD++;
					} else if (bean.getFcRuolo().getIdRuolo().equals("C")) {
						countC++;
					} else if (bean.getFcRuolo().getIdRuolo().equals("A")) {
						countA++;
					}
					refreshContaGiocatori(map, bean.getFcSquadra().getNomeSquadra());
				}
			}
			Double residuo = totCrediti - somma;

			List<FcProperties> list = new ArrayList<FcProperties>();
			if (!map.isEmpty()) {
				Iterator<?> it = map.entrySet().iterator();
				while (it.hasNext()) {
					@SuppressWarnings("rawtypes")
					Map.Entry pairs = (Map.Entry) it.next();
					FcProperties p = new FcProperties();
					p.setKey((String) pairs.getKey());
					p.setValue((String) pairs.getValue());

					if (Integer.parseInt((String) pairs.getValue()) > 5) {

						// ConfirmDialog.createInfo().withCaption("Messaggio di
						// avviso").withMessage(att + "\nTroppi giocatori per la
						// squadra " +
						// (String)pairs.getKey()).withOkButton().open();

						String sq = (String) pairs.getKey();
						int countPSq = 0;
						for (FcFormazione f : data) {
							FcGiocatore bean = f.getFcGiocatore();
							if (bean != null && f.getTotPagato() != null) {
								if (bean.getFcRuolo().getIdRuolo().equals("P")) {
									if (sq.equals(bean.getFcSquadra().getNomeSquadra())) {
										countPSq++;
									}
								}
							}
						}
						int maxG = Integer.parseInt((String) pairs.getValue()) - countPSq;
						if (maxG > 5) {
							descError += descAttore + " Troppi giocatori per la squadra " + sq + " - ";
						}

					}
					list.add(p);
				}
			}

			list.sort((p1,
					p2) -> p2.getValue().compareToIgnoreCase(p1.getValue()));
			tableContaPlayer[i].setItems(list);
			tableContaPlayer[i].getDataProvider().refreshAll();

			lblCreditoPlayer[i].setText("Credito  = " + totCrediti);
			lblTotPagatoPlayer[i].setText("Pagato   = " + somma);
			lblResiduoPlayer[i].setText("Residuo  = " + residuo);
			lblRuoliPlayer[i].setText("P=" + countP + " D=" + countD + " C=" + countC + " A=" + countA);

			lblResiduoPlayer[i].getStyle().set("background", "#ABEBC6");
			if (residuo < 0) {
				lblResiduoPlayer[i].getStyle().set("background", "#EC7063");
				// ConfirmDialog.createInfo().withCaption("Messaggio di
				// errore").withMessage(descAttore + " Residuo minore di
				// 0").withOkButton().open();
				descError += descAttore + " Residuo minore di 0 - ";
			}

			att++;
		}

		saveButton.setEnabled(true);
		lblError.setVisible(false);
		if (StringUtils.isNotEmpty(descError)) {
			saveButton.setEnabled(false);
			lblError.setText(descError);
			lblError.setVisible(true);
		}

		LOG.info("END updateInfoAttore");
	}

	private void refreshContaGiocatori(HashMap<String, String> m, String sq) {

		if (m.containsKey(sq)) {
			String v = m.get(sq);
			int newValue = Integer.parseInt(v) + 1;
			m.put(sq, "" + newValue);
		} else {
			m.put(sq, "1");
		}
	}

	private Grid<FcFormazione> buildTable(FcAttore attore) throws Exception {

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		idCampionato = "" + campionato.getIdCampionato();

		List<FcFormazione> listFormazione = formazioneController.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);
		// LOG.info("" + listFormazione.size());

		Grid<FcFormazione> grid = new Grid<>();
		grid.setAllRowsVisible(true);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		grid.setWidth(widthG);
		grid.setItems(listFormazione);

		if (listFormazione.size() == 0) {
			return grid;
		}

		Binder<FcFormazione> binder = new Binder<>(FcFormazione.class);
		grid.getEditor().setBinder(binder);

		ComboBox<FcGiocatore> giocatore = new ComboBox<>();
		giocatore.setItemLabelGenerator(p -> p.getCognGiocatore());
		giocatore.setClearButtonVisible(true);
		giocatore.setPlaceholder("Giocatore");
		giocatore.setRenderer(new ComponentRenderer<>(g -> {
			VerticalLayout container = new VerticalLayout();

			Span c1 = new Span(g.getCognGiocatore());
			container.add(c1);

			Span c2 = new Span(g.getFcRuolo().getIdRuolo() + " - " + g.getFcSquadra().getNomeSquadra());
			c2.getStyle().set("fontSize", "smaller");
			container.add(c2);

			Span c3 = new Span("Q " + g.getQuotazione());
			c2.getStyle().set("fontSize", "smaller");
			container.add(c3);

			return container;
		}));
		giocatore.setItems(giocatori);
		giocatore.setWidth(widthc1);
		giocatore.addValueChangeListener(evt -> {
			// LOG.info("giocatore.addValueChangeListener");
		});
		// Close the editor in case of forward navigation between
		giocatore.getElement().addEventListener("keydown", event -> grid.getEditor().cancel()).setFilter("event.key === 'Tab' && !event.shiftKey");

		NumberField totPagato = new NumberField();
		totPagato.setMin(0d);
		totPagato.setMax(500d);
		totPagato.setStepButtonsVisible(true);
		totPagato.setWidth(widthc2);
		// Close the editor in case of backward between components
		totPagato.getElement().addEventListener("keydown", event -> grid.getEditor().cancel()).setFilter("event.key === 'Tab' && event.shiftKey");

		Column<FcFormazione> cognGiocatoreColumn = grid.addColumn(formazione -> formazione.getFcGiocatore() != null ? formazione.getFcGiocatore().getCognGiocatore() : null);
		cognGiocatoreColumn.setKey("fcGiocatore");
		// cognGiocatoreColumn.setHeader("G");
		binder.bind(giocatore, "fcGiocatore");
		cognGiocatoreColumn.setEditorComponent(giocatore);

		Column<FcFormazione> totPagatoColumn = grid.addColumn(f -> {
			if (f.getFcGiocatore() != null) {
				return Double.valueOf(f.getTotPagato());
			}
			return Double.valueOf(0);
		});
		totPagatoColumn.setKey("totPagato");
		// totPagatoColumn.setHeader("P");
		binder.bind(totPagato, "totPagato");
		totPagatoColumn.setEditorComponent(totPagato);

		binder.addValueChangeListener(evt -> {
			if (evt.getValue() != null && evt.getValue() instanceof FcGiocatore) {
				FcGiocatore g = ((FcGiocatore) evt.getValue());
				// LOG.info("binder.addValueChangeListener " +
				// g.getCognGiocatore());
				List<FcFormazione> data = grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
				for (FcFormazione f : data) {
					if (f.getFcGiocatore() != null && f.getFcGiocatore().getCognGiocatore().equals(g.getCognGiocatore())) {
						f.setTotPagato(Double.valueOf(g.getQuotazione()));
						grid.getDataProvider().refreshItem(f);
						break;
					}
				}
			}
		});

		binder.addValueChangeListener(event -> {
			LOG.info("addValueChangeListener");
			grid.getEditor().refresh();

			try {
				updateInfoAttore();
			} catch (Exception e) {
				CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
			}
		});

		grid.addItemDoubleClickListener(event -> grid.getEditor().editItem(event.getItem()));

		grid.addItemClickListener(event -> {
			// LOG.info("addItemClickListener");
			// if (binder.getBean() != null && binder.getBean().getFcGiocatore()
			// != null) {
			// LOG.info(binder.getBean().getFcGiocatore().getCognGiocatore() +
			// ", " + binder.getBean().getTotPagato());
			// }
		});

		// grid.getDataProvider().addDataProviderListener(event -> {
		// LOG.info("addDataProviderListener");
		// List<FcFormazione> data = event.getSource().fetch(new
		// Query<>()).collect(Collectors.toList());
		// Double totCrediti = new Double(500);
		// Double somma = new Double(0);
		// String descAttore = "";
		// for (FcFormazione f : data) {
		// if (f.getFcGiocatore() != null && f.getTotPagato() != null) {
		// somma += f.getTotPagato();
		// }
		// descAttore = f.getFcAttore().getDescAttore();
		// }
		// Double residuo = totCrediti - somma;
		//
		// grid.appendFooterRow().getCell(cognGiocatoreColumn).setComponent(new
		// Span("RES " + residuo));
		// grid.appendFooterRow().getCell(totPagatoColumn).setComponent(new
		// Span("TOT " + somma));
		// if (residuo < 0) {
		// ConfirmDialog.createInfo().withCaption("Messaggio di
		// errore").withMessage(descAttore + " Residuo minore di
		// 0").withOkButton().open();
		// }
		// });
		// Fire a data change event to initialize the summary footer
		// grid.getDataProvider().refreshAll();

		return grid;
	}

	private Grid<FcProperties> buildTableContaPlayer() {

		Grid<FcProperties> grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setAllRowsVisible(true);
		// grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
		grid.setWidth(widthG);

		// Column<FcProperties> keyColumn = grid.addColumn(p -> p.getKey());
		// keyColumn.setSortable(false);
		// keyColumn.setWidth(widthc1);

		Column<FcProperties> keyColumn = grid.addColumn(new ComponentRenderer<>(f -> {

			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			cellLayout.setAlignItems(Alignment.STRETCH);

			if (f != null && f.getKey() != null) {
				FcSquadra sq = squadraController.findByNomeSquadra(f.getKey());
				if (sq != null && sq.getImg() != null) {
					try {
						Image img = Utils.getImage(sq.getNomeSquadra(), sq.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				Span lblSquadra = new Span(f.getKey());
				cellLayout.add(lblSquadra);
			}

			return cellLayout;

		}));
		keyColumn.setSortable(false);
		keyColumn.setWidth(widthc1);
		keyColumn.setAutoWidth(true);

		Column<FcProperties> valueColumn = grid.addColumn(p -> p.getValue());
		valueColumn.setSortable(false);
		valueColumn.setWidth(widthc2);
		valueColumn.setAutoWidth(true);

		return grid;
	}

}