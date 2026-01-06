package fcweb.utils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class JasperReporUtils{

	public static ByteArrayInputStream runReportToPdf(InputStream inputStream,
			Map<String, Object> hm, Connection conn) throws Exception {

		byte[] b = JasperRunManager.runReportToPdf(inputStream, hm, conn);
        assert b != null;
        return new ByteArrayInputStream(b);
	}

	@SuppressWarnings("rawtypes")
	public static ByteArrayInputStream runReportToPdf(InputStream inputStream,
			Map<String, Object> hm, Collection coll) throws Exception {

		byte[] b = JasperRunManager.runReportToPdf(inputStream, hm, new JRBeanCollectionDataSource(coll));
        assert b != null;
        return new ByteArrayInputStream(b);
	}

	@SuppressWarnings("rawtypes")
	public static byte[] getReportByteCollectionDataSource(
			InputStream inputStream, Map<String, Object> hm, Collection coll) throws Exception {

        return JasperRunManager.runReportToPdf(inputStream, hm, new JRBeanCollectionDataSource(coll));
	}

	@SuppressWarnings("rawtypes")
	public static void runReportToPdfStream(InputStream inputStream,
			FileOutputStream outputStream, Map<String, Object> hm,
			Collection coll) throws Exception {

		JasperRunManager.runReportToPdfStream(inputStream, outputStream, hm, new JRBeanCollectionDataSource(coll));
	}

	public static void runReportToPdfStream(InputStream inputStream,
			FileOutputStream outputStream, Map<String, Object> hm,
			Connection conn) throws Exception {

		JasperRunManager.runReportToPdfStream(inputStream, outputStream, hm, conn);
	}

}
