package com.universalyoga.adminapp.network;

import java.util.List;
import com.universalyoga.adminapp.models.YogaCourse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("courses") Call<List<YogaCourse>> fetchCourses();
    @POST("courses/sync") Call<Void> syncCourses(@Body List<YogaCourse> courses);
}