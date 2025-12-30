package com.deepintent.di_config_server_demo.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "debug",
    havingValue = "true"
)
public class GitBranchDiagnostics implements ApplicationListener<ContextRefreshedEvent> {

    private final Environment environment;
    private final EnvironmentRepository environmentRepository;

    public GitBranchDiagnostics(Environment environment,
                               EnvironmentRepository environmentRepository) {
        this.environment = environment;
        this.environmentRepository = environmentRepository;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.onApplicationEvent\",\"message\":\"Git branch diagnostics started\",\"data\":{\"timestamp\":\"" + System.currentTimeMillis() + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion agent log

        log.info("==========================================");
        log.info("GIT BRANCH DIAGNOSTICS");
        log.info("==========================================");

        checkBranchConfiguration();
        testBranchResolution();

        log.info("==========================================");
    }

    private void checkBranchConfiguration() {
        log.info("--- Branch Configuration ---");
        
        String serverDefaultLabel = environment.getProperty("spring.cloud.config.server.default-label");
        String compositeDefaultLabel = environment.getProperty("spring.cloud.config.server.composite[0].default-label");
        String gitBranchEnv = System.getenv("GIT_BRANCH");
        
        log.info("Server default-label: {}", serverDefaultLabel);
        log.info("Composite default-label: {}", compositeDefaultLabel);
        log.info("GIT_BRANCH env var: {}", gitBranchEnv != null ? gitBranchEnv : "NOT SET");
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.checkBranchConfiguration\",\"message\":\"Branch configuration values\",\"data\":{\"serverDefaultLabel\":\"" + (serverDefaultLabel != null ? serverDefaultLabel : "null") + "\",\"compositeDefaultLabel\":\"" + (compositeDefaultLabel != null ? compositeDefaultLabel : "null") + "\",\"gitBranchEnv\":\"" + (gitBranchEnv != null ? gitBranchEnv : "NOT_SET") + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion agent log

        String effectiveLabel = compositeDefaultLabel != null ? compositeDefaultLabel : serverDefaultLabel;
        if (effectiveLabel != null && effectiveLabel.contains("main")) {
            log.error("WARNING: Found 'main' in branch configuration: {}", effectiveLabel);
            
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.checkBranchConfiguration\",\"message\":\"WARNING: Found main in branch config\",\"data\":{\"effectiveLabel\":\"" + effectiveLabel + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception e) {}
            // #endregion agent log
        }
    }

    private void testBranchResolution() {
        log.info("--- Testing Branch Resolution ---");
        
        String testApplication = "publisher-service";
        String testProfile = "dev";
        String testLabel = "master";
        
        log.info("Testing with: application={}, profile={}, label={}", testApplication, testProfile, testLabel);
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.testBranchResolution\",\"message\":\"Testing branch resolution\",\"data\":{\"application\":\"" + testApplication + "\",\"profile\":\"" + testProfile + "\",\"label\":\"" + testLabel + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion agent log
        
        try {
            var env = environmentRepository.findOne(testApplication, testProfile, testLabel);
            
            if (env != null && env.getPropertySources() != null && !env.getPropertySources().isEmpty()) {
                log.info("SUCCESS: Retrieved config for label '{}' with {} property sources", testLabel, env.getPropertySources().size());
                
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.testBranchResolution\",\"message\":\"Branch resolution SUCCESS\",\"data\":{\"label\":\"" + testLabel + "\",\"propertySourceCount\":" + env.getPropertySources().size() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion agent log
            } else {
                log.error("FAILED: No property sources for label '{}'", testLabel);
                
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.testBranchResolution\",\"message\":\"Branch resolution FAILED\",\"data\":{\"label\":\"" + testLabel + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception e) {}
                // #endregion agent log
            }
        } catch (Exception e) {
            if (e.getCause() instanceof org.eclipse.jgit.api.errors.RefNotFoundException) {
                org.eclipse.jgit.api.errors.RefNotFoundException refEx = (org.eclipse.jgit.api.errors.RefNotFoundException) e.getCause();
                log.error("ERROR: Branch not found - {}", refEx.getMessage());
                
                // #region agent log
                try {
                    java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
                    fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.testBranchResolution\",\"message\":\"RefNotFoundException - branch not found\",\"data\":{\"error\":\"" + refEx.getClass().getSimpleName() + "\",\"message\":\"" + refEx.getMessage().replace("\"", "'") + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                    fw.close();
                } catch (Exception ex) {}
                // #endregion agent log
            }
            log.error("ERROR during branch resolution test: {}", e.getMessage(), e);
            
            // #region agent log
            try {
                java.io.FileWriter fw = new java.io.FileWriter("/Users/tanmay.thanvi/Deepintent/SideProjects/DI Config Server POC/.cursor/debug.log", true);
                fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"startup\",\"hypothesisId\":\"C\",\"location\":\"GitBranchDiagnostics.testBranchResolution\",\"message\":\"Branch resolution ERROR\",\"data\":{\"error\":\"" + e.getClass().getSimpleName() + "\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n");
                fw.close();
            } catch (Exception ex) {}
            // #endregion agent log
        }
    }
}

