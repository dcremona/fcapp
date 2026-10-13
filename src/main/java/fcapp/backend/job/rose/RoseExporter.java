package fcapp.backend.job.rose;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.CampionatoService;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.SquadraService;
import fcapp.utils.Utils;

/**
 * Responsabilità unica: esportazione rose (fc_exp_rosea / free player).
 */
@Service
public class RoseExporter {

    private static final Logger log = LoggerFactory.getLogger(RoseExporter.class);

    @Autowired private CampionatoService campionatoService;
    @Autowired private SquadraService squadraService;
    @Autowired private FormazioneService formazioneService;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private JdbcTemplate jdbcTemplate;

    public void executeUpdateDbFcExpRoseA(boolean freePlayer, Integer idCampionato) {
        log.info("START executeUpdateDbFcExpRoseA");

        FcCampionato campionato = campionatoService.findByIdCampionato(idCampionato);
        String table = freePlayer ? "fc_exp_free_pl" : "fc_exp_rosea";

        jdbcTemplate.update("delete from " + table);

        List<FcSquadra> ls = squadraService.findAll();
        int numRighe = 81;
        if (ls.size() > 20) numRighe = 121;
        if (ls.size() > 30) numRighe = 161;

        for (int i = 1; i <= numRighe; i++) {
            StringBuilder col = new StringBuilder();
            StringBuilder val = new StringBuilder();
            for (int c = 1; c <= 10; c++) {
                col.append("S").append(c).append(",R").append(c).append(",Q").append(c).append(",");
                val.append("null,null,null,");
            }
            col = new StringBuilder(col.substring(0, col.length() - 1));
            val = new StringBuilder(val.substring(0, val.length() - 1));

            String update = "insert into " + table + " (id," + col + ") values (" + i + "," + val + ")";
            jdbcTemplate.update(update);
        }

        int i = 0;
        for (FcSquadra s : ls) {
            int c = s.getIdSquadra();
            if (i > 9 && i < 20) c = c - 10;
            else if (i > 19 && i < 30) c = c - 20;
            else if (i > 29 && i < 40) c = c - 30;

            String id = "1";
            if (i > 9) id = "41";
            if (i > 19) id = "81";
            if (i > 29) id = "121";

            String update = "update " + table + " set S" + c + "='" + s.getNomeSquadra()
                    + "', R" + c + "='R', Q" + c + "='Q' WHERE ID=" + id;
            jdbcTemplate.update(update);

            List<FcGiocatore> giocatores;
            if (freePlayer) {
                List<FcFormazione> allFormaz = formazioneService.findByFcCampionato(campionato);
                List<Integer> listNotIn = new ArrayList<>();
                for (FcFormazione f : allFormaz) {
                    if (f.getFcGiocatore() != null) {
                        listNotIn.add(f.getFcGiocatore().getIdGiocatore());
                    }
                }
                giocatores = giocatoreService
                        .findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(true, s, listNotIn);
            } else {
                giocatores = giocatoreService.findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(true, s);
            }

            int key = 2;
            if (i > 9 && i < 20) key = 42;
            else if (i > 19 && i < 30) key = 82;
            else if (i > 29 && i < 40) key = 122;

            for (int i2 = 0; i2 < 40; i2++) {
                String cognGiocatore = "";
                String ruolo = "";
                String sQuot = "";
                if (i2 < giocatores.size()) {
                    FcGiocatore g = giocatores.get(i2);
                    cognGiocatore = Utils.replaceString(g.getCognGiocatore(), "'", "''");
                    ruolo = g.getFcRuolo().getIdRuolo();
                    sQuot = g.getQuotazione().toString();
                }

                update = "update " + table + " set S" + c + "='" + cognGiocatore
                        + "', R" + c + "='" + ruolo + "', Q" + c + "='" + sQuot + "' WHERE ID=" + key;
                jdbcTemplate.update(update);
                key++;
            }
            i++;
        }
        log.info("END executeUpdateDbFcExpRoseA");
    }
}
