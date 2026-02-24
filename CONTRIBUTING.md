# Contributing to Data Pipeline Template

Thank you for your interest in contributing! This guide will help you get started.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [AI Code Review Process](#ai-code-review-process)
- [Code Standards](#code-standards)
- [Testing Requirements](#testing-requirements)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

Please be respectful and constructive in all interactions. We're all here to learn and improve.

## Getting Started

### Prerequisites

- Git
- Docker & Docker Compose
- Python 3.10+
- Make

### Setup Development Environment

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/data-pipeline-template.git
   cd data-pipeline-template
   ```

2. **Start the development environment**
   ```bash
   make up
   sleep 30  # Wait for Airflow to start
   ```

3. **Verify setup**
   ```bash
   make ci
   ```

4. **Access Airflow UI**
   - URL: http://localhost:8080
   - Username: `airflow`
   - Password: `airflow`

## Development Workflow

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write code following our [Code Standards](#code-standards)
   - Add tests for new functionality
   - Update documentation as needed

3. **Test locally**
   ```bash
   make ci
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request**
   - Use the pull request template
   - Wait for AI code review
   - Address feedback

## AI Code Review Process

This repository uses automated AI-powered code review. Here's what to expect:

### What Happens Automatically

When you open a pull request:

1. **AI Code Review** (2-5 min)
   - Centralized AI service analyzes your code changes
   - Posts review comments and suggestions
   - Flags potential issues
   - Uses enterprise-grade AI models

2. **Static Analysis** (1-2 min)
   - Pylint checks code quality
   - Flake8 checks PEP 8 compliance
   - Only runs on changed Python files

3. **Complexity Analysis** (1 min)
   - Measures cyclomatic complexity
   - Calculates maintainability index
   - Reports code metrics

4. **Security Scan** (2-3 min)
   - Trivy scans for vulnerabilities
   - Checks for hardcoded secrets
   - Validates Docker best practices

### Responding to AI Review

**Critical Issues (🚨)** - Must fix before merge:
- Security vulnerabilities
- Hardcoded credentials
- Breaking changes

**Warnings (⚠️)** - Should fix:
- High complexity code
- Debug statements
- Missing error handling
- Code style issues

**Suggestions (💡)** - Consider:
- Refactoring opportunities
- Performance improvements
- Better naming conventions

### If You Disagree

AI is not perfect! If you disagree with a suggestion:
1. Reply to the comment explaining your reasoning
2. Tag a human maintainer for their opinion
3. We'll discuss and make a decision together

### Quick Reference

See [.github/AI_REVIEW_GUIDE.md](.github/AI_REVIEW_GUIDE.md) for detailed information.

## Code Standards

### Python

- **Style**: Follow PEP 8
- **Docstrings**: Use Google-style docstrings
- **Type Hints**: Use type hints for function signatures
- **Line Length**: Max 100 characters
- **Imports**: Use absolute imports, group by standard/third-party/local

**Example:**
```python
from typing import Dict, List
import logging

from airflow import DAG
from airflow.operators.python import PythonOperator


def process_data(data: List[Dict]) -> Dict:
    """Process cryptocurrency data from API.
    
    Args:
        data: List of exchange dictionaries
        
    Returns:
        Processed data dictionary
        
    Raises:
        ValueError: If data is empty or invalid
    """
    if not data:
        raise ValueError("Data cannot be empty")
    
    # Process data
    return {"processed": True, "count": len(data)}
```

### YAML/Configuration Files

- Use 2 spaces for indentation
- Quote string values
- Add comments for complex configurations
- Keep alphabetically sorted when possible

### Dockerfiles

- Pin base image versions (avoid `:latest`)
- Run as non-root user
- Minimize layers
- Add health checks
- Use multi-stage builds when beneficial

### Terraform

- Use meaningful resource names
- Add descriptions to variables
- Tag all resources appropriately
- Use modules for reusability

## Testing Requirements

### Required Tests

All new features must include:

1. **Unit Tests**
   - Test individual functions
   - Mock external dependencies
   - Aim for >80% coverage

2. **DAG Validation Tests**
   - Ensure DAG can be parsed
   - Verify task dependencies
   - Check for cycles

### Running Tests

```bash
# Run all tests
make ci

# Run specific test file
docker exec -it airflow pytest tests/dags/test_your_file.py

# Run with coverage
docker exec -it airflow pytest --cov=dags tests/
```

### Test Structure

```python
import pytest
from dags.your_module import your_function


class TestYourFunction:
    """Test suite for your_function."""
    
    def test_normal_case(self):
        """Test normal execution."""
        result = your_function(valid_input)
        assert result == expected_output
    
    def test_edge_case(self):
        """Test edge case handling."""
        result = your_function(edge_case_input)
        assert result is not None
    
    def test_error_handling(self):
        """Test error conditions."""
        with pytest.raises(ValueError):
            your_function(invalid_input)
```

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks
- **perf**: Performance improvements
- **ci**: CI/CD changes

### Examples

```bash
feat(dags): add crypto price tracking DAG

Add new DAG to track cryptocurrency prices using CoinGecko API.
Includes error handling and data quality checks.

Closes #123

---

fix(coincap): handle missing exchange data

Previously crashed when exchange data was null.
Now logs warning and continues processing.

---

docs(readme): update AI review setup instructions

Add missing steps for OpenAI API key configuration.
```

## Pull Request Process

### Before Opening PR

- [ ] Code follows style guidelines
- [ ] Tests pass locally (`make ci`)
- [ ] Documentation is updated
- [ ] Commits follow commit guidelines
- [ ] No debug statements or TODOs
- [ ] Code is self-reviewed

### Opening PR

1. **Use the PR template**
   - Fill out all sections
   - Link related issues
   - Describe changes clearly

2. **Add appropriate labels**
   - `bug`, `enhancement`, `documentation`, etc.

3. **Request review**
   - AI review runs automatically
   - Tag human reviewers if needed

### During Review

1. **Monitor AI review**
   - Check Actions tab for progress
   - Review comments within 24 hours

2. **Address feedback**
   - Fix critical issues immediately
   - Discuss warnings and suggestions
   - Push updates to trigger re-review

3. **Keep PR updated**
   - Rebase on main if conflicts arise
   - Respond to review comments
   - Mark conversations as resolved

### Merging

PRs can be merged when:
- ✅ All tests pass
- ✅ AI review comments are addressed
- ✅ At least one human approves
- ✅ No merge conflicts
- ✅ Documentation is updated

## Project Structure

```
data-pipeline-template/
├── .github/              # GitHub workflows and templates
│   ├── workflows/        # CI/CD workflows
│   ├── AI_REVIEW_GUIDE.md
│   └── PULL_REQUEST_TEMPLATE.md
├── containers/           # Docker containers
│   └── airflow/         # Airflow container setup
├── dags/                # Airflow DAGs
├── data/                # Sample data
├── terraform/           # Infrastructure as code
├── tests/               # Test files
│   └── dags/           # DAG tests
└── visualization/       # Dashboard code
```

## Common Tasks

### Adding a New DAG

1. Create DAG file in `dags/` directory
2. Add corresponding test in `tests/dags/`
3. Update documentation if needed
4. Test locally with `make ci`

### Updating Dependencies

1. Modify `containers/airflow/requirements.txt`
2. Rebuild containers: `make down && make up`
3. Test thoroughly
4. Document breaking changes

### Modifying Infrastructure

1. Update Terraform files in `terraform/`
2. Test with `terraform plan`
3. Document changes in PR
4. Update README if user-facing

## Getting Help

- 📖 Check existing documentation
- 💬 Open a discussion for questions
- 🐛 Open an issue for bugs
- 📧 Contact maintainers

## Recognition

Contributors will be:
- Listed in release notes
- Credited in documentation
- Thanked in commit messages

Thank you for contributing! 🎉

---

For more information:
- [AI Review Guide](.github/AI_REVIEW_GUIDE.md)
- [Workflow Documentation](.github/workflows/README.md)
- [Main README](README.md)
