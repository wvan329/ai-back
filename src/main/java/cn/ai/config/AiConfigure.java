package cn.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class AiConfigure {

    @Value("classpath:/ai/system-prompt.st")
    private Resource systemPrompt;

    @Bean
    public ChatClient chat(ChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        //自定义拦截器
//                        new MyAdvisor(),
                        //记录输入输出日志
//                        new SimpleLoggerAdvisor(),
                        //敏感词拦截器
                        new SafeGuardAdvisor(List.of("王广阔"), "你怎么敢", 1),
                        //上下文记忆
                        //PromptChatMemoryAdvisor与MessageChatMemoryAdvisor功能类似
                        //PromptChatMemoryAdvisor兼容更多模型，用这个即可
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
//                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                ).build();
    }

/*    @Bean
    public ChatClient reasoner(ChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultOptions(
                        ChatOptions.builder()
                                .model("deepseek-reasoner")
                                .build()
                )
                .defaultSystem("你是一只很可爱的小猫")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }*/

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        //ChatMemoryAutoConfiguration.class
        //上面这个类注入了ChatMemory和ChatMemoryRepository
        //现在希望设置保存的最大上下文长度，于是手动构造一个ChatMemory

        //这个10表示上下文记录10条。10条就是五轮对话
        //比如：   我：你好（一条） ai：你好啊（两条） 我：你叫什么名字（三条）

        //自带的记忆会自动把数据库里之前的对话也删了，我觉得所有对话都可以保存下来，所以自己改造了MyMessageWindowChatMemory
        return MyMessageWindowChatMemory.builder()
                .maxMessages(14)
                //一旦引入JdbcChatMemoryRepository这个依赖，注入的就是JdbcChatMemoryRepository仓库了
                //因为JdbcChatMemoryRepositoryAutoConfiguration的自动配置类有顺序，在ChatMemoryAutoConfiguration之前执行。
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }
}
