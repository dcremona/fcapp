package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataDettInfoService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final GiornataDettInfoRepository giornataDettInfoRepository;

	public GiornataDettInfoService(
			GiornataDettInfoRepository giornataDettInfoRepository) {
		this.giornataDettInfoRepository = giornataDettInfoRepository;
	}

	public List<FcGiornataDettInfo> findAll() {
		return (List<FcGiornataDettInfo>) giornataDettInfoRepository.findAll();
	}

	public FcGiornataDettInfo findByFcAttoreAndFcGiornataInfo(FcAttore attore,
			FcGiornataInfo giornataInfo) {
		return giornataDettInfoRepository.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
	}

	public FcGiornataDettInfo save(FcGiornataDettInfo c) {
		FcGiornataDettInfo fcGiornataDettInfo = null;
		try {
			fcGiornataDettInfo = giornataDettInfoRepository.save(c);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcGiornataDettInfo;
	}

}