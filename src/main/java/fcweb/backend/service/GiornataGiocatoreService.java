package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcGiornataGiocatore;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataGiocatoreService{

	private final GiornataGiocatoreRepository giornataGiocatoreRepository;

	@Autowired
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

}