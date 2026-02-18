package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcSquadra;

@Service
public class SquadraService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final SquadraRepository squadraRepository;

	public SquadraService(SquadraRepository squadraRepository) {
		this.squadraRepository = squadraRepository;
	}

	public List<FcSquadra> findAll() {
		return (List<FcSquadra>) squadraRepository.findAll(sortByIdSquadra());
	}

	public FcSquadra findByNomeSquadra(String nomeSquadra) {
		return squadraRepository.findByNomeSquadra(nomeSquadra);
	}

	public FcSquadra findByIdSquadra(int idSquadra) {
		return squadraRepository.findByIdSquadra(idSquadra);
	}

	private Sort sortByIdSquadra() {
		return Sort.by(Sort.Direction.ASC, "idSquadra");
	}

	public FcSquadra updateSquadra(FcSquadra c) {
		FcSquadra squadra = null;
		try {
			squadra = squadraRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return squadra;
	}

	public void deleteSquadra(FcSquadra c) {
        try {
			squadraRepository.delete(c);
        } catch (Exception ignored) {

		}
	}

}