package fcweb.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcCalendarioCompetizione;

public interface CalendarioCompetizioneRepository
		extends CrudRepository<FcCalendarioCompetizione, Long>{

	Page<FcCalendarioCompetizione> findAll(Pageable pageable);

	Iterable<FcCalendarioCompetizione> findAll(Sort sort);

	public List<FcCalendarioCompetizione> findByIdGiornata(int idGiornata);

	public List<FcCalendarioCompetizione> findByIdGiornataOrderByDataAsc(int idGiornata);

	public List<FcCalendarioCompetizione> findByIdGiornataAndDataGreaterThanEqual(
			int idGiornata, LocalDateTime data);

	public List<FcCalendarioCompetizione> findByIdGiornataAndDataLessThanEqual(
			int idGiornata, LocalDateTime data);

}