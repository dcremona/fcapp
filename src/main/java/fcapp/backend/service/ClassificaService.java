package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaId;

@Service
public class ClassificaService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final ClassificaRepository classificaRepository;

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

	public FcClassifica save(FcClassifica classifica) {
		FcClassifica fcClassifica = null;
		try {
			fcClassifica = classificaRepository.save(classifica);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcClassifica;
	}

	public void delete(FcClassifica classifica) {
        try {
			classificaRepository.delete(classifica);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}