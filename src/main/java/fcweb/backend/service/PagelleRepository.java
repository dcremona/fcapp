package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.backend.data.entity.FcPagelleId;

public interface PagelleRepository
		extends CrudRepository<FcPagelle, FcPagelleId>{

	Page<FcPagelle> findAll(Pageable pageable);

	Iterable<FcPagelle> findAll(Sort sort);

	FcPagelle findFirstByOrderByFcGiornataInfoAsc();

	FcPagelle findTopByOrderByFcGiornataInfoDesc();

	public List<FcPagelle> findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
			FcGiornataInfo giornataInfo);

	public List<FcPagelle> findByFcGiocatore(FcGiocatore giocatore);

	public FcPagelle findByFcGiornataInfoAndFcGiocatore(
			FcGiornataInfo giornataInfo, FcGiocatore giocatore);

}