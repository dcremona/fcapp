package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcStatoGiocatore;

@Service
public class StatoGiocatoreService {

    private final StatoGiocatoreRepository statoGiocatoreRepository;

    @Autowired
    public StatoGiocatoreService(StatoGiocatoreRepository statoGiocatoreRepository) {
        this.statoGiocatoreRepository = statoGiocatoreRepository;
    }

    public List<FcStatoGiocatore> findAll() {
        List<FcStatoGiocatore> l = (List<FcStatoGiocatore>) statoGiocatoreRepository.findAll();
        return l;
    }

}