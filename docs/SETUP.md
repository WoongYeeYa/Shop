# 로컬 환경 설정

## 필수 도구

- JDK 17 (`JAVA_HOME` 설정)
- Maven 3.9 이상
- MySQL 8
- Tomcat 9

프로젝트는 Java 17로 컴파일한다. 저장소의 `.tools`에 휴대용 JDK/Maven이 있으면 빌드 스크립트가 이를 우선 사용하고, 없으면 PATH에 설정된 도구를 사용한다.

## DB 준비

```sql
CREATE DATABASE my_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'myshop'@'localhost' IDENTIFIED BY 'local-password';
GRANT ALL PRIVILEGES ON my_shop.* TO 'myshop'@'localhost';
```

`src/main/resources/db/schema.sql`을 실행한 후 Tomcat 실행 환경에 다음 값을 설정한다.

- `DB_URL` (기본값 `jdbc:mysql://localhost:3306/my_shop?...`)
- `DB_USERNAME` (기본값 `myshop`)
- `DB_PASSWORD` (기본값 없음, 로컬에서도 환경 변수 사용 권장)

## 실행

```bash
mvn clean test package
```

Windows에서는 실행 정책의 영향을 받지 않는 `scripts\build.cmd`를 권장한다. PowerShell에서는 `powershell -ExecutionPolicy Bypass -File scripts/build.ps1`도 사용할 수 있다.

WAR를 Tomcat 9 `webapps`에 배포한다. 운영 비밀번호와 `.env` 파일은 커밋하지 않는다.
