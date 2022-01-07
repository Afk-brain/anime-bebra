package org.mo.bots.data.objects;

import com.google.gson.annotations.SerializedName;

public class Product {

    int cost;
    @SerializedName("menu_category_id")
    int categoryId;
    String photo;
    @SerializedName("product_id")
    int id;

}
