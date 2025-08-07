package cn.ai.config;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;


public class MyAdvisor implements BaseAdvisor {
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        System.out.println("系统提示词：" + chatClientRequest.prompt().getSystemMessage().getText());
        System.out.println("用户输入：" + chatClientRequest.prompt().getUserMessage().getText());


        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        System.out.println(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
