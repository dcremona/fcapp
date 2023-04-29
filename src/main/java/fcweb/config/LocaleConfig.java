package fcweb.config;

import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class LocaleConfig{

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@PostConstruct
	public void init() {

		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

		LOG.info("Date in Europe/Berlin: " + new Date().toString());

		String basePathData = System.getProperty("user.dir");
		LOG.info("basePathData " + basePathData);

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