package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcRuolo;

@Service
public class RuoloService{

	private final RuoloRepository ruoloRepository;

	@Autowired
	public RuoloService(RuoloRepository ruoloRepository) {
		this.ruoloRepository = ruoloRepository;
	}

	public List<FcRuolo> findAll() {
		return (List<FcRuolo>) ruoloRepository.findAll(sortByIdRuoloDesc());
	}

	private Sort sortByIdRuoloDesc() {
		return Sort.by(Sort.Direction.DESC, "idRuolo");
	}

}