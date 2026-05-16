package fcapp.backend.service;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;

@Service
public class GiocatoreService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final GiocatoreRepository giocatoreRepository;

	public GiocatoreService(GiocatoreRepository giocatoreRepository) {
		this.giocatoreRepository = giocatoreRepository;
	}

	public List<FcGiocatore> findAll() {
		return (List<FcGiocatore>) giocatoreRepository.findAll();
	}

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo) {
		return giocatoreRepository.findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(ruolo, flagAttivo);
	}

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo, Collection<Integer> giocatore) {
		return giocatoreRepository.findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(ruolo, flagAttivo, giocatore);
	}

	public List<FcGiocatore> findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
            boolean flagAttivo, FcSquadra squadra,
            Collection<Integer> giocatore) {
		return giocatoreRepository.findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(flagAttivo, squadra, giocatore);
	}

	public List<FcGiocatore> findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(
            boolean flagAttivo, FcSquadra squadra) {
		return giocatoreRepository.findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(flagAttivo, squadra);
	}
	
	public List<FcGiocatore> findByCognGiocatoreContaining(
            String cognGiocatore) {
		return giocatoreRepository.findByCognGiocatoreContaining(cognGiocatore);
	}

	public FcGiocatore findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(
            String nomeGiocatore, FcSquadra squadra, FcRuolo ruolo) {
		return giocatoreRepository.findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(nomeGiocatore, squadra, ruolo);
	}
	
	public List<FcGiocatore> findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(
			FcRuolo ruolo, FcSquadra squadra) {
		List<FcGiocatore> l;
		if (ruolo == null && squadra == null) {
			l = (List<FcGiocatore>) giocatoreRepository.findAll();
		} else if (ruolo != null && squadra == null) {
			l = giocatoreRepository.findByFcRuoloOrderByQuotazioneDesc(ruolo);
		} else if (ruolo == null) {
			l = giocatoreRepository.findByFcSquadraOrderByQuotazioneDesc(squadra);
		} else {
			l = giocatoreRepository.findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(ruolo, squadra);
		}
		return l;
	}
	
	public FcGiocatore findByNomeImg(String nomeImg,boolean flagAttivo) {
		return giocatoreRepository.findByNomeImgAndFlagAttivo(nomeImg,flagAttivo);
	}
	
	public FcGiocatore findByIdGiocatore(int idGiocatore) {
		return giocatoreRepository.findByIdGiocatore(idGiocatore);
	}

	// em
	public List<FcGiocatore> findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			Collection<Integer> notIn) {
		return giocatoreRepository.findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(notIn);
	}

	public FcGiocatore save(FcGiocatore c) {
		FcGiocatore giocatore = null;
		try {
			giocatore = giocatoreRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return giocatore;
	}

	public void delete(FcGiocatore c) {
        try {
			giocatoreRepository.delete(c);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}