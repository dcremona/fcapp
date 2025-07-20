package fcweb.config;

import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fcweb.backend.data.entity.FcProperties;
import fcweb.backend.service.ProprietaService;
import jakarta.annotation.PostConstruct;

@Configuration
public class LocaleConfig{

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProprietaService proprietaController;


	@PostConstruct
	public void init() {

		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

		LOG.info("Date in Europe/Berlin: " + new Date().toString());

		String basePathData = System.getProperty("user.dir");
		LOG.info("basePathData " + basePathData);

	}

	@Bean
	public String getCronValueUfficiosi() {
		FcProperties p = proprietaController.findByKey("ufficiosi.cron.expression");
		if (p != null) {
			LOG.info("Ufficiosi cron " + p.getValue());
			return p.getValue();
		} else {
			return "0 0 9 * * *";
		}
	}

	@Bean
	public String getCronValueUfficiali() {
		FcProperties p = proprietaController.findByKey("ufficiali.cron.expression");
		if (p != null) {
			LOG.info("Ufficiali cron " + p.getValue());
			return p.getValue();
		} else {
			return "0 30 16 * * *";
		}
	}

//	public static String createFolderData() {
//		String basePathData = System.getProperty("user.dir");
//		// LOG.info("user.dir " + basePathData);
//		if (!basePathData.equals("/")) {
//			basePathData = basePathData + "/data/";
//		}
//		// LOG.info("basePathData " + basePathData);
//		File f = new File(basePathData);
//		if (!f.exists()) {
//			boolean flag = f.mkdir();
//			if (!flag) {
//				// LOG.info("ERROR mkdir - NOT exist " + basePathData);
//			}
//		}
//		basePathData = "/home/myuser/";
//		return basePathData;
//	}

}