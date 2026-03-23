---
name: init
description: Use this skill when the user wants to start working, begin implementation, or says things like 시작하자, 구현 시작, 작업 시작, let's start, begin coding.
disable-model-invocation: false
allowed-tools: Bash, Read, Glob, Grep
---

# 작업 시작

## Step 1: 현재 상태 확인

```bash
git branch --show-current
```

```bash
git status --short
```

## Step 2: 핵심 문서 로드

아래 파일들을 Read로 읽기:

1. `CLAUDE.md` - 프로젝트 개요 및 컨벤션

존재하면 추가로 읽기:
2. `docs/biz-logic.md` - 비즈니스 규칙
3. `docs/architecture.md` - 아키텍처
4. `docs/convention.md` - 코딩 컨벤션

## Step 3: 브랜치명에서 도메인 추출

| 브랜치 패턴 | 관련 도메인 | 추가 확인 |
|------------|------------|----------|
| `*user*`, `*auth*` | user | 인증, 사용자 관리 |
| `*project*` | project | 프로젝트 CRUD |
| `*task*`, `*kanban*` | task | 태스크, 칸반 보드 |
| `*comment*` | comment | 코멘트 기능 |
| `*dashboard*` | dashboard | 대시보드, 시각화 |

## Step 4: 브리핑 출력

```markdown
## 프로젝트 컨텍스트

**브랜치**: {브랜치명}
**관련 도메인**: {도메인}

### 핵심 비즈니스 규칙
- {해당 도메인의 주요 규칙 3-5개}

### 아키텍처 주의사항
- Controller → Service → Repository 계층 구조 준수
- Entity 직접 노출 금지 (DTO 변환 필요)
- AppException + @RestControllerAdvice로 예외 처리

### 준비 완료. 무엇을 구현할까요?
```
