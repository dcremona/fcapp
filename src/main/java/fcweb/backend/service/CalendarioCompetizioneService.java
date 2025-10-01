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
		List<FcCalendarioCompetizione> l = (List<FcCalendarioCompetizione>) calendarioTimRepository.findAll(sortByIdAsc());
		return l;
	}

	private Sort sortByIdAsc() {
		return Sort.by(Sort.Direction.ASC, "id");
	}

	public List<FcCalendarioCompetizione> findCustom(
			FcGiornataInfo fcGiornataInfo) {

		List<FcCalendarioCompetizione> l = null;
		if (fcGiornataInfo == null) {
			l = (List<FcCalendarioCompetizione>) calendarioTimRepository.findAll(sortByIdAsc());
		} else {
			l = calendarioTimRepository.findByIdGiornataOrderByDataAsc(fcGiornataInfo.getCodiceGiornata());
		}
		return l;
	}

	public List<FcCalendarioCompetizione> findByIdGiornata(int idGiornata) {
		List<FcCalendarioCompetizione> l = calendarioTimRepository.findByIdGiornata(idGiornata);
		return l;
	}

	public List<FcCalendarioCompetizione> findByIdGiornataOrderByDataAsc(
			int idGiornata) {
		List<FcCalendarioCompetizione> l = calendarioTimRepository.findByIdGiornataOrderByDataAsc(idGiornata);
		return l;
	}

	public List<FcCalendarioCompetizione> findByIdGiornataAndDataLessThanEqual(
			int idGiornata, LocalDateTime data) {
		List<FcCalendarioCompetizione> l = calendarioTimRepository.findByIdGiornataAndDataLessThanEqual(idGiornata, data);
		return l;
	}

	public FcCalendarioCompetizione updateCalendarioTim(
			FcCalendarioCompetizione caòemdarioTim) {
		FcCalendarioCompetizione fcCalendarioTim = null;
		try {
			fcCalendarioTim = calendarioTimRepository.save(caòemdarioTim);
		} catch (Exception ex) {
		}
		return fcCalendarioTim;
	}

	public String deleteCalendarioTim(FcCalendarioCompetizione caòemdarioTim) {
		String id = "";
		try {
			calendarioTimRepository.delete(caòemdarioTim);
			id = "" + caòemdarioTim.getId();
		} catch (Exception ex) {
			return "Error delete caledarioTim: " + ex.toString();
		}
		return "caledarioTim succesfully delete with id = " + id;
	}

}