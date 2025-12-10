package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcStatistiche;

@Service
public class StatisticheService{

	private final StatisticheRepository statisticheRepository;

	@Autowired
	public StatisticheService(StatisticheRepository statisticheRepository) {
		this.statisticheRepository = statisticheRepository;
	}

	public List<FcStatistiche> findAll() {
		return (List<FcStatistiche>) statisticheRepository.findAll(sortByIdRuoloDesc());
	}

	public List<FcStatistiche> findByFlagAttivo(boolean flagAttivo) {
		return statisticheRepository.findByFlagAttivo(flagAttivo);
	}

	private Sort sortByIdRuoloDesc() {
		return Sort.by(Sort.Direction.DESC, "idRuolo");
	}

	public FcStatistiche updateStatistiche(FcStatistiche statistiche) {
		FcStatistiche fcStatistiche = null;
		try {
			fcStatistiche = statisticheRepository.save(statistiche);
		} catch (Exception ex) {
		}
		return fcStatistiche;
	}

	public String deleteStatistiche(FcStatistiche statistiche) {
		String id = "";
		try {
			statisticheRepository.delete(statistiche);
			id = "" + statistiche.getIdGiocatore();
		} catch (Exception ex) {
			return "Error delete FcStatistiche: " + ex.toString();
		}
		return "statistiche succesfully delete with id = " + id;
	}

}