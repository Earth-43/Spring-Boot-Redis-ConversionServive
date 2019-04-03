package com.spring.redis.controller;

import com.spring.redis.cache.UserCacheKey;
import com.spring.redis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    @Qualifier("sessionCacheManager")
    private RedisCacheManager sessionCacheManager;


    @Autowired
    @Qualifier("userCacheManager")
    private RedisCacheManager userCacheManager;

    @Autowired
    private UserService userService;

    /*Request mappings are not upto the rest standards so please dont' judge :) */

    @RequestMapping("/")
    public String index() {
        Cache sessionCache = sessionCacheManager.getCache("session-cache");
        sessionCache.get("_first_token");
        LocalDateTime currentTime = LocalDateTime.now();
        sessionCache.put("_first_token", LocalDateTime.now());
        return "Greetings from Spring Boot!";
    }

    @PostMapping("/set-key")
    public String setKey() {
        Cache sessionCache = sessionCacheManager.getCache("session-cache");
        sessionCache.put("_first_token", new Date().getTime());
        return "Done the job";
    }

    @GetMapping("/get-key")
    public Map getKey() {
        Map testMap = new HashMap();
        Cache sessionCache = sessionCacheManager.getCache("session-cache");
        Object sessionKey = sessionCache.get("_first_token").get();
        if (sessionKey != null) {
            sessionCache.put("_first_token", new Date().getTime());
            testMap.put("key-exist", true);
            return testMap;
        }
        testMap.put("key-exist", false);
        return testMap;
    }

    // Should return user object instead of map
    @GetMapping("/user-data")
    public Map getUserData() {
        Cache userCache = userCacheManager.getCache("user-cache");
        UserCacheKey userCacheKey = new UserCacheKey("some-data-from-request");
        Cache.ValueWrapper result = userCache != null ? userCache.get(userCacheKey) : null;
        Map<String, String> userData = result == null ? null : (Map<String, String>) result.get();
        if (CollectionUtils.isEmpty(userData)) {
            String userId = String.valueOf(userService.getUserId(userCacheKey));
            String cacheKey = String.format("%s_%s", userId, "$user_data$");
            userCache.put(cacheKey, userId);
        }

        return userData;
    }

}