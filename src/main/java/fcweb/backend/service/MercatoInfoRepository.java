package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcMercatoDettInfo;

public interface MercatoInfoRepository extends CrudRepository<FcMercatoDettInfo, Long>{

	Page<FcMercatoDettInfo> findAll(Pageable pageable);

	Iterable<FcMercatoDettInfo> findAll(Sort sort);

	public List<FcMercatoDettInfo> findByFcAttoreOrderByFcGiornataInfoAsc(FcAttore attore);


}