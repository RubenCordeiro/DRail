package pt.up.fe.cmov.drail;

import java.io.Serializable;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

public final class ApiService {

    public static final String API_URL = "http://192.168.56.1:3000";

    public static final ApiService.DRail service = new Retrofit.Builder()
            .baseUrl(ApiService.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService.DRail.class);

    public interface DRail {

    }
}
