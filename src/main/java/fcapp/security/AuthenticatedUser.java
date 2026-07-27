package fcapp.security;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcProperties;
import fcapp.backend.service.AttoreRepository;
import fcapp.backend.service.CampionatoService;
import fcapp.backend.service.GiornataInfoRepository;
import fcapp.backend.service.PagelleService;
import fcapp.backend.service.ProprietaService;
import fcapp.utils.Utils;

@Component
public class AuthenticatedUser{

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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

	private void setSession(FcAttore attore) {

		List<FcProperties> lProprieta = proprietaController.findAll();
		if (lProprieta.isEmpty()) {
			return;
		}

		Properties properties = new Properties();
		for (FcProperties prop : lProprieta) {
			properties.setProperty(prop.getKey(), prop.getValue());
		}

		FcCampionato campionato = campionatoController.findByActive(true);
		if (campionato == null) {
			return;
		}
        LOG.info("Campionato: {}", campionato.getIdCampionato());
		type = campionato.getType();

		FcPagelle currentGG = pagelleController.findCurrentGiornata();
		FcGiornataInfo giornataInfo;
		if (currentGG == null) {
			giornataInfo = giornataInfoRepository.findByCodiceGiornata(1);
		} else {
			giornataInfo = currentGG.getFcGiornataInfo();
			if (currentGG.getFcGiornataInfo().getCodiceGiornata() > campionato.getEnd()) {
				giornataInfo = giornataInfoRepository.findByCodiceGiornata(campionato.getEnd());
			}
		}
        LOG.info("CurrentGG: {}", giornataInfo.getCodiceGiornata());

		String fusoOrario = properties.getProperty("FUSO_ORARIO");
		HashMap map = Utils.getNextDate(giornataInfo);
		LocalDateTime nextDate = (LocalDateTime)map.get("1");
		String nextDateFormat = (String)map.get("2");

		long millisDiff = 0;
		try {
			millisDiff = Utils.getMillisDiff(nextDateFormat, fusoOrario);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
        LOG.info("millisDiff : {}", millisDiff);
        LOG.info("Login {} success", attore.getDescAttore());

		// Set a session attribute
		VaadinSession.getCurrent().setAttribute("GIORNATA_INFO", giornataInfo);
		VaadinSession.getCurrent().setAttribute("ATTORE", attore);
		VaadinSession.getCurrent().setAttribute("PROPERTIES", properties);
		VaadinSession.getCurrent().setAttribute("CAMPIONATO", campionato);
		VaadinSession.getCurrent().setAttribute("FUTURE", nextDate);
		VaadinSession.getCurrent().setAttribute("NEXTDATE", nextDateFormat);
		VaadinSession.getCurrent().setAttribute("MILLISDIFF", millisDiff);
		VaadinSession.getCurrent().setAttribute("COUNTDOWNDATE", getCalendarCountDown(nextDateFormat, fusoOrario));

	}

	private Date getCalendarCountDown(String currentDataGiornata,
			String fusoOrario) {

		Calendar c = Calendar.getInstance();
		int dd = 0;
		int mm = 0;
		int yy = 0;
		int h = 0;
		int m = 0;
		try {
			int fuso = Integer.parseInt(fusoOrario);
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
