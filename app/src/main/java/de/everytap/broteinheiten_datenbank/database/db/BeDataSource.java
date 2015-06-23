package de.everytap.broteinheiten_datenbank.database.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.everytap.broteinheiten_datenbank.model.Food;

/**
 * Created by randombyte on 20.12.2014.
 */
public class BeDataSource {

    private SQLiteDatabase database;
    private BeDatabase beDatabase;
    private String[] allColumns = {BeDatabase.COLUMN_ID, BeDatabase.COLUMN_FOOD, BeDatabase.COLUMN_BE, BeDatabase.COLUMN_USER_CREATED};

    public BeDataSource(Context context) {
        beDatabase = new BeDatabase(context);
    }

    public void open() throws SQLException {
        database = beDatabase.getWritableDatabase();
    }

    public void close() {
        beDatabase.close();
    }

    public long addData(Food food) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BeDatabase.COLUMN_FOOD, food.getName());
        contentValues.put(BeDatabase.COLUMN_BE, food.getBe());
        contentValues.put(BeDatabase.COLUMN_USER_CREATED, food.isUserCreated() ? 1 : 0);

        return database.insert(BeDatabase.TABLE_BE, null, contentValues);
    }

    public void addData(List<Food> foodList) {

        database.beginTransaction();
        try {
            for (Food food : foodList) {
                addData(food);
            }
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
            //todo: fehlerbehandlung
        } finally {
            database.endTransaction();
        }
    }

    public void updateDataById(Food food) {
        ContentValues values = new ContentValues();
        values.put(BeDatabase.COLUMN_FOOD, food.getName());
        values.put(BeDatabase.COLUMN_BE, food.getBe());
        values.put(BeDatabase.COLUMN_USER_CREATED, food.isUserCreated());

        database.update(BeDatabase.TABLE_BE, values, BeDatabase.COLUMN_ID + " = " + food.getId(), null); //Anhand der Id updaten
    }

    public void deleteDataByID(int id) {
        database.delete(BeDatabase.TABLE_BE, BeDatabase.COLUMN_ID + " = " + id, null);
    }

    public Food getById(int id) {
        return rawQuerySafeOneItem("SELECT * FROM " + BeDatabase.TABLE_BE + " WHERE " + BeDatabase.COLUMN_ID + " = " + id + ";");
    }

    public @Nullable ArrayList<Food> getData(String query, boolean onlyUserCreatedItems) {

        String orderedBy = BeDatabase.COLUMN_FOOD + " COLLATE NOCASE ASC";
        String whereClause = "";

        if (query != null && !query.isEmpty()) {
            //Nicht leer
            whereClause = BeDatabase.COLUMN_FOOD + " LIKE \'%" + appendWildcard(query) + "%\'";
        }

        if (onlyUserCreatedItems) {

            //Wenn schon was in whereClause drin, nächstes Argument mit AND anhängen
            if (!whereClause.isEmpty()) {
                whereClause += " AND ";
            }

             whereClause += BeDatabase.COLUMN_USER_CREATED + " = 1"; //true => 1, weil kein boolean in SQLite
        }

        Cursor cursor = database.query(
                BeDatabase.TABLE_BE,
                allColumns,
                whereClause,
                null,
                null,
                null,
                orderedBy
        );

        ArrayList<Food> foodList = cursorToFood(cursor);
        cursor.close();
        return foodList;
    }

    public void deleteEverything() {
        database.execSQL("DELETE FROM " + BeDatabase.TABLE_BE);
    }

    private Food rawQuerySafeOneItem(String query) { //Nur das erste(und einzige) Item der query holen
        Cursor cursor = database.rawQuery(query, null);

        if (cursor == null) return null;

        cursor.moveToFirst();
        Food food = oneEntryOfcursorToFood(cursor);
        cursor.close();

        return food;
    }

    private Food oneEntryOfcursorToFood(Cursor cursor) {
        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        String be = cursor.getString(2);
        boolean userCreated = cursor.getInt(3) == 1;
        //return new Food(cursor.getString(0), cursor.getString(1));
        return new Food(id, name, be, userCreated);
    }

    private ArrayList<Food> cursorToFood(Cursor cursor) {

        if (moveCursorSafe(cursor)) return null;

        ArrayList<Food> foodList = new ArrayList<Food>();

        while (!cursor.isAfterLast()) { //Eintrag da?
            foodList.add(oneEntryOfcursorToFood(cursor));
            cursor.moveToNext();
        }

        return foodList;
    }

    private String appendWildcard(String query) {
        if (query.isEmpty()) return query;

        final StringBuilder builder = new StringBuilder();
        final String[] splits = query.split(" ");

        for (String split : splits)
            builder.append(split).append("%").append(" ");

        return builder.toString().trim();
    }

    public boolean isDbOpen() {
        return database != null && database.isOpen();
    }

    /**
     *
     * @return true if cursor is null
     */
    private static boolean moveCursorSafe(Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            return false;
        } else {
            Log.d("Database", "Cursor is null");
            return true;
        }
    }
}
