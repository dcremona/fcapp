package fcapp.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcStatoGiocatore;

public interface StatoGiocatoreRepository
		extends CrudRepository<FcStatoGiocatore, Long>{

	Page<FcStatoGiocatore> findAll(Pageable pageable);

	Iterable<FcStatoGiocatore> findAll(Sort sort);

}