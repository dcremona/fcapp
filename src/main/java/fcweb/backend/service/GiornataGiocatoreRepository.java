package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatoreId;
import fcweb.backend.data.entity.FcGiornataInfo;

public interface GiornataGiocatoreRepository extends CrudRepository<FcGiornataGiocatore, FcGiornataGiocatoreId> {

    Page<FcGiornataGiocatore> findAll(Pageable pageable);

    Iterable<FcGiornataGiocatore> findAll(Sort sort);

    FcGiornataGiocatore findFirstByOrderByFcGiornataInfoAsc();

    FcGiornataGiocatore findTopByOrderByFcGiornataInfoDesc();

    public List<FcGiornataGiocatore> findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
            FcGiornataInfo giornataInfo);

    public List<FcGiornataGiocatore> findByFcGiocatore(FcGiocatore giocatore);

    public FcGiornataGiocatore findByFcGiornataInfoAndFcGiocatore(FcGiornataInfo giornataInfo, FcGiocatore giocatore);

}