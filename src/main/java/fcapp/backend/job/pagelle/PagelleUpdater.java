package fcapp.backend.job.pagelle;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcPagelleId;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.EmailService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.PagelleService;
import fcapp.backend.service.SquadraService;
import fcapp.utils.Costants;
import fcapp.utils.Utils;

/**
 * Responsabilità unica: inizializzazione e aggiornamento pagelle/voti.
 */
@Service
public class PagelleUpdater {

    private static final Logger log = LoggerFactory.getLogger(PagelleUpdater.class);

    @Autowired private Environment env;
    @Autowired private EmailService emailService;
    @Autowired private GiornataInfoService giornataInfoService;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private PagelleService pagelleService;
    @Autowired private SquadraService squadraService;
    @Autowired private JdbcTemplate jdbcTemplate;

    public void initPagelle(Integer giornata) {
        FcGiornataInfo info = giornataInfoService.findByCodiceGiornata(giornata);
        for (FcGiocatore g : giocatoreService.findAll()) {
            FcPagelle p = new FcPagelle();
            FcPagelleId pk = new FcPagelleId();
            pk.setIdGiornata(info.getCodiceGiornata());
            pk.setIdGiocatore(g.getIdGiocatore());
            p.setId(pk);
            pagelleService.save(p);
        }
    }

    public void aggiornamentoPFGiornata(Properties p, String fileName, String idGiornata) {
        log.info("START aggiornamentoPFGiornata");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            StringBuilder infoNew = new StringBuilder();
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>FC</title></head><body><br><br><table>")
                .append("<tr><td>").append(Costants.GIOCATORE).append("</td><td>CountSv</td><td>NewVoto</td>")
                .append("<td>G</td><td>CS</td><td>TS</td><td>Minuti_Giocati</td></tr>");

            List<CSVRecord> records = parser.getRecords();
            for (int i = 1; i < records.size(); i++) {
                CSVRecord r = records.get(i);
                String idGiocatore = StringUtils.defaultIfEmpty(r.get(0), "0");
                try { Integer.parseInt(idGiocatore); } catch (Exception e) { continue; }

                String cogn = StringUtils.defaultIfEmpty(r.get(1), "");
                String ruolo = StringUtils.defaultIfEmpty(r.get(2), "");
                String squadra = r.get(3);
                String minGiocati = r.get(4);
                String g = StringUtils.defaultIfEmpty(r.get(5), "0");
                String goalRealizzato = StringUtils.defaultIfEmpty(r.get(6), "0");
                String goalSubito = StringUtils.defaultIfEmpty(r.get(7), "0");
                String autorete = StringUtils.defaultIfEmpty(r.get(8), "0");
                String assist = StringUtils.defaultIfEmpty(r.get(9), "0");
                String cs = StringUtils.defaultIfEmpty(r.get(10), "0");
                String ts = StringUtils.defaultIfEmpty(r.get(11), "0");
                String ammonizione = StringUtils.defaultIfEmpty(r.get(13), "0");
                String espulsione = StringUtils.defaultIfEmpty(r.get(14), "0");
                String rigoreFallito = StringUtils.defaultIfEmpty(r.get(15), "0");
                String rigoreParato = StringUtils.defaultIfEmpty(r.get(16), "0");
                String rigoreSegnato = StringUtils.defaultIfEmpty(r.get(17), "0");

                FcGiocatore giocatore = giocatoreService.findByIdGiocatore(Integer.parseInt(idGiocatore));
                if (giocatore == null) {
                    List<FcGiocatore> list = giocatoreService.findByCognGiocatoreContaining(cogn);
                    if (list != null && list.size() == 1) giocatore = list.get(0);
                }

                if (giocatore != null) {
                    int countSv = 0;
                    
                    String[] arrG = normalizeVoto(g, ruolo, countSv);
                    g = arrG[0];
                    countSv = Integer.parseInt(arrG[1]);
                    
                    String[] arrCS = normalizeVoto(cs, ruolo, countSv);
                    cs = arrCS[0];
                    countSv = Integer.parseInt(arrCS[1]);
                    
                    String[] arrTS = normalizeVoto(ts, ruolo, countSv);
                    ts = arrTS[0];
                    countSv = Integer.parseInt(arrTS[1]);

                    long votoG = toVoto(g);
                    long votoCS = toVoto(cs);
                    long votoTS = toVoto(ts);

                    if (countSv == 1) {
                        if ("0".equals(g)) g = (votoCS <= votoTS) ? cs : ts;
                        else if ("0".equals(cs)) cs = (votoG <= votoTS) ? g : ts;
                        else if ("0".equals(ts)) ts = (votoG <= votoCS) ? g : cs;
                    } else if (countSv == 2) {
                        g = cs = ts = "0";
                    }

                    long newVoto = getNewVoto(g, cs, ts);

                    if (countSv >= 1) {
                        html.append("<tr><td>").append(giocatore.getCognGiocatore())
                            .append("</td><td>").append(countSv)
                            .append("</td><td>").append(newVoto)
                            .append("</td><td>").append(g)
                            .append("</td><td>").append(cs)
                            .append("</td><td>").append(ts)
                            .append("</td><td>").append(minGiocati).append("</td></tr>");
                    }

                    String update = "update fc_pagelle set voto_giocatore=" + newVoto
                            + ",g=" + votoG + ",cs=" + votoCS + ",ts=" + votoTS
                            + ",goal_realizzato=" + goalRealizzato + ",goal_subito=" + goalSubito
                            + ",ammonizione=" + ammonizione + ",espulsione=" + espulsione
                            + ",rigore_segnato=" + rigoreSegnato + ",rigore_fallito=" + rigoreFallito
                            + ",rigore_parato=" + rigoreParato + ",autorete=" + autorete
                            + ",assist=" + assist
                            + " where id_giocatore=" + idGiocatore + " and id_giornata=" + idGiornata;
                    jdbcTemplate.update(update);
                } else {
                    infoNew.append("\nNOT FOUND ").append(idGiocatore).append(" ")
                           .append(cogn).append(" ").append(ruolo).append(" ").append(squadra);
                }
            }

            html.append("</table><br><br><p>").append(infoNew).append("</p><br><br><p>Ciao Davide</p></body></html>");

            String[] to = Utils.tornaArrayString(p.getProperty("to"), ";");
            String subject = "INFO aggiornamentoPFGiornata GIORNATA " + idGiornata;
            try {
                emailService.sendMail(false, env.getProperty("spring.mail.secondary.username"), to, null, null, subject, html.toString(), "text/html", null);
            } catch (Exception e) {
                try {
                    emailService.sendMail(true, env.getProperty("spring.mail.primary.username"), to, null, null, subject, html.toString(), "text/html", null);
                } catch (Exception e2) {
                    log.error(e2.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in aggiornamentoPFGiornata", e);
        }
        log.info("END aggiornamentoPFGiornata");
    }

    private String[] normalizeVoto(String v, String ruolo, int countSv) {
        v = Utils.replaceString(v, ",", ".");
        if ("P".equals(ruolo)) {
            if (v.isEmpty() || "s.v.".equals(v) || "s,v,".equals(v) || "0".equals(v)) return new String[] {"6",""+countSv};
        } else {
            if (v.isEmpty() || "s.v.".equals(v) || "s,v,".equals(v) || "0".equals(v)) {
                countSv++;
                return new String[] {"0",""+countSv};
            }
        }
        return new String[] {v,""+countSv};
    }

    private long toVoto(String v) {
        return new BigDecimal(v).multiply(new BigDecimal(Costants.DIVISORE_100)).longValue();
    }

    private long getNewVoto(String g, String cs, String ts) {
        BigDecimal media = new BigDecimal(g).add(new BigDecimal(cs)).add(new BigDecimal(ts))
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        return media.multiply(new BigDecimal(Costants.DIVISORE_100)).longValue();
    }

    public void seiPolitico(Integer giornata, FcSquadra squadra) {
        FcGiornataInfo info = new FcGiornataInfo();
        info.setCodiceGiornata(giornata);
        List<FcPagelle> list = pagelleService.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(info);
        for (FcPagelle p : list) {
            if (squadra.getIdSquadra() == p.getFcGiocatore().getFcSquadra().getIdSquadra()) {
                String sql = "UPDATE fc_pagelle SET voto_giocatore=600,g=0,cs=0,ts=0,goal_realizzato=0,goal_subito=0,"
                        + "ammonizione=0,espulsione=0,rigore_segnato=0,rigore_fallito=0,rigore_parato=0,autorete=0,assist=0 "
                        + "WHERE ID_GIOCATORE=" + p.getFcGiocatore().getIdGiocatore() + " AND ID_GIORNATA=" + giornata;
                jdbcTemplate.execute(sql);
                jdbcTemplate.execute("UPDATE fc_giornata_dett SET VOTO=600 WHERE ID_GIOCATORE="
                        + p.getFcGiocatore().getIdGiocatore() + " AND ID_GIORNATA=" + giornata);
            }
        }
    }

    public void checkSeiPolitico(Integer giornata) {
        for (FcSquadra s : squadraService.findAll()) {
            String sql = "select COUNT(p.ID_GIOCATORE) from fc_pagelle p, fc_giocatore g "
                    + "where g.id_giocatore=p.id_giocatore and p.id_giornata=" + giornata
                    + " and g.id_squadra=" + s.getIdSquadra()
                    + " and goal_realizzato=0 and goal_subito=0 and ammonizione=0 and espulsione=0 "
                    + "and rigore_segnato=0 and rigore_fallito=0 and rigore_parato=0 and autorete=0 and assist=0 "
                    + "and (ts=600 or ts=0) and (cs=600 or cs=0) and (g=601 or g=604 or g=0)";
            Boolean b = jdbcTemplate.query(sql, rs -> rs.next() && rs.getInt(1) > 20);
            if (Boolean.TRUE.equals(b)) {
                seiPolitico(giornata, s);
            }
        }
    }

    public void aggiornaVotiGiocatori(int giornata, int forzaVotoGiocatore, boolean bRoundVoto) {
        FcGiornataInfo info = new FcGiornataInfo();
        info.setCodiceGiornata(giornata);
        for (FcPagelle p : pagelleService.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(info)) {
            int voto = Utils.buildVoto(p, bRoundVoto);
            if (forzaVotoGiocatore == 0) voto = forzaVotoGiocatore;
            jdbcTemplate.execute("UPDATE fc_giornata_dett SET VOTO=" + voto
                    + " WHERE ID_GIOCATORE=" + p.getFcGiocatore().getIdGiocatore()
                    + " AND ID_GIORNATA=" + p.getFcGiornataInfo().getCodiceGiornata());
        }
    }
}
