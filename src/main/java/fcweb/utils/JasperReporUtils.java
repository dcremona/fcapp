package fcweb.utils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class JasperReporUtils{

	public static ByteArrayInputStream runReportToPdf(InputStream inputStream,
			Map<String, Object> hm, Connection conn) {

		byte[] b = null;
		try {
			b = JasperRunManager.runReportToPdf(inputStream, hm, conn);
		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		return new ByteArrayInputStream(b);

		// return null;
	}

	@SuppressWarnings("rawtypes")
	public static ByteArrayInputStream runReportToPdf(InputStream inputStream,
			Map<String, Object> hm, Collection coll) {

		byte[] b = null;
		try {
			b = JasperRunManager.runReportToPdf(inputStream, hm, new JRBeanCollectionDataSource(coll));
		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		return new ByteArrayInputStream(b);

		// return null;
	}

	@SuppressWarnings("rawtypes")
	public static byte[] getReportByteCollectionDataSource(
			InputStream inputStream, Map<String, Object> hm, Collection coll) {

		byte[] b = null;
		try {
			b = JasperRunManager.runReportToPdf(inputStream, hm, new JRBeanCollectionDataSource(coll));
		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		return b;

		// return null;
	}

	@SuppressWarnings("rawtypes")
	public static void runReportToPdfStream(InputStream inputStream,
			FileOutputStream outputStream, Map<String, Object> hm,
			Collection coll) {

		try {
			JasperRunManager.runReportToPdfStream(inputStream, outputStream, hm, new JRBeanCollectionDataSource(coll));
		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
	}

	public static void runReportToPdfStream(InputStream inputStream,
			FileOutputStream outputStream, Map<String, Object> hm,
			Connection conn) {

		try {
			JasperRunManager.runReportToPdfStream(inputStream, outputStream, hm, conn);
		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
	}

}
