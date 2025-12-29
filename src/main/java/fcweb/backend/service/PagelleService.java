package fcweb.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;

@Service
public class PagelleService{

	private final PagelleRepository pagelleRepository;

	@Autowired
	public PagelleService(PagelleRepository pagelleRepository) {
		this.pagelleRepository = pagelleRepository;
	}

	public List<FcPagelle> findAll() {
		return (List<FcPagelle>) pagelleRepository.findAll();
	}

	public FcPagelle findCurrentGiornata() {
		return pagelleRepository.findTopByOrderByFcGiornataInfoDesc();
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

	public FcPagelle updatePagelle(FcPagelle c) {
		FcPagelle fcPagelle;
		try {
			fcPagelle = pagelleRepository.save(c);
		} catch (Exception ex) {
			return null;
		}
		return fcPagelle;
	}

	public void deletePagelle(FcPagelle c) {
        try {
			pagelleRepository.delete(c);
        } catch (Exception ignored) {
		}
	}

}