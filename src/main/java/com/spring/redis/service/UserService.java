package com.spring.redis.service;

import com.spring.redis.cache.UserCacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class UserService {

    public Long getUserId(UserCacheKey userCacheKey) {
        // get the user from database based on userCacheKey
        return 1L;
    }
}
