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
		List<FcCampionato> l = (List<FcCampionato>) campionatoRepository.findAll();
		return l;
	}

	public FcCampionato findByIdCampionato(Integer idCampionato) {
		FcCampionato c = campionatoRepository.findByIdCampionato(idCampionato);
		return c;
	}

	public FcCampionato findByActive(boolean active) {
		FcCampionato c = campionatoRepository.findByActive(active);
		return c;
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