---
name: check-arch
description: Use this skill when the user wants to check architecture rules, verify layered architecture compliance, or review code for architecture violations.
disable-model-invocation: false
allowed-tools: Bash, Read, Glob, Grep
argument-hint: [검사할 도메인명 (선택)]
---

# 아키텍처 검사

$ARGUMENTS

## Step 1: 검사 대상 결정

사용자 지정 없으면 전체 도메인: `user`, `project`, `task`, `comment`, `common`, `config`, `global`

## Step 2: 규칙별 검사 실행

### Rule 1: Controller → Repository 직접 호출 금지

```
Grep: "Repository" in domain/*/controller/**/*.java
```

위반: Controller에서 Repository를 직접 import하거나 주입

### Rule 2: 순환 의존 금지 (도메인 간)

```
Grep: "import com.teamdashboard.domain.user" in domain/task/**/*.java
Grep: "import com.teamdashboard.domain.task" in domain/user/**/*.java
```

위반: 도메인 간 양방향 의존이 존재 (단방향만 허용)

### Rule 3: Entity 직접 노출 금지

```
Grep: "@Entity" in domain/*/controller/**/*.java
Grep: "ResponseBody.*Entity" in domain/*/controller/**/*.java
```

위반: Controller가 Entity를 직접 반환 (DTO 사용 필요)

### Rule 4: Service 계층 확인

```
Grep: "@Service" in domain/*/service/**/*.java
```

정상: 비즈니스 로직이 Service 계층에 위치

### Rule 5: 공통 예외 처리 패턴

```
Grep: "try.*catch" in domain/*/controller/**/*.java
```

위반: Controller에서 직접 try-catch (AppException + @RestControllerAdvice 사용 필요)

## Step 3: 결과 출력

```markdown
## 검사 결과

### Controller 규칙
{위반 없음 | 위반 발견}
{위반 시: - `{파일}:{라인}` - {내용}}

### 도메인 순환 의존 규칙
{위반 없음 | 위반 발견}

### Entity 노출 규칙
{위반 없음 | 위반 발견}

### Service 계층 규칙
{위반 없음 | 위반 발견}

### 예외 처리 규칙
{위반 없음 | 위반 발견}

---
총 위반: {n}건
```

## Step 4: 수정 방안 (위반 시)

위반 건별로 수정 방안 제시:
- 어떤 코드를 어떻게 변경해야 하는지
- DTO 추가 필요 여부
- Service 계층 분리 필요 여부
- AppException 전환 필요 여부
