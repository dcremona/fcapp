package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;

@Service
public class MercatoService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final MercatoRepository mercatoRepository;

	public MercatoService(MercatoRepository mercatoRepository) {
		this.mercatoRepository = mercatoRepository;
	}

	public List<FcMercatoDett> findAll() {
		return (List<FcMercatoDett>) mercatoRepository.findAll(sortByGiornataInfoAndattoreAsc());
	}

	public List<FcMercatoDett> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
			FcGiornataInfo from, FcGiornataInfo to, FcAttore attore) {
		return mercatoRepository.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(from, to, attore);
	}

	public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(
			FcAttore attore) {
		return mercatoRepository.findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(attore);
	}

	public FcMercatoDett insertMercatoDett(FcMercatoDett c) {
		FcMercatoDett fcMercatoDett = null;
		try {
			fcMercatoDett = mercatoRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcMercatoDett;
	}

	public void deleteMercatoDett(FcMercatoDett c) {
        try {
			mercatoRepository.delete(c);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

	private Sort sortByGiornataInfoAndattoreAsc() {
		return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcAttore", "id");
	}

}