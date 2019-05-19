package edu.yuferov.chat.common.dto.responses;

import edu.yuferov.chat.common.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponseBody {
    private List<UserDto> users;
}
