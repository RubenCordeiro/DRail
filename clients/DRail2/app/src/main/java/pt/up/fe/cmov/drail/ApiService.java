package pt.up.fe.cmov.drail;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public final class ApiService {

    public static final String API_URL = "http://172.30.44.223:3000";

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
        public final String nextStationName;
        public final Double distance;

        public HydratedTrip(int id, String departureDate, String arrivalDate, int trainId, String prevStationName, String nextStationName, Double distance) {
            this.id = id;
            this.departureDate = departureDate;
            this.arrivalDate = arrivalDate;
            this.trainId = trainId;
            this.prevStationName = prevStationName;
            this.nextStationName = nextStationName;
            this.distance = distance;
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

    public static class CreditCard implements Serializable {
        public final String number;
        public final String expireDate;

        public CreditCard(String number) {
            this.number = number;
            this.expireDate = "2015-11-11T14:23:16.686Z";
        }
    }

    public static class RegisterUserRequest implements Serializable {
        public final String name;
        public final String username;
        public final String email;
        public final String password;
        public final String role;
        public final ArrayList<CreditCard> creditCards;

        public RegisterUserRequest(String name, String username, String email, String password, String cc) {
            this.name = name;
            this.username = username;
            this.password = password;
            this.email = email;
            this.role = "passenger";
            this.creditCards = new ArrayList<>();
            creditCards.add(new CreditCard(cc));
        }
    }

    public static class LoginUserRequest implements Serializable {
        public final String email;
        public final String password;

        public LoginUserRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class LoginUserResponse implements Serializable {
        public final String username;
        public final int id;
        public final String token;
        public final String role;
        public final String email;

        public LoginUserResponse(String email, String username, int id, String token, String role) {
            this.username = username;
            this.id = id;
            this.token = token;
            this.role = role;
            this.email = email;
        }
    }

    public static class TripValidation {
        public final int id;
        public final int trainId;

        public TripValidation(int id, int trainId) {
            this.id = id;
            this.trainId = trainId;
        }
    }

    public static class TripsValidation {
        public final ArrayList<TripValidation> trips;

        public TripsValidation(ArrayList<TripValidation> trips) {
            this.trips = trips;
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

        @POST("api/register")
        Call<LoginUserResponse> register(@Body RegisterUserRequest user);

        @POST("api/login")
        Call<LoginUserResponse> login(@Body LoginUserRequest user);

        @POST("/api/users/{id}/tickets")
        Call<String> buyTickets(@Header("Bearer") String bearer, @Path("id") int userId,
                                     @Body TripsValidation validation);
    }
}
