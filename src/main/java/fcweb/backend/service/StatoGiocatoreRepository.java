package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcStatoGiocatore;

public interface StatoGiocatoreRepository
		extends CrudRepository<FcStatoGiocatore, Long>{

	Page<FcStatoGiocatore> findAll(Pageable pageable);

	Iterable<FcStatoGiocatore> findAll(Sort sort);

	public FcStatoGiocatore findByDescStatoGiocatore(String descStatoGiocatore);
}