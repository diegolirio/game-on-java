package br.com.animalgame.animalgame.config.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Getter
@Setter
@Component
public class GlobalErrorAttr extends DefaultErrorAttributes {
//
//    private int status = HttpStatus.BAD_REQUEST.value();
//    private String message = "please provide a name";

    public GlobalErrorAttr() {
        super(false);
    }
//
//    @Override
//    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
//        Map<String, Object> map = super.getErrorAttributes(request, includeStackTrace);
//        map.put("status", getStatus());
//        map.put("message", getMessage());
//        return map;
//    }
}