package common.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcPagelle;
import fcweb.utils.Costants;

public class Utils{

	private static Logger LOG = LoggerFactory.getLogger(Utils.class);

	public static boolean isValidVaadinSession() {
		if (VaadinSession.getCurrent().getAttribute("CAMPIONATO") == null || VaadinSession.getCurrent().getAttribute("ATTORE") == null) {
			LOG.info("isValidVaadinSession = false ");
			return false;
		}
		LOG.info("isValidVaadinSession = true ");
		return true;
	}

	public static String replaceString(String sText, String Old, String New) {
		String x1 = new String();
		String x2 = new String();

		int lunOld = Old.length();

		int P = sText.indexOf(Old);

		while (P != -1) {
			x1 = sText.substring(0, P);
			x2 = sText.substring(P + lunOld);
			sText = x1 + New + x2;
			P = sText.indexOf(Old, (x1 + New).length());
		}

		return sText;
	}

	/**
	 * Method getColorFromProperties.
	 * 
	 * @param color
	 * @return Color
	 */
	public static Color getColorFromProperties(String color) {

		StringTokenizer st = new StringTokenizer(color,"|");
		if (st.countTokens() == 3) {
			int[] rgb = new int[3];
			int conta = 0;
			while (st.hasMoreTokens()) {
				rgb[conta] = Integer.parseInt(st.nextToken().trim());
				conta++;
			}
			return new Color(rgb[0],rgb[1],rgb[2]);
		} else if (color.equals("black"))
			return Color.black;
		else if (color.equals("white"))
			return Color.white;
		else if (color.equals("lightGray"))
			return Color.lightGray;
		else if (color.equals("gray"))
			return Color.gray;
		else if (color.equals("darkGray"))
			return Color.darkGray;
		else if (color.equals("red"))
			return Color.red;
		else if (color.equals("pink"))
			return Color.pink;
		else if (color.equals("orange"))
			return Color.orange;
		else if (color.equals("yellow"))
			return Color.yellow;
		else if (color.equals("green"))
			return Color.green;
		else if (color.equals("magenta"))
			return Color.magenta;
		else if (color.equals("cyan"))
			return Color.cyan;
		else if (color.equals("blue"))
			return Color.blue;
		else
			return Color.black;
	}

	/**
	 * @param filePath
	 * @return
	 */
	public static Properties readFileProperties(String filePath)
			throws IOException {
		Properties props = new Properties();
		BufferedInputStream bufferedInputStream = null;
		bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
		props.load(bufferedInputStream);
		bufferedInputStream.close();
		return props;
	}

	public static void writeConfigFile(String filePath, String header)
			throws IOException {

		Properties props = new Properties();
		OutputStream outputStream = new FileOutputStream(filePath);
		props.store(outputStream, header);
	}

	/**
	 * @param sTime1
	 * @return
	 */
	public static long timeInSecond(String sTime1) {

		int iHour = 0;
		int iMin = 0;
		int iSec = 0;
		// int frm = 0;
		try {
			iHour = Integer.parseInt(sTime1.substring(0, 2)) + 1;
			iMin = Integer.parseInt(sTime1.substring(3, 5));
			if (sTime1.length() == 8) {
				iSec = Integer.parseInt(sTime1.substring(6, 8));
			} else if (sTime1.length() > 8) {
				iSec = Integer.parseInt(sTime1.substring(6, 8));
			}
		} catch (NumberFormatException exNum) {
			LOG.error(exNum.getMessage());
			return -1;
		}
		Calendar cldTime1 = Calendar.getInstance();
		cldTime1.clear();
		cldTime1.set(Calendar.HOUR, iHour);
		cldTime1.set(Calendar.MINUTE, iMin);
		cldTime1.set(Calendar.SECOND, iSec);

		long second = cldTime1.getTimeInMillis() / 1000;

		return second;
	}

	public static boolean isValidDate(int giorno, int mese, int anno) {
		boolean dataValida = false;

		if (anno > 1900) {

			int nGiorniMese[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
			boolean annoBisestile;

			annoBisestile = (anno % 4 == 0 && anno % 100 != 0) || anno % 400 == 0;
			if (mese < 13 && mese > 0) {
				if (giorno <= nGiorniMese[mese - 1] && giorno > 0) {
					dataValida = true;
				} else {
					if (mese == 2 && annoBisestile && giorno == 29) {
						dataValida = true;
					} else {
						dataValida = false;
					}
				}
			} else {
				dataValida = false;
			}
		}

		return dataValida;
	}

	public static int isValidDate(String text) {

		if (text == null || text.trim().equals("") || text.trim().equals("  /  /    ")) {
			return 0; // data vuota
		}

		if (text.charAt(2) != '/' || text.charAt(5) != '/') {
			return 1; // data incompleta
		}

		int m = 0,d = 0,y = 0;
		try {
			m = Integer.parseInt(text.substring(3, 5));
			d = Integer.parseInt(text.substring(0, 2));
			y = Integer.parseInt(text.substring(6, 10));
		} catch (NumberFormatException e) {
			LOG.error(e.getMessage());
			return 1; // data incompleta
		}
		if (!isValidDate(d, m, y)) {
			return 2; // data completa ma errata
		}
		return 3; // data esistente
	}

	public static String formatDate(Date d, String newFormat) {

		String item = "";
		try {

			if (d != null) {
				SimpleDateFormat formatter = new SimpleDateFormat(newFormat);
				item = formatter.format(d);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			item = "";
		}
		return item;
	}

	public static String formatLocalDateTime(LocalDateTime d,
			String newFormat) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newFormat);
		String currentDataGiornata = d.format(formatter);
		return currentDataGiornata;
	}

	// public static boolean[] tornaArrayBoolean(String sArray, String div) {
	// StringTokenizer st = new StringTokenizer(sArray,div);
	// boolean[] vet = new boolean[st.countTokens()];
	// int conta = 0;
	// while (st.hasMoreTokens()) {
	// vet[conta] = new Boolean(st.nextToken().trim()).booleanValue();
	// conta++;
	// }
	// return vet;
	// }

	public static String[] tornaArrayString(String sArray, String div) {
		StringTokenizer st = new StringTokenizer(sArray,div);
		String[] vet = new String[st.countTokens()];
		int conta = 0;
		while (st.hasMoreTokens()) {
			vet[conta] = st.nextToken().toString();
			conta++;
		}
		return vet;
	}

	public static boolean downloadFile(String fAddress, String filePath)
			throws Exception {

		int size = 1024;

		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int ByteRead = 0;
			Url = new URL(fAddress);

			uCon = Url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			outStream = new BufferedOutputStream(new FileOutputStream(filePath));
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
			}

			return true;

		} catch (Exception e) {
			LOG.error(e.getMessage());
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

			// Image image = ImageIO.read(is);
			// Image originalImage = image.getScaledInstance(40, 60,
			// Image.SCALE_DEFAULT);
			// BufferedImage bi = createResizedCopy(originalImage, 40, 60,
			// true);
			// ImageIO.write(bi, "png", new File(filePathOutput));

			resizeImage(is, filePathOutput, 40, 60);

			return true;

		} catch (Exception e) {
			LOG.error(e.getMessage());
			return false;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private static InputStream resizeImage(InputStream uploadedInputStream,
			String fileName, int width, int height) {

		try {
			BufferedImage image = ImageIO.read(uploadedInputStream);
			java.awt.Image originalImage = image.getScaledInstance(width, height, java.awt.Image.SCALE_DEFAULT);

			int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
			BufferedImage resizedImage = new BufferedImage(width,height,type);

			Graphics2D g2d = resizedImage.createGraphics();
			g2d.drawImage(originalImage, 0, 0, width, height, null);
			g2d.dispose();
			g2d.setComposite(AlphaComposite.Src);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			// ImageIO.write(resizedImage, fileName.split("\\.")[1],
			// byteArrayOutputStream);
			ImageIO.write(resizedImage, "png", new File(fileName));

			return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			LOG.error(e.getMessage());
			// Something is going wrong while resizing image
			return uploadedInputStream;
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
				LOG.error(e.getMessage());
			}
		}
		return null;
	}

	public static String buildInfoGiornata(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return "" + giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + "° Lega - " + giornataInfo.getCodiceGiornata() + "° Serie A) ";
		}
		return "ND";
	}

	public static String buildInfoGiornataMobile(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return "" + giornataInfo.getCodiceGiornata() + "° Serie A ";
		}
		return "ND";
	}

	public static String buildInfoGiornataHtml(FcGiornataInfo giornataInfo) {

		if (giornataInfo != null) {
			return "" + giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + " Lega - " + giornataInfo.getCodiceGiornata() + " Serie A) ";
		}
		return "ND";
	}

	public static String buildInfoGiornataEm(FcGiornataInfo giornataInfo,
			FcCampionato campionato) {

		if (giornataInfo != null) {
			return "" + giornataInfo.getDescGiornataFc() + " (" + giornataInfo.getIdGiornataFc() + "° Lega - " + giornataInfo.getCodiceGiornata() + "° " + campionato.getDescCampionato() + ") ";
		}
		return "ND";
	}

	public static Image getImage(String nomeImg, InputStream inputStream) {
		StreamResource resource = new StreamResource(nomeImg,() -> {
			return inputStream;
		});
		Image img = new Image(resource,"");
		return img;
	}

	public static int buildVoto(FcPagelle pagelle, boolean bRoundVoto) {

		String ID_RUOLO = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
		int VOTO_GIOCATORE = pagelle.getVotoGiocatore();

		int GOAL_REALIZZATO = pagelle.getGoalRealizzato();
		int GOAL_SUBITO = pagelle.getGoalSubito();

		int AMMONIZIONE = pagelle.getAmmonizione();
		int ESPULSO = pagelle.getEspulsione();

		int RF = pagelle.getRigoreFallito();
		int RP = pagelle.getRigoreParato();
		int AUT = pagelle.getAutorete();
		int ASSIST = pagelle.getAssist();

		int G = 0;
		int CS = 0;
		int TS = 0;
		if (pagelle.getG() != null) {
			G = pagelle.getG().intValue();
		}
		if (pagelle.getCs() != null) {
			CS = pagelle.getCs().intValue();
		}
		if (pagelle.getTs() != null) {
			TS = pagelle.getTs().intValue();
		}

		if (GOAL_REALIZZATO != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE + (GOAL_REALIZZATO * Costants.DIV_3_0);
		}
		if (GOAL_SUBITO != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE - (GOAL_SUBITO * Costants.DIV_1_0);
		}
		if (AMMONIZIONE != 0 && VOTO_GIOCATORE != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE - Costants.DIV_0_5;
		}
		if (ESPULSO != 0) {
			if (AMMONIZIONE != 0) {
				VOTO_GIOCATORE = VOTO_GIOCATORE + Costants.DIV_0_5;
			}
			VOTO_GIOCATORE = VOTO_GIOCATORE - Costants.DIV_1_0;
		}
		/*
		 * if (RS!=0) { VOTO_GIOCATORE = VOTO_GIOCATORE - (RS*DIV_10); }
		 */
		if (RF != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE - (RF * Costants.DIV_3_0);
		}
		if (RP != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE + (RP * Costants.DIV_3_0);
		}
		if (AUT != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE - (AUT * Costants.DIV_2_0);
		}
		if (ASSIST != 0) {
			VOTO_GIOCATORE = VOTO_GIOCATORE + (ASSIST * Costants.DIV_1_0);
		}
		if (ID_RUOLO.equals("P") && GOAL_SUBITO == 0 && ESPULSO == 0 && VOTO_GIOCATORE != 0) {
			if (G != 0 && CS != 0 && TS != 0) {
				VOTO_GIOCATORE = VOTO_GIOCATORE + Costants.DIVISORE_100;
			}
		}
		LOG.debug("bRoundVoto          -----> " + bRoundVoto);
		LOG.debug("VOTO_GIOCATORE      -----> " + VOTO_GIOCATORE);
		if (bRoundVoto) {
			int roundVotoGiocatore = Utils.arrotonda(VOTO_GIOCATORE);
			LOG.debug("roundVotoGiocatore      -----> " + roundVotoGiocatore);
			return roundVotoGiocatore;
		} else {
			return VOTO_GIOCATORE;
		}
	}

	public static int arrotonda(int input) {

		BigDecimal bdInput = new BigDecimal(input);
		BigDecimal bd10 = new BigDecimal(Costants.DIVISORE_10);
		BigDecimal bd = bdInput.divide(bd10);
		LOG.debug(bd.toString());
		BigDecimal bd2 = Utils.roundBigDecimal(bd);
		LOG.debug(bd2.toPlainString());
		BigDecimal bd3 = bd2.multiply(bd10);
		LOG.debug("" + bd3.intValue());
		return bd3.intValue();
	}

	public static BigDecimal roundBigDecimal(final BigDecimal input) {
		return input.round(new MathContext(input.toBigInteger().toString().length(),RoundingMode.HALF_UP));
	}
}
