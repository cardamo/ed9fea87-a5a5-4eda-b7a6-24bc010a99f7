package com.booking.recruitment.hotel.controller;

import com.booking.recruitment.hotel.model.City;
import com.booking.recruitment.hotel.model.Hotel;
import com.booking.recruitment.hotel.repository.CityRepository;
import com.booking.recruitment.hotel.repository.HotelRepository;
import com.booking.testing.SlowTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:data.sql")
@SlowTest
class HotelControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Autowired private HotelRepository repository;
  @Autowired private CityRepository cityRepository;

  @Test
  @DisplayName("When all hotels are requested then they are all returned")
  void allHotelsRequested() throws Exception {
    mockMvc
        .perform(get("/hotel"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(jsonPath("$", hasSize((int) repository.count())));
  }

  @Test
  @DisplayName("When a hotel creation is requested then it is persisted")
  void hotelCreatedCorrectly() throws Exception {
    City city =
        cityRepository
            .findById(1L)
            .orElseThrow(
                () -> new IllegalStateException("Test dataset does not contain a city with ID 1!"));
    Hotel newHotel = Hotel.builder().setName("This is a test hotel").setCity(city).build();

    Long newHotelId =
        mapper
            .readValue(
                mockMvc
                    .perform(
                        post("/hotel")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(newHotel)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(),
                Hotel.class)
            .getId();

    newHotel.setId(newHotelId); // Populate the ID of the hotel after successful creation

    Hotel getNewHotelResult = mapper
        .readValue(
            mockMvc
                .perform(
                    get("/hotel/" + newHotelId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Hotel.class);

    assertThat(getNewHotelResult.getName(), equalTo(newHotel.getName()));
    assertThat(getNewHotelResult.getCity(), equalTo(city));
  }

  @Test
  void hotelNotFoundAfterDeletion() throws Exception {
    Hotel newHotel = createNewHotel();

    mockMvc
        .perform(
            delete("/hotel/" + newHotel.getId()))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/hotel/" + newHotel.getId()))
        .andExpect(status().isNotFound());
  }

  private Hotel createNewHotel() throws Exception {
    Hotel newHotel = Hotel.builder().setName("This is a test hotel").build();
    return mapper
        .readValue(
            mockMvc
                .perform(
                    post("/hotel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newHotel)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Hotel.class);
  }

  @Test
  @DisplayName("When a hotel creation is requested then specifying the ID is not allowed")
  void hotelNotCreatedIfIdSpecified() throws Exception {
    long newHotelId = 891995;
    mockMvc
        .perform(
            get("/hotel/" + newHotelId))
        .andExpect(status().isNotFound()); // check that id is not taken before creation

    Hotel newHotel = Hotel.builder().setName("This is a test hotel").setId(newHotelId).build();

    mockMvc
        .perform(
            post("/hotel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newHotel)))
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse()
        .getContentAsString(); // XXX: would be nice to have a clear message in response

    mockMvc
        .perform(
            get("/hotel/" + newHotelId))
        .andExpect(status().isNotFound());
  }
}
