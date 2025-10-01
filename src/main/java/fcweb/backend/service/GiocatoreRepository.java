package fcweb.backend.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;

public interface GiocatoreRepository extends CrudRepository<FcGiocatore, Long>{

	Page<FcGiocatore> findAll(Pageable pageable);

	Iterable<FcGiocatore> findAll(Sort sort);

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo);

	public List<FcGiocatore> findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(
			FcRuolo ruolo, boolean flagAttivo, Collection<Integer> giocatore);

	public List<FcGiocatore> findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			boolean flagAttivo, FcSquadra squadra,
			Collection<Integer> giocatore);

	public List<FcGiocatore> findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(
			boolean flagAttivo, FcSquadra squadra);

	public List<FcGiocatore> findByFcRuoloOrderByQuotazioneDesc(FcRuolo ruolo);

	public List<FcGiocatore> findByFcSquadraOrderByQuotazioneDesc(
			FcSquadra squadra);

	public List<FcGiocatore> findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(
			FcRuolo ruolo, FcSquadra squadra);

	public List<FcGiocatore> findByCognGiocatoreStartingWith(
			String cognGiocatore);

	public FcGiocatore findByCognGiocatoreStartingWithAndFcSquadra(
			String nomeGiocatore, FcSquadra squadra);

	public FcGiocatore findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(
			String nomeGiocatore, FcSquadra squadra, FcRuolo ruolo);

	public List<FcGiocatore> findByCognGiocatoreContaining(
			String cognGiocatore);

	public FcGiocatore findByNomeGiocatoreContaining(String nomeGiocatore);

	public FcGiocatore findByNomeImg(String nomeImg);

	// public FcGiocatore findByNomeGiocatoreContainingAndFcSquadra(String
	// nomeGiocatore,FcSquadra squadra);
	//
	// public FcGiocatore
	// findByNomeGiocatoreContainingAndFcSquadraAndFcRuolo(String
	// nomeGiocatore,FcSquadra squadra,FcRuolo ruolo);

	public FcGiocatore findByIdGiocatore(int idGiocatore);

	public List<FcGiocatore> findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
			Collection<Integer> notIn);

}