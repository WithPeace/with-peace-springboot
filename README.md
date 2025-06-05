# 🌿 청하: 청년 정책 정보 제공 및 커뮤니티 플랫폼  
청하는 청년들이 다양한 청년 정책 정보를 쉽게 찾고, 공유할 수 있도록 만든 플랫폼입니다.  
사용자는 맞춤형 정책 추천을 받을 수 있으며, 커뮤니티 기능을 통해 정보를 공유하고 의견을 나눌 수 있습니다.  

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
