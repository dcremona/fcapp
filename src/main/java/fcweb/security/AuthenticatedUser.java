package fcweb.security;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.service.AttoreRepository;
import fcweb.backend.service.CampionatoService;
import fcweb.backend.service.GiornataInfoRepository;
import fcweb.backend.service.PagelleService;
import fcweb.backend.service.ProprietaService;

@Component
public class AuthenticatedUser{

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final AttoreRepository userRepository;

	private final AuthenticationContext authenticationContext;

	public AuthenticatedUser(AuthenticationContext authenticationContext,
			AttoreRepository userRepository) {
		this.userRepository = userRepository;
		this.authenticationContext = authenticationContext;
	}

	public Optional<FcAttore> get() {
		Optional<FcAttore> maybeUser = authenticationContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
		if (maybeUser.isPresent()) {
			FcAttore user = maybeUser.get();
			if (!user.isActive()) {
				return maybeUser;
			}
			setSession(user);
		}
		return maybeUser;

	}

	public void logout() {
		authenticationContext.logout();
	}

	@Autowired
	private CampionatoService campionatoController;

	@Autowired
	private ProprietaService proprietaController;

	@Autowired
	private PagelleService pagelleController;

	@Autowired
	private GiornataInfoRepository giornataInfoRepository;
	
	private String type = null; 

	private boolean setSession(FcAttore attore) {

		List<FcProperties> lProprieta = proprietaController.findAll();
		if (lProprieta.size() == 0) {
			// CustomMessageDialog.showMessageError("Contattare amministratore!
			// (loadProperties)");
			return false;
		}

		Properties properties = new Properties();
		for (FcProperties prop : lProprieta) {
			properties.setProperty(prop.getKey(), prop.getValue());
		}

		FcCampionato campionato = campionatoController.findByActive(true);
		if (campionato == null) {
			// CustomMessageDialog.showMessageError("Contattare amministratore!
			// (campionato=null)");
			return false;
		}
		LOG.info("Campionato: " + campionato.getIdCampionato());
		type = campionato.getType();

		FcPagelle currentGG = pagelleController.findCurrentGiornata();
		FcGiornataInfo giornataInfo = null;
		if (currentGG == null) {
			giornataInfo = giornataInfoRepository.findByCodiceGiornata(Integer.valueOf(1));
		} else {
			giornataInfo = currentGG.getFcGiornataInfo();
			if (currentGG.getFcGiornataInfo().getCodiceGiornata() > campionato.getEnd()) {
				giornataInfo = giornataInfoRepository.findByCodiceGiornata(campionato.getEnd());
			}
		}
		LOG.info("CurrentGG: " + giornataInfo.getCodiceGiornata());

		String fusoOrario = (String) properties.getProperty("FUSO_ORARIO");
		String nextDate = Utils.getNextDate(giornataInfo);

		long millisDiff = 0;
		try {
			millisDiff = Utils.getMillisDiff(nextDate, fusoOrario);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		LOG.info("millisDiff : " + millisDiff);
		LOG.info("Login " + attore.getDescAttore() + " success");

		// Set a session attribute
		VaadinSession.getCurrent().setAttribute("GIORNATA_INFO", giornataInfo);
		VaadinSession.getCurrent().setAttribute("ATTORE", attore);
		VaadinSession.getCurrent().setAttribute("PROPERTIES", properties);
		VaadinSession.getCurrent().setAttribute("CAMPIONATO", campionato);
		VaadinSession.getCurrent().setAttribute("NEXTDATE", nextDate);
		VaadinSession.getCurrent().setAttribute("MILLISDIFF", millisDiff);
		VaadinSession.getCurrent().setAttribute("COUNTDOWNDATE", getCalendarCountDown(nextDate, fusoOrario));

		return true;
	}

	private Date getCalendarCountDown(String currentDataGiornata,
			String FUSO_ORARIO) {

		Calendar c = Calendar.getInstance();
		int dd = 0;
		int mm = 0;
		int yy = 0;
		int h = 0;
		int m = 0;
		try {
			int fuso = Integer.parseInt(FUSO_ORARIO);
			dd = Integer.parseInt(currentDataGiornata.substring(0, 2));
			mm = Integer.parseInt(currentDataGiornata.substring(3, 5)) - 1;
			yy = Integer.parseInt(currentDataGiornata.substring(6, 10));
			h = Integer.parseInt(currentDataGiornata.substring(11, 13)) - fuso;
			m = Integer.parseInt(currentDataGiornata.substring(14, 16));
		} catch (Exception e) {
			LOG.error("getCalendarCountDown ");
		}
		c.set(yy, mm, dd, h, m, 0);

		return c.getTime();
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}
