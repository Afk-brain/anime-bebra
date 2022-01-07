package org.mo.bots.data.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("category_id")
    public int id;
    @SerializedName("category_name")
    public String name;
    @SerializedName("category_photo")
    public String photo;

}
