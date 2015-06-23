package de.everytap.broteinheiten_datenbank.database;

/**
 * Created by randombyte on 06.01.2015.
 */

public class Umlaut {

    private String umlaut;
    private String umlautHtml;

    public Umlaut() {
    }

    public Umlaut(String umlaut, String umlautHtml) {
        this.umlaut = umlaut;
        this.umlautHtml = umlautHtml;
    }

    public String getUmlaut() {
        return umlaut;
    }

    public void setUmlaut(String umlaut) {
        this.umlaut = umlaut;
    }

    public String getUmlautHtml() {
        return umlautHtml;
    }

    public void setUmlautHtml(String umlautHtml) {
        this.umlautHtml = umlautHtml;
    }
}
