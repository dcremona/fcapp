package fcweb.backend.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;

@Controller
public class EmJobProcessFileCsv{

	private final static Log LOG = LogFactory.getLog(EmJobProcessFileCsv.class);

	final static int size = 1024;

	public void downloadCsv(String http_url, String path_csv, String fileName,
			int headCount) throws Exception {
		try {
			fileDownload(http_url, fileName + ".html", path_csv);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}

		File input = new File(path_csv + fileName + ".html");
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");

		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");

		StringBuilder data = new StringBuilder();
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
						data.append(rowData).append(";");
					}
					data.append("\n");
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
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	public void downloadCsvNoExcel(String http_url, String path_csv,

			String fileName, int headCount) throws Exception {
		try {
			LOG.debug(http_url);
			fileDownload(http_url, fileName + ".html", path_csv);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}

		File input = new File(path_csv + fileName + ".html");
		Document doc = Jsoup.parse(input, "UTF-8", "https://example.com/");

		// select all <tr> or Table Row Elements
		Elements tableRows = doc.select("table");

		HashMap<String, String> mapSQ = new HashMap<>();
		StringBuilder data = new StringBuilder();
		// Load ArrayList with table row strings
		for (Element tableRow : tableRows) {

			Elements trRows = tableRow.select("tr");
			int conta = 0;
			for (Element trRow : trRows) {
				conta++;
				if (conta > headCount) {
					Elements tdRows = trRow.select("td");

					HashMap<String, String> map = new HashMap<>();
					map.put("AMM", "0");
					map.put("ESP", "0");
					map.put("GDV", "0");

					int c = 0;
					StringBuilder rowValue = new StringBuilder();
					for (Element tdRow : tdRows) {
                        tdRow.text();
                        String rowData = tdRow.text();
						// LOG.debug(rowData);
						// R;SQ_GIOCATORE;VM;GF;GS;AU;AS;VR;GF;GS;AU;AS;VT;GF;GS;AU;AS;SB;PA;TR;SU;VM;VR;VT;M2;M3;
						if (c == 0) {
							map.put("R", rowData);
						} else if (c == 1) {
							map.put("SQ_GIOCATORE", rowData);

							Element link = tdRow.select("a").first();
							if (link != null) {
								String linkText = link.text(); // "example""
								if (StringUtils.isNotEmpty(linkText)) {
									// LOG.debug(linkText);
									map.put("SQ_GIOCATORE", linkText);
								}
							}

							List<Node> childNodes = tdRow.childNodes();
							for (Node n : childNodes) {
								String sclass = n.attr("class");
								if ("cart-rosso".equals(sclass)) {
									map.put("ESP", "1");
								} else {
									String title = n.attr("title");
									if ("ammonito".equals(title)) {
										map.put("AMM", "1");
									} else if ("Goal Decisivo".equals(title)) {
										map.put("GDV", "1");
									}
								}
							}
						} else if (c == 2) {
							map.put("VM", rowData);
						} else if (c == 3) {
							map.put("GF", "0");
							List<Node> childNodes = tdRow.childNodes();
							int gf = 0;
							for (Node n : childNodes) {
								String title = n.attr("title");
								if ("Goal Realizzati".equals(title)) {
									gf++;
								}
							}
							map.put("GF", "" + gf);
						} else if (c == 4) {
							map.put("GS", "0");
							List<Node> childNodes = tdRow.childNodes();
							int gs = 0;
							for (Node n : childNodes) {
								String title = n.attr("title");
								if ("Goal Subiti".equals(title)) {
									gs++;
								}
							}
							map.put("GS", "" + gs);
						} else if (c == 5) {
							map.put("GAU", "0");
							List<Node> childNodes = tdRow.childNodes();
							int aut = 0;
							for (Node n : childNodes) {
								String title = n.attr("title");
								if ("AutoGoal".equals(title)) {
									aut++;
								}
							}
							map.put("GAU", "" + aut);
						} else if (c == 6) {
							map.put("GAS", "");
							List<Node> childNodes = tdRow.childNodes();
							int assist = 0;
							for (Node n : childNodes) {
								String title = n.attr("title");
								if ("Assist".equals(title)) {
									assist++;
								}
							}
							map.put("GAS", "" + assist);
						} else if (c == 7) {
							map.put("VR", rowData);
						} else if (c == 8) {
							map.put("RGF", rowData);
						} else if (c == 9) {
							map.put("RGS", rowData);
						} else if (c == 10) {
							map.put("RAU", rowData);
						} else if (c == 11) {
							map.put("RAS", rowData);
						} else if (c == 12) {
							map.put("VT", rowData);
						} else if (c == 13) {
							map.put("TGF", rowData);
						} else if (c == 14) {
							map.put("TGS", rowData);
						} else if (c == 15) {
							map.put("TAU", rowData);
						} else if (c == 16) {
							map.put("TAS", rowData);
						} else if (c == 17) {
							map.put("SB", rowData);
						} else if (c == 18) {
							map.put("PA", rowData);
						} else if (c == 19) {
							map.put("TR", rowData);
						} else if (c == 20) {
							map.put("SU", rowData);
						} else if (c == 21) {
							map.put("VM2", rowData);
						} else if (c == 22) {
							map.put("VR2", rowData);
						} else if (c == 23) {
							map.put("VT2", rowData);
						} else if (c == 24) {
							map.put("M2", rowData);
						} else if (c == 25) {
							map.put("M3", rowData);
						}
						c++;
						rowValue.append(rowData).append(";");
					}

					if (rowValue.indexOf("M2;M3;") != -1) {
						String squadra = map.get("SQ_GIOCATORE");
						mapSQ.put("SQUADRA", squadra);

                    } else {
						// R;SQ_GIOCATORE;VM;GF;GS;AU;AS;VR;GF;GS;AU;AS;VT;GF;GS;AU;AS;SB;PA;TR;SU;VM;VR;VT;M2;M3;

						String ruolo = map.get("R");
						if (StringUtils.isEmpty(ruolo) || "M".equals(ruolo)) {
							continue;
						}

						data.append(map.get("R")).append(";");
						data.append(map.get("SQ_GIOCATORE")).append(";");
						data.append(mapSQ.get("SQUADRA")).append(";");
						data.append(map.get("VM")).append(";");
						data.append(map.get("GF")).append(";");
						data.append(map.get("GS")).append(";");
						data.append(map.get("GAU")).append(";");
						data.append(map.get("GAS")).append(";");
						data.append(map.get("AMM")).append(";");
						data.append(map.get("ESP")).append(";");
						data.append(map.get("SB")).append(";");
						data.append(map.get("PA")).append(";");
						data.append(map.get("TR")).append(";");
						data.append(map.get("SU")).append(";");
						data.append(map.get("GDV")).append(";");
						data.append("\n");
					}
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
			byte[] strToBytes = data.toString().getBytes();
			outputStream.write(strToBytes);

		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

	}

	public void fileDownload(String fAddress, String localFileName,
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

		OutputStream outStream = null;
		URLConnection uCon;
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
			LOG.error(e.getMessage());
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
