package com.example.dinindu.sales_data_sender;

/**
 * Created by Dinindu on 7/25/2015.
 */
public class Region {
    public String region;
    public int region_id;

    Region(String r, int id){
        region = r;
        region_id = id;
    }

    @Override
    public String toString(){
        return region;
    }
}
