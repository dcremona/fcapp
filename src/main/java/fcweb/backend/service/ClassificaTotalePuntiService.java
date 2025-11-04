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
			FcCampionato campionato, FcGiornataInfo giornataInfo) {
		return classificaTotalePuntiRepository.findByFcCampionatoAndFcGiornataInfo(campionato, giornataInfo);
	}

	public List<FcClassificaTotPt> findAll() {
		return (List<FcClassificaTotPt>) classificaTotalePuntiRepository.findAll();
	}

	public FcClassificaTotPt findByFcCampionatoAndFcAttoreAndFcGiornataInfo(
			FcCampionato campionato, FcAttore attore,
			FcGiornataInfo giornataInfo) {
		return classificaTotalePuntiRepository.findByFcCampionatoAndFcAttoreAndFcGiornataInfo(campionato, attore, giornataInfo);
	}

	public FcClassificaTotPt findByFcAttoreAndFcGiornataInfo(FcAttore attore,
			FcGiornataInfo giornataInfo) {
		return classificaTotalePuntiRepository.findByFcAttoreAndFcGiornataInfo(attore, giornataInfo);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<ClassificaBean> getModelClassifica(int currGG) {

		String inGG = "";
		if (currGG >= 1) {
			inGG += "1";
		}
		if (currGG > 2) {
			inGG += ",2";
		}
		if (currGG > 3) {
			inGG += ",3";
		}
		if (currGG > 4) {
			inGG += ",4";
		}
		if (currGG > 5) {
			inGG += ",5";
		}
		if (currGG > 6) {
			inGG += ",6";
		}
		if (currGG >= 7) {
			inGG += ",7";
		}

		String query = " SELECT ";
		query += " B.DESC_ATTORE AS squadra, ";
		query += " SUM(IF(id_giornata in (" + inGG + "),  TOT_PT, 0)) AS  tot_pt, ";
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
			@Override
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
		query += " ris.somma2 / " + Costants.DIVISORE_100 + " as tot_pt,  ";
		query += " ris.somma3 as pt_tvst  ";
		query += " FROM  ";
		query += " (  ";
		query += " select t.id_giornata, t.id_attore,  t.punti, ";
		query += " (select  sum(u.punti) from fc_giornata_ris u where u.id_giornata <= t.id_giornata and u.id_attore = t.id_attore and u.id_giornata >=" + start + "  and u.id_giornata<=" + endClas + " )  as SOMMA,  ";
		query += " (select  sum(v.tot_pt_rosa) from  fc_classifica_tot_pt v where v.id_giornata <= t.id_giornata and v.id_attore = t.id_attore and v.id_giornata >=" + start + "  and v.id_giornata<=" + end + " )  as SOMMA2,  ";
		query += " (select  sum(v.pt_tvst) from  fc_classifica_tot_pt v where v.id_giornata <= t.id_giornata and v.id_attore = t.id_attore and v.id_giornata >=" + start + "  and v.id_giornata<=" + end + " )  as SOMMA3  ";
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
			@Override
			public ClassificaBean mapRow(ResultSet rs, int rownumber)
					throws SQLException {

				String squadra = rs.getString(1);
				String giornata = rs.getString(2);
				Double punti = rs.getDouble(3);
				Double totPunti = rs.getDouble(4);
				Double ptTvst = rs.getDouble(5);

				ClassificaBean bean = new ClassificaBean();
				bean.setSquadra(squadra);
				bean.setGiornata(giornata);
				bean.setPunti(punti);
				bean.setTotPunti(totPunti);
				bean.setPtTvst(ptTvst);

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
			@Override
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