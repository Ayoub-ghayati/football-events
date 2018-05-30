package org.djar.football.player.connect;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.djar.football.Topics;
import org.djar.football.event.PlayerStartedCareer;
import org.djar.football.stream.EventPublisher;
import org.djar.football.stream.JsonPojoSerde;
import org.djar.football.test.StreamsTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StreamUtils;

public class PlayerEventProducerTest {

    private StreamsTester tester;
    private PlayerEventProducer producer;

    @Before
    public void setUp() {
        tester = new StreamsTester(getClass().getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        producer = new PlayerEventProducer(new EventPublisher(null, getClass().getSimpleName(), 1));
        producer.build(streamsBuilder);

        Topology topology = streamsBuilder.build();
        tester.setUp(topology);
    }

    @Test
    public void test() throws Exception {
        String json = StreamUtils.copyToString(getClass().getResourceAsStream("player-inserted.json"),
                Charset.defaultCharset());
        tester.sendStringMessage(1L, json, "fb-connect.public.players");
        ProducerRecord<String, PlayerStartedCareer> event = tester.read(Topics.topicName(PlayerStartedCareer.class),
                new StringDeserializer(), new JsonPojoSerde<>(PlayerStartedCareer.class));

        assertThat(event.key()).isEqualTo("1");
        assertThat(event.value().getPlayerId()).isEqualTo("1");
        assertThat(event.value().getName()).isEqualTo("Player One");
    }

    @After
    public void tearDown() throws Exception {
        tester.close();
    }
}
