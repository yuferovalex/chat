package edu.yuferov.chat.common.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NamePasswordRequestParams {
    private String name;
    private String password;
}
