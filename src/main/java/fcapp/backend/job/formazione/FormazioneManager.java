package fcapp.backend.job.formazione;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.service.FormazioneService;

/**
 * Responsabilità unica: gestione formazioni (442, reset, copia giornata precedente).
 */
@Service
public class FormazioneManager {

    @Autowired private FormazioneService formazioneService;
    @Autowired private JdbcTemplate jdbcTemplate;

    public void inserisciUltimaFormazione(int idAttore, int giornata) {
        int prev = giornata - 1;
        jdbcTemplate.update("delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore=" + idAttore);
        jdbcTemplate.update("delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore);

        jdbcTemplate.update("insert into fc_giornata_dett (ID_GIORNATA, ID_ATTORE, ID_GIOCATORE, ID_STATO_GIOCATORE, ORDINAMENTO, VOTO) "
                + "SELECT " + giornata + "," + idAttore + ",ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,0 from fc_giornata_dett "
                + "where id_giornata=" + prev + " and id_attore=" + idAttore);

        jdbcTemplate.update("insert into fc_giornata_dett_info (ID_GIORNATA, ID_ATTORE,FLAG_INVIO,DATA_INVIO) "
                + "select " + giornata + "," + idAttore + ",FLAG_INVIO,DATA_INVIO from fc_giornata_dett_info "
                + "where id_giornata=" + prev + " and id_attore=" + idAttore);
    }

    public void resetFormazione(int idAttore, int giornata) {
        jdbcTemplate.update("delete from fc_giornata_dett_info where id_giornata=" + giornata + " and id_attore=" + idAttore);
        jdbcTemplate.update("delete from fc_giornata_dett where id_giornata=" + giornata + " and id_attore=" + idAttore);
    }

    public void inserisciFormazione442(FcCampionato campionato, FcAttore attore, int giornata) {
        int idAttore = attore.getIdAttore();
        jdbcTemplate.update("DELETE FROM fc_giornata_dett WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore);

        List<FcFormazione> list = formazioneService.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);

        ArrayList<FcFormazione> titP = new ArrayList<>(), titD = new ArrayList<>(), titC = new ArrayList<>(), titA = new ArrayList<>();
        ArrayList<FcFormazione> risP = new ArrayList<>(), risD = new ArrayList<>(), risC = new ArrayList<>(), risA = new ArrayList<>();
        ArrayList<FcFormazione> tribuna = new ArrayList<>();

        for (FcFormazione f : list) {
            FcGiocatore g = f.getFcGiocatore();
            if (g == null) continue;
            String r = g.getFcRuolo().getIdRuolo();
            switch (r) {
                case "P" -> { if (titP.isEmpty()) titP.add(f); else if (risP.isEmpty()) risP.add(f); else tribuna.add(f); }
                case "D" -> { if (titD.size() < 4) titD.add(f); else if (risD.size() < 2) risD.add(f); else tribuna.add(f); }
                case "C" -> { if (titC.size() < 4) titC.add(f); else if (risC.size() < 2) risC.add(f); else tribuna.add(f); }
                case "A" -> { if (titA.size() < 2) titA.add(f); else if (risA.size() < 2) risA.add(f); else tribuna.add(f); }
            }
        }

        int ord = 1;
        ord = insertList(titP, "T", giornata, idAttore, ord);
        ord = insertList(titD, "T", giornata, idAttore, ord);
        ord = insertList(titC, "T", giornata, idAttore, ord);
        ord = insertList(titA, "T", giornata, idAttore, ord);
        ord = insertList(risP, "R", giornata, idAttore, ord);
        ord = insertList(risD, "R", giornata, idAttore, ord);
        ord = insertList(risC, "R", giornata, idAttore, ord);
        insertList(risA, "R", giornata, idAttore, ord);

        int ordTrib = 19;
        for (FcFormazione f : tribuna) {
            if (f.getFcGiocatore() == null) continue;
            insertOne(f.getFcGiocatore().getIdGiocatore(), "N", giornata, idAttore, ordTrib++);
        }

        jdbcTemplate.update("DELETE FROM fc_giornata_dett_info WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore);
        String dataora = jdbcTemplate.query("select sysdate() from dual", rs -> rs.next() ? rs.getString(1) : null);
        jdbcTemplate.update("INSERT INTO fc_giornata_dett_info (ID_GIORNATA,ID_ATTORE, FLAG_INVIO,DATA_INVIO) VALUES ("
                + giornata + "," + idAttore + ",1, '" + dataora + "')");
    }

    private int insertList(List<FcFormazione> list, String stato, int giornata, int idAttore, int startOrd) {
        int ord = startOrd;
        for (FcFormazione f : list) {
            insertOne(f.getFcGiocatore().getIdGiocatore(), stato, giornata, idAttore, ord++);
        }
        return ord;
    }

    private void insertOne(int idGiocatore, String stato, int giornata, int idAttore, int ord) {
        jdbcTemplate.update("INSERT INTO fc_giornata_dett (ID_GIORNATA,ID_ATTORE, ID_GIOCATORE,ID_STATO_GIOCATORE,ORDINAMENTO,VOTO) VALUES ("
                + giornata + "," + idAttore + "," + idGiocatore + ",'" + stato + "'," + ord + ",0)");
    }
}
