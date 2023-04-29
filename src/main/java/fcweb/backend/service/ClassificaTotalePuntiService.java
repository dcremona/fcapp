package fcweb.backend.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fcweb.backend.data.ClassificaBean;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcClassificaTotPt;
import fcweb.backend.data.entity.FcClassificaTotPtId;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.utils.Costants;

@Service
public class ClassificaTotalePuntiService{

	private final ClassificaTotalePuntiRepository classificaTotalePuntiRepository;

	@Autowired
	public ClassificaTotalePuntiService(
			ClassificaTotalePuntiRepository classificaTotalePuntiRepository) {
		this.classificaTotalePuntiRepository = classificaTotalePuntiRepository;
	}

	public List<FcClassificaTotPt> findByFcCampionatoAndFcGiornataInfo(
			FcCampionato campionato,FcGiornataInfo giornataInfo) {
		List<FcClassificaTotPt> l = (List<FcClassificaTotPt>) classificaTotalePuntiRepository.findByFcCampionatoAndFcGiornataInfo(campionato,giornataInfo);
		return l;
	}
	public List<FcClassificaTotPt> findAll() {
		List<FcClassificaTotPt> l = (List<FcClassificaTotPt>) classificaTotalePuntiRepository.findAll();
		return l;
	}

	public FcClassificaTotPt findByFcCampionatoAndFcAttoreAndFcGiornataInfo(
			FcCampionato campionato, FcAttore attore,
			FcGiornataInfo giornataInfo) {
		FcClassificaTotPt l = classificaTotalePuntiRepository.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
		return l;
	}

	public FcClassificaTotPt findByFcAttoreAndFcGiornataInfo(FcAttore attore,
			FcGiornataInfo giornataInfo) {
		FcClassificaTotPt l = (FcClassificaTotPt) classificaTotalePuntiRepository.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
		return l;
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<ClassificaBean> getModelClassifica(int curr_gg) {

		String in_gg = "";
		if (curr_gg >= 1) {
			in_gg += "1";
		}
		if (curr_gg > 2) {
			in_gg += ",2";
		}
		if (curr_gg > 3) {
			in_gg += ",3";
		}
		if (curr_gg > 4) {
			in_gg += ",4";
		}
		if (curr_gg > 5) {
			in_gg += ",5";
		}
		if (curr_gg > 6) {
			in_gg += ",6";
		}
		if (curr_gg >= 7) {
			in_gg += ",7";
		}

		String query = " SELECT ";
		query += " B.DESC_ATTORE AS squadra, ";
		query += " SUM(IF(id_giornata in (" + in_gg + "),  TOT_PT, 0)) AS  tot_pt, ";
		query += " SUM(IF(id_giornata = 1, TOT_PT, 0)) AS  punti_giornata_1, ";
		query += " SUM(IF(id_giornata = 2, TOT_PT, 0)) AS  punti_giornata_2, ";
		query += " SUM(IF(id_giornata = 3, TOT_PT, 0)) AS  punti_giornata_3, ";
		query += " SUM(IF(id_giornata = 4, TOT_PT, 0)) AS  punti_giornata_4, ";
		query += " SUM(IF(id_giornata = 5, TOT_PT, 0)) AS  punti_giornata_5, ";
		query += " SUM(IF(id_giornata = 6, TOT_PT, 0)) AS  punti_giornata_6, ";
		query += " SUM(IF(id_giornata = 7, TOT_PT, 0)) AS  punti_giornata_7, ";
		query += " SUM( TOT_PT)  AS tot_punti_parziale ";
		query += " FROM ";
		query += " fc_classifica_tot_pt A, ";
		query += " fc_attore B ";
		query += " WHERE ";
		query += " A.ID_ATTORE = B.ID_ATTORE ";
		query += " GROUP BY ";
		query += " B.DESC_ATTORE ";
		query += " ORDER BY ";
		query += " 2 DESC ";

		List<ClassificaBean> cList = jdbcTemplate.query(query, new RowMapper<ClassificaBean>(){
			public ClassificaBean mapRow(ResultSet rs, int rownumber)
					throws SQLException {

				String squadra = rs.getString(1);
				Double tot_punti = rs.getDouble(2);
				Double punti_giornata_1 = rs.getDouble(3);
				Double punti_giornata_2 = rs.getDouble(4);
				Double punti_giornata_3 = rs.getDouble(5);
				Double punti_giornata_4 = rs.getDouble(6);
				Double punti_giornata_5 = rs.getDouble(7);
				Double punti_giornata_6 = rs.getDouble(8);
				Double punti_giornata_7 = rs.getDouble(9);
				Double tot_punti_parziale = rs.getDouble(10);

				ClassificaBean bean = new ClassificaBean();
				bean.setSquadra(squadra);
				bean.setTotPunti(tot_punti);
				bean.setPuntiGiornata1(punti_giornata_1);
				bean.setPuntiGiornata2(punti_giornata_2);
				bean.setPuntiGiornata3(punti_giornata_3);
				bean.setPuntiGiornata4(punti_giornata_4);
				bean.setPuntiGiornata5(punti_giornata_5);
				bean.setPuntiGiornata6(punti_giornata_6);
				bean.setPuntiGiornata7(punti_giornata_7);
				bean.setTotPuntiParziale(tot_punti_parziale);

				return bean;
			}
		});

		return cList;
	}

	public List<ClassificaBean> getModelGrafico(String idAttoreA,
			String idAttoreB, FcCampionato campionato) {

		int start = campionato.getStart();
		int end = campionato.getEnd();
		int endClas = end - 5;

		String query = " SELECT ";
		query += " att.desc_attore, ";
		query += " gi.id_giornata_fc,  ";
		query += " ris.somma as punti,  ";
		query += " ris.somma2 / " + Costants.DIVISORE_100 + " as tot_pt  ";
		query += " FROM  ";
		query += " (  ";
		query += " select t.id_giornata, t.id_attore,  t.punti, (select  sum(u.punti) from fc_giornata_ris u where u.id_giornata <= t.id_giornata and u.id_attore = t.id_attore and u.id_giornata >=" + start + "  and u.id_giornata<=" + endClas + " )  as SOMMA,  ";
		query += " (select  sum(v.tot_pt_rosa) from  fc_classifica_tot_pt v where v.id_giornata <= t.id_giornata and v.id_attore = t.id_attore and v.id_giornata >=" + start + "  and v.id_giornata<=" + endClas + " )  as SOMMA2  ";
		query += " from fc_giornata_ris t ";
		query += " ) ";
		query += " ris, ";
		query += " fc_attore att, ";
		query += " fc_classifica_tot_pt p,  ";
		query += " fc_giornata_info gi  ";
		query += " WHERE att.id_attore= ris.id_attore  ";
		query += " and att.id_attore= p.id_attore  ";
		query += " and att.id_attore in (" + idAttoreA + "," + idAttoreB + ")  ";
		query += " and ris.id_giornata= p.id_giornata  ";
		query += " and gi.codice_giornata= p.id_giornata  ";
		query += " and p.id_giornata >=" + start + " and p.id_giornata<= " + end;
		query += " order by p.id_attore,p.id_giornata ";
		// System.out.println(query);
		List<ClassificaBean> cList = jdbcTemplate.query(query, new RowMapper<ClassificaBean>(){
			public ClassificaBean mapRow(ResultSet rs, int rownumber)
					throws SQLException {

				String squadra = rs.getString(1);
				String giornata = rs.getString(2);
				Double punti = rs.getDouble(3);
				Double totPunti = rs.getDouble(4);

				ClassificaBean bean = new ClassificaBean();
				bean.setSquadra(squadra);
				bean.setGiornata(giornata);
				bean.setPunti(punti);
				bean.setTotPunti(totPunti);

				return bean;
			}
		});

		return cList;
	}

	public List<ClassificaBean> getModelGraficoEm(String idAttoreA,
			String idAttoreB, FcCampionato campionato) {

		int start = campionato.getStart();
		int end = campionato.getEnd();

		String query = " SELECT ";
		query += " att.desc_attore, ";
		query += " gi.id_giornata_fc,  ";
		query += " ris.somma2 / " + Costants.DIVISORE_10 + " as tot_pt  ";
		query += " FROM  ";
		query += " ( select  ";
		query += "   t.id_giornata,  ";
		query += "   t.id_attore, ";
		query += "   (select  sum(v.tot_pt) from  fc_classifica_tot_pt v where v.id_giornata <= t.id_giornata and v.id_attore = t.id_attore)  as SOMMA2  ";
		query += " from fc_classifica_tot_pt t ";
		query += " ) ";
		query += " ris, ";
		query += " fc_attore att, ";
		query += " fc_classifica_tot_pt p,  ";
		query += " fc_giornata_info gi  ";
		query += " WHERE att.id_attore= ris.id_attore  ";
		query += " and att.id_attore= p.id_attore  ";
		query += " and att.id_attore in (" + idAttoreA + "," + idAttoreB + ")  ";
		query += " and ris.id_giornata= p.id_giornata  ";
		query += " and gi.codice_giornata= p.id_giornata  ";
		query += " and p.id_giornata >=" + start + " and p.id_giornata<= " + end;
		query += " order by p.id_attore,p.id_giornata ";

		List<ClassificaBean> cList = jdbcTemplate.query(query, new RowMapper<ClassificaBean>(){
			public ClassificaBean mapRow(ResultSet rs, int rownumber)
					throws SQLException {

				String squadra = rs.getString(1);
				String giornata = rs.getString(2);
				Double totPunti = rs.getDouble(3);

				ClassificaBean bean = new ClassificaBean();
				bean.setSquadra(squadra);
				bean.setGiornata(giornata);
				bean.setTotPunti(totPunti);

				return bean;
			}
		});

		return cList;
	}

	public String createEm(FcAttore attore, FcCampionato campionato,
			Double totPunti) {
		String id = "";
		try {
			FcClassificaTotPt clas = new FcClassificaTotPt();
			FcClassificaTotPtId classificaPK = new FcClassificaTotPtId();
			classificaPK.setIdAttore(attore.getIdAttore());
			classificaPK.setIdCampionato(campionato.getIdCampionato());
			clas.setId(classificaPK);
			clas.setTotPt(totPunti);
			classificaTotalePuntiRepository.save(clas);
			id = classificaPK.getIdGiornata() + " " + classificaPK.getIdAttore();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error creating the ClassificaTotalePuntiRepository: " + ex.toString();
		}
		return "ClassificaTotalePuntiRepository succesfully created with id = " + id;
	}

}