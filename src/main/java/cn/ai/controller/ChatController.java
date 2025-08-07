package cn.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private Map<String, ChatClient> chat;

    @RequestMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(String prompt,
                             @RequestParam(defaultValue = "chat") String model,
                             @RequestParam(defaultValue = "1")String user) {
        return chat.get(model).prompt()
                .user(prompt)
                .system(p -> p.param("role", "杠精"))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
                .stream()
                .content();
    }
}
