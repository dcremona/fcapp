package fcapp.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiornataDettInfo;
import fcapp.backend.data.entity.FcGiornataDettInfoId;
import fcapp.backend.data.entity.FcGiornataInfo;

public interface GiornataDettInfoRepository
		extends CrudRepository<FcGiornataDettInfo, FcGiornataDettInfoId>{

	Page<FcGiornataDettInfo> findAll(Pageable pageable);

	Iterable<FcGiornataDettInfo> findAll(Sort sort);

	FcGiornataDettInfo findByFcAttoreAndFcGiornataInfo(FcAttore attore,
                                                       FcGiornataInfo giornataInfo);

}