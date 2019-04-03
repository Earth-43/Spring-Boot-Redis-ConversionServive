package com.spring.redis.cache;

import com.spring.redis.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;


@AllArgsConstructor
public class UserCacheKeyConverter implements Converter<UserCacheKey, String> {

    private UserService userService;

    @Override
    public String convert(UserCacheKey userCacheKey) {
        String userId = String.valueOf(userService.getUserId(userCacheKey));
        return String.format("%s_%s", userId, "$user_data$");
    }
}
