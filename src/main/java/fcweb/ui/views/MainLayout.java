package fcweb.ui.views;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;

import fcweb.backend.data.entity.FcAttore;
import fcweb.security.AuthenticatedUser;
import fcweb.ui.views.admin.FcAccessoView;
import fcweb.ui.views.admin.FcCalendarioCompetizioneView;
import fcweb.ui.views.admin.FcCampionatoView;
import fcweb.ui.views.admin.FcClassificaView;
import fcweb.ui.views.admin.FcExpStatView;
import fcweb.ui.views.admin.FcFormazioneView;
import fcweb.ui.views.admin.FcGiocatoreView;
import fcweb.ui.views.admin.FcGiornataDettView;
import fcweb.ui.views.admin.FcGiornataInfoView;
import fcweb.ui.views.admin.FcGiornataView;
import fcweb.ui.views.admin.FcMercatoDettView;
import fcweb.ui.views.admin.FcPagelleView;
import fcweb.ui.views.admin.FcPropertiesView;
import fcweb.ui.views.admin.FcSquadraView;
import fcweb.ui.views.admin.FcUserView;
import fcweb.ui.views.em.EmClassificaView;
import fcweb.ui.views.em.EmDownloadView;
import fcweb.ui.views.em.EmFormazioniView;
import fcweb.ui.views.em.EmHomeView;
import fcweb.ui.views.em.EmImpostazioniView;
import fcweb.ui.views.em.EmMercatoView;
import fcweb.ui.views.em.EmRegolamentoView;
import fcweb.ui.views.em.EmSquadreView;
import fcweb.ui.views.em.EmStatisticheView;
import fcweb.ui.views.em.EmTeamInsertView;
import fcweb.ui.views.seriea.AlboView;
import fcweb.ui.views.seriea.CalendarioView;
import fcweb.ui.views.seriea.ClassificaView;
import fcweb.ui.views.seriea.DownloadView;
import fcweb.ui.views.seriea.FormazioniView;
import fcweb.ui.views.seriea.FreePlayersView;
import fcweb.ui.views.seriea.HomeView;
import fcweb.ui.views.seriea.ImpostazioniView;
import fcweb.ui.views.seriea.MercatoView;
import fcweb.ui.views.seriea.RegolamentoView;
import fcweb.ui.views.seriea.SquadreAllView;
import fcweb.ui.views.seriea.SquadreView;
import fcweb.ui.views.seriea.SqualificatiIndisponibiliView;
import fcweb.ui.views.seriea.StatisticheView;
import fcweb.ui.views.seriea.TeamInsertMobileView;
import fcweb.ui.views.seriea.TeamInsertView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout{

	private static final long serialVersionUID = 1L;

	private H2 viewTitle;

	private AuthenticatedUser authenticatedUser;
	private AccessAnnotationChecker accessChecker;

	public MainLayout(AuthenticatedUser authenticatedUser,
			AccessAnnotationChecker accessChecker) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;

		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	private void addHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		//toggle.getElement().setAttribute("aria-label", "Menu toggle");
		toggle.setAriaLabel("Menu toggle");

		viewTitle = new H2();
		viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

		addToNavbar(true, toggle, viewTitle);
	}

	private void addDrawerContent() {
		H1 appName = new H1("Fc App");
		appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
		Header header = new Header(appName);

		Scroller scroller = new Scroller(createNavigation());

		SideNav navAdmin = createNavigationAdmin();
		if (navAdmin != null) {
			addToDrawer(header, scroller, navAdmin, createFooter());
		} else {
			addToDrawer(header, scroller, createFooter());
		}
	}

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

		Optional<FcAttore> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			String type = authenticatedUser.getType();
			if ("1".equals(type)) {
				if (accessChecker.hasAccess(HomeView.class)) {
					nav.addItem(new SideNavItem("Home", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));
				}

				if (accessChecker.hasAccess(TeamInsertView.class)) {
					nav.addItem(new SideNavItem("Schera Formazione", TeamInsertView.class, LineAwesomeIcon.FUTBOL_SOLID.create()));
				}

				if (accessChecker.hasAccess(TeamInsertMobileView.class)) {
					nav.addItem(new SideNavItem("Mobile",TeamInsertMobileView.class,LineAwesomeIcon.MOBILE_SOLID.create()));
				}

				if (accessChecker.hasAccess(SquadreView.class)) {
					nav.addItem(new SideNavItem("Rose",SquadreView.class,LineAwesomeIcon.USER_SOLID.create()));
				}

				if (accessChecker.hasAccess(SquadreAllView.class)) {
					nav.addItem(new SideNavItem("Tutte le Rose",SquadreAllView.class,LineAwesomeIcon.USER_FRIENDS_SOLID.create()));
				}

				if (accessChecker.hasAccess(CalendarioView.class)) {
					nav.addItem(new SideNavItem("Calendario",CalendarioView.class,LineAwesomeIcon.CALENDAR_SOLID.create()));
				}

				if (accessChecker.hasAccess(ClassificaView.class)) {
					nav.addItem(new SideNavItem("Classifica",ClassificaView.class,LineAwesomeIcon.TABLE_SOLID.create()));
				}

				if (accessChecker.hasAccess(FormazioniView.class)) {
					nav.addItem(new SideNavItem("Formazioni",FormazioniView.class,LineAwesomeIcon.CALENDAR_CHECK_SOLID.create()));
				}

				if (accessChecker.hasAccess(StatisticheView.class)) {
					nav.addItem(new SideNavItem("Statistiche",StatisticheView.class,LineAwesomeIcon.CHART_LINE_SOLID.create()));
				}

				if (accessChecker.hasAccess(StatisticheView.class)) {
					nav.addItem(new SideNavItem("Squalificati-Indisponibili",SqualificatiIndisponibiliView.class,LineAwesomeIcon.REDDIT_SQUARE.create()));
				}

				if (accessChecker.hasAccess(DownloadView.class)) {
					nav.addItem(new SideNavItem("Download",DownloadView.class,LineAwesomeIcon.FILE_DOWNLOAD_SOLID.create()));
				}

				if (accessChecker.hasAccess(AlboView.class)) {
					nav.addItem(new SideNavItem("Albo",AlboView.class,LineAwesomeIcon.HISTORY_SOLID.create()));
				}

				if (accessChecker.hasAccess(RegolamentoView.class)) {
					nav.addItem(new SideNavItem("Regolamento",RegolamentoView.class,LineAwesomeIcon.COMMENT_SOLID.create()));
				}

			} else {

				if (accessChecker.hasAccess(EmHomeView.class)) {
					nav.addItem(new SideNavItem("Home",EmHomeView.class,LineAwesomeIcon.HOME_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmMercatoView.class)) {
					nav.addItem(new SideNavItem("Mercato",EmMercatoView.class,LineAwesomeIcon.SEARCH_DOLLAR_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmTeamInsertView.class)) {
					nav.addItem(new SideNavItem("Schera Formazione",EmTeamInsertView.class,LineAwesomeIcon.FUTBOL_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmSquadreView.class)) {
					nav.addItem(new SideNavItem("Rose",EmSquadreView.class,LineAwesomeIcon.USER_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmClassificaView.class)) {
					nav.addItem(new SideNavItem("Classifica",EmClassificaView.class,LineAwesomeIcon.TABLE_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmFormazioniView.class)) {
					nav.addItem(new SideNavItem("Formazioni",EmFormazioniView.class,LineAwesomeIcon.CALENDAR_CHECK_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmStatisticheView.class)) {
					nav.addItem(new SideNavItem("Statistiche",EmStatisticheView.class,LineAwesomeIcon.CHART_LINE_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmDownloadView.class)) {
					nav.addItem(new SideNavItem("Download",EmDownloadView.class,LineAwesomeIcon.FILE_DOWNLOAD_SOLID.create()));
				}

				if (accessChecker.hasAccess(EmRegolamentoView.class)) {
					nav.addItem(new SideNavItem("Regolamento",EmRegolamentoView.class,LineAwesomeIcon.COMMENT_SOLID.create()));
				}
			}
		}

		return nav;
    }

    private SideNav createNavigationAdmin() {

    	SideNav adminNav = new SideNav();
		adminNav.setLabel("Admin");
		adminNav.setCollapsible(true);
		adminNav.setExpanded(false);

		Optional<FcAttore> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			String type = authenticatedUser.getType();

			if ("1".equals(type)) {

				// ADMIN
				if (accessChecker.hasAccess(ImpostazioniView.class)) {
					adminNav.addItem(new SideNavItem("Impostazioni",ImpostazioniView.class,LineAwesomeIcon.TOOLS_SOLID.create()));
				} else {
					return null;
				}

				if (accessChecker.hasAccess(MercatoView.class)) {
					adminNav.addItem(new SideNavItem("Mercato",MercatoView.class,LineAwesomeIcon.SEARCH_DOLLAR_SOLID.create()));
				}

				if (accessChecker.hasAccess(FreePlayersView.class)) {
					adminNav.addItem(new SideNavItem("Free Players",FreePlayersView.class,LineAwesomeIcon.FREE_CODE_CAMP.create()));
				}

				if (accessChecker.hasAccess(FcPropertiesView.class)) {
					adminNav.addItem(new SideNavItem("Proprieta",FcPropertiesView.class,LineAwesomeIcon.TOOLBOX_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcUserView.class)) {
					adminNav.addItem(new SideNavItem("Utenti",FcUserView.class,LineAwesomeIcon.USER_EDIT_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcGiocatoreView.class)) {
					adminNav.addItem(new SideNavItem("Giocatore",FcGiocatoreView.class,LineAwesomeIcon.PLAYSTATION.create()));
				}

				if (accessChecker.hasAccess(FcGiornataInfoView.class)) {
					adminNav.addItem(new SideNavItem("GiornataInfo",FcGiornataInfoView.class,LineAwesomeIcon.INFO_CIRCLE_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcGiornataView.class)) {
					adminNav.addItem(new SideNavItem("Giornata",FcGiornataView.class,LineAwesomeIcon.CALENDAR_DAY_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcGiornataDettView.class)) {
					adminNav.addItem(new SideNavItem("GiornataDett",FcGiornataDettView.class,LineAwesomeIcon.CALENDAR_CHECK_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcFormazioneView.class)) {
					adminNav.addItem(new SideNavItem("Formazione",FcFormazioneView.class,LineAwesomeIcon.USERS_COG_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcClassificaView.class)) {
					adminNav.addItem(new SideNavItem("Classifica",FcClassificaView.class,LineAwesomeIcon.TABLET_ALT_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcMercatoDettView.class)) {
					adminNav.addItem(new SideNavItem("MercatoDett",FcMercatoDettView.class,LineAwesomeIcon.BORDER_ALL_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcPagelleView.class)) {
					adminNav.addItem(new SideNavItem("Pagelle",FcPagelleView.class,LineAwesomeIcon.POLL_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcExpStatView.class)) {
					adminNav.addItem(new SideNavItem("ExpStat",FcExpStatView.class,LineAwesomeIcon.KEYBOARD_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcCampionatoView.class)) {
					adminNav.addItem(new SideNavItem("Campionato",FcCampionatoView.class,LineAwesomeIcon.BRUSH_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcSquadraView.class)) {
					adminNav.addItem(new SideNavItem("Squadre Serie A",FcSquadraView.class,LineAwesomeIcon.STEAM_SQUARE.create()));
				}

				if (accessChecker.hasAccess(FcCalendarioCompetizioneView.class)) {
					adminNav.addItem(new SideNavItem("Calendario Serie A",FcCalendarioCompetizioneView.class,LineAwesomeIcon.CALENDAR_ALT_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcAccessoView.class)) {
					adminNav.addItem(new SideNavItem("Accesso",FcAccessoView.class,LineAwesomeIcon.UNIVERSAL_ACCESS_SOLID.create()));
				}

			} else {

				// ADMIN
				if (accessChecker.hasAccess(EmImpostazioniView.class)) {
					adminNav.addItem(new SideNavItem("Impostazioni",EmImpostazioniView.class,LineAwesomeIcon.TOOLS_SOLID.create()));
				} else {
					return null;
				}

				if (accessChecker.hasAccess(FcPropertiesView.class)) {
					adminNav.addItem(new SideNavItem("Proprieta",FcPropertiesView.class,LineAwesomeIcon.TOOLBOX_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcUserView.class)) {
					adminNav.addItem(new SideNavItem("Utenti",FcUserView.class,LineAwesomeIcon.USER_EDIT_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcGiocatoreView.class)) {
					adminNav.addItem(new SideNavItem("Giocatore",FcGiocatoreView.class,LineAwesomeIcon.PLAYSTATION.create()));
				}

				if (accessChecker.hasAccess(FcGiornataInfoView.class)) {
					adminNav.addItem(new SideNavItem("GiornataInfo",FcGiornataInfoView.class,LineAwesomeIcon.INFO_CIRCLE_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcGiornataDettView.class)) {
					adminNav.addItem(new SideNavItem("GiornataDett",FcGiornataDettView.class,LineAwesomeIcon.CALENDAR_CHECK_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcFormazioneView.class)) {
					adminNav.addItem(new SideNavItem("Formazione",FcFormazioneView.class,LineAwesomeIcon.USERS_COG_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcMercatoDettView.class)) {
					adminNav.addItem(new SideNavItem("MercatoDett",FcMercatoDettView.class,LineAwesomeIcon.BORDER_ALL_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcPagelleView.class)) {
					adminNav.addItem(new SideNavItem("Pagelle",FcPagelleView.class,LineAwesomeIcon.POLL_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcCampionatoView.class)) {
					adminNav.addItem(new SideNavItem("Campionato",FcCampionatoView.class,LineAwesomeIcon.BRUSH_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcSquadraView.class)) {
					adminNav.addItem(new SideNavItem("Squadre EM",FcSquadraView.class,LineAwesomeIcon.STEAM_SQUARE.create()));
				}

				if (accessChecker.hasAccess(FcCalendarioCompetizioneView.class)) {
					adminNav.addItem(new SideNavItem("Calendario EM",FcCalendarioCompetizioneView.class,LineAwesomeIcon.CALENDAR_ALT_SOLID.create()));
				}

				if (accessChecker.hasAccess(FcAccessoView.class)) {
					adminNav.addItem(new SideNavItem("Accesso",FcAccessoView.class,LineAwesomeIcon.UNIVERSAL_ACCESS_SOLID.create()));
				}
			}
		}

        return adminNav;
    }


	private Footer createFooter() {
		Footer layout = new Footer();

		Optional<FcAttore> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			FcAttore user = maybeUser.get();

			Avatar avatar = new Avatar(user.getName());
			StreamResource resource = new StreamResource("profile-pic",() -> new ByteArrayInputStream(user.getProfilePicture()));
			avatar.setImageResource(resource);
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			MenuBar userMenu = new MenuBar();
			userMenu.setThemeName("tertiary-inline contrast");

			MenuItem userName = userMenu.addItem("");
			Div div = new Div();
			div.add(avatar);
			div.add(user.getName());
			div.add(new Icon("lumo","dropdown"));
			div.getElement().getStyle().set("display", "flex");
			div.getElement().getStyle().set("align-items", "center");
			div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
			userName.add(div);
			userName.getSubMenu().addItem("Esci", e -> {
				authenticatedUser.logout();
			});

			layout.add(userMenu);
		} else {
			Anchor loginLink = new Anchor("login","Entra");
			layout.add(loginLink);
		}

		return layout;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}
}
