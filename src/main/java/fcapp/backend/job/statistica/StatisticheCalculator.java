package fcapp.backend.job.statistica;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcStatistiche;
import fcapp.backend.service.FormazioneService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.PagelleService;
import fcapp.backend.service.StatisticheService;
import fcapp.utils.Costants;

/**
 * Responsabilità unica: calcolo e salvataggio statistiche stagionali.
 */
@Service
public class StatisticheCalculator {

    private static final Logger log = LoggerFactory.getLogger(StatisticheCalculator.class);

    @Autowired private PagelleService pagelleService;
    @Autowired private GiocatoreService giocatoreService;
    @Autowired private FormazioneService formazioneService;
    @Autowired private StatisticheService statisticheService;

    public void statistiche(FcCampionato campionato) {
        log.info("START statistiche");
        List<FcPagelle> all = pagelleService.findAll();
        if (all.isEmpty()) return;

        int appoId = all.get(0).getFcGiocatore().getIdGiocatore();
        int voto = 0, fanta = 0, goalF = 0, goalS = 0, amm = 0, esp = 0, rigF = 0, rigS = 0, assist = 0, giocate = 0;

        for (FcPagelle p : all) {
            int id = p.getFcGiocatore().getIdGiocatore();
            if (id == appoId) {
                if (p.getVotoGiocatore() > 0) {
                    voto += p.getVotoGiocatore();
                    fanta += buildFantaMedia(p);
                    goalF += p.getGoalRealizzato();
                    goalS += p.getGoalSubito();
                    amm += p.getAmmonizione();
                    esp += p.getEspulsione();
                    rigF += p.getRigoreFallito();
                    rigS += p.getRigoreSegnato();
                    assist += p.getAssist();
                    giocate++;
                }
            } else {
                saveStat(appoId, campionato, voto, fanta, goalF, goalS, amm, esp, rigF, rigS, assist, giocate);
                appoId = id;
                voto = p.getVotoGiocatore();
                fanta = buildFantaMedia(p);
                goalF = p.getGoalRealizzato();
                goalS = p.getGoalSubito();
                amm = p.getAmmonizione();
                esp = p.getEspulsione();
                rigF = p.getRigoreFallito();
                rigS = p.getRigoreSegnato();
                assist = p.getAssist();
                giocate = p.getVotoGiocatore() > 0 ? 1 : 0;
            }
        }
        saveStat(appoId, campionato, voto, fanta, goalF, goalS, amm, esp, rigF, rigS, assist, giocate);
        log.info("END statistiche");
    }

    private void saveStat(int idGiocatore, FcCampionato campionato, int voto, int fanta,
                          int goalF, int goalS, int amm, int esp, int rigF, int rigS, int assist, int giocate) {
        FcGiocatore g = giocatoreService.findByIdGiocatore(idGiocatore);
        FcStatistiche s = new FcStatistiche();
        s.setIdGiocatore(g.getIdGiocatore());
        s.setCognGiocatore(g.getCognGiocatore());
        s.setIdRuolo(g.getFcRuolo().getIdRuolo());
        s.setNomeSquadra(g.getFcSquadra().getNomeSquadra());

        List<FcFormazione> form = formazioneService.findByFcCampionatoAndFcGiocatore(campionato, g);
        s.setProprietario(form.isEmpty() ? "" : form.get(0).getFcAttore().getDescAttore());

        s.setAmmonizione(amm);
        s.setAssist(assist);
        s.setEspulsione(esp);
        s.setGiocate(giocate);
        s.setGoalFatto(goalF);
        s.setGoalSubito(goalS);
        s.setMediaVoto(giocate > 0 ? (double) voto / giocate : 0.0);
        s.setFantaMedia(giocate > 0 ? (double) fanta / giocate : 0.0);
        s.setRigoreSbagliato(rigF);
        s.setRigoreSegnato(rigS);
        s.setFlagAttivo(g.isFlagAttivo());
        statisticheService.save(s);
    }

    public int buildFantaMedia(FcPagelle p) {
        int v = p.getVotoGiocatore();
        if (v == 0) return 0;

        if (p.getGoalRealizzato() != 0) v += p.getGoalRealizzato() * 3 * Costants.DIVISORE_100;
        if (p.getGoalSubito() != 0) v -= p.getGoalSubito() * Costants.DIVISORE_100;
        if (p.getAmmonizione() != 0) v -= Costants.DIVISORE_100;
        if (p.getEspulsione() != 0) {
            if (p.getAmmonizione() != 0) v += Costants.DIVISORE_100;
            v -= 2 * Costants.DIVISORE_100;
        }
        if (p.getRigoreFallito() != 0) v -= p.getRigoreFallito() * 3 * Costants.DIVISORE_100;
        if (p.getRigoreParato() != 0) v += p.getRigoreParato() * 3 * Costants.DIVISORE_100;
        if (p.getAutorete() != 0) v -= p.getAutorete() * 2 * Costants.DIVISORE_100;
        if (p.getAssist() != 0) v += p.getAssist() * Costants.DIVISORE_100;

        if ("P".equals(p.getFcGiocatore().getFcRuolo().getIdRuolo())
                && p.getGoalSubito() == 0 && p.getEspulsione() == 0 && v != 0
                && p.getG() != null && p.getCs() != null && p.getTs() != null
                && p.getG() != 0 && p.getCs() != 0 && p.getTs() != 0) {
            v += Costants.DIVISORE_100;
        }
        return v;
    }
}
