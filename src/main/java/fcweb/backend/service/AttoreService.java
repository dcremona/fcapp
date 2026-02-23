package fcweb.backend.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;

@Service
public class AttoreService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final AttoreRepository attoreRepository;

	public AttoreService(AttoreRepository attoreRepository) {
		this.attoreRepository = attoreRepository;
	}

	public Optional<FcAttore> get(Long id) {
		return attoreRepository.findById(id);
	}

	public Page<FcAttore> list(Pageable pageable) {
		return attoreRepository.findAll(pageable);
	}

	public Page<FcAttore> list(Pageable pageable,
			Specification<FcAttore> filter) {
		return attoreRepository.findAll(filter, pageable);
	}

	public int count() {
		return (int) attoreRepository.count();
	}

	public List<FcAttore> findAll() {
		return attoreRepository.findAll();
	}

	public List<FcAttore> findByActive(boolean active) {
		return attoreRepository.findByActive(active);
	}

	public FcAttore save(FcAttore c) {
		FcAttore fcAttore = null;
		try {
			fcAttore = attoreRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcAttore;
	}

	public void delete(Long id) {
		attoreRepository.deleteById(id);
	}
	
}
