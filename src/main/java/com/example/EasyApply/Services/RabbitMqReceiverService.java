package com.example.EasyApply.Services;

import com.example.EasyApply.Data.CompanyDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class RabbitMqReceiverService {
    ConnectionFactory factory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final  String QUEUE_NAME = "hello";
    Channel channel;
    public void initializeRabbitMq() {
        try {
            factory  = new ConnectionFactory();
            factory.setHost("localhost");  // Use the container name if in a separate Docker container
            factory.setPort(5672);         // AMQP port

            Connection connection = factory.newConnection();
            channel  = connection.createChannel() ;


        }catch (IOException e){
            e.printStackTrace();
        }catch (TimeoutException e){
            e.printStackTrace();
        }


       }
       public void receive() {
           try {

               boolean durable = true;
               channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

               DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                   String message = new String(delivery.getBody(), "UTF-8");
                   CompanyDetailsResponse customMessage = objectMapper.readValue(message, CompanyDetailsResponse.class);

                   System.out.println(" [x] Received '" + customMessage.getFailedMap() + "'");
               };
               channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
               });
           }catch (IOException e){
               e.printStackTrace();
           }
       }
}
