package fcweb.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;

@Service
public class PagelleService {

    private final PagelleRepository pagelleRepository;

    @Autowired
    public PagelleService(PagelleRepository pagelleRepository) {
        this.pagelleRepository = pagelleRepository;
    }

    public List<FcPagelle> findAll() {
        List<FcPagelle> l = (List<FcPagelle>) pagelleRepository.findAll();
        return l;
    }

    public FcPagelle findCurrentGiornata() {
        FcPagelle currentGiornata = pagelleRepository.findTopByOrderByFcGiornataInfoDesc();
        return currentGiornata;
    }

    public List<FcPagelle> findByCustonm(FcGiornataInfo giornataInfo, FcGiocatore giocatore) {

        List<FcPagelle> l = null;
        if (giornataInfo == null && giocatore == null) {
            l = (List<FcPagelle>) pagelleRepository.findAll();
        } else if (giornataInfo != null && giocatore == null) {
            l = pagelleRepository
                    .findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
                            giornataInfo);
        } else if (giornataInfo == null && giocatore != null) {
            l = pagelleRepository.findByFcGiocatore(giocatore);
        } else if (giornataInfo != null && giocatore != null) {
            FcPagelle fcPagelle = pagelleRepository.findByFcGiornataInfoAndFcGiocatore(giornataInfo, giocatore);
            l = new ArrayList<>();
            l.add(fcPagelle);
        }
        return l;
    }

    public FcPagelle updatePagelle(FcPagelle c) {
        FcPagelle fcPagelle = null;
        try {
            fcPagelle = pagelleRepository.save(c);
        } catch (Exception ex) {
            return fcPagelle;
        }
        return fcPagelle;
    }

    public String deletePagelle(FcPagelle c) {
        String id = "";
        try {
            pagelleRepository.delete(c);
            id = "" + c.getId();
        } catch (Exception ex) {
            return "Error delete : " + ex.toString();
        }
        return "Pagelle succesfully delete with id = " + id;
    }

}