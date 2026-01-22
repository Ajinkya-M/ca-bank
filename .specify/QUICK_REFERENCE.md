# Spec-Kit Quick Reference

## 🚀 Quick Start Commands

### 1. Create a Specification
```
"Create a spec for [feature description]"
```
**Example**: "Create a spec for user authentication with JWT"

### 2. Generate Technical Plan
```
"Create a technical plan for [feature]"
```
**Example**: "Create a technical plan for JWT authentication"

### 3. Generate Task List
```
"Generate tasks for [feature]"
```
**Example**: "Generate tasks for JWT authentication"

### 4. Implement Feature
```
"Implement [feature]"
```
**Example**: "Implement JWT authentication"

---

## 📁 File Locations

| Document Type | Location |
|---------------|----------|
| **Constitution** | `.specify/memory/constitution.md` |
| **Spec Template** | `.specify/templates/spec-template.md` |
| **Plan Template** | `.specify/templates/plan-template.md` |
| **Task Template** | `.specify/templates/task-template.md` |
| **Feature Specs** | `.specify/specs/[feature-name]/` |

---

## 🔄 Workflow at a Glance

```
1. SPECIFY    → Define what to build (spec.md)
   ↓
2. PLAN       → Create technical approach (plan.md)
   ↓
3. TASKS      → Break down into tasks (tasks.md)
   ↓
4. IMPLEMENT  → Execute the implementation
```

---

## 📋 Constitution Highlights

### Code Quality
- Follow SOLID principles
- Maintain 80%+ test coverage
- Write meaningful JavaDoc
- Use clean code practices

### Security
- Encrypt sensitive data
- Validate all inputs
- Implement proper authentication
- Log security events

### Performance
- API responses < 200ms (95th percentile)
- Optimize database queries
- Use caching appropriately
- Handle async operations

### API Design
- Follow REST principles
- Use consistent response format
- Version APIs properly
- Document with Swagger

### Testing
- Unit tests for all services
- Integration tests for APIs
- Load testing for critical paths
- Test data should be realistic

---

## 🎯 Example Feature Development

**Feature**: Add money transfer capability

**Step 1: Specify**
```
User: "Create a spec for money transfer between member accounts"
→ Creates: .specify/specs/money-transfer/spec.md
```

**Step 2: Plan**
```
User: "Create a plan for money transfer"
→ Creates: .specify/specs/money-transfer/plan.md
```

**Step 3: Tasks**
```
User: "Generate tasks for money transfer"
→ Creates: .specify/specs/money-transfer/tasks.md
```

**Step 4: Implement**
```
User: "Implement the money transfer feature"
→ Creates code files, tests, etc.
```

---

## ✅ Definition of Done

Before marking a feature complete:
- [ ] Code follows project standards
- [ ] Unit tests written (80%+ coverage)
- [ ] Integration tests passed
- [ ] Code reviewed
- [ ] Documentation updated
- [ ] No security vulnerabilities
- [ ] Performance benchmarks met
- [ ] Tested in staging

---

## 💡 Pro Tips

1. **Review Constitution First**: Always check `.specify/memory/constitution.md` before starting
2. **Be Specific**: The more detail you provide, the better the specification
3. **Review Each Phase**: Approve spec before plan, plan before tasks
4. **Update as You Go**: Keep documents current as requirements change
5. **Track Progress**: Update task statuses regularly
6. **Document Decisions**: Record important choices in the relevant docs

---

## 🆘 Common Questions

**Q: When should I create a new specification?**
A: For any new feature, significant change, or API modification

**Q: Can I skip the plan phase?**
A: Not recommended. The plan ensures thorough thinking about implementation

**Q: How detailed should tasks be?**
A: Each task should be completable in a few hours to a day

**Q: What if requirements change?**
A: Update the spec and regenerate the plan/tasks as needed

**Q: Should I version my specs?**
A: Keep them in git; the git history serves as versioning

---

## 📚 Resources

- Full Documentation: `.specify/README.md`
- Constitution: `.specify/memory/constitution.md`
- Templates: `.specify/templates/`
- Your Specs: `.specify/specs/`

---

**Last Updated**: 2026-01-22  
**Version**: 1.0
