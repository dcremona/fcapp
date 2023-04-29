package fcweb.ui.views.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AttoreService;
import fcweb.ui.views.MainLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Utenti")
@Route(value = "user", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcUserView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreService;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	public FcUserView() {
		LOG.info("FcUserView()");
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

		GridCrud<FcAttore> crud = new GridCrud<>(FcAttore.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcAttore> formFactory = new DefaultCrudFormFactory<>(FcAttore.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		formFactory.setVisibleProperties(CrudOperation.READ, "id", "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.ADD, "id", "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.UPDATE, "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.DELETE, "id", "username");

		crud.getGrid().setColumns("id", "username", "hashedPassword", "name", "roles", "idAttore", "descAttore", "cognome", "nome", "email", "cellulare");

		crud.getGrid().addColumn(new ComponentRenderer<>(user -> {
			Checkbox check = new Checkbox();
			check.setValue(user.isNotifiche());
			return check;
		})).setHeader("Notifiche");

		crud.getGrid().addColumn(new ComponentRenderer<>(user -> {
			Checkbox check = new Checkbox();
			check.setValue(user.isActive());
			return check;
		})).setHeader("Attivo");

		crud.getGrid().addColumn(new ComponentRenderer<>(u -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setSizeFull();
			if (u != null && u.getProfilePicture() != null) {
				StreamResource resource = new StreamResource(u.getName(),() -> {
					InputStream inputStream = null;
					try {
						inputStream = new ByteArrayInputStream(u.getProfilePicture());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return inputStream;
				});
				Image img = new Image(resource,"");
				img.setSrc(resource);
				cellLayout.add(img);
			}
			return cellLayout;
		}));

		formFactory.setFieldType("hashedPassword", PasswordField.class);
		formFactory.setFieldProvider("roles", user -> {
			CheckboxGroup<Role> checkboxes = new CheckboxGroup<>();
			checkboxes.setItems(Role.values());
			return checkboxes;
		});

		crud.getGrid().setColumnReorderingAllowed(true);

		crud.setRowCountCaption("%d user(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		// logic configuration
		crud.setOperations(() -> attoreService.findAll(), user -> attoreService.update(user), user -> {

			String password = user.getHashedPassword();
			int strength = 10; // work factor of bcrypt
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength,new SecureRandom());
			String encodedPassword = bCryptPasswordEncoder.encode(user.getHashedPassword());
			System.out.println(encodedPassword);

			boolean isPasswordMatch = bCryptPasswordEncoder.matches(password, encodedPassword);
			System.out.println("Password : " + password + "   isPasswordMatch    : " + isPasswordMatch);

			isPasswordMatch = bCryptPasswordEncoder.matches(password, encodedPassword);
			System.out.println("Password : " + password + "   isPasswordMatch    : " + isPasswordMatch);

			user.setHashedPassword(encodedPassword);

			if (user.getId().equals(10L)) {
				throw new CrudOperationException("Simulated error.");
			}
			return attoreService.update(user);
		}, user -> attoreService.delete(user.getId()));

		add(crud);

	}

}