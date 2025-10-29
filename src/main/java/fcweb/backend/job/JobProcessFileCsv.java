package fcweb.backend.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

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

import fcweb.utils.Costants;

@Controller
public class JobProcessFileCsv{

	private static final Logger log = LoggerFactory.getLogger(JobProcessFileCsv.class);

	private static final int SIZE = 1024;

	private static final String EXT_HTML = ".html";
	private static final String EXT_CSV = ".csv";

	public void downloadCsv(String httpUrl, String pathCsv, String fileName,
			int headCount) throws Exception {

		log.info("downloadCsv START");

		File input = null;
		try {
			fileDownload(httpUrl, fileName + EXT_HTML, pathCsv);
			input = new File(pathCsv + fileName + EXT_HTML);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		StringBuilder data = new StringBuilder();

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");

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
						data.append(rowData);
						data.append(";");
					}
					data.append("\n");
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(pathCsv + fileName + EXT_CSV);
			if (f.exists()) {
				f.delete();
			}

			outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV);
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		log.info("downloadCsv END");
	}

	public void downloadCsvSqualificatiInfortunati(String httpUrl,
			String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvSqualificatiInfortunati START");

		File input = null;
		try {
			fileDownload(httpUrl, fileName + EXT_HTML, pathCsv);
			input = new File(pathCsv + fileName + EXT_HTML);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		StringBuilder data = new StringBuilder();

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");
		// Load ArrayList with table row strings
		for (Element tableRow : tableRows) {
			Elements trRows = tableRow.select("tr");
			for (Element trRow : trRows) {
				Elements tdRows = trRow.select("td");
				boolean bFind = false;
				String nomegic = null;
				for (Element tdRow : tdRows) {
					if (bFind) {
						String rowData = tdRow.text();
						data.append(nomegic);
						data.append(";");
						data.append(rowData);
						data.append("\n");
						bFind = false;
						nomegic = null;
					}
					Elements children = tdRow.children();

					for (Element c : children) {
						String href = c.attr("href");
						if (StringUtils.isNotEmpty(href)) {
							int idx = href.indexOf("nomegio=");
							if (idx != -1) {
								href = href.substring(idx, href.length());
								idx = href.indexOf("=");
								if (idx != -1) {
									nomegic = href.substring(idx + 1, href.length());
									log.info(nomegic);
									bFind = true;
								}
							}
						}
					}
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(pathCsv + fileName + EXT_CSV);
			if (f.exists()) {
				f.delete();
			}

			outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV);
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		log.info("downloadCsvSqualificatiInfortunati END");
	}

	public void downloadCsvProbabili(String httpUrl, String pathCsv,
			String fileName) throws Exception {

		log.info("downloadCsvProbabili START");

		File input = null;
		try {
			fileDownload(httpUrl, fileName + EXT_HTML, pathCsv);
			input = new File(pathCsv + fileName + EXT_HTML);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		StringBuilder data = new StringBuilder();

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");
		// Load ArrayList with table row strings
		for (Element tableRow : tableRows) {
			Elements trRows = tableRow.select("tr");
			for (Element trRow : trRows) {
				Elements thRows = trRow.select("th");
				for (Element tdRow : thRows) {
					String rowData = tdRow.text();
					if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1 && (Costants.TITOLARI.equals(rowData) || Costants.PANCHINA.equals(rowData))) {
						data.append(rowData);
						data.append(";");
						data.append(rowData);
						data.append("\n");
					}
				}

				Elements tdRows = trRow.select("td");
				for (Element tdRow : tdRows) {
					String rowData = tdRow.text();
					if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1) {
						data.append(rowData);
						data.append(";");
						data.append(rowData);
						data.append("\n");
					}
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(pathCsv + fileName + EXT_CSV);
			if (f.exists()) {
				f.delete();
			}

			outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV);
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		log.info("downloadCsvProbabili END");
	}

	public void downloadCsvProbabiliFantaGazzetta(String httpUrl,
			String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvProbabiliFantaGazzetta START");

		File input = null;
		try {
			fileDownload(httpUrl, fileName + EXT_HTML, pathCsv);
			input = new File(pathCsv + fileName + EXT_HTML);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		StringBuilder data = new StringBuilder();

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		Elements ulRows = doc.select("li");

		for (Element liRow : ulRows) {
			Element parent = liRow.parent();
			String classNameParent = parent.className();
			String rowData = liRow.text();
			String className = liRow.className();
			if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1 && "player-item pill".equals(className)) {
				String lastCharacter = rowData.substring(rowData.length() - 1);
				if ("%".equals(lastCharacter)) {
					Elements children = liRow.children();
					String href = null;
					for (Element c : children) {
						href = c.attr("href");
						if (StringUtils.isNotEmpty(href) && StringUtils.length(href) > 1) {
							StringBuilder percentuale = new StringBuilder();
							char[] letters = rowData.toCharArray();
							for (char l : letters) {
								Boolean flag = Character.isDigit(l);
								if (flag.booleanValue()) {
									percentuale.append(l);
								}
							}

							StringBuilder nomeImg = new StringBuilder();
							char[] letters2 = href.toCharArray();
							for (char l : letters2) {
								Boolean flag = Character.isDigit(l);
								if (flag.booleanValue()) {
									nomeImg.append(l);
								}
							}

							// log.info(" nomeImg=" + nomeImg + " percentuale="
							// + percentuale.toString() + " href " + href);

							data.append(nomeImg);
							data.append(";");
							if ("player-list starters".equals(classNameParent)) {
								data.append(Costants.TITOLARE);
							} else {
								data.append(Costants.PANCHINA);
							}
							data.append(";");
							data.append(percentuale.toString());
							data.append(";");
							data.append(href);
							data.append("\n");

						}
					}
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(pathCsv + fileName + EXT_CSV);
			if (f.exists()) {
				f.delete();
			}

			outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV);
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		log.info("downloadCsvProbabiliFantaGazzetta END");
	}

	public void downloadCsvSqualificatiInfortunatiFantaGazzetta(String httpUrl,
			String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvSqualificatiInfortunatiFantaGazzetta START");

		File input = null;
		try {
			fileDownload(httpUrl, fileName + EXT_HTML, pathCsv);
			input = new File(pathCsv + fileName + EXT_HTML);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

		StringBuilder data = new StringBuilder();

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		Elements ulRows = doc.select("ul");

		for (Element ulRow : ulRows) {
			String rowData = ulRow.text();
			String className = ulRow.className();
			Element parent =  ulRow.parent().parent();
			//String rowDataparent = parent.text();
			String classNameparent = parent.className();

			if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1 && ("injured-list".equals(className) || "suspendeds-list".equals(className))) {
				Elements children = ulRow.children();
				String href = null;

				for (Element c : children) {
					Elements childrenLi = c.children();
					for (Element li : childrenLi) {
						href = li.attr("href");
						if (StringUtils.isNotEmpty(href) && StringUtils.length(href) > 1) {
							StringBuilder nomeImg = new StringBuilder();
							char[] letters2 = href.toCharArray();
							for (char l : letters2) {
								Boolean flag = Character.isDigit(l);
								if (flag.booleanValue()) {
									nomeImg.append(l);
								}
							}

							String infoSqualificatoInfortunato = "";
							String note = "";
							if ("injured-list".equals(className)) {
								infoSqualificatoInfortunato = Costants.INFORTUNATO;
								for (Element p : childrenLi) {
									String classNameNote = p.className();
									if ("description".equals(classNameNote)) {
										note = p.text();
									}
								}
							} else if ("suspendeds-list".equals(className) && "suspendeds".equals(classNameparent)) {
								infoSqualificatoInfortunato = Costants.SQUALIFICATO;
								note = Costants.SQUALIFICATO;
							} else {
								log.info(" nomeImg=" + nomeImg + " percentuale=0" + " href " + href);
								continue;
							}
							
							data.append(nomeImg);
							data.append(";");
							data.append(infoSqualificatoInfortunato);
							data.append(";");
							data.append("0");
							data.append(";");
							data.append(href);
							data.append(";");
							data.append(note);
							data.append("\n");

						}
					}
				}
			}
		}

		FileOutputStream outputStream = null;
		try {
			// DELETE
			File f = new File(pathCsv + fileName + EXT_CSV);
			if (f.exists()) {
				f.delete();
			}

			outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV);
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		log.info("downloadCsvSqualificatiInfortunatiFantaGazzetta END");
	}

	private void fileDownload(String fAddress, String localFileName,
			String destinationDir) throws Exception {

		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager(){
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs,
					String authType) {
			}

			@Override
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
			byte[] buf;
			int byteRead = 0;
			int byteWritten = 0;
			URL url = new URL(fAddress);
			outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + localFileName));

			uCon = url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[SIZE];
			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
				byteWritten += byteRead;
			}
			log.info("File name: " + localFileName + " bytes: " + byteWritten);
			log.info("Downloaded Successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
			if (outStream != null) {
				outStream.close();
			}
		}
	}

}
