package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpStat;

@Service
public class ExpStatService{

	private final ExpStatRepository expStatRepository;

	@Autowired
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
		} catch (Exception ignored) {
		}
		return fcExpStat;
	}

	public void deleteExpStat(FcExpStat expStat) {
        try {
			expStatRepository.delete(expStat);
        } catch (Exception ignored) {

		}
	}

}