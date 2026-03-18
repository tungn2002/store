package com.personal.store_api.scheduler;

import com.personal.store_api.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        int initialCount = invalidatedTokenRepository.findAll().size();
        invalidatedTokenRepository.deleteExpiredTokens();
        int removedCount = initialCount - invalidatedTokenRepository.findAll().size();
        
        if (removedCount > 0) {
            log.info("Cleaned up {} expired invalidated tokens", removedCount);
        }
    }
}
