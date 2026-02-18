package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcMercatoDettInfo;

@Service
public class MercatoInfoService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final MercatoInfoRepository mercatoInfoRepository;

	public MercatoInfoService(MercatoInfoRepository mercatoInfoRepository) {
		this.mercatoInfoRepository = mercatoInfoRepository;
	}

	public List<FcMercatoDettInfo> findAll() {
		return (List<FcMercatoDettInfo>) mercatoInfoRepository.findAll(sortByGiornataInfoAndattoreAsc());
	}

	private Sort sortByGiornataInfoAndattoreAsc() {
		return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcAttore");
	}

	public List<FcMercatoDettInfo> findByFcAttoreOrderByFcGiornataInfoAsc(
			FcAttore fcAttore) {
		return mercatoInfoRepository.findByFcAttoreOrderByFcGiornataInfoAsc(fcAttore);
	}

	public void insertMercatoDettInfo(
			FcMercatoDettInfo mercatoInfo) {
        try {
            mercatoInfoRepository.save(mercatoInfo);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}
}