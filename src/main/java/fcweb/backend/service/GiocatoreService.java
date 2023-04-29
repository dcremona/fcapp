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
		List<FcGiocatore> l = (List<FcGiocatore>) giocatoreRepository.findAll();
		return l;
	}

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo) {
		List<FcGiocatore> l = giocatoreRepository.findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(ruolo, flagAttivo);
		return l;
	}

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo, Collection<Integer> giocatore) {
		List<FcGiocatore> l = giocatoreRepository.findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(ruolo, flagAttivo, giocatore);
		return l;
	}

	public List<FcGiocatore> findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo, FcSquadra squadra,
			Collection<Integer> giocatore) {
		List<FcGiocatore> l = giocatoreRepository.findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(flagAttivo, squadra, giocatore);
		return l;
	}

	public List<FcGiocatore> findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(
			FcRuolo ruolo, FcSquadra squadra) {
		List<FcGiocatore> l = null;
		if (ruolo == null && squadra == null) {
			l = (List<FcGiocatore>) giocatoreRepository.findAll();
		} else if (ruolo != null && squadra == null) {
			l = giocatoreRepository.findByFcRuoloOrderByQuotazioneDesc(ruolo);
		} else if (ruolo == null && squadra != null) {
			l = giocatoreRepository.findByFcSquadraOrderByQuotazioneDesc(squadra);
		} else if (ruolo != null && squadra != null) {
			l = giocatoreRepository.findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(ruolo, squadra);
		}
		return l;
	}

	// em
	public List<FcGiocatore> findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			Collection<Integer> notIn) {
		List<FcGiocatore> l = giocatoreRepository.findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(notIn);
		return l;
	}

	public FcGiocatore updateGiocatore(FcGiocatore c) {
		FcGiocatore giocatore = null;
		try {
			giocatore = giocatoreRepository.save(c);
		} catch (Exception ex) {
			return giocatore;
		}
		return giocatore;
	}

	public String deleteGiocatore(FcGiocatore c) {
		String id = "";
		try {
			giocatoreRepository.delete(c);
			id = "" + c.getIdGiocatore();
		} catch (Exception ex) {
			return "Error delete : " + ex.toString();
		}
		return "Giocatore succesfully delete with id = " + id;
	}

}