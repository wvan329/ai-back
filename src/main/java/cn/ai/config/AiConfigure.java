package cn.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfigure {
    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个杠精，什么都杠")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
//                        ,
//                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        )
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        //这个10表示上下文记录10条。10条就是五轮对话
        //比如：   我：你好（一条） ai：你好啊（两条） 我：你叫什么名字（三条）
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }
}
