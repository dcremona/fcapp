package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcExpStat;

public interface AlboRepository extends CrudRepository<FcExpStat, Long>{

	Page<FcExpStat> findAll(Pageable pageable);

	Iterable<FcExpStat> findAll(Sort sort);
	
	
}