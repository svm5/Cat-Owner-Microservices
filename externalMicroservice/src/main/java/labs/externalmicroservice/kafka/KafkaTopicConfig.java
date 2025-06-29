package labs.externalmicroservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    NewTopic topic1() {
        return TopicBuilder.name("create_owner").build();
    }

    @Bean
    NewTopic topic2() {
        return TopicBuilder.name("delete_all_owners").build();
    }

    @Bean
    NewTopic topic3() {
        return TopicBuilder.name("get_owner_by_id_request").build();
    }

    @Bean
    NewTopic topic4() {
        return TopicBuilder.name("get_owner_by_id_response").build();
    }

    @Bean
    NewTopic topic5() {
        return TopicBuilder.name("delete_owner_by_id_request").build();
    }

    @Bean
    NewTopic topic6() {
        return TopicBuilder.name("delete_owner_by_id_response").build();
    }

    @Bean
    NewTopic topic7() {
        return TopicBuilder.name("delete_owner_by_id").build();
    }

    @Bean
    NewTopic topic8() {
        return TopicBuilder.name("create_cat_request").build();
    }

    @Bean
    NewTopic topic9() {
        return TopicBuilder.name("create_cat_response").build();
    }

    @Bean
    NewTopic topic10() {
        return TopicBuilder.name("get_cat_request").build();
    }

    @Bean
    NewTopic topic11() {
        return TopicBuilder.name("get_cat_response").build();
    }

    @Bean
    NewTopic topic12() {
        return TopicBuilder.name("make_friends_request").build();
    }

    @Bean
    NewTopic topic16() {
        return TopicBuilder.name("make_friends_response").build();
    }

    @Bean
    NewTopic topic13() {
        return TopicBuilder.name("unmake_friends_request").build();
    }

    @Bean
    NewTopic topic17() {
        return TopicBuilder.name("unmake_friends_response").build();
    }

    @Bean
    NewTopic topic14() {
        return TopicBuilder.name("delete_all_cats").build();
    }

    @Bean
    NewTopic topic15() {
        return TopicBuilder.name("delete_cat_by_id").build();
    }
}
