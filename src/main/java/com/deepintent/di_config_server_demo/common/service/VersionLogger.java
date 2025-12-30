package com.deepintent.di_config_server_demo.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.deepintent.di_config_server_demo.common.config.ServerConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class VersionLogger implements ApplicationListener<ApplicationReadyEvent> {

  private final ServerConfig serverConfig;

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    log.info("\uD83D\uDE80 DI Config Server started successfully. Version: {}", serverConfig.getServiceVersion());
  }
}
