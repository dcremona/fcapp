package fcapp.backend.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import fcapp.utils.Costants;

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

		assert input != null;
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");

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

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {

			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

			// Path path = Paths.get(pathCsv + fileName + EXT_CSV);
			// cleanUp(path);

		} catch (Exception e) {
			log.error(e.getMessage());
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

		assert input != null;
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");
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
								href = href.substring(idx);
								idx = href.indexOf("=");
								if (idx != -1) {
									nomegic = href.substring(idx + 1);
									log.info(nomegic);
									bFind = true;
								}
							}
						}
					}
				}
			}
		}

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {

			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

			// Path path = Paths.get(pathCsv + fileName + EXT_CSV);
			// cleanUp(path);

		} catch (Exception e) {
			log.error(e.getMessage());
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

		assert input != null;
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");
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

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {

			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

			// Path path = Paths.get(pathCsv + fileName + EXT_CSV);
			// cleanUp(path);

		} catch (Exception e) {
			log.error(e.getMessage());
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

		assert input != null;
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");

		Elements ulRows = doc.select("li");

		for (Element liRow : ulRows) {
			Element parent = liRow.parent();
			assert parent != null;
			String classNameParent = parent.className();
			String rowData = liRow.text();
			String className = liRow.className();
			if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1 && "player-item pill".equals(className)) {
				String lastCharacter = rowData.substring(rowData.length() - 1);
				if ("%".equals(lastCharacter)) {
					Elements children = liRow.children();
					String href;
					for (Element c : children) {
						href = c.attr("href");
						if (StringUtils.isNotEmpty(href) && StringUtils.length(href) > 1) {
							StringBuilder percentuale = new StringBuilder();
							char[] letters = rowData.toCharArray();
							for (char l : letters) {
								boolean flag = Character.isDigit(l);
								if (flag) {
									percentuale.append(l);
								}
							}

							StringBuilder nomeImg = new StringBuilder();
							char[] letters2 = href.toCharArray();
							for (char l : letters2) {
								boolean flag = Character.isDigit(l);
								if (flag) {
									nomeImg.append(l);
								}
							}

							data.append(nomeImg);
							data.append(";");
							if ("player-list starters".equals(classNameParent)) {
								data.append(Costants.TITOLARE);
							} else {
								data.append(Costants.PANCHINA);
							}
							data.append(";");
							data.append(percentuale);
							data.append(";");
							data.append(href);
							data.append("\n");

						}
					}
				}
			}
		}

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {

			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

			// Path path = Paths.get(pathCsv + fileName + EXT_CSV);
			// cleanUp(path);

		} catch (Exception e) {
			log.error(e.getMessage());
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

		assert input != null;
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");

		Elements ulRows = doc.select("ul");

		for (Element ulRow : ulRows) {
			String rowData = ulRow.text();
			String className = ulRow.className();
			assert ulRow.parent() != null;
			Element parent = ulRow.parent().parent();
			// String rowDataparent = parent.text();
			assert parent != null;
			String classNameparent = parent.className();

			if (StringUtils.isNotEmpty(rowData) && StringUtils.length(rowData) > 1 && ("injured-list".equals(className) || "suspendeds-list".equals(className))) {
				Elements children = ulRow.children();
				String href;

				for (Element c : children) {
					Elements childrenLi = c.children();
					for (Element li : childrenLi) {
						href = li.attr("href");
						if (StringUtils.isNotEmpty(href) && StringUtils.length(href) > 1) {
							StringBuilder nomeImg = new StringBuilder();
							char[] letters2 = href.toCharArray();
							for (char l : letters2) {
								boolean flag = Character.isDigit(l);
								if (flag) {
									nomeImg.append(l);
								}
							}

							String infoSqualificatoInfortunato;
							String note = "";
							if ("injured-list".equals(className)) {
								infoSqualificatoInfortunato = Costants.INFORTUNATO;
								for (Element p : childrenLi) {
									String classNameNote = p.className();
									if ("description".equals(classNameNote)) {
										note = p.text();
									}
								}
							} else if ("suspendeds".equals(classNameparent)) {
								infoSqualificatoInfortunato = Costants.SQUALIFICATO;
								note = Costants.SQUALIFICATO;
							} else {
								log.info(" nomeImg={} percentuale=0 href {}", nomeImg, href);
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

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {

			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

			// Path path = Paths.get(pathCsv + fileName + EXT_CSV);
			// cleanUp(path);

		} catch (Exception e) {
			log.error(e.getMessage());
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
		} catch (Exception ignored) {
		}

		URLConnection uCon;
		InputStream is = null;

		try (OutputStream outStream = new BufferedOutputStream(new FileOutputStream(destinationDir + localFileName))) {
			byte[] buf;
			int byteRead;
			int byteWritten = 0;
			URL url = new URL(fAddress);

			uCon = url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[SIZE];
			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
				byteWritten += byteRead;
			}
			log.info("File name: {} bytes: {}", localFileName, byteWritten);
			log.info("Downloaded Successfully.");
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public void downloadCsvFromXlsx(InputStream is, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvFromXlsx START");

		StringBuilder data = new StringBuilder();

		try {

			Workbook workbook = WorkbookFactory.create(is);

			// Retrieving the number of sheets in the Workbook
			log.info("Workbook has {} Sheets : ", workbook.getNumberOfSheets());

			// 1. You can obtain a sheetIterator and iterate over it
			Iterator<Sheet> sheetIterator = workbook.sheetIterator();
			log.info("Retrieving Sheets using Iterator");
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				log.info("=> {}", sheet.getSheetName());
			}

			// Getting the Sheet at index zero
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter dataFormatter = new DataFormatter();

			int conta = 0;
			for (Row row : sheet) {

				if (conta == 0 || conta == 1 || conta == 2) {
					conta++;
					log.info("SCARTO RIGA HEADER ");
					continue;
				}

				String idGiocatore = "";
				String cognGiocatore = "";
				String ruolo = "";
				String squadra = "";
				String minGiocati = "";
				String g = "";
				String goalRealizzato = "";
				String goalSubito = "";
				String autorete = "";
				String assist = "";
				String cs = "";
				String ts = "";
				String m3 = "";
				String ammonizione = "";
				String espulsione = "";
				String rigoreFallito = "";
				String rigoreParato = "";
				String rigoreSegnato = "";
				
				for (Cell cell : row) {

					String cellValue = dataFormatter.formatCellValue(cell);
					if (cell.getColumnIndex() == 0) {
						idGiocatore = cellValue;
					} else if (cell.getColumnIndex() == 1) {
						cognGiocatore = cellValue.toUpperCase();
					} else if (cell.getColumnIndex() == 2) {
						ruolo = cellValue.toUpperCase();
					} else if (cell.getColumnIndex() == 4) {
						squadra = cellValue.toUpperCase();
					} else if (cell.getColumnIndex() == 5) {
						minGiocati = cellValue;
					} else if (cell.getColumnIndex() == 6) {
						g = cellValue;
					} else if (cell.getColumnIndex() == 7) {
						goalRealizzato = cellValue;
					} else if (cell.getColumnIndex() == 8) {
						goalSubito = cellValue;
					} else if (cell.getColumnIndex() == 9) {
						autorete = cellValue;
					} else if (cell.getColumnIndex() == 10) {
						assist = cellValue;
					} else if (cell.getColumnIndex() == 11) {
						cs = cellValue;
					} else if (cell.getColumnIndex() == 16) {
						ts = cellValue;
					} else if (cell.getColumnIndex() == 22) {
						m3 = cellValue;
					} else if (cell.getColumnIndex() == 23) {
						ammonizione = cellValue;
					} else if (cell.getColumnIndex() == 24) {
						espulsione = cellValue;
					} else if (cell.getColumnIndex() == 27) {
						rigoreFallito = cellValue;
					} else if (cell.getColumnIndex() == 28) {
						rigoreParato = cellValue;
					} else if (cell.getColumnIndex() == 29) {
						rigoreSegnato = cellValue;
					}
				}
				
				if (StringUtils.isEmpty(cognGiocatore) && StringUtils.isEmpty(ruolo)
						&& StringUtils.isEmpty(squadra) && StringUtils.isEmpty(idGiocatore)) {
					log.info("SCARTO RIGA VUOTA ");
					continue;
				}
				
				data.append(idGiocatore);
				data.append(";");
				data.append(cognGiocatore);
				data.append(";");
				data.append(ruolo);
				data.append(";");
				data.append(squadra);
				data.append(";");
				data.append(minGiocati);
				data.append(";");
				data.append(g);
				data.append(";");
				data.append(goalRealizzato);
				data.append(";");
				data.append(goalSubito);
				data.append(";");
				data.append(autorete);
				data.append(";");
				data.append(assist);
				data.append(";");
				data.append(cs);
				data.append(";");
				data.append(ts);
				data.append(";");
				data.append(m3);
				data.append(";");
				data.append(ammonizione);
				data.append(";");
				data.append(espulsione);
				data.append(";");
				data.append(rigoreFallito);
				data.append(";");
				data.append(rigoreParato);
				data.append(";");
				data.append(rigoreSegnato);
				data.append(";");
				data.append("\n");
			}


		} catch (Exception e) {
			log.error("Error in downloadCsvFromXlsx !!!");
			throw e;
		}

		try (FileOutputStream outputStream = new FileOutputStream(pathCsv + fileName + EXT_CSV)) {
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

		log.info("downloadCsvFromXlsx END");
	}

	// public void cleanUp(Path path) throws IOException {
	// log.info("START cleanUp... ");
	// Files.delete(path);
	// log.info("END cleanUp Successfully.");
	// }

}
