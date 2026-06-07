package fcapp.ui.views.admin;

import java.io.Serial;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Utenti")
@Route(value = "user", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcUserView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FcUserView.class);

    private static final int BCRYPT_STRENGTH = 10;
    private static final Long SIMULATED_ERROR_USER_ID = 10L;

    private static final String FIELD_ID = "id";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_HASHED_PASSWORD = "hashedPassword";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ROLES = "roles";
    private static final String FIELD_ID_ATTORE = "idAttore";
    private static final String FIELD_DESC_ATTORE = "descAttore";
    private static final String FIELD_COGNOME = "cognome";
    private static final String FIELD_NOME = "nome";
    private static final String FIELD_CELLULARE = "cellulare";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NOTIFICHE = "notifiche";
    private static final String FIELD_ACTIVE = "active";

    private final transient AttoreService attoreService;
    private final transient AccessoService accessoService;
    private final transient BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(BCRYPT_STRENGTH, new SecureRandom());

    public FcUserView(
            AttoreService attoreService,
            AccessoService accessoService) {
        LOG.info("Initializing {}", FcUserView.class.getSimpleName());
        this.attoreService = attoreService;
        this.accessoService = accessoService;
    }

    @PostConstruct
    void init() {
        LOG.info("Running init for {}", FcUserView.class.getSimpleName());

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        configureLayout();
        add(buildCrud());
    }

    private void configureLayout() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();
    }

    private GridCrud<FcAttore> buildCrud() {
        GridCrud<FcAttore> crud =
                new GridCrud<>(FcAttore.class, new HorizontalSplitCrudLayout());

        configureFormFactory(crud);
        configureGrid(crud);
        configureOperations(crud);

        crud.setRowCountCaption("%d user(s) found");
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(true);

        return crud;
    }

    private void configureFormFactory(GridCrud<FcAttore> crud) {
        DefaultCrudFormFactory<FcAttore> formFactory =
                new DefaultCrudFormFactory<>(FcAttore.class);
        formFactory.setUseBeanValidation(false);
        crud.setCrudFormFactory(formFactory);

        formFactory.setVisibleProperties(
                CrudOperation.READ,
                FIELD_ID,
                FIELD_USERNAME,
                FIELD_HASHED_PASSWORD,
                FIELD_NAME,
                FIELD_ROLES,
                FIELD_ID_ATTORE,
                FIELD_DESC_ATTORE,
                FIELD_COGNOME,
                FIELD_NOME,
                FIELD_CELLULARE,
                FIELD_EMAIL,
                FIELD_NOTIFICHE,
                FIELD_ACTIVE);

        formFactory.setVisibleProperties(
                CrudOperation.ADD,
                FIELD_ID,
                FIELD_USERNAME,
                FIELD_HASHED_PASSWORD,
                FIELD_NAME,
                FIELD_ROLES,
                FIELD_ID_ATTORE,
                FIELD_DESC_ATTORE,
                FIELD_COGNOME,
                FIELD_NOME,
                FIELD_CELLULARE,
                FIELD_EMAIL,
                FIELD_NOTIFICHE,
                FIELD_ACTIVE);

        formFactory.setVisibleProperties(
                CrudOperation.UPDATE,
                FIELD_USERNAME,
                FIELD_HASHED_PASSWORD,
                FIELD_NAME,
                FIELD_ROLES,
                FIELD_ID_ATTORE,
                FIELD_DESC_ATTORE,
                FIELD_COGNOME,
                FIELD_NOME,
                FIELD_CELLULARE,
                FIELD_EMAIL,
                FIELD_NOTIFICHE,
                FIELD_ACTIVE);

        formFactory.setVisibleProperties(
                CrudOperation.DELETE,
                FIELD_ID,
                FIELD_USERNAME);

        formFactory.setFieldType(FIELD_HASHED_PASSWORD, PasswordField.class);
        formFactory.setFieldProvider(FIELD_ROLES, field -> {
            CheckboxGroup<Role> checkboxes = new CheckboxGroup<>();
            checkboxes.setItems(Role.values());
            return checkboxes;
        });
    }

    private void configureGrid(GridCrud<FcAttore> crud) {
        crud.getGrid().removeAllColumns();

        crud.getGrid().addColumn(FcAttore::getId).setHeader("Id");
        crud.getGrid().addColumn(FcAttore::getIdAttore).setHeader("Id Attore");
        crud.getGrid().addColumn(FcAttore::getDescAttore).setHeader("Descrizione");
        crud.getGrid().addColumn(FcAttore::getUsername).setHeader("Username");
        crud.getGrid().addColumn(FcAttore::getCellulare).setHeader("Cellulare");
        crud.getGrid().addColumn(user -> user.getRoles() != null ? user.getRoles().toString() : "")
                .setHeader("Roles");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(this::buildNotificheCheckbox))
                .setHeader("Notifiche");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(this::buildActiveCheckbox))
                .setHeader("Attivo");

        crud.getGrid()
                .addColumn(new ComponentRenderer<>(this::buildAvatarLayout))
                .setHeader("Avatar");

        crud.getGrid().setColumnReorderingAllowed(true);
    }

    private Checkbox buildNotificheCheckbox(FcAttore user) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(user != null && user.isNotifiche());
        checkbox.setEnabled(false);
        return checkbox;
    }

    private Checkbox buildActiveCheckbox(FcAttore user) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(user != null && user.isActive());
        checkbox.setEnabled(false);
        return checkbox;
    }

    private HorizontalLayout buildAvatarLayout(FcAttore user) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setSizeFull();

        if (user != null && user.getProfilePicture() != null) {
            Avatar avatar = new Avatar(user.getName());
            avatar.setImageResource(Utils.getStreamResource("profile-pic", user.getProfilePicture()));
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");
            cellLayout.add(avatar);
        }

        return cellLayout;
    }

    private void configureOperations(GridCrud<FcAttore> crud) {
        crud.setOperations(
                attoreService::findAll,
                attoreService::save,
                this::updateUser,
                user -> attoreService.delete(user.getId()));
    }

    private FcAttore updateUser(FcAttore user) {
        encodePassword(user);

        if (SIMULATED_ERROR_USER_ID.equals(user.getId())) {
            throw new CrudOperationException("Simulated error.");
        }

        return attoreService.save(user);
    }

    private void encodePassword(FcAttore user) {
        if (user == null || user.getHashedPassword() == null || user.getHashedPassword().isBlank()) {
            return;
        }

        user.setHashedPassword(passwordEncoder.encode(user.getHashedPassword()));
    }
}
