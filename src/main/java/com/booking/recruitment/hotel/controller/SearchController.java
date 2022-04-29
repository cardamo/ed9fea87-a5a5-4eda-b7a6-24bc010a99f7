package com.booking.recruitment.hotel.controller;

import com.booking.recruitment.hotel.model.City;
import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.service.CityService;
import com.booking.recruitment.hotel.service.HotelService;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class SearchController {
  private final CityService cityService;
  private final HotelService hotelService;

  public SearchController(CityService cityService, HotelService hotelService) {
    this.cityService = cityService;
    this.hotelService = hotelService;
  }

  @GetMapping("/{cityId}")
  public ResponseEntity<List<HotelWithDistance>> getAllCities(@RequestParam(defaultValue = "distance") SortBy sortBy, @PathVariable Long cityId) {
    Optional<City> city = cityService.getCityById(cityId);
    if (!city.isPresent()) {
      return ResponseEntity.notFound().build(); // 404 is more logical for PathVariable than empty result
    }

    GeoPoint cityLocation = new GeoPoint(city.get().getCityCentreLatitude(), city.get().getCityCentreLongitude());

    return ResponseEntity.ok(hotelService.getHotelsByCity(cityId)
        .stream()
        .map(hotel -> new HotelWithDistance(
            hotel,
            distanceKm(cityLocation, new GeoPoint(hotel.getLatitude(), hotel.getLongitude()))
        ))
        .sorted(Comparator.comparing(hotel -> hotel.distanceKm))
        .limit(3)
        .collect(Collectors.toList()));
  }

  private double distanceKm(GeoPoint p1, GeoPoint p2) { // conscientiously googled
    double dLat = Math.toRadians(p2.latitude - p1.latitude);
    double dLon = Math.toRadians(p2.longitude - p1.longitude);

    double lat1 = Math.toRadians(p1.latitude);
    double lat2 = Math.toRadians(p2.latitude);

    double a = Math.pow(Math.sin(dLat / 2), 2) +
        Math.pow(Math.sin(dLon / 2), 2) *
            Math.cos(lat1) *
            Math.cos(lat2);
    double rad = 6371;
    double c = 2 * Math.asin(Math.sqrt(a));
    return rad * c;
  }

  public enum SortBy {
    distance // Define as enum just to validate the only possible value for query param.
  }

  static class GeoPoint {
    final double latitude;
    final double longitude;

    public GeoPoint(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }
  }

  // It's not required though useful to have distance values in the result
  public static class HotelWithDistance {
    @JsonUnwrapped
    private final Hotel hotel;
    private final double distanceKm;

    public HotelWithDistance(Hotel hotel, double distanceKm) {
      this.hotel = hotel;
      this.distanceKm = distanceKm;
    }

    public Hotel getHotel() {
      return hotel;
    }

    public double getDistanceKm() {
      return distanceKm;
    }
  }
}
