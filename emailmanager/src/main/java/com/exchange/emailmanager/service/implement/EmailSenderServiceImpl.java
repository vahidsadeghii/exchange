package com.exchange.emailmanager.service.implement;


import com.exchange.emailmanager.domain.UserStatus;
import com.exchange.emailmanager.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {
    private final JavaMailSender javaMailSender;
    @Override
    public void mailSender(String emailTo, String verifyCode, String expirationDate) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        String link = "localhost:8070/authentication/open/v1/verify-code";
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject("Verify Email");
            helper.setText("Dear Customer,"+"\n\nPlease click of the following link to finish your registration:\n\n"+
                    link +"\n\n"+
                    "and enter your verification code: "+verifyCode+"\n\n"+
                    "Please check the expired date.If the token is expired, send request to refresh token:" +
                    expirationDate+"\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void changeEmailSender(String emailTo, String verificationCode, String expiredDate) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        String link = "localhost:8070/authentication/open/v1/email";
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject("Verification code to change email");
            helper.setText("Dear Customer,"+"\n\nPlease click of the following link to verification to change your email:\n\n"+
                    link +"\n\n"+
                    "and enter your verification code: "+verificationCode+"\n\n"+
                    "Please check the expired date.If the token is expired, send request to refresh token:" +
                    expiredDate+"\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void forgotPassword(String emailTo, String verificationCode, String expiredDate) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        String link = "localhost:8070/authentication/open/v1/reset-password";
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject("Forgot Password");
            helper.setText("Dear Customer,"+"\n\nPlease click of the following link to verification to rest your password:\n\n"+
                    link +"\n\n"+
                    "and enter your verification code: "+verificationCode+"\n\n"+
                    "Please check the expired date.If the token is expired, send request to refresh token:" +
                    expiredDate+"\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void sendMessage(String emailTo, String message, String subject) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject(subject);
            helper.setText("Dear Customer,"+
                    "\n\n"+ message +"\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void setUserStatus(String emailTo, UserStatus userStatus, String subject) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject(subject);
            helper.setText("Dear Customer,"+
                    "\n\n"+ "Your status in this system is changed to "+userStatus+"\n\n"+
                    "For further questions please do not hesitate to contact our Support Team."+
                    "\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void changeEmailVerifyCode(String emailTo, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(emailTo);
            helper.setSubject("Change Email");
            helper.setText("Dear Customer,"+
                    "\n\n"+ message +"\n\n"+
                    "For further questions please do not hesitate to contact our Support Team."+
                    "\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void bookingAppointment(String firstName, String lastName, String email,
                                   String appointmentTime, String userInsuranceType,
                                   String appointmentDescription, String insuranceNumber,
                                   String appointmentStatus, String serviceProviderTitle) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(email);
            helper.setSubject("Appointment status");
            helper.setText("Dear "+ firstName+" "+ lastName+","+
                    "\n\n"+ "You created an appointment with the following details:" +"\n\n"+
                    "Healthy service: "+ serviceProviderTitle +"\n\n"+
                    "Appointment time: "+appointmentTime +"\n\n"+
                    "Insurance type: "+ userInsuranceType +"\n\n"+
                    "Insurance number: "+insuranceNumber +"\n\n"+
                    "Description: " + appointmentDescription +"\n\n"+
                    "For further questions please do not hesitate to contact our Support Team."+
                    "\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void serviceProviderBookingStatus(String email, String appointmentTime, String title, String patientNumber,
                                             String insuranceType, String description, String appointmentStatus) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(email);
            helper.setSubject("Appointment status");
            helper.setText("Dear "+ title +","+
                    "\n\n"+ "Details of new appointment time:" +"\n\n"+
                    "PatientNumber: "+ patientNumber +"\n\n"+
                    "Appointment time: "+appointmentTime +"\n\n"+
                    "Insurance type: "+ insuranceType +"\n\n"+
                    "Description: "+description +"\n\n"+
                    "Appointment status: " + appointmentStatus +"\n\n"+
                    "For further questions please do not hesitate to contact our Support Team."+
                    "\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

    @Override
    public void profileMessageDlt(String firstName, String lastName, String email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom("healthycareservice@net.at");
            helper.addTo(email);
            helper.setSubject("Appointment status");
            helper.setText("Dear "+ firstName +" "+ firstName +","+
                    "Thank you for registering! Your registration is successful."+
                    "\n\n\n"+
                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.getStackTrace();
        }
    }

//    @Override
//    public void serviceProviderBookingStatut(String email, String appointmentTime, String title, String patientNumber,
//                                             String insuranceType, String description, String appointmentStatus){
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
//        try {
//            helper.setFrom("healthycareservice@net.at");
//            helper.addTo(email);
//            helper.setSubject("Change Email");
//            helper.setText("Dear Service provider,"+
//                    "\n\n"+ "Your new booking appointment is at " + appointmentTime+ "."+"\n\n"+
//                    "For further questions please do not hesitate to contact our Support Team."+
//                    "\n\n\n"+
//                    "Beast regards\n"+"HealthyCare Service Team\n"+ LocalDateTime.now());
//            javaMailSender.send(mimeMessage);
//        } catch (MessagingException e) {
//            e.getStackTrace();
//        }
//    }


}
