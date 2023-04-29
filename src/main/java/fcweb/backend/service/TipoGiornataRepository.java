package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcTipoGiornata;

public interface TipoGiornataRepository extends CrudRepository<FcTipoGiornata, Long>{

	Page<FcTipoGiornata> findAll(Pageable pageable);

	Iterable<FcTipoGiornata> findAll(Sort sort);

	public FcTipoGiornata findByDescTipoGiornata(String descTipoGiornata);
}