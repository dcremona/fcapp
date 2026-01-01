package common.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.imageio.ImageIO;

import fcweb.backend.data.FormazioneJasper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.utils.Costants;
import fcweb.utils.JasperReporUtils;

public class Utils{

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static boolean isValidVaadinSession() {
		if (VaadinSession.getCurrent().getAttribute("CAMPIONATO") == null || VaadinSession.getCurrent().getAttribute("ATTORE") == null) {
			log.info("isValidVaadinSession = false ");
			return false;
		}
		log.info("isValidVaadinSession = true ");
		return true;
	}

	public static String replaceString(String sText, String Old, String New) {
		String x1;
		String x2;

		int lunOld = Old.length();

		int p = sText.indexOf(Old);

		while (p != -1) {
			x1 = sText.substring(0, p);
			x2 = sText.substring(p + lunOld);
			sText = x1 + New + x2;
			p = sText.indexOf(Old, (x1 + New).length());
		}

		return sText;
	}

	public static String formatDate(Date d, String newFormat) {

		String item = "";
		try {

			if (d != null) {
				SimpleDateFormat formatter = new SimpleDateFormat(newFormat);
				item = formatter.format(d);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			item = "";
		}
		return item;
	}

	public static String formatLocalDateTime(LocalDateTime d,
			String newFormat) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newFormat);
		return d.format(formatter);
	}

	public static String[] tornaArrayString(String sArray, String div) {
		StringTokenizer st = new StringTokenizer(sArray,div);
		String[] vet = new String[st.countTokens()];
		int conta = 0;
		while (st.hasMoreTokens()) {
			vet[conta] = st.nextToken();
			conta++;
		}
		return vet;
	}

	public static boolean downloadFile(String fAddress, String filePath)
			throws Exception {

		int size = 1024;

		OutputStream outStream = null;
		URLConnection uCon;

		InputStream is = null;
		try {
			URL url;
			byte[] buf;
			int byteRead;
			url = new URL(fAddress);

			uCon = url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			outStream = new BufferedOutputStream(new FileOutputStream(filePath));
			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}

			return true;

		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (is != null) {
				is.close();
			}
			if (outStream != null) {
				outStream.close();
			}
		}
	}

	public static boolean buildFileSmall(String filePathInput,
			String filePathOutput) throws Exception {

		InputStream is = null;
		try {
			File initialFile = new File(filePathInput);
			is = new FileInputStream(initialFile);

			resizeImage(is, filePathOutput);
			return true;

		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private static void resizeImage(InputStream uploadedInputStream,
									String fileName) {

		try {
			BufferedImage image = ImageIO.read(uploadedInputStream);
			java.awt.Image originalImage = image.getScaledInstance(40, 60, java.awt.Image.SCALE_DEFAULT);

			int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
			BufferedImage resizedImage = new BufferedImage(40, 60,type);

			Graphics2D g2d = resizedImage.createGraphics();
			g2d.drawImage(originalImage, 0, 0, 40, 60, null);
			g2d.dispose();
			g2d.setComposite(AlphaComposite.Src);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            ImageIO.write(resizedImage, "png", new File(fileName));

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public static byte[] getImage(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			try {
				BufferedImage bufferedImage = ImageIO.read(file);
				ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "png", byteOutStream);
				return byteOutStream.toByteArray();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return new byte[0];
	}

	public static String buildInfoGiornataRight(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return " (" + giornataInfo.getIdGiornataFc() + "° Lega - " + giornataInfo.getCodiceGiornata() + "° Serie A) ";
		}
		return "ND";
	}

	public static String buildInfoGiornata(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + "° Lega - " + giornataInfo.getCodiceGiornata() + "° Serie A) ";
		}
		return "ND";
	}

	public static String buildInfoGiornataMobile(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return giornataInfo.getCodiceGiornata() + "° Serie A ";
		}
		return "ND";
	}

	public static String buildInfoGiornataHtml(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + " Lega - " + giornataInfo.getCodiceGiornata() + " Serie A) ";
		}
		return "ND";
	}

	public static String buildInfoGiornataEm(FcGiornataInfo giornataInfo,
			FcCampionato campionato) {

		if (giornataInfo != null) {
			return giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + "° Lega - " + giornataInfo.getCodiceGiornata() + "° " + campionato.getDescCampionato() + ") ";
		}
		return "ND";
	}

	public static int buildVoto(FcPagelle pagelle, boolean bRoundVoto) {

		String idRuolo = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
		int votoGiocatore = pagelle.getVotoGiocatore();

		int goalRealizzato = pagelle.getGoalRealizzato();
		int goalSubito = pagelle.getGoalSubito();

		int ammonizione = pagelle.getAmmonizione();
		int espulso = pagelle.getEspulsione();

		int rf = pagelle.getRigoreFallito();
		int rp = pagelle.getRigoreParato();
		int aut = pagelle.getAutorete();
		int assist = pagelle.getAssist();

		int g = 0;
		int cs = 0;
		int ts = 0;
		if (pagelle.getG() != null) {
			g = pagelle.getG().intValue();
		}
		if (pagelle.getCs() != null) {
			cs = pagelle.getCs().intValue();
		}
		if (pagelle.getTs() != null) {
			ts = pagelle.getTs().intValue();
		}

		if (goalRealizzato != 0) {
			votoGiocatore = votoGiocatore + (goalRealizzato * Costants.DIV_3_0);
		}
		if (goalSubito != 0) {
			votoGiocatore = votoGiocatore - (goalSubito * Costants.DIV_1_0);
		}
		if (ammonizione != 0 && votoGiocatore != 0) {
			votoGiocatore = votoGiocatore - Costants.DIV_0_5;
		}
		if (espulso != 0) {
			if (ammonizione != 0) {
				votoGiocatore = votoGiocatore + Costants.DIV_0_5;
			}
			votoGiocatore = votoGiocatore - Costants.DIV_1_0;
		}
		/*
		 * if (RS!=0) { VOTO_GIOCATORE = VOTO_GIOCATORE - (RS*DIV_10); }
		 */
		if (rf != 0) {
			votoGiocatore = votoGiocatore - (rf * Costants.DIV_3_0);
		}
		if (rp != 0) {
			votoGiocatore = votoGiocatore + (rp * Costants.DIV_3_0);
		}
		if (aut != 0) {
			votoGiocatore = votoGiocatore - (aut * Costants.DIV_2_0);
		}
		if (assist != 0) {
			votoGiocatore = votoGiocatore + (assist * Costants.DIV_1_0);
		}
		if (idRuolo.equals("P") && goalSubito == 0 && espulso == 0 && votoGiocatore != 0) {
			if (g != 0 && cs != 0 && ts != 0) {
				votoGiocatore = votoGiocatore + Costants.DIVISORE_100;
			}
		}
        log.debug("bRoundVoto          -----> {}", bRoundVoto);
        log.debug("VOTO_GIOCATORE      -----> {}", votoGiocatore);
		if (bRoundVoto) {
			int roundVotoGiocatore = Utils.arrotonda(votoGiocatore);
            log.debug("roundVotoGiocatore      -----> {}", roundVotoGiocatore);
			return roundVotoGiocatore;
		} else {
			return votoGiocatore;
		}
	}

	public static int arrotonda(int input) {

		BigDecimal bdInput = new BigDecimal(input);
		BigDecimal bd10 = new BigDecimal(Costants.DIVISORE_10);
		BigDecimal bd = bdInput.divide(bd10);
		// log.debug(bd.toString());
		BigDecimal bd2 = Utils.roundBigDecimal(bd);
		// log.debug(bd2.toPlainString());
		BigDecimal bd3 = bd2.multiply(bd10);
		// log.debug("" + bd3.intValue());
		return bd3.intValue();
	}

	public static BigDecimal roundBigDecimal(final BigDecimal input) {
		return input.round(new MathContext(input.toBigInteger().toString().length(),RoundingMode.HALF_UP));
	}

	public static String getNextDate(FcGiornataInfo giornataInfo) {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime currentDate = LocalDateTime.now();

		LocalDateTime dataAnticipo = getLocalDateTime(giornataInfo, now);
		LocalDateTime dataGiornata = giornataInfo.getDataGiornata();
		LocalDateTime dataPosticipo = giornataInfo.getDataPosticipo();

		if (dataGiornata != null) {
			currentDate = dataGiornata;

			log.info("now.getDayOfWeek() : {}", now.getDayOfWeek());
			if (dataAnticipo != null) {
				currentDate = dataAnticipo;
                log.info("dataGiornata.getDayOfWeek() : {}", dataGiornata.getDayOfWeek());
				if (now.isAfter(dataAnticipo) && now.getDayOfWeek() == dataGiornata.getDayOfWeek()) {
					currentDate = dataGiornata;
				}
			}

			if (dataPosticipo != null) {
                log.info("dataPosticipo.getDayOfWeek() : {}", dataPosticipo.getDayOfWeek());
				if (now.getDayOfWeek() == dataPosticipo.getDayOfWeek()) {
					currentDate = dataGiornata;
				}
			} else {
				if (dataAnticipo != null) {
					currentDate = dataAnticipo;
				}
			}
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		return currentDate.format(formatter);
	}

	private static @Nullable LocalDateTime getLocalDateTime(FcGiornataInfo giornataInfo, LocalDateTime now) {
		LocalDateTime dataAnticipo = null;
		LocalDateTime dataAnticipo1 = giornataInfo.getDataAnticipo1();
		LocalDateTime dataAnticipo2 = giornataInfo.getDataAnticipo2();
		if (dataAnticipo1 != null && dataAnticipo2 != null) {
			if (now.isBefore(dataAnticipo1)) {
				dataAnticipo = dataAnticipo1;
			} else if (now.isAfter(dataAnticipo1) && now.getDayOfWeek() == dataAnticipo2.getDayOfWeek()) {
				dataAnticipo = dataAnticipo2;
			} else {
				dataAnticipo = dataAnticipo1;
			}
		} else if (dataAnticipo1 == null && dataAnticipo2 != null) {
			dataAnticipo = dataAnticipo2;
		}
		return dataAnticipo;
	}

	public static long getMillisDiff(String nextDate, String fusoOrario)
			throws Exception {

		Calendar c = Calendar.getInstance();
		DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strDate1 = fmt.format(c.getTime());

        fmt.setLenient(false);
		Date d1 = fmt.parse(strDate1);
		Date d2 = fmt.parse(nextDate);

		// Calculates the difference in milliseconds.
		long millisDiff = d2.getTime() - d1.getTime();
		int seconds = (int) (millisDiff / 1000 % 60);
		int minutes = (int) (millisDiff / 60000 % 60);
		int hours = (int) (millisDiff / 3600000 % 24);
		int days = (int) (millisDiff / 86400000);

        log.info("{} days, ", days);
        log.info("{} hours, ", hours);
        log.info("{} minutes, ", minutes);
        log.info("{} seconds", seconds);

		long diffFuso = Long.parseLong(fusoOrario) * 3600000;
		millisDiff = millisDiff - diffFuso;

		if (millisDiff < 0) {
			millisDiff = 0;
		}

		return millisDiff;
	}

	public static Image getImage(String nomeImg, InputStream inputStream) {
		StreamResource resource = new StreamResource(nomeImg,() -> inputStream);
		return new Image(resource,"");
	}

    public static Image buildImage(String nomeImg, Resource r) {
		StreamResource resource = new StreamResource(nomeImg,() -> {
			InputStream inputStream = null;
			try {
				inputStream = r.getInputStream();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			return inputStream;
		});

		return new Image(resource,"");
	}

	public static StreamResource getStreamResource(String fiileName,
			Connection conn, Map<String, Object> hm, InputStream inputStream) {
		return new StreamResource(fiileName,() -> {
			try {
				return JasperReporUtils.runReportToPdf(inputStream, hm, conn);
			} catch (Exception ex2) {
				log.error(ex2.getMessage());
			}
			return null;
		});
	}

	public static StreamResource getStreamResource(String fiileName,ArrayList<FormazioneJasper> coll, Map<String, Object> hm, InputStream inputStream) {
		return new StreamResource(fiileName,() -> {
			try {
				return JasperReporUtils.runReportToPdf(inputStream, hm, coll);
			} catch (Exception ex2) {
				log.error(ex2.getMessage());
			}
			return null;
		});
	}

	public static StreamResource getStreamResource(String inputStreamName,
			byte[] bytes) {

		return new StreamResource(inputStreamName,() -> new ByteArrayInputStream(bytes));

	}

}
