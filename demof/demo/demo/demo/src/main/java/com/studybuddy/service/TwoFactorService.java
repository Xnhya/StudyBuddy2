package com.studybuddy.service;

import com.studybuddy.model.User;
import com.studybuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class TwoFactorService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwoFactorService.class);
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_LENGTH = 6;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Genera un OTP de 6 dígitos, lo guarda en el usuario y envía por correo
     * @param user Usuario al que se le enviará el OTP
     * @return OTP generado
     */
    public String generateAndSendOtp(User user) {
        try {
            String otp = generateOtp();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
            
            user.setOtpCode(otp);
            user.setOtpExpiry(expiryTime);
            user.setTwoFactorEnabled(true);
            
            userRepository.save(user);
            
            logger.info("OTP code set for user: {}, expiry: {}", user.getUsername(), expiryTime);
            
            // Intentar enviar correo, pero no fallar si hay problema de email
            try {
                emailService.sendOtpEmail(user.getEmail(), otp);
                logger.info("OTP email sent successfully to: {}", user.getEmail());
            } catch (Exception emailEx) {
                logger.warn("Failed to send OTP email to {}, but OTP is saved in DB: {}", 
                           user.getEmail(), emailEx.getMessage());
                // No lanzar excepción - el OTP está guardado, el usuario puede verificar desde logs
                // En desarrollo, mostrar el OTP en logs; en producción, solo guardar
                logger.info("DEBUG - OTP code for user {} is: {}", user.getUsername(), otp);
            }
            
            return otp;
        } catch (Exception ex) {
            logger.error("Error in generateAndSendOtp for user {}: {}", user.getUsername(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate OTP", ex);
        }
    }
    
    /**
     * Valida si el OTP ingresado es correcto y no ha expirado
     * @param user Usuario que intenta validar
     * @param inputOtp OTP ingresado por el usuario
     * @return true si es válido, false si no
     */
    public boolean validateOtp(User user, String inputOtp) {
        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            logger.warn("No OTP found for user: {}", user.getUsername());
            return false;
        }
        
        // Verificar si el OTP ha expirado
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            logger.warn("OTP expired for user: {}", user.getUsername());
            clearOtp(user);
            return false;
        }
        
        // Verificar si el OTP es correcto
        if (!user.getOtpCode().equals(inputOtp.trim())) {
            logger.warn("Incorrect OTP for user: {}", user.getUsername());
            return false;
        }
        
        logger.info("OTP validated successfully for user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Limpia el OTP después de una validación exitosa
     * @param user Usuario cuyo OTP debe ser limpiado
     */
    public void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
        logger.info("OTP cleared for user: {}", user.getUsername());
    }
    
    /**
     * Genera un OTP aleatorio de 6 dígitos
     * @return Código OTP
     */
    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
