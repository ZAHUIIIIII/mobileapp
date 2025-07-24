package com.universalyoga.adminapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    public static Retrofit get() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://yoga-19447-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}