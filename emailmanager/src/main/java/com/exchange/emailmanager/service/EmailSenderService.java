package com.exchange.emailmanager.service;


import com.exchange.emailmanager.domain.UserStatus;

public interface EmailSenderService {
    void mailSender(String emailTo, String verificationCode, String expiredDate);
    void changeEmailSender(String emailTo, String verificationCode, String expiredDate);

   void forgotPassword(String emailTo, String verificationCode, String expiredDate);
   void sendMessage(String emailTo, String  message, String subject);

    void setUserStatus(String emailTo, UserStatus userStatus, String subject);

    void changeEmailVerifyCode(String emailTo, String message);

    void bookingAppointment(String firstName, String lastName, String email,
                            String appointmentTime, String userInsuranceType,
                            String appointmentDescription, String insuranceNumber,
                            String appointmentStatus, String serviceProviderTitle);
    void serviceProviderBookingStatus(String email, String appointmentTime, String title, String patientNumber,
                                      String insuranceType, String description, String appointmentStatus);

    void profileMessageDlt(String firstName, String lastName, String email);
}