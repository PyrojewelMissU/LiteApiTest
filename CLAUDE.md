# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an API automation testing platform built with Spring Boot 3.2.1, TestNG, and REST Assured. It's a data-driven test framework that reads test cases from JSON/YAML/Excel files and executes HTTP requests with validation.

## Build and Run Commands

```bash
# Compile the project
mvn clean compile

# Run all tests (uses testng-modern.xml by default)
mvn clean test

# Run tests with traditional TestNG config (Excel data source)
mvn clean test -DxmlFileName=testng.xml

# Run tests with specific data format
mvn clean test -DxmlFileName=testng-modern.xml

# Start Spring Boot application (for development)
mvn spring-boot:run

# Generate Allure report after test run
allure serve target/allure-results
```

## Architecture

### Test Execution Flow
1. **Configuration Loading**: `ApiConfig` reads `api-config.xml` for root URL, headers, and initial parameters
2. **Data Loading**: Test data loaded from JSON/YAML/Excel via `DataReaderFactory` into `ApiDataBean` objects
3. **Test Execution**: `ApiTestModern` (or legacy `ApiTest`) executes each test case using REST Assured
4. **Parameter Substitution**: `${param}` placeholders replaced with values from shared data pool, `__funcName()` replaced with function results
5. **Response Validation**: JsonPath-based assertions verify response data
6. **Data Extraction**: `save` field extracts response values to shared pool for subsequent tests

### Key Classes

- `test.com.sen.api.ApiTestModern` - Modern test runner using REST Assured + Allure
- `test.com.sen.api.TestBase` - Base class with parameter substitution and validation logic
- `com.sen.api.beans.ApiDataBean` - Test case data model with fields: run, method, url, param, verify, save
- `com.sen.api.configs.ApiConfig` - XML configuration parser for root URL and headers
- `com.sen.api.utils.RestAssuredUtil` - HTTP request execution wrapper
- `com.sen.api.utils.FunctionUtil` - Dynamic function execution for test data generation

### Data Formats

Test data can be in:
- **JSON**: `data/api-test-data.json` (recommended)
- **YAML**: `data/api-test-data.yml`
- **Excel**: `case/api-data.xls` (legacy)

### Special Syntax in Test Data

- `${paramName}` - Reference saved parameter from previous test or config
- `__random(length,numericOnly)` - Generate random string
- `__date(format)` - Generate formatted date string
- `__bodyfile(path)` - Reference file for upload
- `$.jsonpath` - JsonPath expressions in verify/save fields

## Configuration Files

- `api-config.xml` - API root URL, headers, initial parameters
- `testng-modern.xml` - Modern TestNG suite with JSON/Allure
- `testng.xml` - Legacy TestNG suite with Excel/ExtentReports
- `src/main/resources/application.yml` - Spring Boot config (port 8080)

## Tech Stack

- Java 17, Spring Boot 3.2.1
- TestNG 7.8.0
- REST Assured 5.4.0
- Jackson 2.16.1 / FastJSON (legacy)
- Allure Report 2.25.0 / ExtentReports (legacy)
- MyBatis 3.0.3, MySQL 8.2.0
