package fcweb.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class CalendarioCompetizioneService{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final CalendarioCompetizioneRepository calendarioCompetizioneRepository;

	public CalendarioCompetizioneService(
			CalendarioCompetizioneRepository calendarioCompetizioneRepository) {
		this.calendarioCompetizioneRepository = calendarioCompetizioneRepository;
	}

	public List<FcCalendarioCompetizione> findAll() {
		return (List<FcCalendarioCompetizione>) calendarioCompetizioneRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}

	public List<FcCalendarioCompetizione> findCustom(
			FcGiornataInfo fcGiornataInfo) {

		List<FcCalendarioCompetizione> l;
		if (fcGiornataInfo == null) {
			l = (List<FcCalendarioCompetizione>) calendarioCompetizioneRepository.findAll(sortByIdAsc());
		} else {
			l = calendarioCompetizioneRepository.findByIdGiornataOrderByDataAsc(fcGiornataInfo.getCodiceGiornata());
		}
		return l;
	}
	
	public List<FcCalendarioCompetizione> findByIdGiornata(
			int idGiornata) {
		return calendarioCompetizioneRepository.findByIdGiornata(idGiornata);
	}

	public List<FcCalendarioCompetizione> findByIdGiornataOrderByDataAsc(
			int idGiornata) {
		return calendarioCompetizioneRepository.findByIdGiornataOrderByDataAsc(idGiornata);
	}

	public List<FcCalendarioCompetizione> findByIdGiornataAndDataLessThanEqual(
			int idGiornata, LocalDateTime data) {
		return calendarioCompetizioneRepository.findByIdGiornataAndDataLessThanEqual(idGiornata, data);
	}

	public FcCalendarioCompetizione updateCalendarioTim(
			FcCalendarioCompetizione calendarioTim) {
		FcCalendarioCompetizione fcCalendarioTim = null;
		try {
			fcCalendarioTim = calendarioCompetizioneRepository.save(calendarioTim);
		} catch (Exception ignored) {
		}
		return fcCalendarioTim;
	}

	public void deleteCalendarioTim(FcCalendarioCompetizione calendarioTim) {
        try {
        	calendarioCompetizioneRepository.delete(calendarioTim);
        } catch (Exception ignored) {

		}
	}
	
	public FcCalendarioCompetizione save(FcCalendarioCompetizione c) {
		FcCalendarioCompetizione fcCalendarioCompetizione = null;
		try {
			fcCalendarioCompetizione = calendarioCompetizioneRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcCalendarioCompetizione;
	}
	
	public void deleteAll() {
		calendarioCompetizioneRepository.deleteAll();	
	}


}