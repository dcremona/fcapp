package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcClassifica;
import fcapp.backend.data.entity.FcClassificaId;

public interface ClassificaRepository
		extends CrudRepository<FcClassifica, FcClassificaId>{

	Page<FcClassifica> findAll(Pageable pageable);

	Iterable<FcClassifica> findAll(Sort sort);

	List<FcClassifica> findByFcCampionatoOrderByTotPuntiRosaDesc(
            FcCampionato campionato);

	List<FcClassifica> findByFcCampionatoOrderByTotPuntiTvsTDesc(
            FcCampionato campionato);

	FcClassifica findByFcCampionatoAndFcAttore(FcCampionato campionato,
                                               FcAttore attore);

	List<FcClassifica> findByFcCampionatoOrderByPuntiDescIdPosizAsc(
            FcCampionato campionato);

}