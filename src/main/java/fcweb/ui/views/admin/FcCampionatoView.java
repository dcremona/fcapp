package fcweb.ui.views.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import common.util.Utils;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.CampionatoService;
import fcweb.ui.views.MainLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Campionato")
@Route(value = "campionato", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcCampionatoView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CampionatoService campionatoController;

    @Autowired
    public Environment env;

    @Autowired
    private AccessoService accessoController;

    public FcCampionatoView() {
        log.info("FcCampionatoView()");
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

        GridCrud<FcCampionato> crud = new GridCrud<>(FcCampionato.class, new HorizontalSplitCrudLayout());

        DefaultCrudFormFactory<FcCampionato> formFactory = new DefaultCrudFormFactory<>(FcCampionato.class);
        crud.setCrudFormFactory(formFactory);
        formFactory.setUseBeanValidation(false);

        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.READ, "idCampionato", "descCampionato", "type",
                "dataInizio", "dataFine", "start", "end", "active");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "idCampionato", "descCampionato", "type",
                "dataInizio", "dataFine", "start", "end", "active");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "idCampionato", "descCampionato", "type",
                "dataInizio", "dataFine", "start", "end", "active");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.DELETE, "idCampionato", "descCampionato");

        crud.getGrid().setColumns("idCampionato", "descCampionato", "type", "dataInizio", "dataFine", "start", "end",
                "active");

        crud.getGrid().addColumn(new ComponentRenderer<>(user -> {
            Checkbox check = new Checkbox();
            check.setValue(user.isActive());
            return check;
        })).setHeader("Attivo");

        crud.getGrid().setColumnReorderingAllowed(true);

        crud.setRowCountCaption("%d campionato(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        crud.setFindAllOperation(() -> campionatoController.findAll());
        crud.setAddOperation(c -> campionatoController.updateCampionato(c));
        crud.setUpdateOperation(c -> campionatoController.updateCampionato(c));
        crud.setDeleteOperation(c -> campionatoController.deleteCampionato(c));

        add(crud);
    }

}