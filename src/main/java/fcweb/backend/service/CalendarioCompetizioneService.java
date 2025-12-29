package fcweb.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcCalendarioCompetizione;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class CalendarioCompetizioneService{

	private final CalendarioCompetizioneRepository calendarioTimRepository;

	@Autowired
	public CalendarioCompetizioneService(
			CalendarioCompetizioneRepository calendarioTimRepository) {
		this.calendarioTimRepository = calendarioTimRepository;
	}

	public List<FcCalendarioCompetizione> findAll() {
		return (List<FcCalendarioCompetizione>) calendarioTimRepository.findAll(sortByIdAsc());
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}

	public List<FcCalendarioCompetizione> findCustom(
			FcGiornataInfo fcGiornataInfo) {

		List<FcCalendarioCompetizione> l;
		if (fcGiornataInfo == null) {
			l = (List<FcCalendarioCompetizione>) calendarioTimRepository.findAll(sortByIdAsc());
		} else {
			l = calendarioTimRepository.findByIdGiornataOrderByDataAsc(fcGiornataInfo.getCodiceGiornata());
		}
		return l;
	}

	public List<FcCalendarioCompetizione> findByIdGiornataOrderByDataAsc(
			int idGiornata) {
		return calendarioTimRepository.findByIdGiornataOrderByDataAsc(idGiornata);
	}

	public List<FcCalendarioCompetizione> findByIdGiornataAndDataLessThanEqual(
			int idGiornata, LocalDateTime data) {
		return calendarioTimRepository.findByIdGiornataAndDataLessThanEqual(idGiornata, data);
	}

	public FcCalendarioCompetizione updateCalendarioTim(
			FcCalendarioCompetizione calendarioTim) {
		FcCalendarioCompetizione fcCalendarioTim = null;
		try {
			fcCalendarioTim = calendarioTimRepository.save(calendarioTim);
		} catch (Exception ignored) {
		}
		return fcCalendarioTim;
	}

	public void deleteCalendarioTim(FcCalendarioCompetizione calendarioTim) {
        try {
			calendarioTimRepository.delete(calendarioTim);
        } catch (Exception ignored) {

		}
	}

}