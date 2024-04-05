package com.capitan.chatapp.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    private String roomName;

    @ManyToMany
    @JoinTable(name = "conversation_participants", joinColumns = @JoinColumn(name = "conversation_room_name"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<UserEntity> participants = new ArrayList<>();
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    public Conversation(String roomName) {
        this.roomName = roomName;

    }
}
