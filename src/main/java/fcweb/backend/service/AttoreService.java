package fcweb.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;

@Service
public class AttoreService{

	private final AttoreRepository repository;

	public AttoreService(AttoreRepository repository) {
		this.repository = repository;
	}

	public Optional<FcAttore> get(Long id) {
		return repository.findById(id);
	}

	public FcAttore update(FcAttore entity) {
		return repository.save(entity);
	}

	public void delete(Long id) {
		repository.deleteById(id);
	}

	public Page<FcAttore> list(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Page<FcAttore> list(Pageable pageable,
			Specification<FcAttore> filter) {
		return repository.findAll(filter, pageable);
	}

	public int count() {
		return (int) repository.count();
	}

	public List<FcAttore> findAll() {
		return repository.findAll();
	}

	public List<FcAttore> findByActive(boolean active) {
		List<FcAttore> l = repository.findByActive(active);
		return l;
	}

}
