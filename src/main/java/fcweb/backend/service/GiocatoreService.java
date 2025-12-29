package fcweb.backend.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;

@Service
public class GiocatoreService{

	private final GiocatoreRepository giocatoreRepository;

	@Autowired
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

	// em
	public List<FcGiocatore> findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			Collection<Integer> notIn) {
		return giocatoreRepository.findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(notIn);
	}

	public FcGiocatore updateGiocatore(FcGiocatore c) {
		FcGiocatore giocatore;
		try {
			giocatore = giocatoreRepository.save(c);
		} catch (Exception ex) {
			return null;
		}
		return giocatore;
	}

	public void deleteGiocatore(FcGiocatore c) {
        try {
			giocatoreRepository.delete(c);
        } catch (Exception ignored) {

		}
	}

}