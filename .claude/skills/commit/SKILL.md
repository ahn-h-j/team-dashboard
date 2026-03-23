---
name: commit
description: Use this skill when the user wants to commit changes. Backend and frontend are committed separately.
disable-model-invocation: false
allowed-tools: Bash, Read, Glob, Grep
argument-hint: [커밋 메시지 힌트 (선택)]
---

# 커밋 생성

$ARGUMENTS

## 필수 규칙

1. **계층별 분리 커밋**: entity → repository/service → controller 순서
2. **프론트엔드 별도 커밋**: frontend/ 변경은 반드시 분리
3. **Co-Authored-By 금지**

## Step 1: 변경사항 분석

```bash
git status
git diff --stat
```

파일을 아래 기준으로 분류:

| 경로 패턴 | 계층 |
|----------|------|
| `*/domain/*/entity/**` | entity |
| `*/domain/*/repository/**` | repository |
| `*/domain/*/service/**` | service |
| `*/domain/*/controller/**` | controller |
| `*/domain/*/dto/**` | dto |
| `*/config/**`, `*/common/**`, `*/global/**` | config |
| `frontend/**` | frontend |
| `docs/**` | docs |
| `**/test/**` | test |

## Step 2: 커밋 순서

1. docs (문서)
2. config (설정, 공통)
3. entity (엔티티, 도메인 모델)
4. repository (데이터 접근)
5. service (비즈니스 로직)
6. dto + controller (API 계층)
7. test (테스트)
8. frontend (별도 커밋)

## Step 3: 커밋 메시지 형식

```
<type>(<context>): <subject>

- 변경 내용 1
- 변경 내용 2
```

### Type
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `docs`: 문서
- `test`: 테스트
- `chore`: 설정

### Context (비즈니스 도메인)
- `user`: 사용자, 인증
- `project`: 프로젝트 관리
- `task`: 태스크, 칸반
- `comment`: 코멘트
- `dashboard`: 대시보드, 시각화
- `config`: 설정, 공통

### Subject
- 한글, 50자 이내, 마침표 없음, 명령문

## Step 4: 커밋 실행

```bash
# 스테이징
git add <files>

# 커밋 (HEREDOC)
git commit -m "$(cat <<'EOF'
feat(task): Task 엔티티 및 상태 관리 구현

- Task 엔티티 추가
- TaskStatus enum 정의
EOF
)"
```

## Step 5: 결과 보고

```
## 커밋 완료

1. `abc1234` feat(task): Task 엔티티 및 상태 관리 구현
2. `def5678` feat(task): Task CRUD API 구현
3. `ghi9012` feat(task): 칸반 보드 UI 구현 (frontend)

총 3개 커밋
```

## 예시: 실제 커밋 히스토리

```
feat(user): User 엔티티 및 역할 enum 추가
feat(user): JWT 인증 및 Spring Security 설정 구현
feat(project): 프로젝트 CRUD Service 구현
feat(project): 프로젝트 API 엔드포인트 구현
feat(dashboard): 대시보드 메인 페이지 UI 구현
chore(config): 환경변수 및 DB 설정 추가
```
