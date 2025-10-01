package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataDettInfoId;
import fcweb.backend.data.entity.FcGiornataInfo;

public interface GiornataDettInfoRepository
		extends CrudRepository<FcGiornataDettInfo, FcGiornataDettInfoId>{

	Page<FcGiornataDettInfo> findAll(Pageable pageable);

	Iterable<FcGiornataDettInfo> findAll(Sort sort);

	public FcGiornataDettInfo findByFcAttoreAndFcGiornataInfo(FcAttore attore,
			FcGiornataInfo giornataInfo);

}