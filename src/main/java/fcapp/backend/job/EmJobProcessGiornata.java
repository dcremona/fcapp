package fcapp.backend.job;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcPagelleId;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.PagelleService;
import fcapp.backend.service.SquadraService;
import fcapp.backend.service.StatisticheService;
import fcapp.utils.Costants;
import fcapp.utils.Utils;

@Controller
public class EmJobProcessGiornata{

	private final static Log LOG = LogFactory.getLog(EmJobProcessGiornata.class);

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private GiornataDettService giornataDettService;

	@Autowired
	private AttoreService attoreService;

	@Autowired
	private PagelleService pagelleService;

	@Autowired
	private GiornataInfoService giornataInfoService;

	@Autowired
	private GiocatoreService giocatoreService;

	@Autowired
	private StatisticheService statisticheService;

	@Autowired
	private SquadraService squadraService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void eminitPagelle(Integer giornata) {
		LOG.info("START eminitPagelle");
		FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(giornata);
		LOG.debug("" + giornataInfo.getCodiceGiornata());
		List<FcGiocatore> giocatores = giocatoreService.findAll();
		for (FcGiocatore giocatore : giocatores) {
			FcPagelle pagelle = new FcPagelle();
			FcPagelleId pagellePK = new FcPagelleId();
			pagellePK.setIdGiornata(giornataInfo.getCodiceGiornata());
			pagellePK.setIdGiocatore(giocatore.getIdGiocatore());
			pagelle.setId(pagellePK);
			pagelleService.save(pagelle);
		}
		LOG.info("END eminitPagelle");
	}

	public void emaggiornamentoPFGiornataOLD(String fileName) {

		LOG.info("START emaggiornamentoPFGiornataOLD");
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try (FileReader fileReader = new FileReader(fileName); CSVParser csvFileParser = new CSVParser(fileReader,csvFileFormat)) {

			// initialize FileReader object

			// initialize CSVParser object

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				FcGiocatore giocatore = null;
				List<FcGiocatore> listGiocatore = this.giocatoreService.findByCognGiocatoreContaining(record.get(2));
				if (listGiocatore != null && listGiocatore.size() == 1) {
					giocatore = listGiocatore.get(0);
				}

				if (giocatore != null) {

					long votoG = getVotoG(record, giocatore);

					String goalRealizzato = StringUtils.isEmpty(record.get(5)) ? "0" : record.get(5);
					String goalSubito = StringUtils.isEmpty(record.get(6)) ? "0" : record.get(6);
					String rigoreSegnato = StringUtils.isEmpty(record.get(9)) ? "0" : record.get(9);
					String rigoreFallito = StringUtils.isEmpty(record.get(10)) ? "0" : record.get(10);
					String rigoreParato = StringUtils.isEmpty(record.get(11)) ? "0" : record.get(11);
					String autorete = StringUtils.isEmpty(record.get(12)) ? "0" : record.get(12);
					String assist = StringUtils.isEmpty(record.get(13)) ? "0" : record.get(13);

					String update = "update fc_pagelle set voto_giocatore=" + votoG;
					update += ",goal_realizzato=" + goalRealizzato;
					update += ",goal_subito=" + goalSubito;
					update += ",ammonizione=" + record.get(7);
					update += ",espulsione=" + record.get(8);
					update += ",rigore_segnato=" + rigoreSegnato;
					update += ",rigore_fallito=" + rigoreFallito;
					update += ",rigore_parato=" + rigoreParato;
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
			LOG.error("Error in CsvFileReader !!!");
		}
	}

	private long getVotoG(CSVRecord r, FcGiocatore giocatore) {
		String g = r.get(4);
		// PORTIERE SV
		boolean b1 = g.isEmpty() || g.equals("s.v.") || g.equals("s,v,");
		if ("P".equals(giocatore.getFcRuolo().getIdRuolo())) {
			if (b1) {
				g = "6";
			}
			// LOG.info("PORTIERE s.v.: "+COGN_GIOCATORE);
		} else {
			if (b1) {
				g = "0";
			}
		}

		BigDecimal bgG = new BigDecimal(g);
		BigDecimal mG = new BigDecimal("10");
		BigDecimal risG = bgG.multiply(mG);
		return risG.longValue();
	}

	public void emaggiornamentoPFGiornata(Properties p, String fileName,
			String idGiornata) {

		LOG.info("START emaggiornamentoPFGiornata");

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try (FileReader fileReader = new FileReader(fileName); CSVParser csvFileParser = new CSVParser(fileReader,csvFileFormat)) {
			try {

				// initialize FileReader object

				// initialize CSVParser object

				// Get a list of CSV file records
				List<CSVRecord> csvRecords = csvFileParser.getRecords();

				// String infoVoti = "";
				StringBuilder infoNewGiocatore = new StringBuilder();

				StringBuilder formazioneHtml = new StringBuilder();
				formazioneHtml.append("<html><head><title>FC</title></head>\n");
				formazioneHtml.append("<body>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");

				formazioneHtml.append("<table>");

				formazioneHtml.append("<tr>");
				formazioneHtml.append("<td>");
				formazioneHtml.append(Costants.GIOCATORE);
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("count_sv ");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("New_Voto ");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("G");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("CS");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("TS");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("Minuti Giocati");
				formazioneHtml.append("</td>");
				formazioneHtml.append("</tr>");

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
					String g = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String goalRealizzato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String goalSubito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String autorete = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String assist = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String cs = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					// String GF= record.get(11);
					c++;
					// String GS= record.get(12);
					c++;
					// String Aut= record.get(13);
					c++;
					// String Ass= record.get(14);
					c++;
					String ts = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					// String GF= record.get(16);
					c++;
					// String GS= record.get(17);
					c++;
					StringUtils.isEmpty(record.get(18));
					c++;
					c++;
					// String M2 = record.get(20);
					c++;
					// String M3 = StringUtils.isEmpty(record.get(c)) ? "0" :
					// record.get(c);
					c++;
					String ammonizione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String espulsione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String gdv = record.get(c);
					c++;
					String gdp = record.get(c);
					c++;
					String rigoreFallito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGS
					c++;
					String rigoreParato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGP
					c++;
					String rigoreSegnato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RT

					FcGiocatore giocatore = null;
					if (StringUtils.isNotEmpty(idGiocatore)) {
						giocatore = this.giocatoreService.findByIdGiocatore(Integer.parseInt(idGiocatore));
						if (giocatore == null) {
							List<FcGiocatore> listGiocatore = this.giocatoreService.findByCognGiocatoreContaining(cognGiocatore);
							if (listGiocatore != null && listGiocatore.size() == 1) {
								giocatore = listGiocatore.get(0);
							}
						}
					}

					if (giocatore != null) {

						int countSv = 0;

						g = Utils.replaceString(g, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (g.isEmpty() || g.equals("s.v.") || g.equals("s,v,")) {
								g = "6";
								// LOG.debug("PORTIERE s.v.: "+Giocatore);
							}
						} else {
							if (g.isEmpty() || g.equals("s.v.") || g.equals("s,v,")) {
								g = "0";
								countSv++;
							}
						}
						BigDecimal bgG = new BigDecimal(g);
						BigDecimal mG = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risG = bgG.multiply(mG);
						long votoG = risG.longValue();

						cs = Utils.replaceString(cs, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (cs.isEmpty() || cs.equals("s.v.") || cs.equals("s,v,")) {
								cs = "6";
							}
						} else {
							if (cs.isEmpty() || cs.equals("s.v.") || cs.equals("s,v,")) {
								cs = "0";
								countSv++;
							}
						}

						BigDecimal bgCS = new BigDecimal(cs);
						BigDecimal mCS = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risCS = bgCS.multiply(mCS);
						long votoCS = risCS.longValue();

						ts = Utils.replaceString(ts, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (ts.isEmpty() || ts.equals("s.v.") || ts.equals("s,v,")) {
								ts = "6";
							}
						} else {
							if (ts.isEmpty() || ts.equals("s.v.") || ts.equals("s,v,")) {
								ts = "0";
								countSv++;
							}
						}

						BigDecimal bgTS = new BigDecimal(ts);
						BigDecimal mTS = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risTS = bgTS.multiply(mTS);
						long votoTS = risTS.longValue();

						// String VOTO_GIOCATORE = Utils.replaceString(M3, ",",
						// ".");
						// PORTIERE SV
						// if (VOTO_GIOCATORE.equals("s.v.") ||
						// VOTO_GIOCATORE.equals("s,v,") && ruolo.equals("P")) {
						// } else {
						// }

						if (countSv == 1) {
							if ("0".equals(g)) {
								if (votoCS <= votoTS) {
									g = cs;
								} else {
									g = ts;
								}
							} else if ("0".equals(cs)) {
								if (votoG <= votoTS) {
									cs = g;
								} else {
									cs = ts;
								}
							} else {
								if (votoG <= votoCS) {
									ts = g;
								} else {
									ts = cs;
								}
							}
						} else if (countSv == 2) {
							// LOG.info("count_sv = " + count_sv + " set all 0
							// ");
							g = "0";
							cs = "0";
							ts = "0";
						}

						long newVoto = getNewVoto(g, cs, ts);

						if (countSv == 1 || countSv == 2) {

							formazioneHtml.append("<tr>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(giocatore.getCognGiocatore());
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(countSv);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(newVoto);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(g);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(cs);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(ts);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(minGiocati);
							formazioneHtml.append("</td>");
							formazioneHtml.append("</tr>");
						}

						String update = "update fc_pagelle set voto_giocatore=" + votoG;
						update += ",g=" + votoG;
						update += ",cs=" + votoCS;
						update += ",ts=" + votoTS;
						update += ",goal_realizzato=" + goalRealizzato;
						update += ",goal_subito=" + goalSubito;
						update += ",ammonizione=" + ammonizione;
						update += ",espulsione=" + espulsione;
						update += ",rigore_segnato=" + rigoreSegnato;
						update += ",rigore_fallito=" + rigoreFallito;
						update += ",rigore_parato=" + rigoreParato;
						update += ",autorete=" + autorete;
						update += ",assist=" + assist;
						update += ",gdv=" + gdv;
						update += ",gdp=" + gdp;
						update += " where id_giocatore=" + idGiocatore;
						update += " and id_giornata=" + idGiornata;

						jdbcTemplate.update(update);

					} else {
						LOG.info("*************************");
						LOG.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
						LOG.info("*************************");

						infoNewGiocatore.append("\n<br>" + "NOT FOUND ").append(idGiocatore).append(" ").append(cognGiocatore).append(" ").append(ruolo).append(" ").append(squadra);
					}
				}

				String emailDestinatario = p.getProperty("to");
				String[] to = null;
				if (emailDestinatario != null && !emailDestinatario.isEmpty()) {
					to = Utils.tornaArrayString(emailDestinatario, ";");
				}
				String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;

				formazioneHtml.append("</table>\n");

				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");

				formazioneHtml.append("<p>").append(infoNewGiocatore).append("</p>\n");

				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<p>Ciao Davide</p>\n");
				formazioneHtml.append("</body>\n");
				formazioneHtml.append("<html>");

				try {
					String from = env.getProperty("spring.mail.secondary.username");
					emailService.sendMail(false, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
				} catch (Exception e) {
					LOG.error(e.getMessage());
					try {
						String from = env.getProperty("spring.mail.primary.username");
						emailService.sendMail(true, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
					} catch (Exception e2) {
						LOG.error(e2.getMessage());
					}
				}

				LOG.info("END emaggiornamentoPFGiornata");

			} catch (Exception e) {
				LOG.error("Error in CsvFileReader !!!" + e.getMessage());
			}
		} catch (IOException e) {
			LOG.error("Error while closing fileReader/csvFileParser !!!");
		}
	}

	private long getNewVoto(String g, String cs, String ts) {
		String divide = "3";
		BigDecimal bgG = new BigDecimal(g);
		BigDecimal bgCS = new BigDecimal(cs);
		BigDecimal bgTS = new BigDecimal(ts);
		BigDecimal tot0 = bgG.add(bgCS);
		BigDecimal tot1 = tot0.add(bgTS);
		BigDecimal media = tot1.divide(new BigDecimal(divide), 2, RoundingMode.HALF_UP);
		BigDecimal moltipl = new BigDecimal(Costants.DIVISORE_10);
		BigDecimal ris = media.multiply(moltipl);
		return ris.longValue();
	}

	public void emaggiornamentoPFGiornataNoExcel(Properties p, String fileName,
			String idGiornata) {

		LOG.info("START emaggiornamentoPFGiornata");

		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try (FileReader fileReader = new FileReader(fileName); CSVParser csvFileParser = new CSVParser(fileReader,csvFileFormat)) {
			try {

				// initialize FileReader object

				// initialize CSVParser object

				// Get a list of CSV file records
				List<CSVRecord> csvRecords = csvFileParser.getRecords();

				// String infoVoti = "";
				StringBuilder infoNewGiocatore = new StringBuilder();

				StringBuilder formazioneHtml = new StringBuilder();
				formazioneHtml.append("<html><head><title>FC</title></head>\n");
				formazioneHtml.append("<body>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");

				formazioneHtml.append("<table>");

				formazioneHtml.append("<tr>");
				formazioneHtml.append("<td>");
				formazioneHtml.append(Costants.GIOCATORE);
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("count_sv ");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("New_Voto ");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("G");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("CS");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("TS");
				formazioneHtml.append("</td>");
				formazioneHtml.append("<td>");
				formazioneHtml.append("Minuti Giocati");
				formazioneHtml.append("</td>");
				formazioneHtml.append("</tr>");

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
					String g = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String goalRealizzato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String goalSubito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String autorete = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String assist = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String ammonizione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String espulsione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
					c++;
					String rigoreFallito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGS
					c++;
					String rigoreParato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGP
					c++;
					String rigoreSegnato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RT
					c++;
					c++;
					String gdv = record.get(c);

					String cs = "0";
					String ts = "0";
					// String M3 = "0";

					FcGiocatore giocatore;

					FcRuolo fcRuolo = new FcRuolo();
					fcRuolo.setIdRuolo(ruolo);
					FcSquadra fcSquadra = squadraService.findByNomeSquadra(squadra);
					if (squadra == null) {
						LOG.info("SCARTO " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + null);
						continue;
					}
					// LOG.info("FIND " + cognGiocatore+ " squadra " +squadra);
					giocatore = this.giocatoreService.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(cognGiocatore, fcSquadra, fcRuolo);

					if (giocatore != null) {

						idGiocatore = "" + giocatore.getIdGiocatore();

						int countSv = 0;

						g = Utils.replaceString(g, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (g.isEmpty() || g.equals("s.v.") || g.equals("s,v,")) {
								g = "6";
								// LOG.debug("PORTIERE s.v.: "+Giocatore);
							}
						} else {
							if (g.isEmpty() || g.equals("s.v.") || g.equals("s,v,")) {
								g = "0";
								countSv++;
							}
						}
						BigDecimal bgG = new BigDecimal(g);
						BigDecimal mG = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risG = bgG.multiply(mG);
						long votoG = risG.longValue();

						cs = Utils.replaceString(cs, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (cs.isEmpty() || cs.equals("s.v.") || cs.equals("s,v,")) {
								cs = "6";
							}
						} else {
							if (cs.isEmpty() || cs.equals("s.v.") || cs.equals("s,v,")) {
								cs = "0";
								countSv++;
							}
						}

						BigDecimal bgCS = new BigDecimal(cs);
						BigDecimal mCS = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risCS = bgCS.multiply(mCS);
						long votoCS = risCS.longValue();

						ts = Utils.replaceString(ts, ",", ".");
						// PORTIERE SV
						if (ruolo.equals("P")) {
							if (ts.isEmpty() || ts.equals("s.v.") || ts.equals("s,v,")) {
								ts = "6";
							}
						} else {
							if (ts.isEmpty() || ts.equals("s.v.") || ts.equals("s,v,")) {
								ts = "0";
								countSv++;
							}
						}

						BigDecimal bgTS = new BigDecimal(ts);
						BigDecimal mTS = new BigDecimal(Costants.DIVISORE_10);
						BigDecimal risTS = bgTS.multiply(mTS);
						long votoTS = risTS.longValue();

						// String VOTO_GIOCATORE = Utils.replaceString(M3, ",",
						// ".");
						// PORTIERE SV
						// if (VOTO_GIOCATORE.equals("s.v.") ||
						// VOTO_GIOCATORE.equals("s,v,") && ruolo.equals("P")) {
						// } else {
						// if (VOTO_GIOCATORE.equals("s,v,")) {
						// }
						// }

						if (countSv == 1) {
							if ("0".equals(g)) {
								if (votoCS <= votoTS) {
									g = cs;
								} else {
									g = ts;
								}
								// LOG.info("G = " + G + " CS " + CS + " TS " +
								// TS);
							} else if ("0".equals(cs)) {
								if (votoG <= votoTS) {
									cs = g;
								} else {
									cs = ts;
								}
								// LOG.info("CS = " + CS + " G " + G + " TS " +
								// TS);
							} else {
								if (votoG <= votoCS) {
									ts = g;
								} else {
									ts = cs;
								}
								// LOG.info("TS = " + TS + " G " + G + " CS " +
								// CS);
							}
						} else if (countSv == 2) {
							// LOG.info("count_sv = " + count_sv + " set all 0
							// ");
							g = "0";
							cs = "0";
							ts = "0";
						}

						long newVoto = getNewVoto(g, cs, ts);

						if (countSv == 1 || countSv == 2) {
							LOG.info("new_voto - countSv " + countSv + " - " + giocatore.getCognGiocatore() + " new_voto " + newVoto + " G = " + g + " CS " + cs + " TS " + ts);

							formazioneHtml.append("<tr>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(giocatore.getCognGiocatore());
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(countSv);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(newVoto);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(g);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(cs);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(ts);
							formazioneHtml.append("</td>");
							formazioneHtml.append("<td>");
							formazioneHtml.append(minGiocati);
							formazioneHtml.append("</td>");
							formazioneHtml.append("</tr>");
						}

						String update = "update fc_pagelle set voto_giocatore=" + votoG;
						update += ",g=" + votoG;
						update += ",cs=" + votoCS;
						update += ",ts=" + votoTS;
						update += ",goal_realizzato=" + goalRealizzato;
						update += ",goal_subito=" + goalSubito;
						update += ",ammonizione=" + ammonizione;
						update += ",espulsione=" + espulsione;
						update += ",rigore_segnato=" + rigoreSegnato;
						update += ",rigore_fallito=" + rigoreFallito;
						update += ",rigore_parato=" + rigoreParato;
						update += ",autorete=" + autorete;
						update += ",assist=" + assist;
						update += ",gdv=" + gdv;
						// update += ",gdp=" + Gdp;
						update += " where id_giocatore=" + idGiocatore;
						update += " and id_giornata=" + idGiornata;

						jdbcTemplate.update(update);

					} else {
						LOG.info("*************************");
						LOG.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
						LOG.info("*************************");

						infoNewGiocatore.append("\n<br>" + "NOT FOUND ").append(idGiocatore).append(" ").append(cognGiocatore).append(" ").append(ruolo).append(" ").append(squadra);
					}
				}

				String emailDestinatario = p.getProperty("to");
				String[] to = null;
				if (emailDestinatario != null && !emailDestinatario.isEmpty()) {
					to = Utils.tornaArrayString(emailDestinatario, ";");
				}
				String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;
				formazioneHtml.append("</table>\n");

				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");

				formazioneHtml.append("<p>").append(infoNewGiocatore).append("</p>\n");

				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<br>\n");
				formazioneHtml.append("<p>Ciao Davide</p>\n");
				formazioneHtml.append("</body>\n");
				formazioneHtml.append("<html>");

				try {
					String from = env.getProperty("spring.mail.secondary.username");
					emailService.sendMail(false, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
				} catch (Exception e) {
					LOG.error(e.getMessage());
					try {
						String from = env.getProperty("spring.mail.primary.username");
						emailService.sendMail(true, from, to, null, null, subject, formazioneHtml.toString(), "text/html", null);
					} catch (Exception e2) {
						LOG.error(e2.getMessage());
					}
				}

				LOG.info("END emaggiornamentoPFGiornata");

			} catch (Exception e) {
				LOG.error("Error in CsvFileReader !!!" + e.getMessage());
			}
		} catch (IOException e) {
			LOG.error("Error while closing fileReader/csvFileParser !!!");
		}
	}

	public void emalgoritmo(Integer giornata, FcCampionato campionato) {

		LOG.info("START emalgoritmo");

		LOG.info("giornata " + giornata);

		// List<String> schemi = new ArrayList<>();
		// schemi.add("1-5-4-1");
		// schemi.add("1-5-3-2");
		// schemi.add("1-4-5-1");
		// schemi.add("1-4-4-2");
		// schemi.add("1-4-3-3");
		// schemi.add("1-3-5-2");
		// schemi.add("1-3-4-3");

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcAttore> l = attoreService.findAll();

		for (FcAttore attore : l) {
			LOG.info("attore " + attore.getDescAttore());
			List<FcGiornataDett> lGiocatori = giornataDettService.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

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

				switch (tit.getFcGiocatore().getFcRuolo().getIdRuolo()) {
					case "P" -> countP++;
					case "D" -> countD++;
					case "C" -> countC++;
					case "A" -> countA++;
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
				switch (tit.getFcGiocatore().getFcRuolo().getIdRuolo()) {
					case "P" -> countP++;
					case "D" -> countD++;
					case "C" -> countC++;
					case "A" -> countA++;
				}
				nextSchema = countP + "-" + countD + "-" + countC + "-" + countA;
			}
			LOG.debug("NUOVO SCHEMA 2" + attore.getDescAttore() + " " + nextSchema);

			String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + attore.getIdAttore() + " AND ID_GIORNATA=" + giornata;
			jdbcTemplate.update(query);

			query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT) VALUES (" + campionato.getIdCampionato() + "," + attore.getIdAttore() + "," + giornata + "," + totPunti + ")";
			jdbcTemplate.update(query);
		}

		LOG.info("END emalgoritmo");

	}

	public void ricalcolaTotPunti(Integer giornata, FcCampionato campionato) {

		LOG.info("START ricalcolaTotPunti");

		LOG.info("giornata " + giornata);

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcAttore> l = attoreService.findAll();
		for (FcAttore attore : l) {
			String sql = " select sum(voto) from fc_giornata_dett";
			sql += " where id_attore=" + attore.getIdAttore();
			sql += " and id_giornata=" + giornata;
			sql += " and id_stato_giocatore='T'";
			sql += " and flag_attivo='S'";
			jdbcTemplate.query(sql, rs -> {
				int totPunti;
				while (rs.next()) {
					totPunti = rs.getInt(1);
					LOG.debug(attore.getDescAttore() + " " + totPunti);

					String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + attore.getIdAttore() + " AND ID_GIORNATA=" + giornata;
					jdbcTemplate.update(query);

					query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT) VALUES (" + campionato.getIdCampionato() + "," + attore.getIdAttore() + "," + giornata + "," + totPunti + ")";
					jdbcTemplate.update(query);

				}
				return "1";
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

	public void emstatistiche() {

		LOG.info("START emstatistiche");

		List<FcPagelle> lPagelle = pagelleService.findAll();

		int giocate = 0;

		FcPagelle pagelle = lPagelle.get(0);
		int appoIdGiocatore = pagelle.getFcGiocatore().getIdGiocatore();
		FcGiocatore fcGiocatore;

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

				FcGiocatore appoFcGiocatore = this.giocatoreService.findByIdGiocatore(appoIdGiocatore);

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
					mediaVoto = (double) votoGiocatore / giocate;
				}
				statistiche.setMediaVoto(mediaVoto);
				double fantaMediaVoto = 0.0;
				if (giocate > 0) {
					fantaMediaVoto = (double) fantaMedia / giocate;
				}
				statistiche.setFantaMedia(fantaMediaVoto);
				statistiche.setRigoreSbagliato(rigoreFallito);
				statistiche.setRigoreSegnato(rigoreSegnato);
				statistiche.setFcGiocatore(appoFcGiocatore);
				statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

				statisticheService.save(statistiche);

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

		FcGiocatore appoFcGiocatore = this.giocatoreService.findByIdGiocatore(appoIdGiocatore);

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
			mediaVoto = (double) votoGiocatore / giocate;
		}
		statistiche.setMediaVoto(mediaVoto);
		double fantaMediaVoto = 0.0;
		if (giocate > 0) {
			fantaMediaVoto = (double) fantaMedia / giocate;
		}
		statistiche.setFantaMedia(fantaMediaVoto);
		statistiche.setRigoreSbagliato(rigoreFallito);
		statistiche.setRigoreSegnato(rigoreSegnato);
		statistiche.setFcGiocatore(appoFcGiocatore);
		statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

		statisticheService.save(statistiche);

		LOG.info("END emstatistiche");

	}

	public void aggiornaFlagAttivoGiocatore(int giornata) {

		LOG.info("END aggiornaFlagAttivoGiocatore");

		jdbcTemplate.update("update fc_giocatore set flag_attivo = 0");
		jdbcTemplate.update("update fc_giocatore set flag_attivo = 1 where id_squadra in (select id_squadra_casa from fc_calendario_tim where codice_giornata = " + giornata + ")");
		jdbcTemplate.update("update fc_giocatore set flag_attivo = 1 where id_squadra in (select id_squadra_fuori from fc_calendario_tim where codice_giornata = " + giornata + ")");

		jdbcTemplate.update("update fc_statistiche set flag_attivo = 0");
		jdbcTemplate.update("update fc_statistiche set flag_attivo = 1 where nome_squadra in (select squadra_casa from fc_calendario_tim where codice_giornata = " + giornata + ")");
		jdbcTemplate.update("update fc_statistiche set flag_attivo = 1 where nome_squadra in (select squadra_fuori from fc_calendario_tim where codice_giornata = " + giornata + ")");

		LOG.info("END emstatistiche");

	}

	public void eminserisciUltimaFormazione(int idAttore, int giornata) {
		LOG.info("START inserisciUltimaFormazione");
		int prevGG = giornata - 1;

		String delete = "delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete);
		String delete2 = "delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete2);

		String ins = "insert into fc_giornata_dett (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO) ";
		ins += "SELECT " + giornata + "," + idAttore + ",ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,0 from fc_giornata_dett where id_giornata=" + prevGG + " and id_attore=" + idAttore;
		jdbcTemplate.update(ins);

		String ins2 = "insert into fc_giornata_dett_info (ID_GIORNATA, ID_ATTORE,FLAG_INVIO,DATA_INVIO) ";
		ins2 += "select " + giornata + "," + idAttore + ",FLAG_INVIO,DATA_INVIO from fc_giornata_dett_info where id_giornata=" + prevGG + " and id_attore=" + idAttore;
		jdbcTemplate.update(ins2);

		LOG.info("END inserisciUltimaFormazione");
	}

	public void eminitDb(Integer codiceGiornata) {
		LOG.info("START eminitDb");

		FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(codiceGiornata);
		List<FcGiocatore> giocatores = giocatoreService.findAll();

		for (FcGiocatore giocatore : giocatores) {
			FcStatistiche statistiche = getFcStatistiche(giocatore);
			statisticheService.save(statistiche);
		}

		for (FcGiocatore giocatore : giocatores) {
			// LOG.debug(giocatore.getCognGiocatore());
			FcPagelle pagelle = new FcPagelle();
			FcPagelleId pagellePK = new FcPagelleId();
			pagellePK.setIdGiornata(giornataInfo.getCodiceGiornata());
			pagellePK.setIdGiocatore(giocatore.getIdGiocatore());
			pagelle.setId(pagellePK);
			pagelleService.save(pagelle);
		}

		LOG.info("END eminitDb");

	}

	private @NonNull FcStatistiche getFcStatistiche(FcGiocatore giocatore) {
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
		return statistiche;
	}

	public HashMap<Object, Object> initDbGiocatori(String fileName,
			boolean updateQuotazioni, String percenutale) throws Exception {

		LOG.info("START initDbGiocatori");

		HashMap<Object, Object> map = new HashMap<>();
		ArrayList<FcGiocatore> listGiocatoriAdd = new ArrayList<>();
		ArrayList<FcGiocatore> listGiocatoriDel = new ArrayList<>();

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try (CSVParser csvFileParser = new CSVParser(new FileReader(fileName),csvFileFormat)) {

			// Create a new list of student to be filled by CSV file data
			List<FcGiocatore> giocatores = new ArrayList<>();

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				FcGiocatore giocatore;
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
				FcSquadra squadra = squadraService.findByNomeSquadra(nomeSquadra);
				if (squadra == null) {
					LOG.info("SCARTO " + cognGiocatore + " " + idRuolo + " " + nomeSquadra + " " + quotazioneAttuale);
					continue;
				}

				giocatore = this.giocatoreService.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(cognGiocatore, squadra, ruolo);
				if (giocatore == null) {
					// lastIdGiocatore++;
					giocatore = new FcGiocatore();
					giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
					giocatore.setQuotazione(5);
					LOG.info("NEW GIOCATORE " + idGiocatore + " " + cognGiocatore + " " + idRuolo + " " + nomeSquadra + " " + quotazioneAttuale);
					listGiocatoriAdd.add(giocatore);
				}

				if (updateQuotazioni) {
					int newQuotaz = calcolaQuotazione(quotazioneAttuale, percenutale);
					giocatore.setQuotazione(newQuotaz);
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

			if (!giocatores.isEmpty()) {

				for (FcGiocatore giocatore : giocatores) {

					// LOG.info("SAVE GIOCATORE ");
					giocatoreService.save(giocatore);

					FcStatistiche statistiche = getFcStatistiche(giocatore);

					statisticheService.save(statistiche);

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
				// giocatoreService.findByIdGiocatore(idGiocatore);
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
			}

			LOG.info("END initDbGiocatori");

			map.put("listAdd", listGiocatoriAdd);
			map.put("listDel", listGiocatoriDel);

			return map;

		} catch (Exception e) {
			LOG.error("Error in initDbGiocatori !!!");
			throw e;
		}
	}

	private int calcolaQuotazione(String quotazione, String percentuale) {

		String q = Utils.replaceString(quotazione, ",", ".");
		BigDecimal bgQ = new BigDecimal(q);
		// bgQ.setScale(BigDecimal.ROUND_HALF_UP);

		long newQuot;
		double newQuotazione = getNewQuotazione(percentuale, bgQ);
		// LOG.debug(" newQuotazione " + newQuotazione);
		newQuot = Math.round(newQuotazione);
		if (newQuot < 1) {
			newQuot = 1;
		}
		LOG.debug(" new_quot " + newQuot);

		return (int) newQuot;
	}

	private double getNewQuotazione(String percentuale, BigDecimal bgQ) {
		double appo;
		appo = (Double.parseDouble(bgQ.toString()) * Double.parseDouble(percentuale)) / Costants.DIVISORE_100;
		return Double.parseDouble(bgQ.toString()) - appo;
	}

}
