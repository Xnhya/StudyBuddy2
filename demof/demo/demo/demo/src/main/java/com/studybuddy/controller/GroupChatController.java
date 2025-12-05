package com.studybuddy.controller;

import com.studybuddy.model.Message;
import com.studybuddy.model.StudyGroup;
import com.studybuddy.model.User;
import com.studybuddy.repository.MessageRepository;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class GroupChatController {

    private static final Logger logger = LoggerFactory.getLogger(GroupChatController.class);

    private final StudyGroupRepository groupRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public GroupChatController(StudyGroupRepository groupRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/groups/{id}/chat")
    public String chatPage(@PathVariable Long id, Model model, Authentication auth) {
        StudyGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) {
            return "redirect:/groups";
        }
        model.addAttribute("group", group);
        model.addAttribute("username", auth != null ? auth.getName() : "");
        return "group/chat";
    }

    @GetMapping("/api/groups/{id}/messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable Long id) {
        try {
            StudyGroup group = groupRepository.findById(id).orElse(null);
            if (group == null) return ResponseEntity.notFound().build();

            List<Message> messages = messageRepository.findByStudyGroupOrderBySentAtDesc(group);
            List<Map<String,Object>> dto = messages.stream()
                .map(m -> {
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("id", m.getId());
                    msgMap.put("content", m.getContent());
                    msgMap.put("sender", m.getSender() != null ? m.getSender().getUsername() : "Sistema");
                    msgMap.put("sentAt", m.getSentAt());
                    msgMap.put("type", m.getMessageType());
                    return msgMap;
                }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("messages", dto));
        } catch (Exception ex) {
            logger.error("Error getting messages for group {}: {}", id, ex.toString(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_server_error"));
        }
    }

    @PostMapping("/api/groups/{id}/messages")
    @ResponseBody
    public ResponseEntity<?> postMessage(@PathVariable Long id,
                                         @RequestBody Map<String,String> payload,
                                         Authentication auth) {
        logger.debug("POST /api/groups/{}/messages payload={}, user={} ", id, payload, auth!=null?auth.getName():null);
        try {
            StudyGroup group = groupRepository.findById(id).orElse(null);
            if (group == null) return ResponseEntity.notFound().build();

            if (!Boolean.TRUE.equals(group.getIsChatEnabled())) {
                logger.info("Chat attempt but chat disabled for group {}", id);
                return ResponseEntity.badRequest().body(Map.of("error", "Chat not enabled for this group"));
            }

            if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));

            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));

            String content = payload.getOrDefault("content", "").trim();
            if (content.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Empty message"));

            Message m = new Message();
            m.setContent(content);
            m.setSender(user);
            m.setStudyGroup(group);
            Message saved = messageRepository.save(m);

            logger.info("Saved message {} in group {} by user {}", saved.getId(), id, user.getUsername());

            return ResponseEntity.ok(Map.of("message", "saved", "id", saved.getId()));
        } catch (Exception ex) {
            logger.error("Error posting message to group {}: {}", id, ex.toString(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_server_error"));
        }
    }
}
