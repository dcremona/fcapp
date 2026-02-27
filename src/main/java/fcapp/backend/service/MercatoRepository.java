package fcapp.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcMercatoDett;

public interface MercatoRepository extends CrudRepository<FcMercatoDett, Long>{

	Page<FcMercatoDett> findAll(Pageable pageable);

	Iterable<FcMercatoDett> findAll(Sort sort);

	List<FcMercatoDett> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
            FcGiornataInfo from, FcGiornataInfo to, FcAttore attore);

	List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(
            FcAttore attore);

}