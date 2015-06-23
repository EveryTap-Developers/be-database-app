package de.everytap.broteinheiten_datenbank.database;


import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 19.12.2014.
 */
public class JsonParser {

    public static List<Food> parseJson(JsonArray jsonArray) {

        //BLOCKING

        int size = jsonArray.size();
        List<Food> foodList = new ArrayList<Food>(size); //Schon passend Liste erzeugen

        //Durch alle Einträge durch
        for (int i = 0; i < size; i++) {
            String name = jsonArray.get(i).getAsJsonArray().get(0).getAsString();
            String be = jsonArray.get(i).getAsJsonArray().get(1).getAsString();

            foodList.add(new Food(name, be, false));
        }

        return foodList;
    }

}
