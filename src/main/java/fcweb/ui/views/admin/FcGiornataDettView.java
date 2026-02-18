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
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

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
import fcweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serial;

@PageTitle("GiornataDett")
@Route(value = "giornataDett", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataDettView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public Environment env;

	private final AttoreService attoreService;
	private final GiornataInfoService giornataInfoService;
	private final GiocatoreService giocatoreService;
	private final StatoGiocatoreService statoGiocatoreService;
	private final GiornataDettService giornataDettService;
	private final AccessoService accessoService;

	private final ComboBox<FcAttore> attoreFilter = new ComboBox<>();
	private final ComboBox<FcGiornataInfo> giornataInfoFilter = new ComboBox<>();
//	private TextField flagAttivoFilter = new TextField();

	public FcGiornataDettView(AttoreService attoreService,GiornataInfoService giornataInfoService,GiocatoreService giocatoreService,
			StatoGiocatoreService statoGiocatoreService,GiornataDettService giornataDettService,AccessoService accessoService) {
		log.info("FcGiornataDettView()");
		this.attoreService = attoreService;
		this.giornataInfoService = giornataInfoService;
		this.giocatoreService = giocatoreService;
		this.statoGiocatoreService = statoGiocatoreService;
		this.giornataDettService = giornataDettService;
		this.accessoService = accessoService;
	}

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoService.insertAccesso(this.getClass().getName());
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

		crud.getCrudFormFactory().setFieldProvider("fcGiornataInfo", new ComboBoxProvider<>("Giornata",giornataInfoService.findAll(),new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),FcGiornataInfo::getDescGiornataFc));
		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcGiocatore", new ComboBoxProvider<>(Costants.GIOCATORE,giocatoreService.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));
		crud.getCrudFormFactory().setFieldProvider("fcStatoGiocatore", new ComboBoxProvider<>("Stato",statoGiocatoreService.findAll(),new TextRenderer<>(FcStatoGiocatore::getDescStatoGiocatore),FcStatoGiocatore::getDescStatoGiocatore));

		crud.setRowCountCaption("%d Giornata(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		giornataInfoFilter.setPlaceholder("Giornata");
		giornataInfoFilter.setItems(giornataInfoService.findAll());
		if ("1".equals(campionato.getType())) {
			giornataInfoFilter.setItemLabelGenerator(Utils::buildInfoGiornata);
		} else {
			giornataInfoFilter.setItemLabelGenerator(g -> Utils.buildInfoGiornataEm(g, campionato));
		}
		giornataInfoFilter.addValueChangeListener(e -> crud.refreshGrid());
		giornataInfoFilter.setClearButtonVisible(true);
		crud.getCrudLayout().addFilterComponent(giornataInfoFilter);

		attoreFilter.setPlaceholder("Attore");
		attoreFilter.setItems(attoreService.findByActive(true));
		attoreFilter.setItemLabelGenerator(FcAttore::getDescAttore);
		attoreFilter.addValueChangeListener(e -> crud.refreshGrid());
		attoreFilter.setClearButtonVisible(true);
		crud.getCrudLayout().addFilterComponent(attoreFilter);

        Button clearFilters = new Button("clear");
		clearFilters.addClickListener(event -> {
//			flagAttivoFilter.clear();
			attoreFilter.clear();
		});
		crud.getCrudLayout().addFilterComponent(clearFilters);

		crud.setFindAllOperation(() -> giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attoreFilter.getValue(), giornataInfoFilter.getValue()));
		crud.setAddOperation(giornataDettService::insertGiornataDett);
		crud.setUpdateOperation(giornataDettService::updateGiornataDett);
		crud.setDeleteOperation(giornataDettService::deleteGiornataDett);

		add(crud);

	}

}