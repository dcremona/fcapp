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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcStatoGiocatore;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.GiornataDettService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.StatoGiocatoreService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("GiornataDett")
@Route(value = "giornatadett", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataDettView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private GiornataInfoService giornataInfoController;

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	private StatoGiocatoreService statoGiocatoreController;

	@Autowired
	private GiornataDettService giornataDettController;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	private ComboBox<FcAttore> attoreFilter = new ComboBox<>();
	private ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>();
	private TextField flagAttivoFilter = new TextField();

	public FcGiornataDettView() {
		log.info("FcGiornataDettView()");
	}

	@PostConstruct
	void init() {
		log.info("init");
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

		GridCrud<FcGiornataDett> crud = new GridCrud<>(FcGiornataDett.class,new HorizontalSplitCrudLayout());
		DefaultCrudFormFactory<FcGiornataDett> formFactory = new DefaultCrudFormFactory<>(FcGiornataDett.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "ordinamento", "fcGiornataInfo", "fcAttore", "fcGiocatore", "fcStatoGiocatore", "voto", "flagAttivo");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "ordinamento", "fcGiornataInfo", "fcAttore", "fcGiocatore", "fcStatoGiocatore", "voto", "flagAttivo");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "ordinamento", "fcStatoGiocatore", "voto", "flagAttivo");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "ordinamento", "fcGiornataInfo", "fcAttore", "fcGiocatore");

		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiornataInfo() != null ? f.getFcGiornataInfo().getDescGiornataFc() : "")).setHeader("Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getOrdinamento() : "")).setHeader("Ordinamento");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttore() != null ? f.getFcAttore().getDescAttore() : "")).setHeader("Attore");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiocatore() != null ? f.getFcGiocatore().getCognGiocatore() : "")).setHeader(Costants.GIOCATORE);
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcStatoGiocatore() != null ? f.getFcStatoGiocatore().getDescStatoGiocatore() : "")).setHeader("Stato");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getVoto() != null ? f.getVoto().toString() : "")).setHeader("Voto");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFlagAttivo() != null ? f.getFlagAttivo() : "")).setHeader("Attivo");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcGiornataInfo", new ComboBoxProvider<>("Giornata",giornataInfoController.findAll(),new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),FcGiornataInfo::getDescGiornataFc));
		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreController.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcGiocatore", new ComboBoxProvider<>(Costants.GIOCATORE,giocatoreController.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));
		crud.getCrudFormFactory().setFieldProvider("fcStatoGiocatore", new ComboBoxProvider<>("Stato",statoGiocatoreController.findAll(),new TextRenderer<>(FcStatoGiocatore::getDescStatoGiocatore),FcStatoGiocatore::getDescStatoGiocatore));

		crud.setRowCountCaption("%d Giornata(s) found");
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
		giornataInfoFilter.setClearButtonVisible(true);
		crud.getCrudLayout().addFilterComponent(giornataInfoFilter);

		attoreFilter.setPlaceholder("Attore");
		attoreFilter.setItems(attoreController.findByActive(true));
		attoreFilter.setItemLabelGenerator(FcAttore::getDescAttore);
		attoreFilter.addValueChangeListener(e -> crud.refreshGrid());
		attoreFilter.setClearButtonVisible(true);
		crud.getCrudLayout().addFilterComponent(attoreFilter);

		flagAttivoFilter.setPlaceholder("filter by flag...");
		flagAttivoFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(flagAttivoFilter);

		Button clearFilters = new Button("clear");
		clearFilters.addClickListener(event -> {
			flagAttivoFilter.clear();
			attoreFilter.clear();
		});
		crud.getCrudLayout().addFilterComponent(clearFilters);

		crud.setFindAllOperation(() -> giornataDettController.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attoreFilter.getValue(), giornataInfoFilter.getValue()));
		crud.setAddOperation(g -> giornataDettController.insertGiornataDett(g));
		crud.setUpdateOperation(g -> giornataDettController.updateGiornataDett(g));
		crud.setDeleteOperation(g -> giornataDettController.deleteGiornataDett(g));

		add(crud);

	}

}