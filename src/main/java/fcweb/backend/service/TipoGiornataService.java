package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcTipoGiornata;

@Service
public class TipoGiornataService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final TipoGiornataRepository tipoGiornataRepository;

	public TipoGiornataService(TipoGiornataRepository tipoGiornataRepository) {
		this.tipoGiornataRepository = tipoGiornataRepository;
	}

	public List<FcTipoGiornata> findAll() {
		return (List<FcTipoGiornata>) tipoGiornataRepository.findAll(sortByIdTipoGiornataDesc());
	}

	private Sort sortByIdTipoGiornataDesc() {
		return Sort.by(Sort.Direction.DESC, "idTipoGiornata");
	}

	public FcTipoGiornata save(FcTipoGiornata c) {
		FcTipoGiornata fcTipoGiornata = null;
		try {
			fcTipoGiornata = tipoGiornataRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcTipoGiornata;
	}

}