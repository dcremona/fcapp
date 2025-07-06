package fcweb.ui.views.admin;

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

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AttoreService attoreService;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	public FcUserView() {
		log.info("FcUserView()");
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

		GridCrud<FcAttore> crud = new GridCrud<>(FcAttore.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcAttore> formFactory = new DefaultCrudFormFactory<>(FcAttore.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		formFactory.setVisibleProperties(CrudOperation.READ, "id", "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.ADD, "id", "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.UPDATE, "username", "hashedPassword", "name", "roles", "profilePicture", "idAttore", "descAttore", "cognome", "nome", "cellulare", "email", "notifiche", "active");
		formFactory.setVisibleProperties(CrudOperation.DELETE, "id", "username");

		crud.getGrid().setColumns("idAttore", "descAttore", "username", "name", "email", "cellulare", "roles");

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
				Avatar avatar = new Avatar(u.getName());
				avatar.setImageResource(Utils.getStreamResource("profile-pic", u.getProfilePicture()));
				avatar.setThemeName("xsmall");
				avatar.getElement().setAttribute("tabindex", "-1");
				cellLayout.add(avatar);

			}
			return cellLayout;
		}));
/*
		Column<FcAttore> profilePictureColumn = crud.getGrid().addColumn(new ComponentRenderer<>(u -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setSizeFull();

			FileBuffer fileBuffer = new FileBuffer();
			Upload singleFileUpload = new Upload(fileBuffer);
			singleFileUpload.setDropAllowed(true);
			singleFileUpload.addSucceededListener(event -> {
				try {

					// Get information about the uploaded file
					InputStream fileData = fileBuffer.getInputStream();
					// String fileName = event.getFileName();
					// long contentLength = event.getContentLength();
					// String mimeType = event.getMIMEType();

					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					int nRead;
					byte[] data = new byte[4];
					while ((nRead = fileData.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}
					buffer.flush();
					byte[] targetArray = buffer.toByteArray();
					u.setProfilePicture(targetArray);
					attoreService.update(u);
					CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
				} catch (Exception e) {
					CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
				}

			});
			cellLayout.add(singleFileUpload);

			return cellLayout;
		}));
		profilePictureColumn.setWidth("350px");
*/
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

			//String password = user.getHashedPassword();
			int strength = 10; // work factor of bcrypt
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength,new SecureRandom());
			String encodedPassword = bCryptPasswordEncoder.encode(user.getHashedPassword());
			//System.out.println(encodedPassword);

			//boolean isPasswordMatch = bCryptPasswordEncoder.matches(password, encodedPassword);
			//System.out.println("Password : " + password + "   isPasswordMatch    : " + isPasswordMatch);

			//isPasswordMatch = bCryptPasswordEncoder.matches(password, encodedPassword);
			//System.out.println("Password : " + password + "   isPasswordMatch    : " + isPasswordMatch);

			user.setHashedPassword(encodedPassword);

			if (user.getId().equals(10L)) {
				throw new CrudOperationException("Simulated error.");
			}
			return attoreService.update(user);
		}, user -> attoreService.delete(user.getId()));

		add(crud);

	}

}