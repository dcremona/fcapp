package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcGiornata;
import fcweb.backend.data.entity.FcGiornataId;
import fcweb.backend.data.entity.FcGiornataInfo;

public interface GiornataRepository
		extends CrudRepository<FcGiornata, FcGiornataId>{

	Page<FcGiornata> findAll(Pageable pageable);

	Iterable<FcGiornata> findAll(Sort sort);

	List<FcGiornata> findByFcGiornataInfo(FcGiornataInfo giornataInfo);

	List<FcGiornata> findByFcGiornataInfoOrderByFcTipoGiornata(
            FcGiornataInfo giornataInfo);

	List<FcGiornata> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualOrderByFcGiornataInfo(
            FcGiornataInfo start, FcGiornataInfo end);

}