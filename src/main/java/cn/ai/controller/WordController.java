package cn.ai.controller;


import cn.ai.entity.Word;
import cn.ai.service.IWordService;
import cn.satoken.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2025-08-09
 */
@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    IWordService wordService;
    @Autowired
    private ChatClient ai;
    @Value("classpath:/ai/hanzi-prompt.st")
    private Resource hanziPrompt;

    @RequestMapping(value = "/getWords")
    @SneakyThrows
    public Result getWords(@RequestParam String word) {
        Word w = wordService.lambdaQuery().eq(Word::getWord, word).one();
        if (w != null) {
            return Result.data(w.getWords());
        }
        String words = ai.prompt()
                .user(word)
                .system(hanziPrompt)
                .call()
                .content();
        List<String> wordList = objectMapper.readValue(words, List.class);
        Word build = Word.builder().word(word).words(wordList).build();
        wordService.save(build);
        return Result.data(wordList);
    }
}
