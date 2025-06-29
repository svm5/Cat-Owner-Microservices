package labs.externalmicroservice.kafka;

import labs.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, Long> stringLongProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, Long> stringLongKafkaTemplate() {
        return new KafkaTemplate<>(stringLongProducerFactory());
    }

    @Bean
    public ProducerFactory<String, CreateOwnerDTO> createOwnerDTOProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

//    @Bean
//    public ProducerFactory<String, OwnerDTO> ownerDTOProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }

//    @Bean
//    public ProducerFactory<String, Long> getOwnerByIdProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }

//    @Bean
//    public ConsumerFactory<String, OwnerDTO> consumerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
//        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(configProps);
//    }

    @Bean
    public KafkaTemplate<String, CreateOwnerDTO> createOwnerDTOKafkaTemplate() {
        return new KafkaTemplate<>(createOwnerDTOProducerFactory());
    }

//    @Bean
//    KafkaMessageListenerContainer<String, OwnerDTO> getOwnerKafkaMessageListenerContainer(
//            ConsumerFactory<String, OwnerDTO> consumerFactory) {
//        String replyTopic = "get_owner_by_id_response";
//        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
//        containerProperties.setGroupId("group1");
//        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
//    }

//    @Bean
//    ReplyingKafkaTemplate<String, Long, OwnerDTO> getOwnerReplyingKafkaTemplate(
//            ProducerFactory<String, Long> producerFactory,
//            KafkaMessageListenerContainer<String, OwnerDTO> kafkaMessageListenerContainer
//    ) {
////        Duration replyTimeout = synchronousKafkaProperties.replyTimeout();
//        var replyingKafkaTemplate = new ReplyingKafkaTemplate<>(producerFactory, kafkaMessageListenerContainer);
//        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(30));
////        replyingKafkaTemplate.setRe
//        return replyingKafkaTemplate;
//    }
//
//    @Bean
//    KafkaTemplate<String, OwnerDTO> getOwnerKafkaTemplate(ProducerFactory<String, OwnerDTO> producerFactory) {
//        return new KafkaTemplate<>(producerFactory);
//    }

// 1111111111111
//
//    @Bean
//    public ProducerFactory<String, Long> getOwnerProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean
//    public ConsumerFactory<String, OwnerDTO> getOwnerConsumerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
//        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(configProps);
//    }
//
//    @Bean
//    public KafkaTemplate<String, Long> getOwnerKafkaTemplate() {
//        return new KafkaTemplate<>(getOwnerProducerFactory());
//    }
//
//    @Bean
//    public ConcurrentMessageListenerContainer<String, OwnerDTO> replyListenerContainer() {
//        ContainerProperties containerProperties = new ContainerProperties("get_owner_by_id_response");
//        return new ConcurrentMessageListenerContainer<>(
//                getOwnerConsumerFactory(),
//                containerProperties
//        );
//    }
//
    @Bean
    public ReplyingKafkaTemplate<String, Long, GetOwnerDTO> replyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, Long, GetOwnerDTO> template = new ReplyingKafkaTemplate<>(
                requestProducerFactory(),
                replyListenerContainer()
        );
//        template.setDefaultTopic("get_owner_by_id_request");
//        template.setReplyTimeout(replyTimeout);
        return template;
    }

    @Bean
    public Map < String, Object > consumerConfigs() {
        Map < String, Object > props = new HashMap < > ();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");

        return props;
    }

    @Bean
    public Map < String, Object > producerConfigs() {
        Map < String, Object > props = new HashMap < > ();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory < String, Long > requestProducerFactory() {
        return new DefaultKafkaProducerFactory < > (producerConfigs());
    }

    @Bean
    public ProducerFactory < String, GetAllOwnersRequest > getAllOwnersRequestProducerFactory() {
        return new DefaultKafkaProducerFactory < > (producerConfigs());
    }

    @Bean
    public ConsumerFactory < String, GetOwnerDTO > replyConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory < String, GetAllOwnersResponse> getAllOwnersReplyConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaMessageListenerContainer < String, GetOwnerDTO > replyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties("get_owner_by_id_response");
        return new KafkaMessageListenerContainer < > (replyConsumerFactory(), containerProperties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, GetAllOwnersResponse> getAllOwnersReplyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties("get_all_owners_response");
        return new KafkaMessageListenerContainer < > (getAllOwnersReplyConsumerFactory(), containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, GetAllOwnersRequest, GetAllOwnersResponse> getAllOwnersReplyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, GetAllOwnersRequest, GetAllOwnersResponse> template = new ReplyingKafkaTemplate<>(
                getAllOwnersRequestProducerFactory(),
                getAllOwnersReplyListenerContainer()
        );
        return template;
    }

    //
    @Bean
    public ProducerFactory < String, CreateCatDTO > createCatRequestProducerFactory() {
        return new DefaultKafkaProducerFactory < > (producerConfigs());
    }

    @Bean
    public ConsumerFactory < String, CatDTO> createCatReplyConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaMessageListenerContainer<String, CatDTO> createCatReplyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties("create_cat_response");
        return new KafkaMessageListenerContainer < > (createCatReplyConsumerFactory(), containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, CreateCatDTO, CatDTO> createCatReplyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, CreateCatDTO, CatDTO> template = new ReplyingKafkaTemplate<>(
                createCatRequestProducerFactory(),
                createCatReplyListenerContainer()
        );
        return template;
    }

    //
    @Bean
    public ProducerFactory < String, Long > getCatRequestProducerFactory() {
        return new DefaultKafkaProducerFactory < > (producerConfigs());
    }

    @Bean
    public ConsumerFactory < String, GetCatDTO> getCatReplyConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaMessageListenerContainer<String, GetCatDTO> getCatReplyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties("get_cat_response");
        return new KafkaMessageListenerContainer < > (getCatReplyConsumerFactory(), containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Long, GetCatDTO> getCatReplyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, Long, GetCatDTO> template = new ReplyingKafkaTemplate<>(
                getCatRequestProducerFactory(),
                getCatReplyListenerContainer()
        );
        return template;
    }
    // all about friends

//    @Bean
//    public ProducerFactory<String, CatsFriendsRequest> friendsProducerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(
//                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//                bootstrapServers);
//        configProps.put(
//                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        configProps.put(
//                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean
//    public KafkaTemplate<String, CatsFriendsRequest> friendsKafkaTemplate() {
//        return new KafkaTemplate<>(friendsProducerFactory());
//    }

    @Bean
    public ProducerFactory < String, CatsFriendsRequest > friendsRequestProducerFactory() {
        return new DefaultKafkaProducerFactory < > (producerConfigs());
    }

    @Bean
    public ConsumerFactory < String, CatsFriendsResponse> friendsReplyConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaMessageListenerContainer<String, CatsFriendsResponse> friendsReplyListenerContainer() {
        ContainerProperties containerProperties = new ContainerProperties("make_friends_response", "unmake_friends_response");
        return new KafkaMessageListenerContainer < > (friendsReplyConsumerFactory(), containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, CatsFriendsRequest, CatsFriendsResponse> friendsReplyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, CatsFriendsRequest, CatsFriendsResponse> template = new ReplyingKafkaTemplate<>(
                friendsRequestProducerFactory(),
                friendsReplyListenerContainer()
        );
        return template;
    }

    //
}
