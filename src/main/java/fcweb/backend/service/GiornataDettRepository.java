package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;

public interface GiornataDettRepository
		extends CrudRepository<FcGiornataDett, Long>{

	Page<FcGiornataDett> findAll(Pageable pageable);

	Iterable<FcGiornataDett> findAll(Sort sort);

	public List<FcGiornataDett> findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(
			FcAttore attore, FcGiornataInfo giornataInfo);

	public List<FcGiornataDett> findByFcGiornataInfoOrderByOrdinamentoAsc(
			FcGiornataInfo giornataInfo);

}