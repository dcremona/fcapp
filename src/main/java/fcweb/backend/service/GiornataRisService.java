package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataRis;

@Service
public class GiornataRisService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final GiornataRisRepository giornataRisRepository;

	public GiornataRisService(GiornataRisRepository giornataRisRepository) {
		this.giornataRisRepository = giornataRisRepository;
	}

	public List<FcGiornataRis> findAll() {
		return (List<FcGiornataRis>) giornataRisRepository.findAll();
	}

	public List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoAsc(
			FcAttore fcAttore) {
		return giornataRisRepository.findByFcAttoreOrderByFcGiornataInfoAsc(fcAttore);
	}

	public FcGiornataRis save(FcGiornataRis c) {
		FcGiornataRis fcGiornataRis = null;
		try {
			fcGiornataRis = giornataRisRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcGiornataRis;
	}

}