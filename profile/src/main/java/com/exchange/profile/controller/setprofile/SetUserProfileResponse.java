package com.exchange.profile.controller.setprofile;

import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.UserStatus;

import java.time.LocalDate;

public record SetUserProfileResponse (String firstName, String lastName, String phoneNumber,
                                      String email, UserStatus status,
                                      GenderType genderType, LocalDate birthday, String address) {
}
