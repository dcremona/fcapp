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

	List<FcGiocatore> findByFcRuoloAndFlagAttivoOrderByQuotazioneDesc(
            FcRuolo ruolo, boolean flagAttivo);

	List<FcGiocatore> findByFcRuoloAndFlagAttivoAndIdGiocatoreNotInOrderByQuotazioneDesc(
            FcRuolo ruolo, boolean flagAttivo, Collection<Integer> giocatore);

	List<FcGiocatore> findByFlagAttivoAndFcSquadraAndIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
            boolean flagAttivo, FcSquadra squadra,
            Collection<Integer> giocatore);

	List<FcGiocatore> findByFlagAttivoAndFcSquadraOrderByFcRuoloDescQuotazioneDesc(
            boolean flagAttivo, FcSquadra squadra);

	List<FcGiocatore> findByFcRuoloOrderByQuotazioneDesc(FcRuolo ruolo);

	List<FcGiocatore> findByFcSquadraOrderByQuotazioneDesc(
            FcSquadra squadra);

	List<FcGiocatore> findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(
            FcRuolo ruolo, FcSquadra squadra);

	FcGiocatore findByCognGiocatoreStartingWithAndFcSquadraAndFcRuolo(
            String nomeGiocatore, FcSquadra squadra, FcRuolo ruolo);

	List<FcGiocatore> findByCognGiocatoreContaining(
            String cognGiocatore);

	FcGiocatore findByNomeImg(String nomeImg);

	FcGiocatore findByIdGiocatore(int idGiocatore);

	List<FcGiocatore> findByIdGiocatoreNotInOrderByFcRuoloDescQuotazioneDesc(
            Collection<Integer> notIn);

}