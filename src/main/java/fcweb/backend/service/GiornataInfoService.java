package fcweb.backend.service;

import java.util.List;

import org.jfree.util.Log;
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
		return (List<FcGiornataInfo>) giornataInfoRepository.findAll();
	}

	public FcGiornataInfo findByCodiceGiornata(Integer gg) {
		return giornataInfoRepository.findByCodiceGiornata(gg);
	}

	public FcGiornataInfo findByDescGiornataFc(String descGiornataFc) {
		return giornataInfoRepository.findByDescGiornataFc(descGiornataFc);
	}

	public List<FcGiornataInfo> findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(
			Integer from, Integer to) {
		return (List<FcGiornataInfo>) giornataInfoRepository.findByCodiceGiornataGreaterThanEqualAndCodiceGiornataLessThanEqual(from, to);
	}

	public FcGiornataInfo updateGiornataInfo(FcGiornataInfo giornataInfo) {
		FcGiornataInfo fcGiornataInfo = null;
		try {
			fcGiornataInfo = giornataInfoRepository.save(giornataInfo);
		} catch (Exception ex) {
			Log.error(ex.getMessage());
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