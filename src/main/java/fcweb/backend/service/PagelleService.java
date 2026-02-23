package fcweb.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;

@Service
public class PagelleService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final PagelleRepository pagelleRepository;

	public PagelleService(PagelleRepository pagelleRepository) {
		this.pagelleRepository = pagelleRepository;
	}

	public List<FcPagelle> findAll() {
		return (List<FcPagelle>) pagelleRepository.findAll(sortBy());
	}

	private Sort sortBy() {
		return Sort.by(Sort.Direction.ASC, "fcGiocatore");
	}

	public FcPagelle findCurrentGiornata() {
		return pagelleRepository.findTopByOrderByFcGiornataInfoDesc();
	}
	
	public List<FcPagelle> findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(
            FcGiornataInfo giornataInfo) {
		return pagelleRepository.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);
	}
	
	public FcPagelle findByFcGiornataInfoAndFcGiocatore(
            FcGiornataInfo giornataInfo, FcGiocatore giocatore) {
		return pagelleRepository.findByFcGiornataInfoAndFcGiocatore(giornataInfo, giocatore);
	}

	public List<FcPagelle> findByCustonm(FcGiornataInfo giornataInfo,
			FcGiocatore giocatore) {

		List<FcPagelle> l;
		if (giornataInfo == null && giocatore == null) {
			l = (List<FcPagelle>) pagelleRepository.findAll();
		} else if (giornataInfo != null && giocatore == null) {
			l = pagelleRepository.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);
		} else if (giornataInfo == null) {
			l = pagelleRepository.findByFcGiocatore(giocatore);
		} else {
			FcPagelle fcPagelle = pagelleRepository.findByFcGiornataInfoAndFcGiocatore(giornataInfo, giocatore);
			l = new ArrayList<>();
			l.add(fcPagelle);
		}
		return l;
	}
	
	public FcPagelle save(FcPagelle c) {
		FcPagelle fcPagelle = null;
		try {
			fcPagelle = pagelleRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcPagelle;
	}

	public void delete(FcPagelle c) {
        try {
			pagelleRepository.delete(c);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}