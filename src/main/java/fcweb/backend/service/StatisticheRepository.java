package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcStatistiche;

public interface StatisticheRepository
		extends CrudRepository<FcStatistiche, Long>{

	Page<FcStatistiche> findAll(Pageable pageable);

	Iterable<FcStatistiche> findAll(Sort sort);

	public List<FcStatistiche> findByFlagAttivo(boolean flagAttivo);

}