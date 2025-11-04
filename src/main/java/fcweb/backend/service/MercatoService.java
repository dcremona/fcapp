package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;

@Service
public class MercatoService{

	private final MercatoRepository mercatoRepository;

	@Autowired
	public MercatoService(MercatoRepository mercatoRepository) {
		this.mercatoRepository = mercatoRepository;
	}

	public List<FcMercatoDett> findAll() {
		return (List<FcMercatoDett>) mercatoRepository.findAll(sortByGiornataInfoAndattoreAsc());
	}

	public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDesc(
			FcAttore attore) {
		return mercatoRepository.findByFcAttoreOrderByFcGiornataInfoDesc(attore);
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
			return fcMercatoDett;
		}
		return fcMercatoDett;
	}

	public String deleteMercatoDett(FcMercatoDett c) {
		String id = "";
		try {
			mercatoRepository.delete(c);
			id = "" + c.getId();
		} catch (Exception ex) {
			return "Error delete : " + ex.toString();
		}
		return "FcMercatoDett succesfully delete with id = " + id;
	}

	private Sort sortByGiornataInfoAndattoreAsc() {
		return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcAttore", "id");
	}

}