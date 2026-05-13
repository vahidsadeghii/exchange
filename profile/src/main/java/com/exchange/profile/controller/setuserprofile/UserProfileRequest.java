package com.exchange.profile.controller.setuserprofile;

import java.time.LocalDate;

public record UserProfileRequest(String firstName, String lastName,
                                 String phoneNumber, String email,
                                 String address, String avatarId,
                                 String avatarLink, String fileName,
                                 LocalDate birthday, String genderType) {
}
