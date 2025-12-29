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

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.FormazioneService;
import fcweb.backend.service.GiocatoreService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serial;

@PageTitle("Formazione")
@Route(value = "formazione", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcFormazioneView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private FormazioneService formazioneController;

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	public FcFormazioneView() {
		log.info("FcFormazioneView()");
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

		GridCrud<FcFormazione> crud = new GridCrud<>(FcFormazione.class,new HorizontalSplitCrudLayout());
		DefaultCrudFormFactory<FcFormazione> formFactory = new DefaultCrudFormFactory<>(FcFormazione.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "fcAttore", "fcGiocatore", "totPagato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "fcAttore", "fcGiocatore", "totPagato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "fcAttore", "fcGiocatore", "totPagato");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id", "fcGiocatore");

		crud.getGrid().setColumns("id", "fcAttore", "fcGiocatore", "totPagato");
		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getId().getOrdinamento() : "")).setHeader("Id");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttore() != null ? f.getFcAttore().getDescAttore() : "")).setHeader("Attore");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiocatore() != null ? f.getFcGiocatore().getCognGiocatore() : "")).setHeader(Costants.GIOCATORE);
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotPagato() != null ? f.getTotPagato().toString() : "")).setHeader("Pagato");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreController.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcGiocatore", new ComboBoxProvider<>(Costants.GIOCATORE,giocatoreController.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));

		crud.setRowCountCaption("%d Formazione(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(false);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		crud.setFindAllOperation(() -> formazioneController.findByFcCampionato(campionato));
		crud.setAddOperation(user -> formazioneController.updateFormazione(user));
		crud.setUpdateOperation(user -> formazioneController.updateFormazione(user));
		crud.setDeleteOperation(user -> formazioneController.deleteFormazione(user));

		add(crud);

	}

}