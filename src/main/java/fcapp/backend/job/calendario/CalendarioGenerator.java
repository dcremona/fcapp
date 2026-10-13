package fcapp.backend.job.calendario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataId;
import fcapp.backend.service.AttoreService;

/**
 * Responsabilità unica: generazione calendario campionato fantasy (andata/ritorno).
 */
@Service
public class CalendarioGenerator {

    @Autowired private AttoreService attoreService;
    @Autowired private JdbcTemplate jdbcTemplate;

    public void generaCalendario(FcCampionato campionato) {
        Integer[] squadre = new Integer[8];
        int[] solution = {1, 2, 3, 4, 5, 6, 7, 8};
        shuffleArray(solution);
        for (int i = 0; i < 8; i++) {
            squadre[i] = solution[i];
        }
        calendarNew(campionato, squadre);
    }

    private void shuffleArray(int[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = ThreadLocalRandom.current().nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private void calendarNew(FcCampionato campionato, Integer[] squadre) {
        int ggStart = 1;
        int ggEnd = 14;
        int incremento = 0;

        if (campionato.getIdCampionato() == 2) {
            ggStart = 20;
            ggEnd = 33;
            incremento = 19;
        }

        List<FcAttore> attori = attoreService.findAll();
        for (FcAttore attore : attori) {
            if (attore.getIdAttore() > 0 && attore.getIdAttore() < 9) {
                for (int gg = ggStart; gg <= ggEnd; gg++) {
                    jdbcTemplate.execute("DELETE FROM fc_giornata where id_attore_casa = " + attore.getIdAttore()
                            + " and id_giornata =" + gg);
                }
            }
        }

        List<FcGiornata> calend = new ArrayList<>();
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
            int idGiornata = g.getId().getIdGiornata();
            int idCasa = g.getFcAttoreByIdAttoreCasa().getIdAttore();
            int idFuori = g.getFcAttoreByIdAttoreFuori().getIdAttore();

            jdbcTemplate.execute("insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values ("
                    + idGiornata + "," + idCasa + "," + idFuori + ",0)");
            jdbcTemplate.execute("insert into fc_giornata (ID_GIORNATA,ID_ATTORE_CASA,ID_ATTORE_FUORI,ID_TIPO_GIORNATA) Values ("
                    + (idGiornata + 7) + "," + idFuori + "," + idCasa + ",0)");
        }
    }

    private FcGiornata buildPartita(Integer giornata, Integer idCasa, Integer idFuori) {
        FcGiornata partita = new FcGiornata();
        FcGiornataId pk = new FcGiornataId();
        pk.setIdGiornata(giornata);
        pk.setIdAttoreCasa(idCasa);
        partita.setId(pk);

        FcAttore casa = new FcAttore();
        casa.setIdAttore(idCasa);
        partita.setFcAttoreByIdAttoreCasa(casa);

        FcAttore fuori = new FcAttore();
        fuori.setIdAttore(idFuori);
        partita.setFcAttoreByIdAttoreFuori(fuori);

        return partita;
    }
}
