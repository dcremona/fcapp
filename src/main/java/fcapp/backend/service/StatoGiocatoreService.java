package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcStatoGiocatore;

@Service
public class StatoGiocatoreService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final StatoGiocatoreRepository statoGiocatoreRepository;

	public StatoGiocatoreService(
			StatoGiocatoreRepository statoGiocatoreRepository) {
		this.statoGiocatoreRepository = statoGiocatoreRepository;
	}

	public List<FcStatoGiocatore> findAll() {
		return (List<FcStatoGiocatore>) statoGiocatoreRepository.findAll();
	}

	public FcStatoGiocatore save(FcStatoGiocatore c) {
		FcStatoGiocatore fcStatoGiocatore = null;
		try {
			fcStatoGiocatore = statoGiocatoreRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcStatoGiocatore;
	}

}