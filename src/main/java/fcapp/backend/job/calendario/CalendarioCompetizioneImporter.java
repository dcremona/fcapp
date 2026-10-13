package fcapp.backend.job.calendario;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcCalendarioCompetizione;
import fcapp.backend.data.entity.FcGiornataInfo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.CalendarioCompetizioneService;
import fcapp.backend.service.GiornataInfoService;
import fcapp.backend.service.SquadraService;

/**
 * Responsabilità unica: import e aggiornamento calendario reale (Serie A / Mondiale).
 */
@Service
public class CalendarioCompetizioneImporter {

    private static final Logger log = LoggerFactory.getLogger(CalendarioCompetizioneImporter.class);

    @Autowired private CalendarioCompetizioneService calendarioService;
    @Autowired private GiornataInfoService giornataInfoService;
    @Autowired private SquadraService squadraService;

    public void initDbCalendarioCompetizione(String fileName) throws Exception {
        log.info("START initDbCalendarioCompetizione");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            calendarioService.deleteAll();
            List<CSVRecord> records = parser.getRecords();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (int i = 1; i < records.size(); i++) {
                CSVRecord r = records.get(i);
                FcCalendarioCompetizione c = new FcCalendarioCompetizione();
                c.setIdGiornata(Integer.parseInt(r.get(0)));
                c.setData(LocalDateTime.parse(r.get(1), formatter));
                c.setSquadraCasa(r.get(2));
                c.setSquadraFuori(r.get(3));
                c.setIdSquadraCasa(Integer.parseInt(r.get(4)));
                c.setIdSquadraFuori(Integer.parseInt(r.get(5)));
                c.setRisultato(r.get(6));
                calendarioService.save(c);
            }
            log.info("END initDbCalendarioCompetizione");
        }
    }

    public void updateCalendarioTim(String fileName, int idGiornata) throws Exception {
        log.info("START updateCalendarioTim");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            List<FcCalendarioCompetizione> list = calendarioService.findByIdGiornata(idGiornata);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (CSVRecord r : parser) {
                String dataOra = normalizeDataOra(r.get(0));
                String squadraCasa = r.get(1).toUpperCase();
                String ris = r.get(2);

                for (FcCalendarioCompetizione c : list) {
                    if (c.getSquadraCasa().substring(0, 3).toUpperCase().equals(squadraCasa.substring(0, 3))) {
                        c.setData(LocalDateTime.parse(dataOra, formatter));
                        c.setRisultato(ris);
                        calendarioService.save(c);
                    }
                }
            }
            log.info("END updateCalendarioTim");
        }
    }

    public void updateCalendarioMondiale(String fileName, int idGiornata) throws Exception {
        log.info("START updateCalendarioMondiale");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            List<FcCalendarioCompetizione> list = calendarioService.findByIdGiornata(idGiornata);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");

            for (CSVRecord r : parser) {
                String dataOra = normalizeDataOra(r.get(0));
                String squadraCasa = r.get(2).toUpperCase();
                String ris = r.get(5);

                for (FcCalendarioCompetizione c : list) {
                    if (c.getSquadraCasa().substring(0, 3).toUpperCase().equals(squadraCasa.substring(0, 3))) {
                        String data = dataOra.substring(0, 6) + "20" + dataOra.substring(6, 8);
                        String ora = dataOra.substring(dataOra.length() - 5);
                        c.setData(LocalDateTime.parse(data + " " + ora, formatter));
                        c.setRisultato(ris);
                        calendarioService.save(c);
                    }
                }
            }
            log.info("END updateCalendarioMondiale");
        }
    }

    public void insertCalendarioTim(String fileName, int idGiornata) throws Exception {
        log.info("START insertCalendarioTim");
        CSVFormat format = CSVFormat.Builder.create().setDelimiter(';').build();

        try (CSVParser parser = new CSVParser(new FileReader(fileName), format)) {
            boolean bUpdate = false;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm");

            for (CSVRecord r : parser) {
                String dataOra = normalizeDataOra(r.get(0));
                String squadraCasa = r.get(1).toUpperCase();
                String squadraFuori = r.get(3).toUpperCase();

                String data = dataOra.substring(0, 6) + "20" + dataOra.substring(6, 8);
                String ora = dataOra.substring(dataOra.length() - 5);
                LocalDateTime dateTime = LocalDateTime.parse(data + " " + ora, formatter);

                FcCalendarioCompetizione c = new FcCalendarioCompetizione();
                c.setIdGiornata(idGiornata);
                c.setData(dateTime);

                FcSquadra sCasa = squadraService.findByNomeSquadra(squadraCasa);
                c.setIdSquadraCasa(sCasa.getIdSquadra());
                c.setSquadraCasa(squadraCasa);

                FcSquadra sFuori = squadraService.findByNomeSquadra(squadraFuori);
                c.setIdSquadraFuori(sFuori.getIdSquadra());
                c.setSquadraFuori(squadraFuori);

                calendarioService.save(c);

                if (!bUpdate) {
                    FcGiornataInfo info = giornataInfoService.findByCodiceGiornata(idGiornata);
                    info.setDataGiornata(dateTime);
                    giornataInfoService.save(info);
                    bUpdate = true;
                }
            }
            log.info("END insertCalendarioTim");
        }
    }

    public void deleteAll() {
        calendarioService.deleteAll();
    }

    private String normalizeDataOra(String dataOra) {
        if (dataOra.length() == 18 || dataOra.length() == 17) {
            if (dataOra.charAt(2) == '/' && dataOra.charAt(5) == '/') {
                int idxOra = dataOra.indexOf(":");
                if (idxOra != -1) {
                    String hhmm = dataOra.substring(idxOra + 1).trim();
                    if (hhmm.length() == 4) {
                        dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
                    }
                }
            } else {
                int idx = dataOra.indexOf("/");
                if (idx == 1) {
                    dataOra = "0" + dataOra;
                    int idxOra = dataOra.indexOf(":");
                    if (idxOra != -1) {
                        String hhmm = dataOra.substring(idxOra + 1).trim();
                        if (hhmm.length() == 4) {
                            dataOra = dataOra.substring(0, idxOra + 1) + " 0" + hhmm;
                        }
                    }
                }
            }
        }
        return dataOra;
    }
}
