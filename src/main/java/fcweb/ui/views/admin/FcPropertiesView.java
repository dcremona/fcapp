package fcweb.ui.views.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.ProprietaService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serial;

@PageTitle("Proprietà")
@Route(value = "proprietà", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcPropertiesView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public Environment env;

	private final ProprietaService proprietaService;
	private final AccessoService accessoService;

	public FcPropertiesView(ProprietaService proprietaService,AccessoService accessoService) {
		log.info("FcPropertiesView()");
		this.proprietaService = proprietaService;
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

		GridCrud<FcProperties> crud = new GridCrud<>(FcProperties.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcProperties> formFactory = new DefaultCrudFormFactory<>(FcProperties.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "key", "value");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "key", "value");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "key", "value");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "key");

		crud.getGrid().setColumns("key", "value");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.setRowCountCaption("%d property(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(proprietaService::findAll);
		crud.setAddOperation(proprietaService::save);
		crud.setUpdateOperation(proprietaService::save);
		crud.setDeleteOperation(proprietaService::delete);

		add(crud);
	}

}