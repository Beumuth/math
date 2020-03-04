package com.beumuth.math.core.internal.client;

import com.beumuth.math.client.ClientConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ClientConfigurations implements ApplicationContextAware {

    public static ClientConfiguration LOCAL;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        LOCAL = new ClientConfiguration(context.getApplicationName());
    }
}
