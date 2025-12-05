package com.studybuddy.service;

import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.User;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.UserRepository;
import com.studybuddy.controller.StudyGroupController.CreateGroupRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    public StudyGroupServiceImpl(StudyGroupRepository studyGroupRepository, 
                                UserRepository userRepository) {
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<StudyGroup> findAll() {
        return studyGroupRepository.findAll();
    }

    @Override
    public Optional<StudyGroup> findById(Long id) {
        return studyGroupRepository.findById(id);
    }

    @Override
    public StudyGroup createGroup(CreateGroupRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        StudyGroup group = new StudyGroup();
        // Asignación directa y segura
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setSubject(request.getSubject());
        group.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 10);
        group.setIsPublic(request.isPublic());
        group.setIsChatEnabled(request instanceof com.studybuddy.controller.StudyGroupController.CreateGroupRequest ? ((com.studybuddy.controller.StudyGroupController.CreateGroupRequest) request).isChatEnabled() : false);
        
        // Datos automáticos
        group.setCreator(creator);
        group.setCreatedAt(LocalDateTime.now());
        group.setIsActive(true);
        
        // Inicializar miembros y añadir al creador
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>());
        }
        group.addMember(creator);
        
        return studyGroupRepository.save(group);
    }

    @Override
    public StudyGroup updateGroup(Long id, Map<String, Object> updates, String username) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        
        // Validación simple de propiedad
        if (group.getCreator() == null || !group.getCreator().getUsername().equals(username)) {
             // En producción usarías una excepción personalizada
             throw new RuntimeException("No tienes permiso para editar este grupo");
        }

        if (updates.containsKey("name")) group.setName((String) updates.get("name"));
        if (updates.containsKey("description")) group.setDescription((String) updates.get("description"));
        if (updates.containsKey("subject")) group.setSubject((String) updates.get("subject"));
        
        return studyGroupRepository.save(group);
    }

    @Override
    public void deleteGroup(Long id, String username) {
        StudyGroup group = studyGroupRepository.findById(id).orElse(null);
        if (group != null && group.getCreator() != null && group.getCreator().getUsername().equals(username)) {
            studyGroupRepository.delete(group);
        }
    }

    @Override
    public void joinGroup(Long groupId, String username) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Usar el método helper del modelo
        if (!group.addMember(user)) {
            throw new RuntimeException("No se pudo unir: El grupo está lleno o ya eres miembro.");
        }
        studyGroupRepository.save(group);
    }

    @Override
    public void leaveGroup(Long groupId, String username) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Usar el método helper del modelo
        if (group.removeMember(user)) {
            studyGroupRepository.save(group);
        }
    }

    @Override
    public List<StudyGroup> findByMember(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return studyGroupRepository.findByMemberId(user.getId());
    }
}