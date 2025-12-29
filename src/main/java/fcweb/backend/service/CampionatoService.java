package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcCampionato;

@Service
public class CampionatoService{

	private final CampionatoRepository campionatoRepository;

	@Autowired
	public CampionatoService(CampionatoRepository campionatoRepository) {
		this.campionatoRepository = campionatoRepository;
	}

	public List<FcCampionato> findAll() {
		return (List<FcCampionato>) campionatoRepository.findAll();
	}

	public FcCampionato findByActive(boolean active) {
		return campionatoRepository.findByActive(active);
	}

	public FcCampionato updateCampionato(FcCampionato c) {
		FcCampionato fcCampionato = null;
		try {
			fcCampionato = campionatoRepository.save(c);
		} catch (Exception ignored) {

		}
		return fcCampionato;
	}

	public void deleteCampionato(FcCampionato c) {
        try {
			campionatoRepository.delete(c);
        } catch (Exception ignored) {

		}
	}

}