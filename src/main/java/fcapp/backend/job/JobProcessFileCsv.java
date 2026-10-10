package fcapp.backend.job;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.RuoloService;
import fcapp.backend.service.SquadraService;
import fcapp.utils.Costants;

@Controller
public class JobProcessFileCsv {

	private static final Logger log = LoggerFactory.getLogger(JobProcessFileCsv.class);

	private static final String EXT_XLSX = ".xlsx";
	private static final String EXT_HTML = ".html";
	private static final String EXT_CSV = ".csv";

	private static final String DEFAULT_BASE_URL = "https://example.com/";

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " + "AppleWebKit/537.36 "
			+ "(KHTML, like Gecko) " + "Chrome/140.0.0.0 Safari/537.36";

	private static final String PIANETAFANTA_REFERER = "https://www.pianetafanta.it/";

	private static final Pattern NOMEGIO_PATTERN = Pattern.compile("nomegio=([^&]+)");

	private final SquadraService squadraService;
	private final RuoloService ruoloService;
	private final HttpClient httpClient;

	public JobProcessFileCsv(SquadraService squadraService, RuoloService ruoloService) {

		this.squadraService = squadraService;
		this.ruoloService = ruoloService;

		this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
	}

// -------------------------------------------------------------------------
// PUBLIC API
// -------------------------------------------------------------------------

	public void downloadCsv(String httpUrl, String pathCsv, String fileName, int headCount) throws Exception {

		log.info("downloadCsv START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element table : document.select("table")) {
				appendTableRows(table, data, headCount);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsv END");
		}
	}

	public void downloadCsvCalendarioSerieA(String httpUrl, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvCalendarioSerieA START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element div : document.select("div.cal-partita-card")) {
				appendCalendarRow(div, data);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsvCalendarioSerieA END");
		}
	}

	public void downloadCsvSqualificatiInfortunati(String httpUrl, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvSqualificatiInfortunati START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element table : document.select("table")) {
				appendSuspendedInjuredRows(table, data);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsvSqualificatiInfortunati END");
		}
	}

	public void downloadCsvProbabili(String httpUrl, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvProbabili START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element table : document.select("table")) {
				appendProbabiliRows(table, data);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsvProbabili END");
		}
	}

	public void downloadCsvProbabiliFantaGazzetta(String httpUrl, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvProbabiliFantaGazzetta START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element player : document.select("li.player-item.pill")) {
				appendFantaGazzettaPlayer(player, data);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsvProbabiliFantaGazzetta END");
		}
	}

	public void downloadCsvSqualificatiInfortunatiFantaGazzetta(String httpUrl, String pathCsv, String fileName)
			throws Exception {

		log.info("downloadCsvSqualificatiInfortunatiFantaGazzetta START");

		Path htmlFile = downloadHtml(httpUrl, pathCsv, fileName);

		try {
			var document = Jsoup.parse(htmlFile.toFile(), StandardCharsets.UTF_8.name(), DEFAULT_BASE_URL);

			StringBuilder data = new StringBuilder();

			for (Element list : document.select("ul.injured-list, ul.suspendeds-list")) {
				appendFantaGazzettaSuspendedInjured(list, data);
			}

			writeCsv(pathCsv, fileName, data);

		} finally {
			log.info("downloadCsvSqualificatiInfortunatiFantaGazzetta END");
		}
	}

	public void downloadVotiXlsxCsv(String httpUrl, String pathCsv, String fileName) throws Exception {

		log.info("downloadVotiXlsxCsv START");

		try {
			Path xlsxFile = downloadXlsx(httpUrl, pathCsv, fileName);

			try (InputStream inputStream = Files.newInputStream(xlsxFile)) {
				downloadCsvFromXlsx(inputStream, pathCsv, fileName);
			}

		} catch (Exception ex) {
			log.error("Errore durante downloadVotiXlsxCsv", ex);
			throw ex;
		} finally {
			log.info("downloadVotiXlsxCsv END");
		}
	}

	public void downloadCsvFromXlsx(InputStream inputStream, String pathCsv, String fileName) throws Exception {

		log.info("downloadCsvFromXlsx START");

		try {
			String data = parseVotiWorkbook(inputStream);
			writeCsv(pathCsv, fileName, data);

		} catch (Exception ex) {
			log.error("Error in downloadCsvFromXlsx", ex);
			throw ex;
		} finally {
			log.info("downloadCsvFromXlsx END");
		}
	}

	public void downloadQuotazioniCsvFromXlsx(InputStream inputStream, String pathCsv, String fileName)
			throws Exception {

		log.info("downloadQuotazioniCsvFromXlsx START");

		try {
			String data = parseQuotazioniWorkbook(inputStream);
			writeCsv(pathCsv, fileName, data);

		} catch (Exception ex) {
			log.error("Error in downloadQuotazioniCsvFromXlsx", ex);
			throw ex;
		} finally {
			log.info("downloadQuotazioniCsvFromXlsx END");
		}
	}

// -------------------------------------------------------------------------
// HTML
// -------------------------------------------------------------------------

	private Path downloadHtml(String url, String destinationDir, String fileName)
			throws IOException, InterruptedException {

		Path output = resolvePath(destinationDir, fileName + EXT_HTML);

		log.info("Download HTML: {} -> {}", url, output);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT)
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;" + "q=0.9,image/avif,image/webp,*/*;q=0.8")
				.header("Referer", PIANETAFANTA_REFERER).GET().build();

		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		validateResponse(response);

		Files.createDirectories(output.getParent());
		Files.write(output, response.body());

		log.info("HTML scaricato: {} ({} bytes)", output, response.body().length);

		return output;
	}

	private Path downloadXlsx(String url, String destinationDir, String fileName)
			throws IOException, InterruptedException {

		Path output = resolvePath(destinationDir, fileName + EXT_XLSX);

		log.info("Download XLSX: {} -> {}", url, output);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT)
				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;" + "q=0.9,image/avif,image/webp,*/*;q=0.8")
				.header("Referer", PIANETAFANTA_REFERER).GET().build();

		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		log.info("HTTP status: {}", response.statusCode());

		validateResponse(response);

		Files.createDirectories(output.getParent());
		Files.write(output, response.body());

		log.info("File XLSX scaricato: {} ({} bytes)", output.toAbsolutePath(), response.body().length);

		return output;
	}

	private void validateResponse(HttpResponse<byte[]> response) throws IOException {

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			String body = new String(response.body(), StandardCharsets.UTF_8);

			log.error("Download fallito. HTTP status: {} - {}", response.statusCode(), body);

			throw new IOException("Download fallito. HTTP status: " + response.statusCode());
		}
	}

// -------------------------------------------------------------------------
// HTML PARSING
// -------------------------------------------------------------------------

	private void appendTableRows(Element table, StringBuilder data, int headCount) {

		int rowIndex = 0;

		for (Element row : table.select("tr")) {
			rowIndex++;

			if (rowIndex <= headCount) {
				continue;
			}

			for (Element cell : row.select("td")) {
				String value = cell.text();

				if (StringUtils.isEmpty(value)) {
					Element image = cell.selectFirst("img");

					if (image != null) {
						value = image.attr("title");

						if (StringUtils.isEmpty(value)) {
							value = image.attr("alt");
						}
					}
				}

				appendValue(data, value);
			}

			newLine(data);
		}
	}

	private void appendCalendarRow(Element partita, StringBuilder data) {

		String date = "";
		String time = "";
		String homeTeam = "";
		String awayTeam = "";
		String result = "";

		Element datetime = partita.selectFirst(".cal-partita-datetime");

		if (datetime != null) {
			Element dateElement = datetime.selectFirst(".cal-partita-date");

			if (dateElement != null) {
				date = extractTextNode(dateElement, 4);
			}

			Element timeElement = datetime.selectFirst(".cal-partita-time");

			if (timeElement != null) {
				time = extractTextNode(timeElement, 2);
			}
		}

		Element body = partita.selectFirst(".cal-partita-body");

		if (body != null) {
			Element home = body.selectFirst(".cal-partita-team:not(.cal-partita-team--away)");

			if (home != null) {
				homeTeam = home.text();
			}

			Element score = body.selectFirst(".cal-partita-score-wrap");

			if (score != null) {
				result = score.text();
			}

			Element away = body.selectFirst(".cal-partita-team.cal-partita-team--away");

			if (away != null) {
				awayTeam = away.text();
			}
		}

		appendValue(data, date + " " + time);
		appendValue(data, homeTeam);
		appendValue(data, result);
		appendValue(data, awayTeam);
		newLine(data);
	}

	private void appendSuspendedInjuredRows(Element table, StringBuilder data) {

		for (Element row : table.select("tr")) {

			String nomeGiocatore = null;
			boolean found = false;

			for (Element cell : row.select("td")) {

				if (found) {
					String rowData = cell.text();

					appendValue(data, nomeGiocatore);
					appendValue(data, rowData);
					newLine(data);

					found = false;
					nomeGiocatore = null;
				}

				for (Element child : cell.children()) {

					String href = child.attr("href");

					String nomeGio = extractNomeGioco(href);

					if (nomeGio != null) {
						nomeGiocatore = nomeGio;
						found = true;

						log.info("nomegio={}", nomeGiocatore);
					}
				}
			}
		}
	}

	private void appendProbabiliRows(Element table, StringBuilder data) {

		for (Element row : table.select("tr")) {

			for (Element header : row.select("th")) {
				String value = header.text();

				if (isValidText(value) && (Costants.TITOLARI.equals(value) || Costants.PANCHINA.equals(value))) {

					appendValue(data, value);
					appendValue(data, value);
					newLine(data);
				}
			}

			for (Element cell : row.select("td")) {
				String value = cell.text();

				if (isValidText(value)) {
					appendValue(data, value);
					appendValue(data, value);
					newLine(data);
				}
			}
		}
	}

	private void appendFantaGazzettaPlayer(Element player, StringBuilder data) {

		String rowData = player.text();

		if (!isValidText(rowData) || !rowData.endsWith("%")) {
			return;
		}

		Element parent = player.parent();

		if (parent == null) {
			return;
		}

		String playerType = "player-list starters".equals(parent.className()) ? Costants.TITOLARE : Costants.PANCHINA;

		String percentage = extractDigits(rowData);

		for (Element child : player.children()) {

			String href = child.attr("href");

			if (StringUtils.isEmpty(href)) {
				continue;
			}

			String playerId = extractDigits(href);

			if (StringUtils.isEmpty(playerId)) {
				continue;
			}

			appendValue(data, playerId);
			appendValue(data, playerType);
			appendValue(data, percentage);
			appendValue(data, href);
			newLine(data);
		}
	}

	private void appendFantaGazzettaSuspendedInjured(Element list, StringBuilder data) {

		String listClass = list.className();

		boolean injured = "injured-list".equals(listClass);

		boolean suspended = "suspendeds-list".equals(listClass);

		if (!injured && !suspended) {
			return;
		}

		for (Element child : list.children()) {

			for (Element item : child.children()) {

				String href = item.attr("href");

				if (StringUtils.isEmpty(href)) {
					continue;
				}

				String playerId = extractDigits(href);

				if (StringUtils.isEmpty(playerId)) {
					continue;
				}

				String type;
				String note = "";

				if (injured) {
					type = Costants.INFORTUNATO;

					Element description = item.selectFirst(".description");

					if (description != null) {
						note = description.text();
					}

				} else {
					type = Costants.SQUALIFICATO;
					note = Costants.SQUALIFICATO;
				}

				appendValue(data, playerId);
				appendValue(data, type);
				appendValue(data, "0");
				appendValue(data, href);
				appendValue(data, note);
				newLine(data);
			}
		}
	}

	private String extractTextNode(Element element, int childIndex) {

		if (element.childNodeSize() <= childIndex) {
			return "";
		}

		Node node = element.childNode(childIndex);

		if (node instanceof TextNode textNode) {
			return textNode.text();
		}

		return "";
	}

	private String extractNomeGioco(String href) {

		if (StringUtils.isEmpty(href)) {
			return null;
		}

		Matcher matcher = NOMEGIO_PATTERN.matcher(href);

		return matcher.find() ? matcher.group(1) : null;
	}

// -------------------------------------------------------------------------
// XLSX
// -------------------------------------------------------------------------

	private String parseVotiWorkbook(InputStream inputStream) throws IOException {

		StringBuilder data = new StringBuilder();

		try (Workbook workbook = WorkbookFactory.create(inputStream)) {

			logWorkbookSheets(workbook);

			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			int rowIndex = 0;

			for (Row row : sheet) {

				if (rowIndex++ < 3) {
					log.info("SCARTO RIGA HEADER");
					continue;
				}

				VotiRow values = readVotiRow(row, formatter);

				if (values.isEmpty()) {
					log.info("SCARTO RIGA VUOTA");
					continue;
				}

				appendVotiRow(data, values);
			}
		}

		return data.toString();
	}

	private VotiRow readVotiRow(Row row, DataFormatter formatter) {

		VotiRow values = new VotiRow();

		for (Cell cell : row) {

			String value = formatter.formatCellValue(cell);

			switch (cell.getColumnIndex()) {

			case 0 -> values.idGiocatore = value;

			case 1 -> values.cognGiocatore = value.toUpperCase();

			case 2 -> values.ruolo = value.toUpperCase();

			case 4 -> values.squadra = value.toUpperCase();

			case 5 -> values.minGiocati = value;

			case 8 -> values.g = value;

			case 9 -> values.goalRealizzato = value;

			case 10 -> values.goalSubito = value;

			case 11 -> values.autorete = value;

			case 12 -> values.assist = value;

			case 14 -> values.cs = value;

			case 20 -> values.ts = value;

			case 27 -> values.m3 = value;

			case 28 -> values.ammonizione = value;

			case 29 -> values.espulsione = value;

			case 32 -> values.rigoreFallito = value;

			case 33 -> values.rigoreParato = value;

			case 34 -> values.rigoreSegnato = value;

			default -> {
				// Colonna non utilizzata
			}
			}
		}

		return values;
	}

	private void appendVotiRow(StringBuilder data, VotiRow values) {

		appendValue(data, values.idGiocatore);
		appendValue(data, values.cognGiocatore);
		appendValue(data, values.ruolo);
		appendValue(data, values.squadra);
		appendValue(data, values.minGiocati);
		appendValue(data, values.g);
		appendValue(data, values.goalRealizzato);
		appendValue(data, values.goalSubito);
		appendValue(data, values.autorete);
		appendValue(data, values.assist);
		appendValue(data, values.cs);
		appendValue(data, values.ts);
		appendValue(data, values.m3);
		appendValue(data, values.ammonizione);
		appendValue(data, values.espulsione);
		appendValue(data, values.rigoreFallito);
		appendValue(data, values.rigoreParato);
		appendValue(data, values.rigoreSegnato);

		newLine(data);
	}

	private String parseQuotazioniWorkbook(InputStream inputStream) throws IOException {

		StringBuilder data = new StringBuilder();

		appendValue(data, "idGiocatore");
		appendValue(data, "giocatore");
		appendValue(data, "r");
		appendValue(data, "r1");
		appendValue(data, "squadra");
		appendValue(data, "qi");
		appendValue(data, "qa");
		newLine(data);

		try (Workbook workbook = WorkbookFactory.create(inputStream)) {

			logWorkbookSheets(workbook);

			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (Row row : sheet) {

				QuotazioneRow values = readQuotazioneRow(row, formatter);

				if (values.isEmpty()) {
					log.info("SCARTO RIGA VUOTA");
					continue;
				}

				FcRuolo ruolo = ruoloService.findByIdRuolo(values.r);

				if (ruolo == null) {
					log.error("FcRuolo null: {}", values.r);
					continue;
				}

				FcSquadra squadra = squadraService.findByNomeSquadra(values.squadra);

				if (squadra == null) {
					log.error("FcSquadra null: {}", values.squadra);
					continue;
				}

				appendQuotazioneRow(data, values);
			}
		}

		return data.toString();
	}

	private QuotazioneRow readQuotazioneRow(Row row, DataFormatter formatter) {

		QuotazioneRow values = new QuotazioneRow();

		for (Cell cell : row) {

			String value = formatter.formatCellValue(cell);

			switch (cell.getColumnIndex()) {

			case 0 -> values.idGiocatore = value.toUpperCase();

			case 1 -> values.r = value.toUpperCase();

			case 2 -> values.r1 = value.toUpperCase();

			case 3 -> values.giocatore = value.toUpperCase();

			case 4 -> values.squadra = value;

			case 5 -> values.qi = value;

			case 6 -> values.qa = value;

			default -> {
				// Colonna non utilizzata
			}
			}
		}

		return values;
	}

	private void appendQuotazioneRow(StringBuilder data, QuotazioneRow values) {

		appendValue(data, values.idGiocatore);
		appendValue(data, values.giocatore);
		appendValue(data, values.r);
		appendValue(data, values.r1);
		appendValue(data, values.squadra);
		appendValue(data, values.qi);
		appendValue(data, values.qa);

		newLine(data);
	}

	private void logWorkbookSheets(Workbook workbook) {

		log.info("Workbook has {} sheets", workbook.getNumberOfSheets());

		for (Sheet sheet : workbook) {
			log.info("Sheet: {}", sheet.getSheetName());
		}
	}

// -------------------------------------------------------------------------
// FILE / CSV
// -------------------------------------------------------------------------

	private void writeCsv(String directory, String fileName, CharSequence data) throws IOException {

		Path output = resolvePath(directory, fileName + EXT_CSV);

		Files.createDirectories(output.getParent());

		Files.writeString(output, data.toString(), StandardCharsets.UTF_8);

		log.info("CSV scritto: {} ({} bytes)", output.toAbsolutePath(), Files.size(output));
	}

	private Path resolvePath(String directory, String fileName) {

		return Path.of(directory).resolve(fileName).normalize();
	}

	private void appendValue(StringBuilder data, String value) {

		data.append(value == null ? "" : value);
		data.append(';');
	}

	private void newLine(StringBuilder data) {
		data.append(System.lineSeparator());
	}

	private boolean isValidText(String value) {
		return StringUtils.isNotEmpty(value) && value.length() > 1;
	}

	private String extractDigits(String value) {

		if (StringUtils.isEmpty(value)) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		for (char character : value.toCharArray()) {
			if (Character.isDigit(character)) {
				result.append(character);
			}
		}

		return result.toString();
	}

// -------------------------------------------------------------------------
// INTERNAL DTOs
// -------------------------------------------------------------------------

	private static final class VotiRow {

		private String idGiocatore = "";
		private String cognGiocatore = "";
		private String ruolo = "";
		private String squadra = "";
		private String minGiocati = "";
		private String g = "";
		private String goalRealizzato = "";
		private String goalSubito = "";
		private String autorete = "";
		private String assist = "";
		private String cs = "";
		private String ts = "";
		private String m3 = "";
		private String ammonizione = "";
		private String espulsione = "";
		private String rigoreFallito = "";
		private String rigoreParato = "";
		private String rigoreSegnato = "";

		private boolean isEmpty() {
			return StringUtils.isEmpty(cognGiocatore) && StringUtils.isEmpty(ruolo) && StringUtils.isEmpty(squadra)
					&& StringUtils.isEmpty(idGiocatore);
		}
	}

	private static final class QuotazioneRow {

		private String idGiocatore = "";
		private String r = "";
		private String r1 = "";
		private String giocatore = "";
		private String squadra = "";
		private String qi = "";
		private String qa = "";

		private boolean isEmpty() {
			return StringUtils.isEmpty(idGiocatore) && StringUtils.isEmpty(r) && StringUtils.isEmpty(giocatore)
					&& StringUtils.isEmpty(squadra) && StringUtils.isEmpty(qa);
		}
	}

}
