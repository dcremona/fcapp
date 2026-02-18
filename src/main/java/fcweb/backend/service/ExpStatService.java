package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpStat;

@Service
public class ExpStatService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final ExpStatRepository expStatRepository;

	public ExpStatService(ExpStatRepository expStatRepository) {
		this.expStatRepository = expStatRepository;
	}

	public List<FcExpStat> findAll() {
		return (List<FcExpStat>) expStatRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}

	public FcExpStat updateExpStat(FcExpStat expStat) {
		FcExpStat fcExpStat = null;
		try {
			fcExpStat = expStatRepository.save(expStat);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcExpStat;
	}

	public void deleteExpStat(FcExpStat expStat) {
        try {
			expStatRepository.delete(expStat);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}