package com.studybuddy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username:noreply@studybuddy.com}")
    private String fromEmail;
    
    /**
     * Envía un correo simple con el código OTP para 2FA
     * @param toEmail Correo del destinatario
     * @param otpCode Código OTP de 6 dígitos
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("StudyBuddy - Código de Verificación 2FA");
            message.setText(buildOtpEmailContent(otpCode));
            
            javaMailSender.send(message);
            logger.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Error sending OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    
    /**
     * Construye el contenido del correo con el código OTP
     */
    private String buildOtpEmailContent(String otpCode) {
        return "¡Hola!\n\n" +
               "Tu código de verificación (OTP) para acceder a StudyBuddy es:\n\n" +
               "═══════════════════════\n" +
               "     " + otpCode + "\n" +
               "═══════════════════════\n\n" +
               "Este código expira en 10 minutos.\n\n" +
               "Si no solicitaste este código, ignora este mensaje.\n\n" +
               "Saludos,\n" +
               "Equipo StudyBuddy";
    }
}
