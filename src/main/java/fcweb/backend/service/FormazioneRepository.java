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

public interface FormazioneRepository extends CrudRepository<FcFormazione, FcFormazioneId>{

	Page<FcFormazione> findAll(Pageable pageable);

	Iterable<FcFormazione> findAll(Sort sort);

	public List<FcFormazione> findByFcCampionato(FcCampionato campionato);

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByIdOrdinamentoAsc(
			FcCampionato campionato, FcAttore attore);

	public List<FcFormazione> findByFcCampionatoAndFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcCampionato campionato, FcAttore attore);

	public FcFormazione findByFcCampionatoAndFcAttoreAndFcGiocatore(FcCampionato campionato,
			FcAttore attore,FcGiocatore giocatore);

	public List<FcFormazione> findByFcCampionatoAndFcGiocatore(FcCampionato campionato,
			FcGiocatore giocatore);

	public List<FcFormazione> findByFcAttoreOrderByFcGiocatoreFcRuoloDescTotPagatoDesc(
			FcAttore attore);

}