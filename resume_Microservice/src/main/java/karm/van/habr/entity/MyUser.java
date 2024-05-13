package karm.van.habr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String password;

    private String email;

    private String role;

    private String firstname;

    private String lastname;

    @Column(columnDefinition = "text")
    private String description;

    private String country;

    private String roleInCommand;

    private String skills;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    private List<Resume> resumes;

    @Lob
    private byte[] profileImage;

    private String imageType;

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private Settings settings;
}
