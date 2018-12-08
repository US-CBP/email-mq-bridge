/*
 * All Application code is Copyright 2016, The Department of Homeland Security (DHS), U.S. Customs and Border Protection (CBP).
 *
 * Please see LICENSE.txt for details.
 */
package gov.gtas;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class EmailToQueueApplication extends SpringBootServletInitializer {
    //   SpringApplication.run(EmailToQueueApplication.class, args);
}


