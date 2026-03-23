package com.teamdashboard.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teamdashboard.domain.comment.Comment;
import com.teamdashboard.domain.project.Project;
import com.teamdashboard.domain.task.Task;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    private String avatar;

    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }
}
