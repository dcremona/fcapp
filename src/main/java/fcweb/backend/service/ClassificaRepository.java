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

public interface ClassificaRepository extends CrudRepository<FcClassifica, FcClassificaId> {

    Page<FcClassifica> findAll(Pageable pageable);

    Iterable<FcClassifica> findAll(Sort sort);

    public List<FcClassifica> findByFcCampionatoOrderByTotPuntiDesc(FcCampionato campionato);

    public List<FcClassifica> findByFcCampionatoOrderByTotPuntiRosaDesc(FcCampionato campionato);

    public List<FcClassifica> findByFcCampionatoOrderByTotPuntiTvsTDesc(FcCampionato campionato);

    public FcClassifica findByFcCampionatoAndFcAttore(FcCampionato campionato, FcAttore attore);

    public List<FcClassifica> findByFcCampionatoOrderByPuntiDescIdPosizAsc(FcCampionato campionato);

}