package cn.ai.controller;

import cn.ai.entity.Word;
import cn.ai.service.IWordService;
import cn.satoken.util.Result;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private static final String MODEL = "qwen-tts";
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    IWordService wordService;
    //    @Autowired
//    DashScopeSpeechSynthesisModel ttsModel;
    MultiModalConversation conv = new MultiModalConversation();
    StringBuilder sb = new StringBuilder();
    @Autowired
    private Map<String, ChatClient> aiMap;
    @Autowired
    private ChatMemory chatMemory;
    @Value("classpath:/ai/hanzi-prompt.st")
    private Resource hanziPrompt;
    @Value("classpath:/ai/chat-prompt.st")
    private Resource chatPrompt;
    @Value("${ai.ali-key}")
    private String aliKey;

//    @GetMapping(value = "/chat", produces = "text/stream;charset=utf-8")
//    public Flux<String> chat(String prompt, @RequestParam(defaultValue = "1") String user) {
//        return aiMap.get("deepseekChat")
//                .prompt()
//                .user(prompt)
//                .system(chatPrompt)
////                .system(p -> p.param("role", "杠精"))
//                .advisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
//                .stream()
//                .content();
//    }


    @GetMapping("/getWords")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Throwable.class)
    public Result getWords(@RequestParam String word) {
        Word w = wordService.lambdaQuery().eq(Word::getWord, word).one();
        if (w != null) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("text", w.getWords());
            resultMap.put("url", "ai/audio/" + word + ".mp3"); // 前端直接访问
            return Result.data(resultMap);
        }

        String words = aiMap.get("deepseekChat")
                .prompt()
                .user(word)
                .system(hanziPrompt)
                .call()
                .content();
        List<String> wordList = objectMapper.readValue(words, List.class);
        Word build = Word.builder().word(word).words(wordList).build();

        sb.setLength(0);
        for (String s : wordList) {
            sb.append(s).append("。").append("\n");
        }

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model(MODEL)
                .text(sb.toString())
                .voice(AudioParameters.Voice.CHERRY)
                .apiKey(aliKey)
                .build();
        MultiModalConversationResult result = conv.call(param);
        String audioUrl = result.getOutput().getAudio().getUrl();

        // 下载音频文件到本地
        File dir = new File("app/audio");
        if (!dir.exists()) {
            dir.mkdirs();  // 递归创建目录
        }
        try (InputStream in = new URL(audioUrl).openStream();
             FileOutputStream out = new FileOutputStream("app/audio/" + word + ".mp3")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        wordService.save(build);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("text", build.getWords());
        resultMap.put("url", "ai/audio/" + word + ".mp3"); // 前端直接访问
        return Result.data(resultMap);

    }


    @GetMapping("/audio/{fileName}")
    @SneakyThrows
    public void getAudio(@PathVariable String fileName, HttpServletResponse response) {
        File file = new File("app/audio/" + fileName);
        response.setContentType("audio/mpeg");
        Files.copy(file.toPath(), response.getOutputStream());
    }

}
