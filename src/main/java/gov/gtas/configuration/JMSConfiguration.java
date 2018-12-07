/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
@PropertySource("classpath:application.yml")
public class JMSConfiguration {

    @Value("${mq.connection}")
    private String DEFAULT_BROKER_URL;

    @Value("${mq.outbound.queue}")
    private String OUTBOUND_QUEUE;

    @Value("${mq.username}")
    private String USER_NAME;

    @Value("${mq.password}")
    private String PASSWORD;

    @Value("${mq.concurrency}")
    String concurrency;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(DEFAULT_BROKER_URL);
        connectionFactory.setUserName(USER_NAME);
        connectionFactory.setPassword(PASSWORD);
        return connectionFactory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(connectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplateJason() {
        return new JmsTemplate(cachingConnectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplateFile() {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setDefaultDestinationName(OUTBOUND_QUEUE);
        return jmsTemplate;
    }
}
