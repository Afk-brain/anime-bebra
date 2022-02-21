package org.mo.bots.data.objects;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("product_name")
    public String name;
    public int cost;
    @SerializedName("menu_category_id")
    public int categoryId;
    public String photo;
    @SerializedName("product_id")
    public int id;
    public Spot[] spots;
    public Ingredient[] ingredients;

    public int getPrice() {
        return spots[0].price;
    }

    static public class Ingredient {
        @SerializedName("ingredient_name")
        public String name;
    }

}
