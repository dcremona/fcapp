package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcRuolo;

public interface RuoloRepository extends CrudRepository<FcRuolo, Long>{

	Page<FcRuolo> findAll(Pageable pageable);

	Iterable<FcRuolo> findAll(Sort sort);

}