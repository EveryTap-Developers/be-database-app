package de.everytap.broteinheiten_datenbank.database;

import java.util.List;

import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 06.01.2015.
 */

public class UmlauteChanger {

    public static final Umlaut[] umlautArray = new Umlaut[] {new Umlaut("ä", "&auml;"), new Umlaut("Ä", "&Auml;"), new Umlaut("ö", "&ouml;"), new Umlaut("Ö", "&Ouml;"), new Umlaut("ü", "&uuml;"), new Umlaut("Ü", "&Uuml;"), new Umlaut("ß", "&szlig;")};
    public static List<Food> change(List<Food> foodList) {

        for (Food food : foodList) {
            food.changeUmlaute();
        }

        return foodList;
    }


}
