package com.example.testJWTAuthentication.Controller;

import com.example.testJWTAuthentication.Services.MyUserDetailsService;
import com.example.testJWTAuthentication.model.AuthenticationRequest;
import com.example.testJWTAuthentication.model.AuthenticationResponse;
import com.example.testJWTAuthentication.util.JWTUtil;
import com.example.testJWTAuthentication.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class TestController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("/hello")
    public String hello() {

        return "Hello World";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect Username or password, ", e);
        }
        final UserDetails userDetails = myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        return new ResponseEntity<>(new AuthenticationResponse(jwt), HttpStatus.OK);
    }

    @GetMapping("/blacklist-token")
    public ResponseEntity<?> blacklistToken(HttpServletRequest request) throws Exception {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwt = authorizationHeader.substring(7);
        if (redisUtil.checkBlackListToken(jwt)) {
            return new ResponseEntity<>(new AuthenticationResponse(jwt), HttpStatus.OK);
        }
        redisUtil.pushToken(jwt);
        return new ResponseEntity<>(new AuthenticationResponse(jwt), HttpStatus.OK);
    }
}
