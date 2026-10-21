package fcapp.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcPagelle;

/**
 * Utility methods for the Fantasy Calcio application.
 * <p>
 * This class is not meant to be instantiated.
 */
public final class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final int THUMB_WIDTH = 40;
    private static final int THUMB_HEIGHT = 60;
    private static final DateTimeFormatter DEFAULT_DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Utils() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // Session
    // -------------------------------------------------------------------------

    public static boolean isValidVaadinSession() {
        VaadinSession session = VaadinSession.getCurrent();
        boolean valid = session != null
                && session.getAttribute("CAMPIONATO") != null
                && session.getAttribute("ATTORE") != null;

        log.info("isValidVaadinSession = {}", valid);
        return valid;
    }

    // -------------------------------------------------------------------------
    // String helpers
    // -------------------------------------------------------------------------

    /**
     * Replaces all occurrences of {@code old} with {@code nuovo}.
     * Prefer {@link String#replace(CharSequence, CharSequence)} when possible.
     */
    public static String replaceString(String text, String old, String nuovo) {
        if (text == null || old == null || nuovo == null) {
            return text;
        }
        return text.replace(old, nuovo);
    }

    public static String[] tornaArrayString(String sArray, String div) {
        if (sArray == null || div == null) {
            return new String[0];
        }
        StringTokenizer st = new StringTokenizer(sArray, div);
        String[] result = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            result[i++] = st.nextToken();
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Date / time formatting
    // -------------------------------------------------------------------------

    public static String formatDate(Date d, String newFormat) {
        if (d == null || newFormat == null) {
            return "";
        }
        try {
            return new SimpleDateFormat(newFormat).format(d);
        } catch (Exception e) {
            log.error("formatDate failed: {}", e.getMessage());
            return "";
        }
    }

    public static String formatLocalDateTime(LocalDateTime d, String newFormat) {
        if (d == null || newFormat == null) {
            return "";
        }
        return d.format(DateTimeFormatter.ofPattern(newFormat));
    }

    // -------------------------------------------------------------------------
    // File / image utilities
    // -------------------------------------------------------------------------

    public static boolean downloadFile(String fAddress, String filePath) {
        final int bufferSize = 1024;
        try (InputStream is = new URL(fAddress).openConnection().getInputStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {

            byte[] buf = new byte[bufferSize];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return true;
        } catch (Exception e) {
            log.error("downloadFile failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean buildFileSmall(String filePathInput, String filePathOutput) {
        try (InputStream is = new FileInputStream(filePathInput)) {
            resizeImage(is, filePathOutput);
            return true;
        } catch (Exception e) {
            log.error("buildFileSmall failed: {}", e.getMessage());
            return false;
        }
    }

    private static void resizeImage(InputStream uploadedInputStream, String fileName) {
        try {
            BufferedImage image = ImageIO.read(uploadedInputStream);
            if (image == null) {
                log.warn("Could not read image from stream");
                return;
            }

            int type = (image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType();
            BufferedImage resized = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, type);

            Graphics2D g2d = resized.createGraphics();
            try {
                g2d.setComposite(AlphaComposite.Src);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(image, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, null);
            } finally {
                g2d.dispose();
            }

            ImageIO.write(resized, "png", new File(fileName));
        } catch (IOException e) {
            log.error("resizeImage failed: {}", e.getMessage());
        }
    }

    public static byte[] getImage(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return new byte[0];
        }
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return new byte[0];
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("getImage failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    // -------------------------------------------------------------------------
    // Giornata info builders
    // -------------------------------------------------------------------------

    public static String buildInfoGiornataRight(FcGiornataInfo giornataInfo) {
        if (giornataInfo == null) {
            return "ND";
        }
        return " (" + giornataInfo.getIdGiornataFc() + "° Lega - "
                + giornataInfo.getCodiceGiornata() + "° Serie A) ";
    }

    public static String buildInfoGiornata(FcGiornataInfo giornataInfo) {
        if (giornataInfo == null) {
            return "ND";
        }
        return giornataInfo.getDescGiornataFc() + " ("
                + giornataInfo.getIdGiornataFc() + "° Lega - "
                + giornataInfo.getCodiceGiornata() + "° Serie A) ";
    }

    public static String buildInfoGiornataMobile(FcGiornataInfo giornataInfo) {
        if (giornataInfo == null) {
            return "ND";
        }
        return giornataInfo.getCodiceGiornata() + "° Serie A ";
    }

    public static String buildInfoGiornataHtml(FcGiornataInfo giornataInfo) {
        if (giornataInfo == null) {
            return "ND";
        }
        return giornataInfo.getDescGiornataFc() + " ("
                + giornataInfo.getIdGiornataFc() + " Lega - "
                + giornataInfo.getCodiceGiornata() + " Serie A) ";
    }

    public static String buildInfoGiornataEm(FcGiornataInfo giornataInfo, FcCampionato campionato) {
        if (giornataInfo == null) {
            return "ND";
        }
        String campionatoDesc = (campionato != null) ? campionato.getDescCampionato() : "Serie A";
        return giornataInfo.getDescGiornataFc() + " ("
                + giornataInfo.getIdGiornataFc() + "° Lega - "
                + giornataInfo.getCodiceGiornata() + "° " + campionatoDesc + ") ";
    }

    // -------------------------------------------------------------------------
    // Voto calculation
    // -------------------------------------------------------------------------

    public static int buildVoto(FcPagelle pagelle, boolean bRoundVoto) {
        if (pagelle == null || pagelle.getFcGiocatore() == null) {
            return 0;
        }

        String idRuolo = pagelle.getFcGiocatore().getFcRuolo().getIdRuolo();
        int voto = pagelle.getVotoGiocatore();

        int goalRealizzato = pagelle.getGoalRealizzato();
        int goalSubito = pagelle.getGoalSubito();
        int ammonizione = pagelle.getAmmonizione();
        int espulso = pagelle.getEspulsione();
        int rf = pagelle.getRigoreFallito();
        int rp = pagelle.getRigoreParato();
        int aut = pagelle.getAutorete();
        int assist = pagelle.getAssist();

        int g = nullSafeInt(pagelle.getG());
        int cs = nullSafeInt(pagelle.getCs());
        int ts = nullSafeInt(pagelle.getTs());

        if (goalRealizzato != 0) {
            voto += goalRealizzato * Costants.DIV_3_0;
        }
        if (goalSubito != 0) {
            voto -= goalSubito * Costants.DIV_1_0;
        }
        if (ammonizione != 0 && voto != 0) {
            voto -= Costants.DIV_0_5;
        }
        if (espulso != 0) {
            if (ammonizione != 0) {
                voto += Costants.DIV_0_5; // compensate the previous yellow
            }
            voto -= Costants.DIV_1_0;
        }
        if (rf != 0) {
            voto -= rf * Costants.DIV_3_0;
        }
        if (rp != 0) {
            voto += rp * Costants.DIV_3_0;
        }
        if (aut != 0) {
            voto -= aut * Costants.DIV_2_0;
        }
        if (assist != 0) {
            voto += assist * Costants.DIV_1_0;
        }

        // clean sheet bonus for goalkeepers
        if ("P".equals(idRuolo)
                && goalSubito == 0
                && espulso == 0
                && voto != 0
                && g != 0 && cs != 0 && ts != 0) {
            voto += Costants.DIVISORE_100;
        }

        log.info("giocatore={},bRoundVoto={}, voto={}", pagelle.getFcGiocatore().getCognGiocatore(),bRoundVoto, voto);

        return bRoundVoto ? arrotonda(voto) : voto;
    }

    private static int nullSafeInt(Double value) {
        return value != null ? value.intValue() : 0;
    }

    public static int arrotonda(int input) {
        BigDecimal bdInput = BigDecimal.valueOf(input);
        BigDecimal bd10 = BigDecimal.valueOf(Costants.DIVISORE_10);
        BigDecimal divided = bdInput.divide(bd10);
        BigDecimal rounded = roundBigDecimal(divided);
        return rounded.multiply(bd10).intValue();
    }

    public static BigDecimal roundBigDecimal(final BigDecimal input) {
        int precision = input.toBigInteger().toString().length();
        return input.round(new MathContext(precision, RoundingMode.HALF_UP));
    }

    // -------------------------------------------------------------------------
    // Next date / countdown helpers
    // -------------------------------------------------------------------------

    public static Map<String, Object> getNextDate(FcGiornataInfo giornataInfo) {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentDate = now;

        if (giornataInfo != null && giornataInfo.getDataGiornata() != null) {
            LocalDateTime dataGiornata = giornataInfo.getDataGiornata();
            LocalDateTime dataAnticipo = resolveDataAnticipo(giornataInfo, now);
            LocalDateTime dataPosticipo = giornataInfo.getDataPosticipo();

            currentDate = dataGiornata;

            if (dataAnticipo != null) {
                currentDate = dataAnticipo;
                if (now.isAfter(dataAnticipo) && now.getDayOfWeek() == dataGiornata.getDayOfWeek()) {
                    currentDate = dataGiornata;
                }
            }

            if (dataPosticipo != null) {
                if (now.getDayOfWeek() == dataPosticipo.getDayOfWeek()) {
                    currentDate = dataGiornata;
                }
            } else if (dataAnticipo != null) {
                currentDate = dataAnticipo;
            }
        }

        map.put("1", currentDate);
        map.put("2", currentDate.format(DEFAULT_DTF));
        return map;
    }

    private static @Nullable LocalDateTime resolveDataAnticipo(FcGiornataInfo giornataInfo, LocalDateTime now) {
        LocalDateTime anticipo1 = giornataInfo.getDataAnticipo1();
        LocalDateTime anticipo2 = giornataInfo.getDataAnticipo2();

        if (anticipo1 != null && anticipo2 != null) {
            if (now.isBefore(anticipo1)) {
                return anticipo1;
            }
            if (now.isAfter(anticipo1) && now.getDayOfWeek() == anticipo2.getDayOfWeek()) {
                return anticipo2;
            }
            return anticipo1;
        }
        if (anticipo1 == null && anticipo2 != null) {
            return anticipo2;
        }
        return anticipo1;
    }

    public static long getMillisDiff(String nextDate, String fusoOrario) throws Exception {
        DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        fmt.setLenient(false);

        Date now = Calendar.getInstance().getTime();
        Date target = fmt.parse(nextDate);

        long millisDiff = target.getTime() - now.getTime();

        // logging for diagnostics
        long seconds = (millisDiff / 1000) % 60;
        long minutes = (millisDiff / 60_000) % 60;
        long hours = (millisDiff / 3_600_000) % 24;
        long days = millisDiff / 86_400_000;
        log.info("{} days, {} hours, {} minutes, {} seconds", days, hours, minutes, seconds);

        long timezoneOffsetMs = Long.parseLong(fusoOrario) * 3_600_000L;
        millisDiff -= timezoneOffsetMs;

        return Math.max(millisDiff, 0L);
    }

    // -------------------------------------------------------------------------
    // Vaadin Image / StreamResource helpers
    // -------------------------------------------------------------------------

    public static Image getImage(String nomeImg, InputStream inputStream) {
        StreamResource resource = new StreamResource(nomeImg, () -> inputStream);
        return new Image(resource, "");
        
//		return new Image(DownloadHandler.fromInputStream(event -> {
        
//		try {
//			String fileName = nomeImg;
//			String contentType = "image/png";
//			long contentLength = -1; // or -1 if unknown
//
//			return new DownloadResponse(inputStream,fileName,contentType,contentLength);
//		} catch (Exception e) {
//			return DownloadResponse.error(500);
//		}
//	}),"");
        
    }

    public static Image buildImage(String nomeImg, Resource r) {
        StreamResource resource = new StreamResource(nomeImg, () -> {
            try {
                return r.getInputStream();
            } catch (IOException e) {
                log.error("buildImage failed: {}", e.getMessage());
                return null;
            }
        });
        return new Image(resource, "");
        
//		return new Image(DownloadHandler.forClassResource(r.getClass(), "/images/"+nomeImg),nomeImg);
		
//		try {
//			return new Image(DownloadHandler.forFile(r.getFile()),"");
//		} catch (IOException e) {
//		}
//		return null;
        
    }

//    public static StreamResource getStreamResource(String fileName, Connection conn,
//                                                   Map<String, Object> hm, InputStream inputStream) {
//        return new StreamResource(fileName, () -> {
//            try {
//                return JasperReportUtils.runReportToPdf(inputStream, hm, conn);
//            } catch (Exception e) {
//                log.error("getStreamResource (JDBC) failed: {}", e.getMessage());
//                return null;
//            }
//        });
//    }

//    public static StreamResource getStreamResource(String fileName, List<FormazioneJasper> coll,
//                                                   Map<String, Object> hm, InputStream inputStream) {
//        return new StreamResource(fileName, () -> {
//            try {
//                return JasperReportUtils.runReportToPdf(inputStream, hm, coll);
//            } catch (Exception e) {
//                log.error("getStreamResource (collection) failed: {}", e.getMessage());
//                return null;
//            }
//        });
//    }

//    public static StreamResource getStreamResource(String inputStreamName, byte[] bytes) {
//        return new StreamResource(inputStreamName, () -> new ByteArrayInputStream(bytes));
//    }

    // -------------------------------------------------------------------------
    // Player info
    // -------------------------------------------------------------------------

    public static String getInfoPlayer(FcGiocatore bean) {
        if (bean == null) {
            return "";
        }

        StringBuilder info = new StringBuilder();
        info.append(bean.getCognGiocatore()).append('\n');
        info.append("Squadra: ").append(bean.getFcSquadra().getNomeSquadra()).append('\n');
        info.append("Giocate: ").append(bean.getFcStatistiche().getGiocate()).append('\n');

        if (bean.getFcStatistiche() != null && bean.getFcStatistiche().getMediaVoto() != 0) {
            NumberFormat formatter = new DecimalFormat("#0.00");
            String mv = formatter.format(bean.getFcStatistiche().getMediaVoto() / (double) Costants.DIVISORE_100);
            String fv = formatter.format(bean.getFcStatistiche().getFantaMedia() / (double) Costants.DIVISORE_100);

            info.append("MV: ").append(mv).append('\n');
            info.append("FV: ").append(fv).append('\n');
            info.append("Goal: ").append(bean.getFcStatistiche().getGoalFatto()).append('\n');
            info.append("Assist: ").append(bean.getFcStatistiche().getAssist()).append('\n');
            info.append("Ammonizione: ").append(bean.getFcStatistiche().getAmmonizione()).append('\n');
            info.append("Espulsione: ").append(bean.getFcStatistiche().getEspulsione()).append('\n');

            if (Costants.P.equalsIgnoreCase(bean.getFcRuolo().getIdRuolo())) {
                info.append("Goal Subito: ").append(bean.getFcStatistiche().getGoalSubito()).append('\n');
            }
        }

        info.append("Probabile: ")
            .append(StringUtils.isNotEmpty(bean.getNomeGiocatore()) ? bean.getNomeGiocatore() : "N.D.")
            .append('\n');

        return info.toString();
    }
}