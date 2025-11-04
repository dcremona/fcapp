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

	public FcCampionato findByIdCampionato(Integer idCampionato) {
		return campionatoRepository.findByIdCampionato(idCampionato);
	}

	public FcCampionato findByActive(boolean active) {
		return campionatoRepository.findByActive(active);
	}

	public FcCampionato updateCampionato(FcCampionato c) {
		FcCampionato fcCampionato = null;
		try {
			fcCampionato = campionatoRepository.save(c);
		} catch (Exception ex) {

		}
		return fcCampionato;
	}

	public String deleteCampionato(FcCampionato c) {
		String id = "";
		try {
			campionatoRepository.delete(c);
			id = "" + c.getIdCampionato();
		} catch (Exception ex) {
			return "Error delete : " + ex.toString();
		}
		return "campionato succesfully delete with id = " + id;
	}

}