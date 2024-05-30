package com.capitan.chatapp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.capitan.chatapp.dto.GetFriendRequestDto;

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
    private Integer id;

    @Column(unique = true)
    @NotBlank
    @Email(message = "Please provide a valid email address")
    private String username;

    private boolean isFirstLogin;

    @Column(unique = true)

    private String nickname;

    @NotBlank
    private String password;

    private String profileImg;

    private boolean isOnline;

    public UserEntity(int id, String profileImg, String nickname) {
        this.id = id;
        this.nickname = nickname;
        this.profileImg = profileImg;
    }

    public UserEntity(String username) {
        this.username = username;
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "senderEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> friendshipsAsSender = new ArrayList<>();

    @OneToMany(mappedBy = "receiverEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> friendshipsAsReciever = new ArrayList<>();

    public String generateEncodedNickname(String nickNamePrefix) {

        int atIndex = this.username.indexOf('@');
        String usernamePrefix = atIndex != -1 ? username.substring(0, atIndex) : username;

        StringBuilder numericUsernamePrefix = new StringBuilder();
        for (char c : usernamePrefix.toCharArray()) {
            numericUsernamePrefix.append((int) c);
        }

        Random ran = new Random();
        int randomNum = ran.nextInt(1000);

        String encodedNickname = nickNamePrefix + numericUsernamePrefix.toString() + randomNum;

        return encodedNickname;
    }

    /*
     * public class GetFriendRequestDto {
     * private int id;
     * private String profileImg;
     * private String nickname;
     * 
     * @DateTimeFormat(pattern = "MM-dd-yyyy")
     * private LocalDate date;
     * }
     * 
     * 
     * public List<GetFriendRequestDto> getFriendships() {
     * List<FriendDto> friends = new ArrayList<>();
     * for (Friendship friendship : friendshipsAsSender) {
     * friends.add(friendship.getReceiverEntity());
     * }
     * for (Friendship friendship : friendshipsAsReciever) {
     * friends.add(friendship.getSenderEntity());
     * }
     * return friends;
     * }
     */

    public List<GetFriendRequestDto> getFriendRequests() {
        List<GetFriendRequestDto> fRequests = new ArrayList<>();

        for (Friendship friendship : friendshipsAsReciever) {
            if (friendship.getStatus().equals(FriendshiptStatus.PENDING)) {
                UserEntity sender = friendship.getSenderEntity();
                GetFriendRequestDto dto = new GetFriendRequestDto(friendship.getId(), sender.getProfileImg(),
                        sender.getNickname(), friendship.getDate());
                fRequests.add(dto);
            } else {
                continue;
            }

        }
        return fRequests;

    }
}
