package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcRegolamento;

public interface RegolamentoRepository extends CrudRepository<FcRegolamento, Long>{

	Page<FcRegolamento> findAll(Pageable pageable);

	Iterable<FcRegolamento> findAll(Sort sort);
	

}