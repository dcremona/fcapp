package fcapp.backend.service;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import fcapp.backend.data.FormazioneJasper;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class JasperPdfService {

	private final DataSource dataSource;

	public JasperPdfService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public byte[] generaPdf(String jasper,Map<String, Object> parametri) throws Exception {

		try (InputStream report = getClass().getResourceAsStream(jasper)) {

			if (report == null) {
				throw new IllegalStateException("Report Jasper non trovato");
			}

			try (Connection conn = dataSource.getConnection()) {

				return JasperRunManager.runReportToPdf(report, parametri, conn);
			}
		}
	}

	public byte[] generaPdf(String jasper,Map<String, Object> parametri,List<FormazioneJasper> coll) throws Exception {

		try (InputStream report = getClass().getResourceAsStream(jasper)) {

			if (report == null) {
				throw new IllegalStateException("Report Jasper non trovato");
			}

			try (Connection conn = dataSource.getConnection()) {

				return JasperRunManager.runReportToPdf(report, parametri, new JRBeanCollectionDataSource(coll));
			}
		}
	}
}