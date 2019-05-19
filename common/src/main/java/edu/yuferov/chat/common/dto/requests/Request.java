package edu.yuferov.chat.common.dto.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    private String path;
    private String session;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "path", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SendMessageRequestParams.class, name = "/room/sendMessage"),
            @JsonSubTypes.Type(value = NamePasswordRequestParams.class, name = "/room/create"),
            @JsonSubTypes.Type(value = NamePasswordRequestParams.class, name = "/room/enter"),
            @JsonSubTypes.Type(value = NamePasswordRequestParams.class, name = "/user/login"),
            @JsonSubTypes.Type(value = NamePasswordRequestParams.class, name = "/user/register"),
            @JsonSubTypes.Type(value = GetMessageRequestParams.class, name = "/room/getMessages")
    })
    private Object params;

}
