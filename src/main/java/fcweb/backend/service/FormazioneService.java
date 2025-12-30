package fcweb.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcFormazioneId;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcStatistiche;

@Service
public class FormazioneService{

	private final FormazioneRepository formazioneRepository;

	@Autowired
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
        } catch (Exception ignored) {
		}
	}

	public FcFormazione updateFormazione(FcFormazione c) {
		FcFormazione giocatore;
		try {
			giocatore = formazioneRepository.save(c);
		} catch (Exception ex) {
			return null;
		}
		return giocatore;
	}

	public void deleteFormazione(FcFormazione c) {
        try {
			formazioneRepository.delete(c);
        } catch (Exception ignored) {
		}
	}

}