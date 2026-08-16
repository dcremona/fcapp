package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcRuolo;

@Service
public class RuoloService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final RuoloRepository ruoloRepository;

	public RuoloService(RuoloRepository ruoloRepository) {
		this.ruoloRepository = ruoloRepository;
	}

	public List<FcRuolo> findAll() {
		return (List<FcRuolo>) ruoloRepository.findAll(sortByIdRuoloDesc());
	}

	public FcRuolo findByIdRuolo(String idRuolo) {
		return ruoloRepository.findByIdRuolo(idRuolo);
	}

	private Sort sortByIdRuoloDesc() {
		return Sort.by(Sort.Direction.DESC, "idRuolo");
	}

	public FcRuolo save(FcRuolo c) {
		FcRuolo fcRuolo = null;
		try {
			fcRuolo = ruoloRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcRuolo;
	}

}