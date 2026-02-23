package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcCampionato;

@Service
public class CampionatoService{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final CampionatoRepository campionatoRepository;

	public CampionatoService(CampionatoRepository campionatoRepository) {
		this.campionatoRepository = campionatoRepository;
	}

	public List<FcCampionato> findAll() {
		return (List<FcCampionato>) campionatoRepository.findAll();
	}
	
	public FcCampionato findByIdCampionato(Integer idCampionato) {
		return campionatoRepository.findByIdCampionato(idCampionato);
	}

	public FcCampionato findByActive(boolean active) {
		return campionatoRepository.findByActive(active);
	}

	public FcCampionato save(FcCampionato c) {
		FcCampionato fcCampionato = null;
		try {
			fcCampionato = campionatoRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcCampionato;
	}

	public void delete(FcCampionato c) {
        try {
			campionatoRepository.delete(c);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}