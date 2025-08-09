package cn.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatClient ai;
    @Autowired
    private ChatMemory chatMemory;

    @Value("classpath:/ai/chat-prompt.st")
    private Resource chatPrompt;

    @RequestMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(String prompt, @RequestParam(defaultValue = "1") String user) {
        return ai.prompt()
                .user(prompt)
                .system(chatPrompt)
//                .system(p -> p.param("role", "杠精"))
                .advisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
                .stream()
                .content();
    }
}
