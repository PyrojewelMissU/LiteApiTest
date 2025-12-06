# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

API automation testing platform built with Spring Boot 3.2.1, TestNG 7.8.0, and REST Assured 5.4.0. Data-driven test framework that reads test cases from JSON/YAML/Excel/CSV files and executes HTTP requests with validation.

## Build and Run Commands

```bash
# Compile the project
mvn clean compile

# Run all tests (uses testng-modern.xml by default)
mvn clean test

# Run single test class
mvn clean test -Dtest=ApiTestModern

# Start Spring Boot application
mvn spring-boot:run

# Generate and view Allure report
mvn allure:report
allure serve target/allure-results

# Run with specific Spring profile
mvn clean test -Dspring.profiles.active=prod

# Package as JAR (skip tests)
mvn clean package -DskipTests
```

## Architecture

### Test Execution Flow
1. **Configuration Loading**: `ApiConfig` reads `api-config.yml` for root URL, headers, initial parameters
2. **Data Loading**: `DataReaderFactory` auto-detects format (JSON/YAML/Excel/CSV) and loads into `ApiDataBean` objects
3. **Pre-processing**: Execute `preParam` functions (`__random()`, `__date()`) and save to shared pool
4. **Parameter Substitution**: Replace `${param}` with pool values, `__funcName()` with function results
5. **Request Execution**: `RestAssuredUtil` sends HTTP request via REST Assured
6. **Validation**: JsonPath assertions verify response against `verify` field
7. **Data Extraction**: `save` field extracts response values to shared pool for subsequent tests
8. **Database Operations**: Execute `preSql`/`postSql`, validate with `dbVerify`

### Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| `ApiTestModern` | `src/test/java/test/com/sen/api/` | Main test runner (REST Assured + Allure) |
| `TestBase` | `src/test/java/test/com/sen/api/` | Parameter substitution, validation logic |
| `ApiDataBean` | `src/main/java/com/sen/api/beans/` | Test case data model |
| `ApiConfig` | `src/main/java/com/sen/api/configs/` | YAML configuration parser |
| `RestAssuredUtil` | `src/main/java/com/sen/api/utils/` | HTTP request wrapper |
| `FunctionUtil` | `src/main/java/com/sen/api/utils/` | Dynamic function executor (reflection-based) |
| `DataReaderFactory` | `src/main/java/com/sen/api/utils/` | Auto-format detection and data loading |
| `AssertUtil` | `src/main/java/com/sen/api/utils/` | Enhanced assertion with 14+ operators |

### Data Formats

| Format | Location | Reader Class |
|--------|----------|--------------|
| JSON (recommended) | `data/api-test-data.json` | `JsonDataReader` |
| YAML | `data/api-test-data.yml` | `YamlDataReader` |
| CSV | `data/*.csv` | `CsvDataReader` |
| Excel (legacy) | `case/api-data.xls` | `ExcelUtil` |

### ApiDataBean Fields

| Field | Type | Description |
|-------|------|-------------|
| `run` | boolean | Execute this test case |
| `desc` | String | Test description |
| `url` | String | Relative or absolute URL |
| `method` | String | GET, POST, PUT, DELETE, UPLOAD |
| `param` | String | JSON request body |
| `status` | int | Expected HTTP status code |
| `verify` | String | JsonPath assertions: `$.id=1;$.name=test` |
| `save` | String | Extract response: `userId=$.id;email=$.email` |
| `preParam` | String | Pre-generate params: `id=__random(8)` |
| `contains` | boolean | true=substring match, false=exact match |
| `sleep` | int | Seconds to wait before execution |
| `preSql` | String | SQL to execute before test |
| `postSql` | String | SQL to execute after test |
| `dbVerify` | String | Database assertions |
| `tags` | String | Comma-separated tags for filtering: `smoke,P0,user` |
| `group` | String | Test group name |
| `priority` | int | Test priority level |

### Assertion Operators

| Operator | Example | Meaning |
|----------|---------|---------|
| `=` or `==` | `$.code=0` | Equals |
| `!=` or `<>` | `$.code!=1` | Not equals |
| `>` | `$.count>0` | Greater than |
| `>=` | `$.count>=1` | Greater or equal |
| `<` | `$.count<100` | Less than |
| `<=` | `$.count<=50` | Less or equal |
| `~=` | `$.name~=^test.*` | Regex match |
| `:exist` | `$.id:exist` | Field exists |
| `:null` | `$.deleted:null` | Field is null |
| `:notNull` | `$.id:notNull` | Field not null |
| `:empty` | `$.name:empty` | Field is empty |
| `:notEmpty` | `$.name:notEmpty` | Field not empty |
| `:in` | `$.type:in[A,B,C]` | In list |
| `:notIn` | `$.type:notIn[X,Y]` | Not in list |

### Special Syntax

```
${paramName}              - Reference from shared pool
__random(length,numeric)  - Random string: __random(8,true) → "12345678"
__date(format)            - Current date: __date(yyyy-MM-dd) → "2025-12-06"
__md5(text)               - MD5 hash
__base64(text)            - Base64 encode
__aes(plain,key)          - AES encrypt
__rsa(plain,pubkey)       - RSA encrypt
__sha(text,algo)          - SHA hash (SHA1, SHA256, SHA512)
__plus(n1,n2,...)         - Addition
__sub(n1,n2,...)          - Subtraction
__multi(n1,n2,...)        - Multiplication
__max(n1,n2,...)          - Maximum value
__randomText(len)         - Random text with Chinese chars
__randomStrArr(arr,str,num) - Random string array
$.jsonpath                - JsonPath expression in verify/save
```

### Function Classes (14 Total)

Located in `src/main/java/com/sen/api/functions/`:
- `DateFunction`, `RandomFunction`, `RandomTextFunction`, `RandomStrArrFunction`
- `Md5Function`, `Base64Function`, `AesFunction`, `RsaFunction`, `ShaFunction`
- `PlusFunction`, `SubFunction`, `MultiFunction`, `MaxFunction`

## Configuration Files

| File | Purpose |
|------|---------|
| `api-config.yml` | Root URL, headers, initial parameters |
| `testng-modern.xml` | TestNG suite configuration (JSON/YAML + Allure) |
| `src/main/resources/application.yml` | Spring Boot config (port 8080) |
| `src/main/resources/application-{profile}.yml` | Profile-specific config (dev/test/prod) |
| `src/main/resources/logback.xml` | Logging configuration |

## Tech Stack

- **Core**: Java 17, Spring Boot 3.2.1, TestNG 7.8.0
- **HTTP**: REST Assured 5.4.0
- **Data**: Jackson 2.16.1, SnakeYAML 2.2, Apache POI 5.2.5
- **Database**: MyBatis 3.0.3, MySQL 8.2.0
- **Reporting**: Allure 2.25.0
- **Testing**: WireMock 3.3.1, JUnit 5.10.1
- **Quality**: Checkstyle 10.12.5, SpotBugs 4.8.3

## Test Listeners

- `AutoTestListener` - Test retry and deduplication
- `RetryListener` - Configurable retry mechanism (default: 2 retries)

## Exception Hierarchy

```
ApiTestException (base)
├── ConfigurationException (config loading errors)
├── DataReadException (data file reading errors)
├── VerificationException (assertion failures)
└── ErrorRespStatusException (HTTP status code errors)
```

## Adding New Dynamic Functions

1. Create class in `com.sen.api.functions` implementing `Function` interface
2. Class name must end with `Function` (e.g., `MyCustomFunction`)
3. Implement `execute(String[] args)` method
4. Use in test data as `__myCustom(arg1,arg2)`
