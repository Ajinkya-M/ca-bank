---
description: Use spec-kit to develop a new feature
---

# Using Spec-Kit for Feature Development

This workflow guides you through using spec-kit to develop a new feature from specification to implementation.

## Prerequisites
- Spec-kit is initialized (`.specify/` directory exists)
- Constitution has been reviewed

## Steps

### 1. Review the Constitution
Before starting any new feature, review the project principles:
```
Open: .specify/memory/constitution.md
```
Familiarize yourself with:
- Code quality standards
- Security requirements
- Testing expectations
- Performance targets

### 2. Create a Feature Specification
Ask to create a detailed specification for your feature:
```
"Create a spec for [describe your feature in detail]"
```

**Example**:
```
"Create a spec for JWT-based authentication that allows members to login 
and receive a token for subsequent API requests"
```

This will:
- Use the `speckit_specify` tool
- Create `.specify/specs/[feature-name]/spec.md`
- Define requirements, acceptance criteria, API contracts, and data models

### 3. Review the Specification
- Open the generated spec file
- Verify all requirements are captured
- Check acceptance criteria are correct
- Ensure API contracts match expectations
- Request modifications if needed

### 4. Generate Technical Plan
Once the spec is approved, create the technical implementation plan:
```
"Create a technical plan for [feature-name]"
```

This will:
- Use the `speckit_plan` tool
- Analyze current architecture
- Propose implementation approach
- Identify database/API changes
- Define testing strategy
- Create `.specify/specs/[feature-name]/plan.md`

### 5. Review the Technical Plan
- Open the generated plan file
- Verify the technical approach is sound
- Check database schema changes
- Review API modifications
- Assess risks and dependencies
- Request adjustments if needed

### 6. Generate Task Breakdown
Break down the plan into actionable tasks:
```
"Generate tasks for [feature-name]"
```

This will:
- Use the `speckit_tasks` tool
- Create granular, actionable tasks
- Organize by phase and type
- Estimate effort
- Create `.specify/specs/[feature-name]/tasks.md`

### 7. Review Tasks
- Open the generated tasks file
- Verify task breakdown is complete
- Check dependencies are identified
- Adjust estimates if needed
- Prioritize tasks

### 8. Implement the Feature
Execute the implementation:
```
"Implement [feature-name]"
```

This will:
- Use the `speckit_implement` tool
- Create/modify code files
- Follow the technical plan
- Adhere to constitution principles
- Generate tests

### 9. Track Progress
As you work through implementation:
- Update task statuses in `tasks.md`
- Mark completed items with `[x]`
- Document decisions and blockers
- Note any deviations from the plan

### 10. Verify Completion
Before marking the feature as done, verify:
- [ ] All code follows project standards
- [ ] Unit tests written (80%+ coverage)
- [ ] Integration tests passed
- [ ] Code reviewed
- [ ] API documentation updated
- [ ] No security vulnerabilities
- [ ] Performance benchmarks met
- [ ] Tested in staging environment

## Example: Complete Flow

```
User: "I need to add JWT authentication to the member service"

Step 1: Review constitution
→ Opens .specify/memory/constitution.md

Step 2: Create specification
User: "Create a spec for JWT authentication with login endpoint returning tokens"
→ Creates .specify/specs/jwt-authentication/spec.md

Step 3: Review spec
→ Opens and reviews the specification document

Step 4: Create plan
User: "Create a technical plan for JWT authentication"
→ Creates .specify/specs/jwt-authentication/plan.md

Step 5: Review plan
→ Opens and reviews the technical plan

Step 6: Generate tasks
User: "Generate tasks for JWT authentication"
→ Creates .specify/specs/jwt-authentication/tasks.md

Step 7: Review tasks
→ Opens and reviews task breakdown

Step 8: Implement
User: "Implement JWT authentication"
→ Creates JwtUtil.java, SecurityConfig.java, updates controller, adds tests

Step 9: Track progress
→ Updates tasks.md as work progresses

Step 10: Verify completion
→ Checks all definition of done criteria
```

## Tips

- **Be Specific**: The more detail you provide in step 2, the better the outputs
- **Iterate**: Don't hesitate to ask for modifications at any step
- **Stay Aligned**: Refer back to the constitution during implementation
- **Document**: Record important decisions in the appropriate documents
- **Update**: Keep specs/plans current if requirements change

## Quick Reference

For a quick reference guide, see: `.specify/QUICK_REFERENCE.md`
