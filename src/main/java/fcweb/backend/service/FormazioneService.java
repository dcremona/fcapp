package fcweb.backend.service;

import java.util.ArrayList;
import java.util.List;

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
		List<FcFormazione> l = (List<FcFormazione>) formazioneRepository.findAll();
		return l;
	}

	public List<FcFormazione> findByFcCampionato(FcCampionato campionato) {
		List<FcFormazione> l = (List<FcFormazione>) formazioneRepository.findByFcCampionato(campionato);
		return l;
	}

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(
			FcCampionato campionato, FcAttore attore) {
		List<FcFormazione> l = (List<FcFormazione>) formazioneRepository.findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(campionato, attore);
		return l;
	}

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcCampionato campionato, FcAttore attore, boolean view) {
		List<FcFormazione> l = (List<FcFormazione>) formazioneRepository.findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(campionato, attore);

		if (view) {

			List<FcFormazione> lNew = new ArrayList<FcFormazione>();
			for (FcFormazione f : l) {
				if (f.getFcGiocatore() != null) {
					lNew.add(f);
				} else {

					FcStatistiche sNew = new FcStatistiche();
					sNew.setMediaVoto(Double.valueOf(0));
					sNew.setFantaMedia(Double.valueOf(0));

					FcRuolo rNew = new FcRuolo();

					FcGiocatore gNew = new FcGiocatore();
					gNew.setFcStatistiche(sNew);
					gNew.setFcRuolo(rNew);
					gNew.setIdGiocatore(-1);
					gNew.setQuotazione(Integer.valueOf(0));

					FcFormazione fNew = new FcFormazione();
					fNew.setTotPagato(Double.valueOf(0));
					fNew.setFcGiocatore(gNew);

					fNew.setFcAttore(f.getFcAttore());
					fNew.setFcCampionato(f.getFcCampionato());

					lNew.add(fNew);
				}
			}

			return lNew;

		}

		return l;

	}

	public FcFormazione findByFcCampionatoAndFcAttoreAndFcGiocatore(
			FcCampionato campionato, FcAttore attore, FcGiocatore giocatore) {
		FcFormazione l = formazioneRepository.findByFcCampionatoAndFcAttoreAndFcGiocatore(campionato, attore, giocatore);
		return l;
	}

	public List<FcFormazione> findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcAttore attore) {
		List<FcFormazione> l = (List<FcFormazione>) formazioneRepository.findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(attore);
		return l;
	}

	public String createFormazione(FcAttore attore, Integer idCampionato,
			Integer ordinamento) {
		String id = "";
		try {
			FcFormazione formazione = new FcFormazione();
			FcFormazioneId formazionePK = new FcFormazioneId();
			formazionePK.setIdCampionato(idCampionato);
			formazionePK.setIdAttore(attore.getIdAttore());
			formazionePK.setOrdinamento(ordinamento);
			formazione.setId(formazionePK);
			formazioneRepository.save(formazione);
			id = formazionePK.getOrdinamento() + " " + formazionePK.getIdAttore();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error creating the formazione: " + ex.toString();
		}
		return "formazione succesfully created with id = " + id;
	}

	public FcFormazione updateFormazione(FcFormazione c) {
		FcFormazione giocatore = null;
		try {
			giocatore = formazioneRepository.save(c);
		} catch (Exception ex) {
			return giocatore;
		}
		return giocatore;
	}

	public String deleteFormazione(FcFormazione c) {
		String id = "";
		try {
			formazioneRepository.delete(c);
			id = "" + c.getId().getOrdinamento();
		} catch (Exception ex) {
			return "Error delete : " + ex.toString();
		}
		return "Formazione succesfully delete with id = " + id;
	}

}