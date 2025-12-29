package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcCampionato;

public interface CampionatoRepository
		extends CrudRepository<FcCampionato, Long>{

	Page<FcCampionato> findAll(Pageable pageable);

	Iterable<FcCampionato> findAll(Sort sort);

	FcCampionato findByIdCampionato(Integer idCampionato);

	FcCampionato findByActive(boolean active);
}