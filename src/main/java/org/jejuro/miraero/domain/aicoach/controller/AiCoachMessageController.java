package org.jejuro.miraero.domain.aicoach.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachMessageService;
import org.jejuro.miraero.domain.aicoach.service.AiCoachStreamingService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-coach/conversations")
@Api(tags = "AI 코치 - 메시지")
public class AiCoachMessageController {

    private final AiCoachMessageService aiCoachMessageService;
    private final AiCoachStreamingService aiCoachStreamingService;

    @GetMapping("/{conversationId}/messages")
    @ApiOperation(value = "AI 코치 메시지 목록 조회", notes = "로그인 사용자가 소유한 대화의 메시지를 조회합니다. senderType이 USER면 사용자 메시지, ASSISTANT면 AI 응답입니다.")
    public ResponseEntity<ApiResponse<List<AiCoachMessageResponse>>> getMessages(
            @ApiParam(value = "AI 코치 대화 ID", example = "1", required = true) @PathVariable Long conversationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<AiCoachMessageResponse> responses = aiCoachMessageService.getMessages(
                user.getUserId(),
                conversationId
        );
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{conversationId}/messages")
    @ApiOperation(value = "AI 코치에게 질문 전송", notes = "사용자 질문을 저장한 뒤 금융 현황을 반영해 AI 응답을 생성합니다. 응답은 생성된 ASSISTANT 메시지입니다. AI 응답 생성에 실패해도 사용자 질문은 이미 저장될 수 있으므로, 실패 시 메시지 목록을 다시 조회해 상태를 동기화하세요.")
    public ResponseEntity<ApiResponse<AiCoachMessageResponse>> sendQuestion(
            @ApiParam(value = "AI 코치 대화 ID", example = "1", required = true) @PathVariable Long conversationId,
            @Valid @RequestBody AiCoachMessageCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachMessageResponse response = aiCoachMessageService.sendQuestion(
                user.getUserId(),
                conversationId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping(value = "/{conversationId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation(value = "AI 코치 질문 스트리밍 전송", notes = "답변 토큰을 SSE로 전송합니다. 이벤트는 started, delta, completed, error입니다. completed 이벤트의 message에 저장된 ASSISTANT 메시지가 포함됩니다.")
    public ResponseEntity<SseEmitter> streamQuestion(
            @ApiParam(value = "AI 코치 대화 ID", example = "1", required = true) @PathVariable Long conversationId,
            @Valid @RequestBody AiCoachMessageCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-transform");
        headers.add("X-Accel-Buffering", "no");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(aiCoachStreamingService.streamQuestion(user.getUserId(), conversationId, request));
    }
}
