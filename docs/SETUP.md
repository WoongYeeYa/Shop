# 로컬 환경 설정

## 필수 도구

- JDK 8 이상 (`JAVA_HOME` 설정)
- Maven 3.9 이상
- MySQL 8
- Tomcat 9

현재 확인된 로컬 환경에는 Java 8만 있고 Maven과 Git은 PATH에 없다.

## DB 준비

```sql
CREATE DATABASE morrow_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'morrow'@'localhost' IDENTIFIED BY 'local-password';
GRANT ALL PRIVILEGES ON morrow_shop.* TO 'morrow'@'localhost';
```

`src/main/resources/db/schema.sql`을 실행한 후 Tomcat 실행 환경에 다음 값을 설정한다.

- `DB_URL` (기본값 `jdbc:mysql://localhost:3306/morrow_shop?...`)
- `DB_USERNAME` (기본값 `morrow`)
- `DB_PASSWORD` (기본값 없음, 로컬에서도 환경 변수 사용 권장)

## 실행

```bash
mvn clean test package
```

Windows에서는 `powershell -ExecutionPolicy Bypass -File scripts/build.ps1`로도 빌드할 수 있다. 프로젝트 내부 휴대용 도구가 있으면 우선 사용하고, 없으면 PATH의 Maven을 사용한다.

WAR를 Tomcat 9 `webapps`에 배포한다. 운영 비밀번호와 `.env` 파일은 커밋하지 않는다.
