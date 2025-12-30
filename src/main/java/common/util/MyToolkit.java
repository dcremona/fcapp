/*
 * @(#)SivaToolkit.java	2.2 - 21/12/1999
 */

package common.util;

public class MyToolkit{

	// Metodo utilizzato per ordinare un array di campi del buffer
	// considerando solo il campo passato
	public static void sortBuffer(String[] a, Object[] ValoreBuffer) {
		int L,R;
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

	private static void siftBuffer(int L, int R, String[] a, Object[] rstDati) {
		int i,j;
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

	public static void sortBufferN(long[] a, Object[] ValoreBuffer) {
		int L,R;
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

	private static void siftBufferN(int L, int R, long[] a, Object[] rstDati) {
		int i,j;
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

	// metodo utilizzato per formattare una stringa con tanti caratteri
	// uguali a quello passato, quanti sono quelli richiesti..
	public static String formatText(String s, int lng, String f, int pos) {
		int iLen = s.length();

		if (iLen > lng) {
			return s.substring(0, lng);
		}

		lng -= iLen;

		StringBuilder sRiempimento = new StringBuilder(lng);

        sRiempimento.append(String.valueOf(f).repeat(lng));

		if (pos == 0) {
			return sRiempimento + s;
		} else {
			return s + sRiempimento;
		}
	}

	/*
	 * public static String addDecimalPoint(String sText) { int iLen =
	 * sText.length(); String sApp = new String(); String sApp2 = new String();
	 * String sDec = ""; int iNdx = 0; if ((iNdx = sText.indexOf(',')) != -1) {
	 * sDec = sText.substring(iNdx); sText = sText.substring(0, iNdx); iLen =
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

	public static boolean isValidDate(int giorno, int mese, int anno) {
		boolean dataValida = false;

		if (anno > 1900) {
			int[] nGiorniMese = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
			boolean annoBisestile;

			annoBisestile = (anno % 4 == 0 && anno % 100 != 0) || anno % 400 == 0;
			if (mese < 13 && mese > 0) {
				if (giorno <= nGiorniMese[mese - 1] && giorno > 0) {
					dataValida = true;
				} else {
                    dataValida = mese == 2 && annoBisestile && giorno == 29;
				}
			}
		}

		return dataValida;
	}

}
