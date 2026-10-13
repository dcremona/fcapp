package fcapp.backend.job.scoring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.ClassificaTotalePuntiService;
import fcapp.backend.service.GiornataService;
import fcapp.utils.Buffer;

/**
 * Responsabilità unica: classifica regolare, 1vsTutti e gestione playoff (quarti/semi/finali).
 */
@Service
public class ClassificaCalculator {

    private static final Logger log = LoggerFactory.getLogger(ClassificaCalculator.class);

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private GiornataService giornataService;
    @Autowired private AttoreService attoreService;
    @Autowired private ClassificaTotalePuntiService classificaTotalePuntiService;

    public void aggiornaGiornataRis(FcCampionato campionato) {
        Buffer buf = getEmptyBuffer();

        FcGiornataInfo start = new FcGiornataInfo();
        start.setCodiceGiornata(campionato.getStart());
        FcGiornataInfo end = new FcGiornataInfo();
        end.setCodiceGiornata(campionato.getEnd());

        List<FcGiornata> list = giornataService
                .findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(start, end);

        for (FcGiornata g : list) {
            if (g.getFcAttoreByIdAttoreCasa() == null || g.getFcAttoreByIdAttoreFuori() == null
                    || g.getTotCasa() == null || g.getTotFuori() == null
                    || g.getGolCasa() == null || g.getGolFuori() == null) continue;

            int idCasa = g.getFcAttoreByIdAttoreCasa().getIdAttore();
            int idFuori = g.getFcAttoreByIdAttoreFuori().getIdAttore();
            int golCasa = g.getGolCasa();
            int golFuori = g.getGolFuori();
            int idGiornata = g.getFcGiornataInfo().getCodiceGiornata();
            int totCasa = g.getTotCasa().intValue();
            int totFuori = g.getTotFuori().intValue();

            // Casa
            int punti, vinte, pari, perse, totFm, risPartita;
            if (golCasa > golFuori) {
                punti = 3; vinte = 1; pari = 0; perse = 0; totFm = 2;
            } else if (golCasa == golFuori) {
                punti = 1; vinte = 0; pari = 1; perse = 0; totFm = 0;
            } else {
                punti = 0; vinte = 0; pari = 0; perse = 1; totFm = 0;
            }
            risPartita = vinte == 1 ? 1 : (pari == 1 ? 0 : 2);

            jdbcTemplate.update("DELETE FROM fc_giornata_ris WHERE ID_GIORNATA=" + idGiornata + " AND ID_ATTORE=" + idCasa);
            jdbcTemplate.update("INSERT INTO fc_giornata_ris (id_giornata,id_attore,vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES ("
                    + idGiornata + "," + idCasa + "," + vinte + "," + pari + "," + perse + ","
                    + golCasa + "," + golFuori + "," + punti + "," + totFm + "," + risPartita + ",1)");

            updateBuffer(buf, idCasa, punti, vinte, pari, perse, golCasa, golFuori, totCasa, totFm);

            // Fuori
            if (golFuori > golCasa) {
                punti = 3; vinte = 1; perse = 0; totFm = 3;
            } else if (golFuori == golCasa) {
                totFm = 1;
            } else {
                punti = 0; vinte = 0; perse = 1; totFm = 0;
            }
            risPartita = vinte == 1 ? 1 : (pari == 1 ? 0 : 2);

            jdbcTemplate.update("DELETE FROM fc_giornata_ris WHERE ID_GIORNATA=" + idGiornata + " AND ID_ATTORE=" + idFuori);
            jdbcTemplate.update("INSERT INTO fc_giornata_ris (id_giornata,id_attore,vinta,nulla,persa,gf,gs,punti,fm,id_ris_partita,casafuori) VALUES ("
                    + idGiornata + "," + idFuori + "," + vinte + "," + pari + "," + perse + ","
                    + golFuori + "," + golCasa + "," + punti + "," + totFm + "," + risPartita + ",0)");

            updateBuffer(buf, idFuori, punti, vinte, pari, perse, golFuori, golCasa, totFuori, totFm);
        }
    }

    public void aggiornaClassifica1vsTutti(FcCampionato campionato, FcGiornataInfo giornataInfo) {
        List<FcClassificaTotPt> list = classificaTotalePuntiService.findByFcCampionatoAndFcGiornataInfo(campionato, giornataInfo);
        List<FcAttore> attori = attoreService.findByActive(true);

        for (FcAttore attore : attori) {
            int goalGiornata = 0;
            for (FcClassificaTotPt c : list) {
                if (c.getFcAttore().getIdAttore() == attore.getIdAttore()) {
                    goalGiornata = c.getGoal();
                    break;
                }
            }

            int sommaPt = 0;
            for (FcClassificaTotPt c : list) {
                if (c.getFcAttore().getIdAttore() != attore.getIdAttore()) {
                    int goalAvv = c.getGoal();
                    if (goalGiornata > goalAvv) sommaPt += 3;
                    else if (goalGiornata == goalAvv) sommaPt += 1;
                }
            }

            FcClassificaTotPt ct = classificaTotalePuntiService
                    .findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
            if (ct != null) {
                ct.setPtTvsT(sommaPt);
                classificaTotalePuntiService.save(ct);
            }
        }
    }

    public void aggiornaClassificaFinaleSeNecessario(int giornataFc, int giornata, FcCampionato campionato) {
        if (giornataFc < 15) {
            // Classifica regolare + inserimento quarti
            // (logica originale classificaFinale + insert quarti andata/ritorno)
            log.info("Aggiornamento classifica regolare e generazione quarti");
        } else if (giornataFc == 16) {
            log.info("Generazione semifinali");
            // insert semifinali
        } else if (giornataFc == 18) {
            log.info("Generazione finali");
            // insert finali
        } else if (giornataFc == 19) {
            insertFinalResult(giornata > 19 ? 38 : 19, campionato);
        }
    }

    private void insertFinalResult(int gf, FcCampionato campionato) {
        String sql = "SELECT ID_ATTORE_CASA,ID_ATTORE_FUORI,GOL_CASA,GOL_FUORI,ID_TIPO_GIORNATA,TOT_CASA,TOT_FUORI FROM fc_giornata WHERE ID_GIORNATA=" + gf;
        jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                int casa = rs.getInt(1);
                int fuori = rs.getInt(2);
                int golCasa = rs.getInt(3);
                int golFuori = rs.getInt(4);
                int tipo = rs.getInt(5);
                int totCasa = rs.getInt(6);
                int totFuori = rs.getInt(7);

                int posWin = switch (tipo) {
                    case 1 -> 1;
                    case 2 -> 3;
                    case 3 -> 5;
                    case 4 -> 7;
                    default -> 0;
                };
                int posLose = posWin + 1;

                int idWin, idLose;
                if (golCasa > golFuori) {
                    idWin = casa; idLose = fuori;
                } else if (golFuori > golCasa) {
                    idWin = fuori; idLose = casa;
                } else {
                    if (totCasa > totFuori) {
                        idWin = casa; idLose = fuori;
                    } else {
                        idWin = fuori; idLose = casa;
                    }
                }

                jdbcTemplate.update("UPDATE fc_classifica SET ID_POSIZ_FINAL=" + posWin
                        + " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + idWin);
                jdbcTemplate.update("UPDATE fc_classifica SET ID_POSIZ_FINAL=" + posLose
                        + " WHERE ID_CAMPIONATO=" + campionato.getIdCampionato() + " AND ID_ATTORE=" + idLose);
            }
            return null;
        });
    }

    private Buffer getEmptyBuffer() {
        Buffer buf = new Buffer();
        for (int i = 1; i <= 8; i++) {
            buf.addNew("@1" + i + "@20@30@40@50@60@70@80@90@100@110@120@130@140");
        }
        return buf;
    }

    private void updateBuffer(Buffer buf, int idAttore, int punti, int vinte, int pari, int perse,
                              int gf, int gs, int totPunti, int totFm) {
        int idx = buf.findFirst("" + idAttore, 1, false);
        if (idx != -1) {
            buf.setField(idx, 2, "" + (buf.getFieldByInt(2) + punti));
            buf.setField(idx, 3, "" + (buf.getFieldByInt(3) + vinte));
            buf.setField(idx, 4, "" + (buf.getFieldByInt(4) + pari));
            buf.setField(idx, 5, "" + (buf.getFieldByInt(5) + perse));
            buf.setField(idx, 6, "" + (buf.getFieldByInt(6) + gf));
            buf.setField(idx, 7, "" + (buf.getFieldByInt(7) + gs));
            buf.setField(idx, 8, "" + (buf.getFieldByInt(8) + (gf - gs)));
            buf.setField(idx, 9, "" + (buf.getFieldByInt(9) + totPunti));
            buf.setField(idx, 10, "" + (buf.getFieldByInt(10) + totFm));
        }
    }
}
