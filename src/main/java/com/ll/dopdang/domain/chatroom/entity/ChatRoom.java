package com.ll.dopdang.domain.chatroom.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
	@Id
	@Column(unique = true)
	private String roomId;

	private String client;
	private String expert;

	private LocalDateTime lastReadAtUser1;
	private LocalDateTime lastReadAtUser2;

	@OneToMany(mappedBy = "chatRoom")
	@JsonIgnore
	private List<ChatMessage> messages;
}
