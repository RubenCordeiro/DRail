package pt.up.fe.cmov.inspectorapp;

import java.io.Serializable;
import java.util.ArrayList;
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

    public static class Train {
        public final int id;
        public final String date;

        public Train(int id, String date) {
            this.id = id;
            this.date = date;
        }
    }

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

    public static class Trip implements Serializable {
        public final int id;
        public final String departureDate;
        public final String arrivalDate;
        public final int trainId;

        public Trip(int id, String departureDate, String arrivalDate, int trainId) {
            this.id = id;
            this.departureDate = departureDate;
            this.arrivalDate = arrivalDate;
            this.trainId = trainId;
        }
    }

    public static class Ticket implements Serializable {
        public final int id;
        public final String creationDate;
        public final String status;
        public final ArrayList<Integer> trips;
        public final String signature;

        public Ticket(int id, String creationDate, String status, ArrayList<Integer> trips, String signature) {
            this.id = id;
            this.creationDate = creationDate;
            this.status = status;
            this.trips = trips;
            this.signature = signature;
        }
    }

    public interface DRail {
        @GET("/api/stations")
        Call<List<Station>> listStations(@Header("Bearer") String bearer);

        @GET("/api/trips")
        Call<List<List<Trip>>> listTrips(@Header("Bearer") String bearer,
                                         @Query("from") int departureStationId,
                                         @Query("to") int arrivalStationId);

        @GET("/api/tickets")
        Call<List<Ticket>> listTickets(@Header("Bearer") String bearer,
                                       @Query("trips") ArrayList<Integer> trips);
    }
}
