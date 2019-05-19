package edu.yuferov.chat.common.dto.responses;

import edu.yuferov.chat.common.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponseBody {
    private List<MessageDto> messages;
}
