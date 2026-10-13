package fcapp.backend.job;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.job.calendario.CalendarioCompetizioneImporter;
import fcapp.backend.job.calendario.CalendarioGenerator;
import fcapp.backend.job.formazione.FormazioneManager;
import fcapp.backend.job.pagelle.PagelleUpdater;
import fcapp.backend.job.player.GiocatoreInitializer;
import fcapp.backend.job.rose.RoseExporter;
import fcapp.backend.job.scoring.AlgoritmoService;
import fcapp.backend.job.statistica.StatisticheCalculator;

/**
 * Facade / Orchestratore.
 * Delega tutta la logica di business alle classi specializzate (SRP).
 */
@Controller
public class JobProcessGiornataNew {

    @Autowired private GiocatoreInitializer giocatoreInitializer;
    @Autowired private PagelleUpdater pagelleUpdater;
    @Autowired private FormazioneManager formazioneManager;
    @Autowired private StatisticheCalculator statisticheCalculator;
    @Autowired private AlgoritmoService algoritmoService;
    @Autowired private CalendarioGenerator calendarioGenerator;
    @Autowired private CalendarioCompetizioneImporter calendarioCompetizioneImporter;
    @Autowired private RoseExporter roseExporter;

    // ==================== GIOCATORE ====================
    public HashMap<Object, Object> initDbGiocatori(String httpUrlImg, String imgPath, String fileName,
                                                  boolean updateQuotazioni, boolean updateImg, String percentuale) throws Exception {
        return giocatoreInitializer.initDbGiocatori(httpUrlImg, imgPath, fileName, updateQuotazioni, updateImg, percentuale);
    }

    public void updateImgGiocatore(InputStream is) throws Exception {
        giocatoreInitializer.updateImgGiocatore(is);
    }

    public void initDbGiornataGiocatore(FcGiornataInfo giornataInfo, String fileName,
                                        boolean bSqualificato, boolean bInfortunato) throws Exception {
        giocatoreInitializer.initDbGiornataGiocatore(giornataInfo, fileName, bSqualificato, bInfortunato);
    }

    public void initDbProbabili(String fileName) throws Exception {
        giocatoreInitializer.initDbProbabili(fileName);
    }

    public void initDbProbabiliFantaGazzetta(String fileName) throws Exception {
        giocatoreInitializer.initDbProbabiliFantaGazzetta(fileName);
    }

    public void initDbSqualificatiInfortunatiFantaGazzetta(FcGiornataInfo giornataInfo, String fileName) throws Exception {
        giocatoreInitializer.initDbSqualificatiInfortunatiFantaGazzetta(giornataInfo, fileName);
    }

    // ==================== PAGELLE ====================
    public void initPagelle(Integer giornata) {
        pagelleUpdater.initPagelle(giornata);
    }

    public void aggiornamentoPFGiornata(Properties p, String fileName, String idGiornata) {
        pagelleUpdater.aggiornamentoPFGiornata(p, fileName, idGiornata);
    }

    public void seiPolitico(Integer giornata, FcSquadra squadra) {
        pagelleUpdater.seiPolitico(giornata, squadra);
    }

    public void checkSeiPolitico(Integer giornata) {
        pagelleUpdater.checkSeiPolitico(giornata);
    }

    public void aggiornaVotiGiocatori(int giornata, int forzaVotoGiocatore, boolean bRoundVoto) {
        pagelleUpdater.aggiornaVotiGiocatori(giornata, forzaVotoGiocatore, bRoundVoto);
    }

    // ==================== FORMAZIONE ====================
    public void inserisciUltimaFormazione(int idAttore, int giornata) {
        formazioneManager.inserisciUltimaFormazione(idAttore, giornata);
    }

    public void resetFormazione(int idAttore, int giornata) {
        formazioneManager.resetFormazione(idAttore, giornata);
    }

    public void inserisciFormazione442(FcCampionato campionato, FcAttore attore, int giornata) {
        formazioneManager.inserisciFormazione442(campionato, attore, giornata);
    }

    // ==================== STATISTICHE ====================
    public void statistiche(FcCampionato campionato) {
        statisticheCalculator.statistiche(campionato);
    }

    // ==================== ALGORITMO ====================
    public void algoritmo(Integer giornata, FcCampionato campionato, int forzaVotoGiocatore, boolean bRoundVoto) {
        algoritmoService.execute(giornata, campionato, forzaVotoGiocatore, bRoundVoto);
    }

    public void aggiornaTotRosa(String idCampionato, int giornata) {
        algoritmoService.aggiornaTotRosa(idCampionato, giornata);
    }

    public void aggiornaScore(int giornata, String colPt, String colScore) {
        algoritmoService.aggiornaScore(giornata, colPt, colScore);
    }

    // ==================== CALENDARIO ====================
    public void generaCalendario(FcCampionato campionato) {
        calendarioGenerator.generaCalendario(campionato);
    }

    public void initDbCalendarioCompetizione(String fileName) throws Exception {
        calendarioCompetizioneImporter.initDbCalendarioCompetizione(fileName);
    }

    public void updateCalendarioTim(String fileName, int idGiornata) throws Exception {
        calendarioCompetizioneImporter.updateCalendarioTim(fileName, idGiornata);
    }

    public void updateCalendarioMondiale(String fileName, int idGiornata) throws Exception {
        calendarioCompetizioneImporter.updateCalendarioMondiale(fileName, idGiornata);
    }

    public void insertCalendarioTim(String fileName, int idGiornata) throws Exception {
        calendarioCompetizioneImporter.insertCalendarioTim(fileName, idGiornata);
    }

    public void deleteAllCalendarioTim() {
        calendarioCompetizioneImporter.deleteAll();
    }

    // ==================== ROSE ====================
    public void executeUpdateDbFcExpRoseA(boolean freePlayer, Integer idCampionato) {
        roseExporter.executeUpdateDbFcExpRoseA(freePlayer, idCampionato);
    }
}
