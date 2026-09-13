package com.nodapex.client;

import com.nodapex.client.post.PostService;
import com.nodapex.client.todo.TodoService;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

//@Configuration
public class TraditionalConfig {

    // clients

    @Bean
    RestClient jsonplaceholderRestClient() {
        return RestClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    // Http Service Proxy Factories

    @Bean
    HttpServiceProxyFactory jsonPlaceholderProxyFactory(RestClient jsonplaceholderRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(jsonplaceholderRestClient))
                .build();
    }

    // JsonPlaceholderService Service Proxies

    @Bean
    TodoService todoService(HttpServiceProxyFactory jsonPlaceholderProxyFactory) {
        return jsonPlaceholderProxyFactory.createClient(TodoService.class);
    }

    @Bean
    PostService postService(HttpServiceProxyFactory jsonPlaceholderProxyFactory) {
        return jsonPlaceholderProxyFactory.createClient(PostService.class);
    }

    // comments, albums, photos, users

}
