package fcweb.backend.job;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.data.entity.FcPagelleId;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AttoreRepository;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.GiocatoreRepository;
import fcweb.backend.service.GiornataDettRepository;
import fcweb.backend.service.GiornataInfoRepository;
import fcweb.backend.service.PagelleRepository;
import fcweb.backend.service.SquadraRepository;
import fcweb.backend.service.StatisticheRepository;
import fcweb.utils.Costants;

@Controller
public class EmJobProcessGiornata{

	private final static Log LOG = LogFactory.getLog(EmJobProcessGiornata.class);

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private GiornataDettRepository giornataDettRepository;

	@Autowired
	private AttoreRepository attoreRepository;

	@Autowired
	private PagelleRepository pagelleRepository;

	@Autowired
	private GiornataInfoRepository giornataInfoRepository;

	@Autowired
	private GiocatoreRepository giocatoreRepository;

	@Autowired
	private StatisticheRepository statisticheRepository;

	@Autowired
	private SquadraRepository squadraRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping(value = "/eminitPagelle", method = RequestMethod.POST)
	@ResponseBody
	public void eminitPagelle(Integer giornata) {
		LOG.info("START eminitPagelle");
		FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(giornata);
		LOG.debug("" + giornataInfo.getCodiceGiornata());
		List<FcGiocatore> giocatores = (List<FcGiocatore>) giocatoreRepository.findAll();
		for (FcGiocatore giocatore : giocatores) {
			FcPagelle pagelle = new FcPagelle();
			FcPagelleId pagellePK = new FcPagelleId();
			pagellePK.setIdGiornata(giornataInfo.getCodiceGiornata());
			pagellePK.setIdGiocatore(giocatore.getIdGiocatore());
			pagelle.setId(pagellePK);
			pagelleRepository.save(pagelle);
		}
		LOG.info("END eminitPagelle");
	}

	@RequestMapping(value = "/emaggiornamentoPFGiornataOLD", method = RequestMethod.POST)
	@ResponseBody
	public void emaggiornamentoPFGiornataOLD(String fileName)
			throws IOException {

		LOG.info("START emaggiornamentoPFGiornataOLD");
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				// LOG.info("");
				// LOG.info("ID_GIORNATA " +record.get(0));
				// LOG.info("SQUADRA " +record.get(1));
				// LOG.info("COGN_GIOCATORE " + record.get(2));
				// LOG.info("ID_GIOCATORE " +record.get(3));
				// LOG.info("VOTO_GIOCATORE " + record.get(4));
				// LOG.info("GOAL_REALIZZATO " +record.get(5));
				// LOG.info("GOAL_SUBITO "+record.get(6));
				// LOG.info("AMMONIZIONE "+record.get(7));
				// LOG.info("ESPULSIONE "+record.get(8));
				// LOG.info("RIGORE_SEGNATO "+record.get(9));
				// LOG.info("RIGORE_FALLITO "+record.get(10));
				// LOG.info("RIGORE_PARATO " +record.get(11));
				// LOG.info("AUTORETE "+record.get(12));
				// LOG.info("ASSIST "+record.get(13));
				// LOG.info("GDV "+record.get(14));
				// LOG.info("GDP "+record.get(15));

				FcGiocatore giocatore = null;
				List<FcGiocatore> listGiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(record.get(2));
				if (listGiocatore != null && listGiocatore.size() == 1) {
					giocatore = listGiocatore.get(0);
				}

				if (giocatore != null) {

					String G = record.get(4);
					// PORTIERE SV
					if ("P".equals(giocatore.getFcRuolo().getIdRuolo())) {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "6";
						}
						// LOG.info("PORTIERE s.v.: "+COGN_GIOCATORE);
					} else {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "0";
							// if ( !ASSIST.equals("0") || !AUT.equals("0") ||
							// !GOAL_REALIZZATO.equals("0") ) {
							// G = "6";
							// }
						}
					}

					BigDecimal bgG = new BigDecimal(G);
					BigDecimal mG = new BigDecimal("10");
					BigDecimal risG = bgG.multiply(mG);
					long votoG = risG.longValue();

					String goal_realizzato = StringUtils.isEmpty(record.get(5)) ? "0" : record.get(5);
					String goal_subito = StringUtils.isEmpty(record.get(6)) ? "0" : record.get(6);
					String rigore_segnato = StringUtils.isEmpty(record.get(9)) ? "0" : record.get(9);
					String rigore_fallito = StringUtils.isEmpty(record.get(10)) ? "0" : record.get(10);
					String rigore_parato = StringUtils.isEmpty(record.get(11)) ? "0" : record.get(11);
					String autorete = StringUtils.isEmpty(record.get(12)) ? "0" : record.get(12);
					String assist = StringUtils.isEmpty(record.get(13)) ? "0" : record.get(13);

					String update = "update fc_pagelle set voto_giocatore=" + votoG;
					update += ",goal_realizzato=" + goal_realizzato;
					update += ",goal_subito=" + goal_subito;
					update += ",ammonizione=" + record.get(7);
					update += ",espulsione=" + record.get(8);
					update += ",rigore_segnato=" + rigore_segnato;
					update += ",rigore_fallito=" + rigore_fallito;
					update += ",rigore_parato=" + rigore_parato;
					update += ",autorete=" + autorete;
					update += ",assist=" + assist;
					update += ",gdv=" + record.get(14);
					update += ",gdp=" + record.get(15);
					update += " where id_giocatore=" + giocatore.getIdGiocatore();
					update += " and id_giornata=" + record.get(0);
					// LOG.info(update);
					jdbcTemplate.update(update);

				} else {
					LOG.info(record.get(2) + " " + record.get(1));
				}
			}

			LOG.info("END emaggiornamentoPFGiornata");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in CsvFileReader !!!");
		} finally {
			if (fileReader != null) {
				fileReader.close();
			}
			if (csvFileParser != null) {
				csvFileParser.close();
			}
		}
	}

	@RequestMapping(value = "/emaggiornamentoPFGiornata", method = RequestMethod.POST)
	@ResponseBody
	public void emaggiornamentoPFGiornata(Properties p, String fileName,
			String idGiornata) {

		LOG.info("START emaggiornamentoPFGiornata");

		FileReader fileReader = null;

		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		// CSVFormat csvFileFormat =
		// CSVFormat.EXCEL.withHeader(PAGELLE_HEADER_MAPPING);
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// String infoVoti = "";
			String infoNewGiocatore = "";

			String formazioneHtml = "";
			formazioneHtml += "<html><head><title>FC</title></head>\n";
			formazioneHtml += "<body>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";

			formazioneHtml += "<table>";

			formazioneHtml += "<tr>";
			formazioneHtml += "<td>";
			formazioneHtml += Costants.GIOCATORE;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "count_sv ";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "New_Voto ";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "G";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "CS";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "TS";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "Minuti Giocati";
			formazioneHtml += "</td>";
			formazioneHtml += "</tr>";

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);
				// LOG.info("" + record.size());

				int c = 0;
				String idGiocatore = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				try {
					Integer.parseInt(idGiocatore);
				} catch (Exception e) {
					continue;
				}

				c++;
				String cognGiocatore = StringUtils.isEmpty(record.get(c)) ? "" : record.get(c);
				c++;
				String ruolo = StringUtils.isEmpty(record.get(c)) ? "" : record.get(c);
				c++;
				// String Ruolo2 = record.get(3);
				c++;
				String squadra = record.get(c);
				c++;
				String minGiocati = record.get(c);
				c++;
				String G = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String goal_realizzato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String goal_subito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String autorete = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String assist = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String CS = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				// String GF= record.get(11);
				c++;
				// String GS= record.get(12);
				c++;
				// String Aut= record.get(13);
				c++;
				// String Ass= record.get(14);
				c++;
				String TS = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				// String GF= record.get(16);
				c++;
				// String GS= record.get(17);
				c++;
				// String Aut = StringUtils.isEmpty(record.get(18)) ? "0" :
				// record.get(18);
				c++;
				// String Ass = StringUtils.isEmpty(record.get(19)) ? "0" :
				// record.get(19);
				c++;
				// String M2 = record.get(20);
				c++;
				String M3 = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String ammonizione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String espulsione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String Gdv = record.get(c);
				c++;
				String Gdp = record.get(c);
				c++;
				String rigore_fallito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGS
				c++;
				String rigore_parato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGP
				c++;
				String rigore_segnato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RT
				c++;
				// String rigore_subito = StringUtils.isEmpty(record.get(c)) ?
				// "0" : record.get(c); // RS
				c++;
				// String T = record.get(30);
				c++;
				// String VG = record.get(31);
				c++;
				// String VC = record.get(32);
				c++;
				// String VTS = record.get(0);

				FcGiocatore giocatore = null;
				if (StringUtils.isNotEmpty(idGiocatore)) {
					giocatore = this.giocatoreRepository.findByIdGiocatore(Integer.parseInt(idGiocatore));
					if (giocatore == null) {
						List<FcGiocatore> listGiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
						if (listGiocatore != null && listGiocatore.size() == 1) {
							giocatore = listGiocatore.get(0);
						}
					}
				}

				if (giocatore != null) {

					int count_sv = 0;

					G = Utils.replaceString(G, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "6";
							// LOG.debug("PORTIERE s.v.: "+Giocatore);
						}
					} else {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "0";
							count_sv++;
						}
					}
					BigDecimal bgG = new BigDecimal(G);
					BigDecimal mG = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risG = bgG.multiply(mG);
					long votoG = risG.longValue();

					CS = Utils.replaceString(CS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,")) {
							CS = "6";
						}
					} else {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,")) {
							CS = "0";
							count_sv++;
						}
					}

					BigDecimal bgCS = new BigDecimal(CS);
					BigDecimal mCS = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risCS = bgCS.multiply(mCS);
					long votoCS = risCS.longValue();

					TS = Utils.replaceString(TS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,")) {
							TS = "6";
						}
					} else {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,")) {
							TS = "0";
							count_sv++;
						}
					}

					BigDecimal bgTS = new BigDecimal(TS);
					BigDecimal mTS = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risTS = bgTS.multiply(mTS);
					long votoTS = risTS.longValue();

					String VOTO_GIOCATORE = Utils.replaceString(M3, ",", ".");
					// PORTIERE SV
					if (VOTO_GIOCATORE.equals("s.v.") || VOTO_GIOCATORE.equals("s,v,") && ruolo.equals("P")) {
						VOTO_GIOCATORE = "6";
					} else {
						if (VOTO_GIOCATORE.equals("s.v.") || VOTO_GIOCATORE.equals("s,v,")) {
							VOTO_GIOCATORE = "0";
						}
					}

					// BigDecimal bg = new BigDecimal(VOTO_GIOCATORE);
					// BigDecimal m = new BigDecimal(Costants.DIVISORE_10);
					// BigDecimal ris = bg.multiply(m);
					// long voto = ris.longValue();
					// LOG.debug("voto M3 " + voto);

					if (count_sv == 1) {
						if ("0".equals(G)) {
							if (votoCS <= votoTS) {
								G = CS;
							} else {
								G = TS;
							}
							// LOG.info("G = " + G + " CS " + CS + " TS " + TS
							// );
						} else if ("0".equals(CS)) {
							if (votoG <= votoTS) {
								CS = G;
							} else {
								CS = TS;
							}
							// LOG.info("CS = " + CS + " G " + G + " TS " + TS
							// );
						} else if ("0".equals(TS)) {
							if (votoG <= votoCS) {
								TS = G;
							} else {
								TS = CS;
							}
							// LOG.info("TS = " + TS + " G " + G + " CS " + CS
							// );
						}
					} else if (count_sv == 2) {
						// LOG.info("count_sv = " + count_sv + " set all 0 ");
						G = "0";
						CS = "0";
						TS = "0";
					}

					String divide = "3";
					BigDecimal _bgG = new BigDecimal(G);
					BigDecimal _bgCS = new BigDecimal(CS);
					BigDecimal _bgTS = new BigDecimal(TS);
					BigDecimal _tot0 = _bgG.add(_bgCS);
					BigDecimal _tot1 = _tot0.add(_bgTS);
					BigDecimal _media = _tot1.divide(new BigDecimal(divide), 2, RoundingMode.HALF_UP);
					BigDecimal _moltipl = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal _ris = _media.multiply(_moltipl);
					long new_voto = _ris.longValue();

					if (count_sv == 1 || count_sv == 2) {
						// LOG.info("new_voto - count_sv " + count_sv + " - " +
						// giocatore.getCognGiocatore() + " new_voto " +
						// new_voto + " G = " + G + " CS " + CS + " TS " + TS);
						// infoVoti += "\n" + "new_voto - count_sv " + count_sv
						// + " - " + giocatore.getCognGiocatore() + " new_voto "
						// + new_voto + " G = " + G + " CS " + CS + " TS " + TS
						// + " minutiGiocati " + minGiocati;

						formazioneHtml += "<tr>";
						formazioneHtml += "<td>";
						formazioneHtml += giocatore.getCognGiocatore();
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += count_sv;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += new_voto;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += G;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += CS;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += TS;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += minGiocati;
						formazioneHtml += "</td>";
						formazioneHtml += "</tr>";
					}

					String update = "update fc_pagelle set voto_giocatore=" + votoG;
					update += ",g=" + votoG;
					update += ",cs=" + votoCS;
					update += ",ts=" + votoTS;
					update += ",goal_realizzato=" + goal_realizzato;
					update += ",goal_subito=" + goal_subito;
					update += ",ammonizione=" + ammonizione;
					update += ",espulsione=" + espulsione;
					update += ",rigore_segnato=" + rigore_segnato;
					update += ",rigore_fallito=" + rigore_fallito;
					update += ",rigore_parato=" + rigore_parato;
					update += ",autorete=" + autorete;
					update += ",assist=" + assist;
					update += ",gdv=" + Gdv;
					update += ",gdp=" + Gdp;
					update += " where id_giocatore=" + idGiocatore;
					update += " and id_giornata=" + idGiornata;

					jdbcTemplate.update(update);

				} else {
					LOG.info("*************************");
					LOG.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
					LOG.info("*************************");

					infoNewGiocatore += "\n<br>" + "NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra;
				}
			}

			String email_destinatario = p.getProperty("to");
			String[] to = null;
			if (email_destinatario != null && !email_destinatario.equals("")) {
				to = Utils.tornaArrayString(email_destinatario, ";");
			}
			String[] cc = null;
			String[] bcc = null;
			String[] att = null;
			String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;

			// String message = "\n";
			// message += infoVoti;
			// message += "\n\n\n";
			// message += infoNewGiocatore;

			formazioneHtml += "</table>\n";

			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";

			formazioneHtml += "<p>" + infoNewGiocatore + "</p>\n";

			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<p>Ciao Davide</p>\n";
			formazioneHtml += "</body>\n";
			formazioneHtml += "<html>";

			try {
				String from = env.getProperty("spring.mail.secondary.username");
				emailService.sendMail(false, from, to, cc, bcc, subject, formazioneHtml, "text/html", att);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				try {
					String from = env.getProperty("spring.mail.primary.username");
					emailService.sendMail(true, from, to, cc, bcc, subject, formazioneHtml, "text/html", att);
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
				}
			}

			LOG.info("END emaggiornamentoPFGiornata");

		} catch (Exception e) {
			LOG.error("Error in CsvFileReader !!!" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
				csvFileParser.close();
			} catch (IOException e) {
				LOG.error("Error while closing fileReader/csvFileParser !!!");
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(value = "/emaggiornamentoPFGiornataNoExcel", method = RequestMethod.POST)
	@ResponseBody
	public void emaggiornamentoPFGiornataNoExcel(Properties p, String fileName,
			String idGiornata) {

		LOG.info("START emaggiornamentoPFGiornata");

		FileReader fileReader = null;

		CSVParser csvFileParser = null;
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// String infoVoti = "";
			String infoNewGiocatore = "";

			String formazioneHtml = "";
			formazioneHtml += "<html><head><title>FC</title></head>\n";
			formazioneHtml += "<body>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";

			formazioneHtml += "<table>";

			formazioneHtml += "<tr>";
			formazioneHtml += "<td>";
			formazioneHtml += Costants.GIOCATORE;
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "count_sv ";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "New_Voto ";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "G";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "CS";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "TS";
			formazioneHtml += "</td>";
			formazioneHtml += "<td>";
			formazioneHtml += "Minuti Giocati";
			formazioneHtml += "</td>";
			formazioneHtml += "</tr>";

			for (CSVRecord record : csvRecords) {
				String idGiocatore = "";
				String minGiocati = "";

				int c = 0;
				String ruolo = StringUtils.isEmpty(record.get(c)) ? "" : record.get(c);
				c++;
				String cognGiocatore = StringUtils.isEmpty(record.get(c)) ? "" : record.get(c);
				c++;
				String squadra = StringUtils.isEmpty(record.get(c)) ? "" : record.get(c);
				c++;
				String G = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String goal_realizzato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String goal_subito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String autorete = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String assist = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String ammonizione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String espulsione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
				c++;
				String rigore_fallito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGS
				c++;
				String rigore_parato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGP
				c++;
				String rigore_segnato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RT
				c++;
				// String rigore_subito = StringUtils.isEmpty(record.get(c)) ?
				// "0" : record.get(c);// RS
				c++;
				String Gdv = record.get(c);

				String CS = "0";
				String TS = "0";
				String M3 = "0";

				FcGiocatore giocatore = null;
				// if (StringUtils.isNotEmpty(idGiocatore)) {
				// giocatore =
				// this.giocatoreRepository.findByIdGiocatore(Integer.parseInt(idGiocatore));
				// if (giocatore == null) {
				// List<FcGiocatore> listGiocatore =
				// this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
				// if (listGiocatore != null && listGiocatore.size() == 1) {
				// giocatore = listGiocatore.get(0);
				// idGiocatore = "" + giocatore.getIdGiocatore();
				// }
				// }
				// }

				FcRuolo fcRuolo = new FcRuolo();
				fcRuolo.setIdRuolo(ruolo);
				FcSquadra fcSquadra = squadraRepository.findByNomeSquadra(squadra);
				if (squadra == null) {
					LOG.info("SCARTO " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
					continue;
				}
				// LOG.info("FIND " + cognGiocatore+ " squadra " +squadra);
				giocatore = this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(cognGiocatore, fcSquadra, fcRuolo);
				// giocatore =
				// this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadra(cognGiocatore,fcSquadra);

				if (giocatore != null) {

					idGiocatore = "" + giocatore.getIdGiocatore();

					int count_sv = 0;

					G = Utils.replaceString(G, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "6";
							// LOG.debug("PORTIERE s.v.: "+Giocatore);
						}
					} else {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "0";
							count_sv++;
						}
					}
					BigDecimal bgG = new BigDecimal(G);
					BigDecimal mG = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risG = bgG.multiply(mG);
					long votoG = risG.longValue();

					CS = Utils.replaceString(CS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,")) {
							CS = "6";
						}
					} else {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,")) {
							CS = "0";
							count_sv++;
						}
					}

					BigDecimal bgCS = new BigDecimal(CS);
					BigDecimal mCS = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risCS = bgCS.multiply(mCS);
					long votoCS = risCS.longValue();

					TS = Utils.replaceString(TS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,")) {
							TS = "6";
						}
					} else {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,")) {
							TS = "0";
							count_sv++;
						}
					}

					BigDecimal bgTS = new BigDecimal(TS);
					BigDecimal mTS = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal risTS = bgTS.multiply(mTS);
					long votoTS = risTS.longValue();

					String VOTO_GIOCATORE = Utils.replaceString(M3, ",", ".");
					// PORTIERE SV
					if (VOTO_GIOCATORE.equals("s.v.") || VOTO_GIOCATORE.equals("s,v,") && ruolo.equals("P")) {
						VOTO_GIOCATORE = "6";
					} else {
						if (VOTO_GIOCATORE.equals("s.v.") || VOTO_GIOCATORE.equals("s,v,")) {
							VOTO_GIOCATORE = "0";
						}
					}

					// BigDecimal bg = new BigDecimal(VOTO_GIOCATORE);
					// BigDecimal m = new BigDecimal(Costants.DIVISORE_10);
					// BigDecimal ris = bg.multiply(m);
					// long voto = ris.longValue();
					// LOG.debug("voto M3 " + voto);

					if (count_sv == 1) {
						if ("0".equals(G)) {
							if (votoCS <= votoTS) {
								G = CS;
							} else {
								G = TS;
							}
							// LOG.info("G = " + G + " CS " + CS + " TS " + TS);
						} else if ("0".equals(CS)) {
							if (votoG <= votoTS) {
								CS = G;
							} else {
								CS = TS;
							}
							// LOG.info("CS = " + CS + " G " + G + " TS " + TS);
						} else if ("0".equals(TS)) {
							if (votoG <= votoCS) {
								TS = G;
							} else {
								TS = CS;
							}
							// LOG.info("TS = " + TS + " G " + G + " CS " + CS);
						}
					} else if (count_sv == 2) {
						// LOG.info("count_sv = " + count_sv + " set all 0 ");
						G = "0";
						CS = "0";
						TS = "0";
					}

					String divide = "3";
					BigDecimal _bgG = new BigDecimal(G);
					BigDecimal _bgCS = new BigDecimal(CS);
					BigDecimal _bgTS = new BigDecimal(TS);
					BigDecimal _tot0 = _bgG.add(_bgCS);
					BigDecimal _tot1 = _tot0.add(_bgTS);
					BigDecimal _media = _tot1.divide(new BigDecimal(divide), 2, RoundingMode.HALF_UP);
					BigDecimal _moltipl = new BigDecimal(Costants.DIVISORE_10);
					BigDecimal _ris = _media.multiply(_moltipl);
					long new_voto = _ris.longValue();

					if (count_sv == 1 || count_sv == 2) {
						LOG.info("new_voto - count_sv " + count_sv + " - " + giocatore.getCognGiocatore() + " new_voto " + new_voto + " G = " + G + " CS " + CS + " TS " + TS);
						// infoVoti += "\n" + "new_voto - count_sv " + count_sv
						// + " - " + giocatore.getCognGiocatore() + " new_voto "
						// + new_voto + " G = " + G + " CS " + CS + " TS " + TS
						// + " minutiGiocati " + minGiocati;

						formazioneHtml += "<tr>";
						formazioneHtml += "<td>";
						formazioneHtml += giocatore.getCognGiocatore();
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += count_sv;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += new_voto;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += G;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += CS;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += TS;
						formazioneHtml += "</td>";
						formazioneHtml += "<td>";
						formazioneHtml += minGiocati;
						formazioneHtml += "</td>";
						formazioneHtml += "</tr>";
					}

					String update = "update fc_pagelle set voto_giocatore=" + votoG;
					update += ",g=" + votoG;
					update += ",cs=" + votoCS;
					update += ",ts=" + votoTS;
					update += ",goal_realizzato=" + goal_realizzato;
					update += ",goal_subito=" + goal_subito;
					update += ",ammonizione=" + ammonizione;
					update += ",espulsione=" + espulsione;
					update += ",rigore_segnato=" + rigore_segnato;
					update += ",rigore_fallito=" + rigore_fallito;
					update += ",rigore_parato=" + rigore_parato;
					update += ",autorete=" + autorete;
					update += ",assist=" + assist;
					update += ",gdv=" + Gdv;
					// update += ",gdp=" + Gdp;
					update += " where id_giocatore=" + idGiocatore;
					update += " and id_giornata=" + idGiornata;

					jdbcTemplate.update(update);

				} else {
					LOG.info("*************************");
					LOG.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
					LOG.info("*************************");

					infoNewGiocatore += "\n<br>" + "NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra;
				}
			}

			String email_destinatario = p.getProperty("to");
			String[] to = null;
			if (email_destinatario != null && !email_destinatario.equals("")) {
				to = Utils.tornaArrayString(email_destinatario, ";");
			}
			String[] cc = null;
			String[] bcc = null;
			String[] att = null;
			String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;
			// String message = "\n";
			// message += infoVoti;
			// message += "\n\n\n";
			// message += infoNewGiocatore;
			formazioneHtml += "</table>\n";

			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";

			formazioneHtml += "<p>" + infoNewGiocatore + "</p>\n";

			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<br>\n";
			formazioneHtml += "<p>Ciao Davide</p>\n";
			formazioneHtml += "</body>\n";
			formazioneHtml += "<html>";

			try {
				String from = env.getProperty("spring.mail.secondary.username");
				emailService.sendMail(false, from, to, cc, bcc, subject, formazioneHtml, "text/html", att);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				try {
					String from = env.getProperty("spring.mail.primary.username");
					emailService.sendMail(true, from, to, cc, bcc, subject, formazioneHtml, "text/html", att);
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
				}
			}

			LOG.info("END emaggiornamentoPFGiornata");

		} catch (Exception e) {
			LOG.error("Error in CsvFileReader !!!" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
				csvFileParser.close();
			} catch (IOException e) {
				LOG.error("Error while closing fileReader/csvFileParser !!!");
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(value = "/emalgoritmo", method = RequestMethod.POST)
	@ResponseBody
	public void emalgoritmo(Integer giornata, FcCampionato campionato)
			throws Exception {

		LOG.info("START emalgoritmo");

		LOG.info("giornata " + giornata);

		List<String> schemi = new ArrayList<>();
		schemi.add("1-5-4-1");
		schemi.add("1-5-3-2");
		schemi.add("1-4-5-1");
		schemi.add("1-4-4-2");
		schemi.add("1-4-3-3");
		schemi.add("1-3-5-2");
		schemi.add("1-3-4-3");

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcAttore> l = attoreRepository.findAll();

		for (FcAttore attore : l) {
			LOG.info("attore " + attore.getDescAttore());
			List<FcGiornataDett> lGiocatori = giornataDettRepository.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

			ArrayList<FcGiornataDett> titolari = new ArrayList<>();
			ArrayList<FcGiornataDett> riserve = new ArrayList<>();
			ArrayList<FcGiornataDett> novoto = new ArrayList<>();

			int totPunti = 0;
			for (FcGiornataDett gd : lGiocatori) {

				FcPagelle fcPagelle = gd.getFcPagelle();
				int ordinamento = gd.getOrdinamento();
				int idGiocatore = gd.getFcGiocatore().getIdGiocatore();
				int votoGiocatore = buildFantaMedia(gd.getFcPagelle());
				Double votoGazzetta = fcPagelle.getG();
				int votoG = 0;
				if (votoGazzetta != null) {
					votoG = votoGazzetta.intValue();
				}

				String sql = " update fc_giornata_dett set voto = " + votoGiocatore;
				if (ordinamento < 12) {

					if (votoGiocatore == 0 && votoG == 0) {
						novoto.add(gd);
					} else {
						titolari.add(gd);
					}

					totPunti = totPunti + votoGiocatore;
					sql += " , flag_attivo='S'";
					sql += " , id_stato_giocatore='T'";
				} else {
					riserve.add(gd);
					sql += " , flag_attivo='N'";
					sql += " , id_stato_giocatore='R'";
				}
				sql += " where id_giornata = " + giornata;
				sql += " and id_attore = " + attore.getIdAttore();
				sql += " and id_giocatore = " + idGiocatore;
				jdbcTemplate.execute(sql);
			}

			int countCambi = 0;
			ArrayList<FcGiornataDett> novotoProcess = new ArrayList<>();

			for (FcGiornataDett ris : riserve) {

				int votoGiocatore = buildFantaMedia(ris.getFcPagelle());
				String idRuolo = ris.getFcGiocatore().getFcRuolo().getIdRuolo();
				if (votoGiocatore != 0) {

					for (FcGiornataDett sv : novoto) {

						String idRuolo2 = sv.getFcGiocatore().getFcRuolo().getIdRuolo();

						if (!novotoProcess.contains(sv) && idRuolo2.equals(idRuolo)) {

							String sql = " update fc_giornata_dett set flag_attivo='N', id_stato_giocatore='R'";
							sql += " where id_giornata = " + giornata;
							sql += " and id_attore = " + attore.getIdAttore();
							sql += " and id_giocatore = " + sv.getFcGiocatore().getIdGiocatore();
							jdbcTemplate.execute(sql);

							sql = " update fc_giornata_dett set flag_attivo='S', id_stato_giocatore='T'";
							sql += " where id_giornata = " + giornata;
							sql += " and id_attore = " + attore.getIdAttore();
							sql += " and id_giocatore = " + ris.getFcGiocatore().getIdGiocatore();
							jdbcTemplate.execute(sql);

							totPunti = totPunti + votoGiocatore;

							countCambi++;
							titolari.add(ris);
							novotoProcess.add(sv);
							break;
						}
					}
				}

				if (countCambi == 3) {
					break;
				}
			}

			for (FcGiornataDett nvp : novotoProcess) {
				novoto.remove(nvp);
			}

			int countP = 0;
			int countD = 0;
			int countC = 0;
			int countA = 0;
			String nextSchema = "";
			for (FcGiornataDett tit : titolari) {

				int idx = riserve.indexOf(tit);
				if (idx != -1) {
					riserve.remove(tit);
				}

				if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("P")) {
					countP++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("D")) {
					countD++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("C")) {
					countC++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("A")) {
					countA++;
				}
				nextSchema = countP + "-" + countD + "-" + countC + "-" + countA;
			}
			LOG.debug("NUOVO SCHEMA 1" + attore.getDescAttore() + " " + nextSchema);

			// START FORZA CAMBI RIMASTI IN BASE ORDINE INSERITI (NON VIENE
			// CONSIDERATO IL RUOLO
			// AVVIENE IL CAMBIO DI SCHEMA
			if (countCambi < 3) {

				for (FcGiornataDett ris : riserve) {

					int votoGiocatore = buildFantaMedia(ris.getFcPagelle());
					String idRuolo = ris.getFcGiocatore().getFcRuolo().getIdRuolo();

					if ("P".equals(idRuolo) && countP == 1) {
						continue;
					} else if ("D".equals(idRuolo) && countD == 5) {
						continue;
					} else if ("C".equals(idRuolo) && countC == 5) {
						continue;
					} else if ("A".equals(idRuolo) && countA == 3) {
						continue;
					}

					if (votoGiocatore != 0) {

						for (FcGiornataDett sv : novoto) {

							String idRuolo2 = sv.getFcGiocatore().getFcRuolo().getIdRuolo();
							if ("P".equals(idRuolo2)) {
								if (!"P".equals(idRuolo)) {
									continue;
								}
							}

							if (!novotoProcess.contains(sv) && !idRuolo2.equals(idRuolo)) {

								String sql = " update fc_giornata_dett set flag_attivo='N', id_stato_giocatore='R'";
								sql += " where id_giornata = " + giornata;
								sql += " and id_attore = " + attore.getIdAttore();
								sql += " and id_giocatore = " + sv.getFcGiocatore().getIdGiocatore();
								jdbcTemplate.execute(sql);

								sql = " update fc_giornata_dett set flag_attivo='S', id_stato_giocatore='T'";
								sql += " where id_giornata = " + giornata;
								sql += " and id_attore = " + attore.getIdAttore();
								sql += " and id_giocatore = " + ris.getFcGiocatore().getIdGiocatore();
								jdbcTemplate.execute(sql);

								totPunti = totPunti + votoGiocatore;

								countCambi++;
								titolari.add(ris);
								novotoProcess.add(sv);
								break;
							}
						}
					}

					if (countCambi == 3) {
						break;
					}
				}
			}
			// END

			countP = 0;
			countD = 0;
			countC = 0;
			countA = 0;
			nextSchema = "";
			for (FcGiornataDett tit : titolari) {
				if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("P")) {
					countP++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("D")) {
					countD++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("C")) {
					countC++;
				} else if (tit.getFcGiocatore().getFcRuolo().getIdRuolo().equals("A")) {
					countA++;
				}
				nextSchema = countP + "-" + countD + "-" + countC + "-" + countA;
			}
			LOG.debug("NUOVO SCHEMA 2" + attore.getDescAttore() + " " + nextSchema);

			String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + attore.getIdAttore() + " AND ID_GIORNATA=" + giornata + "";
			jdbcTemplate.update(query);

			query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT) VALUES (" + campionato.getIdCampionato() + "," + attore.getIdAttore() + "," + giornata + "," + totPunti + ")";
			jdbcTemplate.update(query);
		}

		LOG.info("END emalgoritmo");

	}

	@RequestMapping(value = "/ricalcolaTotPunti", method = RequestMethod.POST)
	@ResponseBody
	public void ricalcolaTotPunti(Integer giornata, FcCampionato campionato)
			throws Exception {

		LOG.info("START ricalcolaTotPunti");

		LOG.info("giornata " + giornata);

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcAttore> l = attoreRepository.findAll();
		for (FcAttore attore : l) {
			String sql = " select sum(voto) from fc_giornata_dett";
			sql += " where id_attore=" + attore.getIdAttore();
			sql += " and id_giornata=" + giornata;
			sql += " and id_stato_giocatore='T'";
			sql += " and flag_attivo='S'";
			jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
				@Override
				public String extractData(ResultSet rs)
						throws SQLException, DataAccessException {
					int totPunti = 0;
					while (rs.next()) {
						totPunti = rs.getInt(1);
						LOG.debug(attore.getDescAttore() + " " + totPunti);

						String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + attore.getIdAttore() + " AND ID_GIORNATA=" + giornata + "";
						jdbcTemplate.update(query);

						query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT) VALUES (" + campionato.getIdCampionato() + "," + attore.getIdAttore() + "," + giornata + "," + totPunti + ")";
						jdbcTemplate.update(query);

					}
					return "1";
				}
			});

		}
		LOG.info("END ricalcolaTotPunti");
	}

	private int buildFantaMedia(FcPagelle pagelle) {

		int votoGiocatore = pagelle.getVotoGiocatore();
		if (votoGiocatore == 0) {
			return votoGiocatore;
		}
		int goalRealizzato = pagelle.getGoalRealizzato();
		int goalSubito = pagelle.getGoalSubito();
		int ammonizione = pagelle.getAmmonizione();
		int espulso = pagelle.getEspulsione();
		int rigoreFallito = pagelle.getRigoreFallito();
		int rigoreParato = pagelle.getRigoreParato();
		int autorete = pagelle.getAutorete();
		int assist = pagelle.getAssist();
		int gdv = pagelle.getGdv();

		if (goalRealizzato != 0) {
			votoGiocatore = votoGiocatore + (goalRealizzato * 30);
		}
		if (goalSubito != 0) {
			votoGiocatore = votoGiocatore - (goalSubito * 10);
		}
		if (ammonizione != 0) {
			votoGiocatore = votoGiocatore - 10;
		}
		if (espulso != 0) {
			if (ammonizione != 0) {
				votoGiocatore = votoGiocatore + 10;
			}
			votoGiocatore = votoGiocatore - 20;
		}
		if (rigoreFallito != 0) {
			votoGiocatore = votoGiocatore - (rigoreFallito * 30);
		}
		if (rigoreParato != 0) {
			votoGiocatore = votoGiocatore + (rigoreParato * 30);
		}
		if (autorete != 0) {
			votoGiocatore = votoGiocatore - (autorete * 20);
		}
		if (assist != 0) {
			votoGiocatore = votoGiocatore + (assist * 10);
		}
		if (gdv == 1) {
			votoGiocatore = votoGiocatore + 10;
		}

		return votoGiocatore;

	}

	private Sort sortBy() {
		return Sort.by(Sort.Direction.ASC, "fcGiocatore");
	}

	public void emstatistiche(int giornata) throws Exception {

		LOG.info("START emstatistiche");

		List<FcPagelle> lPagelle = (List<FcPagelle>) pagelleRepository.findAll(sortBy());

		int giocate = 0;

		FcPagelle pagelle = lPagelle.get(0);
		int appoIdGiocatore = pagelle.getFcGiocatore().getIdGiocatore();
		FcGiocatore fcGiocatore = null;

		int votoGiocatore = 0;
		int fantaMedia = 0;
		int goalRealizzato = 0;
		int goalSubito = 0;
		int ammonizione = 0;
		int espulso = 0;
		int rigoreFallito = 0;
		int rigoreSegnato = 0;
		int assist = 0;

		for (FcPagelle p : lPagelle) {

			fcGiocatore = p.getFcGiocatore();
			int idGiocatore = fcGiocatore.getIdGiocatore();
			// LOG.info("idGiocatore " + idGiocatore);

			if (idGiocatore == appoIdGiocatore) {

				if (p.getVotoGiocatore() > 0) {

					votoGiocatore += p.getVotoGiocatore();
					fantaMedia += buildFantaMedia(p);
					goalRealizzato += p.getGoalRealizzato();
					goalSubito += p.getGoalSubito();
					ammonizione += p.getAmmonizione();
					espulso += p.getEspulsione();
					rigoreFallito += p.getRigoreFallito();
					rigoreSegnato += p.getRigoreSegnato();
					assist += p.getAssist();

					giocate = giocate + 1;
				}
			} else {

				FcGiocatore appoFcGiocatore = this.giocatoreRepository.findByIdGiocatore(appoIdGiocatore);

				FcStatistiche statistiche = new FcStatistiche();
				statistiche.setIdGiocatore(appoFcGiocatore.getIdGiocatore());
				statistiche.setCognGiocatore(appoFcGiocatore.getCognGiocatore());
				statistiche.setIdRuolo(appoFcGiocatore.getFcRuolo().getIdRuolo());
				statistiche.setNomeSquadra(appoFcGiocatore.getFcSquadra().getNomeSquadra());

				statistiche.setProprietario(null);
				statistiche.setAmmonizione(ammonizione);
				statistiche.setAssist(assist);
				statistiche.setEspulsione(espulso);
				statistiche.setGiocate(giocate);
				statistiche.setGoalFatto(goalRealizzato);
				statistiche.setGoalSubito(goalSubito);
				double mediaVoto = 0.0;
				if (giocate > 0) {
					mediaVoto = votoGiocatore / giocate;
				}
				statistiche.setMediaVoto(mediaVoto);
				double fantaMediaVoto = 0.0;
				if (giocate > 0) {
					fantaMediaVoto = fantaMedia / giocate;
				}
				statistiche.setFantaMedia(fantaMediaVoto);
				statistiche.setRigoreSbagliato(rigoreFallito);
				statistiche.setRigoreSegnato(rigoreSegnato);
				statistiche.setFcGiocatore(appoFcGiocatore);
				statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

				// LOG.info("SAVE STATISTICA GIOCATORE " +
				// appoFcGiocatore.getIdGiocatore() + " " +
				// appoFcGiocatore.getCognGiocatore());

				statisticheRepository.save(statistiche);

				appoIdGiocatore = idGiocatore;

				votoGiocatore = p.getVotoGiocatore();
				fantaMedia = buildFantaMedia(p);
				goalRealizzato = p.getGoalRealizzato();
				goalSubito = p.getGoalSubito();
				ammonizione = p.getAmmonizione();
				espulso = p.getEspulsione();
				rigoreFallito = p.getRigoreFallito();
				rigoreSegnato = p.getRigoreSegnato();
				assist = p.getAssist();

				giocate = 0;
				if (p.getVotoGiocatore() > 0) {
					giocate = giocate + 1;
				}
			}
		}

		FcGiocatore appoFcGiocatore = this.giocatoreRepository.findByIdGiocatore(appoIdGiocatore);

		FcStatistiche statistiche = new FcStatistiche();
		statistiche.setIdGiocatore(appoFcGiocatore.getIdGiocatore());
		statistiche.setCognGiocatore(appoFcGiocatore.getCognGiocatore());
		statistiche.setIdRuolo(appoFcGiocatore.getFcRuolo().getIdRuolo());
		statistiche.setNomeSquadra(appoFcGiocatore.getFcSquadra().getNomeSquadra());

		statistiche.setProprietario(null);
		statistiche.setAmmonizione(ammonizione);
		statistiche.setAssist(assist);
		statistiche.setEspulsione(espulso);
		statistiche.setGiocate(giocate);
		statistiche.setGoalFatto(goalRealizzato);
		statistiche.setGoalSubito(goalSubito);
		double mediaVoto = 0.0;
		if (giocate > 0) {
			mediaVoto = votoGiocatore / giocate;
		}
		statistiche.setMediaVoto(mediaVoto);
		double fantaMediaVoto = 0.0;
		if (giocate > 0) {
			fantaMediaVoto = fantaMedia / giocate;
		}
		statistiche.setFantaMedia(fantaMediaVoto);
		statistiche.setRigoreSbagliato(rigoreFallito);
		statistiche.setRigoreSegnato(rigoreSegnato);
		statistiche.setFcGiocatore(appoFcGiocatore);
		statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

		statisticheRepository.save(statistiche);

		LOG.info("END emstatistiche");

	}

	public void aggiornaFlagAttivoGiocatore(int giornata) throws Exception {

		LOG.info("END aggiornaFlagAttivoGiocatore");

		jdbcTemplate.update("update fc_giocatore set flag_attivo = 0");
		jdbcTemplate.update("update fc_giocatore set flag_attivo = 1 where id_squadra in (select id_squadra_casa from fc_calendario_tim where codice_giornata = " + giornata + ")");
		jdbcTemplate.update("update fc_giocatore set flag_attivo = 1 where id_squadra in (select id_squadra_fuori from fc_calendario_tim where codice_giornata = " + giornata + ")");

		jdbcTemplate.update("update fc_statistiche set flag_attivo = 0");
		jdbcTemplate.update("update fc_statistiche set flag_attivo = 1 where nome_squadra in (select squadra_casa from fc_calendario_tim where codice_giornata = " + giornata + ")");
		jdbcTemplate.update("update fc_statistiche set flag_attivo = 1 where nome_squadra in (select squadra_fuori from fc_calendario_tim where codice_giornata = " + giornata + ")");

		LOG.info("END emstatistiche");

	}

	public void emstatisticheOld() throws Exception {

		LOG.info("START statistiche");

		List<FcPagelle> lPagelle = (List<FcPagelle>) pagelleRepository.findAll(sortBy());

		int giocate = 0;

		FcPagelle pagelle = lPagelle.get(0);
		int appoIdGiocatore = pagelle.getFcGiocatore().getIdGiocatore();
		int votoGiocatore = 0;
		int fantaMedia = 0;
		int goalRealizzato = 0;
		int ammonizione = 0;
		int espulso = 0;
		int rigoreFallito = 0;
		int rigoreSegnato = 0;
		int assist = 0;

		for (FcPagelle p : lPagelle) {

			int idGiocatore = p.getFcGiocatore().getIdGiocatore();
			if (idGiocatore == appoIdGiocatore) {

				if (p.getVotoGiocatore() > 0) {

					votoGiocatore += p.getVotoGiocatore();
					fantaMedia += buildFantaMedia(p);
					goalRealizzato += p.getGoalRealizzato();
					ammonizione += p.getAmmonizione();
					espulso += p.getEspulsione();
					rigoreFallito += p.getRigoreFallito();
					rigoreSegnato += p.getRigoreSegnato();
					assist += p.getAssist();

					giocate = giocate + 1;
				}

			} else {

				String update = "";
				if (giocate > 0) {
					update = "update fc_statistiche set media_voto=" + (votoGiocatore / giocate);
					update += ",fanta_media=" + (fantaMedia / giocate);
				} else {
					update = "update fc_statistiche set media_voto=0";
					update += ",fanta_media=0";
				}
				update += ",giocate=" + giocate;
				update += ",goal_fatto=" + goalRealizzato;
				update += ",ammonizione=" + ammonizione;
				update += ",espulsione=" + espulso;
				update += ",rigore_sbagliato=" + rigoreFallito;
				update += ",rigore_segnato=" + rigoreSegnato;
				update += ",assist=" + assist;
				update += " where id_giocatore=" + appoIdGiocatore;

				jdbcTemplate.update(update);

				appoIdGiocatore = p.getFcGiocatore().getIdGiocatore();

				votoGiocatore = p.getVotoGiocatore();
				fantaMedia = buildFantaMedia(p);
				goalRealizzato = p.getGoalRealizzato();
				ammonizione = p.getAmmonizione();
				espulso = p.getEspulsione();
				rigoreFallito = p.getRigoreFallito();
				rigoreSegnato = p.getRigoreSegnato();
				assist = p.getAssist();

				giocate = 0;
				if (p.getVotoGiocatore() > 0) {
					giocate = giocate + 1;
				}
			}
		}

		String update = "";
		if (giocate > 0) {
			update = "update fc_statistiche set media_voto=" + (votoGiocatore / giocate);
			update += ",fanta_media=" + (fantaMedia / giocate);
		} else {
			update = "update fc_statistiche set media_voto=0";
			update += ",fanta_media=0";
		}

		update += ",giocate=" + giocate;
		update += ",goal_fatto=" + goalRealizzato;
		update += ",ammonizione=" + ammonizione;
		update += ",espulsione=" + espulso;
		update += ",rigore_sbagliato=" + rigoreFallito;
		update += ",rigore_segnato=" + rigoreSegnato;
		update += ",assist=" + assist;
		update += " where id_giocatore=" + appoIdGiocatore;

		jdbcTemplate.update(update);

		LOG.info("END statistiche");

	}

	@RequestMapping(value = "/eminserisciUltimaFormazione", method = RequestMethod.POST)
	@ResponseBody
	public void eminserisciUltimaFormazione(int idAttore, int giornata)
			throws Exception {
		LOG.info("START inserisciUltimaFormazione");
		int prev_gg = giornata - 1;

		String delete = "delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete);
		String delete2 = "delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete2);

		String ins = "insert into fc_giornata_dett (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO) ";
		ins += "SELECT " + giornata + "," + idAttore + ",ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,0 from fc_giornata_dett where id_giornata=" + prev_gg + " and id_attore=" + idAttore;
		jdbcTemplate.update(ins);

		String ins2 = "insert into fc_giornata_dett_info (ID_GIORNATA, ID_ATTORE,FLAG_INVIO,DATA_INVIO) ";
		ins2 += "select " + giornata + "," + idAttore + ",FLAG_INVIO,DATA_INVIO from fc_giornata_dett_info where id_giornata=" + prev_gg + " and id_attore=" + idAttore;
		jdbcTemplate.update(ins2);

		LOG.info("END inserisciUltimaFormazione");
	}

	@RequestMapping(value = "/eminitDb", method = RequestMethod.POST)
	@ResponseBody
	public void eminitDb(Integer codiceGiornata) throws Exception {
		LOG.info("START eminitDb");

		FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(codiceGiornata);
		List<FcGiocatore> giocatores = (List<FcGiocatore>) giocatoreRepository.findAll();

		for (FcGiocatore giocatore : giocatores) {
			FcStatistiche statistiche = new FcStatistiche();
			statistiche.setIdGiocatore(giocatore.getIdGiocatore());
			statistiche.setCognGiocatore(giocatore.getCognGiocatore());
			statistiche.setIdRuolo(giocatore.getFcRuolo().getIdRuolo());
			statistiche.setNomeSquadra(giocatore.getFcSquadra().getNomeSquadra());
			statistiche.setAmmonizione(0);
			statistiche.setAssist(0);
			statistiche.setEspulsione(0);
			statistiche.setFantaMedia(0.0);
			statistiche.setGiocate(0);
			statistiche.setGoalFatto(0);
			statistiche.setGoalSubito(0);
			statistiche.setMediaVoto(0.0);
			statistiche.setRigoreSbagliato(0);
			statistiche.setRigoreSegnato(0);
			statistiche.setFlagAttivo(giocatore.isFlagAttivo());
			statisticheRepository.save(statistiche);
		}

		for (FcGiocatore giocatore : giocatores) {
			// LOG.debug(giocatore.getCognGiocatore());
			FcPagelle pagelle = new FcPagelle();
			FcPagelleId pagellePK = new FcPagelleId();
			pagellePK.setIdGiornata(giornataInfo.getCodiceGiornata());
			pagellePK.setIdGiocatore(giocatore.getIdGiocatore());
			pagelle.setId(pagellePK);
			pagelleRepository.save(pagelle);
		}

		LOG.info("END eminitDb");

	}

	public HashMap<Object, Object> initDbGiocatori(String httpUrlImg,
			String imgPath, String fileName, boolean updateQuotazioni,
			String percenutale) throws Exception {

		LOG.info("START initDbGiocatori");

		HashMap<Object, Object> map = new HashMap<>();
		ArrayList<FcGiocatore> listGiocatoriAdd = new ArrayList<>();
		ArrayList<FcGiocatore> listGiocatoriDel = new ArrayList<>();

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// Create a new list of student to be filled by CSV file data
			List<FcGiocatore> giocatores = new ArrayList<>();

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// giocatoreRepository.deleteAll();
			// List<FcGiocatore> listG = (List<FcGiocatore>)
			// giocatoreRepository.findAll();
			// int count = listG.size();
			// int lastIdGiocatore = 1;
			// if (count > 0) {
			// FcGiocatore lastG = listG.get(count - 1);
			// lastIdGiocatore = lastG.getIdGiocatore();
			// }

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				FcGiocatore giocatore = null;
				String idGiocatore = record.get(0);
				String cognGiocatore = record.get(1).trim();
				String idRuolo = record.get(2);
				String nomeSquadra = record.get(4).trim();
				String quotazioneIniziale = record.get(5);
				String quotazioneAttuale = record.get(6);
				LOG.debug("giocatore " + cognGiocatore + " qI " + quotazioneIniziale + " qA " + quotazioneAttuale);

				if ("704".equals(idGiocatore)) {
					cognGiocatore = "ZARE-EMERY";
				}

				FcRuolo ruolo = new FcRuolo();
				ruolo.setIdRuolo(idRuolo);
				FcSquadra squadra = squadraRepository.findByNomeSquadra(nomeSquadra);
				if (squadra == null) {
					LOG.info("SCARTO " + cognGiocatore + " " + idRuolo + " " + nomeSquadra + " " + quotazioneAttuale);
					continue;
				}

				// if (StringUtils.isNotEmpty(idGiocatore)) {
				// giocatore =
				// this.giocatoreRepository.findByIdGiocatore(Integer.parseInt(idGiocatore));
				// if (giocatore == null) {
				// giocatore = new FcGiocatore();
				// giocatore.setQuotazione(5);
				// LOG.info("NEW GIOCATORE " + idGiocatore + " " + cognGiocatore
				// + " " + idRuolo + " " + nomeSquadra + " " +
				// quotazioneAttuale);
				// listGiocatoriAdd.add(giocatore);
				// }
				// }

				// List<FcGiocatore> lgiocatore =
				// this.giocatoreRepository.findByCognGiocatoreStartingWith(nomeSquadra);
				// for (FcGiocatore g : lgiocatore) {
				// giocatore = g;
				// if (!g.getFcSquadra().getNomeSquadra().equals(nomeSquadra)) {
				// LOG.info("ATTENZIONE SQUADRA DIFFERENTE ");
				// LOG.info("" + idGiocatore + ";" + cognGiocatore + ";" +
				// idRuolo + ";" + nomeSquadra + ";" + quotazioneAttuale + ";" +
				// quotazioneAttuale);
				// }
				// }
				// giocatore =
				// this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadra(cognGiocatore,
				// squadra);
				giocatore = this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(cognGiocatore, squadra, ruolo);
				if (giocatore == null) {
					// lastIdGiocatore++;
					giocatore = new FcGiocatore();
					giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
					giocatore.setQuotazione(5);
					LOG.info("NEW GIOCATORE " + idGiocatore + " " + cognGiocatore + " " + idRuolo + " " + nomeSquadra + " " + quotazioneAttuale);
					listGiocatoriAdd.add(giocatore);
				}

				if (updateQuotazioni) {
					int newQuotaz = calcolaQuotazione(quotazioneAttuale, idRuolo, percenutale);
					giocatore.setQuotazione(Integer.valueOf(newQuotaz));
				}

				// giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
				giocatore.setCognGiocatore(cognGiocatore);
				giocatore.setFcRuolo(ruolo);
				giocatore.setFcSquadra(squadra);
				giocatore.setFlagAttivo(true);
				if (giocatore.isFlagAttivo()) {
					giocatores.add(giocatore);
				}
			}

			if (giocatores.size() > 0) {

				// for (FcGiocatore gioc : listG) {
				// String sql = "UPDATE fc_giocatore SET ";
				// sql += " FLAG_ATTIVO=0";
				// sql += " WHERE ID_GIOCATORE=" + gioc.getIdGiocatore();
				// this.jdbcTemplate.execute(sql);
				// }

				for (FcGiocatore giocatore : giocatores) {

					// LOG.info("SAVE GIOCATORE ");
					giocatoreRepository.save(giocatore);

					FcStatistiche statistiche = new FcStatistiche();
					// statistiche.setFcGiocatore(giocatore);
					statistiche.setIdGiocatore(giocatore.getIdGiocatore());
					statistiche.setCognGiocatore(giocatore.getCognGiocatore());
					statistiche.setIdRuolo(giocatore.getFcRuolo().getIdRuolo());
					statistiche.setNomeSquadra(giocatore.getFcSquadra().getNomeSquadra());
					statistiche.setAmmonizione(0);
					statistiche.setAssist(0);
					statistiche.setEspulsione(0);
					statistiche.setFantaMedia(0.0);
					statistiche.setGiocate(0);
					statistiche.setGoalFatto(0);
					statistiche.setGoalSubito(0);
					statistiche.setMediaVoto(0.0);
					statistiche.setRigoreSbagliato(0);
					statistiche.setRigoreSegnato(0);
					statistiche.setFlagAttivo(giocatore.isFlagAttivo());

					// LOG.info(" GIOCATORE " + giocatore.getIdGiocatore() + " "
					// + giocatore.getCognGiocatore() + " " +
					// giocatore.getFcRuolo().getIdRuolo());

					// LOG.info("SAVE STATISTICA ");
					statisticheRepository.save(statistiche);

				}

				// String sql = " select id_giocatore,cogn_giocatore from
				// fc_giocatore where flag_attivo=0 and id_giocatore not in
				// (select distinct id_giocatore from fc_giornata_dett where
				// id_giocatore is not null) ";
				// jdbcTemplate.query(sql, new
				// ResultSetExtractor<ArrayList<FcGiocatore>>(){
				//
				// @Override
				// public ArrayList<FcGiocatore> extractData(ResultSet rs)
				// throws SQLException, DataAccessException {
				// int idGiocatore = 0;
				// String cognGiocatore = "";
				// while (rs.next()) {
				// idGiocatore = rs.getInt(1);
				// cognGiocatore = rs.getString(2);
				// LOG.info("idGiocatore " + idGiocatore + " cognGiocatore " +
				// cognGiocatore);
				// FcGiocatore giocatore =
				// giocatoreRepository.findByIdGiocatore(idGiocatore);
				// listGiocatoriDel.add(giocatore);
				// }
				// return null;
				// }
				// });
				//
				// String delete1 = " delete from fc_statistiche where
				// id_giocatore in ( ";
				// delete1 += " select id_giocatore from fc_giocatore where
				// flag_attivo=0 and id_giocatore not in (select distinct
				// id_giocatore from fc_giornata_dett where id_giocatore is not
				// null) ";
				// delete1 += " ) ";
				// jdbcTemplate.update(delete1);
				// LOG.info("delete1 " + delete1);
				// String delete2 = " delete from fc_pagelle where id_giocatore
				// in ( ";
				// delete2 += " select id_giocatore from fc_giocatore where
				// flag_attivo=0 and id_giocatore not in (select distinct
				// id_giocatore from fc_giornata_dett where id_giocatore is not
				// null)";
				// delete2 += " ) ";
				// jdbcTemplate.update(delete2);
				// LOG.info("delete2 " + delete2);
				// String delete3 = " delete from fc_giocatore where
				// flag_attivo=0 and id_giocatore not in (select distinct
				// id_giocatore from fc_giornata_dett where id_giocatore is not
				// null) ";
				// jdbcTemplate.update(delete3);
				// LOG.info("delete3 " + delete3);
			}

			LOG.info("END initDbGiocatori");

			map.put("listAdd", listGiocatoriAdd);
			map.put("listDel", listGiocatoriDel);

			return map;

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in initDbGiocatori !!!");
			throw e;
		} finally {
			if (fileReader != null) {
				fileReader.close();
			}
			if (csvFileParser != null) {
				csvFileParser.close();
			}
		}
	}

	public HashMap<Object, Object> initDbGiocatoriExcel(InputStream is)
			throws Exception {

		LOG.info("START initDbGiocatoriExcel");

		HashMap<Object, Object> map = new HashMap<>();
		ArrayList<FcGiocatore> listGiocatoriAdd = new ArrayList<>();
		ArrayList<FcGiocatore> listGiocatoriDel = new ArrayList<>();

		try {

			// Create a new list of student to be filled by CSV file data
			List<FcGiocatore> giocatores = new ArrayList<>();

			// giocatoreRepository.deleteAll();
			List<FcGiocatore> listG = (List<FcGiocatore>) giocatoreRepository.findAll();

			Workbook workbook = WorkbookFactory.create(is);

			// Retrieving the number of sheets in the Workbook
			LOG.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

			// 1. You can obtain a sheetIterator and iterate over it
			Iterator<Sheet> sheetIterator = workbook.sheetIterator();
			LOG.info("Retrieving Sheets using Iterator");
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				LOG.info("=> " + sheet.getSheetName());
			}

			// Getting the Sheet at index zero
			Sheet sheet = workbook.getSheetAt(0);

			DataFormatter dataFormatter = new DataFormatter();
			LOG.info("Iterating over Rows and Columns using for-each loop");

			int conta = 0;
			for (Row row : sheet) {

				if (conta == 0) {
					conta++;
					LOG.info("SCARTO RIGA HEADER ");
					continue;
				}

				FcGiocatore giocatore = null;
				String idGiocatore = "";
				String gameName = "";
				String cognGiocatore = "";
				String nome = "";
				String idRuolo = "";
				String nomeSquadra = "";
				String quotazioneAttuale = "";

				for (Cell cell : row) {

					String cellValue = dataFormatter.formatCellValue(cell);
					if (cell.getColumnIndex() == 0) {
						idGiocatore = cellValue;
					} else if (cell.getColumnIndex() == 1) {
						cognGiocatore = cellValue;
					} else if (cell.getColumnIndex() == 2) {
						nome = cellValue;
					} else if (cell.getColumnIndex() == 3) {
						nomeSquadra = cellValue.toUpperCase();
					} else if (cell.getColumnIndex() == 4) {
						idRuolo = cellValue;
					} else if (cell.getColumnIndex() == 6) {
						quotazioneAttuale = cellValue;
					}
				}

				if (StringUtils.isEmpty(cognGiocatore) && StringUtils.isEmpty(idRuolo) && StringUtils.isEmpty(nomeSquadra) && StringUtils.isEmpty(quotazioneAttuale)) {
					LOG.info("SCARTO RIGA VUOTA ");
					continue;
				}

				// LOG.debug("giocatore " + gameName + " qA " +
				// quotazioneAttuale);
				if (StringUtils.isNotEmpty(idGiocatore)) {

					FcRuolo ruolo = new FcRuolo();
					ruolo.setIdRuolo(idRuolo);
					FcSquadra squadra = squadraRepository.findByNomeSquadra(nomeSquadra);
					gameName = (cognGiocatore + " " + nome).toUpperCase();
					// LOG.info("FIND " + gameName + " " + idRuolo + " " +
					// nomeSquadra + " " + quotazioneAttuale);
					// giocatore =
					// this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(gameName,
					// squadra, ruolo);

					List<FcGiocatore> lgiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(gameName);
					for (FcGiocatore g : lgiocatore) {
						giocatore = g;
						if (!g.getFcSquadra().getNomeSquadra().equals(nomeSquadra)) {
							LOG.info("ATTENZIONE SQUADRA DIFFERENTE ");
							LOG.info("" + idGiocatore + ";" + gameName + ";" + idRuolo + ";" + nomeSquadra + ";" + quotazioneAttuale + ";" + quotazioneAttuale);
						}
					}
					// giocatore =
					// this.giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadra(gameName,
					// squadra);
					if (giocatore == null) {
						// LOG.info("NEW GIOCATORE " );
						System.out.println("" + idGiocatore + ";" + gameName + ";" + idRuolo + ";" + nomeSquadra + ";" + quotazioneAttuale + ";" + quotazioneAttuale);

						giocatore = new FcGiocatore();
						giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
						giocatore.setCognGiocatore(gameName);
						giocatore.setNomeGiocatore("");
						giocatore.setQuotazione(Integer.valueOf(quotazioneAttuale));
						giocatore.setFcRuolo(ruolo);
						giocatore.setFcSquadra(squadra);
						listGiocatoriAdd.add(giocatore);
					}
				}

				giocatore.setFlagAttivo(true);
				if (giocatore.isFlagAttivo()) {
					giocatores.add(giocatore);
				}
			}

			if (giocatores.size() > 0) {

				for (FcGiocatore gioc : listG) {
					String sql = "UPDATE fc_giocatore SET ";
					sql += " FLAG_ATTIVO=0";
					sql += " WHERE ID_GIOCATORE=" + gioc.getIdGiocatore();
					this.jdbcTemplate.execute(sql);
				}

				for (FcGiocatore giocatore : giocatores) {

					// LOG.info("SAVE GIOCATORE ");
					giocatoreRepository.save(giocatore);

					FcStatistiche statistiche = new FcStatistiche();
					// statistiche.setFcGiocatore(giocatore);
					statistiche.setIdGiocatore(giocatore.getIdGiocatore());
					statistiche.setCognGiocatore(giocatore.getCognGiocatore());
					statistiche.setIdRuolo(giocatore.getFcRuolo().getIdRuolo());
					statistiche.setNomeSquadra(giocatore.getFcSquadra().getNomeSquadra());
					statistiche.setAmmonizione(0);
					statistiche.setAssist(0);
					statistiche.setEspulsione(0);
					statistiche.setFantaMedia(0.0);
					statistiche.setGiocate(0);
					statistiche.setGoalFatto(0);
					statistiche.setGoalSubito(0);
					statistiche.setMediaVoto(0.0);
					statistiche.setRigoreSbagliato(0);
					statistiche.setRigoreSegnato(0);
					statistiche.setFlagAttivo(giocatore.isFlagAttivo());

					// LOG.info(" GIOCATORE " + giocatore.getIdGiocatore() + " "
					// + giocatore.getCognGiocatore() + " " +
					// giocatore.getFcRuolo().getIdRuolo());

					// LOG.info("SAVE STATISTICA ");
					statisticheRepository.save(statistiche);

				}

				String sql = " select id_giocatore,cogn_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null) ";
				jdbcTemplate.query(sql, new ResultSetExtractor<ArrayList<FcGiocatore>>(){
					@Override
					public ArrayList<FcGiocatore> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						int idGiocatore = 0;
						String cognGiocatore = "";
						while (rs.next()) {
							idGiocatore = rs.getInt(1);
							cognGiocatore = rs.getString(2);
							LOG.info("idGiocatore " + idGiocatore + " cognGiocatore " + cognGiocatore);
							FcGiocatore giocatore = giocatoreRepository.findByIdGiocatore(idGiocatore);
							listGiocatoriDel.add(giocatore);
						}
						return null;
					}
				});
			}

			LOG.info("END initDbGiocatoriExcel");

			map.put("listAdd", listGiocatoriAdd);
			map.put("listDel", listGiocatoriDel);

			return map;

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in initDbGiocatoriExcel !!!");
			throw e;
		}
	}

	private int calcolaQuotazione(String quotazione, String idRuolo,
			String percentuale) {

		String q = Utils.replaceString(quotazione, ",", ".");
		BigDecimal bgQ = new BigDecimal(q);
		// bgQ.setScale(BigDecimal.ROUND_HALF_UP);

		long new_quot = 0;
		double appo = 0;
		// if ("A".equals(idRuolo)) {
		// appo = (Double.parseDouble(bgQ.toString()) *
		// Double.parseDouble("30")) / Costants.DIVISORE_100;
		// new_quot = Math.round(Double.parseDouble(bgQ.toString()) - appo);
		// } else {
		// appo = (Double.parseDouble(bgQ.toString()) * Double.parseDouble("2"))
		// / 3;
		// new_quot = Math.round(appo);
		// }
		appo = (Double.parseDouble(bgQ.toString()) * Double.parseDouble(percentuale)) / Costants.DIVISORE_100;
		double newQuotazione = Double.parseDouble(bgQ.toString()) - appo;
		// LOG.debug(" newQuotazione " + newQuotazione);
		new_quot = Math.round(newQuotazione);
		if (new_quot < 1) {
			new_quot = 1;
		}
		LOG.debug(" new_quot " + new_quot);

		return (int) new_quot;
	}

}
