package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final GiornataRepository giornataRepository;

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
	
	public FcGiornata save(FcGiornata giornata) {
		FcGiornata fcGiornata = null;
		try {
			fcGiornata = giornataRepository.save(giornata);
		} catch (Exception ex) {
			log.error(ex.getMessage());
        }
		return fcGiornata;
	}

	public void delete(FcGiornata giornata) {
        try {
			giornataRepository.delete(giornata);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}