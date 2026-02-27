package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;
import fcapp.backend.data.entity.FcPagelleId;

public interface PagelleRepository
		extends CrudRepository<FcPagelle, FcPagelleId>{

	Page<FcPagelle> findAll(Pageable pageable);

	Iterable<FcPagelle> findAll(Sort sort);

	FcPagelle findTopByOrderByFcGiornataInfoDesc();

	List<FcPagelle> findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
            FcGiornataInfo giornataInfo);

	List<FcPagelle> findByFcGiocatore(FcGiocatore giocatore);

	FcPagelle findByFcGiornataInfoAndFcGiocatore(
            FcGiornataInfo giornataInfo, FcGiocatore giocatore);

}