package com.exchange.profile.controller.setprofile;

import com.exchange.profile.domain.GenderType;

import java.time.LocalDate;

public record SetUserProfileRequest (String firstName, String lastName, String phoneNumber, GenderType genderType, LocalDate birthday, String address) {
}
