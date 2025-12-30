package com.deepintent.di_config_server_demo.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ServerConfig {
  @Value("${deepintent.config-server.version}")
  private String serviceVersion;
}
