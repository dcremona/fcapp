package fcapp.backend.job.player;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataGiocatore;
import fcapp.backend.data.entity.FcGiornataGiocatoreId;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataGiocatoreService;
import fcapp.backend.service.RuoloService;
import fcapp.backend.service.SquadraService;
import fcapp.backend.service.StatisticheService;
import fcapp.utils.Costants;
import fcapp.utils.Utils;

/**
 * Responsabilità unica: import giocatori, quotazioni, immagini, squalificati e probabili.
 */
@Service
public class GiocatoreInitializer {

    private static final Logger log = LoggerFactory.getLogger(GiocatoreInitializer.class);
    private static final String IMG_NO_CAMPIONCINO = "no-campioncino.png";

    @Autowired private Environment env;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private SquadraService squadraService;
    @Autowired private RuoloService ruoloService;
    @Autowired private StatisticheService statisticheService;
    @Autowired private GiornataGiocatoreService giornataGiocatoreService;
    @Autowired private JdbcTemplate jdbcTemplate;

    public HashMap<Object, Object> initDbGiocatori(String httpUrlImg, String imgPath, String fileName,
                                                  boolean updateQuotazioni, boolean updateImg, String percentuale) throws Exception {
        log.info("START initDbGiocatori");

        HashMap<Object, Object> map = new HashMap<>();
        ArrayList<FcGiocatore> listGiocatoriAdd = new ArrayList<>();
        ArrayList<FcGiocatore> listGiocatoriDel = new ArrayList<>();

        CSVFormat csvFileFormat = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser csvFileParser = new CSVParser(new FileReader(fileName), csvFileFormat)) {
            List<FcGiocatore> giocatores = new ArrayList<>();
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            List<FcGiocatore> listG = giocatoreService.findAll();
            LocalDateTime now = LocalDateTime.now();

            for (int i = 1; i < csvRecords.size(); i++) {
                CSVRecord r = csvRecords.get(i);
                String idGiocatore = r.get(0);
                String cognGiocatore = r.get(1);
                String idRuolo = r.get(2);
                String nomeSquadra = r.get(4);
                String quotazioneAttuale = r.get(6);

                if (StringUtils.isNotEmpty(idGiocatore)) {
                    FcGiocatore giocatore = giocatoreService.findByIdGiocatore(Integer.parseInt(idGiocatore));
                    if (giocatore == null) {
                        giocatore = new FcGiocatore();
                        giocatore.setData(now);
                        giocatore.setQuotazione(calcolaQuotazione(quotazioneAttuale, percentuale));
                        listGiocatoriAdd.add(giocatore);
                    }

                    if (updateQuotazioni) {
                        giocatore.setQuotazione(calcolaQuotazione(quotazioneAttuale, percentuale));
                    }

                    giocatore.setIdGiocatore(Integer.parseInt(idGiocatore));
                    giocatore.setCognGiocatore(cognGiocatore);

                    FcRuolo ruolo = new FcRuolo();
                    ruolo.setIdRuolo(idRuolo);
                    giocatore.setFcRuolo(ruolo);

                    FcSquadra squadra = squadraService.findByNomeSquadra(nomeSquadra);
                    giocatore.setFcSquadra(squadra);

                    boolean flagAttivo = !"No".equals(r.get(7));
                    giocatore.setFlagAttivo(flagAttivo);
                    if (giocatore.isFlagAttivo()) {
                        giocatores.add(giocatore);
                    }

                    if (updateImg || (giocatore.getNomeImg() == null && giocatore.getImg() == null)) {
                        handleImage(giocatore, cognGiocatore, httpUrlImg, imgPath);
                    }
                } else {
                    FcGiocatore senzaId = new FcGiocatore();
                    senzaId.setData(now);
                    FcRuolo fcRuolo = ruoloService.findByIdRuolo(idRuolo);
                    if (fcRuolo == null) continue;
                    FcSquadra fcSquadra = squadraService.findByNomeSquadra(nomeSquadra);
                    if (fcSquadra == null) continue;

                    senzaId.setIdGiocatore(0);
                    senzaId.setCognGiocatore(cognGiocatore);
                    senzaId.setFcRuolo(fcRuolo);
                    senzaId.setFcSquadra(fcSquadra);
                    senzaId.setQuotazione(calcolaQuotazione(quotazioneAttuale, percentuale));
                    listGiocatoriAdd.add(senzaId);
                }
            }

            if (!giocatores.isEmpty()) {
                for (FcGiocatore gioc : listG) {
                    jdbcTemplate.execute("UPDATE fc_giocatore SET FLAG_ATTIVO=0 WHERE ID_GIOCATORE=" + gioc.getIdGiocatore());
                }

                for (FcGiocatore g : giocatores) {
                    giocatoreService.save(g);
                    statisticheService.save(createEmptyStatistiche(g));
                }

                String sql = "select id_giocatore,cogn_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
                jdbcTemplate.query(sql, (ResultSetExtractor<Void>) rs -> {
                    while (rs.next()) {
                        listGiocatoriDel.add(giocatoreService.findByIdGiocatore(rs.getInt(1)));
                    }
                    return null;
                });

                executeCleanupDeletes();
            }

            map.put("listAdd", listGiocatoriAdd);
            map.put("listDel", listGiocatoriDel);
            log.info("END initDbGiocatori");
            return map;
        }
    }

    private void handleImage(FcGiocatore giocatore, String cognGiocatore, String httpUrlImg, String imgPath) {
        String nomeImg = cognGiocatore.toUpperCase();
        if (giocatore.getNomeImg() != null && !IMG_NO_CAMPIONCINO.equals(giocatore.getNomeImg())
                && nomeImg.equals(giocatore.getNomeImg())) {
            nomeImg = giocatore.getNomeImg();
            int idx = nomeImg.indexOf(".png");
            if (idx != -1) nomeImg = nomeImg.substring(0, idx);
        }

        nomeImg = Utils.replaceString(nomeImg, "'", "");
        nomeImg = Utils.replaceString(nomeImg, "_", "-");
        nomeImg = Utils.replaceString(nomeImg, " ", "-");
        nomeImg = Utils.replaceString(nomeImg, ".", "");
        nomeImg = Utils.replaceString(nomeImg, "'", "-") + ".png";

        boolean flag = Utils.downloadFile(httpUrlImg + nomeImg, imgPath + nomeImg);
        if (!flag) {
            int idx = nomeImg.indexOf("-");
            if (idx != -1) {
                String nomeImg2 = nomeImg.substring(0, idx) + ".png";
                flag = Utils.downloadFile(httpUrlImg + nomeImg2, imgPath + nomeImg2);
                nomeImg = flag ? nomeImg2 : IMG_NO_CAMPIONCINO;
            } else {
                nomeImg = IMG_NO_CAMPIONCINO;
            }
        }

        if (!IMG_NO_CAMPIONCINO.equals(nomeImg)) {
            Utils.buildFileSmall(imgPath + nomeImg, imgPath + "small-" + nomeImg);
            if (!new File(imgPath + nomeImg).exists() || !new File(imgPath + "small-" + nomeImg).exists()) {
                nomeImg = IMG_NO_CAMPIONCINO;
            }
        }

        giocatore.setNomeImg(nomeImg);
        giocatore.setImg(BlobProxy.generateProxy(Utils.getImage(imgPath + nomeImg)));
        giocatore.setImgSmall(BlobProxy.generateProxy(Utils.getImage(imgPath + "small-" + nomeImg)));
    }

    private FcStatistiche createEmptyStatistiche(FcGiocatore g) {
        FcStatistiche s = new FcStatistiche();
        s.setIdGiocatore(g.getIdGiocatore());
        s.setCognGiocatore(g.getCognGiocatore());
        s.setIdRuolo(g.getFcRuolo().getIdRuolo());
        s.setNomeSquadra(g.getFcSquadra().getNomeSquadra());
        s.setAmmonizione(0);
        s.setAssist(0);
        s.setEspulsione(0);
        s.setFantaMedia(0.0);
        s.setGiocate(0);
        s.setGoalFatto(0);
        s.setGoalSubito(0);
        s.setMediaVoto(0.0);
        s.setRigoreSbagliato(0);
        s.setRigoreSegnato(0);
        s.setFlagAttivo(g.isFlagAttivo());
        return s;
    }

    private int calcolaQuotazione(String quotazione, String percentuale) {
        String q = Utils.replaceString(quotazione, ",", ".");
        BigDecimal bgQ = new BigDecimal(q);
        double appo = (Double.parseDouble(bgQ.toString()) * Double.parseDouble(percentuale)) / Costants.DIVISORE_100;
        long newQuot = Math.round(Double.parseDouble(bgQ.toString()) - appo);
        return (int) Math.max(newQuot, 1);
    }

    private void executeCleanupDeletes() {
        String base = "select id_giocatore from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)";
        jdbcTemplate.update("delete from fc_statistiche where id_giocatore in (" + base + ")");
        jdbcTemplate.update("delete from fc_pagelle where id_giocatore in (" + base + ")");
        jdbcTemplate.update("delete from fc_giornata_giocatore where id_giocatore in (" + base + ")");
        jdbcTemplate.update("delete from fc_giocatore where flag_attivo=0 and id_giocatore not in (select distinct id_giocatore from fc_giornata_dett where id_giocatore is not null)");
    }

    public void updateImgGiocatore(InputStream is) throws Exception {
        log.info("START updateImgGiocatore");
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        LocalDateTime now = LocalDateTime.now();
        int conta = 0;

        for (Row row : sheet) {
            if (conta++ < 2) continue;

            String idGiocatore = "", idRuolo = "", cognGiocatore = "", nomeSquadra = "", quotazioneAttuale = "";
            for (Cell cell : row) {
                String cellValue = dataFormatter.formatCellValue(cell);
                switch (cell.getColumnIndex()) {
                    case 0 -> idGiocatore = cellValue;
                    case 1 -> idRuolo = cellValue.toUpperCase();
                    case 3 -> cognGiocatore = cellValue.toUpperCase();
                    case 4 -> nomeSquadra = cellValue.toUpperCase();
                    case 5 -> quotazioneAttuale = cellValue;
                }
            }

            if (StringUtils.isAllEmpty(cognGiocatore, idRuolo, nomeSquadra, quotazioneAttuale)) continue;

            if (StringUtils.isNotEmpty(idGiocatore)) {
                FcRuolo ruolo = ruoloService.findByIdRuolo(idRuolo);
                FcSquadra squadra = squadraService.findByNomeSquadra(nomeSquadra);
                if (ruolo == null || squadra == null) continue;

                FcGiocatore g = giocatoreService.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(cognGiocatore, squadra, ruolo);
                if (g == null) continue;

                String nomeImgNew = idGiocatore + ".png";
                g.setNomeImg(nomeImgNew);
                String basePathData = env.getProperty("PATH_TMP");
                if (basePathData == null || !new File(basePathData).exists()) return;

                boolean flag = Utils.downloadFile(Costants.HTTP_URL_IMG + nomeImgNew, basePathData + nomeImgNew);
                if (flag) {
                    Utils.buildFileSmall(basePathData + nomeImgNew, basePathData + "small-" + nomeImgNew);
                    g.setImg(BlobProxy.generateProxy(Utils.getImage(basePathData + nomeImgNew)));
                    g.setImgSmall(BlobProxy.generateProxy(Utils.getImage(basePathData + "small-" + nomeImgNew)));
                    g.setData(now);
                    giocatoreService.save(g);
                }
            }
        }
        log.info("END updateImgGiocatore");
    }

    public void initDbGiornataGiocatore(FcGiornataInfo giornataInfo, String fileName,
                                        boolean bSqualificato, boolean bInfortunato) throws Exception {
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();
        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            for (CSVRecord r : parser) {
                String cogn = r.get(0);
                String note = r.get(1);
                List<FcGiocatore> list = giocatoreService.findByCognGiocatoreContaining(cogn);
                if (list != null && list.size() == 1) {
                    FcGiocatore g = list.get(0);
                    FcGiornataGiocatore gg = new FcGiornataGiocatore();
                    FcGiornataGiocatoreId pk = new FcGiornataGiocatoreId();
                    pk.setIdGiornata(giornataInfo.getCodiceGiornata());
                    pk.setIdGiocatore(g.getIdGiocatore());
                    gg.setId(pk);
                    gg.setInfortunato(bInfortunato);
                    gg.setSqualificato(bSqualificato);
                    gg.setNote(bInfortunato ? "Infortunato: " + note : "Squalificato: " + note);
                    giornataGiocatoreService.save(gg);
                }
            }
        }
    }

    public void initDbProbabili(String fileName) throws Exception {
        jdbcTemplate.execute("UPDATE fc_giocatore SET NOME_GIOCATORE=null");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();
        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            boolean panchina = false;
            for (CSVRecord r : parser) {
                String cogn = r.get(0);
                if (Costants.TITOLARI.equals(cogn) || Costants.PANCHINA.equals(cogn)) {
                    panchina = Costants.PANCHINA.equals(cogn);
                    continue;
                }
                List<FcGiocatore> list = giocatoreService.findByCognGiocatoreContaining(cogn);
                if (list != null && list.size() == 1) {
                    FcGiocatore g = list.get(0);
                    g.setNomeGiocatore(panchina ? Costants.PANCHINA : Costants.TITOLARE);
                    giocatoreService.save(g);
                }
            }
        }
    }

    public void initDbProbabiliFantaGazzetta(String fileName) throws Exception {
        jdbcTemplate.execute("UPDATE fc_giocatore SET NOME_GIOCATORE=null,PERCENTUALE=null");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();
        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            for (CSVRecord r : parser) {
                String nomeImg = r.get(0);
                String titolarePanchina = r.get(1);
                String percentuale = r.get(2);
                FcGiocatore g = giocatoreService.findByNomeImg(nomeImg + ".png", true);
                if (g != null) {
                    g.setNomeGiocatore(titolarePanchina);
                    try {
                        g.setPercentuale(Integer.parseInt(percentuale));
                    } catch (Exception e) {
                        g.setPercentuale(0);
                    }
                    giocatoreService.save(g);
                }
            }
        }
    }

    public void initDbSqualificatiInfortunatiFantaGazzetta(FcGiornataInfo giornataInfo, String fileName) throws Exception {
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();
        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            for (CSVRecord r : parser) {
                String nomeImg = r.get(0);
                String tipo = r.get(1);
                String note = r.get(4);
                FcGiocatore g = giocatoreService.findByNomeImg(nomeImg + ".png", true);
                if (g != null) {
                    FcGiornataGiocatore gg = new FcGiornataGiocatore();
                    FcGiornataGiocatoreId pk = new FcGiornataGiocatoreId();
                    pk.setIdGiornata(giornataInfo.getCodiceGiornata());
                    pk.setIdGiocatore(g.getIdGiocatore());
                    gg.setId(pk);
                    gg.setInfortunato(Costants.INFORTUNATO.equals(tipo));
                    gg.setSqualificato(Costants.SQUALIFICATO.equals(tipo));
                    gg.setNote(Costants.INFORTUNATO.equals(tipo) ? "Infortunato: " + note : "Squalificato: " + note);
                    giornataGiocatoreService.save(gg);
                }
            }
        }
    }
}
