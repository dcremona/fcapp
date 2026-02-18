package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpFreePl;

@Service
public class ExpFreePlService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final ExpFreePlRepository expFreePlRepository;

	public ExpFreePlService(ExpFreePlRepository expFreePlRepository) {
		this.expFreePlRepository = expFreePlRepository;
	}

	public List<FcExpFreePl> findAll() {
		return (List<FcExpFreePl>) expFreePlRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}
	
	public FcExpFreePl save(FcExpFreePl c) {
		FcExpFreePl fcExpFreePl = null;
		try {
			fcExpFreePl = expFreePlRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcExpFreePl;
	}


}