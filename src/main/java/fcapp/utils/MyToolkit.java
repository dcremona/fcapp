/*
 * @(#)SivaToolkit.java	2.2 - 21/12/1999
 */

package fcapp.utils;

public class MyToolkit {
	private MyToolkit() {
		/* This utility class should not be instantiated */
	}

	// Metodo utilizzato per ordinare un array di campi del buffer
	// considerando solo il campo passato
	public static void sortBuffer(String[] a, Object[] valoreBuffer) {
		int l;
		int r;
		Object x;

		l = ((a.length) / 2) + 1;
		r = a.length - 1;

		while (l > 0) {
			siftBuffer(--l, r, a, valoreBuffer);
		}

		while (r > 0) {
			x = a[0];
			a[0] = a[r];
			a[r] = (String) x;
			x = valoreBuffer[0];
			valoreBuffer[0] = valoreBuffer[r];
			valoreBuffer[r] = x;
			siftBuffer(l, --r, a, valoreBuffer);
		}

	}

	private static void siftBuffer(int l, int r, String[] a, Object[] rstDati) {
		int i;
		int j;
		String x;
		Object x2;

		i = l;
		j = 2 * l;
		x = a[l];
		x2 = rstDati[l];

		if ((j < r) && (a[j].compareTo(a[j + 1]) < 0)) {
			j++;
		}

		while ((j <= r) && (x.compareTo(a[j]) < 0)) {
			a[i] = a[j];
			rstDati[i] = rstDati[j];
			i = j;
			j *= 2;
			if ((j < r) && (a[j].compareTo(a[j + 1]) < 0)) {
				j++;
			}
		}

		a[i] = x;
		rstDati[i] = x2;

	}

	public static void sortBufferN(long[] a, Object[] valoreBuffer) {
		int l;
		int r;
		Object x2;
		long x;

		l = ((a.length) / 2) + 1;
		r = a.length - 1;

		while (l > 0) {
			siftBufferN(--l, r, a, valoreBuffer);
		}

		while (r > 0) {
			x = a[0];
			a[0] = a[r];
			a[r] = x;
			x2 = valoreBuffer[0];
			valoreBuffer[0] = valoreBuffer[r];
			valoreBuffer[r] = x2;
			siftBufferN(l, --r, a, valoreBuffer);
		}

	}

	private static void siftBufferN(int l, int r, long[] a, Object[] rstDati) {
		int i;
		int j;
		long x;
		Object x2;
		i = l;
		j = 2 * l;
		x = a[l];
		x2 = rstDati[l];

		if ((j < r) && (a[j] < a[j + 1])) {
			j++;
		}

		while ((j <= r) && (x < a[j])) {
			a[i] = a[j];
			rstDati[i] = rstDati[j];
			i = j;
			j *= 2;
			if ((j < r) && (a[j] < a[j + 1])) {
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
