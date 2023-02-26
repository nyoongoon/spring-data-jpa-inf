package com.example.datajpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter //실무에선 setter 권장 x
@NoArgsConstructor(access = AccessLevel.PROTECTED)//jpa기본생성자는protected
@ToString(of = {"id", "name"})
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;
    @OneToMany(mappedBy = "team") //연관관계시 한쪽에 mappedBy 설정 -> 외래키 없는 쪽에 거는 것이 좋다.
    private List<Member> members = new ArrayList<>();

    public Team(String name){
        this.name = name;
    }
}
