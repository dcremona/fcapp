package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcExpFreePl;

public interface ExpFreePlRepository extends CrudRepository<FcExpFreePl, Long>{

	Page<FcExpFreePl> findAll(Pageable pageable);

	Iterable<FcExpFreePl> findAll(Sort sort);

}