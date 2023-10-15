package com.cydeo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)//if is null field don't  show
public class ResponseWrapper {

    private boolean success;
    private String message;
    private Integer code;
    private Object data;

    public ResponseWrapper(String message, Object data,HttpStatus httpStatus) {
        this.success = true;
        this.message = message;
        this.code = httpStatus.value();
        this.data = data;// when we delete this field is Null in Json, that why we  put @JsonInclude.Include.NON_NULL
    }

    public ResponseWrapper(String message,HttpStatus httpStatus) {
        this.message = message;
        this.code = httpStatus.value();
        this.success = true;
    }

}
