package fcapp.ui.views.user;

import java.io.Serial;
import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AttoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Profilo")
@Route(value = "profilo", layout = MainLayout.class)
@RolesAllowed("USER")
public class ProfiloView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ProfiloView.class);

	private static final String SESSION_ATTORE = "ATTORE";

	private final transient AttoreService attoreService;
	private final transient AccessoService accessoService;

	private PasswordField password1;
	private PasswordField password2;
	private Checkbox notifica;
	private Button salvaDb;

	private static final int BCRYPT_STRENGTH = 10;
	private final transient BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH,
			new SecureRandom());

	public ProfiloView(AttoreService attoreService, AccessoService accessoService) {
		log.info("Initializing {}", ProfiloView.class.getSimpleName());
		this.attoreService = attoreService;
		this.accessoService = accessoService;
	}

	@PostConstruct
	void init() {
		log.info("Running init for {}", ProfiloView.class.getSimpleName());

		if (!Utils.isValidVaadinSession()) {
			return;
		}

		accessoService.insertAccesso(getClass().getName());
		configureLayout();

		FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);

		password1 = new PasswordField();
		password2 = new PasswordField();
		notifica = new Checkbox();
		if (attore != null) {
			notifica.setValue(attore.isNotifiche());	
		}

		salvaDb = new Button("Salva");
		salvaDb.setIcon(VaadinIcon.DATABASE.create());
		salvaDb.addClickListener(this);
		add(salvaDb);

		FormLayout formLayout = new FormLayout();
		formLayout.setAutoResponsive(true);
		formLayout.setLabelsAside(true);
		formLayout.addFormItem(password1, "Nuova Password");
		formLayout.addFormItem(password2, "Conferma Password");
		formLayout.addFormItem(notifica, "Notifiche");
		formLayout.addFormItem(salvaDb,"-->");

		this.add(formLayout);
	}

	private void configureLayout() {
		setMargin(true);
		setSpacing(true);
		setSizeFull();
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		try {
			FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);

			if (event.getSource() != salvaDb) {
				return;
			}

			if (attore != null && StringUtils.isNotEmpty(password1.getValue()) && StringUtils.isNotEmpty(password2.getValue())
					&& password1.getValue().equals(password2.getValue())) {

				attore.setHashedPassword(password1.getValue());
				attore.setNotifiche(notifica.getValue());
				this.updateUser(attore);

				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
			} else {
				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_ERROR_PASSWR);
			}

		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

	private FcAttore updateUser(FcAttore user) {
		encodePassword(user);
		return attoreService.save(user);
	}

	private void encodePassword(FcAttore user) {
		if (user == null || user.getHashedPassword() == null || user.getHashedPassword().isBlank()) {
			return;
		}
		user.setHashedPassword(passwordEncoder.encode(user.getHashedPassword()));
	}

	@SuppressWarnings("unchecked")
	private <T> T getSessionAttribute(String key, Class<T> type) {
		Object value = VaadinSession.getCurrent().getAttribute(key);
		return value == null ? null : (T) value;
	}

}
