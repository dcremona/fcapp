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
		List<FcClassifica> l = (List<FcClassifica>) classificaRepository.findAll();
		return l;
	}

	public List<FcClassifica> findByFcCampionatoOrderByPuntiDescIdPosizAsc(
			FcCampionato campionato) {
		List<FcClassifica> l = classificaRepository.findByFcCampionatoOrderByPuntiDescIdPosizAsc(campionato);
		return l;
	}

	public List<FcClassifica> findByFcCampionatoOrderByTotPuntiDesc(
			FcCampionato campionato) {
		List<FcClassifica> l = classificaRepository.findByFcCampionatoOrderByTotPuntiDesc(campionato);
		return l;
	}

	public List<FcClassifica> findByFcCampionatoOrderByTotPuntiRosaDesc(
			FcCampionato campionato) {
		List<FcClassifica> l = classificaRepository.findByFcCampionatoOrderByTotPuntiRosaDesc(campionato);
		return l;
	}

	public List<FcClassifica> findByFcCampionatoOrderByTotPuntiTvsTDesc(
			FcCampionato campionato) {
		List<FcClassifica> l = classificaRepository.findByFcCampionatoOrderByTotPuntiTvsTDesc(campionato);
		return l;
	}
	
	public FcClassifica findByFcCampionatoAndFcAttore(FcCampionato campionato,
			FcAttore attore) {
		FcClassifica l = classificaRepository.findByFcCampionatoAndFcAttore(campionato, attore);
		return l;
	}

	public String create(FcAttore attore, FcCampionato campionato,
			Double totPunti) {
		String id = "";
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
			return "Error creating the classifica: " + ex.toString();
		}
		return "classifica succesfully created with id = " + id;
	}

	public FcClassifica updateClassifica(FcClassifica classifica) {
		FcClassifica fcClassifica = null;
		try {
			fcClassifica = classificaRepository.save(classifica);
		} catch (Exception ex) {

		}
		return fcClassifica;
	}

	public String deleteClassifica(FcClassifica classifica) {
		String id = "";
		try {
			classificaRepository.delete(classifica);
			id = "" + classifica.getId().getIdCampionato();
		} catch (Exception ex) {
			return "Error delete giornata: " + ex.toString();
		}
		return "classifica succesfully delete with id = " + id;
	}

}