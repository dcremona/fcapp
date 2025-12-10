package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataService{

	private final GiornataRepository giornataRepository;

	@Autowired
	public GiornataService(GiornataRepository giornataRepository) {
		this.giornataRepository = giornataRepository;
	}

	private Sort sortBy() {
		return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcTipoGiornata", "fcAttoreByIdAttoreCasa");
	}

	public List<FcGiornata> findAll() {
		return (List<FcGiornata>) giornataRepository.findAll(sortBy());
	}

	public List<FcGiornata> findByFcGiornataInfo(FcGiornataInfo giornataInfo) {
		return giornataRepository.findByFcGiornataInfoOrderByFcTipoGiornata(giornataInfo);
	}

	public List<FcGiornata> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(
			FcGiornataInfo start, FcGiornataInfo end) {
		return giornataRepository.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(start, end);
	}

	public FcGiornata updateGiornata(FcGiornata giornata) {
		FcGiornata fcGiornata = null;
		try {
			fcGiornata = giornataRepository.save(giornata);
		} catch (Exception ex) {
		}
		return fcGiornata;
	}

	public String deleteGiornata(FcGiornata giornata) {
		String id = "";
		try {
			giornataRepository.delete(giornata);
			id = "" + giornata.getId().getIdGiornata();
		} catch (Exception ex) {
			return "Error delete giornata: " + ex.toString();
		}
		return "giornata succesfully delete with id = " + id;
	}

}