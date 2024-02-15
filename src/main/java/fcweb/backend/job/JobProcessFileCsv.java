package fcweb.backend.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class JobProcessFileCsv{

	private static final Logger LOG = LoggerFactory.getLogger(JobProcessFileCsv.class);

	final static int size = 1024;

	public void downloadCsv(String http_url, String path_csv, String fileName,
			int headCount) throws Exception {

		LOG.info("downloadCsv START");

		File input = null;
		try {
			fileDownload(http_url, fileName + ".html", path_csv);
			input = new File(path_csv + fileName + ".html");
			// input = new File(path_csv + "Quotazioni Giocatori - 15 giornata -
			// 2022_2023 - QATAR 2022.html");
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");

		String data = "";
		// Load ArrayList with table row strings
		for (Element tableRow : tableRows) {

			Elements trRows = tableRow.select("tr");
			int conta = 0;
			for (Element trRow : trRows) {
				conta++;
				if (conta > headCount) {
					Elements tdRows = trRow.select("td");
					for (Element tdRow : tdRows) {
						String rowData = tdRow.text();
						if (StringUtils.isEmpty(rowData)) {
							Elements img = tdRow.select("img");
							rowData = img.attr("title");
							if (StringUtils.isEmpty(rowData)) {
								rowData = img.attr("alt");
							}
						}
						// LOG.debug(rowData);
						data += rowData + ";";
					}
					data += "\n";
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(path_csv + fileName + ".csv");
			if (f.exists()) {
				f.delete();
			}
			outputStream = new FileOutputStream(path_csv + fileName + ".csv");
			byte[] strToBytes = data.getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	public ArrayList<String> downloadCsvSqualificatiInfortunati(String http_url,
			String path_csv, String fileName) throws Exception {

		ArrayList<String> listGiocatori = new ArrayList<String>();
		
		LOG.info("downloadCsvSqualificatiInfortunati START");
		File input = null;
		try {
			fileDownload(http_url, fileName + ".html", path_csv);
			input = new File(path_csv + fileName + ".html");
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");
		// Load ArrayList with table row strings
		for (Element tableRow : tableRows) {
			Elements trRows = tableRow.select("tr");
			for (Element trRow : trRows) {
				Elements tdRows = trRow.select("td");
				for (Element tdRow : tdRows) {
					Elements children = tdRow.children();
					for (Element c : children) {
						String href = c.attr("href");
						if (StringUtils.isNotEmpty(href)) {
							int idx = href.indexOf("nomegio=");
							if (idx != -1) {
								href = href.substring(idx, href.length());
								idx = href.indexOf("=");
								if (idx != -1) {
									String nomegio = href.substring(idx + 1, href.length());
									System.out.println(nomegio);
									listGiocatori.add(nomegio);
								}
							}
						}
					}
				}
			}
		}
		
		String data = "";
		for (String g : listGiocatori) {
			data += g + ";\n";
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(path_csv + fileName + ".csv");
			if (f.exists()) {
				f.delete();
			}
			outputStream = new FileOutputStream(path_csv + fileName + ".csv");
			byte[] strToBytes = data.getBytes();
			outputStream.write(strToBytes);
			
			return listGiocatori;

		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		
		return listGiocatori;
	}

	private void fileDownload(String fAddress, String localFileName,
			String destinationDir) throws Exception {

		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager(){
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs,
					String authType) {
			}
		} };

		// Activate the new trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int ByteRead,ByteWritten = 0;
			Url = new URL(fAddress);
			outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + localFileName));

			uCon = Url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}
			LOG.info("Downloaded Successfully.");
			LOG.debug("File name:\"" + localFileName + "\"\nNo ofbytes :" + ByteWritten);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}

}
