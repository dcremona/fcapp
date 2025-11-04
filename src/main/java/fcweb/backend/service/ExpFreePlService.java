package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpFreePl;

@Service
public class ExpFreePlService{

	private final ExpFreePlRepository expFreePlRepository;

	@Autowired
	public ExpFreePlService(ExpFreePlRepository expFreePlRepository) {
		this.expFreePlRepository = expFreePlRepository;
	}

	public List<FcExpFreePl> findAll() {
		return (List<FcExpFreePl>) expFreePlRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}

}