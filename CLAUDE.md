# Team Dashboard SaaS

## 프로젝트 개요
팀 프로젝트/태스크 관리 대시보드. 실시간 상태 추적, 팀원별 워크로드 시각화, 칸반 보드 제공.

## 기술 스택
- **Frontend**: React 18 + TypeScript + Tailwind CSS + Vite
- **Backend**: Spring Boot 3 + Java 17 + Gradle
- **Database**: MySQL + Spring Data JPA
- **Auth**: Spring Security + JWT
- **Testing**: JUnit 5 (unit) + Playwright (E2E)

## 디렉토리 구조
```
├── frontend/          # React 프론트엔드
│   └── src/
│       ├── components/  # UI 컴포넌트
│       ├── hooks/       # 커스텀 훅
│       ├── pages/       # 라우트별 페이지
│       └── lib/         # 유틸리티
├── backend/           # Spring Boot 백엔드
│   └── src/main/java/com/teamdashboard/
│       ├── config/      # Security, JWT 설정
│       ├── common/      # 공통 응답, 예외
│       ├── domain/      # 엔티티별 패키지 (user, project, task, comment)
│       └── global/      # 글로벌 예외 핸들러
```

## 컨벤션
- 함수명: camelCase
- 컴포넌트: PascalCase
- API 응답: `{ success: boolean, data?: T, error?: string }`
- 에러 처리: @RestControllerAdvice + 커스텀 AppException
- 커밋: conventional commits (feat:, fix:, refactor:)

## 테스트 명령어
- `cd backend && gradle test` — JUnit 유닛 테스트
- `cd frontend && npm run test` — Vitest 유닛 테스트
- `npx playwright test` — Playwright E2E
- `cd frontend && npm run lint` — ESLint + Prettier 체크

## 작업 규칙
- 기능 구현 완료 후 반드시 code-tutor 서브에이전트를 실행하여 학습 가이드를 제공한다

## 주요 엔티티
- User (id, email, name, role, avatar)
- Project (id, name, description, ownerId)
- Task (id, title, status, priority, assigneeId, projectId)
- Comment (id, content, authorId, taskId)
