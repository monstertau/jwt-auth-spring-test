package com.example.testJWTAuthentication.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, String> template;
    private final int MAX_TOKEN_LIST_SIZE = 10;

    public void pushToken(String jwt) {
        template.opsForList().rightPush("blacklist_jwt", jwt);
        Long size = template.opsForList().size("blacklist_jwt");
        assert size != null;
        if (size > MAX_TOKEN_LIST_SIZE) {
            template.opsForList().trim("blacklist_jwt", size - 10, size);
        }

    }

    public Boolean checkBlackListToken(String jwt) {
        List<String> tokenList = template.opsForList().range("blacklist_jwt", 0, MAX_TOKEN_LIST_SIZE);
        assert tokenList != null;
        return tokenList.contains(jwt);
    }
}

