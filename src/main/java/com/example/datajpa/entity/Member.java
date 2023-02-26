package com.example.datajpa.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter //실무에선 setter 권장 x
@NoArgsConstructor(access = AccessLevel.PROTECTED)//jpa기본생성자는protected
@ToString(of = {"id", "userName", "age"}) //연관관계 필드는 toString 안하는 게 좋음(무한루프주의)
public class Member {
    @Id @GeneratedValue
    @Column(name="member_id") //실무에서 이렇게 하면 join하기 좋음
    private Long id;
    private String userName;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY) //일대다 관계(Member가 1) //ManyToOne은 Lazy로 수정
    @JoinColumn(name="team_id")
    private Team team;
    public Member(String userName) {
        this.userName = userName;
    }

    public Member(String userName, int age, Team team) {
        this.userName = userName;
        this.age = age;
        //this.team = team; team 객체에서도 연관관계 바꾸어 주어야하므로
        if(team != null){
            changeTeam(team);
        }
    }

    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }
}
