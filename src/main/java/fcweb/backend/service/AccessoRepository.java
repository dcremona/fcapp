package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAccesso;

public interface AccessoRepository extends CrudRepository<FcAccesso, Long>{

	Page<FcAccesso> findAll(Pageable pageable);

	Iterable<FcAccesso> findAll(Sort sort);
	

}