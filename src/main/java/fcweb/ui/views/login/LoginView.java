package fcweb.ui.views.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import fcweb.security.AuthenticatedUser;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver{

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private static final long serialVersionUID = 1L;

	private final AuthenticatedUser authenticatedUser;

	public LoginView(AuthenticatedUser authenticatedUser) {
//		this.authenticatedUser = authenticatedUser;
//		setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));
//
//        LoginI18n i18n = LoginI18n.createDefault();
//        i18n.setHeader(new LoginI18n.Header());
//        i18n.getHeader().setTitle("fcapp-v24");
//        i18n.getHeader().setDescription("Login using user/user or admin/admin");
//        i18n.setAdditionalInformation(null);
//        setI18n(i18n);
//
//        setForgotPasswordButtonVisible(false);
//        setOpened(true);
		
		this.authenticatedUser = authenticatedUser;
		setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

		LoginI18n i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle("Fc App");
		// i18n.getHeader().setDescription("Login using user/user or admin/admin");
		i18n.setAdditionalInformation(null);

		LoginI18n.Form i18nForm = i18n.getForm();
		//i18nForm.setTitle("Fc App");
		i18nForm.setUsername("Email");
		i18nForm.setPassword("Password");
		i18nForm.setSubmit("Login");
		i18nForm.setForgotPassword("Password dimenticata?");
		i18nForm.setSubmit("Login");
		
		LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
		i18nErrorMessage.setTitle("Email o password non valide");
		i18nErrorMessage.setMessage("Verifica di aver inserito l'email e la password corrette e riprova.");
		i18nErrorMessage.setUsername("Email richiesta");
		i18nErrorMessage.setPassword("Password richiesta");
		i18n.setErrorMessage(i18nErrorMessage);

		setI18n(i18n);

		setForgotPasswordButtonVisible(false);
		setOpened(true);

		UI.getCurrent().getPage().retrieveExtendedClientDetails(event -> {
			int resX = event.getScreenWidth();
			int resY = event.getScreenHeight();
			LOG.info("resX " + resX);
			LOG.info("resY " + resY);
			LOG.info("Math.max " + Math.max(resX, resY));
			if (Math.max(resX, resY) < 900) {
				// small screen detected
				LOG.info("small screen detected ");
			}
		});
		
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (authenticatedUser.get().isPresent()) {
			// Already logged in
			setOpened(false);
			event.forwardTo("");
		}

		setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
	}
}
