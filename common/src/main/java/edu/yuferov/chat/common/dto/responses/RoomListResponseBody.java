package edu.yuferov.chat.common.dto.responses;

import edu.yuferov.chat.common.dto.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponseBody {
    private List<RoomDto> rooms;
}
