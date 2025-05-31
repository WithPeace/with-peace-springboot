# 🌿 청하: 청년 정책 정보 제공 및 커뮤니티 플랫폼  
청하는 청년들이 겪는 정책 정보 접근의 어려움을 해결하기 위해 기획된 서비스입니다.  
**"청년에게 도움이 되는 서비스란 무엇일까?"** 라는 근본적인 질문에서 시작해,  
팀원 모두가 초기 기획 단계부터 머리를 맞대고 서비스의 구조와 기능을 함께 설계했습니다.

1차 배포 이후, 단순한 정보 제공을 넘어서 다음과 같은 기능들을 추가하며 발전했습니다.
- 관심 지역 및 분야 맞춤 설정
- 사용자 행동 기반 추천 시스템
- 일일 밸런스 게임을 통한 의견 교류

이를 통해 현실에서 청년들이 정책 정보를 찾는 데 겪는 어려움을 해소하고,  
정책을 직관적으로 탐색하고 개인에게 맞는 정책을 추천받을 수 있는 플랫폼으로 완성했습니다.

기술적 구현을 넘어, **실제 사용자에게 의미 있고 유용한 경험을 제공하는 정책 플랫폼**을 만드는 것이 이 프로젝트의 궁극적인 목표입니다.

<br>

## 🔗 관련 링크

| 구분 | 링크 |
|------|------|
| 🖥 ERD | https://dbdiagram.io/d/청하ERD-6835e7ca6980ade2ebb20ee5 |
| 📚 Swagger 문서 | https://staging.cheongha.xyz/swagger-ui/index.html#/ |

<br>

## 🛠 기술 스택  
| 구분                 | 기술 & 도구                                                          |
| -------------------- | -------------------------------------------------------------------- |
| Language             | Java 17                                                              |
| Framework            | Spring Boot 3.2.2                                                    |
| Database             | MySQL 8.0.42, Redis                                                  |
| API & Doc            | RESTful API, Swagger                                                 |
| Infra                | Google Cloud Platform(Compute Engine, Cloud Storage), Nginx, Docker  |
| CI/CD                | GitHub Actions                                                       |
| Monitoring & Testing | Prometheus, Grafana, InfluxDB, K6                                    |
| Collaboration Tools  | GitHub, Notion, Slack                                                |
| Development Tools    | IntelliJ IDEA, DataGrip, Postman                                     |

<br>

## 🏗 프로젝트 아키텍처

### 시스템 아키텍처
<img src="https://github.com/user-attachments/assets/96eb5af0-12fd-43dd-a4ea-01bb03c4b484" width="800"/>


### 디렉터리 구조
```
com.example.withpeace           // 프로젝트의 루트 패키지
├── controller                  // REST API 컨트롤러 계층
├── service                     // 비즈니스 로직 계층
├── repository                  // 데이터 접근 계층 (DAO)
├── domain                      // 엔티티 클래스 및 데이터 모델
├── dto                         // 데이터 전송 객체 (DTO)
│   ├── request                 // API 요청에 사용되는 DTO 계층
│   └── response                // API 응답에 사용되는 DTO 계층
│
└── resources                         // 설정 파일 및 기타 리소스
    ├── application.yml               // 기본 환경 설정
    ├── application-local.yml         // 로컬 환경 설정
    ├── application-dev.yml           // 개발 환경 설정
    └── application-staging-test.yml  // 스테이징 환경 설정

// 기타 주요 파일 및 디렉토리
├── build.gradle              // Gradle 빌드 설정 파일
├── Dockerfile                // Docker 이미지 빌드 설정
└── .github/workflows         // CI/CD 설정 파일이 포함된 디렉터리
    └── cicd.yml              // GitHub Actions 워크플로우
```

<br>
