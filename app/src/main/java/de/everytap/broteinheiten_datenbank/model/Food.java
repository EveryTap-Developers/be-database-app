package de.everytap.broteinheiten_datenbank.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import de.everytap.broteinheiten_datenbank.database.Umlaut;
import de.everytap.broteinheiten_datenbank.database.UmlauteChanger;

/**
 * Created by randombyte on 18.12.2014.
 */
public class Food implements Parcelable{

    private int id = -1;
    private String name;
    private float be; //Broteinheiten
    private boolean userCreated;
    private ArrayList<Integer> categories = new ArrayList<Integer>();

    public Food(int id) { //Nur zum List durchsuchen
        this.id = id;
    }

    public Food(String name, float be, boolean userCreated) {
        this.name = name;
        this.be = be;
        this.userCreated = userCreated;
    }

    public Food(int id, String name, float be, boolean userCreated) {
        this.id = id;
        this.name = name;
        this.be = be;
        this.userCreated = userCreated;
    }

    public Food(Parcel parcel) {
        id = parcel.readInt();
        name = parcel.readString();
        be = parcel.readFloat();
        userCreated = parcel.readInt() != 0;
        parcel.readList(categories, Integer.class.getClassLoader());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBe() {
        return be;
    }

    public void setBe(float be) {
        this.be = be;
    }

    public float getKh() { //Kohlenhydrate
        return be*12;
    }

    public void setKh(float kh) {
        this.be = kh/12;
    }

    public boolean isUserCreated() {
        return userCreated;
    }

    public void setUserCreated(boolean userCreated) {
        this.userCreated = userCreated;
    }

    public boolean addCategory(int category) {
        return categories.add(category);
    }

    public boolean removeCategory(int category) {
        return categories.contains(category) && categories.remove(Integer.valueOf(category)); //damit kein index
    }

    public ArrayList<Integer> getCategories() {
        return categories;
    }

    public void changeUmlaute() {
        for (Umlaut umlaut : UmlauteChanger.umlautArray) {
            name = name.replace(umlaut.getUmlautHtml(), umlaut.getUmlaut());
        }
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Food && ((Food) o).getId() == id; //Like a boss!
    }

    @Override
    public String toString() {
        return "Food[" + id +"]: Name: " + name + ";  Be: " + be + "; UserCreated: " + userCreated + ";";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeFloat(be);
        dest.writeInt(userCreated ? 1 : 0);
        dest.writeList(categories);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel source) {
            return new Food(source);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    public static ArrayList<Food> filterOnlyUserCreated(ArrayList<Food> foodList, boolean onlyUserCreated) {

        if (!onlyUserCreated) {
            return foodList;
        }

        ArrayList<Food> filtered = new ArrayList<Food>();

        for (Food food : foodList) {
            if (food.isUserCreated()) {
                filtered.add(food);
            }
        }

        return filtered;
    }
}
