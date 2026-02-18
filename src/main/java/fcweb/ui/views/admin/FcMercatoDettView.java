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

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.GiornataInfoService;
import fcweb.backend.service.MercatoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("MercatoDett")
@Route(value = "mercatoDett", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcMercatoDettView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public Environment env;

	private final MercatoService mercatoService;
	private final GiornataInfoService giornataInfoService;
	private final AttoreService attoreService;
	private final GiocatoreService giocatoreService;
	private final AccessoService accessoService;

	public FcMercatoDettView(MercatoService mercatoService,GiornataInfoService giornataInfoService,AttoreService attoreService,GiocatoreService giocatoreService,AccessoService accessoService) {
		log.info("FcMercatoDettView()");
		this.mercatoService = mercatoService;
		this.giornataInfoService = giornataInfoService;
		this.attoreService = attoreService;
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

		GridCrud<FcMercatoDett> crud = new GridCrud<>(FcMercatoDett.class,new HorizontalSplitCrudLayout());
		DefaultCrudFormFactory<FcMercatoDett> formFactory = new DefaultCrudFormFactory<>(FcMercatoDett.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		formFactory.setVisibleProperties(CrudOperation.READ, "id", "fcGiornataInfo", "fcAttore", "fcGiocatoreByIdGiocVen", "fcGiocatoreByIdGiocAcq", "dataCambio", "nota");
		formFactory.setVisibleProperties(CrudOperation.ADD, "id", "fcGiornataInfo", "fcAttore", "fcGiocatoreByIdGiocVen", "fcGiocatoreByIdGiocAcq", "dataCambio", "nota");
		formFactory.setVisibleProperties(CrudOperation.UPDATE, "id", "fcGiornataInfo", "fcAttore", "fcGiocatoreByIdGiocVen", "fcGiocatoreByIdGiocAcq", "dataCambio", "nota");
		formFactory.setVisibleProperties(CrudOperation.DELETE, "id", "fcGiornataInfo", "fcAttore", "fcGiocatoreByIdGiocVen", "fcGiocatoreByIdGiocAcq");

		crud.getGrid().setColumns("id", "fcAttore", "fcGiocatoreByIdGiocVen", "fcGiocatoreByIdGiocAcq", "fcGiornataInfo", "dataCambio", "nota");
		crud.getGrid().removeAllColumns();
		crud.getGrid().addColumn(new TextRenderer<>(g -> g != null ? "" + g.getId() : "")).setHeader("Id");
		crud.getGrid().addColumn(new TextRenderer<>(f -> f != null && f.getFcGiornataInfo() != null ? f.getFcGiornataInfo().getDescGiornataFc() : "")).setHeader("Giornata");
		crud.getGrid().addColumn(new TextRenderer<>(g -> g != null ? g.getFcAttore().getDescAttore() : "")).setHeader("Attore");
		crud.getGrid().addColumn(new TextRenderer<>(g -> g != null && g.getFcGiocatoreByIdGiocVen() != null ? g.getFcGiocatoreByIdGiocVen().getCognGiocatore() : "")).setHeader("Giocatore Ven");
		crud.getGrid().addColumn(new TextRenderer<>(g -> g != null && g.getFcGiocatoreByIdGiocAcq() != null ? g.getFcGiocatoreByIdGiocAcq().getCognGiocatore() : "")).setHeader("Giocatore Acq");

		Column<FcMercatoDett> dataColumn = crud.getGrid().addColumn(new LocalDateTimeRenderer<>(FcMercatoDett::getDataCambio,() -> DateTimeFormatter.ofPattern(Costants.DATA_FORMATTED)));
		dataColumn.setHeader("Data Cambio");
		dataColumn.setSortable(false);
		dataColumn.setAutoWidth(true);
		dataColumn.setFlexGrow(2);

		crud.getGrid().addColumn(new TextRenderer<>(g -> g != null ? g.getNota() : "")).setHeader("Nota");

		crud.getGrid().setColumnReorderingAllowed(true);

		formFactory.setFieldProvider("fcGiornataInfo", new ComboBoxProvider<>("Giornata",giornataInfoService.findAll(),new TextRenderer<>(FcGiornataInfo::getDescGiornataFc),FcGiornataInfo::getDescGiornataFc));
		formFactory.setFieldProvider("fcAttore", new ComboBoxProvider<>("Attore",attoreService.findByActive(true),new TextRenderer<>(FcAttore::getDescAttore),FcAttore::getDescAttore));
		formFactory.setFieldProvider("fcGiocatoreByIdGiocVen", new ComboBoxProvider<>("Giocatore Acq",giocatoreService.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));
		formFactory.setFieldProvider("fcGiocatoreByIdGiocAcq", new ComboBoxProvider<>("Giocatore Ven",giocatoreService.findAll(),new TextRenderer<>(FcGiocatore::getCognGiocatore),FcGiocatore::getCognGiocatore));
		formFactory.setFieldProvider("dataCambio", a -> new DateTimePicker());

		crud.setRowCountCaption("%d Mercato(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		crud.setFindAllOperation(() -> mercatoService.findAll());
		crud.setAddOperation(g -> mercatoService.insertMercatoDett(g));
		crud.setUpdateOperation(g -> mercatoService.insertMercatoDett(g));
		crud.setDeleteOperation(g -> mercatoService.deleteMercatoDett(g));

		add(crud);
	}
}