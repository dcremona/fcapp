package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcExpStat;

@Service
public class AlboService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final AlboRepository alboRepository;

	public AlboService(AlboRepository alboRepository) {
		this.alboRepository = alboRepository;
	}

	public List<FcExpStat> findAll() {
		return(List<FcExpStat>) alboRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.DESC, "id");
	}

	public FcExpStat save(FcExpStat c) {
		FcExpStat fcExpStat = null;
		try {
			fcExpStat = alboRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcExpStat;
	}

}