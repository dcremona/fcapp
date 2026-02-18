package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataGiocatoreService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final GiornataGiocatoreRepository giornataGiocatoreRepository;

	public GiornataGiocatoreService(
			GiornataGiocatoreRepository giornataGiocatoreRepository) {
		this.giornataGiocatoreRepository = giornataGiocatoreRepository;
	}

	public List<FcGiornataGiocatore> findAll() {
		return (List<FcGiornataGiocatore>) giornataGiocatoreRepository.findAll();
	}

	public List<FcGiornataGiocatore> findByCustonm(FcGiornataInfo giornataInfo,
			FcGiocatore giocatore) {
		List<FcGiornataGiocatore> l = null;
		if (giornataInfo == null && giocatore == null) {
			l = (List<FcGiornataGiocatore>) giornataGiocatoreRepository.findAll();
		} else if (giornataInfo != null && giocatore == null) {
			l = giornataGiocatoreRepository.findByFcGiornataInfoOrderByFcGiocatoreFcSquadraAscFcGiocatoreFcRuoloDescFcGiocatoreAsc(giornataInfo);
		} else if (giornataInfo == null) {
			l = giornataGiocatoreRepository.findByFcGiocatore(giocatore);
		}
		return l;
	}

	public void deleteByCustonm(FcGiornataInfo giornataInfo) {
		List<FcGiornataGiocatore> listSqualificatiInfortunati = this.findByCustonm(giornataInfo, null);
		for (FcGiornataGiocatore gg : listSqualificatiInfortunati) {
			giornataGiocatoreRepository.deleteById(gg.getId());
		}
	}
	
	public FcGiornataGiocatore save(FcGiornataGiocatore c) {
		FcGiornataGiocatore fcGiornataGiocatore = null;
		try {
			fcGiornataGiocatore = giornataGiocatoreRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcGiornataGiocatore;
	}


}