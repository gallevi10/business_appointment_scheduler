package com.javaworkshop.business_scheduler.util;

import com.javaworkshop.business_scheduler.service.BusinessInfoService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

// This class is a utility for sending emails using JavaMailSender.
@Component
public class EmailUtil {

    @Value("${spring.mail.username}")
    private String EMAIL_FROM;

    private BusinessInfoService businessInfoService;
    private JavaMailSender javaMailSender;

    @Autowired
    public EmailUtil(JavaMailSender javaMailSender, BusinessInfoService businessInfoService) {
        this.javaMailSender = javaMailSender;
        this.businessInfoService = businessInfoService;
    }

    public void sendMail(String toEmail, String subject, String body) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, false);
        helper.setFrom(new InternetAddress(EMAIL_FROM, businessInfoService.getBusinessInfo().getName()));
        javaMailSender.send(message);
    }

}
