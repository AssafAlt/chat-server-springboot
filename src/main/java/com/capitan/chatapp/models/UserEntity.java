package com.capitan.chatapp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    @NotBlank
    @Email(message = "Please provide a valid email address")
    private String username;

    @Column(unique = true)

    private String nickname;

    @NotBlank
    private String password;

    private String profileImg;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "senderEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendRequest> sentFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiverEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendRequest> receivedFriendRequests = new ArrayList<>();

    public String generateEncodedNickname(String nickNamePrefix) {
        // Extract the prefix of the username until the '@' sign
        int atIndex = this.username.indexOf('@');
        String usernamePrefix = atIndex != -1 ? username.substring(0, atIndex) : username;

        // Convert the username prefix to numbers using ASCII values
        StringBuilder numericUsernamePrefix = new StringBuilder();
        for (char c : usernamePrefix.toCharArray()) {
            numericUsernamePrefix.append((int) c);
        }

        Random ran = new Random();
        int randomNum = ran.nextInt(1000);

        // Combine the nickname, numeric username prefix, and a random number
        String encodedNickname = nickNamePrefix + numericUsernamePrefix.toString() + randomNum;

        return encodedNickname;
    }
}
