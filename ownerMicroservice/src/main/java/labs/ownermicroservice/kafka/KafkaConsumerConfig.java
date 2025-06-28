package labs.ownermicroservice.kafka;

import labs.CreateOwnerDTO;
import labs.GetAllOwnersRequest;
import labs.OwnerDTO;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, Long> ownerIdconsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(Long.class));
    }

    @Bean
    public ConsumerFactory<String, GetAllOwnersRequest> getAllOwnersConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(GetAllOwnersRequest.class));
    }

    @Bean
    public ConsumerFactory<String, CreateOwnerDTO> createOwnerDTOConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(CreateOwnerDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateOwnerDTO> createOwnerDTOKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CreateOwnerDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createOwnerDTOConsumerFactory());
        return factory;
    }

//    @Bean
//    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Long>> getOwnerKafkaListenerContainerFactory(
//            ConsumerFactory<String, Long> consumerFactory,
//            KafkaTemplate<String, OwnerDTO> kafkaTemplate) {
//        var factory = new ConcurrentKafkaListenerContainerFactory<String, Long>();
//        factory.setConsumerFactory(consumerFactory);
//        factory.setReplyTemplate(kafkaTemplate);
//        return factory;
//    }

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
    public ConsumerFactory < String, Long > requestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConsumerFactory < String, GetAllOwnersRequest > getAllOwnersRequestConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public KafkaListenerContainerFactory < ConcurrentMessageListenerContainer < String, Long >> requestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, Long > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(requestConsumerFactory());
        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, OwnerDTO > replyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate < String, OwnerDTO > replyTemplate() {
        return new KafkaTemplate < > (replyProducerFactory());
    }

    //

    @Bean
    public KafkaListenerContainerFactory < ConcurrentMessageListenerContainer < String, GetAllOwnersRequest>> getAllOwnersRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory < String, GetAllOwnersRequest > factory =
                new ConcurrentKafkaListenerContainerFactory < > ();
        factory.setConsumerFactory(getAllOwnersRequestConsumerFactory());
        factory.setReplyTemplate(getAllOwnersReplyTemplate());
        return factory;
    }

    @Bean
    public ProducerFactory< String, List<OwnerDTO>> getAllOwnersReplyProducerFactory() {
        return new DefaultKafkaProducerFactory< >(producerConfigs());
    }

    @Bean
    public KafkaTemplate < String, List<OwnerDTO> > getAllOwnersReplyTemplate() {
        return new KafkaTemplate < > (getAllOwnersReplyProducerFactory());
    }
}
