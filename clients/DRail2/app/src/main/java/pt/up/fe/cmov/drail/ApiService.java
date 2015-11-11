package pt.up.fe.cmov.drail;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

public final class ApiService {

    public static final String API_URL = "http://192.168.1.171:3000";

    public static final ApiService.DRail service = new Retrofit.Builder()
            .baseUrl(ApiService.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService.DRail.class);

    public static class Station {
        public final int id;
        public final String name;
        public final boolean isCentral;
        public final double longitude;
        public final double latitude;

        public Station(int id, String name, boolean isCentral, double longitude, double latitude) {
            this.id = id;
            this.name = name;
            this.isCentral = isCentral;
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }

    public static class GraphEdge {
        public final int start;
        public final int end;

        public GraphEdge(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public static class Graph {
        public final ArrayList<Station> stations;
        public final ArrayList<GraphEdge> trips;

        public Graph(ArrayList<Station> stations, ArrayList<GraphEdge> trips) {
            this.stations = stations;
            this.trips = trips;
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

    public static class HydratedTrip implements Serializable {
        public final int id;
        public final String departureDate;
        public final String arrivalDate;
        public final int trainId;
        public final String prevStationName;

        public HydratedTrip(int id, String departureDate, String arrivalDate, int trainId, String prevStationName) {
            this.id = id;
            this.departureDate = departureDate;
            this.arrivalDate = arrivalDate;
            this.trainId = trainId;
            this.prevStationName = prevStationName;
        }
    }

    public static class Ticket implements Serializable {
        public final int id;
        public final String creationDate;
        public final String status;
        public final ArrayList<Integer> trips;
        public final String signature;
        public final String startStation;
        public final String endStation;

        public Ticket(int id, String creationDate, String status, ArrayList<Integer> trips, String signature, String startStation, String endStation) {
            this.id = id;
            this.creationDate = creationDate;
            this.status = status;
            this.trips = trips;
            this.signature = signature;
            this.startStation = startStation;
            this.endStation = endStation;
        }

        @Override public String toString() {
            return String.format("%s to %s (%s)", startStation, endStation, creationDate.replace('T', ' ').substring(0, 19));
        }
    }

    public interface DRail {
        @GET("/api/graph")
        Call<Graph> getGraph(@Header("Bearer") String bearer);

        @GET("/api/trips")
        Call<ArrayList<ArrayList<Trip>>> getTrips(@Header("Bearer") String bearer, @Query("from") int from, @Query("to") int to);

        @GET("/api/trips/hydrated")
        Call<ArrayList<ArrayList<HydratedTrip>>> getHydratedTrips(@Header("Bearer") String bearer, @Query("from") int from, @Query("to") int to);

        @GET("api/users/{id}/tickets")
        Call<ArrayList<Ticket>> getTickets(@Header("Bearer") String bearer, @Path("id") int userId);
    }
}
