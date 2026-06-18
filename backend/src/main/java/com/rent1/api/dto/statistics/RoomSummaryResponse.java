package com.rent1.api.dto.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomSummaryResponse {
    private long total;
    private long vacant;
    private long occupied;
    private long expiringSoon;
}
