package fcweb.ui.views.admin;

import java.io.Serial;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import common.util.Utils;
import fcweb.backend.data.entity.FcAccesso;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.CampionatoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accesso")
@Route(value = "fcAccesso", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcAccessoView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private CampionatoService campionatoController;

	@Autowired
	public Environment env;

	public FcAccessoView() {
		log.info("FcAccessoView()");
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

		GridCrud<FcAccesso> crud = new GridCrud<>(FcAccesso.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcAccesso> formFactory = new DefaultCrudFormFactory<>(FcAccesso.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "fcAttore", "data", "note", "fcCampionato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "fcAttore", "data", "note", "fcCampionato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "fcAttore", "data", "note", "fcCampionato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id", "fcAttore");

		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getId() : "")).setHeader("Id");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttore() != null ? f.getFcAttore().getDescAttore() : "")).setHeader("Attore");

		Column<FcAccesso> dataColumn = crud.getGrid().addColumn(new LocalDateTimeRenderer<>(FcAccesso::getData,() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);

		Column<FcAccesso> noteColumn = crud.getGrid().addColumn(new TextRenderer<>(s -> s == null ? "" : s.getNote())).setHeader("Info");
		noteColumn.setSortable(false);
		noteColumn.setAutoWidth(true);

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreController.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("data", a -> new DateTimePicker());
		crud.getCrudFormFactory().setFieldProvider("fcCampionato", new ComboBoxProvider<>("Camionato",campionatoController.findAll(),new TextRenderer<>(FcCampionato::getDescCampionato),FcCampionato::getDescCampionato));

		crud.setRowCountCaption("%d Accesso(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(() -> accessoController.findAll());
		crud.setAddOperation(user -> accessoController.updateAccesso(user));
		crud.setUpdateOperation(user -> accessoController.updateAccesso(user));
		crud.setDeleteOperation(user -> accessoController.deleteAccesso(user));

		add(crud);
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

	}

}