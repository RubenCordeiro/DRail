package pt.up.fe.cmov.inspectorapp;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;

public final class ApiService {

    public static final String API_URL = "http://192.168.56.1:3000";

    public static class Station {
        public final int id;
        public final String name;
        public final boolean isCentral;

        public Station(int id, String name, boolean isCentral) {
            this.id = id;
            this.name = name;
            this.isCentral = isCentral;
        }
    }

    public interface DRail {
        @GET("/api/stations")
        Call<List<Station>> listStations(@Header("Bearer") String bearer);
    }

}
