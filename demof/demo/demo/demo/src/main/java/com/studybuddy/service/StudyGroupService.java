package com.studybuddy.service;

import com.studybuddy.model.StudyGroup;
import com.studybuddy.controller.StudyGroupController.CreateGroupRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StudyGroupService {
    
    List<StudyGroup> findAll();
    
    Optional<StudyGroup> findById(Long id);
    
    StudyGroup createGroup(CreateGroupRequest request, String username);
    
    StudyGroup updateGroup(Long id, Map<String, Object> updates, String username);
    
    void deleteGroup(Long id, String username);
    
    void joinGroup(Long groupId, String username);
    
    void leaveGroup(Long groupId, String username);
    
    List<StudyGroup> findByMember(String username);
}