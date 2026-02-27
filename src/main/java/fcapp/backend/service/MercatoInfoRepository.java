package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcMercatoDettInfo;

public interface MercatoInfoRepository
		extends CrudRepository<FcMercatoDettInfo, Long>{

	Page<FcMercatoDettInfo> findAll(Pageable pageable);

	Iterable<FcMercatoDettInfo> findAll(Sort sort);

	List<FcMercatoDettInfo> findByFcAttoreOrderByFcGiornataInfoAsc(
            FcAttore attore);

}