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

public interface GiornataGiocatoreRepository
		extends CrudRepository<FcGiornataGiocatore, FcGiornataGiocatoreId>{

	Page<FcGiornataGiocatore> findAll(Pageable pageable);

	Iterable<FcGiornataGiocatore> findAll(Sort sort);

	List<FcGiornataGiocatore> findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
            FcGiornataInfo giornataInfo);

	List<FcGiornataGiocatore> findByFcGiocatore(FcGiocatore giocatore);

}