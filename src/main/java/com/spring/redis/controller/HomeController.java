package com.spring.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
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
    @Qualifier("optionsCacheManager")
    private RedisCacheManager optionsCacheManager;


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
        Cache optionsCache = optionsCacheManager.getCache("options-cache");
        sessionCache.put("_first_token", new Date().getTime());
        optionsCache.put("_first_options", new Date().getTime());
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

}