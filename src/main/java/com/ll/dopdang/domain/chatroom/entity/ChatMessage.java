package com.ll.dopdang.domain.chatroom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String roomId;
	private String sender;
	private String receiver;
	private String content;
	private LocalDateTime timestamp;

	@Column(columnDefinition = "TEXT")
	private String fileUrl;
}
