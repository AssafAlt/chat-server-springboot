package com.capitan.chatapp.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friend_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "sender_id", "receiver_id" })
})
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @DateTimeFormat(pattern = "MM-dd-yyyy")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity senderEntity;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiverEntity;

    private String status;

    public FriendRequest(int id, LocalDate date, String status) {
        this.id = id;
        this.date = date;
        this.status = status;
    }

}
