package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcFormazione;
import fcweb.backend.data.entity.FcFormazioneId;
import fcweb.backend.data.entity.FcGiocatore;

public interface FormazioneRepository
		extends CrudRepository<FcFormazione, FcFormazioneId>{

	Page<FcFormazione> findAll(Pageable pageable);

	Iterable<FcFormazione> findAll(Sort sort);

	List<FcFormazione> findByFcCampionato(FcCampionato campionato);

	List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(
            FcCampionato campionato, FcAttore attore);

	List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
            FcCampionato campionato, FcAttore attore);

	List<FcFormazione> findByFcCampionatoAndFcGiocatore(
            FcCampionato campionato, FcGiocatore giocatore);

	List<FcFormazione> findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
            FcAttore attore);

}