package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcGiornataInfo;

@Service
public class GiornataInfoService{

	private final GiornataInfoRepository giornataInfoRepository;

	@Autowired
	public GiornataInfoService(GiornataInfoRepository giornataInfoRepository) {
		this.giornataInfoRepository = giornataInfoRepository;
	}

	public List<FcGiornataInfo> findAll() {
		List<FcGiornataInfo> l = (List<FcGiornataInfo>) giornataInfoRepository.findAll();
		return l;
	}

	public FcGiornataInfo findByCodiceGiornata(Integer gg) {
		FcGiornataInfo info = giornataInfoRepository.findByCodiceGiornata(gg);
		return info;
	}

	public FcGiornataInfo findByDescGiornataFc(String descGiornataFc) {
		FcGiornataInfo fcGiornataInfo = giornataInfoRepository.findByDescGiornataFc(descGiornataFc);
		return fcGiornataInfo;
	}

	public List<FcGiornataInfo> findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(
			Integer from, Integer to) {
		List<FcGiornataInfo> l = (List<FcGiornataInfo>) giornataInfoRepository.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
		return l;
	}

	public FcGiornataInfo updateGiornataInfo(FcGiornataInfo giornataInfo) {
		FcGiornataInfo fcGiornataInfo = null;
		try {
			fcGiornataInfo = giornataInfoRepository.save(giornataInfo);
		} catch (Exception ex) {

		}
		return fcGiornataInfo;
	}

	public String deleteGiornataInfo(FcGiornataInfo giornataInfo) {
		String id = "";
		try {
			giornataInfoRepository.delete(giornataInfo);
			id = "" + giornataInfo.getCodiceGiornata();
		} catch (Exception ex) {
			return "Error delete giornataInfo: " + ex.toString();
		}
		return "giornataInfo succesfully delete with id = " + id;
	}

}