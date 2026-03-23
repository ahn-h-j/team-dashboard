---
name: test
description: Use this skill when the user wants to run tests, execute test suite, check if tests pass, or verify code changes with tests.
disable-model-invocation: false
allowed-tools: Bash, Read, Glob, Grep
argument-hint: [테스트 클래스명 또는 모듈 (선택)]
---

# 테스트 실행

$ARGUMENTS

## Step 1: 실행 대상 결정

| 사용자 요청 | 명령어 |
|------------|--------|
| 백엔드 전체 / 지정 없음 | `cd backend && gradle test` |
| 클래스 지정 | `cd backend && gradle test --tests "{클래스명}"` |
| 패턴 지정 | `cd backend && gradle test --tests "*{패턴}*"` |
| 프론트엔드 전체 | `cd frontend && npm run test` |
| 프론트엔드 특정 파일 | `cd frontend && npx vitest run {파일경로}` |
| E2E 테스트 | `npx playwright test` |
| 린트 검사 | `cd frontend && npm run lint` |

## Step 2: 테스트 실행

해당 명령어 실행.

## Step 3: 결과 분석

### 성공 시
```
테스트 통과: {n}개 테스트 성공
```

### 실패 시
아래 항목 분석 후 보고:

1. **실패 테스트**: 클래스명.메서드명
2. **실패 원인**: assertion 실패 / 예외 / 타임아웃
3. **관련 코드**: `{파일경로}:{라인번호}`
4. **해결 방안**: 코드 수정 제안 또는 테스트 수정 필요 여부

## 테스트 구조 참고

```
backend/src/test/java/com/teamdashboard/
├── domain/
│   ├── user/         # User 관련 테스트
│   ├── project/      # Project 관련 테스트
│   ├── task/         # Task 관련 테스트
│   └── comment/      # Comment 관련 테스트
└── ...

frontend/src/
├── components/__tests__/   # 컴포넌트 테스트
├── hooks/__tests__/        # 훅 테스트
└── ...

e2e/                        # Playwright E2E 테스트
```
