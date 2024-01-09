package fcweb.security;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
		// return
		// authenticationContext.getAuthenticatedUser(UserDetails.class).map(userDetails
		// -> userRepository.findByUsername(userDetails.getUsername()));

		Optional<FcAttore> maybeUser = authenticationContext.getAuthenticatedUser(UserDetails.class).map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
		if (maybeUser.isPresent()) {
			FcAttore user = maybeUser.get();
			if (!user.isActive()) {
				return null;
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
		String nextDate = getNextDate(giornataInfo);

		long millisDiff = 0;
		try {
			millisDiff = getMillisDiff(nextDate, fusoOrario);
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

	private String getNextDate(FcGiornataInfo giornataInfo) {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime currentDate = LocalDateTime.now();

		LocalDateTime dataAnticipo = null;
		LocalDateTime dataAnticipo1 = giornataInfo.getDataAnticipo1();
		LocalDateTime dataAnticipo2 = giornataInfo.getDataAnticipo2();
		if (dataAnticipo1 != null && dataAnticipo2 != null) {
			if (now.isBefore(dataAnticipo1)) {
				dataAnticipo = dataAnticipo1;
			} else if (now.isAfter(dataAnticipo1)) {
				dataAnticipo = dataAnticipo2;
			}
		} else if (dataAnticipo1 == null && dataAnticipo2 != null) {
			dataAnticipo = dataAnticipo2;
		}
		LocalDateTime dataGiornata = giornataInfo.getDataGiornata();
		LocalDateTime dataPosticipo = giornataInfo.getDataPosticipo();

		if (dataGiornata != null) {
			currentDate = dataGiornata;

			if (dataAnticipo != null) {
				currentDate = dataAnticipo;
				LOG.info("now.getDayOfWeek() : " + now.getDayOfWeek());
				LOG.info("dataGiornata.getDayOfWeek() : " + dataGiornata.getDayOfWeek());
				if ( now.isAfter(dataAnticipo) && now.getDayOfWeek() == dataGiornata.getDayOfWeek() ) {
					currentDate = dataGiornata;
				}
			}
			
			if (dataPosticipo != null) {
				LOG.info("now.getDayOfWeek() : " + now.getDayOfWeek());
				LOG.info("dataPosticipo.getDayOfWeek() : " + dataPosticipo.getDayOfWeek());
				if ( now.getDayOfWeek() == dataPosticipo.getDayOfWeek() ) {
					currentDate = dataGiornata;
				}
			} else {
				if (dataAnticipo != null) {
					currentDate = dataAnticipo;	
				}
			}
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String currentDataGiornata = currentDate.format(formatter);

		return currentDataGiornata;
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

	private long getMillisDiff(String nextDate, String fusoOrario)
			throws Exception {

		Calendar c = Calendar.getInstance();
		DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String currentDataGiornata = fmt.format(c.getTime());
		// String strDate1 = "2018/12/17 19:00:00";
		// String strDate2 = "2018/12/17 20:00:00";
		String strDate1 = currentDataGiornata;
		String strDate2 = nextDate;

		fmt.setLenient(false);
		Date d1 = fmt.parse(strDate1);
		Date d2 = fmt.parse(strDate2);

		// Calculates the difference in milliseconds.
		long millisDiff = d2.getTime() - d1.getTime();
		int seconds = (int) (millisDiff / 1000 % 60);
		int minutes = (int) (millisDiff / 60000 % 60);
		int hours = (int) (millisDiff / 3600000 % 24);
		int days = (int) (millisDiff / 86400000);

		LOG.info(days + " days, ");
		LOG.info(hours + " hours, ");
		LOG.info(minutes + " minutes, ");
		LOG.info(seconds + " seconds");

		long diffFuso = Long.parseLong(fusoOrario) * 3600000;
		millisDiff = millisDiff - diffFuso;

		if (millisDiff < 0) {
			millisDiff = 0;
		}

		return millisDiff;
	}

}
