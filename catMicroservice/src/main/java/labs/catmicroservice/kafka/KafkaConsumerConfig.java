package labs.catmicroservice.kafka;

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
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map < String, Object > props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");

        return props;
    }

    @Bean
    public Map <String, Object>producerConfigs() {
        Map < String, Object > props = new HashMap < > ();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    @Bean
    public ConsumerFactory<String, CreateCatDTO> createCatRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(CreateCatDTO.class));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer< String, CreateCatDTO >> createCatRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, CreateCatDTO > factory =
                new ConcurrentKafkaListenerContainerFactory< >();
        factory.setConsumerFactory(createCatRequestConsumerFactory());
        factory.setReplyTemplate(createCatReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, CatDTO> createCatReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate< String, CatDTO > createCatReplyTemplate() {
        return new KafkaTemplate < > (createCatReplyProducerFactory());
    }

    //
    @Bean
    public ConsumerFactory<String, Long> getCatRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(Long.class));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer< String, Long>> getCatRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, Long > factory =
                new ConcurrentKafkaListenerContainerFactory< >();
        factory.setConsumerFactory(getCatRequestConsumerFactory());
        factory.setReplyTemplate(getCatReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, GetCatDTO> getCatReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate< String, GetCatDTO > getCatReplyTemplate() {
        return new KafkaTemplate < > (getCatReplyProducerFactory());
    }
    // old friends

//    @Bean
//    public ConsumerFactory<String, CatsFriendsRequest> catsFriendsConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(CatsFriendsRequest.class));
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, CatsFriendsRequest> catsFriendsKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, CatsFriendsRequest> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(catsFriendsConsumerFactory());
//        return factory;
//    }

    //
    @Bean
    public ConsumerFactory< String, Long > requestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer< String, Long >> requestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory< String, Long > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(requestConsumerFactory());
//        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    // frineds
    // ???
//    @Bean
//    public ConsumerFactory<String, CatsFriendsRequest> friendsConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(CatsFriendsRequest.class));
//    }

    @Bean
    public ConsumerFactory < String, CatsFriendsRequest > friendsRequestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaListenerContainerFactory < ConcurrentMessageListenerContainer < String, CatsFriendsRequest>> friendsRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, CatsFriendsRequest > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(friendsRequestConsumerFactory());
        factory.setReplyTemplate(friendsReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, CatsFriendsResponse> friendsReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate < String, CatsFriendsResponse> friendsReplyTemplate() {
        return new KafkaTemplate < > (friendsReplyProducerFactory());
    }

    // all cats

    @Bean
    public ConsumerFactory < String, GetAllCatsRequest > getAllCatsRequestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaListenerContainerFactory < ConcurrentMessageListenerContainer < String, GetAllCatsRequest>> getAllCatsRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, GetAllCatsRequest > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(getAllCatsRequestConsumerFactory());
        factory.setReplyTemplate(getAllCatsReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, GetAllCatsResponse> getAllCatsReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate < String, GetAllCatsResponse> getAllCatsReplyTemplate() {
        return new KafkaTemplate < > (getAllCatsReplyProducerFactory());
    }

    // change owner

    @Bean
    public ConsumerFactory < String, ChangeOwnerRequest > changeOwnerRequestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaListenerContainerFactory < ConcurrentMessageListenerContainer < String, ChangeOwnerRequest>> changeOwnerRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, ChangeOwnerRequest > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(changeOwnerRequestConsumerFactory());
        factory.setReplyTemplate(changeOwnerReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, ChangeOwnerResponse> changeOwnerReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate < String, ChangeOwnerResponse> changeOwnerReplyTemplate() {
        return new KafkaTemplate < > (changeOwnerReplyProducerFactory());
    }

    //
}
