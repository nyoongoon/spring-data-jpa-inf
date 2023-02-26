# 프로젝트 전 설정
- gradle로 테스트 아닌 intelij로 테스트 하기 설정
- 롬복 설정 - 어노테이션 프로세서스 설정
## 쿼리파라미터 로그 남기기
- implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.7'
- 운영에서 쓰려면 성능 고려해야함
```
insert into member (user_name, id) values (?, ?)
insert into member (user_name, id) values ('memberA', 1);
```

# JPA 인텔리제이로 잘 보이게 설정
- file-projectStructure  - facets 


# application.yml
logging.level:
org.hibernate.SQL: debug # sql쿼리 로그로 남기기

# JPA 주의사항
- @Transactional : JPA의 모든 데이터 변경사항은 트랜잭션 안에서 이루어져야함 


# 엔티티 예시
```java
@Entity
@Getter @Setter //실무에선 setter 권장 x
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String userName;

    protected Member() { //엔티티는 기본생성자 있어야함!!!

    }

    public Member(String userName) {
        this.userName = userName;
    }
}
```

# 기존 JPA 예시
```java
@Repository
public class MemberJpaRepository {
    @PersistenceContext //스프링컨테이너가 JPA에 있는 영속성 컨텍스트 (엔티티매니저)를 주입해 줌
    private EntityManager em;

    public Member save(Member member){
        em.persist(member);
        return member;
    }

    public Member find(Long id){
        return em.find(Member.class, id);
    }
}
```

# 테스트 예시
```java
@Transactional
@SpringBootTest
@Rollback(false) // 스프링 테스트가 트랜잭션 롤백을 자동으로 함. 보고싶으면 false
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Test
    void testMember(){
        Member member = new Member("memberA");
        Member savedMember= memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(savedMember.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUserName()).isEqualTo(member.getUserName());
        assertThat(findMember).isEqualTo(member); //JPA에서 제공하는 1차 캐시 때문에 같은 인스턴스임
    }
}
```

# 스프링 데이터 JPA Repository 예시
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
```
# 엔티티 연관관계 매핑 
## 일대다 관계
### 다
```java
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
```
### 일 (외래키가 없는 쪽에 mappedBy 권장!)
```java
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
```


# 연관관계
- JPA에서 모든 연관관계는 Lazy로 셋팅을 해야한다.
- 기본값인 EAGER는 성능 최적화하기 힘들어서 실무에서 X
## 지연로딩
- 조회대상 엔티티만 먼저 조회, 연관관계 엔티티는 가짜객체로 갖고 있음
- 가짜객체를 실제 사용할 때, 그때 관련 쿼리를 날림. 

# 스프링 데이터 JPA 공통 인터페이스 설정
- interface 작성해두면 스프링 데이터 JPA가 프록시 객체를 생성
- @Repository 없어도 가능 컴포턴트 스캔 스프링 데이터 JPA가 자동으로
- JPA예외를 스프링 예외로 변환하는 과정도 자동처리

# 쿼리메소드
- 스프링 데이터 JPA가 메소드 이름을 분석해서 JPQL을 생성하고 실행
- 쿼리 메소드 필터 조건 따라야함.

## JPA namedQuery
- 실무에서 잘 안쓰임
- 애플리케이션 로딩시점에 파싱해서 에러 잡아주는 장점은 있음

## @Query, 리포지토리에 쿼리 정의하기
- @Query도 애플리케이션 로딩시점에 파싱해서 에러 잡아줌
```
@Query("select m from Member m where m.username = :username and m.age= :age")
List<Member> findUser(@Param("username") String username, @Param("age") int age);
```

### @Query 값, DTO 조회하기
#### 단순한 값 하나를 조회
```
@Query("select m.username from Member m")
List<String> findUsernameList();
```
#### DTO로 직접 조회
- DTO 생성
```java
@Data //엔티티인경우는 지양 DTO는 ㄱㅊㄱㅊ
@AllArgsConstructor
public class MemberDto{
    private Long id;
    private String name;
    private String teamName;
}
```
- Repository에서 @Query사용 (new 키워드, 패키지 경로 사용 주의 !!!)
```
@Query(select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```