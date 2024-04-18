package fcweb.backend.job;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;

import common.util.Buffer;
import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatoreId;
import fcweb.backend.data.entity.FcGiornataId;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.data.entity.FcPagelleId;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.data.entity.FcStatistiche;
import fcweb.backend.service.AttoreRepository;
import fcweb.backend.service.CalendarioCompetizioneRepository;
import fcweb.backend.service.CampionatoRepository;
import fcweb.backend.service.ClassificaTotalePuntiRepository;
import fcweb.backend.service.EmailService;
import fcweb.backend.service.FormazioneRepository;
import fcweb.backend.service.GiocatoreRepository;
import fcweb.backend.service.GiornataDettRepository;
import fcweb.backend.service.GiornataGiocatoreRepository;
import fcweb.backend.service.GiornataInfoRepository;
import fcweb.backend.service.GiornataRepository;
import fcweb.backend.service.PagelleRepository;
import fcweb.backend.service.SquadraRepository;
import fcweb.backend.service.StatisticheRepository;
import fcweb.utils.Costants;

@Controller
public class JobProcessGiornata{

	private static final Logger LOG = LoggerFactory.getLogger(JobProcessGiornata.class);

	@Autowired
	private Environment env;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CampionatoRepository campionatoRepository;

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
	private SquadraRepository squadraRepository;

	@Autowired
	private StatisticheRepository statisticheRepository;

	@Autowired
	private FormazioneRepository formazioneRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CalendarioCompetizioneRepository calendarioTimRepository;

	@Autowired
	private GiornataRepository giornataRepository;

	@Autowired
	private ClassificaTotalePuntiRepository classificaTotalePuntiRepository;

	@Autowired
	private GiornataGiocatoreRepository giornataGiocatoreRepository;

	public HashMap<Object, Object> initDbGiocatori(String httpUrlImg,
			String imgPath, String fileName, boolean updateQuotazioni,
			boolean updateImg, String percentuale) throws Exception {

		LOG.info("START initDbGiocatori");

		HashMap<Object, Object> map = new HashMap<Object, Object>();
		ArrayList<FcGiocatore> listGiocatoriAdd = new ArrayList<FcGiocatore>();
		ArrayList<FcGiocatore> listGiocatoriDel = new ArrayList<FcGiocatore>();

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// Create a new list of student to be filled by CSV file data
			List<FcGiocatore> giocatores = new ArrayList<FcGiocatore>();

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// giocatoreRepository.deleteAll();
			List<FcGiocatore> listG = (List<FcGiocatore>) giocatoreRepository.findAll();

			LocalDateTime now = LocalDateTime.now();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				FcGiocatore giocatore = null;
				String idGiocatore = record.get(0);
				String cognGiocatore = record.get(1);
				String idRuolo = record.get(2);
				String nomeSquadra = record.get(4);
				String quotazioneIniziale = record.get(5);
				String quotazioneAttuale = record.get(6);
				LOG.debug("giocatore " + cognGiocatore + " qI " + quotazioneIniziale + " qA " + quotazioneAttuale);
				if (StringUtils.isNotEmpty(idGiocatore)) {
					giocatore = this.giocatoreRepository.findByIdGiocatore(Integer.parseInt(idGiocatore));
					if (giocatore == null) {
						// List<FcGiocatore> listGiocatore =
						// this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
						// if (listGiocatore != null && listGiocatore.size() ==
						// 1) {
						giocatore = new FcGiocatore();
						giocatore.setData(now);
						int newQuotaz = calcolaQuotazione(quotazioneAttuale, idRuolo, percentuale);
						giocatore.setQuotazione(Integer.valueOf(newQuotaz));
						LOG.info("NEW GIOCATORE " + idGiocatore + " " + cognGiocatore + " " + idRuolo + " " + nomeSquadra + " " + newQuotaz);
						listGiocatoriAdd.add(giocatore);
						// } else {
						// LOG.error(" NOME GIOCATORE DOPPIO " + cognGiocatore +
						// " " + idRuolo + " " + nomeSquadra);
						// }
					}
				}

				if (updateQuotazioni) {
					int newQuotaz = calcolaQuotazione(quotazioneAttuale, idRuolo, percentuale);
					giocatore.setQuotazione(Integer.valueOf(newQuotaz));
				}

				giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
				giocatore.setCognGiocatore(cognGiocatore);

				FcRuolo ruolo = new FcRuolo();
				ruolo.setIdRuolo(idRuolo);
				giocatore.setFcRuolo(ruolo);

				FcSquadra squadra = squadraRepository.findByNomeSquadra(nomeSquadra);
				giocatore.setFcSquadra(squadra);

				boolean flagAttivo = !"No".equals(record.get(7)) ? true : false;
				giocatore.setFlagAttivo(flagAttivo);
				if (giocatore.isFlagAttivo()) {
					giocatores.add(giocatore);
				}

				if (updateImg || (giocatore.getNomeImg() == null && giocatore.getImg() == null)) {
					String nomeImg = cognGiocatore.toUpperCase();
					if (giocatore.getNomeImg() != null) {
						if (!"no-campioncino.png".equals(giocatore.getNomeImg()) && nomeImg.equals(giocatore.getNomeImg())) {
							nomeImg = giocatore.getNomeImg();
							int idx = nomeImg.indexOf(".png");
							if (idx != -1) {
								String nomeImg2 = nomeImg.substring(0, idx);
								nomeImg = nomeImg2;
								LOG.info("NEW nomeImg " + nomeImg);
							}
						}
					}
					nomeImg = Utils.replaceString(nomeImg, "'", "");
					nomeImg = Utils.replaceString(nomeImg, "_", "-");
					nomeImg = Utils.replaceString(nomeImg, " ", "-");
					nomeImg = Utils.replaceString(nomeImg, ".", "");
					nomeImg = Utils.replaceString(nomeImg, "'", "-");
					nomeImg = nomeImg + ".png";

					boolean flag = Utils.downloadFile(httpUrlImg + nomeImg, imgPath + nomeImg);
					if (!flag) {
						int idx = nomeImg.indexOf("-");
						if (idx != -1) {
							String nomeImg2 = nomeImg.substring(0, idx) + ".png";
							flag = Utils.downloadFile(httpUrlImg + nomeImg2, imgPath + nomeImg2);
							if (!flag) {
								nomeImg = "no-campioncino.png";
							} else {

								nomeImg = nomeImg2;
								flag = Utils.buildFileSmall(imgPath + nomeImg, imgPath + "small-" + nomeImg);

								File existFile = new File(imgPath + nomeImg);
								if (!existFile.exists()) {
									nomeImg = "no-campioncino.png";
									LOG.info("NOT existFile " + imgPath + nomeImg);
								}

								File existFileSmall = new File(imgPath + "small-" + nomeImg);
								if (!existFileSmall.exists()) {
									nomeImg = "no-campioncino.png";
									LOG.info("NOT existFileSmall " + imgPath + "small-" + nomeImg);
								}
							}

						} else {
							nomeImg = "no-campioncino.png";
						}

					} else {
						flag = Utils.buildFileSmall(imgPath + nomeImg, imgPath + "small-" + nomeImg);

						File existFile = new File(imgPath + nomeImg);
						if (!existFile.exists()) {
							nomeImg = "no-campioncino.png";
							LOG.info("NOT existFile " + imgPath + nomeImg);
						}

						File existFileSmall = new File(imgPath + "small-" + nomeImg);
						if (!existFileSmall.exists()) {
							nomeImg = "no-campioncino.png";
							LOG.info("NOT existFileSmall " + imgPath + "small-" + nomeImg);
						}
					}
					// nomeImg = "no-campioncino.png";

					giocatore.setNomeImg(nomeImg);
					giocatore.setImg(BlobProxy.generateProxy(Utils.getImage(imgPath + nomeImg)));
					giocatore.setImgSmall(BlobProxy.generateProxy(Utils.getImage(imgPath + "small-" + nomeImg)));
				}
			}

			if (giocatores.size() > 0) {

				for (FcGiocatore gioc : listG) {
					String sql = "\n UPDATE fc_giocatore SET ";
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

				String delete1 = " delete from fc_statistiche where id_giocatore in ( ";
				delete1 += " select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null) ";
				delete1 += " ) ";
				jdbcTemplate.update(delete1);
				LOG.info("delete1 " + delete1);

				String delete2 = " delete from fc_pagelle where id_giocatore in ( ";
				delete2 += " select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
				delete2 += " ) ";
				jdbcTemplate.update(delete2);
				LOG.info("delete2 " + delete2);

				String delete3 = " delete from fc_giornata_giocatore where id_giocatore in ( ";
				delete3 += " select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
				delete3 += " ) ";
				jdbcTemplate.update(delete3);
				LOG.info("delete3 " + delete3);

				String delete4 = " delete from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null) ";
				jdbcTemplate.update(delete4);
				LOG.info("delete4 " + delete4);

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

	public void initiDb(Integer codiceGiornata) throws Exception {
		LOG.info("START initiDb");

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

		LOG.info("END initiDb");

	}

	public void generaCalendario(FcCampionato campionato) throws Exception {

		Integer[] squadreInt = new Integer[8];
		String[] squadre = new String[8];
		int[] solutionArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
		shuffleArray(solutionArray);
		for (int i = 0; i < solutionArray.length; i++) {
			LOG.debug(solutionArray[i] + " ");
			squadre[i] = "" + solutionArray[i];
			squadreInt[i] = Integer.parseInt(squadre[i]);
		}
		LOG.debug("");

		// algoritmoDiBerger(squadre);

		calendarNew(campionato, squadreInt);
	}

	private Sort sortByIdSquadra() {
		return Sort.by(Sort.Direction.ASC, "idSquadra");
	}

	public void initPagelle(Integer giornata) {
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
	}

	public void executeUpdateDbFcExpRoseA(boolean freePlayer,
			Integer idCampionato) throws Exception {

		LOG.info("START executeUpdateDbFcExpRoseA");

		FcCampionato campionato = campionatoRepository.findByIdCampionato(idCampionato);

		String table = "fc_exp_rosea";
		if (freePlayer) {
			table = "fc_exp_free_pl";
		}

		jdbcTemplate.update("delete from " + table);

		List<FcSquadra> ls = (List<FcSquadra>) squadraRepository.findAll(sortByIdSquadra());
		int numRighe = 81;
		if (ls.size() > 20) {
			numRighe = 121;
		}
		if (ls.size() > 30) {
			numRighe = 161;
		}

		String ORDINAMENTO = "";
		String update = "";

		for (int i = 1; i <= numRighe; i++) {
			ORDINAMENTO = "" + i;

			String col = "";
			String val = "";
			for (int c = 1; c <= 10; c++) {
				col = col + "S" + c + ",R" + c + ",Q" + c + ",";
				val = val + "null,null,null,";
			}

			col = col.substring(0, col.length() - 1);
			val = val.substring(0, val.length() - 1);

			update = "insert into " + table + " (id," + col + ") values (";
			update += ORDINAMENTO + ",";
			update += val;
			update += ")";

			jdbcTemplate.update(update);
		}

		int i = 0;
		for (FcSquadra s : ls) {

			int c = s.getIdSquadra();
			if (i > 9 && i < 20) {
				c = c - 10;
			} else if (i > 19 && i < 30) {
				c = c - 20;
			} else if (i > 29 && i < 40) {
				c = c - 30;
			}

			String up1 = "S" + c + "='" + s.getNomeSquadra() + "'";
			String up2 = "R" + c + "='R'";
			String up3 = "Q" + c + "='Q'";

			String id = "1";
			if (i > 9) {
				id = "41";
			}
			if (i > 19) {
				id = "81";
			}
			if (i > 29) {
				id = "121";
			}

			update = "update " + table + " set " + up1 + " , " + up2 + " , " + up3 + " WHERE ID=" + id;

			jdbcTemplate.update(update);

			List<FcGiocatore> giocatores = null;
			if (freePlayer) {
				List<FcFormazione> allFormaz = formazioneRepository.findByFcCampionato(campionato);
				List<Integer> listNotIn = new ArrayList<Integer>();
				for (FcFormazione f : allFormaz) {
					if (f.getFcGiocatore() != null) {
						listNotIn.add(f.getFcGiocatore().getIdGiocatore());
					}
				}
				giocatores = (List<FcGiocatore>) giocatoreRepository.findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(true, s, listNotIn);

			} else {
				giocatores = (List<FcGiocatore>) giocatoreRepository.findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(true, s);
			}

			int newRec = giocatores.size();
			LOG.info(s.getNomeSquadra() + " TOT " + newRec);
			String cognGiocatore = "";
			String ruolo = "";
			String sQuot = "";

			int key = 2;
			if (i > 9 && i < 20) {
				key = 42;
			} else if (i > 19 && i < 30) {
				key = 82;
			} else if (i > 29 && i < 40) {
				key = 122;
			}

			for (int i2 = 0; i2 < 40; i2++) {
				if (i2 < newRec) {
					FcGiocatore giocatore = (FcGiocatore) giocatores.get(i2);
					cognGiocatore = giocatore.getCognGiocatore();
					ruolo = giocatore.getFcRuolo().getIdRuolo();
					sQuot = giocatore.getQuotazione().toString();
				} else {
					cognGiocatore = "";
					ruolo = "";
					sQuot = "";
				}
				cognGiocatore = Utils.replaceString(cognGiocatore, "'", "''");
				up1 = "S" + c + "='" + cognGiocatore + "'";
				up2 = "R" + c + "='" + ruolo + "'";
				up3 = "Q" + c + "='" + sQuot + "'";

				update = "update " + table + " set " + up1 + " , " + up2 + " , " + up3 + " WHERE ID=" + key;

				jdbcTemplate.update(update);

				key++;
			}

			i++;
		}
		LOG.info("END executeUpdateDbFcExpRoseA");
	}

	public void aggiornamentoPFGiornata(Properties p, String fileName,
			String idGiornata) {

		LOG.info("START aggiornamentoPFGiornata");

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
			formazioneHtml += "Giocatore";
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
				// LOG.info(""+record.size());

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
				// String Gdv = record.get(24);
				c++;
				// String Gdp = record.get(25);
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
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,"))
							G = "6";
						// LOG.debug("PORTIERE s.v.: "+Giocatore);
					} else {
						if (G.equals("") || G.equals("s.v.") || G.equals("s,v,")) {
							G = "0";
							count_sv++;
						}
					}
					BigDecimal bgG = new BigDecimal(G);
					BigDecimal mG = new BigDecimal(Costants.DIVISORE_100);
					BigDecimal risG = bgG.multiply(mG);
					long votoG = risG.longValue();

					CS = Utils.replaceString(CS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,"))
							CS = "6";
					} else {
						if (CS.equals("") || CS.equals("s.v.") || CS.equals("s,v,")) {
							CS = "0";
							count_sv++;
						}
					}

					BigDecimal bgCS = new BigDecimal(CS);
					BigDecimal mCS = new BigDecimal(Costants.DIVISORE_100);
					BigDecimal risCS = bgCS.multiply(mCS);
					long votoCS = risCS.longValue();

					TS = Utils.replaceString(TS, ",", ".");
					// PORTIERE SV
					if (ruolo.equals("P")) {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,"))
							TS = "6";
					} else {
						if (TS.equals("") || TS.equals("s.v.") || TS.equals("s,v,")) {
							TS = "0";
							count_sv++;
						}
					}

					BigDecimal bgTS = new BigDecimal(TS);
					BigDecimal mTS = new BigDecimal(Costants.DIVISORE_100);
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

					BigDecimal bg = new BigDecimal(VOTO_GIOCATORE);
					BigDecimal m = new BigDecimal("100");
					BigDecimal ris = bg.multiply(m);
					long voto = ris.longValue();
					LOG.debug("voto M3 " + voto);

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
					BigDecimal _moltipl = new BigDecimal(Costants.DIVISORE_100);
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

					String update = "update fc_pagelle set voto_giocatore=" + new_voto;
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
					update += " where id_giocatore=" + idGiocatore;
					update += " and id_giornata=" + idGiornata;

					jdbcTemplate.update(update);

				} else {
					LOG.info("*************************");
					LOG.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
					LOG.info("*************************");

					infoNewGiocatore += "\n" + "NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra;
				}
			}

			String email_destinatario = (String) p.getProperty("to");
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
				String from = (String) env.getProperty("spring.mail.secondary.username");
				emailService.sendMail(false,from, to, cc, bcc, subject, formazioneHtml, "text/html", "3", att);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				try {
					String from = (String) env.getProperty("spring.mail.primary.username");
					emailService.sendMail(true,from, to, cc, bcc, subject, formazioneHtml, "text/html", "3", att);
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
				}
			}

			LOG.info("END aggiornamentoPFGiornata");

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

	public void seiPolitico(Integer giornata, FcSquadra squadra)
			throws Exception {

		LOG.info("START seiPolitico");
		LOG.info("Giornata " + giornata + " squadra " + squadra.getNomeSquadra());
		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcPagelle> lPagelle = (List<FcPagelle>) pagelleRepository.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);
		int v = 600;
		for (FcPagelle pagelle : lPagelle) {

			FcSquadra sq = pagelle.getFcGiocatore().getFcSquadra();
			boolean check = false;
			if (squadra != null && (squadra.getIdSquadra() == sq.getIdSquadra())) {
				check = true;
			}

			if (squadra == null || check) {
				String sql = "UPDATE fc_pagelle SET ";
				sql += " voto_giocatore=" + v;
				sql += " ,g=0";
				sql += " ,cs=0";
				sql += " ,ts=0";
				sql += " ,goal_realizzato=0";
				sql += " ,goal_subito=0";
				sql += " ,ammonizione=0";
				sql += " ,espulsione=0";
				sql += " ,rigore_segnato=0";
				sql += " ,rigore_fallito=0";
				sql += " ,rigore_parato=0";
				sql += " ,autorete=0";
				sql += " ,assist=0";
				sql += " WHERE ID_GIOCATORE=" + pagelle.getFcGiocatore().getIdGiocatore();
				sql += " AND ID_GIORNATA=" + giornataInfo.getCodiceGiornata();
				this.jdbcTemplate.execute(sql);

				sql = "UPDATE fc_giornata_dett SET ";
				sql += " VOTO=" + v;
				sql += " WHERE ID_GIOCATORE=" + pagelle.getFcGiocatore().getIdGiocatore();
				sql += " AND ID_GIORNATA=" + giornataInfo.getCodiceGiornata();
				this.jdbcTemplate.execute(sql);
			}
		}

		LOG.info("END seiPolitico");

	}

	public void checkSeiPolitico(Integer giornata) throws Exception {

		LOG.info("START checkSeiPolitico");

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);

		List<FcSquadra> ls = (List<FcSquadra>) squadraRepository.findAll(sortByIdSquadra());
		for (FcSquadra s : ls) {

			String sql = " select COUNT(p.ID_GIOCATORE) from fc_pagelle p, ";
			sql += " fc_giocatore g ";
			sql += " where  g.id_giocatore=p.id_giocatore ";
			sql += " and p.id_giornata=" + giornata.intValue();
			sql += " and g.id_squadra=" + s.getIdSquadra();
			sql += " and goal_realizzato=0";
			sql += " and goal_subito=0";
			sql += " and ammonizione=0";
			sql += " and espulsione=0";
			sql += " and rigore_segnato=0";
			sql += " and rigore_fallito=0";
			sql += " and rigore_parato=0";
			sql += " and autorete=0";
			sql += " and assist=0";
			sql += " and ( ts = 600 or ts = 0) ";
			sql += " and ( cs = 600 or cs = 0) ";
			sql += " and ( g = 601 or g = 604 or g = 0) ";
			sql += " AND ( VOTO_GIOCATORE = 600 or VOTO_GIOCATORE = 601 or VOTO_GIOCATORE = 604) ";

			Boolean bSeiPolitico = jdbcTemplate.query(sql, new ResultSetExtractor<Boolean>(){
				@Override
				public Boolean extractData(ResultSet rs)
						throws SQLException, DataAccessException {
					if (rs.next()) {
						int count = rs.getInt(1);
						if (count > 20) {
							return Boolean.TRUE;
						}
					}
					return Boolean.FALSE;
				}
			});

			if (bSeiPolitico.booleanValue()) {
				seiPolitico(giornata, s);
			}
		}

		LOG.info("END checkSeiPolitico");
	}

	public void aggiornaVotiGiocatori(int giornata, int forzaVotoGiocatore,
			boolean bRoundVoto) throws Exception {

		LOG.info("START aggiornaVotiGiocatori");

		// List<Pagelle> lPagelle = (List<Pagelle>) pagelleRepository.findAll();

		FcGiornataInfo giornataInfo = new FcGiornataInfo();
		giornataInfo.setCodiceGiornata(giornata);
		List<FcPagelle> lPagelle = (List<FcPagelle>) pagelleRepository.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);

		for (FcPagelle pagelle : lPagelle) {

			int VOTO_GIOCATORE = Utils.buildVoto(pagelle, bRoundVoto);

			if (forzaVotoGiocatore == 0) {
				VOTO_GIOCATORE = forzaVotoGiocatore;
			}

			String sql = "UPDATE fc_giornata_dett SET ";
			sql += " VOTO=" + VOTO_GIOCATORE;
			sql += " WHERE ID_GIOCATORE=" + pagelle.getFcGiocatore().getIdGiocatore();
			sql += " AND ID_GIORNATA=" + pagelle.getFcGiornataInfo().getCodiceGiornata();
			this.jdbcTemplate.execute(sql);
		}

		LOG.info("END aggiornaVotiGiocatori");

	}

	public void aggiornaTotRosa(String idCampionato, int giornata)
			throws Exception {

		LOG.info("START aggiornaTotRosa");

		String sql = " select pt.id_giornata,pt.id_attore,";
		sql += " sum(pt.voto) as tot25 ";
		sql += " from fc_giornata_dett pt ";
		sql += " where  pt.id_giornata>= " + giornata;
		sql += " group by pt.id_giornata,pt.id_attore ";
		sql += " order by 1,3 desc";

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				int id_giornata = 0;
				int id_attore = 0;
				int tot25 = 0;
				while (rs.next()) {

					id_giornata = rs.getInt(1);
					id_attore = rs.getInt(2);
					tot25 = rs.getInt(3);

					String sqlUpdate = " UPDATE fc_classifica_tot_pt SET ";
					sqlUpdate += " tot_pt_rosa=" + tot25;
					sqlUpdate += " WHERE id_attore=" + id_attore;
					sqlUpdate += " AND id_giornata=" + id_giornata;
					jdbcTemplate.execute(sqlUpdate);
				}

				return "1";
			}
		});

		for (int attore = 1; attore < 9; attore++) {

			sql = " SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA) , ID_ATTORE FROM fc_classifica_tot_pt ";
			sql += " WHERE ID_CAMPIONATO=" + idCampionato;
			sql += " AND ID_ATTORE =" + attore;

			jdbcTemplate.query(sql, new ResultSetExtractor<String>(){

				@Override
				public String extractData(ResultSet rs)
						throws SQLException, DataAccessException {
					if (rs.next()) {

						String tot_punti = rs.getString(1);
						String tot_punti_old = rs.getString(2);
						String tot_punti_rosa = rs.getString(3);
						String idAttore = rs.getString(4);

						String query = " UPDATE fc_classifica SET TOT_PUNTI=" + tot_punti + ",";
						query += " TOT_PUNTI_OLD=" + tot_punti_old + ",";
						query += " TOT_PUNTI_ROSA=" + tot_punti_rosa;
						query += " WHERE ID_CAMPIONATO=" + idCampionato;
						query += " AND ID_ATTORE =" + idAttore;

						jdbcTemplate.update(query);

						return "1";
					}

					return null;
				}
			});

		}

		LOG.info("END aggiornaTotRosa");

	}

	public void aggiornaScore(int giornata, String colPt, String colScore)
			throws Exception {

		LOG.info("START aggiornaScore");

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("7", "25");
		map.put("6", "18");
		map.put("5", "15");
		map.put("4", "12");
		map.put("3", "10");
		map.put("2", "8");
		map.put("1", "6");
		map.put("0", "4");

		String sql = " select pt.id_giornata,pt.id_attore,";
		sql += " sum(pt." + colPt + ") as score ";
		sql += " from fc_classifica_tot_pt pt ";
		sql += " where pt.id_giornata>= " + giornata;
		sql += " group by pt.id_giornata,pt.id_attore ";
		sql += " order by 1,3 desc";

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				int id_giornata = 0;
				int id_attore = 0;
				String score = "0";

				int conta = 7;
				while (rs.next()) {

					id_giornata = rs.getInt(1);
					id_attore = rs.getInt(2);

					score = "" + conta;
					if ("score_grand_prix".equals(colScore)) {
						score = map.get("" + conta);
					}

					String sqlUpdate = " UPDATE fc_classifica_tot_pt SET ";
					sqlUpdate += colScore + "=" + score;
					sqlUpdate += " WHERE id_attore=" + id_attore;
					sqlUpdate += " AND id_giornata=" + id_giornata;
					jdbcTemplate.execute(sqlUpdate);

					conta--;
					if (conta == -1) {
						conta = 7;
					}
				}

				return "1";
			}
		});

		LOG.info("END aggiornaScore");

	}

	private Sort sortBy() {
		return Sort.by(Sort.Direction.ASC, "fcGiocatore");
	}

	public void statistiche(FcCampionato campionato) throws Exception {

		LOG.info("START statistiche");

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

				List<FcFormazione> listFormazione = formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato, appoFcGiocatore);
				String proprietario = "";
				if (listFormazione != null && listFormazione.size() > 0) {
					FcFormazione formazione = (FcFormazione) listFormazione.get(0);
					if (formazione != null) {
						proprietario = formazione.getFcAttore().getDescAttore();
					}
				}

				statistiche.setProprietario(proprietario);
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

				// LOG.debug("SAVE STATISTICA GIOCATORE " +
				// appoFcGiocatore.getIdGiocatore() + " " +
				// appoFcGiocatore.getCognGiocatore() + " " + proprietario);

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

		List<FcFormazione> listFormazione = formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato, appoFcGiocatore);
		String proprietario = "";
		if (listFormazione != null && listFormazione.size() > 0) {
			FcFormazione formazione = (FcFormazione) listFormazione.get(0);
			if (formazione != null) {
				proprietario = formazione.getFcAttore().getDescAttore();
			}
		}

		statistiche.setProprietario(proprietario);
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
		// appoFcGiocatore.getCognGiocatore() + " " + proprietario);

		statisticheRepository.save(statistiche);

		// for (FcPagelle p : lPagelle) {
		//
		// FcGiocatore giocatore = p.getFcGiocatore();
		// int idGiocatore = giocatore.getIdGiocatore();
		// if (idGiocatore == appoIdGiocatore) {
		//
		// if (p.getVotoGiocatore() > 0) {
		//
		// votoGiocatore += p.getVotoGiocatore();
		// fantaMedia += buildFantaMedia(p);
		// goalRealizzato += p.getGoalRealizzato();
		// goalSubito += p.getGoalSubito();
		// ammonizione += p.getAmmonizione();
		// espulso += p.getEspulsione();
		// rigoreFallito += p.getRigoreFallito();
		// rigoreSegnato += p.getRigoreSegnato();
		// assist += p.getAssist();
		//
		// giocate = giocate + 1;
		// }
		//
		// } else {
		//
		// String update = "";
		// if (giocate > 0) {
		// update = "update fc_statistiche set media_voto=" + (votoGiocatore /
		// giocate);
		// update += ",fanta_media=" + (fantaMedia / giocate);
		// } else {
		// update = "update fc_statistiche set media_voto=0";
		// update += ",fanta_media=0";
		// }
		// update += ",proprietario='" + proprietario + "'";
		// update += ",giocate=" + giocate;
		// update += ",goal_fatto=" + goalRealizzato;
		// update += ",goal_subito=" + goalSubito;
		// update += ",ammonizione=" + ammonizione;
		// update += ",espulsione=" + espulso;
		// update += ",rigore_sbagliato=" + rigoreFallito;
		// update += ",rigore_segnato=" + rigoreSegnato;
		// update += ",assist=" + assist;
		// update += " where id_giocatore=" + appoIdGiocatore;
		//
		// jdbcTemplate.update(update);
		//
		// appoIdGiocatore = giocatore.getIdGiocatore();
		// formazione =
		// formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato,
		// p.getFcGiocatore());
		// proprietario = "";
		// if (formazione != null) {
		// proprietario = formazione.getFcAttore().getDescAttore();
		// }
		//
		// votoGiocatore = p.getVotoGiocatore();
		// fantaMedia = buildFantaMedia(p);
		// goalRealizzato = p.getGoalRealizzato();
		// goalSubito = p.getGoalSubito();
		// ammonizione = p.getAmmonizione();
		// espulso = p.getEspulsione();
		// rigoreFallito = p.getRigoreFallito();
		// rigoreSegnato = p.getRigoreSegnato();
		// assist = p.getAssist();
		//
		// giocate = 0;
		// if (p.getVotoGiocatore() > 0) {
		// giocate = giocate + 1;
		// }
		// }
		// }
		//
		// String update = "";
		// if (giocate > 0) {
		// update = "update fc_statistiche set media_voto=" + (votoGiocatore /
		// giocate);
		// update += ",fanta_media=" + (fantaMedia / giocate);
		// } else {
		// update = "update fc_statistiche set media_voto=0";
		// update += ",fanta_media=0";
		// }
		// update += ",proprietario='" + proprietario + "'";
		// update += ",giocate=" + giocate;
		// update += ",goal_fatto=" + goalRealizzato;
		// update += ",goal_subito=" + goalSubito;
		// update += ",ammonizione=" + ammonizione;
		// update += ",espulsione=" + espulso;
		// update += ",rigore_sbagliato=" + rigoreFallito;
		// update += ",rigore_segnato=" + rigoreSegnato;
		// update += ",assist=" + assist;
		// update += " where id_giocatore=" + appoIdGiocatore;
		//
		// jdbcTemplate.update(update);

		LOG.info("END statistiche");

	}

	public void inserisciUltimaFormazione(int idAttore, int giornata)
			throws Exception {
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
	}

	public void resetFormazione(int idAttore, int giornata) throws Exception {
		String delete = "delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete);
		String delete2 = "delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore;
		jdbcTemplate.update(delete2);
	}

	public void inserisciFormazione442(FcCampionato campionato, FcAttore attore,
			int giornata) throws Exception {

		int idAttore = attore.getIdAttore();
		String query = " DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
		jdbcTemplate.update(query);

		List<FcFormazione> listFormazione = formazioneRepository.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);

		ArrayList<FcFormazione> listTribuna = new ArrayList<FcFormazione>();
		boolean bInsert18 = false;
		for (FcFormazione f : listFormazione) {

			int ord = f.getId().getOrdinamento();
			if (f.getFcGiocatore() == null) {
				continue;
			}
			if (ord == 2) {
				ord = 12;
			} else if (ord == 3 || ord == 4 || ord == 5 || ord == 6 || ord == 7 || ord == 8 || ord == 9 || ord == 10 || ord == 11 || ord == 12) {
				ord--;
			} else if (ord == 13 || ord == 14 || ord == 15 || ord == 16 || ord == 17) {

			} else if (ord > 17) {
				if (!bInsert18 && "A".equals(f.getFcGiocatore().getFcRuolo().getIdRuolo())) {
					ord = 18;
					bInsert18 = true;
				} else {
					listTribuna.add(f);
					continue;
				}
			}

			int idGiocatore = f.getFcGiocatore().getIdGiocatore();
			int ordinamento = ord;
			String idStatoGiocatore = "T";
			if (ord > 11) {
				idStatoGiocatore = "R";
			}

			query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + giornata + ",";
			query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ordinamento + ",0)";
			jdbcTemplate.update(query);

		}

		int ordTrib = 19;
		for (FcFormazione f : listTribuna) {
			int ordinamento = ordTrib;
			if (f.getFcGiocatore() == null) {
				continue;
			}
			ordTrib++;
			String idStatoGiocatore = "N";
			int idGiocatore = f.getFcGiocatore().getIdGiocatore();

			query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES (" + giornata + ",";
			query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ordinamento + ",0)";

			jdbcTemplate.update(query);
		}

		query = " DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
		jdbcTemplate.update(query);

		String dataora = getSysdate();
		query = " INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES (" + giornata + ",";
		query += idAttore + ",1, '" + dataora + "')";

		jdbcTemplate.update(query);

	}

	private String getSysdate() {

		String sql = "select sysdate() from dual";
		return jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				if (rs.next()) {

					String dataora = rs.getString(1);
					return dataora;
				}
				return null;
			}
		});
	}

	public void algoritmo(Integer giornata, FcCampionato campionato,
			int forzaVotoGiocatore, boolean bRoundVoto) throws Exception {

		LOG.info("START algoritmo");

		int MOLTIPLICATORE = 10000;

		int giornata_fc = giornata;
		if (giornata > 19) {
			giornata_fc = giornata - 19;
		}
		Buffer bufBonus = new Buffer();
		if (giornata_fc == 15) {
			bufBonus = getAttoriBonusOttaviAndata("" + campionato.getIdCampionato());
		} else if (giornata_fc == 17) {
			bufBonus = getAttoriBonusSemifinaliAndata("" + campionato.getIdCampionato());
		}

		LOG.info("giornata " + giornata);

		FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(giornata);

		List<FcGiornata> lGiornata = giornataRepository.findByFcGiornataInfo(giornataInfo);

		List<FcAttore> l = (List<FcAttore>) attoreRepository.findByActive(true);

		for (FcAttore attore : l) {

			if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {

			} else {
				continue;
			}

			LOG.debug("----------------------------------------");
			LOG.debug("START DESC_ATTORE      -----> " + attore.getDescAttore());
			LOG.debug("----------------------------------------");
			LOG.debug("");

			List<FcGiornataDett> lGiocatori = giornataDettRepository.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

			int ID_ATTORE = attore.getIdAttore();

			String id_cogn_min_por_tit = "";
			String id_cogn_min_dif_tit = "";
			String id_cogn_min_cen_tit = "";
			String id_cogn_min_att_tit = "";

			String id_cogn_min_por_ris = "";
			String id_cogn_min_dif_ris = "";
			String id_cogn_min_cen_ris = "";
			String id_cogn_min_att_ris = "";

			int voto_min_por_tit = MOLTIPLICATORE;
			int voto_min_dif_tit = MOLTIPLICATORE;
			int voto_min_cen_tit = MOLTIPLICATORE;
			int voto_min_att_tit = MOLTIPLICATORE;

			int voto_min_por_ris = MOLTIPLICATORE;
			int voto_min_dif_ris = MOLTIPLICATORE;
			int voto_min_cen_ris = MOLTIPLICATORE;
			int voto_min_att_ris = MOLTIPLICATORE;

			// int count_por = 0;
			int count_dif = 0;
			int count_cen = 0;
			int count_att = 0;

			int somma = 0;
			int sommaTitolariRiserve = 0;
			boolean bSomma = true;

			String[] cambi = new String[7];
			cambi[0] = "";
			cambi[1] = "";
			cambi[2] = "";
			cambi[3] = "";

			// X OGNI RUOLO VERIFICO SE e' POSSIBILE IL CAMBIO DELLA SECONDA
			// RISERVA
			boolean flagChangeRis2Dif = validateCambioRiserva2(lGiocatori, "D");
			boolean flagChangeRis2Cen = validateCambioRiserva2(lGiocatori, "C");
			boolean flagChangeRis2Att = validateCambioRiserva2(lGiocatori, "A");

			for (FcGiornataDett giornataDett : lGiocatori) {

				// if (giornataDett.getOrdinamento() > 18) {
				// continue;
				// }

				FcGiocatore giocatore = giornataDett.getFcGiocatore();

				FcPagelle pagelle = pagelleRepository.findByFcGiornataInfoAndFcGiocatore(giornataInfo, giocatore);

				int ORDINAMENTO = giornataDett.getOrdinamento();
				String ID_GIOCATORE = "" + giocatore.getIdGiocatore();
				String ID_STATO_GIOCATORE = giornataDett.getFcStatoGiocatore().getIdStatoGiocatore();
				String ID_RUOLO = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
				int ESPULSO = pagelle.getEspulsione();

				int VOTO_GIOCATORE = Utils.buildVoto(pagelle, bRoundVoto);

				if (forzaVotoGiocatore == 0) {
					VOTO_GIOCATORE = forzaVotoGiocatore;
				}

				// VOTO_GIOCATORE SENZA PENALITA DI 0,5 DELLA SECONDA RISERVA
				int VOTO_GIOCATORE_NO_PENALITA = VOTO_GIOCATORE;

				LOG.debug(giornataDett.getOrdinamento() + " ID_GIOCATORE " + ID_GIOCATORE + " " + giocatore.getCognGiocatore() + " VOTO_GIOCATORE " + VOTO_GIOCATORE);

				if (giornataDett.getOrdinamento() > 18) {
					String query = "UPDATE fc_giornata_dett SET ";
					query += " FLAG_ATTIVO='N' , VOTO=" + VOTO_GIOCATORE;
					query += " WHERE ID_GIOCATORE=" + ID_GIOCATORE;
					query += " AND ID_GIORNATA=" + giornata;
					query += " AND ID_ATTORE=" + ID_ATTORE;

					jdbcTemplate.update(query);

					continue;
				}

				if (ID_RUOLO.equals("P")) {
					// count_por++;
					if (ID_STATO_GIOCATORE.equals("T")) {
						if (ESPULSO == 0 && voto_min_por_tit > VOTO_GIOCATORE) {
							voto_min_por_tit = VOTO_GIOCATORE;
							id_cogn_min_por_tit = ID_GIOCATORE;
						}
					} else if (ID_STATO_GIOCATORE.equals("R")) {
						voto_min_por_ris = VOTO_GIOCATORE;
						id_cogn_min_por_ris = ID_GIOCATORE;
					}

				} else if (ID_RUOLO.equals("D")) {
					count_dif++;

					if (ID_STATO_GIOCATORE.equals("T")) {
						if (ESPULSO == 0 && voto_min_dif_tit > VOTO_GIOCATORE) {
							voto_min_dif_tit = VOTO_GIOCATORE;
							id_cogn_min_dif_tit = ID_GIOCATORE;
						}
					} else if (ID_STATO_GIOCATORE.equals("R")) {

						if (ORDINAMENTO == 13) {

							if (!flagChangeRis2Dif) {
								voto_min_dif_ris = VOTO_GIOCATORE;
								id_cogn_min_dif_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[4] = ID_GIOCATORE;
							}

						} else if (ORDINAMENTO == 14) {

							if (flagChangeRis2Dif) {
								VOTO_GIOCATORE = VOTO_GIOCATORE - Costants.DIV_0_5;
								voto_min_dif_ris = VOTO_GIOCATORE;
								id_cogn_min_dif_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[4] = ID_GIOCATORE;
							}
						}
					}

				} else if (ID_RUOLO.equals("C")) {
					count_cen++;

					if (ID_STATO_GIOCATORE.equals("T")) {
						if (ESPULSO == 0 && voto_min_cen_tit > VOTO_GIOCATORE) {
							voto_min_cen_tit = VOTO_GIOCATORE;
							id_cogn_min_cen_tit = ID_GIOCATORE;
						}

					} else if (ID_STATO_GIOCATORE.equals("R")) {

						if (ORDINAMENTO == 15) {

							if (!flagChangeRis2Cen) {
								voto_min_cen_ris = VOTO_GIOCATORE;
								id_cogn_min_cen_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[5] = ID_GIOCATORE;
							}

						} else if (ORDINAMENTO == 16) {

							if (flagChangeRis2Cen) {
								VOTO_GIOCATORE = VOTO_GIOCATORE - Costants.DIV_0_5;
								voto_min_cen_ris = VOTO_GIOCATORE;
								id_cogn_min_cen_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[5] = ID_GIOCATORE;
							}
						}
					}

				} else if (ID_RUOLO.equals("A")) {
					count_att++;

					if (ID_STATO_GIOCATORE.equals("T")) {
						if (ESPULSO == 0 && voto_min_att_tit > VOTO_GIOCATORE) {
							voto_min_att_tit = VOTO_GIOCATORE;
							id_cogn_min_att_tit = ID_GIOCATORE;
						}
					} else if (ID_STATO_GIOCATORE.equals("R")) {

						if (ORDINAMENTO == 17) {

							if (!flagChangeRis2Att) {
								voto_min_att_ris = VOTO_GIOCATORE;
								id_cogn_min_att_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[6] = ID_GIOCATORE;
							}

						} else if (ORDINAMENTO == 18) {

							if (flagChangeRis2Att) {
								VOTO_GIOCATORE = VOTO_GIOCATORE - Costants.DIV_0_5;
								voto_min_att_ris = VOTO_GIOCATORE;
								id_cogn_min_att_ris = ID_GIOCATORE;
							} else {
								bSomma = false;
								cambi[6] = ID_GIOCATORE;
							}
						}
					}
				}

				if (bSomma) {
					somma = somma + VOTO_GIOCATORE;
				}
				bSomma = true;

				sommaTitolariRiserve = sommaTitolariRiserve + VOTO_GIOCATORE_NO_PENALITA;

				String query = "UPDATE fc_giornata_dett SET ";
				query += " FLAG_ATTIVO='S' , VOTO=" + VOTO_GIOCATORE;
				query += " WHERE ID_GIOCATORE=" + ID_GIOCATORE;
				query += " AND ID_GIORNATA=" + giornata;
				query += " AND ID_ATTORE=" + ID_ATTORE;

				jdbcTemplate.update(query);
			}

			LOG.debug("somma parziale   " + somma);
			LOG.debug("voto_min_por_tit " + voto_min_por_tit);
			LOG.debug("voto_min_dif_tit " + voto_min_dif_tit);
			LOG.debug("voto_min_cen_tit " + voto_min_cen_tit);
			LOG.debug("voto_min_att_tit " + voto_min_att_tit);

			LOG.debug("voto_min_por_ris " + voto_min_por_ris);
			LOG.debug("voto_min_dif_ris " + voto_min_dif_ris);
			LOG.debug("voto_min_cen_ris " + voto_min_cen_ris);
			LOG.debug("voto_min_att_ris " + voto_min_att_ris);

			// int diff_voto_min_por = voto_min_por_tit < 0 ? MOLTIPLICATORE :
			// (voto_min_por_tit - voto_min_por_ris + MOLTIPLICATORE);
			// int diff_voto_min_dif = voto_min_dif_tit < 0 ? MOLTIPLICATORE :
			// (voto_min_dif_tit - voto_min_dif_ris + MOLTIPLICATORE);
			// int diff_voto_min_cen = voto_min_cen_tit < 0 ? MOLTIPLICATORE :
			// (voto_min_cen_tit - voto_min_cen_ris + MOLTIPLICATORE);
			// int diff_voto_min_att = voto_min_att_tit < 0 ? MOLTIPLICATORE :
			// (voto_min_att_tit - voto_min_att_ris + MOLTIPLICATORE);
			int diff_voto_min_por = voto_min_por_tit - voto_min_por_ris + MOLTIPLICATORE;
			int diff_voto_min_dif = voto_min_dif_tit - voto_min_dif_ris + MOLTIPLICATORE;
			int diff_voto_min_cen = voto_min_cen_tit - voto_min_cen_ris + MOLTIPLICATORE;
			int diff_voto_min_att = voto_min_att_tit - voto_min_att_ris + MOLTIPLICATORE;

			Buffer b = new Buffer();
			b.addNew("@1" + voto_min_por_tit + "@2" + voto_min_por_ris + "@3" + diff_voto_min_por + "@4" + id_cogn_min_por_tit + "@5" + id_cogn_min_por_ris);
			b.addNew("@1" + voto_min_dif_tit + "@2" + voto_min_dif_ris + "@3" + diff_voto_min_dif + "@4" + id_cogn_min_dif_tit + "@5" + id_cogn_min_dif_ris);
			b.addNew("@1" + voto_min_cen_tit + "@2" + voto_min_cen_ris + "@3" + diff_voto_min_cen + "@4" + id_cogn_min_cen_tit + "@5" + id_cogn_min_cen_ris);
			b.addNew("@1" + voto_min_att_tit + "@2" + voto_min_att_ris + "@3" + diff_voto_min_att + "@4" + id_cogn_min_att_tit + "@5" + id_cogn_min_att_ris);

			LOG.debug("PRIMA--------------------");
			LOG.debug(b.getItem(1));
			LOG.debug(b.getItem(2));
			LOG.debug(b.getItem(3));
			LOG.debug(b.getItem(4));
			LOG.debug("--------------------");

			b.sort(3);

			LOG.debug("DOPO--------------------");
			LOG.debug(b.getItem(1));
			LOG.debug(b.getItem(2));
			LOG.debug(b.getItem(3));
			LOG.debug(b.getItem(4));
			LOG.debug("--------------------");

			for (int x = 0; x < b.getRowCount(); x++) {
				if (x == 0 || x == 1) {
					if (b.getFieldByInt(3) < MOLTIPLICATORE) {
						somma = somma - b.getFieldByInt(1);
						cambi[x] = b.getField(4);
					} else {
						somma = somma - b.getFieldByInt(2);
						cambi[x] = b.getField(5);
					}
				} else if (x == 2 || x == 3) {
					somma = somma - b.getFieldByInt(2);
					cambi[x] = b.getField(5);
				}
				b.moveNext();
			}

			for (int q = 0; q < cambi.length; q++) {

				String query = "UPDATE fc_giornata_dett SET ";
				query += " FLAG_ATTIVO='N'";
				query += " WHERE ID_GIOCATORE=" + cambi[q];
				query += " AND ID_GIORNATA=" + giornata;
				query += " AND ID_ATTORE=" + ID_ATTORE;

				jdbcTemplate.update(query);

			}

			// algoritmo dopo aver verificato se possono entrare 2 cambi
			// prendendo le differenze di voto migliori
			// successivamente prova ad effettuare il terzo cambio se e solo se
			// non si è raggiunto 11 giocatori con voto
			// prova ad inserire il terzo cambio prendendo in cosiderazione solo
			// il primo cambio per ruolo (deve avere ovviamente un voto >0)
			// per esempio:
			// P titolare non ha giocato ti entra il P riserva
			// oppure D titolare non ha giocato ti entra la prima D riserva 1 o
			// riserva 2 con penalità
			// oppure C titolare non ha giocato ti entra la prima C riserva 1 o
			// riserva 2 con penalità
			// oppure A titolare non ha giocato ti entra la prima A riserva 1 o
			// riserva 2 con penalità

			// VERIFICO TERZO CAMBIO POSSIBILE
			ArrayList<String> listaRuoliPossibiliCambi = new ArrayList<String>();
			ArrayList<String> listaIdGiocatoriCambiati = new ArrayList<String>();

			List<FcGiornataDett> lGiocatori2 = giornataDettRepository.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
			int countCambiEffettuati = 0;
			for (FcGiornataDett gd : lGiocatori2) {
				if (gd.getOrdinamento() == 12 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
					listaRuoliPossibiliCambi.add("P");
				}
				if (gd.getOrdinamento() == 13 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
					listaRuoliPossibiliCambi.add("D");
				}
				if (gd.getOrdinamento() == 15 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
					listaRuoliPossibiliCambi.add("C");
				}
				if (gd.getOrdinamento() == 17 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
					listaRuoliPossibiliCambi.add("A");
				}
				if (gd.getOrdinamento() > 11 && gd.getVoto() > 0 && "S".equals(gd.getFlagAttivo())) {
					countCambiEffettuati++;
				}
				if (gd.getOrdinamento() < 12 && gd.getVoto() == 0 && "N".equals(gd.getFlagAttivo())) {
					listaIdGiocatoriCambiati.add("" + gd.getId().getIdGiocatore());
				}
			}

			LOG.info("countCambiEffettuati       " + countCambiEffettuati);
			LOG.info("somma parziale prima cambi " + somma);
			boolean bCambioEffettuato = false;
			for (String r : listaRuoliPossibiliCambi) {

				if ("P".equals(r)) {
					HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori2, 12, r, somma);
					if (mapResult.containsKey("SOMMA")) {
						somma = Integer.parseInt((String) mapResult.get("SOMMA"));
						listaIdGiocatoriCambiati.add((String) mapResult.get("ID_GIOCATORE"));
						bCambioEffettuato = true;
						countCambiEffettuati++;
					}
				} else if ("D".equals(r)) {
					HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori2, 13, r, somma);
					if (mapResult.containsKey("SOMMA")) {
						somma = Integer.parseInt((String) mapResult.get("SOMMA"));
						listaIdGiocatoriCambiati.add((String) mapResult.get("ID_GIOCATORE"));
						bCambioEffettuato = true;
						countCambiEffettuati++;
					}
				} else if ("C".equals(r)) {
					HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori2, 15, r, somma);
					if (mapResult.containsKey("SOMMA")) {
						somma = Integer.parseInt((String) mapResult.get("SOMMA"));
						listaIdGiocatoriCambiati.add((String) mapResult.get("ID_GIOCATORE"));
						bCambioEffettuato = true;
						countCambiEffettuati++;
					}
				} else if ("A".equals(r)) {
					HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori2, 17, r, somma);
					if (mapResult.containsKey("SOMMA")) {
						somma = Integer.parseInt((String) mapResult.get("SOMMA"));
						listaIdGiocatoriCambiati.add((String) mapResult.get("ID_GIOCATORE"));
						bCambioEffettuato = true;
						countCambiEffettuati++;
					}
				}

				if (bCambioEffettuato) {
					LOG.info("3 CAMBIO 1 RISERVA EFFETTUATO");
					break;
				}
			}

			LOG.info("1 somma parziale dopo cambi " + somma);
			LOG.info("1 countCambiEffettuati      " + countCambiEffettuati);

			// sta iniziando ad essere complicato ... io riassumeri ..
			// ammessi 2 cambi con le regole che già sappiamo..
			// il 3 cambio è ammesso solo se non giocano 2 titolari di pari
			// ruolo .. ( es NON posso cambiare PDA ma potrei cambiare PAA)
			// in entrambi i casi vale la regola che il 2 cambio pari ruolo ha
			// un malus di -0,5

			if (countCambiEffettuati < 3) {

				listaRuoliPossibiliCambi = new ArrayList<String>();

				List<FcGiornataDett> lGiocatori3 = giornataDettRepository.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
				for (FcGiornataDett gd : lGiocatori3) {
					if (gd.getOrdinamento() == 14 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
						listaRuoliPossibiliCambi.add("D");
					}
					if (gd.getOrdinamento() == 16 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
						listaRuoliPossibiliCambi.add("C");
					}
					if (gd.getOrdinamento() == 18 && gd.getVoto() > 0 && "N".equals(gd.getFlagAttivo())) {
						listaRuoliPossibiliCambi.add("A");
					}
				}

				LOG.info("VERIFICO 3 CAMBIO 2 RISERVA");
				for (String r : listaRuoliPossibiliCambi) {
					if ("D".equals(r)) {
						HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori3, 14, r, somma);
						if (mapResult.containsKey("SOMMA")) {
							somma = Integer.parseInt((String) mapResult.get("SOMMA"));
							somma = somma - Costants.DIV_0_5;
							bCambioEffettuato = true;
							countCambiEffettuati++;
						}
					} else if ("C".equals(r)) {
						HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori3, 16, r, somma);
						if (mapResult.containsKey("SOMMA")) {
							somma = Integer.parseInt((String) mapResult.get("SOMMA"));
							somma = somma - Costants.DIV_0_5;
							bCambioEffettuato = true;
							countCambiEffettuati++;
						}
					} else if ("A".equals(r)) {
						HashMap<String, String> mapResult = effettuaCambio(giornata, ID_ATTORE, listaIdGiocatoriCambiati, lGiocatori3, 18, r, somma);
						if (mapResult.containsKey("SOMMA")) {
							somma = Integer.parseInt((String) mapResult.get("SOMMA"));
							somma = somma - Costants.DIV_0_5;
							bCambioEffettuato = true;
							countCambiEffettuati++;
						}
					}
					if (bCambioEffettuato) {
						LOG.info("3 CAMBIO 2 RISERVA EFFETTUATO");
						break;
					}
				}
			}

			LOG.info("2 somma parziale dopo cambi " + somma);
			LOG.info("2 countCambiEffettuati      " + countCambiEffettuati);

			// BONUS MALUS SCHEMA
			count_dif = count_dif - 2;
			count_cen = count_cen - 2;
			count_att = count_att - 2;

			if (count_dif == 5 && count_cen == 4 && count_att == 1) {
				somma = somma + Costants.DIV_2_0;
				sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_2_0;
			} else if (count_dif == 5 && count_cen == 3 && count_att == 2) {
				somma = somma + Costants.DIV_1_0;
				sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_0;
			} else if (count_dif == 4 && count_cen == 5 && count_att == 1) {
				somma = somma + Costants.DIV_1_0;
				sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_0;
			} else if (count_dif == 4 && count_cen == 3 && count_att == 3) {
				somma = somma - Costants.DIV_1_0;
				sommaTitolariRiserve = sommaTitolariRiserve - Costants.DIV_1_0;
			} else if (count_dif == 3 && count_cen == 4 && count_att == 3) {
				somma = somma - Costants.DIV_2_0;
				sommaTitolariRiserve = sommaTitolariRiserve - Costants.DIV_2_0;
			}

			LOG.info("somma parziale " + somma);

			// BONUS CASA
			if (giornata_fc < 15) {
				for (FcGiornata g : lGiornata) {
					if (ID_ATTORE == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {
						somma = somma + Costants.DIV_1_5;
						LOG.info("somma dopo bonus casa " + somma);
						sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_5;
						break;
					}
				}
			}

			// CALCOLO TOTALE PUNTEGGIO
			String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + "" + campionato.getIdCampionato() + " AND ID_ATTORE=" + ID_ATTORE + " AND ID_GIORNATA=" + giornata + "";
			jdbcTemplate.update(query);

			int totGoalTVsTutti = getTotGoal(somma);
			int sommaTVsTutti = somma;

			query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT,TOT_PT_OLD,GOAL) ";
			query += " VALUES (" + "" + campionato.getIdCampionato() + "," + ID_ATTORE + "," + giornata + "," + sommaTitolariRiserve + "," + sommaTVsTutti + "," + totGoalTVsTutti + ")";
			jdbcTemplate.update(query);

			// BONUS QUARTI
			if (giornata_fc == 15) {
				int idx = bufBonus.findFirst("" + ID_ATTORE, 1, false);
				if (idx != -1) {
					somma = somma + bufBonus.getFieldByInt(2);
				}
			}

			// BONUS SEMIFINALI
			if (giornata_fc == 17) {
				int idx = bufBonus.findFirst("" + ID_ATTORE, 1, false);
				if (idx != -1) {
					somma = somma + bufBonus.getFieldByInt(2);
				}
			}

			// int roundSomma = Utils.arrotonda(somma);

			LOG.debug("----------------------------------------");
			LOG.debug("END DESC_ATTORE       -----> " + attore.getDescAttore());
			LOG.debug("SCHEMA                -----> " + count_dif + "-" + count_cen + "-" + count_att);
			LOG.debug("TOTALE FINALE         -----> " + somma);
			int totGoal = getTotGoal(somma);
			LOG.debug("GOAL SEGNATI          -----> " + totGoal);
			LOG.debug("GOAL   TUTTI VS TUTTI -----> " + totGoalTVsTutti);
			LOG.debug("TOTALE TUTTI VS TUTTI -----> " + sommaTVsTutti);
			LOG.debug("----------------------------------------");
			LOG.debug("");

			for (FcGiornata g : lGiornata) {

				if (ID_ATTORE == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {

					query = "UPDATE fc_giornata SET ";
					query += " TOT_CASA=" + somma + ",";
					query += " GOL_CASA=" + totGoal;
					query += " WHERE ID_GIORNATA=" + giornata;
					query += " AND ID_ATTORE_CASA=" + ID_ATTORE;

					jdbcTemplate.update(query);

				} else if (ID_ATTORE == g.getFcAttoreByIdAttoreFuori().getIdAttore()) {

					query = "UPDATE fc_giornata SET ";
					query += " TOT_FUORI=" + somma + ",";
					query += " GOL_FUORI=" + totGoal;
					query += " WHERE ID_GIORNATA=" + giornata;
					query += " AND ID_ATTORE_FUORI=" + ID_ATTORE;

					jdbcTemplate.update(query);
				}
			}
		}

		// AGGIORNO fc_giornata_ris

		Buffer buf = new Buffer();
		buf.addNew("@11@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@12@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@13@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@14@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@15@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@16@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@17@20@30@40@50@60@70@80@90@100@110@120@130@140");
		buf.addNew("@18@20@30@40@50@60@70@80@90@100@110@120@130@140");

		FcGiornataInfo start = new FcGiornataInfo();
		start.setCodiceGiornata(campionato.getStart());

		FcGiornataInfo end = new FcGiornataInfo();
		end.setCodiceGiornata(campionato.getEnd());

		List<FcGiornata> lSEGiornat = giornataRepository.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(start, end);

		int idAttoreCasa = 0;
		int idAttoreFuori = 0;
		int totCasa = 0;
		int totFuori = 0;
		int golCasa = 0;
		int golFuori = 0;
		int idGiornata = 0;

		int punti = 0;
		int vinte = 0;
		int pari = 0;
		int perse = 0;
		int gf = 0;
		int gs = 0;
		int dr = 0;
		int totPunti = 0;
		int totFm = 0;
		int risPartita = 0;

		for (FcGiornata g : lSEGiornat) {

			if (g.getFcAttoreByIdAttoreCasa() == null || g.getFcAttoreByIdAttoreFuori() == null || g.getTotCasa() == null || g.getTotFuori() == null || g.getGolCasa() == null || g.getGolFuori() == null) {
				continue;
			}
			idAttoreCasa = g.getFcAttoreByIdAttoreCasa().getIdAttore();
			idAttoreFuori = g.getFcAttoreByIdAttoreFuori().getIdAttore();
			totCasa = g.getTotCasa().intValue();
			totFuori = g.getTotFuori().intValue();
			golCasa = g.getGolCasa();
			golFuori = g.getGolFuori();
			idGiornata = g.getFcGiornataInfo().getCodiceGiornata();

			if (golCasa > golFuori) {
				punti = 3;
				vinte = 1;
				pari = 0;
				perse = 0;
				totFm = 2;
			} else if (golCasa == golFuori) {
				punti = 1;
				vinte = 0;
				pari = 1;
				perse = 0;
				totFm = 0;
			} else if (golCasa < golFuori) {
				punti = 0;
				vinte = 0;
				pari = 0;
				perse = 1;
				totFm = 0;
			}
			gf = golCasa;
			gs = golFuori;
			dr = gf - gs;
			totPunti = totCasa;

			if (vinte == 1) {
				risPartita = 1;
			} else if (pari == 1) {
				risPartita = 0;
			} else if (perse == 1) {
				risPartita = 2;
			}

			String query = " DELETE FROM fc_giornata_ris WHERE ID_GIORNATA =" + idGiornata + " AND ID_ATTORE=" + idAttoreCasa;
			// LOG.info(query);
			jdbcTemplate.update(query);

			query = "INSERT INTO fc_giornata_ris (id_giornata,id_attore,vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES (" + idGiornata + ",";
			query += idAttoreCasa + "," + vinte + ",";
			query += pari + "," + perse + ",";
			query += gf + "," + gs + ",";
			query += punti + "," + totFm + "," + risPartita + ",1)";
			// LOG.info(query);
			jdbcTemplate.update(query);

			int idx = buf.findFirst("" + idAttoreCasa, 1, false);
			if (idx != -1) {
				int currPunt = buf.getFieldByInt(2);
				currPunt = currPunt + punti;
				buf.setField(idx, 2, "" + currPunt);

				int currVinte = buf.getFieldByInt(3);
				currVinte = currVinte + vinte;
				buf.setField(idx, 3, "" + currVinte);

				int currPari = buf.getFieldByInt(4);
				currPari = currPari + pari;
				buf.setField(idx, 4, "" + currPari);

				int currPerse = buf.getFieldByInt(5);
				currPerse = currPerse + perse;
				buf.setField(idx, 5, "" + currPerse);

				int currGf = buf.getFieldByInt(6);
				currGf = currGf + gf;
				buf.setField(idx, 6, "" + currGf);

				int currGs = buf.getFieldByInt(7);
				currGs = currGs + gs;
				buf.setField(idx, 7, "" + currGs);

				int currDr = buf.getFieldByInt(8);
				currDr = currDr + dr;
				buf.setField(idx, 8, "" + currDr);

				int currTot_punti = buf.getFieldByInt(9);
				currTot_punti = currTot_punti + totPunti;
				buf.setField(idx, 9, "" + currTot_punti);

				int currTot_fm = buf.getFieldByInt(10);
				currTot_fm = currTot_fm + totFm;
				buf.setField(idx, 10, "" + currTot_fm);
			}

			if (golFuori > golCasa) {
				punti = 3;
				vinte = 1;
				pari = 0;
				perse = 0;
				totFm = 3;
			} else if (golFuori == golCasa) {
				punti = 1;
				vinte = 0;
				pari = 1;
				perse = 0;
				totFm = 1;
			} else if (golFuori < golCasa) {
				punti = 0;
				vinte = 0;
				pari = 0;
				perse = 1;
				totFm = 0;
			}
			gf = golFuori;
			gs = golCasa;
			dr = gf - gs;
			totPunti = totFuori;

			if (vinte == 1) {
				risPartita = 1;
			} else if (pari == 1) {
				risPartita = 0;
			} else if (perse == 1) {
				risPartita = 2;
			}
			query = " DELETE FROM fc_giornata_ris WHERE ID_GIORNATA =" + idGiornata + " AND ID_ATTORE=" + idAttoreFuori;
			jdbcTemplate.update(query);

			query = "INSERT INTO fc_giornata_ris (id_giornata,id_attore, vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES (" + idGiornata + ",";
			query += idAttoreFuori + "," + vinte + ",";
			query += pari + "," + perse + ",";
			query += gf + "," + gs + ",";
			query += punti + "," + totFm + "," + risPartita + ",0)";
			jdbcTemplate.update(query);

			idx = buf.findFirst("" + idAttoreFuori, 1, false);
			if (idx != -1) {
				int currPunt = buf.getFieldByInt(2);
				currPunt = currPunt + punti;
				buf.setField(idx, 2, "" + currPunt);

				int currVinte = buf.getFieldByInt(3);
				currVinte = currVinte + vinte;
				buf.setField(idx, 3, "" + currVinte);

				int currPari = buf.getFieldByInt(4);
				currPari = currPari + pari;
				buf.setField(idx, 4, "" + currPari);

				int currPerse = buf.getFieldByInt(5);
				currPerse = currPerse + perse;
				buf.setField(idx, 5, "" + currPerse);

				int currGf = buf.getFieldByInt(6);
				currGf = currGf + gf;
				buf.setField(idx, 6, "" + currGf);

				int currGs = buf.getFieldByInt(7);
				currGs = currGs + gs;
				buf.setField(idx, 7, "" + currGs);

				int currDr = buf.getFieldByInt(8);
				currDr = currDr + dr;
				buf.setField(idx, 8, "" + currDr);

				int currTot_punti = buf.getFieldByInt(9);
				currTot_punti = currTot_punti + totPunti;
				buf.setField(idx, 9, "" + currTot_punti);

				int currTot_fm = buf.getFieldByInt(10);
				currTot_fm = currTot_fm + totFm;
				buf.setField(idx, 10, "" + currTot_fm);
			}
		}

		// AGGIORNO classifica 1 vs tutti

		List<FcClassificaTotPt> lClasTotPt = classificaTotalePuntiRepository.findByFcCampionatoAndFcGiornataInfo(campionato, giornataInfo);

		for (FcAttore attore : l) {

			// Ottieni goal giornata
			int goalGiornata = 0;
			int sommaPtGiornata = 0;
			for (FcClassificaTotPt clasTot : lClasTotPt) {
				if (clasTot.getFcAttore().getIdAttore() == attore.getIdAttore()) {
					goalGiornata = clasTot.getGoal();
					break;
				}
			}

			for (FcClassificaTotPt clasTot : lClasTotPt) {
				if (clasTot.getFcAttore().getIdAttore() != attore.getIdAttore()) {
					int goalGiornataAvversario = clasTot.getGoal();

					if (goalGiornata > goalGiornataAvversario) {
						punti = 3;
					} else if (goalGiornata == goalGiornataAvversario) {
						punti = 1;
					} else if (goalGiornata < goalGiornataAvversario) {
						punti = 0;
					}

					sommaPtGiornata = sommaPtGiornata + punti;
				}
			}

			FcClassificaTotPt fcClassificaTotPt = classificaTotalePuntiRepository.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
			fcClassificaTotPt.setPtTvsT(sommaPtGiornata);
			classificaTotalePuntiRepository.save(fcClassificaTotPt);

			LOG.debug("----------------------------------------");
			LOG.debug("DESC_ATTORE               -----> " + attore.getDescAttore());
			LOG.debug("GOAL_GIORNATA 1VSTUTTI    -----> " + goalGiornata);
			LOG.debug("SOMMA PUNTI   1VSTUTTI    -----> " + sommaPtGiornata);
			LOG.debug("----------------------------------------");

		}

		// AGGIORNO CLASSIFICA SE NON SONO ARRIVATO AI QUARTI
		if (giornata_fc < 15) {

			String query = " DELETE FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();

			jdbcTemplate.update(query);

			Buffer newBuf = classificaFinale(buf, campionato);
			newBuf.sort(12);
			newBuf.moveFirst();
			for (int i = 0; i < buf.getRecordCount(); i++) {

				String sql = " SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA), SUM(pt_tvst) FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();
				sql += " AND ID_ATTORE =" + newBuf.getField(1);

				jdbcTemplate.query(sql, new ResultSetExtractor<String>(){

					@Override
					public String extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						if (rs.next()) {

							String tot_punti = rs.getString(1);
							String tot_punti_old = rs.getString(2);
							String tot_punti_rosa = rs.getString(3);
							String pt_tvst = rs.getString(4);

							String query = " INSERT INTO fc_classifica (ID_ATTORE,PUNTI,VINTE,PARI,PERSE,GF,GS,DR,";
							query += " TOT_PUNTI,TOT_FM,ID_CAMPIONATO,ID_POSIZ,ID_POSIZ_FINAL,TOT_PUNTI_OLD,TOT_PUNTI_ROSA,tot_punti_TvsT,FM_MERCATO) VALUES (" + newBuf.getField(1) + ",";
							query += newBuf.getField(2) + "," + newBuf.getField(3) + ",";
							query += newBuf.getField(4) + "," + newBuf.getField(5) + ",";
							query += newBuf.getField(6) + "," + newBuf.getField(7) + ",";
							query += newBuf.getField(8) + "," + tot_punti + ",";
							query += newBuf.getField(10) + "," + campionato.getIdCampionato() + ",";
							query += newBuf.getField(12) + "," + newBuf.getField(13) + ",";
							query += tot_punti_old + "," + tot_punti_rosa + "," + pt_tvst + ",0)";

							jdbcTemplate.update(query);

							return "1";
						}

						return null;
					}
				});

				newBuf.moveNext();
			}

			// INSERISCI GIORNATA QUARTI ANDATA-RITORNO
			int q_a = 0;
			int q_r = 0;
			if (giornata > 19) {
				q_a = 34;
				q_r = 35;
			} else {
				q_a = 15;
				q_r = 16;
			}

			jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + q_a);
			jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + q_r);

			String[][] quarti = new String[8][3];

			quarti[0][0] = "" + q_a;
			newBuf.setCurrentIndex(8);
			quarti[0][1] = newBuf.getField(1);
			newBuf.setCurrentIndex(1);
			quarti[0][2] = newBuf.getField(1);

			quarti[1][0] = "" + q_a;
			newBuf.setCurrentIndex(7);
			quarti[1][1] = newBuf.getField(1);
			newBuf.setCurrentIndex(2);
			quarti[1][2] = newBuf.getField(1);

			quarti[2][0] = "" + q_a;
			newBuf.setCurrentIndex(6);
			quarti[2][1] = newBuf.getField(1);
			newBuf.setCurrentIndex(3);
			quarti[2][2] = newBuf.getField(1);

			quarti[3][0] = "" + q_a;
			newBuf.setCurrentIndex(5);
			quarti[3][1] = newBuf.getField(1);
			newBuf.setCurrentIndex(4);
			quarti[3][2] = newBuf.getField(1);

			quarti[4][0] = "" + q_r;
			quarti[4][1] = quarti[0][2];
			quarti[4][2] = quarti[0][1];

			quarti[5][0] = "" + q_r;
			quarti[5][1] = quarti[1][2];
			quarti[5][2] = quarti[1][1];

			quarti[6][0] = "" + q_r;
			quarti[6][1] = quarti[2][2];
			quarti[6][2] = quarti[2][1];

			quarti[7][0] = "" + q_r;
			quarti[7][1] = quarti[3][2];
			quarti[7][2] = quarti[3][1];

			for (int i = 0; i < 8; i++) {

				query = "INSERT INTO fc_giornata (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata)  VALUES (" + quarti[i][0] + ",";
				query += quarti[i][1] + "," + quarti[i][2] + ",";
				query += "null" + "," + "null" + ",";
				query += "null" + "," + "null" + ",7)";

				jdbcTemplate.update(query);

				query = "";
			}

			LOG.debug("--------------------------");

		} else {

			// AGGIORNO CLASSIFICA PUNTEGGIO DOPO QUARTI

			for (int attore = 1; attore < 9; attore++) {

				String sql = "SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA) , SUM(pt_tvst), ID_ATTORE FROM fc_classifica_tot_pt ";
				sql += " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + attore;

				jdbcTemplate.query(sql, new ResultSetExtractor<String>(){

					@Override
					public String extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						if (rs.next()) {

							String tot_punti = rs.getString(1);
							String tot_punti_old = rs.getString(2);
							String tot_punti_rosa = rs.getString(3);
							String pt_tvst = rs.getString(4);
							String idAttore = rs.getString(5);

							String query = " UPDATE fc_classifica SET TOT_PUNTI=" + tot_punti + ",";
							query += " TOT_PUNTI_OLD=" + tot_punti_old + ",";
							query += " TOT_PUNTI_ROSA=" + tot_punti_rosa + ",";
							query += " tot_punti_TvsT=" + pt_tvst;
							query += " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + idAttore;
							jdbcTemplate.update(query);

							return "1";
						}

						return null;
					}
				});

			}
		}

		if (giornata_fc == 16) {
			// INSERISCI GIORNATA SEMIFINALI ANDATA-RITORNO
			int g_a = 0;
			int g_r = 0;
			if (giornata > 19) {
				g_a = 36;
				g_r = 37;
			} else {
				g_a = 17;
				g_r = 18;
			}

			jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + g_a);
			jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + g_r);

			int gg_a = g_a - 2;
			int gg_r = g_r - 2;
			Buffer calen = getCalendarioScontroAndataRitorno(gg_a, gg_r, false, campionato);

			String[][] semifinali = new String[8][4];

			calen.setCurrentIndex(1);
			semifinali[0][0] = "" + g_a;
			semifinali[0][1] = calen.getField(1);
			semifinali[0][2] = calen.getField(2);
			semifinali[0][3] = "5";

			calen.setCurrentIndex(2);
			semifinali[1][0] = "" + g_a;
			semifinali[1][1] = calen.getField(1);
			semifinali[1][2] = calen.getField(2);
			semifinali[1][3] = "5";

			calen.setCurrentIndex(3);
			semifinali[2][0] = "" + g_a;
			semifinali[2][1] = calen.getField(1);
			semifinali[2][2] = calen.getField(2);
			semifinali[2][3] = "6";

			calen.setCurrentIndex(4);
			semifinali[3][0] = "" + g_a;
			semifinali[3][1] = calen.getField(1);
			semifinali[3][2] = calen.getField(2);
			semifinali[3][3] = "6";

			semifinali[4][0] = "" + g_r;
			semifinali[4][1] = semifinali[0][2];
			semifinali[4][2] = semifinali[0][1];
			semifinali[4][3] = semifinali[0][3];

			semifinali[5][0] = "" + g_r;
			semifinali[5][1] = semifinali[1][2];
			semifinali[5][2] = semifinali[1][1];
			semifinali[5][3] = semifinali[1][3];

			semifinali[6][0] = "" + g_r;
			semifinali[6][1] = semifinali[2][2];
			semifinali[6][2] = semifinali[2][1];
			semifinali[6][3] = semifinali[2][3];

			semifinali[7][0] = "" + g_r;
			semifinali[7][1] = semifinali[3][2];
			semifinali[7][2] = semifinali[3][1];
			semifinali[7][3] = semifinali[3][3];

			for (int i = 0; i < semifinali.length; i++) {

				String query = "INSERT INTO fc_giornata  (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata) VALUES (" + semifinali[i][0] + ",";
				query += semifinali[i][1] + "," + semifinali[i][2] + ",";
				query += "null" + "," + "null" + ",";
				query += "null" + "," + "null" + ",";
				query += semifinali[i][3] + ")";

				jdbcTemplate.update(query);

				query = "";
			}
			LOG.debug("--------------------------");
		}

		if (giornata_fc == 18) {

			// INSERISCI GIORNATE FINALI
			int g_f = 0;
			if (giornata > 19) {
				g_f = 38;
			} else {
				g_f = 19;
			}

			jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + g_f);

			int gg_a = g_f - 2;
			int gg_r = g_f - 1;

			// CALCOLO ERRATO RIVEDERE
			Buffer calen = getCalendarioScontroAndataRitorno(gg_a, gg_r, true, campionato);

			String[][] finali = new String[4][4];

			calen.setCurrentIndex(1);
			finali[0][0] = "" + g_f;
			finali[0][1] = calen.getField(1);
			finali[0][2] = calen.getField(2);
			finali[0][3] = "1";

			calen.setCurrentIndex(2);
			finali[1][0] = "" + g_f;
			finali[1][1] = calen.getField(1);
			finali[1][2] = calen.getField(2);
			finali[1][3] = "2";

			calen.setCurrentIndex(3);
			finali[2][0] = "" + g_f;
			finali[2][1] = calen.getField(1);
			finali[2][2] = calen.getField(2);
			finali[2][3] = "3";

			calen.setCurrentIndex(4);
			finali[3][0] = "" + g_f;
			finali[3][1] = calen.getField(1);
			finali[3][2] = calen.getField(2);
			finali[3][3] = "4";

			for (int i = 0; i < finali.length; i++) {

				String query = "INSERT INTO fc_giornata (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata) VALUES (" + finali[i][0] + ",";
				query += finali[i][1] + "," + finali[i][2] + ",";
				query += "null" + "," + "null" + ",";
				query += "null" + "," + "null" + ",";
				query += finali[i][3] + ")";

				jdbcTemplate.update(query);

				query = "";
			}
		}

		if (giornata_fc == 19) {
			int g_f = 0;
			if (giornata > 19) {
				g_f = 38;
			} else {
				g_f = 19;
			}
			insertFinalResult(g_f, campionato);
		}

		LOG.info("END algoritmo");

	}

	private HashMap<String, String> effettuaCambio(Integer giornata,
			int ID_ATTORE, ArrayList<String> listaIdGiocatoriCambiati,
			List<FcGiornataDett> lGiocatori, int ordinamento,
			String ruoloGiocatore, int somma) throws Exception {

		HashMap<String, String> mapResult = new HashMap<String, String>();
		boolean bChange = false;
		for (FcGiornataDett gd : lGiocatori) {

			if (gd.getOrdinamento() == ordinamento && gd.getVoto() > 0) {

				FcGiocatore giocatoreDaCambiare = gd.getFcGiocatore();

				for (FcGiornataDett findGiocatore : lGiocatori) {

					FcGiocatore giocatoreDaSostituire = findGiocatore.getFcGiocatore();

					if (ordinamento == 14 || ordinamento == 16 || ordinamento == 18) {

						String idGiocatore = "" + findGiocatore.getId().getIdGiocatore();
						if (listaIdGiocatoriCambiati.indexOf(idGiocatore) != -1) {
							continue;
						}
					}

					if (findGiocatore.getOrdinamento() < 12 && ruoloGiocatore.equals(giocatoreDaSostituire.getFcRuolo().getIdRuolo()) && findGiocatore.getVoto() == 0 && gd.getVoto() > findGiocatore.getVoto()) {

						somma = somma - findGiocatore.getVoto().intValue();

						String query = "UPDATE fc_giornata_dett SET ";
						query += " FLAG_ATTIVO='N'";
						query += " WHERE ID_GIOCATORE=" + findGiocatore.getFcGiocatore().getIdGiocatore();
						query += " AND ID_GIORNATA=" + giornata;
						query += " AND ID_ATTORE=" + ID_ATTORE;

						jdbcTemplate.update(query);

						somma = somma + gd.getVoto().intValue();

						query = "UPDATE fc_giornata_dett SET ";
						query += " FLAG_ATTIVO='S'";
						query += " WHERE ID_GIOCATORE=" + giocatoreDaCambiare.getIdGiocatore();
						query += " AND ID_GIORNATA=" + giornata;
						query += " AND ID_ATTORE=" + ID_ATTORE;

						jdbcTemplate.update(query);

						LOG.info("Cambio Giocatore FUORI " + findGiocatore.getFcGiocatore().getCognGiocatore() + " DENTRO " + gd.getFcGiocatore().getCognGiocatore());

						mapResult.put("SOMMA", "" + somma);
						mapResult.put("ID_GIOCATORE", "" + gd.getFcGiocatore().getIdGiocatore());
						bChange = true;
						break;
					}
				}
			}

			if (bChange) {
				break;
			}
		}

		return mapResult;
	}

	private boolean validateCambioRiserva2(List<FcGiornataDett> lGiocatori,
			String ruolo) throws Exception {

		String ID_RUOLO = "";
		int VOTO_GIOCATORE = 0;
		int ORDINAMENTO = 0;
		boolean flagTitolare = false;
		boolean flagRiserva = false;

		for (FcGiornataDett giornataDett : lGiocatori) {

			ID_RUOLO = giornataDett.getFcGiocatore().getFcRuolo().getIdRuolo();
			VOTO_GIOCATORE = giornataDett.getFcPagelle().getVotoGiocatore();
			ORDINAMENTO = giornataDett.getOrdinamento();

			if (ruolo.equals(ID_RUOLO)) {

				if (ORDINAMENTO < 12) {
					// esiste un titolare che non ha preso voto ?
					if (VOTO_GIOCATORE == 0) {
						flagTitolare = true;
					}
				}

				if (ID_RUOLO.equals("D")) {

					if (ORDINAMENTO == 13) {

						// esiste la riserva 1 che non ha preso voto ?
						if (VOTO_GIOCATORE == 0) {
							flagRiserva = true;
						}
					} else if (ORDINAMENTO == 14) {
						if (VOTO_GIOCATORE == 0) {
							flagRiserva = false;
						}
					}

				} else if (ID_RUOLO.equals("C")) {

					if (ORDINAMENTO == 15) {

						if (VOTO_GIOCATORE == 0) {
							flagRiserva = true;
						}
					} else if (ORDINAMENTO == 16) {
						if (VOTO_GIOCATORE == 0) {
							flagRiserva = false;
						}
					}

				} else if (ID_RUOLO.equals("A")) {

					if (ORDINAMENTO == 17) {

						if (VOTO_GIOCATORE == 0) {
							flagRiserva = true;
						}
					} else if (ORDINAMENTO == 18) {
						if (VOTO_GIOCATORE == 0) {
							flagRiserva = false;
						}
					}
				}
			}

		} // END IF

		if (flagTitolare && flagRiserva) {
			return true;
		}

		return false;

	}

	private int getTotGoal(int somma) {

		int increment = 400;
		int start1 = 6600;
		int end1 = 6999;
		int start2 = start1 + increment;
		int end2 = end1 + increment;
		int start3 = start2 + increment;
		int end3 = end2 + increment;
		int start4 = start3 + increment;
		int end4 = end3 + increment;
		int start5 = start4 + increment;
		int end5 = end4 + increment;
		int start6 = start5 + increment;
		int end6 = end5 + increment;
		int start7 = start6 + increment;
		int end7 = end6 + increment;
		int start8 = start7 + increment;
		int end8 = end7 + increment;
		int start9 = start8 + increment;
		int end9 = end8 + increment;
		int start10 = start9 + increment;
		int end10 = end9 + increment;
		int start11 = start10 + increment;
		int end11 = end10 + increment;
		int start12 = start11 + increment;
		int end12 = end11 + increment;
		int start13 = start12 + increment;
		int end13 = end12 + increment;
		int start14 = start12 + increment;
		int end14 = end12 + increment;

		int goal_casa = 0;
		if (somma >= start1 && somma <= end1) {
			goal_casa = 1;
		} else if (somma >= start2 && somma <= end2) {
			goal_casa = 2;
		} else if (somma >= start3 && somma <= end3) {
			goal_casa = 3;
		} else if (somma >= start4 && somma <= end4) {
			goal_casa = 4;
		} else if (somma >= start5 && somma <= end5) {
			goal_casa = 5;
		} else if (somma >= start6 && somma <= end6) {
			goal_casa = 6;
		} else if (somma >= start7 && somma <= end7) {
			goal_casa = 7;
		} else if (somma >= start8 && somma <= end8) {
			goal_casa = 8;
		} else if (somma >= start9 && somma <= end9) {
			goal_casa = 9;
		} else if (somma >= start10 && somma <= end10) {
			goal_casa = 10;
		} else if (somma >= start11 && somma <= end11) {
			goal_casa = 11;
		} else if (somma >= start12 && somma <= end12) {
			goal_casa = 12;
		} else if (somma >= start13 && somma <= end13) {
			goal_casa = 13;
		} else if (somma >= start14 && somma <= end14) {
			goal_casa = 14;
		}
		return goal_casa;

	}

	/*
	 * CALCOLO LE POSIZIONI FINALI IN CASO DI PUNTEGGIO PARI
	 * 
	 * 1 ) Punti negli scontri diretti 2 ) Differenza reti negli scontri diretti
	 * 3 ) Differenza reti generali 4 ) Gol realizzati totali 5 ) Punteggio
	 * totale ottenuto
	 */
	private Buffer classificaFinale(Buffer buf, FcCampionato campionato) {

		/*
		 * Buffer buf = new Buffer();
		 * buf.addNew("@13@231@310@41@53@644@723@821@910220@1024@111@121");
		 * buf.addNew("@12@223@37@42@55@633@728@85@99995@1017@111@122");
		 * buf.addNew("@14@221@37@40@57@641@742@8-1@910125@1016@111@123");
		 * buf.addNew("@11@219@36@41@57@633@728@85@99980@1014@111@124");
		 * buf.addNew("@17@218@36@40@58@623@733@8-10@99365@1015@111@125");
		 * buf.addNew("@16@218@35@43@56@633@738@8-5@99970@1015@111@126");
		 * buf.addNew("@15@215@33@46@55@627@733@8-6@99815@1012@111@127");
		 * buf.addNew("@18@215@34@43@57@623@732@8-9@99595@1012@111@128");
		 */
		buf.sort(2);
		buf.setCurrentIndex(1);

		int posizione = 8;
		ArrayList<String> listAttoriProcessati = new ArrayList<String>(); 
		
		String appoPt = buf.getField(2);
		Buffer appoBuf = new Buffer();
		appoBuf.addNew(buf.getItem(1));

		for (int i = 2; i <= buf.getRecordCount(); i++) {
			buf.setCurrentIndex(i);
			
			if (appoPt.equals(buf.getField(2))) {
				appoBuf.addNew(buf.getItem(i));
			} else {

				try {
					Buffer finale = calcolaPosizione(appoBuf, posizione, campionato);
					// LOG.debug("SIZE "+finale.getRecordCount());
					for (int r = 1; r <= finale.getRecordCount(); r++) {
						finale.setCurrentIndex(r);
						String idAttore = finale.getField(1);
						
						if (listAttoriProcessati.indexOf(idAttore) != -1) {
							continue;
						}
						
						int row = buf.findFirst(idAttore, 1, true);
						if (row != -1) {
							// LOG.debug("posizione "+finale.getField(2));
							buf.setField(row, 12, finale.getField(2));
							listAttoriProcessati.add(idAttore);
						}
						
						posizione--;
					}

				} catch (Exception ex) {
				}

				// ROTTURA DI CODICE
				appoBuf = new Buffer();
				buf.setCurrentIndex(i);
				appoBuf.addNew(buf.getItem(i));

				appoPt = buf.getField(2);
			}
		}
		
		
		try {
			Buffer finale = calcolaPosizione(appoBuf, posizione, campionato);
			// LOG.debug("SIZE "+finale.getRecordCount());
			for (int r = 1; r <= finale.getRecordCount(); r++) {
				finale.setCurrentIndex(r);
				String idAttore = finale.getField(1);
				
				if (listAttoriProcessati.indexOf(idAttore) != -1) {
					continue;
				}
				
				int row = buf.findFirst(finale.getField(1), 1, true);
				if (row != -1) {
					// LOG.debug("posizione "+finale.getField(2));
					buf.setField(row, 12, finale.getField(2));
					listAttoriProcessati.add(idAttore);
				}
				posizione--;
			}

		} catch (Exception ex) {
		}
		return buf;
	}

	private Buffer getBufferScontro(int posizione, String att1,
			String att2, int p1, int p2) {
		
		Buffer position = new Buffer();
		if (p1 > p2) {
			position.addNew("@1" + att2 + "@2" + posizione);
			posizione--;
			position.addNew("@1" + att1 + "@2" + posizione);
			
			return position;
		} else if (p1 < p2) {
			position.addNew("@1" + att1 + "@2" + posizione);
			posizione--;
			position.addNew("@1" + att2 + "@2" + posizione);
			
			return position;
		}
		return null;
	}

	private Buffer calcolaPosizione(Buffer buffer, int posizione,
			FcCampionato campionato) throws Exception {

		Buffer position = new Buffer();
		int righe = buffer.getRecordCount();
		// LOG.debug("righe "+righe);
		if (righe == 1) {

			String attore = buffer.getField(1);
			position.addNew("@1" + attore + "@2" + posizione);

		} else if (righe == 2) {

			buffer.setCurrentIndex(1);
			String att1 = buffer.getField(1);
			String diffRetiGeneraliAtt1 = buffer.getField(8);
			String goalTotAtt1 = buffer.getField(6);
			String ptTotAtt1 = buffer.getField(9);
			buffer.setCurrentIndex(2);
			String att2 = buffer.getField(1);
			String diffRetiGeneraliAtt2 = buffer.getField(8);
			String goalTotAtt2 = buffer.getField(6);
			String ptTotAtt2 = buffer.getField(9);

			ArrayList<String> giornate = getGiornateGiocate(campionato, att1, buffer);

			String[] risAtt1 = getInfoAttore(att1, giornate);
			String[] risAtt2 = getInfoAttore(att2, giornate);

			int p1 = Integer.parseInt(risAtt1[0]);
			int p2 = Integer.parseInt(risAtt2[0]);
			// 1) Punti negli scontri diretti
			LOG.info("1) Punti negli scontri diretti ");
			position = getBufferScontro( posizione, att1, att2, p1, p2);
			if (position == null) {
				// 2) Differenza reti negli scontri diretti
				LOG.info("2) Differenza reti negli scontri diretti ");
				int reti1 = Integer.parseInt(risAtt1[1]);
				int reti2 = Integer.parseInt(risAtt2[1]);
				position = getBufferScontro( posizione, att1, att2, reti1, reti2);
				if (position == null) {
					// 3) Differenza reti generali
					LOG.info("3) Differenza reti generali");
					int retiGen1 = Integer.parseInt(diffRetiGeneraliAtt1);
					int retiGen2 = Integer.parseInt(diffRetiGeneraliAtt2);
					position = getBufferScontro( posizione, att1, att2, retiGen1, retiGen2);
					if (position == null) {
						// 4) Gol realizzati totali
						LOG.info("4) Gol realizzati totali");
						int goalTot1 = Integer.parseInt(goalTotAtt1);
						int goalTot2 = Integer.parseInt(goalTotAtt2);
						position = getBufferScontro( posizione, att1, att2, goalTot1, goalTot2);
						if (position == null) {
							// 5) Punteggio totale ottenuto
							LOG.info("5) Punteggio totale ottenuto");
							int ptTot1 = Integer.parseInt(ptTotAtt1);
							int ptTot2 = Integer.parseInt(ptTotAtt2);
							position = getBufferScontro( posizione, att1, att2, ptTot1, ptTot2);
							if (position == null) {
								// SPARATE
								LOG.info("SPARATE!!!!!!!!!!!!!!!!1");
							}
						}
					}
				}
			}

		} else {

			LOG.debug("@ATTORI CON PARITA PUNTI " + buffer.getRecordCount());

			Buffer mapInfo = new Buffer();

			// PER OGNI ATTORE OTTENGO ULTERIORI INFO PUNTO 1 e PUNTO 2 NEI
			// SCONTRI DIRETTI
			for (int r = 1; r <= buffer.getRecordCount(); r++) {
				buffer.setCurrentIndex(r);
				String attore = buffer.getField(1);

				ArrayList<String> giornate = getGiornateGiocate(campionato, attore, buffer);
				String[] ris = getInfoAttore(attore, giornate);

				// 1) Punti negli scontri diretti
				String ptScontriDiretti = ris[0];
				// 2) Differenza reti negli scontri diretti
				String diffRetiScontriDiretti = ris[0];
				// 3) Differenza reti generali
				String diffRetiGenerali = buffer.getField(8);
				// 4) Gol realizzati totali
				String goalTot = buffer.getField(6);
				// 5) Punteggio totale ottenuto
				String ptTot = buffer.getField(9);

				mapInfo.addNew("@1" + attore + "@2" + ptScontriDiretti + "@3" + diffRetiScontriDiretti + "@4" + diffRetiGenerali + "@5" + goalTot + "@6" + ptTot);

			}

			// 1) Punti negli scontri diretti
			LOG.info(" 1) Punti negli scontri diretti ");
			mapInfo.sort(2);
			for (int r = 1; r <= mapInfo.getRecordCount(); r++) {
				mapInfo.setCurrentIndex(r);
				String attore = mapInfo.getField(1);
				position.addNew("@1" + attore + "@2" + posizione);
				posizione--;
			}
		}
		// else {
		//
		// LOG.debug("@ATTORI CON PARITA PUNTI " + buffer.getRecordCount());
		// buffer.sort(9);
		// for (int r = 1; r <= buffer.getRecordCount(); r++) {
		// buffer.setCurrentIndex(r);
		// String attore = buffer.getField(1);
		// position.addNew("@1" + attore + "@2" + posizione);
		// posizione--;
		// }
		// }
		return position;
	}

	private ArrayList<String> getGiornateGiocate(FcCampionato campionato,
			String attore, Buffer buffer) {

		String start = campionato.getStart().toString();
		String end = campionato.getEnd().toString();

		ArrayList<String> giornate = new ArrayList<String>();

		buffer.setCurrentIndex(2);
		String att2 = buffer.getField(1);

		// SELEZIONO GIORNATE GIOCATE
		String sql = " SELECT ID_GIORNATA FROM fc_giornata WHERE ID_GIORNATA >=" + start;
		sql += " AND ID_GIORNATA <=" + end;
		sql += " AND ( ID_ATTORE_CASA = " + attore;
		sql += "       AND ID_ATTORE_FUORI = " + att2;
		sql += "       OR ID_ATTORE_CASA = " + att2;
		sql += "          AND ID_ATTORE_FUORI = " + attore + " ) ";

		int righe = buffer.getRecordCount();
		if (righe > 2) {
			for (int r = 1; r <= buffer.getRecordCount(); r++) {
				buffer.setCurrentIndex(r);
				String attNext = buffer.getField(1);
				if (attore.equals(attNext)) {
					continue;
				}
				sql += " OR ( ID_ATTORE_CASA = " + attore;
				sql += "       AND ID_ATTORE_FUORI = " + attNext;
				sql += "       OR ID_ATTORE_CASA = " + attNext;
				sql += "          AND ID_ATTORE_FUORI = " + attore + " ) ";
			}
		}

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				while (rs.next()) {
					giornate.add(rs.getString(1));
				}
				return null;
			}
		});

		return giornate;
	}

	private String[] getInfoAttore(String idAttore,
			ArrayList<String> giornate) {

		String ggIn = "";
		for (String g : giornate) {
			ggIn += g + ",";
		}
		if (ggIn.length() != -1) {
			ggIn = ggIn.substring(0, ggIn.length() - 1);
		}

		String[] ris = new String[2];

		// SELEZIONO PUNTI SCONTRO, DIFFERENZA RETI SCONTRI
		String sql = " SELECT SUM(PUNTI), SUM(GF)-SUM(GS) AS DIFF ";
		sql += " FROM fc_giornata_ris ";
		sql += " WHERE ID_GIORNATA IN (" + ggIn + ") ";
		sql += " AND ID_ATTORE= " + idAttore;

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){

			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				if (rs.next()) {

					String punti = rs.getString(1);
					String dif = rs.getString(2);

					ris[0] = punti;
					ris[1] = dif;

					return "1";
				}

				return null;
			}
		});

		return ris;
	}

	// CREA CALENDARIO X SEMIFINALI
	public Buffer getCalendarioScontroAndataRitorno(int g_a, int g_r,
			boolean calFinale, FcCampionato campionato) throws Exception {

		String sql = " SELECT ID_ATTORE_CASA,ID_ATTORE_FUORI,GOL_CASA,GOL_FUORI,TOT_CASA,TOT_FUORI, ";
		sql += " (SELECT ID_POSIZ FROM fc_classifica WHERE ID_ATTORE = ID_ATTORE_FUORI and id_campionato=" + campionato.getIdCampionato() + ") ID_POSIZ ";
		sql += " FROM fc_giornata WHERE ID_GIORNATA=" + g_a + " OR ID_GIORNATA=" + g_r;
		sql += " ORDER BY ID_GIORNATA,ID_TIPO_GIORNATA,ID_POSIZ";

		Buffer buf = new Buffer();

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				int attore_casa = 0;
				int attore_fuori = 0;
				int gol_casa = 0;
				int gol_fuori = 0;
				int tot_casa = 0;
				int tot_fuori = 0;
				int id_posizione = 0;
				while (rs.next()) {

					attore_casa = rs.getInt(1);
					attore_fuori = rs.getInt(2);
					gol_casa = rs.getInt(3);
					gol_fuori = rs.getInt(4);
					tot_casa = rs.getInt(5);
					tot_fuori = rs.getInt(6);
					id_posizione = rs.getInt(7);

					buf.addNew("@1" + attore_casa + "@2" + attore_fuori + "@3" + gol_casa + "@4" + gol_fuori + "@5" + tot_casa + "@6" + tot_fuori + "@7" + id_posizione);

				}

				return "1";
			}
		});

		Buffer bufAppo = new Buffer();

		sql = " SELECT ID_ATTORE,ID_POSIZ FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				int att = 0;
				int id_posiz = 0;
				int goal_casa = 0;
				int goal_fuori = 0;
				int somma_goal = 0;
				int tot_casa = 0;
				int tot_fuori = 0;
				int somma_tot = 0;

				while (rs.next()) {

					att = rs.getInt(1);
					id_posiz = rs.getInt(2);

					int idx = buf.findFirst("" + att, 1, false);
					if (idx != -1) {
						goal_casa = buf.getFieldByInt(3);
						tot_casa = buf.getFieldByInt(5);
					}
					idx = buf.findFirst("" + att, 2, false);
					if (idx != -1) {
						goal_fuori = buf.getFieldByInt(4);
						tot_fuori = buf.getFieldByInt(6);
					}
					somma_goal = goal_casa + goal_fuori;
					somma_tot = tot_casa + tot_fuori;

					bufAppo.addNew("@1" + att + "@2" + goal_casa + "@3" + goal_fuori + "@4" + somma_goal + "@5" + tot_casa + "@6" + tot_fuori + "@7" + somma_tot + "@8" + id_posiz);

				}

				return "1";
			}
		});

		Buffer bufCalendarSemi = new Buffer();
		bufCalendarSemi.addNew("@10@20");
		bufCalendarSemi.addNew("@10@20");
		bufCalendarSemi.addNew("@10@20");
		bufCalendarSemi.addNew("@10@20");

		Buffer bufWin = new Buffer();
		Buffer bufLose = new Buffer();

		int att_1 = 0;
		int att_1_somma_goal = 0;
		// int att_1_goal_fuori = 0;
		int att_1_somma_tot = 0;
		int att_1_id_posiz = 0;

		int att_2 = 0;
		int att_2_somma_goal = 0;
		// int att_2_goal_fuori = 0;
		int att_2_somma_tot = 0;
		int att_2_id_posiz = 0;

		buf.moveFirst();
		int id_win = 0;
		int id_lose = 0;
		for (int r = 1; r < 5; r++) {

			att_1 = buf.getFieldByInt(1);
			att_2 = buf.getFieldByInt(2);

			int idx = bufAppo.findFirst("" + att_1, 1, false);
			if (idx != -1) {
				// att_1_goal_fuori = bufAppo.getFieldByInt(3);
				att_1_somma_goal = bufAppo.getFieldByInt(4);
				att_1_somma_tot = bufAppo.getFieldByInt(7);
				att_1_id_posiz = bufAppo.getFieldByInt(8);
			}
			idx = bufAppo.findFirst("" + att_2, 1, false);
			if (idx != -1) {
				// att_2_goal_fuori = bufAppo.getFieldByInt(3);
				att_2_somma_goal = bufAppo.getFieldByInt(4);
				att_2_somma_tot = bufAppo.getFieldByInt(7);
				att_2_id_posiz = bufAppo.getFieldByInt(8);
			}

			if (att_1_somma_goal == att_2_somma_goal) {
				// UGUALE
				if (att_1_somma_tot > att_2_somma_tot) {
					id_win = att_1;
					id_lose = att_2;
				} else if (att_2_somma_tot > att_1_somma_tot) {
					id_win = att_2;
					id_lose = att_1;
				} else {
					// RIVEDERE SPAREGGIO
					id_win = att_2;
					id_lose = att_1;
				}

			} else if (att_1_somma_goal > att_2_somma_goal) {
				// MAGGIORE
				id_win = att_1;
				id_lose = att_2;

			} else if (att_1_somma_goal < att_2_somma_goal) {
				// MINORE
				id_win = att_2;
				id_lose = att_1;
			}

			int pos_clas = 0;
			if (id_win == att_1) {
				pos_clas = att_1_id_posiz;
			} else {
				pos_clas = att_2_id_posiz;
			}
			bufWin.addNew("@1" + id_win + "@2" + pos_clas);

			if (id_lose == att_1) {
				pos_clas = att_1_id_posiz;
			} else {
				pos_clas = att_2_id_posiz;
			}
			bufLose.addNew("@1" + id_lose + "@2" + pos_clas);

			if (r == 1) {
				bufCalendarSemi.setField(1, 2, "" + id_win);
				bufCalendarSemi.setField(4, 2, "" + id_lose);
			}

			if (r == 2) {
				bufCalendarSemi.setField(2, 2, "" + id_win);
				bufCalendarSemi.setField(3, 2, "" + id_lose);
			}

			if (r == 3) {
				bufCalendarSemi.setField(2, 1, "" + id_win);
				bufCalendarSemi.setField(3, 1, "" + id_lose);
			}

			if (r == 4) {
				bufCalendarSemi.setField(1, 1, "" + id_win);
				bufCalendarSemi.setField(4, 1, "" + id_lose);
			}

			buf.moveNext();
		}

		printBuffer(bufCalendarSemi);
		Buffer bufCalendar = new Buffer();

		if (!calFinale) {

			// CONTROLLO
			for (int r = 1; r < 5; r++) {
				bufCalendarSemi.setCurrentIndex(r);

				att_1 = bufCalendarSemi.getFieldByInt(1);
				att_2 = bufCalendarSemi.getFieldByInt(2);
				int idx = bufAppo.findFirst("" + att_1, 1, false);
				if (idx != -1) {
					att_1_id_posiz = bufAppo.getFieldByInt(8);
				}
				idx = bufAppo.findFirst("" + att_2, 1, false);
				if (idx != -1) {
					att_2_id_posiz = bufAppo.getFieldByInt(8);
				}

				if (att_1_id_posiz < att_2_id_posiz) {
					bufCalendar.addNew("@1" + att_2 + "@2" + att_1);
				} else {
					bufCalendar.addNew("@1" + att_1 + "@2" + att_2);
				}

			}
			printBuffer(bufCalendar);
		} else {

			// OK
			// LOG.info("WIN");
			printBuffer(bufWin);

			// LOG.info("LOSE");
			printBuffer(bufLose);

			// FINALISSIMA 1/2
			bufWin.setCurrentIndex(1);
			String a1 = bufWin.getField(1);
			int a1_pos_clas = bufWin.getFieldByInt(2);
			bufWin.setCurrentIndex(2);
			String a2 = bufWin.getField(1);
			int a2_pos_clas = bufWin.getFieldByInt(2);
			if (a1_pos_clas < a2_pos_clas) {
				bufCalendar.addNew("@1" + a2 + "@2" + a1);
			} else {
				bufCalendar.addNew("@1" + a1 + "@2" + a2);
			}

			// FINALISSIMA 3/4
			bufLose.setCurrentIndex(1);
			a1 = bufLose.getField(1);
			a1_pos_clas = bufLose.getFieldByInt(2);
			bufLose.setCurrentIndex(2);
			a2 = bufLose.getField(1);
			a2_pos_clas = bufLose.getFieldByInt(2);
			if (a1_pos_clas < a2_pos_clas) {
				bufCalendar.addNew("@1" + a2 + "@2" + a1);
			} else {
				bufCalendar.addNew("@1" + a1 + "@2" + a2);
			}

			// FINALISSIMA 5/6
			bufWin.setCurrentIndex(3);
			a1 = bufWin.getField(1);
			a1_pos_clas = bufWin.getFieldByInt(2);
			bufWin.setCurrentIndex(4);
			a2 = bufWin.getField(1);
			a2_pos_clas = bufWin.getFieldByInt(2);
			if (a1_pos_clas < a2_pos_clas) {
				bufCalendar.addNew("@1" + a2 + "@2" + a1);
			} else {
				bufCalendar.addNew("@1" + a1 + "@2" + a2);
			}

			// FINALISSIMA 7/8
			bufLose.setCurrentIndex(3);
			a1 = bufLose.getField(1);
			a1_pos_clas = bufLose.getFieldByInt(2);
			bufLose.setCurrentIndex(4);
			a2 = bufLose.getField(1);
			a2_pos_clas = bufLose.getFieldByInt(2);
			if (a1_pos_clas < a2_pos_clas) {
				bufCalendar.addNew("@1" + a2 + "@2" + a1);
			} else {
				bufCalendar.addNew("@1" + a1 + "@2" + a2);
			}

			// LOG.info("CALENDAR");
			printBuffer(bufCalendar);

		}

		return bufCalendar;
	}

	// FINALEEEEEEEEEEEEEE
	public void insertFinalResult(int g_f, FcCampionato campionato)
			throws Exception {

		String sql = " SELECT ID_ATTORE_CASA,ID_ATTORE_FUORI,GOL_CASA,GOL_FUORI,ID_TIPO_GIORNATA,TOT_CASA,TOT_FUORI  FROM fc_giornata " + " WHERE ID_GIORNATA=" + g_f;

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {

				int attore_casa = 0;
				int attore_fuori = 0;
				int gol_casa = 0;
				int gol_fuori = 0;
				int id_tipo_gg = 0;
				int id_win = 0;
				int id_lose = 0;
				int id_posiz_win = 0;
				int id_posiz_lose = 0;
				int tot_casa = 0;
				int tot_fuori = 0;

				while (rs.next()) {

					attore_casa = rs.getInt(1);

					attore_fuori = rs.getInt(2);

					gol_casa = rs.getInt(3);

					gol_fuori = rs.getInt(4);

					id_tipo_gg = rs.getInt(5);

					tot_casa = rs.getInt(6);

					tot_fuori = rs.getInt(7);

					if (id_tipo_gg == 1) {
						id_posiz_win = 1;
						id_posiz_lose = 2;
					} else if (id_tipo_gg == 2) {
						id_posiz_win = 3;
						id_posiz_lose = 4;
					} else if (id_tipo_gg == 3) {
						id_posiz_win = 5;
						id_posiz_lose = 6;
					} else if (id_tipo_gg == 4) {
						id_posiz_win = 7;
						id_posiz_lose = 8;
					}

					if (gol_casa > gol_fuori) {
						id_win = attore_casa;
						id_lose = attore_fuori;
					} else if (gol_fuori > gol_casa) {
						id_win = attore_fuori;
						id_lose = attore_casa;
					} else if (gol_fuori == gol_casa) {
						// SPARREGGGGGIOOOOOOOOOO
						// LOG.info("SPAREGGIO " + attore_fuori + " " +
						// attore_casa);

						if (tot_casa > tot_fuori) {
							id_win = attore_casa;
							id_lose = attore_fuori;
						} else if (tot_fuori > tot_casa) {
							id_win = attore_fuori;
							id_lose = attore_casa;
						} else if (tot_fuori == tot_casa) {
							// SPARREGGGGGIOOOOOOOOOO
							// LOG.info("SPAREGGIO " + attore_fuori + " " +
							// attore_casa);
						}
					}

					String query = " UPDATE fc_classifica SET " + " ID_POSIZ_FINAL=" + id_posiz_win + " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + id_win;
					jdbcTemplate.update(query);

					query = " UPDATE fc_classifica SET " + " ID_POSIZ_FINAL=" + id_posiz_lose + " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + id_lose;
					jdbcTemplate.update(query);

				}

				return "1";
			}
		});

	}

	public Buffer getAttoriBonusOttaviAndata(String idCampionato)
			throws Exception {

		String sql = " SELECT ID_ATTORE FROM fc_classifica WHERE ID_CAMPIONATO=" + idCampionato + " AND ID_POSIZ<5 ORDER BY ID_POSIZ";

		Buffer buf = new Buffer();

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {

				String bonus = "";
				String bonus2 = "";
				int i = 0;
				while (rs.next()) {

					if (i == 0) {
						bonus = "800";
						bonus2 = "8";
					} else if (i == 1) {
						bonus = "600";
						bonus2 = "6";
					} else if (i == 2) {
						bonus = "400";
						bonus2 = "4";
					} else if (i == 3) {
						bonus = "200";
						bonus2 = "2";
					}
					buf.addNew("@1" + rs.getString(1) + "@2" + bonus + "@3" + bonus2);
					i++;
				}

				return "1";
			}
		});

		return buf;
	}

	public Buffer getAttoriBonusSemifinaliAndata(String idCampionato)
			throws Exception {

		String sql = " SELECT ID_ATTORE,VINTE FROM fc_classifica WHERE ID_CAMPIONATO=" + idCampionato;

		Buffer buf = new Buffer();

		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs)
					throws SQLException, DataAccessException {

				int bonus = 0;
				int bonus2 = 0;
				while (rs.next()) {

					bonus2 = rs.getInt(2);
					bonus = bonus2 * Costants.DIVISORE_100;
					System.out.println("@1" + rs.getString(1) + "@2" + bonus + "@3" + bonus2);
					buf.addNew("@1" + rs.getString(1) + "@2" + bonus + "@3" + bonus2);
				}

				return "1";
			}
		});

		return buf;

	}

	private void printBuffer(Buffer tmp) {

		for (int j = 1; j <= tmp.getRecordCount(); j++) {
			tmp.setCurrentIndex(j);
			// LOG.debug(tmp.getField(1) + " - " + tmp.getField(2));
		}
	}

	private int buildFantaMedia(FcPagelle pagelle) {

		int votoGiocatore = pagelle.getVotoGiocatore();
		if (votoGiocatore != 0) {

			int goalRealizzato = pagelle.getGoalRealizzato();
			int goalSubito = pagelle.getGoalSubito();
			int ammonizione = pagelle.getAmmonizione();
			int espulso = pagelle.getEspulsione();
			int rigoreFallito = pagelle.getRigoreFallito();
			int rigoreParato = pagelle.getRigoreParato();
			int autorete = pagelle.getAutorete();
			int assist = pagelle.getAssist();
			// int gdv = pagelle.getGdv();
			int G = 0;
			int CS = 0;
			int TS = 0;
			if (pagelle.getG() != null) {
				G = pagelle.getG().intValue();
			}
			if (pagelle.getCs() != null) {
				CS = pagelle.getCs().intValue();
			}
			if (pagelle.getTs() != null) {
				TS = pagelle.getTs().intValue();
			}
			if (goalRealizzato != 0) {
				votoGiocatore = votoGiocatore + (goalRealizzato * 3 * Costants.DIVISORE_100);
			}
			if (goalSubito != 0) {
				votoGiocatore = votoGiocatore - (goalSubito * 1 * Costants.DIVISORE_100);
			}
			if (ammonizione != 0) {
				votoGiocatore = votoGiocatore - (1 * Costants.DIVISORE_100);
			}
			if (espulso != 0) {
				if (ammonizione != 0) {
					votoGiocatore = votoGiocatore + (1 * Costants.DIVISORE_100);
				}
				votoGiocatore = votoGiocatore - (2 * Costants.DIVISORE_100);
			}
			if (rigoreFallito != 0) {
				votoGiocatore = votoGiocatore - (rigoreFallito * 3 * Costants.DIVISORE_100);
			}
			if (rigoreParato != 0) {
				votoGiocatore = votoGiocatore + (rigoreParato * 3 * Costants.DIVISORE_100);
			}
			if (autorete != 0) {
				votoGiocatore = votoGiocatore - (autorete * 2 * Costants.DIVISORE_100);
			}
			if (assist != 0) {
				votoGiocatore = votoGiocatore + (assist * 1 * Costants.DIVISORE_100);
			}
			if (pagelle.getFcGiocatore().getFcRuolo().getIdRuolo().equals("P") && goalSubito == 0 && espulso == 0 && votoGiocatore != 0) {
				if (G != 0 && CS != 0 && TS != 0) {
					votoGiocatore = votoGiocatore + Costants.DIVISORE_100;
				}
			}
		}

		// if (pagelle.getId().getIdGiocatore() == 975) {
		// LOG.info(pagelle.getId().getIdGiornata() + " " + votoGiocatore);
		// }
		return votoGiocatore;

	}

	// GENERA CALENDARIO UTIL
	// private void algoritmoDiBerger(String[] squadre) {
	//
	// Map<String, String> mapSquadre = new HashMap<String, String>();
	//
	// int GG_START = 1;
	// int GG_END = 14;
	// // GG_START = 20;
	// // GG_END = 33;
	//
	// List<FcAttore> l = (List<FcAttore>) attoreRepository.findAll();
	// for (FcAttore attore : l) {
	// if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {
	// mapSquadre.put("" + attore.getIdAttore(), attore.getDescAttore());
	// for (int gg = GG_START; gg <= GG_END; gg++) {
	// String queryDelete = "DELETE FROM fc_giornata where id_attore_casa = " +
	// attore.getIdAttore() + " and id_giornata =" + gg;
	// this.jdbcTemplate.execute(queryDelete);
	// }
	// } else {
	// continue;
	// }
	// }
	//
	// int numero_squadre = squadre.length;
	// int giornate = numero_squadre - 1;
	//
	// /* crea gli array per le due liste in casa e fuori */
	// String[] casa = new String[numero_squadre / 2];
	// String[] trasferta = new String[numero_squadre / 2];
	//
	// for (int i = 0; i < numero_squadre / 2; i++) {
	// casa[i] = squadre[i];
	// trasferta[i] = squadre[numero_squadre - 1 - i];
	// }
	//
	// for (int i = 0; i < giornate; i++) {
	// /* stampa le partite di questa giornata */
	// int giornata = i + 1;
	// LOG.debug("%d^ Giornata " + giornata);
	//
	// /* alterna le partite in casa e fuori */
	// if (i % 2 == 0) {
	// for (int j = 0; j < numero_squadre / 2; j++) {
	// // mapSquadre.get(trasferta[j]), mapSquadre.get(casa[j]));
	// LOG.debug(mapSquadre.get(trasferta[j]) + " " + mapSquadre.get(casa[j]));
	// String sqlA = "insert into fc_giornata
	// (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values (" +
	// giornata + "," + trasferta[j] + "," + casa[j] + ",0) ";
	// this.jdbcTemplate.execute(sqlA);
	//
	// String sqlR = "insert into fc_giornata
	// (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values (" +
	// (giornata + 7) + "," + casa[j] + "," + trasferta[j] + ",0) ";
	// this.jdbcTemplate.execute(sqlR);
	// }
	//
	// } else {
	// for (int j = 0; j < numero_squadre / 2; j++) {
	// // mapSquadre.get(trasferta[j]));
	// LOG.debug(mapSquadre.get(casa[j]) + " " + mapSquadre.get(trasferta[j]));
	//
	// String sqlA = "insert into fc_giornata
	// (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values (" +
	// giornata + "," + casa[j] + "," + trasferta[j] + ",0) ";
	// this.jdbcTemplate.execute(sqlA);
	//
	// String sqlR = "insert into fc_giornata
	// (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values (" +
	// (giornata + 7) + "," + trasferta[j] + "," + casa[j] + ",0) ";
	// this.jdbcTemplate.execute(sqlR);
	//
	// }
	// }
	//
	// // Ruota in gli elementi delle liste, tenendo fisso il primo
	// // elemento
	// // Salva l'elemento fisso
	// String pivot = casa[0];
	//
	// /*
	// * sposta in avanti gli elementi di "trasferta" inserendo all'inizio
	// * l'elemento casa[1] e salva l'elemento uscente in "riporto"
	// */
	//
	// String riporto = trasferta[trasferta.length - 1];
	// trasferta = shiftRight(trasferta, casa[1]);
	//
	// /*
	// * sposta a sinistra gli elementi di "casa" inserendo all'ultimo
	// * posto l'elemento "riporto"
	// */
	//
	// casa = shiftLeft(casa, riporto);
	//
	// // ripristina l'elemento fisso
	// casa[0] = pivot;
	// }
	// }

	// private String[] shiftLeft(String[] data, String add) {
	// String[] temp = new String[data.length];
	// for (int i = 0; i < data.length - 1; i++) {
	// temp[i] = data[i + 1];
	// }
	// temp[data.length - 1] = add;
	// return temp;
	// }
	//
	// private String[] shiftRight(String[] data, String add) {
	// String[] temp = new String[data.length];
	// for (int i = 1; i < data.length; i++) {
	// temp[i] = data[i - 1];
	// }
	// temp[0] = add;
	// return temp;
	// }

	// Implementing Fisher–Yates shuffle
	private void shuffleArray(int[] ar) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	private void calendarNew(FcCampionato campionato, Integer[] squadre) {

		Map<Integer, String> mapSquadre = new HashMap<Integer, String>();
		int GG_START = 1;
		int GG_END = 14;
		int incremento = 0;

		if (campionato.getIdCampionato() == 2) {
			GG_START = 20;
			GG_END = 33;
			incremento = 19;
		}

		List<FcAttore> l = (List<FcAttore>) attoreRepository.findAll();
		for (FcAttore attore : l) {
			if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {
				mapSquadre.put(attore.getIdAttore(), attore.getDescAttore());
				for (int gg = GG_START; gg <= GG_END; gg++) {
					String queryDelete = "DELETE FROM fc_giornata where id_attore_casa = " + attore.getIdAttore() + " and id_giornata =" + gg;
					this.jdbcTemplate.execute(queryDelete);
				}
			} else {
				continue;
			}
		}

		ArrayList<FcGiornata> calend = new ArrayList<FcGiornata>();

		calend.add(buildPartita(1 + incremento, squadre[0], squadre[1]));
		calend.add(buildPartita(1 + incremento, squadre[2], squadre[7]));
		calend.add(buildPartita(1 + incremento, squadre[3], squadre[6]));
		calend.add(buildPartita(1 + incremento, squadre[5], squadre[4]));

		calend.add(buildPartita(2 + incremento, squadre[1], squadre[5]));
		calend.add(buildPartita(2 + incremento, squadre[4], squadre[3]));
		calend.add(buildPartita(2 + incremento, squadre[6], squadre[2]));
		calend.add(buildPartita(2 + incremento, squadre[7], squadre[0]));

		calend.add(buildPartita(3 + incremento, squadre[2], squadre[4]));
		calend.add(buildPartita(3 + incremento, squadre[3], squadre[1]));
		calend.add(buildPartita(3 + incremento, squadre[5], squadre[0]));
		calend.add(buildPartita(3 + incremento, squadre[6], squadre[7]));

		calend.add(buildPartita(4 + incremento, squadre[0], squadre[3]));
		calend.add(buildPartita(4 + incremento, squadre[1], squadre[2]));
		calend.add(buildPartita(4 + incremento, squadre[4], squadre[6]));
		calend.add(buildPartita(4 + incremento, squadre[7], squadre[5]));

		calend.add(buildPartita(5 + incremento, squadre[2], squadre[0]));
		calend.add(buildPartita(5 + incremento, squadre[3], squadre[5]));
		calend.add(buildPartita(5 + incremento, squadre[4], squadre[7]));
		calend.add(buildPartita(5 + incremento, squadre[6], squadre[1]));

		calend.add(buildPartita(6 + incremento, squadre[0], squadre[6]));
		calend.add(buildPartita(6 + incremento, squadre[1], squadre[4]));
		calend.add(buildPartita(6 + incremento, squadre[3], squadre[7]));
		calend.add(buildPartita(6 + incremento, squadre[5], squadre[2]));

		calend.add(buildPartita(7 + incremento, squadre[2], squadre[3]));
		calend.add(buildPartita(7 + incremento, squadre[4], squadre[0]));
		calend.add(buildPartita(7 + incremento, squadre[6], squadre[5]));
		calend.add(buildPartita(7 + incremento, squadre[7], squadre[1]));

		for (FcGiornata g : calend) {
			Integer idGiornata = g.getId().getIdGiornata();
			Integer idAttoreCasa = g.getFcAttoreByIdAttoreCasa().getIdAttore();
			Integer idAttoreFuori = g.getFcAttoreByIdAttoreFuori().getIdAttore();
			LOG.debug(idGiornata + " " + mapSquadre.get(idAttoreCasa) + "  " + mapSquadre.get(idAttoreFuori));

			String sqlA = "insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values  (" + idGiornata + "," + idAttoreCasa + "," + idAttoreFuori + ",0) ";
			this.jdbcTemplate.execute(sqlA);

			String sqlR = "insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values  (" + (idGiornata + 7) + "," + idAttoreFuori + "," + idAttoreCasa + ",0) ";
			this.jdbcTemplate.execute(sqlR);

		}

	}

	private FcGiornata buildPartita(Integer giornata, Integer idAttoreCasa,
			Integer idAttoreFuori) {

		FcGiornata partita = new FcGiornata();

		FcGiornataId giornataPK = new FcGiornataId();
		giornataPK.setIdGiornata(giornata);
		giornataPK.setIdAttoreCasa(idAttoreCasa);
		partita.setId(giornataPK);

		FcAttore attoreCasa = new FcAttore();
		attoreCasa.setIdAttore(idAttoreCasa);
		partita.setFcAttoreByIdAttoreCasa(attoreCasa);

		FcAttore attoreFuori = new FcAttore();
		attoreFuori.setIdAttore(idAttoreFuori);
		partita.setFcAttoreByIdAttoreFuori(attoreFuori);

		return partita;
	}

	public void initDbCalendarioCompetizione(String fileName) throws Exception {

		LOG.info("START initDbCalendarioCompetizione");

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			calendarioTimRepository.deleteAll();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				FcCalendarioCompetizione calendarioTim = new FcCalendarioCompetizione();
				String idGiornata = record.get(0);
				String data = record.get(1);
				String squadraCasa = record.get(2);
				String squadraFuori = record.get(3);
				int idSquadraCasa = Integer.parseInt(record.get(4));
				int idSquadraFuori = Integer.parseInt(record.get(5));
				String risultato = record.get(6);

				LOG.debug("idGiornata " + idGiornata + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

				calendarioTim.setIdGiornata(Integer.parseInt(idGiornata));

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime dateTime = LocalDateTime.parse(data, formatter);
				calendarioTim.setData(dateTime);
				calendarioTim.setIdSquadraCasa(idSquadraCasa);
				calendarioTim.setSquadraCasa(squadraCasa);
				calendarioTim.setIdSquadraFuori(idSquadraFuori);
				calendarioTim.setSquadraFuori(squadraFuori);
				calendarioTim.setRisultato(risultato);

				calendarioTimRepository.save(calendarioTim);

			}

			LOG.info("END initDbCalendarioCompetizione");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in initDbCalendarioCompetizione !!!");
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

	public void updateCalendarioTim(String fileName, int idGiornata)
			throws Exception {

		LOG.info("START updateCalendarioTim");

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			List<FcCalendarioCompetizione> listCalendarioTim = calendarioTimRepository.findByIdGiornata(idGiornata);

			for (int i = 0; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				String dataOra = record.get(0);

				if (dataOra.length() == 18 || dataOra.length() == 17) {
					if (dataOra.substring(2, 3).equals("/") && dataOra.substring(5, 6).equals("/")) {

						int idxOra = dataOra.indexOf(":");
						if (idxOra != -1) {
							String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
							if (hhmm.length() == 4) {
								dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
							}
						}

					} else {
						int idx = dataOra.indexOf("/");
						if (idx == 1) {
							dataOra = "0" + dataOra;
							int idxOra = dataOra.indexOf(":");
							if (idxOra != -1) {
								String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
								if (hhmm.length() == 4) {
									dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
								}
							}
						} else if (idx == 2) {
						}
					}
				}
				String squadraCasa = record.get(1).toUpperCase();
				String ris = record.get(2);
				String squadraFuori = record.get(3).toUpperCase();
				LOG.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

				for (FcCalendarioCompetizione cTim : listCalendarioTim) {
					if (cTim.getSquadraCasa().substring(0, 3).toUpperCase().equals(squadraCasa.substring(0, 3))) {
						String data = dataOra.substring(0, 6) + "20" + dataOra.substring(6, 8);
						String ora = dataOra.substring(dataOra.length() - 5, dataOra.length());
						String str = data + " " + ora;
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");
						LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
						cTim.setData(dateTime);
						cTim.setRisultato(ris);
						calendarioTimRepository.save(cTim);
					}
				}
			}

			LOG.info("END updateCalendarioTim");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in updateCalendarioTim !!!");
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

	public void updateCalendarioMondiale(String fileName, int idGiornata)
			throws Exception {

		LOG.info("START updateCalendarioTim");

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			List<FcCalendarioCompetizione> listCalendarioTim = calendarioTimRepository.findByIdGiornata(idGiornata);

			for (int i = 0; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				String dataOra = record.get(0);

				if (dataOra.length() == 18 || dataOra.length() == 17) {
					if (dataOra.substring(2, 3).equals("/") && dataOra.substring(5, 6).equals("/")) {

						int idxOra = dataOra.indexOf(":");
						if (idxOra != -1) {
							String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
							if (hhmm.length() == 4) {
								dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
							}
						}

					} else {
						int idx = dataOra.indexOf("/");
						if (idx == 1) {
							dataOra = "0" + dataOra;
							int idxOra = dataOra.indexOf(":");
							if (idxOra != -1) {
								String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
								if (hhmm.length() == 4) {
									dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
								}
							}
						} else if (idx == 2) {
						}
					}
				}
				String squadraCasa = record.get(2).toUpperCase();
				String squadraFuori = record.get(4).toUpperCase();
				String ris = record.get(5);
				LOG.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

				for (FcCalendarioCompetizione cTim : listCalendarioTim) {
					if (cTim.getSquadraCasa().substring(0, 3).toUpperCase().equals(squadraCasa.substring(0, 3))) {
						String data = dataOra.substring(0, 6) + "20" + dataOra.substring(6, 8);
						String ora = dataOra.substring(dataOra.length() - 5, dataOra.length());
						String str = data + " " + ora;
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");
						LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
						cTim.setData(dateTime);
						cTim.setRisultato(ris);
						calendarioTimRepository.save(cTim);
					}
				}
			}

			LOG.info("END updateCalendarioTim");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in updateCalendarioTim !!!");
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

	public void deleteAllCalendarioTim() {
		calendarioTimRepository.deleteAll();
	}

	public void insertCalendarioTim(String fileName, int idGiornata)
			throws Exception {

		LOG.info("START insertCalendarioTim");

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			boolean bUpdate = false;
			for (int i = 0; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				String dataOra = record.get(0);

				if (dataOra.length() == 18 || dataOra.length() == 17) {
					if (dataOra.substring(2, 3).equals("/") && dataOra.substring(5, 6).equals("/")) {

						int idxOra = dataOra.indexOf(":");
						if (idxOra != -1) {
							String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
							if (hhmm.length() == 4) {
								dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
							}
						}

					} else {
						int idx = dataOra.indexOf("/");
						if (idx == 1) {
							dataOra = "0" + dataOra;
							int idxOra = dataOra.indexOf(":");
							if (idxOra != -1) {
								String hhmm = dataOra.substring(idxOra + 1, dataOra.length()).trim();
								if (hhmm.length() == 4) {
									dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
								}
							}
						} else if (idx == 2) {
						}
					}
				}
				String squadraCasa = record.get(1).toUpperCase();
				// String ris = record.get(2);
				String squadraFuori = record.get(3).toUpperCase();
				LOG.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

				String data = dataOra.substring(0, 6) + "20" + dataOra.substring(6, 8);
				String ora = dataOra.substring(dataOra.length() - 5, dataOra.length());
				String str = data + " " + ora;
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");
				LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

				FcCalendarioCompetizione calendarioTim = new FcCalendarioCompetizione();
				calendarioTim.setIdGiornata(idGiornata);
				calendarioTim.setData(dateTime);

				FcSquadra squadra = squadraRepository.findByNomeSquadra(squadraCasa);
				calendarioTim.setIdSquadraCasa(squadra.getIdSquadra());
				calendarioTim.setSquadraCasa(squadraCasa);

				squadra = squadraRepository.findByNomeSquadra(squadraFuori);
				calendarioTim.setIdSquadraFuori(squadra.getIdSquadra());
				calendarioTim.setSquadraFuori(squadraFuori);

				calendarioTimRepository.save(calendarioTim);

				if (!bUpdate) {
					FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(idGiornata);
					giornataInfo.setDataGiornata(dateTime);
					giornataInfoRepository.save(giornataInfo);
					bUpdate = true;
				}
			}

			LOG.info("END insertCalendarioTim");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in insertCalendarioTim !!!");
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

	public void initDbGiornataGiocatore(FcGiornataInfo giornataInfo,
			String fileName, boolean bSqualificato, boolean bInfortunato)
			throws Exception {

		LOG.info("START initDbGiornataGiocatore");

		FileReader fileReader = null;
		CSVParser csvFileParser = null;

		// Create the CSVFormat object with the header mapping
		@SuppressWarnings("deprecation")
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

		try {

			// initialize FileReader object
			fileReader = new FileReader(fileName);

			// initialize CSVParser object
			csvFileParser = new CSVParser(fileReader,csvFileFormat);

			// Get a list of CSV file records
			List<CSVRecord> csvRecords = csvFileParser.getRecords();

			// LocalDateTime now = LocalDateTime.now();

			for (int i = 1; i < csvRecords.size(); i++) {
				CSVRecord record = csvRecords.get(i);

				String cognGiocatore = record.get(0);
				String note = record.get(1);

				List<FcGiocatore> listGiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
				FcGiocatore giocatore = listGiocatore.get(0);

				FcGiornataGiocatore giornataGiocatore = new FcGiornataGiocatore();
				FcGiornataGiocatoreId giornataGiocatorePK = new FcGiornataGiocatoreId();
				giornataGiocatorePK.setIdGiornata(giornataInfo.getCodiceGiornata());
				giornataGiocatorePK.setIdGiocatore(giocatore.getIdGiocatore());
				giornataGiocatore.setId(giornataGiocatorePK);
				giornataGiocatore.setInfortunato(bInfortunato);
				giornataGiocatore.setSqualificato(bSqualificato);
				if (bInfortunato) {
					giornataGiocatore.setNote("Infortunato: " + note);
				} else if (bSqualificato) {
					giornataGiocatore.setNote("Squalificato: " + note);
				}
				this.giornataGiocatoreRepository.save(giornataGiocatore);

			}

			LOG.info("END initDbGiornataGiocatore");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error in initDbGiornataGiocatore !!!");
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
}
