package fcweb.backend.data;

import java.io.Serializable;

//Here is a bean
public class FormazioneJasper implements Serializable {

    private static final long serialVersionUID = 1L;

    String ruolo;
    String giocatore;
    String squadra;
    int quotazione = 0;
    int tot_pagato = 0;

    public FormazioneJasper() {
        super();
    }

    public FormazioneJasper(String ruolo, String giocatore, String squadra, int quotazione, int tot_pagato) {
        super();
        this.ruolo = ruolo;
        this.giocatore = giocatore;
        this.squadra = squadra;
        this.quotazione = quotazione;
        this.tot_pagato = tot_pagato;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public String getGiocatore() {
        return giocatore;
    }

    public void setGiocatore(String giocatore) {
        this.giocatore = giocatore;
    }

    public String getSquadra() {
        return squadra;
    }

    public void setSquadra(String squadra) {
        this.squadra = squadra;
    }

    public int getQuotazione() {
        return quotazione;
    }

    public void setQuotazione(int quotazione) {
        this.quotazione = quotazione;
    }

    public int getTot_pagato() {
        return tot_pagato;
    }

    public void setTot_pagato(int tot_pagato) {
        this.tot_pagato = tot_pagato;
    }

}