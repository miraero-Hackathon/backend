package org.jejuro.miraero.domain.mydata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "금융기관 마이데이터 연결 정보")
public class MyDataConnectionResponse {

  @ApiModelProperty(value = "연결 정보 ID", example = "1")
  private Long connectionId;
  @ApiModelProperty(value = "금융기관명", example = "국민은행")
  private String institutionName;
  @ApiModelProperty(value = "연결 상태", example = "CONNECTED")
  private String connectionStatus;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @ApiModelProperty(value = "동의 일시(yyyy-MM-dd'T'HH:mm:ss)", example = "2026-08-12T10:30:00")
  private LocalDateTime agreedAt;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @ApiModelProperty(value = "연결 또는 동의 만료 일시(yyyy-MM-dd'T'HH:mm:ss)")
  private LocalDateTime expiresAt;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @ApiModelProperty(value = "마지막 동기화 일시(yyyy-MM-dd'T'HH:mm:ss)")
  private LocalDateTime lastSyncedAt;
  @ApiModelProperty(value = "동기화 상태", example = "SUCCESS")
  private String syncStatus;
}
