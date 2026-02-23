package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataDettService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final GiornataDettRepository giornataDettRepository;

	public GiornataDettService(GiornataDettRepository giornataDettRepository) {
		this.giornataDettRepository = giornataDettRepository;
	}

	public List<FcGiornataDett> findAll() {
		return (List<FcGiornataDett>) giornataDettRepository.findAll();
	}

	public List<FcGiornataDett> findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(
			FcAttore attore, FcGiornataInfo giornataInfo) {
		return giornataDettRepository.findByFcAttoreAndFcGiornataInfoOrderByOrdinamentoAsc(attore, giornataInfo);
	}

	public FcGiornataDett save(FcGiornataDett c) {
		FcGiornataDett fcGiornataDett = null;
		try {
			fcGiornataDett = giornataDettRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcGiornataDett;
	}

	public void delete(FcGiornataDett c) {
        try {
			giornataDettRepository.delete(c);
        } catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

}