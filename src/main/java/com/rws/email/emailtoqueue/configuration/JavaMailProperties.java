package com.rws.email.emailtoqueue.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Configuration
@PropertySource("classpath:application.yml")
public class JavaMailProperties {

    @Value("${email.mail.props.imap.socketFactory.class}")
    String socketFactory;

    @Value("${email.mail.props.imap.socketFactory.fallback}")
    String fallBack;

    @Value("${email.mail.props.store.protocol}")
    String protocol;

    @Value("${email.mail.props.debug}")
    String debug;

    @Bean
    Properties javaProperties() {
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.imap.socketFactory.class", socketFactory);
        javaMailProperties.setProperty("mail.imap.socketFactory.fallback", fallBack);
        javaMailProperties.setProperty("mail.store.protocol", protocol);
        javaMailProperties.setProperty("mail.debug", debug);
        return javaMailProperties;
    }
}
