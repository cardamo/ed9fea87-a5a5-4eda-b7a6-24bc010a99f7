package com.booking.recruitment.hotel.service;

import com.booking.recruitment.hotel.model.City;

import java.util.List;
import java.util.Optional;

public interface CityService {
  List<City> getAllCities();

  Optional<City> getCityById(Long id);

  City createCity(City city);
}
