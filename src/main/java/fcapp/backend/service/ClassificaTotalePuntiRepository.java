package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassificaTotPt;
import fcapp.backend.data.entity.FcClassificaTotPtId;
import fcapp.backend.data.entity.FcGiornataInfo;

public interface ClassificaTotalePuntiRepository
		extends CrudRepository<FcClassificaTotPt, FcClassificaTotPtId>{

	Page<FcClassificaTotPt> findAll(Pageable pageable);

	Iterable<FcClassificaTotPt> findAll(Sort sort);

	List<FcClassificaTotPt> findByFcCampionatoAndFcGiornataInfo(
            FcCampionato campionato, FcGiornataInfo giornataInfo);

	FcClassificaTotPt findByFcCampionatoAndFcAttoreAndFcGiornataInfo(
            FcCampionato campionato, FcAttore attore,
            FcGiornataInfo giornataInfo);

	FcClassificaTotPt findByFcAttoreAndFcGiornataInfo(FcAttore attore,
                                                      FcGiornataInfo giornataInfo);

}