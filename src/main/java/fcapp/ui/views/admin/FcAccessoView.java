package fcapp.ui.views.admin;

import java.io.Serial;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fcapp.backend.data.entity.FcAccesso;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.CampionatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accesso")
@Route(value = "fcAccesso", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcAccessoView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	@Serial
    private static final long serialVersionUID = 1L;

	private final transient Logger log = LoggerFactory.getLogger(this.getClass());
	private final transient AccessoService accessoService;
	private final transient AttoreService attoreService;
	private final transient CampionatoService campionatoService;

	public FcAccessoView(AccessoService accessoService,AttoreService attoreService,CampionatoService campionatoService) {
		log.info("FcAccessoView()");
		this.accessoService = accessoService;
		this.attoreService = attoreService;
		this.campionatoService = campionatoService;
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

		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("data", a -> new DateTimePicker());
		crud.getCrudFormFactory().setFieldProvider("fcCampionato", new ComboBoxProvider<>("Campionato",campionatoService.findAll(),new TextRenderer<>(FcCampionato::getDescCampionato),FcCampionato::getDescCampionato));

		crud.setRowCountCaption("%d Accesso(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(accessoService::findAll);
		crud.setAddOperation(accessoService::save);
		crud.setUpdateOperation(accessoService::save);
		crud.setDeleteOperation(accessoService::delete);

		add(crud);
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

	}

}