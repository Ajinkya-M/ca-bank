# Implementation Plan: MemberService Unit Testing (Refined)

## 1. Overview
This plan outlines the steps to implement unit tests for `MemberService`, including infrastructure setup and test suite development.

## 2. Phase 1: Infrastructure Setup

### 2.1 Update `pom.xml` for JaCoCo
Enable the JaCoCo plugin to track coverage and enforce thresholds.

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>METHOD</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>1.00</minimum>
                            </limit>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 2.2 Test Configuration
Add `src/test/resources/application-test.properties` with:
- `spring.profiles.active=test`
- `spring.main.banner-mode=off` (for faster execution)

## 3. Phase 2: Test Support Layer

### 3.1 `TestDataFactory`
Create a centralized factory in `src/test/java/com/ca_bank/member_service/util/` to provide consistent test objects.
- `createMember()`: Standard John Doe entity.
- `createMemberResponseDTO()`: Standard DTO.
- `createMemberList(int size)`: Generative list utility.

## 4. Phase 3: Unit Test Implementation

### 4.1 `MemberServiceTest`
- Use `@ExtendWith(MockitoExtension.class)`.
- Use `@Mock` for `MemberRepository`.
- Use `@InjectMocks` for `MemberService`.

**Test Cases:**
1. `createMember()`: Verify repository interaction and result.
2. `getAllMembers()`: Verify list size and mapping logic.
3. `getMember()`: Test both `found` (returning DTO) and `not found` (returning null) paths.

## 5. Phase 4: Validation & Cleanup

1. **Execution**: Run `./mvnw clean test`.
2. **Coverage**: Run `./mvnw jacoco:report`.
3. **Audit**: Verify `target/site/jacoco/index.html` shows 100% method coverage for `MemberService`.

---
**Status**: Ready for Implementation
