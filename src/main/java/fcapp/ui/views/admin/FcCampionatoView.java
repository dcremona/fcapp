package fcapp.ui.views.admin;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.CampionatoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Campionato")
@Route(value = "campionato", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcCampionatoView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final transient Logger log = LoggerFactory.getLogger(this.getClass());
	private final transient AccessoService accessoService;
	private final transient CampionatoService campionatoService;

	public FcCampionatoView(AccessoService accessoService,CampionatoService campionatoService) {
		log.info("FcCampionatoView()");
		this.accessoService = accessoService;
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

		GridCrud<FcCampionato> crud = new GridCrud<>(FcCampionato.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcCampionato> formFactory = new DefaultCrudFormFactory<>(FcCampionato.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "idCampionato", "descCampionato", "type", "dataInizio", "dataFine", "start", "end", "active");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "idCampionato", "descCampionato", "type", "dataInizio", "dataFine", "start", "end", "active");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "idCampionato", "descCampionato", "type", "dataInizio", "dataFine", "start", "end", "active");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "idCampionato", "descCampionato");

		crud.getGrid().setColumns("idCampionato", "descCampionato", "type", "dataInizio", "dataFine", "start", "end", "active");

		crud.getGrid().addColumn(new ComponentRenderer<>(user -> {
			Checkbox check = new Checkbox();
			check.setValue(user.isActive());
			return check;
		})).setHeader("Attivo");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.setRowCountCaption("%d campionato(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(campionatoService::findAll);
		crud.setAddOperation(campionatoService::save);
		crud.setUpdateOperation(campionatoService::save);
		crud.setDeleteOperation(campionatoService::delete);

		add(crud);
	}

}