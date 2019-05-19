package edu.yuferov.chat.common.dto.responses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.yuferov.chat.common.dto.requests.GetMessageRequestParams;
import edu.yuferov.chat.common.dto.requests.NamePasswordRequestParams;
import edu.yuferov.chat.common.dto.requests.SendMessageRequestParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String path;
    private String error;
    private Integer status;
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "path", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SessionIdResponseBody.class, name = "/user/login"),
            @JsonSubTypes.Type(value = SessionIdResponseBody.class, name = "/user/register"),
            @JsonSubTypes.Type(value = MessageListResponseBody.class, name = "/room/getMessages"),
            @JsonSubTypes.Type(value = RoomListResponseBody.class, name = "/room/list"),
            @JsonSubTypes.Type(value = UserListResponseBody.class, name = "/room/getUsers")
    })
    private Object data;

    public boolean isError() {
        return error != null;
    }
}
