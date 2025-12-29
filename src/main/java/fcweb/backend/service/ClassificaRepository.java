package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassifica;
import fcweb.backend.data.entity.FcClassificaId;

public interface ClassificaRepository
		extends CrudRepository<FcClassifica, FcClassificaId>{

	Page<FcClassifica> findAll(Pageable pageable);

	Iterable<FcClassifica> findAll(Sort sort);

	List<FcClassifica> findByFcCampionatoOrderByTotPuntiDesc(
            FcCampionato campionato);

	List<FcClassifica> findByFcCampionatoOrderByTotPuntiRosaDesc(
            FcCampionato campionato);

	List<FcClassifica> findByFcCampionatoOrderByTotPuntiTvsTDesc(
            FcCampionato campionato);

	FcClassifica findByFcCampionatoAndFcAttore(FcCampionato campionato,
                                               FcAttore attore);

	List<FcClassifica> findByFcCampionatoOrderByPuntiDescIdPosizAsc(
            FcCampionato campionato);

}