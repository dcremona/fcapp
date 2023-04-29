package fcweb.ui.views.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import common.util.Utils;
import fcweb.backend.data.entity.FcExpStat;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.ExpStatService;
import fcweb.ui.views.MainLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;


@PageTitle("ExpStat")
@Route(value = "fcExpStat", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcExpStatView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ExpStatService expStatController;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	public FcExpStatView() {
		LOG.info("FcExpStatView()");
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

		GridCrud<FcExpStat> crud = new GridCrud<>(FcExpStat.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcExpStat> formFactory = new DefaultCrudFormFactory<>(FcExpStat.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "anno", "campionato", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "scudetto", "winClasPt", "winClasReg");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "anno", "campionato", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "scudetto", "winClasPt", "winClasReg");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "id", "anno", "campionato", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "scudetto", "winClasPt", "winClasReg");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.setRowCountCaption("%d ExpStat(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(() -> expStatController.findAll());
		crud.setAddOperation(s -> expStatController.updateExpStat(s));
		crud.setUpdateOperation(s -> expStatController.updateExpStat(s));
		crud.setDeleteOperation(s -> expStatController.deleteExpStat(s));

		add(crud);
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {

	}

}