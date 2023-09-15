package com.github.commerce.web.controller.chat;

import com.github.commerce.entity.collection.Chat;
import com.github.commerce.repository.user.UserDetailsImpl;
import com.github.commerce.service.chat.ChatService;
import com.github.commerce.web.dto.chat.ChatDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "채팅 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/api/chat")
@RestController
public class ChatController {
    private final ChatService chatService;

    @ApiOperation(value = "유저의 해당 판매자와 대화했던 채팅방 목록 조회, 로그인필요")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/user/{sellerId}")
    public ResponseEntity<Map<String, Object>> getUserChats(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("sellerId") Long sellerId

    ){
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(chatService.getUserChatList(userId, sellerId));
    }

    @ApiOperation(value = "판매자가 대화했던 채팅방 목록 조회, 로그인필요")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/seller/{sellerId}/{productId}")
    public ResponseEntity<Map<String, Object>> getSellerChats(
            //@AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("sellerId") Long sellerId,
            @PathVariable("productId") Long productId

    ){
        return ResponseEntity.ok(chatService.getSellerChatList(sellerId, productId));
    }

    @ApiOperation(value = "채팅방 채팅내역 상세조회, 로그인필요")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    @CrossOrigin(origins = "*")
    @GetMapping("/detail/{customRoomId}")
    public ResponseEntity<ChatDto> getChatRoom(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String customRoomId
    ){

        return ResponseEntity.ok(chatService.getChatRoom(customRoomId));
    }
}
