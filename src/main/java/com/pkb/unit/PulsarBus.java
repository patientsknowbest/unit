package com.pkb.unit;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Implementation of a Bus backed by a pulsar instance,
 * so that units may talk to each other across applications.
 */
public class PulsarBus implements Bus {
    private final PulsarClient pulsarClient;
    private final String fullyQualifiedTopic;
    private final String subscriptionName;

    public PulsarBus(String serviceURL,
                     String fullyQualifiedTopic,
                     String subscriptionName) {
        this.subscriptionName = subscriptionName;
        try {
            this.pulsarClient = PulsarClient.builder()
                    .operationTimeout(10000, TimeUnit.DAYS)
                    .connectionTimeout(1, TimeUnit.SECONDS)
                    .serviceUrl(serviceURL)
                    .build();
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
        this.fullyQualifiedTopic = fullyQualifiedTopic;
    }

    @Override
    public Consumer<Message> sink() {
        try {
            Producer<Message> producer = pulsarClient.newProducer(Schema.AVRO(Message.class))
                    .topic(fullyQualifiedTopic)
                    .create();
            return producer::send;
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Observable<Message> events() {
        return Observable.fromIterable(() -> new PulsarConsumerIterator(pulsarClient, fullyQualifiedTopic, subscriptionName));
    }

    private static class PulsarConsumerIterator implements Iterator<Message> {
        private final org.apache.pulsar.client.api.Consumer<Message> pulsarConsumer;

        private PulsarConsumerIterator(PulsarClient pulsarClient,
                                       String fullyQualifiedTopic,
                                       String subscriptionName) {
            try {
                this.pulsarConsumer = pulsarClient.newConsumer(Schema.AVRO(Message.class))
                        .topic(fullyQualifiedTopic)
                        .subscriptionName(subscriptionName)
                        .subscribe();
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return !pulsarConsumer.hasReachedEndOfTopic();
        }

        @Override
        public Message next() {
            try {
                org.apache.pulsar.client.api.Message<Message> pulsarMessage = pulsarConsumer.receive();
                pulsarConsumer.acknowledge(pulsarMessage);
                return pulsarMessage.getValue();
            } catch (PulsarClientException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
