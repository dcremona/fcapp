package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.data.entity.FcClassificaId;

@Service
public class ClassificaService{

	private final ClassificaRepository classificaRepository;

	@Autowired
	public ClassificaService(ClassificaRepository classificaRepository) {
		this.classificaRepository = classificaRepository;
	}

	public List<FcClassifica> findAll() {
		return (List<FcClassifica>) classificaRepository.findAll();
	}

	public List<FcClassifica> findByFcCampionatoOrderByPuntiDescIdPosizAsc(
			FcCampionato campionato) {
		return classificaRepository.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);
	}

	public List<FcClassifica> findByFcCampionatoOrderByTotPuntiRosaDesc(
			FcCampionato campionato) {
		return classificaRepository.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
	}

	public List<FcClassifica> findByFcCampionatoOrderByTotPuntiTvsTDesc(
			FcCampionato campionato) {
		return classificaRepository.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);
	}

	public FcClassifica findByFcCampionatoAndFcAttore(FcCampionato campionato,
			FcAttore attore) {
		return classificaRepository.findByFcCampionatoAndFcAttore(campionato, attore);
	}

	public String create(FcAttore attore, FcCampionato campionato,
			Double totPunti) {
		String id;
		try {
			FcClassifica clas = new FcClassifica();
			FcClassificaId classificaPK = new FcClassificaId();
			classificaPK.setIdAttore(attore.getIdAttore());
			classificaPK.setIdCampionato(campionato.getIdCampionato());
			clas.setId(classificaPK);
			clas.setTotPunti(totPunti);
			clas.setTotPuntiOld(totPunti);
			clas.setTotPuntiRosa(totPunti);
			classificaRepository.save(clas);
			id = clas.getFcAttore().toString();
		} catch (Exception ex) {
			return "Error creating the classifica: " + ex;
		}
		return "classifica succesfully created with id = " + id;
	}

	public FcClassifica updateClassifica(FcClassifica classifica) {
		FcClassifica fcClassifica = null;
		try {
			fcClassifica = classificaRepository.save(classifica);
		} catch (Exception ignored) {
		}
		return fcClassifica;
	}

	public void deleteClassifica(FcClassifica classifica) {
        try {
			classificaRepository.delete(classifica);
        } catch (Exception ignored) {

		}
	}

}