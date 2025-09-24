package fcweb.backend.job;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

import com.vaadin.flow.server.VaadinSession;

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
public class JobProcessGiornata {

    private static final Logger log = LoggerFactory.getLogger(JobProcessGiornata.class);

    private static final int MOLTIPLICATORE = 10000;

    private static final String IMG_NO_CAMPIONCINO = "no-campioncino.png";

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

    public HashMap<Object, Object> initDbGiocatori(String httpUrlImg, String imgPath, String fileName,
            boolean updateQuotazioni, boolean updateImg, String percentuale) throws Exception {

        log.info("START initDbGiocatori");

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
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

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
                log.debug("giocatore " + cognGiocatore + " qI " + quotazioneIniziale + " qA " + quotazioneAttuale);
                if (StringUtils.isNotEmpty(idGiocatore)) {
                    giocatore = this.giocatoreRepository.findByIdGiocatore(Integer.parseInt(idGiocatore));
                    if (giocatore == null) {
                        giocatore = new FcGiocatore();
                        giocatore.setData(now);
                        int newQuotaz = calcolaQuotazione(quotazioneAttuale, idRuolo, percentuale);
                        giocatore.setQuotazione(Integer.valueOf(newQuotaz));
                        log.info("NEW GIOCATORE " + idGiocatore + " " + cognGiocatore + " " + idRuolo + " "
                                + nomeSquadra + " " + newQuotaz);
                        listGiocatoriAdd.add(giocatore);
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
                            if (!IMG_NO_CAMPIONCINO.equals(giocatore.getNomeImg())
                                    && nomeImg.equals(giocatore.getNomeImg())) {
                                nomeImg = giocatore.getNomeImg();
                                int idx = nomeImg.indexOf(".png");
                                if (idx != -1) {
                                    String nomeImg2 = nomeImg.substring(0, idx);
                                    nomeImg = nomeImg2;
                                    log.info("NEW nomeImg " + nomeImg);
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
                                    nomeImg = IMG_NO_CAMPIONCINO;
                                } else {

                                    nomeImg = nomeImg2;
                                    flag = Utils.buildFileSmall(imgPath + nomeImg, imgPath + "small-" + nomeImg);

                                    File existFile = new File(imgPath + nomeImg);
                                    if (!existFile.exists()) {
                                        nomeImg = IMG_NO_CAMPIONCINO;
                                        log.info("NOT existFile " + imgPath + nomeImg);
                                    }

                                    File existFileSmall = new File(imgPath + "small-" + nomeImg);
                                    if (!existFileSmall.exists()) {
                                        nomeImg = IMG_NO_CAMPIONCINO;
                                        log.info("NOT existFileSmall " + imgPath + "small-" + nomeImg);
                                    }
                                }

                            } else {
                                nomeImg = IMG_NO_CAMPIONCINO;
                            }

                        } else {
                            flag = Utils.buildFileSmall(imgPath + nomeImg, imgPath + "small-" + nomeImg);

                            File existFile = new File(imgPath + nomeImg);
                            if (!existFile.exists()) {
                                nomeImg = IMG_NO_CAMPIONCINO;
                                log.info("NOT existFile " + imgPath + nomeImg);
                            }

                            File existFileSmall = new File(imgPath + "small-" + nomeImg);
                            if (!existFileSmall.exists()) {
                                nomeImg = IMG_NO_CAMPIONCINO;
                                log.info("NOT existFileSmall " + imgPath + "small-" + nomeImg);
                            }
                        }
                        // nomeImg = "no-campioncino.png";

                        giocatore.setNomeImg(nomeImg);
                        giocatore.setImg(BlobProxy.generateProxy(Utils.getImage(imgPath + nomeImg)));
                        giocatore.setImgSmall(BlobProxy.generateProxy(Utils.getImage(imgPath + "small-" + nomeImg)));
                    }
                }
            }

            if (!giocatores.isEmpty()) {

                for (FcGiocatore gioc : listG) {
                    String sql = "UPDATE fc_giocatore SET FLAG_ATTIVO=0 WHERE ID_GIOCATORE=" + gioc.getIdGiocatore();
                    this.jdbcTemplate.execute(sql);
                }

                for (FcGiocatore giocatore : giocatores) {

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

                    statisticheRepository.save(statistiche);

                }

                String sql = " select id_giocatore,cogn_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null) ";
                jdbcTemplate.query(sql, new ResultSetExtractor<ArrayList<FcGiocatore>>() {

                    @Override
                    public ArrayList<FcGiocatore> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        int idGiocatore = 0;
                        String cognGiocatore = "";
                        while (rs.next()) {
                            idGiocatore = rs.getInt(1);
                            cognGiocatore = rs.getString(2);
                            log.info("idGiocatore " + idGiocatore + " cognGiocatore " + cognGiocatore);
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
                // log.info("delete1 " + delete1);

                String delete2 = " delete from fc_pagelle where id_giocatore in ( ";
                delete2 += " select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
                delete2 += " ) ";
                jdbcTemplate.update(delete2);
                // log.info("delete2 " + delete2);

                String delete3 = " delete from fc_giornata_giocatore where id_giocatore in ( ";
                delete3 += " select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
                delete3 += " ) ";
                jdbcTemplate.update(delete3);
                // log.info("delete3 " + delete3);

                String delete4 = " delete from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null) ";
                jdbcTemplate.update(delete4);
                // log.info("delete4 " + delete4);

            }

            log.info("END initDbGiocatori");

            map.put("listAdd", listGiocatoriAdd);
            map.put("listDel", listGiocatoriDel);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in initDbGiocatori !!!");
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

    private int calcolaQuotazione(String quotazione, String idRuolo, String percentuale) {

        String q = Utils.replaceString(quotazione, ",", ".");
        BigDecimal bgQ = new BigDecimal(q);

        double appo = (Double.parseDouble(bgQ.toString()) * Double.parseDouble(percentuale)) / Costants.DIVISORE_100;
        double newQuotazione = Double.parseDouble(bgQ.toString()) - appo;
        long newQuot = Math.round(newQuotazione);
        if (newQuot < 1) {
            newQuot = 1;
        }
        log.debug(" newQuot " + newQuot);

        return (int) newQuot;
    }

    public void initiDb(Integer codiceGiornata) throws Exception {
        log.info("START initiDb");

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
            FcPagelle pagelle = new FcPagelle();
            FcPagelleId pagellePK = new FcPagelleId();
            pagellePK.setIdGiornata(giornataInfo.getCodiceGiornata());
            pagellePK.setIdGiocatore(giocatore.getIdGiocatore());
            pagelle.setId(pagellePK);
            pagelleRepository.save(pagelle);
        }

        log.info("END initiDb");

    }

    public void generaCalendario(FcCampionato campionato) throws Exception {

        Integer[] squadreInt = new Integer[8];
        String[] squadre = new String[8];
        int[] solutionArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
        shuffleArray(solutionArray);
        for (int i = 0; i < solutionArray.length; i++) {
            // log.debug(solutionArray[i] + " ");
            squadre[i] = "" + solutionArray[i];
            squadreInt[i] = Integer.parseInt(squadre[i]);
        }
        log.debug("");

        // algoritmoDiBerger(squadre);

        calendarNew(campionato, squadreInt);
    }

    private Sort sortByIdSquadra() {
        return Sort.by(Sort.Direction.ASC, "idSquadra");
    }

    public void initPagelle(Integer giornata) {
        FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(giornata);
        log.debug("" + giornataInfo.getCodiceGiornata());
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

    public void executeUpdateDbFcExpRoseA(boolean freePlayer, Integer idCampionato) throws Exception {

        log.info("START executeUpdateDbFcExpRoseA");

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

        String ordinamento = "";
        String update = "";

        for (int i = 1; i <= numRighe; i++) {
            ordinamento = "" + i;

            String col = "";
            String val = "";
            for (int c = 1; c <= 10; c++) {
                col = col + "S" + c + ",R" + c + ",Q" + c + ",";
                val = val + "null,null,null,";
            }

            col = col.substring(0, col.length() - 1);
            val = val.substring(0, val.length() - 1);

            update = "insert into " + table + " (id," + col + ") values (";
            update += ordinamento + ",";
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
                List<Integer> listNotIn = new ArrayList<>();
                for (FcFormazione f : allFormaz) {
                    if (f.getFcGiocatore() != null) {
                        listNotIn.add(f.getFcGiocatore().getIdGiocatore());
                    }
                }
                giocatores = giocatoreRepository
                        .findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(true, s,
                                listNotIn);

            } else {
                giocatores = giocatoreRepository.findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(true, s);
            }

            int newRec = giocatores.size();
            // log.info(s.getNomeSquadra() + " TOT " + newRec);
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
                    FcGiocatore giocatore = giocatores.get(i2);
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
        log.info("END executeUpdateDbFcExpRoseA");
    }

    public void aggiornamentoPFGiornata(Properties p, String fileName, String idGiornata) {

        log.info("START aggiornamentoPFGiornata");

        FileReader fileReader = null;

        CSVParser csvFileParser = null;

        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

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
            formazioneHtml.append("CountSv ");
            formazioneHtml.append("</td>");
            formazioneHtml.append("<td>");
            formazioneHtml.append("NewVoto ");
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
            formazioneHtml.append("Minuti_Giocati");
            formazioneHtml.append("</td>");
            formazioneHtml.append("</tr>");

            for (int i = 1; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);
                // log.info(""+record.size());
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
                // String Aut = StringUtils.isEmpty(record.get(18)) ? "0" :
                // record.get(18);
                c++;
                // String Ass = StringUtils.isEmpty(record.get(19)) ? "0" :
                // record.get(19);
                c++;
                // String M2 = record.get(20);
                c++;
                String m3 = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
                c++;
                String ammonizione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
                c++;
                String espulsione = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);
                c++;
                // String Gdv = record.get(24);
                c++;
                // String Gdp = record.get(25);
                c++;
                String rigoreFallito = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGS
                c++;
                String rigoreParato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RIGP
                c++;
                String rigoreSegnato = StringUtils.isEmpty(record.get(c)) ? "0" : record.get(c);// RT
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
                        List<FcGiocatore> listGiocatore = this.giocatoreRepository
                                .findByCognGiocatoreContaining(cognGiocatore);
                        if (listGiocatore != null && !listGiocatore.isEmpty() && listGiocatore.size() == 1) {
                            giocatore = listGiocatore.get(0);
                        }
                    }
                }

                if (giocatore != null) {

                    int countSv = 0;

                    g = Utils.replaceString(g, ",", ".");
                    // PORTIERE SV
                    if (ruolo.equals("P")) {
                        if (g.equals("") || g.equals("s.v.") || g.equals("s,v,")) {
                            g = "6";
                        }
                    } else {
                        if (g.equals("") || g.equals("s.v.") || g.equals("s,v,")) {
                            g = "0";
                            countSv++;
                        }
                    }
                    BigDecimal bgG = new BigDecimal(g);
                    BigDecimal mG = new BigDecimal(Costants.DIVISORE_100);
                    BigDecimal risG = bgG.multiply(mG);
                    long votoG = risG.longValue();

                    cs = Utils.replaceString(cs, ",", ".");
                    // PORTIERE SV
                    if (ruolo.equals("P")) {
                        if (cs.equals("") || cs.equals("s.v.") || cs.equals("s,v,")) {
                            cs = "6";
                        }
                    } else {
                        if (cs.equals("") || cs.equals("s.v.") || cs.equals("s,v,")) {
                            cs = "0";
                            countSv++;
                        }
                    }

                    BigDecimal bgCS = new BigDecimal(cs);
                    BigDecimal mCS = new BigDecimal(Costants.DIVISORE_100);
                    BigDecimal risCS = bgCS.multiply(mCS);
                    long votoCS = risCS.longValue();

                    ts = Utils.replaceString(ts, ",", ".");
                    // PORTIERE SV
                    if (ruolo.equals("P")) {
                        if (ts.equals("") || ts.equals("s.v.") || ts.equals("s,v,")) {
                            ts = "6";
                        }
                    } else {
                        if (ts.equals("") || ts.equals("s.v.") || ts.equals("s,v,")) {
                            ts = "0";
                            countSv++;
                        }
                    }

                    BigDecimal bgTS = new BigDecimal(ts);
                    BigDecimal mTS = new BigDecimal(Costants.DIVISORE_100);
                    BigDecimal risTS = bgTS.multiply(mTS);
                    long votoTS = risTS.longValue();

                    String votoGiocatore = Utils.replaceString(m3, ",", ".");
                    // PORTIERE SV
                    if (votoGiocatore.equals("s.v.") || votoGiocatore.equals("s,v,") && ruolo.equals("P")) {
                        votoGiocatore = "6";
                    } else {
                        if (votoGiocatore.equals("s.v.") || votoGiocatore.equals("s,v,")) {
                            votoGiocatore = "0";
                        }
                    }

                    BigDecimal bg = new BigDecimal(votoGiocatore);
                    BigDecimal m = new BigDecimal("100");
                    BigDecimal ris = bg.multiply(m);
                    long voto = ris.longValue();
                    log.debug("voto M3 " + voto);

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
                        } else if ("0".equals(ts)) {
                            if (votoG <= votoCS) {
                                ts = g;
                            } else {
                                ts = cs;
                            }
                        }
                    } else if (countSv == 2) {
                        g = "0";
                        cs = "0";
                        ts = "0";
                    }

                    String divide = "3";
                    BigDecimal bdG = new BigDecimal(g);
                    BigDecimal bdCS = new BigDecimal(cs);
                    BigDecimal bdTS = new BigDecimal(ts);
                    BigDecimal bdTot0 = bdG.add(bdCS);
                    BigDecimal bdTot1 = bdTot0.add(bdTS);
                    BigDecimal bdMedia = bdTot1.divide(new BigDecimal(divide), 2, RoundingMode.HALF_UP);
                    BigDecimal bdMoltipl = new BigDecimal(Costants.DIVISORE_100);
                    BigDecimal bdRis = bdMedia.multiply(bdMoltipl);
                    long newVoto = bdRis.longValue();

                    if (countSv == 1 || countSv == 2) {
                        log.info("NewNoto - CountSv " + countSv + " - " + giocatore.getCognGiocatore() + " newVoto "
                                + newVoto + " G = " + g + " CS " + cs + " TS " + ts);

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

                    String update = "update fc_pagelle set voto_giocatore=" + newVoto;
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
                    update += " where id_giocatore=" + idGiocatore;
                    update += " and id_giornata=" + idGiornata;

                    jdbcTemplate.update(update);

                } else {
                    log.info("*************************");
                    log.info("NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
                    log.info("*************************");

                    infoNewGiocatore.append(
                            "\n" + "NOT FOUND " + idGiocatore + " " + cognGiocatore + " " + ruolo + " " + squadra);
                }
            }

            String emailDestinatario = p.getProperty("to");
            String[] to = null;
            if (emailDestinatario != null && !emailDestinatario.equals("")) {
                to = Utils.tornaArrayString(emailDestinatario, ";");
            }
            String[] cc = null;
            String[] bcc = null;
            String[] att = null;
            String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;

            // String message = "\n";
            // message += infoVoti;
            // message += "\n\n\n";
            // message += infoNewGiocatore;

            formazioneHtml.append("</table>\n");

            formazioneHtml.append("<br>\n");
            formazioneHtml.append("<br>\n");
            formazioneHtml.append("<br>\n");

            formazioneHtml.append("<p>" + infoNewGiocatore.toString() + "</p>\n");

            formazioneHtml.append("<br>\n");
            formazioneHtml.append("<br>\n");
            formazioneHtml.append("<br>\n");
            formazioneHtml.append("<p>Ciao Davide</p>\n");
            formazioneHtml.append("</body>\n");
            formazioneHtml.append("<html>");

            try {
                String from = env.getProperty("spring.mail.secondary.username");
                emailService.sendMail(false, from, to, cc, bcc, subject, formazioneHtml.toString(), "text/html", "3",
                        att);
            } catch (Exception e) {
                log.error(e.getMessage());
                try {
                    String from = env.getProperty("spring.mail.primary.username");
                    emailService.sendMail(true, from, to, cc, bcc, subject, formazioneHtml.toString(), "text/html", "3",
                            att);
                } catch (Exception e2) {
                    log.error(e2.getMessage());
                }
            }

            log.info("END aggiornamentoPFGiornata");

        } catch (Exception e) {
            log.error("Error in CsvFileReader !!!" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
                log.error("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }
    }

    public void seiPolitico(Integer giornata, FcSquadra squadra) throws Exception {

        log.info("START seiPolitico");
        log.info("Giornata " + giornata + " squadra " + squadra.getNomeSquadra());
        FcGiornataInfo giornataInfo = new FcGiornataInfo();
        giornataInfo.setCodiceGiornata(giornata);

        List<FcPagelle> lPagelle = pagelleRepository
                .findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);
        int v = 600;
        for (FcPagelle pagelle : lPagelle) {

            FcSquadra sq = pagelle.getFcGiocatore().getFcSquadra();

            if (squadra.getIdSquadra() == sq.getIdSquadra()) {
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

        log.info("END seiPolitico");

    }

    public void checkSeiPolitico(Integer giornata) throws Exception {

        log.info("START checkSeiPolitico");

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

            Boolean bSeiPolitico = jdbcTemplate.query(sql, new ResultSetExtractor<Boolean>() {
                @Override
                public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 20) {
                            return Boolean.TRUE;
                        }
                    }
                    return Boolean.FALSE;
                }
            });

            if (bSeiPolitico != null && bSeiPolitico.booleanValue()) {
                seiPolitico(giornata, s);
            }
        }

        log.info("END checkSeiPolitico");
    }

    public void aggiornaVotiGiocatori(int giornata, int forzaVotoGiocatore, boolean bRoundVoto) throws Exception {

        log.info("START aggiornaVotiGiocatori");

        FcGiornataInfo giornataInfo = new FcGiornataInfo();
        giornataInfo.setCodiceGiornata(giornata);
        List<FcPagelle> lPagelle = pagelleRepository
                .findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);

        for (FcPagelle pagelle : lPagelle) {

            int votoGiocatore = Utils.buildVoto(pagelle, bRoundVoto);

            if (forzaVotoGiocatore == 0) {
                votoGiocatore = forzaVotoGiocatore;
            }

            String sql = "UPDATE fc_giornata_dett SET ";
            sql += " VOTO=" + votoGiocatore;
            sql += " WHERE ID_GIOCATORE=" + pagelle.getFcGiocatore().getIdGiocatore();
            sql += " AND ID_GIORNATA=" + pagelle.getFcGiornataInfo().getCodiceGiornata();
            this.jdbcTemplate.execute(sql);
        }

        log.info("END aggiornaVotiGiocatori");

    }

    public void aggiornaTotRosa(String idCampionato, int giornata) throws Exception {

        log.info("START aggiornaTotRosa");

        String sql = " select pt.id_giornata,pt.id_attore,";
        sql += " sum(pt.voto) as tot25 ";
        sql += " from fc_giornata_dett pt ";
        sql += " where  pt.id_giornata>= " + giornata;
        sql += " group by pt.id_giornata,pt.id_attore ";
        sql += " order by 1,3 desc";

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                int idGiornata = 0;
                int idAttore = 0;
                int tot25 = 0;
                while (rs.next()) {

                    idGiornata = rs.getInt(1);
                    idAttore = rs.getInt(2);
                    tot25 = rs.getInt(3);

                    String sqlUpdate = " UPDATE fc_classifica_tot_pt SET ";
                    sqlUpdate += " tot_pt_rosa=" + tot25;
                    sqlUpdate += " WHERE id_attore=" + idAttore;
                    sqlUpdate += " AND id_giornata=" + idGiornata;
                    jdbcTemplate.execute(sqlUpdate);
                }

                return "1";
            }
        });

        for (int attore = 1; attore < 9; attore++) {

            sql = " SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA) , ID_ATTORE FROM fc_classifica_tot_pt ";
            sql += " WHERE ID_CAMPIONATO=" + idCampionato;
            sql += " AND ID_ATTORE =" + attore;

            jdbcTemplate.query(sql, new ResultSetExtractor<String>() {

                @Override
                public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                    if (rs.next()) {

                        String totPunti = rs.getString(1);
                        String totPuntiOld = rs.getString(2);
                        String totPuntiRosa = rs.getString(3);
                        String idAttore = rs.getString(4);

                        String query = " UPDATE fc_classifica SET TOT_PUNTI=" + totPunti + ",";
                        query += " TOT_PUNTI_OLD=" + totPuntiOld + ",";
                        query += " TOT_PUNTI_ROSA=" + totPuntiRosa;
                        query += " WHERE ID_CAMPIONATO=" + idCampionato;
                        query += " AND ID_ATTORE =" + idAttore;

                        jdbcTemplate.update(query);

                        return "1";
                    }

                    return null;
                }
            });

        }

        log.info("END aggiornaTotRosa");

    }

    public void aggiornaScore(int giornata, String colPt, String colScore) throws Exception {

        log.info("START aggiornaScore");

        HashMap<String, String> map = new HashMap<>();
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

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                int idGiornata = 0;
                int idAttore = 0;
                String score = "0";

                int conta = 7;
                while (rs.next()) {

                    idGiornata = rs.getInt(1);
                    idAttore = rs.getInt(2);

                    score = "" + conta;
                    if ("score_grand_prix".equals(colScore)) {
                        score = map.get("" + conta);
                    }

                    String sqlUpdate = " UPDATE fc_classifica_tot_pt SET ";
                    sqlUpdate += colScore + "=" + score;
                    sqlUpdate += " WHERE id_attore=" + idAttore;
                    sqlUpdate += " AND id_giornata=" + idGiornata;
                    jdbcTemplate.execute(sqlUpdate);

                    conta--;
                    if (conta == -1) {
                        conta = 7;
                    }
                }

                return "1";
            }
        });

        log.info("END aggiornaScore");

    }

    private Sort sortBy() {
        return Sort.by(Sort.Direction.ASC, "fcGiocatore");
    }

    public void statistiche(FcCampionato campionato) throws Exception {

        log.info("START statistiche");

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
            log.info("idGiocatore " + idGiocatore);

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

                List<FcFormazione> listFormazione = formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato,
                        appoFcGiocatore);
                String proprietario = "";
                if (!listFormazione.isEmpty()) {
                    FcFormazione formazione = listFormazione.get(0);
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
                // statistiche.setFcGiocatore(appoFcGiocatore);
                statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

                log.debug("SAVE STATISTICA GIOCATORE " + appoFcGiocatore.getIdGiocatore() + " "
                        + appoFcGiocatore.getCognGiocatore() + " " + proprietario);

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

        List<FcFormazione> listFormazione = formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato,
                appoFcGiocatore);
        String proprietario = "";
        if (!listFormazione.isEmpty()) {
            FcFormazione formazione = listFormazione.get(0);
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
        // statistiche.setFcGiocatore(appoFcGiocatore);
        statistiche.setFlagAttivo(appoFcGiocatore.isFlagAttivo());

        statisticheRepository.save(statistiche);

        log.info("END statistiche");

    }

    public void inserisciUltimaFormazione(int idAttore, int giornata) throws Exception {
        int prevGG = giornata - 1;

        String delete = "delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore="
                + idAttore;
        jdbcTemplate.update(delete);
        String delete2 = "delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore;
        jdbcTemplate.update(delete2);

        String ins = "insert into fc_giornata_dett (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO) ";
        ins += "SELECT " + giornata + "," + idAttore
                + ",ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,0 from fc_giornata_dett where id_giornata=" + prevGG
                + " and id_attore=" + idAttore;
        jdbcTemplate.update(ins);

        String ins2 = "insert into fc_giornata_dett_info (ID_GIORNATA, ID_ATTORE,FLAG_INVIO,DATA_INVIO) ";
        ins2 += "select " + giornata + "," + idAttore
                + ",FLAG_INVIO,DATA_INVIO from fc_giornata_dett_info where id_giornata=" + prevGG + " and id_attore="
                + idAttore;
        jdbcTemplate.update(ins2);
    }

    public void resetFormazione(int idAttore, int giornata) throws Exception {
        String delete = "delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore="
                + idAttore;
        jdbcTemplate.update(delete);
        String delete2 = "delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore;
        jdbcTemplate.update(delete2);
    }

    public void inserisciFormazione442(FcCampionato campionato, FcAttore attore, int giornata) throws Exception {

        int idAttore = attore.getIdAttore();
        String query = " DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
        jdbcTemplate.update(query);

        List<FcFormazione> listFormazione = formazioneRepository
                .findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);

        ArrayList<FcFormazione> listTitP = new ArrayList<>();
        ArrayList<FcFormazione> listTitD = new ArrayList<>();
        ArrayList<FcFormazione> listTitC = new ArrayList<>();
        ArrayList<FcFormazione> listTitA = new ArrayList<>();

        ArrayList<FcFormazione> listRisP = new ArrayList<>();
        ArrayList<FcFormazione> listRisD = new ArrayList<>();
        ArrayList<FcFormazione> listRisC = new ArrayList<>();
        ArrayList<FcFormazione> listRisA = new ArrayList<>();

        ArrayList<FcFormazione> listTribuna = new ArrayList<>();

        for (FcFormazione f : listFormazione) {

            FcGiocatore g = f.getFcGiocatore();
            if (g == null) {
                continue;
            }
            String r = g.getFcRuolo().getIdRuolo();

            if ("P".equals(r)) {

                if (listTitP.isEmpty()) {
                    listTitP.add(f);
                } else if (listRisP.isEmpty()) {
                    listRisP.add(f);
                } else {
                    listTribuna.add(f);
                }

            } else if ("D".equals(r)) {

                if (listTitD.isEmpty() || listTitD.size() < 4) {
                    listTitD.add(f);
                } else if (listRisD.isEmpty() || listRisD.size() < 2) {
                    listRisD.add(f);
                } else {
                    listTribuna.add(f);
                }

            } else if ("C".equals(r)) {

                if (listTitC.isEmpty() || listTitC.size() < 4) {
                    listTitC.add(f);
                } else if (listRisC.isEmpty() || listRisC.size() < 2) {
                    listRisC.add(f);
                } else {
                    listTribuna.add(f);
                }

            } else if ("A".equals(r)) {

                if (listTitA.isEmpty() || listTitA.size() < 2) {
                    listTitA.add(f);
                } else if (listRisA.isEmpty() || listRisA.size() < 2) {
                    listRisA.add(f);
                } else {
                    listTribuna.add(f);
                }

            }
        }

        // TITOLARI
        int ord = 1;
        for (FcFormazione f : listTitP) {

            String idStatoGiocatore = "T";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listTitD) {

            ord++;
            String idStatoGiocatore = "T";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listTitC) {

            ord++;
            String idStatoGiocatore = "T";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listTitA) {

            ord++;
            String idStatoGiocatore = "T";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        // RISERVE
        for (FcFormazione f : listRisP) {

            ord++;
            String idStatoGiocatore = "R";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listRisD) {

            ord++;
            String idStatoGiocatore = "R";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listRisC) {

            ord++;
            String idStatoGiocatore = "R";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        for (FcFormazione f : listRisA) {

            ord++;
            String idStatoGiocatore = "R";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ord + ",0)";
            jdbcTemplate.update(query);

        }

        // TRIBUNA
        int ordTrib = 19;
        for (FcFormazione f : listTribuna) {
            int ordinamento = ordTrib;
            if (f.getFcGiocatore() == null) {
                continue;
            }
            ordTrib++;
            String idStatoGiocatore = "N";
            int idGiocatore = f.getFcGiocatore().getIdGiocatore();

            query = " INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                    + giornata + ",";
            query += idAttore + "," + idGiocatore + ",'" + idStatoGiocatore + "'," + ordinamento + ",0)";

            jdbcTemplate.update(query);
        }

        query = " DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore;
        jdbcTemplate.update(query);

        String dataora = getSysdate();
        query = " INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES (" + giornata
                + ",";
        query += idAttore + ",1, '" + dataora + "')";

        jdbcTemplate.update(query);

    }

    private String getSysdate() {

        String sql = "select sysdate() from dual";
        return jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        });
    }

    public void algoritmo(Integer giornata, FcCampionato campionato, int forzaVotoGiocatore, boolean bRoundVoto)
            throws Exception {

        log.info("START algoritmo");

        int giornataFc = giornata;
        if (giornata > 19) {
            giornataFc = giornata - 19;
        }
        Buffer bufBonus = new Buffer();
        if (giornataFc == 15) {
            bufBonus = getAttoriBonusOttaviAndata("" + campionato.getIdCampionato());
        } else if (giornataFc == 17) {
            bufBonus = getAttoriBonusSemifinaliAndata("" + campionato.getIdCampionato());
        }

        log.info("giornata " + giornata);

        FcGiornataInfo giornataInfo = giornataInfoRepository.findByCodiceGiornata(giornata);

        List<FcGiornata> lGiornata = giornataRepository.findByFcGiornataInfo(giornataInfo);

        List<FcAttore> l = attoreRepository.findByActive(true);

        for (FcAttore attore : l) {

            if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {

            } else {
                continue;
            }

            log.debug("----------------------------------------");
            log.debug("START DESC_ATTORE      -----> " + attore.getDescAttore());
            log.debug("----------------------------------------");
            log.debug("");

            List<FcGiornataDett> lGiocatori = giornataDettRepository
                    .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

            int idAttore = attore.getIdAttore();

            String idCognMinPorTit = "";
            String idCognMinDifTit = "";
            String idCognMinCenTit = "";
            String idCognMinAttTit = "";

            String idCognMinPorRis = "";
            String idCognMinDifRis = "";
            String idCognMinCenRis = "";
            String idCognMinAttRis = "";

            int votoMinPorTit = MOLTIPLICATORE;
            int votoMinDifTit = MOLTIPLICATORE;
            int votoMinCenTit = MOLTIPLICATORE;
            int votoMinAttTit = MOLTIPLICATORE;

            int votoMinPorRis = MOLTIPLICATORE;
            int votoMinDifRis = MOLTIPLICATORE;
            int votoMinCenRis = MOLTIPLICATORE;
            int votoMinAttRis = MOLTIPLICATORE;

            // int countPor = 0;
            int countDif = 0;
            int countCen = 0;
            int countAtt = 0;

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

                int ordinamento = giornataDett.getOrdinamento();
                String idGiocatore = "" + giocatore.getIdGiocatore();
                String idStatoGicatore = giornataDett.getFcStatoGiocatore().getIdStatoGiocatore();
                String idRuolo = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
                int espulso = pagelle.getEspulsione();

                int votoGiocatore = Utils.buildVoto(pagelle, bRoundVoto);

                if (forzaVotoGiocatore == 0) {
                    votoGiocatore = forzaVotoGiocatore;
                }

                // VOTO_GIOCATORE SENZA PENALITA DI 0,5 DELLA SECONDA RISERVA
                int votoGiocatoreNoPenalita = votoGiocatore;
                // log.debug(giornataDett.getOrdinamento() + " ID_GIOCATORE " + idGiocatore + "
                // " + giocatore.getCognGiocatore() + " VOTO_GIOCATORE " + votoGiocatore);
                if (giornataDett.getOrdinamento() > 18) {
                    String query = "UPDATE fc_giornata_dett SET ";
                    query += " FLAG_ATTIVO='N' , VOTO=" + votoGiocatore;
                    query += " WHERE ID_GIOCATORE=" + idGiocatore;
                    query += " AND ID_GIORNATA=" + giornata;
                    query += " AND ID_ATTORE=" + idAttore;

                    jdbcTemplate.update(query);

                    continue;
                }

                if (idRuolo.equals("P")) {
                    // count_por++;
                    if (idStatoGicatore.equals("T")) {
                        if (espulso == 0 && votoMinPorTit > votoGiocatore) {
                            votoMinPorTit = votoGiocatore;
                            idCognMinPorTit = idGiocatore;
                        }
                    } else if (idStatoGicatore.equals("R")) {
                        votoMinPorRis = votoGiocatore;
                        idCognMinPorRis = idGiocatore;
                    }

                } else if (idRuolo.equals("D")) {
                    countDif++;

                    if (idStatoGicatore.equals("T")) {
                        if (espulso == 0 && votoMinDifTit > votoGiocatore) {
                            votoMinDifTit = votoGiocatore;
                            idCognMinDifTit = idGiocatore;
                        }
                    } else if (idStatoGicatore.equals("R")) {

                        if (ordinamento == 13) {

                            if (!flagChangeRis2Dif) {
                                votoMinDifRis = votoGiocatore;
                                idCognMinDifRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[4] = idGiocatore;
                            }

                        } else if (ordinamento == 14) {

                            if (flagChangeRis2Dif) {
                                votoGiocatore = votoGiocatore - Costants.DIV_0_5;
                                votoMinDifRis = votoGiocatore;
                                idCognMinDifRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[4] = idGiocatore;
                            }
                        }
                    }

                } else if (idRuolo.equals("C")) {
                    countCen++;

                    if (idStatoGicatore.equals("T")) {
                        if (espulso == 0 && votoMinCenTit > votoGiocatore) {
                            votoMinCenTit = votoGiocatore;
                            idCognMinCenTit = idGiocatore;
                        }

                    } else if (idStatoGicatore.equals("R")) {

                        if (ordinamento == 15) {

                            if (!flagChangeRis2Cen) {
                                votoMinCenRis = votoGiocatore;
                                idCognMinCenRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[5] = idGiocatore;
                            }

                        } else if (ordinamento == 16) {

                            if (flagChangeRis2Cen) {
                                votoGiocatore = votoGiocatore - Costants.DIV_0_5;
                                votoMinCenRis = votoGiocatore;
                                idCognMinCenRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[5] = idGiocatore;
                            }
                        }
                    }

                } else if (idRuolo.equals("A")) {
                    countAtt++;

                    if (idStatoGicatore.equals("T")) {
                        if (espulso == 0 && votoMinAttTit > votoGiocatore) {
                            votoMinAttTit = votoGiocatore;
                            idCognMinAttTit = idGiocatore;
                        }
                    } else if (idStatoGicatore.equals("R")) {

                        if (ordinamento == 17) {

                            if (!flagChangeRis2Att) {
                                votoMinAttRis = votoGiocatore;
                                idCognMinAttRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[6] = idGiocatore;
                            }

                        } else if (ordinamento == 18) {

                            if (flagChangeRis2Att) {
                                votoGiocatore = votoGiocatore - Costants.DIV_0_5;
                                votoMinAttRis = votoGiocatore;
                                idCognMinAttRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[6] = idGiocatore;
                            }
                        }
                    }
                }

                if (bSomma) {
                    somma = somma + votoGiocatore;
                }
                bSomma = true;

                sommaTitolariRiserve = sommaTitolariRiserve + votoGiocatoreNoPenalita;

                String query = "UPDATE fc_giornata_dett SET ";
                query += " FLAG_ATTIVO='S' , VOTO=" + votoGiocatore;
                query += " WHERE ID_GIOCATORE=" + idGiocatore;
                query += " AND ID_GIORNATA=" + giornata;
                query += " AND ID_ATTORE=" + idAttore;

                jdbcTemplate.update(query);
            }

            log.debug("somma parziale   " + somma);
            log.debug("voto_min_por_tit " + votoMinPorTit);
            log.debug("voto_min_dif_tit " + votoMinDifTit);
            log.debug("voto_min_cen_tit " + votoMinCenTit);
            log.debug("voto_min_att_tit " + votoMinAttTit);

            log.debug("voto_min_por_ris " + votoMinPorRis);
            log.debug("voto_min_dif_ris " + votoMinDifRis);
            log.debug("voto_min_cen_ris " + votoMinCenRis);
            log.debug("voto_min_att_ris " + votoMinAttRis);

            int diffVotoMinPor = votoMinPorTit - votoMinPorRis + MOLTIPLICATORE;
            int diffVotoMinDif = votoMinDifTit - votoMinDifRis + MOLTIPLICATORE;
            int diffVotoMinCen = votoMinCenTit - votoMinCenRis + MOLTIPLICATORE;
            int diffVotoMinAtt = votoMinAttTit - votoMinAttRis + MOLTIPLICATORE;

            Buffer b = new Buffer();
            b.addNew("@1" + votoMinPorTit + "@2" + votoMinPorRis + "@3" + diffVotoMinPor + "@4" + idCognMinPorTit + "@5"
                    + idCognMinPorRis);
            b.addNew("@1" + votoMinDifTit + "@2" + votoMinDifRis + "@3" + diffVotoMinDif + "@4" + idCognMinDifTit + "@5"
                    + idCognMinDifRis);
            b.addNew("@1" + votoMinCenTit + "@2" + votoMinCenRis + "@3" + diffVotoMinCen + "@4" + idCognMinCenTit + "@5"
                    + idCognMinCenRis);
            b.addNew("@1" + votoMinAttTit + "@2" + votoMinAttRis + "@3" + diffVotoMinAtt + "@4" + idCognMinAttTit + "@5"
                    + idCognMinAttRis);

            log.debug("PRIMA--------------------");
            log.debug(b.getItem(1));
            log.debug(b.getItem(2));
            log.debug(b.getItem(3));
            log.debug(b.getItem(4));
            log.debug("--------------------");

            b.sort(3);

            log.debug("DOPO--------------------");
            log.debug(b.getItem(1));
            log.debug(b.getItem(2));
            log.debug(b.getItem(3));
            log.debug(b.getItem(4));
            log.debug("--------------------");

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

            for (String element : cambi) {

                String query = "UPDATE fc_giornata_dett SET ";
                query += " FLAG_ATTIVO='N'";
                query += " WHERE ID_GIOCATORE=" + element;
                query += " AND ID_GIORNATA=" + giornata;
                query += " AND ID_ATTORE=" + idAttore;

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
            ArrayList<String> listaRuoliPossibiliCambi = new ArrayList<>();
            ArrayList<String> listaIdGiocatoriCambiati = new ArrayList<>();

            List<FcGiornataDett> lGiocatori2 = giornataDettRepository
                    .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
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

            log.info("countCambiEffettuati       " + countCambiEffettuati);
            log.info("somma parziale prima cambi " + somma);
            boolean bCambioEffettuato = false;
            for (String r : listaRuoliPossibiliCambi) {

                if ("P".equals(r)) {
                    HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                            lGiocatori2, 12, r, somma);
                    if (mapResult.containsKey("SOMMA")) {
                        somma = Integer.parseInt(mapResult.get("SOMMA"));
                        listaIdGiocatoriCambiati.add(mapResult.get("ID_GIOCATORE"));
                        bCambioEffettuato = true;
                        countCambiEffettuati++;
                    }
                } else if ("D".equals(r)) {
                    HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                            lGiocatori2, 13, r, somma);
                    if (mapResult.containsKey("SOMMA")) {
                        somma = Integer.parseInt(mapResult.get("SOMMA"));
                        listaIdGiocatoriCambiati.add(mapResult.get("ID_GIOCATORE"));
                        bCambioEffettuato = true;
                        countCambiEffettuati++;
                    }
                } else if ("C".equals(r)) {
                    HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                            lGiocatori2, 15, r, somma);
                    if (mapResult.containsKey("SOMMA")) {
                        somma = Integer.parseInt(mapResult.get("SOMMA"));
                        listaIdGiocatoriCambiati.add(mapResult.get("ID_GIOCATORE"));
                        bCambioEffettuato = true;
                        countCambiEffettuati++;
                    }
                } else if ("A".equals(r)) {
                    HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                            lGiocatori2, 17, r, somma);
                    if (mapResult.containsKey("SOMMA")) {
                        somma = Integer.parseInt(mapResult.get("SOMMA"));
                        listaIdGiocatoriCambiati.add(mapResult.get("ID_GIOCATORE"));
                        bCambioEffettuato = true;
                        countCambiEffettuati++;
                    }
                }

                if (bCambioEffettuato) {
                    log.info("3 CAMBIO 1 RISERVA EFFETTUATO");
                    break;
                }
            }

            log.info("1 somma parziale dopo cambi " + somma);
            log.info("1 countCambiEffettuati      " + countCambiEffettuati);

            // sta iniziando ad essere complicato ... io riassumeri ..
            // ammessi 2 cambi con le regole che già sappiamo..
            // il 3 cambio è ammesso solo se non giocano 2 titolari di pari
            // ruolo .. ( es NON posso cambiare PDA ma potrei cambiare PAA)
            // in entrambi i casi vale la regola che il 2 cambio pari ruolo ha
            // un malus di -0,5

            if (countCambiEffettuati < 3) {

                listaRuoliPossibiliCambi = new ArrayList<>();

                List<FcGiornataDett> lGiocatori3 = giornataDettRepository
                        .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
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

                log.info("VERIFICO 3 CAMBIO 2 RISERVA");
                for (String r : listaRuoliPossibiliCambi) {
                    if ("D".equals(r)) {
                        HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                                lGiocatori3, 14, r, somma);
                        if (mapResult.containsKey("SOMMA")) {
                            somma = Integer.parseInt(mapResult.get("SOMMA"));
                            somma = somma - Costants.DIV_0_5;
                            bCambioEffettuato = true;
                            countCambiEffettuati++;
                        }
                    } else if ("C".equals(r)) {
                        HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                                lGiocatori3, 16, r, somma);
                        if (mapResult.containsKey("SOMMA")) {
                            somma = Integer.parseInt(mapResult.get("SOMMA"));
                            somma = somma - Costants.DIV_0_5;
                            bCambioEffettuato = true;
                            countCambiEffettuati++;
                        }
                    } else if ("A".equals(r)) {
                        HashMap<String, String> mapResult = effettuaCambio(giornata, idAttore, listaIdGiocatoriCambiati,
                                lGiocatori3, 18, r, somma);
                        if (mapResult.containsKey("SOMMA")) {
                            somma = Integer.parseInt(mapResult.get("SOMMA"));
                            somma = somma - Costants.DIV_0_5;
                            bCambioEffettuato = true;
                            countCambiEffettuati++;
                        }
                    }
                    if (bCambioEffettuato) {
                        log.info("3 CAMBIO 2 RISERVA EFFETTUATO");
                        break;
                    }
                }
            }

            log.info("2 somma parziale dopo cambi " + somma);
            log.info("2 countCambiEffettuati      " + countCambiEffettuati);

            // BONUS MALUS SCHEMA
            countDif = countDif - 2;
            countCen = countCen - 2;
            countAtt = countAtt - 2;

            if (countDif == 5 && countCen == 4 && countAtt == 1) {
                somma = somma + Costants.DIV_2_0;
                sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_2_0;
            } else if (countDif == 5 && countCen == 3 && countAtt == 2) {
                somma = somma + Costants.DIV_1_0;
                sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_0;
            } else if (countDif == 4 && countCen == 5 && countAtt == 1) {
                somma = somma + Costants.DIV_1_0;
                sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_0;
            } else if (countDif == 4 && countCen == 3 && countAtt == 3) {
                somma = somma - Costants.DIV_1_0;
                sommaTitolariRiserve = sommaTitolariRiserve - Costants.DIV_1_0;
            } else if (countDif == 3 && countCen == 4 && countAtt == 3) {
                somma = somma - Costants.DIV_2_0;
                sommaTitolariRiserve = sommaTitolariRiserve - Costants.DIV_2_0;
            }

            log.info("somma parziale " + somma);

            // BONUS CASA
            if (giornataFc < 15) {
                for (FcGiornata g : lGiornata) {
                    if (idAttore == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {
                        somma = somma + Costants.DIV_1_5;
                        log.info("somma dopo bonus casa " + somma);
                        sommaTitolariRiserve = sommaTitolariRiserve + Costants.DIV_1_5;
                        break;
                    }
                }
            }

            // CALCOLO TOTALE PUNTEGGIO
            String query = "DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + "" + campionato.getIdCampionato()
                    + " AND ID_ATTORE=" + idAttore + " AND ID_GIORNATA=" + giornata + "";
            jdbcTemplate.update(query);

            int totGoalTVsTutti = getTotGoal(somma);
            int sommaTVsTutti = somma;

            query = "INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT,TOT_PT_OLD,GOAL) ";
            query += " VALUES (" + "" + campionato.getIdCampionato() + "," + idAttore + "," + giornata + ","
                    + sommaTitolariRiserve + "," + sommaTVsTutti + "," + totGoalTVsTutti + ")";
            jdbcTemplate.update(query);

            // BONUS QUARTI
            if (giornataFc == 15) {
                int idx = bufBonus.findFirst("" + idAttore, 1, false);
                if (idx != -1) {
                    somma = somma + bufBonus.getFieldByInt(2);
                }
            }

            // BONUS SEMIFINALI
            if (giornataFc == 17) {
                int idx = bufBonus.findFirst("" + idAttore, 1, false);
                if (idx != -1) {
                    somma = somma + bufBonus.getFieldByInt(2);
                }
            }

            // int roundSomma = Utils.arrotonda(somma);

            log.debug("----------------------------------------");
            log.debug("END DESC_ATTORE       -----> " + attore.getDescAttore());
            log.debug("SCHEMA                -----> " + countDif + "-" + countCen + "-" + countAtt);
            log.debug("TOTALE FINALE         -----> " + somma);
            int totGoal = getTotGoal(somma);
            log.debug("GOAL SEGNATI          -----> " + totGoal);
            log.debug("GOAL   TUTTI VS TUTTI -----> " + totGoalTVsTutti);
            log.debug("TOTALE TUTTI VS TUTTI -----> " + sommaTVsTutti);
            log.debug("----------------------------------------");
            log.debug("");

            for (FcGiornata g : lGiornata) {

                if (idAttore == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {

                    query = "UPDATE fc_giornata SET ";
                    query += " TOT_CASA=" + somma + ",";
                    query += " GOL_CASA=" + totGoal;
                    query += " WHERE ID_GIORNATA=" + giornata;
                    query += " AND ID_ATTORE_CASA=" + idAttore;

                    jdbcTemplate.update(query);

                } else if (idAttore == g.getFcAttoreByIdAttoreFuori().getIdAttore()) {

                    query = "UPDATE fc_giornata SET ";
                    query += " TOT_FUORI=" + somma + ",";
                    query += " GOL_FUORI=" + totGoal;
                    query += " WHERE ID_GIORNATA=" + giornata;
                    query += " AND ID_ATTORE_FUORI=" + idAttore;

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

        List<FcGiornata> lSEGiornat = giornataRepository
                .findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(start, end);

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

            if (g.getFcAttoreByIdAttoreCasa() == null || g.getFcAttoreByIdAttoreFuori() == null
                    || g.getTotCasa() == null || g.getTotFuori() == null || g.getGolCasa() == null
                    || g.getGolFuori() == null) {
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

            String query = " DELETE FROM fc_giornata_ris WHERE ID_GIORNATA =" + idGiornata + " AND ID_ATTORE="
                    + idAttoreCasa;
            // log.info(query);
            jdbcTemplate.update(query);

            query = "INSERT INTO fc_giornata_ris (id_giornata,id_attore,vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES ("
                    + idGiornata + ",";
            query += idAttoreCasa + "," + vinte + ",";
            query += pari + "," + perse + ",";
            query += gf + "," + gs + ",";
            query += punti + "," + totFm + "," + risPartita + ",1)";
            // log.info(query);
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

                int currTotPunti = buf.getFieldByInt(9);
                currTotPunti = currTotPunti + totPunti;
                buf.setField(idx, 9, "" + currTotPunti);

                int currTotFm = buf.getFieldByInt(10);
                currTotFm = currTotFm + totFm;
                buf.setField(idx, 10, "" + currTotFm);
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

            query = "INSERT INTO fc_giornata_ris (id_giornata,id_attore, vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES ("
                    + idGiornata + ",";
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

                int currTotPunti = buf.getFieldByInt(9);
                currTotPunti = currTotPunti + totPunti;
                buf.setField(idx, 9, "" + currTotPunti);

                int currTotFm = buf.getFieldByInt(10);
                currTotFm = currTotFm + totFm;
                buf.setField(idx, 10, "" + currTotFm);
            }
        }

        // AGGIORNO classifica 1 vs tutti

        List<FcClassificaTotPt> lClasTotPt = classificaTotalePuntiRepository
                .findByFcCampionatoAndFcGiornataInfo(campionato, giornataInfo);

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

            FcClassificaTotPt fcClassificaTotPt = classificaTotalePuntiRepository
                    .findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
            fcClassificaTotPt.setPtTvsT(sommaPtGiornata);
            classificaTotalePuntiRepository.save(fcClassificaTotPt);

            log.debug("----------------------------------------");
            log.debug("DESC_ATTORE               -----> " + attore.getDescAttore());
            log.debug("GOAL_GIORNATA 1VSTUTTI    -----> " + goalGiornata);
            log.debug("SOMMA PUNTI   1VSTUTTI    -----> " + sommaPtGiornata);
            log.debug("----------------------------------------");

        }

        // AGGIORNO CLASSIFICA SE NON SONO ARRIVATO AI QUARTI
        if (giornataFc < 15) {

            String query = " DELETE FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();

            jdbcTemplate.update(query);

            Buffer newBuf = classificaFinale(buf, campionato);
            newBuf.sort(12);
            newBuf.moveFirst();
            for (int i = 0; i < buf.getRecordCount(); i++) {

                String sql = " SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA), SUM(pt_tvst) FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO="
                        + campionato.getIdCampionato();
                sql += " AND ID_ATTORE =" + newBuf.getField(1);

                jdbcTemplate.query(sql, new ResultSetExtractor<String>() {

                    @Override
                    public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {

                            String totPunti = rs.getString(1);
                            String totPuntiOld = rs.getString(2);
                            String totPuntiRosa = rs.getString(3);
                            String ptTvst = rs.getString(4);

                            String query = " INSERT INTO fc_classifica (ID_ATTORE,PUNTI,VINTE,PARI,PERSE,GF,GS,DR,";
                            query += " TOT_PUNTI,TOT_FM,ID_CAMPIONATO,ID_POSIZ,ID_POSIZ_FINAL,TOT_PUNTI_OLD,TOT_PUNTI_ROSA,tot_punti_TvsT,FM_MERCATO) VALUES ("
                                    + newBuf.getField(1) + ",";
                            query += newBuf.getField(2) + "," + newBuf.getField(3) + ",";
                            query += newBuf.getField(4) + "," + newBuf.getField(5) + ",";
                            query += newBuf.getField(6) + "," + newBuf.getField(7) + ",";
                            query += newBuf.getField(8) + "," + totPunti + ",";
                            query += newBuf.getField(10) + "," + campionato.getIdCampionato() + ",";
                            query += newBuf.getField(12) + "," + newBuf.getField(13) + ",";
                            query += totPuntiOld + "," + totPuntiRosa + "," + ptTvst + ",0)";

                            jdbcTemplate.update(query);

                            return "1";
                        }

                        return null;
                    }
                });

                newBuf.moveNext();
            }

            // INSERISCI GIORNATA QUARTI ANDATA-RITORNO
            int qa = 0;
            int qr = 0;
            if (giornata > 19) {
                qa = 34;
                qr = 35;
            } else {
                qa = 15;
                qr = 16;
            }

            jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + qa);
            jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + qr);

            String[][] quarti = new String[8][3];

            quarti[0][0] = "" + qa;
            newBuf.setCurrentIndex(8);
            quarti[0][1] = newBuf.getField(1);
            newBuf.setCurrentIndex(1);
            quarti[0][2] = newBuf.getField(1);

            quarti[1][0] = "" + qa;
            newBuf.setCurrentIndex(7);
            quarti[1][1] = newBuf.getField(1);
            newBuf.setCurrentIndex(2);
            quarti[1][2] = newBuf.getField(1);

            quarti[2][0] = "" + qa;
            newBuf.setCurrentIndex(6);
            quarti[2][1] = newBuf.getField(1);
            newBuf.setCurrentIndex(3);
            quarti[2][2] = newBuf.getField(1);

            quarti[3][0] = "" + qa;
            newBuf.setCurrentIndex(5);
            quarti[3][1] = newBuf.getField(1);
            newBuf.setCurrentIndex(4);
            quarti[3][2] = newBuf.getField(1);

            quarti[4][0] = "" + qr;
            quarti[4][1] = quarti[0][2];
            quarti[4][2] = quarti[0][1];

            quarti[5][0] = "" + qr;
            quarti[5][1] = quarti[1][2];
            quarti[5][2] = quarti[1][1];

            quarti[6][0] = "" + qr;
            quarti[6][1] = quarti[2][2];
            quarti[6][2] = quarti[2][1];

            quarti[7][0] = "" + qr;
            quarti[7][1] = quarti[3][2];
            quarti[7][2] = quarti[3][1];

            for (int i = 0; i < 8; i++) {

                query = "INSERT INTO fc_giornata (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata)  VALUES ("
                        + quarti[i][0] + ",";
                query += quarti[i][1] + "," + quarti[i][2] + ",";
                query += "null" + "," + "null" + ",";
                query += "null" + "," + "null" + ",7)";

                jdbcTemplate.update(query);

            }

            log.debug("--------------------------");

        } else {

            // AGGIORNO CLASSIFICA PUNTEGGIO DOPO QUARTI

            for (int attore = 1; attore < 9; attore++) {

                String sql = "SELECT SUM(TOT_PT) , SUM(TOT_PT_OLD) , SUM(TOT_PT_ROSA) , SUM(pt_tvst), ID_ATTORE FROM fc_classifica_tot_pt ";
                sql += " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + attore;

                jdbcTemplate.query(sql, new ResultSetExtractor<String>() {

                    @Override
                    public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {

                            String totPunti = rs.getString(1);
                            String totPuntiOld = rs.getString(2);
                            String totPuntiRosa = rs.getString(3);
                            String ptTvst = rs.getString(4);
                            String idAttore = rs.getString(5);

                            String query = " UPDATE fc_classifica SET TOT_PUNTI=" + totPunti + ",";
                            query += " TOT_PUNTI_OLD=" + totPuntiOld + ",";
                            query += " TOT_PUNTI_ROSA=" + totPuntiRosa + ",";
                            query += " tot_punti_TvsT=" + ptTvst;
                            query += " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE ="
                                    + idAttore;
                            jdbcTemplate.update(query);

                            return "1";
                        }

                        return null;
                    }
                });

            }
        }

        if (giornataFc == 16) {
            // INSERISCI GIORNATA SEMIFINALI ANDATA-RITORNO
            int ggSemiAnd = 0;
            int ggSemirit = 0;
            if (giornata > 19) {
                ggSemiAnd = 36;
                ggSemirit = 37;
            } else {
                ggSemiAnd = 17;
                ggSemirit = 18;
            }

            jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + ggSemiAnd);
            jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + ggSemirit);

            int ggA = ggSemiAnd - 2;
            int ggR = ggSemirit - 2;
            Buffer calen = getCalendarioScontroAndataRitorno(ggA, ggR, false, campionato);

            String[][] semifinali = new String[8][4];

            calen.setCurrentIndex(1);
            semifinali[0][0] = "" + ggSemiAnd;
            semifinali[0][1] = calen.getField(1);
            semifinali[0][2] = calen.getField(2);
            semifinali[0][3] = "5";

            calen.setCurrentIndex(2);
            semifinali[1][0] = "" + ggSemiAnd;
            semifinali[1][1] = calen.getField(1);
            semifinali[1][2] = calen.getField(2);
            semifinali[1][3] = "5";

            calen.setCurrentIndex(3);
            semifinali[2][0] = "" + ggSemiAnd;
            semifinali[2][1] = calen.getField(1);
            semifinali[2][2] = calen.getField(2);
            semifinali[2][3] = "6";

            calen.setCurrentIndex(4);
            semifinali[3][0] = "" + ggSemiAnd;
            semifinali[3][1] = calen.getField(1);
            semifinali[3][2] = calen.getField(2);
            semifinali[3][3] = "6";

            semifinali[4][0] = "" + ggSemirit;
            semifinali[4][1] = semifinali[0][2];
            semifinali[4][2] = semifinali[0][1];
            semifinali[4][3] = semifinali[0][3];

            semifinali[5][0] = "" + ggSemirit;
            semifinali[5][1] = semifinali[1][2];
            semifinali[5][2] = semifinali[1][1];
            semifinali[5][3] = semifinali[1][3];

            semifinali[6][0] = "" + ggSemirit;
            semifinali[6][1] = semifinali[2][2];
            semifinali[6][2] = semifinali[2][1];
            semifinali[6][3] = semifinali[2][3];

            semifinali[7][0] = "" + ggSemirit;
            semifinali[7][1] = semifinali[3][2];
            semifinali[7][2] = semifinali[3][1];
            semifinali[7][3] = semifinali[3][3];

            for (String[] element : semifinali) {

                String query = "INSERT INTO fc_giornata  (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata) VALUES ("
                        + element[0] + ",";
                query += element[1] + "," + element[2] + ",";
                query += "null" + "," + "null" + ",";
                query += "null" + "," + "null" + ",";
                query += element[3] + ")";

                jdbcTemplate.update(query);

            }
            log.debug("--------------------------");
        }

        if (giornataFc == 18) {

            // INSERISCI GIORNATE FINALI
            int ggFin = 0;
            if (giornata > 19) {
                ggFin = 38;
            } else {
                ggFin = 19;
            }

            jdbcTemplate.update("DELETE FROM fc_giornata WHERE ID_GIORNATA = " + ggFin);

            int ggAnd = ggFin - 2;
            int ggRit = ggFin - 1;

            // CALCOLO ERRATO RIVEDERE
            Buffer calen = getCalendarioScontroAndataRitorno(ggAnd, ggRit, true, campionato);

            String[][] finali = new String[4][4];

            calen.setCurrentIndex(1);
            finali[0][0] = "" + ggFin;
            finali[0][1] = calen.getField(1);
            finali[0][2] = calen.getField(2);
            finali[0][3] = "1";

            calen.setCurrentIndex(2);
            finali[1][0] = "" + ggFin;
            finali[1][1] = calen.getField(1);
            finali[1][2] = calen.getField(2);
            finali[1][3] = "2";

            calen.setCurrentIndex(3);
            finali[2][0] = "" + ggFin;
            finali[2][1] = calen.getField(1);
            finali[2][2] = calen.getField(2);
            finali[2][3] = "3";

            calen.setCurrentIndex(4);
            finali[3][0] = "" + ggFin;
            finali[3][1] = calen.getField(1);
            finali[3][2] = calen.getField(2);
            finali[3][3] = "4";

            for (String[] element : finali) {

                String query = "INSERT INTO fc_giornata (id_giornata,id_attore_casa,id_attore_fuori,gol_casa,gol_fuori,tot_casa,tot_fuori,id_tipo_giornata) VALUES ("
                        + element[0] + ",";
                query += element[1] + "," + element[2] + ",";
                query += "null" + "," + "null" + ",";
                query += "null" + "," + "null" + ",";
                query += element[3] + ")";

                jdbcTemplate.update(query);
            }
        }

        if (giornataFc == 19) {
            int ggFin = 0;
            if (giornata > 19) {
                ggFin = 38;
            } else {
                ggFin = 19;
            }
            insertFinalResult(ggFin, campionato);
        }

        log.info("END algoritmo");

    }

    private HashMap<String, String> effettuaCambio(Integer giornata, int idAttore,
            ArrayList<String> listaIdGiocatoriCambiati, List<FcGiornataDett> lGiocatori, int ordinamento,
            String ruologiocatore, int somma) throws Exception {

        log.info("START effettuaCambio");

        HashMap<String, String> mapResult = new HashMap<>();
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

                    if (findGiocatore.getOrdinamento() < 12
                            && ruologiocatore.equals(giocatoreDaSostituire.getFcRuolo().getIdRuolo())
                            && findGiocatore.getVoto() == 0 && gd.getVoto() > findGiocatore.getVoto()) {

                        somma = somma - findGiocatore.getVoto().intValue();

                        String query = "UPDATE fc_giornata_dett SET ";
                        query += " FLAG_ATTIVO='N'";
                        query += " WHERE ID_GIOCATORE=" + findGiocatore.getFcGiocatore().getIdGiocatore();
                        query += " AND ID_GIORNATA=" + giornata;
                        query += " AND ID_ATTORE=" + idAttore;

                        jdbcTemplate.update(query);

                        somma = somma + gd.getVoto().intValue();

                        query = "UPDATE fc_giornata_dett SET ";
                        query += " FLAG_ATTIVO='S'";
                        query += " WHERE ID_GIOCATORE=" + giocatoreDaCambiare.getIdGiocatore();
                        query += " AND ID_GIORNATA=" + giornata;
                        query += " AND ID_ATTORE=" + idAttore;

                        jdbcTemplate.update(query);

                        log.info("Cambio Giocatore FUORI " + findGiocatore.getFcGiocatore().getCognGiocatore()
                                + " DENTRO " + gd.getFcGiocatore().getCognGiocatore());

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
        log.info("END effettuaCambio");

        return mapResult;
    }

    private boolean validateCambioRiserva2(List<FcGiornataDett> lGiocatori, String ruolo) throws Exception {

        String idRuolo = "";
        int votoGiocatore = 0;
        int ordinamento = 0;
        boolean flagTitolare = false;
        boolean flagRiserva = false;

        for (FcGiornataDett giornataDett : lGiocatori) {

            idRuolo = giornataDett.getFcGiocatore().getFcRuolo().getIdRuolo();
            votoGiocatore = giornataDett.getFcPagelle().getVotoGiocatore();
            ordinamento = giornataDett.getOrdinamento();

            if (ruolo.equals(idRuolo)) {

                if (ordinamento < 12) {
                    // esiste un titolare che non ha preso voto ?
                    if (votoGiocatore == 0) {
                        flagTitolare = true;
                    }
                }

                if (idRuolo.equals("D")) {

                    if (ordinamento == 13) {

                        // esiste la riserva 1 che non ha preso voto ?
                        if (votoGiocatore == 0) {
                            flagRiserva = true;
                        }
                    } else if (ordinamento == 14) {
                        if (votoGiocatore == 0) {
                            flagRiserva = false;
                        }
                    }

                } else if (idRuolo.equals("C")) {

                    if (ordinamento == 15) {

                        if (votoGiocatore == 0) {
                            flagRiserva = true;
                        }
                    } else if (ordinamento == 16) {
                        if (votoGiocatore == 0) {
                            flagRiserva = false;
                        }
                    }

                } else if (idRuolo.equals("A")) {

                    if (ordinamento == 17) {

                        if (votoGiocatore == 0) {
                            flagRiserva = true;
                        }
                    } else if (ordinamento == 18) {
                        if (votoGiocatore == 0) {
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

        int goalCasa = 0;
        if (somma >= start1 && somma <= end1) {
            goalCasa = 1;
        } else if (somma >= start2 && somma <= end2) {
            goalCasa = 2;
        } else if (somma >= start3 && somma <= end3) {
            goalCasa = 3;
        } else if (somma >= start4 && somma <= end4) {
            goalCasa = 4;
        } else if (somma >= start5 && somma <= end5) {
            goalCasa = 5;
        } else if (somma >= start6 && somma <= end6) {
            goalCasa = 6;
        } else if (somma >= start7 && somma <= end7) {
            goalCasa = 7;
        } else if (somma >= start8 && somma <= end8) {
            goalCasa = 8;
        } else if (somma >= start9 && somma <= end9) {
            goalCasa = 9;
        } else if (somma >= start10 && somma <= end10) {
            goalCasa = 10;
        } else if (somma >= start11 && somma <= end11) {
            goalCasa = 11;
        } else if (somma >= start12 && somma <= end12) {
            goalCasa = 12;
        } else if (somma >= start13 && somma <= end13) {
            goalCasa = 13;
        } else if (somma >= start14 && somma <= end14) {
            goalCasa = 14;
        }
        return goalCasa;

    }

    /*
     * CALCOLO LE POSIZIONI FINALI IN CASO DI PUNTEGGIO PARI
     *
     * 1 ) Punti negli scontri diretti 2 ) Differenza reti negli scontri diretti 3 )
     * Differenza reti generali 4 ) Gol realizzati totali 5 ) Punteggio totale
     * ottenuto
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
        ArrayList<String> listAttoriProcessati = new ArrayList<>();

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
                    // log.debug("SIZE "+finale.getRecordCount());
                    for (int r = 1; r <= finale.getRecordCount(); r++) {
                        finale.setCurrentIndex(r);
                        String idAttore = finale.getField(1);

                        if (listAttoriProcessati.indexOf(idAttore) != -1) {
                            continue;
                        }

                        int row = buf.findFirst(idAttore, 1, true);
                        if (row != -1) {
                            // log.debug("posizione "+finale.getField(2));
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
            // log.debug("SIZE "+finale.getRecordCount());
            for (int r = 1; r <= finale.getRecordCount(); r++) {
                finale.setCurrentIndex(r);
                String idAttore = finale.getField(1);

                if (listAttoriProcessati.indexOf(idAttore) != -1) {
                    continue;
                }

                int row = buf.findFirst(finale.getField(1), 1, true);
                if (row != -1) {
                    // log.debug("posizione "+finale.getField(2));
                    buf.setField(row, 12, finale.getField(2));
                    listAttoriProcessati.add(idAttore);
                }
                posizione--;
            }

        } catch (Exception ex) {
        }
        return buf;
    }

    private Buffer getBufferScontro(int posizione, String att1, String att2, int p1, int p2) {

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

    private Buffer calcolaPosizione(Buffer buffer, int posizione, FcCampionato campionato) throws Exception {

        Buffer position = new Buffer();
        int righe = buffer.getRecordCount();
        // log.debug("righe "+righe);
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
            log.info("1) Punti negli scontri diretti ");
            position = getBufferScontro(posizione, att1, att2, p1, p2);
            if (position == null) {
                // 2) Differenza reti negli scontri diretti
                log.info("2) Differenza reti negli scontri diretti ");
                int reti1 = Integer.parseInt(risAtt1[1]);
                int reti2 = Integer.parseInt(risAtt2[1]);
                position = getBufferScontro(posizione, att1, att2, reti1, reti2);
                if (position == null) {
                    // 3) Differenza reti generali
                    log.info("3) Differenza reti generali");
                    int retiGen1 = Integer.parseInt(diffRetiGeneraliAtt1);
                    int retiGen2 = Integer.parseInt(diffRetiGeneraliAtt2);
                    position = getBufferScontro(posizione, att1, att2, retiGen1, retiGen2);
                    if (position == null) {
                        // 4) Gol realizzati totali
                        log.info("4) Gol realizzati totali");
                        int goalTot1 = Integer.parseInt(goalTotAtt1);
                        int goalTot2 = Integer.parseInt(goalTotAtt2);
                        position = getBufferScontro(posizione, att1, att2, goalTot1, goalTot2);
                        if (position == null) {
                            // 5) Punteggio totale ottenuto
                            log.info("5) Punteggio totale ottenuto");
                            int ptTot1 = Integer.parseInt(ptTotAtt1);
                            int ptTot2 = Integer.parseInt(ptTotAtt2);
                            position = getBufferScontro(posizione, att1, att2, ptTot1, ptTot2);
                            if (position == null) {
                                // SPARATE
                                log.info("SPARATE!!!!!!!!!!!!!!!!1");
                            }
                        }
                    }
                }
            }

        } else {

            log.debug("@ATTORI CON PARITA PUNTI " + buffer.getRecordCount());

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

                mapInfo.addNew("@1" + attore + "@2" + ptScontriDiretti + "@3" + diffRetiScontriDiretti + "@4"
                        + diffRetiGenerali + "@5" + goalTot + "@6" + ptTot);

            }

            // 1) Punti negli scontri diretti
            log.info(" 1) Punti negli scontri diretti ");
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
        // log.debug("@ATTORI CON PARITA PUNTI " + buffer.getRecordCount());
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

    private ArrayList<String> getGiornateGiocate(FcCampionato campionato, String attore, Buffer buffer) {

        String start = campionato.getStart().toString();
        String end = campionato.getEnd().toString();

        ArrayList<String> giornate = new ArrayList<>();

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

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    giornate.add(rs.getString(1));
                }
                return null;
            }
        });

        return giornate;
    }

    private String[] getInfoAttore(String idAttore, ArrayList<String> giornate) {

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

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {

            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
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
    public Buffer getCalendarioScontroAndataRitorno(int ga, int gr, boolean calFinale, FcCampionato campionato)
            throws Exception {

        String sql = " SELECT ID_ATTORE_CASA,ID_ATTORE_FUORI,GOL_CASA,GOL_FUORI,TOT_CASA,TOT_FUORI, ";
        sql += " (SELECT ID_POSIZ FROM fc_classifica WHERE ID_ATTORE = ID_ATTORE_FUORI and id_campionato="
                + campionato.getIdCampionato() + ") ID_POSIZ ";
        sql += " FROM fc_giornata WHERE ID_GIORNATA=" + ga + " OR ID_GIORNATA=" + gr;
        sql += " ORDER BY ID_GIORNATA,ID_TIPO_GIORNATA,ID_POSIZ";

        Buffer buf = new Buffer();

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                int attoreCasa = 0;
                int attoreFuori = 0;
                int golCasa = 0;
                int golFuori = 0;
                int totCasa = 0;
                int totFuori = 0;
                int idPosizione = 0;
                while (rs.next()) {

                    attoreCasa = rs.getInt(1);
                    attoreFuori = rs.getInt(2);
                    golCasa = rs.getInt(3);
                    golFuori = rs.getInt(4);
                    totCasa = rs.getInt(5);
                    totFuori = rs.getInt(6);
                    idPosizione = rs.getInt(7);

                    buf.addNew("@1" + attoreCasa + "@2" + attoreFuori + "@3" + golCasa + "@4" + golFuori + "@5"
                            + totCasa + "@6" + totFuori + "@7" + idPosizione);

                }

                return "1";
            }
        });

        Buffer bufAppo = new Buffer();

        sql = " SELECT ID_ATTORE,ID_POSIZ FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                int att = 0;
                int idPosiz = 0;
                int goalCasa = 0;
                int goalFuori = 0;
                int sommaGoal = 0;
                int totCasa = 0;
                int totFuori = 0;
                int sommaTot = 0;

                while (rs.next()) {

                    att = rs.getInt(1);
                    idPosiz = rs.getInt(2);

                    int idx = buf.findFirst("" + att, 1, false);
                    if (idx != -1) {
                        goalCasa = buf.getFieldByInt(3);
                        totCasa = buf.getFieldByInt(5);
                    }
                    idx = buf.findFirst("" + att, 2, false);
                    if (idx != -1) {
                        goalFuori = buf.getFieldByInt(4);
                        totFuori = buf.getFieldByInt(6);
                    }
                    sommaGoal = goalCasa + goalFuori;
                    sommaTot = totCasa + totFuori;

                    bufAppo.addNew("@1" + att + "@2" + goalCasa + "@3" + goalFuori + "@4" + sommaGoal + "@5" + totCasa
                            + "@6" + totFuori + "@7" + sommaTot + "@8" + idPosiz);

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

        int att1 = 0;
        int att1SommaGoal = 0;
        int att1SommaTot = 0;
        int att1IdPosiz = 0;

        int att2 = 0;
        int att2SommaGoal = 0;
        // int att_2_goal_fuori = 0;
        int att2SommaTot = 0;
        int att2IdPosiz = 0;

        buf.moveFirst();
        int idWin = 0;
        int idLose = 0;
        for (int r = 1; r < 5; r++) {

            att1 = buf.getFieldByInt(1);
            att2 = buf.getFieldByInt(2);

            int idx = bufAppo.findFirst("" + att1, 1, false);
            if (idx != -1) {
                // att_1_goal_fuori = bufAppo.getFieldByInt(3);
                att1SommaGoal = bufAppo.getFieldByInt(4);
                att1SommaTot = bufAppo.getFieldByInt(7);
                att1IdPosiz = bufAppo.getFieldByInt(8);
            }
            idx = bufAppo.findFirst("" + att2, 1, false);
            if (idx != -1) {
                // att_2_goal_fuori = bufAppo.getFieldByInt(3);
                att2SommaGoal = bufAppo.getFieldByInt(4);
                att2SommaTot = bufAppo.getFieldByInt(7);
                att2IdPosiz = bufAppo.getFieldByInt(8);
            }

            if (att1SommaGoal == att2SommaGoal) {
                // UGUALE
                if (att1SommaTot > att2SommaTot) {
                    idWin = att1;
                    idLose = att2;
                } else if (att2SommaTot > att1SommaTot) {
                    idWin = att2;
                    idLose = att1;
                } else {
                    // RIVEDERE SPAREGGIO
                    idWin = att2;
                    idLose = att1;
                }

            } else if (att1SommaGoal > att2SommaGoal) {
                // MAGGIORE
                idWin = att1;
                idLose = att2;

            } else if (att1SommaGoal < att2SommaGoal) {
                // MINORE
                idWin = att2;
                idLose = att1;
            }

            int posClas = 0;
            if (idWin == att1) {
                posClas = att1IdPosiz;
            } else {
                posClas = att2IdPosiz;
            }
            bufWin.addNew("@1" + idWin + "@2" + posClas);

            if (idLose == att1) {
                posClas = att1IdPosiz;
            } else {
                posClas = att2IdPosiz;
            }
            bufLose.addNew("@1" + idLose + "@2" + posClas);

            if (r == 1) {
                bufCalendarSemi.setField(1, 2, "" + idWin);
                bufCalendarSemi.setField(4, 2, "" + idLose);
            }

            if (r == 2) {
                bufCalendarSemi.setField(2, 2, "" + idWin);
                bufCalendarSemi.setField(3, 2, "" + idLose);
            }

            if (r == 3) {
                bufCalendarSemi.setField(2, 1, "" + idWin);
                bufCalendarSemi.setField(3, 1, "" + idLose);
            }

            if (r == 4) {
                bufCalendarSemi.setField(1, 1, "" + idWin);
                bufCalendarSemi.setField(4, 1, "" + idLose);
            }

            buf.moveNext();
        }

        printBuffer(bufCalendarSemi);
        Buffer bufCalendar = new Buffer();

        if (!calFinale) {

            // CONTROLLO
            for (int r = 1; r < 5; r++) {
                bufCalendarSemi.setCurrentIndex(r);

                att1 = bufCalendarSemi.getFieldByInt(1);
                att2 = bufCalendarSemi.getFieldByInt(2);
                int idx = bufAppo.findFirst("" + att1, 1, false);
                if (idx != -1) {
                    att1IdPosiz = bufAppo.getFieldByInt(8);
                }
                idx = bufAppo.findFirst("" + att2, 1, false);
                if (idx != -1) {
                    att2IdPosiz = bufAppo.getFieldByInt(8);
                }

                if (att1IdPosiz < att2IdPosiz) {
                    bufCalendar.addNew("@1" + att2 + "@2" + att1);
                } else {
                    bufCalendar.addNew("@1" + att1 + "@2" + att2);
                }

            }
            printBuffer(bufCalendar);
        } else {

            // OK
            // log.info("WIN");
            printBuffer(bufWin);

            // log.info("LOSE");
            printBuffer(bufLose);

            // FINALISSIMA 1/2
            bufWin.setCurrentIndex(1);
            String a1 = bufWin.getField(1);
            int a1PosClas = bufWin.getFieldByInt(2);
            bufWin.setCurrentIndex(2);
            String a2 = bufWin.getField(1);
            int a2PosClas = bufWin.getFieldByInt(2);
            if (a1PosClas < a2PosClas) {
                bufCalendar.addNew("@1" + a2 + "@2" + a1);
            } else {
                bufCalendar.addNew("@1" + a1 + "@2" + a2);
            }

            // FINALISSIMA 3/4
            bufLose.setCurrentIndex(1);
            a1 = bufLose.getField(1);
            a2PosClas = bufLose.getFieldByInt(2);
            bufLose.setCurrentIndex(2);
            a2 = bufLose.getField(1);
            a2PosClas = bufLose.getFieldByInt(2);
            if (a1PosClas < a2PosClas) {
                bufCalendar.addNew("@1" + a2 + "@2" + a1);
            } else {
                bufCalendar.addNew("@1" + a1 + "@2" + a2);
            }

            // FINALISSIMA 5/6
            bufWin.setCurrentIndex(3);
            a1 = bufWin.getField(1);
            a1PosClas = bufWin.getFieldByInt(2);
            bufWin.setCurrentIndex(4);
            a2 = bufWin.getField(1);
            a2PosClas = bufWin.getFieldByInt(2);
            if (a1PosClas < a2PosClas) {
                bufCalendar.addNew("@1" + a2 + "@2" + a1);
            } else {
                bufCalendar.addNew("@1" + a1 + "@2" + a2);
            }

            // FINALISSIMA 7/8
            bufLose.setCurrentIndex(3);
            a1 = bufLose.getField(1);
            a1PosClas = bufLose.getFieldByInt(2);
            bufLose.setCurrentIndex(4);
            a2 = bufLose.getField(1);
            a2PosClas = bufLose.getFieldByInt(2);
            if (a1PosClas < a2PosClas) {
                bufCalendar.addNew("@1" + a2 + "@2" + a1);
            } else {
                bufCalendar.addNew("@1" + a1 + "@2" + a2);
            }

            // log.info("CALENDAR");
            printBuffer(bufCalendar);

        }

        return bufCalendar;
    }

    // FINALEEEEEEEEEEEEEE
    public void insertFinalResult(int gf, FcCampionato campionato) throws Exception {

        String sql = " SELECT ID_ATTORE_CASA,ID_ATTORE_FUORI,GOL_CASA,GOL_FUORI,ID_TIPO_GIORNATA,TOT_CASA,TOT_FUORI  FROM fc_giornata "
                + " WHERE ID_GIORNATA=" + gf;

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {

                int attoreCasa = 0;
                int attoreFuori = 0;
                int golCasa = 0;
                int golFuori = 0;
                int idTipoGG = 0;
                int idWin = 0;
                int idLose = 0;
                int idPosizWin = 0;
                int idPosizLose = 0;
                int totCasa = 0;
                int totFuori = 0;

                while (rs.next()) {

                    attoreCasa = rs.getInt(1);
                    attoreFuori = rs.getInt(2);
                    golCasa = rs.getInt(3);
                    golFuori = rs.getInt(4);
                    idTipoGG = rs.getInt(5);
                    totCasa = rs.getInt(6);
                    totFuori = rs.getInt(7);

                    if (idTipoGG == 1) {
                        idPosizWin = 1;
                        idPosizLose = 2;
                    } else if (idTipoGG == 2) {
                        idPosizWin = 3;
                        idPosizLose = 4;
                    } else if (idTipoGG == 3) {
                        idPosizWin = 5;
                        idPosizLose = 6;
                    } else if (idTipoGG == 4) {
                        idPosizWin = 7;
                        idPosizLose = 8;
                    }

                    if (golCasa > golFuori) {
                        idWin = attoreCasa;
                        idLose = attoreFuori;
                    } else if (golFuori > golCasa) {
                        idWin = attoreFuori;
                        idLose = attoreCasa;
                    } else if (golFuori == golCasa) {
                        // SPARREGGGGGIOOOOOOOOOO
                        // log.info("SPAREGGIO " + attore_fuori + " " +
                        // attore_casa);

                        if (totCasa > totFuori) {
                            idWin = attoreCasa;
                            idLose = attoreFuori;
                        } else if (totFuori > totCasa) {
                            idWin = attoreFuori;
                            idLose = attoreCasa;
                        } else if (totFuori == totCasa) {
                            // SPARREGGGGGIOOOOOOOOOO
                            // log.info("SPAREGGIO " + attore_fuori + " " +
                            // attore_casa);
                        }
                    }

                    String query = " UPDATE fc_classifica SET " + " ID_POSIZ_FINAL=" + idPosizWin
                            + " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE =" + idWin;
                    jdbcTemplate.update(query);

                    query = " UPDATE fc_classifica SET " + " ID_POSIZ_FINAL=" + idPosizLose + " WHERE ID_CAMPIONATO="
                            + campionato.getIdCampionato() + " AND ID_ATTORE =" + idLose;
                    jdbcTemplate.update(query);

                }

                return "1";
            }
        });

    }

    public Buffer getAttoriBonusOttaviAndata(String idCampionato) throws Exception {

        String sql = " SELECT ID_ATTORE FROM fc_classifica WHERE ID_CAMPIONATO=" + idCampionato
                + " AND ID_POSIZ<5 ORDER BY ID_POSIZ";

        Buffer buf = new Buffer();

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {

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

    public Buffer getAttoriBonusSemifinaliAndata(String idCampionato) throws Exception {

        String sql = " SELECT ID_ATTORE,VINTE FROM fc_classifica WHERE ID_CAMPIONATO=" + idCampionato;

        Buffer buf = new Buffer();

        jdbcTemplate.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {

                int bonus = 0;
                int bonus2 = 0;
                while (rs.next()) {

                    bonus2 = rs.getInt(2);
                    bonus = bonus2 * Costants.DIVISORE_100;
                    // System.out.println("@1" + rs.getString(1) + "@2" + bonus + "@3" + bonus2);
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
            // log.debug(tmp.getField(1) + " - " + tmp.getField(2));
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
            int g = 0;
            int cs = 0;
            int ts = 0;
            if (pagelle.getG() != null) {
                g = pagelle.getG().intValue();
            }
            if (pagelle.getCs() != null) {
                cs = pagelle.getCs().intValue();
            }
            if (pagelle.getTs() != null) {
                ts = pagelle.getTs().intValue();
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
            if (pagelle.getFcGiocatore().getFcRuolo().getIdRuolo().equals("P") && goalSubito == 0 && espulso == 0
                    && votoGiocatore != 0) {
                if (g != 0 && cs != 0 && ts != 0) {
                    votoGiocatore = votoGiocatore + Costants.DIVISORE_100;
                }
            }
        }

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
    // log.debug("%d^ Giornata " + giornata);
    //
    // /* alterna le partite in casa e fuori */
    // if (i % 2 == 0) {
    // for (int j = 0; j < numero_squadre / 2; j++) {
    // // mapSquadre.get(trasferta[j]), mapSquadre.get(casa[j]));
    // log.debug(mapSquadre.get(trasferta[j]) + " " + mapSquadre.get(casa[j]));
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
    // log.debug(mapSquadre.get(casa[j]) + " " + mapSquadre.get(trasferta[j]));
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

        Map<Integer, String> mapSquadre = new HashMap<>();
        int ggStart = 1;
        int ggEnd = 14;
        int incremento = 0;

        if (campionato.getIdCampionato() == 2) {
            ggStart = 20;
            ggEnd = 33;
            incremento = 19;
        }

        List<FcAttore> l = attoreRepository.findAll();
        for (FcAttore attore : l) {
            if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {
                mapSquadre.put(attore.getIdAttore(), attore.getDescAttore());
                for (int gg = ggStart; gg <= ggEnd; gg++) {
                    String queryDelete = "DELETE FROM fc_giornata where id_attore_casa = " + attore.getIdAttore()
                            + " and id_giornata =" + gg;
                    this.jdbcTemplate.execute(queryDelete);
                }
            }
        }

        ArrayList<FcGiornata> calend = new ArrayList<>();

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
            // log.debug(idGiornata + " " + mapSquadre.get(idAttoreCasa) + " " +
            // mapSquadre.get(idAttoreFuori));

            String sqlA = "insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values  ("
                    + idGiornata + "," + idAttoreCasa + "," + idAttoreFuori + ",0) ";
            this.jdbcTemplate.execute(sqlA);

            String sqlR = "insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values  ("
                    + (idGiornata + 7) + "," + idAttoreFuori + "," + idAttoreCasa + ",0) ";
            this.jdbcTemplate.execute(sqlR);

        }

    }

    private FcGiornata buildPartita(Integer giornata, Integer idAttoreCasa, Integer idAttoreFuori) {

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

        log.info("START initDbCalendarioCompetizione");

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

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

                log.debug("idGiornata " + idGiornata + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

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

            log.info("END initDbCalendarioCompetizione");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in initDbCalendarioCompetizione !!!");
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

    public void updateCalendarioTim(String fileName, int idGiornata) throws Exception {

        log.info("START updateCalendarioTim");

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            List<FcCalendarioCompetizione> listCalendarioTim = calendarioTimRepository.findByIdGiornata(idGiornata);

            for (CSVRecord record : csvRecords) {
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
                log.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

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

            log.info("END updateCalendarioTim");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in updateCalendarioTim !!!");
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

    public void updateCalendarioMondiale(String fileName, int idGiornata) throws Exception {

        log.info("START updateCalendarioTim");

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            List<FcCalendarioCompetizione> listCalendarioTim = calendarioTimRepository.findByIdGiornata(idGiornata);

            for (CSVRecord record : csvRecords) {
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
                log.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

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

            log.info("END updateCalendarioTim");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in updateCalendarioTim !!!");
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

    public void insertCalendarioTim(String fileName, int idGiornata) throws Exception {

        log.info("START insertCalendarioTim");

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            boolean bUpdate = false;
            for (CSVRecord record : csvRecords) {
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
                log.debug("data " + dataOra + " squadraCasa " + squadraCasa + " squadraFuori " + squadraFuori);

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

            log.info("END insertCalendarioTim");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in insertCalendarioTim !!!");
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

    public void initDbGiornataGiocatore(FcGiornataInfo giornataInfo, String fileName, boolean bSqualificato,
            boolean bInfortunato) throws Exception {

        log.info("START initDbGiornataGiocatore");

        log.info("bSqualificato " + bSqualificato);
        log.info("bInfortunato " + bInfortunato);

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            // LocalDateTime now = LocalDateTime.now();

            for (CSVRecord record : csvRecords) {
                String cognGiocatore = record.get(0);
                String note = record.get(1);

                List<FcGiocatore> listGiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
                if (listGiocatore != null && !listGiocatore.isEmpty() && listGiocatore.size() == 1) {
                    FcGiocatore giocatore = listGiocatore.get(0);
                    if (giocatore != null) {
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

                    } else {
                        log.info("cognGiocatore " + cognGiocatore);
                    }

                } else {
                    log.info("cognGiocatore " + cognGiocatore);
                }
            }
            log.info("END initDbGiornataGiocatore");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in initDbGiornataGiocatore !!!");
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

    public void initDbProbabili(FcGiornataInfo giornataInfo, String fileName) throws Exception {
        
        log.info("START initDbProbabili");

        String sql = "UPDATE fc_giocatore SET NOME_GIOCATORE=null";
        this.jdbcTemplate.execute(sql);
        
        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            boolean boolPanchina = false;
            for (CSVRecord record : csvRecords) {
                String cognGiocatore = record.get(0);

                if (Costants.TITOLARI.equals(cognGiocatore) || Costants.PANCHINA.equals(cognGiocatore)) {

                    if (Costants.PANCHINA.equals(cognGiocatore)) {
                        boolPanchina = true;
                    } else if (Costants.TITOLARI.equals(cognGiocatore)) {
                        boolPanchina = false;
                    }
                    continue;
                }

                List<FcGiocatore> listGiocatore = this.giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
                if (listGiocatore != null && !listGiocatore.isEmpty() && listGiocatore.size() == 1) {
                    FcGiocatore giocatore = listGiocatore.get(0);
                    if (giocatore != null) {
                        giocatore.setNomeGiocatore(boolPanchina ? Costants.PANCHINA : Costants.TITOLARE);
                        giocatoreRepository.save(giocatore);
                    } else {
                        log.info("cognGiocatore " + cognGiocatore);
                    }

                } else {
                    log.info("cognGiocatore " + cognGiocatore);
                }
            }

            log.info("END initDbProbabili");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in initDbProbabili !!!");
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

    public void initDbProbabiliFantaGazzetta(FcGiornataInfo giornataInfo, String fileName) throws Exception {
        
        log.info("START initDbProbabiliFantaGazzetta");

        String sql = "UPDATE fc_giocatore SET NOME_GIOCATORE=null,PERCENTUALE=null";
        this.jdbcTemplate.execute(sql);
        
        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        // Create the CSVFormat object with the header mapping
        @SuppressWarnings("deprecation")
        CSVFormat csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');

        try {

            // initialize FileReader object
            fileReader = new FileReader(fileName);

            // initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            // Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            for (CSVRecord record : csvRecords) {
                String nomeImg = record.get(0);
                String titolarePanchina = record.get(1);
                String percentuale = record.get(2);
                String href = record.get(3);
                FcGiocatore giocatore = this.giocatoreRepository.findByNomeImg(nomeImg +".png");
                if (giocatore != null) {
                    giocatore.setNomeGiocatore(titolarePanchina);
                    Integer perc = null;
                    try {
                        perc = Integer.parseInt(percentuale);
                    } catch (Exception e) {
                        perc = Integer.parseInt("0");
                    }
                    giocatore.setPercentuale(perc);
                    giocatoreRepository.save(giocatore);
                } else {
                    log.info("href " + href);  
                }
            }

            log.info("END initDbProbabiliFantaGazzetta");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in initDbProbabiliFantaGazzetta !!!");
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

    public void updateImgGiocatore(InputStream is) throws Exception {

        log.info("START updateImgGiocatore");

        try {

            Workbook workbook = WorkbookFactory.create(is);

            // Retrieving the number of sheets in the Workbook
            log.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

            // 1. You can obtain a sheetIterator and iterate over it
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            log.info("Retrieving Sheets using Iterator");
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                log.info("=> " + sheet.getSheetName());
            }

            // Getting the Sheet at index zero
            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter dataFormatter = new DataFormatter();
            log.info("Iterating over Rows and Columns using for-each loop");

            int conta = 0;
            for (Row row : sheet) {

                if (conta == 0 || conta == 1) {
                    conta++;
                    log.info("SCARTO RIGA HEADER ");
                    continue;
                }

                String idGiocatore = "";
                String idRuolo = "";
                String cognGiocatore = "";
                String nomeSquadra = "";
                String quotazioneAttuale = "";

                for (Cell cell : row) {

                    String cellValue = dataFormatter.formatCellValue(cell);
                    if (cell.getColumnIndex() == 0) {
                        idGiocatore = cellValue;
                    } else if (cell.getColumnIndex() == 1) {
                        idRuolo = cellValue.toUpperCase();
                    } else if (cell.getColumnIndex() == 3) {
                        cognGiocatore = cellValue.toUpperCase();
                    } else if (cell.getColumnIndex() == 4) {
                        nomeSquadra = cellValue.toUpperCase();
                    } else if (cell.getColumnIndex() == 5) {
                        quotazioneAttuale = cellValue;
                    }
                }

                if (StringUtils.isEmpty(cognGiocatore) && StringUtils.isEmpty(idRuolo)
                        && StringUtils.isEmpty(nomeSquadra) && StringUtils.isEmpty(quotazioneAttuale)) {
                    log.info("SCARTO RIGA VUOTA ");
                    continue;
                }

                if (StringUtils.isNotEmpty(idGiocatore)) {

                    List<FcGiocatore> lgiocatore = this.giocatoreRepository
                            .findByCognGiocatoreContaining(cognGiocatore);
                    for (FcGiocatore g : lgiocatore) {
                        if (!g.getFcSquadra().getNomeSquadra().equals(nomeSquadra)) {
                            log.info("ATTENZIONE SQUADRA DIFFERENTE ");
                            log.info("" + idGiocatore + ";" + cognGiocatore + ";" + idRuolo + ";" + nomeSquadra + ";" + quotazioneAttuale + ";" + quotazioneAttuale);
                            continue;
                        }
                        String nomeImgNew = idGiocatore + ".png";
                        g.setNomeImg(nomeImgNew);
                        try {
                            Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
                            String basePathData = (String) p.get("PATH_TMP");
                            log.info("basePathData " + basePathData);
                            File f = new File(basePathData);
                            if (!f.exists()) {
                                log.error("Error basePathData " + basePathData);
                                return;
                            }

                            String newImg = g.getNomeImg();
                            log.info("newImg " + newImg);
                            log.info("httpUrlImg " + Costants.HTTP_URL_IMG);
                            String imgPath = basePathData;

                            boolean flag = Utils.downloadFile(Costants.HTTP_URL_IMG + newImg, imgPath + newImg);
                            log.info("bResult 1 " + flag);
                            flag = Utils.buildFileSmall(imgPath + newImg, imgPath + "small-" + newImg);
                            log.info("bResult 2 " + flag);

                            g.setImg(BlobProxy.generateProxy(Utils.getImage(imgPath + newImg)));
                            g.setImgSmall(BlobProxy.generateProxy(Utils.getImage(imgPath + "small-" + newImg)));

                            log.info("SAVE GIOCATORE ");
                            giocatoreRepository.save(g);

                        } catch (Exception e) {
                            log.error("Error in download save img !!!");
                        }
                    }
                }
            }

            log.info("END updateImgGiocatore");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in updateImgGiocatore !!!");
            throw e;
        }
    }

}
