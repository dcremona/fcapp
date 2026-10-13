package fcapp.backend.job.scoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.data.entity.FcGiornataDett;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.service.AttoreService;
import fcapp.backend.service.GiornataDettService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.GiornataService;
import fcapp.backend.service.PagelleService;
import fcapp.utils.Buffer;
import fcapp.utils.Costants;
import fcapp.utils.Utils;

/**
 * Responsabilità unica: calcolo punteggio giornata (voti, cambi, bonus, gol).
 * Contiene la logica principale dell'algoritmo di fantacalcio.
 */
@Service
public class AlgoritmoService {

    private static final Logger log = LoggerFactory.getLogger(AlgoritmoService.class);
    private static final int MOLTIPLICATORE = 10_000;

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AttoreService attoreService;
    @Autowired private GiornataService giornataService;
    @Autowired private GiornataDettService giornataDettService;
    @Autowired private GiornataInfoService giornataInfoService;
    @Autowired private PagelleService pagelleService;
    @Autowired private ClassificaCalculator classificaCalculator;

    public void execute(Integer giornata, FcCampionato campionato, int forzaVotoGiocatore, boolean bRoundVoto) {
        log.info("START algoritmo giornata={}", giornata);

        int giornataFc = giornata > 19 ? giornata - 19 : giornata;
        Buffer bufBonus = loadBonus(giornataFc, campionato);

        FcGiornataInfo giornataInfo = giornataInfoService.findByCodiceGiornata(giornata);
        List<FcGiornata> partite = giornataService.findByFcGiornataInfo(giornataInfo);
        List<FcAttore> attori = attoreService.findByActive(true);

        for (FcAttore attore : attori) {
            if (attore.getIdAttore() < 1 || attore.getIdAttore() > 8) continue;
            processAttore(attore, giornata, giornataFc, giornataInfo, partite, campionato, forzaVotoGiocatore, bRoundVoto, bufBonus);
        }

        classificaCalculator.aggiornaGiornataRis(campionato);
        classificaCalculator.aggiornaClassifica1vsTutti(campionato, giornataInfo);
        classificaCalculator.aggiornaClassificaFinaleSeNecessario(giornataFc, giornata, campionato);

        log.info("END algoritmo");
    }

    private void processAttore(FcAttore attore, int giornata, int giornataFc,
                               FcGiornataInfo giornataInfo, List<FcGiornata> partite,
                               FcCampionato campionato, int forzaVoto, boolean roundVoto, Buffer bufBonus) {

        List<FcGiornataDett> dettagli = giornataDettService
                .findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);

        int idAttore = attore.getIdAttore();
        int somma = 0;
        int sommaTitolariRiserve = 0;
        int countDif = 0, countCen = 0, countAtt = 0;

        // Min voti per ruolo
        int votoMinPorTit = MOLTIPLICATORE, votoMinDifTit = MOLTIPLICATORE;
        int votoMinCenTit = MOLTIPLICATORE, votoMinAttTit = MOLTIPLICATORE;
        int votoMinPorRis = MOLTIPLICATORE, votoMinDifRis = MOLTIPLICATORE;
        int votoMinCenRis = MOLTIPLICATORE, votoMinAttRis = MOLTIPLICATORE;
        String idCognMinPorTit = "", idCognMinDifTit = "", idCognMinCenTit = "", idCognMinAttTit = "";
        String idCognMinPorRis = "", idCognMinDifRis = "", idCognMinCenRis = "", idCognMinAttRis = "";

        boolean flagChangeRis2Dif = validateCambioRiserva2(dettagli, "D");
        boolean flagChangeRis2Cen = validateCambioRiserva2(dettagli, "C");
        boolean flagChangeRis2Att = validateCambioRiserva2(dettagli, "A");

        String[] cambi = new String[7];
        for (int i = 0; i < 7; i++) cambi[i] = "";

        for (FcGiornataDett dett : dettagli) {
            FcGiocatore giocatore = dett.getFcGiocatore();
            FcPagelle pagelle = pagelleService.findByFcGiornataInfoAndFcGiocatore(giornataInfo, giocatore);

            int ordinamento = dett.getOrdinamento();
            String idGiocatore = "" + giocatore.getIdGiocatore();
            String idStato = dett.getFcStatoGiocatore().getIdStatoGiocatore();
            String idRuolo = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
            int espulso = pagelle.getEspulsione();

            int votoGiocatore = Utils.buildVoto(pagelle, roundVoto);
            if (forzaVoto == 0) votoGiocatore = forzaVoto;
            int votoNoPenalita = votoGiocatore;

            if (ordinamento > 18) {
                jdbcTemplate.update("UPDATE fc_giornata_dett SET FLAG_ATTIVO='N', VOTO=" + votoGiocatore
                        + " WHERE ID_GIOCATORE=" + idGiocatore + " AND ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore);
                continue;
            }

            boolean bSomma = true;
            switch (idRuolo) {
                case "P" -> {
                    if ("T".equals(idStato)) {
                        if (espulso == 0 && votoMinPorTit > votoGiocatore) {
                            votoMinPorTit = votoGiocatore;
                            idCognMinPorTit = idGiocatore;
                        }
                    } else if ("R".equals(idStato)) {
                        votoMinPorRis = votoGiocatore;
                        idCognMinPorRis = idGiocatore;
                    }
                }
                case "D" -> {
                    countDif++;
                    if ("T".equals(idStato)) {
                        if (espulso == 0 && votoMinDifTit > votoGiocatore) {
                            votoMinDifTit = votoGiocatore;
                            idCognMinDifTit = idGiocatore;
                        }
                    } else if ("R".equals(idStato)) {
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
                                votoGiocatore -= Costants.DIV_0_5;
                                votoMinDifRis = votoGiocatore;
                                idCognMinDifRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[4] = idGiocatore;
                            }
                        }
                    }
                }
                case "C" -> {
                    countCen++;
                    if ("T".equals(idStato)) {
                        if (espulso == 0 && votoMinCenTit > votoGiocatore) {
                            votoMinCenTit = votoGiocatore;
                            idCognMinCenTit = idGiocatore;
                        }
                    } else if ("R".equals(idStato)) {
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
                                votoGiocatore -= Costants.DIV_0_5;
                                votoMinCenRis = votoGiocatore;
                                idCognMinCenRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[5] = idGiocatore;
                            }
                        }
                    }
                }
                case "A" -> {
                    countAtt++;
                    if ("T".equals(idStato)) {
                        if (espulso == 0 && votoMinAttTit > votoGiocatore) {
                            votoMinAttTit = votoGiocatore;
                            idCognMinAttTit = idGiocatore;
                        }
                    } else if ("R".equals(idStato)) {
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
                                votoGiocatore -= Costants.DIV_0_5;
                                votoMinAttRis = votoGiocatore;
                                idCognMinAttRis = idGiocatore;
                            } else {
                                bSomma = false;
                                cambi[6] = idGiocatore;
                            }
                        }
                    }
                }
            }

            if (bSomma) somma += votoGiocatore;
            sommaTitolariRiserve += votoNoPenalita;

            jdbcTemplate.update("UPDATE fc_giornata_dett SET FLAG_ATTIVO='S', VOTO=" + votoGiocatore
                    + " WHERE ID_GIOCATORE=" + idGiocatore + " AND ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore);
        }

        // Scelta migliori cambi
        int diffPor = votoMinPorTit - votoMinPorRis + MOLTIPLICATORE;
        int diffDif = votoMinDifTit - votoMinDifRis + MOLTIPLICATORE;
        int diffCen = votoMinCenTit - votoMinCenRis + MOLTIPLICATORE;
        int diffAtt = votoMinAttTit - votoMinAttRis + MOLTIPLICATORE;

        Buffer b = new Buffer();
        b.addNew("@1" + votoMinPorTit + "@2" + votoMinPorRis + "@3" + diffPor + "@4" + idCognMinPorTit + "@5" + idCognMinPorRis);
        b.addNew("@1" + votoMinDifTit + "@2" + votoMinDifRis + "@3" + diffDif + "@4" + idCognMinDifTit + "@5" + idCognMinDifRis);
        b.addNew("@1" + votoMinCenTit + "@2" + votoMinCenRis + "@3" + diffCen + "@4" + idCognMinCenTit + "@5" + idCognMinCenRis);
        b.addNew("@1" + votoMinAttTit + "@2" + votoMinAttRis + "@3" + diffAtt + "@4" + idCognMinAttTit + "@5" + idCognMinAttRis);
        b.sort(3);

        for (int x = 0; x < b.getRowCount(); x++) {
            if (x == 0 || x == 1) {
                if (b.getFieldByInt(3) < MOLTIPLICATORE) {
                    somma -= b.getFieldByInt(1);
                    cambi[x] = b.getField(4);
                } else {
                    somma -= b.getFieldByInt(2);
                    cambi[x] = b.getField(5);
                }
            } else {
                somma -= b.getFieldByInt(2);
                cambi[x] = b.getField(5);
            }
            b.moveNext();
        }

        for (String element : cambi) {
            if (element == null || element.isEmpty()) continue;
            jdbcTemplate.update("UPDATE fc_giornata_dett SET FLAG_ATTIVO='N' WHERE ID_GIOCATORE=" + element
                    + " AND ID_GIORNATA=" + giornata + " AND ID_ATTORE=" + idAttore);
        }

        // Schema bonus/malus
        countDif -= 2; countCen -= 2; countAtt -= 2;
        if (countDif == 5 && countCen == 4 && countAtt == 1) {
            somma += Costants.DIV_2_0;
            sommaTitolariRiserve += Costants.DIV_2_0;
        } else if (countDif == 5 && countCen == 3 && countAtt == 2) {
            somma += Costants.DIV_1_0;
            sommaTitolariRiserve += Costants.DIV_1_0;
        } else if (countDif == 4 && countCen == 5 && countAtt == 1) {
            somma += Costants.DIV_1_0;
            sommaTitolariRiserve += Costants.DIV_1_0;
        } else if (countDif == 4 && countCen == 3 && countAtt == 3) {
            somma -= Costants.DIV_1_0;
            sommaTitolariRiserve -= Costants.DIV_1_0;
        } else if (countDif == 3 && countCen == 4 && countAtt == 3) {
            somma -= Costants.DIV_2_0;
            sommaTitolariRiserve -= Costants.DIV_2_0;
        }

        // Bonus casa
        if (giornataFc < 15) {
            for (FcGiornata g : partite) {
                if (idAttore == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {
                    somma += Costants.DIV_1_5;
                    sommaTitolariRiserve += Costants.DIV_1_5;
                    break;
                }
            }
        }

        // Bonus playoff
        if (giornataFc == 15 || giornataFc == 17) {
            int idx = bufBonus.findFirst("" + idAttore, 1, false);
            if (idx != -1) somma += bufBonus.getFieldByInt(2);
        }

        int totGoal = getTotGoal(somma);
        int totGoalTVsTutti = getTotGoal(sommaTitolariRiserve);

        // Persist
        jdbcTemplate.update("DELETE FROM fc_classifica_tot_pt WHERE ID_CAMPIONATO=" + campionato.getIdCampionato()
                + " AND ID_ATTORE=" + idAttore + " AND ID_GIORNATA=" + giornata);
        jdbcTemplate.update("INSERT INTO fc_classifica_tot_pt (ID_CAMPIONATO,ID_ATTORE,ID_GIORNATA,TOT_PT,TOT_PT_OLD,GOAL) VALUES ("
                + campionato.getIdCampionato() + "," + idAttore + "," + giornata + ","
                + sommaTitolariRiserve + "," + somma + "," + totGoalTVsTutti + ")");

        for (FcGiornata g : partite) {
            if (idAttore == g.getFcAttoreByIdAttoreCasa().getIdAttore()) {
                jdbcTemplate.update("UPDATE fc_giornata SET TOT_CASA=" + somma + ", GOL_CASA=" + totGoal
                        + " WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE_CASA=" + idAttore);
            } else if (idAttore == g.getFcAttoreByIdAttoreFuori().getIdAttore()) {
                jdbcTemplate.update("UPDATE fc_giornata SET TOT_FUORI=" + somma + ", GOL_FUORI=" + totGoal
                        + " WHERE ID_GIORNATA=" + giornata + " AND ID_ATTORE_FUORI=" + idAttore);
            }
        }
    }

    private boolean validateCambioRiserva2(List<FcGiornataDett> lGiocatori, String ruolo) {
        boolean flagTitolare = false;
        boolean flagRiserva = false;
        for (FcGiornataDett dett : lGiocatori) {
            String idRuolo = dett.getFcGiocatore().getFcRuolo().getIdRuolo();
            if (!ruolo.equals(idRuolo)) continue;
            int voto = dett.getFcPagelle().getVotoGiocatore();
            int ord = dett.getOrdinamento();
            if (ord < 12 && voto == 0) flagTitolare = true;
            if ("D".equals(idRuolo)) {
                if (ord == 13) flagRiserva = (voto == 0);
                else if (ord == 14 && voto == 0) flagRiserva = false;
            } else if ("C".equals(idRuolo)) {
                if (ord == 15) flagRiserva = (voto == 0);
                else if (ord == 16 && voto == 0) flagRiserva = false;
            } else if ("A".equals(idRuolo)) {
                if (ord == 17) flagRiserva = (voto == 0);
                else if (ord == 18 && voto == 0) flagRiserva = false;
            }
        }
        return flagTitolare && flagRiserva;
    }

    private int getTotGoal(int somma) {
        final int increment = 400;
        int start = 6600, end = 6999;
        for (int goal = 1; goal <= 13; goal++) {
            if (somma >= start && somma <= end) return goal;
            start += increment;
            end += increment;
        }
        return 0;
    }

    private Buffer loadBonus(int giornataFc, FcCampionato campionato) {
        Buffer buf = new Buffer();
        if (giornataFc == 15) {
            // bonus ottavi
            String sql = "SELECT ID_ATTORE FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato()
                    + " AND ID_POSIZ<5 ORDER BY ID_POSIZ";
            jdbcTemplate.query(sql, rs -> {
                int i = 0;
                while (rs.next()) {
                    String bonus = switch (i) {
                        case 0 -> "800";
                        case 1 -> "600";
                        case 2 -> "400";
                        case 3 -> "200";
                        default -> "0";
                    };
                    buf.addNew("@1" + rs.getString(1) + "@2" + bonus + "@3" + (Integer.parseInt(bonus) / 100));
                    i++;
                }
                return null;
            });
        } else if (giornataFc == 17) {
            String sql = "SELECT ID_ATTORE,VINTE FROM fc_classifica WHERE ID_CAMPIONATO=" + campionato.getIdCampionato();
            jdbcTemplate.query(sql, rs -> {
                while (rs.next()) {
                    int bonus2 = rs.getInt(2);
                    int bonus = bonus2 * Costants.DIVISORE_100;
                    buf.addNew("@1" + rs.getString(1) + "@2" + bonus + "@3" + bonus2);
                }
                return null;
            });
        }
        return buf;
    }

    public void aggiornaTotRosa(String idCampionato, int giornata) {
        String sql = "select pt.id_giornata,pt.id_attore, sum(pt.voto) as tot25 "
                + "from fc_giornata_dett pt where pt.id_giornata>= " + giornata
                + " group by pt.id_giornata,pt.id_attore order by 1,3 desc";
        jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                jdbcTemplate.execute("UPDATE fc_classifica_tot_pt SET tot_pt_rosa=" + rs.getInt(3)
                        + " WHERE id_attore=" + rs.getInt(2) + " AND id_giornata=" + rs.getInt(1));
            }
            return null;
        });

        for (int attore = 1; attore < 9; attore++) {
            String s = "SELECT SUM(TOT_PT), SUM(TOT_PT_OLD), SUM(TOT_PT_ROSA), ID_ATTORE FROM fc_classifica_tot_pt "
                    + "WHERE ID_CAMPIONATO=" + idCampionato + " AND ID_ATTORE=" + attore;
            jdbcTemplate.query(s, rs -> {
                if (rs.next()) {
                    jdbcTemplate.update("UPDATE fc_classifica SET TOT_PUNTI=" + rs.getString(1)
                            + ", TOT_PUNTI_OLD=" + rs.getString(2)
                            + ", TOT_PUNTI_ROSA=" + rs.getString(3)
                            + " WHERE ID_CAMPIONATO=" + idCampionato + " AND ID_ATTORE=" + rs.getString(4));
                }
                return null;
            });
        }
    }

    public void aggiornaScore(int giornata, String colPt, String colScore) {
        Map<String, String> map = new HashMap<>();
        map.put("7", "25"); map.put("6", "18"); map.put("5", "15"); map.put("4", "12");
        map.put("3", "10"); map.put("2", "8"); map.put("1", "6"); map.put("0", "4");

        String sql = "select pt.id_giornata,pt.id_attore, sum(pt." + colPt + ") as score "
                + "from fc_classifica_tot_pt pt where pt.id_giornata>= " + giornata
                + " group by pt.id_giornata,pt.id_attore order by 1,3 desc";
        jdbcTemplate.query(sql, rs -> {
            int conta = 7;
            while (rs.next()) {
                String score = "score_grand_prix".equals(colScore) ? map.get("" + conta) : "" + conta;
                jdbcTemplate.execute("UPDATE fc_classifica_tot_pt SET " + colScore + "=" + score
                        + " WHERE id_attore=" + rs.getInt(2) + " AND id_giornata=" + rs.getInt(1));
                conta--;
                if (conta == -1) conta = 7;
            }
            return null;
        });
    }
}
