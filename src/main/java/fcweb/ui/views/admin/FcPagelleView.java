package fcweb.ui.views.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.PagelleService;
import fcweb.ui.views.MainLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;



@PageTitle("Pagelle")
@Route(value = "pagelle", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcPagelleView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	private PagelleService pagelleController;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	private ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<FcGiornataInfo>();
	private ComboBox<FcGiocatore> giocatoreFilter = new ComboBox<FcGiocatore>();

	public FcPagelleView() {
		LOG.info("FcPagelleView()");
	}

	@PostConstruct
	void init() {
		LOG.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initLayout();
	}

	private void initLayout() {

		this.setMargin(true);
		this.setSpacing(true);
		this.setSizeFull();

		GridCrud<FcPagelle> crud = new GridCrud<>(FcPagelle.class,new HorizontalSplitCrudLayout());
		DefaultCrudFormFactory<FcPagelle> formFactory = new DefaultCrudFormFactory<>(FcPagelle.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "fcGiornataInfo", "fcGiocatore", "ammonizione", "assist", "autorete", "cs", "espulsione", "g", "goalRealizzato", "goalSubito", "rigoreFallito", "rigoreParato", "rigoreSegnato", "ts", "votoGiocatore", "gdv");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "fcGiornataInfo", "fcGiocatore", "ammonizione", "assist", "autorete", "cs", "espulsione", "g", "goalRealizzato", "goalSubito", "rigoreFallito", "rigoreParato", "rigoreSegnato", "ts", "votoGiocatore", "gdv");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "fcGiornataInfo", "fcGiocatore", "ammonizione", "assist", "autorete", "cs", "espulsione", "g", "goalRealizzato", "goalSubito", "rigoreFallito", "rigoreParato", "rigoreSegnato", "ts", "votoGiocatore", "gdv");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "fcGiornataInfo", "fcGiocatore", "ammonizione", "assist", "autorete", "cs", "espulsione", "g", "goalRealizzato", "goalSubito", "rigoreFallito", "rigoreParato", "rigoreSegnato", "ts", "votoGiocatore", "gdv");

		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiornataInfo() != null ? f.getFcGiornataInfo().getDescGiornataFc() : "")).setHeader("Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiocatore() != null ? f.getFcGiocatore().getCognGiocatore() : "")).setHeader("Giocatore");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getVotoGiocatore() : "")).setHeader("Voto");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getG() : "")).setHeader("G");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getTs() : "")).setHeader("Ts");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getCs() : "")).setHeader("Ts");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcGiornataInfo", new ComboBoxProvider<>("Giornata",giornataInfoController.findAll(),new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),FcGiornataInfo::getDescGiornataFc));
		crud.getCrudFormFactory().setFieldProvider("fcGiocatore", new ComboBoxProvider<>("Giocatore",giocatoreController.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));

		crud.setRowCountCaption("%d Pagelle(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		giornataInfoFilter.setPlaceholder("Giornata");
		giornataInfoFilter.setItems(giornataInfoController.findAll());
		if ("1".equals(campionato.getType())) {
			giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornata(g));
		} else {
			giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
		}
		giornataInfoFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(giornataInfoFilter);
		giornataInfoFilter.setClearButtonVisible(true);

		giocatoreFilter.setPlaceholder("Giocatore");
		giocatoreFilter.setItems(giocatoreController.findAll());
		giocatoreFilter.setItemLabelGenerator(FcGiocatore::getCognGiocatore);
		giocatoreFilter.setRenderer(new ComponentRenderer<>(g -> {
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
		giocatoreFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(giocatoreFilter);
		giocatoreFilter.setClearButtonVisible(true);

		Button clearFilters = new Button("clear");
		clearFilters.addClickListener(event -> {
			giornataInfoFilter.clear();
			giocatoreFilter.clear();
		});
		crud.getCrudLayout().addFilterComponent(clearFilters);

		crud.setFindAllOperation(() -> pagelleController.findByCustonm(giornataInfoFilter.getValue(), giocatoreFilter.getValue()));
		crud.setAddOperation(user -> pagelleController.updatePagelle(user));
		crud.setUpdateOperation(user -> pagelleController.updatePagelle(user));
		crud.setDeleteOperation(user -> pagelleController.deletePagelle(user));

		add(crud);
	}

}