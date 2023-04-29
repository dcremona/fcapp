package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelReader{

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReader.class);

	public static final String COLONNA_RISPOSTA = "C";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT1 = "c:\\temp\\Psv-Principi Contabili-MA106_1.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT1 = "c:\\temp\\Psv-Principi Contabili-MA106_1_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT2 = "c:\\temp\\Organizzazione e sviluppo delle risorse umane-MA106_da ordinare.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT2 = "c:\\temp\\Organizzazione e sviluppo delle risorse umane-MA106_da ordinare_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT3 = "c:\\temp\\Psv-Business Planning-MA106_2.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT3 = "c:\\temp\\Psv-Business Planning-MA106_2_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT4 = "c:\\temp\\Statistica Aziendale - da ordinare.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT4 = "c:\\temp\\Statistica Aziendale - da ordinare_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT5 = "c:\\temp\\Programmazione e Controllo- da ordinare.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT5 = "c:\\temp\\Programmazione e Controllo- da ordinare_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT6 = "c:\\temp\\Diritto Tributario- da ordinare.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT6 = "c:\\temp\\Diritto Tributario- da ordinare_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT7 = "c:\\temp\\BE.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT7 = "c:\\temp\\BE_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT8 = "c:\\temp\\GE.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT8 = "c:\\temp\\GE_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT9 = "c:\\temp\\DCA.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT9 = "c:\\temp\\DCA_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT10 = "c:\\temp\\DF.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT10 = "c:\\temp\\DF_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_INPUT11 = "c:\\temp\\PE.xlsx";
	public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT11 = "c:\\temp\\PE_OUTPUT.xlsx";

	public static final String SAMPLE_XLSX_FILE_PATH_FINAL_OUTPUT = "c:\\temp\\FINAL_OUTPUT.xlsx";

	public static void main(String[] args)
			throws Exception, InvalidFormatException {

		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT1,SAMPLE_XLSX_FILE_PATH_OUTPUT1);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT2,SAMPLE_XLSX_FILE_PATH_OUTPUT2);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT3,SAMPLE_XLSX_FILE_PATH_OUTPUT3);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT4,SAMPLE_XLSX_FILE_PATH_OUTPUT4);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT5,SAMPLE_XLSX_FILE_PATH_OUTPUT5);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT6,SAMPLE_XLSX_FILE_PATH_OUTPUT6);
		
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT7, SAMPLE_XLSX_FILE_PATH_OUTPUT7);
	    // processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT8, SAMPLE_XLSX_FILE_PATH_OUTPUT8);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT9,SAMPLE_XLSX_FILE_PATH_OUTPUT9);
		// processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT10,SAMPLE_XLSX_FILE_PATH_OUTPUT10);
		processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT11, SAMPLE_XLSX_FILE_PATH_OUTPUT11);

		// buildUnionFileOutputXls();
	}

	public static void buildUnionFileOutputXls() throws Exception {

		String[] filesOutput = new String[] { SAMPLE_XLSX_FILE_PATH_OUTPUT1, SAMPLE_XLSX_FILE_PATH_OUTPUT2, SAMPLE_XLSX_FILE_PATH_OUTPUT3, SAMPLE_XLSX_FILE_PATH_OUTPUT4, SAMPLE_XLSX_FILE_PATH_OUTPUT5, SAMPLE_XLSX_FILE_PATH_OUTPUT6 };

		Map<String, Domanda> domande = new HashMap<>();
		int conta = 1;

		for (String f : filesOutput) {

			LOGGER.info("file ==> " + f);
			// Creating a Workbook from an Excel file (.xls or .xlsx)
			Workbook workbook = WorkbookFactory.create(new File(f));

			int totale = 0;

			// Retrieving the number of sheets in the Workbook
			LOGGER.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

			// 1. You can obtain a sheetIterator and iterate over it
			Iterator<Sheet> sheetIterator = workbook.sheetIterator();
			LOGGER.info("Retrieving Sheets using Iterator");
			while (sheetIterator.hasNext()) {
				Sheet sheet = sheetIterator.next();
				LOGGER.info("=> " + sheet.getSheetName());
			}

			// Getting the Sheet at index zero
			Sheet sheet = workbook.getSheetAt(0);

			// Create a DataFormatter to format and get each cell's value as
			// String
			DataFormatter dataFormatter = new DataFormatter();

			LOGGER.info("Iterating over Rows and Columns using for-each loop");
			Domanda domanda = null;
			for (Row row : sheet) {

				String a = "";
				String b = "";
				String c = "";
				String d = "";

				for (Cell cell : row) {

					String cellValue = dataFormatter.formatCellValue(cell);
					CellStyle cellStyle = cell.getCellStyle();
					LOGGER.info("cellStyle " + cellStyle);
					if (cell.getColumnIndex() == 0) {
						a = cellValue;
					} else if (cell.getColumnIndex() == 1) {
						b = cellValue;
					} else if (cell.getColumnIndex() == 2) {
						c = cellValue;
					} else if (cell.getColumnIndex() == 3) {
						d = cellValue;
					}
				}

				if (StringUtils.isEmpty(a) && StringUtils.isEmpty(b) && StringUtils.isEmpty(c)) {
					LOGGER.info("SCARTO RIGA VUOTA ");
					continue;
				}

				if (StringUtils.isEmpty(a)) {
					// DOMANDA
					domanda = new Domanda();
					domanda.setId(conta);
					domanda.setDescrizione(b);
					domanda.setDescrizioneOrig(d);

					domande.put(domanda.getDescrizione() + "_" + conta, domanda);
					conta++;
					totale++;
				} else {

					// RISPOSTA
					if ("X".equals(c.toUpperCase().trim())) {
						Risposta risposta = new Risposta();
						risposta.setId(1);
						risposta.setDescrizione(b);
						risposta.setTipoRisposta(a);

						domanda.setRisposta(risposta);
					} else {
						LOGGER.info("NO RISPOSTA ");
					}

				}
			}

			LOGGER.info("TOTALE NUMERO DOMANDE " + totale);
			LOGGER.info("TOTALE COMPLESSIVO DOMANDE " + conta);

			// Closing the workbook
			workbook.close();
		}

		writeFileXls(domande, SAMPLE_XLSX_FILE_PATH_FINAL_OUTPUT, false);

	}

	public static void writeFileXls(Map<String, Domanda> domande, String output,
			boolean writeD) throws Exception {

		XSSFWorkbook workbook2 = new XSSFWorkbook();
		XSSFSheet sheet2 = workbook2.createSheet("Test Java");

		CellStyle cellStyle = sheet2.getWorkbook().createCellStyle();
		Font font = sheet2.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 16);
		cellStyle.setFont(font);

		int rowCount = 0;
		List<Domanda> domandeByDesc = new ArrayList<>(domande.values());
		Collections.sort(domandeByDesc, Comparator.comparing(Domanda::getDescrizione));
		for (Domanda d : domandeByDesc) {

			CellStyle newStyleD = sheet2.getWorkbook().createCellStyle();
			newStyleD.cloneStyleFrom(d.getCellStyle());

			Risposta r = d.getRisposta();
			if (r == null) {
				LOGGER.info("DOMANDA SENZA RISPOSTA --> RIGA=" + d.getRigaXlx() + " DOMANDA=" + d.getDescrizioneOrig());
				continue;
			}

			CellStyle newStyleR = sheet2.getWorkbook().createCellStyle();
			newStyleR.cloneStyleFrom(r.getCellStyle());

			Row rowDomanda = sheet2.createRow(++rowCount);
		
			Cell cellDomA = rowDomanda.createCell(0);
			cellDomA.setCellValue("");

			Cell cellDomB = rowDomanda.createCell(1);
			cellDomB.setCellValue(d.getDescrizione());
			cellDomB.setCellStyle(cellStyle);

			Cell cellDomC = rowDomanda.createCell(2);
			cellDomC.setCellValue("");

			if (writeD) {
				Cell cellDomD = rowDomanda.createCell(3);
				cellDomD.setCellValue(d.getDescrizioneOrig());
				cellDomD.setCellStyle(cellStyle);
			}

			Row rowRisposta = sheet2.createRow(++rowCount);
			Cell cellRisA = rowRisposta.createCell(0);
			cellRisA.setCellValue(r.getTipoRisposta());
			Cell cellRisB = rowRisposta.createCell(1);
			cellRisB.setCellValue(r.getDescrizione());
			cellRisB.setCellStyle(newStyleR);
			Cell cellRisC = rowRisposta.createCell(2);
			cellRisC.setCellValue("x");

		}

		LOGGER.info("autoSizeColumns");
		autoSizeColumns(workbook2);

		try (FileOutputStream outputStream = new FileOutputStream(output)) {
			try {
				workbook2.write(outputStream);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			LOGGER.error(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage());
			e1.printStackTrace();
		}
		// Closing the workbook
		workbook2.close();

		LOGGER.info("Closing the workbook");

	}

	public static void autoSizeColumns(Workbook workbook) {
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			if (sheet.getPhysicalNumberOfRows() > 0) {
				Row row = sheet.getRow(sheet.getFirstRowNum());
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					int columnIndex = cell.getColumnIndex();
					sheet.autoSizeColumn(columnIndex);
				}
			}
		}
	}

	public static void processSingleFileXls(String input, String output)
			throws Exception {

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(new File(input));

		// Retrieving the number of sheets in the Workbook
		LOGGER.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

		/*
		 * =============================================================
		 * Iterating over all the sheets in the workbook (Multiple ways)
		 * =============================================================
		 */

		// 1. You can obtain a sheetIterator and iterate over it
		Iterator<Sheet> sheetIterator = workbook.sheetIterator();
		LOGGER.info("Retrieving Sheets using Iterator");
		while (sheetIterator.hasNext()) {
			Sheet sheet = sheetIterator.next();
			LOGGER.info("=> " + sheet.getSheetName());
		}

		// 2. Or you can use a for-each loop
		LOGGER.info("Retrieving Sheets using for-each loop");
		for (Sheet sheet : workbook) {
			LOGGER.info("=> " + sheet.getSheetName());
		}

		// 3. Or you can use a Java 8 forEach with lambda
		LOGGER.info("Retrieving Sheets using Java 8 forEach with lambda");
		workbook.forEach(sheet -> {
			LOGGER.info("=> " + sheet.getSheetName());
		});

		/*
		 * ==================================================================
		 * Iterating over all the rows and columns in a Sheet (Multiple ways)
		 * ==================================================================
		 */

		// Getting the Sheet at index zero
		Sheet sheet = workbook.getSheetAt(0);

		// Create a DataFormatter to format and get each cell's value as String
		DataFormatter dataFormatter = new DataFormatter();

		// // 1. You can obtain a rowIterator and columnIterator and iterate
		// over
		// // them
		// LOGGER.info("\n\nIterating over Rows and Columns using
		// Iterator\n");
		// Iterator<Row> rowIterator = sheet.rowIterator();
		// while (rowIterator.hasNext()) {
		// Row row = rowIterator.next();
		//
		// // Now let's iterate over the columns of the current row
		// Iterator<Cell> cellIterator = row.cellIterator();
		//
		// while (cellIterator.hasNext()) {
		// Cell cell = cellIterator.next();
		// String cellValue = dataFormatter.formatCellValue(cell);
		// System.out.print(cellValue + "\t");
		// }
		// LOGGER.info();
		// }

		Map<String, Domanda> domande = new HashMap<>();
		int conta = 1;
		// 2. Or you can use a for-each loop to iterate over the rows and
		// columns

		LOGGER.info("Iterating over Rows and Columns using for-each loop");
		Domanda domanda = null;
		for (Row row : sheet) {

			String a = "";
			String b = "";
			String c = "";
			String d = "";
			CellStyle cellStyleA = null;
			CellStyle cellStyleB = null;

			for (Cell cell : row) {

				String cellValue = dataFormatter.formatCellValue(cell);
				CellStyle cellStyle = cell.getCellStyle();

				if (cell.getColumnIndex() == 0) {
					a = cellValue;
					cellStyleA = cellStyle;
				} else if (cell.getColumnIndex() == 1) {
					b = cellValue;
					cellStyleB = cellStyle;
				} else if (cell.getColumnIndex() == 2) {
					c = cellValue;
				} else if (cell.getColumnIndex() == 3) {
					d = cellValue;
				}
			}
			int rigaXlx = row.getRowNum();
			if (input.equals(SAMPLE_XLSX_FILE_PATH_INPUT8) && rigaXlx> 1253) {
				c = d;
			}
			if (StringUtils.isEmpty(a) && StringUtils.isEmpty(b) && StringUtils.isEmpty(c)) {
				LOGGER.info("SCARTO RIGA VUOTA " + rigaXlx);
				continue;
			}

			String risp = c;
			if (COLONNA_RISPOSTA.equals("D")) {
				risp = d;
			}

			if ("A".equals(a.toUpperCase().trim()) || "B".equals(a.toUpperCase().trim()) || "C".equals(a.toUpperCase().trim()) || "D".equals(a.toUpperCase().trim())) {
				// RISPOSTA
				if ("X".equals(risp.toUpperCase().trim())) {
					Risposta risposta = new Risposta();
					risposta.setId(1);
					risposta.setDescrizione(StringUtils.capitalize(b.trim()));
					risposta.setTipoRisposta(a);
					risposta.setCellStyle(cellStyleB);

					domanda.setRisposta(risposta);
				} else {
					if (StringUtils.isNotEmpty(risp)) {
						LOGGER.info("RECUPERO risposta valore = " + risp);
						Risposta risposta = new Risposta();
						risposta.setId(1);
						risposta.setDescrizione(StringUtils.capitalize(b.trim()));
						risposta.setTipoRisposta(a);
						risposta.setCellStyle(cellStyleB);

						domanda.setRisposta(risposta);
					}
				}

			} else {

				if (StringUtils.isNotEmpty(a)) {
					// DOMANDA
					String descOrig = a;
					int idx = a.indexOf(" ");
					a = a.substring(idx + 1, a.length());

					a = StringUtils.capitalize(a.trim());

					domanda = new Domanda();
					domanda.setId(conta);
					domanda.setDescrizioneOrig(descOrig);
					domanda.setDescrizione(a);
					domanda.setRigaXlx(rigaXlx);
					domanda.setCellStyle(cellStyleA);

					domande.put(domanda.getDescrizione() + "_" + conta, domanda);
					conta++;
				} else {
					LOGGER.info("SCARTO RIGA DOMANDA NON TROVATA");
				}
			}
		}

		LOGGER.info("TOTALE NUMERO DOMANDE " + conta);

		// Closing the workbook
		workbook.close();

		writeFileXls(domande, output, true);
	}

	/*
	 * 
	 * 
	 * public static void test(String[] args) throws
	 * DatatypeConfigurationException {
	 * 
	 * // Create a new list of student to be filled by CSV file data Map<String,
	 * Domanda> domande = new HashMap<>();
	 * 
	 * FileReader fileReader = null; CSVParser csvFileParser = null;
	 * 
	 * // Create the CSVFormat object with the header mapping CSVFormat
	 * csvFileFormat = CSVFormat.EXCEL.withDelimiter(';');
	 * 
	 * try {
	 * 
	 * // initialize FileReader object fileReader = new
	 * FileReader("c:\\temp\\test.csv");
	 * 
	 * // initialize CSVParser object csvFileParser = new
	 * CSVParser(fileReader,csvFileFormat);
	 * 
	 * // Get a list of CSV file records List<CSVRecord> csvRecords =
	 * csvFileParser.getRecords();
	 * 
	 * int conta = 1; Domanda domanda = null; for (int i = 1; i <
	 * csvRecords.size(); i++) { CSVRecord record = csvRecords.get(i);
	 * 
	 * String a = record.get(0); String b = record.get(1); String c =
	 * record.get(2); String d = record.get(3);
	 * 
	 * if (StringUtils.isEmpty(a) && StringUtils.isEmpty(b) &&
	 * StringUtils.isEmpty(c) && StringUtils.isEmpty(d)) { continue; }
	 * 
	 * if ("a".equals(a) || "b".equals(a) || "c".equals(a) || "d".equals(a)) {
	 * 
	 * if ("X".equals(c.toUpperCase())) { Risposta risposta = new Risposta();
	 * risposta.setId(1); risposta.setDescrizione(b);
	 * 
	 * domanda.setRisposta(risposta); } else { if (StringUtils.isNotEmpty(c)) {
	 * LOGGER.info("scarto risposta " + c); Risposta risposta = new Risposta();
	 * risposta.setId(1); risposta.setDescrizione(b);
	 * 
	 * domanda.setRisposta(risposta); } }
	 * 
	 * } else {
	 * 
	 * if (StringUtils.isNotEmpty(a) && StringUtils.isEmpty(b) &&
	 * StringUtils.isEmpty(c) && StringUtils.isEmpty(d)) { // DOMANDA int idx =
	 * a.indexOf(" "); a = a.substring(idx + 1, a.length());
	 * 
	 * domanda = new Domanda(); domanda.setId(conta);
	 * domanda.setDescrizione(a.toUpperCase().trim());
	 * 
	 * domande.put(domanda.getDescrizione(), domanda); conta++; } } }
	 * 
	 * // // not yet sorted // List<Domanda> domandeByDesc = new
	 * ArrayList<>(domande.values()); // Collections.sort(domandeByDesc, //
	 * Comparator.comparing(Domanda::getDescrizione)); // for (Domanda d :
	 * domandeByDesc) { // LOGGER.info(d.getDescrizione() + "\t" + d.getId());
	 * // data += d.getDescrizione() + ";"; // data += "\n"; // // Risposta r =
	 * d.getRisposta(); // if (r != null) { // LOGGER.info(" --> " +
	 * r.getDescrizione()); // data += r.getDescrizione() + ";"; // data +=
	 * "\n"; // } // }
	 * 
	 * LOGGER.info("END");
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } finally { if (fileReader
	 * != null) { try { fileReader.close(); } catch (IOException e) {
	 * 
	 * e.printStackTrace(); } } if (csvFileParser != null) { try {
	 * csvFileParser.close(); } catch (IOException e) {
	 * 
	 * e.printStackTrace(); } } }
	 * 
	 * // String path_csv = "c:\\temp\\"; // String fileName = "output"; //
	 * BufferedWriter output = null; // try { // // Create temp file. // File
	 * temp = File.createTempFile("temp_" + fileName, ".csv"); // // Delete temp
	 * file when program exits. // temp.deleteOnExit(); // // // Write to temp
	 * file // output = new BufferedWriter(new FileWriter(temp)); //
	 * output.write(data); // if (output != null) { // output.close(); // } //
	 * // // DELETE // File f = new File(path_csv + fileName + ".csv"); // if
	 * (f.exists()) { // f.delete(); // } // // // Destination directory // //
	 * File dir = new File(path_csv); // // create the destination file object
	 * // File dest = new File(path_csv + fileName + ".csv"); // // // Move file
	 * to new directory // boolean success = temp.renameTo(dest); // if
	 * (!success) { // // File was not successfully moved // } // // } catch
	 * (Exception e) { // // } finally { // if (output != null) { // try { //
	 * output.close(); // } catch (IOException e) { // // e.printStackTrace();
	 * // } // } // }
	 * 
	 * workbook = new XSSFWorkbook(); XSSFSheet sheet =
	 * workbook.createSheet("Java Books");
	 * 
	 * // Object[][] bookData = { { "Head First Java", "Kathy Serria", 79 }, {
	 * // "Effective Java", "Joshua Bloch", 36 }, { "Clean Code", "Robert //
	 * martin", 42 }, { "Thinking in Java", "Bruce Eckel", 35 }, };
	 * 
	 * // int rowCount = 0;
	 * 
	 * // for (Object[] aBook : bookData) { // Row row =
	 * sheet.createRow(++rowCount); // int columnCount = 0; // // for (Object
	 * field : aBook) { // Cell cell = row.createCell(++columnCount); // if
	 * (field instanceof String) { // cell.setCellValue((String) field); // }
	 * else if (field instanceof Integer) { // cell.setCellValue((Integer)
	 * field); // } // } // }
	 * 
	 * int rowCount = 0; List<Domanda> domandeByDesc = new
	 * ArrayList<>(domande.values()); Collections.sort(domandeByDesc,
	 * Comparator.comparing(Domanda::getDescrizione)); for (Domanda d :
	 * domandeByDesc) { LOGGER.info(d.getDescrizione() + "\t" + d.getId());
	 * 
	 * Row row1 = sheet.createRow(++rowCount); Cell cell1 = row1.createCell(0);
	 * cell1.setCellValue(d.getDescrizione());
	 * 
	 * Risposta r = d.getRisposta(); if (r != null) {
	 * LOGGER.info("                     --> " + r.getDescrizione()); Row row2 =
	 * sheet.createRow(++rowCount); Cell cell2 = row2.createCell(0);
	 * cell2.setCellValue(r.getDescrizione()); } }
	 * 
	 * try (FileOutputStream outputStream = new
	 * FileOutputStream("c:\\temp\\JavaTest.xlsx")) { try {
	 * workbook.write(outputStream); } catch (IOException e) {
	 * 
	 * e.printStackTrace(); } } catch (FileNotFoundException e1) {
	 * 
	 * e1.printStackTrace(); } catch (IOException e1) {
	 * 
	 * e1.printStackTrace(); } }
	 * 
	 */

}