package fcapp.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcFormazione;
import fcapp.backend.data.entity.FcFormazioneId;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcStatistiche;

@Service
public class FormazioneService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final FormazioneRepository formazioneRepository;

	public FormazioneService(FormazioneRepository formazioneRepository) {
		this.formazioneRepository = formazioneRepository;
	}

	public List<FcFormazione> findAll() {
		return (List<FcFormazione>) formazioneRepository.findAll();
	}

	public List<FcFormazione> findByFcCampionato(FcCampionato campionato) {
		return formazioneRepository.findByFcCampionato(campionato);
	}

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(
			FcCampionato campionato, FcAttore attore) {
		return formazioneRepository.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);
	}
	
	public List<FcFormazione> findByFcCampionatoAndFcGiocatore(
            FcCampionato campionato, FcGiocatore giocatore) {
		return formazioneRepository.findByFcCampionatoAndFcGiocatore(campionato,giocatore);
	}

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcCampionato campionato, FcAttore attore, boolean view) {
		List<FcFormazione> l = formazioneRepository.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore);

		if (view) {

			List<FcFormazione> lNew = new ArrayList<>();
			for (FcFormazione f : l) {
				if (f.getFcGiocatore() != null) {
					lNew.add(f);
				} else {

					FcFormazione fNew = getFcFormazione(f);

					lNew.add(fNew);
				}
			}

			return lNew;

		}

		return l;

	}

	private @NonNull FcFormazione getFcFormazione(FcFormazione f) {
		FcStatistiche sNew = new FcStatistiche();
		sNew.setMediaVoto((double) 0);
		sNew.setFantaMedia((double) 0);

		FcRuolo rNew = new FcRuolo();

		FcGiocatore gNew = new FcGiocatore();
		gNew.setFcStatistiche(sNew);
		gNew.setFcRuolo(rNew);
		gNew.setIdGiocatore(-1);
		gNew.setQuotazione(0);

		FcFormazione fNew = new FcFormazione();
		fNew.setTotPagato(0);
		fNew.setFcGiocatore(gNew);

		fNew.setFcAttore(f.getFcAttore());
		fNew.setFcCampionato(f.getFcCampionato());
		return fNew;
	}

	public List<FcFormazione> findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcAttore attore) {
		return formazioneRepository.findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(attore);
	}

	public void createFormazione(FcAttore attore, Integer idCampionato,
								 Integer ordinamento) {
        try {
			FcFormazione formazione = new FcFormazione();
			FcFormazioneId formazionePK = new FcFormazioneId();
			formazionePK.setIdCampionato(idCampionato);
			formazionePK.setIdAttore(attore.getIdAttore());
			formazionePK.setOrdinamento(ordinamento);
			formazione.setId(formazionePK);
			formazioneRepository.save(formazione);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

	public FcFormazione save(FcFormazione c) {
		FcFormazione giocatore = null;
		try {
			giocatore = formazioneRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return giocatore;
	}

	public void delete(FcFormazione c) {
        try {
			formazioneRepository.delete(c);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}