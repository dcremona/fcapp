package fcapp.ui.views.admin;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Formazione")
@Route(value = "formazione", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcFormazioneView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final transient Logger log = LoggerFactory.getLogger(this.getClass());
	private final transient AttoreService attoreService;
	private final transient FormazioneService formazioneService;
	private final transient GiocatoreService giocatoreService;
	private final transient AccessoService accessoService;

	public FcFormazioneView(AttoreService attoreService,FormazioneService formazioneService,GiocatoreService giocatoreService,AccessoService accessoService) {
		log.info("FcFormazioneView()");
		this.attoreService = attoreService;
		this.formazioneService = formazioneService;
		this.giocatoreService = giocatoreService;
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

		crud.getCrudFormFactory().setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcGiocatore", new ComboBoxProvider<>(Costants.GIOCATORE,giocatoreService.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));

		crud.setRowCountCaption("%d Formazione(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(false);

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");

		crud.setFindAllOperation(() -> formazioneService.findByFcCampionato(campionato));
		crud.setAddOperation(formazioneService::save);
		crud.setUpdateOperation(formazioneService::save);
		crud.setDeleteOperation(formazioneService::delete);

		add(crud);

	}

}