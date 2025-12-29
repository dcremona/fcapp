package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDett;
import fcweb.backend.data.entity.FcGiornataDettId;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataDettService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final GiornataDettRepository giornataDettRepository;

	@Autowired
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

	public FcGiornataDett insertGiornataDett(FcGiornataDett c) {
		FcGiornataDett fcGiornataDett;
		try {

			FcGiornataDettId id = new FcGiornataDettId();
			id.setIdGiornata(c.getFcGiornataInfo().getCodiceGiornata());
			id.setIdAttore(c.getFcAttore().getIdAttore());
			id.setIdGiocatore(c.getFcGiocatore().getIdGiocatore());
			c.setId(id);
			fcGiornataDett = giornataDettRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return null;
		}
		return fcGiornataDett;
	}

	public FcGiornataDett updateGiornataDett(FcGiornataDett c) {
		FcGiornataDett fcGiornataDett;
		try {
			fcGiornataDett = giornataDettRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return null;
		}
		return fcGiornataDett;
	}

	public void deleteGiornataDett(FcGiornataDett c) {
        try {
			giornataDettRepository.delete(c);
        } catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

}