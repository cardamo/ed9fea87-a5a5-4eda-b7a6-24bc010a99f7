package com.booking.recruitment.hotel.service.impl;

import com.booking.recruitment.hotel.exception.BadRequestException;
import com.booking.recruitment.hotel.model.City;
import com.booking.recruitment.hotel.repository.CityRepository;
import com.booking.recruitment.hotel.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class DefaultCityService implements CityService {
  private final CityRepository cityRepository;

  @Autowired
  DefaultCityService(CityRepository cityRepository) {
    this.cityRepository = cityRepository;
  }

  @Override
  public Optional<City> getCityById(Long id) {
    return cityRepository.findById(id);
  }

  @Override
  public List<City> getAllCities() {
    return cityRepository.findAll();
  }

  @Override
  public City createCity(City city) {
    if (city.getId() != null) {
      throw new BadRequestException("The ID must not be provided when creating a new City");
    }

    return cityRepository.save(city);
  }
}
