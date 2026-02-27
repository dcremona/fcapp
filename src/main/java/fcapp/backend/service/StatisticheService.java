package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcStatistiche;

@Service
public class StatisticheService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final StatisticheRepository statisticheRepository;

	public StatisticheService(StatisticheRepository statisticheRepository) {
		this.statisticheRepository = statisticheRepository;
	}

	public List<FcStatistiche> findAll() {
		return (List<FcStatistiche>) statisticheRepository.findAll(sortByIdRuoloDesc());
	}

	private Sort sortByIdRuoloDesc() {
		return Sort.by(Sort.Direction.DESC, "idRuolo");
	}
	
	public FcStatistiche save(FcStatistiche c) {
		FcStatistiche fcStatistiche = null;
		try {
			fcStatistiche = statisticheRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcStatistiche;
	}


}