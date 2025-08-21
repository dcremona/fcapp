/*
 * @(#)SivaToolkit.java	2.2 - 21/12/1999
 */

package common.util;

import java.awt.Frame;
import java.util.Calendar;

import javax.swing.JOptionPane;

public class MyToolkit {

    public final static String ENTER_STRING = "<BR>";

    public static int[] getFieldMatching(Buffer buffer, String[][] valori) {
        int iNumRec = buffer.getRecordCount();
        int iTotRecord = 0;

        int iTotValori = valori[0].length;

        boolean bEqual, bFieldEqual;
        int nMatchingRecord[] = new int[iNumRec];

        buffer.moveFirst();

        String sFieldNotEqual[] = new String[iTotValori];
        for (int i = 0; i < iTotValori; i++) {
            sFieldNotEqual[i] = "";
        }

        for (int r = 1; r <= iNumRec; r++) {
            bEqual = true;

            try {
                for (int c = 0; c < iTotValori; c++) {
                    bFieldEqual = ((valori[1][c].trim().equals(""))
                            || ((buffer.getField(Integer.parseInt(valori[0][c])).indexOf(valori[1][c]) == 0) ? true
                                    : false));
                    bEqual = bEqual && bFieldEqual;

                    if (!bFieldEqual) {
                        sFieldNotEqual[c] = "KO";
                    }
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(new Frame(),
                        "SivaToolkit, metodo getFieldMatching: " + e + "\nValore campo array non numerico.");
                // ErrorManager.showErrorMessage("SivaToolkit, metodo
                // getFieldMatching: "+e+"\nValore campo array non numerico.");
                return null;
            }

            if (bEqual) {
                nMatchingRecord[iTotRecord] = r;
                iTotRecord++;
            }

            buffer.moveNext();
        }

        if (iTotRecord == 0) {
            for (int i = 0; i < iTotValori; i++) {
                if (sFieldNotEqual[i].equals("KO")) {
                    valori[0][i] = "KO";
                }
            }

            return null;
        } else {
            int ret[] = new int[iTotRecord];
            System.arraycopy(nMatchingRecord, 0, ret, 0, iTotRecord);
            return ret;
        }
    }

    public static int checkFieldMatching(String sMatchField, int iCol, Buffer rstDati) {
        int iMax = rstDati.getRecordCount();
        // int iMatch = 0;
        // int iRecMatch = 0;

        if (rstDati.findFirst(sMatchField, iCol, false) != -1) {
            return 0;
        }

        rstDati.moveFirst();

        for (int idx = 1; idx <= iMax; idx++) {
            if (rstDati.getField(iCol).indexOf(sMatchField) == 0) {
                return 1;
            }

            rstDati.moveNext();
        }

        return -1;
    }

    public static int freeCode(Buffer buffer, int iCol) {
        if (buffer.getRecordCount() == 0) {
            return 1;
        }

        if (!buffer.getFieldType(iCol).equals("NUMERIC")) {
            JOptionPane.showMessageDialog(new Frame(),
                    "SivaToolkit, metodo freeCode: Campo " + iCol + " non numerico.");
            return -1;
        }

        // Creo un clone del buffer originale
        Buffer clone = buffer.getClone();

        // Ordino il clone
        try {
            clone.sort(iCol);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new Frame(), "SivaToolkit, metodo freeCode: " + e);
            // ErrorManager.showErrorMessage("SivaToolkit, metodo freeCode: " +
            // e);
            return -1;
        }

        // Elimino eventuali duplicati dal clone
        clone = deleteDuplicate(clone, iCol);

        int iRec = clone.getRecordCount();

        clone.moveFirst();

        int iDiff;

        if (clone.getFieldByInt(iCol) == 0) {
            clone.moveNext();
            iDiff = 0;
        } else {
            iDiff = 1;
        }

        for (int i = 1; i <= iRec; i++) {
            if (clone.getFieldByInt(iCol) != i) {
                return i;
            }
            clone.moveNext();
        }

        return (iRec + iDiff);
    }

    public static String replaceEComm(String t) {
        int start, end;
        String sApp = new String();
        String sub = new String();
        String CurChar = new String();

        end = t.length();

        for (int i = 0; i < end; i++) {
            CurChar = t.substring(i, i + 1);

            if (CurChar.equals("&")) {
                start = -1;

                if (i + 1 != end) {
                    start = t.indexOf("=", i + 1);
                }

                if (start != -1) {
                    sub = t.substring(i + 1, start);
                    if (sub.indexOf("&") != -1) {
                        sApp += "%26";
                    } else {
                        sApp += CurChar;
                    }
                } else {
                    if (t.substring(i, i + 1).equals("&")) {
                        sApp += "%26";
                    } else {
                        sApp += CurChar;
                    }
                }
            } else {
                sApp += CurChar;
            }
        }

        return sApp;
    }

    public static String[] createTriggerListArray(Buffer risQuery, int col1, int lcol1, int col2, int lcol2, int spaces,
            int align1, int align2) {
        int nRec = risQuery.getRecordCount();
        String aStrApp[] = new String[nRec];
        String asResultSet[][] = risQuery.getRecordSet();
        String SPACE = new String();

        // spazi aggiuntivi
        for (int i = 0; i < spaces; i++) {
            SPACE += " ";
        }

        col1--;
        col2--;

        // se il secondo campo va allineato a sx ottimizzo e non chiamo
        // FormattaTesto..
        if (align2 != 1) {
            for (int c = 0; c < nRec; c++) {
                aStrApp[c] = formatText(asResultSet[c][col1], lcol1, " ", align1).concat(SPACE)
                        .concat(formatText(asResultSet[c][col2], lcol2, " ", align2));
            }
        } else {
            for (int c = 0; c < nRec; c++) {
                aStrApp[c] = formatText(asResultSet[c][col1], lcol1, " ", align1).concat(SPACE)
                        .concat(asResultSet[c][col2]);
            }
        }

        return aStrApp;
    }

    // Il Metodo restituisce la data, nel formato gg/mm/aaaa,
    // ottenuta sommando o sottraendo tanti giorni,mesi ed anni
    // quanti sono quelli passati.
    public static String getDateFrom(String date, int day, int month, int year) {
        if (date.trim().equals("")) {
            return date;
        }

        if (date.trim().length() != 10) {
            JOptionPane.showMessageDialog(new Frame(),
                    "SivaToolkit getDateFrom:\n Errore lunghezza data (" + date + ")");
            return date;
        }

        if (date.charAt(2) != '/' || date.charAt(5) != '/') {
            JOptionPane.showMessageDialog(new Frame(), "SivaToolkit.getDateFrom \n Errore formato data (" + date + ")");
            return date;
        }
        int m = 0, d = 0, y = 0;
        Calendar calend = Calendar.getInstance();

        try {
            m = Integer.parseInt(date.substring(3, 5)) - 1;
            d = Integer.parseInt(date.substring(0, 2));
            y = Integer.parseInt(date.substring(6, 10));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(new Frame(),
                    "SivaToolkit.getDateFrom \n Errore valori non numerici(" + date + ")");
            // ErrorManager.showErrorMessage("SivaToolkit.getDateFrom \n Errore
            // valori non numerici("+date+")");
            return date;
        }

        if (!isValidDate(d, m + 1, y)) {
            JOptionPane.showMessageDialog(new Frame(),
                    "SivaToolkit.getDateFrom \n Errore data non corretta(" + date + ")");
            return date;
        }
        calend.set(y, m, d);

        calend.add(Calendar.YEAR, year);
        calend.add(Calendar.MONTH, month);
        calend.add(Calendar.DATE, day);

        // Compone la stringa da ritornare in modo che sia formata sicuramente
        // del
        // seguente formato: gg/mm/aaaa. (X Es. 01/01/1999)
        date = "";
        d = calend.get(Calendar.DATE);
        if (d < 10) {
            date += "0" + d + "/";
        } else {
            date += d + "/";
        }

        m = (calend.get(Calendar.MONTH)) + 1;
        if (m < 10) {
            date += "0" + m + "/";
        } else {
            date += m + "/";
        }

        date += calend.get(Calendar.YEAR);

        return date; // risultato dato elaborato
    }

    // Metodo utilizzato per ordinare un array di stringhe
    public static void sortS(String a[]) throws Exception {
        int L, R;
        String x = new String();

        L = (a.length) / 2;
        R = a.length - 1;

        while (L > 0) {
            siftS(--L, R, a);
        }

        while (R > 0) {
            x = a[0];
            a[0] = a[R];
            a[R] = x;
            siftS(L, --R, a);
        }
    }

    // Metodo utilizzato per ordinare un array di interi
    public static void sortN(int a[]) throws Exception {
        int L, R, x;
        L = ((a.length) / 2) + 1;
        R = a.length - 1;

        while (L > 0) {
            sift(--L, R, a);
        }

        while (R > 0) {
            x = a[0];
            a[0] = a[R];
            a[R] = x;
            sift(L, --R, a);
        }
    }

    // Metodo utilizzato per ordinare un array di campi del buffer
    // considerando solo il campo passato
    public static void sortBuffer(String a[], Object ValoreBuffer[]) throws Exception {
        int L, R;
        Object x;

        L = ((a.length) / 2) + 1;
        R = a.length - 1;

        while (L > 0) {
            siftBuffer(--L, R, a, ValoreBuffer);
        }

        while (R > 0) {
            x = a[0];
            a[0] = a[R];
            a[R] = (String) x;
            x = ValoreBuffer[0];
            ValoreBuffer[0] = ValoreBuffer[R];
            ValoreBuffer[R] = x;
            siftBuffer(L, --R, a, ValoreBuffer);
        }

    }

    private static void siftBuffer(int L, int R, String a[], Object rstDati[]) throws Exception {
        int i, j;
        String x;
        Object x2;

        i = L;
        j = 2 * L;
        x = a[L];
        x2 = rstDati[L];

        if ((j < R) && (a[j].compareTo(a[j + 1]) < 0)) {
            j++;
        }

        while ((j <= R) && (x.compareTo(a[j]) < 0)) {
            a[i] = a[j];
            rstDati[i] = rstDati[j];
            i = j;
            j *= 2;
            if ((j < R) && (a[j].compareTo(a[j + 1]) < 0)) {
                j++;
            }
        }

        a[i] = x;
        rstDati[i] = x2;

    }

    public static void sortBufferN(long a[], Object ValoreBuffer[]) throws Exception {
        int L, R;
        Object x2;
        long x;

        L = ((a.length) / 2) + 1;
        R = a.length - 1;

        while (L > 0) {
            siftBufferN(--L, R, a, ValoreBuffer);
        }

        while (R > 0) {
            x = a[0];
            a[0] = a[R];
            a[R] = x;
            x2 = ValoreBuffer[0];
            ValoreBuffer[0] = ValoreBuffer[R];
            ValoreBuffer[R] = x2;
            siftBufferN(L, --R, a, ValoreBuffer);
        }

    }

    private static void siftBufferN(int L, int R, long a[], Object rstDati[]) throws Exception {
        int i, j;
        long x;
        Object x2;
        i = L;
        j = 2 * L;
        x = a[L];
        x2 = rstDati[L];

        if ((j < R) && (a[j] < a[j + 1])) {
            j++;
        }

        while ((j <= R) && (x < a[j])) {
            a[i] = a[j];
            rstDati[i] = rstDati[j];
            i = j;
            j *= 2;
            if ((j < R) && (a[j] < a[j + 1])) {
                j++;
            }
        }

        a[i] = x;
        rstDati[i] = x2;

    }

    ////////////////////////////////////////////////////////////////////

    private static void siftS(int L, int R, String a[]) throws Exception {
        int i, j;
        String x = new String();

        i = L;
        j = 2 * L;
        x = a[L];

        if ((j < R) && (a[j].compareTo(a[j + 1]) < 0)) {
            j++;
        }

        while ((j <= R) && (x.compareTo(a[j]) < 0)) {
            a[i] = a[j];
            i = j;
            j *= 2;
            if ((j < R) && (a[j].compareTo(a[j + 1]) < 0)) {
                j++;
            }
        }

        a[i] = x;
    }

    private static void sift(int L, int R, int a[]) throws Exception {
        int i, j, x;
        i = L;
        j = 2 * L;
        x = a[L];

        if ((j < R) && (a[j] < a[j + 1])) {
            j++;
        }

        while ((j <= R) && (x < a[j])) {
            a[i] = a[j];
            i = j;
            j *= 2;
            if ((j < R) && (a[j] < a[j + 1])) {
                j++;
            }
        }

        a[i] = x;
    }

    // metodo utilizzato per formattare una stringa con tanti caratteri
    // uguali a quello passato, quanti sono quelli richiesti..
    public static String formatText(String s, int lng, String f, int pos) {
        int iLen = s.length();

        if (iLen > lng) {
            return s.substring(0, lng);
        }

        lng -= iLen;

        StringBuffer sRiempimento = new StringBuffer(lng);

        for (int i = 0; i < lng; i++) {
            sRiempimento.append(f);
        }

        if (pos == 0) {
            return sRiempimento + s;
        } else {
            return s + sRiempimento;
        }
    }

    /*
     * public static String addDecimalPoint(String sText) { int iLen =
     * sText.length(); String sApp = new String(); String sApp2 = new String();
     * String sDec = ""; int iNdx = 0; if ((iNdx = sText.indexOf(',')) != -1) { sDec
     * = sText.substring(iNdx); sText = sText.substring(0, iNdx); iLen =
     * sText.length(); } int iCont = 0;
     *
     * for (int i = iLen; i > 0; i--) { char k = sText.charAt(i - 1);
     *
     * if (k != ',') { iCont++;
     *
     * if (iCont > 3) { sApp += "." + sApp.valueOf(k); iCont = 1; } else sApp +=
     * sApp.valueOf(k); } }
     *
     * iLen = sApp.length();
     *
     * for (int y = iLen; y > 0; y--) sApp2 += sApp.charAt(y - 1);
     *
     * return sApp2 + sDec; }
     */

    // Check Partita IVA
    public static boolean checkIVA(String text) {
        String V = new String();
        int j, V2, CR;

        try {
            if (text.length() == 11) {
                CR = 0;
                j = 1;
                while (j < 10) {
                    V2 = 2 * Integer.parseInt(text.substring(j, j + 1));
                    if (V2 > 9) {
                        V2 -= 9;
                    }
                    CR = CR + V2 + Integer.parseInt(text.substring(j - 1, j));
                    j += 2;
                }
                V = ((100 - CR) + "").substring(((100 - CR) + "").length() - 1);

                if (!(V.equals((text.substring(text.length() - 1))))) {
                    return false; // Partita IVA errata !
                } else {
                    return true; // Partita IVA esatta !!
                }
            }
        } catch (NumberFormatException err) {
            return false;
        }

        return false;

    }

    // Check numero Carta di Credito
    public static boolean checkCreditCard(String CartCrd) {
        int iChecksum = 0;
        boolean bCifraPari = false;
        int iCifraCorr = 0;

        for (int i = CartCrd.length() - 1; i >= 0; i--) {
            try {
                iCifraCorr = Integer.parseInt(CartCrd.substring(i, i + 1));
            } catch (NumberFormatException e) {
                return false;
            }

            if (bCifraPari) {
                iCifraCorr *= 2;
                if (iCifraCorr >= 10) {
                    iCifraCorr -= 9;
                }
            }

            iChecksum += iCifraCorr;
            bCifraPari = !bCifraPari;
        }

        if (iChecksum % 10 != 0) {
            return false;
        } else {
            return true;
        }
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

    public static Buffer deleteDuplicate(Buffer rstDati, int iCol) {
        String sAppCampoAttBuffer = new String();
        String sAppCampoSucBuffer = new String();

        int iRec = rstDati.getRecordCount();

        if (iRec == 1) {
            return rstDati.getClone();
        }

        int aiRecordValidi[] = new int[iRec];
        int cont = 0, i = 1;

        while (i <= iRec) {
            rstDati.setCurrentIndex(i);
            aiRecordValidi[cont++] = i;

            if (i >= iRec) {
                break;
            }

            sAppCampoAttBuffer = rstDati.getField(iCol);

            rstDati.setCurrentIndex(++i);
            sAppCampoSucBuffer = rstDati.getField(iCol);

            while (sAppCampoAttBuffer.equals(sAppCampoSucBuffer)) {
                rstDati.setCurrentIndex(++i);
                if (i <= iRec) {
                    sAppCampoSucBuffer = rstDati.getField(iCol);
                } else {
                    break;
                }
            }
        }

        int aiRecordValidiFinale[] = new int[cont];
        System.arraycopy(aiRecordValidi, 0, aiRecordValidiFinale, 0, cont);
        return rstDati.getClone(aiRecordValidiFinale);
    }

    public static String rTrim(String s) {
        int iLen = s.length() - 1, i;

        for (i = iLen; i > 0; i--) {
            if (s.charAt(i) != ' ') {
                break;
            }
        }

        return s.substring(0, i + 1);
    }

    public static String lTrim(String s) {
        int iLen = s.length(), i;

        for (i = 0; i < iLen; i++) {
            if (s.charAt(i) != ' ') {
                break;
            }
        }

        return s.substring(i);
    }

    // public static Container searchLastParent(Component component) {
    // Component oldComp = component;
    //
    // while (true) {
    // oldComp = oldComp.getParent();
    // if (oldComp == null)
    // break;
    // else if (oldComp instanceof Frame || oldComp instanceof java.applet.Applet ||
    // oldComp instanceof java.awt.Dialog)
    // break;
    // }
    //
    // return (oldComp == null ? null : (Container) oldComp);
    // }
}
