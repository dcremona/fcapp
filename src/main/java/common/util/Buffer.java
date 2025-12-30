package common.util;

import java.io.Serial;
import java.util.StringTokenizer;

import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author dcremona
 * <p>
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class Buffer extends javax.swing.table.AbstractTableModel
		implements ComboBoxModel<Object>{
	/**
	 *
	 */
	@Serial
    private static final long serialVersionUID = 1L;

	private String[] asColumnNames;
	private String[][] theRecordSet;
	private int[][] aiFieldType;
	private int iRecordCount = 0;
	private int iRowIndex = 0;
	private String sFindText;
	private int iFindCol;
	private boolean bFindCaseSensitive;
	private boolean bDataTypeKnow = false;
	private int iFieldCount = -1;
	private int iDefaultAdd = 200;
	private int iSortedField = 0;
	private boolean bSortAlways = false;

	// Variabili per il controllo del livello di errore avvenuto
	private boolean ERROR = false;
	private int ERROR_CODE = 0;

	private String TAG = null,DB_TIME = null,QUERY_NAME = null,EXIT = null;

    public boolean EOF = true;

	public Buffer(String[] asColumnNames) {
        this.asColumnNames = asColumnNames;
        // System.out.print("\nNEW"+"_"+this);

		resetVar();
	}

	public Buffer() {

	}

	/**
	 * Metodo utilizzato per rimuovere tutti i dati contenuti nel Buffer.
	 */
	public void clear() {
		// Generate notification
		fireIntervalRemoved(0, iRecordCount - 1);
		fireTableRowsDeleted(0, iRecordCount - 1);

		theRecordSet = null;
		aiFieldType = null;
		iRecordCount = 0;
		iRowIndex = 0;
		sFindText = "";
		iFindCol = 0;
		bFindCaseSensitive = false;
		iSortedField = 0;
		bSortAlways = false;
		EOF = true;
		TAG = null;
		DB_TIME = null;
		ERROR = false;
		ERROR_CODE = 0;
		setDataTypeKnow(false);

	}

	/**
	 * Imposta il numero della colonna per la quale risulta ordinato il Buffer.
	 *
	 * @param iField
	 *            Indica il numero della colonna secondo la quale @ ordinato il
	 *            Buffer.
	 * @see #getSortedField()
	 */
	public void setSortedField(int iField) {
		iSortedField = iField;
	}

	/**
	 * Ritorna il numero della colonna per la quale @ attualmente ordinato il
	 * Buffer.
	 *
	 * @return Il numero della colonna per cui e' attualmente ordinato il
	 *         Buffer.
	 * @see #setSortedField(int)
	 */
	public int getSortedField() {
		return iSortedField;
	}

	public void setDataTypeKnow(boolean bDataTypeKnow) {
		this.bDataTypeKnow = bDataTypeKnow;
	}

	/**
	 * Metodo utilizzato per settare il numero di elementi da aggiungere a
	 * RecordSet, ad ogni chiamata ad AddItem. La velocit@ di aggiunta @
	 * proporzionale alla sua grandezza.
	 *
     */
	public void setDefaultAdd(int add) {
		iDefaultAdd = add;
	}

    /**
	 * Metodo utilizzato per creare un'altra istanza, ma distinta, del Buffer
	 * attuale.
	 *
	 * @return Un'istanza distinta del Buffer attuale.
	 * @see #getClone(int[])
	 */
	public Buffer getClone() {
		Buffer clone = new Buffer();

		if (iRecordCount == 0) {
			return clone;
		}

		clone.setDefaultAdd(iDefaultAdd);

		String[] asFieldType = new String[iFieldCount];
		for (int i = 1; i <= iFieldCount; i++) {
			asFieldType[i - 1] = getFieldType(i);
		}

		String[][] RecordSetClone = new String[iRecordCount][iFieldCount];
		for (int i = 0; i < iRecordCount; i++) {
            System.arraycopy(theRecordSet[i], 0, RecordSetClone[i], 0, iFieldCount);
		}
		clone.setRecordSet(RecordSetClone, asFieldType, iFieldCount, iRecordCount);

		clone.setSortedField(iSortedField);

		clone.TAG = TAG;
		clone.DB_TIME = DB_TIME;
		clone.QUERY_NAME = QUERY_NAME;
		clone.EXIT = EXIT;

		return clone;
	}

	/**
	 * Metodo utilizzato per creare un' istanza parziale e distinta, del Buffer
	 * attuale composta per@ dai soli record indicati nell'array passato.
	 * Comunemente usato col metodo getFieldMatching(Buffer ,String[][]) di
	 * SivaToolkit.
	 *
	 * @param aiRecord
	 *            Array di interi indicante quali record che si vogliono
	 *            'clonare' dal Buffer attuale.
	 * @return Un'istanza distinta del Buffer attuale.
	 * @see #getClone()
     */
	public Buffer getClone(int[] aiRecord) {
		if (iRecordCount == 0) {
			return null;
		}

		Buffer clone = new Buffer();
		int iCloneLen = aiRecord.length;

		clone.setDefaultAdd(aiRecord.length);

		String[][] asRecordSetClone = new String[iCloneLen][iFieldCount];

		for (int i = 0; i < iCloneLen; i++) {
			System.arraycopy(theRecordSet, aiRecord[i] - 1, asRecordSetClone, i, 1);
		}

		String[] asFieldType = new String[iFieldCount];
		for (int i = 1; i <= iFieldCount; i++) {
			asFieldType[i - 1] = getFieldType(i);
		}

		clone.setRecordSet(asRecordSetClone, asFieldType, iFieldCount, iCloneLen);

        clone.TAG = TAG;
		clone.DB_TIME = DB_TIME;
		clone.QUERY_NAME = QUERY_NAME;
		clone.EXIT = EXIT;

		return clone;
	}

	/**
	 * Metodo utilizzato per ordinare il contenuto del Buffer in base al numero
	 * di campo passato.
	 *
	 * @param col
	 *            Intero indicante il numero del campo per il qualesi vuole
	 *            ordinare il Buffer.
	 * @see #sort(int[])
	 */
	public void sort(int col) {
		// se viene chiamato il Sort su la stessa colonna secondo la quale
		// attualmente ordinato il Buffer, non lo eseguo a meno che non sia
		// impostato SortAlwais a true.
		if (iRecordCount < 2 || col < 1 || col > iFieldCount || (col == getSortedField() && !bSortAlways)) {
			return;
		}

        int max = iRecordCount;
		String[] sVal = new String[max];
		for (int i = 0; i < max; i++) {
			sVal[i] = theRecordSet[i][col - 1];
		}

		if (getFieldType(col).equals("STRING")) {
			try {
				MyToolkit.sortBuffer(sVal, theRecordSet);
			} catch (Exception ignored) {
			}
		} else if (getFieldType(col).equals("NUMERIC")) {
			try {
				long[] lVal = new long[max];
				for (int i = 0; i < max; i++) {
					lVal[i] = Long.parseLong(sVal[i]);
				}
				MyToolkit.sortBufferN(lVal, theRecordSet);
			} catch (Exception ignored) {
			}
		} else if (getFieldType(col).equals("DATE")) {
			// Se si tratta di una data la formatto in aaaa/mm/gg
			// per trattarla come una qualsiasi stringa
			try {
				String temp;
				for (int i = 0; i < max; i++) {
					if (!sVal[i].isEmpty()) {
                        temp = sVal[i].substring(6, 10) + "/";
						temp += sVal[i].substring(3, 5) + "/";
						temp += sVal[i].substring(0, 2) + sVal[i].substring(11);
						sVal[i] = temp;
					}
				}

				MyToolkit.sortBuffer(sVal, theRecordSet);
			} catch (Exception ignored) {
			}
		}

		setSortedField(col);

		// generate notification
		fireTableRowsUpdated(0, iRecordCount - 1);
	}

	/**
	 * Metodo utilizzato per ordinare il contenuto del Buffer in base ai campi
	 * ricevuti in un array i quali sono in ordine crescente di importanza.
	 *
	 * @param colonne
	 *            Array di interi indicante l'ordine crescente delle colonne per
	 *            le quali si vuol ordinare il Buffer.
	 * @see #sort(int)
	 */

	// Metodo utilizzato per ordinare il contenuto del buffer
	// in base ai campi ricevuti in un array i quali sono in ordine
	// crescente di importanza
	public void sort(int[] colonne) {
		if (iRecordCount < 2) {
			return;
		}

		for (int element : colonne) {
			if (element > iFieldCount) {
				return;
			}
		}

		String sType;
		int iLeadingSpace;

		String[] sSort = new String[iRecordCount];
		Integer[] objSortPosition = new Integer[iRecordCount];

		int[] app = new int[colonne.length];

		// per ogni colonna mi trovo la lunghezza massima
		// e vedo se sono tutti valori numerici oppure no
		int max;
		String sVal;

		for (int j = 0; j < colonne.length; j++) {
			max = 0;
			for (int k = 0; k < iRecordCount; k++) {
				sVal = theRecordSet[k][colonne[j] - 1];

				if (sVal.length() > max) {
					max = sVal.length();
				}
			}

			app[j] = max;
		}

		// Riempio l'array per l'ordinamento sSort
		for (int i = 0; i < iRecordCount; i++) {
			objSortPosition[i] = i;
			sSort[i] = "";

			for (int j = 0; j < colonne.length; j++) {
				sVal = theRecordSet[i][colonne[j] - 1];
				sType = getFieldType(j + 1);

				// 0=riempio con spazi a sinistra
				// 1=riempio con spazi a destra
				iLeadingSpace = sType.equals("NUMERIC") ? 0 : 1;

				if (!sType.equals("DATE")) {
					sSort[i] = MyToolkit.formatText(sVal, app[j], " ", iLeadingSpace);
				} else {
					sSort[i] = sVal.substring(6) + "/" + sVal.substring(3, 5) + "/" + sVal.substring(0, 2);
				}
			}
		}

		try {
			MyToolkit.sortBuffer(sSort, objSortPosition);
		} catch (Exception ignored) {
		}

		String[][] theRecordSetApp = new String[iRecordCount][iFieldCount];
		for (int i = 0; i < iRecordCount; i++) {
			System.arraycopy(theRecordSet, objSortPosition[i], theRecordSetApp, i, 1);
		}

        theRecordSet = new String[iRecordCount][iFieldCount];

		System.arraycopy(theRecordSetApp, 0, theRecordSet, 0, iRecordCount);

        // nel caso di Sort su pi@ colonne non tengo pi@ conto della colonna
		// su cui @ ordinato il Buffer.
		setSortedField(0);

		// generate notification
		fireTableRowsUpdated(0, iRecordCount - 1);
	}

	/**
	 * Metodo utilizzato per sapere il numero di linee (record) contenute nel
	 * buffer dopo l'esecuzione della query.
	 *
	 * @return Numero di record del Buffer.
	 */
	// metodo utilizzato per sapere il numero di linee (record) contenute
	// nel buffer dopo l'esecuzione della query
	public int getRecordCount() {
		return iRecordCount;
	}

	/**
	 * Metodo utilizzato per spostarsi alla prima linea del buffer.
	 *
	 * @see #moveNext()
	 */
	// metodo utilizzato per spostarsi alla prima linea del buffer
	public void moveFirst() {
		if (iRecordCount > 0) {
			iRowIndex = 0;
			EOF = false;
		}
	}

	/**
	 * Metodo utilizzato per spostarsi alla linea successiva del buffer.
	 *
	 * @see #moveFirst()
     */
	// metodo utilizzato per spostarsi alla linea successiva del buffer
	public void moveNext() {
		if (iRowIndex + 1 < iRecordCount) {
			iRowIndex++;
		} else {
			EOF = true;
		}
	}

	public String setFieldType(int field, String type) {
		// Stati possibili dell'array aiFieldType
		//
		// 0 = non ancora impostato
		// -1 = true
		// 1 = false

		// se il valore del campo @ vuoto, lascio il tipo attuale, se impostato,
		// quando
		// questo @ di tipo DATE, altrimenti metto per default NUMERIC
        switch (type) {
            case "PREC" -> {
                type = getFieldType(field);

                if (!type.equals("DATE")) {
                    if (aiFieldType[field - 1][1] == 0) {
                        aiFieldType[field - 1][1] = -1;
                    } else {
                        aiFieldType[field - 1][1] = aiFieldType[field - 1][1];
                    }

                    type = "NUMERIC";
                }
            }
            case "STRING" -> {
                if (aiFieldType[field - 1][0] == 0) {
                    aiFieldType[field - 1][0] = -1;
                } else {
                    aiFieldType[field - 1][0] = aiFieldType[field - 1][0];
                }
            }
            case "NUMERIC" -> {
                if (aiFieldType[field - 1][1] == 0) {
                    aiFieldType[field - 1][1] = -1;
                } else {
                    aiFieldType[field - 1][1] = aiFieldType[field - 1][1];
                }
            }
            case "DATE" -> {
                if (aiFieldType[field - 1][2] == 0) {
                    aiFieldType[field - 1][2] = -1;
                } else {
                    aiFieldType[field - 1][2] = aiFieldType[field - 1][2];
                }
            }
        }

		return type;
	}

	/**
	 * Metodo utilizzato per proseguire l'ultima ricerca effettuata con il
	 * metodo FindFirst,senza tornare all'inizio del buffer.
	 *
	 * @return Ritorna un intero indicante la posizione, all'interno del Buffer,
	 *         dello stesso argomento dell'ultima ricerca effettuata da
	 *         findFirst(java.lang.String, int, boolean)
	 * @see #findFirst(java.lang.String, int, boolean)
	 */
	public int findNext() {
		if (iRowIndex > iRecordCount - 2 || iRecordCount < 2) {
			return -1;
		}

		String sCampo;
		String sCampo2;

		if (bFindCaseSensitive) {
			sCampo2 = sFindText.trim();

			for (int i = iRowIndex + 1; i < iRecordCount; i++) {
				sCampo = theRecordSet[i][iFindCol - 1];

				if (sCampo.equals(sCampo2)) {
					iRowIndex = i;
					return iRowIndex + 1;
				}
			}
		} else {
			sCampo2 = sFindText.trim();

			for (int i = iRowIndex + 1; i < iRecordCount; i++) {
				sCampo = theRecordSet[i][iFindCol - 1];

				if (sCampo.equalsIgnoreCase(sCampo2)) {
					iRowIndex = i;
					return iRowIndex + 1;
				}

			}
		}

		// no match!
		return -1;

	}

	/**
	 * Metodo utilizzato per ricercare la prima riga contenente,nella colonna
	 * passata, la stringa cercata.
	 *
	 * @param sText
	 *            Stringa da ricercare.
	 * @param col
	 *            Colonna nella quale effettuare la ricerca.
	 * @param caseSensitive
	 *            Se True durante la ricerca si tiene conto anche dei caratteri
	 *            maiuscoli e minuscoli.
	 * @return L'indice del Buffer dove si trova la stringa cercata.
	 * @see #findNext()
	 */
	public int findFirst(String sText, int col, boolean caseSensitive) {
		String sCampo;
		String sCampo2;

		if (iRecordCount < 1) {
			return -1;
		}

		if (caseSensitive) {
			sCampo2 = sText.trim();

			for (int i = 0; i < iRecordCount; i++) {
				sCampo = theRecordSet[i][col - 1];

				if (sCampo.equals(sCampo2)) {
					sFindText = sText;
					iFindCol = col;
					bFindCaseSensitive = true;

					iRowIndex = i;
					EOF = false;
					return iRowIndex + 1;
				}
			}
		} else {
			sCampo2 = sText.trim();

			for (int i = 0; i < iRecordCount; i++) {
				sCampo = theRecordSet[i][col - 1];

				if (sCampo.equalsIgnoreCase(sCampo2)) {
					sFindText = sText;
					iFindCol = col;
					bFindCaseSensitive = false;

					iRowIndex = i;
					EOF = false;
					return iRowIndex + 1;
				}

			}
		}

		sFindText = "";
		iFindCol = 0;

		// no match!
		return -1;
	}

	/**
	 * Metodo utilizzato per spostarsi su una linea del buffer.
	 *
	 * @param row
	 *            Riga del buffer che si vuol puntare.
	 */
	// metodo utilizzato per spostarsi su una linea del buffer
	public void setCurrentIndex(int row) {
		if (iRecordCount > 0 && row > 0 && row <= iRecordCount) {
			iRowIndex = row - 1;
		}
	}

	/**
	 * Metodo utilizzato per invertire l'ordine dei record del buffer.
	 */
	// Metodo utilizzato per invertire l'ordine dei record del buffer
	public void revert() {
		String[][] ArrayApp = new String[iRecordCount][iFieldCount];

		for (int i = 0; i < iRecordCount; i++) {
            System.arraycopy(theRecordSet[iRecordCount - i - 1], 0, ArrayApp[i], 0, iFieldCount);
		}

		System.arraycopy(ArrayApp, 0, theRecordSet, 0, iRecordCount);

        setSortedField(0);

		// Generate notification
		fireTableRowsUpdated(0, iRecordCount - 1);
		fireContentsChanged(0, iRecordCount - 1);
	}

	/**
	 * Ritorna il contenuto del campo della riga correntemente puntata.
	 *
	 * @param iFieldNumber
	 *            Numero del campo di cui si vuole il valore.
	 * @return Stringa contenente il valore del campo.
	 */
	public String getField(int iFieldNumber) {
		if (iFieldNumber > iFieldCount) {
			JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo getField:\nCampo " + iFieldNumber + " inesistente!");
			// ErrorManager.showErrorMessage("Buffer, metodo getField:\nCampo
			// "+iFieldNumber+" inesistente!");
			return "END OF BUFFER";
		} else {
			return theRecordSet[iRowIndex][iFieldNumber - 1];
		}
	}

	/**
	 * Ritorna il contenuto del campo della riga correntemente puntata come
	 * valore di tipo intero.
	 *
	 * @param iFieldNumber
	 *            Numero del campo di cui si vuole il valore.
	 * @return Intero indicante il contenuto del campo. Altrimenti 0 in caso di
	 *         errore.
	 */
	public int getFieldByInt(int iFieldNumber) {
		if (iFieldNumber > iFieldCount) {
			JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo getFieldByInt:\nCampo " + iFieldNumber + " inesistente!");
			// ErrorManager.showErrorMessage("Buffer, metodo
			// getFieldByInt:\nCampo "+iFieldNumber+" inesistente!");
			return 0;
		} else {
			try {
				return Integer.parseInt(theRecordSet[iRowIndex][iFieldNumber - 1]);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(new JFrame(), "Record, metodo getFieldByInt: " + e + "\nCampo " + iFieldNumber + " non numerico!");
				// ErrorManager.showErrorMessage("Record, metodo getFieldByInt:
				// "+e+"\nCampo "+iFieldNumber+" non numerico!");
				return 0;
			}
		}
	}

	/**
	 * Imposta il contenuto del Buffer relativo alla colonna e al campo
	 * specificato.
	 *
	 * @param iRecordNumber
	 *            Indica il numero del record (riga) del Buffer su cui vogliamo
	 *            scrivere.
	 * @param iFieldNumber
	 *            Indica il numero del campo relativo al record del Buffer
	 *            specificato.
	 * @param sFieldValue
	 *            Testo che si vuol inserire nel campo specificato.
	 */
	public void setField(int iRecordNumber, int iFieldNumber,
			String sFieldValue) {
		String field_type;

		if (iRecordNumber > iRecordCount) {
			JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo setField:\nRecord n@ " + iRecordNumber + " inesistente !");
			// ErrorManager.showErrorMessage("Buffer, metodo setField:\nRecord
			// n@ "+iRecordNumber+" inesistente !");
			return;
		}

		try {
			try {
				theRecordSet[iRecordNumber - 1][iFieldNumber - 1] = sFieldValue;
			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo setField: " + e + "\nCampo n@ " + iFieldNumber + " inesistente !");
				// ErrorManager.showErrorMessage("Buffer, metodo setField:
				// "+e+"\nCampo n@ "+iFieldNumber+" inesistente !");
			}
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo setField: " + e + "\nCampo n@ " + iFieldNumber + " inesistente !");
			// ErrorManager.showErrorMessage("Buffer, metodo setField:
			// "+e+"\nCampo n@ "+iFieldNumber+" inesistente !");
		}

		// controllo il tipo del campo
		field_type = checkFieldType(sFieldValue);

		// setto il tipo del valore del campo richiamando il metodo del
		// recordset
		setFieldType(iFieldNumber, field_type);

		// se modifico il campo secondo cui @ attualmente ordinato il Buffer
		// non so pi@ se l'ordinamento @ valido.
		if (getSortedField() == iFieldNumber) {
			setSortedField(0);
		}

		// Generate
		fireTableCellUpdated(iRecordNumber - 1, iFieldNumber - 1);
	}

	/**
	 * Elimina la riga del Buffer indicata nel parametro passato.
	 *
	 * @param item
	 *            Riga del Buffer da eliminare.
	 */
	public void delete(int item) {
		if (iRecordCount > 0) {
			String[][] ArrayApp = new String[iRecordCount - 1][iFieldCount];

			if (item > 1) {
				int numelem = item - 1;
				System.arraycopy(theRecordSet, 0, ArrayApp, 0, numelem);

				if (iRecordCount > item) {
					System.arraycopy(theRecordSet, item, ArrayApp, item - 1, iRecordCount - numelem - 1);
				}
			} else {
				System.arraycopy(theRecordSet, item, ArrayApp, 0, iRecordCount - 1);
			}

			theRecordSet = new String[iRecordCount - 1][iFieldCount];
			System.arraycopy(ArrayApp, 0, theRecordSet, 0, iRecordCount - 1);

            // decrementa il numero di elementi presenti nel buffer..
			iRecordCount--;

			if (iRecordCount == 0) {
				resetVar();
			}

			// Generate notification
			fireTableRowsDeleted(item - 1, item - 1);
			fireIntervalRemoved(item - 1, item - 1);
		}
	}

	/**
	 * Sostituisce il contenuto di un record del Buffer con la stringa di testo
	 * specificata.
	 *
	 * @param row
	 *            Riga del Buffer che si vuol modificare.
	 * @param item
	 *            Testo da inserire.
	 */
	// Metodo utilizzato per sostituire un'intero record del buffer
	public void replace(int row, String item) {
		if (iRecordCount < row || row < 1) {
        } else {
			setRecordValues(item, row - 1);

			// Generate notification
			fireTableDataChanged();
			fireContentsChanged(row - 1, row - 1);
		}
	}

	/**
	 * Metodo utilizzato per stabilire di che tipo (STRING, NUMERIC, DATE) @ il
	 * contenuto del campo.
	 *
	 * @param field
	 *            Numero del campo (del record correntemente puntato) del quale
	 *            si vuol conoscere il tipo.
	 * @return Stringa indicante il tipo di dato contenuto nel campo.
	 */
	public String getFieldType(int field) {
		if (aiFieldType[field - 1][0] == -1) {
			return "STRING";
		} else if (aiFieldType[field - 1][1] == -1) {
			return "NUMERIC";
		} else if (aiFieldType[field - 1][2] != 0) {
			return "DATE";
		} else {
			return "";
		}
	}

	/**
	 * Metodo usato per settare il numero di campi contenuti in un record; viene
	 * richiamato dal metodo setLine della classe Record.
	 *
	 * @param count
	 *            Numero di campi del record.
	 */
	// Metodo usato per settare il numero di campi contenuti in un record;
	// viene richiamato dal metodo setLine della classe Record.
	void setFieldCount(int count) {
		iFieldCount = count;
	}

	private int countRecordField(String record) {
		int cont = 1,pos = 0;
		while (true) {
			pos = record.indexOf(("@" + cont), pos);
			if (pos == -1) {
				break;
			}
			cont++;
		}

		return cont - 1;
	}

	// Non usare dall'esterno !
	void createRecordSet(String sFirstItem) {
		// instanzia il RecordSet, grande quanto iDefaultAdd
		// e setta il numero di campi del buffer
		aiFieldType = null;
		theRecordSet = null;
		setFieldCount(countRecordField(sFirstItem));
		theRecordSet = new String[iDefaultAdd][iFieldCount];
		aiFieldType = new int[iFieldCount][3];
	}

	void setRecordValuesNoCheckFieldType(String line, int iRecordNumber) {
		String sFieldValue;
		int token = 1,extra;

		// controlla che non ci sia uno spazio vuoto all'inizio..
		if (line.charAt(0) == ' ') {
			line = line.substring(1);
		}

		StringTokenizer sTokenizer = new StringTokenizer(line,"@");

		/////////////////////////////////////////////////////////////////////////////////////////////
		// ..In questo modo i Record possono avere numero di campi diversi.
		// (N.B.) Comunque i vari Record che vengono aggiunti dopo il primo, non
		///////////////////////////////////////////////////////////////////////////////////////////// possono
		// avere numero di campi maggiore di quest'ultimo !
		int iCount = sTokenizer.countTokens();
		/////////////////////////////////////////////////////////////////////////////////////////////

		for (int iNumCampo = 1; iNumCampo <= iCount; iNumCampo++) {
			///////////////////////////////////////////
			// estraggo il campo corrente...
			sFieldValue = sTokenizer.nextToken();

			if (token < 10) {
				extra = 1;
			} else {
				extra = 2;
			}

			sFieldValue = sFieldValue.substring(extra);

			token++;
			///////////////////////////////////////////

			// nel caso ci sia il carattere @ in un campo, scompatto tutto il
			// record usando
			// ValoreBuffer, in quanto questo metodo darebbe errore!
			if (iNumCampo > iFieldCount) {
				try {
					ValoreBuffer(line, iRecordNumber);
					return;
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo setRecordValues:\nErrore durante lo spacchettamento del record n. " + iRecordNumber + " !" + "\n" + (line.length() > 100 ? (line.substring(0, 50) + "\n" + line.substring(50)) : line));
					// ErrorManager.showErrorMessage("Buffer, metodo
					// setRecordValues:\nErrore durante lo spacchettamento del
					// record n. " + iRecordNumber + " !" + "\n" +
					// (line.length() > 100 ? (line.substring(0,50) + "\n" +
					// line.substring(50)) : line));
					return;
				}
			}

			// salvo il valore del campo nell'array del Record
			if (getFieldType(iNumCampo).equals("DATE") && sFieldValue.length() == 10) {
				theRecordSet[iRecordNumber][iNumCampo - 1] = sFieldValue.substring(0, 10);
			} else {
				theRecordSet[iRecordNumber][iNumCampo - 1] = sFieldValue.trim();
			}
		}
	}

	void setRecordValues(String line, int iRecordNumber) {
		String field_type,sFieldValue;
		int token = 1,extra;

		// controlla che non ci sia uno spazio vuoto all'inizio..
		if (line.charAt(0) == ' ') {
			line = line.substring(1);
		}

		StringTokenizer sTokenizer = new StringTokenizer(line,"@");

		/////////////////////////////////////////////////////////////////////////////////////////////
		// ..In questo modo i Record possono avere numero di campi diversi.
		// (N.B.) Comunque i vari Record che vengono aggiunti dopo il primo, non
		///////////////////////////////////////////////////////////////////////////////////////////// possono
		// avere numero di campi maggiore di quest'ultimo !
		int iCount = sTokenizer.countTokens();
		/////////////////////////////////////////////////////////////////////////////////////////////

		for (int iNumCampo = 1; iNumCampo <= iCount; iNumCampo++) {
			///////////////////////////////////////////
			// estraggo il campo corrente...
			sFieldValue = sTokenizer.nextToken();

			if (token < 10) {
				extra = 1;
			} else {
				extra = 2;
			}

			sFieldValue = sFieldValue.substring(extra);

			token++;
			///////////////////////////////////////////

			// nel caso ci sia il carattere @ in un campo, scompatto tutto il
			// record usando
			// ValoreBuffer, in quanto questo metodo darebbe errore!
			if (iNumCampo > iFieldCount) {
				try {
					ValoreBuffer(line, iRecordNumber);
					return;
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo setRecordValues:\nErrore durante lo spacchettamento del record n. " + iRecordNumber + " !" + "\n" + (line.length() > 100 ? (line.substring(0, 50) + "\n" + line.substring(50)) : line));
					// ErrorManager.showErrorMessage("Buffer, metodo
					// setRecordValues:\nErrore durante lo spacchettamento del
					// record n. " + iRecordNumber + " !" + "\n" +
					// (line.length() > 100 ? (line.substring(0,50) + "\n" +
					// line.substring(50)) : line));
					return;
				}
			}

			// controllo il tipo del campo
			field_type = checkFieldType(sFieldValue);

			// setto il tipo del valore del campo richiamando il metodo del
			// RecordSet
			field_type = setFieldType(iNumCampo, field_type);

			// salvo il valore del campo nell'array del Record
			if (field_type.equals("DATE") && sFieldValue.length() == 10) {
				theRecordSet[iRecordNumber][iNumCampo - 1] = sFieldValue.substring(0, 10);
			} else {
				theRecordSet[iRecordNumber][iNumCampo - 1] = sFieldValue.trim();
			}
		}
	}

	/**
	 * Metodo utilizzato per aggiungere un'elemento al buffer.
	 *
	 * @param item
	 *            Stringa contenente i campi da inserire nel Buffer.
	 */
	public void addNew(String item) {
		int iSize = theRecordSet == null ? 0 : theRecordSet.length;

		// se la grandezza del RecordSet @ minore del numero di riga, aggiungo
		// spazio sul RecordSet, tanto quanto iDefaultAdd
		if (iSize <= (iRecordCount + 1)) {
			// il recordset non viene creato in caso di errore, quindi lo creo
			if (theRecordSet == null) {
				createRecordSet(item);
			}
			// aumento lo spazio sul RecordSet
			addSpaceForRecord(iDefaultAdd);
		}

		// aggiunge il nuovo elemento; se il server torna DATA_TYPE usa
		// setRecordValuesNoCheckFieldType che spezzetta senza controllo del
		// tipo
		if (!bDataTypeKnow) {
			setRecordValues(item, iRecordCount);
		} else {
			setRecordValuesNoCheckFieldType(item, iRecordCount);
		}

		////////////////////////////////////////////////////////////////////////
		// se aggiungo un elemento al Buffer non so
		// pi@ se l'ordinamento @ valido

		// N.B.) Alla fine del primo riempimento di un buffer
		// da parte di QueryManager e/o ThreadQuering, i dati
		// sul formato del buffer (se inviati dal Server),
		// vengono reimpostati.
		setSortedField(0);
		////////////////////////////////////////////////////////////////////////

		// Incrementa il numero di elementi contenuti nell'array originale
		iRecordCount++;

		EOF = false;

		// Generate notification
		fireTableRowsInserted(getRecordCount() - 1, getRecordCount() - 1);
		fireIntervalAdded(getRecordCount() - 1, getRecordCount() - 1);

	}

	private void addSpaceForRecord(int iNumRecords) {
		// dimensiona un array di appoggio grande quanto quello attuale
		String[][] ArrayApp = new String[iRecordCount][iFieldCount];

		// copia il contenuto attuale nell'array di appoggio
		if (iRecordCount > 0) {
			System.arraycopy(theRecordSet, 0, ArrayApp, 0, iRecordCount);
		}

		// ridimensiona l'array originale aumentandolo di DEF_ADD
        theRecordSet = new String[iRecordCount + iNumRecords][iFieldCount];

		// copia il contenuto dell'array di appoggio nell'originale
		if (iRecordCount > 0) {
			System.arraycopy(ArrayApp, 0, theRecordSet, 0, iRecordCount);
		}

    }

	/**
	 * Ritorna il record del Buffer correntemente selezionato.
	 */
	public int getCurrentIndex() {
		return iRowIndex + 1;
	}

	// Non utilizzare dall'esterno !
	public String[][] getRecordSet() {
		return theRecordSet;
	}

	/**
	 * Metodo utilizzato per prendere una linea intera dal buffer prima linea =
	 * 1.
	 *
	 * @param item
	 *            Numero del record che si vuol estrarre.
	 * @return Stringa contenente il record del Buffer estratto.
	 */
	public String getItem(int item) {
		item--;

		if (item >= iRecordCount || item < 0) {
			JOptionPane.showMessageDialog(new JFrame(), "Buffer, metodo getItem:\nRecord n@ " + item + " inesistente !");
			// ErrorManager.showErrorMessage("Buffer, metodo getItem:\nRecord n@
			// "+item+" inesistente !");
			return "END OF BUFFER";
		}

        StringBuilder sLine = new StringBuilder();
		for (int i = 0; i < iFieldCount; i++) {
			sLine.append("@").append(i + 1).append(theRecordSet[item][i]).append(i == iFieldCount - 1 ? "" : " ");
		}

		return sLine.toString();
	}

	public void setRecordSet(String[][] RecordSet, String[] asFieldType,
                             int iFieldCount, int iRecordCount) {
		this.iRecordCount = iRecordCount;
		this.iFieldCount = iFieldCount;
		iRowIndex = 0;

		theRecordSet = RecordSet;
		/*
		 * theRecordSet = new String[iRecordCount][iFieldCount]; for(int
		 * i=0;i<iRecordCount;i++) for(int c=0;c<iFieldCount;c++)
		 * theRecordSet[i][c] = RecordSet[i][c];
		 */

        aiFieldType = new int[iFieldCount][3];
		for (int i = 1; i <= iFieldCount; i++) {
			setFieldType(i, asFieldType[i - 1]);
		}

	}

	private static String checkFieldType(String sFieldValue) {
		boolean bNum;
		int iLen;

		sFieldValue = sFieldValue.trim();
		iLen = sFieldValue.length();

		// controllo se @ una data
		if (iLen == 10) {
			if (sFieldValue.charAt(2) == '/' && sFieldValue.charAt(5) == '/') {
				return "DATE";
			} else {

                // controllo se @ un numero
				for (int i = 0; i < iLen; i++) {
					bNum = Character.isDigit(sFieldValue.charAt(i));
					// se false viene impostato il tipo STRING
					if (!bNum) {
						return "STRING";
					}
				}

				// altrimenti viene impostato il tipo NUMERIC
				return "NUMERIC";
			}
		} else {

            // controllo se @ un numero
			for (int i = 0; i < iLen; i++) {
				bNum = Character.isDigit(sFieldValue.charAt(i));
				// se false viene impostato il tipo STRING
				if (!bNum) {
					return "STRING";
				}
			}

			if (iLen > 0) {
				// se non @ vuoto viene impostato il tipo NUMERIC
				return "NUMERIC";
			} else {
				// altrimenti viene lasciato il tipo PRECedente
				return "PREC";
			}
		}
	}

	private void ValoreBuffer(String row, int iNumRecord)
			throws java.lang.ArrayIndexOutOfBoundsException {
		int In,Out;
		String sFieldType,sField;

		for (int NumCampo = 1; NumCampo <= iFieldCount; NumCampo++) {

            String campo1;
			String campo2;

			campo1 = "@" + NumCampo;
			campo2 = "@" + (NumCampo + 1);

			In = row.indexOf(campo1);
			int In2 = row.lastIndexOf(campo1);

			if (In == -1) {
				throw new java.lang.ArrayIndexOutOfBoundsException();
			}

			Out = row.indexOf(campo2, In);

			int out = Out;
			if (out == -1) {
				out = row.length() - 1;
			}

			if ((In2 > In) && (In2 < out)) {
				In = In2;
			}

			if (Out > In) {
				sField = row.substring(In + campo1.length(), Out - 1);
			} else {
				sField = row.substring(In + (campo1.length()));
			}

			// controllo il tipo del campo
			sFieldType = checkFieldType(sField);

			// setto il tipo del valore del campo richiamando il metodo del
			// RecordSet
			sFieldType = setFieldType(NumCampo, sFieldType);

			// salvo il valore del campo nell'array del Record
			if (sFieldType.equals("DATE") && sField.length() == 10) {
				theRecordSet[iNumRecord][NumCampo - 1] = sField.substring(0, 10);
			} else {
				theRecordSet[iNumRecord][NumCampo - 1] = sField.trim();
			}
		}
	}

	private void resetVar() {
		theRecordSet = null;
		aiFieldType = null;
		iRecordCount = 0;
		iRowIndex = 0;
		ERROR = false;
		sFindText = "";
		iFindCol = 0;
		bFindCaseSensitive = false;
		TAG = null;
		DB_TIME = null;
		QUERY_NAME = null;
		EXIT = null;
		setDataTypeKnow(false);
		iSortedField = 0;
		bSortAlways = false;
		EOF = true;
		System.gc();
	}

	public int getErrorCode() {
		return ERROR_CODE;
	}

	public void setErrorCode(int code) {
		ERROR_CODE = code;
	}

	/**
	 * Ritorna il numero di righe della Table associata al Buffer.
	 *
	 * @return Indica il numero di righe presenti nella Table associata al
	 *         Buffer.
	 * @see #getRecordCount()
	 */
	/* Ridefinizione della classe AbstractTableModel */

	@Override
	public int getRowCount() {
		return getRecordCount();
	}

	/**
	 * Ritorna il numero di colonne del Buffer.
	 *
	 * @return Numero di colonne del Buffer.
	 */

	@Override
	public int getColumnCount() {
		return iFieldCount;
	}

	/**
	 * Ritorna il nome associato alla relativa colonna del Buffer.
	 *
	 * @param column
	 *            Numero della colonna della quale si vuol estrarre il nome.
	 * @return Nome corrente della colonna del Buffer.
	 */
	@Override
	public String getColumnName(int column) {
		if (asColumnNames == null || asColumnNames.length <= column) {
			return super.getColumnName(column);
		} else {
			return asColumnNames[column];
		}
	}

	/**
	 * Metodo che ritorna un booleano indicante se il campo specificato dalle
	 * cordinate (riga, colonna) @ editabile.
	 *
	 * @param row
	 *            Riga del Buffer.
	 * @param column
	 *            Colonna del Buffer.
	 * @return True se il campo @ editabile.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	/**
	 * Metodo che ritorna il valore contenuto nel campo specificato dalle
	 * cordinate (riga, colonna).
	 *
	 * @param rowIndex
	 *            Indice della riga del Buffer.
	 * @param columnIndex
	 *            Indice della colonna del Buffer.
	 * @return Object indicante il contenuto del campo. Per poterlo utilizzare @
	 *         necessario effettuare il relativo cast (parse).
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return theRecordSet[rowIndex][columnIndex];
	}

	/**
	 * Metodo per impostare un Object nel campo specificato dalle cordinate
	 * passate alla funzione.
	 *
	 * @param aValue
	 *            Object da inserire nel campo.
	 * @param row
	 *            Indice della riga del Buffer.
	 * @param column
	 *            Indice della colonna del Buffer.
	 */
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		setField(row + 1, column + 1, aValue.toString().trim());
	}

	@Override
	public Class<String> getColumnClass(int columnIndex) {
		return String.class;
	}

	/* Implementazione dell'interfaccia ComboBoxModel */

	// implements javax.swing.ComboBoxModel
	@Override
	public Object getSelectedItem() {
		return null;
	}

	// implements javax.swing.ComboBoxModel
	@Override
	public void setSelectedItem(Object anObject) {
	}

	/* Implementazione dell'interfaccia ListModel */

	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class, l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class, l);
	}

	/**
	 * Ritorna l'i-esimo elemento del buffer.
	 *
	 * @param index
	 *            Indice della riga del Buffer.
	 * @return Object indicante l'i-esimo elemento del Buffer.
	 */
	@Override
	public Object getElementAt(int index) {
		if (index >= 0 && index < theRecordSet.length) {
			return theRecordSet[index];
		} else {
			return null;
		}
	}

	@Override
	public int getSize() {
		return getRecordCount();
	}

	/**
	 * Richiamato ogni qual volta vengono aggiunti al model uno o pi@ elementi.
	 * I nuovi elementi sono specificati dall'intervallo chiuso index0, index1.
	 *
	 * @param index0
	 *            Un estremo del nuovo intervallo.
	 * @param index1
	 *            L'altro estremo del nuovo intervallo.
	 * @see EventListenerList
	 * @see ListDataListener
	 * @see ListDataEvent
	 */
	protected void fireIntervalAdded(int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,index0,index1);
				}
				((ListDataListener) listeners[i + 1]).intervalAdded(e);
			}
		}
	}

	/**
	 * Richiamato ogni qual volta vengono eliminati dal model uno o pi@
	 * elementi. Gli elementi rimossi sono specificati dall'intervallo chiuso
	 * index0, index1.
	 *
	 * @param index0
	 *            Un estremo dell' intervallo.
	 * @param index1
	 *            L'altro estremo dell'intervallo.
	 * @see EventListenerList
	 * @see ListDataListener
	 * @see ListDataEvent
	 */
	protected void fireIntervalRemoved(int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,index0,index1);
				}
				((ListDataListener) listeners[i + 1]).intervalRemoved(e);
			}
		}
	}

	/**
	 * Richiamato ogni qual volta vengono cambiati dal model uno o pi@ elementi.
	 * Gli elementi cambiati sono specificati dall'intervallo chiuso index0,
	 * index1.
	 *
	 * @param index0
	 *            Un estremo dell' intervallo.
	 * @param index1
	 *            L'altro estremo dell'intervallo.
	 * @see EventListenerList
	 * @see ListDataListener
	 * @see ListDataEvent
	 */
	protected void fireContentsChanged(int index0, int index1) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,index0,index1);
				}
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}

	public void refresh() {
		// generate notification
		fireTableRowsUpdated(0, iRecordCount - 1);
		fireContentsChanged(0, iRecordCount - 1);
	}

    public boolean isERROR() {
        return ERROR;
    }

    public void setERROR(boolean ERROR) {
        this.ERROR = ERROR;
    }

    /*
	 * protected void finalize() throws Throwable {
	 * System.out.print("\nFINALIZE"+"_"+this); super.finalize(); }
	 */

}
