package org.jejuro.miraero.domain.aicoach.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;
import org.jejuro.miraero.domain.aicoach.service.AiCoachConversationService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-coach/conversations")
@Api(tags = "AI 코치 - 대화방")
public class AiCoachConversationController {

    private final AiCoachConversationService aiCoachConversationService;

    @GetMapping("/latest")
    @ApiOperation(value = "가장 최근 AI 코치 대화 조회", notes = "로그인 사용자의 최근 대화를 조회합니다. 대화가 없으면 성공 응답의 data가 null입니다.")
    public ResponseEntity<ApiResponse<AiCoachConversationResponse>> getLatestConversation(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachConversationResponse response =
                aiCoachConversationService.getLatestConversation(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @ApiOperation(value = "AI 코치 대화 생성", notes = "빈 대화를 새로 생성합니다. 첫 질문을 전송하면 AI가 생성한 제목 또는 질문 내용으로 대화 제목이 갱신됩니다.")
    public ResponseEntity<ApiResponse<AiCoachConversationCreateResponse>> createConversation(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        AiCoachConversationCreateResponse response =
                aiCoachConversationService.createConversation(user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @ApiOperation(value = "AI 코치 대화 목록 조회", notes = "로그인 사용자의 AI 코치 대화 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AiCoachConversationResponse>>> getConversations(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<AiCoachConversationResponse> responses =
                aiCoachConversationService.getConversations(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{conversationId}")
    @ApiOperation(value = "AI 코치 대화 삭제", notes = "로그인 사용자가 소유한 대화와 해당 메시지를 삭제합니다. 성공 시 data는 null입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @ApiParam(value = "AI 코치 대화 ID", example = "1", required = true) @PathVariable Long conversationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        aiCoachConversationService.deleteConversation(user.getUserId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
