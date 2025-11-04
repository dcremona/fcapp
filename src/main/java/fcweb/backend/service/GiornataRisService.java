package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcGiornataRis;

@Service
public class GiornataRisService{

	private final GiornataRisRepository giornataRisRepository;

	@Autowired
	public GiornataRisService(GiornataRisRepository giornataRisRepository) {
		this.giornataRisRepository = giornataRisRepository;
	}

	public List<FcGiornataRis> findAll() {
		return (List<FcGiornataRis>) giornataRisRepository.findAll();
	}

	public List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoDesc(
			FcAttore fcAttore) {
		return giornataRisRepository.findByFcAttoreOrderByFcGiornataInfoDesc(fcAttore);
	}

	public List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoAsc(
			FcAttore fcAttore) {
		return giornataRisRepository.findByFcAttoreOrderByFcGiornataInfoAsc(fcAttore);
	}

	public List<FcGiornataRis> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqual(
			FcGiornataInfo start, FcGiornataInfo end) {
		return giornataRisRepository.findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqual(start, end);
	}

}