package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataDettInfo;
import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataDettInfoService{

	private final GiornataDettInfoRepository giornataDettInfoRepository;

	@Autowired
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

}