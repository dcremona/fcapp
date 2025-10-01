package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcExpRosea;

public interface ExpRoseARepository extends CrudRepository<FcExpRosea, Long>{

	Page<FcExpRosea> findAll(Pageable pageable);

	Iterable<FcExpRosea> findAll(Sort sort);

}