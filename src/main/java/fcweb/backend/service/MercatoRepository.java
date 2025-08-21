package fcweb.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;

public interface MercatoRepository extends CrudRepository<FcMercatoDett, Long> {

    Page<FcMercatoDett> findAll(Pageable pageable);

    Iterable<FcMercatoDett> findAll(Sort sort);

    public List<FcMercatoDett> findByFcGiornataInfo(FcGiornataInfo fcGiornataInfo);

    // public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoAsc(FcAttore
    // attore);

    public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDesc(FcAttore attore);

    public List<FcMercatoDett> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
            FcGiornataInfo from, FcGiornataInfo to, FcAttore attore);

    public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(FcAttore attore);

}