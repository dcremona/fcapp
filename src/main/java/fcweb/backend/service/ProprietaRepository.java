package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcProperties;

public interface ProprietaRepository extends CrudRepository<FcProperties, Long>{

	Page<FcProperties> findAll(Pageable pageable);

	Iterable<FcProperties> findAll(Sort sort);

	FcProperties findByKey(String key);

}