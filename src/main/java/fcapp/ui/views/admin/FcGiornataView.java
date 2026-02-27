package fcapp.ui.views.admin;

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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcTipoGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.backend.service.TipoGiornataService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.io.Serial;

@PageTitle("Giornata")
@Route(value = "giornata", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiornataView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public Environment env;

	private final GiornataService giornataService;
	private final AttoreService attoreService;
	private final GiornataInfoService giornataInfoService;
	private final TipoGiornataService tipoGiornataService;
	private final AccessoService accessoService;

	public FcGiornataView(GiornataService giornataService,AttoreService attoreService,GiornataInfoService giornataInfoService,TipoGiornataService tipoGiornataService,AccessoService accessoService) {
		log.info("FcGiornataView()");
		this.giornataService = giornataService;
		this.attoreService = attoreService;
		this.giornataInfoService = giornataInfoService;
		this.tipoGiornataService = tipoGiornataService;
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

		GridCrud<FcGiornata> crud = new GridCrud<>(FcGiornata.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcGiornata> formFactory = new DefaultCrudFormFactory<>(FcGiornata.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "id", "fcTipoGiornata", "fcGiornataInfo", "fcAttoreByIdAttoreCasa", "fcAttoreByIdAttoreFuori", "golCasa", "golFuori", "totCasa", "totFuori");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "id", "fcTipoGiornata", "fcGiornataInfo", "fcAttoreByIdAttoreCasa", "fcAttoreByIdAttoreFuori", "golCasa", "golFuori", "totCasa", "totFuori");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "id", "fcTipoGiornata", "fcGiornataInfo", "fcAttoreByIdAttoreCasa", "fcAttoreByIdAttoreFuori", "golCasa", "golFuori", "totCasa", "totFuori");
		crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "id", "fcTipoGiornata", "fcGiornataInfo", "fcAttoreByIdAttoreCasa");

		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null ? "" + f.getId().getIdGiornata() : "")).setHeader("Id Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiornataInfo() != null ? f.getFcGiornataInfo().getDescGiornataFc() : "")).setHeader("Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcTipoGiornata() != null ? f.getFcTipoGiornata().getDescTipoGiornata() : "")).setHeader("Tipo Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttoreByIdAttoreCasa() != null ? f.getFcAttoreByIdAttoreCasa().getDescAttore() : "")).setHeader("Attore Casa");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcAttoreByIdAttoreFuori() != null ? f.getFcAttoreByIdAttoreFuori().getDescAttore() : "")).setHeader("Attore Fuori");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getGolCasa() != null ? f.getGolCasa().toString() : "")).setHeader("Gol Casa");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getGolFuori() != null ? f.getGolFuori().toString() : "")).setHeader("Gol Fuori");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotCasa() != null ? f.getTotCasa().toString() : "")).setHeader("Tot Casa");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getTotFuori() != null ? f.getTotFuori().toString() : "")).setHeader("Tot Fuori");

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.getCrudFormFactory().setFieldProvider("fcGiornataInfo", new ComboBoxProvider<>("Giornata",giornataInfoService.findAll(),new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),FcGiornataInfo::getDescGiornataFc));
		crud.getCrudFormFactory().setFieldProvider("fcAttoreByIdAttoreCasa", new ComboBoxProvider<>("Attore Casa",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcAttoreByIdAttoreFuori", new ComboBoxProvider<>("Attore Fuori",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		crud.getCrudFormFactory().setFieldProvider("fcTipoGiornata", new ComboBoxProvider<>("Tipo Giornata",tipoGiornataService.findAll(),new TextRenderer<>(FcTipoGiornata::getDescTipoGiornata),FcTipoGiornata::getDescTipoGiornata));

		crud.setRowCountCaption("%d Giornata(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(giornataService::findAll);
		crud.setAddOperation(giornataService::save);
		crud.setUpdateOperation(giornataService::save);
		crud.setDeleteOperation(giornataService::delete);

		add(crud);
	}

}