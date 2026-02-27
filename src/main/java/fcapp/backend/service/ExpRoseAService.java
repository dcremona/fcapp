package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcExpRosea;

@Service
public class ExpRoseAService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final ExpRoseARepository expRoseARepository;

	public ExpRoseAService(ExpRoseARepository expRoseARepository) {
		this.expRoseARepository = expRoseARepository;
	}

	public List<FcExpRosea> findAll() {
		return (List<FcExpRosea>) expRoseARepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}
	
	public FcExpRosea save(FcExpRosea c) {
		FcExpRosea fcExpRosea = null;
		try {
			fcExpRosea = expRoseARepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcExpRosea;
	}

}