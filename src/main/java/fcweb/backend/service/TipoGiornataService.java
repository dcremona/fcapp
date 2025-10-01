package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcTipoGiornata;

@Service
public class TipoGiornataService{

	private final TipoGiornataRepository tipoGiornataRepository;

	@Autowired
	public TipoGiornataService(TipoGiornataRepository tipoGiornataRepository) {
		this.tipoGiornataRepository = tipoGiornataRepository;
	}

	public List<FcTipoGiornata> findAll() {
		List<FcTipoGiornata> l = (List<FcTipoGiornata>) tipoGiornataRepository.findAll(sortByIdTipoGiornataDesc());
		return l;
	}

	private Sort sortByIdTipoGiornataDesc() {
		return Sort.by(Sort.Direction.DESC, "idTipoGiornata");
	}

}