package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiornataRis;
import fcapp.backend.data.entity.FcGiornataRisId;

public interface GiornataRisRepository
		extends CrudRepository<FcGiornataRis, FcGiornataRisId>{

	Page<FcGiornataRis> findAll(Pageable pageable);

	Iterable<FcGiornataRis> findAll(Sort sort);

	List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoAsc(
            FcAttore fcAttore);

}