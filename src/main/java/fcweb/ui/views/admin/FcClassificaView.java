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

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.CampionatoService;
import fcweb.backend.service.ClassificaService;
import fcweb.ui.views.MainLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;


@PageTitle("Classifica")
@Route(value = "classificaAdmin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcClassificaView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ClassificaService classificaController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private CampionatoService campionatoController;

	@Autowired
	public Environment env;

//	private ComboBox<FcAttore> attoreFilter = new ComboBox<FcAttore>();
	private ComboBox<FcCampionato> campionatoFilter = new ComboBox<>();

	@Autowired
	private AccessoService accessoController;

	public FcClassificaView() {
		LOG.info("FcClassificaView()");
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

		GridCrud<FcClassifica> crud = new GridCrud<>(FcClassifica.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcClassifica> formFactory = new DefaultCrudFormFactory<>(FcClassifica.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "fcCampionato", "fcAttore", "punti", "idPosiz", "idPosizFinal", "totPunti", "totPuntiOld", "totPuntiRosa");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "fcCampionato", "fcAttore", "punti", "idPosiz", "idPosizFinal", "totPunti", "totPuntiOld", "totPuntiRosa");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "fcCampionato", "fcAttore", "punti", "idPosiz", "idPosizFinal", "totPunti", "totPuntiOld", "totPuntiRosa");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id", "fcCampionato", "fcAttore", "punti");

		// private int dr;
		// private int fmMercato;
		// private int gf;
		// private int gs;
		// private int idPosiz;
		// private int idPosizFinal;
		// private int pari;
		// private int perse;
		// private int punti;
		// private int totFm;
		// private Double totPunti;
		// private Double totPuntiOld;
		// private Double totPuntiRosa;
		// private int vinte;

		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getId().getIdCampionato() : "")).setHeader("Id");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcCampionato() != null ? f.getFcCampionato().getDescCampionato() : "")).setHeader("Campionato");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttore() != null ? f.getFcAttore().getDescAttore() : "")).setHeader("Attore");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? f.getPunti()+"" : "")).setHeader("Punti");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? f.getIdPosiz()+"" : "")).setHeader("idPosiz");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? f.getIdPosizFinal()+"" : "")).setHeader("idPosizFinal");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotPunti() != null ? f.getTotPunti().toString() : "")).setHeader("TotPunti");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotPuntiOld() != null ? f.getTotPuntiOld().toString() : "")).setHeader("TotPunti Old");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotPuntiRosa() != null ? f.getTotPuntiRosa().toString() : "")).setHeader("TotPunti Rosa");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcCampionato", new ComboBoxProvider<>("Camionato",campionatoController.findAll(),new TextRenderer<>(FcCampionato::getDescCampionato),FcCampionato::getDescCampionato));
		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreController.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));

		crud.setRowCountCaption("%d Classifica(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		campionatoFilter.setPlaceholder("Campionato");
		campionatoFilter.setItems(campionatoController.findAll());
		campionatoFilter.setItemLabelGenerator(FcCampionato::getDescCampionato);
		campionatoFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(campionatoFilter);
		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		campionatoFilter.setValue(campionato);


//		attoreFilter.setPlaceholder("Attore");
//		attoreFilter.setItems(attoreController.findByActive(true));
//		attoreFilter.setItemLabelGenerator(FcAttore::getDescAttore);
//		attoreFilter.addValueChangeListener(e -> crud.refreshGrid());
//		crud.getCrudLayout().addFilterComponent(attoreFilter);

		// flagAttivoFilter.setPlaceholder("filter by flag...");
		// flagAttivoFilter.addValueChangeListener(e -> crud.refreshGrid());
		// crud.getCrudLayout().addFilterComponent(flagAttivoFilter);

		Button clearFilters = new Button("clear");
		clearFilters.addClickListener(event -> {
			campionatoFilter.clear();
			//attoreFilter.clear();
		});
		crud.getCrudLayout().addFilterComponent(clearFilters);

		//crud.setFindAllOperation(() -> classificaController.findAll());
		crud.setFindAllOperation(() -> classificaController.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionatoFilter.getValue()));
		crud.setAddOperation(user -> classificaController.updateClassifica(user));
		crud.setUpdateOperation(user -> classificaController.updateClassifica(user));
		crud.setDeleteOperation(user -> classificaController.deleteClassifica(user));

		add(crud);
	}

}