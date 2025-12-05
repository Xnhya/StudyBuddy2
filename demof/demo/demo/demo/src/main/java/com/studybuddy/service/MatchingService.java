package com.studybuddy.service;

import com.studybuddy.model.User;
import com.studybuddy.repository.UserRepository;
import com.studybuddy.repository.StudyGroupRepository; // Aseguramos este import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingService {
    
    @Autowired
    private UserRepository userRepository;
    
    // ENCONTRAR COMPAÑEROS COMPATIBLES
    public List<User> findMatches(User currentUser) {
        // CORRECCIÓN CRÍTICA: Cambiado de findByIsActiveTrue() a findByEnabledTrue()
        // para coincidir con la entidad User y el repositorio actualizado.
        List<User> allUsers = userRepository.findByEnabledTrue();
        
        List<User> matches = new ArrayList<>();
        
        for (User user : allUsers) {
            if (user.getId().equals(currentUser.getId())) {
                continue; // Saltar el usuario actual
            }
            
            int score = calculateMatchScore(currentUser, user);
            
            if (score >= 2) { // Mínimo 2 puntos de compatibilidad
                matches.add(user);
            }
            
            // Limitar a 10 matches
            if (matches.size() >= 10) {
                break;
            }
        }
        
        return matches;
    }
    
    // CALCULAR PUNTUACIÓN DE MATCH
    private int calculateMatchScore(User user1, User user2) {
        int score = 0;
        
        // Misma carrera = 2 puntos
        if (user1.getCareer() != null && user1.getCareer().equals(user2.getCareer())) {
            score += 2;
        }
        
        // Misma universidad = 1 punto
        if (user1.getUniversity() != null && user1.getUniversity().equals(user2.getUniversity())) {
            score += 1;
        }
        
        // Mismo semestre = 1 punto
        if (user1.getSemester() != null && user1.getSemester().equals(user2.getSemester())) {
            score += 1;
        }
        
        // Intereses en común = 1 punto por interés
        if (user1.getInterests() != null && user2.getInterests() != null) {
            long commonInterests = user1.getInterests().stream()
                    .filter(user2.getInterests()::contains)
                    .count();
            score += commonInterests;
        }
        
        return score;
    }
    
    // BUSCAR POR CARRERA Y SEMESTRE
    public List<User> findByCareerAndSemester(String career, Integer semester) {
        return userRepository.findByCareerAndSemester(career, semester);
    }
    
    // BUSCAR POR INTERESES
    public List<User> findByInterest(String interestName) {
        return userRepository.findByInterestName(interestName);
    }
    
    // OBTENER RECOMENDACIONES DE GRUPOS
    public List<com.studybuddy.model.StudyGroup> recommendGroups(User user, 
                                                                 StudyGroupRepository groupRepo) {
        List<com.studybuddy.model.StudyGroup> allGroups = groupRepo.findByIsPublicTrue();
        List<com.studybuddy.model.StudyGroup> recommendations = new ArrayList<>();
        
        for (com.studybuddy.model.StudyGroup group : allGroups) {
            if (group.isMember(user) || group.isFull()) {
                continue;
            }
            
            // Priorizar grupos de la misma carrera
            if (user.getCareer() != null && group.getSubject() != null &&
                group.getSubject().toLowerCase().contains(user.getCareer().toLowerCase())) {
                recommendations.add(0, group); // Agregar al inicio
            } else {
                recommendations.add(group);
            }
            
            if (recommendations.size() >= 5) {
                break;
            }
        }
        
        return recommendations;
    }
}