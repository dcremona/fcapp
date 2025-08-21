package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class ExcelReader2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReader2.class);

    public static final String SAMPLE_XLSX_FILE_PATH_INPUT1 = "c:\\\\temp\\\\Quotazioni_Fantacalcio_Stagione_2022_23.xlsx";
    public static final String SAMPLE_XLSX_FILE_PATH_OUTPUT1 = "c:\\temp\\Quotazioni_Fantacalcio_Stagione_2022_23_OUTPUT.xlsx";

    public static void main(String[] args) throws Exception, InvalidFormatException {

        processSingleFileXls(SAMPLE_XLSX_FILE_PATH_INPUT1, SAMPLE_XLSX_FILE_PATH_INPUT1);

    }

    public static void writeFileXls(Map<String, Domanda> domande, String output, boolean writeD) throws Exception {

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

    public static void processSingleFileXls(String input, String output) throws Exception {

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(input));

        // Retrieving the number of sheets in the Workbook
        LOGGER.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

        /*
         * ============================================================= Iterating over
         * all the sheets in the workbook (Multiple ways)
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
         * ================================================================== Iterating
         * over all the rows and columns in a Sheet (Multiple ways)
         * ==================================================================
         */

        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        LOGGER.info("Iterating over Rows and Columns using for-each loop");
        for (Row row : sheet) {

            String id = "";
            String r = "";
            String rm = "";
            String nome = "";
            String squadra = "";
            String qta = "";
            String qti = "";

            for (Cell cell : row) {

                String cellValue = dataFormatter.formatCellValue(cell);
                if (cell.getColumnIndex() == 0) {
                    id = cellValue;
                } else if (cell.getColumnIndex() == 1) {
                    r = cellValue;
                } else if (cell.getColumnIndex() == 2) {
                    rm = cellValue;
                } else if (cell.getColumnIndex() == 3) {
                    nome = cellValue;
                } else if (cell.getColumnIndex() == 4) {
                    squadra = cellValue;
                } else if (cell.getColumnIndex() == 5) {
                    qta = cellValue;
                } else if (cell.getColumnIndex() == 6) {
                    qti = cellValue;
                }
            }
            int rigaXlx = row.getRowNum();
            if (StringUtils.isEmpty(id) && StringUtils.isEmpty(r) && StringUtils.isEmpty(nome)) {
                LOGGER.info("SCARTO RIGA VUOTA " + rigaXlx);
                continue;
            }

            LOGGER.info("id " + id + " r " + r + " rm " + rm + " nome " + nome + " squadra " + squadra + " qta " + qta
                    + " qti " + qti);

        }
        // Closing the workbook
        workbook.close();

    }

}