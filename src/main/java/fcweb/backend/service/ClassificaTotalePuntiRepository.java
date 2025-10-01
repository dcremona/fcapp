package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcClassificaTotPtId;
import fcweb.backend.data.entity.FcGiornataInfo;

public interface ClassificaTotalePuntiRepository
		extends CrudRepository<FcClassificaTotPt, FcClassificaTotPtId>{

	Page<FcClassificaTotPt> findAll(Pageable pageable);

	Iterable<FcClassificaTotPt> findAll(Sort sort);

	public List<FcClassificaTotPt> findByFcCampionatoAndFcGiornataInfo(
			FcCampionato campionato, FcGiornataInfo giornataInfo);

	public FcClassificaTotPt findByFcCampionatoAndFcAttoreAndFcGiornataInfo(
			FcCampionato campionato, FcAttore attore,
			FcGiornataInfo giornataInfo);

	public FcClassificaTotPt findByFcAttoreAndFcGiornataInfo(FcAttore attore,
			FcGiornataInfo giornataInfo);

}