package fcweb.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import fcweb.backend.data.entity.FcGiornataInfo;

public interface GiornataInfoRepository extends CrudRepository<FcGiornataInfo, Long> {

    Page<FcGiornataInfo> findAll(Pageable pageable);

    Iterable<FcGiornataInfo> findAll(Sort sort);

    public FcGiornataInfo findByCodiceGiornata(Integer codiceGiornata);

    public FcGiornataInfo findByDescGiornataFc(String descGiornataFc);

    Iterable<FcGiornataInfo> findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(Integer from,
            Integer to);

}